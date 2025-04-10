package ru.zzz3230.tetris.controller;

import ru.zzz3230.activity.ActivityManager;
import ru.zzz3230.tblib.dto.LeaderboardData;
import ru.zzz3230.tblib.dto.LeaderboardEntry;
import ru.zzz3230.tetris.TbClientManager;
import ru.zzz3230.tetris.model.LeaderboardContext;
import ru.zzz3230.tetris.swingUi.SwingLeaderboardPage;
import ru.zzz3230.tetris.utils.Controller;
import ru.zzz3230.tetris.utils.NavigationManager;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;

public class LeaderboardController extends Controller {

    private final ActivityManager activityManager;
    private final TbClientManager clientManager;

    public LeaderboardController(NavigationManager navigationManager, ActivityManager activityManager, TbClientManager clientManager) {
        super(navigationManager);
        this.activityManager = activityManager;
        this.clientManager = clientManager;
        var page = new SwingLeaderboardPage(this);
        view = page;

        var players = clientManager.getNetworkClient().getLeaderboard(LeaderboardData.Scope.GLOBAL);
        HashMap<Integer, Image> avatars = new HashMap<>();
        for (LeaderboardEntry entry : players.players()) {
            avatars.put(entry.aid(), clientManager.getNetworkClient().getAvatar(entry.aid()));
        }

        page.update(new LeaderboardContext(players, avatars));
    }

    public void gotoMainMenu() {
        MainMenuController mainMenuController = new MainMenuController(navigationManager, activityManager, clientManager);
        mainMenuController.showView();
    }

    public List<LeaderboardEntry> getLeaderboard() {
        return Arrays.stream(clientManager.getNetworkClient().getLeaderboard(LeaderboardData.Scope.GLOBAL).players()).toList();
    }
}
