import DB.HistoricalData;
import DigitalTwin.CompositeAreaTwin;
import DigitalTwin.DigitalTwinDecode;
import DigitalTwin.DigitalTwinUser;
import Helper.DigitalTwinUserHelper;
import REST.CORSFilter;
import REST.FileRequestHandler;
import Settings.SettingsUtil;
import com.sun.net.httpserver.HttpServer;
import org.glassfish.jersey.jdkhttp.JdkHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.Scanner;

public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        DigitalTwinUser digitalTwinUser = new DigitalTwinUser();
        DigitalTwinUserHelper.digitalTwinUser = digitalTwinUser;
        //decoderTwin runs on port 3142
        DigitalTwinDecode digitalTwinDecode = new DigitalTwinDecode();
        CompositeAreaTwin compositeAreaTwin1 = new CompositeAreaTwin("1", 3141);
        digitalTwinUser.startPeriodicRequests("1");
        try {
            Thread.sleep(30000);
        } catch (InterruptedException e1) {
            e1.printStackTrace();
        }
        CompositeAreaTwin compositeAreaTwin3 = new CompositeAreaTwin("3", 3143);
        digitalTwinUser.startPeriodicRequests("3");
        startServer();
    }

    private static void startServer() {
        //Web Server for REST API and GUI
        ResourceConfig resourceConfig = new ResourceConfig(DigitalTwinUserHelper.class, HistoricalData.class,
                CORSFilter.class, SettingsUtil.class);
        try (Scanner scanner = new Scanner(System.in)) {
            URI baseUri = new URI("http://localhost:8080/rest/");
            HttpServer server = JdkHttpServerFactory.createHttpServer(baseUri, resourceConfig, false);
            server.start();
            server.createContext("/", new FileRequestHandler()); // for Web-GUI
            logger.info("Press Enter to stop the Server");
            scanner.nextLine();
            logger.info("Shutting down ...");
            server.stop(0);
            logger.info("Server stopped");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}