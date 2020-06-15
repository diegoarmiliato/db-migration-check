package com.thomsonreuters.integration;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.apache.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class FileChecker {
    
    private File file;
    private Logger logger = Logger.getLogger(FileChecker.class);
    private JSONObject keywords = new JSONObject();
    public List<Integer> LogErrors = new ArrayList<Integer>();

    public FileChecker(File file) {
        this.file = file;
        try {
            JSONParser parser = new JSONParser();
            FileReader reader = new FileReader("src/main/resources/keywords.json");
            Object obj = parser.parse(reader);
            keywords = (JSONObject) obj;
        } catch (IOException|ParseException e) {
          logger.error(e);
        }        
    }

    public void run() throws IOException{
        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8));

        String line;
        int idx = 1;
        
        JSONArray matches = (JSONArray) keywords.get("match");
        JSONArray ignores = (JSONArray) keywords.get("ignore");

        while ((line = br.readLine()) != null) {
            final String fLine = line;
            final int fIdx = idx;
            matches.forEach(match -> {
                final String compare = match.toString().toLowerCase();
                if (fLine.toLowerCase().contains(compare)) {
                    Boolean isValid = true;
                    if (!ignores.isEmpty()) {
                        for (int ignoreIdx = 0; ignoreIdx  < ignores.size(); ignoreIdx++) {
                            final String exclude = ignores.get(ignoreIdx).toString().toLowerCase();
                            if (fLine.toLowerCase().contains(exclude)) {
                                isValid = false;
                                break;
                            }
                        }
                    }                    
                    if (isValid) {
                        LogErrors.add(fIdx);
                        System.out.println("Line " + fIdx + ": " + fLine); 
                    }
                }
            });
            idx++;
        }

        br.close();
    }
}