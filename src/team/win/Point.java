package team.win;

import java.io.Serializable;

public class Point implements Serializable {
	private static final long serialVersionUID = -7306770469627642246L;

	float mX;
	float mY;
	Point(float x, float y) {
		if(x < 0.0 || y < 0.0 || x > 1.0 || y > 1.0)
			throw new RuntimeException("All fucked");
		mX = x;
		mY = y;
	}
}