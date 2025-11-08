package com.app.nfusion.globe3d;

import android.content.Context;
import android.opengl.GLES32;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;

import javax.microedition.khronos.opengles.GL10;

public class Renderer implements GLSurfaceView.Renderer {

    private final Context context;
    private Sphere sphere;
    private int earthTexture;
    private int moonTexture;
    private int backgroundTexture;
    private int cloudTexture;
    private final float[] mvpMatrix = new float[16];
    private final float[] earthRotationMatrix = new float[16];
    private final float[] earthViewMatrix = new float[16];
    private final float[] earthProjectionMatrix = new float[16];
    private final float[] earthModelMatrix = new float[16];
    private final float[] moonRotationMatrix = new float[16];
    private final float[] moonModelMatrix = new float[16];
    private float earthAngle;
    private float moonAngle;
    private float cloudAngle;
    private float zoomScale = 1.0f; // Zoom default scale
    private static final float minScale = 0.1f; // Zoom min scale
    private static final float maxScale = 5.0f; // Zoom max scale
    private static final float moonOffset = 8.0f; // Real scale distance: Moon is ~60 Earth radii away (scaled for visibility)
    private static final float moonRotationSpeed = 0.5f;
    private static final float moonScaleFactor = 0.27f; // Real scale: Moon diameter is ~27% of Earth's
    private static final float cloudRotationSpeed = 0.7f; // Clouds rotate faster than Earth due to wind patterns
    private static final float cloudAlpha = 0.5f; // Cloud transparency
    private volatile float cameraAzimuth = (float) Math.PI;
    private volatile float cameraElevation = 0.0f;
    private static final float baseCameraDistance = 3.0f;
    private static final float minElevation = -1.4f;
    private static final float maxElevation = 1.4f;
    private static final float fullTurn = (float) (Math.PI * 2.0f);
    private volatile boolean isOrbitingMoon = false;
    private volatile boolean isTransitioning = false;
    private volatile float transitionProgress = 0.0f;
    private static final float TRANSITION_SPEED = 0.015f;
    private final float[] backgroundMatrix = new float[16];
    private float motionBlurIntensity = 0.0f;

    // Initialize the renderer with context and set up identity matrices
    public Renderer(Context context) {
        this.context = context;
        Matrix.setIdentityM(earthRotationMatrix, 0);
        Matrix.setIdentityM(moonRotationMatrix, 0);
        Matrix.setIdentityM(moonModelMatrix, 0);
    }

    // Render a single frame: update rotations, handle transitions, and draw all objects
    @Override
    public void onDrawFrame(GL10 gl) {
        GLES32.glClear(GLES32.GL_COLOR_BUFFER_BIT | GLES32.GL_DEPTH_BUFFER_BIT);

        updateEarthRotation();
        updateMoonRotation();
        updateCloudRotation();

        if (isTransitioning) {
            updateTransition();
        }

        // Calculate camera target: always look at (0,0,0) - objects move around this point
        // When orbiting Earth: Earth at (0,0,0), Moon at (0,0,moonOffset)
        // When orbiting Moon: Moon at (0,0,0), Earth at (0,0,-moonOffset)
        float cameraTargetX = 0.0f;
        float cameraTargetY = 0.0f;
        float cameraTargetZ = 0.0f;

        float cameraDistance = baseCameraDistance / zoomScale;
        float x = cameraTargetX + (float) (cameraDistance * Math.cos(cameraElevation) * Math.sin(cameraAzimuth));
        float y = cameraTargetY + (float) (cameraDistance * Math.sin(cameraElevation));
        float z = cameraTargetZ + (float) (cameraDistance * Math.cos(cameraElevation) * Math.cos(cameraAzimuth));

        Matrix.setLookAtM(earthViewMatrix, 0,
                x, y, z,
                cameraTargetX, cameraTargetY, cameraTargetZ,
                0, 1, 0);

        // Compute the projection and view transformation
        Matrix.multiplyMM(mvpMatrix, 0, earthProjectionMatrix, 0, earthViewMatrix, 0);

        // Draw background space
        drawBackground();

        // Draw the Earth with motion blur during transition, otherwise draw normally
        float currentBlurIntensity = motionBlurIntensity;
        boolean currentlyTransitioning = isTransitioning;
        if (currentlyTransitioning && currentBlurIntensity > 0.01f) {
            drawEarthWithBlur();
        } else {
            drawEarth();
        }

        // Draw the Moon with motion blur during transition, otherwise draw normally
        if (currentlyTransitioning && currentBlurIntensity > 0.01f) {
            drawMoonWithBlur();
        } else {
            drawMoon();
        }
    }

    // Initialize OpenGL state and load textures when surface is created
    @Override
    public void onSurfaceCreated(GL10 gl, javax.microedition.khronos.egl.EGLConfig config) {
        GLES32.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);        // Set the clear color to black with full opacity
        GLES32.glEnable(GLES32.GL_DEPTH_TEST);                                    // Enable depth testing for accurate 3D rendering
        GLES32.glEnable(GLES32.GL_BLEND);                                         // Enable blending for transparency effects
        GLES32.glBlendFunc(GLES32.GL_SRC_ALPHA, GLES32.GL_ONE_MINUS_SRC_ALPHA);   // Set the blending function to use the source alpha value for transparency blending

        sphere = new Sphere();
        sphere.init();

        earthTexture = TextureHelper.loadTexture(context, R.drawable.earth_texture);
        moonTexture = TextureHelper.loadTexture(context, R.drawable.moon_texture);
        backgroundTexture = TextureHelper.loadTexture(context, R.drawable.milky_way);
        cloudTexture = TextureHelper.loadTexture(context, R.drawable.earth_clouds);
    }

    // Update viewport and projection matrix when surface size changes
    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        GLES32.glViewport(0, 0, width, height);
        float ratio = (float) width / height;
        Matrix.frustumM(earthProjectionMatrix, 0, -ratio, ratio, -1, 1, 0.5f, 100.0f);
    }

    // Update Earth rotation angle for continuous rotation animation
    private void updateEarthRotation() {
        // Rotate Earth around y-axis
        earthAngle += moonRotationSpeed;
    }

    // Update Moon rotation angle for continuous rotation animation
    private void updateMoonRotation() {
        // Rotate Moon at half speed around y-axis
        moonAngle -= (moonRotationSpeed / 2.0f);
    }

    // Update cloud rotation angle for realistic cloud movement (faster than Earth due to wind)
    private void updateCloudRotation() {
        // Rotate clouds faster than Earth to simulate wind patterns
        cloudAngle += cloudRotationSpeed;
    }

    // Calculate Earth's Z position based on current orbit state and transition progress
    private float calculateEarthZPosition() {
        float currentProgress = transitionProgress;
        boolean currentlyTransitioning = isTransitioning;
        boolean currentlyOrbitingMoon = isOrbitingMoon;
        if (currentlyTransitioning) {
            // During transition, interpolate Earth position between start and end positions
            float startZ = currentlyOrbitingMoon ? -moonOffset : 0.0f;
            float endZ = currentlyOrbitingMoon ? 0.0f : -moonOffset;
            return startZ + (endZ - startZ) * currentProgress;
        } else {
            // After transition: if orbiting Moon, Earth is at -moonOffset; if orbiting Earth, Earth is at center
            return currentlyOrbitingMoon ? -moonOffset : 0.0f;
        }
    }

    // Calculate Moon's Z position based on current orbit state and transition progress
    private float calculateMoonZPosition() {
        float currentProgress = transitionProgress;
        boolean currentlyTransitioning = isTransitioning;
        boolean currentlyOrbitingMoon = isOrbitingMoon;
        if (currentlyTransitioning) {
            // During transition, interpolate Moon position between start and end positions
            float startZ = currentlyOrbitingMoon ? 0.0f : moonOffset;
            float endZ = currentlyOrbitingMoon ? moonOffset : 0.0f;
            return startZ + (endZ - startZ) * currentProgress;
        } else {
            // After transition: if orbiting Earth, Moon is at moonOffset; if orbiting Moon, Moon is at center
            return currentlyOrbitingMoon ? 0.0f : moonOffset;
        }
    }

    // Draw the Earth sphere with current rotation and position
    private void drawEarth() {
        // Compute the rotation for the Earth
        Matrix.setRotateM(earthRotationMatrix, 0, earthAngle, 0.0f, 1.0f, 0.0f);

        // Compute the final MVP matrix for the Earth
        Matrix.multiplyMM(mvpMatrix, 0, earthProjectionMatrix, 0, earthViewMatrix, 0);
        Matrix.multiplyMM(mvpMatrix, 0, mvpMatrix, 0, earthRotationMatrix, 0);

        // Position Earth: interpolate during transition
        Matrix.setIdentityM(earthModelMatrix, 0);
        float earthZ = calculateEarthZPosition();
        if (earthZ != 0.0f) {
            Matrix.translateM(earthModelMatrix, 0, 0.0f, 0.0f, earthZ);
        }
        Matrix.scaleM(earthModelMatrix, 0, zoomScale, zoomScale, zoomScale);
        Matrix.multiplyMM(mvpMatrix, 0, mvpMatrix, 0, earthModelMatrix, 0);

        // Draw the Earth with cloud overlay
        float cloudRotationDegrees = cloudAngle - earthAngle;
        sphere.drawEarth(earthTexture, cloudTexture, cloudRotationDegrees, cloudAlpha, mvpMatrix);
    }

    // Draw the Earth sphere with motion blur effect during transition
    private void drawEarthWithBlur() {
        // Compute the rotation for the Earth
        Matrix.setRotateM(earthRotationMatrix, 0, earthAngle, 0.0f, 1.0f, 0.0f);

        // Compute the final MVP matrix for the Earth
        Matrix.multiplyMM(mvpMatrix, 0, earthProjectionMatrix, 0, earthViewMatrix, 0);
        Matrix.multiplyMM(mvpMatrix, 0, mvpMatrix, 0, earthRotationMatrix, 0);

        // Position Earth: interpolate during transition
        Matrix.setIdentityM(earthModelMatrix, 0);
        float earthZ = calculateEarthZPosition();
        if (earthZ != 0.0f) {
            Matrix.translateM(earthModelMatrix, 0, 0.0f, 0.0f, earthZ);
        }
        Matrix.scaleM(earthModelMatrix, 0, zoomScale, zoomScale, zoomScale);
        Matrix.multiplyMM(mvpMatrix, 0, mvpMatrix, 0, earthModelMatrix, 0);

        // Draw the Earth with motion blur and cloud overlay
        float currentBlurIntensity = motionBlurIntensity;
        float cloudRotationDegrees = cloudAngle - earthAngle;
        sphere.drawEarthWithBlur(earthTexture, cloudTexture, cloudRotationDegrees, cloudAlpha, mvpMatrix, currentBlurIntensity);
    }

    // Draw the Moon sphere with current rotation and position
    private void drawMoon() {
        // Compute the rotation for the Moon
        Matrix.setRotateM(moonRotationMatrix, 0, moonAngle, 0.0f, 1.0f, 0.0f);

        // Position Moon: interpolate during transition
        Matrix.setIdentityM(moonModelMatrix, 0);
        float moonZ = calculateMoonZPosition();
        if (moonZ != 0.0f) {
            Matrix.translateM(moonModelMatrix, 0, 0.0f, 0.0f, moonZ);
        }
        Matrix.scaleM(moonModelMatrix, 0, zoomScale * moonScaleFactor, zoomScale * moonScaleFactor, zoomScale * moonScaleFactor);

        Matrix.multiplyMM(mvpMatrix, 0, earthProjectionMatrix, 0, earthViewMatrix, 0);
        Matrix.multiplyMM(mvpMatrix, 0, mvpMatrix, 0, moonRotationMatrix, 0);
        Matrix.multiplyMM(mvpMatrix, 0, mvpMatrix, 0, moonModelMatrix, 0);

        // Draw the Moon
        sphere.drawMoon(moonTexture, mvpMatrix);
    }

    // Draw the Moon sphere with motion blur effect during transition
    private void drawMoonWithBlur() {
        // Compute the rotation for the Moon
        Matrix.setRotateM(moonRotationMatrix, 0, moonAngle, 0.0f, 1.0f, 0.0f);

        // Position Moon: interpolate during transition
        Matrix.setIdentityM(moonModelMatrix, 0);
        float moonZ = calculateMoonZPosition();
        if (moonZ != 0.0f) {
            Matrix.translateM(moonModelMatrix, 0, 0.0f, 0.0f, moonZ);
        }
        Matrix.scaleM(moonModelMatrix, 0, zoomScale * moonScaleFactor, zoomScale * moonScaleFactor, zoomScale * moonScaleFactor);

        Matrix.multiplyMM(mvpMatrix, 0, earthProjectionMatrix, 0, earthViewMatrix, 0);
        Matrix.multiplyMM(mvpMatrix, 0, mvpMatrix, 0, moonRotationMatrix, 0);
        Matrix.multiplyMM(mvpMatrix, 0, mvpMatrix, 0, moonModelMatrix, 0);

        // Draw the Moon with motion blur
        float currentBlurIntensity = motionBlurIntensity;
        sphere.drawMoonWithBlur(moonTexture, mvpMatrix, currentBlurIntensity);
    }

    // Scale the zoom level and clamp it to valid range
    public void scaleSphere(float scaleFactor) {
        zoomScale *= scaleFactor;
        if (zoomScale < minScale) {
            zoomScale = minScale;
        } else if (zoomScale > maxScale) {
            zoomScale = maxScale;
        }
    }

    // Set camera angles for orbiting around the current target
    public void setCameraAngles(float azimuth, float elevation) {
        cameraAzimuth = normalizeAzimuth(azimuth);
        cameraElevation = clampElevation(elevation);
    }

    // Start transition to orbit the Moon
    public synchronized void transitionToMoon() {
        if (!isTransitioning && !isOrbitingMoon) {
            isTransitioning = true;
            transitionProgress = 0.0f;
        }
    }

    // Start transition to orbit the Earth
    public synchronized void transitionToEarth() {
        if (!isTransitioning && isOrbitingMoon) {
            isTransitioning = true;
            transitionProgress = 1.0f;
        }
    }

    // Update transition progress and motion blur intensity during camera movement
    private synchronized void updateTransition() {
        if (!isOrbitingMoon) {
            float newProgress = transitionProgress + TRANSITION_SPEED;
            if (newProgress >= 1.0f) {
                transitionProgress = 1.0f;
                isOrbitingMoon = true;
                isTransitioning = false;
                motionBlurIntensity = 0.0f;
            } else {
                transitionProgress = newProgress;
                motionBlurIntensity = (float) Math.sin(transitionProgress * Math.PI) * 1.2f;
            }
        } else {
            float newProgress = transitionProgress - TRANSITION_SPEED;
            if (newProgress <= 0.0f) {
                transitionProgress = 0.0f;
                isOrbitingMoon = false;
                isTransitioning = false;
                motionBlurIntensity = 0.0f;
            } else {
                transitionProgress = newProgress;
                motionBlurIntensity = (float) Math.sin(transitionProgress * Math.PI) * 1.2f;
            }
        }
    }

    // Draw the background space texture as a large sphere surrounding the scene
    private void drawBackground() {
        Matrix.setIdentityM(backgroundMatrix, 0);
        Matrix.scaleM(backgroundMatrix, 0, 50.0f, 50.0f, 50.0f);
        Matrix.multiplyMM(backgroundMatrix, 0, mvpMatrix, 0, backgroundMatrix, 0);
        sphere.drawBackground(backgroundTexture, backgroundMatrix);
    }

    // Check if currently orbiting the Moon
    public boolean isOrbitingMoon() {
        return isOrbitingMoon;
    }

    // Clamp elevation angle to valid range to prevent camera from going too high or low
    private float clampElevation(float value) {
        if (value < minElevation) {
            return minElevation;
        } else if (value > maxElevation) {
            return maxElevation;
        }
        return value;
    }

    // Normalize azimuth angle to range [-PI, PI] for consistent camera rotation
    private float normalizeAzimuth(float value) {
        value = value % fullTurn;
        if (value > Math.PI) {
            value -= fullTurn;
        } else if (value < -Math.PI) {
            value += fullTurn;
        }
        return value;
    }
}
