package DigitalTwin;

import CSV.CsvReader;
import CSV.WeatherEntry;
import de.ude.es.protocolabstraction.eipmessage.EIPMessage;
import de.ude.es.protocolabstraction.eipmessage.EIPMessage.Parameter;
import de.ude.es.protocolabstraction.eipmessage.EIPMessage.UriType;
import de.ude.es.twininterface.abstracttwin.AbstractTwin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//Digital Twin, spricht IoT Geraet an (CSV Datei)
public class DigitalTwinIoTDevice extends AbstractTwin {

    private final String location;
    private static final String baseTwinURI = "iotDeviceTwin";
    private static CsvReader reader;
    private static final Logger logger = LoggerFactory.getLogger(DigitalTwinIoTDevice.class);

    public static void main(String[] args) {

        logger.info("DigitalTwinIoTDevice {} initialized", args[0]);
        new DigitalTwinIoTDevice(args[0], args[1]);
    }

    public DigitalTwinIoTDevice(String location, String filePath) {
        this.location = location.strip();
        reader = CsvReader.getInstance(filePath);
        if (reader == null) {
            logger.error("Could not initialize CsvReader");
            return;
        }
        startCommunication();
    }

    public void startCommunication() {
        String namespace = "eip://";
        String resolverURI = "database";
        String broker = "localhost";
        initialise(broker, getTwinURI(), resolverURI, namespace);
        start();
    }

    @Override
    public void handleURI(EIPMessage eipMessage) {
        String uri = eipMessage.value;
        String areaTwinURI = eipMessage.sender;
        if (uri.equals("eip://" + getTwinURI() + "/requestData")) {
            sendCurrentData(areaTwinURI);
        } else {
            logger.error("URI wurde nicht definiert!");
        }
    }

    private void sendCurrentData(String areaTwinURI) {
        WeatherEntry nextEntry = reader.getNextEntry();
        EIPMessage sendToAreaTwin = new EIPMessage(areaTwinURI + "/receiveIOTDeviceData", UriType.POST,
                new Parameter[]{new Parameter("currentData", nextEntry.toJSONArrayString())});
        logger.info("IoT Device {} : Sending current device data to compositeAreaTwin", location);
        callURI(sendToAreaTwin);
    }

    public String getTwinURI() {
        return baseTwinURI + location;
    }

}