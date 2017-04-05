import io.undertow.Undertow;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;

import java.io.*;

import java.lang.management.ManagementFactory;     
import com.sun.management.OperatingSystemMXBean;   

public class CoreServer {
    // Instance variables
    private static final String NODE_TYPE = "core";
    private static final int currLevel = 1;

    public static void main(final String[] args) {
        Undertow server = Undertow.builder()
                .addListener(8080, "localhost")
                .setHandler(new HttpHandler() {
                    @Override
                    public void handleRequest(final HttpServerExchange exchange) throws Exception {
                        if (exchange == null || exchange.isInIoThread()) {
                            exchange.dispatch(this);
                            return;
                        }

                        // Setup response headers
                        exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "text/plain");
                        
                        // Get path and process the request
                        String path = exchange.getRequestPath();

                        // Request for heartbeat
                        if (path.equals("/heartbeat")) {
                            exchange.getResponseSender().send("Alive:\t" + getGPUUtil());
                            return;
                        }

                        // Request for core
                        if (path.equals("/core") && NODE_TYPE.equals("core")) {

                            // Invalid request
                            if (exchange.getQueryParameters().get("level") == null) {
                                exchange.getResponseSender().send("Invalid request path for core");
                                return;
                            }

                            // Parse parameters
                            String levelStr = exchange.getQueryParameters().get("level").getFirst();
                            int level = 0;
                            try {
                                level = Integer.parseInt(levelStr);
                            } catch (NumberFormatException e) {
                                e.printStackTrace();
                                exchange.getResponseSender().send("NumberFormatException in level");
                            }
                            System.out.println("level: " + level);
                            return;
                        } 

                        // Request for master node
                        if (path.equals("/master") && NODE_TYPE.equals("master")) {
                            exchange.getResponseSender().send("Request for master shouldn't be sent to master");
                            return;
                        } 

                        // Invalid requeset
                        exchange.getResponseSender().send("Invalid request path, should be " + NODE_TYPE);
                    }
                }).build();
        server.start();
        System.out.println("Started server at http://127.1:8080/  Hit ^C to stop");
    }

    private static String getGPUUtil() {
        OperatingSystemMXBean osmb = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean(); 
        return String.format("%.4f", osmb.getSystemLoadAverage());
    }
}
