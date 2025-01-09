package uk.gov.hmcts.reform.civil.utils;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.enums.MultiPartyScenario;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Mediation;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.civil.enums.mediation.MediationUnsuccessfulReason.APPOINTMENT_NOT_ASSIGNED;
import static uk.gov.hmcts.reform.civil.enums.mediation.MediationUnsuccessfulReason.APPOINTMENT_NO_AGREEMENT;
import static uk.gov.hmcts.reform.civil.enums.mediation.MediationUnsuccessfulReason.NOT_CONTACTABLE_CLAIMANT_ONE;
import static uk.gov.hmcts.reform.civil.enums.mediation.MediationUnsuccessfulReason.NOT_CONTACTABLE_CLAIMANT_TWO;
import static uk.gov.hmcts.reform.civil.enums.mediation.MediationUnsuccessfulReason.PARTY_WITHDRAWS;
import static uk.gov.hmcts.reform.civil.utils.MediationUtils.findMediationUnsuccessfulReason;

class MediationUtilsTest {

    @Test
    void shouldReturnTrue_whenReasonMatch() {
        CaseData caseData = CaseDataBuilder.builder()
            .atStateMediationUnsuccessfulCarm(MultiPartyScenario.ONE_V_ONE)
            .build();

        assertThat(findMediationUnsuccessfulReason(caseData, List.of(NOT_CONTACTABLE_CLAIMANT_ONE, NOT_CONTACTABLE_CLAIMANT_TWO))).isTrue();
    }

    @Test
    void shouldReturnFalse_whenReasonNotMatched() {
        CaseData caseData = CaseDataBuilder.builder()
            .atStateMediationUnsuccessfulCarm(MultiPartyScenario.ONE_V_ONE)
            .build();

        assertThat(findMediationUnsuccessfulReason(caseData, List.of(PARTY_WITHDRAWS, APPOINTMENT_NO_AGREEMENT, APPOINTMENT_NOT_ASSIGNED))).isFalse();
    }

    @Test
    void shouldReturnFalse_whenMoreThanOneReasonMatch() {
        CaseData caseData = CaseDataBuilder.builder()
            .mediation(Mediation.builder()
                           .mediationUnsuccessfulReasonsMultiSelect(List.of(PARTY_WITHDRAWS, APPOINTMENT_NO_AGREEMENT))
                           .build())
            .build();

        assertThat(findMediationUnsuccessfulReason(caseData, List.of(PARTY_WITHDRAWS, APPOINTMENT_NO_AGREEMENT, APPOINTMENT_NOT_ASSIGNED))).isTrue();
    }

    @Test
    void shouldReturnFalse_whenMediationUnsuccessfulReasonsMultiSelectIsNull() {
        CaseData caseData = CaseDataBuilder.builder()
            .mediation(Mediation.builder()
                           .mediationUnsuccessfulReasonsMultiSelect(null)
                           .build())
            .build();

        assertThat(findMediationUnsuccessfulReason(caseData, List.of(PARTY_WITHDRAWS, APPOINTMENT_NO_AGREEMENT, APPOINTMENT_NOT_ASSIGNED))).isFalse();
    }

    @Test
    void shouldReturnFalse_whenMediationIsNull() {
        CaseData caseData = CaseDataBuilder.builder()
            .mediation(null)
            .build();

        assertThat(findMediationUnsuccessfulReason(caseData, List.of(PARTY_WITHDRAWS, APPOINTMENT_NO_AGREEMENT, APPOINTMENT_NOT_ASSIGNED))).isFalse();
    }
}
