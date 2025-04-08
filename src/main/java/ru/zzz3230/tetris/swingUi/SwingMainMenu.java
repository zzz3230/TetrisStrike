package ru.zzz3230.tetris.swingUi;

import ru.zzz3230.tetris.controller.MainMenuController;
import ru.zzz3230.tetris.model.MainMenuContext;
import ru.zzz3230.tetris.utils.Observer;
import ru.zzz3230.tetris.utils.Page;

import javax.swing.*;
import java.awt.*;
import java.net.URL;

public class SwingMainMenu implements Observer<MainMenuContext>, SwingPage {

    JPanel rootPanel;

    public SwingMainMenu(MainMenuController controller) {
        SwingUtilities.invokeLater(() -> {
            rootPanel = new JPanel();
            BoxLayout layout = new BoxLayout(rootPanel, BoxLayout.Y_AXIS);
            rootPanel.setLayout(layout);

//            JTextField nicknameField = new JTextField();
//            nicknameField.setText("player_1337");
//            nicknameField.setAlignmentX(Component.CENTER_ALIGNMENT);
//            nicknameField.setAlignmentY(Component.TOP_ALIGNMENT);
//            rootPanel.add(nicknameField);

            rootPanel.add(Box.createVerticalGlue());

            JButton playBtn = new JButton("PLAY");
            playBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
            playBtn.addActionListener((e) -> {
                controller.startGame();
            });
            rootPanel.add(playBtn);

            JButton leaderboardBtn = new JButton();
            leaderboardBtn.setText("LEADERBOARD");
            leaderboardBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
            leaderboardBtn.addActionListener((e) -> {
                controller.showLeaderboard();
            });
            rootPanel.add(leaderboardBtn);

            rootPanel.add(Box.createVerticalGlue());
        });
    }

    @Override
    public void update(MainMenuContext context) {

    }

    @Override
    public JPanel getRootPanel() {
        return rootPanel;
    }
}
