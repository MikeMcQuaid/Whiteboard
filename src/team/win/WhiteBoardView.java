package team.win;

import java.util.LinkedList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.view.MotionEvent;
import android.view.View;

public class WhiteBoardView extends View {

	private static final float TOUCH_TOLERANCE = 4;

	private final Paint mPaint = new Paint();
	private final DataStore mDataStore;
	
	private List<Point> mPoints;

	private boolean mPendingEventDown;
	private Timer mEventDownTimer;
	private float mZoomLevel = 2.0f;
	private float mBoardOffsetX = 0.0f;
	private float mBoardOffsetY = 0.0f;
	private float mWidth, mHeight;
	private float mStrokeWidth;
	private float mX, mY;
	private int mColor;
	private String mDrawIp;
	
	private class EventDownTimerTask extends TimerTask {
		private float mX, mY;

		public EventDownTimerTask(float x, float y) {
			mX = x;
			mY = y;
		}
	
		@Override
		public void run() {
			mPendingEventDown = false;
			touchStart(mX, mY);
		}
	}

	public WhiteBoardView(Context context, DataStore ds, int strokeWidth, int color) {
		super(context);
		mDataStore = ds;
		resetPoints();
		initPaintState();
		setPrimColor(color);
		setPrimStrokeWidth(strokeWidth);
		initSize(getResources().getDisplayMetrics().widthPixels,
				 getResources().getDisplayMetrics().heightPixels);
		mStrokeWidth = strokeWidth;
		mColor = color;
	}

	private void initSize(float w, float h) {
        mDataStore.setAspectRatio(w / h);
        mWidth = w;
        mHeight = h;
	}

	private void initPaintState() {
		mPaint.setAntiAlias(true);
		mPaint.setDither(true);
		mPaint.setStyle(Paint.Style.STROKE);
		mPaint.setStrokeJoin(Paint.Join.ROUND);
		mPaint.setStrokeCap(Paint.Cap.ROUND);
	}

	private void resetPoints() {
		mPoints = new LinkedList<Point>();
	}
	
	public void undo() {
		if (mDataStore.size() <= 0)
			return;
		mDataStore.remove(mDataStore.size() - 1);
		invalidate();
	}

	protected void onDraw(Canvas c) {
		Paint background = new Paint();
		background.setColor(Color.WHITE);
		background.setStyle(Paint.Style.FILL);
		c.drawRect(0, 0, mWidth, mHeight, background);

		// Keep this algorithm synchronized with HTML5 canvas paint function
		for (Primitive p : mDataStore.mPrimitiveList) {
			Point firstScreenPoint = worldToScreen(p.mPoints.get(0));
			float firstPointX = firstScreenPoint.mX;
			float firstPointY = firstScreenPoint.mY;
			mPaint.setColor(p.mColor | 0xFF000000);
			mPaint.setStrokeWidth(p.mStrokeWidth * mWidth * mZoomLevel);
			if (p.mPoints.size() > 1) {
				Path path = new Path();
				float lastX = firstPointX;
				float lastY = firstPointY;
				path.moveTo(lastX, lastY);
				for (int i = 1; i < p.mPoints.size() - 1; i++) {
					firstScreenPoint = worldToScreen(p.mPoints.get(i));
					float pointX = firstScreenPoint.mX;
					float pointY = firstScreenPoint.mY;
					path.quadTo(lastX, lastY, (lastX + pointX) / 2, (lastY + pointY) / 2);
					lastX = pointX;
					lastY = pointY;
				}
				c.drawPath(path, mPaint);
			} else {
				c.drawPoint(firstPointX, firstPointY, mPaint);
			}
		}

		if (mDrawIp != null) {
			Paint text = new Paint();
			text.setColor(Color.BLACK);
			text.setTextAlign(Paint.Align.CENTER);
			text.setAntiAlias(true);
			text.setDither(true);
			text.setSubpixelText(true);
			text.setShadowLayer(3, 0, 0, Color.WHITE);
			// TODO: Calculate this sensibly
			text.setTextSize(50);
			c.drawText(mDrawIp, (float)mWidth/2, (float)mHeight/2, text);
		}
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		initSize(w, h);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		float x = event.getX();
		float y = event.getY();

		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			if(mPendingEventDown) {
				toggleZoomLevel(x, y);
				mPendingEventDown = false;
				mEventDownTimer.cancel();
				invalidate();
			} else {
				mPendingEventDown = true;
				mEventDownTimer = new Timer();
				mEventDownTimer.schedule(new EventDownTimerTask(x, y), 150);
			}
			break;
		case MotionEvent.ACTION_MOVE:
			if(!mPendingEventDown)
				touchMove(x, y);
			break;
		case MotionEvent.ACTION_UP:
			break;
		}
		return true;
	}

	private void touchStart(float x, float y) {
		// WTF?
		if(x < 0.0 || y < 0.0 || x > mWidth || y > mHeight)
			return;
		resetPoints();
		mPoints.add(screenToWorld(x, y));
		mDataStore.add(new Primitive(mStrokeWidth / mWidth, mColor, mPoints));
		postInvalidate();
	}

	private void touchMove(float x, float y) {
		// WTF?
		if(x < 0.0 || y < 0.0 || x > mWidth || y > mHeight)
			return;
		float dx = Math.abs(x - mX);
		float dy = Math.abs(y - mY);
		if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
			mPoints.add(screenToWorld(x, y));
			mDataStore.remove(mDataStore.size() - 1);
			mDataStore.add(new Primitive(mStrokeWidth / mWidth, mColor, mPoints));
			mX = x;
			mY = y;
		}
		invalidate();
	}

	protected void setDrawIp(String drawIp) {
		mDrawIp = drawIp;
		invalidate();
	}

	protected void setPrimColor(int c) {
		mColor = c;
	}

	protected void setPrimStrokeWidth(int w) {
		mStrokeWidth = w;
	}

	private void toggleZoomLevel(float x, float y) {
		if(mZoomLevel == 2.0f) {
			// zooming out, don't want offsets
			mBoardOffsetX = 0.0f;
			mBoardOffsetY = 0.0f;
			mZoomLevel = 1.0f;
		} else {
			// x,y are the centre of zoom box; compute top-left
			// corner and rewrite in world coordinates
			x = (x / mWidth) - 0.25f;
			y = (y / mHeight) - 0.25f;

			// clamp to max 0.5f,0.5f (bottom-right quadrant)
			x = Math.max(Math.min(x, 0.5f), 0.0f);
			y = Math.max(Math.min(y, 0.5f), 0.0f);

			mBoardOffsetX = x;
			mBoardOffsetY = y;
			mZoomLevel = 2.0f;
		}
	}

	private Point worldToScreen(Point p) {
		return new Point((p.mX - mBoardOffsetX) * mWidth * mZoomLevel,
						 (p.mY - mBoardOffsetY) * mHeight * mZoomLevel);
	}

	private Point screenToWorld(float x, float y) {
		return new Point(x / mWidth / mZoomLevel + mBoardOffsetX,
						 y / mHeight / mZoomLevel + mBoardOffsetY);
	}
}
