package com.emperium.neoporiumscanner.xray;

public record BasicColor(int r, int g, int b, int a) {
    public BasicColor(int r, int g, int b) {
        this(r, g, b, 255);
    }

    public int getRGB() {
        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    public float getRedFloat() {
        return r / 255.0f;
    }

    public float getGreenFloat() {
        return g / 255.0f;
    }

    public float getBlueFloat() {
        return b / 255.0f;
    }

    public float getAlphaFloat() {
        return a / 255.0f;
    }

    public BasicColor withAlpha(int alpha) {
        return new BasicColor(r, g, b, Math.max(0, Math.min(255, alpha)));
    }

    public BasicColor brighter() {
        return new BasicColor(
                Math.min(255, (int)(r * 1.2)),
                Math.min(255, (int)(g * 1.2)),
                Math.min(255, (int)(b * 1.2)),
                a
        );
    }

    public BasicColor darker() {
        return new BasicColor(
                Math.max(0, (int)(r * 0.8)),
                Math.max(0, (int)(g * 0.8)),
                Math.max(0, (int)(b * 0.8)),
                a
        );
    }

    @Override
    public String toString() {
        return String.format("RGBA(%d, %d, %d, %d)", r, g, b, a);
    }
}