package pakk.api;

import java.io.IOException;
import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;

/**
 * Generic class to start our API server.
 * Reference can be found in official jersey examples: https://github.com/jersey/jersey
 */
public class App {
    private static final URI BASE_URI = URI.create("http://0.0.0.0:8080/");

    public static void main(String[] args) {
        try {
            System.out.println("Twitter API implementation using Jersey");

            final ResourceConfig resourceConfig = new ResourceConfig(TwitterResource.class);
	    resourceConfig.register(CORSResponseFilter.class);
            final HttpServer server = GrizzlyHttpServerFactory.createHttpServer(BASE_URI, resourceConfig, false);
            
            Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
                @Override
                public void run() {
                    server.shutdownNow();
                }
            }));
            server.start();

            System.out.println(String.format("Application started.\nTry out %s\nStop the application using CTRL+C",
                    BASE_URI));
            Thread.currentThread().join();
        } catch (IOException | InterruptedException ex) {
            Logger.getLogger(App.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
