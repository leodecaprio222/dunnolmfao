package com.emperium.neoporiumscanner.xray;

public class BasicColor {
    private final int r;
    private final int g;
    private final int b;
    private final int a;

    public BasicColor(int r, int g, int b) {
        this(r, g, b, 255);
    }

    public BasicColor(int r, int g, int b, int a) {
        this.r = Math.max(0, Math.min(255, r));
        this.g = Math.max(0, Math.min(255, g));
        this.b = Math.max(0, Math.min(255, b));
        this.a = Math.max(0, Math.min(255, a));
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

    public int getA() {
        return a;
    }

    public float[] getFloats() {
        return new float[]{r / 255.0f, g / 255.0f, b / 255.0f, a / 255.0f};
    }

    public int getRGB() {
        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    public BasicColor withAlpha(int alpha) {
        return new BasicColor(r, g, b, alpha);
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
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        BasicColor that = (BasicColor) obj;
        return r == that.r && g == that.g && b == that.b && a == that.a;
    }

    @Override
    public int hashCode() {
        int result = r;
        result = 31 * result + g;
        result = 31 * result + b;
        result = 31 * result + a;
        return result;
    }

    @Override
    public String toString() {
        return "BasicColor{" +
                "r=" + r +
                ", g=" + g +
                ", b=" + b +
                ", a=" + a +
                '}';
    }
}