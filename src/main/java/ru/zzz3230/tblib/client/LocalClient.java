package ru.zzz3230.tblib.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.zzz3230.tblib.FileIO;
import ru.zzz3230.tblib.exceptions.ClientException;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;

public class LocalClient {
    private static final Logger log = LoggerFactory.getLogger(LocalClient.class);
    private String tbDataPath;
    private boolean isReady = false;
    private LocalAuthData localAuthData;

    public static record LocalAuthData(String integrationTag, String username, String jwt) {}

    public LocalClient() {
        this.tbDataPath = System.getProperty("user.home") + "/.tbdatastorage";
        if(!new java.io.File(tbDataPath).exists()){
            if(!new java.io.File(tbDataPath).mkdirs()){
                throw new ClientException("Unable to create '" + this.tbDataPath + "' directory");
            }
        }
        isReady = true;
    }

    public boolean isFileExist(String path) {
        String fileName = tbDataPath + "/" + path;
        java.io.File file = new java.io.File(fileName);
        return file.exists();
    }

    public FileIO getFile(String path) throws IOException {
        if(!isReady){
            throw new ClientException("Client is not ready");
        }
        String fileName = tbDataPath + "/" + path;
        java.io.File file = new java.io.File(fileName);

        if(!file.exists() && !file.createNewFile()){
            throw new ClientException("File '" + path + "' does not exist and cannot be created");
        }
        try {
            return new FileIO(file, "UTF-8");
        } catch (java.io.FileNotFoundException e) {
            throw new ClientException("File '" + path + "' not found");
        }
    }


    public String downloadFile(String url, String name) throws IOException {
        try {
            URL website = URI.create(url).toURL();
            try(ReadableByteChannel rbc = Channels.newChannel(website.openStream());
                FileOutputStream fos = new FileOutputStream(tbDataPath + "/" + name)){
                fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
            }
            catch (IOException e) {
                throw new IOException(e);
            }

        } catch (MalformedURLException e) {
            throw new IOException(e);
        }
        return tbDataPath + "/" + name;
    }

    public LocalAuthData getLocalAuthData() {
        if(localAuthData != null){
            return localAuthData;
        }
        if(!isFileExist(".auth0")){
            try {
                getFile(".auth0");
            } catch (IOException e) {
                log.error(e.getMessage());
            }
            localAuthData = new LocalAuthData(null, null, null);
            return localAuthData;
        }

        try(FileIO file = getFile(".auth0")){
            String[] data = file.getReader().lines().toArray(String[]::new);
            if(data.length != 3){
                log.error("Invalid local auth data, recreating");
                localAuthData = new LocalAuthData(null, null, null);
                return localAuthData;
            }
            localAuthData = new LocalAuthData(data[0], data[1], data[2]);
        } catch (IOException e) {
            throw new ClientException(e.getMessage());
        }
        return localAuthData;
    }
    public void saveLocalAuthData(){
        if(localAuthData == null){
            throw new ClientException("Local auth data is null");
        }
        try {
            FileIO file = getFile(".auth0");
            file.getWriter().write(localAuthData.integrationTag + "\n" + localAuthData.username + "\n" + localAuthData.jwt);
            file.close();
        } catch (IOException e) {
            throw new ClientException(e.getMessage());
        }
    }
    public void setLocalAuthData(LocalAuthData localAuthData) {
        this.localAuthData = localAuthData;
    }
}
