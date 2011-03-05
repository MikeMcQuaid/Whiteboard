package team.win;

import java.util.LinkedList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.graphics.Point;

public class DataStore {
	private List<Primitive> mPrimitiveList;

	public DataStore() {
		super();
		mPrimitiveList = new LinkedList<Primitive>();
	}

	public boolean add(Primitive p) {
		return mPrimitiveList.add(p);
	}

	public String getAllPrimitivesAsJSON() throws JSONException {
		JSONArray primitives = new JSONArray();
		for (Primitive primitive : mPrimitiveList) {
			JSONArray pointArray = new JSONArray();
			for (Point point : primitive.mPoints) {
				pointArray.put(point.x);
				pointArray.put(point.y);
			}
			JSONObject primObject = new JSONObject();
			primObject.put("color", "" + Integer.toHexString(primitive.mPaint.getColor()));
			primObject.put("strokeWidth", primitive.mPaint.getStrokeWidth());
			primObject.put("points", pointArray);
			primitives.put(primObject);
		}
		JSONObject o = new JSONObject();
		o.put("width", 800);
		o.put("height", 480);
		o.put("primitives", primitives);
		return o.toString();
	}
}