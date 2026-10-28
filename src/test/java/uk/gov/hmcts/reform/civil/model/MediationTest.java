package uk.gov.hmcts.reform.civil.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.enums.mediation.MediationUnsuccessfulOutcome;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class MediationTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void shouldOmitEmptyNewMediationFieldsFromCaseData() {
        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setMediation(new Mediation()
            .setUnsuccessfulMediationReason("Party withdraws"));

        Map<String, Object> data = caseData.toMap(objectMapper);

        assertThat(data).containsEntry("unsuccessfulMediationReason", "Party withdraws");
        assertThat(data).containsKey("mediationUnsuccessfulReasonsMultiSelect");
        assertThat(data).doesNotContainKeys(
            "mediationUnsuccessfulOutcome",
            "mediationNotConductedReason",
            "mediationNotAttendedReason",
            "mediationCancelledReason",
            "mediationAbandonedReason",
            "mediationNotSettledReason"
        );
    }

    @Test
    void shouldIncludeNewMediationFieldWhenAValueIsSet() {
        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setMediation(new Mediation()
            .setMediationUnsuccessfulOutcome(MediationUnsuccessfulOutcome.NOT_CONDUCTED));

        Map<String, Object> data = caseData.toMap(objectMapper);

        assertThat(String.valueOf(data.get("mediationUnsuccessfulOutcome"))).isEqualTo("NOT_CONDUCTED");
        assertThat(data).doesNotContainKey("mediationNotConductedReason");
    }
}
