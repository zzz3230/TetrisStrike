package ru.zzz3230.tetris.swingUi;

import javax.swing.*;
import java.awt.*;
import java.util.Arrays;

class SwingGamePanel extends JPanel {
    private final int rows;
    private final int cols;

    private final Color[][] field;

    public SwingGamePanel(int rows, int cols) {
        this.rows = rows;
        this.cols = cols;
        this.field = new Color[rows][cols];
        clear();
        // Для корректной отрисовки используем двойную буферизацию
        setDoubleBuffered(true);
    }

    public void setCellColor(int row, int col, Color color) {
        field[row][col] = color;
    }

    public void clear(){
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                field[i][j] = new Color(0x0, true);
            }
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // Рассчитываем ширину/высоту ячейки в зависимости от текущего размера панели
        int cellWidth = getWidth() / cols;
        int cellHeight = getHeight() / rows;

        cellWidth = Math.min(cellWidth, cellHeight);
        cellHeight = cellWidth;

        // Рисуем сетку (или сразу фигуры, если нужна логика)
        g.setColor(Color.GRAY);
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                // Координаты верхнего левого угла ячейки
                int x = c * cellWidth;
                int y = r * cellHeight;

                // Рисуем прямоугольник
                g.drawRect(x, y, cellWidth, cellHeight);

                // Для примера: заполним несколько ячеек «цветом» (можно убрать)
                //if ((r + c) % 5 == 0) {
                    g.setColor(field[r][c]);
                    g.fillRect(x+1, y+1, cellWidth-1, cellHeight-1);
                    g.setColor(Color.GRAY);
                //}
            }
        }
    }
}