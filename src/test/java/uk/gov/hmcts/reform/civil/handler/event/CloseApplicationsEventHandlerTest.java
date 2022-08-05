package uk.gov.hmcts.reform.civil.handler.event;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.event.CloseApplicationsEvent;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.genapplication.CaseLink;
import uk.gov.hmcts.reform.civil.model.genapplication.GeneralApplication;
import uk.gov.hmcts.reform.civil.sampledata.CaseDetailsBuilder;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.MAIN_CASE_CLOSED;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.wrapElements;

@ExtendWith(SpringExtension.class)
class CloseApplicationsEventHandlerTest {

    @Mock
    private CoreCaseDataService coreCaseDataService;

    @Mock
    private CaseDetailsConverter caseDetailsConverter;

    @InjectMocks
    private CloseApplicationsEventHandler handler;

    @Test
    void shouldNotTriggerCloseApplicationEventWhenCaseDoesNotHaveAnyGeneralApplication() {
        CloseApplicationsEvent event = new CloseApplicationsEvent(1L);
        when(coreCaseDataService.getCase(event.getCaseId()))
                .thenReturn(CaseDetailsBuilder.builder().data(getCaseWithNoApplications()).build());
        when(caseDetailsConverter.toCaseData(any(CaseDetails.class))).thenReturn(getCaseWithNoApplications());
        handler.triggerApplicationClosedEvent(event);

        verify(coreCaseDataService, times(0)).triggerEvent(anyLong(), any(CaseEvent.class));
    }

    @Test
    void shouldTriggerCloseApplicationEventForEveryGeneralApplicationUnderClaim() {
        CloseApplicationsEvent event = new CloseApplicationsEvent(1L);
        when(coreCaseDataService.getCase(event.getCaseId()))
                .thenReturn(CaseDetailsBuilder.builder().data(getCaseWithTwoGeneralApplications(1234L, 5678L)).build());
        when(caseDetailsConverter.toCaseData(any(CaseDetails.class)))
                .thenReturn(getCaseWithTwoGeneralApplications(1234L, 5678L));
        handler.triggerApplicationClosedEvent(event);

        verify(coreCaseDataService, times(1)).triggerEvent(1234L, MAIN_CASE_CLOSED);
        verify(coreCaseDataService, times(1)).triggerEvent(5678L, MAIN_CASE_CLOSED);
    }

    private CaseData getCaseWithNoApplications() {
        return CaseData.builder().build();
    }

    private CaseData getCaseWithTwoGeneralApplications(Long id1, Long id2) {
        return CaseData.builder()
                .generalApplications(wrapElements(getGeneralApplication(id1), getGeneralApplication(id2)))
                .build();
    }

    private GeneralApplication getGeneralApplication(Long id) {
        return GeneralApplication.builder().caseLink(CaseLink.builder().caseReference(id.toString()).build()).build();
    }

}
