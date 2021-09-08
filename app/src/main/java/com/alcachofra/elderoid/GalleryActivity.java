package com.alcachofra.elderoid;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.alcachofra.elderoid.utils.ElderoidActivity;
import com.alcachofra.elderoid.utils.dialog.DialogOption;
import com.alcachofra.elderoid.utils.netie.Cue;
import com.alcachofra.elderoid.utils.FileInfo;
import com.alcachofra.elderoid.utils.netie.Netie;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

public class GalleryActivity extends ElderoidActivity {

    Netie netie;
    RecyclerView recycler;
    final int REQUEST_IMAGE_CAPTURE = 1;
    final String IMAGES_FOLDER_NAME = "Camera";
    GalleryAdapter adapter;
    boolean comingFromCamera = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);



        recycler = findViewById(R.id.photo_grid);

        List<String> photos;
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE}, Elderoid.PERMISSIONS);
            return;
        }
        else {
            photos = getPhotos();
        }
        if (getIntent().getExtras() != null && getIntent().getExtras().getString(Elderoid.COMING_FROM, "").equals("CAMERA")) {
            netie = new Netie(
                    this,
                    Netie.NetieWindowType.WITH_HOME_BUTTON,
                    Arrays.asList(
                            new Cue(Elderoid.string(R.string.gallery_1), R.drawable.netie_happy),
                            new Cue(Elderoid.string(R.string.gallery_2), R.drawable.netie)
                                    .setOption1(Elderoid.string(R.string.yes), v -> enterCamera())
                    ),
                    true
            );
            netie.setBalloon(Elderoid.string(R.string.photo_saved_gallery))
                    .setExpression(R.drawable.netie_happy);
        }
        else if (photos != null && photos.size() > 0) netie = new Netie(
                this,
                Netie.NetieWindowType.WITH_HOME_BUTTON,
                Arrays.asList(
                        new Cue(Elderoid.string(R.string.gallery_1), R.drawable.netie_happy),
                        new Cue(Elderoid.string(R.string.gallery_2), R.drawable.netie)
                                .setOption1(Elderoid.string(R.string.yes), v -> enterCamera())
                ),
                true
        );
        else netie = new Netie(
                this,
                Netie.NetieWindowType.WITH_HOME_BUTTON,
                Arrays.asList(
                        new Cue(Elderoid.string(R.string.no_photos), R.drawable.netie_confused)
                                .setOption1(Elderoid.string(R.string.yes), v -> enterCamera())
                ),
                false
        );

        System.out.println(photos);
        recycler.setHasFixedSize(true);
        recycler.setLayoutManager(new GridLayoutManager(this, 2));
        adapter = new GalleryActivity.GalleryAdapter(photos);
        recycler.setAdapter(adapter);
    }

    public void enterCamera() {
        comingFromCamera = true;
        PackageManager pm = getPackageManager();
        if (pm.hasSystemFeature(PackageManager.FEATURE_CAMERA)) takePhoto();
        else netie.setBalloon(Elderoid.string(R.string.camera_not_working))
                .setExpression(R.drawable.netie_concerned);
    }

    private void takePhoto() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        Uri uri;
        // Ensure that there's a camera activity to handle the intent:
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ContentResolver resolver = getContentResolver();
                ContentValues contentValues = new ContentValues();

                contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, getFileName()); // File name
                contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg"); // File extension
                contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, "DCIM/Camera"); // Directory

                uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues); // Uri
            }
            else {
                // Create the File where the photo should go
                File photoFile = getFile();

                // Continue only if the File was successfully created
                if (photoFile != null) {
                    uri = FileProvider.getUriForFile(this,
                            BuildConfig.APPLICATION_ID + ".provider",
                            photoFile);
                }
                else {
                    netie.setBalloon(Elderoid.string(R.string.no_camera))
                            .setExpression(R.drawable.netie_concerned);
                    return;
                }
            }

            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    private File getFile() {
        // File object to be returned:
        File jpgFile;

        // Create an image file name with datestamp and labels:
        String timeStamp = getFileName();
        String imageFileName = timeStamp + ".label1";

        File externalFilesDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);

        String stStorageDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DCIM).toString() + File.separator + IMAGES_FOLDER_NAME;
        if (!new File(stStorageDir).exists()) {
            new File(stStorageDir).mkdir();
        }
        jpgFile = new File(stStorageDir, imageFileName + ".jpg");
        return jpgFile;
    }

    @SuppressLint("SimpleDateFormat")
    private String getFileName() {
        return "IMG_" + new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_IMAGE_CAPTURE:
                if (resultCode == RESULT_OK) {
                    Intent intent = new Intent(getApplicationContext(), GalleryActivity.class);
                    intent.putExtra(Elderoid.COMING_FROM, "CAMERA");
                    startActivity(intent);
                }
                else if (resultCode == RESULT_CANCELED)
                    netie.setBalloon(Elderoid.string(R.string.photo_canceled))
                            .setExpression(R.drawable.netie_confused);
                else
                    netie.setBalloon(Elderoid.string(R.string.photo_save_error))
                            .setExpression(R.drawable.netie_concerned);
                break;
        }
    }

    public List<String> getPhotos() {
        Uri u = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        String[] projection = {MediaStore.Images.ImageColumns.DATA};
        Cursor cursor = null;
        SortedSet<String> dirList = new TreeSet<>();
        ArrayList<FileInfo> files = new ArrayList<>();
        ArrayList<String> result = new ArrayList<>();

        String[] directories = null;
        if (u != null) cursor = getContentResolver().query(u, projection, null, null, null);

        if (cursor == null) return result;

        if (cursor.moveToFirst()) {
            do {
                String tempDir = cursor.getString(0);
                tempDir = tempDir.substring(0, tempDir.lastIndexOf("/"));
                try {
                    dirList.add(tempDir);
                } catch(Exception e) {
                    e.printStackTrace();
                }
            }
            while (cursor.moveToNext());
            directories = new String[dirList.size()];
            dirList.toArray(directories);
        }

        cursor.close();

        for(int i = 0; i < dirList.size(); i++)  {
            File imageDir = new File(directories[i]);
            File[] imageList = imageDir.listFiles();
            if (imageList == null) continue;
            for (File imagePath : imageList) {
                try {
                    if (imagePath.isDirectory()) imageList = imagePath.listFiles();
                    if (imagePath.getName().contains(".jpg")|| imagePath.getName().contains(".JPG")
                            || imagePath.getName().contains(".jpeg")|| imagePath.getName().contains(".JPEG")
                            || imagePath.getName().contains(".png") || imagePath.getName().contains(".PNG")
                            || imagePath.getName().contains(".gif") || imagePath.getName().contains(".GIF")
                            || imagePath.getName().contains(".bmp") || imagePath.getName().contains(".BMP")
                    ) {
                        String path = imagePath.getAbsolutePath();
                        long time = imagePath.lastModified();
                        files.add(new FileInfo(path, time));
                    }
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        Collections.sort(files, Collections.reverseOrder());
        for (FileInfo file : files) {
            result.add(file.getPath());
        }
        return result;
    }

    private boolean removeFile(File file) {
        return file.delete();
    }

    class GalleryAdapter extends RecyclerView.Adapter<GalleryAdapter.PhotoViewHolder> {
        private final List<String> photos;

        class PhotoViewHolder extends RecyclerView.ViewHolder {
            final AppCompatImageView image;
            final AppCompatImageButton thrash;

            public PhotoViewHolder(ViewGroup container) {
                super(LayoutInflater.from(GalleryActivity.this).inflate(R.layout.cell_photo, container, false));
                image = itemView.findViewById(R.id.image);
                thrash = itemView.findViewById(R.id.thrash);
            }
        }

        public GalleryAdapter(List<String> photos) {
            super();
            this.photos = photos;
        }

        @NonNull
        @Override
        public GalleryAdapter.PhotoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new GalleryAdapter.PhotoViewHolder(parent);
        }

        @Override
        public void onBindViewHolder(@NonNull GalleryAdapter.PhotoViewHolder holder, int position) {
            RequestOptions requestOptions = new RequestOptions().transform(new CenterCrop(), new RoundedCorners(16));

            String photo = photos.get(position);

            Glide.with(Elderoid.getContext())
                    .load(photo)
                    .apply(requestOptions)
                    .into(holder.image);

            holder.image.setOnClickListener(v -> {
                Intent intent = new Intent(getApplicationContext(), PhotoActivity.class);
                intent.putExtra(Elderoid.IMAGE_FILE_NAME, photo);
                startActivity(intent);
            });

            holder.thrash.setOnClickListener(v -> {
                DialogOption dialog = new DialogOption(Elderoid.string(R.string.dialog_remove_photo),
                        Elderoid.string(R.string.dialog_remove_photo_message),
                        Elderoid.string(R.string.dialog_remove),
                        Elderoid.string(R.string.dialog_cancel),
                        (d, w) -> {
                            if (removeFile(new File(photo))) removeAt(position);
                        },
                        (d, w) -> {}
                );
                dialog.show(getSupportFragmentManager(), "Delete Photo Dialog");
            });
        }

        @Override
        public int getItemCount() {
            return this.photos.size();
        }

        public void removeAt(int position) {
            photos.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, photos.size());
        }
    }
}