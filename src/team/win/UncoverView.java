package team.win;

import java.nio.IntBuffer;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Cap;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ImageView;

public class UncoverView extends ImageView {

	private static final int BITMAP_SCALE_RATIO = 4;
	Bitmap uncoveredPoints;
	Bitmap coveredPoints;
	int[] pointBuffer;
	int lastTouchX, lastTouchY;
	private boolean uncoverImage;
	
	public UncoverView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public UncoverView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public UncoverView(Context context) {
		super(context);
	}
	
	@Override
	protected void onLayout(boolean changed, int left, int top, int right,
			int bottom) {
		super.onLayout(changed, left, top, right, bottom);
		if( getWidth() == 0 || getHeight() == 0 ) return;
		init();
	}
	
	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		if( getWidth() == 0 || getHeight() == 0 ) return;
		init();
	}

	private void init() {
		uncoveredPoints = Bitmap.createBitmap( (int)( getWidth() / (float) BITMAP_SCALE_RATIO ) + 1, (int)( getHeight() / (float) BITMAP_SCALE_RATIO ) + 1, Config.ARGB_8888);
		coveredPoints = Bitmap.createBitmap( (int)( getWidth() / (float) BITMAP_SCALE_RATIO ) + 1, (int)( getHeight() / (float) BITMAP_SCALE_RATIO ) + 1, Config.ARGB_8888);
		
		// Buffer is probably way larger than we actually need...
		pointBuffer = new int[uncoveredPoints.getRowBytes() * getHeight()];
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		if( !uncoverImage ) {
			return;
		}
		
		Paint paint = new Paint();
		paint.setARGB(255, 255, 255, 255);
		
		IntBuffer buffer = IntBuffer.wrap(pointBuffer);
		uncoveredPoints.copyPixelsToBuffer(buffer);
		for( int i = 0; i < pointBuffer.length; i++ ) {
			pointBuffer[i] = ( pointBuffer[i] ^ -1 ) & 0xF8FFCFCF;
		}
		coveredPoints.copyPixelsFromBuffer(buffer);
		
		Matrix matrix = new Matrix();
		matrix.postScale(BITMAP_SCALE_RATIO, BITMAP_SCALE_RATIO);
		canvas.drawBitmap(coveredPoints, matrix, paint);
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if( event.getAction() == MotionEvent.ACTION_DOWN ) {
			lastTouchX = (int) event.getX();
			lastTouchY = (int) event.getY();
		}
		
		Canvas c = new Canvas(uncoveredPoints);
		Matrix matrix = new Matrix();
		matrix.postScale( 1 / (float) BITMAP_SCALE_RATIO, 1 / (float) BITMAP_SCALE_RATIO );
		c.setMatrix(matrix);
		Paint p = new Paint();
		p.setStrokeWidth(20);
		p.setStrokeCap(Cap.ROUND);
		p.setColor(Color.WHITE);
		c.drawLine( event.getX(), event.getY(), lastTouchX, lastTouchY, p );
		
		lastTouchX = (int) event.getX();
		lastTouchY = (int) event.getY();

		invalidate();
		return true;
	}
	
	public void setUncoverImage( boolean uncoverImage ) {
		this.uncoverImage = uncoverImage;
	}
}
