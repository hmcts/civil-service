package uk.gov.hmcts.reform.civil.ga.model.genapplication;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.testutils.ObjectMapperFactory;

import static org.assertj.core.api.Assertions.assertThat;

class HearingLengthTest {

    private final ObjectMapper objectMapper = ObjectMapperFactory.instance();

    @Test
    void shouldDeserializeDecimalHoursPersistedByCcd() throws Exception {
        String json = """
            {
              "lengthListOtherDays": "0",
              "lengthListOtherHours": "2.5",
              "lengthListOtherMinutes": "0"
            }
            """;

        HearingLength hearingLength = objectMapper.readValue(json, HearingLength.class);

        assertThat(hearingLength.getLengthListOtherDays()).isEqualTo("0");
        assertThat(hearingLength.getLengthListOtherHours()).isEqualTo("2.5");
        assertThat(hearingLength.getLengthListOtherMinutes()).isEqualTo("0");
    }
}
