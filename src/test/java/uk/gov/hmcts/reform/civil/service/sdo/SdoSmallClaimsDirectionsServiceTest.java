package uk.gov.hmcts.reform.civil.service.sdo;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.enums.sdo.OrderDetailsPagesSectionsToggle;
import uk.gov.hmcts.reform.civil.enums.sdo.SmallTrack;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.sdo.SmallClaimsWitnessStatement;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SdoSmallClaimsDirectionsServiceTest {

    private final SdoSmallClaimsDirectionsService service = new SdoSmallClaimsDirectionsService();

    @Test
    void shouldResolveAdditionalDirections() {
        CaseData viaStandardList = CaseDataBuilder.builder().build();
        viaStandardList.setSmallClaims(List.of(SmallTrack.smallClaimCreditHire));

        CaseData viaDrawDirections = CaseDataBuilder.builder().build();
        viaDrawDirections.setDrawDirectionsOrderSmallClaimsAdditionalDirections(
            List.of(SmallTrack.smallClaimRoadTrafficAccident)
        );

        assertThat(service.hasSmallAdditionalDirections(viaStandardList, SmallTrack.smallClaimCreditHire)).isTrue();
        assertThat(service.hasSmallAdditionalDirections(viaStandardList, SmallTrack.smallClaimRoadTrafficAccident)).isFalse();
        assertThat(service.hasSmallAdditionalDirections(viaDrawDirections, SmallTrack.smallClaimRoadTrafficAccident)).isTrue();
    }

    @Test
    void shouldDetectVariableStates() {
        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setSmallClaimsHearingToggle(List.of(OrderDetailsPagesSectionsToggle.SHOW));
        caseData.setSmallClaimsDocumentsToggle(List.of(OrderDetailsPagesSectionsToggle.SHOW));
        caseData.setSmallClaimsWitnessStatementToggle(List.of(OrderDetailsPagesSectionsToggle.SHOW));
        SmallClaimsWitnessStatement witnessStatement = new SmallClaimsWitnessStatement();
        witnessStatement.setSmallClaimsNumberOfWitnessesToggle(List.of(OrderDetailsPagesSectionsToggle.SHOW));
        caseData.setSmallClaimsWitnessStatement(witnessStatement);
        caseData.setSmallClaimsFlightDelayToggle(List.of(OrderDetailsPagesSectionsToggle.SHOW));
        caseData.setSmallClaimsAddNewDirections(List.of());
        caseData.setSdoR2SmallClaimsUseOfWelshToggle(List.of(OrderDetailsPagesSectionsToggle.SHOW));

        assertThat(service.hasSmallClaimsVariable(caseData, SmallClaimsVariable.HEARING_TOGGLE)).isTrue();
        assertThat(service.hasSmallClaimsVariable(caseData, SmallClaimsVariable.DOCUMENTS_TOGGLE)).isTrue();
        assertThat(service.hasSmallClaimsVariable(caseData, SmallClaimsVariable.NUMBER_OF_WITNESSES_TOGGLE)).isTrue();
        assertThat(service.hasSmallClaimsVariable(caseData, SmallClaimsVariable.ADD_NEW_DIRECTIONS)).isTrue();
        assertThat(service.hasSmallClaimsVariable(caseData, SmallClaimsVariable.WELSH_TOGGLE)).isTrue();
        assertThat(service.hasSmallClaimsVariable(caseData, SmallClaimsVariable.METHOD_TOGGLE)).isTrue();
        assertThat(service.hasSmallClaimsVariable(caseData, SmallClaimsVariable.FLIGHT_DELAY_TOGGLE)).isTrue();
    }

}
