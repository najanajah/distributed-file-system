import java.net.InetAddress;
import java.util.Map;

/**
 * The genric request handler interface
 * @author ruanpingcheng
 *
 */
public interface RequestHandler {
    public Map<String,Object> handleRequest(Map<String,Object> request,
                                            InetAddress client);
}