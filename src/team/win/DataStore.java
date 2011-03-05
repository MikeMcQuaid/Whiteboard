package team.win;

import java.util.LinkedList;

import org.json.JSONException;
import org.json.JSONStringer;

import android.graphics.Paint;

public class DataStore {
	private LinkedList<Paint> mPrimitiveList;

	public DataStore() {
		super();
	}

	public boolean add(Paint p) {
		return mPrimitiveList.add(p);
	}

	public String getAllPrimitivesAsJSON() throws JSONException {
		JSONStringer s = new JSONStringer();
		s.object();
		s.key("width");
		s.value(800);
		s.key("height");
		s.value(480);
		s.array();
		for (Paint p : mPrimitiveList) {
			s.key("color");
			s.value(p.getColor());
			s.key("strokeWidth");
			s.value(p.getStrokeWidth());
			s.array();
			// FIXME: ADD PATH
			s.endArray();
		}
		s.endArray();
		s.endObject();
		return s.toString();
	}
}