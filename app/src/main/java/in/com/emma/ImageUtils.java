package in.com.emma;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by sunny on 21/7/16.
 */
public class ImageUtils {
    public static final String TEMP_PNG = "temp.png";
    public static final String YYYY_M_MDD_H_HMMSS = "yyyyMMdd_HHmmss";

    public static String createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat(YYYY_M_MDD_H_HMMSS).format(new Date());
        return ("JPEG_" + timeStamp+".png");
    }


    public static Drawable getDrawableFromUri(Context context, Uri imageUri) {
        Drawable drawable;
        try {
            InputStream inputStream = context.getContentResolver().openInputStream(imageUri);
            drawable = Drawable.createFromStream(inputStream, imageUri.toString());
        } catch (FileNotFoundException e) {
            drawable = context.getResources().getDrawable(R.mipmap.ic_launcher);
        }
        return drawable;
    }





    public static String getRealPathFromURI(Context context, Uri contentURI) {
        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {
                column
        };

        try {
            cursor = context.getContentResolver().query(contentURI, projection, null, null,
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                final int index = cursor.getColumnIndex(column);
                if(index != -1)
                return cursor.getString(index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return contentURI.getPath();
    }


    public static Bitmap getBitmapFromUri(Context context, Uri imageUri) throws IOException {
        return exifAdjustment(getRealPathFromURI(context,imageUri), MediaStore.Images.Media.getBitmap(context.getContentResolver(), imageUri));
    }


    public static Bitmap getBitmapFromUri(Context context, String path, Uri imageUri) throws IOException {
        return exifAdjustment(path, MediaStore.Images.Media.getBitmap(context.getContentResolver(), imageUri));
    }

    public static File ResizeBitmap(Context context, String path) {
        File file = null;
        final File root = getRootFolder(context);

        Bitmap b = BitmapFactory.decodeFile(path);

        if(b != null) {
            int newWidth = 600;
            int newHeight = 400;

            int origWidth = b.getWidth();
            int origHeight = b.getHeight();


            if (newWidth < newHeight) {
                newHeight = Math.round(newWidth * ((float) origHeight / origWidth));
            } else if (newHeight < newWidth) {
                newWidth = Math.round(newHeight * ((float) origWidth / origHeight));
            }


            Bitmap out = Bitmap.createScaledBitmap(b, newWidth, newHeight, false);
            out = exifAdjustment(path,out);
            file = new File(root, TEMP_PNG);
            FileOutputStream fOut;
            try {
                fOut = new FileOutputStream(file);
                out.compress(Bitmap.CompressFormat.PNG, 100, fOut);
                fOut.flush();
                fOut.close();
                b.recycle();
                out.recycle();
            } catch (Exception e) {
            }
        }
        return file;
    }

    @NonNull
    public static File getRootFolder(Context context) {
        final File root = new File(Environment.getExternalStorageDirectory()
                + File.separator
                + context.getApplicationContext().getString(R.string.app_name)
                + File.separator);
        if (!root.exists())
            root.mkdirs();
        return root;
    }


    public static Bitmap exifAdjustment(String path, Bitmap correctedBitmap) {
        if(TextUtils.isEmpty(path) || correctedBitmap == null) {
            return null;
        }
        Matrix matrix = new Matrix();
        ExifInterface ei = null;
        try {
            ei = new ExifInterface(path);
        } catch (IOException e) {
            e.printStackTrace();
        }
        int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

        switch (orientation) {
            case ExifInterface.ORIENTATION_ROTATE_90:
                matrix.postRotate(90);
                correctedBitmap = Bitmap.createBitmap(correctedBitmap, 0, 0, correctedBitmap.getWidth(), correctedBitmap.getHeight(), matrix, true);
                break;

            case ExifInterface.ORIENTATION_ROTATE_180:
                matrix.postRotate(180);
                correctedBitmap = Bitmap.createBitmap(correctedBitmap, 0, 0, correctedBitmap.getWidth(), correctedBitmap.getHeight(), matrix, true);
                break;

            case ExifInterface.ORIENTATION_ROTATE_270:
                matrix.postRotate(270);
                correctedBitmap = Bitmap.createBitmap(correctedBitmap, 0, 0, correctedBitmap.getWidth(), correctedBitmap.getHeight(), matrix, true);
                break;


        }
        return correctedBitmap;

    }


    public static int manipulateColor(int color, float factor) {
        int a = Color.alpha(color);
        int r = Math.round(Color.red(color) * factor);
        int g = Math.round(Color.green(color) * factor);
        int b = Math.round(Color.blue(color) * factor);
        return Color.argb(a,
                Math.min(r,255),
                Math.min(g,255),
                Math.min(b,255));
    }
}
