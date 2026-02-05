package uk.gov.hmcts.reform.civil.service.dj;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.model.BusinessProcess;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.directionsorder.DirectionsOrderCaseProgressionService;
import uk.gov.hmcts.reform.civil.utils.AssignCategoryId;

import static uk.gov.hmcts.reform.civil.callback.CaseEvent.STANDARD_DIRECTION_ORDER_DJ;
import static uk.gov.hmcts.reform.civil.utils.HearingUtils.getHearingNotes;

@Service
@RequiredArgsConstructor
@Slf4j
public class DjSubmissionService {

    private static final String CASE_MANAGEMENT_CATEGORY = "caseManagementOrders";

    private final AssignCategoryId assignCategoryId;
    private final DirectionsOrderCaseProgressionService directionsOrderCaseProgressionService;

    public CaseData prepareSubmission(CaseData caseData, String authToken) {
        log.info("Preparing DJ submission payload for caseId {}", caseData.getCcdCaseReference());
        caseData.setBusinessProcess(BusinessProcess.ready(STANDARD_DIRECTION_ORDER_DJ));
        caseData.setHearingNotes(getHearingNotes(caseData));

        removePreviewDocument(caseData);
        assignDocumentCategories(caseData);
        directionsOrderCaseProgressionService.applyCaseProgressionRouting(
            caseData,
            authToken,
            false,
            false
        );

        return caseData;
    }

    private void removePreviewDocument(CaseData caseData) {
        if (caseData.getOrderSDODocumentDJ() != null) {
            log.info("Removing DJ preview document before submission for caseId {}", caseData.getCcdCaseReference());
            caseData.setOrderSDODocumentDJ(null);
        }
    }

    private void assignDocumentCategories(CaseData caseData) {
        assignCategoryId.assignCategoryIdToCollection(
            caseData.getOrderSDODocumentDJCollection(),
            element -> element.getValue().getDocumentLink(),
            CASE_MANAGEMENT_CATEGORY
        );
    }

}
