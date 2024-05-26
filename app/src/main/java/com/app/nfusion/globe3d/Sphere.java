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
    private int positionHandler;
    private int textureCoordinateHandler;
    private int earthTextureUniformHandler;
    private int moonTextureUniformHandler;
    private int matrixHandler;
    private int numIndices;

    // Initialize the sphere
    public void init() {
        generateSphere();

        String vertexShaderCode =
                "uniform mat4 uMVPMatrix;" +
                        "attribute vec4 aPosition;" +
                        "attribute vec2 aTexCoord;" +
                        "varying vec2 vTexCoord;" +
                        "void main() {" +
                        "  gl_Position = uMVPMatrix * aPosition;" +
                        "  vTexCoord = aTexCoord;" +
                        "}";

        String sphereFragmentShaderCode =
                "precision mediump float;" +
                        "uniform sampler2D uEarthTexture;" +
                        "varying vec2 vTexCoord;" +
                        "void main() {" +
                        "  gl_FragColor = texture2D(uEarthTexture, vTexCoord);" +
                        "}";

        String moonFragmentShaderCode =
                "precision mediump float;" +
                        "uniform sampler2D uCloudTexture;" +
                        "varying vec2 vTexCoord;" +
                        "void main() {" +
                        "  vec4 cloudColor = texture2D(uCloudTexture, vTexCoord);" +
                        "  if (cloudColor.r < 0.1 && cloudColor.g < 0.1 && cloudColor.b < 0.1) {" +
                        "    discard;" + // Discard fragments with black color in the cloud texture
                        "  } else {" +
                        "    gl_FragColor = cloudColor;" +
                        "  }" +
                        "}";

        int vertexShader = loadShader(GLES32.GL_VERTEX_SHADER, vertexShaderCode);
        int sphereFragmentShader = loadShader(GLES32.GL_FRAGMENT_SHADER, sphereFragmentShaderCode);
        int cloudFragmentShader = loadShader(GLES32.GL_FRAGMENT_SHADER, moonFragmentShaderCode);

        programEarth = GLES32.glCreateProgram();
        GLES32.glAttachShader(programEarth, vertexShader);
        GLES32.glAttachShader(programEarth, sphereFragmentShader);
        GLES32.glLinkProgram(programEarth);

        programMoon = GLES32.glCreateProgram();
        GLES32.glAttachShader(programMoon, vertexShader);
        GLES32.glAttachShader(programMoon, cloudFragmentShader);
        GLES32.glLinkProgram(programMoon);

        positionHandler = GLES32.glGetAttribLocation(programEarth, "aPosition");
        textureCoordinateHandler = GLES32.glGetAttribLocation(programEarth, "aTexCoord");
        earthTextureUniformHandler = GLES32.glGetUniformLocation(programEarth, "uEarthTexture");
        moonTextureUniformHandler = GLES32.glGetUniformLocation(programMoon, "uCloudTexture");
        matrixHandler = GLES32.glGetUniformLocation(programEarth, "uMVPMatrix");
    }

    // Load a shader of the specified type with the given code
    private int loadShader(int type, String shaderCode) {
        int shader = GLES32.glCreateShader(type);
        GLES32.glShaderSource(shader, shaderCode);
        GLES32.glCompileShader(shader);
        return shader;
    }

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
}
