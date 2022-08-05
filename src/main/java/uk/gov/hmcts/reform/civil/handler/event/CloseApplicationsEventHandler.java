package uk.gov.hmcts.reform.civil.handler.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.event.CloseApplicationsEvent;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;

import static java.lang.Long.parseLong;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.MAIN_CASE_CLOSED;

@Slf4j
@Service
@RequiredArgsConstructor
public class CloseApplicationsEventHandler {

    private final CoreCaseDataService coreCaseDataService;
    private final CaseDetailsConverter caseDetailsConverter;


    @EventListener
    public void triggerApplicationClosedEvent(CloseApplicationsEvent event) {
        log.info("Inside CloseApplicationsEventHandler");
        CaseData caseData = caseDetailsConverter.toCaseData(coreCaseDataService.getCase(event.getCaseId()));
        if (caseData.getGeneralApplications() != null) {
            caseData.getGeneralApplications()
                    .forEach(application ->
                            triggerEvent(parseLong(application.getValue().getCaseLink().getCaseReference())));
        }
    }

    private void triggerEvent(Long caseId) {
        try {
            coreCaseDataService.triggerEvent(caseId, MAIN_CASE_CLOSED);
        } catch (Exception e) {
            log.error("Could not trigger event to close application [{}]", caseId);
        }
    }
}
