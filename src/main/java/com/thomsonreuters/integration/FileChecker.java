package com.thomsonreuters.integration;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class FileChecker {

    private File file;

    public FileChecker(File file) {
        this.file = file;
    }

    public void run() throws IOException{
        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8));

        String line;
        int idx = 1;

        while ((line = br.readLine()) != null) {
            System.out.println("Line " + idx + ": " + line);
            idx++;
        }

        br.close();
    }
}