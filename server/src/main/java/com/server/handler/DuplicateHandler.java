package com.server.handler;

import com.server.constant.Constants;
import com.server.exception.ListTypeMismatchException;
import com.server.helper.ListTypeChecker;
import com.server.helper.Util;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.net.InetAddress;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Scanner;

public class DuplicateHandler implements RequestHandler {
    static Logger logger = LogManager.getLogger(DuplicateHandler.class.getName());

    @Override
    public List<Object> handleRequest(List<Object> request, InetAddress client, int clientPort) {
        logger.entry();

        // validate parameters
        try {
            ListTypeChecker.check(request, Constants.DuplicateServiceExpectedRequestFormat);
        } catch (ListTypeMismatchException e) {
            return Util.errorPacket(e.getMessage());
        }

        // retrieve parameters
        char requestType = (char) request.get(0);
        int requestId = (Integer) request.get(1);
        String sourcePath = (String) request.get(2);
//        String destinationPath = (String) request.get(3);

        String content;

        // perform the duplication
        try {
            File sourceFile = new File(sourcePath);

            // check if the source file exists
            if (!sourceFile.exists()) {
                String msg = Util.nonExistFileMsg(sourcePath);
                logger.error(msg);
                return Util.errorPacket(msg);
            }

            // duplicate the file at the same folder as the source file with the current time
            // appended at the back of the source file name to make it unique

            // get current time as the suffix of filename
            LocalDateTime currentTime = LocalDateTime.now();
            // define the time format
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm-ss");
            String formattedTime = currentTime.format(formatter);
            String destinationFilePath = sourcePath.replace(".", "_" + formattedTime + ".");

            File destinationFile = new File(destinationFilePath);

            Scanner fileScanner = new Scanner(sourceFile);
            content = fileScanner.useDelimiter("\\Z").next();
            fileScanner.close();

            // create the parent folder for destination file if it doesn't exist
            if (destinationFile.getParentFile() != null && !destinationFile.getParentFile().exists()) {
                destinationFile.getParentFile().mkdirs();
            }

            BufferedWriter bw = new BufferedWriter(new FileWriter(destinationFile));
            bw.write(content);
            bw.close();
        } catch (FileNotFoundException e) {
            String msg = Util.nonExistFileMsg(e.getMessage());
            logger.error(msg);
            return Util.errorPacket(msg);
        } catch (IOException e) {
            String msg = "Internal IO Exception: " + e.getMessage();
            logger.error(msg);
            return Util.errorPacket(msg);
        }

        // construct the reply message
        List<Object> reply = Util.successPacket("File " + sourcePath + " Duplication Succeeded.");

        logger.exit();
        return reply;
    }
}
