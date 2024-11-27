package uk.gov.hmcts.reform.civil.handler.callback.user.createsdocallbackhandler.submitsdo;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.enums.CaseCategory;
import uk.gov.hmcts.reform.civil.helpers.sdo.SdoHelper;
import uk.gov.hmcts.reform.civil.model.CaseData;

import static uk.gov.hmcts.reform.civil.enums.AllocatedTrack.FAST_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.AllocatedTrack.SMALL_CLAIM;

@Slf4j
@Component
public class ClaimsTrackBasedOnJudgeSelectionFieldUpdater implements SdoCaseDataFieldUpdater {

    @Override
    public void update(CaseData caseData, CaseData.CaseDataBuilder<?, ?> dataBuilder) {
        CaseCategory caseAccessCategory = caseData.getCaseAccessCategory();
        switch (caseAccessCategory) {
            case UNSPEC_CLAIM:
                if (SdoHelper.isSmallClaimsTrack(caseData)) {
                    log.debug("Setting allocated track to SMALL_CLAIM for case {}", caseData.getCcdCaseReference());
                    dataBuilder.allocatedTrack(SMALL_CLAIM);
                } else if (SdoHelper.isFastTrack(caseData)) {
                    log.debug("Setting allocated track to FAST_CLAIM for case {}", caseData.getCcdCaseReference());
                    dataBuilder.allocatedTrack(FAST_CLAIM);
                }
                break;
            case SPEC_CLAIM:
                if (SdoHelper.isSmallClaimsTrack(caseData)) {
                    log.debug("Setting response claim track to SMALL_CLAIM for case {}", caseData.getCcdCaseReference());
                    dataBuilder.responseClaimTrack(SMALL_CLAIM.name());
                } else if (SdoHelper.isFastTrack(caseData)) {
                    log.debug("Setting response claim track to FAST_CLAIM for case {}", caseData.getCcdCaseReference());
                    dataBuilder.responseClaimTrack(FAST_CLAIM.name());
                }
                break;
            default:
                break;
        }
    }
}
