package team.win;

import java.util.LinkedList;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

public class WhiteBoardView extends View {
    
	private Bitmap bitmap;
    private Canvas canvas;
    private List<Paint> paints;
    private List<Path> paths;
    private Paint currentPaint;
    private Path currentPath;
	private DataStore mDataStore;
    
    private float mX, mY;
    private static final float TOUCH_TOLERANCE = 4;
    
    public Paint paintBuilder() {
    	Paint paint = new Paint(Paint.DITHER_FLAG);
        paint.setAntiAlias(true);
        paint.setDither(true);
        paint.setColor(0xFFFF0000);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStrokeWidth(12);
        return paint;
    }
    
	public WhiteBoardView(Context context, DataStore ds) {
		super(context);
		bitmap = Bitmap.createBitmap(320, 480, Bitmap.Config.ARGB_8888);
        canvas = new Canvas(bitmap);
        currentPath = new Path();
        currentPaint = paintBuilder();
        paints = new LinkedList<Paint>();
        paths = new LinkedList<Path>();
		mDataStore = ds;
	}
	
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
    }
    
    private void touch_start(float x, float y) {
    	Log.i("Move", String.format("mouse_down detected at (%f.0, %f.0)", x, y));
        currentPath.reset();
        currentPath.moveTo(x, y);
        mX = x;
        mY = y;
    }
    private void touch_move(float x, float y) {
    	Log.i("Move", String.format("mouse_down detected at (%f.0, %f.0)", x, y));
        float dx = Math.abs(x - mX);
        float dy = Math.abs(y - mY);
        if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
            currentPath.quadTo(mX, mY, (x + mX)/2, (y + mY)/2);
            mX = x;
            mY = y;
        }
    }
    private void touch_up() {
        currentPath.lineTo(mX, mY);
        // commit the path to our offscreen
        canvas.drawPath(currentPath, currentPaint);
        paints.add(currentPaint);
        paths.add(currentPath);
        // kill this so we don't double draw
        currentPath = new Path();
        currentPaint = paintBuilder();
        
        Log.i("Paints", String.format("%d", paints.size()));
        Log.i("Paths", String.format("%d", paths.size()));
    }
    
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();
        
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                touch_start(x, y);
                invalidate();
                break;
            case MotionEvent.ACTION_MOVE:
                touch_move(x, y);
                invalidate();
                break;
            case MotionEvent.ACTION_UP:
                touch_up();
                invalidate();
                break;
        }
        return true;
    }
	
	protected void onDraw(Canvas c) {
        c.drawColor(0xFFAAAAAA);
        
        c.drawBitmap(bitmap, 0, 0, currentPaint);
        
        c.drawPath(currentPath, currentPaint);
	}
}
