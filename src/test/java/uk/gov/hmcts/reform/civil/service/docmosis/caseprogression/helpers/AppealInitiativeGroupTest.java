package uk.gov.hmcts.reform.civil.service.docmosis.caseprogression.helpers;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.civil.enums.finalorders.AppealList;
import uk.gov.hmcts.reform.civil.enums.finalorders.ApplicationAppealList;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.docmosis.casepogression.JudgeFinalOrderForm;
import uk.gov.hmcts.reform.civil.model.finalorders.AppealChoiceSecondDropdown;
import uk.gov.hmcts.reform.civil.model.finalorders.AppealGrantedRefused;
import uk.gov.hmcts.reform.civil.model.finalorders.FinalOrderAppeal;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;

import java.time.LocalDate;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(SpringExtension.class)
public class AppealInitiativeGroupTest {

    @InjectMocks
    private AppealInitiativePopulator appealInitiativePopulator;

    @Test
    void shouldPopulateAppealDetails_WhenAppealGrantedAndCircuitCourt() {
        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
            .finalOrderAppealComplex(FinalOrderAppeal
                                         .builder().applicationList(ApplicationAppealList.GRANTED)
                                         .appealGrantedDropdown(AppealGrantedRefused.builder()
                                                                    .circuitOrHighCourtList(ApplicationAppealList.CIRCUIT_COURT)
                                                                    .appealChoiceSecondDropdownA(AppealChoiceSecondDropdown.builder()
                                                                                                     .appealGrantedRefusedDate(LocalDate.now().plusDays(5))
                                                                                                     .build()).build()).build()).build();
        JudgeFinalOrderForm.JudgeFinalOrderFormBuilder builder = JudgeFinalOrderForm.builder();
        builder = appealInitiativePopulator.populateAppealDetails(builder, caseData);
        LocalDate appealGrantedDate = LocalDate.now().plusDays(5);

        JudgeFinalOrderForm form = builder.build();
        Assertions.assertEquals("true", form.getAppealGranted());
        Assertions.assertEquals("a", form.getTableAorB());
        Assertions.assertEquals(appealGrantedDate, form.getAppealDate());
    }

    @Test
    void shouldPopulateAppealDetails_WhenAppealRefusedAndCircuitCourt() {
        LocalDate appealRefusedDate = LocalDate.now().plusDays(5);
        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
            .finalOrderAppealComplex(FinalOrderAppeal
                                         .builder().applicationList(ApplicationAppealList.REFUSED)
                                         .appealRefusedDropdown(AppealGrantedRefused.builder()
                                                                    .circuitOrHighCourtListRefuse(ApplicationAppealList.CIRCUIT_COURT)
                                                                    .appealChoiceSecondDropdownA(AppealChoiceSecondDropdown.builder()
                                                                                                     .appealGrantedRefusedDate(appealRefusedDate)
                                                                                                     .build()).build()).build()).build();
        JudgeFinalOrderForm.JudgeFinalOrderFormBuilder builder = JudgeFinalOrderForm.builder();
        builder = appealInitiativePopulator.populateAppealDetails(builder, caseData);

        JudgeFinalOrderForm form = builder.build();
        Assertions.assertNull(form.getAppealGranted());
        Assertions.assertEquals("a", form.getTableAorB());
        Assertions.assertEquals(appealRefusedDate, form.getAppealDate());
    }

    @Test
    void shouldPopulateAppealDetails_WhenAppealOtherCases_AndHighCourt() {
        LocalDate appealRefusedDate = LocalDate.now().plusDays(5);
        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
            .finalOrderAppealComplex(FinalOrderAppeal
                                         .builder().applicationList(ApplicationAppealList.HIGH_COURT)
                                         .appealRefusedDropdown(AppealGrantedRefused.builder()
                                                                    .circuitOrHighCourtListRefuse(ApplicationAppealList.HIGH_COURT)
                                                                    .appealChoiceSecondDropdownA(AppealChoiceSecondDropdown.builder()
                                                                                                     .appealGrantedRefusedDate(appealRefusedDate)
                                                                                                     .build()).build()).build()).build();
        JudgeFinalOrderForm.JudgeFinalOrderFormBuilder builder = JudgeFinalOrderForm.builder();
        builder = appealInitiativePopulator.populateAppealDetails(builder, caseData);

        JudgeFinalOrderForm form = builder.build();
        Assertions.assertNull(form.getAppealGranted());
        Assertions.assertEquals("b", form.getTableAorB());
    }

    @Test
    void testGetAppealFor() {
        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
            .finalOrderRecitals(null)
            .finalOrderAppealComplex(FinalOrderAppeal.builder().list(AppealList.CLAIMANT).build()).build();
        String response = appealInitiativePopulator.getAppealFor(caseData);
        assertEquals(AppealList.CLAIMANT.name().toLowerCase() + "'s", response);
    }

    @Test
    void testGetAppealForOthers() {
        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
            .finalOrderRecitals(null)
            .finalOrderAppealComplex(FinalOrderAppeal.builder().otherText("test").list(AppealList.OTHER).build()).build();
        String response = appealInitiativePopulator.getAppealFor(caseData);
        assertEquals("test", response);
    }

    @ParameterizedTest
    @MethodSource("testCircuitOrHighCourtData")
    void testCircuitOrHighCourt(CaseData caseData, String expectedResponse) {
        String response = appealInitiativePopulator.circuitOrHighCourt(caseData);
        assertEquals(expectedResponse, response);
    }

    static Stream<Arguments> testCircuitOrHighCourtData() {
        return Stream.of(
            Arguments.of(
                CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
                    .finalOrderAppealComplex(FinalOrderAppeal
                                                 .builder().applicationList(ApplicationAppealList.REFUSED)
                                                 .appealRefusedDropdown(AppealGrantedRefused.builder()
                                                                            .circuitOrHighCourtListRefuse(ApplicationAppealList.CIRCUIT_COURT)
                                                                            .build()).build()).build(),
                "a"
            ),
            Arguments.of(
                CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
                    .finalOrderAppealComplex(FinalOrderAppeal
                                                 .builder().applicationList(ApplicationAppealList.GRANTED)
                                                 .appealGrantedDropdown(AppealGrantedRefused.builder()
                                                                            .circuitOrHighCourtList(ApplicationAppealList.CIRCUIT_COURT)
                                                                            .build()).build()).build(),
                "a"
            ),
            Arguments.of(
                CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
                    .finalOrderAppealComplex(FinalOrderAppeal
                                                 .builder().applicationList(ApplicationAppealList.REFUSED)
                                                 .appealRefusedDropdown(AppealGrantedRefused.builder()
                                                                            .circuitOrHighCourtListRefuse(ApplicationAppealList.HIGH_COURT)
                                                                            .build()).build()).build(),
                "b"
            ),
            Arguments.of(
                CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
                    .finalOrderAppealComplex(FinalOrderAppeal
                                                 .builder().applicationList(ApplicationAppealList.GRANTED)
                                                 .appealGrantedDropdown(AppealGrantedRefused.builder()
                                                                            .circuitOrHighCourtList(ApplicationAppealList.HIGH_COURT)
                                                                            .build()).build()).build(),
                "b"
            )
        );
    }

    @ParameterizedTest
    @MethodSource("testGetAppealDateData")
    void testGetAppealDate(CaseData caseData, LocalDate expectedResponse) {
        LocalDate response = appealInitiativePopulator.getAppealDate(caseData);
        assertEquals(expectedResponse, response);
    }

    static Stream<Arguments> testGetAppealDateData() {
        return Stream.of(
            Arguments.of(
                CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
                    .finalOrderAppealComplex(FinalOrderAppeal
                                                 .builder().applicationList(ApplicationAppealList.REFUSED)
                                                 .appealRefusedDropdown(AppealGrantedRefused.builder()
                                                                            .circuitOrHighCourtListRefuse(ApplicationAppealList.CIRCUIT_COURT)
                                                                            .appealChoiceSecondDropdownA(AppealChoiceSecondDropdown.builder()
                                                                                                             .appealGrantedRefusedDate(LocalDate.now().plusDays(1))
                                                                                                             .build()).build()).build()).build(),
                LocalDate.now().plusDays(1)
            ),
            Arguments.of(
                CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
                    .finalOrderAppealComplex(FinalOrderAppeal
                                                 .builder().applicationList(ApplicationAppealList.GRANTED)
                                                 .appealGrantedDropdown(AppealGrantedRefused.builder()
                                                                            .circuitOrHighCourtList(ApplicationAppealList.CIRCUIT_COURT)
                                                                            .appealChoiceSecondDropdownA(AppealChoiceSecondDropdown.builder()
                                                                                                             .appealGrantedRefusedDate(LocalDate.now().plusDays(10))
                                                                                                             .build()).build()).build()).build(),
                LocalDate.now().plusDays(10)
            ),
            Arguments.of(
                CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
                    .finalOrderAppealComplex(FinalOrderAppeal
                                                 .builder().applicationList(ApplicationAppealList.REFUSED)
                                                 .appealRefusedDropdown(AppealGrantedRefused.builder()
                                                                            .circuitOrHighCourtListRefuse(ApplicationAppealList.HIGH_COURT)
                                                                            .appealChoiceSecondDropdownB(AppealChoiceSecondDropdown.builder()
                                                                                                             .appealGrantedRefusedDate(LocalDate.now().plusDays(5))
                                                                                                             .build()).build()).build()).build(),
                LocalDate.now().plusDays(5)
            ),
            Arguments.of(
                CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
                    .finalOrderAppealComplex(FinalOrderAppeal
                                                 .builder().applicationList(ApplicationAppealList.GRANTED)
                                                 .appealGrantedDropdown(AppealGrantedRefused.builder()
                                                                            .circuitOrHighCourtList(ApplicationAppealList.HIGH_COURT)
                                                                            .appealChoiceSecondDropdownB(AppealChoiceSecondDropdown.builder()
                                                                                                             .appealGrantedRefusedDate(LocalDate.now().plusDays(5))
                                                                                                             .build()).build()).build()).build(),
                LocalDate.now().plusDays(5)
            )
        );
    }

}
