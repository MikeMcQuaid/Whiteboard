package team.win;

import java.util.LinkedList;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
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

	public void resetState() {
		paint.reset();
		path.reset();
		points = new LinkedList<Point>();

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
		bitmap = Bitmap.createBitmap(320, 480, Bitmap.Config.ARGB_8888);
		canvas = new Canvas(bitmap);
		mDataStore = ds;
		resetState();
	}
	
	public void setHttpService(HttpService httpService) {
		this.httpService = httpService;
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
	}

	private void touchStart(float x, float y) {
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
		canvas.drawPath(path, paint);
		mDataStore.add(new Primitive(paint, points));
		System.out.println(mDataStore.getAllPrimitivesAsJSON());
		if (httpService != null) {
			httpService.setDataStore(mDataStore);
		}
		resetState();
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
