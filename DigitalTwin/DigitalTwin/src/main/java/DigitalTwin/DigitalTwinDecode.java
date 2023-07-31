package DigitalTwin;

import TCP.TCPJava;
import de.ude.es.protocolabstraction.eipmessage.EIPMessage;
import de.ude.es.protocolabstraction.eipmessage.EIPMessage.Parameter;
import de.ude.es.protocolabstraction.eipmessage.EIPMessage.UriType;
import de.ude.es.twininterface.abstracttwin.AbstractTwin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.Socket;
import java.util.concurrent.TimeUnit;

public class DigitalTwinDecode extends AbstractTwin {

    public static final String TWIN_URI = "decoderTwin";
    private static final Logger logger = LoggerFactory.getLogger(DigitalTwinDecode.class);

    public DigitalTwinDecode() {
        // start communication Autoencoder
        startCommunication();
    }

    public String receiveDecodedData(String encodedData) {
        try (TCPJava decoderConnection = new TCPJava(); Socket socket = decoderConnection.startSocket(3142)) {
            if (socket == null) {
                logger.error("Error initializing socket");
                return null;
            }
            logger.info("Sending encoded data to decoder...");
            decoderConnection.sendMessage(socket, encodedData);
            TimeUnit.SECONDS.sleep(2);
            logger.info("Receiving decoded data from decoder...");
            return decoderConnection.receiveMessage(socket);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private void sendDecodedData(String decodedData) {
        Parameter[] parameter = new Parameter[]{new Parameter("decodedData", decodedData)};
        EIPMessage callTwin1 = new EIPMessage("eip://" + DigitalTwinUser.TWIN_URI + "/receiveData", UriType.POST,
                parameter);
        logger.info("Sending decoded data to userTwin...");
        callURI(callTwin1);
    }

    public void startCommunication() {
        String namespace = "eip://";
        String resolverURI = "database";
        String broker = "localhost";

        initialise(broker, TWIN_URI, resolverURI, namespace);
        start();
    }

    @Override
    public void handleURI(EIPMessage eipMessage) {
        String uri = eipMessage.value;
        switch (uri) {
            case "eip://" + TWIN_URI + "/decode":
                // Funktion, die Daten decodiert
                String decodedData = receiveDecodedData(eipMessage.parameters[0].value.toString());
                sendDecodedData(decodedData);
                break;
            default:
                logger.error("URI wurde nicht definiert!");
                break;
        }
    }

}