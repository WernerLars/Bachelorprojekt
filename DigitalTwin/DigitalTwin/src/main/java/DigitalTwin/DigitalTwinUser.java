package DigitalTwin;

import CSV.WeatherEntry;
import DB.WeatherEntryDAO;
import Settings.SettingsUtil;
import de.ude.es.protocolabstraction.eipmessage.EIPMessage;
import de.ude.es.protocolabstraction.eipmessage.EIPMessage.UriType;
import de.ude.es.twininterface.abstracttwin.AbstractTwin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static Helper.DigitalTwinUserHelper.getEntryList;

// Digital Twin fr Benutzer
public class DigitalTwinUser extends AbstractTwin {

    public static final String TWIN_URI = "userTwin";
    private static CountDownLatch latch = new CountDownLatch(1);
    private static final Logger logger = LoggerFactory.getLogger(DigitalTwinUser.class);

    private String decodedData = null;

    public DigitalTwinUser() {
        startCommunication();
    }

    public List<WeatherEntry> getNextEntries(String areaCode) {
        try {
            latch = new CountDownLatch(1);
            //send request to area twin
            requestArea(areaCode);

            //wait for data
            latch.await(30, TimeUnit.SECONDS);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (decodedData == null || decodedData.isEmpty()) {
            return null;
        }
        List<WeatherEntry> entryList = getEntryList(decodedData);
        this.decodedData = null;
        return entryList;
    }

    private void requestArea(String areaCode) {
        EIPMessage callCompositeAreaTwin = new EIPMessage("eip://" + "area" + areaCode.strip() + "compositeTwin" + "/requestData",
                UriType.GET);
        logger.info("Requesting data from CompositeAreaTwin");
        callURI(callCompositeAreaTwin);
    }

    public void startPeriodicRequests(String areaCode) {
        Thread thread = new Thread(() -> {
            SettingsUtil settings = new SettingsUtil();
            Logger logger = LoggerFactory.getLogger("Periodic Requests");
            try {
                int updatesPerHour = settings.getUpdateDataPerHour();
                while (updatesPerHour > 0) {
                    requestArea(areaCode);

                    //update intervall
                    updatesPerHour = settings.getUpdateDataPerHour();
                    logger.info("Next request will be done in " + 60 / updatesPerHour + " minutes");
                    Thread.sleep(1000 * 60 * 60 / updatesPerHour);
                }
            } catch (Exception e) {
                logger.error(e.toString());
            }
        });
        thread.start();
    }

    private void startCommunication() {
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
            case "eip://" + TWIN_URI + "/receiveData":
                // Funktion, die Daten entgegen nimmt
                WeatherEntryDAO dao = new WeatherEntryDAO();
                this.decodedData = eipMessage.parameters[0].value.toString();
                logger.info("Received decoded data: " + decodedData);
                List<WeatherEntry> entries4Values = WeatherEntryDAO.getEntries4Values(decodedData);
                latch.countDown();
                for (WeatherEntry entry : entries4Values) {
                    dao.save(entry);
                }
                break;
            default:
                logger.error("URI wurde nicht definiert!");
                break;
        }
    }
}