package com.example.user.payme.Objects;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.media.ExifInterface;
import android.util.Log;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Image {
    private static final String TAG = "Image";
    private Bitmap mBitmap;
    private Context mContext;
    private String imagePath;

    public Image(Context mContext, String imagePath) {
        this.mContext = mContext;
        this.imagePath = imagePath;
        Log.d(TAG, "Image: constructor");
    }

    public Bitmap getmBitmap() {
        // Check if image came from camera or gallery
        Pattern imageExtensionPattern = Pattern.compile("\\.(gif|jpg|jpeg|tiff|png)", Pattern.CASE_INSENSITIVE);
        Matcher imageMatcher = imageExtensionPattern.matcher(imagePath);

        if (imageMatcher.find()) {
            this.mBitmap = rotateImage(imagePath);
        } else {
            Uri imageUri = Uri.parse(imagePath);
            try {
                this.mBitmap = MediaStore.Images.Media.getBitmap(mContext.getContentResolver(), imageUri);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return this.mBitmap;
    }

    private Bitmap rotateImage(String imagePath) {
        Bitmap myBitmap = null;
        try {
            ExifInterface exif = new ExifInterface(imagePath);
            Matrix matrix = new Matrix();
            
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            
            switch (orientation){
                case ExifInterface.ORIENTATION_ROTATE_90:
                    matrix.postRotate(90);
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    matrix.postRotate(180);
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    matrix.postRotate(270);
                    break;
                default:
                    break;
            }

            myBitmap = BitmapFactory.decodeFile(imagePath);
            myBitmap = Bitmap.createBitmap(myBitmap, 0 ,0, myBitmap.getWidth(), myBitmap.getHeight(), matrix, true);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return myBitmap;
    }

}
