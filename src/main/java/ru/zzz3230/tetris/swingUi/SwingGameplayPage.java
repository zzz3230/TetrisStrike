package ru.zzz3230.tetris.swingUi;

import ru.zzz3230.tetris.controller.GameplayController;
import ru.zzz3230.tetris.utils.NavigationManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class SwingGameplayPage implements SwingPage {
    JPanel rootPanel;

    private static final int ROWS = 20;   // количество строк в игровом поле
    private static final int COLS = 10;   // количество столбцов в игровом поле

    public SwingGameplayPage(GameplayController controller) {
        SwingUtilities.invokeLater(() -> {
            rootPanel = new JPanel();

            //rootPanel.add(new SwingGameplayForm());
            
            JButton btn = new JButton("Gameplay Test");
            btn.addActionListener((e) -> {
                controller.gotoMainMenu();
            });
            rootPanel.add(btn);


            // ---- Верхняя панель с кнопкой "Выход" ----
            JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            JButton exitButton = new JButton("Выход");
            topPanel.add(exitButton);


            // ---- Центральная панель (игровое поле) ----
            SwingGamePanel gamePanel = new SwingGamePanel(ROWS, COLS);

            // ---- Боковая панель (Next, Score, Level, Help) ----
            JPanel sidePanel = new JPanel();
            sidePanel.setLayout(new BoxLayout(sidePanel, BoxLayout.Y_AXIS));

            // Примерные метки (Label) для имитации блока "Next"
            JLabel nextLabel = new JLabel("Next:");
            nextLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            sidePanel.add(nextLabel);

            // Пример панели под следующую фигуру
            JPanel nextPiecePanel = new JPanel() {
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    // Здесь вы можете рисовать следующую фигуру
                    g.setColor(Color.BLUE);
                    g.fillRect(10, 10, 40, 40);
                }
            };
            nextPiecePanel.setPreferredSize(new Dimension(80, 80));
            sidePanel.add(nextPiecePanel);

            sidePanel.add(Box.createVerticalStrut(20)); // отступ

            // Примерные метки для Score и Level
            JLabel scoreLabel = new JLabel("Score: 0");
            scoreLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            sidePanel.add(scoreLabel);

            JLabel levelLabel = new JLabel("Level: 1");
            levelLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            sidePanel.add(levelLabel);

            sidePanel.add(Box.createVerticalStrut(20)); // отступ

            // Примерная "Help"-секция
            JLabel helpLabel = new JLabel("<html>Help:<br>Left: h / ←<br>Right: l / →<br>Down: j / ↓<br>Rotate: k / ↑<br>Drop: Space<br>Quit: q</html>");
            helpLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            sidePanel.add(helpLabel);

            // ---- Размещаем всё в основном окне ----
            rootPanel.add(topPanel, BorderLayout.NORTH);
            rootPanel.add(gamePanel, BorderLayout.CENTER);
            rootPanel.add(sidePanel, BorderLayout.EAST);



        });
    }

    @Override
    public JPanel getRootPanel() {
        return rootPanel;
    }
}
