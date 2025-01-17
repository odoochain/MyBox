package mara.mybox.bufferedimage;

import java.awt.Color;
import java.awt.image.BufferedImage;
import mara.mybox.data.DoubleRectangle;
import mara.mybox.dev.MyBoxLog;

/**
 * @Author Mara
 * @CreateDate 2019-3-24 11:24:03
 * @License Apache License Version 2.0
 */
public abstract class PixelsBlend {

    public enum ImagesBlendMode {
        NORMAL,
        DISSOLVE,
        DARKEN,
        MULTIPLY,
        COLOR_BURN,
        LINEAR_BURN,
        SOFT_BURN,
        LIGHTEN,
        SCREEN,
        COLOR_DODGE,
        LINEAR_DODGE,
        SOFT_DODGE,
        DIVIDE,
        VIVID_LIGHT,
        LINEAR_LIGHT,
        SUBTRACT,
        AVERAGE,
        OVERLAY,
        HARD_LIGHT,
        SOFT_LIGHT,
        DIFFERENCE,
        NEGATION,
        EXCLUSION,
        REFLECT,
        GLOW,
        FREEZE,
        HEAT,
        STAMP,
        RED,
        GREEN,
        BLUE,
        HUE,
        SATURATION,
        COLOR,
        LUMINOSITY
    }

    protected ImagesBlendMode blendMode;
    protected float opacity;
    protected boolean orderReversed = false, ignoreTransparency = true;

    protected Color foreColor, backColor;
    protected int red, green, blue, alpha;

    public PixelsBlend() {
    }

    protected int blend(int forePixel, int backPixel) {
        if (ignoreTransparency) {
            if (forePixel == 0) {
                return backPixel;
            }
            if (backPixel == 0) {
                return forePixel;
            }
        }
        if (orderReversed) {
            foreColor = new Color(backPixel, true);
            backColor = new Color(forePixel, true);
        } else {
            foreColor = new Color(forePixel, true);
            backColor = new Color(backPixel, true);
        }
        makeRGB();
        makeAlpha();
        Color newColor = new Color(
                Math.min(Math.max(red, 0), 255),
                Math.min(Math.max(green, 0), 255),
                Math.min(Math.max(blue, 0), 255),
                Math.min(Math.max(alpha, 0), 255));
        return newColor.getRGB();
    }

    protected Color blend(Color foreColor, Color backColor) {
        if (foreColor == null) {
            return backColor;
        }
        if (backColor == null) {
            return foreColor;
        }
        int b = blend(foreColor.getRGB(), backColor.getRGB());
        return new Color(b, true);
    }

    // replace this in different blend mode. Refer to "PixelsBlendFactory"
    protected void makeRGB() {
        red = blendValues(foreColor.getRed(), backColor.getRed(), opacity);
        green = blendValues(foreColor.getGreen(), backColor.getGreen(), opacity);
        blue = blendValues(foreColor.getBlue(), backColor.getBlue(), opacity);
    }

    protected void makeAlpha() {
        alpha = (int) (foreColor.getAlpha() * opacity + backColor.getAlpha() * (1.0f - opacity));
    }

    public BufferedImage blend(BufferedImage foreImage, BufferedImage backImage, int x, int y) {
        try {
            if (foreImage == null || backImage == null) {
                return null;
            }
            int imageType = BufferedImage.TYPE_INT_ARGB;
            DoubleRectangle rect = new DoubleRectangle(x, y,
                    x + foreImage.getWidth() - 1, y + foreImage.getHeight() - 1);
            BufferedImage target = new BufferedImage(backImage.getWidth(), backImage.getHeight(), imageType);
            for (int j = 0; j < backImage.getHeight(); ++j) {
                for (int i = 0; i < backImage.getWidth(); ++i) {
                    int backPixel = backImage.getRGB(i, j);
                    if (rect.contains(i, j)) {
                        int forePixel = foreImage.getRGB(i - x, j - y);
                        target.setRGB(i, j, blend(forePixel, backPixel));
                    } else {
                        target.setRGB(i, j, backPixel);
                    }
                }
            }
            return target;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return foreImage;
        }
    }

    /*
        static
     */
    public static Color blend(Color foreColor, Color backColor, float opacity, boolean ignoreTransparency) {
        if (ignoreTransparency) {
            if (foreColor.getRGB() == 0) {
                return backColor;
            }
            if (backColor.getRGB() == 0) {
                return foreColor;
            }
        }
        return blend(foreColor, backColor, opacity);
    }

    public static Color blend(Color foreColor, Color backColor, float opacity) {
        int red = blendValues(foreColor.getRed(), backColor.getRed(), opacity);
        int green = blendValues(foreColor.getGreen(), backColor.getRed(), opacity);
        int blue = blendValues(foreColor.getBlue(), backColor.getRed(), opacity);
        int alpha = blendValues(foreColor.getAlpha(), backColor.getRed(), opacity);
        Color newColor = new Color(
                Math.min(Math.max(red, 0), 255),
                Math.min(Math.max(green, 0), 255),
                Math.min(Math.max(blue, 0), 255),
                Math.min(Math.max(alpha, 0), 255));
        return newColor;
    }

    public static int blendValues(int A, int B, float weight) {
        return (int) (A * weight + B * (1.0f - weight));
    }

    public static PixelsBlend blender(ImagesBlendMode blendMode, float opacity,
            boolean orderReversed, boolean ignoreTransparent) {
        return PixelsBlendFactory.create(blendMode)
                .setBlendMode(blendMode)
                .setOpacity(opacity)
                .setOrderReversed(orderReversed)
                .setIgnoreTransparency(ignoreTransparent);
    }

    public static BufferedImage blend(BufferedImage foreImage, BufferedImage backImage,
            int x, int y, PixelsBlend blender) {
        try {
            if (foreImage == null || backImage == null || blender == null) {
                return null;
            }
            int imageType = BufferedImage.TYPE_INT_ARGB;
            DoubleRectangle rect = new DoubleRectangle(x, y,
                    x + foreImage.getWidth() - 1, y + foreImage.getHeight() - 1);
            BufferedImage target = new BufferedImage(backImage.getWidth(), backImage.getHeight(), imageType);
            for (int j = 0; j < backImage.getHeight(); ++j) {
                for (int i = 0; i < backImage.getWidth(); ++i) {
                    int backPixel = backImage.getRGB(i, j);
                    if (rect.contains(i, j)) {
                        int forePixel = foreImage.getRGB(i - x, j - y);
                        target.setRGB(i, j, blender.blend(forePixel, backPixel));
                    } else {
                        target.setRGB(i, j, backPixel);
                    }
                }
            }
            return target;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return foreImage;
        }
    }

    /*
        get/set
     */
    public ImagesBlendMode getBlendMode() {
        return blendMode;
    }

    public PixelsBlend setBlendMode(ImagesBlendMode blendMode) {
        this.blendMode = blendMode;
        return this;
    }

    public float getOpacity() {
        return opacity;
    }

    public PixelsBlend setOpacity(float opacity) {
        this.opacity = opacity;
        return this;
    }

    public boolean isOrderReversed() {
        return orderReversed;
    }

    public PixelsBlend setOrderReversed(boolean orderReversed) {
        this.orderReversed = orderReversed;
        return this;
    }

    public boolean isIgnoreTransparency() {
        return ignoreTransparency;
    }

    public PixelsBlend setIgnoreTransparency(boolean ignoreTransparency) {
        this.ignoreTransparency = ignoreTransparency;
        return this;
    }

    public Color getForeColor() {
        return foreColor;
    }

    public void setForeColor(Color foreColor) {
        this.foreColor = foreColor;
    }

    public Color getBackColor() {
        return backColor;
    }

    public void setBackColor(Color backColor) {
        this.backColor = backColor;
    }

    public int getRed() {
        return red;
    }

    public void setRed(int red) {
        this.red = red;
    }

    public int getGreen() {
        return green;
    }

    public void setGreen(int green) {
        this.green = green;
    }

    public int getBlue() {
        return blue;
    }

    public void setBlue(int blue) {
        this.blue = blue;
    }

    public int getAlpha() {
        return alpha;
    }

    public void setAlpha(int alpha) {
        this.alpha = alpha;
    }

}
