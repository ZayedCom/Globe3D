package com.app.nfusion.globe3d;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ParticleView extends View {
    private static final int PARTICLE_COUNT = 50;
    private static final float MIN_SIZE = 2.0f;
    private static final float MAX_SIZE = 6.0f;
    private static final float MIN_SPEED = 0.5f;
    private static final float MAX_SPEED = 2.0f;
    private static final int PARTICLE_COLOR = 0xFFFFFFFF; // White color

    private List<Particle> particles;
    private Paint paint;
    private Random random;
    private long lastUpdateTime;
    private boolean isAnimating = true;

    public ParticleView(Context context) {
        super(context);
        init();
    }

    public ParticleView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ParticleView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(PARTICLE_COLOR);
        random = new Random();
        particles = new ArrayList<>();
        lastUpdateTime = System.currentTimeMillis();
    }

    private Particle createParticle() {
        Particle particle = new Particle();
        int width = getWidth();
        int height = getHeight();
        // Use default size if view hasn't been measured yet
        if (width <= 0) width = 1080;
        if (height <= 0) height = 1920;
        particle.x = random.nextFloat() * width;
        particle.y = random.nextFloat() * height;
        particle.size = MIN_SIZE + random.nextFloat() * (MAX_SIZE - MIN_SIZE);
        particle.speedX = (random.nextFloat() - 0.5f) * (MIN_SPEED + random.nextFloat() * (MAX_SPEED - MIN_SPEED));
        particle.speedY = (random.nextFloat() - 0.5f) * (MIN_SPEED + random.nextFloat() * (MAX_SPEED - MIN_SPEED));
        particle.alpha = 0.3f + random.nextFloat() * 0.7f; // Vary opacity
        return particle;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        // Initialize or reinitialize particles when size changes
        if (w > 0 && h > 0) {
            if (particles.isEmpty()) {
                // First time initialization
                for (int i = 0; i < PARTICLE_COUNT; i++) {
                    particles.add(createParticle());
                }
            } else {
                // Reinitialize particles with new size
                particles.clear();
                for (int i = 0; i < PARTICLE_COUNT; i++) {
                    particles.add(createParticle());
                }
            }
            if (isAnimating) {
                invalidate();
            }
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (!isAnimating || particles.isEmpty()) {
            return;
        }

        long currentTime = System.currentTimeMillis();
        float deltaTime = (currentTime - lastUpdateTime) / 16.67f; // Normalize to ~60fps
        if (deltaTime > 2.0f) deltaTime = 1.0f; // Cap delta time
        lastUpdateTime = currentTime;

        int width = getWidth();
        int height = getHeight();

        // Update and draw particles
        for (Particle particle : particles) {
            // Update position
            particle.x += particle.speedX * deltaTime;
            particle.y += particle.speedY * deltaTime;

            // Wrap around edges
            if (particle.x < 0) particle.x = width;
            if (particle.x > width) particle.x = 0;
            if (particle.y < 0) particle.y = height;
            if (particle.y > height) particle.y = 0;

            // Draw particle
            paint.setAlpha((int) (255 * particle.alpha));
            canvas.drawCircle(particle.x, particle.y, particle.size, paint);
        }

        // Draw connections between nearby particles
        drawConnections(canvas);

        if (isAnimating) {
            invalidate();
        }
    }

    private void drawConnections(Canvas canvas) {
        float maxDistance = 150.0f;
        paint.setStrokeWidth(1.0f);

        for (int i = 0; i < particles.size(); i++) {
            Particle p1 = particles.get(i);
            for (int j = i + 1; j < particles.size(); j++) {
                Particle p2 = particles.get(j);
                float dx = p2.x - p1.x;
                float dy = p2.y - p1.y;
                float distance = (float) Math.sqrt(dx * dx + dy * dy);

                if (distance < maxDistance) {
                    // Calculate alpha based on distance (closer = more visible)
                    float alpha = (1.0f - distance / maxDistance) * 0.3f;
                    paint.setAlpha((int) (255 * alpha));
                    canvas.drawLine(p1.x, p1.y, p2.x, p2.y, paint);
                }
            }
        }
    }

    public void startAnimation() {
        isAnimating = true;
        lastUpdateTime = System.currentTimeMillis();
        // Initialize particles if not already done
        if (particles.isEmpty()) {
            int width = getWidth();
            int height = getHeight();
            if (width > 0 && height > 0) {
                for (int i = 0; i < PARTICLE_COUNT; i++) {
                    particles.add(createParticle());
                }
            }
        }
        invalidate();
    }

    public void stopAnimation() {
        isAnimating = false;
    }

    private static class Particle {
        float x;
        float y;
        float speedX;
        float speedY;
        float size;
        float alpha;
    }
}

