package com.example.videouploaddownload;


import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;

public class HttpFileUpload extends AsyncTask<FileInputStream, String, Void> {
	
	URL connectURL;
	String responseString;
	File Archive;
	String Title;
	String Description;
	int type;
	byte[] dataToServer;
	FileInputStream fileInputStream = null;
	
	String[] respuestaServer = new String[2]; 

	public ProgressDialog pDialog;
	private Context activity;
	
	private Handler bridge;

	public HttpFileUpload() {
	}

	public HttpFileUpload(Context activity, String pathOfArchive, String urlString, int type, String vTitle, String vDesc) {
		this.activity = activity;
		try {
			this.connectURL = new URL(urlString);
			this.Archive = new File(pathOfArchive);
			this.Title = vTitle;
			this.Description = vDesc;
			this.type = type;
			Log.w("url", urlString);
		} catch (Exception ex) {
			Log.i("HttpFileUpload", "URL Malformatted");
		}
		this.bridge = new Handler();
		
	}

	@Override
	protected void onPreExecute() {
		super.onPreExecute();
		pDialog = new ProgressDialog(activity);
		pDialog.setMessage("Downloading file..");
		pDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		pDialog.setIndeterminate(true);
		pDialog.setCancelable(false);
		pDialog.show();
	}

	@Override
	protected Void doInBackground(FileInputStream... fstrm) {
		
		fileInputStream = fstrm[0];
		
		String filevideo = "trip_temp_vid.mp4";
		String fileimage = "trip_temp_vid.jpg";
		String fileaudio = "trip_temp_vid.m4a";
		String contentType = null;
		String lineEnd = "\r\n";
		String twoHyphens = "--";
		String boundary = "*****";
		String Tag = "fSnd";
		final long fileSize = this.Archive.length();
		//String[] respuestaArray = new String[4];
		try {
			Log.e(Tag, "Starting Http File Sending to URL");

			// Open a HTTP connection to the URL
			HttpURLConnection conn = (HttpURLConnection) connectURL
					.openConnection();

			// Allow Inputs
			conn.setDoInput(true);

			// Allow Outputs
			conn.setDoOutput(true);

			// Don't use a cached copy.
			conn.setUseCaches(false);

			// Use a post method.
			conn.setRequestMethod("POST");

			conn.setRequestProperty("Connection", "Keep-Alive");

			conn.setRequestProperty("Content-Type",
					"multipart/form-data;boundary=" + boundary);

			DataOutputStream dos = new DataOutputStream(conn.getOutputStream());

			dos.writeBytes(twoHyphens + boundary + lineEnd);
			/*dos.writeBytes("Content-Disposition: form-data; name=\"title\""
					+ lineEnd);
			dos.writeBytes(lineEnd);
			dos.writeBytes(Title);
			dos.writeBytes(lineEnd);
			dos.writeBytes(twoHyphens + boundary + lineEnd);

			dos.writeBytes("Content-Disposition: form-data; name=\"description\""
					+ lineEnd);
			dos.writeBytes(lineEnd);
			dos.writeBytes(Description);
			dos.writeBytes(lineEnd);
			dos.writeBytes(twoHyphens + boundary + lineEnd);*/
			
			String archivotmp = null;
			if(type == 2){
				archivotmp = fileimage;
				contentType = "image/*";
			}else if(type == 4){
				archivotmp = fileaudio;
				contentType = "audio/*";
			}else if(type == 5){
				archivotmp = filevideo;
				contentType = "video/*";
			}
			

			dos.writeBytes("Content-Disposition: form-data; name=\"uploadedfile\";filename=\""
					+ archivotmp + "\"" + lineEnd);
			dos.writeBytes("Content-Type: " + contentType + lineEnd);
			dos.writeBytes("Content-Transfer-Encoding: 64bit" + lineEnd);

			dos.writeBytes(lineEnd);

			Log.e(Tag, "Headers are written");

			// create a buffer of maximum size
			int bytesAvailable = fileInputStream.available();

			int maxBufferSize = 1024;
			int bufferSize = Math.min(bytesAvailable, maxBufferSize);
			byte[] buffer = new byte[bufferSize];
			int sentBytes = 0;

			// read file and write it into form...
			int bytesRead = fileInputStream.read(buffer, 0, bufferSize);

			while (bytesRead > 0) {
				this.bridge.post(new Runnable() {						
					@Override
					public void run() {
						// TODO Auto-generated method stub
						pDialog.setIndeterminate(false);
						 // Update progress dialog       
					}
				});
						
				dos.write(buffer, 0, bufferSize);
				bytesAvailable = fileInputStream.available();
				bufferSize = Math.min(bytesAvailable, maxBufferSize);
				bytesRead = fileInputStream.read(buffer, 0, bufferSize);
				
				sentBytes += bytesRead;
				publishProgress(""+(int)(sentBytes * 100 / fileSize));
				
				this.bridge.post(new Runnable() {						
					@Override
					public void run() {
						// TODO Auto-generated method stub
						pDialog.setIndeterminate(true);
						pDialog.setProgress(100);
						pDialog.setMessage("Save file..");
					}
				});
				
			}
			
			
			dos.writeBytes(lineEnd);
			dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

			// close streams
			fileInputStream.close();

			dos.flush();

			Log.e(Tag,"File Sent, Response: "
							+ String.valueOf(conn.getResponseCode()));

			InputStream is = conn.getInputStream();

			// retrieve the response from server
			int ch;

			StringBuffer b = new StringBuffer();
			while ((ch = is.read()) != -1) {
				b.append((char) ch);
			}
			String s = b.toString();
			Log.i("Response", s);

			dos.close();
			
			

			// DECODIFICACION JSON
			try {
				JSONObject respuestaJSON = (new JSONObject(s));
				String status = respuestaJSON.getString("status");
				String url = respuestaJSON.getString("url");
				//String title = respuestaJSON.getString("title");
				//String description = respuestaJSON.getString("description");
				
				
				Log.w("status", status.toString());
				Log.w("url", url.toString());
				//Log.w("title", title.toString());
				//Log.w("description", description.toString());

				this.respuestaServer[0] = status;
				this.respuestaServer[1] = url;
				//this.respuestaServer[2] = title;
				//this.respuestaServer[3] = description;
				
				MainActivity.result = this.respuestaServer;
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			

		} catch (MalformedURLException ex) {
			Log.e(Tag, "URL error: " + ex.getMessage(), ex);
		}

		catch (IOException ioe) {
			Log.e(Tag, "IO error: " + ioe.getMessage(), ioe);
		}		
		
		return null;
		

	}

	protected void onProgressUpdate(String... progress) {
		Log.d("ANDRO_ASYNC", progress[0]);
		pDialog.setProgress(Integer.parseInt(progress[0]));
	}

	@Override
	protected void onPostExecute(Void file) {
		pDialog.dismiss();
		
	}
	
}
