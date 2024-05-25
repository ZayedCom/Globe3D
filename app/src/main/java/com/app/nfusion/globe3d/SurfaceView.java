package com.app.nfusion.globe3d;

import android.annotation.SuppressLint;
import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;

public class SurfaceView extends GLSurfaceView {

    private com.app.nfusion.globe3d.Renderer renderer;
    private ScaleGestureDetector scaleGestureDetector;

    public SurfaceView(Context context) {
        super(context);
        init(context, null);
    }

    public SurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public SurfaceView(Context context, AttributeSet attrs, int defStyle) {
        super(context);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        renderer = new com.app.nfusion.globe3d.Renderer(context);
        setEGLContextClientVersion(3);
        setRenderer(renderer);
        scaleGestureDetector = new ScaleGestureDetector(context, new ScaleListener());
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent e) {
        scaleGestureDetector.onTouchEvent(e);
        e.getX();
        e.getY();
        return true;
    }

    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            float scaleFactor = detector.getScaleFactor();
            renderer.scaleSphere(scaleFactor);
            // Request render to update the view
            requestRender();
            return true;
        }
    }
}
