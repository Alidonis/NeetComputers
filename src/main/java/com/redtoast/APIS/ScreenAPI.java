package com.redtoast.APIS;

import com.redtoast.graphics.FloadFillArray;
import com.redtoast.graphics.RGBGraphicsArray;
import com.redtoast.simulation.annotations.Exposed;
import com.redtoast.simulation.annotations.Index;
import com.redtoast.simulation.annotations.Range;
import com.redtoast.simulation.base.API;
import com.redtoast.simulation.base.LangError;
import com.redtoast.simulation.base.LangThread;
import com.redtoast.simulation.value.ValueTypes.List;
import com.redtoast.simulation.value.ValueTypes.Tuple;
import com.redtoast.simulation.value.VarType;
import org.joml.Vector2i;
import org.joml.Vector3i;

public class ScreenAPI implements API
{
    private final int height;
    private final int width;
    private int alpha = 255;
    private final RGBGraphicsArray Graphics;
    private RGBGraphicsArray GraphicsBuffer;
    private Vector defualtColor = new Vector(0,0,0);
    private double angle = 0;
    private Vector2i rotatePos = null;
    private boolean roobj = false;

    @Override
    public String getLabel() {
        return "screen";
    }

    @Override
    public void onCall(LangThread thread){
        //this graphics library is a old project that used to run on Jframe's, please forgive me
        //thanks for nothing lua
        int indexOffset = thread.getLang().equals("Lua 5.2") ? 1 : 0;
    }

    public static class Vector
    {
        public int x;//R --if used as color, colors will look like this
        public int y;//G
        public int z;//B
        public Vector(int X, int Y){
            x=X;
            y=Y;
            z=0;
        }
        public Vector(int X, int Y, int Z){
            x=X;
            y=Y;
            z=Z;
        }
        public Vector(int[] point){
            x=point[0];
            y=point[1];
            if (point.length>2){
                z=point[2];
            }else{
                z=0;
            }
        }
        public double distance(Vector point){
            return Math.sqrt(Math.pow(point.x - x,2) + Math.pow(point.y - y,2));
        }
        public Vector add(Vector point){
            return new Vector(x+point.x,y+point.y);
        }
    }

    public int applyAlpha(int channel, int base, int alpha){
        if (alpha == 255) return channel;
        double newChannel = 255 - (double) (alpha / 255) * (255 - channel);
        double oldChannel = 255 - (double) (1 - alpha / 255) * (255 - base);
        return (int) Math.round(newChannel + oldChannel);
    }

    public Vector2i rotate(Vector2i vec, Vector2i base, double angle){
        int x1 = vec.x - base.x;
        int y1 = vec.y - base.y;
        double b = Math.toRadians(angle);
        double x2 = Math.cos(b) * x1 - Math.sin(b) * y1;
        double y2 = Math.sin(b) * x1 + Math.cos(b) * y1;
        return new Vector2i(
                (int) Math.round(x2) + base.x,
                (int) Math.round(y2) + base.y
        );
    }

    public ScreenAPI(RGBGraphicsArray graphics)
    {
        Graphics = graphics;
        Vector2i size = graphics.getSize();
        height = size.y;
        width = size.x;
        GraphicsBuffer = new RGBGraphicsArray(graphics.pixels);
        setColor(255,255,255);
    }

    @Exposed
    public void draw()//refreash graphics on screen
    {
        Graphics.pixels = GraphicsBuffer.pixels;
    }

    @Exposed
    public void setRotation(double angle, int x, int y){
        this.angle = angle;
        rotatePos = new Vector2i(x, y);
        roobj = false;
    }

    @Exposed
    public void setRotation(double angle){
        this.angle = angle;
        rotatePos = null;
        roobj = true;
    }

    @Exposed
    public void setRotation(){
        rotatePos = null;
        roobj = false;
    }

    public void startRotationSession(Vector2i pos){
        if (roobj){
            rotatePos = pos;
        }
    }

    public void startRotationSession(int x, int y){
        startRotationSession(new Vector2i(x, y));
    }

    public void startRotationSession(Vector pos){
        startRotationSession(pos.x, pos.y);
    }

    public void endRotationSession(){
        if (roobj) setRotation();
    }

    @Exposed
    public void setColor(@Index( strict = true ) @Range( range = 256 ) int R, @Index( strict = true ) @Range( range = 256 ) int G, @Index( strict = true ) @Range( range = 256 ) int B)//sets the color used when not specificly provided by a draw function
    {
        defualtColor = new Vector(R,G,B);
    }
    @Exposed
    public void setColor(@Index( strict = true ) @Range( range = 256 ) int R, @Index( strict = true ) @Range( range = 256 ) int G, @Index( strict = true ) @Range( range = 256 ) int B, @Index( strict = true ) @Range( range = 256 ) int A)//sets the color used when not specificly provided by a draw function
    {
        setColor(R,G,B);
        alpha = A;
    }
    @Exposed
    public void setColor(@Index( strict = true ) @Range( range = 256 ) int A){
        alpha = A;
    }
    public void setColor(Vector color){
        setColor(Math.clamp(color.x,0,255),Math.clamp(color.y,0,255),Math.clamp(color.z,0,255));
    }

    @Exposed
    public void drawLine(@Index int x1, @Index int y1, @Index int x2, @Index int y2)//draws a line, duh
    {
        drawLine(x1,height-y1,x2,height-y2,defualtColor.x,defualtColor.y,defualtColor.z);
    }

    @Exposed
    public void drawLine(@Index int x0, @Index int y0, @Index int x1, @Index int y1, @Index( strict = true ) @Range( range = 256 ) int R, @Index( strict = true ) @Range( range = 256 ) int G, @Index( strict = true ) @Range( range = 256 ) int B) {
        Vector2i p1 = rotateLocal(x0, y0);
        Vector2i p2 = rotateLocal(x1, y1);
        int dx = Math.abs(p2.x - p1.x);
        int dy = Math.abs(p2.y - p1.y);
        int sx = p1.x < p2.x ? 1 : -1;
        int sy = p1.y < p2.y ? 1 : -1;
        int err = dx - dy;

        while (true) {
            rawDrawPixel(p1.x, p1.y, R, G, B);

            if (p1.x == p2.x && p1.y == p2.y) break;

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

    public void drawLine(Vector pointA, Vector pointB){
        drawLine(pointA.x,pointA.y,pointB.x,pointB.y);
    }
    public void drawLine(Vector pointA, Vector pointB, int R, int G, int B){
        drawLine(pointA.x,pointA.y,pointB.x,pointB.y,R,G,B);
    }
    public void drawLine(Vector pointA, Vector pointB, Vector color){
        drawLine(pointA.x,pointA.y,pointB.x,pointB.y,color.x,color.y,color.z);
    }
    public void drawLine(int x1, int y1, int x2, int y2, Vector color){
        drawLine(x1,y1,x2,y2,color.x,color.y,color.z);
    }

    public Vector2i rotateLocal(int x, int y){
        if (rotatePos==null || angle == 0){
            return new Vector2i(x,y);
        }else{
            return rotate(new Vector2i(x, y), rotatePos, angle);
        }
    }

    @Exposed
    public void drawPixel(@Index int x, @Index int y)//you will never guess what this bad boy does
    {
        drawPixel(x, y, defualtColor);
    }
    private void rawDrawPixel(int x, int y, int R, int G, int B){
        Vector3i old = alpha==255 ? new Vector3i() : RGBGraphicsArray.decimalToRgb(GraphicsBuffer.get(x, y));
        GraphicsBuffer.set(x, y, RGBGraphicsArray.rgbToDecimal(
                applyAlpha(R, old.x, alpha),
                applyAlpha(G, old.y, alpha),
                applyAlpha(B, old.z, alpha)
        ));
    }
    @Exposed
    public void drawPixel(@Index int x, @Index int y, @Index( strict = true ) @Range( range = 256 ) int R, @Index( strict = true ) @Range( range = 256 ) int G, @Index( strict = true ) @Range( range = 256 ) int B)
    {
        Vector2i pos = rotateLocal(x, y);
        rawDrawPixel(pos.x, pos.y, R, G, B);
    }
    public void drawPixel(Vector point){
        drawPixel(point.x,point.y);
    }
    public void drawPixel(Vector point, int R, int G, int B){
        drawPixel(point.x,point.y,R,G,B);
    }
    public void drawPixel(Vector point, Vector color){
        drawPixel(point.x,point.y,color.x,color.y,color.z);
    }
    public void drawPixel(int x, int y, Vector color){
        drawPixel(x,y,color.x,color.y,color.z);
    }

    public static double colorDistance(int R1, int G1, int B1, int R2, int G2, int B2) {
        int R = R1 - R2;
        int G = G1 - G2;
        int B = B1 - B2;
        return (double) (R + G + B) / 3;
    }

    @Exposed
    public void floadFill(int x, int y,@Index( strict = true ) @Range( range = 256 ) int tolerance,@Index( strict = true ) @Range( range = 256 ) int R,@Index( strict = true ) @Range( range = 256 ) int G,@Index( strict = true ) @Range( range = 256 ) int B){
        if (x < 0 || x >= width) throw new LangError("Argument #0: value not in range [0-"+width+']');
        if (y < 0 || y >= height) throw new LangError("Argument #1: value not in range [0-"+height+']');
        if (tolerance==255){
            fill(R, G, B);
            return;
        }
        Vector3i baseColors = RGBGraphicsArray.decimalToRgb(GraphicsBuffer.get(x, y));
        FloadFillArray FFA = new FloadFillArray(width, height) {
            @Override
            public void setPixel(int x, int y) {
                rawDrawPixel(x, y, R, G, B);
            }

            @Override
            public boolean isValid(int x, int y) {
                Vector3i colors = RGBGraphicsArray.decimalToRgb(GraphicsBuffer.get(x, y));
                if (tolerance==0) {
                    if (colors.x != baseColors.x) return false;
                    if (colors.y != baseColors.y) return false;
                    return colors.z == baseColors.z;
                }else{
                    return colorDistance(colors.x, colors.y, colors.z, baseColors.x, baseColors.y, baseColors.z) <= tolerance;
                }
            }
        };
        FFA.start(x, y);
    }

    @Exposed
    public void floadFill(int x, int y,@Index( strict = true ) @Range( range = 256 ) int R,@Index( strict = true ) @Range( range = 256 ) int G,@Index( strict = true ) @Range( range = 256 ) int B){
        floadFill(x, y, 0, R, G, B);
    }

    @Exposed
    public void fill(@Index( strict = true ) @Range( range = 256 ) int R,@Index( strict = true ) @Range( range = 256 ) int G,@Index( strict = true ) @Range( range = 256 ) int B){
        for (int w = 0; w < width; w++){
            for (int h = 0; h < height; h++){
                rawDrawPixel(w,h,R,G,B);
            }
        }
    }

    @Exposed
    public void fill(@Index int x1, @Index int y1, @Index int x2, @Index int y2, @Index( strict = true ) @Range( range = 256 ) int R,@Index( strict = true ) @Range( range = 256 ) int G,@Index( strict = true ) @Range( range = 256 ) int B){
        for (int w = Math.min(x1, x2); w < Math.max(x1, x2); w++){
            for (int h = Math.min(y1, y2); h < Math.max(y1, y2); h++){
                rawDrawPixel(w,h,R,G,B);
            }
        }
    }

    @Exposed
    public void fill(@Index int x1, @Index int y1, @Index int x2, @Index int y2){
        fill(x1, y1, x2, y2, defualtColor.x, defualtColor.y, defualtColor.z);
    }

    public int average(int... nums){
        int toltal = 0;
        for (int i = 0; i < nums.length; i++){
            toltal += nums[i];
        }
        return toltal / nums.length;
    }

    public Vector2i average(Vector2i... nums){
        int toltalX = 0;
        int toltalY = 0;
        for (int i = 0; i < nums.length; i++){
            toltalX += nums[i].x;
            toltalY += nums[i].y;
        }
        return new Vector2i(toltalX / nums.length, toltalY / nums.length);
    }

    public Vector average(Vector... nums){
        int toltalX = 0;
        int toltalY = 0;
        for (int i = 0; i < nums.length; i++){
            toltalX += nums[i].x;
            toltalY += nums[i].y;
        }
        return new Vector(toltalX / nums.length, toltalY / nums.length);
    }

    @Exposed
    public void drawRectangle(@Index int x1, @Index int y1, @Index int x2, @Index int y2, @Index( strict = true ) @Range( range = 256 ) int R, @Index( strict = true ) @Range( range = 256 ) int G, @Index( strict = true ) @Range( range = 256 ) int B)//draws a rectangle between two points
    {
        startRotationSession(average(x1,x2), average(y1, y2));
        drawLine(x1,y1,x2,y1,R,G,B);
        drawLine(x2,y1,x2,y2,R,G,B);
        drawLine(x2,y2,x1,y2,R,G,B);
        drawLine(x1,y2,x1,y1,R,G,B);
        endRotationSession();
    }
    @Exposed
    public void drawRectangle(@Index int x1, @Index int y1, @Index int x2, @Index int y2){
        startRotationSession(average(x1,x2), average(y1, y2));
        drawLine(x1,y1,x2,y1);
        drawLine(x2,y1,x2,y2);
        drawLine(x2,y2,x1,y2);
        drawLine(x1,y2,x1,y1);
        endRotationSession();
    }
    public void drawRectangle(Vector pointA, Vector pointB){
        drawRectangle(pointA.x,pointA.y,pointB.x,pointB.y);
    }
    public void drawRectangle(Vector pointA, Vector pointB, int R, int G, int B){
        drawRectangle(pointA.x,pointA.y,pointB.x,pointB.y,R,G,B);
    }
    public void drawRectangle(Vector pointA, Vector pointB, Vector color){
        drawRectangle(pointA.x,pointA.y,pointB.x,pointB.y,color.x,color.y,color.z);
    }
    public void drawRectangle(int x1, int y1, int x2, int y2, Vector color){
        drawRectangle(x1,y1,x2,y2,color.x,color.y,color.z);
    }

    public void drawBezier(Vector pointA, Vector pointB, Vector pointC, Vector color)//draw a bezier curver through the provided points
    {
        startRotationSession(average(pointA, pointB, pointC));
        int samples = Math.round((int)(pointA.distance(pointC)/3));
        int[] Xs = new int[samples + 1];
        int[] Ys = new int[samples + 1];
        Xs[0] = pointA.x;
        Ys[0] = pointA.y;
        Xs[samples] = pointC.x;
        Ys[samples] = pointC.y;
        for (int i = 1; i < samples; i++)
        {
            Vector point = bezier(pointA,pointB,pointC,(double)i/(double)samples);
            Xs[i] = point.x;
            Ys[i] = point.y;
        }
        for (int i = 0; i < samples; i++)
        {
            drawLine(Xs[i],Ys[i],Xs[i+1],Ys[i+1], color);
        }
        endRotationSession();
    }
    public void drawBezier(Vector pointA, Vector pointB, Vector pointC){
        drawBezier(pointA, pointB, pointC, defualtColor);
    }
    @Exposed
    public void drawBezier(@Index int x1, @Index int y1, @Index int x2, @Index int y2, @Index int x3, @Index int y3){
        drawBezier(new Vector(x1,y1),new Vector(x2,y2),new Vector(x3,y3));
    }
    public void drawBezier(Vector pointA, Vector pointB, Vector pointC, int R, int G, int B){
        drawBezier(pointA, pointB, pointC, new Vector(R,G,B));
    }
    public void drawBezier(int x1, int y1, int x2, int y2, int x3, int y3, Vector color){
        drawBezier(new Vector(x1,y1),new Vector(x2,y2),new Vector(x3,y3),color);
    }
    @Exposed
    public void drawBezier(@Index int x1, @Index int y1, @Index int x2, @Index int y2, @Index int x3, @Index int y3, @Index( strict = true ) @Range( range = 256 ) int R, @Index( strict = true ) @Range( range = 256 ) int G, @Index( strict = true ) @Range( range = 256 ) int B){
        drawBezier(new Vector(x1,y1),new Vector(x2,y2),new Vector(x3,y3),new Vector(R,G,B));
    }
    private Vector interlopeDistance(Vector pointA, Vector pointB, double T)
    {
        int[] endPoint = new int[2];
        endPoint[0] = (int)Math.round(T * ( pointB.x - pointA.x)) + pointA.x;
        endPoint[1] = (int)Math.round(T * ( pointB.y - pointA.y)) + pointA.y;
        return new Vector(endPoint);
    }
    private Vector bezier(Vector pointA, Vector pointB, Vector pointC, double T)
    {
        Vector pointAB = interlopeDistance(pointA, pointB, T);
        Vector pointBC = interlopeDistance(pointB, pointC, T);
        return interlopeDistance(pointAB, pointBC, T);
    }

    //draws a spline (bezier with more points)
    public void drawSpline(Vector[] points,Vector color){
        startRotationSession(average(points));
        int samples=0;
        for (int i = 0; i < points.length - 1; i++){
            samples += Math.sqrt(Math.pow(Math.abs(points[i].x-points[i+1].x),2)+Math.pow(Math.abs(points[i].y-points[i+1].y),2));
        }
        samples /= 5;
        Vector[] draw = new Vector[samples];
        for (int i = 0; i < samples; i++){
            draw[i] = spline(points,(double)i/(double)samples);
        }
        for (int i = 0; i < samples - 1; i++)
        {
            drawLine(draw[i],draw[i+1], color);
        }
        endRotationSession();
    }
    public void drawSpline(Vector[] points){
        drawSpline(points, defualtColor);
    }
    public void drawSpline(Vector[] points,int R, int G, int B){
        drawSpline(points,new Vector(R,G,B));
    }
    public void drawSpline(Integer[] Xs, Integer[] Ys,Vector color){
        Vector[] vecs = new Vector[Math.min(Xs.length,Ys.length)];
        for (int i = 0; i < Math.min(Xs.length,Ys.length);i++){
            vecs[i] = new Vector(Xs[i],Ys[i]);
        }
        drawSpline(vecs,color);
    }
    @Exposed
    public void drawSpline(List Xs, List Ys){
        if (!Xs.check((val) -> val.instanceOf(VarType.NUMBER))) throw new LangError("Argument #1: all vals in list must be numbers");
        if (!Ys.check((val) -> val.instanceOf(VarType.NUMBER))) throw new LangError("Argument #2: all vals in list must be numbers");
        if (Xs.size()!= Ys.size()) throw new LangError("drawSpline called with balance of X and Y values");
        drawSpline(Xs.cast((val) -> val.toInt()).toArray(new Integer[]{}),Ys.cast((val) -> val.toInt()).toArray(new Integer[]{}), defualtColor);
    }
    @Exposed
    public void drawSpline(List Xs, List Ys, @Index( strict = true ) @Range( range = 256 ) int R, @Index( strict = true ) @Range( range = 256 ) int G, @Index( strict = true ) @Range( range = 256 ) int B){
        if (!Xs.check((val) -> val.instanceOf(VarType.NUMBER))) throw new LangError("Argument #1: all vals in list must be numbers");
        if (!Ys.check((val) -> val.instanceOf(VarType.NUMBER))) throw new LangError("Argument #2: all vals in list must be numbers");
        if (Xs.size()!= Ys.size()) throw new LangError("drawSpline called with balance of X and Y values");
        drawSpline(Xs.cast((val) -> val.toInt()).toArray(new Integer[]{}),Ys.cast((val) -> val.toInt()).toArray(new Integer[]{}),new Vector(R,G,B));
    }
    public void drawSpline(Integer[] cords,Vector color){
        Vector[] vecs = new Vector[cords.length/2*2];
        for (int i = 0; i < cords.length/2;i++){
            vecs[i] = new Vector(cords[i*2],cords[i*2+1]);
        }
        drawSpline(vecs,color);
    }
    @Exposed
    public void drawSpline(List cords){
        if (!cords.check((val) -> val.instanceOf(VarType.NUMBER))) throw new LangError("Argument #1: all vals in list must be numbers");
        if (cords.size()%2==1) throw new LangError("Argument #1: list input contained a uneven amount of values (not valid list of coordinates)");
        drawSpline(cords.cast((val) -> val.toInt()).toArray(new Integer[]{}),defualtColor);
    }
    @Exposed
    public void drawSpline(List cords, @Index( strict = true ) @Range( range = 256 ) int R, @Index( strict = true ) @Range( range = 256 ) int G, @Index( strict = true ) @Range( range = 256 ) int B){
        if (!cords.check((val) -> val.instanceOf(VarType.NUMBER))) throw new LangError("Argument #1: all vals in list must be numbers");
        if (cords.size()%2==1) throw new LangError("Argument #1: list input contained a uneven amount of values (not valid list of coordinates)");
        drawSpline(cords.cast((val) -> val.toInt()).toArray(new Integer[]{}),new Vector(R,G,B));
    }

    public Vector spline(Vector[] points,double T){
        Vector[] vecs = points;
        while (vecs.length>1){
            Vector[] newVec = new Vector[vecs.length-1];
            for (int i = 0; i < vecs.length - 1; i++){
                newVec[i] = interlopeDistance(vecs[i],vecs[i+1],T);
            }
            vecs = newVec;
        }
        return vecs[0];
    }

    public void drawCircle(Vector pointA, Vector pointB, Vector color)//draw a circle inbetween the two points
    {
        startRotationSession(average(pointA, pointB));
        double radius = Math.abs(pointB.y-pointA.y)/2d;
        double scaleX = (double)Math.abs(pointB.x-pointA.x)/Math.abs(pointB.y-pointA.y);
        Vector point = new Vector((pointA.x+pointB.x)/2,(pointA.x+pointB.x)/2);
        int samples = (int)Math.round(Math.PI * radius / 6d);
        int[] Xs = new int[samples + 1];
        int[] Ys = new int[samples + 1];
        for (int i = 0; i <= samples; i++)
        {
            double angle = Math.toRadians(90d * (double)i/(double)samples);
            Ys[i] = (int)(Math.sin(angle) * radius);
            Xs[i] = (int)(Math.cos(angle) * radius * scaleX);
        }
        for (int i = 0; i < samples; i++)
        {
            drawLine(Xs[i] + point.x,Ys[i] + point.x,Xs[i+1] + point.x,Ys[i+1] + point.y, color);
            drawLine(-Xs[i] + point.x,Ys[i] + point.y,-Xs[i+1] + point.x,Ys[i+1] + point.y, color);
            drawLine(-Xs[i] + point.x,-Ys[i] + point.y,-Xs[i+1] + point.x,-Ys[i+1] + point.y, color);
            drawLine(Xs[i] + point.x,-Ys[i] + point.y,Xs[i+1] + point.x,-Ys[i+1] + point.y, color);
        }
        endRotationSession();
    }
    @Exposed
    public void drawCircle(@Index int x1, @Index int y1, @Index int x2, @Index int y2){
        drawCircle(new Vector(x1,y1),new Vector(x2,y2),defualtColor);
    }
    @Exposed
    public void drawCircle(@Index int x1, @Index int y1, @Index int x2, @Index int y2, @Index( strict = true ) @Range( range = 256 ) int R, @Index( strict = true ) @Range( range = 256 ) int G, @Index( strict = true ) @Range( range = 256 ) int B){
        drawCircle(new Vector(x1,y1),new Vector(x2,y2),new Vector(R,G,B));
    }
    public void drawCircle(Vector pointA, Vector pointB){
        drawCircle(pointA.x,pointA.y,pointB.x,pointB.y);
    }
    public void drawCircle(Vector pointA, Vector pointB, int R, int G, int B){
        drawCircle(pointA.x,pointA.y,pointB.x,pointB.y,R,G,B);
    }
    public void drawCircle(int x1, int y1, int x2, int y2, Vector color){
        drawCircle(x1,y1,x2,y2,color.x,color.y,color.z);
    }

    public void drawCircle(Vector point, int radius, Vector color)//draw a circle of given radius around point
    {
        startRotationSession(point);
        int samples = (int)Math.round(Math.PI * (double)radius / 6d);
        int[] Xs = new int[samples + 1];
        int[] Ys = new int[samples + 1];
        for (int i = 0; i <= samples; i++)
        {
            double angle = Math.toRadians(90d * (double)i/(double)samples);
            Ys[i] = (int)(Math.sin(angle) * (double)radius);
            Xs[i] = (int)(Math.cos(angle) * (double)radius);
        }
        for (int i = 0; i < samples; i++)
        {
            drawLine(Xs[i] + point.x + 1,Ys[i] + point.y + 1,Xs[i+1] + point.x + 1,Ys[i+1] + point.y + 1, color);
            drawLine(-Xs[i] + point.x + 1,Ys[i] + point.y + 1,-Xs[i+1] + point.x + 1,Ys[i+1] + point.y + 1, color);
            drawLine(-Xs[i] + point.x + 1,-Ys[i] + point.y + 1,-Xs[i+1] + point.x + 1,-Ys[i+1] + point.y + 1, color);
            drawLine(Xs[i] + point.x + 1,-Ys[i] + point.y + 1,Xs[i+1] + point.x + 1,-Ys[i+1] + point.y + 1, color);
        }
        endRotationSession();
    }
    public void drawCircle(Vector point, int radius){
        drawCircle(point, radius, defualtColor);
    }
    @Exposed
    public void drawCircle(@Index int x, @Index int y, int radius){
        drawCircle(new Vector(x,y),radius);
    }
    public void drawCircle(int x, int y, int radius, Vector color){
        drawCircle(new Vector(x,y),radius,color);
    }
    public void drawCircle(Vector point, int radius, int R, int G, int B){
        drawCircle(point, radius, new Vector(R,G,B));
    }
    @Exposed
    public void drawCircle(@Index int x, @Index int y, int radius, @Index( strict = true ) @Range( range = 256 ) int R, @Index( strict = true ) @Range( range = 256 ) int G, @Index( strict = true ) @Range( range = 256 ) int B){
        drawCircle(new Vector(x,y),radius, new Vector(R,G,B));
    }

    /*draws a polygon (equilateral triangles, hexagons, octagons)
     * around the provided (point)
     * the (n) perameter is the amount of sides your polygon has
     * and (size) is the radius of your polygon mapped over a circle
     * /polygons circumradius
     * if you want to make the polygon based off a apothem, use Window.toRadius
     * Window.toRadius(n, apothem) -> radius
     * and (color) is color, duh
     */
    public void drawPolygon(Vector point,int n, int size, Vector color){
        startRotationSession(point);
        double radius = (double)size;
        Vector[] points = new Vector[n + 1];
        for (int i = 0; i <= n; i++){
            int angle = (int)Math.round(360d * (double)i/(double)n);
            angle %= 360;
            if (angle<=90){
                double radian = Math.toRadians(90-angle);
                points[i] = new Vector((int)(Math.cos(radian)*radius),
                        -(int)(Math.sin(radian)*radius));
            }else if (angle<=180){
                double radian = Math.toRadians(angle-90);
                points[i] = new Vector((int)(Math.cos(radian)*radius),
                        (int)(Math.sin(radian)*radius));
            }else if (angle<-270){
                double radian = Math.toRadians(angle-180);
                points[i] = new Vector(-(int)(Math.cos(radian)*radius),
                        (int)(Math.sin(radian)*radius));
            }else{
                double radian = Math.toRadians(angle-270);
                points[i] = new Vector(-(int)(Math.cos(radian)*radius),
                        -(int)(Math.sin(radian)*radius));
            }
        }
        for (int i = 0; i < n; i++){
            drawLine(points[i].add(point),points[i+1].add(point),color);
        }
        endRotationSession();
    }
    @Exposed
    public static int toRadius(int n,int apothem){
        return (int)Math.round((double)apothem*Math.tan(Math.PI/n)/Math.sin(Math.PI/n));
    }
    public void drawPolygon(Vector point, int n, int size, int R, int G, int B){
        drawPolygon(point,n,size,new Vector(R,G,B));
    }
    public void drawPolygon(Vector point, int n, int size){
        drawPolygon(point,n,size,defualtColor);
    }
    public void drawPolygon(int x, int y, int n, int size, Vector color){
        drawPolygon(new Vector(x,y),n,size,color);
    }
    @Exposed
    public void drawPolygon(@Index int x, @Index int y, int n, int size, @Index( strict = true ) @Range( range = 256 ) int R, @Index( strict = true ) @Range( range = 256 ) int G, @Index( strict = true ) @Range( range = 256 ) int B){
        drawPolygon(new Vector(x,y),n,size,new Vector(R,G,B));
    }
    @Exposed
    public void drawPolygon(@Index int x, @Index int y, int n, int size){
        drawPolygon(new Vector(x,y),n,size,defualtColor);
    }

    @Exposed
    public Tuple getSize() {return new Tuple(GraphicsBuffer.getSize().x, GraphicsBuffer.getSize().y);}
}