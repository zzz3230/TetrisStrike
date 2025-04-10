package ru.zzz3230.tetris.swingUi;

import ru.zzz3230.tetris.utils.Page;

import javax.swing.*;

public interface SwingPage extends Page {
    JPanel getRootPanel();
    default void onAttached(JFrame frame){};
    default void onDetached(JFrame frame){};
}
