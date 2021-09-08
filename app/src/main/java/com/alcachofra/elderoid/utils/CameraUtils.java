package com.alcachofra.elderoid.utils;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.PackageManager;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

import androidx.core.content.FileProvider;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class CameraUtils {

    // Activity request codes
    public static final int CAMERA_CAPTURE_IMAGE_REQUEST_CODE = 100;
    public static final int MEDIA_TYPE_IMAGE = 1;

    // Gallery directory name to store the images or videos
    public static final String GALLERY_DIRECTORY_NAME = "Hello Camera";

    // Image and Video file extensions
    public static final String IMAGE_EXTENSION = "jpg";

    /**
     * Refreshes gallery on adding new image/video. Gallery won't be refreshed on older devices until device is rebooted.
     * @param context
     * @param filePath
     */
    public static void refreshGallery(Context context, String filePath) {
        addImageToGallery(context, filePath);
        // ScanFile so it will be appeared on Gallery
        MediaScannerConnection.scanFile(context,
                new String[]{filePath}, null,
                (path, uri) -> {
                });
    }

    /**
     * Add image to storage gallery.
     * @param context Context.
     * @param filePath Path of image file.
     */
    private static void addImageToGallery(Context context, String filePath) {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.DATA, filePath);
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg"); // or image/png
        context.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
    }

    /**
     * Checks whether the device has camera or not.
     * @param context Context.
     * @return True if device supports camera. False otherwise.
     */
    public static boolean isCameraAvailable(Context context) {
        return context.getPackageManager().hasSystemFeature(
                PackageManager.FEATURE_CAMERA);
    }

    /**
     * Get URI for File.
     * @param context Context.
     * @param file File to request URI for.
     * @return URI.
     */
    public static Uri getOutputMediaFileUri(Context context, File file) {
        return FileProvider.getUriForFile(context, context.getPackageName() + ".provider", file);
    }

    /**
     * Creates and returns the image or video file before opening the camera
     * @param type Type (MEDIA_TYPE_IMAGE is the only one available at the moment)
     * @return File (video or image).
     */
    public static File getOutputMediaFile(int type) {

        // External sdcard location
        File mediaStorageDir = new File(
                Environment
                        .getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                GALLERY_DIRECTORY_NAME);

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.e(GALLERY_DIRECTORY_NAME, "Oops! Failed create "
                        + GALLERY_DIRECTORY_NAME + " directory");
                return null;
            }
        }

        File mediaFile;
        if (type == MEDIA_TYPE_IMAGE) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator
                    + getFileName() + "." + IMAGE_EXTENSION);
        } else {
            return null;
        }

        return mediaFile;
    }

    /**
     * Get file name according to Date and Time (so it's always different, and easily sorted)
     * @return String with file name.
     */
    @SuppressLint("SimpleDateFormat")
    private static String getFileName() {
        return "IMG_" + new SimpleDateFormat("yyyyMMdd_HHmmss",
                Locale.getDefault()).format(new Date());
    }
}
