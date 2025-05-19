package com.redtoast.graphics;

import org.joml.Vector2i;

public class GraphicsInterface
{
    //this graphics library is a old project that used to run on Jframe's, please forgive me
    private int height;
    private int width;
    private RGBGraphicsArray Graphics;
    private RGBGraphicsArray GraphicsBuffer;
    private Vector defualtColor = new Vector(0,0,0);

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

    public GraphicsInterface(RGBGraphicsArray graphics)
    {
        Graphics = graphics;
        Vector2i size = graphics.getSize();
        height = size.y;
        width = size.x;
        GraphicsBuffer = new RGBGraphicsArray(graphics.pixels);
    }

    public void draw()//refreash graphics on screen
    {
        Graphics.pixels = GraphicsBuffer.pixels;
        GraphicsBuffer = new RGBGraphicsArray(Graphics.pixels);
    }

    public void setColor(int R, int G, int B)//sets the color used when not specificly provided by a draw function
    {
        defualtColor = new Vector(R,G,B);
    }
    public void setColor(Vector color){
        setColor(color.x,color.y,color.z);
    }

    public void drawLine(int x1, int y1, int x2, int y2)//draws a line, duh
    {
        drawLine(x1,height-y1,x2,height-y2,defualtColor.x,defualtColor.y,defualtColor.z);
    }
    public void drawLine(int x1, int y1, int x2, int y2, int R, int G, int B)
    {
        //equation from here:
        //https://www3.cs.stonybrook.edu/~cse328/2021-lecture-notes/line-drawing.pdf
        int dy = y2 - y1;
        int dx = x2 - x1;
        for (int i = Math.min(x1,x2); i < Math.max(x1,x2);i++){
            int y = (int) Math.round(y1+(i-x1)*((double)dy/dx));
            drawPixel(i,y,R,G,B);
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

    public void drawPixel(int x, int y)//you will never guess what this bad boy does
    {
        GraphicsBuffer.set(x,y,RGBGraphicsArray.rgbToDecimal(defualtColor.x,defualtColor.y,defualtColor.z));
    }
    public void drawPixel(int x, int y, int R, int G, int B)
    {
        GraphicsBuffer.set(x,y,RGBGraphicsArray.rgbToDecimal(R,G,B));
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

    public void drawRectangle(int x1, int y1, int x2, int y2, int R, int G, int B)//draws a rectangle between two points
    {
        drawLine(x1,y1,x2,y1,R,G,B);
        drawLine(x2,y1,x2,y2,R,G,B);
        drawLine(x2,y2,x1,y2,R,G,B);
        drawLine(x1,y2,x1,y1,R,G,B);
    }
    public void drawRectangle(int x1, int y1, int x2, int y2){
        drawLine(x1,y1,x2,y1);
        drawLine(x2,y1,x2,y2);
        drawLine(x2,y2,x1,y2);
        drawLine(x1,y2,x1,y1);
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
    }
    public void drawBezier(Vector pointA, Vector pointB, Vector pointC){
        drawBezier(pointA, pointB, pointC, defualtColor);
    }
    public void drawBezier(int x1, int y1, int x2, int y2, int x3, int y3){
        drawBezier(new Vector(x1,y1),new Vector(x2,y2),new Vector(x3,y3));
    }
    public void drawBezier(Vector pointA, Vector pointB, Vector pointC, int R, int G, int B){
        drawBezier(pointA, pointB, pointC, new Vector(R,G,B));
    }
    public void drawBezier(int x1, int y1, int x2, int y2, int x3, int y3, Vector color){
        drawBezier(new Vector(x1,y1),new Vector(x2,y2),new Vector(x3,y3),color);
    }
    public void drawBezier(int x1, int y1, int x2, int y2, int x3, int y3, int R, int G, int B){
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
            drawLine(draw[i],draw[i+1]);
        }
    }
    public void drawSpline(Vector[] points){
        drawSpline(points, defualtColor);
    }
    public void drawSpline(Vector[] points,int R, int G, int B){
        drawSpline(points,new Vector(R,G,B));
    }
    public void drawSpline(int[] Xs, int[] Ys,Vector color){
        Vector[] vecs = new Vector[Math.min(Xs.length,Ys.length)];
        for (int i = 0; i < Math.min(Xs.length,Ys.length);i++){
            vecs[i] = new Vector(Xs[i],Ys[i]);
        }
        drawSpline(vecs,color);
    }
    public void drawSpline(int[] Xs, int[] Ys){
        drawSpline(Xs,Ys, defualtColor);
    }
    public void drawSpline(int[] Xs, int[] Ys,int R,int G,int B){
        drawSpline(Xs,Ys,new Vector(R,G,B));
    }
    public void drawSpline(int[] cords,Vector color){
        Vector[] vecs = new Vector[cords.length/2*2];
        for (int i = 0; i < cords.length/2;i++){
            vecs[i] = new Vector(cords[i*2],cords[i*2+1]);
        }
        drawSpline(vecs,color);
    }
    public void drawSpline(int[] cords){
        drawSpline(cords,defualtColor);
    }
    public void drawSpline(int[] cords, int R,int G,int B){
        drawSpline(cords,new Vector(R,G,B));
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
    }
    public void drawCircle(int x1, int y1, int x2, int y2){
        drawCircle(new Vector(x1,y1),new Vector(x2,y2),defualtColor);
    }
    public void drawCircle(int x1, int y1, int x2, int y2, int R, int G, int B){
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
            drawLine(Xs[i] + point.x,Ys[i] + point.y,Xs[i+1] + point.x,Ys[i+1] + point.y, color);
            drawLine(-Xs[i] + point.x,Ys[i] + point.y,-Xs[i+1] + point.x,Ys[i+1] + point.y, color);
            drawLine(-Xs[i] + point.x,-Ys[i] + point.y,-Xs[i+1] + point.x,-Ys[i+1] + point.y, color);
            drawLine(Xs[i] + point.x,-Ys[i] + point.y,Xs[i+1] + point.x,-Ys[i+1] + point.y, color);
        }
    }
    public void drawCircle(Vector point, int radius){
        drawCircle(point, radius, defualtColor);
    }
    public void drawCircle(int x, int y, int radius){
        drawCircle(new Vector(x,y),radius);
    }
    public void drawCircle(int x, int y, int radius, Vector color){
        drawCircle(new Vector(x,y),radius,color);
    }
    public void drawCircle(Vector point, int radius, int R, int G, int B){
        drawCircle(point, radius, new Vector(R,G,B));
    }
    public void drawCircle(int x, int y, int radius, int R, int G, int B){
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
        double radius = (double)size;
        Vector[] points = new Vector[n + 1];
        for (int i = 0; i <= n; i++){
            int angle = (int)Math.round(360d * (double)i/(double)n);
            angle %= 360;
            if (angle<=90){
                double radian = Math.toRadians(90-angle);
                points[i] = new Vector((int)(Math.cos(radian)*radius),
                        (int)(Math.sin(radian)*radius));
            }else if (angle<=180){
                double radian = Math.toRadians(angle-90);
                points[i] = new Vector((int)(Math.cos(radian)*radius),
                        -(int)(Math.sin(radian)*radius));
            }else if (angle<-270){
                double radian = Math.toRadians(angle-180);
                points[i] = new Vector(-(int)(Math.cos(radian)*radius),
                        -(int)(Math.sin(radian)*radius));
            }else{
                double radian = Math.toRadians(angle-270);
                points[i] = new Vector(-(int)(Math.cos(radian)*radius),
                        (int)(Math.sin(radian)*radius));
            }
        }
        for (int i = 0; i < n; i++){
            drawLine(points[i].add(point),points[i+1].add(point),color);
        }
    }
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
    public void drawPolygon(int x, int y, int n, int size, int R, int G, int B){
        drawPolygon(new Vector(x,y),n,size,new Vector(R,G,B));
    }
    public void drawPolygon(int x, int y, int n, int size){
        drawPolygon(new Vector(x,y),n,size,defualtColor);
    }
}