package com.emperium.neoporiumscanner.utils;

public class ColorUtils {
    public static int blendColors(int color1, int color2, float ratio) {
        float inverseRatio = 1.0f - ratio;

        int a1 = (color1 >> 24) & 0xFF;
        int r1 = (color1 >> 16) & 0xFF;
        int g1 = (color1 >> 8) & 0xFF;
        int b1 = color1 & 0xFF;

        int a2 = (color2 >> 24) & 0xFF;
        int r2 = (color2 >> 16) & 0xFF;
        int g2 = (color2 >> 8) & 0xFF;
        int b2 = color2 & 0xFF;

        int a = (int)(a1 * inverseRatio + a2 * ratio);
        int r = (int)(r1 * inverseRatio + r2 * ratio);
        int g = (int)(g1 * inverseRatio + g2 * ratio);
        int b = (int)(b1 * inverseRatio + b2 * ratio);

        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    public static int withAlpha(int color, int alpha) {
        return (alpha << 24) | (color & 0xFFFFFF);
    }
}