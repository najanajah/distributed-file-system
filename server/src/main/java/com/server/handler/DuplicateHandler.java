package com.server.handler;

import com.server.constant.Constants;
import com.server.exception.ListTypeMismatchException;
import com.server.helper.ListTypeChecker;
import com.server.helper.Util;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.net.InetAddress;
import java.nio.file.Paths;
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
        String destinationPath = (String) request.get(3);

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

            // check if the destination path is already taken
            File destinationFile = new File(destinationPath);
            if (destinationFile.exists()) {
                String msg = "Renamed file " + destinationFile + " already exists";
                logger.error(msg);
                return Util.errorPacket(msg);
            }

            Scanner fileScanner = new Scanner(sourceFile);
            content = fileScanner.useDelimiter("\\Z").next();
            fileScanner.close();

            // create the parent folder for destination file if doesn't exist
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
