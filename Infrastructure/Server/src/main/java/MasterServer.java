import io.undertow.Undertow;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;

public class MasterServer {
    // Instance variables
    private static final String NODE_TYPE = "master";
    private static DataModel dataModel;
    private static HeartBeatModel heartBeatModel;
    private static boolean hasInitDataModel = false;
    private static boolean hasInitHeartBeatModel = false;
    private static final int MAX_RETRY_TIMES = 3;
    private static final int SLEEP_INTERVAL = 10000;
    private static final int RETRY_INTERVAL = 1000;
    private static Map<String, Integer> healthMap = new HashMap<String, Integer>();
    private static boolean hasInitMap = false;
    
    private static String[] coreURL new String[]{
        "localhost:8080"
    };

    public static void main(final String[] args) {

        // Init model
        try {
            if (!hasInitDataModel) {
                dataModel = new DataModel();
            }
            hasInitDataModel = true;
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            if (!hasInitHeartBeatModel) {
                heartBeatModel = new HeartBeatModel();
            }
            hasInitHeartBeatModel = true;
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (!hasInitMap) {
            for (String url : coreURL) {
                healthMap.put(url, false);
            }
            hasInitMap = true;
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

                        // Request for health of cores
                        if (path.equals("/health")) {
                            // Add the health of each server to sb
                            StringBuilder sb = new StringBuilder();
                            for (String url : coreURL) {
                                sb.append(url);
                                sb.append("\t");

                                if (healthMap.get(url) == 0) {
                                    sb.append("GREAT");
                                } else if (healthMap.get(url) == 1) {
                                    sb.append("NORMAL");
                                } else {
                                    sb.append("FAILED");
                                }
                            
                                sb.append("\n");
                            }
                            exchange.getResponseSender().send(sb.toString());
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

    /**
     * Check for heart beat
     */
    private void checkHeartBeat() throws Exception {

        Runnable runnable = new Runnable() {
            public void run() {

                while (true) {
                    
                    for (String url : coreURL) {
                        // Maximum retry times for each server
                        int i = 0;
                        for (; i < MAX_RETRY_TIMES; i++) {
                            URL obj = new URL(url);
                            HttpURLConnection con = (HttpURLConnection) obj.openConnection();

                            // Optional, default is GET
                            con.setRequestMethod("GET");

                            int responseCode = con.getResponseCode();

                            // If invalid now, then wait for retry
                            if (responseCode != 200) {
                                try {
                                    Thread.sleep(RETRY_INTERVAL);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                                continue;
                            }

                            System.out.println("\nSending 'GET' request to URL : " + url);
                            System.out.println("Response Code : " + responseCode);

                            // Get response
                            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
                            String inputLine;
                            StringBuffer response = new StringBuffer();

                            while ((inputLine = in.readLine()) != null) {
                                response.append(inputLine);
                            }

                            in.close();

                            // Get util
                            double util = 0D;
                            String utilStr = sb.toString().trim().split("\t")[1].trim();
                            try {
                                level = Double.parseDouble(utilStr);
                            } catch (NumberFormatException e) {
                                e.printStackTrace();
                                exchange.getResponseSender().send("NumberFormatException in heartbeat");
                            }
                            // Insert heartbeat data into database
                            heartBeatModel.insert(util);
                        }

                        if (i == MAX_RETRY_TIMES) {
                            // If this is the second time that the core is down, then mark as failed
                            if (healthMap.get(url) == 0) {
                                healthMap.put(url, 1);
                            } else if (healthMap.get(url) == 1) {
                                healthMap.put(url, 2);
                            }
                        }
                    }

                    try {
                        Thread.sleep(SLEEP_INTERVAL);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        };

        Thread thread = new Thread(runnable);
        thread.start();
        }
    }
}
