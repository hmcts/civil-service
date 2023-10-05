package uk.gov.hmcts.reform.migration.migration;

import org.apache.commons.io.IOUtils;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Service
public class CaseIdsFileReader {

    public List<String> readCaseIds() {

        String data = readString("/caseIds.txt");
        return Arrays.stream(data.split("[\r\n]+"))
            .filter(Objects::nonNull)
            .map(String::trim)
            .filter(Predicate.not(String::isEmpty))
            .collect(Collectors.toList());
    }

    public String readString(String resourcePath) {
        return new String(readBytes(resourcePath), StandardCharsets.UTF_8);
    }

    private byte[] readBytes(String resourcePath) {
        try (InputStream inputStream = CaseIdsFileReader.class.getResourceAsStream(resourcePath)) {
            return IOUtils.toByteArray(inputStream);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        } catch (NullPointerException e) {
            throw new IllegalStateException("Unable to read resource: " + resourcePath, e);
        }
    }
}
