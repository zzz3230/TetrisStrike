package ru.zzz3230.tetris.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.zzz3230.activity.ActivityManager;
import ru.zzz3230.activity.AuthProvider;
import ru.zzz3230.tblib.client.LocalClient;
import ru.zzz3230.tetris.TbClientManager;
import ru.zzz3230.tetris.model.MainMenuContext;
import ru.zzz3230.tetris.model.MainMenuModel;
import ru.zzz3230.tetris.swingUi.MainMenuForm;
import ru.zzz3230.tetris.swingUi.SwingMainMenu;
import ru.zzz3230.tetris.swingUi.SwingNavigationManager;
import ru.zzz3230.tetris.utils.*;

import java.awt.*;
import java.awt.image.RescaleOp;
import java.io.StringReader;
import java.util.Locale;

public class MainMenuController extends Controller {
    private static final Logger log = LoggerFactory.getLogger(MainMenuController.class);
    private final ActivityManager activityManager;
    private final TbClientManager clientManager;
    private final MainMenuModel model;

    public MainMenuController(NavigationManager navigationManager, ActivityManager activityManager, TbClientManager clientManager) {
        super(navigationManager);
        this.activityManager = activityManager;
        this.clientManager = clientManager;
        model = new MainMenuModel();
        var mainMenu = new MainMenuForm(this);
        model.setObserver(mainMenu);
        this.view = mainMenu;


        if(activityManager.isInitialized()){
            if(clientManager.getNetworkClient().alreadyLoggedIn()){
                playAsOnline();
            }
            else {
                playAsOffline();
            }

            return;
        }

        activityManager.init(() -> {
            if(!isActive()){
                return;
            }

            if(tryCachedAuth()){
                playAsOnline();
                return;
            }

            AuthProvider[] providers = new AuthProvider[2];
            providers[0] = AuthProvider.OFFLINE;

            if(activityManager.isAvailable()){
                providers[1] = AuthProvider.DISCORD;
            }

            mainMenu.showWelcomeDialog(providers,provider -> {
                if(provider == AuthProvider.DISCORD){
                    String token = tryGetTokenViaDiscord();
                    if(token == null){
                        boolean status = tryRegisterViaDiscord();
                        if(!status){
                            playAsOffline();
                            return;
                        }

                        token = tryGetTokenViaDiscord();
                    }

                    if(token == null){
                        playAsOffline();
                        return;
                    }

                    saveToken(token);
                    playAsOnline();
                }
                else if(provider == AuthProvider.OFFLINE){
                    playAsOffline();
                }
            });
        });
    }

    private void saveToken(String token){
        String userId = activityManager.getUserId();
        assert userId != null;

        clientManager.getLocalClient().setLocalAuthData(new LocalClient.LocalAuthData(
                userId,
                clientManager.getNetworkClient().getUsername(),
                token
        ));
        clientManager.getLocalClient().saveLocalAuthData();
    }

    private boolean tryCachedAuth(){
        String userId = activityManager.getUserId();

        if(userId == null || clientManager.getNetworkClient().validateCachedCredentials(userId)){
            if(clientManager.getNetworkClient().tryLoginByCachedCredentials()){
                return true;
            }
        }

        return false;
    }

    private String tryGetTokenViaDiscord(){
        String userId = activityManager.getUserId();
        assert userId != null;
        return clientManager.getNetworkClient().discordLogin(userId).orElse(null);
    }

    private boolean tryRegisterViaDiscord(){
        return clientManager.getNetworkClient().discordRegister(
                activityManager.getUserId(),
                activityManager.getUsername(),
                activityManager.getAvatarId(),
                activityManager.getUserId()
        );
    }

    private void playAsOffline(){
        model.setUsername(null);
    }
    private void playAsOnline(){
        model.setUsername(clientManager.getNetworkClient().getUsername());
        Image rawAvatar = clientManager.getNetworkClient().getAvatar(clientManager.getNetworkClient().getAid());
        Image scaledAvatar = rawAvatar.getScaledInstance(32, 32, java.awt.Image.SCALE_SMOOTH);
        model.setAvatar(scaledAvatar);
    }

    public void startGame() {
        GameplayController gameplayController = new GameplayController(navigationManager, activityManager, clientManager);
        hideView();
        gameplayController.showView();
    }

    public void showLeaderboard() {
        LeaderboardController leaderboardController = new LeaderboardController(navigationManager, activityManager, clientManager);
        hideView();
        leaderboardController.showView();
    }

    public void startAuthVia(AuthProvider provider){

    }
}
