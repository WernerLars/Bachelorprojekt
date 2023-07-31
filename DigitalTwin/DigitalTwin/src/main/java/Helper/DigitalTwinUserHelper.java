package Helper;

import CSV.WeatherEntry;
import DB.WeatherEntryDAO;
import DigitalTwin.DigitalTwinUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MultivaluedHashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Path("twin")
public class DigitalTwinUserHelper {
    public static DigitalTwinUser digitalTwinUser = null;
    private static final Logger logger = LoggerFactory.getLogger(DigitalTwinUserHelper.class);

    @GET
    @Produces("application/json")
    @Path("/{areaCode}")
    public static String getNextEntriesAsJson(@PathParam("areaCode") String areaCode) {
        List<WeatherEntry> entries = digitalTwinUser.getNextEntries(areaCode);
        if (entries != null) {
            return toJSON(entries);
        }

        logger.error("No response received. Returning last entry in history");
        WeatherEntryDAO dao = new WeatherEntryDAO();
        Map<String, List<String>> params = new MultivaluedHashMap<>();
        List<String> paramList = new ArrayList<>();
        paramList.add("1");
        params.put("limit", paramList);
        return "[" + String.join(",", dao.getWhere(params).stream().map(o -> o.toJSON()).collect(Collectors.toList())) + "]";
    }

    @GET
    @Produces("application/json")
    @Path("/{areaCode}/{valueKeys}")
    public String getNextEntriesValuesAsJson(@PathParam("areaCode") String areaCode, @PathParam("valueKeys") String valueKeyString) {
        List<WeatherEntry> entries = digitalTwinUser.getNextEntries(areaCode);
        if (entries != null) {
            return toJSON(entries, valueKeyString);
        }

        logger.error("No response received. Returning last values from history");
        WeatherEntryDAO dao = new WeatherEntryDAO();
        List<String> paramList = new ArrayList<>();
        paramList.add("1");
        Map<String, List<String>> params = new MultivaluedHashMap<>();
        params.put("limit", paramList);
        return "[" + String.join(",", dao.getWhere(params).stream().map(o -> o.getValuesAsJson(valueKeyString)).collect(Collectors.toList())) + "]";
    }

    public static WeatherEntry getEntry(String[] values) {
        WeatherEntry entry = new WeatherEntry();
        entry.setGridNo(Integer.parseInt(values[0]));
        entry.setLatitude(values[1]);
        entry.setLongitude(values[2]);
        entry.setAltitude(values[3]);
        entry.setDay(values[4]);
        entry.setTempMax(values[5]);
        entry.setTempMin(values[6]);
        entry.setTempAvg(values[7]);
        entry.setWindspeed(values[8]);
        entry.setVapourpressure(values[9]);
        entry.setRadiation(values[10]);
        return entry;
    }

    public static List<WeatherEntry> getEntryList(String decodedData) {
        List<WeatherEntry> entries = new ArrayList<>();
        decodedData = decodedData.substring(1, decodedData.length() - 1);
        int start;
        int end;
        while ((start = decodedData.indexOf("[")) != -1 && (end = decodedData.indexOf("]")) != -1) {
            String entryAsJson = decodedData.substring(start, end - 1);
            entries.add(getEntry(entryAsJson.replaceAll("\\[|\\]", "").split(",")));
            if (decodedData.length() <= end + 1) {
                break;
            }
            decodedData = decodedData.substring(end + 2);
        }
        return entries;
    }

    public static String toJSON(List<WeatherEntry> entryList) {
        StringBuilder jsonString = new StringBuilder("[");
        for (WeatherEntry entry : entryList) {
            jsonString.append(entry.toJSON()).append(",");
        }
        jsonString.deleteCharAt(jsonString.length());
        jsonString.append("]");
        return jsonString.toString();
    }

    public static String toJSON(List<WeatherEntry> entryList, String valueKeys) {
        StringBuilder jsonString = new StringBuilder("[");
        for (WeatherEntry entry : entryList) {
            jsonString.append(entry.getValuesAsJson(valueKeys)).append(",");
        }
        jsonString.deleteCharAt(jsonString.length());
        jsonString.append("]");
        return jsonString.toString();
    }

}
