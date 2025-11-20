package uk.gov.hmcts.reform.civil.service.sdo;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.enums.sdo.OrderDetailsPagesSectionsToggle;
import uk.gov.hmcts.reform.civil.enums.sdo.SmallTrack;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.sdo.SmallClaimsWitnessStatement;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SdoSmallClaimsDirectionsServiceTest {

    private final SdoSmallClaimsDirectionsService service = new SdoSmallClaimsDirectionsService();

    @Test
    void shouldResolveAdditionalDirections() {
        CaseData viaStandardList = CaseData.builder()
            .smallClaims(List.of(SmallTrack.smallClaimCreditHire))
            .build();

        CaseData viaDrawDirections = CaseData.builder()
            .drawDirectionsOrderSmallClaimsAdditionalDirections(List.of(SmallTrack.smallClaimRoadTrafficAccident))
            .build();

        assertThat(service.hasSmallAdditionalDirections(viaStandardList, SmallTrack.smallClaimCreditHire)).isTrue();
        assertThat(service.hasSmallAdditionalDirections(viaStandardList, SmallTrack.smallClaimRoadTrafficAccident)).isFalse();
        assertThat(service.hasSmallAdditionalDirections(viaDrawDirections, SmallTrack.smallClaimRoadTrafficAccident)).isTrue();
    }

    @Test
    void shouldDetectVariableStates() {
        CaseData caseData = CaseData.builder()
            .smallClaimsHearingToggle(List.of(OrderDetailsPagesSectionsToggle.SHOW))
            .smallClaimsDocumentsToggle(List.of(OrderDetailsPagesSectionsToggle.SHOW))
            .smallClaimsWitnessStatementToggle(List.of(OrderDetailsPagesSectionsToggle.SHOW))
            .smallClaimsWitnessStatement(SmallClaimsWitnessStatement.builder()
                                             .smallClaimsNumberOfWitnessesToggle(List.of(OrderDetailsPagesSectionsToggle.SHOW))
                                             .build())
            .smallClaimsFlightDelayToggle(List.of(OrderDetailsPagesSectionsToggle.SHOW))
            .smallClaimsAddNewDirections(List.of())
            .sdoR2SmallClaimsUseOfWelshToggle(List.of(OrderDetailsPagesSectionsToggle.SHOW))
            .build();

        assertThat(service.hasSmallClaimsVariable(caseData, SmallClaimsVariable.HEARING_TOGGLE)).isTrue();
        assertThat(service.hasSmallClaimsVariable(caseData, SmallClaimsVariable.DOCUMENTS_TOGGLE)).isTrue();
        assertThat(service.hasSmallClaimsVariable(caseData, SmallClaimsVariable.NUMBER_OF_WITNESSES_TOGGLE)).isTrue();
        assertThat(service.hasSmallClaimsVariable(caseData, SmallClaimsVariable.ADD_NEW_DIRECTIONS)).isTrue();
        assertThat(service.hasSmallClaimsVariable(caseData, SmallClaimsVariable.WELSH_TOGGLE)).isTrue();
        assertThat(service.hasSmallClaimsVariable(caseData, SmallClaimsVariable.METHOD_TOGGLE)).isTrue();
        assertThat(service.hasSmallClaimsVariable(caseData, SmallClaimsVariable.FLIGHT_DELAY_TOGGLE)).isTrue();
    }

}
