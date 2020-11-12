package com.whalecloud.minialita.utils;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Base64;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class BitmapUtil {

    public static BitmapUtil getInstance() {
        return InstanceHolder.INSTANCE;
    }

    private static final class InstanceHolder {
        private static final BitmapUtil INSTANCE = new BitmapUtil();
    }

    private BitmapUtil() {
    }

    /**
     * new File
     */
    public File getPicFile() {
        File file = new File(Environment.getExternalStorageDirectory() + "/pic/", System.currentTimeMillis() + ".jpg");
        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return file;
    }

    /**
     * 尺寸大小的缩放
     */
    public void getImage(String srcPath, String desPath) {
        BitmapFactory.Options newOpts = new BitmapFactory.Options();
        //将这个参数的inJustDecodeBounds属性设置为true就可以让解析方法禁止为bitmap分配内存，
        // 返回值也不再是一个Bitmap对象，而是null。虽然Bitmap是null了，
        // 但是BitmapFactory.Options的outWidth、outHeight和outMimeType属性都会被赋值
        newOpts.inJustDecodeBounds = true;
        Bitmap bitmap = BitmapFactory.decodeFile(srcPath, newOpts);//文件图片用该方法,此时Bitmap为null

        newOpts.inJustDecodeBounds = false;
        int w = newOpts.outWidth;//源图片的宽，高
        int h = newOpts.outHeight;
        //最好动态指定设置宽高
        float hh = 480;//这里设置高度为480f
        float ww = 320;//这里设置宽度为320f
        //缩放比。由于是固定比例缩放，只用高或者宽其中一个数据进行计算即可
        int be = 1;//be=1表示不缩放
        if (w > h && w > ww) {//如果宽度大的话根据宽度固定大小缩放
            be = (int) (newOpts.outWidth / ww);
        } else if (w < h && h > hh) {//如果高度高的话根据宽度固定大小缩放
            be = (int) (newOpts.outHeight / hh);
        }
        if (be <= 0)
            be = 1;
        newOpts.inSampleSize = be;//设置缩放比例
        //重新读入图片，注意此时已经把options.inJustDecodeBounds 设回false了
        bitmap = BitmapFactory.decodeFile(srcPath, newOpts);
        compressImage(bitmap, desPath);//压缩好比例大小后再进行质量压缩

    }

    /**
     * 存储大小的缩放
     */
    public void compressImage(Bitmap image, String file) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        FileOutputStream fileOutputStream = null;
        try {
            fileOutputStream = new FileOutputStream(file);
            image.compress(Bitmap.CompressFormat.JPEG, 80, baos);// 质量压缩方法，这里100表示不压缩，把压缩后的数据存放到baos中
            int options = 85;
            while ((baos.toByteArray().length / 1024 > 200) && (options > 1)) { // 循环判断如果压缩后图片是否大于200kb,大于继续压缩
                baos.reset();// 重置baos即清空baos
                image.compress(Bitmap.CompressFormat.JPEG, options, baos);// 这里压缩options%，把压缩后的数据存放到baos中
                options -= 10;// 每次都减少10
            }
            baos.writeTo(fileOutputStream);
            // 用完了记得回收
            image.recycle();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                fileOutputStream.close();
                baos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public String getPathByUri(final Context context, final Uri uri) {
        try {
            return getRealFilePath(context, uri);
        } catch (Exception e) {
            return uri.getPath();
        }
    }

    /**
     * get url by uri
     */
    private String getRealFilePath(final Context context, final Uri uri) {
        if (null == uri) return null;
        final String scheme = uri.getScheme();
        String data = null;
        if (scheme == null)
            data = uri.getPath();
        else if (ContentResolver.SCHEME_FILE.equals(scheme)) {
            data = uri.getPath();
        } else if (ContentResolver.SCHEME_CONTENT.equals(scheme)) {
            Cursor cursor = context.getContentResolver().query(uri, new String[]{MediaStore.Images.ImageColumns.DATA}, null, null, null);
            if (null != cursor) {
                if (cursor.moveToFirst()) {
                    int index = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
                    if (index > -1) {
                        data = cursor.getString(index);
                    }
                }
                cursor.close();
            }
        }
        return data;
    }


    /**
     * @Title: zoomImage
     * @Description: 缩放图片
     * @param bitmapSrc
     * @param height
     *            高度
     * @param width
     *            宽度
     * @return
     * @throws
     */
    public static Bitmap zoomImage(Bitmap bitmapSrc, int height, int width) {
        Bitmap bitmap = null;

        if (bitmapSrc != null) {
            int bitmapHeight = bitmapSrc.getHeight();
            int bitmapWidth = bitmapSrc.getWidth();

            // 依据设定的图片高宽算出比例
            float balance = getBalance(width, height, bitmapHeight, bitmapWidth);

            // 缩放图片动作
            Matrix matrix = new Matrix();
            matrix.postScale(balance, balance);
            try {
                bitmap = Bitmap.createBitmap(bitmapSrc, 0, 0, bitmapWidth, bitmapHeight, matrix, true);
            } catch (OutOfMemoryError e) {
                e.printStackTrace();
            }

            if (bitmapSrc != null) {
                bitmapSrc.recycle();
                bitmapSrc = null;
            }
        }

        return bitmap;
    }

    /**
     * @Title: getBalance
     * @Description: 缩放比例
     * @param bitmapHeight
     *            源图片高度
     * @param bitmapWidth
     *            源图片宽度
     * @return float
     * @throws
     */
    public static float getBalance(int width, int height, float bitmapHeight, float bitmapWidth) {
        // 图片比率
        float balanceHeight = width / bitmapHeight;
        float balanceWidth = height / bitmapWidth;
        return Math.min(balanceHeight, balanceWidth);
    }

    public static boolean saveBitmap(Bitmap bitmap, String path) {
        try {
            File f = FileUtil.createFile(path);
            f.createNewFile();
            FileOutputStream fOut = new FileOutputStream(f);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fOut);
            fOut.flush();
            fOut.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 从磁盘中获取图像
     */
    public static Bitmap getBitmapFromDisk(String imagePath) {
        return getBitmapFromDisk(imagePath, 0, 0);
    }

    /**
     * 从磁盘中获取图像
     *
     * @param imagePath
     * @param outWidth
     * @param outHeight
     * @return
     */
    public static Bitmap getBitmapFromDisk(String imagePath, float outWidth, float outHeight) {
        return getBitmapFromDisk(imagePath, outWidth, outHeight, true);
    }

    /**
     * 从磁盘中获取图像
     *
     * @param imagePath
     * @param outWidth
     * @param outHeight
     * @return
     */
    public static Bitmap getBitmapFromDisk(String imagePath, float outWidth, float outHeight, boolean delete) {
        // 验证参数
        if (TextUtils.isEmpty(imagePath)) {
            return null;
        }

        FileInputStream fis = null;
        try {
            fis = new FileInputStream(imagePath);
            FileDescriptor fd = fis.getFD();
            if (outWidth <= 0 || outHeight <= 0) {
                return BitmapFactory.decodeFileDescriptor(fd);
            }

            BitmapFactory.Options options = getOptions(imagePath, outWidth, outHeight);
            options.inInputShareable = android.os.Build.VERSION.SDK_INT < 19;
            options.inPurgeable = true;

            return BitmapFactory.decodeFileDescriptor(fd, null, options);

            // return BitmapFactory.decodeFile(imagePath,
            // getOptions(imagePath, outWidth, outHeight));
        } catch (OutOfMemoryError error) {
            error.printStackTrace();
            return null;
        } catch (Exception e) {
            if (delete) {
                new File(imagePath).delete();
            }
            return null;
        } finally {
            try {
                if (fis != null) {
                    fis.close();
                }
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        }
    }

    /**
     * @param imageFile
     * @param outWidth  单位是dp
     * @param outHeight 单位是dp
     * @return
     */
    public static BitmapFactory.Options getOptions(String imageFile, float outWidth, float outHeight) {
        // 获取图像的大小

        BitmapFactory.Options bitmapFactoryOptions = new BitmapFactory.Options();
        bitmapFactoryOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(imageFile, bitmapFactoryOptions);

        double yRatio = (double) (bitmapFactoryOptions.outHeight / outHeight);
        double xRatio = (double) (bitmapFactoryOptions.outWidth / outWidth);

        if (yRatio > 1 || xRatio > 1) {
            if (yRatio > xRatio) {
                bitmapFactoryOptions.inSampleSize = (int) Math.round(yRatio);
            } else {
                bitmapFactoryOptions.inSampleSize = (int) Math.round(xRatio);
            }
        } else {
            bitmapFactoryOptions.inSampleSize = 1;
        }
        if (outHeight == 1) {
            bitmapFactoryOptions.inSampleSize = (int) Math.round(xRatio);
        }
        if (outWidth == 1) {
            bitmapFactoryOptions.inSampleSize = (int) Math.round(yRatio);
        }

        bitmapFactoryOptions.inJustDecodeBounds = false;

        return bitmapFactoryOptions;
    }

    /**
     * bitmap转换为圆角 服务端统计的崩溃日志，有相当多的trying to use a recycled
     * bitmap，从这个方法抛出，因此加了异常捕获
     *
     * @param bitmap
     * @param pixels
     * @return
     */
    public static Bitmap toRoundCorner(Bitmap bitmap, int pixels) {
        if (bitmap == null || bitmap.isRecycled())
            return null;
        Bitmap output = null;

        try {
            output = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(output);

            final int color = 0xff424242;
            final Paint paint = new Paint();
            final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
            final RectF rectF = new RectF(rect);
            final float roundPx = pixels;

            paint.setAntiAlias(true);
            canvas.drawARGB(0, 0, 0, 0);
            paint.setColor(color);
            canvas.drawRoundRect(rectF, roundPx, roundPx, paint);

            paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
            canvas.drawBitmap(bitmap, rect, rect, paint);
            bitmap.recycle();
            canvas = null;
        } catch (OutOfMemoryError e) {
            e.printStackTrace();
        }
        return output;
    }
    public static String compressImage(String filePath, String targetPath, int quality)  {
        Bitmap bm = getSmallBitmap(filePath);//获取一定尺寸的图片
        int degree = readPictureDegree(filePath);//获取相片拍摄角度
        if(degree!=0){//旋转照片角度，防止头像横着显示
            bm=rotateBitmap(bm,degree);
        }
        File outputFile=new File(targetPath);
        try {
            if (!outputFile.exists()) {
                outputFile.getParentFile().mkdirs();
                //outputFile.createNewFile();
            }else{
                outputFile.delete();
            }
            FileOutputStream out = new FileOutputStream(outputFile);
            bm.compress(Bitmap.CompressFormat.JPEG, quality, out);
        }catch (Exception e){}
        return outputFile.getPath();
    }

    /**
     * 将图片转换成Base64编码的字符串
     */
    public static String imageToBase64(String path){
        if(TextUtils.isEmpty(path)){
            return null;
        }
        InputStream is = null;
        byte[] data = null;
        String result = null;
        try{
            is = new FileInputStream(path);
            //创建一个字符流大小的数组。
            data = new byte[is.available()];
            //写入数组
            is.read(data);
            //用默认的编码格式进行编码
            result = Base64.encodeToString(data,Base64.DEFAULT);
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            if(null !=is){
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }
        return result;
    }

    /**
     * 根据路径获得图片信息并按比例压缩，返回bitmap
     */
    public static Bitmap getSmallBitmap(String filePath) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;//只解析图片边沿，获取宽高
        BitmapFactory.decodeFile(filePath, options);
        // 计算缩放比
        options.inSampleSize = calculateInSampleSize(options, 1080, 1920);
        // 完整解析图片返回bitmap
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(filePath, options);
    }

    /**
     * 获取照片角度
     * @param path
     * @return
     */
    public static int readPictureDegree(String path) {
        int degree = 0;
        try {
            ExifInterface exifInterface = new ExifInterface(path);
            int orientation = exifInterface.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL);
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


    public static int calculateInSampleSize(BitmapFactory.Options options,
                                            int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;
        if (height > reqHeight || width > reqWidth) {
            final int heightRatio = Math.round((float) height / (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
        }
        return inSampleSize;
    }
    /**
     * 旋转照片
     * @param bitmap
     * @param degress
     * @return
     */
    public static Bitmap rotateBitmap(Bitmap bitmap, int degress) {
        if (bitmap != null) {
            Matrix m = new Matrix();
            m.postRotate(degress);
            bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(),
                    bitmap.getHeight(), m, true);
            return bitmap;
        }
        return bitmap;
    }


}
