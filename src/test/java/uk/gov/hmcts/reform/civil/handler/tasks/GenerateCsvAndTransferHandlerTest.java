package uk.gov.hmcts.reform.civil.handler.tasks;

import org.camunda.bpm.client.task.ExternalTask;
import org.camunda.bpm.client.task.ExternalTaskService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import uk.gov.hmcts.reform.civil.config.properties.mediation.MediationCSVEmailConfiguration;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.sendgrid.SendGridClient;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.mediation.MediationCSVLipVLipService;
import uk.gov.hmcts.reform.civil.service.mediation.MediationCSVLrvLipService;
import uk.gov.hmcts.reform.civil.service.mediation.MediationCsvServiceFactory;
import uk.gov.hmcts.reform.civil.service.search.MediationCasesSearchService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.enums.CaseState.IN_MEDIATION;

@SpringBootTest(classes = {
    GenerateCsvAndTransferTaskHandler.class,
    JacksonAutoConfiguration.class,
    CaseDetailsConverter.class
})
class GenerateCsvAndTransferHandlerTest {

    @MockBean
    private ExternalTask externalTask;

    @MockBean
    private ExternalTaskService externalTaskService;

    @MockBean
    private MediationCasesSearchService searchService;

    @MockBean
    private CaseDetailsConverter caseDetailsConverter;

    @Autowired
    private GenerateCsvAndTransferTaskHandler inMediationCsvHandler;
    @MockBean
    private  MediationCsvServiceFactory mediationCsvServiceFactory;
    @MockBean
    private  SendGridClient sendGridClient;
    @MockBean
    private  MediationCSVEmailConfiguration mediationCSVEmailConfiguration;
    @MockBean
    private MediationCSVLrvLipService mediationCSVLrvLipService;
    @MockBean
    private MediationCSVLipVLipService mediationCSVLipvLipService;
    @MockBean
    private FeatureToggleService toggleService;

    private CaseDetails caseDetailsWithInMediationState;
    private CaseDetails caseDetailsWithInMediationStateNotToProcess;
    private CaseData caseDataInMediation;
    private CaseData caseDataInMediationNotToProcess;
    private static final String SENDER = "sender@gmail.com";
    private static final String RECIPIENT = "recipient@gmail.com";
    private final LocalDate claimToBeProcessed = LocalDate.now().minusDays(1);
    private final LocalDate claimNotToBeProcessed = LocalDate.now();

    @BeforeEach
    void init() {
        caseDetailsWithInMediationState = getCaseDetails(1L, claimToBeProcessed);
        caseDetailsWithInMediationStateNotToProcess = getCaseDetails(2L, claimNotToBeProcessed);
        caseDataInMediation = getCaseData(1L, claimToBeProcessed);
        caseDataInMediationNotToProcess = getCaseData(2L, claimNotToBeProcessed);
        when(mediationCsvServiceFactory.getMediationCSVService(any())).thenReturn(mediationCSVLrvLipService);
        when(mediationCSVEmailConfiguration.getRecipient()).thenReturn(SENDER);
        when(mediationCSVEmailConfiguration.getSender()).thenReturn(RECIPIENT);
        when(toggleService.isLipVLipEnabled()).thenReturn(false);
    }

    @Test
    void shouldGenerateCsvAndSendEmailSuccessfully() {
        when(searchService.getInMediationCases(claimToBeProcessed, false)).thenReturn(List.of(caseDetailsWithInMediationState));
        when(caseDetailsConverter.toCaseData(caseDetailsWithInMediationState)).thenReturn(caseDataInMediation);
        when(caseDetailsConverter.toCaseData(caseDetailsWithInMediationStateNotToProcess)).thenReturn(caseDataInMediationNotToProcess);

        inMediationCsvHandler.execute(externalTask, externalTaskService);
        verify(searchService).getInMediationCases(claimToBeProcessed, false);
        verify(sendGridClient).sendEmail(anyString(), any());
        verify(sendGridClient, times(1)).sendEmail(anyString(), any());
        verify(externalTaskService).complete(externalTask);
    }

    @Test
    void shouldNotGenerateCsvAndSendEmail() {
        List<CaseDetails> cases = new ArrayList<>();
        String date = (claimNotToBeProcessed.format(DateTimeFormatter.ofPattern("dd-MM-yyyy", Locale.UK))).toString();
        when(externalTask.getVariable(any())).thenReturn(date);
        when(searchService.getInMediationCases(any(), any())).thenReturn(cases);
        when(caseDetailsConverter.toCaseData(caseDetailsWithInMediationStateNotToProcess)).thenReturn(caseDataInMediationNotToProcess);

        inMediationCsvHandler.execute(externalTask, externalTaskService);
        verify(searchService).getInMediationCases(claimNotToBeProcessed, false);
        verify(mediationCsvServiceFactory, times(0)).getMediationCSVService(any());
        verify(sendGridClient, times(0)).sendEmail(anyString(), any());
        verify(externalTaskService).complete(externalTask);
    }

    @Test
    void should_handle_task_from_external_variable() {

        String date = (claimNotToBeProcessed.format(DateTimeFormatter.ofPattern("dd-MM-yyyy", Locale.UK))).toString();
        when(externalTask.getVariable(any())).thenReturn(date);
        when(searchService.getInMediationCases(any(), any())).thenReturn(List.of(caseDetailsWithInMediationState, caseDetailsWithInMediationStateNotToProcess));
        when(caseDetailsConverter.toCaseData(caseDetailsWithInMediationState)).thenReturn(caseDataInMediation);
        when(caseDetailsConverter.toCaseData(caseDetailsWithInMediationStateNotToProcess)).thenReturn(caseDataInMediationNotToProcess);

        inMediationCsvHandler.execute(externalTask, externalTaskService);
        verify(searchService).getInMediationCases(claimNotToBeProcessed, false);
        verify(sendGridClient).sendEmail(anyString(), any());
        verify(sendGridClient, times(1)).sendEmail(anyString(), any());
        verify(externalTaskService).complete(externalTask);
    }

    @Test
    void shouldGenerateCsvAndSendEmailSuccessfully_R2LipVLipFlagEnabled() {
        when(toggleService.isLipVLipEnabled()).thenReturn(true);
        when(mediationCsvServiceFactory.getMediationCSVService(any())).thenReturn(mediationCSVLipvLipService);
        when(searchService.getInMediationCases(claimToBeProcessed, false)).thenReturn(List.of(caseDetailsWithInMediationState));
        when(caseDetailsConverter.toCaseData(caseDetailsWithInMediationState)).thenReturn(caseDataInMediation);
        when(caseDetailsConverter.toCaseData(caseDetailsWithInMediationStateNotToProcess)).thenReturn(caseDataInMediationNotToProcess);

        inMediationCsvHandler.execute(externalTask, externalTaskService);
        verify(searchService).getInMediationCases(claimToBeProcessed, false);
        verify(sendGridClient, times(1)).sendEmail(anyString(), any());
        verify(externalTaskService).complete(externalTask);
    }

    private CaseDetails getCaseDetails(Long ccdId, LocalDate claimMovedToMediation) {

        return CaseDetails.builder().id(ccdId).data(
            Map.of("claimMovedToMediationOn", claimMovedToMediation)).build();
    }

    private CaseData getCaseData(Long ccdId, LocalDate claimMovedDate) {
        return CaseData.builder()
            .ccdCaseReference(ccdId)
            .legacyCaseReference("11111111111111")
            .ccdState(IN_MEDIATION)
            .claimMovedToMediationOn(claimMovedDate)
            .totalClaimAmount(new BigDecimal(9000))
            .applicant1(Party.builder()
                            .type(Party.Type.COMPANY)
                            .companyName("Applicant company name")
                            .partyName("Applicant party name")
                            .partyPhone("0011001100")
                            .partyEmail("applicant@company.com")
                            .build())
            .respondent1(Party.builder()
                             .type(Party.Type.COMPANY)
                             .companyName("Respondent company name")
                             .partyName("Respondent party name")
                             .partyPhone("0022002200")
                             .partyEmail("respondent@company.com").build())
            .build();
    }
}

