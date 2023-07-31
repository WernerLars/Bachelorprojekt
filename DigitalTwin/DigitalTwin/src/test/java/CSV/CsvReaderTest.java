package CSV;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.IOException;

import static java.lang.Integer.MIN_VALUE;

@Test
public class CsvReaderTest {
    private CsvReader reader;

    @BeforeMethod
    public void init() {
        reader = CsvReader.getInstance();
    }

    @Test
    public void nullInput() {
        String line = null;
        WeatherEntry result = reader.getNextEntry(line);
        assert result == null;
    }

    @Test
    public void InputEqualsOutput() {
        String line = "92109;48.80104;9.88433;506;19750103;2.5;-.6;.9;.7;6.04;1831;0\n";
        WeatherEntry result = reader.getNextEntry(line);
        assert result != null;
        assert result.getGridNo() == 92109;
        assert result.getLatitude() == 48.80104;
        assert result.getLongitude() == 9.88433;
        assert result.getAltitude() == 506;
        assert result.getDay() == 19750103;
        assert result.getTempMax() == 2.5;
        assert result.getTempMin() == -0.6;
        assert result.getTempAvg() == 0.9;
        assert result.getWindspeed() == 0.7;
        assert result.getVapourpressure() == 6.04;
        assert result.getRadiation() == 1831;
        assert result.getSnowdepth() == 0;
    }

    @Test
    public void TooFewSemicolon() {
        String line = "92109;48.80104;9.88433;506;197501066.8;.4;3.6;2.7;7.18;2586;";
        WeatherEntry result = reader.getNextEntry(line);
        assert result == null;
    }

    @Test
    public void TooMuchSemicolon() {
        String line = "92109;48.80104;9.88433;506;19750110;3.3;0;-.9;1.2;1.5;604;3012;";
        WeatherEntry result = reader.getNextEntry(line);
        assert result != null;
        assert result.getGridNo() == 92109;
        assert result.getLatitude() == 48.80104;
        assert result.getLongitude() == 9.88433;
        assert result.getAltitude() == 506;
        assert result.getDay() == 19750110;
        assert result.getTempMax() == 3.3;
        assert result.getTempMin() == 0;
        assert result.getTempAvg() == -0.9;
        assert result.getWindspeed() == 1.2;
        assert result.getVapourpressure() == 1.5;
        assert result.getRadiation() == 604;
        assert Double.isNaN(result.getSnowdepth());
    }

    @Test
    public void LetterInsteadOfNumber() {
        String line = "92109;48.80104;9.8a433;506;19750111;4.7;-2.7;1;1.8;6.33;4440;";
        WeatherEntry result = reader.getNextEntry(line);
        assert result != null;
        assert result.getGridNo() == 92109;
        assert result.getLatitude() == 48.80104;
        assert Double.isNaN(result.getLongitude());
        assert result.getAltitude() == 506;
        assert result.getDay() == 19750111;
        assert result.getTempMax() == 4.7;
        assert result.getTempMin() == -2.7;
        assert result.getTempAvg() == 1;
        assert result.getWindspeed() == 1.8;
        assert result.getVapourpressure() == 6.33;
        assert result.getRadiation() == 4440;
        assert result.getSnowdepth() == 0;
    }

    @Test
    public void DoubleInsteadOfInt() {
        String line = "92109;48.80104;9.88433;506;19750.117;8.3;-1.6;3.3;2.3;6.82;4473;";
        WeatherEntry result = reader.getNextEntry(line);
        assert result != null;
        assert result.getGridNo() == 92109;
        assert result.getLatitude() == 48.80104;
        assert result.getLongitude() == 9.88433;
        assert result.getAltitude() == 506;
        assert result.getDay() == MIN_VALUE;
        assert result.getTempMax() == 8.3;
        assert result.getTempMin() == -1.6;
        assert result.getTempAvg() == 3.3;
        assert result.getWindspeed() == 2.3;
        assert result.getVapourpressure() == 6.82;
        assert result.getRadiation() == 4473;
        assert result.getSnowdepth() == 0;
    }

    @Test
    public void EmptyInput() {
        String line = ";;;;;;;;;;;";
        WeatherEntry result = reader.getNextEntry(line);
        assert result == null;
    }

}
