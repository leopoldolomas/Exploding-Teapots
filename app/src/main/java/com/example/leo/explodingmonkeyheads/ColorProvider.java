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

class Color {
    private final int r, g, b;

    public Color(int r, int g, int b) {
        this.r = r;
        this.g = g;
        this.b = b;
    }

    public int getR() {
        return r;
    }

    public int getG() {
        return g;
    }

    public int getB() {
        return b;
    }
}

public class ColorProvider {
    private static int r, g, b;

    private ColorProvider() { } // good old singleton

    public synchronized static void reset() {
        r = g = b = 1;
    }

    {
        reset(); // static blocks are indeed useful in Java!
    }

    public synchronized static Color getNextColor() {
        r++;
        if (r >= 255) {
            r = 255;  // clamp the value
            g++;
            if (g >= 255) {
                g = 255;
                b++;
                if (b >= 255) {
                    b = 255;
                }
            }
        }

        return new Color(r, g, b);
    }
}
