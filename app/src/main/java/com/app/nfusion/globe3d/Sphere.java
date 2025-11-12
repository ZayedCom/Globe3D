package com.app.nfusion.globe3d;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import android.opengl.GLES32;

public class Sphere {

    private FloatBuffer vertexBuffer; // Buffer containing sphere vertex positions
    private FloatBuffer texBuffer; // Buffer containing texture coordinates for vertices
    private ShortBuffer indexBuffer; // Buffer containing triangle indices for rendering

    private static final int LATITUDE_BANDS = 120; // Number of horizontal divisions for sphere geometry
    private static final int LONGITUDE_BANDS = 120; // Number of vertical divisions for sphere geometry

    private int programEarth; // OpenGL shader program ID for rendering Earth
    private int programMoon; // OpenGL shader program ID for rendering Moon
    private int programBackground; // OpenGL shader program ID for rendering background
    private int programMotionBlur; // OpenGL shader program ID for motion blur effect
    private int programSun; // OpenGL shader program ID for rendering Sun
    private int positionHandler; // Shader attribute location for vertex positions
    private int textureCoordinateHandler; // Shader attribute location for texture coordinates
    private int normalHandler; // Shader attribute location for vertex normals
    private int earthTextureUniformHandler; // Shader uniform location for Earth texture
    private int earthNightTextureUniformHandler; // Shader uniform location for Earth night texture
    private int moonTextureUniformHandler; // Shader uniform location for Moon texture
    private int backgroundTextureUniformHandler; // Shader uniform location for background texture
    private int motionBlurTextureUniformHandler; // Shader uniform location for motion blur texture
    private int motionBlurIntensityHandler; // Shader uniform location for motion blur intensity
    private int earthCloudTextureUniformHandler; // Shader uniform location for Earth cloud texture
    private int earthCloudRotationUniformHandler; // Shader uniform location for cloud rotation angle
    private int earthCloudAlphaUniformHandler; // Shader uniform location for cloud transparency
    private int earthSpecularTextureUniformHandler; // Shader uniform location for Earth specular map
    private int earthNormalTextureUniformHandler; // Shader uniform location for Earth normal map
    private int sunTextureUniformHandler; // Shader uniform location for Sun texture
    private int sunGlowIntensityUniformHandler; // Shader uniform location for Sun glow intensity
    private int sunLightDirectionUniformHandler; // Shader uniform location for light direction vector
    private int sunLightColorUniformHandler; // Shader uniform location for light color
    private int motionBlurCloudTextureUniformHandler; // Shader uniform location for motion blur cloud texture
    private int motionBlurCloudRotationUniformHandler; // Shader uniform location for motion blur cloud rotation
    private int motionBlurCloudAlphaUniformHandler; // Shader uniform location for motion blur cloud alpha
    private int matrixHandler; // Shader uniform location for MVP transformation matrix
    private int matrixHandlerBackground; // Shader uniform location for background MVP matrix
    private int matrixHandlerMotionBlur; // Shader uniform location for motion blur MVP matrix
    private int matrixHandlerSun; // Shader uniform location for Sun MVP matrix
    private FloatBuffer normalBuffer; // Buffer containing vertex normal vectors
    private int numIndices; // Total number of triangle indices for rendering

    // Initialize the sphere geometry, shaders, and OpenGL programs
    public void init() {
        generateSphere();

        // Vertex shader code for rendering the sphere (basic version)
        String vertexShaderCode =
                "uniform mat4 uMVPMatrix;" +                                                            // Uniform matrix for model-view-projection transformation
                        "attribute vec4 aPosition;" +                                                   // Attribute for vertex positions
                        "attribute vec2 aTexCoord;" +                                                   // Attribute for texture coordinates
                        "varying vec2 vTexCoord;" +                                                     // Varying variable to pass texture coordinates to fragment shader
                        "void main() {" +
                        "  gl_Position = uMVPMatrix * aPosition;" +                                     // Calculate final position based on MVP matrix
                        "  vTexCoord = aTexCoord;" +                                                    // Pass texture coordinates to the fragment shader
                        "}";

        // Vertex shader code for Earth with lighting support
        String earthVertexShaderCode =
                "uniform mat4 uMVPMatrix;" +                                                            // Uniform matrix for model-view-projection transformation
                        "uniform mat4 uModelMatrix;" +                                                   // Model matrix for normal transformation
                        "attribute vec4 aPosition;" +                                                   // Attribute for vertex positions
                        "attribute vec2 aTexCoord;" +                                                   // Attribute for texture coordinates
                        "attribute vec3 aNormal;" +                                                     // Attribute for vertex normals
                        "varying vec2 vTexCoord;" +                                                     // Varying variable to pass texture coordinates to fragment shader
                        "varying vec3 vNormal;" +                                                       // Varying variable to pass normals to fragment shader
                        "varying vec3 vPosition;" +                                                     // Varying variable to pass position to fragment shader
                        "void main() {" +
                        "  gl_Position = uMVPMatrix * aPosition;" +                                     // Calculate final position based on MVP matrix
                        "  vTexCoord = aTexCoord;" +                                                    // Pass texture coordinates to the fragment shader
                        "  vNormal = normalize(mat3(uModelMatrix) * aNormal);" +                        // Transform and normalize normal vector
                        "  vPosition = vec3(uModelMatrix * aPosition);" +                               // Transform position to world space
                        "}";

        // Fragment shader code for rendering the sphere (basic version for background)
        String sphereFragmentShaderCode =
                "precision mediump float;" +                                                            // Set floating-point precision
                        "uniform sampler2D uEarthTexture;" +                                            // Uniform sampler for earth texture
                        "uniform sampler2D uCloudTexture;" +                                            // Uniform sampler for cloud texture
                        "uniform float uCloudRotation;" +                                               // Uniform for cloud rotation (degrees)
                        "uniform float uCloudAlpha;" +                                                  // Uniform for cloud transparency
                        "varying vec2 vTexCoord;" +                                                     // Varying variable to receive texture coordinates from vertex shader
                        "void main() {" +
                        "  float rotationOffset = uCloudRotation / 360.0;" +                            // Convert rotation degrees to texture offset
                        "  float rotatedU = mod(vTexCoord.x + rotationOffset, 1.0);" +                  // Wrap horizontally for seamless clouds
                        "  vec2 rotatedCoord = vec2(rotatedU, vTexCoord.y);" +                          // Use rotated horizontal coordinate
                        "  vec4 earthColor = texture2D(uEarthTexture, vTexCoord);" +                    // Sample earth texture
                        "  vec4 cloudSample = texture2D(uCloudTexture, rotatedCoord);" +                // Sample cloud texture
                        "  float cloudMask = cloudSample.r;" +                                          // Use red channel as intensity
                        "  float blendFactor = cloudMask * uCloudAlpha;" +                              // Compute blend factor with alpha
                        "  vec3 cloudColor = vec3(cloudMask);" +                                        // Convert to grayscale color
                        "  vec3 finalColor = mix(earthColor.rgb, cloudColor, clamp(blendFactor, 0.0, 1.0));" + // Blend colors
                        "  gl_FragColor = vec4(finalColor, earthColor.a);" +                            // Output final color
                        "}";

        // Fragment shader code for Earth with specular and normal maps
        String earthFragmentShaderCode =
                "precision mediump float;" +                                                            // Set floating-point precision
                        "uniform sampler2D uEarthTexture;" +                                            // Uniform sampler for earth texture
                        "uniform sampler2D uEarthNightTexture;" +                                       // Uniform sampler for earth night texture
                        "uniform sampler2D uCloudTexture;" +                                            // Uniform sampler for cloud texture
                        "uniform sampler2D uSpecularTexture;" +                                         // Uniform sampler for specular map
                        "uniform sampler2D uNormalTexture;" +                                           // Uniform sampler for normal map
                        "uniform float uCloudRotation;" +                                               // Uniform for cloud rotation (degrees)
                        "uniform float uCloudAlpha;" +                                                  // Uniform for cloud transparency
                        "uniform vec3 uLightDirection;" +                                              // Uniform for light direction (from Sun)
                        "uniform vec3 uLightColor;" +                                                   // Uniform for light color
                        "varying vec2 vTexCoord;" +                                                     // Varying variable to receive texture coordinates from vertex shader
                        "varying vec3 vNormal;" +                                                       // Varying variable to receive normals from vertex shader
                        "varying vec3 vPosition;" +                                                     // Varying variable to receive position from vertex shader
                        "void main() {" +
                        "  float rotationOffset = uCloudRotation / 360.0;" +                            // Convert rotation degrees to texture offset
                        "  float rotatedU = mod(vTexCoord.x + rotationOffset, 1.0);" +                  // Wrap horizontally for seamless clouds
                        "  vec2 rotatedCoord = vec2(rotatedU, vTexCoord.y);" +                          // Use rotated horizontal coordinate
                        "  vec4 earthColor = texture2D(uEarthTexture, vTexCoord);" +                     // Sample earth texture
                        "  vec4 earthNightColor = texture2D(uEarthNightTexture, vTexCoord);" +          // Sample earth night texture
                        "  vec4 cloudSample = texture2D(uCloudTexture, rotatedCoord);" +                 // Sample cloud texture
                        "  vec4 specularSample = texture2D(uSpecularTexture, vTexCoord);" +             // Sample specular map
                        "  vec4 normalSample = texture2D(uNormalTexture, vTexCoord);" +                  // Sample normal map
                        "  vec3 normal = normalize(vNormal);" +                                         // Base normal from geometry
                        "  vec3 normalMap = normalize(normalSample.rgb * 2.0 - 1.0);" +                 // Unpack normal map from [0,1] to [-1,1]
                        "  normal = normalize(normal + normalMap * 0.5);" +                             // Blend geometry normal with normal map
                        "  vec3 lightDir = normalize(-uLightDirection);" +                               // Normalize light direction
                        "  float NdotL = dot(normal, lightDir);" +                                      // Calculate dot product (can be negative for shadow)
                        "  float dayNightBlend = smoothstep(-0.3, 0.2, NdotL);" +                       // Smooth transition between day and night (shadow area uses night texture)
                        "  vec3 dayColor = earthColor.rgb;" +                                           // Day texture color
                        "  vec3 nightColor = earthNightColor.rgb * 2.5;" +                              // Night texture color - boosted for visibility
                        "  vec3 baseColor = mix(nightColor, dayColor, dayNightBlend);" +                // Blend day/night textures based on light
                        "  float litNdotL = max(NdotL, 0.0);" +                                         // Clamped for lighting calculations
                        "  float nightFactor = 1.0 - dayNightBlend;" +                                 // How much we're in night (0 = day, 1 = night)
                        "  float ambient = mix(0.15, 0.4, nightFactor);" +                              // Higher ambient on night side for visibility
                        "  vec3 viewDir = normalize(-vPosition);" +                                     // View direction (simplified)
                        "  vec3 reflectDir = reflect(-lightDir, normal);" +                             // Reflection direction
                        "  float specular = pow(max(dot(viewDir, reflectDir), 0.0), 32.0);" +            // Specular highlight
                        "  float specularMask = specularSample.r;" +                                    // Use specular map to mask reflections
                        "  specular *= specularMask * dayNightBlend;" +                                 // Apply specular mask and only on day side
                        "  float cloudMask = cloudSample.r;" +                                          // Use red channel as intensity
                        "  float blendFactor = cloudMask * uCloudAlpha;" +                              // Compute blend factor with alpha
                        "  vec3 cloudColor = vec3(cloudMask);" +                                        // Convert to grayscale color
                        "  vec3 litColor = baseColor * (ambient + litNdotL * uLightColor);" +           // Apply ambient + diffuse lighting
                        "  litColor += specular * uLightColor * 0.5;" +                                 // Add specular highlights
                        "  vec3 finalColor = mix(litColor, cloudColor, clamp(blendFactor, 0.0, 1.0));" + // Blend with clouds
                        "  gl_FragColor = vec4(finalColor, earthColor.a);" +                            // Output final color
                        "}";

        // Fragment shader code for Sun with glowing effect
        String sunFragmentShaderCode =
                "precision mediump float;" +                                                            // Set floating-point precision
                        "uniform sampler2D uSunTexture;" +                                              // Uniform sampler for sun texture
                        "uniform float uGlowIntensity;" +                                               // Uniform for glow intensity
                        "varying vec2 vTexCoord;" +                                                     // Varying variable to receive texture coordinates from vertex shader
                        "void main() {" +
                        "  vec4 sunColor = texture2D(uSunTexture, vTexCoord);" +                       // Sample sun texture
                        "  vec2 center = vec2(0.5, 0.5);" +                                             // Texture center
                        "  float dist = distance(vTexCoord, center);" +                                 // Distance from center
                        "  float glow = 1.0 + uGlowIntensity * (1.0 - dist * 2.0);" +                  // Calculate glow based on distance
                        "  glow = max(glow, 1.0);" +                                                    // Ensure minimum brightness
                        "  vec3 finalColor = sunColor.rgb * glow;" +                                    // Apply glow to sun color
                        "  gl_FragColor = vec4(finalColor, sunColor.a);" +                               // Output final color with glow
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
                        "uniform sampler2D uCloudTexture;" +                                            // Uniform sampler for cloud texture
                        "uniform float uBlurIntensity;" +                                              // Uniform for blur intensity control
                        "uniform float uCloudRotation;" +                                               // Uniform for cloud rotation (degrees)
                        "uniform float uCloudAlpha;" +                                                  // Uniform for cloud transparency
                        "varying vec2 vTexCoord;" +                                                     // Varying variable to receive texture coordinates from vertex shader
                        "void main() {" +
                        "  vec4 baseColor = texture2D(uEarthTexture, vTexCoord);" +                    // Sample original texture color
                        "  vec2 center = vec2(0.5, 0.5);" +                                            // Calculate texture center point
                        "  vec2 dir = normalize(vTexCoord - center);" +                                // Direction vector from center
                        "  float dist = distance(vTexCoord, center);" +                                 // Distance from center for blur strength
                        "  vec4 blurColor = vec4(0.0);" +                                               // Initialize blur color accumulator
                        "  float samples = 8.0;" +                                                      // Number of samples for blur effect
                        "  for (float i = 0.0; i < 8.0; i++) {" +                                      // Accumulate blur samples
                        "    float offset = (i / samples) * uBlurIntensity * dist;" +                  // Offset based on sample index and blur intensity
                        "    vec2 sampleCoord = vTexCoord + dir * offset;" +                            // Sample coordinate along direction vector
                        "    blurColor += texture2D(uEarthTexture, sampleCoord);" +                     // Accumulate color from sample
                        "  }" +
                        "  blurColor /= samples;" +                                                     // Average blur color
                        "  vec3 blurredEarth = mix(baseColor.rgb, blurColor.rgb, clamp(uBlurIntensity, 0.0, 1.0));" + // Blend blurred and base earth color
                        "  float rotationOffset = uCloudRotation / 360.0;" +                            // Convert rotation degrees to texture offset
                        "  float rotatedU = mod(vTexCoord.x + rotationOffset, 1.0);" +                  // Wrap horizontally for clouds
                        "  vec2 rotatedCoord = vec2(rotatedU, vTexCoord.y);" +                          // Use rotated horizontal coordinate
                        "  vec4 cloudSample = texture2D(uCloudTexture, rotatedCoord);" +                // Sample cloud texture
                        "  float cloudMask = cloudSample.r;" +                                          // Use grayscale intensity as mask
                        "  float blendFactor = cloudMask * uCloudAlpha;" +                              // Scale by desired alpha
                        "  vec3 cloudColor = vec3(cloudMask);" +                                        // Convert intensity to RGB
                        "  vec3 finalColor = mix(blurredEarth, cloudColor, clamp(blendFactor, 0.0, 1.0));" + // Overlay clouds onto blurred earth
                        "  gl_FragColor = vec4(finalColor, baseColor.a);" +                             // Output final color with alpha
                        "}";

        int vertexShader = loadShader(GLES32.GL_VERTEX_SHADER, vertexShaderCode);
        int earthVertexShader = loadShader(GLES32.GL_VERTEX_SHADER, earthVertexShaderCode);
        int sphereFragmentShader = loadShader(GLES32.GL_FRAGMENT_SHADER, sphereFragmentShaderCode);
        int earthFragmentShader = loadShader(GLES32.GL_FRAGMENT_SHADER, earthFragmentShaderCode);
        int sunFragmentShader = loadShader(GLES32.GL_FRAGMENT_SHADER, sunFragmentShaderCode);
        int cloudFragmentShader = loadShader(GLES32.GL_FRAGMENT_SHADER, moonFragmentShaderCode);
        int motionBlurFragmentShader = loadShader(GLES32.GL_FRAGMENT_SHADER, motionBlurFragmentShaderCode);

        programEarth = GLES32.glCreateProgram();
        GLES32.glAttachShader(programEarth, earthVertexShader);
        GLES32.glAttachShader(programEarth, earthFragmentShader);
        GLES32.glLinkProgram(programEarth);

        programSun = GLES32.glCreateProgram();
        GLES32.glAttachShader(programSun, vertexShader);
        GLES32.glAttachShader(programSun, sunFragmentShader);
        GLES32.glLinkProgram(programSun);

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
        normalHandler = GLES32.glGetAttribLocation(programEarth, "aNormal");
        earthTextureUniformHandler = GLES32.glGetUniformLocation(programEarth, "uEarthTexture");
        earthNightTextureUniformHandler = GLES32.glGetUniformLocation(programEarth, "uEarthNightTexture");
        earthCloudTextureUniformHandler = GLES32.glGetUniformLocation(programEarth, "uCloudTexture");
        earthCloudRotationUniformHandler = GLES32.glGetUniformLocation(programEarth, "uCloudRotation");
        earthCloudAlphaUniformHandler = GLES32.glGetUniformLocation(programEarth, "uCloudAlpha");
        earthSpecularTextureUniformHandler = GLES32.glGetUniformLocation(programEarth, "uSpecularTexture");
        earthNormalTextureUniformHandler = GLES32.glGetUniformLocation(programEarth, "uNormalTexture");
        sunTextureUniformHandler = GLES32.glGetUniformLocation(programSun, "uSunTexture");
        sunGlowIntensityUniformHandler = GLES32.glGetUniformLocation(programSun, "uGlowIntensity");
        sunLightDirectionUniformHandler = GLES32.glGetUniformLocation(programEarth, "uLightDirection");
        sunLightColorUniformHandler = GLES32.glGetUniformLocation(programEarth, "uLightColor");
        moonTextureUniformHandler = GLES32.glGetUniformLocation(programMoon, "uCloudTexture");
        backgroundTextureUniformHandler = GLES32.glGetUniformLocation(programBackground, "uEarthTexture");
        motionBlurTextureUniformHandler = GLES32.glGetUniformLocation(programMotionBlur, "uEarthTexture");
        motionBlurCloudTextureUniformHandler = GLES32.glGetUniformLocation(programMotionBlur, "uCloudTexture");
        motionBlurCloudRotationUniformHandler = GLES32.glGetUniformLocation(programMotionBlur, "uCloudRotation");
        motionBlurCloudAlphaUniformHandler = GLES32.glGetUniformLocation(programMotionBlur, "uCloudAlpha");
        motionBlurIntensityHandler = GLES32.glGetUniformLocation(programMotionBlur, "uBlurIntensity");
        matrixHandler = GLES32.glGetUniformLocation(programEarth, "uMVPMatrix");
        matrixHandlerBackground = GLES32.glGetUniformLocation(programBackground, "uMVPMatrix");
        matrixHandlerMotionBlur = GLES32.glGetUniformLocation(programMotionBlur, "uMVPMatrix");
        matrixHandlerSun = GLES32.glGetUniformLocation(programSun, "uMVPMatrix");
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
        int numVertices = (LATITUDE_BANDS + 1) * (LONGITUDE_BANDS + 1);
        int numIndices = 6 * LATITUDE_BANDS * LONGITUDE_BANDS;

        float[] vertices = new float[3 * numVertices];
        float[] texCords = new float[2 * numVertices];
        short[] indices = new short[numIndices];

        int vertexIndex = 0;
        int textureCoordinateIndex = 0;
        for (int lat = 0; lat <= LATITUDE_BANDS; lat++) {
            float theta = lat * (float) Math.PI / LATITUDE_BANDS;
            float sinTheta = (float) Math.sin(theta);
            float cosTheta = (float) Math.cos(theta);

            for (int lon = 0; lon <= LONGITUDE_BANDS; lon++) {
                float phi = lon * 2 * (float) Math.PI / LONGITUDE_BANDS;
                float sinPhi = (float) Math.sin(phi);
                float cosPhi = (float) Math.cos(phi);

                float x = cosPhi * sinTheta;
                float z = sinPhi * sinTheta;
                float u = 1.0f - (lon / (float) LONGITUDE_BANDS);
                float v = lat / (float) LATITUDE_BANDS;

                vertices[vertexIndex++] = x;
                vertices[vertexIndex++] = cosTheta;
                vertices[vertexIndex++] = z;

                texCords[textureCoordinateIndex++] = u;
                texCords[textureCoordinateIndex++] = v;
            }
        }

        int index = 0;
        for (int lat = 0; lat < LATITUDE_BANDS; lat++) {
            for (int lon = 0; lon < LONGITUDE_BANDS; lon++) {
                int first = (lat * (LONGITUDE_BANDS + 1)) + lon;
                int second = first + LONGITUDE_BANDS + 1;

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

        // Generate normals (for a sphere, normals are the same as vertex positions)
        float[] normals = new float[3 * numVertices];
        System.arraycopy(vertices, 0, normals, 0, vertices.length);
        normalBuffer = ByteBuffer.allocateDirect(normals.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
        normalBuffer.put(normals).position(0);

        indexBuffer = ByteBuffer.allocateDirect(indices.length * 2).order(ByteOrder.nativeOrder()).asShortBuffer();
        indexBuffer.put(indices).position(0);
    }

    // Draw the Earth using the specified Earth and cloud textures and MVP matrix
    public void drawEarth(int earthTexture, int earthNightTexture, int cloudTexture, int specularTexture, int normalTexture, float cloudRotation, float cloudAlpha, float[] mvpMatrix, float[] modelMatrix, float[] lightDirection, float[] lightColor) {
        GLES32.glUseProgram(programEarth);

        GLES32.glVertexAttribPointer(positionHandler, 3, GLES32.GL_FLOAT, false, 0, vertexBuffer);
        GLES32.glEnableVertexAttribArray(positionHandler);

        GLES32.glVertexAttribPointer(textureCoordinateHandler, 2, GLES32.GL_FLOAT, false, 0, texBuffer);
        GLES32.glEnableVertexAttribArray(textureCoordinateHandler);

        GLES32.glVertexAttribPointer(normalHandler, 3, GLES32.GL_FLOAT, false, 0, normalBuffer);
        GLES32.glEnableVertexAttribArray(normalHandler);

        GLES32.glActiveTexture(GLES32.GL_TEXTURE0);
        GLES32.glBindTexture(GLES32.GL_TEXTURE_2D, earthTexture);
        GLES32.glUniform1i(earthTextureUniformHandler, 0);

        GLES32.glActiveTexture(GLES32.GL_TEXTURE2);
        GLES32.glBindTexture(GLES32.GL_TEXTURE_2D, earthNightTexture);
        GLES32.glUniform1i(earthNightTextureUniformHandler, 2);

        GLES32.glActiveTexture(GLES32.GL_TEXTURE3);
        GLES32.glBindTexture(GLES32.GL_TEXTURE_2D, specularTexture);
        GLES32.glUniform1i(earthSpecularTextureUniformHandler, 3);

        GLES32.glActiveTexture(GLES32.GL_TEXTURE4);
        GLES32.glBindTexture(GLES32.GL_TEXTURE_2D, normalTexture);
        GLES32.glUniform1i(earthNormalTextureUniformHandler, 4);

        // Set all uniforms before binding cloud texture
        GLES32.glUniform1f(earthCloudRotationUniformHandler, cloudRotation);
        GLES32.glUniform1f(earthCloudAlphaUniformHandler, cloudAlpha);
        GLES32.glUniform3fv(sunLightDirectionUniformHandler, 1, lightDirection, 0);
        GLES32.glUniform3fv(sunLightColorUniformHandler, 1, lightColor, 0);
        GLES32.glUniformMatrix4fv(matrixHandler, 1, false, mvpMatrix, 0);
        int modelMatrixHandler = GLES32.glGetUniformLocation(programEarth, "uModelMatrix");
        GLES32.glUniformMatrix4fv(modelMatrixHandler, 1, false, modelMatrix, 0);

        // Bind cloud texture LAST, RIGHT BEFORE drawing, to prevent blend/transparency state from affecting it
        // This ensures the texture parameters are fresh and not corrupted by blending operations
        GLES32.glActiveTexture(GLES32.GL_TEXTURE1);
        GLES32.glBindTexture(GLES32.GL_TEXTURE_2D, cloudTexture);
        // CRITICAL: Force ALL texture parameters EVERY frame to prevent blend/transparency from corrupting texture state
        GLES32.glTexParameteri(GLES32.GL_TEXTURE_2D, GLES32.GL_TEXTURE_BASE_LEVEL, 0);
        GLES32.glTexParameteri(GLES32.GL_TEXTURE_2D, GLES32.GL_TEXTURE_MAX_LEVEL, 0);
        GLES32.glTexParameteri(GLES32.GL_TEXTURE_2D, GLES32.GL_TEXTURE_MIN_FILTER, GLES32.GL_LINEAR);
        GLES32.glTexParameteri(GLES32.GL_TEXTURE_2D, GLES32.GL_TEXTURE_MAG_FILTER, GLES32.GL_LINEAR);
        GLES32.glTexParameteri(GLES32.GL_TEXTURE_2D, GLES32.GL_TEXTURE_WRAP_S, GLES32.GL_REPEAT);
        GLES32.glTexParameteri(GLES32.GL_TEXTURE_2D, GLES32.GL_TEXTURE_WRAP_T, GLES32.GL_REPEAT);
        // Force texture LOD bias to 0 to always use the base mipmap level (full resolution)
        try {
            GLES32.glTexParameterf(GLES32.GL_TEXTURE_2D, 0x8501, 0.0f); // GL_TEXTURE_LOD_BIAS
        } catch (Exception e) {
            // LOD bias not available, continue without it
        }
        // Re-apply anisotropic filtering if available
        try {
            String extensions = GLES32.glGetString(GLES32.GL_EXTENSIONS);
            if (extensions != null) {
                boolean hasAnisotropic = extensions.contains("GL_EXT_texture_filter_anisotropic") ||
                                       extensions.contains("texture_filter_anisotropic") ||
                                       extensions.contains("GL_ARB_texture_filter_anisotropic");
                if (hasAnisotropic) {
                    final float[] maxAnisotropy = new float[1];
                    GLES32.glGetFloatv(0x84FE, maxAnisotropy, 0);
                    if (maxAnisotropy[0] > 0) {
                        GLES32.glTexParameterf(GLES32.GL_TEXTURE_2D, 0x84FF, Math.min(16.0f, maxAnisotropy[0]));
                    }
                }
            }
        } catch (Exception e) {
            // Anisotropic filtering not available, continue without it
        }
        GLES32.glUniform1i(earthCloudTextureUniformHandler, 1);

        GLES32.glDrawElements(GLES32.GL_TRIANGLES, numIndices, GLES32.GL_UNSIGNED_SHORT, indexBuffer);

        GLES32.glDisableVertexAttribArray(positionHandler);
        GLES32.glDisableVertexAttribArray(textureCoordinateHandler);
        GLES32.glDisableVertexAttribArray(normalHandler);
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

    // Draw the background space texture with motion blur effect during transitions
    public void drawBackgroundWithBlur(int backgroundTexture, float[] mvpMatrix, float blurIntensity) {
        GLES32.glUseProgram(programMotionBlur);
        GLES32.glDisable(GLES32.GL_DEPTH_TEST);

        GLES32.glVertexAttribPointer(positionHandler, 3, GLES32.GL_FLOAT, false, 0, vertexBuffer);
        GLES32.glEnableVertexAttribArray(positionHandler);

        GLES32.glVertexAttribPointer(textureCoordinateHandler, 2, GLES32.GL_FLOAT, false, 0, texBuffer);
        GLES32.glEnableVertexAttribArray(textureCoordinateHandler);

        GLES32.glActiveTexture(GLES32.GL_TEXTURE0);
        GLES32.glBindTexture(GLES32.GL_TEXTURE_2D, backgroundTexture);
        GLES32.glUniform1i(motionBlurTextureUniformHandler, 0);

        // Use a dummy texture for cloud (not used, but required by shader)
        GLES32.glActiveTexture(GLES32.GL_TEXTURE1);
        GLES32.glBindTexture(GLES32.GL_TEXTURE_2D, backgroundTexture);
        GLES32.glUniform1i(motionBlurCloudTextureUniformHandler, 1);

        GLES32.glUniform1f(motionBlurIntensityHandler, blurIntensity);
        GLES32.glUniform1f(motionBlurCloudRotationUniformHandler, 0.0f);
        GLES32.glUniform1f(motionBlurCloudAlphaUniformHandler, 0.0f); // Disable cloud blending

        GLES32.glUniformMatrix4fv(matrixHandlerMotionBlur, 1, false, mvpMatrix, 0);

        GLES32.glDrawElements(GLES32.GL_TRIANGLES, numIndices, GLES32.GL_UNSIGNED_SHORT, indexBuffer);

        GLES32.glDisableVertexAttribArray(positionHandler);
        GLES32.glDisableVertexAttribArray(textureCoordinateHandler);
        GLES32.glEnable(GLES32.GL_DEPTH_TEST);
    }

    // Draw the Sun using the specified Sun texture and MVP matrix with glow effect
    public void drawSun(int sunTexture, float[] mvpMatrix, float glowIntensity) {
        GLES32.glUseProgram(programSun);

        GLES32.glVertexAttribPointer(positionHandler, 3, GLES32.GL_FLOAT, false, 0, vertexBuffer);
        GLES32.glEnableVertexAttribArray(positionHandler);

        GLES32.glVertexAttribPointer(textureCoordinateHandler, 2, GLES32.GL_FLOAT, false, 0, texBuffer);
        GLES32.glEnableVertexAttribArray(textureCoordinateHandler);

        GLES32.glActiveTexture(GLES32.GL_TEXTURE0);
        GLES32.glBindTexture(GLES32.GL_TEXTURE_2D, sunTexture);
        GLES32.glUniform1i(sunTextureUniformHandler, 0);

        GLES32.glUniform1f(sunGlowIntensityUniformHandler, glowIntensity);

        GLES32.glUniformMatrix4fv(matrixHandlerSun, 1, false, mvpMatrix, 0);

        GLES32.glDrawElements(GLES32.GL_TRIANGLES, numIndices, GLES32.GL_UNSIGNED_SHORT, indexBuffer);

        GLES32.glDisableVertexAttribArray(positionHandler);
        GLES32.glDisableVertexAttribArray(textureCoordinateHandler);
    }

}
