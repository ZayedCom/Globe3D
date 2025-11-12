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
    private int sunTexture;
    private int earthSpecularTexture;
    private int earthNormalTexture;
    private final float[] mvpMatrix = new float[16];
    private final float[] earthRotationMatrix = new float[16];
    private final float[] earthViewMatrix = new float[16];
    private final float[] earthProjectionMatrix = new float[16];
    private final float[] earthModelMatrix = new float[16];
    private final float[] sunModelMatrix = new float[16];
    private final float[] moonRotationMatrix = new float[16];
    private final float[] moonModelMatrix = new float[16];
    private float earthAngle;
    private float moonAngle;
    private float cloudAngle;
    private float earthOrbitAngle = 0.0f; // Earth's orbit around Sun
    private float moonOrbitAngle = 0.0f; // Moon's orbit around Earth
    private float zoomScale = 1.0f; // Zoom default scale
    private static final float minScale = 0.1f; // Zoom min scale
    private static final float maxScale = 5.0f; // Zoom max scale
    private static final float earthOrbitRadius = 15.0f; // Earth's distance from Sun
    private static final float moonOffset = 3.0f; // Moon's distance from Earth
    private static final float moonRotationSpeed = 0.025f; // 1/4 of previous speed
    private static final float earthOrbitSpeed = 0.005f; // Earth's orbital speed around Sun (1/4 of previous)
    private static final float moonOrbitSpeed = 0.0125f; // Moon's orbital speed around Earth (1/4 of previous)
    private static final float moonScaleFactor = 0.27f; // Real scale: Moon diameter is ~27% of Earth's
    private static final float sunScaleFactor = 2.5f; // Sun size relative to Earth (scaled for visibility)
    private static final float cloudRotationSpeed = 0.0375f; // Clouds rotate faster than Earth due to wind patterns (1/4 of previous)
    private static final float cloudAlpha = 0.5f; // Cloud transparency
    private volatile float cameraAzimuth = (float) Math.PI;
    private volatile float cameraElevation = 0.0f;
    private static final float baseCameraDistance = 3.0f;
    private static final float minElevation = -1.4f;
    private static final float maxElevation = 1.4f;
    private static final float fullTurn = (float) (Math.PI * 2.0f);
    private static final float MIN_CAMERA_DISTANCE = 0.5f; // Minimum distance to prevent camera collision (reduced for more zoom on Earth)
    private volatile int cameraTarget = 0; // 0=Earth, 1=Sun, 2=Moon
    private volatile boolean isTransitioning = false;
    private volatile float transitionProgress = 0.0f;
    private static final float TRANSITION_SPEED = 0.02f;
    private final float[] backgroundMatrix = new float[16];
    private float motionBlurIntensity = 0.0f;
    private final float[] lightDirection = new float[3];
    private final float[] lightColor = new float[]{1.2f, 1.1f, 0.9f}; // Warm sunlight color

    // Initialize the renderer with context and set up identity matrices
    public Renderer(Context context) {
        this.context = context;
        Matrix.setIdentityM(earthRotationMatrix, 0);
        Matrix.setIdentityM(moonRotationMatrix, 0);
        Matrix.setIdentityM(moonModelMatrix, 0);
        Matrix.setIdentityM(sunModelMatrix, 0);
    }

    // Render a single frame: update rotations, handle transitions, and draw all objects
    @Override
    public void onDrawFrame(GL10 gl) {
        GLES32.glClear(GLES32.GL_COLOR_BUFFER_BIT | GLES32.GL_DEPTH_BUFFER_BIT);

        updateEarthRotation();
        updateMoonRotation();
        updateCloudRotation();
        updateEarthOrbit();
        updateMoonOrbit();

        if (isTransitioning) {
            updateTransition();
        }

        // Calculate camera target based on current focus
        float[] targetPos = getCameraTargetPosition();
        float cameraTargetX = targetPos[0];
        float cameraTargetY = targetPos[1];
        float cameraTargetZ = targetPos[2];

        // Calculate camera position with collision detection
        float cameraDistance = baseCameraDistance / zoomScale;
        // Prevent camera from getting too close (collision detection)
        if (cameraTarget == 0) { // Earth
            // Calculate minimum safe distance: Earth radius (1.0) * zoomScale + safety margin
            float earthRadius = zoomScale;
            float minDistance = earthRadius + MIN_CAMERA_DISTANCE;
            if (cameraDistance < minDistance) {
                cameraDistance = minDistance;
                // Also limit zoom scale to prevent getting too close
                float maxZoomForDistance = baseCameraDistance / minDistance;
                if (zoomScale > maxZoomForDistance) {
                    zoomScale = maxZoomForDistance;
                }
            }
        } else if (cameraTarget == 1) { // Sun
            // Prevent camera from getting inside Sun
            float sunRadius = sunScaleFactor * zoomScale;
            float minDistance = sunRadius + 0.5f;
            if (cameraDistance < minDistance) {
                cameraDistance = minDistance;
                // Also limit zoom scale to prevent getting too close
                float maxZoomForDistance = baseCameraDistance / minDistance;
                if (zoomScale > maxZoomForDistance) {
                    zoomScale = maxZoomForDistance;
                }
            }
        } else if (cameraTarget == 2) { // Moon
            // Prevent camera from getting inside Moon - apply same fix as Earth
            // Reduce max zoom by 20% (multiply by 0.8)
            float moonRadius = moonScaleFactor * zoomScale;
            float minDistance = moonRadius + 0.3f;
            // Apply 20% reduction to max zoom
            float adjustedMinDistance = minDistance * 1.25f; // Increase min distance by 25% (equivalent to 20% zoom reduction)
            if (cameraDistance < adjustedMinDistance) {
                cameraDistance = adjustedMinDistance;
                // Also limit zoom scale to prevent getting too close (with 20% reduction)
                float maxZoomForDistance = (baseCameraDistance / adjustedMinDistance) * 0.8f;
                if (zoomScale > maxZoomForDistance) {
                    zoomScale = maxZoomForDistance;
                }
            }
        }

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

        // Draw the Sun first (at center, always visible)
        drawSun();

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
        sunTexture = TextureHelper.loadTexture(context, R.drawable.sun_texture);
        earthSpecularTexture = TextureHelper.loadTexture(context, R.drawable.earth_specular_map);
        earthNormalTexture = TextureHelper.loadTexture(context, R.drawable.earth_normal_map);
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

    // Update Earth's orbit around the Sun
    private void updateEarthOrbit() {
        earthOrbitAngle += earthOrbitSpeed;
        if (earthOrbitAngle > fullTurn) {
            earthOrbitAngle -= fullTurn;
        }
    }

    // Update Moon's orbit around Earth
    private void updateMoonOrbit() {
        moonOrbitAngle += moonOrbitSpeed;
        if (moonOrbitAngle > fullTurn) {
            moonOrbitAngle -= fullTurn;
        }
    }

    // Get Earth's position in world space (orbiting around Sun)
    private float[] getEarthPosition() {
        float x = (float) (earthOrbitRadius * Math.cos(earthOrbitAngle));
        float y = 0.0f;
        float z = (float) (earthOrbitRadius * Math.sin(earthOrbitAngle));
        return new float[]{x, y, z};
    }

    // Get Moon's position in world space (orbiting around Earth)
    private float[] getMoonPosition() {
        float[] earthPos = getEarthPosition();
        float moonX = earthPos[0] + (float) (moonOffset * Math.cos(moonOrbitAngle));
        float moonY = earthPos[1];
        float moonZ = earthPos[2] + (float) (moonOffset * Math.sin(moonOrbitAngle));
        return new float[]{moonX, moonY, moonZ};
    }

    // Get camera target position based on current focus
    private float[] getCameraTargetPosition() {
        return switch (cameraTarget) {
            case 0 -> // Earth
                    getEarthPosition();
            case 1 -> // Sun
                    new float[]{0.0f, 0.0f, 0.0f};
            case 2 -> // Moon
                    getMoonPosition();
            default -> getEarthPosition();
        };
    }

    // Calculate light direction from Sun to Earth
    private void updateLightDirection() {
        float[] earthPos = getEarthPosition();
        // Light direction is from Sun (0,0,0) to Earth
        lightDirection[0] = earthPos[0];
        lightDirection[1] = earthPos[1];
        lightDirection[2] = earthPos[2];
        // Normalize
        float length = (float) Math.sqrt(lightDirection[0] * lightDirection[0] +
                lightDirection[1] * lightDirection[1] +
                lightDirection[2] * lightDirection[2]);
        if (length > 0.001f) {
            lightDirection[0] /= length;
            lightDirection[1] /= length;
            lightDirection[2] /= length;
        }
    }

    // Draw the Sun at the center
    private void drawSun() {
        Matrix.setIdentityM(sunModelMatrix, 0);
        Matrix.scaleM(sunModelMatrix, 0, zoomScale * sunScaleFactor, zoomScale * sunScaleFactor, zoomScale * sunScaleFactor);

        Matrix.multiplyMM(mvpMatrix, 0, earthProjectionMatrix, 0, earthViewMatrix, 0);
        Matrix.multiplyMM(mvpMatrix, 0, mvpMatrix, 0, sunModelMatrix, 0);

        // Draw Sun with glow effect
        float glowIntensity = 0.5f + (float) (Math.sin(System.currentTimeMillis() / 1000.0) * 0.2);
        sphere.drawSun(sunTexture, mvpMatrix, glowIntensity);
    }

    // Draw the Earth sphere with current rotation and position
    private void drawEarth() {
        // Update light direction
        updateLightDirection();

        // Compute the rotation for the Earth
        Matrix.setRotateM(earthRotationMatrix, 0, earthAngle, 0.0f, 1.0f, 0.0f);

        // Get Earth's position in world space
        float[] earthPos = getEarthPosition();

        // Position Earth in world space
        Matrix.setIdentityM(earthModelMatrix, 0);
        Matrix.translateM(earthModelMatrix, 0, earthPos[0], earthPos[1], earthPos[2]);
        Matrix.scaleM(earthModelMatrix, 0, zoomScale, zoomScale, zoomScale);

        // Compute MVP matrix
        Matrix.multiplyMM(mvpMatrix, 0, earthProjectionMatrix, 0, earthViewMatrix, 0);
        float[] tempMatrix = new float[16];
        Matrix.multiplyMM(tempMatrix, 0, earthModelMatrix, 0, earthRotationMatrix, 0);
        Matrix.multiplyMM(mvpMatrix, 0, mvpMatrix, 0, tempMatrix, 0);

        // Draw the Earth with specular, normal maps, and lighting
        float cloudRotationDegrees = cloudAngle - earthAngle;
        sphere.drawEarth(earthTexture, cloudTexture, earthSpecularTexture, earthNormalTexture,
                cloudRotationDegrees, cloudAlpha, mvpMatrix, earthModelMatrix,
                lightDirection, lightColor);
    }

    // Draw the Earth sphere with motion blur effect during transition
    private void drawEarthWithBlur() {
        // Update light direction
        updateLightDirection();

        // Compute the rotation for the Earth
        Matrix.setRotateM(earthRotationMatrix, 0, earthAngle, 0.0f, 1.0f, 0.0f);

        // Get Earth's position in world space
        float[] earthPos = getEarthPosition();

        // Position Earth in world space
        Matrix.setIdentityM(earthModelMatrix, 0);
        Matrix.translateM(earthModelMatrix, 0, earthPos[0], earthPos[1], earthPos[2]);
        Matrix.scaleM(earthModelMatrix, 0, zoomScale, zoomScale, zoomScale);

        // Compute MVP matrix
        Matrix.multiplyMM(mvpMatrix, 0, earthProjectionMatrix, 0, earthViewMatrix, 0);
        float[] tempMatrix = new float[16];
        Matrix.multiplyMM(tempMatrix, 0, earthModelMatrix, 0, earthRotationMatrix, 0);
        Matrix.multiplyMM(mvpMatrix, 0, mvpMatrix, 0, tempMatrix, 0);

        // Draw the Earth with motion blur and cloud overlay
        float currentBlurIntensity = motionBlurIntensity;
        float cloudRotationDegrees = cloudAngle - earthAngle;
        sphere.drawEarthWithBlur(earthTexture, cloudTexture, cloudRotationDegrees, cloudAlpha, mvpMatrix, currentBlurIntensity);
    }

    // Draw the Moon sphere with current rotation and position
    private void drawMoon() {
        // Compute the rotation for the Moon
        Matrix.setRotateM(moonRotationMatrix, 0, moonAngle, 0.0f, 1.0f, 0.0f);

        // Get Moon's position in world space
        float[] moonPos = getMoonPosition();

        // Position Moon in world space
        Matrix.setIdentityM(moonModelMatrix, 0);
        Matrix.translateM(moonModelMatrix, 0, moonPos[0], moonPos[1], moonPos[2]);
        Matrix.scaleM(moonModelMatrix, 0, zoomScale * moonScaleFactor, zoomScale * moonScaleFactor, zoomScale * moonScaleFactor);

        Matrix.multiplyMM(mvpMatrix, 0, earthProjectionMatrix, 0, earthViewMatrix, 0);
        float[] tempMatrix = new float[16];
        Matrix.multiplyMM(tempMatrix, 0, moonModelMatrix, 0, moonRotationMatrix, 0);
        Matrix.multiplyMM(mvpMatrix, 0, mvpMatrix, 0, tempMatrix, 0);

        // Draw the Moon
        sphere.drawMoon(moonTexture, mvpMatrix);
    }

    // Draw the Moon sphere with motion blur effect during transition
    private void drawMoonWithBlur() {
        // Compute the rotation for the Moon
        Matrix.setRotateM(moonRotationMatrix, 0, moonAngle, 0.0f, 1.0f, 0.0f);

        // Get Moon's position in world space
        float[] moonPos = getMoonPosition();

        // Position Moon in world space
        Matrix.setIdentityM(moonModelMatrix, 0);
        Matrix.translateM(moonModelMatrix, 0, moonPos[0], moonPos[1], moonPos[2]);
        Matrix.scaleM(moonModelMatrix, 0, zoomScale * moonScaleFactor, zoomScale * moonScaleFactor, zoomScale * moonScaleFactor);

        Matrix.multiplyMM(mvpMatrix, 0, earthProjectionMatrix, 0, earthViewMatrix, 0);
        float[] tempMatrix = new float[16];
        Matrix.multiplyMM(tempMatrix, 0, moonModelMatrix, 0, moonRotationMatrix, 0);
        Matrix.multiplyMM(mvpMatrix, 0, mvpMatrix, 0, tempMatrix, 0);

        // Draw the Moon with motion blur
        float currentBlurIntensity = motionBlurIntensity;
        sphere.drawMoonWithBlur(moonTexture, mvpMatrix, currentBlurIntensity);
    }

    // Scale the zoom level and clamp it to valid range with collision detection
    public void scaleSphere(float scaleFactor) {
        float newZoomScale = zoomScale * scaleFactor;

        // Apply collision detection based on current camera target
        if (cameraTarget == 0) { // Earth
            float earthRadius = 1.0f;
            float minDistance = earthRadius + MIN_CAMERA_DISTANCE;
            float maxZoomForDistance = baseCameraDistance / minDistance;
            if (newZoomScale > maxZoomForDistance) {
                newZoomScale = maxZoomForDistance;
            }
        } else if (cameraTarget == 1) { // Sun
            float minDistance = sunScaleFactor + 0.5f;
            float maxZoomForDistance = baseCameraDistance / minDistance;
            if (newZoomScale > maxZoomForDistance) {
                newZoomScale = maxZoomForDistance;
            }
        } else if (cameraTarget == 2) { // Moon
            // Apply same collision fix as Earth - limit zoom to prevent camera inside Moon
            // Reduce max zoom by 20% (multiply by 0.8)
            float minDistance = moonScaleFactor + 0.3f;
            // Apply 20% reduction to max zoom
            float adjustedMinDistance = minDistance * 1.25f; // Increase min distance by 25% (equivalent to 20% zoom reduction)
            float maxZoomForDistance = (baseCameraDistance / adjustedMinDistance) * 0.8f;
            if (newZoomScale > maxZoomForDistance) {
                newZoomScale = maxZoomForDistance;
            }
        }

        // Clamp to general min/max scale
        if (newZoomScale < minScale) {
            zoomScale = minScale;
        } else zoomScale = Math.min(newZoomScale, maxScale);
    }

    // Set camera angles for orbiting around the current target
    public void setCameraAngles(float azimuth, float elevation) {
        cameraAzimuth = normalizeAzimuth(azimuth);
        cameraElevation = clampElevation(elevation);
    }

    // Cycle camera target: Earth → Sun → Moon → Earth
    public synchronized void cycleCameraTarget(boolean swipeLeft) {
        if (!isTransitioning) {
            isTransitioning = true;
            if (swipeLeft) {
                // Swipe left: Earth → Sun → Moon → Earth
                cameraTarget = (cameraTarget + 1) % 3;
            } else {
                // Swipe right: Earth → Moon → Sun → Earth
                cameraTarget = (cameraTarget + 2) % 3;
            }
            transitionProgress = 0.0f;
        }
    }

    // Update transition progress and motion blur intensity during camera movement
    private synchronized void updateTransition() {
        float newProgress = transitionProgress + TRANSITION_SPEED;
        if (newProgress >= 1.0f) {
            transitionProgress = 1.0f;
            isTransitioning = false;
            motionBlurIntensity = 0.0f;
        } else {
            transitionProgress = newProgress;
            motionBlurIntensity = (float) Math.sin(transitionProgress * Math.PI) * 1.2f;
        }
    }

    // Draw the background space texture as a large sphere surrounding the scene
    private void drawBackground() {
        Matrix.setIdentityM(backgroundMatrix, 0);
        Matrix.scaleM(backgroundMatrix, 0, 50.0f, 50.0f, 50.0f);
        Matrix.multiplyMM(backgroundMatrix, 0, mvpMatrix, 0, backgroundMatrix, 0);
        sphere.drawBackground(backgroundTexture, backgroundMatrix);
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
