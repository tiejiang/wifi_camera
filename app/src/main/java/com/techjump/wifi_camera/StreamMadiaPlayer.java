package com.techjump.wifi_camera;

import android.app.Activity;
import android.graphics.PixelFormat;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class StreamMadiaPlayer implements SurfaceHolder.Callback {
	private final String TAG = "StreamMadiaPlayer";

	private SurfaceHolder surfaceHolder;

	public static boolean mIsPaused = false, mIsSurfaceReady = false,
			mHasFocus = true;
	public static boolean mExitCalledFromJava;

	private static SurfaceView mSurface;

	// This is what SDL runs in. It invokes SDL_main(), eventually
	protected static Thread mSDLThread;

	protected static Activity mSingleton;

	public static int screenWidth, screenHeight;

	public StreamMadiaPlayer(Activity mainActivity) {
		mSingleton = mainActivity;

		mSurface = (SurfaceView) mainActivity.findViewById(R.id.main_surface);
		surfaceHolder = mSurface.getHolder();
		surfaceHolder.addCallback(this);

		initialize();

		getScreenResolution();
	}

	public static void initialize() {
		mSDLThread = null;
		mExitCalledFromJava = false;
		mIsPaused = false;
		mIsSurfaceReady = false;
		mHasFocus = true;
	}

	public void onLowMemory() {
		Log.d(TAG, "onLowMemory()");
		SDLNative.nativeLowMemory();
	}

	public void onWindowFocusChanged(boolean hasFocus) {
		Log.d(TAG, "onWindowFocusChanged(): " + hasFocus);

//		Toast.makeText(mSingleton, "onWindowFocusChanged() ", Toast.LENGTH_SHORT).show();
		
		mHasFocus = hasFocus;
		if (hasFocus) {
			handleResume();
		}
	}

	public void onResume() {
		Log.d(TAG, "onResume()");
		
//		Toast.makeText(mSingleton, "onResume() ", Toast.LENGTH_SHORT).show();
		
		handleResume();
	}

	public static void handleResume() {
		if (mIsPaused && mIsSurfaceReady && mHasFocus) {
			mIsPaused = false;
			SDLNative.nativeResume();
		}
	}

	public void onPause() {
		Log.d(TAG, "onPause()");
		handlePause();
//		Toast.makeText(mSingleton, "onPause", Toast.LENGTH_SHORT).show();
	}

	/**
	 * Called by onPause or surfaceDestroyed. Even if surfaceDestroyed is the
	 * first to be called, mIsSurfaceReady should still be set to 'true' during
	 * the call to onPause (in a usual scenario).
	 */
	public static void handlePause() {
		if (!mIsPaused && mIsSurfaceReady) {
			mIsPaused = true;
			SDLNative.nativePause();
		}
	}

	public void onDestory() {
		Log.d(TAG, "onDestory");
		
//		Toast.makeText(mSingleton, "onDestory", Toast.LENGTH_SHORT).show();
		
		SDLNative.nativeQuit();

		mExitCalledFromJava = true;

		SDLNative.quitVideoPlay();

		// Now wait for the SDL thread to quit
		if (mSDLThread != null) {
			try {
				mSDLThread.join();
			} catch (Exception e) {
				Log.d(TAG, "Problem stopping thread: " + e);
			}
			mSDLThread = null;

			Log.d(TAG, "Finished waiting for SDL thread");
		}

		initialize();
	}
	
	public void onNetworkConnected() {
		Log.d(TAG, "onNetworkConnected()");
		
		new Thread(new SDLVod(true), "SDLVodThread start").start();
		
//		Toast.makeText(mSingleton, "onNetworkConnected", Toast.LENGTH_SHORT).show();
	}
	
	public void onNetworkDisconnected() {
		Log.d(TAG, "onNetworkDisconnected()");
		
		new Thread(new SDLVod(false), "SDLVodThread stop").start();
		
//		Toast.makeText(mSingleton, "onNetworkDisconnected", Toast.LENGTH_SHORT).show();
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		Log.d(TAG, "surfaceCreated()");
		holder.setType(SurfaceHolder.SURFACE_TYPE_GPU);
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		Log.d(TAG, "surfaceChanged");

//		Toast.makeText(mSingleton, "surfaceChanged", Toast.LENGTH_SHORT).show();
		
		int sdlFormat = 0x15151002; // SDL_PIXELFORMAT_RGB565 by default
		switch (format) {
		case PixelFormat.A_8:
			Log.v("SDL", "pixel format A_8");
			break;
		case PixelFormat.LA_88:
			Log.v("SDL", "pixel format LA_88");
			break;
		case PixelFormat.L_8:
			Log.v("SDL", "pixel format L_8");
			break;
		case PixelFormat.RGBA_4444:
			Log.v("SDL", "pixel format RGBA_4444");
			sdlFormat = 0x15421002; // SDL_PIXELFORMAT_RGBA4444
			break;
		case PixelFormat.RGBA_5551:
			Log.v("SDL", "pixel format RGBA_5551");
			sdlFormat = 0x15441002; // SDL_PIXELFORMAT_RGBA5551
			break;
		case PixelFormat.RGBA_8888:
			Log.v("SDL", "pixel format RGBA_8888");
			sdlFormat = 0x16462004; // SDL_PIXELFORMAT_RGBA8888
			break;
		case PixelFormat.RGBX_8888:
			Log.v("SDL", "pixel format RGBX_8888");
			sdlFormat = 0x16261804; // SDL_PIXELFORMAT_RGBX8888
			break;
		case PixelFormat.RGB_332:
			Log.v("SDL", "pixel format RGB_332");
			sdlFormat = 0x14110801; // SDL_PIXELFORMAT_RGB332
			break;
		case PixelFormat.RGB_565:
			Log.v("SDL", "pixel format RGB_565");
			sdlFormat = 0x15151002; // SDL_PIXELFORMAT_RGB565
			break;
		case PixelFormat.RGB_888:
			Log.v("SDL", "pixel format RGB_888");
			// Not sure this is right, maybe SDL_PIXELFORMAT_RGB24 instead?
			sdlFormat = 0x16161804; // SDL_PIXELFORMAT_RGB888
			break;
		default:
			Log.v("SDL", "pixel format unknown " + format);
			break;
		}

		Log.d(TAG, "Window size:" + width + "x" + height);
		SDLNative.onNativeResize(width, height, sdlFormat);

		// Set mIsSurfaceReady to 'true' *before* making a call to handleResume
		mIsSurfaceReady = true;
		SDLNative.onNativeSurfaceChanged();

		if (mSDLThread == null) {
			mSDLThread = new Thread(new SDLMain(), "SDLThread");
			mSDLThread.start();

			// Set up a listener thread to catch when the native thread ends
			new Thread(new Runnable() {
				public void run() {
					try {
						mSDLThread.join();
					} catch (Exception e) {
					} finally {
						// Native thread has finished
						if (!mExitCalledFromJava) {
							handleNativeExit();
						}
					}
				}
			}).start();
		}
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		Log.d(TAG, "surfaceDestroyed()");

		handlePause();
		// SDLNative.nativeQuit();

		mIsSurfaceReady = false;
		SDLNative.onNativeSurfaceDestroyed();
	}

	/**
	 * Simple nativeInit() runnable
	 */
	class SDLMain implements Runnable {
		SDLNative objSDLInfo = new SDLNative(mSurface, screenWidth, screenHeight);

		public void run() {
			SDLNative.nativeInit(objSDLInfo);
		}
	}
	
	class SDLVod implements Runnable {
		private boolean startFlag = false;
		
		public SDLVod (boolean flag) {
			startFlag = flag;
        }		

		public void run() {
            if (startFlag) {
            	int ret = SDLNative.startVodPlay();
    			if (ret < 0) {
    				Log.d(TAG, "startVodPlay failed");
    			}
            } else {
            	SDLNative.stopVodPlay();
            }
		}
	}

	/* The native thread has finished */
	public static void handleNativeExit() {
		mSDLThread = null;
		mSingleton.finish();
	}

	public void getScreenResolution() {
		screenWidth = mSingleton.getWindowManager().getDefaultDisplay()
				.getWidth();
		screenHeight = mSingleton.getWindowManager().getDefaultDisplay()
				.getHeight();

	}
}