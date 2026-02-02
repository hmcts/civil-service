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
            .finalOrderAppealComplex(new FinalOrderAppeal()
                                         .setApplicationList(ApplicationAppealList.GRANTED)
                                         .setAppealGrantedDropdown(new AppealGrantedRefused()
                                                                       .setCircuitOrHighCourtList(ApplicationAppealList.CIRCUIT_COURT)
                                                                       .setAppealChoiceSecondDropdownA(new AppealChoiceSecondDropdown()
                                                                                                           .setAppealGrantedRefusedDate(
                                                                                                               LocalDate.now().plusDays(
                                                                                                                   5))))).build();
        JudgeFinalOrderForm form = new JudgeFinalOrderForm();
        form = appealInitiativePopulator.populateAppealDetails(form, caseData);
        LocalDate appealGrantedDate = LocalDate.now().plusDays(5);
        Assertions.assertEquals("true", form.getAppealGranted());
        Assertions.assertEquals("a", form.getTableAorB());
        Assertions.assertEquals(appealGrantedDate, form.getAppealDate());
    }

    @Test
    void shouldPopulateAppealDetails_WhenAppealRefusedAndCircuitCourt() {
        LocalDate appealRefusedDate = LocalDate.now().plusDays(5);
        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
            .finalOrderAppealComplex(new FinalOrderAppeal()
                                         .setApplicationList(ApplicationAppealList.REFUSED)
                                         .setAppealRefusedDropdown(new AppealGrantedRefused()
                                                                       .setCircuitOrHighCourtListRefuse(
                                                                           ApplicationAppealList.CIRCUIT_COURT)
                                                                       .setAppealChoiceSecondDropdownA(new AppealChoiceSecondDropdown()
                                                                                                           .setAppealGrantedRefusedDate(
                                                                                                               appealRefusedDate)))).build();
        JudgeFinalOrderForm form = new JudgeFinalOrderForm();
        form = appealInitiativePopulator.populateAppealDetails(form, caseData);
        Assertions.assertNull(form.getAppealGranted());
        Assertions.assertEquals("a", form.getTableAorB());
        Assertions.assertEquals(appealRefusedDate, form.getAppealDate());
    }

    @Test
    void shouldPopulateAppealDetails_WhenAppealOtherCases_AndHighCourt() {
        LocalDate appealRefusedDate = LocalDate.now().plusDays(5);
        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
            .finalOrderAppealComplex(new FinalOrderAppeal()
                                         .setApplicationList(ApplicationAppealList.HIGH_COURT)
                                         .setAppealRefusedDropdown(new AppealGrantedRefused()
                                                                       .setCircuitOrHighCourtListRefuse(
                                                                           ApplicationAppealList.HIGH_COURT)
                                                                       .setAppealChoiceSecondDropdownA(new AppealChoiceSecondDropdown()
                                                                                                           .setAppealGrantedRefusedDate(
                                                                                                               appealRefusedDate)))).build();
        JudgeFinalOrderForm form = new JudgeFinalOrderForm();
        form = appealInitiativePopulator.populateAppealDetails(form, caseData);
        Assertions.assertNull(form.getAppealGranted());
        Assertions.assertEquals("b", form.getTableAorB());
    }

    @Test
    void testGetAppealFor() {
        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
            .finalOrderRecitals(null)
            .finalOrderAppealComplex(new FinalOrderAppeal().setList(AppealList.CLAIMANT)).build();
        String response = appealInitiativePopulator.getAppealFor(caseData);
        assertEquals(AppealList.CLAIMANT.name().toLowerCase() + "'s", response);
    }

    @Test
    void testGetAppealForOthers() {
        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
            .finalOrderRecitals(null)
            .finalOrderAppealComplex(new FinalOrderAppeal().setOtherText("test").setList(AppealList.OTHER)).build();
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
                    .finalOrderAppealComplex(new FinalOrderAppeal()
                                                 .setApplicationList(ApplicationAppealList.REFUSED)
                                                 .setAppealRefusedDropdown(new AppealGrantedRefused()
                                                                               .setCircuitOrHighCourtListRefuse(
                                                                                   ApplicationAppealList.CIRCUIT_COURT))).build(),
                "a"
            ),
            Arguments.of(
                CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
                    .finalOrderAppealComplex(new FinalOrderAppeal()
                                                 .setApplicationList(ApplicationAppealList.GRANTED)
                                                 .setAppealGrantedDropdown(new AppealGrantedRefused()
                                                                               .setCircuitOrHighCourtList(
                                                                                   ApplicationAppealList.CIRCUIT_COURT))).build(),
                "a"
            ),
            Arguments.of(
                CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
                    .finalOrderAppealComplex(new FinalOrderAppeal()
                                                 .setApplicationList(ApplicationAppealList.REFUSED)
                                                 .setAppealRefusedDropdown(new AppealGrantedRefused()
                                                                               .setCircuitOrHighCourtListRefuse(
                                                                                   ApplicationAppealList.HIGH_COURT))).build(),
                "b"
            ),
            Arguments.of(
                CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
                    .finalOrderAppealComplex(new FinalOrderAppeal()
                                                 .setApplicationList(ApplicationAppealList.GRANTED)
                                                 .setAppealGrantedDropdown(new AppealGrantedRefused()
                                                                               .setCircuitOrHighCourtList(
                                                                                   ApplicationAppealList.HIGH_COURT))).build(),
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
                    .finalOrderAppealComplex(new FinalOrderAppeal()
                                                 .setApplicationList(ApplicationAppealList.REFUSED)
                                                 .setAppealRefusedDropdown(new AppealGrantedRefused()
                                                                               .setCircuitOrHighCourtListRefuse(
                                                                                   ApplicationAppealList.CIRCUIT_COURT)
                                                                               .setAppealChoiceSecondDropdownA(new AppealChoiceSecondDropdown()
                                                                                                                   .setAppealGrantedRefusedDate(
                                                                                                                       LocalDate.now().plusDays(
                                                                                                                           1))))).build(),
                LocalDate.now().plusDays(1)
            ),
            Arguments.of(
                CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
                    .finalOrderAppealComplex(new FinalOrderAppeal()
                                                 .setApplicationList(ApplicationAppealList.GRANTED)
                                                 .setAppealGrantedDropdown(new AppealGrantedRefused()
                                                                               .setCircuitOrHighCourtList(
                                                                                   ApplicationAppealList.CIRCUIT_COURT)
                                                                               .setAppealChoiceSecondDropdownA(new AppealChoiceSecondDropdown()
                                                                                                                   .setAppealGrantedRefusedDate(
                                                                                                                       LocalDate.now().plusDays(
                                                                                                                           10))))).build(),
                LocalDate.now().plusDays(10)
            ),
            Arguments.of(
                CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
                    .finalOrderAppealComplex(new FinalOrderAppeal()
                                                 .setApplicationList(ApplicationAppealList.REFUSED)
                                                 .setAppealRefusedDropdown(new AppealGrantedRefused()
                                                                               .setCircuitOrHighCourtListRefuse(
                                                                                   ApplicationAppealList.HIGH_COURT)
                                                                               .setAppealChoiceSecondDropdownB(new AppealChoiceSecondDropdown()
                                                                                                                   .setAppealGrantedRefusedDate(
                                                                                                                       LocalDate.now().plusDays(
                                                                                                                           5))))).build(),
                LocalDate.now().plusDays(5)
            ),
            Arguments.of(
                CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
                    .finalOrderAppealComplex(new FinalOrderAppeal()
                                                 .setApplicationList(ApplicationAppealList.GRANTED)
                                                 .setAppealGrantedDropdown(new AppealGrantedRefused()
                                                                               .setCircuitOrHighCourtList(
                                                                                   ApplicationAppealList.HIGH_COURT)
                                                                               .setAppealChoiceSecondDropdownB(new AppealChoiceSecondDropdown()
                                                                                                                   .setAppealGrantedRefusedDate(
                                                                                                                       LocalDate.now().plusDays(
                                                                                                                           5))))).build(),
                LocalDate.now().plusDays(5)
            )
        );
    }

}
