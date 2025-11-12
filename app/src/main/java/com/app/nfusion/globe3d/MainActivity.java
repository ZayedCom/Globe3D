package com.app.nfusion.globe3d;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.view.ViewGroup;
import android.graphics.drawable.Drawable;
import android.os.Debug;

import androidx.annotation.NonNull;

import java.util.Objects;

public class MainActivity extends Activity {

    private View loadingScreen; // Loading screen overlay
    private ParticleView particleView; // Particle animation view
    private SurfaceView glSurfaceView; // OpenGL surface for 3D rendering
    private Renderer renderer; // Manages the 3D scene and rendering
    private TextView tvPlanetName; // Displays current planet name at top center
    private FrameLayout infoDialog; // Overlay dialog showing planet information
    private LinearLayout infoContentContainer; // Container for info dialog content
    private LinearLayout infoContentLayout; // Layout holding dynamically generated info rows
    private TextView tvPerformanceInfo; // Shows FPS, triangle count, and memory usage
    private final Handler handler = new Handler(Looper.getMainLooper()); // Updates UI on main thread
    private boolean performanceInfoVisible = false; // Tracks if performance info is shown

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize loading screen
        loadingScreen = findViewById(R.id.loading_screen_container);
        particleView = findViewById(R.id.particle_view);
        if (particleView != null) {
            particleView.startAnimation();
        }

        // Initialize OpenGL surface view and renderer
        glSurfaceView = findViewById(R.id.gl_surface_view);

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
                                })
                                .start();
                    }
                });
            }
        });

        // Initialize UI components
        tvPlanetName = findViewById(R.id.tv_planet_name);
        ImageButton btnInfo = findViewById(R.id.btn_info);
        ImageButton btnPerformance = findViewById(R.id.btn_performance);
        infoDialog = findViewById(R.id.info_dialog);
        infoContentContainer = findViewById(R.id.info_content_container);
        infoContentLayout = findViewById(R.id.info_content_layout);
        tvPerformanceInfo = findViewById(R.id.tv_performance_info);

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
        @SuppressLint("UseCompatLoadingForDrawables") Drawable boxBackground = getResources().getDrawable(R.drawable.info_box_background, null);

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
        addInfoRow(getString(R.string.label_number_of_moons), data.numberOfMoons, infoContentLayout, boxBackground);
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

    // Clean up handler callbacks when activity is destroyed
    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(updatePlanetNameRunnable);
        handler.removeCallbacks(updatePerformanceInfoRunnable);
    }
}
