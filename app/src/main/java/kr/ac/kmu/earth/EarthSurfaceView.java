package kr.ac.kmu.earth;

import android.content.Context;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;

import com.google.vr.sdk.base.GvrView;

public class EarthSurfaceView extends GvrView implements ScaleGestureDetector.OnScaleGestureListener {

    private EarthRenderer mRenderer;

    private float mPrevX;
    private float mPrevY;

    private ScaleGestureDetector scaler;
    private boolean scaleMode;

    private final float TOUCH_SCALE_FACTOR = 180F/ 320;

    public EarthSurfaceView(Context context) {
        super(context);

        // Set OpenGL ES 2.0 version 사용
        setEGLContextClientVersion(2);

        // Set Renderer
        mRenderer = new EarthRenderer(context);
        setRenderer(mRenderer);
        setTransitionViewEnabled(false);

        //  vr 설정하면서 추가, maybe google's cardboard 에서 돌리는 ?
        enableCardboardTriggerEmulation();

        //  스케일 체스쳐(두 손가락으로 zoom in/out)
        scaler = new ScaleGestureDetector(context, this);
    }


    //  시간되면 좀 더 정밀하게 테스트하면서 조절
    @Override
    public boolean onTouchEvent(MotionEvent e) {

        scaler.onTouchEvent(e);

        if(scaleMode)
            return true;

        float x = e.getX();
        float y = e.getY();


        switch (e.getAction()) {
            case MotionEvent.ACTION_MOVE:

                float dx = x - mPrevX;
                float dy = y - mPrevY;


                //  중간선 위
                if (y > getHeight() / 2) {
                    dx = dx * -1;
                }

                //  중간선 왼쪽
                if (x < getWidth() / 2) {
                    dy = dy * -1;
                }

                if(Math.abs(dx) > Math.abs(dy)) {
                    mRenderer.mXAngle += (dx) * TOUCH_SCALE_FACTOR;  // = 180.0f / 320
                }
                else {
                    mRenderer.mYAngle += (dy) * TOUCH_SCALE_FACTOR;
                }

        }

        mPrevX = x;
        mPrevY = y;

        return true;
    }


    //  ScaleGestureDetector(두손 줌in/out, 감지 리스너에 대한 이벤트 )
    @Override
    public boolean onScale(ScaleGestureDetector detector) {
        // TODO Auto-generated method stub
        float zoom = detector.getScaleFactor();

        if (zoom > 1f)
            mRenderer.mZoom += .2f;
        else
            mRenderer.mZoom -= .2f;


        if (mRenderer.mZoom < -15f)
            mRenderer.mZoom = -15f;
        else if (mRenderer.mZoom > -3f)
            mRenderer.mZoom = -3f;


        return true;
    }

    @Override
    public boolean onScaleBegin(ScaleGestureDetector detector) {
        scaleMode = true;
        return true;
    }

    @Override
    public void onScaleEnd(ScaleGestureDetector detector) {
        scaleMode = false;
    }

}
