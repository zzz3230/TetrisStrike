package ru.zzz3230.activity;

import de.jcm.discordgamesdk.Core;
import de.jcm.discordgamesdk.CreateParams;
import de.jcm.discordgamesdk.Result;
import de.jcm.discordgamesdk.activity.Activity;
import de.jcm.discordgamesdk.activity.ActivityButton;
import de.jcm.discordgamesdk.activity.ActivityButtonsMode;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.zzz3230.tblib.FileIO;
import ru.zzz3230.tblib.client.LocalClient;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.plaf.IconUIResource;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.time.Instant;
import java.util.function.Consumer;

public class ActivityManager {
    Logger log = LogManager.getLogger(ActivityManager.class);

    private final LocalClient client;
    private volatile Core core;
    private Activity activity;

    private volatile boolean isActivityAvailable = false;
    private volatile boolean isDiscordRunning = false;
    private boolean isInitialized = false;

    public ActivityManager(LocalClient client) {
        this.client = client;
    }

    public boolean isInitialized() {
        return isInitialized;
    }

    public void init(Runnable callback) {
        if(isInitialized){
            return;
        }
        new Thread(() -> {
            try(CreateParams params = new CreateParams())
            {
                params.setClientID(1358066107654869115L);
                params.setFlags(CreateParams.getDefaultFlags());
                try{
                    core = new Core(params);

                }
                catch (Exception ex){
                    log.error(ex.getMessage());
                    log.info("Discord is not running");
                    isActivityAvailable = false;
                    callback.run();
                    return;
                }

                isDiscordRunning = core.isDiscordRunning();
                isActivityAvailable = isDiscordRunning;
            }

            System.out.println(core.userManager().getCurrentUser().getUserId());
            System.out.println(core.userManager().getCurrentUser().getAvatar());
            callback.run();
        }).start();

        isInitialized = true;
    }

    public boolean isAvailable() {
        return isDiscordRunning;
    }

    public String getUsername(){
        if(!isActivityAvailable){
            return null;
        }

        return core.userManager().getCurrentUser().getUsername();
    }

    public void beginExternalAuth(Consumer<String> callback) {
        core.applicationManager().authenticate((a, b) ->{
            if(a != Result.OK){
                callback.accept(null);
            }
            callback.accept(b.token().accessToken());
        });
    }

    public String getUserId(){
        if(core == null){
            return null;
        }

        return String.valueOf(core.userManager().getCurrentUser().getUserId());
    }

    public Image getAvatar(){
        if(!isActivityAvailable){
            return null;
        }

        var avatarId = core.userManager().getCurrentUser().getAvatar();
        var userId = core.userManager().getCurrentUser().getUserId();

        var avatar = "https://cdn.discordapp.com/avatars/" + userId + "/" + avatarId + ".png?size=32";

        try {
            client.downloadFile(avatar, "avatar.png");
        } catch (IOException e) {
            System.err.println("Could not download avatar");
        }

        try {
            return ImageIO.read(client.getFile("avatar.png").getRawReader());
        } catch (IOException e) {
            return null;
        }
    }

    public void setStatus(String status) {
        if(!isActivityAvailable || activity == null){
            return;
        }
        activity.setDetails(status);
        core.activityManager().updateActivity(activity);
    }

    public void startShowActivityIfAvailable(){
        if(!isActivityAvailable){
            return;
        }

        activity = new Activity();

        //activity.addButton(new ActivityButton("Play!", "https://example.com"));
        //activity.setActivityButtonsMode(ActivityButtonsMode.SECRETS);

        activity.setDetails("Starting game");
        activity.setState("and having fun");

        activity.timestamps().setStart(Instant.now());

        new Thread(() -> {
            while(true)
            {
                core.runCallbacks();
                try
                {
                    // Sleep a bit to save CPU
                    Thread.sleep(2000);
                }
                catch(InterruptedException e)
                {
                    throw new RuntimeException(e);
                }
            }
        }).start();
    }

    public String getAvatarId() {
        return core.userManager().getCurrentUser().getAvatar();
    }
}
