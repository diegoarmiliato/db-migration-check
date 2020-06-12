package com.thomsonreuters.integration;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import javax.swing.JFileChooser;

import org.apache.log4j.Logger;

public class Init 
{
    public static void main( String[] args )
    {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

        Logger logger = Logger.getLogger(Init.class);

        logger.info("Processing Initiated at " + dtf.format(LocalDateTime.now()));

        final JFileChooser fileChooser = new JFileChooser();
        int returnVal = fileChooser.showOpenDialog(null);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            try {
                FileChecker fchk = new FileChecker(file);
                fchk.run();
            } catch (IOException e) {
                logger.error(e);
            }
        }

        try {
            Connection con = DatabaseChecker.getConnection();
            con.close();
        } catch (ClassNotFoundException|SQLException e) {
            logger.error(e);
        }
    }
}
