package ru.zzz3230.tetris.swingUi;

import ru.zzz3230.tblib.dto.LeaderboardEntry;
import ru.zzz3230.tetris.controller.LeaderboardController;
import ru.zzz3230.tetris.model.LeaderboardContext;
import ru.zzz3230.tetris.utils.Observer;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;

public class SwingLeaderboardPage implements SwingPage, Observer<LeaderboardContext> {
    private final LeaderboardController controller;
    JPanel rootPanel;

    JPanel worldRecordsPanel;

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


            worldRecordsPanel = new JPanel();
            worldRecordsPanel.setLayout(new BoxLayout(worldRecordsPanel, BoxLayout.Y_AXIS));
            worldRecordsPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

            JScrollPane scrollPane = new JScrollPane(worldRecordsPanel);
            scrollPane.setBorder(null);
            rootPanel.add(scrollPane);

        });
    }

    public static JPanel createRecordPanel(Image avatar, String nickname, String score, String date) {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout(10, 10));
        panel.setPreferredSize(new Dimension(360, 66));
        panel.setMaximumSize(new Dimension(360, 66));
        panel.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(60, 60, 60), 1, true),
                new EmptyBorder(8, 8, 8, 8)
        ));
        panel.setBackground(new Color(40, 40, 40));

        // Аватар
        JLabel avatarLabel = new JLabel();
        avatarLabel.setIcon(new ImageIcon(avatar.getScaledInstance(44, 44, Image.SCALE_SMOOTH)));
        avatarLabel.setPreferredSize(new Dimension(44, 44));
        panel.add(avatarLabel, BorderLayout.WEST);

        // Текстовая часть
        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BorderLayout());
        textPanel.setOpaque(false);

        // Верхняя строка: ник
        JLabel nicknameLabel = new JLabel(nickname);
        nicknameLabel.setForeground(Color.WHITE);

        // Нижняя строка: рекорд + дата (в один ряд, чтобы не обрезалось)
        JPanel scoreDatePanel = new JPanel(new BorderLayout());
        scoreDatePanel.setOpaque(false);

        JLabel scoreLabel = new JLabel(score);
        scoreLabel.setForeground(new Color(102, 217, 239));

        JLabel dateLabel = new JLabel(date);
        dateLabel.setForeground(new Color(160, 160, 160));
        dateLabel.setHorizontalAlignment(SwingConstants.RIGHT);

        scoreDatePanel.add(scoreLabel, BorderLayout.WEST);
        scoreDatePanel.add(dateLabel, BorderLayout.EAST);

        textPanel.add(nicknameLabel, BorderLayout.NORTH);
        textPanel.add(scoreDatePanel, BorderLayout.SOUTH);

        panel.add(textPanel, BorderLayout.CENTER);

        return panel;
    }

    @Override
    public JPanel getRootPanel() {
        return rootPanel;
    }

    @Override
    public void update(LeaderboardContext context) {
        SwingUtilities.invokeLater(() -> {
            worldRecordsPanel.removeAll();

            for (LeaderboardEntry player : context.data().players()) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");

                worldRecordsPanel.add(
                        createRecordPanel(
                                context.avatars().get(player.aid()),
                                player.username(),
                                String.valueOf(player.score()),
                                formatter.format(player.date().toInstant().atZone(ZoneId.systemDefault()))
                        )
                );
                worldRecordsPanel.add(Box.createVerticalStrut(10));
            }
        });
    }
}
