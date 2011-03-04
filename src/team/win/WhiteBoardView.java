package team.win;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.view.MotionEvent;
import android.view.View;

public class WhiteBoardView extends View {
	public WhiteBoardView(Context context) {
		super(context);
	}

	public boolean onTouchEvent(MotionEvent e) {
		return false;
	}
	
	protected void onDraw(Canvas c) {
		Paint paint = new Paint();
		paint.setColor(Color.rgb(255, 255, 255));
		c.drawArc(new RectF(0.0f, 0.0f, 200.0f, 200.0f), 0.0f, 0.0f, true, paint);
	}
}
