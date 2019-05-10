package com.hanayue.ayuemobieview.splash;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;

import com.hanayue.ayuemobieview.R;
import com.hanayue.ayuemobieview.user.LoginActivity;

/**
 * A simple {@link Fragment} subclass.
 */
public class SplashFragment extends Fragment {

    private int[] bgRes = {R.drawable.weather, R.drawable.wallet, R.drawable.note, R.drawable.notifuncation, R.drawable.splash5};


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_splash, null);
        Button btn = (Button) view.findViewById(R.id.splashBtn);
        RelativeLayout rl = (RelativeLayout) view.findViewById(R.id.splashRl);
        int index = getArguments().getInt("index");
        rl.setBackgroundResource(bgRes[index]);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), LoginActivity.class));
                getActivity().finish();
            }
        });
        btn.setVisibility(index == bgRes.length-1 ? View.VISIBLE : View.GONE);
        return view;
    }

}
