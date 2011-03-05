package team.win;

import java.util.LinkedList;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.Region;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

public class WhiteBoardView extends View {

	private final DataStore mDataStore;
	
	private Bitmap bitmap;
	private Canvas canvas;
	private Paint paint = new Paint();
	private Path path = new Path();
	private List<Point> points;
	private HttpService httpService;

	private float mX, mY;
	private static final float TOUCH_TOLERANCE = 4;
	
	public void defaultState() {
		paint.setAntiAlias(true);
		paint.setDither(true);
		paint.setStyle(Paint.Style.STROKE);
		paint.setStrokeJoin(Paint.Join.ROUND);
		paint.setStrokeCap(Paint.Cap.ROUND);
	}
	
	/**
	 * Initialise the WhiteBoard state
	 * @param context ???
	 * @param ds datastore object used to communicate with HttpServer
	 */
	public WhiteBoardView(Context context, DataStore ds, int strokeWidth, int color) {
		super(context);
		bitmap = Bitmap.createBitmap(getResources().getDisplayMetrics().widthPixels, getResources().getDisplayMetrics().widthPixels, Bitmap.Config.ARGB_8888);
		canvas = new Canvas(bitmap);
		Log.i("WidthHeight", String.format("%d %d", getResources().getDisplayMetrics().widthPixels, getResources().getDisplayMetrics().heightPixels));
		Log.i("ClipBounds", canvas.getClipBounds().toShortString());
		mDataStore = ds;
		resetPoints();
		defaultState();
		setPrimColor(color);
		setPrimStrokeWidth(strokeWidth);
	}
	
	public void setHttpService(HttpService httpService) {
		this.httpService = httpService;
	}

	protected void onDraw(Canvas c) {
		c.drawColor(0xFFAAAAAA);
		c.drawBitmap(bitmap, 0, 0, paint);
		c.drawPath(path, paint);
	}
	
	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        canvas = new Canvas(bitmap);
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
			touchUp();
			invalidate();
			break;
		}
		return true;
	}
	
	private void touchStart(float x, float y) {
		resetPoints();
		points.add(new Point((int) x, (int) y));
		mDataStore.add(new Primitive(paint, points));
		
		Log.i("Move", String.format("mouse_down detected at (%f.0, %f.0)", x, y));
		points.add(new Point((int) x, (int) y));
		path.moveTo(x, y);
		mX = x;
		mY = y;
	}

	private void touchMove(float x, float y) {
		points.add(new Point((int) x, (int) y));
		mDataStore.remove(mDataStore.size() - 1);
		mDataStore.add(new Primitive(paint, points));
		
		Log.i("Move", String.format("mouse_down detected at (%f.0, %f.0)", x, y));
		points.add(new Point((int) x, (int) y));
		float dx = Math.abs(x - mX);
		float dy = Math.abs(y - mY);
		if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
			path.quadTo(mX, mY, (x + mX) / 2, (y + mY) / 2);
			mX = x;
			mY = y;
		}
		if (httpService != null) {
			httpService.setDataStore(mDataStore);
		}
	}

	private void touchUp() {
		path.lineTo(mX, mY);
		canvas.drawPath(path, paint);
		path.reset();
		mDataStore.add(new Primitive(paint, points));
		resetPoints();
	}
	
	public void resetPoints() {
		points = new LinkedList<Point>();
	}
	
	public void resetState() {
		// FIXME: Remove these hacks when we can configure
		paint.setAntiAlias(true);
		paint.setDither(true);
		paint.setColor(0xFFFF0000);
		paint.setStyle(Paint.Style.STROKE);
		paint.setStrokeJoin(Paint.Join.ROUND);
		paint.setStrokeCap(Paint.Cap.ROUND);
		paint.setStrokeWidth(12);
	}

	protected void setPrimColor(int c) {
		paint.setColor(c);
	}

	protected void setPrimStrokeWidth(int c) {
		paint.setStrokeWidth(c);
	}
}
