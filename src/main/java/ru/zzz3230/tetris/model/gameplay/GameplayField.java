package ru.zzz3230.tetris.model.gameplay;

import ru.zzz3230.tetris.exceptions.NotImplementedException;
import ru.zzz3230.tetris.dto.BlockData;
import ru.zzz3230.tetris.utils.MathUtils;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;

public class GameplayField {
    GameplayFieldFragment staticBlocks;
    GameplayFieldFragment fallingBlock;

    GameplayFieldFragment[] allFragments;

    private final int ROWS = 24;
    private final int COLS = 10;

    public GameplayField() {
        staticBlocks = new GameplayFieldFragment(ROWS, COLS);
        fallingBlock = new GameplayFieldFragment(ROWS, COLS);
        allFragments = new GameplayFieldFragment[]{staticBlocks, fallingBlock};

        fallingBlock.getCell(3, 1).fill(Color.gray);
        fallingBlock.getCell(3, 2).fill(Color.gray);
        fallingBlock.getCell(4, 2).fill(Color.gray);
        fallingBlock.getCell(5, 2).fill(Color.gray);
    }

    public int getRows() {
        return ROWS;
    }
    public int getCols() {
        return COLS;
    }

    public GameplayFieldFragment getStaticBlocks() {
        return staticBlocks;
    }

    public GameplayFieldFragment getFallingBlock() {
        return fallingBlock;
    }

    private boolean isFilledWithAny(int row, int column, GameplayFieldFragment... except){
        for (GameplayFieldFragment fragment : allFragments) {
            if (except.length > 0 && Arrays.stream(except).anyMatch(x -> x == fragment)) {
                continue;
            }
            if (fragment.getCell(row, column).isFilled()) {
                return true;
            }
        }
        return false;
    }

    public GameplayField.GameplayFieldFragment.Cell getCell(int row, int column) {
        if(staticBlocks.getCell(row, column).isFilled()) {
            return staticBlocks.getCell(row, column);
        } else {
            return fallingBlock.getCell(row, column);
        }
    }

    public class GameplayFieldFragment {

        public static class Cell {
            private boolean isFilled;
            private Color color;

            public boolean isFilled() {
                return isFilled;
            }
            public Color getColor() {
                return color;
            }

            public void fill(Color color) {
                isFilled = true;
                this.color = color;
            }

            public void clear() {
                isFilled = false;
                color = null;
            }
        }

        private final Cell[][] field;

        private final int rows;
        private final int columns;

        public int getRows() {
            return rows;
        }
        public int getCols() {
            return columns;
        }

        private boolean isInRange(int row, int column) {
            return row >= 0 && row < rows && column >= 0 && column < columns;
        }

        private void swapCells(int row1, int column1, int row2, int column2) {
            Cell temp = field[row1][column1];
            field[row1][column1] = field[row2][column2];
            field[row2][column2] = temp;
        }

        public GameplayFieldFragment(int rows, int columns) {
            this.rows = rows;
            this.columns = columns;

            field = new Cell[rows][columns];
            for (int i = 0; i < rows; i++) {
                for (int j = 0; j < columns; j++) {
                    field[i][j] = new Cell();
                }
            }
        }

        public Cell getCell(int row, int column) {
            return field[row][column];
        }

        public boolean isOverlapIfMove(int dRow, int dColumn){
            if(dRow < 0){
                throw new NotImplementedException();
            }
            if(dRow != 0 && dColumn != 0){
                throw new NotImplementedException();
            }

            if(dRow > 0){
                for (int j = columns - 1; j >= 0; j--) {
                    for (int i = rows - 1; i >= 0; i--) {
                        if(getCell(i, j).isFilled()){
                            if(i + dRow >= rows){
                                return true;
                            }
                            if(isFilledWithAny(i + dRow, j)){
                                return true;
                            }

                            break; // Checking next column
                        }
                    }
                }
            }
            if(dColumn > 0){
                for (int i = rows - 1; i >= 0; i--) {
                    for (int j = columns - 1; j >= 0; j--) {
                        if(getCell(i, j).isFilled()){
                            if(j + dColumn >= columns){
                                return true;
                            }
                            if(isFilledWithAny(i, j + dColumn)){
                                return true;
                            }

                            break; // Checking next row
                        }
                    }
                }
            }
            if(dColumn < 0){
                for (int i = rows - 1; i >= 0; i--) {
                    for (int j = 0; j < columns; j++) {
                        if(getCell(i, j).isFilled()){
                            if(j + dColumn < 0){
                                return true;
                            }
                            if(isFilledWithAny(i, j + dColumn)){
                                return true;
                            }

                            break; // Checking next row
                        }
                    }
                }
            }
            return false;
        }

        public void move(int dRow, int dColumn){
            if(dRow < 0){
                throw new NotImplementedException();
            }
            if(dRow != 0 && dColumn != 0){
                throw new NotImplementedException();
            }

            if(dColumn > 0){
                for (int i = rows - 1; i >= 0; i--) {
                    for (int j = columns - 1; j >= 0; j--) {
                        if(isInRange(i, j + dColumn)){
                            swapCells(i, j, i, j + dColumn);
                        }
                        else
                            getCell(i, j).clear();
                    }
                }
            }
            if(dColumn < 0){
                for (int i = rows - 1; i >= 0; i--) {
                    for (int j = 0; j < columns; j++) {
                        if(isInRange(i, j + dColumn)){
                            swapCells(i, j, i, j + dColumn);
                        }
                        else
                            getCell(i, j).clear();
                    }
                }
            }
            if(dRow > 0){
                for (int i = rows - 1; i >= 0; i--) {
                    for (int j = columns - 1; j >= 0; j--) {
                        if(isInRange(i + dRow, j))
                            swapCells(i, j, i + dRow, j);
                        else
                            getCell(i, j).clear();
                    }
                }
            }
        }

        public Point getCenter(){
            float centerRow = 0;
            float centerColumn = 0;
            int filledCells = 0;

            for (int i = 0; i < getRows(); i++) {
                for (int j = 0; j < getCols(); j++) {
                    if(getCell(i, j).isFilled()){
                        centerRow += i;
                        centerColumn += j;
                        filledCells++;
                    }
                }
            }
            centerRow /= filledCells;
            centerColumn /= filledCells;
            int centerRowCell = (int)Math.round(centerRow);
            int centerColumnCell = (int)Math.round(centerColumn);
            return new Point(centerRowCell, centerColumnCell);
        }

        public boolean isOverlapIfRotate(int dRot){
//            if(dRot != 1){
//                throw new NotImplementedException();
//            }

            Point center = getCenter();
            for (int i = 0; i < getRows(); i++) {
                for (int j = 0; j < getCols(); j++) {
                    if(getCell(i, j).isFilled()){
                        Point rotatedPoint = MathUtils.rotatePoint90Deg(new Point(i, j), center, dRot);
                        if(!isInRange(rotatedPoint.x, rotatedPoint.y)){
                            return true;
                        }
                        if(isFilledWithAny(rotatedPoint.x, rotatedPoint.y, this)){
                            return true;
                        }
                    }
                }
            }
            return false;
        }

        public void rotate(int dRot){
            Point center = getCenter();

            GameplayFieldFragment rotatedBlock = new GameplayFieldFragment(getRows(), getCols());
            for (int i = 0; i < getRows(); i++) {
                for (int j = 0; j < getCols(); j++) {
                    if(getCell(i, j).isFilled()){
                        Point rotatedPoint = MathUtils.rotatePoint90Deg(new Point(i, j), center, dRot);
                        int newRow = rotatedPoint.x;
                        int newColumn = rotatedPoint.y;
                        if(rotatedBlock.isInRange(newRow, newColumn))
                            rotatedBlock.getCell(newRow, newColumn).fill(getCell(i, j).getColor());
                    }
                }
            }
            fallingBlock = rotatedBlock;
            allFragments[1] = fallingBlock;
        }

        public void mergeFrom(GameplayFieldFragment fallingBlock) {
            for (int i = 0; i < rows; i++) {
                for (int j = 0; j < columns; j++) {
                    if(fallingBlock.getCell(i, j).isFilled()){
                        field[i][j].fill(fallingBlock.getCell(i, j).getColor());
                    }
                }
            }
        }

        public void clear() {
            for (int i = 0; i < rows; i++) {
                for (int j = 0; j < columns; j++) {
                    field[i][j].clear();
                }
            }
        }

        public boolean pasteBlock(BlockData blockData, int row, int column) {
            boolean[][] form = blockData.form();
            Color color = blockData.color();
            int width = blockData.width();
            int height = blockData.height();

            boolean overridden = false;

            for (int i = 0; i < height; i++) {
                for (int j = 0; j < width; j++) {
                    if (form[i][j]) {
                        if(isFilledWithAny(row + i, column + j)){
                            overridden = true;
                        }
                        field[row + i][column + j].fill(color);
                    }
                }
            }

            return overridden;
        }

        public int[] calculateFilledRows(){
            ArrayList<Integer> cleared = new ArrayList<Integer>();

            for (int i = 0; i < rows; i++) {
                boolean isFilled = true;
                for (int j = 0; j < columns; j++) {
                    if(!getCell(i, j).isFilled()){
                        isFilled = false;
                        break;
                    }
                }
                if(isFilled){
                    cleared.add(i);
                }
            }
            return cleared.stream().mapToInt(i -> i).toArray();
        }

        public int[] clearFilledRows(){

            ArrayList<Integer> cleared = new ArrayList<Integer>();

            for (int i = 0; i < rows; i++) {
                boolean isFilled = true;
                for (int j = 0; j < columns; j++) {
                    if(!getCell(i, j).isFilled()){
                        isFilled = false;
                        break;
                    }
                }
                if(isFilled){
                    cleared.add(i);
                    for (int j = 0; j < columns; j++) {
                        getCell(i, j).clear();
                    }
                    for (int k = i; k > 0; k--) {
                        for (int j = 0; j < columns; j++) {
                            swapCells(k, j, k - 1, j);
                        }
                    }
                }
            }
            return cleared.stream().mapToInt(i -> i).toArray();
        }
    }
}
