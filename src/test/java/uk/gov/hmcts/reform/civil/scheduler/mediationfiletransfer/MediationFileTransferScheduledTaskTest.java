package uk.gov.hmcts.reform.civil.scheduler.mediationfiletransfer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.config.properties.mediation.MediationCSVEmailConfiguration;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sendgrid.EmailData;
import uk.gov.hmcts.reform.civil.sendgrid.SendGridClient;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;
import uk.gov.hmcts.reform.civil.service.mediation.MediationCSVLrvLipService;
import uk.gov.hmcts.reform.civil.service.mediation.MediationCase;
import uk.gov.hmcts.reform.civil.service.mediation.MediationCsvServiceFactory;
import uk.gov.hmcts.reform.civil.service.mediation.MediationJsonService;
import uk.gov.hmcts.reform.civil.service.search.MediationCasesSearchService;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MediationFileTransferScheduledTaskTest {

    private static final long CASE_ID = 123L;
    private static final String SENDER = "sender@example.com";
    private static final String RECIPIENT = "recipient@example.com";

    @Mock
    private MediationCasesSearchService caseSearchService;
    @Mock
    private CoreCaseDataService coreCaseDataService;
    @Mock
    private CaseDetailsConverter caseDetailsConverter;
    @Mock
    private SendGridClient sendGridClient;
    @Mock
    private MediationCSVEmailConfiguration mediationCSVEmailConfiguration;
    @Mock
    private MediationCsvServiceFactory mediationCsvServiceFactory;
    @Mock
    private MediationCSVLrvLipService mediationCsvService;
    @Mock
    private MediationJsonService mediationJsonService;

    private MediationFileTransferScheduledTask task;
    private CaseDetails caseDetails;
    private CaseData caseData;

    @BeforeEach
    void setUp() {
        task = new MediationFileTransferScheduledTask(
            caseSearchService,
            coreCaseDataService,
            caseDetailsConverter,
            sendGridClient,
            mediationCSVEmailConfiguration,
            mediationCsvServiceFactory,
            mediationJsonService
        );
        caseDetails = CaseDetails.builder().id(CASE_ID).data(Map.of()).build();
        caseData = CaseData.builder().ccdCaseReference(CASE_ID).build();
    }

    @Test
    void shouldGenerateCsvEmailAndMarkCaseSent() {
        when(caseSearchService.getInMediationCases(false)).thenReturn(List.of(caseDetails));
        when(caseDetailsConverter.toCaseData(caseDetails)).thenReturn(caseData);
        when(mediationCsvServiceFactory.getMediationCSVService(caseData)).thenReturn(mediationCsvService);
        when(mediationCsvService.generateCSVContent(caseData)).thenReturn("csv-content");
        when(mediationCSVEmailConfiguration.getRecipient()).thenReturn(RECIPIENT);
        when(mediationCSVEmailConfiguration.getSender()).thenReturn(SENDER);

        MediationFileTransferResult result = task.generateCsvAndTransfer(2);

        assertThat(result.succeededCaseIds()).containsExactly(String.valueOf(CASE_ID));
        verify(sendGridClient).sendEmail(eq(SENDER), any(EmailData.class));
        verify(coreCaseDataService).triggerEvent(
            eq(CASE_ID),
            eq(CaseEvent.UPDATE_CASE_DATA),
            eq(Map.of("mediationFileSentToMmt", YesOrNo.YES)),
            any(),
            any()
        );
    }

    @Test
    void shouldGenerateJsonEmailAndMarkCaseSent() {
        when(caseSearchService.getInMediationCases(true)).thenReturn(List.of(caseDetails));
        when(caseDetailsConverter.toCaseData(caseDetails)).thenReturn(caseData);
        when(mediationJsonService.generateJsonContent(caseData)).thenReturn(new MediationCase().setCcdCaseNumber(
            CASE_ID));
        when(mediationCSVEmailConfiguration.getJsonRecipient()).thenReturn(RECIPIENT);
        when(mediationCSVEmailConfiguration.getSender()).thenReturn(SENDER);

        MediationFileTransferResult result = task.generateJsonAndTransfer(2);

        assertThat(result.succeededCaseIds()).containsExactly(String.valueOf(CASE_ID));
        verify(sendGridClient).sendEmail(eq(SENDER), any(EmailData.class));
        verify(coreCaseDataService).triggerEvent(eq(CASE_ID), eq(CaseEvent.UPDATE_CASE_DATA), any(), any(), any());
    }

    @Test
    void shouldReturnNoCasesWhenSearchFindsNothing() {
        when(caseSearchService.getInMediationCases(false)).thenReturn(List.of());

        MediationFileTransferResult result = task.generateCsvAndTransfer(2);

        assertThat(result.totalCases()).isZero();
        verifyNoInteractions(sendGridClient, coreCaseDataService);
    }

    @Test
    void shouldRecordFailedCaseWhenCaseUpdateFails() {
        when(caseSearchService.getInMediationCases(false)).thenReturn(List.of(caseDetails));
        when(caseDetailsConverter.toCaseData(caseDetails)).thenReturn(caseData);
        when(mediationCsvServiceFactory.getMediationCSVService(caseData)).thenReturn(mediationCsvService);
        when(mediationCsvService.generateCSVContent(caseData)).thenReturn("csv-content");
        when(mediationCSVEmailConfiguration.getRecipient()).thenReturn(RECIPIENT);
        when(mediationCSVEmailConfiguration.getSender()).thenReturn(SENDER);
        doThrow(new RuntimeException("update failed")).when(coreCaseDataService)
            .triggerEvent(eq(CASE_ID), eq(CaseEvent.UPDATE_CASE_DATA), any(), any(), any());

        MediationFileTransferResult result = task.generateCsvAndTransfer(1);

        assertThat(result.succeededCaseIds()).isEmpty();
        assertThat(result.failedCases()).hasSize(1);
        assertThat(result.abortedEarly()).isTrue();
    }
}
