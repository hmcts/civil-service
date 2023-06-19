package uk.gov.hmcts.reform.civil.service;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.civil.enums.BusinessProcessStatus;
import uk.gov.hmcts.reform.civil.enums.CaseRole;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.BusinessProcess;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.DefendantPinToPostLRspec;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDetailsBuilder;
import uk.gov.hmcts.reform.civil.service.pininpost.DefendantPinToPostLRspecService;
import uk.gov.hmcts.reform.civil.service.pininpost.exception.PinNotMatchException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.UPDATE_CASE_DATA;

@SpringBootTest(classes = {
    DefendantPinToPostLRspecService.class,
    JacksonAutoConfiguration.class
})
class DefendantPinToPostLRspecServiceTest {

    private static final String CASE_ID = "1";

    @Autowired
    private DefendantPinToPostLRspecService defendantPinToPostLRspecService;

    @MockBean
    private CoreCaseDataService coreCaseDataService;

    @MockBean
    private CaseDetailsConverter caseDetailsConverter;

    @Nested
    class BuildDefendantPinToPost {

        @Test
        void shouldBuildDefendantPinToPost_whenInvoked() {
            DefendantPinToPostLRspec defendantPinToPostLRspec = defendantPinToPostLRspecService
                .buildDefendantPinToPost();
            assertThat(defendantPinToPostLRspec.getExpiryDate())
                .isEqualTo(getDate180days());
            assertThat(defendantPinToPostLRspec.getRespondentCaseRole())
                .isEqualTo(CaseRole.DEFENDANT.getFormattedName());
            assertThat(defendantPinToPostLRspec.getAccessCode())
                .isNotEmpty();
        }

        @Test
        void shouldRunRemovePinInPostData_whenInvoked() {
            CaseData caseData = new CaseDataBuilder().atStateClaimSubmitted()
                .addRespondent1PinToPostLRspec(DefendantPinToPostLRspec.builder()
                                                   .accessCode("TEST1234")
                                                   .expiryDate(LocalDate.now().plusDays(180))
                                                   .build())
                .businessProcess(BusinessProcess.builder().status(BusinessProcessStatus.READY).build())
                .build();

            DefendantPinToPostLRspec pinInPostData = DefendantPinToPostLRspec.builder()
                .expiryDate(LocalDate.now().plusDays(180))
                .build();

            Map<String, Object> data = new HashMap<>();
            data.put("respondent1PinToPostLRspec", pinInPostData);

            defendantPinToPostLRspecService.removePinInPostData(caseData.getCcdCaseReference(), pinInPostData);

            verify(coreCaseDataService).triggerEvent(caseData.getCcdCaseReference(), UPDATE_CASE_DATA, data);
        }

        @Test
        void shouldCheckPinNotValid_whenInvoked() {
            CaseData caseData = new CaseDataBuilder().atStateClaimSubmitted()
                .businessProcess(BusinessProcess.builder().status(BusinessProcessStatus.READY).build())
                .addRespondent1PinToPostLRspec(DefendantPinToPostLRspec.builder()
                                                   .accessCode("TEST1234")
                                                   .expiryDate(LocalDate.now().plusDays(180))
                                                   .build())
                .build();

            CaseDetails caseDetails = CaseDetailsBuilder.builder().data(caseData).build();

            when(caseDetailsConverter.toCaseData(caseDetails)).thenReturn(caseData);

            assertThrows(
                PinNotMatchException.class,
                () ->  defendantPinToPostLRspecService.validatePin(caseDetails, "TEST0000"));
        }

        @Test
        void shouldCheckPinNotValidNoAccessCode_whenInvoked() {
            CaseData caseData = new CaseDataBuilder().atStateClaimSubmitted()
                .businessProcess(BusinessProcess.builder().status(BusinessProcessStatus.READY).build())
                .addRespondent1PinToPostLRspec(DefendantPinToPostLRspec.builder()
                                                   .expiryDate(LocalDate.now().plusDays(180))
                                                   .build())
                .build();

            CaseDetails caseDetails = CaseDetailsBuilder.builder().data(caseData).build();

            when(caseDetailsConverter.toCaseData(caseDetails)).thenReturn(caseData);

            assertThrows(
                PinNotMatchException.class,
                () ->  defendantPinToPostLRspecService.validatePin(caseDetails, "TEST0000"));
        }

        @Test
        void shouldCheckPinNotValidNoPinToPostObject_whenInvoked() {
            CaseData caseData = new CaseDataBuilder().atStateClaimSubmitted()
                .businessProcess(BusinessProcess.builder().status(BusinessProcessStatus.READY).build())
                .build();

            CaseDetails caseDetails = CaseDetailsBuilder.builder().data(caseData).build();

            when(caseDetailsConverter.toCaseData(caseDetails)).thenReturn(caseData);

            assertThrows(
                PinNotMatchException.class,
                () ->  defendantPinToPostLRspecService.validatePin(caseDetails, "TEST1234"));
        }

        @Test
        void shouldCheckPinNotValidPinExpired_whenInvoked() {
            CaseData caseData = new CaseDataBuilder().atStateClaimSubmitted()
                .businessProcess(BusinessProcess.builder().status(BusinessProcessStatus.READY).build())
                .addRespondent1PinToPostLRspec(DefendantPinToPostLRspec.builder()
                                                   .accessCode("TEST1234")
                                                   .expiryDate(LocalDate.now().minusDays(1))
                                                   .build())
                .build();

            CaseDetails caseDetails = CaseDetailsBuilder.builder().data(caseData).build();

            when(caseDetailsConverter.toCaseData(caseDetails)).thenReturn(caseData);

            assertThrows(
                PinNotMatchException.class,
                () ->  defendantPinToPostLRspecService.validatePin(caseDetails, "TEST1234"));
        }
    }

    @Test
    void shouldResetPinExpiryDateSuccessfully() {
        LocalDate expiryDate = LocalDate.of(
            2022,
            1,
            1);
        DefendantPinToPostLRspec initialPin = DefendantPinToPostLRspec.builder()
                                               .accessCode("TEST1234")
                                               .expiryDate(expiryDate)
                                               .build();

        DefendantPinToPostLRspec resetPin = defendantPinToPostLRspecService.resetPinExpiryDate(initialPin);

        assertThat(resetPin.getExpiryDate()).isEqualTo(LocalDate.now().plusDays(180));
        assertThat(resetPin.getAccessCode()).isEqualTo(initialPin.getAccessCode());
    }

    private LocalDate getDate180days() {
        return LocalDate.now()
            .plusDays(180);
    }

}
