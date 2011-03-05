package team.win;

import java.util.LinkedList;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
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
	private float mWidth, mHeight;
	private static final float TOUCH_TOLERANCE = 4;

	public WhiteBoardView(Context context, DataStore ds, int strokeWidth, int color) {
		super(context);
		mDataStore = ds;
		resetPoints();
		initPaintState();
		setPrimColor(color);
		setPrimStrokeWidth(strokeWidth);
		initSize(getResources().getDisplayMetrics().widthPixels,
				 getResources().getDisplayMetrics().heightPixels);
		Log.i("ClipBounds", canvas.getClipBounds().toShortString());
	}

	private void initSize(float w, float h) {
		bitmap = Bitmap.createBitmap((int)w, (int)h, Bitmap.Config.RGB_565);
        canvas = new Canvas(bitmap);
        mDataStore.setAspectRatio(w / h);
        mWidth = w;
        mHeight = h;
	}

	private void initPaintState() {
		paint.setAntiAlias(true);
		paint.setDither(true);
		paint.setStyle(Paint.Style.STROKE);
		paint.setStrokeJoin(Paint.Join.ROUND);
		paint.setStrokeCap(Paint.Cap.ROUND);
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
			touchUp();
			invalidate();
			break;
		}
		return true;
	}
	
	private void touchStart(float x, float y) {
		resetPoints();
		points.add(new Point(x / mWidth, y / mHeight));
		mDataStore.add(new Primitive(paint.getStrokeWidth() / mWidth, paint.getColor(), points));
		
		Log.i("Move", String.format("mouse_down detected at (%f.0, %f.0)", x, y));
		path.moveTo(x, y);
		mX = x;
		mY = y;
	}

	private void touchMove(float x, float y) {
		points.add(new Point(x / mWidth, y / mHeight));
		mDataStore.remove(mDataStore.size() - 1);
		mDataStore.add(new Primitive(paint.getStrokeWidth() / mWidth, paint.getColor(), points));
		if (httpService != null) {
			httpService.setDataStore(mDataStore);
		}

		Log.i("Move", String.format("mouse_down detected at (%f.0, %f.0)", x, y));
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
		resetPoints();
		path.reset();
	}

	public void resetPoints() {
		points = new LinkedList<Point>();
	}
	
	public Paint getPaint() {
		return this.paint;
	}

	public void setPrimColor(int c) {
		paint.setColor(c);
	}

	protected void setPrimStrokeWidth(int c) {
		paint.setStrokeWidth(c);
	}
}
