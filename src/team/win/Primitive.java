package team.win;

import java.util.List;

import android.graphics.Paint;
import android.graphics.Point;

public class Primitive {
	int mStrokeWidth;
	int mColor;
	List<Point> mPoints;
	public Primitive(int strokeWidth, int color, List<Point> points) {
		mStrokeWidth = strokeWidth;
		mColor = color & 0x00FFFFFF;
		mPoints = points;
	}
}
