package com.app.nfusion.globe3d;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import android.opengl.GLES32;

public class Sphere {

    private FloatBuffer vertexBuffer, texBuffer;
    private ShortBuffer indexBuffer;

    private int programEarth;
    private int programMoon;
    private int programBackground;
    private int programMotionBlur;
    private int positionHandler;
    private int textureCoordinateHandler;
    private int earthTextureUniformHandler;
    private int moonTextureUniformHandler;
    private int backgroundTextureUniformHandler;
    private int motionBlurTextureUniformHandler;
    private int motionBlurIntensityHandler;
    private int matrixHandler;
    private int matrixHandlerBackground;
    private int matrixHandlerMotionBlur;
    private int numIndices;

    // Initialize the sphere geometry, shaders, and OpenGL programs
    public void init() {
        generateSphere();

        // Vertex shader code for rendering the sphere
        String vertexShaderCode =
                "uniform mat4 uMVPMatrix;" +                                                            // Uniform matrix for model-view-projection transformation
                        "attribute vec4 aPosition;" +                                                   // Attribute for vertex positions
                        "attribute vec2 aTexCoord;" +                                                   // Attribute for texture coordinates
                        "varying vec2 vTexCoord;" +                                                     // Varying variable to pass texture coordinates to fragment shader
                        "void main() {" +
                        "  gl_Position = uMVPMatrix * aPosition;" +                                     // Calculate final position based on MVP matrix
                        "  vTexCoord = aTexCoord;" +                                                    // Pass texture coordinates to the fragment shader
                        "}";

// Fragment shader code for rendering the sphere
        String sphereFragmentShaderCode =
                "precision mediump float;" +                                                            // Set floating-point precision
                        "uniform sampler2D uEarthTexture;" +                                            // Uniform sampler for earth texture
                        "varying vec2 vTexCoord;" +                                                     // Varying variable to receive texture coordinates from vertex shader
                        "void main() {" +
                        "  gl_FragColor = texture2D(uEarthTexture, vTexCoord);" +                       // Sample earth texture at given texture coordinates
                        "}";

// Fragment shader code for rendering the moon with clouds
        String moonFragmentShaderCode =
                "precision mediump float;" +                                                            // Set floating-point precision
                        "uniform sampler2D uCloudTexture;" +                                            // Uniform sampler for cloud texture
                        "varying vec2 vTexCoord;" +                                                     // Varying variable to receive texture coordinates from vertex shader
                        "void main() {" +
                        "  vec4 cloudColor = texture2D(uCloudTexture, vTexCoord);" +                    // Sample cloud texture at given texture coordinates
                        "  if (cloudColor.r < 0.1 && cloudColor.g < 0.1 && cloudColor.b < 0.1) {" +
                        "    discard;" +                                                                // Discard fragments with black color in the cloud texture
                        "  } else {" +
                        "    gl_FragColor = cloudColor;" +                                              // Set fragment color to cloud color if not discarded
                        "  }" +
                        "}";

        // Fragment shader code for rendering with motion blur effect
        String motionBlurFragmentShaderCode =
                "precision mediump float;" +                                                            // Set floating-point precision
                        "uniform sampler2D uEarthTexture;" +                                            // Uniform sampler for texture
                        "uniform float uBlurIntensity;" +                                              // Uniform for blur intensity control
                        "varying vec2 vTexCoord;" +                                                     // Varying variable to receive texture coordinates from vertex shader
                        "void main() {" +
                        "  vec4 color = texture2D(uEarthTexture, vTexCoord);" +                        // Sample original texture color at given texture coordinates
                        "  vec2 center = vec2(0.5, 0.5);" +                                            // Calculate texture center point
                        "  vec2 dir = normalize(vTexCoord - center);" +                                // Calculate direction vector from center to current coordinate
                        "  float dist = distance(vTexCoord, center);" +                                 // Calculate distance from center for radial blur effect
                        "  vec4 blurColor = vec4(0.0);" +                                               // Initialize blur color accumulator
                        "  float samples = 8.0;" +                                                      // Number of samples for blur effect
                        "  for (float i = 0.0; i < 8.0; i++) {" +                                      // Loop through samples to create blur streaks
                        "    float offset = (i / samples) * uBlurIntensity * dist;" +                  // Calculate offset distance based on sample index and blur intensity
                        "    vec2 sampleCoord = vTexCoord + dir * offset;" +                            // Calculate sample coordinate along radial direction
                        "    blurColor += texture2D(uEarthTexture, sampleCoord);" +                     // Accumulate color from sampled texture coordinate
                        "  }" +
                        "  blurColor /= samples;" +                                                     // Average the accumulated blur color
                        "  gl_FragColor = mix(color, blurColor, uBlurIntensity);" +                     // Blend original color with blurred color based on intensity
                        "}";

        int vertexShader = loadShader(GLES32.GL_VERTEX_SHADER, vertexShaderCode);
        int sphereFragmentShader = loadShader(GLES32.GL_FRAGMENT_SHADER, sphereFragmentShaderCode);
        int cloudFragmentShader = loadShader(GLES32.GL_FRAGMENT_SHADER, moonFragmentShaderCode);
        int motionBlurFragmentShader = loadShader(GLES32.GL_FRAGMENT_SHADER, motionBlurFragmentShaderCode);

        programEarth = GLES32.glCreateProgram();
        GLES32.glAttachShader(programEarth, vertexShader);
        GLES32.glAttachShader(programEarth, sphereFragmentShader);
        GLES32.glLinkProgram(programEarth);

        programMoon = GLES32.glCreateProgram();
        GLES32.glAttachShader(programMoon, vertexShader);
        GLES32.glAttachShader(programMoon, cloudFragmentShader);
        GLES32.glLinkProgram(programMoon);

        programBackground = GLES32.glCreateProgram();
        GLES32.glAttachShader(programBackground, vertexShader);
        GLES32.glAttachShader(programBackground, sphereFragmentShader);
        GLES32.glLinkProgram(programBackground);

        programMotionBlur = GLES32.glCreateProgram();
        GLES32.glAttachShader(programMotionBlur, vertexShader);
        GLES32.glAttachShader(programMotionBlur, motionBlurFragmentShader);
        GLES32.glLinkProgram(programMotionBlur);

        positionHandler = GLES32.glGetAttribLocation(programEarth, "aPosition");
        textureCoordinateHandler = GLES32.glGetAttribLocation(programEarth, "aTexCoord");
        earthTextureUniformHandler = GLES32.glGetUniformLocation(programEarth, "uEarthTexture");
        moonTextureUniformHandler = GLES32.glGetUniformLocation(programMoon, "uCloudTexture");
        backgroundTextureUniformHandler = GLES32.glGetUniformLocation(programBackground, "uEarthTexture");
        motionBlurTextureUniformHandler = GLES32.glGetUniformLocation(programMotionBlur, "uEarthTexture");
        motionBlurIntensityHandler = GLES32.glGetUniformLocation(programMotionBlur, "uBlurIntensity");
        matrixHandler = GLES32.glGetUniformLocation(programEarth, "uMVPMatrix");
        matrixHandlerBackground = GLES32.glGetUniformLocation(programBackground, "uMVPMatrix");
        matrixHandlerMotionBlur = GLES32.glGetUniformLocation(programMotionBlur, "uMVPMatrix");
    }

    // Load a shader of the specified type with the given code
    private int loadShader(int type, String shaderCode) {
        int shader = GLES32.glCreateShader(type);
        GLES32.glShaderSource(shader, shaderCode);
        GLES32.glCompileShader(shader);
        return shader;
    }

    // Generate sphere geometry with vertices, texture coordinates, and indices
    private void generateSphere() {
        int numVertices = (30 + 1) * (30 + 1);
        int numIndices = 6 * 30 * 30;

        float[] vertices = new float[3 * numVertices];
        float[] texCords = new float[2 * numVertices];
        short[] indices = new short[numIndices];

        int vertexIndex = 0;
        int textureCoordinateIndex = 0;
        for (int lat = 0; lat <= 30; lat++) {
            float theta = lat * (float) Math.PI / 30;
            float sinTheta = (float) Math.sin(theta);
            float cosTheta = (float) Math.cos(theta);

            for (int lon = 0; lon <= 30; lon++) {
                float phi = lon * 2 * (float) Math.PI / 30;
                float sinPhi = (float) Math.sin(phi);
                float cosPhi = (float) Math.cos(phi);

                float x = cosPhi * sinTheta;
                float z = sinPhi * sinTheta;
                float u = 1.0f - (lon / (float) 30);
                float v = lat / (float) 30;

                vertices[vertexIndex++] = x;
                vertices[vertexIndex++] = cosTheta;
                vertices[vertexIndex++] = z;

                texCords[textureCoordinateIndex++] = u;
                texCords[textureCoordinateIndex++] = v;
            }
        }

        int index = 0;
        for (int lat = 0; lat < 30; lat++) {
            for (int lon = 0; lon < 30; lon++) {
                int first = (lat * (30 + 1)) + lon;
                int second = first + 30 + 1;

                indices[index++] = (short) first;
                indices[index++] = (short) second;
                indices[index++] = (short) (first + 1);

                indices[index++] = (short) second;
                indices[index++] = (short) (second + 1);
                indices[index++] = (short) (first + 1);
            }
        }

        this.numIndices = numIndices;

        vertexBuffer = ByteBuffer.allocateDirect(vertices.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
        vertexBuffer.put(vertices).position(0);

        texBuffer = ByteBuffer.allocateDirect(texCords.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
        texBuffer.put(texCords).position(0);

        indexBuffer = ByteBuffer.allocateDirect(indices.length * 2).order(ByteOrder.nativeOrder()).asShortBuffer();
        indexBuffer.put(indices).position(0);
    }

    // Draw the Earth using the specified Earth texture and MVP matrix
    public void drawEarth(int earthTexture, float[] mvpMatrix) {
        GLES32.glUseProgram(programEarth);

        GLES32.glVertexAttribPointer(positionHandler, 3, GLES32.GL_FLOAT, false, 0, vertexBuffer);
        GLES32.glEnableVertexAttribArray(positionHandler);

        GLES32.glVertexAttribPointer(textureCoordinateHandler, 2, GLES32.GL_FLOAT, false, 0, texBuffer);
        GLES32.glEnableVertexAttribArray(textureCoordinateHandler);

        GLES32.glActiveTexture(GLES32.GL_TEXTURE0);
        GLES32.glBindTexture(GLES32.GL_TEXTURE_2D, earthTexture);
        GLES32.glUniform1i(earthTextureUniformHandler, 0);

        GLES32.glUniformMatrix4fv(matrixHandler, 1, false, mvpMatrix, 0);

        GLES32.glDrawElements(GLES32.GL_TRIANGLES, numIndices, GLES32.GL_UNSIGNED_SHORT, indexBuffer);

        GLES32.glDisableVertexAttribArray(positionHandler);
        GLES32.glDisableVertexAttribArray(textureCoordinateHandler);
    }

    // Draw the Moon using the specified Moon texture and MVP matrix
    public void drawMoon(int moonTexture, float[] mvpMatrix) {
        GLES32.glUseProgram(programMoon);

        GLES32.glVertexAttribPointer(positionHandler, 3, GLES32.GL_FLOAT, false, 0, vertexBuffer);
        GLES32.glEnableVertexAttribArray(positionHandler);

        GLES32.glVertexAttribPointer(textureCoordinateHandler, 2, GLES32.GL_FLOAT, false, 0, texBuffer);
        GLES32.glEnableVertexAttribArray(textureCoordinateHandler);

        GLES32.glActiveTexture(GLES32.GL_TEXTURE1);
        GLES32.glBindTexture(GLES32.GL_TEXTURE_2D, moonTexture);
        GLES32.glUniform1i(moonTextureUniformHandler, 1);

        GLES32.glUniformMatrix4fv(matrixHandler, 1, false, mvpMatrix, 0);

        GLES32.glDrawElements(GLES32.GL_TRIANGLES, numIndices, GLES32.GL_UNSIGNED_SHORT, indexBuffer);

        GLES32.glDisableVertexAttribArray(positionHandler);
        GLES32.glDisableVertexAttribArray(textureCoordinateHandler);
    }

    // Draw the background space texture as a large sphere with depth testing disabled
    public void drawBackground(int backgroundTexture, float[] mvpMatrix) {
        GLES32.glUseProgram(programBackground);
        GLES32.glDisable(GLES32.GL_DEPTH_TEST);

        GLES32.glVertexAttribPointer(positionHandler, 3, GLES32.GL_FLOAT, false, 0, vertexBuffer);
        GLES32.glEnableVertexAttribArray(positionHandler);

        GLES32.glVertexAttribPointer(textureCoordinateHandler, 2, GLES32.GL_FLOAT, false, 0, texBuffer);
        GLES32.glEnableVertexAttribArray(textureCoordinateHandler);

        GLES32.glActiveTexture(GLES32.GL_TEXTURE2);
        GLES32.glBindTexture(GLES32.GL_TEXTURE_2D, backgroundTexture);
        GLES32.glUniform1i(backgroundTextureUniformHandler, 2);

        GLES32.glUniformMatrix4fv(matrixHandlerBackground, 1, false, mvpMatrix, 0);

        GLES32.glDrawElements(GLES32.GL_TRIANGLES, numIndices, GLES32.GL_UNSIGNED_SHORT, indexBuffer);

        GLES32.glDisableVertexAttribArray(positionHandler);
        GLES32.glDisableVertexAttribArray(textureCoordinateHandler);
        GLES32.glEnable(GLES32.GL_DEPTH_TEST);
    }

    // Draw the Earth with motion blur effect using the motion blur shader program
    public void drawEarthWithBlur(int earthTexture, float[] mvpMatrix, float blurIntensity) {
        GLES32.glUseProgram(programMotionBlur);

        GLES32.glVertexAttribPointer(positionHandler, 3, GLES32.GL_FLOAT, false, 0, vertexBuffer);
        GLES32.glEnableVertexAttribArray(positionHandler);

        GLES32.glVertexAttribPointer(textureCoordinateHandler, 2, GLES32.GL_FLOAT, false, 0, texBuffer);
        GLES32.glEnableVertexAttribArray(textureCoordinateHandler);

        GLES32.glActiveTexture(GLES32.GL_TEXTURE0);
        GLES32.glBindTexture(GLES32.GL_TEXTURE_2D, earthTexture);
        GLES32.glUniform1i(motionBlurTextureUniformHandler, 0);

        GLES32.glUniform1f(motionBlurIntensityHandler, blurIntensity);

        GLES32.glUniformMatrix4fv(matrixHandlerMotionBlur, 1, false, mvpMatrix, 0);

        GLES32.glDrawElements(GLES32.GL_TRIANGLES, numIndices, GLES32.GL_UNSIGNED_SHORT, indexBuffer);

        GLES32.glDisableVertexAttribArray(positionHandler);
        GLES32.glDisableVertexAttribArray(textureCoordinateHandler);
    }

    // Draw the Moon with motion blur effect using the motion blur shader program
    public void drawMoonWithBlur(int moonTexture, float[] mvpMatrix, float blurIntensity) {
        GLES32.glUseProgram(programMotionBlur);

        GLES32.glVertexAttribPointer(positionHandler, 3, GLES32.GL_FLOAT, false, 0, vertexBuffer);
        GLES32.glEnableVertexAttribArray(positionHandler);

        GLES32.glVertexAttribPointer(textureCoordinateHandler, 2, GLES32.GL_FLOAT, false, 0, texBuffer);
        GLES32.glEnableVertexAttribArray(textureCoordinateHandler);

        GLES32.glActiveTexture(GLES32.GL_TEXTURE1);
        GLES32.glBindTexture(GLES32.GL_TEXTURE_2D, moonTexture);
        GLES32.glUniform1i(motionBlurTextureUniformHandler, 1);

        GLES32.glUniform1f(motionBlurIntensityHandler, blurIntensity);

        GLES32.glUniformMatrix4fv(matrixHandlerMotionBlur, 1, false, mvpMatrix, 0);

        GLES32.glDrawElements(GLES32.GL_TRIANGLES, numIndices, GLES32.GL_UNSIGNED_SHORT, indexBuffer);

        GLES32.glDisableVertexAttribArray(positionHandler);
        GLES32.glDisableVertexAttribArray(textureCoordinateHandler);
    }
}
