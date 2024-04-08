package g5.hangestfinal;

import android.Manifest;
import android.app.Activity;
import android.app.UiAutomation;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Camera;
import android.hardware.camera2.CameraCharacteristics;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.test.InstrumentationRegistry;
import android.util.Log;
import android.view.InputDevice;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import net.mready.hover.Hover;
import net.mready.hover.HoverWindow;

import java.io.IOException;


/**
 * Created by Mahajan-PC on 2018-01-24.
 */

public class StartActivity extends Activity {
    public static final int WINDOW_SIMPLE_ID = 1;
    public static final int PERMISSIONS_REQUEST = 1;
    public static int flag=0;

    private static final String PERMISSION_CAMERA = Manifest.permission.CAMERA;
    private static final String PERMISSION_STORAGE = Manifest.permission.WRITE_EXTERNAL_STORAGE;
    Button starter1, starter2, closer, test_model, test_model_front, books;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        starter1 = (Button) findViewById(R.id.start1);
        starter2 = (Button) findViewById(R.id.start2);
        closer = (Button) findViewById(R.id.closer);
        test_model = (Button) findViewById(R.id.start_normal);
        test_model_front = (Button) findViewById(R.id.start_normal_f);
        books = (Button) findViewById(R.id.start_Books);

        test_model.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent myIntent = new Intent(StartActivity.this, ClassifierActivity.class);
                StartActivity.this.startActivity(myIntent);
            }
        });

        test_model_front.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent myIntent = new Intent(StartActivity.this, ClassifierActivityFront.class);
                StartActivity.this.startActivity(myIntent);
            }
        });

        books.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {



                try {
                    openWindow(WINDOW_SIMPLE_ID, BookWindow.class);
                    books.setEnabled(false);
                    starter1.setEnabled(false);
                    starter2.setEnabled(false);
                    test_model.setEnabled(false);
                    test_model_front.setEnabled(false);
                } catch (IllegalStateException e) {
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(),"Opps, something went wrong!", Toast.LENGTH_SHORT).show();
                }
                catch (RuntimeException e) {
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(),"Already started", Toast.LENGTH_SHORT).show();
                }

                PackageManager manager = getApplicationContext().getPackageManager();
                try {
                    Intent i = manager.getLaunchIntentForPackage("com.google.android.apps.books");
                    if (i == null) {
                        throw new ActivityNotFoundException();
                    }
                    i.addCategory(Intent.CATEGORY_LAUNCHER);
                    getApplicationContext().startActivity(i);
                } catch (ActivityNotFoundException e) {
                    Toast.makeText(getApplicationContext(),"Can't Find the game!", Toast.LENGTH_SHORT).show();
                }


            }
        });

        starter1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {



                try {
                    openWindow(WINDOW_SIMPLE_ID, HillClimbWindow.class);
                    books.setEnabled(false);
                    starter1.setEnabled(false);
                    starter2.setEnabled(false);
                    test_model.setEnabled(false);
                    test_model_front.setEnabled(false);
                } catch (IllegalStateException e) {
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(),"Opps, something went wrong!", Toast.LENGTH_SHORT).show();
                }
                catch (RuntimeException e) {
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(),"Already started", Toast.LENGTH_SHORT).show();
                }


                PackageManager manager = getApplicationContext().getPackageManager();
                try {
                    Intent i = manager.getLaunchIntentForPackage("com.fingersoft.hillclimb");
                    if (i == null) {
                        throw new ActivityNotFoundException();
                    }
                    i.addCategory(Intent.CATEGORY_LAUNCHER);
                    getApplicationContext().startActivity(i);
                } catch (ActivityNotFoundException e) {
                    Toast.makeText(getApplicationContext(),"Can't Find the game!", Toast.LENGTH_SHORT).show();
                }


            }
        });

        starter2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {



                try {
                    openWindow(WINDOW_SIMPLE_ID, HillWindowRear.class);
                    books.setEnabled(false);
                    starter1.setEnabled(false);
                    starter2.setEnabled(false);
                    test_model.setEnabled(false);
                    test_model_front.setEnabled(false);
                } catch (IllegalStateException e) {
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(),"Opps, something went wrong!", Toast.LENGTH_SHORT).show();
                }catch (RuntimeException e) {
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(),"Already started", Toast.LENGTH_SHORT).show();
                }

                PackageManager manager = getApplicationContext().getPackageManager();
                try {
                    Intent i = manager.getLaunchIntentForPackage("com.fingersoft.hillclimb");
                    if (i == null) {
                        throw new ActivityNotFoundException();
                    }
                    i.addCategory(Intent.CATEGORY_LAUNCHER);
                    getApplicationContext().startActivity(i);
                } catch (ActivityNotFoundException e) {
                    Toast.makeText(getApplicationContext(),"Can't Find the game!", Toast.LENGTH_SHORT).show();
                }

            }
        });

        closer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                try {
                    Hover.closeWindow(StartActivity.this, WINDOW_SIMPLE_ID);
                    books.setEnabled(true);
                    starter1.setEnabled(true);
                    starter2.setEnabled(true);
                    test_model.setEnabled(true);
                    test_model_front.setEnabled(true);
                } catch (IllegalStateException e) {
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(),"Opps, something went wrong!", Toast.LENGTH_SHORT).show();
                }
            }
        });
        closer.setVisibility(View.GONE);
        starter1.setVisibility(View.GONE);
        starter2.setVisibility(View.GONE);
        if (hasPermission()) {
            starter1.setVisibility(View.VISIBLE);
            starter2.setVisibility(View.VISIBLE);
            closer.setVisibility(View.VISIBLE);
        } else {
            requestPermission();
        }



    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        int x = (int) event.getX();
        int y = (int) event.getY();
        int downTime = (int) event.getDownTime();
        int eventTime = (int) event.getEventTime();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                Log.i("TAG", "touched down"+ "[ "+downTime+", "+eventTime+" ]");
                break;
            case MotionEvent.ACTION_MOVE:
                Log.i("TAG", "moving: (" + x + ", " + y + ")"+ "[ "+downTime+", "+eventTime+" ]");
                break;
            case MotionEvent.ACTION_UP:
                Log.i("TAG", "touched up"+ "[ "+downTime+", "+eventTime+" ]");
                break;
        }
        return super.onTouchEvent(event);
    }

    private void openWindow(int id, Class<? extends HoverWindow> window) {
        // APKs not installed via the Google Play Store require explicit permission to
        // display overlay windows
        if (!Hover.hasOverlayPermission(this)) {
            Hover.requestOverlayPermission(this, 0);
        } else {
            Hover.showWindow(this, id, window);
        }
    }

    @Override
    public void onRequestPermissionsResult(
            final int requestCode, final String[] permissions, final int[] grantResults) {
        if (requestCode == PERMISSIONS_REQUEST) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED
                    && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                starter1.setVisibility(View.VISIBLE);
                starter2.setVisibility(View.VISIBLE);
            } else {
                requestPermission();
            }
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

    private void requestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (shouldShowRequestPermissionRationale(PERMISSION_CAMERA) ||
                    shouldShowRequestPermissionRationale(PERMISSION_STORAGE)) {
                Toast.makeText(StartActivity.this,
                        "Camera AND storage permission are required for this demo", Toast.LENGTH_LONG).show();
            }
            requestPermissions(new String[]{PERMISSION_CAMERA, PERMISSION_STORAGE}, PERMISSIONS_REQUEST);
        }
    }
}
