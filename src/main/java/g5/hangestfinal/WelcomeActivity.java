package g5.hangestfinal;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import net.mready.hover.Hover;

import static g5.hangestfinal.StartActivity.WINDOW_SIMPLE_ID;

public class WelcomeActivity extends AppCompatActivity {
    public static final int WINDOW_SIMPLE_ID = 1;
    public static final int PERMISSIONS_REQUEST = 1;
    public static int flag = 0;
    public static ViewPager viewPager;
    public static int int_items = 4;
    int temp;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        viewPager = (ViewPager) findViewById(R.id.viewpager);

        viewPager.setAdapter(new MyAdapter(getSupportFragmentManager()));

        // Making notification bar transparent
        if (Build.VERSION.SDK_INT >= 21) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        }

        // making notification bar transparent
        changeStatusBarColor();

        FloatingActionButton fab = findViewById(R.id.fab);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (flag == 1) {
                    try {
                        Hover.closeWindow(WelcomeActivity.this, WINDOW_SIMPLE_ID);
                        Snackbar.make(view, "Closed", Snackbar.LENGTH_SHORT)
                                .show();
                        flag = 0;
                        temp=viewPager.getCurrentItem();
                        viewPager.setAdapter(new MyAdapter(getSupportFragmentManager()));
                        viewPager.setCurrentItem(temp);
                    } catch (IllegalStateException e) {
                        e.printStackTrace();
                        Snackbar.make(view, "Something went wrong", Snackbar.LENGTH_LONG)
                                .show();
                    }
                }
            }
        });

    }

    /**
     * Making notification bar transparent
     */
    private void changeStatusBarColor() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.TRANSPARENT);
        }
    }

    class MyAdapter extends FragmentPagerAdapter {

        public MyAdapter(FragmentManager fm) {
            super(fm);
        }

        /**
         * Return fragment with respect to Position .
         */

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return new Fragment_Hill();
                case 1:
                    return new Fragment_Book();
                case 2:
                    return new Fragment_Test();
                case 3:
                    return new Fragment_Tech();
            }
            return null;
        }

        @Override
        public int getCount() {

            return int_items;

        }

        /**
         * This method returns the title of the tab according to the position.
         */

        @Override
        public CharSequence getPageTitle(int position) {

            return null;
        }
    }

}