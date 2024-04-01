import java.net.InetAddress;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class AtMostOnceHandler implements RequestHandler {
    static Logger logger = LogManager.getLogger(AtMostOnceHandler.class.getName());

    //key: Client IP + Request Timestamp; value; original reply
    private Map<String, Map<String,Object>> responseCache = null;
    private RequestHandler nextRqHdler = null;

    public AtMostOnceHandler(Map<String, Map<String, Object>> responseCache, RequestHandler nextRqHdler) {
        super();
        this.responseCache = responseCache;
        this.nextRqHdler = nextRqHdler;
    }

    @Override
    public Map<String, Object> handleRequest(Map<String, Object> request, InetAddress client) {
        logger.entry();
//////////////////////////////////////////////////////////////
//Validate and retrieve tweets
        List<String> missingFields = new LinkedList<String>();

        if(request.get("time") == null){

            missingFields.add("time");
        }

        if(missingFields.size() > 0){
            return Util.errorPacket(Util.missingFieldMsg(missingFields));
        }

        if(!(request.get("time") instanceof Long)){
            return Util.errorPacket(Util.inconsistentFieldTypeMsg("time", "long"));
        }

        long requestTime = (Long)request.get("time");
//////////////////////////////////////////////////////////////

//////////////////////////////////////////////////////////////
        //Search in the cache
        String key = client.getHostName() + "." + requestTime;
        Map<String,Object> preReply = this.responseCache.get(key);

        if(preReply != null) {
            //return the cached reply if found
            logger.info("Find the cached reply " + preReply + " for " + key);
            return preReply;
        }
//////////////////////////////////////////////////////////////

//////////////////////////////////////////////////////////////
//Pass the request to next response handler
        Map<String,Object> nextResponse = this.nextRqHdler.handleRequest(request, client);

//Update the cache using the client ip and timestamp
        this.responseCache.put(key, nextResponse);
        logger.info("Cache the reply " + nextResponse + " for client " + key);

        logger.exit();
        return nextResponse;
    }

}