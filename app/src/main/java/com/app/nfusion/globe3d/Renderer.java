package com.app.nfusion.globe3d;

import android.content.Context;
import android.opengl.GLES32;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class Renderer implements GLSurfaceView.Renderer {

    private final Context context;
    private Sphere sphere;
    private int earthTexture;
    private int cloudTexture;
    private final float[] rotationMatrix = new float[16];
    private final float[] viewMatrix = new float[16];
    private final float[] projectionMatrix = new float[16];
    private final float[] mvpMatrix = new float[16];
    private final float[] modelMatrix = new float[16];
    private float angle;
    private float scale = 1.0f;
    private static final float minScale = 0.5f;
    private static final float maxScale = 2.0f;

    public Renderer(Context context) {
        this.context = context;
        Matrix.setIdentityM(rotationMatrix, 0);
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        GLES32.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        GLES32.glEnable(GLES32.GL_DEPTH_TEST);
        GLES32.glEnable(GLES32.GL_BLEND); // Enable blending for transparency
        GLES32.glBlendFunc(GLES32.GL_SRC_ALPHA, GLES32.GL_ONE_MINUS_SRC_ALPHA); // Set blending function

        sphere = new Sphere();
        sphere.init();

        earthTexture = TextureHelper.loadTexture(context, R.drawable.earth_texture);
        cloudTexture = TextureHelper.loadTexture(context, R.drawable.earth_clouds);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        GLES32.glClear(GLES32.GL_COLOR_BUFFER_BIT | GLES32.GL_DEPTH_BUFFER_BIT);

        angle += 0.5f;
        Matrix.setRotateM(rotationMatrix, 0, angle, 0.0f, 1.0f, 0.0f);

        Matrix.setLookAtM(viewMatrix, 0,
                0, 0, -5,
                0, 0, 0,
                0, 1, 0);

        Matrix.multiplyMM(mvpMatrix, 0, viewMatrix, 0, rotationMatrix, 0);
        Matrix.multiplyMM(mvpMatrix, 0, projectionMatrix, 0, mvpMatrix, 0);

        Matrix.setIdentityM(modelMatrix, 0);
        Matrix.scaleM(modelMatrix, 0, scale, scale, scale);

        Matrix.multiplyMM(mvpMatrix, 0, mvpMatrix, 0, modelMatrix, 0);

        // Draw clouds
        sphere.drawCloud(cloudTexture, mvpMatrix);

        // Draw earth with clouds
        sphere.drawEarth(earthTexture, mvpMatrix);
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        GLES32.glViewport(0, 0, width, height);
        float ratio = (float) width / height;
        Matrix.frustumM(projectionMatrix, 0, -ratio, ratio, -1, 1, 3, 7);
    }

    public void scaleSphere(float scaleFactor) {
        scale *= scaleFactor;
        if (scale < minScale) {
            scale = minScale;
        } else if (scale > maxScale) {
            scale = maxScale;
        }
    }
}
