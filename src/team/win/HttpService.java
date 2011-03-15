package team.win;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

public class HttpService extends Service {
	
	private static final String TAG = Utils.buildLogTag(HttpService.class);
	
	public static final int PORT_NUMBER = 8080;

	public class HttpServiceBinder extends Binder {
		public HttpService getService() {
			return HttpService.this;
		}
	};
	
	private final IBinder binder = new HttpServiceBinder();
	private final Server server;
	
	private DataStore dataStore;

	public HttpService() {
		server = new Server(PORT_NUMBER);
		server.setHandler(new Handler());
	}
	
	public void setDataStore(DataStore dataStore) {
		this.dataStore = dataStore;
	}
	
	@Override
	public synchronized IBinder onBind(Intent intent) {
		if (server.isRunning()) {
			Log.i(TAG, "Not starting server as it is already running");
			return binder;
		}
			
		try {
			server.start();
			Log.i(TAG, "Started server");
		} catch (Exception e) {
			Log.w(TAG, "Unable to start server", e);
			Toast.makeText(this, "Unable to start server: " + e.getMessage(), 3).show();
		}
		return binder;
	}
	
	@Override
	public synchronized void onDestroy() {
		super.onDestroy();
		try {
			server.stop();
			Log.i(TAG, "Stopped server");
			Toast.makeText(this, R.string.label_stopping_whiteboard, 3).show();
		} catch (Exception e) {
			Log.w(TAG, "Unable to stop server", e);
		}
	}

	private class Handler extends AbstractHandler {
		public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
			try {
				if (target.equals("/")) {
					handleIndex(request, response);
				} else if (target.startsWith("/board.json")) {
					handleBoard(request, response);
				} else {
					Log.w(TAG, "No handler defined for " + target);
					response.setStatus(HttpServletResponse.SC_NOT_FOUND);
				}
			} catch (Exception e) {
				Log.e(TAG, "Failed to handle request for " + target, e);
				handleError(response, e);
			}
			baseRequest.setHandled(true);
		}

		private void handleError(HttpServletResponse response, Exception e)
				throws IOException {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			response.setContentType("text/plain");
			e.printStackTrace(response.getWriter());
		}
		
		private void handleIndex(HttpServletRequest request, HttpServletResponse response) throws IOException {
			response.setStatus(HttpServletResponse.SC_OK);
			response.setContentType("text/html");
			IOUtils.copy(getAssets().open("index.html"), response.getOutputStream());
		}
		
		private void handleBoard(HttpServletRequest request, HttpServletResponse response) throws IOException {
			long cacheTime;
			try {
				cacheTime = Long.parseLong(request.getParameter("cacheTime"));
			}
			catch (NumberFormatException e) {
				cacheTime = -1;
			}

			response.setHeader("Cache-Control", "no-cache");
			if (dataStore != null && cacheTime != dataStore.getCacheTime() ) {
				response.setStatus(HttpServletResponse.SC_OK);
				response.setContentType("application/json");
				response.getWriter().print(dataStore.getAllPrimitivesAsJSON());
			} else {
				response.setStatus(HttpServletResponse.SC_NO_CONTENT);
			}
		}
	}
	
	static Intent makeServiceIntent(Context context) {
		Intent intent = new Intent();
		intent.setClass(context, HttpService.class);
		return intent;
	}
}
