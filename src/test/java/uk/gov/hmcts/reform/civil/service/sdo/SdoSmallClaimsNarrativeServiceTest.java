package uk.gov.hmcts.reform.civil.service.sdo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.enums.sdo.OrderDetailsPagesSectionsToggle;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.lenient;
import static uk.gov.hmcts.reform.civil.constants.SdoR2UiConstantSmallClaim.WITNESS_STATEMENT_TEXT;
import static uk.gov.hmcts.reform.civil.service.directionsorder.DirectionsOrderSpecialistTextLibrary.FLIGHT_DELAY_LEGAL_ARGUMENTS_NOTICE;
import static uk.gov.hmcts.reform.civil.service.directionsorder.DirectionsOrderSpecialistTextLibrary.FLIGHT_DELAY_RELATED_CLAIMS_NOTICE;
import static uk.gov.hmcts.reform.civil.service.directionsorder.DirectionsOrderSpecialistTextLibrary.ORDER_WITHOUT_HEARING_RECEIVED_BY_COURT_NO_ARTICLE;
import static uk.gov.hmcts.reform.civil.service.directionsorder.DirectionsOrderSpecialistTextLibrary.SMALL_CLAIMS_HEARING_LISTING_NOTICE;
import static uk.gov.hmcts.reform.civil.service.directionsorder.DirectionsOrderSpecialistTextLibrary.ROAD_TRAFFIC_ACCIDENT_SMALL_CLAIMS;
import static uk.gov.hmcts.reform.civil.service.directionsorder.DirectionsOrderSpecialistTextLibrary.SMALL_CLAIMS_DOCUMENTS_UPLOAD;
import static uk.gov.hmcts.reform.civil.service.directionsorder.DirectionsOrderSpecialistTextLibrary.SMALL_CLAIMS_DOCUMENTS_WARNING;
import static uk.gov.hmcts.reform.civil.service.directionsorder.DirectionsOrderSpecialistTextLibrary.JUDGES_RECITAL_STATEMENTS_OF_CASE_WITH_COMMA;

@ExtendWith(MockitoExtension.class)
class SdoSmallClaimsNarrativeServiceTest {

    @Mock
    private SdoDeadlineService deadlineService;

    private SdoSmallClaimsNarrativeService service;

    @BeforeEach
    void setUp() {
        service = new SdoSmallClaimsNarrativeService(deadlineService);
        lenient().when(deadlineService.nextWorkingDayFromNowWeeks(anyInt()))
            .thenAnswer(invocation -> LocalDate.of(2025, 3, 1)
                .plusWeeks(invocation.getArgument(0, Integer.class)));
        lenient().when(deadlineService.workingDaysFromNow(anyInt()))
            .thenAnswer(invocation -> LocalDate.of(2025, 5, 1)
                .plusDays(invocation.getArgument(0, Integer.class)));
    }

    @Test
    void shouldPopulateJudgesRecitalAndDocuments() {
        CaseData caseData = CaseData.builder().build();

        service.applyJudgesRecital(caseData);
        service.applyDocumentDirections(caseData);

        assertThat(caseData.getSmallClaimsJudgesRecital().getInput())
            .isEqualTo(JUDGES_RECITAL_STATEMENTS_OF_CASE_WITH_COMMA);
        assertThat(caseData.getSmallClaimsDocuments().getInput1())
            .isEqualTo(SMALL_CLAIMS_DOCUMENTS_UPLOAD);
        assertThat(caseData.getSmallClaimsDocuments().getInput2())
            .isEqualTo(SMALL_CLAIMS_DOCUMENTS_WARNING);
    }

    @Test
    void shouldPopulateWitnessStatementsAndCreditHire() {
        CaseData caseData = CaseData.builder().build();

        service.applyWitnessStatements(caseData);
        service.applyCreditHire(caseData);

        assertThat(caseData.getSdoR2SmallClaimsWitnessStatementOther().getSdoR2SmallClaimsRestrictWitness()
                       .getNoOfWitnessClaimant()).isEqualTo(2);
        assertThat(caseData.getSdoR2SmallClaimsWitnessStatementOther().getSdoStatementOfWitness())
            .isEqualTo(WITNESS_STATEMENT_TEXT);
        assertThat(caseData.getSmallClaimsCreditHire().getDate4())
            .isEqualTo(LocalDate.of(2025, 3, 1).plusWeeks(10));
    }

    @Test
    void shouldPopulateRoadTrafficAccident() {
        CaseData caseData = CaseData.builder().build();

        service.applyRoadTrafficAccident(caseData);

        assertThat(caseData.getSmallClaimsRoadTrafficAccident().getInput())
            .isEqualTo(ROAD_TRAFFIC_ACCIDENT_SMALL_CLAIMS);
    }

    @Test
    void shouldPopulateFlightDelayHearingAndNotes() {
        CaseData caseData = CaseData.builder().build();

        service.applyFlightDelaySection(caseData, List.of(OrderDetailsPagesSectionsToggle.SHOW));
        service.applyHearingSection(caseData);
        service.applyNotesSection(caseData);

        assertThat(caseData.getSmallClaimsFlightDelay().getSmallClaimsFlightDelayToggle())
            .containsExactly(OrderDetailsPagesSectionsToggle.SHOW);
        assertThat(caseData.getSmallClaimsFlightDelay().getRelatedClaimsInput())
            .isEqualTo(FLIGHT_DELAY_RELATED_CLAIMS_NOTICE);
        assertThat(caseData.getSmallClaimsFlightDelay().getLegalDocumentsInput())
            .isEqualTo(FLIGHT_DELAY_LEGAL_ARGUMENTS_NOTICE);
        assertThat(caseData.getSmallClaimsHearing().getInput2()).isNotBlank();
        assertThat(caseData.getSmallClaimsHearing().getInput1()).isEqualTo(SMALL_CLAIMS_HEARING_LISTING_NOTICE);
        assertThat(caseData.getSmallClaimsNotes().getInput())
            .startsWith(ORDER_WITHOUT_HEARING_RECEIVED_BY_COURT_NO_ARTICLE);
    }
}
