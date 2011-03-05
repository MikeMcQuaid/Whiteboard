package team.win;

import java.io.Serializable;

public class Point implements Serializable {
	private static final long serialVersionUID = -7306770469627642246L;

	int mX;
	int mY;
	Point(int x, int y) {
		mX = x;
		mY = y;
	}
}