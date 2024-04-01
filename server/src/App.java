import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 ```
 java -jar CZ4013Server.jar 8800 1
 ```
 Server will listen to port 8800 and choose AT_MOST_ONCE semantics as default

 ```
 java -jar CZ4013Server.jar 8800 2
 ```
 Server will listen to port 8800 and choose AT_LEAST_ONCE semantics as default

 ```
 java -jar CZ4013Server.jar 8800 2 5
 ```
 Server will listen to port 8800 and choose AT_LEAST_ONCE semantics as default. The first 5 replies will be lost. This feature is to simulate lost reply scenario.

 ```
 java -jar CZ4013Server.jar 8800 2 5 10
 ```
 Server will listen to port 8800 and choose AT_LEAST_ONCE semantics as default. The first 5 replies will be lost. And each reply will first delay 10 seconds before transmission. This feature is to simulate incomplete interaction scenario. *
 */
public class App
{
    static Logger logger = LogManager.getLogger(App.class.getName());

    public static void main( String[] args )
    {
        logger.entry();
        if(args.length == 0){
            new Server().start();
        }else if(args.length == 1){
            int port = Integer.parseInt(args[0]);
            new Server(port).start();
        }else if(args.length == 2){
            int  semantics = Integer.parseInt(args[1]);
            int port = Integer.parseInt(args[0]);
            new Server(port,semantics).start();
        }else if(args.length == 3){
            int  semantics = Integer.parseInt(args[1]);
            int port = Integer.parseInt(args[0]);
            int lostReplyCount = Integer.parseInt(args[2]);
            Util.lostReplyCount = lostReplyCount;
            new Server(port,semantics).start();

        }else if(args.length == 4){
            int  semantics = Integer.parseInt(args[1]);
            int port = Integer.parseInt(args[0]);
            int lostReplyCount = Integer.parseInt(args[2]);
            Util.lostReplyCount = lostReplyCount;
            int replyDelaySec = Integer.parseInt(args[3]);
            Util.replyDelaySec = replyDelaySec;
            new Server(port,semantics).start();

        }
        logger.exit();
    }
}