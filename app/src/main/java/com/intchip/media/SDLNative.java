package com.intchip.media;

import java.util.Arrays;


import android.annotation.TargetApi;
import android.media.AudioFormat;
import android.media.AudioTrack;
import android.os.Build;
import android.util.Log;
import android.view.InputDevice;
import android.view.Surface;
import android.view.SurfaceView;

public class SDLNative {
	public static final String TAG = "SDLNative";
	
	private static SurfaceView mSurfaceView;
	protected static AudioTrack mAudioTrack;
	public static int screenWidth, screenHeight;
	
	public SDLNative(SurfaceView sv) {
		mSurfaceView = sv;
	}
	
	public SDLNative(SurfaceView sv, int w, int h) {
		mSurfaceView = sv;
		screenWidth = w;
		screenHeight = h;
	}
	
	public static void setScreeen(int w, int h) {
		screenWidth = w;
		screenHeight = h;
	}

	public static native int nativeInit(SDLNative object);
	
	public static native void onNativeResize(int x, int y, int format);
	
	public static native void nativeQuit();
	
	public static native void nativePause();
	
	public static native void nativeResume();
	
	public static native void onNativeSurfaceChanged();
	
    public static native void onNativeSurfaceDestroyed();
    
    public static native void nativeLowMemory();
    
    public static native void nativeFlipBuffers();
    
    public native static void screenChange(int screenWidth, int screenHeight);
    
    public native static int quitVideoPlay();
    
    public native static int startVodPlay();
    
    public native static int stopVodPlay();
    
    public native static int snapshot(String filename);
    
    public native static int reversal();
    
    public native static int videoRecordStart(String filename);
    public native static int videoRecordStop();
    public native static int isVideoRecord();
    
    public static Surface getNativeSurface() {
        return mSurfaceView.getHolder().getSurface();
    }
    
    public static void flipBuffers() {
        nativeFlipBuffers();
    }
        
 // Audio
    public static int audioInit(int sampleRate, boolean is16Bit, boolean isStereo, int desiredFrames) {
        int channelConfig = isStereo ? AudioFormat.CHANNEL_CONFIGURATION_STEREO : AudioFormat.CHANNEL_CONFIGURATION_MONO;
        int audioFormat = is16Bit ? AudioFormat.ENCODING_PCM_16BIT : AudioFormat.ENCODING_PCM_8BIT;
        int frameSize = (isStereo ? 2 : 1) * (is16Bit ? 2 : 1);
        
        Log.d(TAG, "SDL audio: wanted " + (isStereo ? "stereo" : "mono") + " " + (is16Bit ? "16-bit" : "8-bit") + " " + (sampleRate / 1000f) + "kHz, " + desiredFrames + " frames buffer");
        
        // Let the user pick a larger buffer if they really want -- but ye
        // gods they probably shouldn't, the minimums are horrifyingly high
        // latency already
        desiredFrames = Math.max(desiredFrames, (AudioTrack.getMinBufferSize(sampleRate, channelConfig, audioFormat) + frameSize - 1) / frameSize);
        return 0;
    }
    
    public static void audioWriteShortBuffer(short[] buffer) {
        for (int i = 0; i < buffer.length; ) {
			int result = 0;
			if (null != mAudioTrack)
            	result = mAudioTrack.write(buffer, i, buffer.length - i);
            if (result > 0) {
                i += result;
            } else if (result == 0) {
                try {
                    Thread.sleep(1);
                } catch(InterruptedException e) {
                }
            } else {
                Log.d(TAG, "SDL audio: error return from write(short)");
                return;
            }
        }
    }
    
    public static void audioWriteByteBuffer(byte[] buffer) {
        for (int i = 0; i < buffer.length; ) {
			int result = 0;
			if (null != mAudioTrack)
            	result = mAudioTrack.write(buffer, i, buffer.length - i);
            if (result > 0) {
                i += result;
            } else if (result == 0) {
                try {
                    Thread.sleep(1);
                } catch(InterruptedException e) {
                    // Nom nom
                }
            } else {
                Log.w("SDL", "SDL audio: error return from write(short)");
                return;
            }
        }
    }
    
    public static void audioQuit() {
        if (mAudioTrack != null) {
            mAudioTrack.stop();
            mAudioTrack = null;
        }
    }
    
    @TargetApi(Build.VERSION_CODES.GINGERBREAD)
	/**
     * @return an array which may be empty but is never null.
     */
	public static int[] inputGetInputDeviceIds(int sources) {
        int[] ids = InputDevice.getDeviceIds();
        int[] filtered = new int[ids.length];
        int used = 0;
        for (int i = 0; i < ids.length; ++i) {
            InputDevice device = InputDevice.getDevice(ids[i]);
            if ((device != null) && ((device.getSources() & sources) != 0)) {
                filtered[used++] = device.getId();
           }
        }
        return Arrays.copyOf(filtered, used);
    }
    
    public static void pollInputDevices() {
    	
    }
    
    public static boolean setActivityTitle(String title) {
        return true;
    }
    
    static {
    	System.loadLibrary("SDL");
    	System.loadLibrary("swresample-2");
    	System.loadLibrary("swscale-4");
    	System.loadLibrary("avformat-57");
    	System.loadLibrary("avutil-55");
    	System.loadLibrary("avcodec-57");
    	System.loadLibrary("mp4v2");
    	System.loadLibrary("mzmedia");
    }
}
