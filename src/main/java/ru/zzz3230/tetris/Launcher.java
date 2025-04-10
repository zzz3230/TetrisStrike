package ru.zzz3230.tetris;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

public class Launcher {
    public static File getRunningJarFile() {
        try {
            return new File(
                    Launcher.class.getProtectionDomain()
                            .getCodeSource()
                            .getLocation()
                            .toURI()
            );
        } catch (URISyntaxException e) {
            throw new RuntimeException("Не удалось определить путь к jar-файлу", e);
        }
    }

    public static void main(String[] args) {
        File jarFile = getRunningJarFile();
        String javaBin = System.getProperty("java.home") + "/bin/java";
        String mainClass = "ru.zzz3230.tetris.Main";  // <-- другой main

        ProcessBuilder builder = new ProcessBuilder(
                javaBin,
                "--add-exports=java.base/java.lang=ALL-UNNAMED",
                "--add-exports=java.desktop/sun.awt=ALL-UNNAMED",
                "--add-exports=java.desktop/sun.java2d=ALL-UNNAMED",
                "-cp",
                jarFile.getAbsolutePath(),
                mainClass
        );

        builder.inheritIO();

        try {
            Process process = builder.start();
            int exitCode = process.waitFor();
            System.out.println("Процесс завершился с кодом: " + exitCode);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}