package uk.gov.hmcts.reform.civil.service.directionsorder;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.civil.service.directionsorder.DirectionsOrderSpecialistTextLibrary.FAST_TRACK_DIGITAL_PORTAL_BUNDLE_WARNING;

/**
 * Guards against CCD label drift for the text that also appears in runtime services.
 */
class DirectionsOrderTextLibraryCcdParityTest {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Test
    void fastTrackDigitalPortalBundleWarningMatchesCcdLabel() throws IOException {
        Path ccdCaseField = Path.of("ccd-definition/CaseField/CaseField-SDO.json");
        List<Map<String, Object>> fields = OBJECT_MAPPER.readValue(
            Files.newBufferedReader(ccdCaseField),
            new TypeReference<>() {
            }
        );

        Map<String, Object> bundleWarningField = fields.stream()
            .filter(entry -> "fastTrackTrialBundleText".equals(entry.get("ID")))
            .findFirst()
            .orElseThrow(() -> new AssertionError("fastTrackTrialBundleText missing from CCD CaseField-SDO.json"));

        assertThat(bundleWarningField.get("Label"))
            .as("CCD fastTrackTrialBundleText label should mirror FAST_TRACK_DIGITAL_PORTAL_BUNDLE_WARNING")
            .isEqualTo(FAST_TRACK_DIGITAL_PORTAL_BUNDLE_WARNING);
    }
}
