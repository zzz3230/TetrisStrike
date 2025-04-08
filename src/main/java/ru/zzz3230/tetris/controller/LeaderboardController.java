package ru.zzz3230.tetris.controller;

import ru.zzz3230.activity.ActivityManager;
import ru.zzz3230.tblib.dto.LeaderboardData;
import ru.zzz3230.tblib.dto.LeaderboardEntry;
import ru.zzz3230.tetris.TbClientManager;
import ru.zzz3230.tetris.swingUi.SwingLeaderboardPage;
import ru.zzz3230.tetris.utils.Controller;
import ru.zzz3230.tetris.utils.NavigationManager;

import java.util.*;

public class LeaderboardController extends Controller {

    private final ActivityManager activityManager;
    private final TbClientManager clientManager;

    public LeaderboardController(NavigationManager navigationManager, ActivityManager activityManager, TbClientManager clientManager) {
        super(navigationManager);
        this.activityManager = activityManager;
        this.clientManager = clientManager;
        view = new SwingLeaderboardPage(this);
    }

    public void gotoMainMenu() {
        MainMenuController mainMenuController = new MainMenuController(navigationManager, activityManager, clientManager);
        mainMenuController.showView();
    }

    public List<LeaderboardEntry> getLeaderboard() {
        //ArrayList<LeaderboardEntry> leaderboard = new ArrayList<>();
//        leaderboard.add(new LeaderboardEntry(
//                "Player1",
//                100,
//                Date.from(Calendar.getInstance().toInstant())));
//
//        leaderboard.add(new LeaderboardEntry(
//                "User2",
//                95,
//                Date.from(Calendar.getInstance().toInstant())));
//
//        leaderboard.add(new LeaderboardEntry(
//                "Hello world",
//                32,
//                Date.from(Calendar.getInstance().toInstant())));
//
//        leaderboard.add(new LeaderboardEntry(
//                "Player3",
//                10,
//                Date.from(Calendar.getInstance().toInstant())));
//
//        leaderboard.add(new LeaderboardEntry(
//                "Player4",
//                1,
//                Date.from(Calendar.getInstance().toInstant())));

        return Arrays.stream(clientManager.getNetworkClient().getLeaderboard(LeaderboardData.Scope.GLOBAL).players()).toList();
    }
}
