package com.emperium.neoporiumscanner.utils;

import com.emperium.neoporiumscanner.xray.BasicColor;

public class ColorUtils {

    public static BasicColor hexToColor(String hex) {
        if (hex == null || hex.isEmpty()) {
            return new BasicColor(255, 0, 0, 255);
        }

        String cleanHex = hex.startsWith("#") ? hex.substring(1) : hex;

        try {
            if (cleanHex.length() == 6) {
                int r = Integer.parseInt(cleanHex.substring(0, 2), 16);
                int g = Integer.parseInt(cleanHex.substring(2, 4), 16);
                int b = Integer.parseInt(cleanHex.substring(4, 6), 16);
                return new BasicColor(r, g, b, 255);
            } else if (cleanHex.length() == 8) {
                int r = Integer.parseInt(cleanHex.substring(0, 2), 16);
                int g = Integer.parseInt(cleanHex.substring(2, 4), 16);
                int b = Integer.parseInt(cleanHex.substring(4, 6), 16);
                int a = Integer.parseInt(cleanHex.substring(6, 8), 16);
                return new BasicColor(r, g, b, a);
            }
        } catch (NumberFormatException e) {
            // Invalid hex format
        }

        return new BasicColor(255, 0, 0, 255);
    }

    public static String colorToHex(BasicColor color) {
        return String.format("#%02X%02X%02X%02X",
                color.r(), color.g(), color.b(), color.a());
    }

    public static String colorToHexRGB(BasicColor color) {
        return String.format("#%02X%02X%02X",
                color.r(), color.g(), color.b());
    }

    public static BasicColor blendColors(BasicColor color1, BasicColor color2, float ratio) {
        float r = color1.r() * (1 - ratio) + color2.r() * ratio;
        float g = color1.g() * (1 - ratio) + color2.g() * ratio;
        float b = color1.b() * (1 - ratio) + color2.b() * ratio;
        float a = color1.a() * (1 - ratio) + color2.a() * ratio;

        return new BasicColor((int)r, (int)g, (int)b, (int)a);
    }

    public static BasicColor adjustBrightness(BasicColor color, float factor) {
        int r = clamp((int)(color.r() * factor), 0, 255);
        int g = clamp((int)(color.g() * factor), 0, 255);
        int b = clamp((int)(color.b() * factor), 0, 255);

        return new BasicColor(r, g, b, color.a());
    }

    public static BasicColor withAlpha(BasicColor color, int alpha) {
        return new BasicColor(color.r(), color.g(), color.b(), clamp(alpha, 0, 255));
    }

    public static BasicColor getContrastColor(BasicColor color) {
        // Calculate relative luminance (perceived brightness)
        double luminance = (0.299 * color.r() + 0.587 * color.g() + 0.114 * color.b()) / 255;

        // Return black for light backgrounds, white for dark backgrounds
        return luminance > 0.5 ? new BasicColor(0, 0, 0, 255) : new BasicColor(255, 255, 255, 255);
    }

    public static BasicColor getRainbowColor(float progress) {
        // progress should be between 0 and 1
        float hue = progress * 360;
        return hsvToRgb(hue, 1.0f, 1.0f);
    }

    public static BasicColor hsvToRgb(float h, float s, float v) {
        h = h % 360;
        float c = v * s;
        float x = c * (1 - Math.abs((h / 60) % 2 - 1));
        float m = v - c;

        float r, g, b;

        if (h < 60) {
            r = c; g = x; b = 0;
        } else if (h < 120) {
            r = x; g = c; b = 0;
        } else if (h < 180) {
            r = 0; g = c; b = x;
        } else if (h < 240) {
            r = 0; g = x; b = c;
        } else if (h < 300) {
            r = x; g = 0; b = c;
        } else {
            r = c; g = 0; b = x;
        }

        return new BasicColor(
                (int)((r + m) * 255),
                (int)((g + m) * 255),
                (int)((b + m) * 255),
                255
        );
    }

    public static BasicColor getColorForBlockType(String blockType) {
        // Default colors for common block types
        if (blockType.contains("diamond")) {
            return new BasicColor(0, 200, 255, 200);
        } else if (blockType.contains("gold")) {
            return new BasicColor(255, 215, 0, 200);
        } else if (blockType.contains("iron")) {
            return new BasicColor(200, 200, 200, 200);
        } else if (blockType.contains("emerald")) {
            return new BasicColor(0, 200, 0, 200);
        } else if (blockType.contains("redstone")) {
            return new BasicColor(255, 0, 0, 200);
        } else if (blockType.contains("lapis")) {
            return new BasicColor(0, 100, 200, 200);
        } else if (blockType.contains("coal")) {
            return new BasicColor(50, 50, 50, 200);
        } else if (blockType.contains("copper")) {
            return new BasicColor(184, 115, 51, 200);
        } else if (blockType.contains("chest")) {
            return new BasicColor(200, 150, 0, 200);
        } else if (blockType.contains("spawner")) {
            return new BasicColor(150, 0, 150, 200);
        } else {
            return new BasicColor(255, 165, 0, 200); // Orange
        }
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }
}