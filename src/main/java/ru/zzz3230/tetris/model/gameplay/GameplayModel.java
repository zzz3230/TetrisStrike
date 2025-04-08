package ru.zzz3230.tetris.model.gameplay;

import ru.zzz3230.tetris.dto.BlockData;
import ru.zzz3230.tetris.exceptions.GameplayLogicException;
import ru.zzz3230.tetris.model.gameplay.eventsData.BaseEventData;
import ru.zzz3230.tetris.model.gameplay.eventsData.MergeEventData;
import ru.zzz3230.tetris.utils.Observable;

import java.awt.*;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Random;

public class GameplayModel extends Observable<GameplayContext> {

    GameplayField gameplayField;

    GameplayField.GameplayFieldFragment nextBlock;

    final int ROWS = 24;
    final int COLS = 10;

    BlockData[] tests;
    SecureRandom rand = new SecureRandom();

    final int LINES_TO_LEVEL_UP = 3;

    int gameLevel = 0;
    int score = 0;

    BlockData currentFallBlock;
    BlockData nextFallBlock;

    private boolean isPlaying = true;

    public GameplayModel() {
        gameplayField = new GameplayField();
        tests = new BlockData[]{
                new BlockData(3, 2, new boolean[][]{
                        {false, true, true},
                        {true, true, false}
                }, new Color(118, 34, 171)),
                new BlockData(3, 2, new boolean[][]{
                        {true, true, false},
                        {false, true, true}
                }, new Color(83, 83, 83)),
                new BlockData(3, 2, new boolean[][]{
                        {true, true, true},
                        {true, false, false}
                }, new Color(87, 171, 34)),
                new BlockData(3, 2, new boolean[][]{
                        {true, true, true},
                        {false, false, true}
                }, new Color(214, 72, 72)),
                new BlockData(3, 2, new boolean[][]{
                        {false, true, false},
                        {true, true, true}
                }, new Color(243, 150, 56)),
                new BlockData(3, 2, new boolean[][]{
                        {true, true, false},
                        {true, true, false}
                }, new Color(56, 137, 242)),
                new BlockData(4, 1, new boolean[][]{
                        {true, true, true, true}
                }, Color.pink)
        };
    }

    private ArrayList<Integer> emptyFilledRows(){
        return gameplayField.getStaticBlocks().clearFilledRows();
    }

    private void generateFallingBlock(){
        gameplayField.getFallingBlock().clear();
        var bl = tests[rand.nextInt(tests.length)];
        if(nextFallBlock == null){
            nextFallBlock = tests[rand.nextInt(tests.length)];
        }

        currentFallBlock = nextFallBlock;
        nextFallBlock = bl;
        boolean overridden = gameplayField.getFallingBlock().pasteBlock(currentFallBlock, 0, 0);
        if(overridden){
            finishGame();
        }
    }

    private void finishGame(){
        isPlaying = false;
        gameOverNotify();
    }

    public void update(){
        if(!isPlaying){
            return;
        }

        if(currentFallBlock == null){ // First block generating
            generateFallingBlock();
        }

        GameplayEventType eventType = GameplayEventType.UNKNOWN;
        BaseEventData eventData = null;

        if(gameplayField.getFallingBlock().isOverlapIfMove(1, 0)){
            Point fallingBlockCenter = gameplayField.getFallingBlock().getCenter();
            Color fallingBlockColor = currentFallBlock.getColor();
            gameplayField.getStaticBlocks().mergeFrom(gameplayField.getFallingBlock());

            generateFallingBlock();

            eventType = GameplayEventType.FALLING_BLOCK_MERGED;
            eventData = new MergeEventData(fallingBlockCenter, fallingBlockColor);
        } else {
            gameplayField.getFallingBlock().move(1, 0);
        }

        ArrayList<Integer> cleared = emptyFilledRows();
        if(!cleared.isEmpty()){
            score += cleared.size() * 100;
            if(score >= LINES_TO_LEVEL_UP * 100 * (gameLevel + 1)){
                gameLevel++;
            }
            //eventType = GameplayEventType.ROWS_CLEARED;
            //eventData = new MergeEventData(cleared);
        }

        notifyObserver(new GameplayContext(gameplayField, eventType, eventData, score));
    }

    public void moveFallingBlock(int dx, int dy){
        if(!isPlaying){
            return;
        }

        if(dx != 0){ // Horizontal move
            if(!gameplayField.getFallingBlock().isOverlapIfMove(0, dx)){
                gameplayField.getFallingBlock().move(0, dx);
                defaultNotify();
            }
        }
        if(dy > 0){ // Vertical move
            if(!gameplayField.getFallingBlock().isOverlapIfMove(dy, 0)){
                gameplayField.getFallingBlock().move(dy, 0);

                score += dy;

                defaultNotify();
            }
        }

        if(dy < 0){
            throw new GameplayLogicException("Unable to move block up");
        }
    }

    public void rotateFallingBlock(int dRot){
        if(!isPlaying){
            return;
        }

        if(!gameplayField.getFallingBlock().isOverlapIfRotate(dRot)){
            gameplayField.getFallingBlock().rotate(dRot);
            defaultNotify();
        }
    }

    private void defaultNotify(){
        notifyObserver(new GameplayContext(gameplayField, GameplayEventType.UNKNOWN, null, score));
    }
    private void gameOverNotify(){
        notifyObserver(new GameplayContext(gameplayField, GameplayEventType.GAME_OVER, null, score));
    }

    public boolean isPlaying(){
        return isPlaying;
    }

    public int getUpdateDelay(){
        //float secDelay = 0.8f + 1f / (gameLevel/10f + 1f) - 1f;
        float secDelay = 0.8f - 0.1f * gameLevel;
        return (int)(secDelay * 1000);
    }
    public int getScore(){
        return score;
    }
}
