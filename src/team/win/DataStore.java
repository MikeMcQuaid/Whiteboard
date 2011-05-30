package team.win;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.LinkedList;
import java.util.zip.GZIPOutputStream;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class DataStore {
	protected LinkedList<Primitive> mPrimitiveList;
	private float mAspectRatio;
	transient byte[] mJSONCache;
	transient long mJSONCacheTime;

	public DataStore() {
		super();
		mPrimitiveList = new LinkedList<Primitive>();
		mJSONCacheTime = 0;
	}

	public synchronized boolean add(Primitive p) {
		mJSONCacheTime = 0;
		return mPrimitiveList.add(p);
	}

	public synchronized Primitive remove(int index) {
		mJSONCacheTime = 0;
		return mPrimitiveList.remove(index);
	}

	public synchronized int size() {
		return mPrimitiveList.size();
	}

	public synchronized void clear() {
		mJSONCacheTime = 0;
		mPrimitiveList.clear();
	}

	public void setAspectRatio(float aspectRatio) {
		mJSONCacheTime = 0;
		mAspectRatio = aspectRatio;
	}

	public long getCacheTime() {
		return mJSONCacheTime;
	}

	public synchronized void writeAllPrimitivesAsJSONGzipped(OutputStream os) {
		if (mJSONCacheTime == 0) {
			try {
				JSONArray primitives = new JSONArray();
				for (Primitive primitive : mPrimitiveList) {
					JSONArray pointArray = new JSONArray();
					for (Point point : primitive.mPoints) {
						pointArray.put(point.mX);
						pointArray.put(point.mY);
					}
					JSONObject primObject = new JSONObject();
					primObject.put("color", String.format("%06x", primitive.mColor));
					primObject.put("strokeWidth", primitive.mStrokeWidth);
					primObject.put("points", pointArray);
					primitives.put(primObject);
				}
				JSONObject o = new JSONObject();
				o.put("aspectRatio", mAspectRatio);
				o.put("primitives", primitives);
				long time = System.nanoTime();
				o.put("cacheTime", time);

				mJSONCache = o.toString().getBytes();
				mJSONCacheTime = time;
			} catch (JSONException e) {
				throw new RuntimeException(e);
			}
		}

		try {
			GZIPOutputStream gos = new GZIPOutputStream(os);
			gos.write(mJSONCache);
			gos.close();
			os.flush();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public void serializeDataStore(OutputStream outputStream) throws IOException {
		new ObjectOutputStream(outputStream).writeObject(mPrimitiveList);
	}

	@SuppressWarnings("unchecked")
	public synchronized void deserializeDataStore(InputStream inputStream) throws IOException {
		try {
			mPrimitiveList = (LinkedList<Primitive>)new ObjectInputStream(inputStream).readObject();
			System.out.println("Loaded prims");
			for (Primitive p : mPrimitiveList)
				System.out.println(p.toString());
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}
}
