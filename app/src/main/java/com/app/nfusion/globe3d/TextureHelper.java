package com.app.nfusion.globe3d;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES32;
import android.opengl.GLUtils;

public class TextureHelper {

    // Load a texture from a resource ID and return the OpenGL texture handle
    public static int loadTexture(Context context, int resourceId) {
        final int[] textureHandle = new int[1];
        GLES32.glGenTextures(1, textureHandle, 0);

        if (textureHandle[0] != 0) {
            final BitmapFactory.Options options = new BitmapFactory.Options();
            options.inScaled = false;

            final Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), resourceId, options);

            GLES32.glBindTexture(GLES32.GL_TEXTURE_2D, textureHandle[0]);

            GLES32.glTexParameteri(GLES32.GL_TEXTURE_2D, GLES32.GL_TEXTURE_WRAP_S, GLES32.GL_REPEAT);
            GLES32.glTexParameteri(GLES32.GL_TEXTURE_2D, GLES32.GL_TEXTURE_WRAP_T, GLES32.GL_REPEAT);
            GLES32.glTexParameteri(GLES32.GL_TEXTURE_2D, GLES32.GL_TEXTURE_MIN_FILTER, GLES32.GL_LINEAR_MIPMAP_LINEAR);
            GLES32.glTexParameteri(GLES32.GL_TEXTURE_2D, GLES32.GL_TEXTURE_MAG_FILTER, GLES32.GL_LINEAR);

            GLUtils.texImage2D(GLES32.GL_TEXTURE_2D, 0, bitmap, 0);
            GLES32.glGenerateMipmap(GLES32.GL_TEXTURE_2D);

            bitmap.recycle();
        }

        if (textureHandle[0] == 0) {
            throw new RuntimeException("Error loading texture.");
        }

        return textureHandle[0];
    }

    // Load a high-resolution texture with improved filtering for better quality
    public static int loadHighQualityTexture(Context context, int resourceId) {
        final int[] textureHandle = new int[1];
        GLES32.glGenTextures(1, textureHandle, 0);

        if (textureHandle[0] != 0) {
            final BitmapFactory.Options options = new BitmapFactory.Options();
            options.inScaled = false;
            // Ensure we load at full resolution
            options.inSampleSize = 1;
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;

            final Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), resourceId, options);

            GLES32.glBindTexture(GLES32.GL_TEXTURE_2D, textureHandle[0]);

            // Set texture parameters BEFORE uploading texture data to ensure they're applied correctly
            GLES32.glTexParameteri(GLES32.GL_TEXTURE_2D, GLES32.GL_TEXTURE_WRAP_S, GLES32.GL_REPEAT);
            GLES32.glTexParameteri(GLES32.GL_TEXTURE_2D, GLES32.GL_TEXTURE_WRAP_T, GLES32.GL_REPEAT);

            // CRITICAL: Set base and max level to 0 FIRST to prevent any mipmap usage
            GLES32.glTexParameteri(GLES32.GL_TEXTURE_2D, GLES32.GL_TEXTURE_BASE_LEVEL, 0);
            GLES32.glTexParameteri(GLES32.GL_TEXTURE_2D, GLES32.GL_TEXTURE_MAX_LEVEL, 0);

            // Use GL_LINEAR for both min and mag to always use the highest resolution (level 0 only)
            // This prevents blockiness when zooming, as it always uses the full resolution texture
            GLES32.glTexParameteri(GLES32.GL_TEXTURE_2D, GLES32.GL_TEXTURE_MIN_FILTER, GLES32.GL_LINEAR);
            GLES32.glTexParameteri(GLES32.GL_TEXTURE_2D, GLES32.GL_TEXTURE_MAG_FILTER, GLES32.GL_LINEAR);

            // Set texture LOD bias to 0 to force using the base mipmap level
            try {
                GLES32.glTexParameterf(GLES32.GL_TEXTURE_2D, 0x8501, 0.0f); // GL_TEXTURE_LOD_BIAS
            } catch (Exception e) {
                // LOD bias not available, continue without it
            }

            // Enable anisotropic filtering for maximum quality at viewing angles
            try {
                String extensions = GLES32.glGetString(GLES32.GL_EXTENSIONS);
                if (extensions != null) {
                    boolean hasAnisotropic = extensions.contains("GL_EXT_texture_filter_anisotropic") ||
                            extensions.contains("texture_filter_anisotropic") ||
                            extensions.contains("GL_ARB_texture_filter_anisotropic");

                    if (hasAnisotropic) {
                        final float[] maxAnisotropy = new float[1];
                        int maxAnisotropyEnum = 0x84FE; // GL_MAX_TEXTURE_MAX_ANISOTROPY_EXT
                        GLES32.glGetFloatv(maxAnisotropyEnum, maxAnisotropy, 0);

                        if (maxAnisotropy[0] > 0) {
                            int textureAnisotropyEnum = 0x84FF; // GL_TEXTURE_MAX_ANISOTROPY_EXT
                            float anisotropyLevel = Math.min(16.0f, maxAnisotropy[0]);
                            GLES32.glTexParameterf(GLES32.GL_TEXTURE_2D, textureAnisotropyEnum, anisotropyLevel);
                        }
                    }
                }
            } catch (Exception e) {
                // Anisotropic filtering not available, continue without it
            }

            // Upload texture data - NO mipmap generation
            GLUtils.texImage2D(GLES32.GL_TEXTURE_2D, 0, bitmap, 0);
            // Explicitly do NOT call glGenerateMipmap - we want only the base level

            bitmap.recycle();
        }

        if (textureHandle[0] == 0) {
            throw new RuntimeException("Error loading texture.");
        }

        return textureHandle[0];
    }
}
