package uk.gov.hmcts.reform.civil.handler.callback.camunda.notification;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.enums.HwFMoreInfoRequiredDocuments;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.IdamUserDetails;
import uk.gov.hmcts.reform.civil.model.citizenui.HelpWithFeesMoreInformation;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.sampledata.CallbackParamsBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotifyClaimantMoreInformationNeededNotificationHandler.TASK_ID;

@ExtendWith(MockitoExtension.class)
class NotifyClaimantMoreInformationNeededNotificationHandlerTest extends BaseCallbackHandlerTest {

    @Mock
    private NotificationService notificationService;
    @Mock
    private NotificationsProperties notificationsProperties;
    @InjectMocks
    private NotifyClaimantMoreInformationNeededNotificationHandler handler;

    @Nested
    class AboutToSubmitCallback {

        @BeforeEach
        void setup() {
            when(notificationsProperties.getNotifyApplicantForHwFMoreInformationNeeded())
                .thenReturn("notify-claimant-hwf-more-information-needed-notification");
        }

        @Test
        void shouldNotifyClaimantForClaimFee_whenInvoked() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateSpec1v1ClaimSubmitted()
                .specRespondent1Represented(YesOrNo.NO)
                .applicant1Represented(YesOrNo.NO)
                .respondent1ResponseDeadline(LocalDateTime.MAX)
                .respondent1Represented(YesOrNo.NO)
                .helpWithFeesMoreInformationClaimIssue(HelpWithFeesMoreInformation.builder()
                                                           .hwFMoreInfoDocumentDate(LocalDate.now())
                                                           .hwFMoreInfoRequiredDocuments(getDocumentList())
                                                           .build())
                .claimantUserDetails(
                    IdamUserDetails.builder().email("email@email.com").build())
                .build();

            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                CallbackRequest.builder().eventId("NOTIFY_APPLICANT_MORE_INFORMATION_NEEDED").build()).build();

            handler.handle(params);

            verify(notificationService).sendMail(
                any(), any(), anyMap(), any()
            );
        }

        @Test
        void shouldNotifyClaimantForHearingFee_whenInvoked() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateSpec1v1ClaimSubmitted()
                .specRespondent1Represented(YesOrNo.NO)
                .applicant1Represented(YesOrNo.NO)
                .respondent1ResponseDeadline(LocalDateTime.MAX)
                .respondent1Represented(YesOrNo.NO)
                .helpWithFeesMoreInformationHearing(HelpWithFeesMoreInformation.builder()
                                                        .hwFMoreInfoDocumentDate(LocalDate.now())
                                                        .hwFMoreInfoRequiredDocuments(getDocumentList())
                                                        .build())
                .claimantUserDetails(
                    IdamUserDetails.builder().email("email@email.com").build())
                .build();

            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                CallbackRequest.builder().eventId("NOTIFY_APPLICANT_MORE_INFORMATION_NEEDED").build()).build();

            handler.handle(params);

            verify(notificationService).sendMail(
                any(), any(), anyMap(), any()
            );
        }

    }

    @Test
    void shouldReturnCorrectCamundaActivityId_whenInvoked() {
        assertThat(handler.camundaActivityId(CallbackParamsBuilder.builder().request(CallbackRequest.builder().eventId(
            "NOTIFY_APPLICANT_MORE_INFORMATION_NEEDED").build()).build())).isEqualTo(TASK_ID);

    }

    private List<HwFMoreInfoRequiredDocuments> getDocumentList() {
        List<HwFMoreInfoRequiredDocuments> docList = new ArrayList<>();
        docList.add(HwFMoreInfoRequiredDocuments.CHILD_MAINTENANCE);
        return docList;
    }

}
