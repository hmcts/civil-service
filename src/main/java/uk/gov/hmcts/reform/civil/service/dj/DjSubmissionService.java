package uk.gov.hmcts.reform.civil.service.dj;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.model.BusinessProcess;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.CaseData.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.directionsorder.DirectionsOrderCaseProgressionService;
import uk.gov.hmcts.reform.civil.utils.AssignCategoryId;

import static uk.gov.hmcts.reform.civil.callback.CaseEvent.STANDARD_DIRECTION_ORDER_DJ;
import static uk.gov.hmcts.reform.civil.utils.HearingUtils.getHearingNotes;

@Service
@RequiredArgsConstructor
public class DjSubmissionService {

    private static final String CASE_MANAGEMENT_CATEGORY = "caseManagementOrders";

    private final AssignCategoryId assignCategoryId;
    private final DirectionsOrderCaseProgressionService directionsOrderCaseProgressionService;

    public CaseData prepareSubmission(CaseData caseData, String authToken) {
        CaseDataBuilder<?, ?> builder = caseData.toBuilder()
            .businessProcess(BusinessProcess.ready(STANDARD_DIRECTION_ORDER_DJ))
            .hearingNotes(getHearingNotes(caseData));

        removePreviewDocument(caseData, builder);
        assignDocumentCategories(caseData);
        directionsOrderCaseProgressionService.applyEaCourtLocation(caseData, builder);
        directionsOrderCaseProgressionService.updateWaLocationsIfEnabled(caseData, builder, authToken);

        return builder.build();
    }

    private void removePreviewDocument(CaseData caseData, CaseDataBuilder<?, ?> builder) {
        if (caseData.getOrderSDODocumentDJ() != null) {
            builder.orderSDODocumentDJ(null);
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
