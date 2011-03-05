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
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

public class HttpService extends Service {
	
	private final IBinder binder = new HttpServiceBinder();
	
	private DataStore dataStore;
	private Server server;
	
	public void setDataStore(DataStore dataStore) {
		this.dataStore = dataStore;
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		return binder;
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		super.onStartCommand(intent, flags, startId);
		
		server = new Server(8080);
		server.setHandler(new Handler());
		
		try {
			server.start();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		
		return START_STICKY;
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		
		try {
			server.stop();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	private class Handler extends AbstractHandler {
		public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
			try {
				if (target.equals("/")) {
					handleIndex(request, response);
				} else if (target.startsWith("/time")) {
					handleTime(request, response);
				} else if (target.startsWith("/board.json")) {
					handleBoard(request, response);
				} else {
					response.setStatus(HttpServletResponse.SC_NOT_FOUND);
				}
			} catch (Exception e) {
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
		
		private void handleTime(HttpServletRequest request, HttpServletResponse response) throws IOException {
			response.setStatus(HttpServletResponse.SC_OK);
			response.setContentType("application/json");
			response.getOutputStream().println(System.currentTimeMillis());
		}
		
		private void handleBoard(HttpServletRequest request, HttpServletResponse response) throws IOException {
			if (dataStore != null) {
				response.setStatus(HttpServletResponse.SC_OK);
				response.setContentType("application/json");
				response.getWriter().print(dataStore.getAllPrimitivesAsJSON());
			} else {
				response.setStatus(HttpServletResponse.SC_NO_CONTENT);
			}
		}
	}
	
	public class HttpServiceBinder extends Binder {
		public HttpService getService() {
			return HttpService.this;
		}
	};
}
