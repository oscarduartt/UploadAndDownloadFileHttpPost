package com.example.videouploaddownload;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

public class HttpFileDownload extends AsyncTask<String, String, File> {

	public ProgressDialog pDialog;
	private Context activity;
	int typeArchivo;

	public HttpFileDownload() {
	}

	public HttpFileDownload(Context activity, int type) {
		this.activity = activity;
		this.typeArchivo = type;
		// dialog = new ProgressDialog(context);
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
	protected File doInBackground(String... aurl) {
		int count;
		File file = null;

		try {
			URL url = new URL(aurl[0]);
			URLConnection conexion = url.openConnection();
			conexion.connect();

			int lenghtOfFile = conexion.getContentLength();
			Log.d("ANDRO_ASYNC", "Lenght of file: " + lenghtOfFile);
			
			if(this.typeArchivo == 2){
				file = new File(Environment
						.getExternalStorageDirectory().getPath()
						+ "/trip_imageishon_chingona.jpeg");
			}else if(this.typeArchivo == 4){
				file = new File(Environment
						.getExternalStorageDirectory().getPath()
						+ "/trip_audeishon_chingon.m4a");
			}else if(this.typeArchivo == 5){
				file = new File(Environment
						.getExternalStorageDirectory().getPath()
						+ "/trip_videoseishon_chingon2.mp4");
			}
			
			InputStream input = new BufferedInputStream(url.openStream());
			OutputStream output = new FileOutputStream(file);

			byte data[] = new byte[1024];

			long total = 0;

			while ((count = input.read(data)) != -1) {
				total += count;
				publishProgress("" + (int) ((total * 100) / lenghtOfFile));
				output.write(data, 0, count);
			}
			pDialog.setIndeterminate(true);

			output.flush();
			output.close();
			input.close();
		} catch (Exception e) {
			Log.w("Exception", e.toString());
		}
		return file;

	}

	protected void onProgressUpdate(String... progress) {
		Log.d("ANDRO_ASYNC", progress[0]);
		pDialog.setProgress(Integer.parseInt(progress[0]));
	}

	@Override
	protected void onPostExecute(File file) {
		pDialog.dismiss();
		
		String path = file.getAbsolutePath();
		
		if(this.typeArchivo == 2){
			Uri nameimagen = Uri.fromFile(file);
			Log.w("pathfromimage", nameimagen.toString());
			Intent intent = new Intent(Intent.ACTION_VIEW);
			intent.setDataAndType(nameimagen, "image/*");
			activity.startActivity(intent);
		}else if(this.typeArchivo == 4){
			Uri nameaudio = Uri.fromFile(file);
			Log.w("pathfromaudio", nameaudio.toString());
			Intent intent = new Intent(android.content.Intent.ACTION_VIEW);
	        intent.setDataAndType(nameaudio, "audio/*");
	        activity.startActivity(intent);
		}else if(this.typeArchivo == 5){
			MainActivity.reproducirVideo(activity, path);
		}
		
	}
}
