package team.win;

import java.io.Serializable;

public class Point implements Serializable {
	private static final long serialVersionUID = -7306770469627642246L;

	float mX;
	float mY;
	Point(float x, float y) {
		mX = x;
		mY = y;
	}
}