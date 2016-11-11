import io.undertow.Undertow;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;

public class MasterServer {
    // Instance variables
    private static final String NODE_TYPE = "master";
    private static Model model;
    private static boolean hasInit = false;

    public static void main(final String[] args) {

        // Init model
        try {
            if (!hasInit) {
                model = new Model();
            }
            hasInit = true;
        } catch (Exception e) {
            e.printStackTrace();
        }

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
                            exchange.getResponseSender().send("Alive");
                            return;
                        }

                        // Request for core
                        if (path.equals("/core") && NODE_TYPE.equals("core")) {
                            exchange.getResponseSender().send("Request for core shouldn't be sent to master");
                        } 

                        // Request for master node
                        if (path.equals("/master") && NODE_TYPE.equals("master")) {

                            // Invalid request
                            if (exchange.getQueryParameters().get("level") == null || 
                                exchange.getQueryParameters().get("x") == null || 
                                exchange.getQueryParameters().get("w") == null || 
                                exchange.getQueryParameters().get("y") == null) {
                                exchange.getResponseSender().send("Invalid request path for master");
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
                                return;
                            }

                            String xStr = exchange.getQueryParameters().get("x").getFirst();
                            String wStr = exchange.getQueryParameters().get("w").getFirst();
                            String yStr = exchange.getQueryParameters().get("y").getFirst();

                            // Insert the record into database
                            model.insert(level, xStr, wStr, yStr);

                            System.out.println("level: " + level);
                            System.out.println("x: " + xStr);
                            System.out.println("w: " + wStr);
                            System.out.println("y: " + yStr);
                            System.out.println();

                            return;
                        } 
                        
                        // Invalid requeset
                        exchange.getResponseSender().send("Invalid request path, should be " + NODE_TYPE);
                    }
                }).build();
        server.start();
        System.out.println("Started server at http://127.1:8080/  Hit ^C to stop");
    }
}
