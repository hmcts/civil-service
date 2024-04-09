package uk.gov.hmcts.reform.civil.handler.callback.camunda.notification;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.civil.config.PinInPostConfiguration;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.enums.dq.Language;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.ChangeOfRepresentation;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.citizenui.CaseDataLiP;
import uk.gov.hmcts.reform.civil.model.citizenui.RespondentLiPResponse;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.prd.model.Organisation;
import uk.gov.hmcts.reform.civil.sampledata.CallbackParamsBuilder;
import uk.gov.hmcts.reform.civil.service.OrganisationService;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_CLAIMANT_DEFENDANT_REPRESENTED;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_DEFENDANT_AFTER_NOC_APPROVAL;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_DEFENDANT_SOLICITOR_AFTER_NOC_APPROVAL;

@ExtendWith(MockitoExtension.class)
public class NotificationForDefendantRepresentedTest extends BaseCallbackHandlerTest {

    private NotificationForDefendantRepresented notificationHandler;

    @Mock
    private NotificationService notificationService;
    @Mock
    private NotificationsProperties notificationsProperties;
    @Mock
    private PinInPostConfiguration pipInPostConfiguration;
    @Mock
    private OrganisationService organisationService;
    @Captor
    private ArgumentCaptor<String> targetEmail;
    @Captor
    private ArgumentCaptor<String> emailTemplate;
    @Captor
    private ArgumentCaptor<Map<String, String>> notificationDataMap;

    @Captor
    private ArgumentCaptor<String> reference;


    private static final String DEFENDANT_EMAIL_ADDRESS = "defendantmail@hmcts.net";
    private static final String APPLICANT_EMAIL_ADDRESS = "applicantmail@hmcts.net";
    private static final String DEFENDANT_PARTY_NAME = "ABC ABC";
    private static final String REFERENCE_NUMBER = "83729462374";
    private static final String EMAIL_TEMPLATE = "test-notification-id";
    private static final String CLAIMANT_ORG_NAME = "Org Name";

    @BeforeEach
    void setup() {
        notificationHandler = new NotificationForDefendantRepresented(
            organisationService,
            notificationService,
            notificationsProperties
        );
    }

    @ParameterizedTest
    @MethodSource("testCases")
    void notificationForClaimantLipDefendantNOCApproval(Language languagePreference) {
        //Given
        CaseData caseData = CaseData.builder()
            .respondent1(Party.builder().type(Party.Type.COMPANY).companyName(DEFENDANT_PARTY_NAME).partyEmail(
                DEFENDANT_EMAIL_ADDRESS).build())
            .applicant1(Party.builder().type(Party.Type.COMPANY).companyName(CLAIMANT_ORG_NAME)
                            .partyEmail(APPLICANT_EMAIL_ADDRESS).build())
            .legacyCaseReference(REFERENCE_NUMBER)
            .addApplicant2(YesOrNo.NO)
            .addRespondent2(YesOrNo.NO)
            .applicant1Represented(YesOrNo.NO)
            .claimantBilingualLanguagePreference(languagePreference.toString())
            .build();
        if (languagePreference == Language.BOTH) {
            when(notificationsProperties.getNotifyClaimantLipBilingualAfterDefendantNOC())
                .thenReturn(EMAIL_TEMPLATE);
        } else {
            when(notificationsProperties.getNotifyClaimantLipForDefendantRepresentedTemplate())
                .thenReturn(EMAIL_TEMPLATE);
        }

        //When
        notificationHandler.handle(CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData)
                                       .request(CallbackRequest.builder()
                                                    .eventId(NOTIFY_CLAIMANT_DEFENDANT_REPRESENTED.name()).build())
                                       .build());

        //Then
        verify(notificationService, times(1)).sendMail(targetEmail.capture(),
                                                       emailTemplate.capture(),
                                                       notificationDataMap.capture(), reference.capture()
        );
        assertThat(targetEmail.getAllValues().get(0)).isEqualTo(APPLICANT_EMAIL_ADDRESS);
        assertThat(emailTemplate.getAllValues().get(0)).isEqualTo(EMAIL_TEMPLATE);
    }

    @ParameterizedTest
    @MethodSource("testCases")
    void notificationForDefendantLipDefendantNOCApproval(Language languagePreference) {
        //Given
        CaseData caseData = CaseData.builder()
            .respondent1(Party.builder().type(Party.Type.COMPANY).companyName(DEFENDANT_PARTY_NAME).partyEmail(
                DEFENDANT_EMAIL_ADDRESS).build())
            .applicant1(Party.builder().type(Party.Type.COMPANY).companyName(CLAIMANT_ORG_NAME)
                            .partyEmail(APPLICANT_EMAIL_ADDRESS).build())
            .legacyCaseReference(REFERENCE_NUMBER)
            .addApplicant2(YesOrNo.NO)
            .addRespondent2(YesOrNo.NO)
            .respondent1Represented(YesOrNo.NO)
            .caseDataLiP(CaseDataLiP.builder().respondent1LiPResponse(RespondentLiPResponse.builder()
                                                                          .respondent1ResponseLanguage(
                                                                              languagePreference.toString()).build())
                             .build())
            .build();
        if (languagePreference == Language.BOTH) {
            given(notificationsProperties.getNotifyDefendantLipBilingualAfterDefendantNOC())
                .willReturn(EMAIL_TEMPLATE);
        } else {
            given(notificationsProperties.getNotifyDefendantLipForNoLongerAccessTemplate())
                .willReturn(EMAIL_TEMPLATE);
        }

        //When
        notificationHandler.handle(CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData)
                                       .request(CallbackRequest.builder()
                                                    .eventId(NOTIFY_DEFENDANT_AFTER_NOC_APPROVAL.name()).build())
                                       .build());

        //Then
        verify(notificationService, times(1)).sendMail(targetEmail.capture(),
                                                       emailTemplate.capture(),
                                                       notificationDataMap.capture(), reference.capture()
        );
        assertThat(targetEmail.getAllValues().get(0)).isEqualTo(DEFENDANT_EMAIL_ADDRESS);
        assertThat(emailTemplate.getAllValues().get(0)).isEqualTo(EMAIL_TEMPLATE);
    }

    @Test
    void notificationForLRAfterDefendantNOCApproval() {
        //Given
        CaseData caseData = CaseData.builder()
            .respondent1(Party.builder().type(Party.Type.COMPANY).companyName(DEFENDANT_PARTY_NAME).partyEmail(
                DEFENDANT_EMAIL_ADDRESS).build())
            .applicant1(Party.builder().type(Party.Type.COMPANY).companyName(CLAIMANT_ORG_NAME)
                            .partyEmail(APPLICANT_EMAIL_ADDRESS).build())
            .legacyCaseReference(REFERENCE_NUMBER)
            .addApplicant2(YesOrNo.NO)
            .addRespondent2(YesOrNo.NO)
            .changeOfRepresentation(ChangeOfRepresentation.builder().organisationToAddID("lr123").build())
            .respondent1Represented(YesOrNo.YES)
            .respondentSolicitor1EmailAddress("LR@gmail.com")
            .build();

        when(notificationsProperties.getNotifyDefendantLrAfterNoticeOfChangeTemplate())
            .thenReturn(EMAIL_TEMPLATE);

        when(organisationService.findOrganisationById("lr123")).thenReturn(Optional.ofNullable(Organisation.builder()
                                                                                                   .name("test org")
                                                                                                   .build()));

        //When
        notificationHandler.handle(CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData)
                                       .request(CallbackRequest.builder()
                                                    .eventId(NOTIFY_DEFENDANT_SOLICITOR_AFTER_NOC_APPROVAL.name())
                                                    .build())
                                       .build());

        //Then
        verify(notificationService, times(1)).sendMail(targetEmail.capture(),
                                                       emailTemplate.capture(),
                                                       notificationDataMap.capture(), reference.capture()
        );
        assertThat(targetEmail.getAllValues().get(0)).isEqualTo("LR@gmail.com");
        assertThat(emailTemplate.getAllValues().get(0)).isEqualTo(EMAIL_TEMPLATE);
    }


    private static Stream<Object[]> testCases() {
        return Stream.of(
            new Object[] {Language.ENGLISH},
            new Object[] {Language.BOTH}
        );
    }
}
