package uk.gov.hmcts.reform.civil.model;

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

@Service
@Slf4j
public class AirlineEpimsDataLoader {

    private static final String CSV_FILE_PATH = "airline_ePimsID_csv/airline_ePimsID.csv";
    private final List<AirlineEpimsID> airlineEpimsIDList = new ArrayList<>();

    @PostConstruct
    protected void init() {
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream(CSV_FILE_PATH);
        try (CSVReader reader = new CSVReader(new InputStreamReader(inputStream))) {

            List<String[]> linesList = reader.readAll();
            linesList.forEach(line -> {
                AirlineEpimsID airlineEpimsID = AirlineEpimsID.builder()
                    .airline(line[0])
                    .epimsid(line[1])
                    .build();
                airlineEpimsIDList.add(airlineEpimsID);
            });
        } catch (IOException | CsvException  e) {
            log.error("Error occurred while loading the airline_ePimsID.csv file: " + CSV_FILE_PATH + e);
        }
    }

    public List<AirlineEpimsID> getAirlineEpimsIDList() {
        return ImmutableList.copyOf(airlineEpimsIDList);
    }
}
