package g5.hangestfinal;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.brouding.blockbutton.BlockButton;

import static g5.hangestfinal.WelcomeActivity.flag;

public class Fragment_Test extends Fragment {

    BlockButton start_f, start_b;

    @Nullable
    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.lay3, container, false);

        start_f = (BlockButton) view.findViewById(R.id.start_test_front);
        start_b = (BlockButton) view.findViewById(R.id.start_test_back);

        if (flag == 0) {
            start_f.setEnabled(true);
            start_b.setEnabled(true);
        } else {
            start_f.setEnabled(false);
            start_b.setEnabled(false);
        }

        start_f.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent myIntent = new Intent(getContext(), ClassifierActivityFront.class);
                getContext().startActivity(myIntent);
            }
        });


        start_b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent myIntent = new Intent(getContext(), ClassifierActivity.class);
                getContext().startActivity(myIntent);
            }
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (flag == 0) {
            start_f.setEnabled(true);
            start_b.setEnabled(true);
        } else {
            start_f.setEnabled(false);
            start_b.setEnabled(false);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (flag == 0) {
            start_f.setEnabled(true);
            start_b.setEnabled(true);
        } else {
            start_f.setEnabled(false);
            start_b.setEnabled(false);
        }
    }
    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        try {
            if (flag == 0) {
                start_f.setEnabled(true);
                start_b.setEnabled(true);
            } else {
                start_f.setEnabled(false);
                start_b.setEnabled(false);
            }
        }catch(NullPointerException e){}
    }


}
