import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.net.InetAddress;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class InsertHandler implements RequestHandler {
    static Logger logger = LogManager.getLogger(InsertHandler.class.getName());

    @Override
    public Map<String, Object> handleRequest(Map<String, Object> request, InetAddress client) {
//////////////////////////////////////////////////////////////
//Validate and retrieve parameters
        logger.entry();
        List<String> missingFields = new LinkedList<String>();
        if(request.get("code") == null){
            missingFields.add("code");
        }
        if(request.get("path") == null){
            missingFields.add("path");
        }
        if(request.get("offset") == null){
            missingFields.add("offset");
        }
        if(request.get("insertion") == null){
            missingFields.add("insertion");
        }
        if(missingFields.size() > 0){
            return Util.errorPacket(Util.missingFieldMsg(missingFields));
        }

        if(!(request.get("code") instanceof Integer)){
            return Util.errorPacket(Util.inconsistentFieldTypeMsg("code", "integer"));
        }
        int code = (Integer)request.get("code");

        if(code != 2){
            String msg = Util.inconsistentReqCodeMsg("Insert", 2);
            logger.fatal(msg);
            return Util.errorPacket(msg);
        }

        //Check for path field

        if(!(request.get("path") instanceof String)){
            return Util.errorPacket(Util.inconsistentFieldTypeMsg("path", "String"));
        }

        String file = (String)request.get("path");


        if(!(request.get("offset") instanceof Integer)){
            return Util.errorPacket(Util.inconsistentFieldTypeMsg("offset", "Integer"));
        }
        int offset = (Integer)request.get("offset");

        if(!(request.get("insertion") instanceof String)){
            return Util.errorPacket(Util.inconsistentFieldTypeMsg("insertion", "String"));
        }

        String insertedContents = (String)request.get("insertion");

        String content = null;
//////////////////////////////////////////////////////////////

//////////////////////////////////////////////////////////////
//Perform the insertion
        try{
            Path filePath = Paths.get(file);
            File reqFile = filePath.toFile();
            Scanner fileScanner = new Scanner(reqFile);
            content = fileScanner.useDelimiter("\\Z").next();
            fileScanner.close();


            if(offset > content.length()){
                String msg = "Offset " + offset + " exceeds file "
                        + file + " length (" + content.length() + ").";
                return Util.errorPacket(msg);
            }

            StringBuffer buffer = new StringBuffer(content);
            buffer.insert(offset,insertedContents);

            BufferedWriter bw = new BufferedWriter(new FileWriter(reqFile));
            bw.write(buffer.toString());
            bw.close();

        }catch(InvalidPathException e){
            String msg = Util.invalidPathMsg(file);
            logger.error(msg);
            return Util.errorPacket(msg);
        }catch (FileNotFoundException e) {
            String msg = Util.nonExistFileMsg(file);
            logger.error(msg);
            return Util.errorPacket(msg);
        } catch (IOException e) {
            String msg = "Internal IO Exception: " + e.getMessage();
            logger.error(msg);
            return Util.errorPacket(msg);
        }

//////////////////////////////////////////////////////////////

//////////////////////////////////////////////////////////////
//Construct the reply message
        Map<String,Object> reply =
                Util.successPacket("File " + file + " Insertion Succeeded.");


        logger.exit();
        return reply;
    }

}