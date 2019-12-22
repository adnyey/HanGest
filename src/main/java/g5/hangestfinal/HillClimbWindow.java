package g5.hangestfinal;

import android.Manifest;
import android.app.Instrumentation;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.SurfaceTexture;
import android.graphics.Typeface;
import android.hardware.Camera;
import android.media.Image;
import android.media.ImageReader;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.SystemClock;
import android.os.Trace;
import android.support.annotation.Nullable;
import android.util.Log;
import android.util.Size;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.TextureView;
import android.widget.TextView;
import android.widget.Toast;

import net.mready.hover.HoverWindow;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Vector;

import g5.hangestfinal.env.BorderedText;
import g5.hangestfinal.env.ImageUtils;
import g5.hangestfinal.env.Logger;

import static g5.hangestfinal.Randomizer.THRESHOLD;

/**
 * Created by Mahajan-PC on 2018-02-03.
 */

public class HillClimbWindow extends BaseWindow implements ImageReader.OnImageAvailableListener, Camera.PreviewCallback {

    private static final Logger LOGGER = new Logger();

    private static final int PERMISSIONS_REQUEST = 1;

    private static final String PERMISSION_CAMERA = Manifest.permission.CAMERA;
    private static final String PERMISSION_STORAGE = Manifest.permission.WRITE_EXTERNAL_STORAGE;
    private boolean debug = false;

    private Handler handler;
    private HandlerThread handlerThread;
    private boolean useCamera2API;
    private boolean isProcessingFrame = false;
    private byte[][] yuvBytes = new byte[3][];
    private int[] rgbBytes = null;
    private int yRowStride;

    protected int previewWidth = 0;
    protected int previewHeight = 0;

    private Runnable postInferenceCallback;
    private Runnable imageConverter;

    protected static final boolean SAVE_PREVIEW_BITMAP = false;

    private ResultsView resultsView;

    private Bitmap rgbFrameBitmap = null;
    private Bitmap croppedBitmap = null;
    private Bitmap cropCopyBitmap = null;

    private long lastProcessingTimeMs;

    private static final int INPUT_SIZE = 128;
    private static final int IMAGE_MEAN = 128;
    private static final float IMAGE_STD = 128;
    private static final String INPUT_NAME = "input";
    private static final String OUTPUT_NAME = "final_result";
    private String[] gestures = {"five", "fist", "c", "palm", "v", "point"};

    private static final String MODEL_FILE = "file:///android_asset/retrained_graph.pb";
    private static final String LABEL_FILE =
            "file:///android_asset/retrained_labels.txt";


    private static final boolean MAINTAIN_ASPECT = true;

    private static final Size DESIRED_PREVIEW_SIZE = new Size(640, 480);


    private Integer sensorOrientation;
    private Classifier classifier;
    private Matrix frameToCropTransform;
    TextView ques;
    private Matrix cropToFrameTransform;


    private BorderedText borderedText;

    Instrumentation m_Instrumentation = new Instrumentation();
    private boolean m_bDoInject = false, m_bRunning = true;

    @Override
    protected void onCreate(@Nullable Bundle arguments) {
        super.onCreate(arguments);

        addFlags(HoverWindow.FLAG_MOVABLE);

        setTheme(R.style.MaterialTheme);
        setContentView(R.layout.camera_connection_fragment);
        textureView = (AutoFitTextureView) findViewById(R.id.texture);

        if (hasPermission()) {
            startBackgroundThread();
            // When the screen is turned off and turned back on, the SurfaceTexture is already
            // available, and "onSurfaceTextureAvailable" will not be called. In that case, we can open
            // a camera and start preview from here (otherwise, we wait until the surface is ready in
            // the SurfaceTextureListener).

            if (textureView.isAvailable()) {
                camera.startPreview();
            } else {
                textureView.setSurfaceTextureListener(surfaceTextureListener);
                debug = !debug;
                onSetDebug(debug);
            }
        } else {
            //requestPermission();
        }

    }


    private byte[] lastPreviewFrame;

    protected int[] getRgbBytes() {
        imageConverter.run();
        return rgbBytes;
    }

    protected int getLuminanceStride() {
        return yRowStride;
    }

    protected byte[] getLuminance() {
        return yuvBytes[0];
    }

    /**
     * Callback for android.hardware.Camera API
     */
    @Override
    public void onPreviewFrame(final byte[] bytes, final Camera camera) {
        if (isProcessingFrame) {
            LOGGER.w("Dropping frame!");
            return;
        }

        try {
            // Initialize the storage bitmaps once when the resolution is known.
            if (rgbBytes == null) {
                Camera.Size previewSize = camera.getParameters().getPreviewSize();
                previewHeight = previewSize.height;
                previewWidth = previewSize.width;
                rgbBytes = new int[previewWidth * previewHeight];
                onPreviewSizeChosen(new Size(previewSize.width, previewSize.height), 90);
            }
        } catch (final Exception e) {
            LOGGER.e(e, "Exception!");
            return;
        }

        isProcessingFrame = true;
        lastPreviewFrame = bytes;
        yuvBytes[0] = bytes;
        yRowStride = previewWidth;

        imageConverter =
                new Runnable() {
                    @Override
                    public void run() {
                        ImageUtils.convertYUV420SPToARGB8888(bytes, previewWidth, previewHeight, rgbBytes);
                    }
                };

        postInferenceCallback =
                new Runnable() {
                    @Override
                    public void run() {
                        camera.addCallbackBuffer(bytes);
                        isProcessingFrame = false;
                    }
                };
        processImage();
    }

    /**
     * Callback for Camera2 API
     */
    @Override
    public void onImageAvailable(final ImageReader reader) {
        //We need wait until we have some size from onPreviewSizeChosen
        if (previewWidth == 0 || previewHeight == 0) {
            return;
        }
        if (rgbBytes == null) {
            rgbBytes = new int[previewWidth * previewHeight];
        }
        try {
            final Image image = reader.acquireLatestImage();

            if (image == null) {
                return;
            }

            if (isProcessingFrame) {
                image.close();
                return;
            }
            isProcessingFrame = true;
            Trace.beginSection("imageAvailable");
            final Image.Plane[] planes = image.getPlanes();
            fillBytes(planes, yuvBytes);
            yRowStride = planes[0].getRowStride();
            final int uvRowStride = planes[1].getRowStride();
            final int uvPixelStride = planes[1].getPixelStride();

            imageConverter =
                    new Runnable() {
                        @Override
                        public void run() {
                            ImageUtils.convertYUV420ToARGB8888(
                                    yuvBytes[0],
                                    yuvBytes[1],
                                    yuvBytes[2],
                                    previewWidth,
                                    previewHeight,
                                    yRowStride,
                                    uvRowStride,
                                    uvPixelStride,
                                    rgbBytes);
                        }
                    };

            postInferenceCallback =
                    new Runnable() {
                        @Override
                        public void run() {
                            image.close();
                            isProcessingFrame = false;
                        }
                    };

            processImage();
        } catch (final Exception e) {
            LOGGER.e(e, "Exception!");
            Trace.endSection();
            return;
        }
        Trace.endSection();
    }

    @Override
    public synchronized void onStart() {
        LOGGER.d("onStart " + this);
        super.onStart();
        handlerThread = new HandlerThread("inference");
        handlerThread.start();
        handler = new Handler(handlerThread.getLooper());
    }


    @Override
    public synchronized void onStop() {
        LOGGER.d("onStop " + this);
        super.onStop();
//        if (!isFinishing()) {
//            LOGGER.d("Requesting finish");
//            finish();
//        }
        handlerThread.quitSafely();
        try {
            handlerThread.join();
            handlerThread = null;
            handler = null;
        } catch (final InterruptedException e) {
            LOGGER.e(e, "Exception!");
        }
        Randomizer.resetFlags();
        stopCamera();
        stopBackgroundThread();
    }

    @Override
    public synchronized void onDestroy() {
        LOGGER.d("onDestroy " + this);
        super.onDestroy();
    }

    protected synchronized void runInBackground(final Runnable r) {
        if (handler != null) {
            handler.post(r);
        }
    }


    private boolean hasPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return checkSelfPermission(PERMISSION_CAMERA) == PackageManager.PERMISSION_GRANTED &&
                    checkSelfPermission(PERMISSION_STORAGE) == PackageManager.PERMISSION_GRANTED;
        } else {
            return true;
        }
    }

    protected Size getDesiredPreviewFrameSize() {
        return DESIRED_PREVIEW_SIZE;
    }

    private Camera camera;

    private final TextureView.SurfaceTextureListener surfaceTextureListener =
            new TextureView.SurfaceTextureListener() {
                @Override
                public void onSurfaceTextureAvailable(
                        final SurfaceTexture texture, final int width, final int height) {

                    int index = getCameraId();


                    try {
                        camera = Camera.open(index);
                        Camera.Parameters parameters = camera.getParameters();
                        parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);

                        List<Camera.Size> cameraSizes = parameters.getSupportedPreviewSizes();
                        Size[] sizes = new Size[cameraSizes.size()];
                        int i = 0;
                        for (Camera.Size size : cameraSizes) {
                            sizes[i++] = new Size(size.width, size.height);
                        }
                        Size previewSize =
                                CameraConnectionFragment.chooseOptimalSize(
                                        sizes, getDesiredPreviewFrameSize().getWidth(), getDesiredPreviewFrameSize().getHeight());
                        parameters.setPreviewSize(previewSize.getWidth(), previewSize.getHeight());
                        camera.setDisplayOrientation(90);
                        camera.setParameters(parameters);
                        camera.setPreviewTexture(texture);
                        camera.setPreviewCallbackWithBuffer(HillClimbWindow.this);
                        Camera.Size s = camera.getParameters().getPreviewSize();
                        camera.addCallbackBuffer(new byte[ImageUtils.getYUVByteSize(s.height, s.width)]);

                        textureView.setAspectRatio(s.height, s.width);

                        camera.startPreview();
                    } catch (IOException exception) {
                        camera.release();
                    } catch (RuntimeException exception) {
                        Toast.makeText(getApplicationContext(), "Can't connect to camera!", Toast.LENGTH_SHORT).show();
                        //camera.release();
                    }


                }

                @Override
                public void onSurfaceTextureSizeChanged(
                        final SurfaceTexture texture, final int width, final int height) {
                }

                @Override
                public boolean onSurfaceTextureDestroyed(final SurfaceTexture texture) {
                    return true;
                }

                @Override
                public void onSurfaceTextureUpdated(final SurfaceTexture texture) {
                }
            };
    /**
     * An {@link AutoFitTextureView} for camera preview.
     */
    private AutoFitTextureView textureView;

    /**
     * An additional thread for running tasks that shouldn't block the UI.
     */
    private HandlerThread backgroundThread;

    /**
     * Starts a background thread and its {@link Handler}.
     */
    private void startBackgroundThread() {
        backgroundThread = new HandlerThread("CameraBackground");
        backgroundThread.start();
    }

    /**
     * Stops the background thread and its {@link Handler}.
     */
    private void stopBackgroundThread() {
        backgroundThread.quitSafely();
        try {
            backgroundThread.join();
            backgroundThread = null;
        } catch (final InterruptedException e) {
            LOGGER.e(e, "Exception!");
        }
    }

    protected void stopCamera() {
        if (camera != null) {
            camera.stopPreview();
            camera.setPreviewCallback(null);
            camera.release();
            camera = null;
        }
    }

    private int getCameraId() {
        Camera.CameraInfo ci = new Camera.CameraInfo();
        for (int i = 0; i < Camera.getNumberOfCameras(); i++) {
            Camera.getCameraInfo(i, ci);
            if (ci.facing == Camera.CameraInfo.CAMERA_FACING_FRONT)
                return i;
        }
        return -1; // No camera found
    }

    long downTime = SystemClock.uptimeMillis();
    // event time MUST be retrieved only by this way!
    long eventTime = SystemClock.uptimeMillis();
    Thread HILL_CLIMP_ACEL_G = new Thread() {
        public void run() {
            while (true) {
                downTime = SystemClock.uptimeMillis();
                eventTime = SystemClock.uptimeMillis();
                m_Instrumentation.sendPointerSync(MotionEvent.obtain(downTime, eventTime, MotionEvent.ACTION_DOWN, 2271, 1179, 0));
                Log.i("THREAD_ACCEL1", "Accel_Press");
                try {
                    synchronized (Thread.currentThread()) {
                        Log.i("THREAD_ACCEL1", "Accel_Press_Wait");
                        Thread.currentThread().wait();
                    }
                } catch (InterruptedException e) {
                }
            }
        }
    };


    Thread HILL_CLIMP_ACEL_GG = new Thread() {
        public void run() {
            while (true) {
                downTime = SystemClock.uptimeMillis();
                eventTime = SystemClock.uptimeMillis();
                m_Instrumentation.sendPointerSync(MotionEvent.obtain(downTime, eventTime + 1, MotionEvent.ACTION_UP, 2271, 1179, 0));
                Log.i("THREAD_ACCEL2", "Accel_Release");
                try {
                    synchronized (Thread.currentThread()) {
                        Log.i("THREAD_ACCEL2", "Accel_Release_Wait");
                        Thread.currentThread().wait();
                    }
                } catch (InterruptedException e) {
                }
            }
        }
    };

    Thread HILL_CLIMP_BRAK_G = new Thread() {
        public void run() {
            while (true) {
                downTime = SystemClock.uptimeMillis();
                eventTime = SystemClock.uptimeMillis();
                m_Instrumentation.sendPointerSync(MotionEvent.obtain(downTime + 2, eventTime + 2, MotionEvent.ACTION_DOWN, 298, 1196, 0));
                Log.i("THREAD_BRAK1", "Pressing_Brake");
                try {
                    synchronized (Thread.currentThread()) {
                        Log.i("THREAD_BRAK1", "Pressing_Brake_Waiting");
                        Thread.currentThread().wait();
                    }
                } catch (InterruptedException e) {
                }
            }
        }
    };


    Thread HILL_CLIMP_BRAK_GG = new Thread() {
        public void run() {
            while (true) {
                downTime = SystemClock.uptimeMillis();
                eventTime = SystemClock.uptimeMillis();
                m_Instrumentation.sendPointerSync(MotionEvent.obtain(downTime + 2, eventTime + 3, MotionEvent.ACTION_UP, 298, 1196, 0));
                Log.i("THREAD_BRAK2", "Releasing_Brake");
                try {
                    synchronized (Thread.currentThread()) {
                        Log.i("THREAD_BRAK2", "Releasing_Brake_Wait");
                        Thread.currentThread().wait();
                    }
                } catch (InterruptedException e) {
                }
            }
        }
    };

    protected void processImage() {
        rgbFrameBitmap.setPixels(getRgbBytes(), 0, previewWidth, 0, 0, previewWidth, previewHeight);
        final Canvas canvas = new Canvas(croppedBitmap);
        canvas.drawBitmap(rgbFrameBitmap, frameToCropTransform, null);

        // For examining the actual TF input.
        if (SAVE_PREVIEW_BITMAP) {
            ImageUtils.saveBitmap(croppedBitmap);
        }
        runInBackground(
                new Runnable() {
                    @Override
                    public void run() {

                        Thread TAP_MIDDLE = new Thread() {
                            public void run() {

                                long downTime = SystemClock.uptimeMillis();
                                // event time MUST be retrieved only by this way!
                                long eventTime = SystemClock.uptimeMillis();
                                m_Instrumentation.sendPointerSync(MotionEvent.obtain(downTime, eventTime, MotionEvent.ACTION_DOWN, 500, 1440, 0));
                                m_Instrumentation.sendPointerSync(MotionEvent.obtain(downTime, eventTime, MotionEvent.ACTION_UP, 500, 1440, 0));

                            }
                        };

                        final long startTime = SystemClock.uptimeMillis();
                        final List<Classifier.Recognition> results = classifier.recognizeImage(croppedBitmap);
                        lastProcessingTimeMs = SystemClock.uptimeMillis() - startTime;
                        //LOGGER.i("Detect: %s", results);

                        cropCopyBitmap = Bitmap.createBitmap(croppedBitmap);
                        if (resultsView == null) {
                            resultsView = (ResultsView) findViewById(R.id.results);
                        }
                        resultsView.setResults(results);

                            if (results.get(0).getTitle().equals("five") && results.get(0).getConfidence() >= THRESHOLD) {

                                if (Randomizer.flagB) {
                                    Randomizer.flagB = false;
                                    if (!Randomizer.isRunningGGB) {
                                        Randomizer.isRunningGGB = true;
                                        HILL_CLIMP_BRAK_GG.start();
                                    } else {
                                        synchronized (HILL_CLIMP_BRAK_GG) {
                                            HILL_CLIMP_BRAK_GG.notify();
                                        }
                                    }

                                }

                                if (!Randomizer.isRunningGA) {
                                    Randomizer.isRunningGA = true;
                                    Randomizer.flagA = true;
                                    HILL_CLIMP_ACEL_G.start();
                                } else {
                                    synchronized (HILL_CLIMP_ACEL_G) {
                                        Randomizer.flagA = true;
                                        HILL_CLIMP_ACEL_G.notify();
                                    }
                                }


                            } else if (results.get(0).getTitle().equals("palm") && results.get(0).getConfidence() >= THRESHOLD) {

                                if (Randomizer.flagA) {
                                    Randomizer.flagA = false;
                                    if (!Randomizer.isRunningGGA) {
                                        Randomizer.isRunningGGA = true;
                                        HILL_CLIMP_ACEL_GG.start();
                                    } else {
                                        synchronized (HILL_CLIMP_ACEL_GG) {
                                            HILL_CLIMP_ACEL_GG.notify();
                                        }
                                    }
                                }

                                if (!Randomizer.isRunningGB) {
                                    Randomizer.isRunningGB = true;
                                    HILL_CLIMP_BRAK_G.start();
                                    Randomizer.flagB = true;
                                } else {
                                    synchronized (HILL_CLIMP_BRAK_G) {
                                        Randomizer.flagB = true;
                                        HILL_CLIMP_BRAK_G.notify();
                                    }
                                }


                            } else {

                                if (Randomizer.flagA) {
                                    Randomizer.flagA = false;
                                    if (!Randomizer.isRunningGGA) {
                                        Randomizer.isRunningGGA = true;
                                        HILL_CLIMP_ACEL_GG.start();
                                    } else {
                                        synchronized (HILL_CLIMP_ACEL_GG) {
                                            HILL_CLIMP_ACEL_GG.notify();
                                        }
                                    }
                                }

                                if (Randomizer.flagB) {
                                    Randomizer.flagB = false;
                                    if (!Randomizer.isRunningGGB) {
                                        Randomizer.isRunningGGB = true;
                                        HILL_CLIMP_BRAK_GG.start();
                                    } else {
                                        synchronized (HILL_CLIMP_BRAK_GG) {
                                            HILL_CLIMP_BRAK_GG.notify();
                                        }
                                    }

                                }


                            }



                        requestRender();
                        readyForNextImage();


                    }

                });


    }

    public void onSetDebug(final boolean debug) {
    }

    protected void fillBytes(final Image.Plane[] planes, final byte[][] yuvBytes) {
        // Because of the variable row stride it's not possible to know in
        // advance the actual necessary dimensions of the yuv planes.
        for (int i = 0; i < planes.length; ++i) {
            final ByteBuffer buffer = planes[i].getBuffer();
            if (yuvBytes[i] == null) {
                LOGGER.d("Initializing buffer %d at size %d", i, buffer.capacity());
                yuvBytes[i] = new byte[buffer.capacity()];
            }
            buffer.get(yuvBytes[i]);
        }
    }

    public boolean isDebug() {
        return debug;
    }

    public void requestRender() {
        final OverlayView overlay = (OverlayView) findViewById(R.id.debug_overlay);
        if (overlay != null) {
            overlay.postInvalidate();
        }
    }

    public void addCallback(final OverlayView.DrawCallback callback) {
        final OverlayView overlay = (OverlayView) findViewById(R.id.debug_overlay);
        if (overlay != null) {
            overlay.addCallback(callback);
        }
    }

    protected void readyForNextImage() {
        if (postInferenceCallback != null) {
            postInferenceCallback.run();
        }
    }

    private static final float TEXT_SIZE_DIP = 10;

    public void onPreviewSizeChosen(final Size size, final int rotation) {
        final float textSizePx = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, TEXT_SIZE_DIP, getResources().getDisplayMetrics());
        borderedText = new BorderedText(textSizePx);
        borderedText.setTypeface(Typeface.MONOSPACE);

        classifier =
                TensorFlowImageClassifier.create(
                        getAssets(),
                        MODEL_FILE,
                        LABEL_FILE,
                        INPUT_SIZE,
                        IMAGE_MEAN,
                        IMAGE_STD,
                        INPUT_NAME,
                        OUTPUT_NAME);

        previewWidth = size.getWidth();
        previewHeight = size.getHeight();


        LOGGER.i("Sensor orientation: %d, Screen orientation: %d", rotation, 90);

        sensorOrientation = rotation - 90;

        LOGGER.i("Initializing at size %dx%d", previewWidth, previewHeight);
        rgbFrameBitmap = Bitmap.createBitmap(previewWidth, previewHeight, Bitmap.Config.ARGB_8888);
        croppedBitmap = Bitmap.createBitmap(INPUT_SIZE, INPUT_SIZE, Bitmap.Config.ARGB_8888);

        frameToCropTransform = ImageUtils.getTransformationMatrix(
                previewWidth, previewHeight,
                INPUT_SIZE, INPUT_SIZE,
                sensorOrientation, MAINTAIN_ASPECT);

        cropToFrameTransform = new Matrix();
        frameToCropTransform.invert(cropToFrameTransform);

        addCallback(
                new OverlayView.DrawCallback() {
                    @Override
                    public void drawCallback(final Canvas canvas) {
                        renderDebug(canvas);
                    }
                });
    }

    private void renderDebug(final Canvas canvas) {
        if (!isDebug()) {
            return;
        }
        final Bitmap copy = cropCopyBitmap;
        if (copy != null) {
            final Matrix matrix = new Matrix();
            final float scaleFactor = 4;
            matrix.postScale(scaleFactor, scaleFactor);
            matrix.postTranslate(
                    canvas.getWidth() - copy.getWidth() * scaleFactor,
                    canvas.getHeight() - copy.getHeight() * scaleFactor);
            canvas.drawBitmap(copy, matrix, new Paint());

            final Vector<String> lines = new Vector<String>();
            if (classifier != null) {
                String statString = classifier.getStatString();
                String[] statLines = statString.split("\n");
                for (String line : statLines) {
                    lines.add(line);
                }
            }

            lines.add("Crop: " + copy.getWidth() + "x" + copy.getHeight());
            lines.add("Rotation: " + sensorOrientation);
            lines.add("Inference: " + lastProcessingTimeMs + "ms");


            borderedText.drawLines(canvas, 10, canvas.getHeight() - 10, lines);
        }
    }


}
