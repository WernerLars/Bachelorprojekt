package DigitalTwin;

import DB.WeatherEntryDAO;
import TCP.TCPJava;
import de.ude.es.protocolabstraction.eipmessage.EIPMessage;
import de.ude.es.protocolabstraction.eipmessage.EIPMessage.Parameter;
import de.ude.es.protocolabstraction.eipmessage.EIPMessage.UriType;
import de.ude.es.twininterface.abstracttwin.AbstractTwin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class CompositeAreaTwin extends AbstractTwin {

    private final CountDownLatch latch = new CountDownLatch(1);
    private static final Logger logger = LoggerFactory.getLogger(CompositeAreaTwin.class);

    private final String areaCode;
    private final int socketPort;
    private List<String> dataEntries = new ArrayList<String>();

    public CompositeAreaTwin(String areaCode, int socketPort) {
        this.areaCode = areaCode;
        this.socketPort = socketPort;
        startCommunication();
    }

    @Override
    public void handleURI(EIPMessage eipMessage) {
        String uri = eipMessage.value;
        if (uri.equals("eip://" + getTwinURI() + "/requestData")) {
            Thread requestDataThread = new Thread() {
                @Override
                public void run() {
                    logger.info("Sending data requests to IoT devices...");
                    EIPMessage requestChildData = new EIPMessage("eip://" + getTwinURI() + "/requestChildData",
                            UriType.GET);
                    callURI(requestChildData);
                    try {
                        latch.await();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    synchronized (dataEntries) {
                        if (dataEntries.size() != 5) {
                            logger.error("Error requesting data from devices!");
                            return;
                        }
                        logger.info("Received all data entries!");
                        String encodedData = receiveEncodedData();
                        saveEntries(dataEntries);
                        if (encodedData != null) {
                            sendEncodedData(encodedData);
                        }
                    }
                    dataEntries = new ArrayList<String>();
                }
            };
            requestDataThread.start();
        } else if (uri.equals("eip://" + getTwinURI() + "/receiveIOTDeviceData")) {
            addDataEntry((String) eipMessage.parameters[0].value);
        } else {
            logger.error("URI wurde nicht definiert!");
        }
    }

    private synchronized void saveEntries(List<String> dataEntriesAsJson) {
        WeatherEntryDAO dao = new WeatherEntryDAO();
        synchronized (dataEntries) {
            for (String entryAsJson : dataEntriesAsJson) {
                dao.save(WeatherEntryDAO.getEntry4Values(entryAsJson.replaceAll("\\[|\\]", "").split(",")));
            }
        }
    }

    private synchronized void addDataEntry(String dataEntry) {
        dataEntries.add(dataEntry);
        logger.info("Received dataEntry: {}", dataEntry);
        if (dataEntries.size() == 5) {
            latch.countDown();
        }
    }

    private String receiveEncodedData() {
        if (dataEntries != null) {
            try (TCPJava encoderCommunication = new TCPJava();
                 Socket socket = encoderCommunication.startSocket(socketPort)) {
                if (dataEntries == null || socket == null) {
                    return null;
                }
                logger.info("Sending data to AutoEncoder...");
                encoderCommunication.sendMessage(socket, dataEntries.toString());
                // reset dataentries
                dataEntries = new ArrayList<String>();
                TimeUnit.SECONDS.sleep(2);
                logger.info("Receiving encoded data...");
                return encoderCommunication.receiveMessage(socket);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    private void sendEncodedData(String encodedData) {
        Parameter[] parameter = new Parameter[]{new Parameter("encodedData", encodedData)};
        EIPMessage callDecoder = new EIPMessage("eip://" + DigitalTwinDecode.TWIN_URI + "/decode", UriType.POST,
                parameter);
        callURI(callDecoder);
    }

    public void startCommunication() {
        String namespace = "eip://";
        String twinURI = getTwinURI();
        String resolverURI = "database";
        String broker = "localhost";

        initialise(broker, twinURI, resolverURI, namespace);
        start();
    }

    public String getTwinURI() {
        return "area" + areaCode.strip() + "compositeTwin";
    }
}
