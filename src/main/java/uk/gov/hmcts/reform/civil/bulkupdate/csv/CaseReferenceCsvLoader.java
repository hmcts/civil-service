package uk.gov.hmcts.reform.civil.bulkupdate.csv;

import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@NoArgsConstructor
@Component
public class CaseReferenceCsvLoader {

    @SuppressWarnings({"java:S3740", "java:S1488"})
    public List<CaseReference> loadCaseReferenceList(String fileName) {
        try {
            CsvMapper csvMapper = new CsvMapper();
            CsvSchema csvSchema = csvMapper.typedSchemaFor(CaseReference.class).withHeader();
            List<Object> list = new CsvMapper().readerFor(CaseReference.class)
                .with(csvSchema)
                .readValues(getClass().getClassLoader().getResource(fileName))
                .readAll();
            return list.stream()
                .filter(obj -> obj instanceof CaseReference) // Ensure the object is of type CaseReference
                .map(obj -> (CaseReference) obj)             // Cast the object to CaseReference
                .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error occurred while loading object list from file " + fileName, e);
            return Collections.emptyList();
        }
    }

}
