package ru.zzz3230.tetris.swingUi;

import ru.zzz3230.tetris.utils.NavigationManager;
import ru.zzz3230.tetris.utils.Page;

import javax.swing.*;

public class SwingNavigationManager implements NavigationManager {

    private final JFrame frame;
    private SwingPage currentPage;

    public SwingNavigationManager(JFrame frame) {
        this.frame = frame;
    }

    @Override
    public void navigateTo(Page page) {
        if(!(page instanceof SwingPage swingPage)){
            throw new IllegalArgumentException("Page must be an instance of SwingPage");
        }

        SwingUtilities.invokeLater(() ->{
            if (currentPage != null) {
                currentPage.onDetached(frame);
            }
            frame.getContentPane().removeAll();
            frame.add(swingPage.getRootPanel());
            frame.setVisible(true);
            swingPage.onAttached(frame);
            currentPage = swingPage;
        });
    }
}
