package uk.gov.hmcts.reform.civil.notification.handlers.createclaimspecafterpayment;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.config.SystemUpdateUserConfiguration;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.service.BulkPrintService;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.Time;
import uk.gov.hmcts.reform.civil.service.UserService;
import uk.gov.hmcts.reform.civil.service.docmosis.pip.PiPLetterGenerator;

import java.time.LocalDateTime;
import java.util.Set;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyList;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.argThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.enums.CaseState.AWAITING_RESPONDENT_ACKNOWLEDGEMENT;

@ExtendWith(MockitoExtension.class)
class ClaimContinuingOnlineSpecRespondentPartyNotifierTest {

    public static final String CLAIM_NOTIFICATION_DATE = "claimNotificationDate";
    public static final String EVENT_ID = "eventId";
    public static final String TASK_ID = "taskId";
    public static final String RESPONDENT_NAME = "Respondent Name";
    public static final String CONTINUING_ONLINE_SPEC_CLAIM_NOTIFIER = "ContinuingOnlineSpecClaimNotifier";
    public static final String FIRST_CONTACT_PACK = "first-contact-pack";
    public static final String TEST_BEARER_TOKEN = "test-bearer-token";
    public static final long CCD_CASE_REFERENCE = 1234L;

    @Mock
    private CoreCaseDataService caseDataService;

    @Mock
    private FeatureToggleService featureToggleService;

    @Mock
    private PiPLetterGenerator pipLetterGenerator;

    @Mock
    private BulkPrintService bulkPrintService;

    @Mock
    private Time time;

    @Mock
    private UserService userService;

    @Mock
    private SystemUpdateUserConfiguration userConfig;

    @Mock
    private ClaimContinuingOnlineSpecRespondentPartyEmailGenerator partiesGenerator;

    @InjectMocks
    private ClaimContinuingOnlineSpecRespondentPartyNotifier notifier;

    @BeforeEach
    void setup() {
        lenient().when(partiesGenerator.getPartiesToNotify(any(CaseData.class), any())).thenReturn(Set.of());
        lenient().when(userConfig.getUserName()).thenReturn("test-username");
        lenient().when(userConfig.getPassword()).thenReturn("test-password");
        lenient().when(time.now()).thenReturn(LocalDateTime.now());
    }

    @Test
    void shouldNotifyPartiesAndTriggerEvent() {
        Party respondent1 = Party.builder().companyName(RESPONDENT_NAME).type(Party.Type.COMPANY).build();
        CaseData caseData = CaseData.builder()
                .ccdCaseReference(CCD_CASE_REFERENCE)
                .respondent1(respondent1)
                .build();
        String bearerToken = TEST_BEARER_TOKEN;
        byte[] letterContent = new byte[0];
        when(userService.getAccessToken(anyString(), anyString())).thenReturn(bearerToken);
        when(pipLetterGenerator.downloadLetter(caseData, bearerToken)).thenReturn(letterContent);

        notifier.notifyParties(caseData, EVENT_ID, TASK_ID);

        verify(caseDataService).triggerEvent(
                eq(caseData.getCcdCaseReference()),
                eq(CaseEvent.NOTIFY_RESPONDENT_SOLICITOR1_FOR_CLAIM_CONTINUING_ONLINE_SPEC),
                ArgumentMatchers.any()
        );
        verify(bulkPrintService).printLetter(
                eq(letterContent),
                eq(caseData.getLegacyCaseReference()),
                eq(caseData.getLegacyCaseReference()),
                eq(FIRST_CONTACT_PACK),
                anyList()
        );
    }

    @Test
    void shouldSetStateToAwaitingRespondentAcknowledgementWhenNotBilingual() {
        Party respondent1 = Party.builder().companyName(RESPONDENT_NAME).type(Party.Type.COMPANY).build();
        CaseData caseData = CaseData.builder()
                .ccdCaseReference(CCD_CASE_REFERENCE)
                .respondent1(respondent1)
                .build();

        notifier.notifyParties(caseData, EVENT_ID, TASK_ID);

        verify(caseDataService).triggerEvent(
                eq(caseData.getCcdCaseReference()),
                eq(CaseEvent.NOTIFY_RESPONDENT_SOLICITOR1_FOR_CLAIM_CONTINUING_ONLINE_SPEC),
                argThat(updates -> AWAITING_RESPONDENT_ACKNOWLEDGEMENT.name().equals(updates.get("state")))
        );
    }

    @Test
    void shouldReturnCorrectTaskId() {
        String taskId = notifier.getTaskId();

        Assertions.assertEquals(CONTINUING_ONLINE_SPEC_CLAIM_NOTIFIER, taskId);
    }

    @Test
    void shouldSetBilingualLipvLipFlagCorrectly() {
        Party respondent1 = Party.builder().companyName(RESPONDENT_NAME).type(Party.Type.COMPANY).build();
        CaseData caseData = spy(CaseData.builder()
                .respondent1(respondent1)
                .ccdCaseReference(CCD_CASE_REFERENCE)
                .build());

        doReturn(true).when(caseData).isLipvLipOneVOne();
        doReturn(true).when(caseData).isClaimantBilingual();
        when(featureToggleService.isLipVLipEnabled()).thenReturn(true);
        LocalDateTime now = LocalDateTime.now();
        when(time.now()).thenReturn(now);

        notifier.notifyParties(caseData, EVENT_ID, TASK_ID);

        verify(caseDataService).triggerEvent(
                eq(caseData.getCcdCaseReference()),
                eq(CaseEvent.NOTIFY_RESPONDENT_SOLICITOR1_FOR_CLAIM_CONTINUING_ONLINE_SPEC),
                argThat(updates -> updates.containsKey(CLAIM_NOTIFICATION_DATE)
                        && updates.get(CLAIM_NOTIFICATION_DATE).equals(now))
        );
    }
}