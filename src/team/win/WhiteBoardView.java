package team.win;

import java.util.LinkedList;
import java.util.List;

import org.json.JSONException;

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

	private Bitmap bitmap;
	private Canvas canvas;
	private Paint paint = new Paint();
	private Path path = new Path();
	private List<Point> points;
	private DataStore mDataStore;

	private float mX, mY;
	private static final float TOUCH_TOLERANCE = 4;

	public void resetPoints() {
		points = new LinkedList<Point>();
	}
	
	public void resetState() {
		//paint.reset();
		//path.reset();

		// FIXME: Remove these hacks when we can configure
		paint.setAntiAlias(true);
		paint.setDither(true);
		paint.setColor(0xFFFF0000);
		paint.setStyle(Paint.Style.STROKE);
		paint.setStrokeJoin(Paint.Join.ROUND);
		paint.setStrokeCap(Paint.Cap.ROUND);
		paint.setStrokeWidth(12);
	}

	public WhiteBoardView(Context context, DataStore ds) {
		super(context);
		bitmap = Bitmap.createBitmap(getResources().getDisplayMetrics().widthPixels, getResources().getDisplayMetrics().widthPixels, Bitmap.Config.ARGB_8888);
		canvas = new Canvas(bitmap);
		Log.i("WidthHeight", String.format("%d %d", getResources().getDisplayMetrics().widthPixels, getResources().getDisplayMetrics().heightPixels));
		Log.i("ClipBounds", canvas.getClipBounds().toShortString());
		mDataStore = ds;
		resetPoints();
		resetState();
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
	}

	private void touchStart(float x, float y) {
		resetPoints();
		Log.i("Move", String.format("mouse_down detected at (%f.0, %f.0)", x, y));
		points.add(new Point((int) x, (int) y));
		path.moveTo(x, y);
		mX = x;
		mY = y;
	}

	private void touchMove(float x, float y) {
		Log.i("Move", String.format("mouse_down detected at (%f.0, %f.0)", x, y));
		points.add(new Point((int) x, (int) y));
		float dx = Math.abs(x - mX);
		float dy = Math.abs(y - mY);
		if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
			path.quadTo(mX, mY, (x + mX) / 2, (y + mY) / 2);
			mX = x;
			mY = y;
		}
	}

	private void touchUp() {
		path.lineTo(mX, mY);
		mDataStore.add(new Primitive(paint, points));
		try {
			System.out.println(mDataStore.getAllPrimitivesAsJSON());
		} catch (JSONException e) {
			e.printStackTrace();
		}
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

	protected void onDraw(Canvas c) {
		c.drawColor(0xFFAAAAAA);
		c.drawBitmap(bitmap, 0, 0, paint);
		c.drawPath(path, paint);
	}
}
