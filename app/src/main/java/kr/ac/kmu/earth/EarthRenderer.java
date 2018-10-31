package kr.ac.kmu.earth;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.Matrix;

import com.google.vr.sdk.base.Eye;
import com.google.vr.sdk.base.GvrView;
import com.google.vr.sdk.base.HeadTransform;
import com.google.vr.sdk.base.Viewport;

import javax.microedition.khronos.egl.EGLConfig;

public class EarthRenderer implements GvrView.StereoRenderer {

    public volatile float mXAngle;
    public volatile float mYAngle;
    public volatile float mZoom;

    private final float[] mVMatrix = new float[16];
    private final float[] mProjMatrix = new float[16];
    private final float[] mNormalMatrix = new float[16];
    private final float[] mMVPMatrix = new float[16];
    private final float[] mRotationMatrixX = new float[16];
    private final float[] mRotationMatrixY = new float[16];
    private final float[] mPVMatrix = new float[16];
    private final float[] mTempMatrix = new float[16];
    private final float[] mMVMatrix = new float[16];

    private Sphere mEarth;  // Three pool balls

    private Context context;

    public EarthRenderer(Context context) {
        super();
        this.context = context;
    }


    // compile GLSL code prior to using it in OpenGL ES environment
    public static int loadShader(int type, String shaderCode) {

        // create a vertex shader type (GLES20.GL_VERTEX_SHADER)
        // or a fragment shader type (GLES20.GL_FRAGMENT_SHADER)
        int shader = GLES20.glCreateShader(type);

        // add the source code to the shader and compile it
        GLES20.glShaderSource(shader, shaderCode);
        GLES20.glCompileShader(shader);

        return shader;
    }


    private void drawEarth() {
        float[] scalerMatrix = new float[16];
        float[] finalMVPMatrix = new float[16];
        float[] tempMatrix = new float[16];

        Matrix.setIdentityM(scalerMatrix, 0);
        Matrix.scaleM(scalerMatrix, 0, 0.6f, 0.6f, 0.6f);
        Matrix.multiplyMM(tempMatrix, 0, mMVPMatrix, 0, scalerMatrix, 0);

        //Matrix.translateM(finalMVPMatrix, 0, tempMatrix, 0, -5.25f, 1.85f, .35f);
        mEarth.draw(tempMatrix, mNormalMatrix, mMVMatrix);


    }

    @Override
    public void onNewFrame(HeadTransform headTransform) {

    }

    @Override
    public void onDrawEye(Eye eye) {

        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

        // 카메라(뷰) 좌표
        Matrix.setLookAtM(mVMatrix, 0, 0, 0, mZoom, 0f, 0f, 0f, 0f, 1.0f, 0.0f);

        // projection, view 매트릭스
        Matrix.multiplyMM(mPVMatrix, 0, mProjMatrix, 0, mVMatrix, 0);
        Matrix.multiplyMM(mVMatrix, 0, eye.getEyeView(), 0, mVMatrix, 0); //새로 추가된거


        // x축 회전 행렬
        Matrix.setRotateM(mRotationMatrixX, 0, mXAngle, 0, 1.0f, 0f);

        //  y축 회전행렬
        Matrix.setRotateM(mRotationMatrixY, 0, mYAngle, 1.0f, 0, 0);


        Matrix.multiplyMM(mTempMatrix, 0, mPVMatrix, 0, mRotationMatrixX, 0);
        Matrix.multiplyMM(mMVPMatrix, 0, mTempMatrix, 0, mRotationMatrixY, 0);


        //  카메라 행렬 회전
        Matrix.multiplyMM(mTempMatrix, 0, mVMatrix, 0, mRotationMatrixX, 0);
        Matrix.multiplyMM(mMVMatrix, 0, mTempMatrix, 0, mRotationMatrixY, 0);

        //  노멀매트릭스 역, 전치로 구함
        Matrix.invertM(mTempMatrix, 0, mMVMatrix, 0);
        Matrix.transposeM(mNormalMatrix, 0, mTempMatrix, 0);

        drawEarth();
    }

    @Override
    public void onFinishFrame(Viewport viewport) {

    }

    @Override
    public void onSurfaceChanged(int width, int height) {

        GLES20.glViewport(0, 0, width, height);

        float ratio = (float) width / height;

        Matrix.frustumM(mProjMatrix, 0, -ratio, ratio, -1, 1, 3, 20);
    }

    @Override
    public void onSurfaceCreated(EGLConfig config) {
        // TODO Auto-generated method stub
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);
        GLES20.glDepthFunc(GLES20.GL_LEQUAL);

        mZoom = -4.5f;


        mEarth = new Sphere(context, 1, 50, 50);
    }

    @Override
    public void onRendererShutdown() {

    }
}