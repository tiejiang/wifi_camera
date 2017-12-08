package com.intchip.media.utils;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.util.Log;

/**
 * Created by yinyu-tiejiang on 17-10-26.
 */

public class ImageUtil {

    public static Bitmap getBitmap(Bitmap bitmap){

//        int width = bitmap.getWidth();
//        int height = bitmap.getHeight();

        //放大為屏幕的1/2大小
//        float screenWidth  = getWindowManager().getDefaultDisplay().getWidth();     // 屏幕宽（像素，如：480px）
//        float screenHeight = getWindowManager().getDefaultDisplay().getHeight();        // 屏幕高（像素，如：800p）
        int scaleWidth = 5;
        int scaleHeight = 5;

        Log.d("TIEJIANG", "ImageUtil---getBitmap"+ " bitmap= " + bitmap);
        // 取得想要缩放的matrix參數
        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight);
        // 得到新的圖片
        Bitmap newbm = Bitmap.createBitmap(bitmap, 0, 0, scaleWidth, scaleHeight, matrix,true);
        return newbm;
    }
}
