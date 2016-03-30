/*
 * Copyright 2014 http://Bither.net
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.bither.util;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.media.ExifInterface;
import android.util.Log;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class NativeUtil {
    private static int DEFAULT_QUALITY = 95;

    public static int readPictureDegree(String path) {
        int degree = 0;
        try {
            ExifInterface exifInterface = new ExifInterface(path);
            int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    degree = 90;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    degree = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    degree = 270;
                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return degree;
    }

    public static Bitmap rotaingImageView(int angle, Bitmap bitmap) {
        //旋转图片 动作
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        // 创建新的图片
        Bitmap resizedBitmap = Bitmap.createBitmap(bitmap, 0, 0,
                bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        return resizedBitmap;
    }

//    public static Bitmap compressImage(Bitmap image) {
//
//        ByteArrayOutputStream baos = new ByteArrayOutputStream();
//        image.compress(Bitmap.CompressFormat.JPEG, 100, baos);//质量压缩方法，这里100表示不压缩，把压缩后的数据存放到baos中
//        int options = 100;
//        while (baos.toByteArray().length / 1024 > 100) {    //循环判断如果压缩后图片是否大于100kb,大于继续压缩
//            baos.reset();//重置baos即清空baos
//            image.compress(Bitmap.CompressFormat.JPEG, options, baos);//这里压缩options%，把压缩后的数据存放到baos中
//            options -= 10;//每次都减少10
//            if (options < 0) {
//                break;
//            }
//        }
//        ByteArrayInputStream isBm = new ByteArrayInputStream(baos.toByteArray());//把压缩后的数据baos存放到ByteArrayInputStream中
//        Bitmap bitmap = BitmapFactory.decodeStream(isBm, null, null);//把ByteArrayInputStream数据生成图片
//        return bitmap;
//    }

    public static BitmapFactory.Options getBitmapOptions(String paramString) {
        BitmapFactory.Options localOptions = new BitmapFactory.Options();
        localOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(paramString, localOptions);
        return localOptions;
    }

    public static int calculateInSampleSize(BitmapFactory.Options paramOptions, int paramInt1, int paramInt2) {
        int i = paramOptions.outHeight;
        int j = paramOptions.outWidth;
        int k = 1;
        if ((i > paramInt2) || (j > paramInt1)) {
            int m = Math.round(i / paramInt2);
            int n = Math.round(j / paramInt1);
            k = m > n ? m : n;
        }
        return k;
    }

    public static Bitmap decodeScaleImage(String paramString, int paramInt1, int paramInt2) {
        BitmapFactory.Options localOptions = getBitmapOptions(paramString);
        int i = calculateInSampleSize(localOptions, paramInt1, paramInt2);
        localOptions.inSampleSize = i;
        localOptions.inJustDecodeBounds = false;
        Bitmap localBitmap1 = BitmapFactory.decodeFile(paramString, localOptions);
        int j = readPictureDegree(paramString);
        Bitmap localBitmap2 = null;
        if ((localBitmap1 != null) && (j != 0)) {
            localBitmap2 = rotaingImageView(j, localBitmap1);
            localBitmap1.recycle();
            localBitmap1 = null;
            return localBitmap2;
        }
        return localBitmap1;
    }

    /**
     * @param srcImageFile
     * @param driImageFile
     * @param dirFile
     * @param quality
     * @return 成功返回文件路径
     */
    public static String autoCompressBitmap(String srcImageFile, String driImageFile, File dirFile, int quality) {
        try {
            File localFile1 = new File(srcImageFile);
            if (!localFile1.exists())
                return srcImageFile;
            long l = localFile1.length();
            if (l <= 102400L) {
                return srcImageFile;
            }
            Bitmap bit = decodeScaleImage(srcImageFile, 1920, 1080);

            File jpegTrueFile = new File(dirFile, driImageFile);
            compressBitmap(bit, quality,
                    jpegTrueFile.getAbsolutePath(), true);
            return jpegTrueFile.getAbsolutePath();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

    }

    public static void compressBitmap(Bitmap bit, String fileName,
                                      boolean optimize) {
        compressBitmap(bit, DEFAULT_QUALITY, fileName, optimize);

    }


    public static void compressBitmap(Bitmap bit, int quality, String fileName,
                                      boolean optimize) {
        Log.d("native", "compress of native");

        // if (bit.getConfig() != Config.ARGB_8888) {
        Bitmap result = null;

//		result = Bitmap.createBitmap(bit.getWidth() / 3, bit.getHeight() / 3,
//                Config.ARGB_8888);// 缩小3倍
//        Matrix matrix = new Matrix();
//        matrix.postRotate(90);
//        result = Bitmap.createBitmap(bit, 0, 0,
//                bit.getWidth(), bit.getHeight(), matrix, true);
        result = Bitmap.createBitmap(bit.getWidth(), bit.getHeight(),
                Config.ARGB_8888);// 缩小3倍
        Canvas canvas = new Canvas(result);
        Rect rect = new Rect(0, 0, bit.getWidth(), bit.getHeight());// original
//		rect = new Rect(0, 0, bit.getWidth() / 3, bit.getHeight() / 3);// 缩小3倍
        rect = new Rect(0, 0, bit.getWidth(), bit.getHeight());// 缩小3倍
        canvas.drawBitmap(bit, null, rect, null);
        saveBitmap(result, quality, fileName, optimize);
        result.recycle();
        // } else {
        // saveBitmap(bit, quality, fileName, optimize);
        // }

    }

    private static void saveBitmap(Bitmap bit, int quality, String fileName,
                                   boolean optimize) {

        compressBitmap(bit, bit.getWidth(), bit.getHeight(), quality,
                fileName.getBytes(), optimize);

    }

    private static native String compressBitmap(Bitmap bit, int w, int h,
                                                int quality, byte[] fileNameBytes, boolean optimize);

    static {
        System.loadLibrary("jpegbither");
        System.loadLibrary("bitherjni");

    }

}
