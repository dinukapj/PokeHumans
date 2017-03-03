package pokehumans.dinster.com.pokehumans.activities;

import pokehumans.dinster.com.pokehumans.services.*;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.ErrorCallback;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.ShutterCallback;
import android.media.ExifInterface;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;

import pokehumans.dinster.com.pokehumans.R;

public class HuntingActivity extends Activity {

    Activity context;
    Preview preview;
    Camera camera;
    String path = "/sdcard/PokeHumans/cache/images/";
    GestureDetector mDetector;
    ImageView pokeball, ivCapturedIcon, ivEscapedIcon;
    RelativeLayout rlSuccess, rlFailure, btnTryAgain, btnContinue;
    Animation wobble;
    MediaPlayer mediaPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        context = this;

        preview = new Preview(this,
                (SurfaceView) findViewById(R.id.KutCameraFragment));
        FrameLayout frame = (FrameLayout) findViewById(R.id.preview);
        frame.addView(preview);
        preview.setKeepScreenOn(true);

        pokeball = (ImageView) findViewById(R.id.pokeball);
        rlSuccess = (RelativeLayout) findViewById(R.id.rlSuccess);
        rlFailure = (RelativeLayout) findViewById(R.id.rlFailure);
        ivCapturedIcon = (ImageView) findViewById(R.id.ivCapturedIcon);
        ivEscapedIcon = (ImageView) findViewById(R.id.ivEscapedIcon);
        btnTryAgain = (RelativeLayout) findViewById(R.id.btnTryAgain);
        btnContinue = (RelativeLayout) findViewById(R.id.btnContinue);

        rlSuccess.setVisibility(View.GONE);
        rlFailure.setVisibility(View.GONE);

        SimpleGestureListener simpleGestureListener = new SimpleGestureListener();
        simpleGestureListener.setListener(new SimpleGestureListener.Listener() {
            @Override
            public void onScrollHorizontal(float dx) {
            }

            @Override
            public void onScrollVertical(float dy) {
                if(dy > 0){
                    mediaPlayer = MediaPlayer.create(HuntingActivity.this, R.raw.throw_1);
                    mediaPlayer.start();
                    Animation rotate = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.anim_throw);
                    pokeball.setAnimation(rotate);
                    pokeball.startAnimation(rotate);
                    rotate.setAnimationListener(new Animation.AnimationListener() {
                        @Override
                        public void onAnimationStart(Animation animation) {

                        }

                        @Override
                        public void onAnimationEnd(Animation animation) {
                            pokeball.setVisibility(View.GONE);
                            DetermineOutcome();
                        }

                        @Override
                        public void onAnimationRepeat(Animation animation) {

                        }
                    });
                }
            }
        });
        mDetector = new GestureDetector(this, simpleGestureListener);


        btnTryAgain.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {

                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        btnTryAgain.setBackgroundResource(R.drawable.white_button_touched);
                        break;
                    case MotionEvent.ACTION_CANCEL:
                        btnTryAgain.setBackgroundResource(R.drawable.white_button);
                        break;
                    case MotionEvent.ACTION_UP:
                        btnTryAgain.setBackgroundResource(R.drawable.white_button);

                        wobble.cancel();
                        Animation animation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.anim_fade_out);
                        rlFailure.setAnimation(animation);
                        rlFailure.setVisibility(View.GONE);
                        pokeball.setVisibility(View.VISIBLE);

                        mediaPlayer = MediaPlayer.create(HuntingActivity.this, R.raw.escaped);
                        mediaPlayer.start();

                        break;
                }

                return true;
            }
        });

        btnContinue.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {

                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        btnContinue.setBackgroundResource(R.drawable.white_button_touched);
                        break;
                    case MotionEvent.ACTION_CANCEL:
                        btnContinue.setBackgroundResource(R.drawable.white_button);
                        break;
                    case MotionEvent.ACTION_UP:
                        btnContinue.setBackgroundResource(R.drawable.white_button);

                        wobble.cancel();
                        Animation animation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.anim_fade_out);
                        rlSuccess.setAnimation(animation);
                        rlSuccess.setVisibility(View.GONE);
                        pokeball.setVisibility(View.VISIBLE);

                        mediaPlayer = MediaPlayer.create(HuntingActivity.this, R.raw.buttonclick);
                        mediaPlayer.start();

                        break;
                }

                return true;
            }
        });

    }

    private void DetermineOutcome() {
        int result = (int)(Math.random() * 2);
        if(result == 0){
            rlFailure.setVisibility(View.VISIBLE);
            Animation animation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.anim_fade_in);
            rlFailure.setAnimation(animation);
            wobble = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.anim_wobble);
            ivEscapedIcon.setAnimation(wobble);
        }
        else{
            rlSuccess.setVisibility(View.VISIBLE);
            Animation animation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.anim_fade_in);
            rlSuccess.setAnimation(animation);
            wobble = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.anim_wobble);
            ivCapturedIcon.setAnimation(wobble);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mDetector.onTouchEvent(event);
        return super.onTouchEvent(event);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (camera == null) {
            camera = Camera.open();
            camera.startPreview();
            camera.setErrorCallback(new ErrorCallback() {
                public void onError(int error, Camera mcamera) {

                    camera.release();
                    camera = Camera.open();
                    Log.d("Camera died", "error camera");

                }
            });
        }
        if (camera != null) {
            if (Build.VERSION.SDK_INT >= 14)
                setCameraDisplayOrientation(context,
                        CameraInfo.CAMERA_FACING_BACK, camera);
            preview.setCamera(camera);
        }
    }

    private void setCameraDisplayOrientation(Activity activity, int cameraId,
                                             android.hardware.Camera camera) {
        android.hardware.Camera.CameraInfo info = new android.hardware.Camera.CameraInfo();
        android.hardware.Camera.getCameraInfo(cameraId, info);
        int rotation = activity.getWindowManager().getDefaultDisplay()
                .getRotation();
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break;
            case Surface.ROTATION_90:
                degrees = 90;
                break;
            case Surface.ROTATION_180:
                degrees = 180;
                break;
            case Surface.ROTATION_270:
                degrees = 270;
                break;
        }

        int result;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360; // compensate the mirror
        } else { // back-facing
            result = (info.orientation - degrees + 360) % 360;
        }
        camera.setDisplayOrientation(result);
    }


    Camera.AutoFocusCallback mAutoFocusCallback = new Camera.AutoFocusCallback() {
        @Override
        public void onAutoFocus(boolean success, Camera camera) {

            try {
                camera.takePicture(mShutterCallback, null, jpegCallback);
            } catch (Exception e) {

            }

        }
    };

    Camera.ShutterCallback mShutterCallback = new ShutterCallback() {

        @Override
        public void onShutter() {
            // TODO Auto-generated method stub

        }
    };

    public void takeFocusedPicture() {
        camera.autoFocus(mAutoFocusCallback);

    }

    PictureCallback rawCallback = new PictureCallback() {
        public void onPictureTaken(byte[] data, Camera camera) {
            // Log.d(TAG, "onPictureTaken - raw");
        }
    };

    PictureCallback jpegCallback = new PictureCallback() {
        @SuppressWarnings("deprecation")
        public void onPictureTaken(byte[] data, Camera camera) {

            FileOutputStream outStream = null;
            Calendar c = Calendar.getInstance();
            File videoDirectory = new File(path);

            if (!videoDirectory.exists()) {
                videoDirectory.mkdirs();
            }

            try {
                // Write to SD Card
                outStream = new FileOutputStream(path + c.getTime().getSeconds() + ".jpg");
                outStream.write(data);
                outStream.close();


            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {

            }


            Bitmap realImage;
            final BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = 5;

            options.inPurgeable = true;                   //Tell to gc that whether it needs free memory, the Bitmap can be cleared

            options.inInputShareable = true;              //Which kind of reference will be used to recover the Bitmap data after being clear, when it will be used in the future


            realImage = BitmapFactory.decodeByteArray(data, 0, data.length, options);
            ExifInterface exif = null;
            try {
                exif = new ExifInterface(path + c.getTime().getSeconds()
                        + ".jpg");
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            try {
                Log.d("EXIF value",
                        exif.getAttribute(ExifInterface.TAG_ORIENTATION));
                if (exif.getAttribute(ExifInterface.TAG_ORIENTATION)
                        .equalsIgnoreCase("1")) {
                    realImage = rotate(realImage, 90);
                } else if (exif.getAttribute(ExifInterface.TAG_ORIENTATION)
                        .equalsIgnoreCase("8")) {
                    realImage = rotate(realImage, 90);
                } else if (exif.getAttribute(ExifInterface.TAG_ORIENTATION)
                        .equalsIgnoreCase("3")) {
                    realImage = rotate(realImage, 90);
                } else if (exif.getAttribute(ExifInterface.TAG_ORIENTATION)
                        .equalsIgnoreCase("0")) {
                    realImage = rotate(realImage, 90);
                }
            } catch (Exception e) {

            }

            //image.setImageBitmap(realImage);


            //fotoButton.setClickable(true);
            //camera.startPreview();
            //progressLayout.setVisibility(View.GONE);
            //exitButton.setClickable(true);

        }
    };

    public static Bitmap rotate(Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(),
                source.getHeight(), matrix, false);
    }

}