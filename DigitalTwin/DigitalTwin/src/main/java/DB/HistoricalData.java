package DB;

import CSV.WeatherEntry;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import java.util.List;
import java.util.stream.Collectors;

@Path("history")
public class HistoricalData {

    @GET
    @Produces("application/json")
    public String getHistoryJson(@Context UriInfo uriInfo) {
        return getHistoryColumnsJson(null, uriInfo);
    }

    @GET
    @Produces("application/json")
    @Path("/{valueKeys}")
    public String getHistoryColumnsJson(@PathParam("valueKeys") String valueKeysString, @Context UriInfo uriInfo) {
        WeatherEntryDAO dao = new WeatherEntryDAO();
        List<WeatherEntry> entries;
        if (uriInfo.getQueryParameters().isEmpty())
            entries = dao.getAll();
        else
            entries = dao.getWhere(uriInfo.getQueryParameters());

        if (valueKeysString == null || valueKeysString.isEmpty())
            return "[" + String.join(",", entries.stream().map(o -> o.toJSON()).collect(Collectors.toList())) + "]";
        else
            return "[" + String.join(",", entries.stream().map(o -> o.getValuesAsJson(valueKeysString)).collect(Collectors.toList())) + "]";
    }
}
