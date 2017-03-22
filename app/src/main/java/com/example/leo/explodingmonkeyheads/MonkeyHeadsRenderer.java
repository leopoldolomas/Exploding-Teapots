/*
This is free and unencumbered software released into the public domain.

Anyone is free to copy, modify, publish, use, compile, sell, or
distribute this software, either in source code form or as a compiled
binary, for any purpose, commercial or non-commercial, and by any
means.

In jurisdictions that recognize copyright laws, the author or authors
of this software dedicate any and all copyright interest in the
software to the public domain. We make this dedication for the benefit
of the public at large and to the detriment of our heirs and
successors. We intend this dedication to be an overt act of
relinquishment in perpetuity of all present and future rights to this
software under copyright law.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
IN NO EVENT SHALL THE AUTHORS BE LIABLE FOR ANY CLAIM, DAMAGES OR
OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
OTHER DEALINGS IN THE SOFTWARE.

Leopoldo Lomas - March 2017
http://leopoldolomas.info
*/

package com.example.leo.explodingmonkeyheads;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.HashMap;

public class MonkeyHeadsRenderer implements GLSurfaceView.Renderer {
    private static final String TAG = "MonkeyHeadsRenderer";

    MonkeyHead[] monkeyHeads;
    HashMap<Integer, MonkeyHead> colorMonkeyHeadsMap = new HashMap<>();

    final int noOfObjects = 64; // the number of heads to be drawn on the screen
    final int dimensionLength = 22; // the larger the dimension length, the more spacing between each head
    int spacing;

    final float[] mProjectionMatrix = new float[16];
    final float[] mCameraMatrix = new float[16];

    float[] positions, normals;
    String explosionVertexShaderCode, simpleVertexShaderCode, simpleFragmentShaderCode;

    private long lastTick = 0L;
    int viewportWidth, viewportHeight;
    float cameraAngleX, cameraAngleY;

    public MonkeyHeadsRenderer() { }

    public float getCameraAngleX() {
        return cameraAngleX;
    }

    public void setCameraAngleX(float cameraAngleX) {
        this.cameraAngleX = cameraAngleX;
    }

    public float getCameraAngleY() {
        return cameraAngleY;
    }

    public void setCameraAngleY(float cameraAngleY) {
        this.cameraAngleY = cameraAngleY;
    }

    public float[] getPositions() {
        return positions;
    }

    public void setPositions(float[] positions) {
        this.positions = positions;
    }

    public float[] getNormals() {
        return normals;
    }

    public void setNormals(float[] normals) {
        this.normals = normals;
    }

    public String getExplosionVertexShaderCode() {
        return explosionVertexShaderCode;
    }

    public void setExplosionVertexShaderCode(String explosionVertexShaderCode) {
        this.explosionVertexShaderCode = explosionVertexShaderCode;
    }

    public String getSimpleVertexShaderCode() {
        return simpleVertexShaderCode;
    }

    public void setSimpleVertexShaderCode(String simpleVertexShaderCode) {
        this.simpleVertexShaderCode = simpleVertexShaderCode;
    }

    public String getSimpleFragmentShaderCode() {
        return simpleFragmentShaderCode;
    }

    public void setSimpleFragmentShaderCode(String simpleFragmentShaderCode) {
        this.simpleFragmentShaderCode = simpleFragmentShaderCode;
    }

    private void initializeObjects() {
        MonkeyHead.explosionVertexShaderCode = explosionVertexShaderCode;
        MonkeyHead.simpleFragmentShaderCode = simpleFragmentShaderCode;
        MonkeyHead.positions = getPositions();
        MonkeyHead.normals = getNormals();

        monkeyHeads = new MonkeyHead[noOfObjects];

        int objsPerDimension = (int) Math.cbrt(noOfObjects);
        float currentX, currentY, currentZ;
        spacing = dimensionLength / objsPerDimension;

        int i = 0;

        for (int x = 0; x < objsPerDimension; x++) {
            for (int y = 0; y < objsPerDimension; y++) {
                for (int z = 0; z < objsPerDimension; z++) {
                    currentX = (float) (x * spacing - (dimensionLength / 2.0));
                    currentY = (float) (y * spacing - (dimensionLength / 2.0));
                    currentZ = (float) (z * spacing - (dimensionLength / 2.0));

                    monkeyHeads[i] = new MonkeyHead();

                    float[] viewMatrix = new float[16];
                    Matrix.setIdentityM(viewMatrix, 0);
                    Matrix.translateM(viewMatrix, 0, currentX, currentY, currentZ);

                    monkeyHeads[i].setModelViewMatrix(viewMatrix);
                    Color c = ColorProvider.getNextColor();
                    monkeyHeads[i].setPickingColor(c);
                    colorMonkeyHeadsMap.put(c.getR() + c.getG() + c.getB(), monkeyHeads[i]);

                    i++;
                }
            }
        }
    }

    @Override
    public void onSurfaceCreated(GL10 unused, EGLConfig config) {
        // Set the background frame color
        GLES20.glClearColor(0.4f, 0.4f, 0.4f, 1.0f);

        initializeObjects();
    }

    public void draw() {
        draw(false);
    }

    public void draw(boolean objectPickingMode) {
        // Draw background color
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

        Matrix.setIdentityM(mCameraMatrix, 0);
        Matrix.rotateM(mCameraMatrix, 0, -cameraAngleX, 0.0f, 1.0f, 0f);
        Matrix.rotateM(mCameraMatrix, 0, -cameraAngleY, 1.0f, 0.0f, 0f);

        for (MonkeyHead t : monkeyHeads) {
            t.draw(mCameraMatrix, objectPickingMode);
        }
    }

    @Override
    public void onDrawFrame(GL10 unused) {
        //long currentTime = System.currentTimeMillis();
        //long dt = currentTime - lastTick;

        // TODO draw only 60 frames per second, this had to be disabled due to weird flickering problems
        //if (dt > 17) { // only 60 FPS (1000 ms / 60 fps)
            draw();
          //  lastTick = currentTime;
        //}
    }

    @Override
    public void onSurfaceChanged(GL10 unused, int width, int height) {
        // Adjust the viewport based on geometry changes,
        // such as screen rotation
        GLES20.glViewport(0, 0, width, height);

        float ratio = (float) width / height;

        // this projection matrix is applied to object coordinates
        // in the onDrawFrame() method
        Matrix.frustumM(mProjectionMatrix, 0, -ratio, ratio, -1, 1, 3, 50);

        for (MonkeyHead t : monkeyHeads) {
            t.setProjectionMatrix(mProjectionMatrix);
        }

        this.viewportHeight = height;
        this.viewportWidth = width;
    }

    /**
     * Utility method for compiling a OpenGL shader.
     *
     * <p><strong>Note:</strong> When developing shaders, use the checkGlError()
     * method to debug shader coding errors.</p>
     *
     * @param type - Vertex or fragment shader type.
     * @param shaderCode - String containing the shader code.
     * @return - Returns an id for the shader.
     */
    public static int loadShader(int type, String shaderCode){

        // create a vertex shader type (GLES20.GL_VERTEX_SHADER)
        // or a fragment shader type (GLES20.GL_FRAGMENT_SHADER)
        int shader = GLES20.glCreateShader(type);

        // add the source code to the shader and compile it
        GLES20.glShaderSource(shader, shaderCode);
        GLES20.glCompileShader(shader);

        return shader;
    }

    /**
    * Utility method for debugging OpenGL calls. Provide the name of the call
    * just after making it:
    *
    * <pre>
    * mColorHandle = GLES20.glGetUniformLocation(mProgram, "vColor");
    * MonkeyHeadsRenderer.checkGlError("glGetUniformLocation");</pre>
    *
    * If the operation is not successful, the check throws an error.
    *
    * @param glOperation - Name of the OpenGL call to check.
    */
    public static void checkGlError(String glOperation) {
        int error;
        while ((error = GLES20.glGetError()) != GLES20.GL_NO_ERROR) {
            Log.e(TAG, glOperation + ": glError " + error);
            throw new RuntimeException(glOperation + ": glError " + error);
        }
    }

    public void onTapEvent(int x, int y) {
        draw(true); // re-draw scene using unique color per object

        ByteBuffer PixelBuffer = ByteBuffer.allocateDirect(4);
        PixelBuffer.order(ByteOrder.nativeOrder());
        PixelBuffer.position(0);

        // read the pixel touched by the player
        GLES20.glReadPixels(x, this.viewportHeight - y, 1, 1, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, PixelBuffer);
        byte b [] = new byte[4];
        PixelBuffer.get(b);

        int cr = b[0];
        int cg = b[1];
        int cb = b[2];

        cr = (cr < 0) ? (256 - Math.abs(cr)) : cr;
        cg = (cg < 0) ? (256 - Math.abs(cg)) : cg;
        cb = (cb < 0) ? (256 - Math.abs(cb)) : cb;

        // retrieve the picked object by its color hash
        MonkeyHead pickedMonkeyHead = colorMonkeyHeadsMap.get(cr + cg + cb);

        if (pickedMonkeyHead != null) {
            pickedMonkeyHead.setUseExplosionAnimation(true);
        }
    }
}
