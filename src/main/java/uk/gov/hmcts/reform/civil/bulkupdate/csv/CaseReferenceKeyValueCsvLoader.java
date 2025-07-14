package uk.gov.hmcts.reform.civil.bulkupdate.csv;

import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Slf4j
@NoArgsConstructor
@Component
public class CaseReferenceKeyValueCsvLoader {

    public List<CaseReferenceKeyValue> loadCaseReferenceList(String fileName) {
        try {
            CsvMapper csvMapper = new CsvMapper();
            CsvSchema csvSchema = csvMapper.typedSchemaFor(CaseReferenceKeyValue.class).withHeader();

            MappingIterator<CaseReferenceKeyValue> it = new CsvMapper().readerFor(CaseReferenceKeyValue.class)
                .with(csvSchema)
                .readValues(getClass().getClassLoader().getResource(fileName));
            // Read all records from the CSV file into a list
            return it.readAll();

        } catch (Exception e) {
            log.error("Error occurred while loading object list from file " + fileName, e);
            return Collections.emptyList();
        }
    }

}
