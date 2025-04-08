package ru.zzz3230.tblib;

import java.io.*;

public class FileIO implements Closeable {
    private final BufferedReader charReader;
    private final BufferedWriter charWriter;

    private final FileOutputStream fileOutputStream;
    private final FileInputStream fileInputStream;

    public FileIO(File file, String charset) throws IOException {

        fileInputStream = new FileInputStream(file);
        this.charReader = new BufferedReader(new InputStreamReader(fileInputStream, charset));

        fileOutputStream = new FileOutputStream(file, true);
        this.charWriter = new BufferedWriter(new OutputStreamWriter(fileOutputStream, charset));
    }

    public BufferedReader getReader() {
        return charReader;
    }

    public BufferedWriter getWriter() {
        return charWriter;
    }

    public FileInputStream getRawReader(){
        return fileInputStream;
    }

    public FileOutputStream getRawWriter() {
        return fileOutputStream;
    }

    public void close() throws IOException {
        charReader.close();
        charWriter.close();
    }
}