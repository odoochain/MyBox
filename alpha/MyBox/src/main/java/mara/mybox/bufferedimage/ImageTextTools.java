package mara.mybox.bufferedimage;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.List;
import mara.mybox.controller.ControlImageText;
import mara.mybox.data.DoubleRectangle;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.value.Colors;

/**
 * @Author Mara
 * @CreateDate 2018-6-27 18:58:57
 * @License Apache License Version 2.0
 */
public class ImageTextTools {

    public static BufferedImage addTexts(BufferedImage source, Font font, Color color, float transparent,
            List<String> texts, List<Integer> xs, List<Integer> ys) {
        try {
            if (transparent > 1.0F || transparent < 0) {
                transparent = 1.0F;
            }
            int width = source.getWidth();
            int height = source.getHeight();
            int imageType = BufferedImage.TYPE_INT_ARGB;
            BufferedImage target = new BufferedImage(width, height, imageType);
            Graphics2D g = target.createGraphics();
            g.drawImage(source, 0, 0, width, height, null);
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, transparent));
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g.setColor(color);
            g.setFont(font);
            for (int i = 0; i < texts.size(); ++i) {
                g.drawString(texts.get(i), xs.get(i), ys.get(i));
            }
            g.dispose();
            return target;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

    public static BufferedImage addText(BufferedImage backImage, ControlImageText optionsController) {
        try {
            String text = optionsController.getText();
            if (text == null || text.isEmpty()) {
                return backImage;
            }
            float opacity = optionsController.getOpacity();
            if (opacity > 1.0F || opacity < 0) {
                opacity = 1.0F;
            }
            int width = backImage.getWidth();
            int height = backImage.getHeight();
            BufferedImage foreImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = foreImage.createGraphics();
            Color color = optionsController.getAwtColor();
            boolean noBlend = color.equals(Colors.TRANSPARENT);
            if (noBlend) {
                g.drawImage(backImage, 0, 0, width, height, null);
            } else {
                g.setBackground(Colors.TRANSPARENT);
            }
            Font font = optionsController.getFont();
            FontMetrics metrics = g.getFontMetrics(font);
            optionsController.countBaseXY(g, metrics, width, height);
            int baseX = optionsController.getBaseX();
            int baseY = optionsController.getBaseY();
            int linex = baseX, liney = baseY, lineHeight = optionsController.getLineHeight();
            String[] lines = text.split("\n", -1);
            int lend = lines.length - 1;
            int shadow = optionsController.getShadow();
            boolean isOutline = optionsController.isOutline();
            float textOpacity = noBlend ? opacity : 1.0F;
            g.rotate(Math.toRadians(optionsController.getAngle()), baseX, baseY);
            if (optionsController.isVertical()) {
                if (optionsController.isLeftToRight()) {
                    for (int r = 0; r <= lend; r++) {
                        String line = lines[r];
                        liney = baseY;
                        double wmax = 0;
                        for (int i = 0; i < line.length(); i++) {
                            String s = line.charAt(i) + "";
                            addText(g, s, font, color, linex, liney, textOpacity, shadow, isOutline);
                            Rectangle2D sBound = metrics.getStringBounds(s, g);
                            if (lineHeight > 0) {
                                liney += lineHeight;
                            } else {
                                liney += sBound.getHeight();
                            }
                            double sWidth = sBound.getWidth();
                            if (sWidth > wmax) {
                                wmax = sWidth;
                            }
                        }
                        linex += wmax;
                    }
                } else {
                    for (int r = lend; r >= 0; r--) {
                        String line = lines[r];
                        liney = baseY;
                        double wmax = 0;
                        for (int i = 0; i < line.length(); i++) {
                            String s = line.charAt(i) + "";
                            addText(g, s, font, color, linex, liney, textOpacity, shadow, isOutline);
                            Rectangle2D sBound = metrics.getStringBounds(s, g);
                            if (lineHeight > 0) {
                                liney += lineHeight;
                            } else {
                                liney += sBound.getHeight();
                            }
                            double sWidth = sBound.getWidth();
                            if (sWidth > wmax) {
                                wmax = sWidth;
                            }
                        }
                        linex += wmax;
                    }
                }

            } else {
                for (String line : lines) {
                    addText(g, line, font, color, linex, liney, textOpacity, shadow, isOutline);
                    if (lineHeight > 0) {
                        liney += lineHeight;
                    } else {
                        liney += g.getFontMetrics(font).getStringBounds(line, g).getHeight();
                    }
                }
            }
            g.dispose();
            if (noBlend) {
                return foreImage;
            } else {
                return ImageBlend.blend(foreImage, backImage, 0, 0,
                        optionsController.getBlendMode(), opacity,
                        optionsController.orderReversed(), optionsController.ignoreTransparent());
            }
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

    public static void addText(Graphics2D g, String text,
            Font font, Color color,
            int x, int y, float opacity, int shadow, boolean isOutline) {
        try {
            if (text == null || text.isEmpty()) {
                return;
            }
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opacity));
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g.setFont(font);
            g.setColor(color.equals(Colors.TRANSPARENT) ? null : color);
            if (isOutline) {
                FontRenderContext frc = g.getFontRenderContext();
                TextLayout textTl = new TextLayout(text, font, frc);
                Shape outline = textTl.getOutline(null);
                g.translate(x, y);
                g.draw(outline);
                g.translate(-x, -y);
            } else {
                g.drawString(text, x, y);
            }
            if (shadow > 0) {
                // Not blurred. Can improve
                g.setColor(Color.GRAY);
                g.drawString(text, x + shadow, y + shadow);
            }
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public static BufferedImage drawHTML2(BufferedImage backImage, BufferedImage html,
            DoubleRectangle bkRect, Color bkColor, float bkOpacity, int bkarc, int rotate, int margin) {
        try {
            if (html == null || backImage == null || bkRect == null) {
                return backImage;
            }
            int width = backImage.getWidth();
            int height = backImage.getHeight();
            int imageType = BufferedImage.TYPE_INT_ARGB;
            BufferedImage target = new BufferedImage(width, height, imageType);
            Graphics2D g = target.createGraphics();
            g.setBackground(Colors.TRANSPARENT);
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g.drawImage(backImage, 0, 0, null);
            g.rotate(Math.toRadians(rotate), bkRect.getSmallX() + bkRect.getWidth() / 2, bkRect.getSmallY() + bkRect.getHeight() / 2);
            //            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, bkOpacity));
            //            g.setColor(bkColor);
            //            g.fillRoundRect((int) bkRect.getSmallX(), (int) bkRect.getSmallY(),
            //                    (int) bkRect.getWidth(), (int) bkRect.getHeight(), bkarc, bkarc);
            //
            //            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
            g.setColor(Colors.TRANSPARENT);
            //            g.drawImage(html, (int) bkRect.getSmallX() + margin, (int) bkRect.getSmallY() + margin,
            //                    (int) bkRect.getWidth() - 2 * margin, (int) bkRect.getHeight() - 2 * margin,
            //                    null);
            g.drawImage(html, (int) bkRect.getSmallX(), (int) bkRect.getSmallY(), (int) bkRect.getWidth(), (int) bkRect.getHeight(), null);
            g.dispose();
            return target;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return backImage;
        }
    }

    public static BufferedImage drawHTML(BufferedImage backImage, BufferedImage html,
            DoubleRectangle bkRect, Color bkColor, float bkOpacity, int bkarc, int rotate, int margin) {
        try {
            if (html == null || backImage == null || bkRect == null) {
                return backImage;
            }
            int width = backImage.getWidth();
            int height = backImage.getHeight();
            int imageType = BufferedImage.TYPE_INT_ARGB;
            BufferedImage target = new BufferedImage(width, height, imageType);
            Graphics2D g = target.createGraphics();
            g.setBackground(Colors.TRANSPARENT);
            g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g.drawImage(backImage, 0, 0, null);
            g.rotate(Math.toRadians(rotate), bkRect.getSmallX() + bkRect.getWidth() / 2, bkRect.getSmallY() + bkRect.getHeight() / 2);
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, bkOpacity));
            g.setColor(bkColor);
            g.fillRoundRect((int) bkRect.getSmallX(), (int) bkRect.getSmallY(), (int) bkRect.getWidth(), (int) bkRect.getHeight(), bkarc, bkarc);
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0F));
            g.setColor(Colors.TRANSPARENT);
            g.drawImage(html, (int) bkRect.getSmallX() + margin, (int) bkRect.getSmallY() + margin, (int) bkRect.getWidth() - 2 * margin, (int) bkRect.getHeight() - 2 * margin, null);
            g.dispose();
            return target;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return backImage;
        }
    }

    public static BufferedImage drawHTML(BufferedImage backImage, BufferedImage html,
            int htmlX, int htmlY, int htmlWdith, int htmlHeight) {
        try {
            if (html == null || backImage == null) {
                return backImage;
            }
            int width = backImage.getWidth();
            int height = backImage.getHeight();
            int imageType = BufferedImage.TYPE_INT_ARGB;
            BufferedImage target = new BufferedImage(width, height, imageType);
            Graphics2D g = target.createGraphics();
            g.setBackground(Colors.TRANSPARENT);
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g.drawImage(backImage, 0, 0, null);
            //            g.rotate(Math.toRadians(rotate),
            //                    bkRect.getSmallX() + bkRect.getWidth() / 2,
            //                    bkRect.getSmallY() + bkRect.getHeight() / 2);
            //            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, bkOpacity));
            //            g.setColor(bkColor);
            //            g.fillRoundRect((int) bkRect.getSmallX(), (int) bkRect.getSmallY(),
            //                    (int) bkRect.getWidth(), (int) bkRect.getHeight(), bkarc, bkarc);
            //
            //            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
            g.setColor(Colors.TRANSPARENT);
            //            g.drawImage(html, (int) bkRect.getSmallX() + margin, (int) bkRect.getSmallY() + margin,
            //                    (int) bkRect.getWidth() - 2 * margin, (int) bkRect.getHeight() - 2 * margin,
            //                    null);
            g.drawImage(html, htmlX, htmlY, htmlWdith, htmlHeight, null);
            g.dispose();
            return target;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return backImage;
        }
    }

}
