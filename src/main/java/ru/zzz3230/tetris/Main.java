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
        //System.out.println(System.getProperty("user.home"));

//        LocalClient client = new LocalClient();
//        try {
//            var auth = client.getFile(".auth0");
//
//            var file = client.getFile("StrikeTetris/settings.json");
//            Gson gson = new Gson();
//            gson.fromJson(file.getReader(), Main.class);
//
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }

// Set parameters for the Core



//        try {
//            var pair = EccTools.generateECCKeyPair();
//            System.out.println(pair.getPublic());
//            System.out.println(Arrays.toString(pair.getPrivate().getEncoded()));
//
//        } catch (NoSuchAlgorithmException e) {
//            throw new RuntimeException(e);
//        }

//        TbConnection conn = new TbConnection("game-tetris-backend", "F2RHTute.qv2MBaUW");
//        conn.initialize();
//
//        try {
//            conn.sendData("update_score", Map.of("player", "zzz3230", "score", "1000"))
//                    .thenAccept(
//                    x -> {
//                        System.out.println("Update_score finished with " + x.code());
//                    }
//            );
//
//        } catch (ConnectionException e) {
//            throw new RuntimeException(e);
//        }
//
//        try {
//            Thread.sleep(10000);
//        } catch (InterruptedException e) {
//            throw new RuntimeException(e);
//        }

//
        try {
            JDialog.setDefaultLookAndFeelDecorated(true);
            JFrame.setDefaultLookAndFeelDecorated(false);
            //UIManager.setLookAndFeel(new MaterialLookAndFeel(new MaterialLiteTheme()));
            var lf = new MaterialLookAndFeel(new MaterialOceanicTheme());
            lf.getDefaults().put("defaultFont", new Font("Cascadia Code PL", Font.PLAIN, 12));
             UIManager.setLookAndFeel(lf);

             var res = ClassLoader.getSystemClassLoader().getResourceAsStream("fonts/CascadiaCodePL-Regular.otf");

            assert res != null;
            var font  =new FontUIResource(Font.createFonts(res)[0].deriveFont(16.0f));
            Utils.setFont(font);
            //Utils.setUIFont();

        } catch (UnsupportedLookAndFeelException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (FontFormatException e) {
            throw new RuntimeException(e);
        }

        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(600, 700);
        frame.setLocationRelativeTo(null);


        ;

        //new Thread(GLProfile::initSingleton);

        NavigationManager navigationManager = new SwingNavigationManager(frame);
        var c = new MainMenuController(navigationManager, new ActivityManager(new LocalClient()), new TbClientManager());
        c.showView();

        frame.setVisible(true);


        //var mm = new MainMenuForm();
        //frame.add(mm.getRootPanel());



        //mm.onAttached(frame);

//        frame.add(new JButton("Button"));
//
//        MainMenuController c = new MainMenuController((NavigationManager<SwingPage>) new SwingNavigationManager());
//
//        frame.setVisible(true);
//
//        SwingUtilities.invokeLater(() -> {
//            System.out.println("SwingUtilities.invokeLater");
//        });
//
//        System.out.println("args = " + Arrays.toString(args));
    }
}