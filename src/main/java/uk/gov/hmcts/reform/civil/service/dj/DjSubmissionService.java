package uk.gov.hmcts.reform.civil.service.dj;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.enums.CaseCategory;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.BusinessProcess;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.CaseData.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.sdo.SdoFeatureToggleService;
import uk.gov.hmcts.reform.civil.service.sdo.SdoLocationService;
import uk.gov.hmcts.reform.civil.utils.AssignCategoryId;

import static uk.gov.hmcts.reform.civil.callback.CaseEvent.STANDARD_DIRECTION_ORDER_DJ;
import static uk.gov.hmcts.reform.civil.enums.CaseCategory.SPEC_CLAIM;
import static uk.gov.hmcts.reform.civil.utils.HearingUtils.getHearingNotes;

@Service
@RequiredArgsConstructor
public class DjSubmissionService {

    private static final String CASE_MANAGEMENT_CATEGORY = "caseManagementOrders";

    private final AssignCategoryId assignCategoryId;
    private final SdoFeatureToggleService featureToggleService;
    private final SdoLocationService sdoLocationService;

    public CaseData prepareSubmission(CaseData caseData, String authToken) {
        CaseDataBuilder<?, ?> builder = caseData.toBuilder()
            .businessProcess(BusinessProcess.ready(STANDARD_DIRECTION_ORDER_DJ))
            .hearingNotes(getHearingNotes(caseData));

        removePreviewDocument(caseData, builder);
        assignDocumentCategories(caseData);
        updateEaCourtLocation(caseData, builder);
        updateWaLocations(caseData, builder, authToken);

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

    private void updateEaCourtLocation(CaseData caseData, CaseDataBuilder<?, ?> builder) {
        CaseCategory caseCategory = caseData.getCaseAccessCategory();
        if (!SPEC_CLAIM.equals(caseCategory)) {
            return;
        }

        if (featureToggleService.isWelshEnabledForMainCase()) {
            builder.eaCourtLocation(YesOrNo.YES);
            return;
        }

        boolean isLipCase = caseData.isApplicantLiP()
            || caseData.isRespondent1LiP()
            || caseData.isRespondent2LiP();

        if (!isLipCase) {
            builder.eaCourtLocation(YesOrNo.YES);
            return;
        }

        boolean shouldEnableEaCourt = isLipCaseWithProgressionEnabledAndCourtWhiteListed(caseData);
        builder.eaCourtLocation(shouldEnableEaCourt ? YesOrNo.YES : YesOrNo.NO);
    }

    private boolean isLipCaseWithProgressionEnabledAndCourtWhiteListed(CaseData caseData) {
        if (!(caseData.isLipvLipOneVOne() || caseData.isLRvLipOneVOne())) {
            return false;
        }
        return featureToggleService.isCaseProgressionEnabledAndLocationWhiteListed(
            caseData.getCaseManagementLocation().getBaseLocation()
        );
    }

    private void updateWaLocations(CaseData caseData, CaseDataBuilder<?, ?> builder, String authToken) {
        if (!featureToggleService.isMultiOrIntermediateTrackEnabled(caseData)) {
            return;
        }
        sdoLocationService.updateWaLocationsIfRequired(caseData, builder, authToken);
    }
}
