package com.example.videouploaddownload;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;
import android.widget.VideoView;

public class MainActivity extends Activity{

	public static final int MEDIA_TYPE_IMAGE = 1;
	public static final int MEDIA_TYPE_VIDEO = 2;

	private HttpFileDownload DownloadTask;
	private HttpFileUpload UploadTask;
	public static String[] result;
	public static VideoView contentVideo;
	public static ImageButton btnCHingon;
	public int typemsg = 0;
	private Button imgSpeak;
	
	MediaRecorder recorder;
	MediaPlayer player;
	File archivo;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		contentVideo = (VideoView) findViewById(R.id.videoView);
		btnCHingon = (ImageButton) findViewById(R.id.btnCHingon);
		imgSpeak = (Button) findViewById(R.id.btnAudioUpload);
		imgSpeak.setOnTouchListener(new View.OnTouchListener() {
			
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				switch (event.getAction()) {
				case MotionEvent.ACTION_DOWN:
					//tv1.setText("Pulsando");
					grabar();
					break;

				case MotionEvent.ACTION_UP:
					//tv1.setText("Estado");
					detener();
					//reproducir();

				}
				return true;
			}
		});

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		Log.w("content", data.getData().toString());
		if (requestCode == MEDIA_TYPE_VIDEO) {
			if (resultCode == RESULT_OK) {
				String pathOfArchive = getRealPathFromURI(data.getData());
				Log.w("pathfromVideo", pathOfArchive.toString());
				typemsg = 5;
				UploadFile(pathOfArchive, typemsg);
			}

		} else if (resultCode == RESULT_CANCELED) {
			// User cancelled the video capture
			Toast.makeText(this, "Ha candescartado el video",
					Toast.LENGTH_SHORT).show();
		} else {
			// Image capture failed, advise user
			Toast.makeText(this, "Ha ocurrido un error", Toast.LENGTH_SHORT)
					.show();
		}

		if (requestCode == MEDIA_TYPE_IMAGE) {
			if (resultCode == RESULT_OK) {
				String pathOfArchive = getRealPathFromURI(data.getData());
				Log.w("pathfromVideo", pathOfArchive.toString());
				typemsg = 2;
				UploadFile(pathOfArchive, typemsg);
			}

		} else if (resultCode == RESULT_CANCELED) {
			// User cancelled the video capture
			Toast.makeText(this, "Ha candescartado el video",
					Toast.LENGTH_SHORT).show();
		} else {
			// Image capture failed, advise user
			Toast.makeText(this, "Ha ocurrido un error", Toast.LENGTH_SHORT)
					.show();
		}
	}

	public void dispatchTakeVideoIntent(View v) {
		// create new Intent
		Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
		// This value can be 0 for lowest quality and smallest file size or 1
		// for highest quality and larger file size.
		intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 0);
		// limit size 3MB = 3*1048*1048
		intent.putExtra(MediaStore.EXTRA_SIZE_LIMIT, 3294912L);
		// Duration Limit
		intent.putExtra(MediaStore.EXTRA_DURATION_LIMIT, 45);
		// start the Video Capture Intent
		startActivityForResult(intent, MEDIA_TYPE_VIDEO);
	}

	public void GaleriaImg(View v) {
		Intent intent = new Intent(Intent.ACTION_PICK,
				android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
		startActivityForResult(intent, MEDIA_TYPE_IMAGE);

	}

	public void UploadFile(String pathOfArchive, int type) {
		Log.w("Upload", "metodo");
		try {
			// Set your file path here
			String url = String
					.format("http://arws.tripperreality.com/ArDirectory/Message/mediaPostTest?type=%s",
							type);
			Log.w("url", url);
			FileInputStream fstrm = new FileInputStream(pathOfArchive);

			if (UploadTask != null
					&& UploadTask.getStatus() != HttpFileUpload.Status.FINISHED) {
				UploadTask.cancel(true);
			}
			UploadTask = (HttpFileUpload) new HttpFileUpload(MainActivity.this,
					pathOfArchive, url, type, "my file title prueba",
					"my file description prueba").execute(fstrm);

		} catch (FileNotFoundException e) {
			Log.w("FileNotFoundException", e.toString());
		}
	}

	public void downloadAndPlay(View v) {
		String url = "http://arws.tripperreality.com/ARDirectory" + result[1];

		Log.w("�rl", url);

		if (DownloadTask != null
				&& DownloadTask.getStatus() != HttpFileDownload.Status.FINISHED) {
			DownloadTask.cancel(true);
		}
		DownloadTask = (HttpFileDownload) new HttpFileDownload(
				MainActivity.this, this.typemsg).execute(url);

	}

	public String getRealPathFromURI(Uri contentUri) {
		Cursor cursor = null;
		try {
			String[] proj = { MediaStore.Video.Media.DATA };
			cursor = getContentResolver().query(contentUri, proj, null, null,
					null);
			int column_index = cursor
					.getColumnIndexOrThrow(MediaStore.Video.Media.DATA);
			cursor.moveToFirst();
			return cursor.getString(column_index);
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
	}

	public static void reproducirVideo(final Context ctx, final String path) {
		contentVideo.setVideoPath(path);
		contentVideo.requestFocus();
		contentVideo.seekTo(1);

		btnCHingon.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				Uri name = Uri.parse(path);
				Log.w("pathfromimage", name.toString());
				Intent intent = new Intent(Intent.ACTION_VIEW);
				String type = "video/mp4";
				intent.setDataAndType(name, type);
				ctx.startActivity(intent);
			}
		});

	}
	
	public void grabar() {
		recorder = new MediaRecorder();
		recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
		recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
		recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
		MediaPlayer mp = MediaPlayer.create(this, R.raw.beepbeep);
        mp.start();
		File path = new File(Environment.getExternalStorageDirectory()
				.getPath());
		try {
			archivo = File.createTempFile("temporal", ".m4a", path);
		} catch (IOException e) {
		}
		
		recorder.setOutputFile(archivo.getAbsolutePath());
		try {
			recorder.prepare();
		} catch (IOException e) {
		}
		
		recorder.start();
	//	tv1.setText("Grabando");
	//	btnGrabar.setEnabled(false);
	//	btnDetener.setEnabled(true);
	}

	public void detener() {
		recorder.stop();
		recorder.release();
		player = new MediaPlayer();
		//player.setOnCompletionListener(this);
		try {
			player.setDataSource(archivo.getAbsolutePath());
		} catch (IOException e) {
		}
		try {
			player.prepare();
		} catch (IOException e) {
		}
		typemsg = 4;
		String path = archivo.getAbsolutePath();
		UploadFile(path, typemsg);
		//btnGrabar.setEnabled(true);
		//btnDetener.setEnabled(false);
		//btnReproducir.setEnabled(true);
		//tv1.setText("Listo para reproducir");
	}

}
