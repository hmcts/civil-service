package uk.gov.hmcts.reform.civil.service;

import com.google.common.collect.ImmutableList;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
import uk.gov.hmcts.reform.civil.model.AirlineEpimsId;

@Service
@Slf4j
public class AirlineEpimsDataLoader {

    private static final String CSV_FILE_PATH = "airline_ePimsID_csv/airline_ePimsID.csv";
    private final List<AirlineEpimsId> airlineEpimsIDList = new ArrayList<>();

    @PostConstruct
    protected void init() {
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream(CSV_FILE_PATH);
        try (CSVReader reader = new CSVReader(new InputStreamReader(inputStream))) {

            List<String[]> linesList = reader.readAll();
            linesList.forEach(line -> {
                AirlineEpimsId airlineEpimsID = AirlineEpimsId.builder()
                    .airline(line[0])
                    .epimsID(line[1])
                    .build();
                airlineEpimsIDList.add(airlineEpimsID);
            });
        } catch (IOException | CsvException  e) {
            log.error("Error occurred while loading the airline_ePimsID.csv file: " + CSV_FILE_PATH + e);
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    log.error("Error occurred while closing inputStream for the airline_ePimsID.csv");
                }
            }
        }
    }

    public List<AirlineEpimsId> getAirlineEpimsIDList() {
        return ImmutableList.copyOf(airlineEpimsIDList);
    }
}
