package com.redtoast.graphics;

import net.minecraft.network.PacketByteBuf;
import org.joml.Vector2i;
import org.joml.Vector3i;

public class RGBGraphicsArray {
    public int[][] pixels;
    private int sizex, sizey;

    public RGBGraphicsArray(int sizex, int sizey) {
        this.sizex = sizex;
        this.sizey = sizey;
        pixels = new int[sizey][sizex];
    }

    public RGBGraphicsArray(int[][] arr) {
        this.sizex = arr[0].length;
        this.sizey = arr.length;
        pixels = new int[sizey][sizex];
        for (int x = 0; x < sizex; x++) {
            for (int y = 0; y < sizey; y++) {
                pixels[y][x] = arr[y][x];
            }
        }
    }

    public void substituteColor(int targetColor, int substituteColor, boolean ignoreAlpha, boolean matchAlpha) {
        int noAlphaMask = 0x00FFFFFF;
        int onlyAlphaMask = 0xFF000000;
        for (int y = 0; y < sizey; y++) {
            for (int x = 0; x < sizex; x++) {
                int color = pixels[y][x];
                if (!ignoreAlpha) {
                    if (color == targetColor) {
                        pixels[y][x] = substituteColor;
                    }
                } else {
                    if ((color & noAlphaMask) == (targetColor & noAlphaMask)) {
                        if (matchAlpha) {
                            pixels[y][x] = (substituteColor&noAlphaMask) | (color & onlyAlphaMask);
                        } else {
                            pixels[y][x] = substituteColor;
                        }
                    }
                }
            }
        }
    }

    public void makeOpaque() {
        for (int y = 0; y < sizey; y++) {
            for (int x = 0; x < sizex; x++) {
                pixels[y][x] &= 0x00FFFFFF;
            }
        }
    }

    public void makeTransparent() {
        for (int y = 0; y < sizey; y++) {
            for (int x = 0; x < sizex; x++) {
                pixels[y][x] |= 0xFF000000;
            }
        }
    }

    public int get(int x, int y) {
        if (x < 0 || y < 0)
            return 0;
        if (x >= sizex || y >= sizey)
            return 0;
        return pixels[y][x];
    }

    public void set(int x, int y, int color) {
        if (x < 0 || y < 0)
            return;
        if (x >= sizex || y >= sizey)
            return;
        pixels[y][x] = color;
    }

    public static int rgbToDecimal(int red, int green, int blue) {
        /* using formula from https://stackoverflow.com/a/18037185 */
        return (red << 16) & 0xFF0000 | (green << 8) & 0x00FF00 | blue & 0x0000FF;
    }

    public static int opacityToDecimal(int alpha) {
        return (alpha << 24) & 0xFF000000;
    }

    public static Vector3i decimalToRgb(int decimal) {
        return new Vector3i(
                (decimal & 0xFF0000) >> 16,
                (decimal & 0x00FF00) >> 8,
                (decimal & 0x0000FF));
    }

    public void writeScreenToPacketBuf(PacketByteBuf buf) {
        Vector2i size = this.getSize();
        int y = size.y();
        buf.writeInt(y);
        buf.writeInt(size.x());
        for (int i = 0; i < y; i++) {
            buf.writeIntArray(this.pixels[i]);
        }
    }

    public static RGBGraphicsArray fromPacket(PacketByteBuf buf) {
        int y = buf.readInt();
        int x = buf.readInt();
        int[][] arr = new int[y][x];
        for (int i = 0; i < y; i++) {
            arr[i] = buf.readIntArray(x);
        }
        return new RGBGraphicsArray(arr);
    }

    public Vector2i getSize() {
        return new Vector2i(sizex, sizey);
    }

    public int getAmount() {
        return sizex * sizey;
    }

    public void clear() {
        for (int y = 0; y < sizey; y++) {
            for (int x = 0; x < sizex; x++) {
                pixels[y][x] &= 0x00000000;
            }
        }
    }

    public static int blendPixel(int source, int color) {
        int a1 = (source >> 24) & 0xFF;
        int r1 = (source >> 16) & 0xFF;
        int g1 = (source >> 8) & 0xFF;
        int b1 = source & 0xFF;

        int a2 = (color >> 24) & 0xFF;
        int r2 = (color >> 16) & 0xFF;
        int g2 = (color >> 8) & 0xFF;
        int b2 = color & 0xFF;

        float alpha1 = a1 / 255f;
        float alpha2 = a2 / 255f;

        float outA = alpha2 + alpha1 * (1 - alpha2);
        if (outA <= 0)
            return 0;

        int outR = Math.round((r2 * alpha2 + r1 * alpha1 * (1 - alpha2)) / outA);
        int outG = Math.round((g2 * alpha2 + g1 * alpha1 * (1 - alpha2)) / outA);
        int outB = Math.round((b2 * alpha2 + b1 * alpha1 * (1 - alpha2)) / outA);
        int outAlpha = Math.round(outA * 255);

        return (outAlpha << 24) | (outR << 16) | (outG << 8) | outB;
    }

    public void bulkBlendPixels(int startX, int startY, int[] buffer, int bufferWidth, int bufferHeight) {
        if (buffer.length != bufferWidth * bufferHeight * 4) {
            throw new IllegalArgumentException("Buffer size mismatch");
        }

        int bufferIdx = 0;
        for (int py = 0; py < bufferHeight; py++) {
            int targetY = startY + py;
            if (targetY < 0 || targetY >= sizey) {
                bufferIdx += bufferWidth * 4;
                continue;
            }

            for (int px = 0; px < bufferWidth; px++) {
                int targetX = startX + px;

                int r = buffer[bufferIdx++];
                int g = buffer[bufferIdx++];
                int b = buffer[bufferIdx++];
                int a = buffer[bufferIdx++];

                if (targetX >= 0 && targetX < sizex) {
                    int color = (a << 24) | (r << 16) | (g << 8) | b;
                    pixels[targetY][targetX] = blendPixel(pixels[targetY][targetX], color);
                }
            }
        }
    }

    public void bulkWritePixels(int startX, int startY, int[] buffer, int bufferWidth, int bufferHeight) {
        if (buffer.length != bufferWidth * bufferHeight * 4) {
            throw new IllegalArgumentException("Buffer size mismatch");
        }

        int bufferIdx = 0;
        for (int py = 0; py < bufferHeight; py++) {
            int targetY = startY + py;
            if (targetY < 0 || targetY >= sizey) {
                bufferIdx += bufferWidth * 4;
                continue;
            }

            for (int px = 0; px < bufferWidth; px++) {
                int targetX = startX + px;

                int r = buffer[bufferIdx++];
                int g = buffer[bufferIdx++];
                int b = buffer[bufferIdx++];
                int a = buffer[bufferIdx++];

                if (targetX >= 0 && targetX < sizex) {
                    pixels[targetY][targetX] = (a << 24) | (r << 16) | (g << 8) | b;
                }
            }
        }
    }

    public void bulkBlendPixelsWithTransform(int startX, int startY, int[] buffer, int bufferWidth, int bufferHeight,
            java.util.function.BiFunction<Integer, Integer, Vector2i> rotateFunc) {
        if (buffer.length != bufferWidth * bufferHeight * 4) {
            throw new IllegalArgumentException("Buffer size mismatch");
        }

        int bufferIdx = 0;
        for (int py = 0; py < bufferHeight; py++) {
            for (int px = 0; px < bufferWidth; px++) {
                int r = buffer[bufferIdx++];
                int g = buffer[bufferIdx++];
                int b = buffer[bufferIdx++];
                int a = buffer[bufferIdx++];

                int targetX = startX + px;
                int targetY = startY + py;

                if (rotateFunc != null) {
                    Vector2i rotated = rotateFunc.apply(targetX, targetY);
                    targetX = rotated.x;
                    targetY = rotated.y;
                }

                if (targetX >= 0 && targetX < sizex && targetY >= 0 && targetY < sizey) {
                    int color = (a << 24) | (r << 16) | (g << 8) | b;
                    pixels[targetY][targetX] = blendPixel(pixels[targetY][targetX], color);
                }
            }
        }
    }
}
