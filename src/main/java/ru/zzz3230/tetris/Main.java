package ru.zzz3230.tetris;

import com.google.gson.Gson;
import com.jogamp.opengl.GLProfile;
import de.jcm.discordgamesdk.Core;
import de.jcm.discordgamesdk.CreateParams;
import de.jcm.discordgamesdk.activity.Activity;
import de.jcm.discordgamesdk.activity.ActivityButton;
import de.jcm.discordgamesdk.activity.ActivityButtonsMode;
import mdlaf.MaterialLookAndFeel;
import mdlaf.themes.MaterialOceanicTheme;

import java.awt.*;
import java.io.IOException;
import java.time.Instant;

import ru.zzz3230.activity.ActivityManager;
import ru.zzz3230.tblib.client.LocalClient;
import ru.zzz3230.tetris.controller.GameplayController;
import ru.zzz3230.tetris.controller.LeaderboardController;
import ru.zzz3230.tetris.controller.MainMenuController;
import ru.zzz3230.tetris.swingUi.SwingNavigationManager;
import ru.zzz3230.tetris.utils.NavigationManager;
import ru.zzz3230.tetris.utils.Utils;

import javax.swing.*;
import javax.swing.plaf.FontUIResource;

public class Main {


    public static void main(String[] args) {
        System.out.println("Hello, World!");

        try {
            JDialog.setDefaultLookAndFeelDecorated(true);
            JFrame.setDefaultLookAndFeelDecorated(false);
            //UIManager.setLookAndFeel(new MaterialLookAndFeel(new MaterialLiteTheme()));
            var lf = new MaterialLookAndFeel(new MaterialOceanicTheme());
            lf.getDefaults().put("defaultFont", new Font("Cascadia Code PL", Font.PLAIN, 12));
             UIManager.setLookAndFeel(lf);

             var res = ClassLoader.getSystemClassLoader().getResourceAsStream("fonts/CascadiaCodePL-Regular.otf");

            assert res != null;
            var font = new FontUIResource(Font.createFonts(res)[0].deriveFont(16.0f));
            Utils.setFont(font);

        } catch (UnsupportedLookAndFeelException | IOException | FontFormatException e) {
            throw new RuntimeException(e);
        }

        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(600, 700);
        frame.setLocationRelativeTo(null);


        NavigationManager navigationManager = new SwingNavigationManager(frame);
        var c = new MainMenuController(navigationManager, new ActivityManager(new LocalClient()), new TbClientManager());
        c.showView();

        frame.setVisible(true);
    }
}