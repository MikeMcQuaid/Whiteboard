package team.win;

import java.util.LinkedList;
import java.util.List;

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
	private HttpService mHttpService;

	private float mWidth, mHeight;
	private float mStrokeWidth;
	private float mX, mY;
	private int mColor;
	
	private boolean needsRedraw;

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

	public void setHttpService(HttpService httpService) {
		this.mHttpService = httpService;
	}
	
	public void setNeedsRedraw() {
		if (mDataStore.size() <= 0)
			return;
		needsRedraw = true;
		mDataStore.remove(mDataStore.size() - 1);
		invalidate();
	}

	protected void onDraw(Canvas c) {
//		if (needsRedraw) {
			// mDataStore.remove(mDataStore.size() - 1);
			Paint temp = new Paint();
			temp.setColor(Color.WHITE);
			temp.setStyle(Paint.Style.FILL);
			c.drawRect(0, 0, mWidth, mHeight, temp);
//		}

		for (Primitive p : mDataStore.mPrimitiveList) {
			mPaint.setColor(p.mColor | 0xFF000000);
			mPaint.setStrokeWidth(p.mStrokeWidth * mWidth);
			Path path = new Path();
			Point[] points = p.mPoints.toArray(new Point[0]);
			float pX, pY;
			float lX = points[0].mX * mWidth;
			float lY = points[0].mY * mHeight;
			path.moveTo(lX, lY);
			for (int i = 1; i < points.length - 1; i++) {
				pX = points[i].mX * mWidth;
				pY = points[i].mY * mHeight;
				path.lineTo(pX, pY);
				lX = pX;
				lY = pX;
			}
			c.drawPath(path, mPaint);
		}

		needsRedraw = false;
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
			touchStart(x, y);
			invalidate();
			break;
		case MotionEvent.ACTION_MOVE:
			touchMove(x, y);
			invalidate();
			break;
		case MotionEvent.ACTION_UP:
			invalidate();
			break;
		}
		return true;
	}
	
	private void touchStart(float x, float y) {
		resetPoints();
		mPoints.add(new Point(x / mWidth, y / mHeight));
		mDataStore.add(new Primitive(mStrokeWidth / mWidth, mColor, mPoints));
	}

	private void touchMove(float x, float y) {

		float dx = Math.abs(x - mX);
		float dy = Math.abs(y - mY);
		if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
			mPoints.add(new Point(x / mWidth, y / mHeight));
			mDataStore.remove(mDataStore.size() - 1);
			mDataStore.add(new Primitive(mStrokeWidth / mWidth, mColor, mPoints));
			if (mHttpService != null) {
				mHttpService.setDataStore(mDataStore);
			}
			mX = x;
			mY = y;
		}
	}

	public void resetPoints() {
		mPoints = new LinkedList<Point>();
	}

	protected void setPrimColor(int c) {
		mColor = c;
	}

	protected void setPrimStrokeWidth(int w) {
		mStrokeWidth = w;
	}
}
