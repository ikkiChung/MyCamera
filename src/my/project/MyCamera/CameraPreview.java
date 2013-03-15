package my.project.MyCamera;

import java.io.FileOutputStream;
import java.io.IOException;

import android.view.SurfaceHolder;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.ShutterCallback;

public class CameraPreview implements SurfaceHolder.Callback, Camera.PreviewCallback
{
	private Camera mCamera = null;
	private int PreviewSizeWidth;
 	private int PreviewSizeHeight;
	private String NowPictureFileName;
	private Boolean TakePicture = false;
	
    public CameraPreview(int PreviewlayoutWidth, int PreviewlayoutHeight)
    {
		PreviewSizeWidth = PreviewlayoutWidth;
    	PreviewSizeHeight = PreviewlayoutHeight;
    }

	@Override
	public void onPreviewFrame(byte[] arg0, Camera arg1) 
	{
		// At preview mode, the frame data will push to here.
		// But we do not want these data.
	}

	@Override
	public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2, int arg3) 
	{
	    Parameters parameters;
		
		parameters = mCamera.getParameters();
		// Set the camera preview size
		parameters.setPreviewSize(PreviewSizeWidth, PreviewSizeHeight);
		// Set the take picture size, you can set the large size of the camera supported.
		parameters.setPictureSize(PreviewSizeWidth, PreviewSizeHeight);
		
		// Turn on the camera flash. 
		String NowFlashMode = parameters.getFlashMode();
		if ( NowFlashMode != null )
			parameters.setFlashMode(Parameters.FLASH_MODE_ON);
		// Set the auto-focus. 
		String NowFocusMode = parameters.getFocusMode ();
		if ( NowFocusMode != null )
			parameters.setFocusMode("auto");
		
		mCamera.setParameters(parameters);
		
		mCamera.startPreview();
	}

	@Override
	public void surfaceCreated(SurfaceHolder arg0) 
	{
		mCamera = Camera.open();
		try
		{
			// If did not set the SurfaceHolder, the preview area will be black.
			mCamera.setPreviewDisplay(arg0);
			mCamera.setPreviewCallback(this);
		} 
		catch (IOException e)
		{
			mCamera.release();
			mCamera = null;
		}
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder arg0) 
	{
    	mCamera.setPreviewCallback(null);
		mCamera.stopPreview();
		mCamera.release();
		mCamera = null;
	}

	// Take picture interface
	public void CameraTakePicture(String FileName)
	{
		TakePicture = true;
		NowPictureFileName = FileName;
		mCamera.autoFocus(myAutoFocusCallback);
	}
	
	// Set auto-focus interface
	public void CameraStartAutoFocus()
	{
		TakePicture = false;
		mCamera.autoFocus(myAutoFocusCallback);
	}
	
	
	//=================================
    //
    // AutoFocusCallback
    //
    //=================================
	AutoFocusCallback myAutoFocusCallback = new AutoFocusCallback()
	{
		public void onAutoFocus(boolean arg0, Camera NowCamera) 
		{
			if ( TakePicture )
			{
				NowCamera.stopPreview();//fixed for Samsung S2
				NowCamera.takePicture(shutterCallback, rawPictureCallback, jpegPictureCallback);
				TakePicture = false;
			}
		}
	};
	ShutterCallback shutterCallback = new ShutterCallback()
	{
		 public void onShutter() 
		 {
			 // Just do nothing.
		 }
	};
		 
	PictureCallback rawPictureCallback = new PictureCallback()
	{
		 public void onPictureTaken(byte[] arg0, Camera arg1) 
		 {
			 // Just do nothing.
		 }
	};
		 
	PictureCallback jpegPictureCallback = new PictureCallback()
	{
		public void onPictureTaken(byte[] data, Camera arg1) 
		{
			// Save the picture.
			try {
				Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0,data.length);
				FileOutputStream out = new FileOutputStream(NowPictureFileName);
				bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
			} 
			catch (IOException e) 
			{
				e.printStackTrace();
			}
		}
	};	
	
}
