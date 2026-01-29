package com.emperium.neoporiumscanner.xray;

public record BasicColor(int r, int g, int b, int a) {
    public BasicColor {
        r = clamp(r, 0, 255);
        g = clamp(g, 0, 255);
        b = clamp(b, 0, 255);
        a = clamp(a, 0, 255);
    }

    public BasicColor(int r, int g, int b) {
        this(r, g, b, 255);
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    public int getRGB() {
        return (a << 24) | (r << 16) | (g << 8) | b;
    }
}