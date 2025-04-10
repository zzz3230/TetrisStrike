package ru.zzz3230.tetris.model.gameplay;

import ru.zzz3230.tetris.dto.BlockData;
import ru.zzz3230.tetris.dto.GameRulesData;
import ru.zzz3230.tetris.exceptions.GameplayLogicException;
import ru.zzz3230.tetris.model.gameplay.eventsData.BaseEventData;
import ru.zzz3230.tetris.model.gameplay.eventsData.LinesClearedEventData;
import ru.zzz3230.tetris.model.gameplay.eventsData.MergeEventData;
import ru.zzz3230.tetris.model.gameplay.eventsData.NotifyEventData;
import ru.zzz3230.tetris.utils.Observable;

import java.awt.*;
import java.security.SecureRandom;

public class GameplayModel extends Observable<GameplayContext> {

    GameplayField gameplayField;

    GameplayField.GameplayFieldFragment nextBlock;

    final int ROWS = 24;
    final int COLS = 10;

    //BlockData[] tests;
    SecureRandom rand = new SecureRandom();

    final int LINES_TO_LEVEL_UP = 4;

    int gameLevel = 0;
    double score = 0;

    BlockData currentFallBlock;
    BlockData nextFallBlock;

    BlockOrderGenerator blockOrderGenerator;

    private boolean isPlaying = true;

    private int clearRowsTimer = -1;

    public GameplayModel() {
        gameplayField = new GameplayField();
        blockOrderGenerator = new BlockOrderGenerator(new GameRulesData());
    }

    private int[] emptyFilledRows(){
        return gameplayField.getStaticBlocks().clearFilledRows();
    }
    private int[] getFilledRows(){
        return gameplayField.getStaticBlocks().calculateFilledRows();
    }

    private void generateFallingBlock(){
        gameplayField.getFallingBlock().clear();
        var bl = blockOrderGenerator.nextBlock();
        if(nextFallBlock == null){
            nextFallBlock = blockOrderGenerator.nextBlock();
        }

        currentFallBlock = nextFallBlock;
        nextFallBlock = bl;
        boolean overridden = gameplayField.getFallingBlock().pasteBlock(
                currentFallBlock,
                0,
                rand.nextInt(0, COLS - currentFallBlock.width()));
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
            Color fallingBlockColor = currentFallBlock.color();
            gameplayField.getStaticBlocks().mergeFrom(gameplayField.getFallingBlock());

            generateFallingBlock();

            eventData = new MergeEventData(fallingBlockCenter, fallingBlockColor);
        } else {
            gameplayField.getFallingBlock().move(1, 0);
        }



        clearRowsTimer--;
        if(clearRowsTimer == 0){
            int[] cleared = emptyFilledRows();
            score += getScoreByClear(cleared.length);

            levelUpIfNeeded();

            eventData = new NotifyEventData(GameplayEventType.STATIC_BLOCKS_MOVED);

            clearRowsTimer = -1;
        }

        if(clearRowsTimer < 0){
            int[] filledRows = getFilledRows();
            if(filledRows.length != 0){
                clearRowsTimer = 2;
                eventData = new LinesClearedEventData(filledRows);
            }
        }

        notifyObserver(new GameplayContext(gameplayField, eventData, getScore(), getUpdateDelaySec()));
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

                score += moveDownScore(dy);

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
        notifyObserver(new GameplayContext(gameplayField, null, getScore(), getUpdateDelaySec()));
    }
    private void gameOverNotify(){
        notifyObserver(new GameplayContext(gameplayField, new NotifyEventData(GameplayEventType.GAME_OVER), getScore(), getUpdateDelaySec()));
    }

    public boolean isPlaying(){
        return isPlaying;
    }

    public float getUpdateDelaySec(){
        //float secDelay = 0.8f - 0.1f * gameLevel;
        float secDelay = (float)Math.exp(-gameLevel/5f);
        return secDelay;
    }

    public int getUpdateDelayMs(){
        //float secDelay = 0.8f + 1f / (gameLevel/10f + 1f) - 1f;
        return (int)(getUpdateDelaySec() * 1000);
    }
    public int getScore(){
        return (int)score;
    }

    private void levelUpIfNeeded(){
        if(score >= LINES_TO_LEVEL_UP * 100 * (gameLevel + 1)){
            gameLevel++;
        }
    }

    private float getScoreByClear(int lines){
        float base = lines * 100;
        float levelBonus = 10f * gameLevel;
        return switch (lines) {
            case 1 -> base + levelBonus;
            case 2 -> base + 3f * levelBonus;
            case 3 -> base + 4f * levelBonus;
            case 4 -> base + 6f * levelBonus;
            default -> base;
        };
    }

    private float moveDownScore(int dy){
        return (dy + gameLevel / 4f) / 4f;
    }
}
