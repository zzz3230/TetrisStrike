package ru.zzz3230.tetris.swingUi;

import ru.zzz3230.tetris.controller.GameplayController;

import javax.swing.*;
import java.awt.event.*;
import java.util.HashMap;
import java.util.Map;

public class TetrisKeyListener {
    private final GameplayController controller;
    private final Timer fallTimer;
    private final Map<Integer, KeyPressHandler> keyHandlers = new HashMap<>();

    // Настраиваемые задержки (в миллисекундах)
    private final int initialDelay = 200;  // Задержка перед первым повторением
    private final int repeatDelay = 30;   // Задержка между повторениями

    public TetrisKeyListener(GameplayController controller, Timer fallTimer) {
        this.controller = controller;
        this.fallTimer = fallTimer;

        // Инициализация обработчиков для каждой клавиши
        initKeyHandlers();
    }

    private void initKeyHandlers() {
        // Движение влево (A или LEFT) - с автоповтором
        keyHandlers.put(KeyEvent.VK_A, new KeyPressHandler(
                () -> controller.moveFallingBlock(-1, 0),
                true // enable repeat
        ));
        keyHandlers.put(KeyEvent.VK_LEFT, keyHandlers.get(KeyEvent.VK_A));

        // Движение вправо (D или RIGHT) - с автоповтором
        keyHandlers.put(KeyEvent.VK_D, new KeyPressHandler(
                () -> controller.moveFallingBlock(1, 0),
                true // enable repeat
        ));
        keyHandlers.put(KeyEvent.VK_RIGHT, keyHandlers.get(KeyEvent.VK_D));

        // Поворот против часовой (Q или UP) - БЕЗ автоповтора
        keyHandlers.put(KeyEvent.VK_Q, new KeyPressHandler(
                () -> controller.rotateFallingBlock(-1),
                false // disable repeat
        ));
        keyHandlers.put(KeyEvent.VK_UP, keyHandlers.get(KeyEvent.VK_Q));

        // Поворот по часовой (E) - БЕЗ автоповтора
        keyHandlers.put(KeyEvent.VK_E, new KeyPressHandler(
                        () -> controller.rotateFallingBlock(1),
                false // disable repeat
        ));

        // Ускоренное падение (S или DOWN) - особый случай (использует fallTimer)
        keyHandlers.put(KeyEvent.VK_S, new KeyPressHandler(
                () -> fallTimer.start(),
                false // repeat handled by fallTimer
        ));
        keyHandlers.put(KeyEvent.VK_DOWN, keyHandlers.get(KeyEvent.VK_S));
    }

    public boolean dispatchKeyEvent(KeyEvent e) {
        KeyPressHandler handler = keyHandlers.get(e.getKeyCode());
        if (handler == null) return false;

        if (e.getID() == KeyEvent.KEY_PRESSED) {
            handler.keyPressed();
        }
        else if (e.getID() == KeyEvent.KEY_RELEASED) {
            handler.keyReleased();
            // Особый случай для клавиш ускоренного падения
            if (e.getKeyCode() == KeyEvent.VK_DOWN || e.getKeyCode() == KeyEvent.VK_S) {
                fallTimer.stop();
            }
        }

        return false;
    }

    private class KeyPressHandler {
        private Timer repeatTimer;
        private final Runnable action;
        private final boolean enableRepeat;

        public KeyPressHandler(Runnable action, boolean enableRepeat) {
            this.action = action;
            this.enableRepeat = enableRepeat;

            if (enableRepeat) {
                this.repeatTimer = new Timer(repeatDelay, ev -> action.run());
                this.repeatTimer.setInitialDelay(initialDelay);
            }
        }

        public void keyPressed() {
            // Выполняем действие сразу при нажатии
            action.run();

            // Запускаем таймер для повторения только если разрешено
            if (enableRepeat && repeatTimer != null) {
                repeatTimer.start();
            }
        }

        public void keyReleased() {
            if (repeatTimer != null) {
                repeatTimer.stop();
            }
        }
    }
}