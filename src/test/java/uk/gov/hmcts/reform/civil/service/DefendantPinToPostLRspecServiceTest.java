package uk.gov.hmcts.reform.civil.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
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
import uk.gov.hmcts.reform.civil.service.search.exceptions.CaseNotFoundException;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.UPDATE_CASE_DATA;

@SpringBootTest(classes = {
    DefendantPinToPostLRspecService.class,
    CaseDetailsConverter.class,
})

class DefendantPinToPostLRspecServiceTest {

    private static final String CASE_ID = "1";

    @Autowired
    private DefendantPinToPostLRspecService defendantPinToPostLRspecService;

    @MockBean
    private ObjectMapper objectMapper;

    @MockBean
    private CoreCaseDataService coreCaseDataService;

    @Nested
    class BuildDefendantPinToPost {

        @Test
        void shouldBuildDefendantPinToPost_whenInvoked() {
            DefendantPinToPostLRspec defendantPinToPostLRspec = defendantPinToPostLRspecService
                .buildDefendantPinToPost();
            assertThat(defendantPinToPostLRspec.getExpiryDate())
                .isEqualTo(getDate180days());
            assertThat(defendantPinToPostLRspec.getRespondentCaseRole())
                .isEqualTo(CaseRole.RESPONDENTSOLICITORONESPEC.getFormattedName());
            assertThat(defendantPinToPostLRspec.getAccessCode())
                .isNotEmpty();
        }

        @Test
        void shouldRunRemovePinInPostData_whenInvoked() {
            CaseData caseData = new CaseDataBuilder().atStateClaimSubmitted()
                .businessProcess(BusinessProcess.builder().status(BusinessProcessStatus.READY).build())
                .build();

            CaseDetails caseDetails = CaseDetailsBuilder.builder().data(caseData).build();

            when(coreCaseDataService.startUpdate(caseData.getCcdCaseReference().toString(), UPDATE_CASE_DATA))
                .thenReturn(StartEventResponse.builder().caseDetails(caseDetails).build());
            when(coreCaseDataService.submitUpdate(eq(caseData.getCcdCaseReference().toString()), any(CaseDataContent.class))).thenReturn(caseData);

            defendantPinToPostLRspecService.removePinInPostData(caseData.getCcdCaseReference());

            verify(coreCaseDataService).startUpdate(caseData.getCcdCaseReference().toString(), UPDATE_CASE_DATA);
            verify(coreCaseDataService).submitUpdate(eq(caseData.getCcdCaseReference().toString()), any(CaseDataContent.class));
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

            assertThrows(
                PinNotMatchException.class,
                () ->  defendantPinToPostLRspecService.checkPinValid(caseData, "TEST0000"));
        }
    }

    private LocalDate getDate180days() {
        return LocalDate.now()
            .plusDays(180);
    }

}
