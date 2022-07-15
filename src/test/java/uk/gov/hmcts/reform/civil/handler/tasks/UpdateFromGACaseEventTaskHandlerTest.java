package uk.gov.hmcts.reform.civil.handler.tasks;

import org.camunda.bpm.client.task.ExternalTask;
import org.camunda.bpm.client.task.ExternalTaskService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.civil.enums.BusinessProcessStatus;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.BusinessProcess;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDetailsBuilder;
import uk.gov.hmcts.reform.civil.sampledata.GeneralApplicationDetailsBuilder;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;
import uk.gov.hmcts.reform.civil.utils.CaseDataContentConverter;

import java.util.Map;

import static java.lang.Long.parseLong;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.ADD_PDF_TO_MAIN_CASE;

@SpringBootTest(classes = {
    UpdateFromGACaseEventTaskHandler.class,
    JacksonAutoConfiguration.class,
    CaseDetailsConverter.class,
    CaseDataContentConverter.class,
    CoreCaseDataService.class
})
@ExtendWith(SpringExtension.class)
public class UpdateFromGACaseEventTaskHandlerTest {

    private static final String CIVIL_CASE_ID = "1594901956117591";
    private static final String GENERAL_APP_CASE_ID = "1234";

    @Mock
    private ExternalTask mockExternalTask;

    @Mock
    private ExternalTaskService externalTaskService;

    @MockBean
    private CoreCaseDataService coreCaseDataService;

    @MockBean
    private CaseDetailsConverter caseDetailsConverter;

    @Autowired
    private UpdateFromGACaseEventTaskHandler handler;

    @BeforeEach
    void init() {
        when(mockExternalTask.getTopicName()).thenReturn("test");
        when(mockExternalTask.getWorkerId()).thenReturn("worker");
        when(mockExternalTask.getActivityId()).thenReturn("activityId");

        when(mockExternalTask.getAllVariables())
            .thenReturn(Map.of(
                "caseId", GENERAL_APP_CASE_ID,
                "caseEvent", ADD_PDF_TO_MAIN_CASE,
                "generalAppParentCaseLink", CIVIL_CASE_ID
            ));
    }

    @Test
    void testShouldAddGeneralOrderDocument() {
        CaseData caseData = new CaseDataBuilder().atStateClaimDraft()
            .businessProcess(BusinessProcess.builder().status(BusinessProcessStatus.READY).build())
            .build();

        CaseDetails caseDetails = CaseDetailsBuilder.builder().data(caseData).build();
        StartEventResponse startEventResponse = startEventResponse(caseDetails);

        CaseData generalCaseData = GeneralApplicationDetailsBuilder.builder()
            .getTestCaseDataWithGeneralOrderPDFDocument(CaseData.builder().build());

        when(caseDetailsConverter.toGACaseData(coreCaseDataService.getCase(parseLong(GENERAL_APP_CASE_ID))))
            .thenReturn(generalCaseData);

        when(coreCaseDataService.startUpdate(CIVIL_CASE_ID, ADD_PDF_TO_MAIN_CASE)).thenReturn(startEventResponse);

        when(caseDetailsConverter.toCaseData(startEventResponse.getCaseDetails())).thenReturn(caseData);

        when(coreCaseDataService.submitUpdate(eq(CIVIL_CASE_ID), any(CaseDataContent.class))).thenReturn(caseData);

        handler.execute(mockExternalTask, externalTaskService);

        verify(coreCaseDataService).startUpdate(CIVIL_CASE_ID, ADD_PDF_TO_MAIN_CASE);
        verify(coreCaseDataService).submitUpdate(eq(CIVIL_CASE_ID), any(CaseDataContent.class));
        verify(externalTaskService).complete(mockExternalTask);
    }

    @Test
    void testShouldAddDismissalOrderDocument() {
        CaseData caseData = new CaseDataBuilder().atStateClaimDraft()
            .businessProcess(BusinessProcess.builder().status(BusinessProcessStatus.READY).build())
            .build();

        CaseDetails caseDetails = CaseDetailsBuilder.builder().data(caseData).build();
        StartEventResponse startEventResponse = startEventResponse(caseDetails);

        CaseData generalCaseData = GeneralApplicationDetailsBuilder.builder()
            .getTestCaseDataWithDismissalOrderPDFDocument(CaseData.builder().build());

        when(caseDetailsConverter.toGACaseData(coreCaseDataService.getCase(parseLong(GENERAL_APP_CASE_ID))))
            .thenReturn(generalCaseData);

        when(coreCaseDataService.startUpdate(CIVIL_CASE_ID, ADD_PDF_TO_MAIN_CASE)).thenReturn(startEventResponse);

        when(caseDetailsConverter.toCaseData(startEventResponse.getCaseDetails())).thenReturn(caseData);

        when(coreCaseDataService.submitUpdate(eq(CIVIL_CASE_ID), any(CaseDataContent.class))).thenReturn(caseData);

        handler.execute(mockExternalTask, externalTaskService);

        verify(coreCaseDataService).startUpdate(CIVIL_CASE_ID, ADD_PDF_TO_MAIN_CASE);
        verify(coreCaseDataService).submitUpdate(eq(CIVIL_CASE_ID), any(CaseDataContent.class));
        verify(externalTaskService).complete(mockExternalTask);
    }

    @Test
    void testShouldAddDirectionOrderDocument() {
        CaseData caseData = new CaseDataBuilder().atStateClaimDraft()
            .businessProcess(BusinessProcess.builder().status(BusinessProcessStatus.READY).build())
            .build();

        CaseDetails caseDetails = CaseDetailsBuilder.builder().data(caseData).build();
        StartEventResponse startEventResponse = startEventResponse(caseDetails);

        CaseData generalCaseData = GeneralApplicationDetailsBuilder.builder()
            .getTestCaseDataWithDirectionOrderPDFDocument(CaseData.builder().build());

        when(caseDetailsConverter.toGACaseData(coreCaseDataService.getCase(parseLong(GENERAL_APP_CASE_ID))))
            .thenReturn(generalCaseData);

        when(coreCaseDataService.startUpdate(CIVIL_CASE_ID, ADD_PDF_TO_MAIN_CASE)).thenReturn(startEventResponse);

        when(caseDetailsConverter.toCaseData(startEventResponse.getCaseDetails())).thenReturn(caseData);

        when(coreCaseDataService.submitUpdate(eq(CIVIL_CASE_ID), any(CaseDataContent.class))).thenReturn(caseData);

        handler.execute(mockExternalTask, externalTaskService);

        verify(coreCaseDataService).startUpdate(CIVIL_CASE_ID, ADD_PDF_TO_MAIN_CASE);
        verify(coreCaseDataService).submitUpdate(eq(CIVIL_CASE_ID), any(CaseDataContent.class));
        verify(externalTaskService).complete(mockExternalTask);
    }

    private StartEventResponse startEventResponse(CaseDetails caseDetails) {
        return StartEventResponse.builder()
            .token("1594901956117591")
            .eventId(ADD_PDF_TO_MAIN_CASE.name())
            .caseDetails(caseDetails)
            .build();
    }
}
