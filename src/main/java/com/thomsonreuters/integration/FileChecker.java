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
    private static final Logger logger = Logger.getLogger(FileChecker.class);
    private JSONObject keywords = new JSONObject();
    private boolean errors = false;
    
    public static class LogErrors {
        public String FileName;
        public int LineSeq;
        
        public LogErrors(String FileName, int LineSeq) {
            this.FileName = FileName;
            this.LineSeq = LineSeq;
        }
    }
    public List<LogErrors> LogErrors = new ArrayList<LogErrors>();

    // Class used to store the loaded files content
    public static class LoadedFiles {
        public String FileName;
        public int LineSeq;
        public String LineTxt;
        public String LineStatus;

        public LoadedFiles(String FileName, int LineSeq, String LineTxt) {
            this.FileName = FileName;
            this.LineSeq = LineSeq;
            this.LineTxt = LineTxt;
        }
        
        public LoadedFiles(String FileName, int LineSeq) {
            this.FileName = FileName;
            this.LineSeq = LineSeq;
        }
    }
    public List<LoadedFiles> LoadedFiles = new ArrayList<LoadedFiles>();
    
    public static class Files {
        public String FileName;
        public String FileStatus;
        
        public Files(String FileName, String FileStatus) {
            this.FileName = FileName;
            this.FileStatus = FileStatus;
        }
    }    
    public List<Files> Files = new ArrayList<Files>();

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
        this.errors = false;
        // Reads listed files and load them into an ArrayList, avoiding to keep the file locked
        for (File file : files) {
            final BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8));
            Files flList = new Files(file.getName(), "S");            
            Files.add(flList);
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
                    if (!this.errors) { this.errors = true; }
                    setFileWithError(ldFile.FileName);
                    LogErrors.add(new LogErrors(ldFile.FileName, ldFile.LineSeq));
                    ldFile.LineStatus = "E";
                    LoadedFiles.set(i, ldFile);
                    logger.debug("File " + ldFile.FileName + " - Line " + ldFile.LineSeq + ": " + ldFile.LineTxt); 
                }
            }
        }
    }
    
    public void setFileWithError(String FileName) {
        for (int i = 0; i < Files.size(); i++) {
            final Files File = Files.get(i);
            if (File.FileName.matches(FileName)) {
                File.FileStatus = "E";
                Files.set(i, File);
            }
        }
    }
    
    public boolean hasErrors() {
        return this.errors;
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
                logger.error(e);
            }
        }
    }
}