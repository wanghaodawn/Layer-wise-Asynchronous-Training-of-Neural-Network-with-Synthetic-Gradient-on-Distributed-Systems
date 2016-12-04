import io.undertow.Undertow;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;

import java.net.HttpURLConnection;
import java.net.URL;

import java.util.*;
import java.io.*;

public class MasterServer {
    // Global variables
    private static final String NODE_TYPE = "master";

    // Input Data Model
    private static boolean hasInitInputDataModel = false;
    private static InputDataModel inputDataModel;
    private static final String inputDataTableName = "true_input_table";
    
    // Gradient Data Model
    private static boolean hasInitGradientDataModel = false;
    private static GradientDataModel gradientDataModel;
    private static final String gradientDataTableName = "true_gradient_table";


    public static void main(final String[] args) {

        // Init InputDataModel
        try {
            if (!hasInitInputDataModel) {
                inputDataModel = new InputDataModel(inputDataTableName);
            }
            inputDataModel.dropAllItems();
            hasInitInputDataModel = true;
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Init GradientDataModel
        try {
            if (!hasInitGradientDataModel) {
                gradientDataModel = new GradientDataModel(gradientDataTableName);
            }
            gradientDataModel.dropAllItems();
            hasInitGradientDataModel = true;
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Server
        Undertow server = Undertow.builder()
                .addListener(8000, "localhost")
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

                        // Request for master node
                        if (path.equals("/insert_true_input")) {
                            handleInsertTrueInput(exchange);
                        } else if (path.equals("/insert_true_gradient")) {
                            handleInsertTrueGradient(exchange);
                        } else if (path.equals("/get_true_input")) {
                            handleGetTrueInput(exchange);
                        } else if (path.equals("/get_true_gradient")) {
                            handleGetTrueGradient(exchange);
                        } else {
                            // Invalid requeset
                            exchange.getResponseSender().send("Invalid request path");
                        }
                    }
                }).build();
        server.start();

        System.out.println("Started server at http://127.0.0.1:8080/  Hit ^C to stop");
    }


    /**
     * Handle Insert True Input
     */
    public static void handleInsertTrueInput(final HttpServerExchange exchange) throws Exception {
        // Missing any important information or not
        if (exchange.getQueryParameters().get("level") == null || 
            exchange.getQueryParameters().get("iteration") == null || 
            exchange.getQueryParameters().get("true_input") == null) {
            exchange.getResponseSender().send("Invalid request path for master");
            return;
        }

        // Parse parameters
        String levelStr = exchange.getQueryParameters().get("level").getFirst();
        String iterationStr = exchange.getQueryParameters().get("iteration").getFirst();

        int level = 0, iteration = 0;
        try {
            level = Integer.parseInt(levelStr);
            iteration = Integer.parseInt(iterationStr);
        } catch (NumberFormatException e) {
            e.printStackTrace();
            exchange.getResponseSender().send("NumberFormatException in level or iteration");
            return;
        }

        String true_input = exchange.getQueryParameters().get("true_input").getFirst();

        // Insert the record into database
        inputDataModel.insert(level, iteration, true_input);

        // Debug
        System.out.println("handleInsertTrueInput:");
        System.out.println("level: " + level + "\titeration: " + iteration);
        System.out.println("true_input: " + true_input);
        System.out.println();

        exchange.getResponseSender().send("Success");
    }


    /**
     * Handle Insert True Gradient
     */
    public static void handleInsertTrueGradient(final HttpServerExchange exchange) throws Exception {
        // Missing any important information or not
        if (exchange.getQueryParameters().get("level") == null || 
            exchange.getQueryParameters().get("iteration") == null ||
            exchange.getQueryParameters().get("true_gradient") == null) {
            exchange.getResponseSender().send("Invalid request path for master");
            return;
        }

        // Parse parameters
        String levelStr = exchange.getQueryParameters().get("level").getFirst();
        String iterationStr = exchange.getQueryParameters().get("iteration").getFirst();

        int level = 0, iteration = 0;
        try {
            level = Integer.parseInt(levelStr);
            iteration = Integer.parseInt(iterationStr);
        } catch (NumberFormatException e) {
            e.printStackTrace();
            exchange.getResponseSender().send("NumberFormatException in level or iteration");
            return;
        }

        String true_gradient = exchange.getQueryParameters().get("true_gradient").getFirst();

        // Insert the record into database
        gradientDataModel.insert(level, iteration, true_gradient);

        // Debug
        System.out.println("handleInsertTrueGradient:");
        System.out.println("level: " + level + "\titeration: " + iteration);
        System.out.println("true_gradient: " + true_gradient);
        System.out.println();

        exchange.getResponseSender().send("Success");
    }


    /**
     * Handle Get True Input
     */
    public static void handleGetTrueInput(final HttpServerExchange exchange) throws Exception {
        // Missing any important information or not
        if (exchange.getQueryParameters().get("level") == null || 
            exchange.getQueryParameters().get("iteration") == null) {
            exchange.getResponseSender().send("Invalid request path for master");
            return;
        }

        // Parse parameters
        String levelStr = exchange.getQueryParameters().get("level").getFirst();
        String iterationStr = exchange.getQueryParameters().get("iteration").getFirst();

        int level = 0, iteration = 0;
        try {
            level = Integer.parseInt(levelStr);
            iteration = Integer.parseInt(iterationStr);
        } catch (NumberFormatException e) {
            e.printStackTrace();
            exchange.getResponseSender().send("NumberFormatException in level or iteration");
            return;
        }

        // Insert the record into database
        String true_input = inputDataModel.get(level, iteration);
        if (true_input == null) {
            true_input = "";
        }
        exchange.getResponseSender().send(true_input);

        // Debug
        System.out.println("handleGetTrueInput:");
        System.out.println("level: " + level + "\titeration: " + iteration);
        System.out.println("true_input: " + true_input);
        System.out.println();
    }


    /**
     * Handle Get True Gradient
     */
    public static void handleGetTrueGradient(final HttpServerExchange exchange) throws Exception {
        // Missing any important information or not
        if (exchange.getQueryParameters().get("level") == null || 
            exchange.getQueryParameters().get("iteration") == null) {
            exchange.getResponseSender().send("Invalid request path for master");
            return;
        }

        // Parse parameters
        String levelStr = exchange.getQueryParameters().get("level").getFirst();
        String iterationStr = exchange.getQueryParameters().get("iteration").getFirst();

        int level = 0, iteration = 0;
        try {
            level = Integer.parseInt(levelStr);
            iteration = Integer.parseInt(iterationStr);
        } catch (NumberFormatException e) {
            e.printStackTrace();
            exchange.getResponseSender().send("NumberFormatException in level or iteration");
            return;
        }

        // Insert the record into database
        String true_gradient = gradientDataModel.get(level, iteration);
        if (true_gradient == null) {
            true_gradient = "";
        }
        exchange.getResponseSender().send(true_gradient);

        // Debug
        System.out.println("handleGetTrueGradient:");
        System.out.println("level: " + level + "\titeration: " + iteration);
        System.out.println("true_gradient: " + true_gradient);
        System.out.println();
    }
}
