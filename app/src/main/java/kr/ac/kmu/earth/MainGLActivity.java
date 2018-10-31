package kr.ac.kmu.earth;

import android.os.Bundle;

import com.google.vr.sdk.base.AndroidCompat;
import com.google.vr.sdk.base.GvrActivity;

public class MainGLActivity extends GvrActivity {

    private EarthSurfaceView mView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mView = new EarthSurfaceView(this);
        setContentView(mView);

        //  VR을 위해 휴대폰 performance 최대한 끌어냄.
        if (mView.setAsyncReprojectionEnabled(true)) {
            AndroidCompat.setSustainedPerformanceMode(this, true);
        }

        setGvrView(mView);

    }


    @Override
    protected void onResume() {
        super.onResume();

        if (mView != null) {
            mView.onResume();
        }

    }

    @Override
    protected void onPause() {
        super.onPause();

        if (mView != null) {
            mView.onPause();
        }

    }

}
