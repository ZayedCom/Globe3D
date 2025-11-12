package com.app.nfusion.globe3d;

import android.content.Context;
import android.opengl.GLES32;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.opengles.GL10;

public class Renderer implements GLSurfaceView.Renderer {

    private final Context context; // Android context for loading resources
    private Sphere sphere; // Sphere geometry and shader manager
    private int sunTexture; // Sun surface texture ID
    private int mercuryTexture; // Mercury surface texture ID
    private int venusTexture; // Venus surface texture ID
    private int earthTexture; // Earth surface texture ID
    private int marsTexture; // Mars surface texture ID
    private int jupiterTexture; // Jupiter surface texture ID
    private int saturnTexture; // Saturn surface texture ID
    private int uranusTexture; // Uranus surface texture ID
    private int neptuneTexture; // Neptune surface texture ID
    private int moonTexture; // Moon surface texture ID
    private int backgroundTexture; // Space background texture ID
    private int cloudTexture; // Earth cloud layer texture ID
    private int earthSpecularTexture; // Earth specular reflection map ID
    private int earthNormalTexture; // Earth normal map for surface detail ID
    private int earthNightTexture; // Earth night lights texture ID
    private final float[] mvpMatrix = new float[16]; // Model-view-projection transformation matrix
    private final float[] earthViewMatrix = new float[16]; // Camera view transformation matrix
    private final float[] earthProjectionMatrix = new float[16]; // Perspective projection matrix

    // Model matrices for all celestial bodies
    private final float[] sunModelMatrix = new float[16]; // Sun position and scale matrix
    private final float[] mercuryModelMatrix = new float[16]; // Mercury position and scale matrix
    private final float[] venusModelMatrix = new float[16]; // Venus position and scale matrix
    private final float[] earthModelMatrix = new float[16]; // Earth position and scale matrix
    private final float[] marsModelMatrix = new float[16]; // Mars position and scale matrix
    private final float[] jupiterModelMatrix = new float[16]; // Jupiter position and scale matrix
    private final float[] saturnModelMatrix = new float[16]; // Saturn position and scale matrix
    private final float[] uranusModelMatrix = new float[16]; // Uranus position and scale matrix
    private final float[] neptuneModelMatrix = new float[16]; // Neptune position and scale matrix
    private final float[] moonModelMatrix = new float[16]; // Moon position and scale matrix

    // Rotation matrices for all celestial bodies
    private final float[] sunRotationMatrix = new float[16]; // Sun rotation transformation matrix
    private final float[] mercuryRotationMatrix = new float[16]; // Mercury rotation transformation matrix
    private final float[] venusRotationMatrix = new float[16]; // Venus rotation transformation matrix
    private final float[] earthRotationMatrix = new float[16]; // Earth rotation transformation matrix
    private final float[] marsRotationMatrix = new float[16]; // Mars rotation transformation matrix
    private final float[] jupiterRotationMatrix = new float[16]; // Jupiter rotation transformation matrix
    private final float[] saturnRotationMatrix = new float[16]; // Saturn rotation transformation matrix
    private final float[] uranusRotationMatrix = new float[16]; // Uranus rotation transformation matrix
    private final float[] neptuneRotationMatrix = new float[16]; // Neptune rotation transformation matrix
    private final float[] moonRotationMatrix = new float[16]; // Moon rotation transformation matrix

    // Rotation angles for all celestial bodies
    private float sunAngle; // Current Sun rotation angle
    private float mercuryAngle; // Current Mercury rotation angle
    private float venusAngle; // Current Venus rotation angle
    private float earthAngle; // Current Earth rotation angle
    private float marsAngle; // Current Mars rotation angle
    private float jupiterAngle; // Current Jupiter rotation angle
    private float saturnAngle; // Current Saturn rotation angle
    private float uranusAngle; // Current Uranus rotation angle
    private float neptuneAngle; // Current Neptune rotation angle
    private float moonAngle; // Current Moon rotation angle
    private float cloudAngle; // Current cloud layer rotation angle

    // Orbit angles for all planets
    private float mercuryOrbitAngle = 0.0f; // Mercury's position in orbit around Sun
    private float venusOrbitAngle = 0.0f; // Venus's position in orbit around Sun
    private float earthOrbitAngle = 0.0f; // Earth's position in orbit around Sun
    private float marsOrbitAngle = 0.0f; // Mars's position in orbit around Sun
    private float jupiterOrbitAngle = 0.0f; // Jupiter's position in orbit around Sun
    private float saturnOrbitAngle = 0.0f; // Saturn's position in orbit around Sun
    private float uranusOrbitAngle = 0.0f; // Uranus's position in orbit around Sun
    private float neptuneOrbitAngle = 0.0f; // Neptune's position in orbit around Sun
    private float moonOrbitAngle = 0.0f; // Moon's position in orbit around Earth
    private float zoomScale = 1.0f; // Current zoom level multiplier
    private static final float minScale = 0.1f; // Minimum allowed zoom level
    private static final float maxScale = 5.0f; // Maximum allowed zoom level
    // Orbit radii (scaled for visualization, Earth = 15.0)
    private static final float mercuryOrbitRadius = 6.0f; // Distance from Sun to Mercury
    private static final float venusOrbitRadius = 11.0f; // Distance from Sun to Venus
    private static final float earthOrbitRadius = 15.0f; // Distance from Sun to Earth
    private static final float marsOrbitRadius = 23.0f; // Distance from Sun to Mars
    private static final float jupiterOrbitRadius = 50.0f; // Distance from Sun to Jupiter
    private static final float saturnOrbitRadius = 90.0f; // Distance from Sun to Saturn
    private static final float uranusOrbitRadius = 180.0f; // Distance from Sun to Uranus
    private static final float neptuneOrbitRadius = 280.0f; // Distance from Sun to Neptune
    private static final float moonOffset = 3.0f; // Distance from Earth to Moon

    // Rotation speeds per frame
    private static final float sunRotationSpeed = 0.01f; // Sun rotation speed per frame
    private static final float mercuryRotationSpeed = 0.015f; // Mercury rotation speed per frame
    private static final float venusRotationSpeed = 0.002f; // Venus rotation speed per frame (retrograde)
    private static final float earthRotationSpeed = 0.025f; // Earth rotation speed per frame
    private static final float marsRotationSpeed = 0.026f; // Mars rotation speed per frame
    private static final float jupiterRotationSpeed = 0.05f; // Jupiter rotation speed per frame
    private static final float saturnRotationSpeed = 0.04f; // Saturn rotation speed per frame
    private static final float uranusRotationSpeed = 0.03f; // Uranus rotation speed per frame
    private static final float neptuneRotationSpeed = 0.032f; // Neptune rotation speed per frame
    private static final float moonRotationSpeed = 0.025f; // Moon rotation speed per frame
    private static final float cloudRotationSpeed = 0.0375f; // Cloud layer rotation speed per frame

    // Orbital speeds per frame (relative to Earth)
    private static final float mercuryOrbitSpeed = 0.016f; // Mercury orbital speed around Sun per frame
    private static final float venusOrbitSpeed = 0.012f; // Venus orbital speed around Sun per frame
    private static final float earthOrbitSpeed = 0.005f; // Earth orbital speed around Sun per frame
    private static final float marsOrbitSpeed = 0.004f; // Mars orbital speed around Sun per frame
    private static final float jupiterOrbitSpeed = 0.001f; // Jupiter orbital speed around Sun per frame
    private static final float saturnOrbitSpeed = 0.0005f; // Saturn orbital speed around Sun per frame
    private static final float uranusOrbitSpeed = 0.0002f; // Uranus orbital speed around Sun per frame
    private static final float neptuneOrbitSpeed = 0.0001f; // Neptune orbital speed around Sun per frame
    private static final float moonOrbitSpeed = 0.0125f; // Moon orbital speed around Earth per frame

    // Scale factors relative to Earth (Earth = 1.0)
    private static final float sunScaleFactor = 2.5f; // Sun size relative to Earth
    private static final float mercuryScaleFactor = 0.38f; // Mercury size relative to Earth
    private static final float venusScaleFactor = 0.95f; // Venus size relative to Earth
    private static final float earthScaleFactor = 1.0f; // Earth size (reference)
    private static final float marsScaleFactor = 0.53f; // Mars size relative to Earth
    private static final float jupiterScaleFactor = 11.2f; // Jupiter size relative to Earth
    private static final float saturnScaleFactor = 9.4f; // Saturn size relative to Earth
    private static final float uranusScaleFactor = 4.0f; // Uranus size relative to Earth
    private static final float neptuneScaleFactor = 3.9f; // Neptune size relative to Earth
    private static final float moonScaleFactor = 0.27f; // Moon size relative to Earth
    private static final float cloudAlpha = 0.5f; // Cloud layer transparency level
    private volatile float cameraAzimuth = (float) Math.PI; // Camera horizontal rotation angle
    private volatile float cameraElevation = 0.0f; // Camera vertical tilt angle
    private static final float baseCameraDistance = 3.0f; // Default camera distance from target
    private static final float minElevation = -1.4f; // Minimum camera vertical angle
    private static final float maxElevation = 1.4f; // Maximum camera vertical angle
    private static final float fullTurn = (float) (Math.PI * 2.0f); // Full rotation in radians
    private static final float MIN_CAMERA_DISTANCE = 0.5f; // Minimum safe distance to prevent collision
    private volatile int cameraTarget = 3; // Current camera focus: 0=Sun, 1=Mercury, 2=Venus, 3=Earth, 4=Moon, 5=Mars, 6=Jupiter, 7=Saturn, 8=Uranus, 9=Neptune

    // Set loading callback
    public void setLoadingCallback(LoadingCallback callback) {
        this.loadingCallback = callback;
    }

    // Get current camera target name
    public String getCameraTargetName() {
        return switch (cameraTarget) {
            case 0 -> "Sun";
            case 1 -> "Mercury";
            case 2 -> "Venus";
            case 3 -> "Earth";
            case 4 -> "Moon";
            case 5 -> "Mars";
            case 6 -> "Jupiter";
            case 7 -> "Saturn";
            case 8 -> "Uranus";
            case 9 -> "Neptune";
            default -> "Unknown";
        };
    }

    public int getCameraTarget() {
        return cameraTarget;
    }

    public float getFps() {
        return currentFps;
    }

    private volatile boolean isTransitioning = false; // Whether camera is transitioning between targets
    private volatile float transitionProgress = 0.0f; // Progress of camera transition (0.0 to 1.0)
    private static final float TRANSITION_SPEED = 0.015f; // Speed of camera transition per frame (slower for better blur visibility)
    private final float[] transitionStartPos = new float[3]; // Camera target position at start of transition
    private final float[] backgroundMatrix = new float[16]; // Background sphere transformation matrix
    private float motionBlurIntensity = 0.0f; // Motion blur strength during transitions
    private final float[] lightDirection = new float[3]; // Direction of sunlight (from Sun to Earth)
    private final float[] lightColor = new float[]{1.2f, 1.1f, 0.9f}; // Color of sunlight (warm white)

    private int frameCount = 0; // Number of frames rendered since last FPS calculation
    private long lastFpsTime = System.currentTimeMillis(); // Time of last FPS calculation
    private float currentFps = 60.0f; // Current frames per second

    private int orbitProgram; // Shader program for drawing orbit paths
    private int orbitPositionHandler; // Shader attribute location for orbit vertex positions
    private int orbitMatrixHandler; // Shader uniform location for orbit transformation matrix
    private FloatBuffer orbitBuffer; // Buffer containing orbit path vertex data

    // Loading callback interface
    public interface LoadingCallback {
        void onAssetsLoaded();
    }

    private LoadingCallback loadingCallback; // Callback to notify when assets are loaded

    // Initialize the renderer with context and set up identity matrices
    public Renderer(Context context) {
        this.context = context;
        // Initialize all rotation matrices
        Matrix.setIdentityM(sunRotationMatrix, 0);
        Matrix.setIdentityM(mercuryRotationMatrix, 0);
        Matrix.setIdentityM(venusRotationMatrix, 0);
        Matrix.setIdentityM(earthRotationMatrix, 0);
        Matrix.setIdentityM(marsRotationMatrix, 0);
        Matrix.setIdentityM(jupiterRotationMatrix, 0);
        Matrix.setIdentityM(saturnRotationMatrix, 0);
        Matrix.setIdentityM(uranusRotationMatrix, 0);
        Matrix.setIdentityM(neptuneRotationMatrix, 0);
        Matrix.setIdentityM(moonRotationMatrix, 0);
        // Initialize all model matrices
        Matrix.setIdentityM(sunModelMatrix, 0);
        Matrix.setIdentityM(mercuryModelMatrix, 0);
        Matrix.setIdentityM(venusModelMatrix, 0);
        Matrix.setIdentityM(earthModelMatrix, 0);
        Matrix.setIdentityM(marsModelMatrix, 0);
        Matrix.setIdentityM(jupiterModelMatrix, 0);
        Matrix.setIdentityM(saturnModelMatrix, 0);
        Matrix.setIdentityM(uranusModelMatrix, 0);
        Matrix.setIdentityM(neptuneModelMatrix, 0);
        Matrix.setIdentityM(moonModelMatrix, 0);
    }

    // Render a single frame: update rotations, handle transitions, and draw all objects
    @Override
    public void onDrawFrame(GL10 gl) {
        // Track FPS
        frameCount++;
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastFpsTime >= 1000) {
            currentFps = frameCount;
            frameCount = 0;
            lastFpsTime = currentTime;
        }

        GLES32.glClear(GLES32.GL_COLOR_BUFFER_BIT | GLES32.GL_DEPTH_BUFFER_BIT);

        // Update all rotations
        updateSunRotation();
        updateMercuryRotation();
        updateVenusRotation();
        updateEarthRotation();
        updateMarsRotation();
        updateJupiterRotation();
        updateSaturnRotation();
        updateUranusRotation();
        updateNeptuneRotation();
        updateMoonRotation();
        updateCloudRotation();

        // Update all orbits
        updateMercuryOrbit();
        updateVenusOrbit();
        updateEarthOrbit();
        updateMarsOrbit();
        updateJupiterOrbit();
        updateSaturnOrbit();
        updateUranusOrbit();
        updateNeptuneOrbit();
        updateMoonOrbit();

        if (isTransitioning) {
            updateTransition();
        }

        // Calculate camera target based on current focus, interpolating during transitions
        float[] targetPos;
        if (isTransitioning) {
            // Interpolate between start and end positions during transition
            float[] startPos = transitionStartPos;
            float[] endPos = getCameraTargetPosition();
            float t = transitionProgress;
            // Use smooth easing function (ease-in-out)
            float easedT = t * t * (3.0f - 2.0f * t);
            targetPos = new float[]{
                    startPos[0] + (endPos[0] - startPos[0]) * easedT,
                    startPos[1] + (endPos[1] - startPos[1]) * easedT,
                    startPos[2] + (endPos[2] - startPos[2]) * easedT
            };
        } else {
            targetPos = getCameraTargetPosition();
        }
        float cameraTargetX = targetPos[0];
        float cameraTargetY = targetPos[1];
        float cameraTargetZ = targetPos[2];

        // Calculate camera position with collision detection
        float cameraDistance = baseCameraDistance / zoomScale;
        // Prevent camera from getting too close (collision detection)
        float scaleFactor = getScaleFactorForTarget(cameraTarget);
        float radius = scaleFactor * zoomScale;
        float minDistance = radius + MIN_CAMERA_DISTANCE;
        if (cameraTarget == 4) { // Moon - reduce max zoom by 20%
            minDistance *= 1.25f;
        }
        if (cameraDistance < minDistance) {
            cameraDistance = minDistance;
            float maxZoomForDistance = baseCameraDistance / minDistance;
            if (cameraTarget == 4) { // Moon
                maxZoomForDistance *= 0.8f;
            }
            if (zoomScale > maxZoomForDistance) {
                zoomScale = maxZoomForDistance;
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

        // Draw background space with motion blur during transition, otherwise draw normally
        float currentBlurIntensity = motionBlurIntensity;
        boolean currentlyTransitioning = isTransitioning;
        if (currentlyTransitioning && currentBlurIntensity > 0.01f) {
            drawBackgroundWithBlur();
        } else {
            drawBackground();
        }

        // Draw orbit paths for selected object
        drawOrbitPaths();

        // Draw the Sun first (at center, always visible)
        drawSun();

        // Draw all planets in order from Sun
        drawMercury();
        drawVenus();
        drawEarth();
        drawMoon(); // Moon orbits Earth, so draw it right after Earth
        drawMars();
        drawJupiter();
        drawSaturn();
        drawUranus();
        drawNeptune();
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

        sunTexture = TextureHelper.loadTexture(context, R.drawable.sun_texture);
        mercuryTexture = TextureHelper.loadTexture(context, R.drawable.mercury_texture);
        venusTexture = TextureHelper.loadTexture(context, R.drawable.venus_surface_texture);
        earthTexture = TextureHelper.loadTexture(context, R.drawable.earth_texture);
        marsTexture = TextureHelper.loadTexture(context, R.drawable.mars_texture);
        jupiterTexture = TextureHelper.loadTexture(context, R.drawable.jupiter_texture);
        saturnTexture = TextureHelper.loadTexture(context, R.drawable.saturn_texture);
        uranusTexture = TextureHelper.loadTexture(context, R.drawable.uranus_texture);
        neptuneTexture = TextureHelper.loadTexture(context, R.drawable.neptune_texture);
        moonTexture = TextureHelper.loadTexture(context, R.drawable.moon_texture);
        backgroundTexture = TextureHelper.loadTexture(context, R.drawable.milky_way);
        cloudTexture = TextureHelper.loadHighQualityTexture(context, R.drawable.earth_clouds);
        earthSpecularTexture = TextureHelper.loadTexture(context, R.drawable.earth_specular_map);
        earthNormalTexture = TextureHelper.loadTexture(context, R.drawable.earth_normal_map);
        earthNightTexture = TextureHelper.loadTexture(context, R.drawable.earth_texture_night);

        initOrbitRendering();

        // Notify that all assets are loaded
        if (loadingCallback != null) {
            // Post to main thread to ensure UI updates happen on the correct thread
            android.os.Handler handler = new android.os.Handler(android.os.Looper.getMainLooper());
            handler.post(() -> {
                if (loadingCallback != null) {
                    loadingCallback.onAssetsLoaded();
                }
            });
        }
    }

    // Initialize orbit path rendering
    private void initOrbitRendering() {
        // Simple vertex shader for orbit lines
        String orbitVertexShaderCode =
                "uniform mat4 uMVPMatrix;" +
                        "attribute vec4 aPosition;" +
                        "void main() {" +
                        "  gl_Position = uMVPMatrix * aPosition;" +
                        "}";

        // Simple fragment shader for white orbit lines with 70% transparency
        String orbitFragmentShaderCode =
                "precision mediump float;" +
                        "void main() {" +
                        "  gl_FragColor = vec4(1.0, 1.0, 1.0, 0.7);" + // White color with 70% opacity
                        "}";

        int orbitVertexShader = loadShader(GLES32.GL_VERTEX_SHADER, orbitVertexShaderCode);
        int orbitFragmentShader = loadShader(GLES32.GL_FRAGMENT_SHADER, orbitFragmentShaderCode);

        orbitProgram = GLES32.glCreateProgram();
        GLES32.glAttachShader(orbitProgram, orbitVertexShader);
        GLES32.glAttachShader(orbitProgram, orbitFragmentShader);
        GLES32.glLinkProgram(orbitProgram);

        orbitPositionHandler = GLES32.glGetAttribLocation(orbitProgram, "aPosition");
        orbitMatrixHandler = GLES32.glGetUniformLocation(orbitProgram, "uMVPMatrix");

        // Generate orbit paths
        generateOrbitPaths();
    }

    // Load a shader
    private int loadShader(int type, String shaderCode) {
        int shader = GLES32.glCreateShader(type);
        GLES32.glShaderSource(shader, shaderCode);
        GLES32.glCompileShader(shader);
        return shader;
    }

    // Generate orbit path vertices (generic circle generator)
    private void generateOrbitPaths() {
        // We'll generate orbits dynamically in drawOrbitCircle
        // Just initialize a buffer for one orbit circle
        int segments = 180; // Number of segments for smooth orbit circle
        float[] orbitVertices = new float[segments * 3];

        // Generate a unit circle (radius 1.0) - we'll scale it dynamically
        for (int i = 0; i < segments; i++) {
            float angle = (float) (i * 2.0 * Math.PI / segments);
            orbitVertices[i * 3] = (float) Math.cos(angle);
            orbitVertices[i * 3 + 1] = 0.0f;
            orbitVertices[i * 3 + 2] = (float) Math.sin(angle);
        }

        ByteBuffer bb = ByteBuffer.allocateDirect(orbitVertices.length * 4);
        bb.order(ByteOrder.nativeOrder());
        orbitBuffer = bb.asFloatBuffer();
        orbitBuffer.put(orbitVertices);
        orbitBuffer.position(0);
    }

    // Draw an orbit circle at the specified radius
    private void drawOrbitCircle(float radius) {
        // Scale the unit circle to the desired radius
        float[] scaleMatrix = new float[16];
        Matrix.setIdentityM(scaleMatrix, 0);
        Matrix.scaleM(scaleMatrix, 0, radius, 1.0f, radius);

        float[] orbitMatrix = new float[16];
        Matrix.multiplyMM(orbitMatrix, 0, earthProjectionMatrix, 0, earthViewMatrix, 0);
        float[] tempMatrix = new float[16];
        Matrix.multiplyMM(tempMatrix, 0, orbitMatrix, 0, scaleMatrix, 0);
        GLES32.glUniformMatrix4fv(orbitMatrixHandler, 1, false, tempMatrix, 0);
        GLES32.glDrawArrays(GLES32.GL_LINE_LOOP, 0, 180);
    }

    // Get orbit radius for a given camera target
    private float getOrbitRadiusForTarget(int target) {
        return switch (target) {
            case 1 -> mercuryOrbitRadius;
            case 2 -> venusOrbitRadius;
            case 5 -> marsOrbitRadius;
            case 6 -> jupiterOrbitRadius;
            case 7 -> saturnOrbitRadius;
            case 8 -> uranusOrbitRadius;
            case 9 -> neptuneOrbitRadius;
            default -> earthOrbitRadius;
        };
    }

    // Draw orbit paths for the selected object
    private void drawOrbitPaths() {
        GLES32.glUseProgram(orbitProgram);
        GLES32.glLineWidth(2.0f);
        GLES32.glDisable(GLES32.GL_DEPTH_TEST); // Draw orbits on top

        GLES32.glVertexAttribPointer(orbitPositionHandler, 3, GLES32.GL_FLOAT, false, 0, orbitBuffer);
        GLES32.glEnableVertexAttribArray(orbitPositionHandler);

        // Draw orbit based on selected object
        // Sun (cameraTarget == 0) has no orbit - it's at the center
        if (cameraTarget == 4) { // Moon - show Moon's orbit around Earth
            float[] earthPos = getEarthPosition();

            // Create transformation: translate to Earth's position, then scale to Moon's orbit radius
            float[] moonOrbitModelMatrix = new float[16];
            Matrix.setIdentityM(moonOrbitModelMatrix, 0);
            Matrix.translateM(moonOrbitModelMatrix, 0, earthPos[0], earthPos[1], earthPos[2]);

            // Scale the orbit circle to Moon's orbit radius
            float[] scaleMatrix = new float[16];
            Matrix.setIdentityM(scaleMatrix, 0);
            Matrix.scaleM(scaleMatrix, 0, moonOffset, 1.0f, moonOffset);

            // Combine: scale first, then translate
            float[] combinedMatrix = new float[16];
            Matrix.multiplyMM(combinedMatrix, 0, moonOrbitModelMatrix, 0, scaleMatrix, 0);

            // Apply view and projection
            float[] orbitMatrix = new float[16];
            Matrix.multiplyMM(orbitMatrix, 0, earthProjectionMatrix, 0, earthViewMatrix, 0);
            float[] finalMatrix = new float[16];
            Matrix.multiplyMM(finalMatrix, 0, orbitMatrix, 0, combinedMatrix, 0);

            GLES32.glUniformMatrix4fv(orbitMatrixHandler, 1, false, finalMatrix, 0);
            GLES32.glDrawArrays(GLES32.GL_LINE_LOOP, 0, 180);
        } else if (cameraTarget >= 1 && cameraTarget <= 9 && cameraTarget != 4) { // Planets (excluding Moon) - show their orbit around Sun
            float[] orbitMatrix = new float[16];
            Matrix.multiplyMM(orbitMatrix, 0, earthProjectionMatrix, 0, earthViewMatrix, 0);
            GLES32.glUniformMatrix4fv(orbitMatrixHandler, 1, false, orbitMatrix, 0);
            // Draw orbit circle at appropriate radius
            drawOrbitCircle(getOrbitRadiusForTarget(cameraTarget));
        }

        GLES32.glDisableVertexAttribArray(orbitPositionHandler);
        GLES32.glEnable(GLES32.GL_DEPTH_TEST);
    }

    // Update viewport and projection matrix when surface size changes
    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        GLES32.glViewport(0, 0, width, height);
        float ratio = (float) width / height;
        // Use perspective projection with 45 degree FOV to avoid fisheye distortion
        float fov = 45.0f; // Field of view in degrees
        // Increase far plane to 1000.0f to accommodate large background (500.0f scale)
        Matrix.perspectiveM(earthProjectionMatrix, 0, fov, ratio, 0.1f, 1000.0f);
    }

    // Update rotation angles for all celestial bodies
    private void updateSunRotation() {
        sunAngle += sunRotationSpeed;
    }

    private void updateMercuryRotation() {
        mercuryAngle += mercuryRotationSpeed;
    }

    private void updateVenusRotation() {
        venusAngle -= venusRotationSpeed; // Retrograde rotation
    }

    private void updateEarthRotation() {
        earthAngle += earthRotationSpeed;
    }

    private void updateMarsRotation() {
        marsAngle += marsRotationSpeed;
    }

    private void updateJupiterRotation() {
        jupiterAngle += jupiterRotationSpeed;
    }

    private void updateSaturnRotation() {
        saturnAngle += saturnRotationSpeed;
    }

    private void updateUranusRotation() {
        uranusAngle += uranusRotationSpeed;
    }

    private void updateNeptuneRotation() {
        neptuneAngle += neptuneRotationSpeed;
    }

    private void updateMoonRotation() {
        moonAngle += moonRotationSpeed;
    }

    // Update cloud rotation angle for realistic cloud movement (faster than Earth due to wind)
    private void updateCloudRotation() {
        cloudAngle += cloudRotationSpeed;
    }

    // Update orbit angles for all planets
    private void updateMercuryOrbit() {
        mercuryOrbitAngle += mercuryOrbitSpeed;
        if (mercuryOrbitAngle > fullTurn) mercuryOrbitAngle -= fullTurn;
    }

    private void updateVenusOrbit() {
        venusOrbitAngle += venusOrbitSpeed;
        if (venusOrbitAngle > fullTurn) venusOrbitAngle -= fullTurn;
    }

    private void updateEarthOrbit() {
        earthOrbitAngle += earthOrbitSpeed;
        if (earthOrbitAngle > fullTurn) earthOrbitAngle -= fullTurn;
    }

    private void updateMarsOrbit() {
        marsOrbitAngle += marsOrbitSpeed;
        if (marsOrbitAngle > fullTurn) marsOrbitAngle -= fullTurn;
    }

    private void updateJupiterOrbit() {
        jupiterOrbitAngle += jupiterOrbitSpeed;
        if (jupiterOrbitAngle > fullTurn) jupiterOrbitAngle -= fullTurn;
    }

    private void updateSaturnOrbit() {
        saturnOrbitAngle += saturnOrbitSpeed;
        if (saturnOrbitAngle > fullTurn) saturnOrbitAngle -= fullTurn;
    }

    private void updateUranusOrbit() {
        uranusOrbitAngle += uranusOrbitSpeed;
        if (uranusOrbitAngle > fullTurn) uranusOrbitAngle -= fullTurn;
    }

    private void updateNeptuneOrbit() {
        neptuneOrbitAngle += neptuneOrbitSpeed;
        if (neptuneOrbitAngle > fullTurn) neptuneOrbitAngle -= fullTurn;
    }

    private void updateMoonOrbit() {
        moonOrbitAngle += moonOrbitSpeed;
        if (moonOrbitAngle > fullTurn) moonOrbitAngle -= fullTurn;
    }

    // Get positions for all planets in world space
    private float[] getSunPosition() {
        return new float[]{0.0f, 0.0f, 0.0f};
    }

    private float[] getMercuryPosition() {
        float x = (float) (mercuryOrbitRadius * Math.cos(mercuryOrbitAngle));
        float y = 0.0f;
        float z = (float) (mercuryOrbitRadius * Math.sin(mercuryOrbitAngle));
        return new float[]{x, y, z};
    }

    private float[] getVenusPosition() {
        float x = (float) (venusOrbitRadius * Math.cos(venusOrbitAngle));
        float y = 0.0f;
        float z = (float) (venusOrbitRadius * Math.sin(venusOrbitAngle));
        return new float[]{x, y, z};
    }

    private float[] getEarthPosition() {
        float x = (float) (earthOrbitRadius * Math.cos(earthOrbitAngle));
        float y = 0.0f;
        float z = (float) (earthOrbitRadius * Math.sin(earthOrbitAngle));
        return new float[]{x, y, z};
    }

    private float[] getMarsPosition() {
        float x = (float) (marsOrbitRadius * Math.cos(marsOrbitAngle));
        float y = 0.0f;
        float z = (float) (marsOrbitRadius * Math.sin(marsOrbitAngle));
        return new float[]{x, y, z};
    }

    private float[] getJupiterPosition() {
        float x = (float) (jupiterOrbitRadius * Math.cos(jupiterOrbitAngle));
        float y = 0.0f;
        float z = (float) (jupiterOrbitRadius * Math.sin(jupiterOrbitAngle));
        return new float[]{x, y, z};
    }

    private float[] getSaturnPosition() {
        float x = (float) (saturnOrbitRadius * Math.cos(saturnOrbitAngle));
        float y = 0.0f;
        float z = (float) (saturnOrbitRadius * Math.sin(saturnOrbitAngle));
        return new float[]{x, y, z};
    }

    private float[] getUranusPosition() {
        float x = (float) (uranusOrbitRadius * Math.cos(uranusOrbitAngle));
        float y = 0.0f;
        float z = (float) (uranusOrbitRadius * Math.sin(uranusOrbitAngle));
        return new float[]{x, y, z};
    }

    private float[] getNeptunePosition() {
        float x = (float) (neptuneOrbitRadius * Math.cos(neptuneOrbitAngle));
        float y = 0.0f;
        float z = (float) (neptuneOrbitRadius * Math.sin(neptuneOrbitAngle));
        return new float[]{x, y, z};
    }

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
            case 0 -> getSunPosition();
            case 1 -> getMercuryPosition();
            case 2 -> getVenusPosition();
            case 4 -> getMoonPosition();
            case 5 -> getMarsPosition();
            case 6 -> getJupiterPosition();
            case 7 -> getSaturnPosition();
            case 8 -> getUranusPosition();
            case 9 -> getNeptunePosition();
            default -> getEarthPosition();
        };
    }

    // Get scale factor for a given camera target
    private float getScaleFactorForTarget(int target) {
        return switch (target) {
            case 0 -> sunScaleFactor;
            case 1 -> mercuryScaleFactor;
            case 2 -> venusScaleFactor;
            case 4 -> moonScaleFactor;
            case 5 -> marsScaleFactor;
            case 6 -> jupiterScaleFactor;
            case 7 -> saturnScaleFactor;
            case 8 -> uranusScaleFactor;
            case 9 -> neptuneScaleFactor;
            default -> earthScaleFactor;
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
        // Compute the rotation for the Sun
        Matrix.setRotateM(sunRotationMatrix, 0, sunAngle, 0.0f, 1.0f, 0.0f);
        Matrix.setIdentityM(sunModelMatrix, 0);
        Matrix.scaleM(sunModelMatrix, 0, zoomScale * sunScaleFactor, zoomScale * sunScaleFactor, zoomScale * sunScaleFactor);
        Matrix.multiplyMM(mvpMatrix, 0, earthProjectionMatrix, 0, earthViewMatrix, 0);

        float[] tempMatrix = new float[16];

        Matrix.multiplyMM(tempMatrix, 0, sunModelMatrix, 0, sunRotationMatrix, 0);
        Matrix.multiplyMM(mvpMatrix, 0, mvpMatrix, 0, tempMatrix, 0);

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
        sphere.drawEarth(earthTexture, earthNightTexture, cloudTexture, earthSpecularTexture, earthNormalTexture,
                cloudRotationDegrees, cloudAlpha, mvpMatrix, earthModelMatrix,
                lightDirection, lightColor);
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

    // Draw Mercury sphere with current rotation and position
    private void drawMercury() {
        Matrix.setRotateM(mercuryRotationMatrix, 0, mercuryAngle, 0.0f, 1.0f, 0.0f);
        float[] mercuryPos = getMercuryPosition();
        Matrix.setIdentityM(mercuryModelMatrix, 0);
        Matrix.translateM(mercuryModelMatrix, 0, mercuryPos[0], mercuryPos[1], mercuryPos[2]);
        Matrix.scaleM(mercuryModelMatrix, 0, zoomScale * mercuryScaleFactor, zoomScale * mercuryScaleFactor, zoomScale * mercuryScaleFactor);
        Matrix.multiplyMM(mvpMatrix, 0, earthProjectionMatrix, 0, earthViewMatrix, 0);
        float[] tempMatrix = new float[16];
        Matrix.multiplyMM(tempMatrix, 0, mercuryModelMatrix, 0, mercuryRotationMatrix, 0);
        Matrix.multiplyMM(mvpMatrix, 0, mvpMatrix, 0, tempMatrix, 0);
        sphere.drawMoon(mercuryTexture, mvpMatrix);
    }

    // Draw Venus sphere with current rotation and position
    private void drawVenus() {
        Matrix.setRotateM(venusRotationMatrix, 0, venusAngle, 0.0f, 1.0f, 0.0f);
        float[] venusPos = getVenusPosition();
        Matrix.setIdentityM(venusModelMatrix, 0);
        Matrix.translateM(venusModelMatrix, 0, venusPos[0], venusPos[1], venusPos[2]);
        Matrix.scaleM(venusModelMatrix, 0, zoomScale * venusScaleFactor, zoomScale * venusScaleFactor, zoomScale * venusScaleFactor);
        Matrix.multiplyMM(mvpMatrix, 0, earthProjectionMatrix, 0, earthViewMatrix, 0);
        float[] tempMatrix = new float[16];
        Matrix.multiplyMM(tempMatrix, 0, venusModelMatrix, 0, venusRotationMatrix, 0);
        Matrix.multiplyMM(mvpMatrix, 0, mvpMatrix, 0, tempMatrix, 0);
        sphere.drawMoon(venusTexture, mvpMatrix);
    }

    // Draw Mars sphere with current rotation and position
    private void drawMars() {
        Matrix.setRotateM(marsRotationMatrix, 0, marsAngle, 0.0f, 1.0f, 0.0f);
        float[] marsPos = getMarsPosition();
        Matrix.setIdentityM(marsModelMatrix, 0);
        Matrix.translateM(marsModelMatrix, 0, marsPos[0], marsPos[1], marsPos[2]);
        Matrix.scaleM(marsModelMatrix, 0, zoomScale * marsScaleFactor, zoomScale * marsScaleFactor, zoomScale * marsScaleFactor);
        Matrix.multiplyMM(mvpMatrix, 0, earthProjectionMatrix, 0, earthViewMatrix, 0);
        float[] tempMatrix = new float[16];
        Matrix.multiplyMM(tempMatrix, 0, marsModelMatrix, 0, marsRotationMatrix, 0);
        Matrix.multiplyMM(mvpMatrix, 0, mvpMatrix, 0, tempMatrix, 0);
        sphere.drawMoon(marsTexture, mvpMatrix);
    }

    // Draw Jupiter sphere with current rotation and position
    private void drawJupiter() {
        Matrix.setRotateM(jupiterRotationMatrix, 0, jupiterAngle, 0.0f, 1.0f, 0.0f);
        float[] jupiterPos = getJupiterPosition();
        Matrix.setIdentityM(jupiterModelMatrix, 0);
        Matrix.translateM(jupiterModelMatrix, 0, jupiterPos[0], jupiterPos[1], jupiterPos[2]);
        Matrix.scaleM(jupiterModelMatrix, 0, zoomScale * jupiterScaleFactor, zoomScale * jupiterScaleFactor, zoomScale * jupiterScaleFactor);
        Matrix.multiplyMM(mvpMatrix, 0, earthProjectionMatrix, 0, earthViewMatrix, 0);
        float[] tempMatrix = new float[16];
        Matrix.multiplyMM(tempMatrix, 0, jupiterModelMatrix, 0, jupiterRotationMatrix, 0);
        Matrix.multiplyMM(mvpMatrix, 0, mvpMatrix, 0, tempMatrix, 0);
        sphere.drawMoon(jupiterTexture, mvpMatrix);
    }

    // Draw Saturn sphere with current rotation and position
    private void drawSaturn() {
        Matrix.setRotateM(saturnRotationMatrix, 0, saturnAngle, 0.0f, 1.0f, 0.0f);
        float[] saturnPos = getSaturnPosition();
        Matrix.setIdentityM(saturnModelMatrix, 0);
        Matrix.translateM(saturnModelMatrix, 0, saturnPos[0], saturnPos[1], saturnPos[2]);
        Matrix.scaleM(saturnModelMatrix, 0, zoomScale * saturnScaleFactor, zoomScale * saturnScaleFactor, zoomScale * saturnScaleFactor);
        Matrix.multiplyMM(mvpMatrix, 0, earthProjectionMatrix, 0, earthViewMatrix, 0);
        float[] tempMatrix = new float[16];
        Matrix.multiplyMM(tempMatrix, 0, saturnModelMatrix, 0, saturnRotationMatrix, 0);
        Matrix.multiplyMM(mvpMatrix, 0, mvpMatrix, 0, tempMatrix, 0);
        sphere.drawMoon(saturnTexture, mvpMatrix);
    }

    // Draw Uranus sphere with current rotation and position
    private void drawUranus() {
        Matrix.setRotateM(uranusRotationMatrix, 0, uranusAngle, 0.0f, 1.0f, 0.0f);
        float[] uranusPos = getUranusPosition();
        Matrix.setIdentityM(uranusModelMatrix, 0);
        Matrix.translateM(uranusModelMatrix, 0, uranusPos[0], uranusPos[1], uranusPos[2]);
        Matrix.scaleM(uranusModelMatrix, 0, zoomScale * uranusScaleFactor, zoomScale * uranusScaleFactor, zoomScale * uranusScaleFactor);
        Matrix.multiplyMM(mvpMatrix, 0, earthProjectionMatrix, 0, earthViewMatrix, 0);
        float[] tempMatrix = new float[16];
        Matrix.multiplyMM(tempMatrix, 0, uranusModelMatrix, 0, uranusRotationMatrix, 0);
        Matrix.multiplyMM(mvpMatrix, 0, mvpMatrix, 0, tempMatrix, 0);
        sphere.drawMoon(uranusTexture, mvpMatrix);
    }

    // Draw Neptune sphere with current rotation and position
    private void drawNeptune() {
        Matrix.setRotateM(neptuneRotationMatrix, 0, neptuneAngle, 0.0f, 1.0f, 0.0f);
        float[] neptunePos = getNeptunePosition();
        Matrix.setIdentityM(neptuneModelMatrix, 0);
        Matrix.translateM(neptuneModelMatrix, 0, neptunePos[0], neptunePos[1], neptunePos[2]);
        Matrix.scaleM(neptuneModelMatrix, 0, zoomScale * neptuneScaleFactor, zoomScale * neptuneScaleFactor, zoomScale * neptuneScaleFactor);
        Matrix.multiplyMM(mvpMatrix, 0, earthProjectionMatrix, 0, earthViewMatrix, 0);
        float[] tempMatrix = new float[16];
        Matrix.multiplyMM(tempMatrix, 0, neptuneModelMatrix, 0, neptuneRotationMatrix, 0);
        Matrix.multiplyMM(mvpMatrix, 0, mvpMatrix, 0, tempMatrix, 0);
        sphere.drawMoon(neptuneTexture, mvpMatrix);
    }

    // Scale the zoom level and clamp it to valid range with collision detection
    public void scaleSphere(float scaleFactor) {
        float newZoomScale = zoomScale * scaleFactor;

        // Apply collision detection based on current camera target
        float scaleFactorForTarget = getScaleFactorForTarget(cameraTarget);
        float minDistance = scaleFactorForTarget + MIN_CAMERA_DISTANCE;
        if (cameraTarget == 4) { // Moon - reduce max zoom by 20%
            minDistance *= 1.25f;
        }
        float maxZoomForDistance = baseCameraDistance / minDistance;
        if (cameraTarget == 4) { // Moon
            maxZoomForDistance *= 0.8f;
        }
        if (newZoomScale > maxZoomForDistance) {
            newZoomScale = maxZoomForDistance;
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

    // Cycle camera target through all celestial bodies: Sun → Mercury → Venus → Earth → Moon → Mars → Jupiter → Saturn → Uranus → Neptune → Sun
    public synchronized void cycleCameraTarget(boolean swipeLeft) {
        if (!isTransitioning) {
            // Store the current camera target position as the start of transition
            float[] currentPos = getCameraTargetPosition();
            transitionStartPos[0] = currentPos[0];
            transitionStartPos[1] = currentPos[1];
            transitionStartPos[2] = currentPos[2];

            isTransitioning = true;
            if (swipeLeft) {
                // Swipe left: forward through all planets
                cameraTarget = (cameraTarget + 1) % 10;
            } else {
                // Swipe right: backward through all planets
                cameraTarget = (cameraTarget + 9) % 10; // +9 is equivalent to -1 mod 10
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
        // Scale background to accommodate all planets (Neptune is at 280, so use 500 for safety margin)
        Matrix.scaleM(backgroundMatrix, 0, 500.0f, 500.0f, 500.0f);
        Matrix.multiplyMM(backgroundMatrix, 0, mvpMatrix, 0, backgroundMatrix, 0);
        sphere.drawBackground(backgroundTexture, backgroundMatrix);
    }

    // Draw the background space texture with motion blur effect during transitions
    private void drawBackgroundWithBlur() {
        Matrix.setIdentityM(backgroundMatrix, 0);
        // Scale background to accommodate all planets (Neptune is at 280, so use 500 for safety margin)
        Matrix.scaleM(backgroundMatrix, 0, 500.0f, 500.0f, 500.0f);
        Matrix.multiplyMM(backgroundMatrix, 0, mvpMatrix, 0, backgroundMatrix, 0);
        sphere.drawBackgroundWithBlur(backgroundTexture, backgroundMatrix, motionBlurIntensity);
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
