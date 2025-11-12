package com.app.nfusion.globe3d;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.Transformation;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.os.Debug;

import androidx.annotation.NonNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class MainActivity extends Activity {

    // Static constants
    private static final String PREFS_NAME = "data"; // SharedPreferences name
    private static final String KEY_LAUNCH_COUNT = "launchCount"; // Key for launch count
    private static final int MAX_HINT_SHOWS = 3; // Maximum times to show hint

    // Final instance variables
    private final Handler handler = new Handler(Looper.getMainLooper()); // Updates UI on main thread
    private final Map<ImageView, Drawable> originalImageDrawables = new HashMap<>(); // Store original ImageView drawables

    // Private instance variables - UI components
    private View loadingScreen; // Loading screen overlay
    private ParticleView particleView; // Particle animation view
    private SurfaceView glSurfaceView; // OpenGL surface for 3D rendering
    private Renderer renderer; // Manages the 3D scene and rendering
    private TextView tvPlanetName; // Displays current planet name at top center
    private ImageButton btnInfo; // Info button
    private ImageButton btnPerformance; // Performance button
    private FrameLayout infoDialog; // Overlay dialog showing planet information
    private LinearLayout infoContentContainer; // Container for info dialog content
    private LinearLayout infoContentLayout; // Layout holding dynamically generated info rows
    private TextView tvPerformanceInfo; // Shows FPS, triangle count, and memory usage
    private LinearLayout swipeHintContainer; // Container for swipe hint message
    private TextView tvSwipeHint; // Text view for swipe hint
    private ImageView icArrowLeftHint; // Left arrow icon
    private ImageView icArrowRightHint; // Right arrow icon

    // Private instance variables - state
    private boolean performanceInfoVisible = false; // Tracks if performance info is shown
    private boolean hintShownThisSession = false; // Whether hint has been shown in this session

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Increment launch count
        incrementLaunchCount();

        // Initialize loading screen
        loadingScreen = findViewById(R.id.loading_screen_container);
        particleView = findViewById(R.id.particle_view);
        if (particleView != null) {
            particleView.startAnimation();
        }

        // Initialize OpenGL surface view and renderer
        glSurfaceView = findViewById(R.id.gl_surface_view);

        // Set swipe listener to hide hint when user swipes
        glSurfaceView.setSwipeListener(this::hideSwipeHint);

        // Set loading callback after view is fully initialized
        // Post to ensure renderer is ready
        glSurfaceView.post(() -> {
            renderer = (Renderer) glSurfaceView.getRenderer();
            if (renderer != null) {
                renderer.setLoadingCallback(() -> {
                    // Hide loading screen with fade animation
                    if (loadingScreen != null) {
                        loadingScreen.animate()
                                .alpha(0.0f)
                                .setDuration(500)
                                .withEndAction(() -> {
                                    loadingScreen.setVisibility(View.GONE);
                                    if (particleView != null) {
                                        particleView.stopAnimation();
                                    }
                                    // Show UI elements after loading completes
                                    if (tvPlanetName != null) {
                                        tvPlanetName.setVisibility(View.VISIBLE);
                                    }
                                    if (btnInfo != null) {
                                        btnInfo.setVisibility(View.VISIBLE);
                                    }
                                    if (btnPerformance != null) {
                                        btnPerformance.setVisibility(View.VISIBLE);
                                    }
                                    // Check if we should show hint after loading completes
                                    checkAndShowHint();
                                })
                                .start();
                    }
                });
            }
        });

        // Initialize UI components
        tvPlanetName = findViewById(R.id.tv_planet_name);
        btnInfo = findViewById(R.id.btn_info);
        btnPerformance = findViewById(R.id.btn_performance);
        infoDialog = findViewById(R.id.info_dialog);
        infoContentContainer = findViewById(R.id.info_content_container);
        infoContentLayout = findViewById(R.id.info_content_layout);
        tvPerformanceInfo = findViewById(R.id.tv_performance_info);
        swipeHintContainer = findViewById(R.id.swipe_hint_container);
        tvSwipeHint = findViewById(R.id.tv_swipe_hint);
        icArrowLeftHint = findViewById(R.id.ic_arrow_left_hint);
        icArrowRightHint = findViewById(R.id.ic_arrow_right_hint);

        // Hide UI elements during loading
        tvPlanetName.setVisibility(View.GONE);
        btnInfo.setVisibility(View.GONE);
        btnPerformance.setVisibility(View.GONE);

        // Update planet name periodically
        handler.post(updatePlanetNameRunnable);

        // Info button click listener
        btnInfo.setOnClickListener(v -> {
            if (infoDialog.getVisibility() == View.VISIBLE) {
                infoDialog.setVisibility(View.GONE);
            } else {
                updatePlanetInfo();
                infoDialog.setVisibility(View.VISIBLE);
            }
        });

        // Performance button click listener
        btnPerformance.setOnClickListener(v -> {
            performanceInfoVisible = !performanceInfoVisible;
            tvPerformanceInfo.setVisibility(performanceInfoVisible ? View.VISIBLE : View.GONE);
            if (performanceInfoVisible) {
                handler.post(updatePerformanceInfoRunnable);
            }
        });

        // Close info dialog when clicking outside (on background, not on content)
        infoDialog.setOnClickListener(v -> {
            if (v.getId() == R.id.info_dialog) {
                infoDialog.setVisibility(View.GONE);
            }
        });
        // Prevent closing when clicking on content
        infoContentContainer.setOnClickListener(v -> {
            // Do nothing - prevent event from bubbling to parent
        });
    }

    // Runnable for updating planet name display periodically
    private final Runnable updatePlanetNameRunnable = new Runnable() {
        @Override
        public void run() {
            if (renderer != null) {
                tvPlanetName.setText(renderer.getCameraTargetName());
            }
            handler.postDelayed(this, 100); // Update every 100ms
        }
    };

    // Runnable for updating performance metrics display periodically
    private final Runnable updatePerformanceInfoRunnable = new Runnable() {
        @Override
        public void run() {
            if (performanceInfoVisible && renderer != null) {
                // Get FPS from renderer
                float fps = renderer.getFps();

                // Calculate triangle count (approximate: 120x120 sphere = 120*120*2 = 28,800 triangles per sphere)
                int trianglesPerSphere = 120 * 120 * 2;
                int numSpheres = 4; // Earth, Moon, Sun, Background
                int totalTriangles = trianglesPerSphere * numSpheres;

                // Get app memory usage (not device memory)
                Runtime runtime = Runtime.getRuntime();
                long usedMemory = (runtime.totalMemory() - runtime.freeMemory()) / (1024 * 1024); // MB
                long nativeMemory = Debug.getNativeHeapSize() / (1024 * 1024); // MB
                long totalAppMemory = usedMemory + nativeMemory;

                tvPerformanceInfo.setText(String.format(
                        getString(R.string.fps_format),
                        fps, totalTriangles, totalAppMemory
                ));
                handler.postDelayed(this, 100); // Update every 100ms
            }
        }
    };

    // Update planet information dialog with data for currently focused celestial body
    private void updatePlanetInfo() {
        if (renderer == null) return;

        // Get planet data based on current camera target
        PlanetData data;
        int target = renderer.getCameraTarget();
        boolean isMoon = (target == 4);
        data = switch (target) {
            case 0 -> PlanetData.getSun(this);
            case 1 -> PlanetData.getMercury(this);
            case 2 -> PlanetData.getVenus(this);
            case 4 -> PlanetData.getMoon(this);
            case 5 -> PlanetData.getMars(this);
            case 6 -> PlanetData.getJupiter(this);
            case 7 -> PlanetData.getSaturn(this);
            case 8 -> PlanetData.getUranus(this);
            case 9 -> PlanetData.getNeptune(this);
            default -> PlanetData.getEarth(this);
        };

        // Update title text
        TextView infoTitle = findViewById(R.id.info_title);
        if (infoTitle != null) {
            infoTitle.setText(isMoon ? getString(R.string.moon_information) : getString(R.string.planet_information));
        }

        // Clear previous content
        infoContentLayout.removeAllViews();

        // Set dialog to half screen height and center it
        android.view.Display display = getWindowManager().getDefaultDisplay();
        android.graphics.Point size = new android.graphics.Point();
        display.getSize(size);
        int screenHeight = size.y;
        int dialogMaxHeight = screenHeight / 2;

        FrameLayout.LayoutParams containerParams = (FrameLayout.LayoutParams) infoContentContainer.getLayoutParams();
        if (containerParams == null) {
            containerParams = new FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    dialogMaxHeight
            );
            containerParams.gravity = android.view.Gravity.CENTER;
        } else {
            containerParams.height = dialogMaxHeight;
        }
        infoContentContainer.setLayoutParams(containerParams);

        // Get background drawable for info boxes
        @SuppressLint("UseCompatLoadingForDrawables") Drawable boxBackground = getResources().getDrawable(R.drawable.bg_info_box, null);

        // Essential Physical Information section
        addSectionBox(getString(R.string.section_essential_physical), infoContentLayout, boxBackground);
        addInfoRow(getString(R.string.label_mass), data.mass, infoContentLayout, boxBackground);
        addInfoRow(getString(R.string.label_diameter), data.diameter, infoContentLayout, boxBackground);
        addInfoRow(getString(R.string.label_radius), data.radius, infoContentLayout, boxBackground);
        addInfoRow(getString(R.string.label_gravity), data.gravity, infoContentLayout, boxBackground);
        addInfoRow(getString(R.string.label_density), data.density, infoContentLayout, boxBackground);
        addInfoRow(getString(R.string.label_rotation_period), data.rotationPeriod, infoContentLayout, boxBackground);
        addInfoRow(getString(R.string.label_orbital_period), data.orbitalPeriod, infoContentLayout, boxBackground);
        addInfoRow(getString(R.string.label_axial_tilt), data.axialTilt, infoContentLayout, boxBackground);

        // Orbital and Positional Data section
        addSectionBox(getString(R.string.section_orbital_positional), infoContentLayout, boxBackground);
        addInfoRow(getString(R.string.label_orbital_speed), data.orbitalSpeed, infoContentLayout, boxBackground);
        addInfoRow(getString(R.string.label_orbital_eccentricity), data.orbitalEccentricity, infoContentLayout, boxBackground);

        // Only show "Number of Moons" if not viewing the Moon itself
        if (!isMoon) {
            addInfoRow(getString(R.string.label_number_of_moons), data.numberOfMoons, infoContentLayout, boxBackground);
        }
        addInfoRow(getString(R.string.label_rings), data.rings, infoContentLayout, boxBackground);

        // Atmospheric & Surface Data section
        addSectionBox(getString(R.string.section_atmospheric_surface), infoContentLayout, boxBackground);
        addInfoRow(getString(R.string.label_atmospheric_composition), data.atmosphericComposition, infoContentLayout, boxBackground);
        addInfoRow(getString(R.string.label_average_surface_temperature), data.averageSurfaceTemperature, infoContentLayout, boxBackground);
        addInfoRow(getString(R.string.label_surface_pressure), data.surfacePressure, infoContentLayout, boxBackground);
        addInfoRow(getString(R.string.label_color_albedo), data.colorAlbedo, infoContentLayout, boxBackground);
        addInfoRow(getString(R.string.label_notable_surface_features), data.notableSurfaceFeatures, infoContentLayout, boxBackground);

        // Exploration & Observation Data section
        addSectionBox(getString(R.string.section_exploration_observation), infoContentLayout, boxBackground);
        addInfoRow(getString(R.string.label_discovery_date), data.discoveryDate, infoContentLayout, boxBackground);
        addInfoRow(getString(R.string.label_discovered_by), data.discoveredBy, infoContentLayout, boxBackground);
        addInfoRow(getString(R.string.label_missions_visited), data.missionsVisited, infoContentLayout, boxBackground);
    }

    // Add a section header box with rounded corners to the info layout
    private void addSectionBox(String text, LinearLayout parent, Drawable background) {
        TextView sectionView = new TextView(this);
        sectionView.setText(text);
        sectionView.setTextColor(0xFFFFFFFF);
        sectionView.setTextSize(16);
        sectionView.setTypeface(null, android.graphics.Typeface.BOLD);
        sectionView.setPadding(16, 16, 16, 16);
        sectionView.setBackground(background);
        sectionView.setGravity(android.view.Gravity.CENTER);
        sectionView.setMinHeight((int) (48 * getResources().getDisplayMetrics().density));

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, 8, 0, 8);
        sectionView.setLayoutParams(params);

        parent.addView(sectionView);
    }

    // Add an info row with label and value boxes that match heights dynamically
    private void addInfoRow(String label, String value, LinearLayout parent, Drawable background) {
        LinearLayout rowLayout = new LinearLayout(this);
        rowLayout.setOrientation(LinearLayout.HORIZONTAL);
        rowLayout.setBaselineAligned(false); // Prevent baseline alignment for equal height boxes
        rowLayout.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));

        int minBoxHeight = (int) (48 * getResources().getDisplayMetrics().density);

        // Create label box (left side)
        TextView labelView = new TextView(this);
        labelView.setText(label);
        labelView.setTextColor(0xFFFFFFFF);
        labelView.setTextSize(14);
        labelView.setPadding(12, 12, 12, 12);
        labelView.setBackground(Objects.requireNonNull(background.getConstantState()).newDrawable().mutate());
        labelView.setGravity(android.view.Gravity.CENTER);
        labelView.setMinHeight(minBoxHeight);
        labelView.setSingleLine(false);
        labelView.setEllipsize(null);
        LinearLayout.LayoutParams labelParams = new LinearLayout.LayoutParams(
                0,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                1.0f
        );
        labelParams.setMargins(0, 4, 4, 4);

        // Create value box (right side)
        TextView valueView = new TextView(this);
        valueView.setText(value);
        valueView.setTextColor(0xFFFFFFFF);
        valueView.setTextSize(14);
        valueView.setPadding(12, 12, 12, 12);
        valueView.setBackground(background.getConstantState().newDrawable().mutate());
        valueView.setGravity(android.view.Gravity.CENTER);
        valueView.setMinHeight(minBoxHeight);
        valueView.setSingleLine(false);
        valueView.setEllipsize(null);
        LinearLayout.LayoutParams valueParams = new LinearLayout.LayoutParams(
                0,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                1.0f
        );
        valueParams.setMargins(4, 4, 0, 4);

        labelView.setLayoutParams(labelParams);
        valueView.setLayoutParams(valueParams);

        rowLayout.addView(labelView);
        rowLayout.addView(valueView);
        parent.addView(rowLayout);

        // Match heights with retry mechanism to ensure both boxes have equal height
        Runnable matchHeights = getRunnable(labelView, valueView, rowLayout);

        // Start matching after initial layout completes
        rowLayout.post(matchHeights);
    }

    // Create a runnable that matches label and value box heights after layout
    @NonNull
    private Runnable getRunnable(TextView labelView, TextView valueView, LinearLayout rowLayout) {
        final int[] retryCount = {0};
        final int MAX_RETRIES = 20;

        return new Runnable() {
            @Override
            public void run() {
                int labelHeight = labelView.getHeight();
                int valueHeight = valueView.getHeight();

                // Retry if heights not ready yet (layout not complete)
                if ((labelHeight == 0 || valueHeight == 0) && retryCount[0] < MAX_RETRIES) {
                    retryCount[0]++;
                    rowLayout.postDelayed(this, 10);
                    return;
                }

                // Match heights if they're different (set both to maximum height)
                if (labelHeight > 0 && valueHeight > 0 && labelHeight != valueHeight) {
                    int maxHeight = Math.max(labelHeight, valueHeight);

                    LinearLayout.LayoutParams newLabelParams = new LinearLayout.LayoutParams(0, maxHeight, 1.0f);
                    newLabelParams.setMargins(0, 4, 4, 4);

                    LinearLayout.LayoutParams newValueParams = new LinearLayout.LayoutParams(0, maxHeight, 1.0f);
                    newValueParams.setMargins(4, 4, 0, 4);

                    labelView.setLayoutParams(newLabelParams);
                    valueView.setLayoutParams(newValueParams);
                }
            }
        };
    }


    // Resume OpenGL surface view rendering when activity resumes
    @Override
    protected void onResume() {
        super.onResume();
        if (glSurfaceView != null) {
            glSurfaceView.onResume();
        }
    }

    // Pause OpenGL surface view rendering when activity pauses
    @Override
    protected void onPause() {
        super.onPause();
        if (glSurfaceView != null) {
            glSurfaceView.onPause();
        }
    }

    // Check if hint should be shown and display it
    private void checkAndShowHint() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        int launchCount = prefs.getInt(KEY_LAUNCH_COUNT, 0);

        // Only show if we haven't shown it 3 times yet
        if (launchCount < MAX_HINT_SHOWS) {
            // Show hint after loading is complete and a short delay
            handler.postDelayed(() -> {
                if (swipeHintContainer != null && !hintShownThisSession && loadingScreen != null && loadingScreen.getVisibility() == View.GONE) {
                    swipeHintContainer.setVisibility(View.VISIBLE);
                    swipeHintContainer.setAlpha(0.0f);
                    swipeHintContainer.animate()
                            .alpha(1.0f)
                            .setDuration(500)
                            .start();
                    startShimmerAnimation();
                    hintShownThisSession = true;
                }
            }, 2000); // 2 second delay after loading
        }
    }

    // Start shimmer animation on the hint text and icons (iOS 4 style)
    private void startShimmerAnimation() {
        if (tvSwipeHint == null || icArrowLeftHint == null || icArrowRightHint == null || swipeHintContainer == null)
            return;

        // Wait for views to be measured before starting animation
        swipeHintContainer.post(() -> {
            // Create iOS 4 style shimmer effect that sweeps across from left to right (text only)
            applyShimmerToTextView(tvSwipeHint);
            // Apply fade in/out animation to arrows (30% to 100% opacity)
            applyFadeAnimationToImageView(icArrowLeftHint);
            applyFadeAnimationToImageView(icArrowRightHint);
        });
    }

    // Apply iOS 4 style shimmer to TextView using shader
    private void applyShimmerToTextView(TextView textView) {
        if (textView == null) return;

        int viewWidth = textView.getWidth();
        if (viewWidth == 0) {
            textView.measure(
                    View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                    View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
            );
            viewWidth = textView.getMeasuredWidth();
        }

        // Create custom animation that updates the shader
        ShimmerAnimation shimmerAnim = new ShimmerAnimation(textView, viewWidth, true);
        shimmerAnim.setDuration(2000);
        shimmerAnim.setRepeatCount(Animation.INFINITE);
        shimmerAnim.setRepeatMode(Animation.RESTART);
        shimmerAnim.setInterpolator(new LinearInterpolator());

        textView.startAnimation(shimmerAnim);
    }

    // Apply fade in/out animation to ImageView (30% to 100% opacity)
    private void applyFadeAnimationToImageView(ImageView imageView) {
        if (imageView == null) return;

        // Create alpha animation that fades from 30% (0.3) to 100% (1.0)
        android.view.animation.AlphaAnimation fadeAnim = new android.view.animation.AlphaAnimation(0.3f, 1.0f);
        fadeAnim.setDuration(1500);
        fadeAnim.setRepeatCount(Animation.INFINITE);
        fadeAnim.setRepeatMode(Animation.REVERSE);
        fadeAnim.setInterpolator(new LinearInterpolator());

        imageView.startAnimation(fadeAnim);
    }

    // Custom animation class for iOS 4 style shimmer effect
    private class ShimmerAnimation extends Animation {
        private final View targetView;
        private final int viewWidth;
        private final boolean isTextView;
        private final LinearGradient gradient;
        private final Matrix matrix;
        private ShimmerMaskDrawable shimmerDrawable;

        public ShimmerAnimation(View view, int width, boolean isText) {
            this.targetView = view;
            this.viewWidth = width;
            this.isTextView = isText;
            this.matrix = new Matrix();

            // Initialize gradient with initial position
            // For iOS 4 style: white text with bright white shimmer highlight
            // Use white with varying alpha - transparent areas show base white, opaque areas create highlight
            float gradientStart = -viewWidth;
            float gradientEnd = gradientStart + viewWidth * 0.6f;
            // Varying alpha creates the highlight - where alpha is high, it's brighter
            int[] colors = {
                    0x60FFFFFF,  // Semi-transparent white (base - text visible)
                    0x80FFFFFF,  // More opaque white
                    0xFFFFFFFF,  // Fully opaque white (bright highlight at center)
                    0x80FFFFFF,  // More opaque white
                    0x60FFFFFF   // Semi-transparent white (base - text visible)
            };
            float[] positions = {0.0f, 0.35f, 0.5f, 0.65f, 1.0f};

            gradient = new LinearGradient(
                    gradientStart, 0, gradientEnd, 0,
                    colors, positions,
                    Shader.TileMode.CLAMP
            );

            // For ImageView, create shimmer drawable wrapper
            if (!isText && targetView instanceof ImageView iv) {
                Drawable original = iv.getDrawable();
                if (original != null) {
                    // Store original drawable
                    originalImageDrawables.put(iv, original);
                    shimmerDrawable = new ShimmerMaskDrawable(original, gradient);
                    iv.setImageDrawable(shimmerDrawable);
                }
            }
        }

        @Override
        protected void applyTransformation(float interpolatedTime, Transformation t) {
            super.applyTransformation(interpolatedTime, t);

            if (targetView == null) return;

            // Calculate gradient position (moves from -viewWidth to +viewWidth)
            float gradientStart = -viewWidth + (interpolatedTime * viewWidth * 3);

            // Update gradient matrix for movement
            matrix.reset();
            matrix.setTranslate(gradientStart, 0);
            gradient.setLocalMatrix(matrix);

            if (isTextView && targetView instanceof TextView tv) {
                // Apply shader to text paint for iOS 4 style shimmer
                Paint paint = tv.getPaint();
                // Ensure text color is white (base color)
                tv.setTextColor(0xFFFFFFFF);
                // Apply shader with varying alpha - creates highlight effect
                // The shader replaces text color, so use white with varying alpha
                paint.setShader(gradient);
                paint.setXfermode(null);
                targetView.invalidate();
            } else if (targetView instanceof ImageView && shimmerDrawable != null) {
                // Update the shimmer drawable's gradient
                shimmerDrawable.updateGradient(gradient);
                targetView.invalidate();
            }
        }
    }

    // Custom drawable that applies gradient shader as a mask (iOS 4 style)
    private static class ShimmerMaskDrawable extends Drawable {
        private final Drawable originalDrawable;
        private LinearGradient gradient;
        private final Paint paint;
        private final Paint maskPaint;

        public ShimmerMaskDrawable(Drawable original, LinearGradient grad) {
            this.originalDrawable = original;
            this.gradient = grad;
            this.paint = new Paint(Paint.ANTI_ALIAS_FLAG);
            this.maskPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            this.maskPaint.setShader(gradient);

            // Copy bounds from original
            if (original != null) {
                setBounds(original.getBounds());
            }
        }

        public void updateGradient(LinearGradient grad) {
            this.gradient = grad;
            this.maskPaint.setShader(gradient);
        }

        @Override
        public void draw(@NonNull Canvas canvas) {
            if (originalDrawable == null) return;

            // Save layer for proper blending with alpha
            int saveCount = canvas.saveLayer(
                    getBounds().left, getBounds().top,
                    getBounds().right, getBounds().bottom,
                    null
            );

            // Draw original icon first
            originalDrawable.draw(canvas);

            // Draw the gradient shimmer on top using SCREEN mode
            // SCREEN mode adds brightness - white on white makes it brighter (iOS 4 style)
            maskPaint.setShader(gradient);
            maskPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SCREEN));
            canvas.drawRect(getBounds(), maskPaint);

            // Now mask it to only show where the icon is (using DST_IN)
            // This ensures shimmer only appears ON the icon pixels, not behind it
            Paint iconMaskPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            iconMaskPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));
            originalDrawable.draw(canvas);

            canvas.restoreToCount(saveCount);
        }

        @Override
        public void setAlpha(int alpha) {
            if (originalDrawable != null) {
                originalDrawable.setAlpha(alpha);
            }
            paint.setAlpha(alpha);
        }

        @Override
        public void setColorFilter(android.graphics.ColorFilter colorFilter) {
            if (originalDrawable != null) {
                originalDrawable.setColorFilter(colorFilter);
            }
        }

        @Override
        public int getOpacity() {
            return originalDrawable != null ? originalDrawable.getOpacity() : android.graphics.PixelFormat.TRANSLUCENT;
        }

        @Override
        public void setBounds(int left, int top, int right, int bottom) {
            super.setBounds(left, top, right, bottom);
            if (originalDrawable != null) {
                originalDrawable.setBounds(left, top, right, bottom);
            }
        }
    }

    // Hide the hint message (called when user swipes)
    public void hideSwipeHint() {
        if (swipeHintContainer != null && swipeHintContainer.getVisibility() == View.VISIBLE) {
            // Remove shimmer overlay views
            removeShimmerFromView(tvSwipeHint);
            removeShimmerFromView(icArrowLeftHint);
            removeShimmerFromView(icArrowRightHint);

            swipeHintContainer.animate()
                    .alpha(0.0f)
                    .setDuration(300)
                    .withEndAction(() -> swipeHintContainer.setVisibility(View.GONE))
                    .start();
        }
    }

    // Remove shimmer overlay from a view
    private void removeShimmerFromView(View targetView) {
        if (targetView == null) return;

        // Clear animation
        targetView.clearAnimation();

        // Remove shader from TextView
        if (targetView instanceof TextView tv) {
            tv.getPaint().setShader(null);
            tv.invalidate();
        }

        // Clear animation for ImageView (fade animation)
        if (targetView instanceof ImageView iv) {
            // Restore original drawable if it was replaced
            Drawable original = originalImageDrawables.remove(iv);
            if (original != null) {
                iv.setImageDrawable(original);
            }
            // Reset alpha to full opacity
            iv.setAlpha(1.0f);
            iv.clearColorFilter();
            iv.invalidate();
        }
    }

    // Increment launch count when app starts
    private void incrementLaunchCount() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        int launchCount = prefs.getInt(KEY_LAUNCH_COUNT, 0);
        prefs.edit().putInt(KEY_LAUNCH_COUNT, launchCount + 1).apply();
    }

    // Clean up handler callbacks when activity is destroyed
    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(updatePlanetNameRunnable);
        handler.removeCallbacks(updatePerformanceInfoRunnable);
    }
}
