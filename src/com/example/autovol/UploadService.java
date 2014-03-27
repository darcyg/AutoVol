package com.example.autovol;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import android.app.IntentService;
import android.content.Intent;

import com.autovol.ml.CurrentStateListener;

public class UploadService extends IntentService {
	
	private static final String UPLOAD_URL = MainActivity.BASE_URL +
			"/AutoVolWeb/DataUploadServlet";
	private static final int MAX_BUFFER_SIZE = 1*1024*1024;
	
	public UploadService(String str) {
		super(str);
	}

	/**
	 * Uploads data to DataUploadServlet
	 * (async since IntentService)
	 */
	@Override
	protected void onHandleIntent(Intent intent) {
		HttpURLConnection conn = null;
		FileInputStream fileInput = null;
		DataOutputStream outputStream = null;

		int bytesRead, bytesAvailable, bufferSize;
		byte[] buffer;
		try {
			fileInput = openFileInput(CurrentStateListener.SAVED_FILE);

			URL url = new URL(UPLOAD_URL);
			conn = (HttpURLConnection) url.openConnection();
			conn.setDoOutput(true);
			conn.setChunkedStreamingMode(0);
			conn.setRequestProperty("Connection", "Keep-Alive");

			outputStream = new DataOutputStream(conn.getOutputStream());

			bytesAvailable = fileInput.available();
			bufferSize = Math.min(bytesAvailable, MAX_BUFFER_SIZE);
			buffer = new byte[bufferSize];

			bytesRead = fileInput.read(buffer, 0, bufferSize);
			while (bytesRead > 0) {
				outputStream.write(buffer);
				bytesAvailable = fileInput.available();
				bufferSize = Math.min(bytesAvailable, MAX_BUFFER_SIZE);
				bytesRead = fileInput.read(buffer, 0, bufferSize);
			}
			int serverResponseCode = conn.getResponseCode();
			fileInput.close();
			if (serverResponseCode == HttpURLConnection.HTTP_ACCEPTED) {
				File savedFile = new File(getFilesDir(), CurrentStateListener.SAVED_FILE);
				savedFile.delete();
			}
			outputStream.flush();
			outputStream.close();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
}
