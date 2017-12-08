package com.intchip.media;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.Region;
import android.hardware.Camera;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class StreamMadiaPlayer extends SurfaceView implements SurfaceHolder.Callback {

	private final String TAG = "StreamMadiaPlayer";
	private SurfaceHolder surfaceHolder;
	public static boolean mIsPaused = false, mIsSurfaceReady = false, mHasFocus = true;
	public static boolean mExitCalledFromJava;
	private static SurfaceView mSurface;
	private StreamMadiaPlayer mStreamMadiaPlayer;
	// This is what SDL runs in. It invokes SDL_main(), eventually
	protected static Thread mSDLThread;
//	protected static Activity mSingleton;
	public static int screenWidth, screenHeight;
	private Canvas mCanvas;
	private boolean isDrawing;
	private Bitmap mBitmap;
	private Paint paint;        //定义画笔
	private int paintColor = Color.BLACK; //定义画笔默认颜色
	private Resources mResources = null;
	private int height; // 圆的半径
//	private SurfaceHolder sfh = null;
	private Camera camera;
//	public StreamMadiaPlayer(Activity mainActivity) {
//		mSingleton = mainActivity;
//
//		mSurface = (SurfaceView) mainActivity.findViewById(R.id.main_surface);
//		surfaceHolder = mSurface.getHolder();
//		surfaceHolder.addCallback(this);
//
//		mResources = mSurface.getResources();
//		initialize();
//		initPaint();
//
//		getScreenResolution();
//	}

	public StreamMadiaPlayer(Context context, AttributeSet attrs, int defstyle){
		super(context, attrs, defstyle);
		initView();
//		mResources = getResources();
//		initialize();
//		initPaint();
//
//		getScreenResolution();
	}

	public StreamMadiaPlayer(Context context, AttributeSet attrs){
		super(context, attrs);
//		mSurface = (SurfaceView) mainActivity.findViewById(R.id.main_surface);
		initView();

		mResources = getResources();
		initialize();
		initPaint();

		getScreenResolution();
	}

	private void initView() {
		this.setFocusable(true);
		this.setFocusableInTouchMode(true);
//		getHolder().addCallback(this);
		surfaceHolder = getHolder();
		surfaceHolder.addCallback(this);
	}


	@Override
	public void draw(Canvas canvas) {
		Log.e("onDraw", "draw: test");
		Path path = new Path();
		path.addCircle(height / 2, height / 2, height / 2, Path.Direction.CCW);
		canvas.clipPath(path, Region.Op.REPLACE);
		super.draw(canvas);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);

	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);


		int widthMode = MeasureSpec.getMode(widthMeasureSpec);
		int heightMode = MeasureSpec.getMode(heightMeasureSpec);
		int widthSize = MeasureSpec.getSize(widthMeasureSpec);
		int heightSize = MeasureSpec.getSize(heightMeasureSpec);

		height = widthSize;

		Log.e("onMeasure", "draw: widthMeasureSpec = " +widthSize + "  heightMeasureSpec = " + heightSize);

		setMeasuredDimension(widthSize, heightSize);
	}

	public static void initialize() {
		mSDLThread = null;
		mExitCalledFromJava = false;
		mIsPaused = false;
		mIsSurfaceReady = false;
		mHasFocus = true;
	}

	private void initPaint(){
		paint = new Paint();
		//设置消除锯齿
		paint.setAntiAlias(true);
		//设置画笔颜色
		paint.setColor(paintColor);

	}

	class DrawRunnable implements Runnable{

		@Override
		public void run() {

			while (isDrawing){
				drawAimLogo();
				try {
					Thread.sleep(1000);
				}catch (InterruptedException e){
					e.printStackTrace();
				}
			}
		}
	}

	private void drawAimLogo(){

		mCanvas = this.surfaceHolder.lockCanvas();
		if (mCanvas != null){
			synchronized (surfaceHolder){
				try{
					mCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);//绘制透明色
					mCanvas.drawBitmap(mBitmap, screenWidth/2, screenHeight/2, paint);
				}catch (NullPointerException e){
					e.printStackTrace();
				}
			}
		}

		if (mCanvas != null){
			surfaceHolder.unlockCanvasAndPost(mCanvas);
		}

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
		mStreamMadiaPlayer = MainActivity.getMainActivityInstance().getStreamMadiaPlayerInstance();
//		isDrawing = true;
//		mBitmap = BitmapFactory.decodeResource(mResources, R.drawable.aim_at);
//		mBitmap = ImageUtil.getBitmap(mBitmap);
//		new Thread(new DrawRunnable()).start();

		//test code begin
//		camera = Camera.open();
//		try {
//			//设置预览监听
//			camera.setPreviewDisplay(holder);
//			Camera.Parameters parameters = camera.getParameters();
//
//			if (this.getResources().getConfiguration().orientation
//					!= Configuration.ORIENTATION_LANDSCAPE) {
//				parameters.set("orientation", "portrait");
//				camera.setDisplayOrientation(90);
//				parameters.setRotation(90);
//			} else {
//				parameters.set("orientation", "landscape");
//				camera.setDisplayOrientation(0);
//				parameters.setRotation(0);
//			}
//			camera.setParameters(parameters);
//			//启动摄像头预览
//			camera.startPreview();
//			System.out.println("camera.startpreview");
//
//		} catch (IOException e) {
//			e.printStackTrace();
//			camera.release();
//			System.out.println("camera.release");
//		}
		//test code end
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {

		Log.d(TAG, "surfaceChanged");
//		mBitmap = BitmapFactory.decodeResource(mResources, R.drawable.aim_at);
//		mBitmap = ImageUtil.getBitmap(mBitmap);
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
						mSDLThread.join(1000);
					}catch (InterruptedException e){
						e.printStackTrace();
					}
					finally {
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
		isDrawing = false;
		SDLNative.onNativeSurfaceDestroyed();
	}

	/**
	 * Simple nativeInit() runnable
	 */
	class SDLMain implements Runnable {
//		SDLNative objSDLInfo = new SDLNative(mSurface, screenWidth, screenHeight);
		SDLNative objSDLInfo = new SDLNative(mStreamMadiaPlayer, screenWidth, screenHeight);
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
//		mSingleton.finish();
	}

	public void getScreenResolution() {

		DisplayMetrics dm = getResources().getDisplayMetrics();
		screenWidth = dm.widthPixels;
		screenHeight = dm.heightPixels;
//		screenWidth = mSingleton.getWindowManager().getDefaultDisplay().getWidth();
//		screenHeight = mSingleton.getWindowManager().getDefaultDisplay().getHeight();
//		screenWidth = getWidth();
//		screenHeight = getHeight();
	}
}