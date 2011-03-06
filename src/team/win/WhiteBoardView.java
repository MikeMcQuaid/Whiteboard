package team.win;

import java.util.LinkedList;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.Region;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

public class WhiteBoardView extends View {

	private DataStore mDataStore;
	private int mColor;
	private int mStrokeWidth;
	
	private List<Point> points;
	private HttpService httpService;
	private boolean needsRedraw = false;
	
	private float mX, mY;
	private static final float TOUCH_TOLERANCE = 4;

	public WhiteBoardView(Context context, DataStore ds, int strokeWidth, int color) {
		super(context);
		int w = getResources().getDisplayMetrics().widthPixels;
		int h = getResources().getDisplayMetrics().widthPixels;
		mDataStore = ds;
		resetPoints();
		setPrimColor(color);
		setPrimStrokeWidth(strokeWidth);
        //mDataStore.setWidth(w);
        //mDataStore.setHeight(h);
	}
	
	public void setNeedsRedraw()
	{
		if (mDataStore.size() > 0) {
			needsRedraw = true;
			invalidate();
		}
	}

	public void setHttpService(HttpService httpService) {
		this.httpService = httpService;
	}

	protected void onDraw(Canvas c) {

		if (mDataStore.size() == 0)
		{
			needsRedraw = false;
			return;
		}
		
		if (needsRedraw)
		{
			mDataStore.remove(mDataStore.size() - 1);
			Paint temp = new Paint();
			temp.setColor(Color.BLACK);
			temp.setStyle(Paint.Style.FILL);
			//c.clipRect(0,0,480, 800, Region.Op.REPLACE);
			c.drawRect(0,0,480, 800, temp);
			needsRedraw = false;
		}
	
		
		for (Primitive p: mDataStore.mPrimitiveList) {
			Paint paint = new Paint();
			paint.setAntiAlias(true);
			paint.setDither(true);
			paint.setStyle(Paint.Style.STROKE);
			paint.setStrokeJoin(Paint.Join.ROUND);
			paint.setStrokeCap(Paint.Cap.ROUND);
			paint.setColor(Color.RED);
			paint.setStrokeWidth(15);
			Log.i("Loop", String.format("%d", paint.getColor()));
			Path path = new Path();
			Point[] points = p.mPoints.toArray(new Point[0]);
			float x = points[0].x;
			float y = points[0].y;
			path.moveTo(x, y);
			mX = x;
			mY = y;
			for (int i = 1; i < points.length-1; i++) {
				x = points[i].x;
				y = points[i].y;
				float dx = Math.abs(x - mX);
				float dy = Math.abs(y - mY);
				path.quadTo(mX, mY, (x + mX) / 2, (y + mY) / 2);
				mX = x;
				mY = y;
			}
			c.drawPath(path, paint);
			needsRedraw = false;
		}
	}
	
	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
        //mDataStore.setWidth(w);
        //mDataStore.setHeight(h);
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
		points.add(new Point((int) x, (int) y));
		mDataStore.add(new Primitive((int)mStrokeWidth, mColor, points));

	}

	private void touchMove(float x, float y) {
		float dx = Math.abs(x - mX);
		float dy = Math.abs(y - mY);
		if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
			points.add(new Point((int) x, (int) y));
			mDataStore.remove(mDataStore.size() - 1);
			mDataStore.add(new Primitive((int)mStrokeWidth, mColor, points));
			if (httpService != null) {
				httpService.setDataStore(mDataStore);
			}
		}
	}

	public void resetPoints() {
		points = new LinkedList<Point>();
	}
	
	public DataStore getDataStore()
	{
		return this.mDataStore;
	}

	public void setPrimColor(int c) {
		mColor = c;
	}

	protected void setPrimStrokeWidth(int c) {
		mStrokeWidth = c;
	}
}