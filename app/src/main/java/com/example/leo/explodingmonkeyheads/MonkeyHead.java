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

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.Random;

import android.opengl.GLES20;
import android.opengl.Matrix;

public class MonkeyHead {
    private boolean useExplosionAnimation; // whether the explosion animation is currently active
    float color[]; // object's color

    final static float step = 0.3f;
    float elapsedTime = step;

    static FloatBuffer vertexBuffer, normalsBuffer;
    static int mDefaultProgram = -1, mExplosionProgram = -1;

    int mColorHandle, mPositionHandle, mNormalHandle, mModelViewMatrixHandle,
            mProjectionMatrixHandle, mCameraMatrixHandle, mElapsedTimeHandle;

    public static String explosionVertexShaderCode, simpleFragmentShaderCode;

    // for simplicity, we assume the projection matrix will be constant
    float[] modelViewMatrix, projectionMatrix;
    public static float[] positions, normals; // vertices and normals data

    final int coordsPerVertex = 3;
    final int vertexCount;
    final int vertexStride = coordsPerVertex * 4; // 4 bytes per vertex

    Color pickingColor; // unique color used to determine what specific object was picked by the player
    float rotFactorAroundX, rotFactorAroundY, rotFactorAroundZ; // determines how much the object must rotate around each axis

    final static Random random = new Random();

    /**
     * Compiles the shaders and links the program, only if it hasn't been done yet
     */
    public static void compileShaders() {
        if (mDefaultProgram != -1) {
            return; // early return if GL program already exists
        }

        // initialize vertex buffer
        ByteBuffer bb = ByteBuffer.allocateDirect(positions.length * 4);
        bb.order(ByteOrder.nativeOrder());

        vertexBuffer = bb.asFloatBuffer();
        vertexBuffer.put(positions);
        vertexBuffer.position(0);

        // initialize normals buffer
        bb = ByteBuffer.allocateDirect(normals.length * 4);
        bb.order(ByteOrder.nativeOrder());

        normalsBuffer = bb.asFloatBuffer();
        normalsBuffer.put(normals);
        normalsBuffer.position(0);

        int explosionVertexShader = MonkeyHeadsRenderer.loadShader(
                GLES20.GL_VERTEX_SHADER, explosionVertexShaderCode);
        int simpleFragmentShader = MonkeyHeadsRenderer.loadShader(
                GLES20.GL_FRAGMENT_SHADER, simpleFragmentShaderCode);

        mDefaultProgram = GLES20.glCreateProgram();
        GLES20.glAttachShader(mDefaultProgram, explosionVertexShader);
        GLES20.glAttachShader(mDefaultProgram, simpleFragmentShader);
        GLES20.glLinkProgram(mDefaultProgram);
    }

    public MonkeyHead() {
        compileShaders();

        color = new float[] { random.nextFloat(), random.nextFloat(), random.nextFloat(), 1.0f };
        rotFactorAroundX = random.nextFloat();
        rotFactorAroundY = random.nextFloat();
        rotFactorAroundZ = random.nextFloat();

        vertexCount = positions.length / coordsPerVertex;
    }

    public float[] getModelViewMatrix() {
        return modelViewMatrix;
    }

    public void setModelViewMatrix(float[] modelViewMatrix) {
        this.modelViewMatrix = modelViewMatrix;
    }

    public float[] getProjectionMatrix() {
        return projectionMatrix;
    }

    public void setProjectionMatrix(float[] projectionMatrix) {
        this.projectionMatrix = projectionMatrix;
    }

    public float[] getColor() {
        return color;
    }

    public Color getPickingColor() {
        return pickingColor;
    }

    public void setPickingColor(Color pickingColor) {
        this.pickingColor = pickingColor;
    }

    public String getExplosionVertexShaderCode() {
        return explosionVertexShaderCode;
    }

    public void setExplosionVertexShaderCode(String explosionVertexShaderCode) {
        this.explosionVertexShaderCode = explosionVertexShaderCode;
    }

    public String getSimpleFragmentShaderCode() {
        return simpleFragmentShaderCode;
    }

    public void setSimpleFragmentShaderCode(String simpleFragmentShaderCode) {
        this.simpleFragmentShaderCode = simpleFragmentShaderCode;
    }

    public boolean isUseExplosionAnimation() {
        return useExplosionAnimation;
    }

    public void setUseExplosionAnimation(boolean useExplosionAnimation) {
        this.useExplosionAnimation = useExplosionAnimation;
    }

    public void draw(float[] cameraMatrix) {
        draw(cameraMatrix, false);
    }

    public void draw(float[] cameraMatrix, boolean objectPickingMode) {
        GLES20.glUseProgram(mDefaultProgram);

        mPositionHandle = GLES20.glGetAttribLocation(mDefaultProgram, "vPosition");
        mNormalHandle = GLES20.glGetAttribLocation(mDefaultProgram, "vNormal");

        GLES20.glEnableVertexAttribArray(mPositionHandle);
        GLES20.glEnableVertexAttribArray(mNormalHandle);

        GLES20.glVertexAttribPointer(
                mPositionHandle,
                coordsPerVertex,
                GLES20.GL_FLOAT,
                false,
                vertexStride,
                vertexBuffer);

        GLES20.glVertexAttribPointer(
                mNormalHandle,
                coordsPerVertex,
                GLES20.GL_FLOAT,
                false,
                vertexStride,
                normalsBuffer);

        mColorHandle = GLES20.glGetUniformLocation(mDefaultProgram, "vColor");
        MonkeyHeadsRenderer.checkGlError("glGetUniformLocation");

        // set the monkey head color
        float[] c = objectPickingMode ?
                new float[] { pickingColor.getR() / 255.0f, pickingColor.getG() / 255.0f, pickingColor.getB() / 255.0f, 1f } : color;
        GLES20.glUniform4fv(mColorHandle, 1, c, 0);

        mCameraMatrixHandle = GLES20.glGetUniformLocation(mDefaultProgram, "uCameraMatrix");
        MonkeyHeadsRenderer.checkGlError("glGetUniformLocation");

        mModelViewMatrixHandle = GLES20.glGetUniformLocation(mDefaultProgram, "uModelViewMatrix");
        MonkeyHeadsRenderer.checkGlError("glGetUniformLocation");

        mProjectionMatrixHandle = GLES20.glGetUniformLocation(mDefaultProgram, "uProjectionMatrix");
        MonkeyHeadsRenderer.checkGlError("glGetUniformLocation");

        mElapsedTimeHandle = GLES20.glGetUniformLocation(mDefaultProgram, "time");
        MonkeyHeadsRenderer.checkGlError("glGetUniformLocation");

        GLES20.glUniformMatrix4fv(mCameraMatrixHandle, 1, false, cameraMatrix, 0);
        MonkeyHeadsRenderer.checkGlError("glUniformMatrix4fv");

        GLES20.glUniformMatrix4fv(mProjectionMatrixHandle, 1, false, projectionMatrix, 0);
        MonkeyHeadsRenderer.checkGlError("glUniformMatrix4fv");

        Matrix.rotateM(modelViewMatrix, 0, step, rotFactorAroundX, rotFactorAroundY, rotFactorAroundZ);

        GLES20.glUniformMatrix4fv(mModelViewMatrixHandle, 1, false, modelViewMatrix, 0);
        MonkeyHeadsRenderer.checkGlError("glUniformMatrix4fv");

        if (useExplosionAnimation) {
            GLES20.glUniform1f(mElapsedTimeHandle, elapsedTime);
            MonkeyHeadsRenderer.checkGlError("glUniform1f");
            elapsedTime += step;
        } else {
            GLES20.glUniform1f(mElapsedTimeHandle, 0f);
            MonkeyHeadsRenderer.checkGlError("glUniform1f");
        }

        // draw the object
        GLES20.glFrontFace(GLES20.GL_CW);
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, vertexCount);

        // disable vertex array
        GLES20.glDisableVertexAttribArray(mPositionHandle);
    }
}
