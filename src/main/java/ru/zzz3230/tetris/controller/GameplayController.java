package ru.zzz3230.tetris.controller;

import ru.zzz3230.activity.ActivityManager;
import ru.zzz3230.tetris.TbClientManager;
import ru.zzz3230.tetris.model.gameplay.GameplayModel;
import ru.zzz3230.tetris.swingUi.SwingGameplayForm;
import ru.zzz3230.tetris.utils.Controller;
import ru.zzz3230.tetris.utils.NavigationManager;


public class GameplayController extends Controller {
    private final ActivityManager activityManager;
    private final TbClientManager clientManager;
    Thread gameLoopThread;
    volatile boolean runningGame = true;
    GameplayModel model;

    public GameplayController(NavigationManager navigationManager, ActivityManager activityManager, TbClientManager clientManager) {
        super(navigationManager);
        this.activityManager = activityManager;
        this.clientManager = clientManager;
        model = new GameplayModel();
        var gameplayPage = new SwingGameplayForm(this);
        model.setObserver(gameplayPage);
        this.view = gameplayPage;


        startGame();
    }

    public void startGame(){
        gameLoopThread = new Thread(this::gameLoop);
        gameLoopThread.start();
    }

    private void gameLoop() {
        while (model.isPlaying()) {
            try {
                Thread.sleep(model.getUpdateDelayMs());
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            model.update();
        }
        finishGame();
    }

    private void finishGame(){
        if(clientManager.getNetworkClient().alreadyLoggedIn()){
            clientManager.getNetworkClient().gameFinished(model.getScore());
        }
    }

    public void moveFallingBlock(int dx, int dy){
        model.moveFallingBlock(dx, dy);
    }
    public void rotateFallingBlock(int dRot){
        model.rotateFallingBlock(dRot);
    }

    public void gotoMainMenu() {
        MainMenuController mainMenuController = new MainMenuController(navigationManager, activityManager, clientManager);
        mainMenuController.showView();
    }
}
