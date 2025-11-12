package com.app.nfusion.globe3d;

import android.annotation.SuppressLint;
import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;

import androidx.annotation.NonNull;

public class SurfaceView extends GLSurfaceView {

    private static final float ORBIT_TOUCH_SCALE = 0.01f; // Sensitivity multiplier for touch-based camera rotation
    private static final float MIN_ELEVATION = -1.4f; // Minimum allowed camera vertical angle
    private static final float MAX_ELEVATION = 1.4f; // Maximum allowed camera vertical angle
    private static final float FULL_TURN = (float) (Math.PI * 2.0f); // Full rotation in radians

    private com.app.nfusion.globe3d.Renderer renderer; // OpenGL renderer managing the 3D scene

    public Renderer getRenderer() {
        return renderer;
    }

    private ScaleGestureDetector scaleGestureDetector; // Detects pinch-to-zoom gestures
    private GestureDetector swipeGestureDetector; // Detects swipe gestures for planet switching
    private float lastTouchX; // X coordinate of last touch event
    private float lastTouchY; // Y coordinate of last touch event
    private boolean isDragging = false; // Whether user is currently dragging to rotate camera
    private float orbitAzimuth = (float) Math.PI; // Current camera horizontal rotation angle
    private float orbitElevation = 0.0f; // Current camera vertical tilt angle
    private static final float SWIPE_THRESHOLD = 100; // Minimum distance in pixels to register a swipe
    private static final float SWIPE_VELOCITY_THRESHOLD = 100; // Minimum velocity to register a swipe

    // Initialize SurfaceView with default context
    public SurfaceView(Context context) {
        super(context);
        init(context);
    }

    // Initialize SurfaceView with context and attribute set
    public SurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    // Initialize SurfaceView with context, attribute set, and default style
    @SuppressWarnings("unused")
    public SurfaceView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs);
        init(context);
    }

    // Initialize the renderer and gesture detectors
    private void init(Context context) {
        renderer = new com.app.nfusion.globe3d.Renderer(context);
        setEGLContextClientVersion(3);
        setRenderer(renderer);
        setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
        scaleGestureDetector = new ScaleGestureDetector(context, new ScaleListener());
        swipeGestureDetector = new GestureDetector(context, new SwipeGestureListener());
    }

    // Handle touch events for camera rotation, zoom, and swipe gestures
    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent e) {
        int action = e.getActionMasked();
        int pointerCount = e.getPointerCount();
        boolean scaleHandled = scaleGestureDetector.onTouchEvent(e);
        boolean swipeHandled = swipeGestureDetector.onTouchEvent(e);

        if (pointerCount > 1 || scaleGestureDetector.isInProgress()) {
            isDragging = false;
            return true;
        }

        switch (action) {
            case MotionEvent.ACTION_DOWN:
                lastTouchX = e.getX(0);
                lastTouchY = e.getY(0);
                isDragging = true;
                return true;

            case MotionEvent.ACTION_MOVE:
                if (isDragging) {
                    float x = e.getX(0);
                    float y = e.getY(0);
                    float deltaX = x - lastTouchX;
                    float deltaY = y - lastTouchY;

                    orbitAzimuth = wrapAngle(orbitAzimuth - (deltaX * ORBIT_TOUCH_SCALE));
                    orbitElevation = clampElevation(orbitElevation + (deltaY * ORBIT_TOUCH_SCALE));
                    final float newAzimuth = orbitAzimuth;
                    final float newElevation = orbitElevation;
                    queueEvent(() -> renderer.setCameraAngles(newAzimuth, newElevation));

                    lastTouchX = x;
                    lastTouchY = y;
                    return true;
                }
                break;

            case MotionEvent.ACTION_UP:
                isDragging = false;
                performClick();
                return true;

            case MotionEvent.ACTION_CANCEL:
                isDragging = false;
                return true;

            case MotionEvent.ACTION_POINTER_DOWN:
            case MotionEvent.ACTION_POINTER_UP:
                isDragging = false;
                break;
        }

        return scaleHandled || swipeHandled || isDragging;
    }

    // Perform click action for accessibility support
    @Override
    public boolean performClick() {
        return super.performClick();
    }

    // Gesture listener for detecting pinch-to-zoom gestures
    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        // Handle scale gesture to zoom in or out
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            final float scaleFactor = detector.getScaleFactor();
            queueEvent(() -> renderer.scaleSphere(scaleFactor));
            return true;
        }
    }

    // Gesture listener for detecting swipe gestures on empty space to transition between Earth and Moon
    private class SwipeGestureListener extends GestureDetector.SimpleOnGestureListener {
        // Handle down event to enable gesture detection
        @Override
        public boolean onDown(@NonNull MotionEvent e) {
            return true;
        }

        // Handle fling gesture to transition between Earth, Sun, and Moon when swiping on empty space
        @Override
        public boolean onFling(MotionEvent e1, @NonNull MotionEvent e2, float velocityX, float velocityY) {
            if (e1 == null) {
                return false;
            }

            // Check if touch started AND ended on empty space (not on any celestial body)
            // This ensures we only switch when swiping outside objects, not when panning on them
            if (isTouchOnEmptySpace(e1.getX(), e1.getY()) || isTouchOnEmptySpace(e2.getX(), e2.getY())) {
                return false;
            }

            float diffX = e2.getX() - e1.getX();
            float diffY = e2.getY() - e1.getY();

            // Detect horizontal swipe gestures
            if (Math.abs(diffX) > Math.abs(diffY)) {
                if (Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                    // Swipe left (positive diffX) cycles forward: Earth → Sun → Moon → Earth
                    // Swipe right (negative diffX) cycles backward: Earth → Moon → Sun → Earth
                    boolean swipeLeft = diffX > 0;
                    queueEvent(() -> renderer.cycleCameraTarget(swipeLeft));
                    return true;
                }
            }
            return false;
        }
    }

    // Check if touch event occurred on empty space (not on any celestial body)
    private boolean isTouchOnEmptySpace(float touchX, float touchY) {
        // Get screen center
        float centerX = getWidth() / 2.0f;
        float centerY = getHeight() / 2.0f;

        // Calculate distance from center
        float dx = touchX - centerX;
        float dy = touchY - centerY;
        float distance = (float) Math.sqrt(dx * dx + dy * dy);

        // Approximate sphere radius in screen space (adjust based on zoom and screen size)
        // Use a larger radius to be more conservative - we want to ensure we're definitely outside
        // This accounts for Earth, Sun, and Moon at different zoom levels
        float approximateRadius = Math.min(getWidth(), getHeight()) * 0.35f;

        // Touch is on empty space if it's far from center
        return !(distance > approximateRadius);
    }

    // Normalize angle to range [-PI, PI]
    private float wrapAngle(float angle) {
        angle = angle % FULL_TURN;
        if (angle > Math.PI) {
            angle -= FULL_TURN;
        } else if (angle < -Math.PI) {
            angle += FULL_TURN;
        }
        return angle;
    }

    // Clamp elevation angle to valid range
    private float clampElevation(float elevation) {
        if (elevation < MIN_ELEVATION) {
            return MIN_ELEVATION;
        } else if (elevation > MAX_ELEVATION) {
            return MAX_ELEVATION;
        }
        return elevation;
    }
}
