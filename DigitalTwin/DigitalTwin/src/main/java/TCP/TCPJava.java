package TCP;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class TCPJava implements AutoCloseable {

    private final ServerSocket serverSocket = null;
    private static final Logger logger = LoggerFactory.getLogger(TCPJava.class);

    public Socket startSocket(int portNumber) {
        logger.info("Starting socket...");
        try {
            Socket socket = new Socket(InetAddress.getByName(null), portNumber);
            logger.info("Success!");
            return socket;
        } catch (IOException e) {
            logger.error("Server exception!");
            e.printStackTrace();
        }
        return null;
    }

    public String receiveMessage(Socket socket) {
        String message = null;
        logger.info("\n Receiving message...");
        try (
                BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8))
        ) {
            message = reader.readLine();

            logger.info("Message received! :" + message);
        } catch (IOException e) {
            logger.error("Error occured while receiving message!");
            e.printStackTrace();
        }
        return message;
    }

    public void sendMessage(Socket socket, String message) {
        PrintWriter printWriter;
        logger.info("Sending message...");
        try {
            printWriter = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8));
            printWriter.print(message);
            printWriter.flush();
            logger.info("Message sent! :" + message);
        } catch (IOException e) {
            logger.error("Error occured while sending message!");
            e.printStackTrace();
        }
    }

    @Override
    public void close() throws Exception {
        if (serverSocket != null) {
            serverSocket.close();
        }
    }
}
