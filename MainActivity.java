/**
 * Impaired Driver Evidence Collector (IDEC) Application
 * Programmers: Hayden Lowes, Bryce Drew
 * Special thanks to various online android development tutorials
 * Date of Submission: November. 24, 2014
 * Project state: MainActivity (Camera and Main Menu portion) Compiles successfully,
 * however the UploadToServer activity is having trouble recognizing its
 * associated layout activity_upload_to_server
 * 
 * Additional Notes:  All Log function statements are for Logcat debugging purposes only
 */
package com.ense.project374;

import java.io.File;
import java.io.IOException;

import android.app.Activity;
import android.hardware.Camera;
import android.media.MediaRecorder;
import android.media.MediaRecorder.OnErrorListener;
import android.media.MediaRecorder.OnInfoListener;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.MediaController;
import android.widget.TextView;
//Toast is a class used to display widget messages to end users
import android.widget.Toast;
import android.widget.VideoView;

@SuppressWarnings("deprecation") //several camera-related functions are out of date, so deprecation warning is necessary
public class MainActivity extends Activity implements SurfaceHolder.Callback, OnInfoListener, OnErrorListener {

	private static final String TAG = "HAYDEN_TAG"; //used for identifying our Logcat statements
	
	//initialize the buttons, text and video display for the main menu layout
	private Button initializeButton = null;
	private Button startButton = null;
	private Button stopButton = null;
	private Button playButton = null;
	private Button stopPlayButton = null;
	private TextView recordingMessage = null;
	private VideoView videoView = null;
	
	//initialize camera, camera recorder, surface to display elements on
	private SurfaceHolder holder = null;
	private Camera camera = null;
	private MediaRecorder recorder = null;
	private String outputFileName;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); //display main menu layout
        
      //references to User Interface elements (buttons, etc.)
        initializeButton = (Button) findViewById(R.id.init);
        startButton = (Button) findViewById(R.id.start);
        stopButton = (Button) findViewById(R.id.stop);
        playButton = (Button) findViewById(R.id.review);
        stopPlayButton = (Button) findViewById(R.id.stopreview);
        recordingMessage = (TextView) findViewById(R.id.recording);
        videoView = (VideoView) this.findViewById(R.id.videoView);
    }
    
  /**
   * buttonTapped function used for determining what method to execute based on
   * the button the user taps.  refers to the buttons' assigned IDs in onCreate function
   * 
   */
    public void buttonTapped (View view)
    {
    	switch (view.getId())
    	{
    	case R.id.init:
    		initRecorder();
    		break;
    		
    	case R.id.start:
    		beginRecording();
    		break;
    		
    	case R.id.stop:
    		stopRecording();
    		break;
    		
    	case R.id.review:
    		playRecording();
    		break;
    		
    	case R.id.stopreview:
    		stopPlayback();
    		break;
    	}
    }
    
    //stop playing the recorded file
    private void stopPlayback()
    {
		videoView.stopPlayback();
		
	}
    
    //play the recently recorded file
    private void playRecording() 
	{
		MediaController mediaController = new MediaController(this);
		videoView.setMediaController(mediaController);
		videoView.setVideoPath(outputFileName);//determines location of recently recorded video file
		videoView.start();
		stopPlayButton.setEnabled(true);
		
	}
    
    //stop recording video
    private void stopRecording()
	{
		if (recorder != null)
		{
			//tell the recorder to stop listening for technical info and errors
			recorder.setOnInfoListener(null);
			recorder.setOnErrorListener(null);
			
			try
			{
				recorder.stop();
			}
			catch (IllegalStateException e)
			{
				//exception which occurs if the recorder already stopped
				Log.e(TAG, "IllegalStateException in stopRecording function");
			}
			
			releaseRecorder();
			recordingMessage.setText("");
			releaseCamera();
			startButton.setEnabled(true);
			stopButton.setEnabled(false);
			//enable play button since there is now a recorded video file to play
			playButton.setEnabled(true);
			
		}
		
	}
    
  //used to release to the camera once done recording
  	private void releaseCamera() 
  	{
  		if (camera != null)
  		{
  			try
  			{
  				camera.reconnect();
  			}
  			catch (IOException e)
  			{
  				Log.v(TAG, "trouble in releaseCamera function");
  				e.printStackTrace();
  			}
  			camera.release();
  			camera = null;
  		}
  		
  	}
  	
  	private void releaseRecorder() 
	{
		if (recorder != null)
		{
			recorder.release();
			recorder = null;
		}
		
	}
  	
  	private void beginRecording() 
	{
		//listeners are used to output technical info and errors about device's camera as it starts recording
		recorder.setOnInfoListener(this);
		recorder.setOnErrorListener(this);
		recorder.start();
		recordingMessage.setText("Recording Video...");
		//disable start button once recording begins and enable stop button
		startButton.setEnabled(false);
		stopButton.setEnabled(true);
		
	}
  	
  	//initialize the recorder
  	private void initRecorder() 
	{
		if (recorder != null) return; //don't bother initializing if the recorder is already initialized
		
		outputFileName = Environment.getExternalStorageDirectory() +
				"/videoOutput.mp4";
		
		//create a file to output the recording to (deletes possible existing file with same name in process)
		File outFile = new File (outputFileName);
		if (outFile.exists ())
			outFile.delete ();
		
		try
		{
			camera.stopPreview();
			camera.unlock ();
			recorder = new MediaRecorder ();
			recorder.setCamera (camera);
			
			//initializing various settings for the recorder/camera, such as output file type and video resolution
			recorder.setAudioSource (MediaRecorder.AudioSource.CAMCORDER);
			recorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
			recorder.setOutputFormat (MediaRecorder.OutputFormat.MPEG_4);
			recorder.setVideoSize(176, 144);
			recorder.setVideoFrameRate(15);
			recorder.setVideoEncoder (MediaRecorder.VideoEncoder.MPEG_4_SP);
			recorder.setAudioEncoder (MediaRecorder.AudioEncoder.AMR_NB);
			recorder.setMaxDuration(10000); //maximum length of a single recording in ms, set at low value for testing purposes 
			recorder.setPreviewDisplay (holder.getSurface());
			recorder.setOutputFile(outputFileName);
			
			//prepare the recording
			recorder.prepare ();
			Log.v (TAG, "MediaRecorder initialized");
			//disable user's ability to tap initialize button after initialization is complete
			initializeButton.setEnabled (false);
			startButton.setEnabled (true);
		}
		
		catch (Exception exception)
		{
			Log.v (TAG, "MediaRecorder failed to initialize (initRecorder function)");
			exception.printStackTrace ();
		}
		
	}
  	
  	@Override
	public void onError(MediaRecorder mediaRecorder, int error, int extra) 
  	{
		Log.e(TAG, "recording error");
		stopRecording();
		Toast.makeText(this, "Error in Recording, Recorder Stopped", Toast.LENGTH_SHORT).show();
		
	}

  	//used to stop camera from recording if the designated max video length is reached
	@Override
	public void onInfo(MediaRecorder mediaRecorder, int maxVideoDuration, int extra) 
	{
		Log.i(TAG, "entered onInfo function");
		if (maxVideoDuration == MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED)
		{
			Log.i(TAG, "Max Video Duration Reached!");
			stopRecording();
			Toast.makeText(this, "Max Duration Reached, recorder stopping", Toast.LENGTH_SHORT).show();
		}
		
		
	}
	
	//function to create the display
		@Override
		public void surfaceCreated(SurfaceHolder holder) 
		{
			Log.v(TAG, "in surfaceCreated function");
			
			try
			{
				camera.setPreviewDisplay(holder);
				camera.startPreview();
			}
			catch (IOException e)
			{
				Log.v(TAG, "surfaceCreated could not start the preview");
				e.printStackTrace();
			}
			//user can tap the initialize button if a display is successfully created
			initializeButton.setEnabled(true);
			
		}
		
		//surfaceChanged is called if the display changes (orientation, etc.) and
		//when the display is first created
		@Override
		public void surfaceChanged(SurfaceHolder holder, int format, int width,
				int height) 
		{
			if (holder.getSurface() != null) 
		    {
		        camera.stopPreview();
		    }
			
			Camera.Parameters cameraParameters = camera.getParameters();
			cameraParameters.setPreviewSize (width, height);
			camera.setParameters(cameraParameters);
			
			try
			{
				camera.setPreviewDisplay(holder);
			}
			catch (IOException exception)
			{
				exception.printStackTrace();
			}
			camera.startPreview();
		}

		@Override
		public void surfaceDestroyed(SurfaceHolder holder)
		{
			Log.d(TAG, "surfaceDestroyed has been is called");

		    // Stop preview and release camera 
		    if (holder.getSurface() != null) 
		    {
		        camera.stopPreview();
		    }
		    else {
		        Log.d(TAG, "in surfaceDestroyed: preview surface does not exist");
		    }
			
		}
		
		@Override
		protected void onResume()
		{
			Log.v(TAG, "in onResume function");
			super.onResume();
			//buttons are disabled until after checking to verify the camera is initialized
			initializeButton.setEnabled(false);
			startButton.setEnabled(false);
			stopButton.setEnabled(false);
			playButton.setEnabled(false);
			stopPlayButton.setEnabled(false);
			
			if (!initCamera())
			{
				finish();
			}
			
		}
		
		//method to initialize the device's camera
		private boolean initCamera() 
		{
			try
			{
				camera = Camera.open();
				Camera.Parameters cameraParameters = camera.getParameters();
				camera.lock();
				
				holder = videoView.getHolder();
				holder.addCallback(this);
				holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
			}
			catch (RuntimeException runtimeException)
			{
				Log.v(TAG, "Couldn't Initialize the Camera");
				runtimeException.printStackTrace();
				return false;
			}
			return true;
		}
}
