package com.thomsonreuters.integration;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.nio.charset.StandardCharsets;
import java.util.List;

import javax.swing.JFileChooser;

import org.apache.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class FileChecker {
    
    private File[] files;
    private Logger logger = Logger.getLogger(FileChecker.class);
    private JSONObject keywords = new JSONObject();
    public List<Integer> LogErrors = new ArrayList<Integer>();

    // Class used to store the loaded files content
    private class LoadedFiles {
        private String FileName;
        private int LineSeq;
        private String LineTxt;

        private LoadedFiles(String FileName, int LineSeq, String LineTxt) {
            this.FileName = FileName;
            this.LineSeq = LineSeq;
            this.LineTxt = LineTxt;
        }
    }
    public ArrayList<LoadedFiles> LoadedFiles = new ArrayList<LoadedFiles>();

    public FileChecker(File directory) {
        // Defines the filter for files to select as .TXT and .LOG
        FilenameFilter filter = new FilenameFilter(){
        
            @Override
            public boolean accept(File dir, String name) {
                if (name.toLowerCase().endsWith(".txt") || name.toLowerCase().endsWith(".log")) {
                    return true;
                } else {
                    return false;
                }
            }
        };
        //Only selects files on the current directory which enter on the above declared filter
        this.files = directory.listFiles(filter);
        try {
            JSONParser parser = new JSONParser();
            InputStream is = getClass().getClassLoader().getResourceAsStream("keywords.json");
            InputStreamReader ir = new InputStreamReader(is);
            Object obj = parser.parse(ir);
            keywords = (JSONObject) obj;
        } catch (IOException|ParseException e) {
          logger.error(e);
        }        
    }

    public void run() throws IOException{
        // Reads listed files and load them into an ArrayList, avoiding to keep the file locked
        for (File file : files) {
            final BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8));
            int linNum = 1;
            String line;
            while ((line = br.readLine()) != null) {
                LoadedFiles.add(new LoadedFiles(file.getName(), linNum, line));
                linNum++;
            }
            br.close();
        }
        
        final JSONArray matches = (JSONArray) keywords.get("match");
        final JSONArray ignores = (JSONArray) keywords.get("ignore");

        for(int i = 0; i < LoadedFiles.size(); i++) {
            final LoadedFiles ldFile = LoadedFiles.get(i);
            String line = ldFile.LineTxt.toLowerCase();
            for (int ignoreIdx = 0; ignoreIdx  < ignores.size(); ignoreIdx++) {
                final String ignore = ignores.get(ignoreIdx).toString().toLowerCase();
                if (line.contains(ignore)) {
                    final String newLine = line.replace(ignore, "");
                    line = newLine;
                }
            }
            for (int matchIdx = 0; matchIdx  < matches.size(); matchIdx++) {
                final String match = matches.get(matchIdx).toString().toLowerCase();
                if (line.contains(match)) {
                    LogErrors.add(ldFile.LineSeq);
                    System.out.println("File " + ldFile.FileName + " - Line " + ldFile.LineSeq + ": " + ldFile.LineTxt); 
                }
            }
        }
    }

    public static void main (String[] args) {
        final JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        fileChooser.setAcceptAllFileFilterUsed(false);
        final int returnVal = fileChooser.showOpenDialog(null);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            final File filex = fileChooser.getSelectedFile();
            final FileChecker filec = new FileChecker(filex);
            try {
                filec.run();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}