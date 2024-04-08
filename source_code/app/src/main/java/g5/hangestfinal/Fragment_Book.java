package g5.hangestfinal;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.brouding.blockbutton.BlockButton;

import net.mready.hover.Hover;
import net.mready.hover.HoverWindow;

import static g5.hangestfinal.WelcomeActivity.WINDOW_SIMPLE_ID;
import static g5.hangestfinal.WelcomeActivity.flag;


public class Fragment_Book extends Fragment {

    BlockButton start;

    @Nullable
    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.lay2, container, false);

        start=(BlockButton)view.findViewById(R.id.start_book);

        if(flag==0)
        {
            start.setEnabled(true);
        }else
        {
            start.setEnabled(false);
        }

        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    openWindow(WINDOW_SIMPLE_ID, BookWindow.class);
                    flag=1;
                } catch (IllegalStateException e) {
                    e.printStackTrace();
                    Toast.makeText(getContext(),"Opps, something went wrong!", Toast.LENGTH_SHORT).show();
                }
                catch (RuntimeException e) {
                    e.printStackTrace();
                    Toast.makeText(getContext(),"Already started", Toast.LENGTH_SHORT).show();
                }

                PackageManager manager = getActivity().getPackageManager();
                try {
                    Intent i = manager.getLaunchIntentForPackage("com.google.android.apps.books");
                    if (i == null) {
                        throw new ActivityNotFoundException();
                    }
                    i.addCategory(Intent.CATEGORY_LAUNCHER);
                    getContext().startActivity(i);
                } catch (ActivityNotFoundException e) {
                    Toast.makeText(getContext(),"Can't Find the game!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        return view;
    }

    private void openWindow(int id, Class<? extends HoverWindow> window) {
        // APKs not installed via the Google Play Store require explicit permission to
        // display overlay windows
        if (!Hover.hasOverlayPermission(getContext())) {
            Hover.requestOverlayPermission(getActivity(), 0);
        } else {
            Hover.showWindow(getContext(), id, window);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if(flag==0)
        {
            start.setEnabled(true);
        }else
        {
            start.setEnabled(false);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if(flag==0)
        {
            start.setEnabled(true);
        }else
        {
            start.setEnabled(false);
        }
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        try {
            if(flag==0)
            {
                start.setEnabled(true);
            }else
            {
                start.setEnabled(false);
            }
        }catch(NullPointerException e){}
    }
}
