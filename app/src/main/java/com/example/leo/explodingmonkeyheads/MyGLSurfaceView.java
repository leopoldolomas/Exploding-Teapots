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

import android.content.Context;
import android.content.res.Resources;
import android.hardware.SensorManager;
import android.opengl.GLSurfaceView;
import android.support.v4.view.MotionEventCompat;
import android.view.MotionEvent;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * A view container where OpenGL ES graphics can be drawn on screen.
 * This view can also be used to capture touch events, such as a user
 * interacting with drawn objects.
 */
public class MyGLSurfaceView extends GLSurfaceView {

    final MonkeyHeadsRenderer mRenderer;
    // variables used to keep track of user's touch gestures
    float mOriginX, mOriginY, mPreviousX, mPreviousY;
    final float touchScaleFactor = 0.05f;

    public MyGLSurfaceView(Context context, SensorManager mSensorManager, Resources resources) {
        super(context);

        // Create an OpenGL ES 2.0 context.
        setEGLContextClientVersion(2);

        // Set the Renderer for drawing on the GLSurfaceView
        mRenderer = new MonkeyHeadsRenderer();
        setRenderer(mRenderer);

        float[] positions = readArraysResource(R.raw.positions);
        float[] normals = readArraysResource(R.raw.normals);

        mRenderer.setPositions(positions);
        mRenderer.setNormals(normals);

        String explosionVertexShaderCode = readResource(R.raw.explosion_vertex_shader);
        String simpleFragmentShaderCode = readResource(R.raw.simple_fragment_shader);

        mRenderer.setExplosionVertexShaderCode(explosionVertexShaderCode);
        mRenderer.setSimpleFragmentShaderCode(simpleFragmentShaderCode);
    }

    private String readResource(int resourceId) {
        InputStream is = getResources().openRawResource(resourceId);
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        StringBuilder r = new StringBuilder();
        String line;

        try {
            while ((line = br.readLine()) != null) {
                r.append(line).append('\n');
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return r.toString();
    }

    private float[] readArraysResource(int resourceId) {
        String rawStr = readResource(resourceId);
        String[] elements = rawStr.split(",");

        float[] array = new float[elements.length];

        for (int i = 0; i < array.length; i++) {
            try {
                array[i] = Float.parseFloat(elements[i]);
            } catch (NumberFormatException ex) {
                ex.printStackTrace();
            }
        }

        return array;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        final float x = event.getX();
        final float y = event.getY();

        int action = MotionEventCompat.getActionMasked(event);

        switch(action) {
            case (MotionEvent.ACTION_DOWN) :
                mPreviousX = mOriginX = x;
                mPreviousY = mOriginY = y;

                return true;
            case (MotionEvent.ACTION_MOVE) :
                float dx = x - mPreviousX;
                float dy = y - mPreviousY;

                mRenderer.setCameraAngleX(mRenderer.getCameraAngleX() + (dx * touchScaleFactor));
                mRenderer.setCameraAngleY(mRenderer.getCameraAngleY() + (dy * touchScaleFactor));

                mPreviousX = x;
                mPreviousY = y;
                return true;
            case (MotionEvent.ACTION_UP) :
                boolean isTap = Math.abs(mOriginX - x) < 2.0 && Math.abs(mOriginY - y) < 2.0;

                if (isTap) {
                    queueEvent(new Runnable(){
                        @Override
                        public void run() {
                            mRenderer.onTapEvent((int)x, (int)y);
                        }});
                }

                return true;
            case (MotionEvent.ACTION_CANCEL) :

                return true;
            case (MotionEvent.ACTION_OUTSIDE) :

                return true;
            default :
                return super.onTouchEvent(event);
        }
    }
}
