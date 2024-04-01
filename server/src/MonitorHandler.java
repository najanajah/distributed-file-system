import java.net.InetAddress;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class MonitorHandler implements RequestHandler {
    static Logger logger = LogManager.getLogger(MonitorHandler.class.getName());
    /**
     * Key: The file path
     * Value: a set of MonitoringClientInfo that monitors this file
     */
    private Map<Path, Set<MonitoringClientInfo>> monitoringInfo =
            new HashMap<>();


    public MonitorHandler(Map<Path, Set<MonitoringClientInfo>> monitoringInfo) {
        super();
        this.monitoringInfo = monitoringInfo;
    }


    @Override
    public Map<String, Object> handleRequest(Map<String, Object> request, InetAddress client) {
        logger.entry();
//////////////////////////////////////////////////////////////
//Retrieve and validate parameters

        //Check for code field
        List<String> missingFields = new LinkedList<String>();

        if(request.get("code") == null){
            missingFields.add("code");
        }
        if(request.get("path") == null){
            missingFields.add("path");
        }
        if(request.get("port") == null){
            missingFields.add("port");
        }
        if(request.get("duration") == null){
            missingFields.add("duration");
        }
        if(missingFields.size() > 0){
            return Util.errorPacket(Util.missingFieldMsg(missingFields));
        }


        if(!(request.get("code") instanceof Integer)){
            return Util.errorPacket(Util.inconsistentFieldTypeMsg("code", "integer"));
        }
        int code = (Integer)request.get("code");

        if(code != 3){
            String msg = Util.inconsistentReqCodeMsg("Monitor", 3);
            logger.fatal(msg);
            return Util.errorPacket(msg);
        }

        //Check for path field

        if(!(request.get("path") instanceof String)){
            return Util.errorPacket(Util.inconsistentFieldTypeMsg("path", "String"));
        }

        String file = (String)request.get("path");
        Path monitoredPath = Paths.get(file);

        if(!monitoredPath.toFile().exists()){
            String msg = Util.nonExistFileMsg(file);
            logger.error(msg);
            return Util.errorPacket(msg);
        }

        //Get port attribute

        if(!(request.get("port") instanceof Integer)){
            return Util.errorPacket(Util.inconsistentFieldTypeMsg("port", "integer"));
        }
        int port = (Integer)request.get("port");

        //Get duration attribute

        if(!(request.get("duration") instanceof Long)){
            return Util.errorPacket(Util.inconsistentFieldTypeMsg("duration", "long"));
        }
        long duration = (Long)request.get("duration");
//////////////////////////////////////////////////////////////

//////////////////////////////////////////////////////////////
//Add a 	MonitoringClientInfo record to the monitored file
        long expiration = System.currentTimeMillis() + duration;
        MonitoringClientInfo clientInfo = new MonitoringClientInfo(client, port, expiration);

        Set<MonitoringClientInfo> monitoringClients = this.monitoringInfo.get(monitoredPath);
        if(monitoringClients == null){
            monitoringClients = new HashSet<MonitoringClientInfo>();
        }
        monitoringClients.add(clientInfo);

        logger.info("Add " + monitoredPath + " to File " + file + " monitoringList");
        this.monitoringInfo.put(monitoredPath, monitoringClients);
//////////////////////////////////////////////////////////////

//////////////////////////////////////////////////////////////
//Construct the reply
        Map<String,Object> reply = new HashMap<>();
        reply.put("status"	, Integer.valueOf(1));
        reply.put("end", Long.valueOf(expiration));
        reply.put("message", "Monitoring File " + file + " Started.");
        logger.exit();
        return reply;

    }

}