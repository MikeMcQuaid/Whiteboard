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
import android.os.IBinder;

public class HttpService extends Service {
	
	private Server server;

	@Override
	public IBinder onBind(Intent arg0) {
		return null; // clients cannot bind to this service
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
		
		@Override
		public void handle(String target, Request baseRequest, HttpServletRequest request,
				HttpServletResponse response) throws IOException, ServletException {
			if (target.equals("/")) {
				handleIndex(request, response);
			} else if (target.startsWith("/time")) {
				handleTime(request, response);
			} else {
				response.setStatus(HttpServletResponse.SC_NOT_FOUND);
			}
			baseRequest.setHandled(true);
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
		
	}
	
}
