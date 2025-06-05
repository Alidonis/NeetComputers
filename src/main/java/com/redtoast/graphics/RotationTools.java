package com.redtoast.graphics;

import org.joml.Matrix3f;
import org.joml.Vector3f;

public class RotationTools {
    public static Matrix3f generateRotationX(double degrees){
        double radians = Math.toRadians(degrees);
        float cos = (float) Math.cos(radians);
        float sin = (float) Math.sin(radians);
        return new Matrix3f(
                1f, 0, 0,
                0, cos, -sin,
                0, sin, cos
        );
    }

    public static Matrix3f generateRotationY(double degrees){
        double radians = Math.toRadians(degrees);
        float cos = (float) Math.cos(radians);
        float sin = (float) Math.sin(radians);
        return new Matrix3f(
                cos, 0, sin,
                0, 1, 0,
                -sin, 0, cos
        );
    }

    public static Matrix3f generateRotationZ(double degrees){
        double radians = Math.toRadians(degrees);
        float cos = (float) Math.cos(radians);
        float sin = (float) Math.sin(radians);
        return new Matrix3f(
                cos, -sin, 0,
                sin, cos, 0,
                0, 0, 1
        );
    }

    public static Matrix3f createRotationMatrix(double angX, double angY, double angZ){
        return generateRotationX(angX).mul(generateRotationY(angY)).mul(generateRotationZ(angZ));
    }

    public static Vector3f applyRotation(Vector3f pos, Matrix3f rotation){
        return pos.mul(rotation);
    }

    public static Vector3f applyRotation(Vector3f pos,Vector3f PivotPoint, Matrix3f rotation){
        return pos.sub(PivotPoint).mul(rotation).add(PivotPoint);
    }
}
