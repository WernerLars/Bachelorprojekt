package CSV;

import DB.WeatherEntryDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.*;

public class CsvReader implements AutoCloseable {

    private static final String DEFAULT_CSV_FILE_PATH = "/CSV/orderedEntries.csv";
    public static final String FILE_DELIMITER = ";";
    public static final int NUMBER_OF_FIELDS = 11;

    private static final Map<String, CsvReader> reader4File = new HashMap<String, CsvReader>();
    private static BufferedReader bufferedReader;
    private final static Logger logger = LoggerFactory.getLogger(CsvReader.class);

    private CsvReader(String filePath) {
        initReader(filePath);
    }

    public static synchronized CsvReader getInstance(String filePath) {
        if (reader4File.get(filePath) == null) {
            reader4File.put(filePath, new CsvReader(filePath));
        }
        return reader4File.get(filePath);
    }

    public static synchronized CsvReader getInstance() {
        return getInstance(DEFAULT_CSV_FILE_PATH);
    }

    public WeatherEntry getNextEntry() {
        if (bufferedReader != null) {
            String line = null;
            try {
                line = bufferedReader.readLine();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return getWeatherEntry4Line(line);
        }
        return null;
    }

    // only for test purposes
    public WeatherEntry getNextEntry(String line) {
        if (bufferedReader != null) {
            return getWeatherEntry4Line(line);
        }
        return null;
    }

    private WeatherEntry getWeatherEntry4Line(String line) {
        if (line != null && !line.isEmpty()) {
            String[] values = line.split(FILE_DELIMITER, NUMBER_OF_FIELDS);
            if (values.length == NUMBER_OF_FIELDS) {
                return WeatherEntryDAO.getEntry4Values(values);
            }
        }
        return null;
    }

    private static void initReader(String filePath) {
        try {
            bufferedReader = new BufferedReader(new FileReader(CsvReader.class.getResource(filePath).getFile()));
            bufferedReader.readLine(); // skip first line (heading)
        } catch (IOException e) {
            logger.error("Could not initialize CsvReader");
            e.printStackTrace();
        }
    }

    @Override
    public void close() throws Exception {
        bufferedReader.close();
    }

    /**
     * static class to group CSV file by gridNOs
     */
    @SuppressWarnings("unused")
    private static class CSVGrouper {

        private static void formatCSVFile() throws IOException {
            final int[][] gridGroups = {{106098, 106099, 105099, 105098, 104099},
                    {103098, 103099, 102099, 102098, 101098}, {101099, 100099, 100098, 99098, 99099},
                    {98098, 98099, 97098, 97099, 96099}, {95099, 94099, 93102, 94101, 94100},
                    {94102, 95101, 95102, 95100, 96101}, {96102, 96100, 97102, 97100, 97101},
                    {98102, 98101, 98100, 99100, 99101}, {99102, 100101, 100100, 100102, 101100},
                    {101102, 101101, 102102, 102101, 102100}, {103100, 103102, 103101, 104101, 104102},
                    {104100, 105102, 105100, 105101, 106100}, {106102, 106101, 107100, 107101, 107102},
                    {108101, 108102, 109100, 109101, 109102}, {110102, 110101, 111102, 112101, 112102},
                    {113100, 113101, 113102, 114101, 114102}, {120105, 119105, 118105, 114104, 114103},
                    {113103, 113105, 113104, 112104, 112105}, {112103, 111105, 111104, 111103, 110104},
                    {110105, 110103, 109104, 109105, 109103}, {108105, 108103, 108104, 107105, 107104},
                    {107103, 106105, 106104, 106103, 105104}, {105105, 105103, 104105, 104104, 104103},
                    {103105, 103103, 103104, 102105, 102103}, {102104, 101105, 101103, 101104, 100104},
                    {100103, 100105, 99103, 99105, 99104}, {98103, 98105, 98104, 97104, 97105},
                    {97103, 96105, 96104, 96103, 95104}, {95103, 94103, 94104, 93104, 93103},
                    {92103, 92104, 91103, 91104, 90103}, {90102, 90104, 89102, 89103, 89104},
                    {88102, 88103, 88104, 87103, 87104}, {87102, 86103, 86102, 87105, 87106},
                    {87107, 88106, 88107, 88105, 89106}, {89105, 89107, 90107, 90105, 90106},
                    {91105, 91106, 91107, 92105, 92107}, {92106, 93107, 93105, 93106, 94107},
                    {94106, 94105, 95107, 95105, 95106}, {96107, 96106, 97107, 97106, 98106},
                    {98107, 99107, 99106, 100107, 100106}, {101107, 101106, 102107, 102106, 103107},
                    {103106, 104106, 104107, 105106, 105107}, {106106, 106107, 107106, 107107, 108106},
                    {108107, 109106, 109107, 110106, 110107}, {111106, 111107, 112108, 112106, 112107},
                    {113106, 113107, 113108, 114107, 114106}, {114108, 114105, 115106, 115107, 115108},
                    {116107, 116106, 116108, 117106, 117107}, {117108, 118107, 118108, 118106, 119106},
                    {119107, 119108, 119109, 118109, 117110}, {117109, 116109, 116110, 115109, 115110},
                    {114110, 114109, 113109, 113110, 112109}, {112110, 111108, 111110, 111109, 110110},
                    {110108, 110109, 109109, 109108, 109110}, {108110, 108109, 108108, 107108, 107110},
                    {107109, 106109, 106110, 106108, 105109}, {105110, 105108, 104109, 104110, 104108},
                    {103110, 103109, 103108, 102108, 102109}, {102110, 101108, 101110, 101109, 100109},
                    {100110, 100108, 99108, 99109, 99110}, {98109, 98110, 98108, 97110, 97109},
                    {97108, 96109, 96108, 96110, 95109}, {95110, 95108, 94109, 94110, 94108},
                    {93109, 93108, 93110, 92109, 92108}, {92110, 91110, 91108, 91109, 90108},
                    {90109, 90110, 89109, 89108, 89110}, {88108, 88110, 88109, 87110, 87108},
                    {87109, 86109, 86110, 86108, 85110}, {86113, 86112, 86111, 87111, 87113},
                    {87112, 88111, 88113, 88112, 89112}, {89111, 89113, 90113, 90112, 90111},
                    {91111, 91112, 91113, 92111, 92113}, {92112, 93113, 93111, 93112, 94113},
                    {94112, 94111, 95113, 95111, 95112}, {96112, 96111, 96113, 97112, 97113},
                    {97111, 98111, 98113, 98112, 99112}, {99111, 99113, 100112, 100113, 100111},
                    {101113, 101112, 101111, 102113, 102112}, {102111, 103113, 103111, 103112, 104111},
                    {104112, 104113, 105111, 105112, 105113}, {106113, 106111, 106112, 107111, 107113},
                    {107112, 108112, 108111, 108113, 109111}, {109113, 109112, 110111, 110112, 110113},
                    {111113, 111112, 111111, 112113, 112112}, {112111, 113112, 113113, 113111, 114112},
                    {114113, 114111, 115113, 115112, 115111}, {116111, 116112, 117111, 117113, 117112},
                    {118112, 117115, 116115, 116114, 116113}, {115114, 115115, 114114, 114115, 113115},
                    {113114, 112116, 112115, 112114, 111115}, {111114, 111116, 110115, 110114, 110116},
                    {109115, 109116, 109114, 108116, 108115}, {108114, 107115, 107114, 107116, 106116},
                    {106115, 106114, 105114, 105116, 105115}, {104116, 104115, 104114, 103114, 103115},
                    {103116, 102115, 102116, 102114, 101114}, {101115, 101116, 100115, 100116, 100114},
                    {99114, 99115, 99116, 98115, 98116}, {98114, 97114, 97115, 97116, 96114},
                    {96115, 96116, 95115, 95114, 95116}, {94116, 94114, 94115, 93116, 93115},
                    {93114, 92116, 92114, 92115, 91115}, {91114, 91116, 90116, 90115, 90114},
                    {89114, 89115, 89116, 88115, 88114}, {88116, 87116, 87115, 87114, 86114},
                    {86118, 86119, 87118, 87119, 87117}, {88117, 88118, 89117, 89118, 90117},
                    {90119, 90118, 91119, 91117, 91118}, {92119, 92117, 92118, 93118, 93117},
                    {93119, 94117, 94118, 94119, 95118}, {95117, 96117, 97117, 99118, 99117},
                    {100119, 100117, 100118, 101118, 101117}, {101119, 102118, 102117, 102119, 103119},
                    {103118, 103117, 104119, 104117, 104118}, {105119, 105117, 105118, 106117, 106118},
                    {107117, 107118, 108118, 108117, 109118}, {109117, 110118, 110117, 111117, 111118},
                    {112117, 112118, 113117, 113118, 113116}, {114116, 114118, 114117, 115116, 115118},
                    {115117, 116118, 116116, 116117, 117117}, {117118, 117116, 118118, 118117, 118119},
                    {117119, 116120, 116119, 115119, 115120}, {114120, 114119, 114121, 113120, 113119},
                    {113121, 112121, 112120, 112119, 111121}, {111119, 111120, 110119, 110121, 110120},
                    {109119, 109120, 109121, 108121, 108120}, {108119, 107119, 107120, 107121, 106121},
                    {106119, 106120, 105120, 105121, 104120}, {104121, 103121, 103120, 102121, 102120},
                    {101121, 101120, 93120, 92120, 92121}, {91120, 91121, 101123, 102123, 102122},
                    {103122, 103123, 104123, 104122, 105122}, {105123, 106122, 107122, 108122, 109122}};

            String line;
            Map<String, Map<Integer, List<String[]>>> entries4Dates4GridGroup = new HashMap<String, Map<Integer, List<String[]>>>();
            int lineCounter = 0;
            while ((line = bufferedReader.readLine()) != null && !line.isEmpty()) {
                String[] values = line.split(FILE_DELIMITER);
                logger.debug(lineCounter++ + Arrays.toString(values));
                String date = values[4];
                String gridNo = values[0];
                boolean match = false;

                if (entries4Dates4GridGroup.containsKey(date) || entries4Dates4GridGroup.keySet().size() <= 20) {
                    for (int i = 0; i < gridGroups.length; i++) {
                        if (match) {
                            break;
                        }
                        for (int gridGroupEntry = 0; gridGroupEntry < 5; gridGroupEntry++) {
                            if (String.valueOf(gridGroups[i][gridGroupEntry]).equals(gridNo)) {
                                entries4Dates4GridGroup.computeIfAbsent(date, k -> new HashMap<Integer, List<String[]>>());
                                entries4Dates4GridGroup.get(date).computeIfAbsent(Integer.valueOf(i),
                                        k -> new ArrayList<String[]>());
                                entries4Dates4GridGroup.get(date).get(Integer.valueOf(i)).add(values);
                                match = true;
                                break;
                            }
                        }
                    }
                }
            }
            logger.info("Done with Grouping! Writing to file...");
            writeToFile(entries4Dates4GridGroup);
            logger.info("Done!");
        }

        private static void writeToFile(Map<String, Map<Integer, List<String[]>>> entries4Dates4GridGroup)
                throws IOException {
            final File file = new File("src/main/resources/CSV/orderedEntries.csv");
            try (FileWriter writer = new FileWriter(file)) {
                for (String day : entries4Dates4GridGroup.keySet()) {
                    for (Integer gridGroupId : entries4Dates4GridGroup.get(day).keySet()) {
                        List<String[]> gridGroupEntries = entries4Dates4GridGroup.get(day).get(gridGroupId);
                        if (gridGroupEntries.size() == 5) {
                            for (String[] values : gridGroupEntries) {
                                writer.append(String.join(FILE_DELIMITER, values)).append("\n");
                            }
                        } else {
                            logger.error("Gridgroup entries.size() != 5!");
                        }
                    }
                }
            }
        }
    }
}