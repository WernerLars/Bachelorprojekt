package DB;

import CSV.WeatherEntry;

import java.util.List;
import java.util.Map;

public interface DAO<T extends WeatherEntry> {

    List<T> getAll();

    void save(T entry);

    void update(T entry);

    void delete(T entry);

    List<T> getWhere(Map<String, List<String>> queryParams);
}
