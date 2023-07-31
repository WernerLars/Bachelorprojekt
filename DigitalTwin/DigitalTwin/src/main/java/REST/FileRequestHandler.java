package REST;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;

public class FileRequestHandler implements HttpHandler {
    private static final Logger logger = LoggerFactory.getLogger(FileRequestHandler.class);

    public void handle(HttpExchange exchange) throws IOException {
        String filePath = "/Web" + exchange.getRequestURI().getPath();
        if (exchange.getRequestURI().getPath().equals("/"))
            filePath = "/Web/index.html"; //default file
        URL file = getClass().getResource(filePath);
        byte[] content;
        int returnCode;
        //if file exists
        if (file != null) {
            returnCode = 200;
            content = file.openStream().readAllBytes();
        } else {
            returnCode = 404;
            content = "<h1>404 - File not found</h1>".getBytes();
        }

        logger.debug("Requested file: " + exchange.getRequestURI());
        exchange.sendResponseHeaders(returnCode, content.length);
        OutputStream output = exchange.getResponseBody();
        output.write(content);
        output.close();
    }
}
