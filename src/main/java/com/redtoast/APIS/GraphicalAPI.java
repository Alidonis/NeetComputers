package com.redtoast.APIS;

import com.redtoast.graphics.FloodFillArray;
import com.redtoast.graphics.RGBAGraphicsArray;
import com.redtoast.graphics.RGBGraphicsArray;
import com.redtoast.simulation.APILoader;
import com.redtoast.simulation.Runtime;
import com.redtoast.simulation.annotations.Exposed;
import com.redtoast.simulation.annotations.Index;
import com.redtoast.simulation.annotations.Range;
import com.redtoast.simulation.base.Exposable;
import com.redtoast.simulation.base.ExposedError;
import com.redtoast.simulation.base.LangThread;
import com.redtoast.simulation.parameter.FunctionInput;
import com.redtoast.simulation.value.Value;
import com.redtoast.simulation.value.ValueTypes.Function;
import com.redtoast.simulation.value.ValueTypes.List;
import com.redtoast.simulation.value.ValueTypes.Table;
import com.redtoast.simulation.value.ValueTypes.Tuple;
import com.redtoast.simulation.value.VarType;
import org.joml.Vector2i;
import org.joml.Vector3i;

import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

public class GraphicalAPI implements Exposable {

    private Runtime runtime;
    protected final int height;
    protected final int width;
    protected int alpha = 255;
    protected RGBGraphicsArray Graphics;
    protected RGBGraphicsArray GraphicsBuffer;
    protected BadVector defualtColor = new BadVector(0, 0, 0);
    protected double angle = 0;
    protected Vector2i rotatePos = null;
    protected boolean roobj = false;

    private double cachedCos = 1.0;
    private double cachedSin = 0.0;
    private double lastAngle = 0.0;

    public static class BadVector {
        public int x, y, z;

        public BadVector(int X, int Y) {
            x = X;
            y = Y;
            z = 0;
        }

        public BadVector(int X, int Y, int Z) {
            x = X;
            y = Y;
            z = Z;
        }

        public BadVector(int[] point) {
            x = point[0];
            y = point[1];
            z = (point.length > 2) ? point[2] : 0;
        }

        public double distance(BadVector point) {
            int dx = point.x - x;
            int dy = point.y - y;
            return Math.sqrt(dx * dx + dy * dy);
        }

        public BadVector add(BadVector point) {
            return new BadVector(x + point.x, y + point.y);
        }
    }

    public GraphicalAPI(RGBGraphicsArray graphics, Runtime runtime) {
        Graphics = graphics;
        Vector2i size = graphics.getSize();
        height = size.y;
        width = size.x;
        GraphicsBuffer = new RGBGraphicsArray(graphics.pixels);
        GraphicsBuffer.makeOpaque();
        setColor(255, 255, 255);
        this.runtime = runtime;
    }

    @Override
    public void onCall(Runtime runtime, Method method) {
        this.runtime = runtime;
    }

    @Exposed
    public void setRotation(double angle, int x, int y) {
        this.angle = angle;
        rotatePos = new Vector2i(x, y);
        roobj = false;
        updateTrigCache();
    }

    @Exposed
    public void setRotation(double angle) {
        this.angle = angle;
        rotatePos = null;
        roobj = true;
        updateTrigCache();
    }

    @Exposed
    public void setRotation() {
        rotatePos = null;
        roobj = false;
        angle = 0;
        cachedCos = 1.0;
        cachedSin = 0.0;
        lastAngle = 0.0;
    }

    private void updateTrigCache() {
        if (angle != lastAngle) {
            double radians = Math.toRadians(angle);
            cachedCos = Math.cos(radians);
            cachedSin = Math.sin(radians);
            lastAngle = angle;
        }
    }

    public void startRotationSession(Vector2i pos) {
        if (roobj)
            rotatePos = pos;
    }

    public void startRotationSession(int x, int y) {
        startRotationSession(new Vector2i(x, y));
    }

    public void startRotationSession(BadVector pos) {
        startRotationSession(pos.x, pos.y);
    }

    public void endRotationSession() {
        if (roobj)
            setRotation();
    }

    public Vector2i rotateLocal(int x, int y) {
        if (rotatePos == null || angle == 0) {
            return new Vector2i(x, y);
        }

        int x1 = x - rotatePos.x;
        int y1 = y - rotatePos.y;
        int x2 = (int) Math.round(cachedCos * x1 - cachedSin * y1);
        int y2 = (int) Math.round(cachedSin * x1 + cachedCos * y1);
        return new Vector2i(x2 + rotatePos.x, y2 + rotatePos.y);
    }

    public Vector2i rotate(Vector2i vec, Vector2i base, double angle) {
        int x1 = vec.x - base.x;
        int y1 = vec.y - base.y;
        double b = Math.toRadians(angle);
        double x2 = Math.cos(b) * x1 - Math.sin(b) * y1;
        double y2 = Math.sin(b) * x1 + Math.cos(b) * y1;
        return new Vector2i(
                (int) Math.round(x2) + base.x,
                (int) Math.round(y2) + base.y);
    }

    @Exposed
    public void setColor(@Index(strict = true, offset = -1) @Range(range = 256) int R,
            @Index(strict = true, offset = -1) @Range(range = 256) int G,
            @Index(strict = true, offset = -1) @Range(range = 256) int B) {
        defualtColor = new BadVector(R, G, B);
    }

    @Exposed
    public void setColor(@Index(strict = true, offset = -1) @Range(range = 256) int R,
            @Index(strict = true, offset = -1) @Range(range = 256) int G,
            @Index(strict = true, offset = -1) @Range(range = 256) int B,
            @Index(strict = true, offset = -1) @Range(range = 256) int A) {
        setColor(R, G, B);
        alpha = A;
    }

    @Exposed
    public void setColor(@Index(strict = true, offset = -1) @Range(range = 256) int A) {
        alpha = A;
    }

    @Exposed
    public void substituteColor(@Index(strict = true, offset = -1) @Range(range = 256) int Rt,
                                @Index(strict = true, offset = -1) @Range(range = 256) int Gt,
                                @Index(strict = true, offset = -1) @Range(range = 256) int Bt,
                                @Index(strict = true, offset = -1) @Range(range = 256) int Rs,
                                @Index(strict = true, offset = -1) @Range(range = 256) int Gs,
                                @Index(strict = true, offset = -1) @Range(range = 256) int Bs) {
        int targetColor = (255<<24) | (Rt << 16) | (Gt << 8) | Bt;
        int substituteColor = (255<<24) | (Rs << 16) | (Gs << 8) | Bs;
        GraphicsBuffer.substituteColor(targetColor,substituteColor,true,true);
    }


    @Exposed
    public void substituteColor(@Index(strict = true, offset = -1) @Range(range = 256) int Rt,
                                @Index(strict = true, offset = -1) @Range(range = 256) int Gt,
                                @Index(strict = true, offset = -1) @Range(range = 256) int Bt,
                                @Index(strict = true, offset = -1) @Range(range = 256) int At,
                                @Index(strict = true, offset = -1) @Range(range = 256) int Rs,
                                @Index(strict = true, offset = -1) @Range(range = 256) int Gs,
                                @Index(strict = true, offset = -1) @Range(range = 256) int Bs,
                                @Index(strict = true, offset = -1) @Range(range = 256) int As) {
        int targetColor = (At << 24) | (Rt << 16) | (Gt << 8) | Bt;
        int substituteColor = (As << 24) | (Rs << 16) | (Gs << 8) | Bs;
        GraphicsBuffer.substituteColor(targetColor,substituteColor,false,false);
    }

    public void setColor(BadVector color) {
        setColor(color.x, color.y, color.z);
    }

    @Exposed
    public void drawPixel(@Index int x, @Index int y) {
        drawPixel(x, y, defualtColor);
    }

    protected void rawDrawPixel(int x, int y, int R, int G, int B) {
        int color = (alpha << 24) | (R << 16) | (G << 8) | B;
        GraphicsBuffer.set(x, y, RGBGraphicsArray.blendPixel(GraphicsBuffer.get(x, y), color));
    }

    @Exposed
    public void drawPixel(@Index int x, @Index int y,
            @Index(strict = true, offset = -1) @Range(range = 256) int R,
            @Index(strict = true, offset = -1) @Range(range = 256) int G,
            @Index(strict = true, offset = -1) @Range(range = 256) int B) {
        Vector2i pos = rotateLocal(x, y);
        rawDrawPixel(pos.x, pos.y, R, G, B);
    }

    public void drawPixel(BadVector point) {
        drawPixel(point.x, point.y);
    }

    public void drawPixel(BadVector point, int R, int G, int B) {
        drawPixel(point.x, point.y, R, G, B);
    }

    public void drawPixel(BadVector point, BadVector color) {
        drawPixel(point.x, point.y, color.x, color.y, color.z);
    }

    public void drawPixel(int x, int y, BadVector color) {
        drawPixel(x, y, color.x, color.y, color.z);
    }

    @Exposed
    public void drawLine(@Index int x1, @Index int y1, int x2, int y2) {
        drawLine(x1, height - y1, x2, height - y2, defualtColor.x, defualtColor.y, defualtColor.z);
    }

    @Exposed
    public void drawLine(@Index int x0, @Index int y0, @Index int x1, @Index int y1,
            @Index(strict = true, offset = -1) @Range(range = 256) int R,
            @Index(strict = true, offset = -1) @Range(range = 256) int G,
            @Index(strict = true, offset = -1) @Range(range = 256) int B) {
        Vector2i p1 = rotateLocal(x0, y0);
        Vector2i p2 = rotateLocal(x1, y1);
        int dx = Math.abs(p2.x - p1.x);
        int dy = Math.abs(p2.y - p1.y);
        int sx = p1.x < p2.x ? 1 : -1;
        int sy = p1.y < p2.y ? 1 : -1;
        int err = dx - dy;

        while (true) {
            rawDrawPixel(p1.x, p1.y, R, G, B);

            if (p1.x == p2.x && p1.y == p2.y)
                break;

            int e2 = 2 * err;
            if (e2 > -dy) {
                err = err - dy;
                p1.x = p1.x + sx;
            }
            if (e2 < dx) {
                err = err + dx;
                p1.y = p1.y + sy;
            }
        }
    }

    public void drawLine(BadVector pointA, BadVector pointB) {
        drawLine(pointA.x, pointA.y, pointB.x, pointB.y);
    }

    public void drawLine(BadVector pointA, BadVector pointB, int R, int G, int B) {
        drawLine(pointA.x, pointA.y, pointB.x, pointB.y, R, G, B);
    }

    public void drawLine(BadVector pointA, BadVector pointB, BadVector color) {
        drawLine(pointA.x, pointA.y, pointB.x, pointB.y, color.x, color.y, color.z);
    }

    public void drawLine(int x1, int y1, int x2, int y2, BadVector color) {
        drawLine(x1, y1, x2, y2, color.x, color.y, color.z);
    }

    @Exposed
    public void drawLayer(@Index int x, @Index int y, Table layer) {
        AtomicReference<Function> function = new AtomicReference<>();
        layer.foreach((key, value) -> {
            if (key.toString() != null && key.toString().equals("getAsArray")) {
                if (value.instanceOf(VarType.FUNCTION)) {
                    function.set(value.toFunction());
                } else {
                    throw new ExposedError("Table provided not valid layer");
                }
            }
        });
        if (function.get() == null) throw new ExposedError("Invalid layer (no data)");
        if (function.get().isUserGenerated()) throw new ExposedError("Invalid layer (invalid data)");
        Value value;
        try{
            value = function.get().invoke(new FunctionInput(new LinkedList<>()));
        }catch (Throwable ignored) {
            throw new ExposedError("Invalid layer (data retrieval failure)");
        }
        if (!value.instanceOf(VarType.LIST)) throw new ExposedError("Invalid layer (invalid data)");
        int[][] pixels = Layer.translateArray(value.toList());
        Vector2i size = new Vector2i(pixels[0].length, pixels.length);
        for (int x2 = 0; x2 < size.x; x2++) {
            for (int y2 = 0; y2 < size.y; y2++) {
                rawDrawPixel(x + x2, y + y2, (pixels[y2][x2] & 0xFF0000) >> 16, (pixels[y2][x2] & 0x00FF00) >> 8, (pixels[y2][x2] & 0x0000FF));
            }
        }
    }

    @Exposed
    public void drawLayer(@Index int x, @Index int y,@Index int x0,@Index int y0,@Index int x1,@Index int y1, Table layer) {
        AtomicReference<Function> function = new AtomicReference<>();
        layer.foreach((key, value) -> {
            if (key.toString() != null && key.toString().equals("getAsArray")) {
                if (value.instanceOf(VarType.FUNCTION)) {
                    function.set(value.toFunction());
                } else {
                    throw new ExposedError("Table provided not valid layer");
                }
            }
        });
        if (function.get() == null) throw new ExposedError("Invalid layer (no data)");
        if (function.get().isUserGenerated()) throw new ExposedError("Invalid layer (invalid data)");
        Value value;
        try{
            value = function.get().invoke(new FunctionInput(new LinkedList<>()));
        }catch (Throwable ignored) {
            throw new ExposedError("Invalid layer (data retrieval failure)");
        }
        if (!value.instanceOf(VarType.LIST)) throw new ExposedError("Invalid layer (invalid data)");
        int[][] pixels = Layer.translateArray(value.toList());
        Vector2i size = new Vector2i(pixels[0].length, pixels.length);
        int x2 = 0;
        x1 = Math.clamp(x1,0,size.x);
        x0 = Math.clamp(x0,0,size.x);
        y1 = Math.clamp(y1,0,size.y);
        y0 = Math.clamp(y0,0,size.y);
        int signx = x1-x0==0 ? 1 : (x1-x0)/Math.abs(x1-x0);
        int signy = y1-y0==0 ? 1 : (y1-y0)/Math.abs(y1-y0);
        for (int sourcex = x0; sourcex != x1; sourcex+=signx) {
            int y2 = 0;
            for (int sourcey = y0; sourcey != y1; sourcey+=signy) {
                Vector3i colors = RGBGraphicsArray.decimalToRgb(pixels[sourcey][sourcex]);
                rawDrawPixel(x + x2, y + y2, colors.x, colors.y, colors.z);
                y2++;
            }
            x2++;
        }
    }

    @Exposed
    public Table copy(@Index int x1, @Index int y1, int x2, int y2) {
        int sizex = Math.max(x1, x2) - Math.min(x1, x2);
        int sizey = Math.max(y1, y2) - Math.min(y1, y2);
        if (sizex == 0 || sizey == 0)
            throw new ExposedError("Copy range has a dimension of 0 size");
        Vector2i size = GraphicsBuffer.getSize();
        Layer layer = new Layer(new RGBAGraphicsArray(sizex, sizey, true), runtime);
        for (int x = 0; x < sizex; x++) {
            for (int y = 0; y < sizey; y++) {
                int absoluteX = Math.min(x1, x2) + x;
                int absoluteY = Math.min(y1, y2) + y;
                if (!((absoluteX < 0 || absoluteY < 0) || (absoluteX >= size.x || absoluteY >= size.y))) {
                    layer.GraphicsBuffer.set(x, y, GraphicsBuffer.get(absoluteX, absoluteY));
                }
            }
        }
        return APILoader.TableizeAPI(layer, runtime);
    }

    public static double colorDistance(int R1, int G1, int B1, int R2, int G2, int B2) {
        int R = R1 - R2;
        int G = G1 - G2;
        int B = B1 - B2;
        return (double) (R + G + B) / 3;
    }

    @Exposed
    public void floodFill(int x, int y, @Index(strict = true, offset = -1) @Range(range = 256) int tolerance,
            @Index(strict = true, offset = -1) @Range(range = 256) int R,
            @Index(strict = true, offset = -1) @Range(range = 256) int G,
            @Index(strict = true, offset = -1) @Range(range = 256) int B) {
        if (x < 0 || x >= width)
            throw new ExposedError("Argument #0: value not in range [0-" + width + ']');
        if (y < 0 || y >= height)
            throw new ExposedError("Argument #1: value not in range [0-" + height + ']');
        if (tolerance == 255) {
            fill(R, G, B);
            return;
        }
        Vector3i baseColors = RGBGraphicsArray.decimalToRgb(GraphicsBuffer.get(x, y));
        FloodFillArray FFA = new FloodFillArray(width, height) {
            @Override
            public void setPixel(int x, int y) {
                rawDrawPixel(x, y, R, G, B);
            }

            @Override
            public boolean isValid(int x, int y) {
                Vector3i colors = RGBGraphicsArray.decimalToRgb(GraphicsBuffer.get(x, y));
                if (tolerance == 0) {
                    if (colors.x != baseColors.x)
                        return false;
                    if (colors.y != baseColors.y)
                        return false;
                    return colors.z == baseColors.z;
                } else {
                    return colorDistance(colors.x, colors.y, colors.z, baseColors.x, baseColors.y,
                            baseColors.z) <= tolerance;
                }
            }
        };
        FFA.start(x, y);
    }

    @Exposed
    public void floodFill(int x, int y, @Index( strict = true, offset=-1 ) @Range( range = 256 ) int tolerance){
        floodFill(x, y, tolerance, defualtColor.x, defualtColor.y, defualtColor.z);
    }

    @Exposed
    public void floodFill(int x, int y){
        floodFill(x, y, 0, defualtColor.x, defualtColor.y, defualtColor.z);
    }

    @Exposed
    public void floodFill(int x, int y,@Index( strict = true, offset=-1 ) @Range( range = 256 ) int R,@Index( strict = true, offset=-1 ) @Range( range = 256 ) int G,@Index( strict = true, offset=-1 ) @Range( range = 256 ) int B){
        floodFill(x, y, 0, R, G, B);
    }

    @Exposed
    public void fill(@Index(strict = true, offset = -1) @Range(range = 256) int R,
            @Index(strict = true, offset = -1) @Range(range = 256) int G,
            @Index(strict = true, offset = -1) @Range(range = 256) int B) {
        for (int w = 0; w < width; w++) {
            for (int h = 0; h < height; h++) {
                rawDrawPixel(w, h, R, G, B);
            }
        }
    }

    @Exposed
    public void fill(@Index int x1, @Index int y1, int x2, int y2,
            @Index(strict = true, offset = -1) @Range(range = 256) int R,
            @Index(strict = true, offset = -1) @Range(range = 256) int G,
            @Index(strict = true, offset = -1) @Range(range = 256) int B) {
        for (int w = Math.min(x1, x2); w < Math.max(x1, x2); w++) {
            for (int h = Math.min(y1, y2); h < Math.max(y1, y2); h++) {
                rawDrawPixel(w, h, R, G, B);
            }
        }
    }

    @Exposed
    public void fill(@Index int x1, @Index int y1, int x2, int y2) {
        fill(x1, y1, x2, y2, defualtColor.x, defualtColor.y, defualtColor.z);
    }

    @Exposed
    public void fill(){
        fill(defualtColor.x, defualtColor.y, defualtColor.z);
    }

    public int average(int... nums){
        int toltal = 0;
        for (int i = 0; i < nums.length; i++) {
            toltal += nums[i];
        }
        return toltal / nums.length;
    }

    public Vector2i average(Vector2i... nums) {
        int toltalX = 0;
        int toltalY = 0;
        for (int i = 0; i < nums.length; i++) {
            toltalX += nums[i].x;
            toltalY += nums[i].y;
        }
        return new Vector2i(toltalX / nums.length, toltalY / nums.length);
    }

    public BadVector average(BadVector... nums) {
        int toltalX = 0;
        int toltalY = 0;
        for (int i = 0; i < nums.length; i++) {
            toltalX += nums[i].x;
            toltalY += nums[i].y;
        }
        return new BadVector(toltalX / nums.length, toltalY / nums.length);
    }

    @Exposed
    public void drawRectangle(@Index int x1, @Index int y1, int x2, int y2,
            @Index(strict = true, offset = -1) @Range(range = 256) int R,
            @Index(strict = true, offset = -1) @Range(range = 256) int G,
            @Index(strict = true, offset = -1) @Range(range = 256) int B) {
        startRotationSession(average(x1, x2), average(y1, y2));
        drawLine(x1, y1, x2, y1, R, G, B);
        drawLine(x2, y1, x2, y2, R, G, B);
        drawLine(x2, y2, x1, y2, R, G, B);
        drawLine(x1, y2, x1, y1, R, G, B);
        endRotationSession();
    }

    @Exposed
    public void drawRectangle(@Index int x1, @Index int y1, int x2, int y2) {
        startRotationSession(average(x1, x2), average(y1, y2));
        drawLine(x1, y1, x2, y1);
        drawLine(x2, y1, x2, y2);
        drawLine(x2, y2, x1, y2);
        drawLine(x1, y2, x1, y1);
        endRotationSession();
    }

    public void drawRectangle(BadVector pointA, BadVector pointB) {
        drawRectangle(pointA.x, pointA.y, pointB.x, pointB.y);
    }

    public void drawRectangle(BadVector pointA, BadVector pointB, int R, int G, int B) {
        drawRectangle(pointA.x, pointA.y, pointB.x, pointB.y, R, G, B);
    }

    public void drawRectangle(BadVector pointA, BadVector pointB, BadVector color) {
        drawRectangle(pointA.x, pointA.y, pointB.x, pointB.y, color.x, color.y, color.z);
    }

    public void drawRectangle(int x1, int y1, int x2, int y2, BadVector color) {
        drawRectangle(x1, y1, x2, y2, color.x, color.y, color.z);
    }

    public void drawBezier(BadVector pointA, BadVector pointB, BadVector pointC, BadVector color) {
        startRotationSession(average(pointA, pointB, pointC));
        int samples = Math.round((int) (pointA.distance(pointC) / 3));
        int[] Xs = new int[samples + 1];
        int[] Ys = new int[samples + 1];
        Xs[0] = pointA.x;
        Ys[0] = pointA.y;
        Xs[samples] = pointC.x;
        Ys[samples] = pointC.y;
        for (int i = 1; i < samples; i++) {
            BadVector point = bezier(pointA, pointB, pointC, (double) i / (double) samples);
            Xs[i] = point.x;
            Ys[i] = point.y;
        }
        for (int i = 0; i < samples; i++) {
            drawLine(Xs[i], Ys[i], Xs[i + 1], Ys[i + 1], color);
        }
        endRotationSession();
    }

    public void drawBezier(BadVector pointA, BadVector pointB, BadVector pointC) {
        drawBezier(pointA, pointB, pointC, defualtColor);
    }

    @Exposed
    public void drawBezier(@Index int x1, @Index int y1, @Index int x2, @Index int y2, @Index int x3, @Index int y3) {
        drawBezier(new BadVector(x1, y1), new BadVector(x2, y2), new BadVector(x3, y3));
    }

    public void drawBezier(BadVector pointA, BadVector pointB, BadVector pointC, int R, int G, int B) {
        drawBezier(pointA, pointB, pointC, new BadVector(R, G, B));
    }

    public void drawBezier(int x1, int y1, int x2, int y2, int x3, int y3, BadVector color) {
        drawBezier(new BadVector(x1, y1), new BadVector(x2, y2), new BadVector(x3, y3), color);
    }

    @Exposed
    public void drawBezier(@Index int x1, @Index int y1, @Index int x2, @Index int y2, @Index int x3, @Index int y3,
            @Index(strict = true, offset = -1) @Range(range = 256) int R,
            @Index(strict = true, offset = -1) @Range(range = 256) int G,
            @Index(strict = true, offset = -1) @Range(range = 256) int B) {
        drawBezier(new BadVector(x1, y1), new BadVector(x2, y2), new BadVector(x3, y3), new BadVector(R, G, B));
    }

    private BadVector interlopeDistance(BadVector pointA, BadVector pointB, double T) {
        int[] endPoint = new int[2];
        endPoint[0] = (int) Math.round(T * (pointB.x - pointA.x)) + pointA.x;
        endPoint[1] = (int) Math.round(T * (pointB.y - pointA.y)) + pointA.y;
        return new BadVector(endPoint);
    }

    private BadVector bezier(BadVector pointA, BadVector pointB, BadVector pointC, double T) {
        BadVector pointAB = interlopeDistance(pointA, pointB, T);
        BadVector pointBC = interlopeDistance(pointB, pointC, T);
        return interlopeDistance(pointAB, pointBC, T);
    }

    public void drawSpline(BadVector[] points, BadVector color) {
        startRotationSession(average(points));
        int samples = 0;
        for (int i = 0; i < points.length - 1; i++) {
            samples += (int) Math.sqrt(Math.pow(Math.abs(points[i].x - points[i + 1].x), 2)
                    + Math.pow(Math.abs(points[i].y - points[i + 1].y), 2));
        }
        samples /= 5;
        BadVector[] draw = new BadVector[samples];
        for (int i = 0; i < samples; i++) {
            draw[i] = spline(points, (double) i / (double) samples);
        }
        for (int i = 0; i < samples - 1; i++) {
            drawLine(draw[i], draw[i + 1], color);
        }
        endRotationSession();
    }

    public void drawSpline(BadVector[] points) {
        drawSpline(points, defualtColor);
    }

    public void drawSpline(BadVector[] points, int R, int G, int B) {
        drawSpline(points, new BadVector(R, G, B));
    }

    public void drawSpline(Integer[] Xs, Integer[] Ys, BadVector color) {
        BadVector[] vecs = new BadVector[Math.min(Xs.length, Ys.length)];
        for (int i = 0; i < Math.min(Xs.length, Ys.length); i++) {
            vecs[i] = new BadVector(Xs[i], Ys[i]);
        }
        drawSpline(vecs, color);
    }

    @Exposed
    public void drawSpline(List Xs, List Ys) {
        if (!Xs.check((val) -> val.instanceOf(VarType.NUMBER)))
            throw new ExposedError("Argument #1: all vals in list must be numbers");
        if (!Ys.check((val) -> val.instanceOf(VarType.NUMBER)))
            throw new ExposedError("Argument #2: all vals in list must be numbers");
        if (Xs.size() != Ys.size())
            throw new ExposedError("drawSpline called with balance of X and Y values");
        drawSpline(Xs.cast((val) -> val.toInt()).toArray(new Integer[] {}),
                Ys.cast((val) -> val.toInt()).toArray(new Integer[] {}), defualtColor);
    }

    @Exposed
    public void drawSpline(List Xs, List Ys,
            @Index(strict = true, offset = -1) @Range(range = 256) int R,
            @Index(strict = true, offset = -1) @Range(range = 256) int G,
            @Index(strict = true, offset = -1) @Range(range = 256) int B) {
        if (!Xs.check((val) -> val.instanceOf(VarType.NUMBER)))
            throw new ExposedError("Argument #1: all vals in list must be numbers");
        if (!Ys.check((val) -> val.instanceOf(VarType.NUMBER)))
            throw new ExposedError("Argument #2: all vals in list must be numbers");
        if (Xs.size() != Ys.size())
            throw new ExposedError("drawSpline called with balance of X and Y values");
        drawSpline(Xs.cast((val) -> val.toInt()).toArray(new Integer[] {}),
                Ys.cast((val) -> val.toInt()).toArray(new Integer[] {}), new BadVector(R, G, B));
    }

    public void drawSpline(Integer[] cords, BadVector color) {
        BadVector[] vecs = new BadVector[cords.length / 2 * 2];
        for (int i = 0; i < cords.length / 2; i++) {
            vecs[i] = new BadVector(cords[i * 2], cords[i * 2 + 1]);
        }
        drawSpline(vecs, color);
    }

    @Exposed
    public void drawSpline(List cords) {
        if (!cords.check((val) -> val.instanceOf(VarType.NUMBER)))
            throw new ExposedError("Argument #1: all vals in list must be numbers");
        if (cords.size() % 2 == 1)
            throw new ExposedError(
                    "Argument #1: list input contained a uneven amount of values (not valid list of coordinates)");
        drawSpline(cords.cast((val) -> val.toInt()).toArray(new Integer[] {}), defualtColor);
    }

    @Exposed
    public void drawSpline(List cords,
            @Index(strict = true, offset = -1) @Range(range = 256) int R,
            @Index(strict = true, offset = -1) @Range(range = 256) int G,
            @Index(strict = true, offset = -1) @Range(range = 256) int B) {
        if (!cords.check((val) -> val.instanceOf(VarType.NUMBER)))
            throw new ExposedError("Argument #1: all vals in list must be numbers");
        if (cords.size() % 2 == 1)
            throw new ExposedError(
                    "Argument #1: list input contained a uneven amount of values (not valid list of coordinates)");
        drawSpline(cords.cast((val) -> val.toInt()).toArray(new Integer[] {}), new BadVector(R, G, B));
    }

    public BadVector spline(BadVector[] points, double T) {
        BadVector[] vecs = points;
        while (vecs.length > 1) {
            BadVector[] newVec = new BadVector[vecs.length - 1];
            for (int i = 0; i < vecs.length - 1; i++) {
                newVec[i] = interlopeDistance(vecs[i], vecs[i + 1], T);
            }
            vecs = newVec;
        }
        return vecs[0];
    }

    public void drawCircle(BadVector pointA, BadVector pointB, BadVector color) {
        startRotationSession(average(pointA, pointB));
        double radius = Math.abs(pointB.y - pointA.y) / 2d;
        double scaleX = (double) Math.abs(pointB.x - pointA.x) / Math.abs(pointB.y - pointA.y);
        BadVector point = new BadVector((pointA.x + pointB.x) / 2, (pointA.y + pointB.y) / 2);
        int samples = (int) Math.round(Math.PI * radius / 6d);
        int[] Xs = new int[samples + 1];
        int[] Ys = new int[samples + 1];
        for (int i = 0; i <= samples; i++) {
            double angle = Math.toRadians(90d * (double) i / (double) samples);
            Ys[i] = (int) (Math.sin(angle) * radius);
            Xs[i] = (int) (Math.cos(angle) * radius * scaleX);
        }
        for (int i = 0; i < samples; i++) {
            drawLine(Xs[i] + point.x, Ys[i] + point.y, Xs[i + 1] + point.x, Ys[i + 1] + point.y, color);
            drawLine(-Xs[i] + point.x, Ys[i] + point.y, -Xs[i + 1] + point.x, Ys[i + 1] + point.y, color);
            drawLine(-Xs[i] + point.x, -Ys[i] + point.y, -Xs[i + 1] + point.x, -Ys[i + 1] + point.y, color);
            drawLine(Xs[i] + point.x, -Ys[i] + point.y, Xs[i + 1] + point.x, -Ys[i + 1] + point.y, color);
        }
        endRotationSession();
    }

    @Exposed
    public void drawCircle(@Index int x1, @Index int y1, int x2, int y2) {
        drawCircle(new BadVector(x1, y1), new BadVector(x2, y2), defualtColor);
    }

    @Exposed
    public void drawCircle(@Index int x1, @Index int y1, int x2, int y2,
            @Index(strict = true, offset = -1) @Range(range = 256) int R,
            @Index(strict = true, offset = -1) @Range(range = 256) int G,
            @Index(strict = true, offset = -1) @Range(range = 256) int B) {
        drawCircle(new BadVector(x1, y1), new BadVector(x2, y2), new BadVector(R, G, B));
    }

    public void drawCircle(BadVector pointA, BadVector pointB) {
        drawCircle(pointA.x, pointA.y, pointB.x, pointB.y);
    }

    public void drawCircle(BadVector pointA, BadVector pointB, int R, int G, int B) {
        drawCircle(pointA.x, pointA.y, pointB.x, pointB.y, R, G, B);
    }

    public void drawCircle(int x1, int y1, int x2, int y2, BadVector color) {
        drawCircle(x1, y1, x2, y2, color.x, color.y, color.z);
    }

    public void drawCircle(BadVector point, int radius, BadVector color) {
        startRotationSession(point);
        int samples = (int) Math.round(Math.PI * (double) radius / 6d);
        int[] Xs = new int[samples + 1];
        int[] Ys = new int[samples + 1];
        for (int i = 0; i <= samples; i++) {
            double angle = Math.toRadians(90d * (double) i / (double) samples);
            Ys[i] = (int) (Math.sin(angle) * (double) radius);
            Xs[i] = (int) (Math.cos(angle) * (double) radius);
        }
        for (int i = 0; i < samples; i++) {
            drawLine(Xs[i] + point.x + 1, Ys[i] + point.y + 1, Xs[i + 1] + point.x + 1, Ys[i + 1] + point.y + 1, color);
            drawLine(-Xs[i] + point.x + 1, Ys[i] + point.y + 1, -Xs[i + 1] + point.x + 1, Ys[i + 1] + point.y + 1,
                    color);
            drawLine(-Xs[i] + point.x + 1, -Ys[i] + point.y + 1, -Xs[i + 1] + point.x + 1, -Ys[i + 1] + point.y + 1,
                    color);
            drawLine(Xs[i] + point.x + 1, -Ys[i] + point.y + 1, Xs[i + 1] + point.x + 1, -Ys[i + 1] + point.y + 1,
                    color);
        }
        endRotationSession();
    }

    public void drawCircle(BadVector point, int radius) {
        drawCircle(point, radius, defualtColor);
    }

    @Exposed
    public void drawCircle(@Index int x, @Index int y, int radius) {
        drawCircle(new BadVector(x, y), radius);
    }

    public void drawCircle(int x, int y, int radius, BadVector color) {
        drawCircle(new BadVector(x, y), radius, color);
    }

    public void drawCircle(BadVector point, int radius, int R, int G, int B) {
        drawCircle(point, radius, new BadVector(R, G, B));
    }

    @Exposed
    public void drawCircle(@Index int x, @Index int y, int radius,
            @Index(strict = true, offset = -1) @Range(range = 256) int R,
            @Index(strict = true, offset = -1) @Range(range = 256) int G,
            @Index(strict = true, offset = -1) @Range(range = 256) int B) {
        drawCircle(new BadVector(x, y), radius, new BadVector(R, G, B));
    }

    public void drawPolygon(BadVector point, int n, int size, BadVector color) {
        startRotationSession(point);
        BadVector[] points = new BadVector[n + 1];
        for (int i = 0; i <= n; i++) {
            int angle = (int) Math.round(360d * (double) i / (double) n);
            angle %= 360;
            if (angle <= 90) {
                double radian = Math.toRadians(90 - angle);
                points[i] = new BadVector((int) (Math.cos(radian) * (double) size),
                        -(int) (Math.sin(radian) * (double) size));
            } else if (angle <= 180) {
                double radian = Math.toRadians(angle - 90);
                points[i] = new BadVector((int) (Math.cos(radian) * (double) size),
                        (int) (Math.sin(radian) * (double) size));
            } else if (angle <= 270) {
                double radian = Math.toRadians(angle - 180);
                points[i] = new BadVector(-(int) (Math.cos(radian) * (double) size),
                        (int) (Math.sin(radian) * (double) size));
            } else {
                double radian = Math.toRadians(angle - 270);
                points[i] = new BadVector(-(int) (Math.cos(radian) * (double) size),
                        -(int) (Math.sin(radian) * (double) size));
            }
        }
        for (int i = 0; i < n; i++) {
            drawLine(points[i].add(point), points[i + 1].add(point), color);
        }
        endRotationSession();
    }

    @Exposed
    public static int toRadius(int n, int apothem) {
        return (int) Math.round((double) apothem * Math.tan(Math.PI / n) / Math.sin(Math.PI / n));
    }

    public void drawPolygon(BadVector point, int n, int size, int R, int G, int B) {
        drawPolygon(point, n, size, new BadVector(R, G, B));
    }

    public void drawPolygon(BadVector point, int n, int size) {
        drawPolygon(point, n, size, defualtColor);
    }

    public void drawPolygon(int x, int y, int n, int size, BadVector color) {
        drawPolygon(new BadVector(x, y), n, size, color);
    }

    @Exposed
    public void drawPolygon(@Index int x, @Index int y, int n, int size,
            @Index(strict = true, offset = -1) @Range(range = 256) int R,
            @Index(strict = true, offset = -1) @Range(range = 256) int G,
            @Index(strict = true, offset = -1) @Range(range = 256) int B) {
        drawPolygon(new BadVector(x, y), n, size, new BadVector(R, G, B));
    }

    @Exposed
    public void drawPolygon(@Index int x, @Index int y, int n, int size) {
        drawPolygon(new BadVector(x, y), n, size, defualtColor);
    }

    @Exposed
    public Tuple getSize() {
        return new Tuple(GraphicsBuffer.getSize().x, GraphicsBuffer.getSize().y);
    }

    @Exposed
    public void drawPixels(@Index int x, @Index int y, List buffer) {
        drawPixels(x, y, buffer, -1, -1);
    }

    @Exposed
    public void drawPixels(@Index int x, @Index int y, List buffer, int width) {
        drawPixels(x, y, buffer, width, -1);
    }

    @Exposed
    public void drawPixels(@Index int x, @Index int y, List buffer, int width, int height) {
        if (!buffer.check((val) -> val.instanceOf(VarType.NUMBER))) {
            throw new ExposedError("Argument #2: all values in buffer must be numbers");
        }

        int bufferSize = buffer.size();

        int actualWidth, actualHeight;
        if (width > 0 && height > 0) {
            actualWidth = width;
            actualHeight = height;
            if (bufferSize != width * height * 4) {
                throw new ExposedError("Buffer size does not match specified dimensions (expected " +
                        (width * height * 4) + " values for RGBA data)");
            }
        } else if (width > 0) {
            actualWidth = width;
            if (bufferSize % (width * 4) != 0) {
                throw new ExposedError("Buffer size is not divisible by width * 4");
            }
            actualHeight = bufferSize / (width * 4);
        } else {
            int pixelCount = bufferSize / 4;
            actualWidth = (int) Math.sqrt(pixelCount);
            if (actualWidth * actualWidth * 4 != bufferSize) {
                throw new ExposedError("Cannot infer dimensions: buffer size must be width*height*4 or specify dimensions");
            }
            actualHeight = actualWidth;
        }

        int[] pixels = new int[bufferSize];
        for (int i = 0; i < bufferSize; i++) {
            int val = buffer.get(i).toInt();
            if (val < 0 || val > 255) {
                throw new ExposedError("Color values must be in range [0-255] at index " + i);
            }
            pixels[i] = val;
        }

        boolean needsRotation = (rotatePos != null && angle != 0);

        if (needsRotation) {
            startRotationSession(x + actualWidth / 2, y + actualHeight / 2);
            GraphicsBuffer.bulkBlendPixelsWithTransform(x, y, pixels, actualWidth, actualHeight, this::rotateLocal);
            endRotationSession();
        } else {
            GraphicsBuffer.bulkBlendPixels(x, y, pixels, actualWidth, actualHeight);
        }
    }

}
