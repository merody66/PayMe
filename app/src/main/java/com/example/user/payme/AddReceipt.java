package com.example.user.payme;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;


public class AddReceipt extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private static final int requestPermissionID = 101;
    private static final int writePermissionID = 102;

    private SurfaceView mCameraView;
    private CameraSource mCameraSource;
    private ImageButton mTakePicture;
    private String imagePath = "do not exist";


    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    Intent intent = new Intent(AddReceipt.this, MainActivity.class);
                    startActivity(intent);
                    return true;
                case R.id.navigation_history:
                    return true;
                case R.id.navigation_addNewReceipt:
                    return true;
                case R.id.navigation_contacts:
                    return true;
                case R.id.navigation_account:
                    return true;
            }
            return false;
        }
    };

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            try {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                mCameraSource.start(mCameraView.getHolder());
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (grantResults[1] == PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
        }
    }

    private void startCameraSource() {

        //Create the TextRecognizer
        final TextRecognizer textRecognizer = new TextRecognizer.Builder(getApplicationContext()).build();

        if (!textRecognizer.isOperational()) {
            Log.w(TAG, "Detector dependencies not loaded yet");
        } else {

            //Initialize camerasource to use high resolution and set Autofocus on.
            mCameraSource = new CameraSource.Builder(getApplicationContext(), textRecognizer)
                    .setFacing(CameraSource.CAMERA_FACING_BACK)
                    .setRequestedPreviewSize(1280, 1024)
                    .setAutoFocusEnabled(true)
                    .setRequestedFps(2.0f)
                    .build();

            /**
             * Add call back to SurfaceView and check if camera permission is granted.
             * If permission is granted we can start our cameraSource and pass it to surfaceView
             */
            mCameraView.getHolder().addCallback(new SurfaceHolder.Callback() {
                @Override
                public void surfaceCreated(SurfaceHolder holder) {
                    try {

                        if (ActivityCompat.checkSelfPermission(getApplicationContext(),
                                Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {

                            ActivityCompat.requestPermissions(AddReceipt.this,
                                    new String[]{Manifest.permission.CAMERA},
                                    requestPermissionID);
                            return;
                        }
                        mCameraSource.start(mCameraView.getHolder());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
                }

                @Override
                public void surfaceDestroyed(SurfaceHolder holder) {
                    mCameraSource.stop();
                }
            });

            //Set the TextRecognizer's Processor.
            textRecognizer.setProcessor(new Detector.Processor<TextBlock>() {
                @Override
                public void release() {
                }

                /**
                 * Detect all the text from camera using TextBlock and the values into a stringBuilder
                 * which will then be set to the textView.
                 * */
                @Override
                public void receiveDetections(Detector.Detections<TextBlock> detections) {
//                    final SparseArray<TextBlock> items = detections.getDetectedItems();
//                    if (items.size() != 0) {
//
//                        mTextView.post(new Runnable() {
//                            @Override
//                            public void run() {
//                                StringBuilder stringBuilder = new StringBuilder();
//                                for (int i = 0; i < items.size(); i++) {
//                                    TextBlock item = items.valueAt(i);
//                                    stringBuilder.append(item.getValue());
//                                    stringBuilder.append("\n");
//                                }
//                                mTextView.setText(stringBuilder.toString());
//                            }
//                        });
//                    }
                }
            });
        }
    }

    private String getPhotoTime(){
        SimpleDateFormat sdf=new SimpleDateFormat("ddMMyy_hhmmss");
        return sdf.format(new Date());
    }

    // Callback for 'takePicture'
    CameraSource.PictureCallback pictureCallback = new CameraSource.PictureCallback() {
        @Override
        public void onPictureTaken(byte[] bytes) {

            ActivityCompat.requestPermissions(AddReceipt.this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    writePermissionID);

            try {
                String mainpath = Environment.getExternalStorageDirectory() + "/" + Environment.DIRECTORY_DCIM + "/PayMe";
                File basePath = new File(mainpath);
                if (!basePath.exists())
                    Log.d("CAPTURE_BASE_PATH", basePath.mkdirs() ? "Success": "Failed");
                File captureFile = new File(mainpath + "/receipt_" + getPhotoTime() + ".jpg");
                if (!captureFile.exists())
                    Log.d("CAPTURE_FILE_PATH", captureFile.createNewFile() ? "Success": "Failed");

                Log.d(TAG, "onPictureTaken: " + captureFile);
                FileOutputStream stream = new FileOutputStream(captureFile);
                stream.write(bytes);
                stream.flush();
                stream.close();
                Toast.makeText(getApplicationContext(),"Successfully saved at: "+ captureFile.toString(),Toast.LENGTH_LONG).show();

                // send image info to fragment AddShow
                Bundle bundle = new Bundle();
                bundle.putString("imagepath", captureFile.toString());
                imagePath = captureFile.toString();

                Intent intent = new Intent(AddReceipt.this, ShowActivity.class);
                intent.putExtra("imagePath", captureFile.toString());
                startActivity(intent);

            } catch (IOException e) {
                e.printStackTrace();
            } catch (Exception exception) {
                    Toast.makeText(getApplicationContext(),"Error saving: "+ exception.toString(),Toast.LENGTH_LONG).show();
            }
        }
    };

    public String getImagePath() {
        return imagePath;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.manual_appbar, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_manual:
                return true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                Log.d(TAG, "onOptionsItemSelected: default aosdnaksjndkjansd");
                return super.onOptionsItemSelected(item);

        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_receipt);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.toolbarEdit);
        setSupportActionBar(myToolbar);

        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setSelectedItemId(R.id.navigation_addNewReceipt);
        BottomNavigationViewHelper.disableShiftMode(navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        mCameraView = (SurfaceView) findViewById(R.id.surfaceView);
        mTakePicture = (ImageButton) findViewById(R.id.cameraButton);


        startCameraSource();
        mTakePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCameraSource.takePicture(null, pictureCallback);
//                mCameraSource.stop();

            }
        });
    }
}
