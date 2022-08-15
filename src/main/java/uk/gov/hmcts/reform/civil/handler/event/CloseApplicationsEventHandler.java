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
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.VERIFY_AND_CLOSE_APPLICATION;

@Slf4j
@Service
@RequiredArgsConstructor
public class CloseApplicationsEventHandler {

    private final CoreCaseDataService coreCaseDataService;
    private final CaseDetailsConverter caseDetailsConverter;

    @EventListener
    public void triggerApplicationClosedEvent(CloseApplicationsEvent event) {
        CaseData caseData = caseDetailsConverter.toCaseData(coreCaseDataService.getCase(event.getCaseId()));
        if (caseData.getGeneralApplications() != null && caseData.getGeneralApplications().size() > 0) {
            caseData.getGeneralApplications()
                    .forEach(application ->
                            triggerEvent(parseLong(application.getValue().getCaseLink().getCaseReference())));
        }
    }

    private void triggerEvent(Long caseId) {
        try {
            log.info("Triggering VERIFY_AND_CLOSE_APPLICATION event to close the underlying general application: [{}]",
                    caseId);
            coreCaseDataService.triggerGeneralApplicationEvent(caseId, VERIFY_AND_CLOSE_APPLICATION);
        } catch (Exception e) {
            log.error("Could not trigger event to close application [{}]", caseId, e);
        }
    }
}
