package ru.zzz3230.tetris.utils;

import java.awt.*;
import java.awt.geom.Point2D;

public class MathUtils {
    public static Point rotatePoint90Deg(Point point, Point center, int direction) {
        int dx = point.x - center.x;
        int dy = point.y - center.y;
        // direction: 1 - по часовой, -1 - против часовой
        int newX = center.x + direction * dy;
        int newY = center.y - direction * dx;
        return new Point(newX, newY);
    }
}
