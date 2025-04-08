package ru.zzz3230.tetris.utils;

import java.awt.*;
import java.awt.geom.Point2D;

public class MathUtils {
    public static Point rotatePoint90Deg(Point point, Point center){
        float newX = center.x - (point.y - center.y);
        float newY = center.y + (point.x- center.x);
        return new Point((int)Math.round(newX), (int)Math.ceil(newY));
    }
}
