package kr.ac.kmu.earth;

import android.content.Context;
import android.opengl.GLES20;
import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

public class Sphere {

    private final Context mActivityContext;

    //  vertext 쉐이더
    private final String vertexShaderCode =
            "uniform mat4 uMVPMatrix, uMVMatrix, uNormalMat;" +
                    "attribute vec4 vPosition;" +
                    "attribute vec3 vNormal;" +
                    "attribute vec2 a_TexCoordinate;"+
                    "varying vec3 varyingNormal; " +
                    "varying vec3 varyingPos; " +
                    "varying vec2 v_TexCoordinate;"+
                    "void main() {" +
                    "v_TexCoordinate = a_TexCoordinate;"+
                    "  vec4 t = uNormalMat*vec4(vNormal, 0.0);" +
                    "   varyingNormal.xyz = t.xyz; " +
                    "   t = uMVMatrix*vPosition;" +
                    "   varyingPos.xyz = t.xyz; " +
                    "   gl_Position =    uMVPMatrix  * vPosition ;" +
                    "}";

    //  fragment 쉐이더
    private final String fragmentShaderCode =
            "precision mediump float;" +
                    " varying vec3 varyingNormal;" +
                    "varying vec3 varyingPos;" +
                    "varying vec2 v_TexCoordinate;"+
                    "uniform sampler2D u_Texture;"+
                    "uniform vec3 lightDir; " +
                    "void main() {" +
                    "  float Ns = 100.0;  " +
                    "   float kd = 0.9, ks = 0.9; " +
                    "   vec4 light = vec4(1.0, 1.0, 1.0, 1.0); " +
                    "   vec4 lightS = vec4(1.0, 1.0, 1.0, 1.0); " +
                    "   vec3 Nn = normalize(varyingNormal); " +
                    "   vec3 Ln = normalize(lightDir); " +
                    "   vec4 diffuse = kd* light * max(dot(Nn, Ln), 0.0); " +
                    "   vec3 Ref = reflect(Nn, Ln); " +
                    "   float dotV = max(dot(Ref, normalize(varyingPos)), 0.0); " +
                    "   vec4 specular = lightS*ks*pow(dotV, Ns); " +
                    "  gl_FragColor = (diffuse + specular)* texture2D(u_Texture, v_TexCoordinate); " +
                    //"   gl_FragColor = varyingColor*diffuse + specular; " +
                    "}";

    //  각 버퍼 (노멀로 정점의 색을 결정)
    private FloatBuffer vertexBuffer;
    private FloatBuffer normalBuffer;

    //  쉐이더 프로그램
    private final int mProgram;

    //  Attribute(속성) : vertex, color, normal
    private int mPositionHandle;
    private int mNormalHandle;

    //  Uniform 변수 (MVP Matrix, normal Matrix)
    private int mMVPMatrixHandle;
    private int mNormalMatHandle;

    //  버퍼에 연속으로 데이터가 들어있으므로 이 기준으로 나뉘어서 하나의 데이터로 인식
    //  만약에 정점이 (x,y,z,1)로 들어가 있으면 4로 해야 됨. (1은 point, 0은 vector)
    static final int COORDS_PER_VERTEX = 3;


    //  texture : 2018-06-02, 나중 수정, texture 좌표가 이상하게 들어감.
    private FloatBuffer mCubeTextureCoordinates;
    private int mTextureUniformHandle;
    private int mTextureCoordinateHandle;
    private final int mTextureCoordinateDataSize = 2;
    private int mTextureDataHandle;



    //  datas array : vertex, color, normals
    private float[] vertices;
    private float[] normals;


    //  정점의 갯수
    private int vertexCount;

    private final int vertexStride = COORDS_PER_VERTEX * 4;

    // 카메라(뷰)좌표계에서 빛 위치
    float lightDir[] = {0.0f, 1.0f, 8.0f};


    //  에러 체크
    public static int checkShaderError(int shader) {
        final int[] compileStatus = new int[1];

        GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compileStatus, 0);

        if (compileStatus[0] == 0) {
            Log.e("GLES Error:", "Error compiling shader: " + GLES20.glGetShaderInfoLog(shader));
            GLES20.glDeleteShader(shader);
            return 1;
        }
        return 0;
    }

    //  Shere Data : 가져온거 그대로 사용
    private void createSphere(double r, int lats, int longs) {
        int i, j;

        // there are lats*longs number of quads, each requires two triangles with six vertices, each vertex takes 3 floats;
        vertices = new float[lats * longs * 6 * 3];
        normals = new float[lats * longs * 6 * 3];

        vertexCount = vertices.length / COORDS_PER_VERTEX;

        int triIndex = 0;
        for (i = 0; i < lats; i++) {
            double lat0 = Math.PI * (-0.5 + (double) (i) / lats);
            double z0 = Math.sin(lat0);
            double zr0 = Math.cos(lat0);

            double lat1 = Math.PI * (-0.5 + (double) (i + 1) / lats);
            double z1 = Math.sin(lat1);
            double zr1 = Math.cos(lat1);

            //glBegin(GL_QUAD_STRIP);
            for (j = 0; j < longs; j++) {
                double lng = 2 * Math.PI * (double) (j - 1) / longs;
                double x = Math.cos(lng);
                double y = Math.sin(lng);

                lng = 2 * Math.PI * (double) (j) / longs;
                double x1 = Math.cos(lng);
                double y1 = Math.sin(lng);


                // the first triangle
                vertices[triIndex * 9 + 0] = (float) (x * zr0);
                vertices[triIndex * 9 + 1] = (float) (y * zr0);
                vertices[triIndex * 9 + 2] = (float) z0;
                vertices[triIndex * 9 + 3] = (float) (x * zr1);
                vertices[triIndex * 9 + 4] = (float) (y * zr1);
                vertices[triIndex * 9 + 5] = (float) z1;
                vertices[triIndex * 9 + 6] = (float) (x1 * zr0);
                vertices[triIndex * 9 + 7] = (float) (y1 * zr0);
                vertices[triIndex * 9 + 8] = (float) z0;

                triIndex++;
                vertices[triIndex * 9 + 0] = (float) (x1 * zr0);
                vertices[triIndex * 9 + 1] = (float) (y1 * zr0);
                vertices[triIndex * 9 + 2] = (float) z0;
                vertices[triIndex * 9 + 3] = (float) (x * zr1);
                vertices[triIndex * 9 + 4] = (float) (y * zr1);
                vertices[triIndex * 9 + 5] = (float) z1;
                vertices[triIndex * 9 + 6] = (float) (x1 * zr1);
                vertices[triIndex * 9 + 7] = (float) (y1 * zr1);
                vertices[triIndex * 9 + 8] = (float) z1;

                // in this case, the normal is the same as the vertex, plus the normalization;
                for (int kk = -9; kk < 9; kk++) {
                    normals[triIndex * 9 + kk] = vertices[triIndex * 9 + kk];

                }
                triIndex++;
            }

        }
    }


    public Sphere(Context _context, double r, int lats, int longs) {

        mActivityContext = _context;

        createSphere(r, lats, longs);

        //  Vertex 버퍼 할당후, vertices data put
        ByteBuffer bb = ByteBuffer.allocateDirect(vertices.length * 4);
        bb.order(ByteOrder.nativeOrder());

        vertexBuffer = bb.asFloatBuffer();
        vertexBuffer.put(vertices);
        vertexBuffer.position(0);

        //  Normal 버퍼 할당후, normal put
        ByteBuffer bb3 = ByteBuffer.allocateDirect(normals.length * 4);
        bb3.order(ByteOrder.nativeOrder());

        normalBuffer = bb3.asFloatBuffer();
        normalBuffer.put(normals);
        normalBuffer.position(0);


        mTextureDataHandle = TextureHelper.loadTexture(mActivityContext, R.drawable.earth);


        float[] texCoordArray = genTexCoord( 50, 50);
        mCubeTextureCoordinates = ByteBuffer.allocateDirect(texCoordArray.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
        mCubeTextureCoordinates.put(texCoordArray).position(0);

        //  vertexShader , fragmentShader , 쉐이더 프로그램 생성
        int vertexShader = EarthRenderer.loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode);
        checkShaderError(vertexShader);
        int fragmentShader = EarthRenderer.loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode);
        checkShaderError(fragmentShader);
        mProgram = GLES20.glCreateProgram();

        //  쉐이더 프로그램에 생성한 vertex, fragment 쉐이더 붙이기
        GLES20.glAttachShader(mProgram, vertexShader);
        GLES20.glAttachShader(mProgram, fragmentShader);
        GLES20.glLinkProgram(mProgram);

    }

    //  sphere 을 가로,세로 count로 쪼개어서, 각 좌표를 배열에 저장하고 나중에 texture 좌표 지정할때, 이용
    //  cube랑 같이 가져옴.
    public  float[] genTexCoord(int bw, int bh) {
        float[] result = new float[bw * bh * 6 * 2];
        float sizew = 1.0f / bw;
        float sizeh = 1.0f / bh;
        int c = 0;
        for (int i = 0; i < bh; i++) {
            for (int j = 0; j < bw; j++) {

                float s = j * sizew;
                float t = i * sizeh;
                result[c++] = s;
                result[c++] = t;
                result[c++] = s;
                result[c++] = t + sizeh;
                result[c++] = s + sizew;
                result[c++] = t;
                result[c++] = s + sizew;
                result[c++] = t;
                result[c++] = s;
                result[c++] = t + sizeh;
                result[c++] = s + sizew;
                result[c++] = t + sizeh;
            }
        }
        return result;
    }

    public void draw(float[] mvpMatrix, float[] normalMat, float[] mvMat) {

        //  mProgram 프로그램 사용 : 환경 설정
        GLES20.glUseProgram(mProgram);

        //  Attribute(속성) Position 설정
        mPositionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition");
        GLES20.glEnableVertexAttribArray(mPositionHandle);


        // Prepare the triangle coordinate data
        GLES20.glVertexAttribPointer(mPositionHandle, COORDS_PER_VERTEX,
                GLES20.GL_FLOAT, false,
                vertexStride, vertexBuffer);

        // now deal with normals
        mNormalHandle = GLES20.glGetAttribLocation(mProgram, "vNormal");
        GLES20.glEnableVertexAttribArray(mNormalHandle);
        // Prepare the normal data
        GLES20.glVertexAttribPointer(mNormalHandle, COORDS_PER_VERTEX,
                GLES20.GL_FLOAT, false,
                vertexStride, normalBuffer);


        //texture
        mCubeTextureCoordinates.position(0);
        GLES20.glVertexAttribPointer(mTextureCoordinateHandle, mTextureCoordinateDataSize, GLES20.GL_FLOAT, false,
                0, mCubeTextureCoordinates);

        GLES20.glEnableVertexAttribArray(mTextureCoordinateHandle);


        //  light uniform 변수
        int light = GLES20.glGetUniformLocation(mProgram, "lightDir");
        GLES20.glUniform3fv(light, 1, lightDir, 0);

        //  mvp , normal matrix 유니폼 변수 할당 및 전달
        mMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");
        mNormalMatHandle = GLES20.glGetUniformLocation(mProgram, "uNormalMat");

        int MVMatHandle = GLES20.glGetUniformLocation(mProgram, "uMVMatrix");

        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mvpMatrix, 0);
        GLES20.glUniformMatrix4fv(mNormalMatHandle, 1, false, normalMat, 0);
        GLES20.glUniformMatrix4fv(MVMatHandle, 1, false, mvMat, 0);

        //texture
        mTextureUniformHandle = GLES20.glGetUniformLocation(mProgram, "u_Texture");
        mTextureCoordinateHandle = GLES20.glGetAttribLocation(mProgram, "a_TexCoordinate");

        // texture0 부터 활성화 후 바인딩
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureDataHandle);
        GLES20.glUniform1i(mTextureUniformHandle, 0);

        // 삼각형으로 이어 붙여 그림.
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, vertexCount);

        // vertex array 해제
        GLES20.glDisableVertexAttribArray(mPositionHandle);
        GLES20.glDisableVertexAttribArray(mNormalHandle);
        GLES20.glDisableVertexAttribArray(mTextureCoordinateHandle);
    }
}
