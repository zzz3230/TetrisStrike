package ru.zzz3230.tetris.swingUi;

import ru.zzz3230.tetris.controller.LeaderboardController;

import javax.swing.*;

public class SwingLeaderboardPage implements SwingPage{
    private final LeaderboardController controller;
    JPanel rootPanel;
    public SwingLeaderboardPage(LeaderboardController controller) {
        this.controller = controller;
        SwingUtilities.invokeLater(() -> {
            rootPanel = new JPanel();
            rootPanel.setLayout(new BoxLayout(rootPanel, BoxLayout.Y_AXIS));
            JLabel label = new JLabel("Leaderboard");
            rootPanel.add(label);

            JButton btn = new JButton("Back to Main Menu");
            btn.addActionListener((e) -> {
                controller.gotoMainMenu();
            });
            rootPanel.add(btn);


            JPanel listPanel = new JPanel();
            listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
            controller.getLeaderboard().forEach((entry) -> {
                JLabel labelEntry = new JLabel(entry.username() + " - " + entry.score());
                listPanel.add(labelEntry);
            });

            rootPanel.add(listPanel);

        });
    }

    @Override
    public JPanel getRootPanel() {
        return rootPanel;
    }
}
