package uk.gov.hmcts.reform.civil.handler.callback.user.createsdocallbackhandler;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.enums.CaseCategory;
import uk.gov.hmcts.reform.civil.handler.callback.user.task.CaseTask;
import uk.gov.hmcts.reform.civil.helpers.sdo.SdoHelper;
import uk.gov.hmcts.reform.civil.model.BusinessProcess;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2SmallClaimsHearing;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2Trial;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;

import java.util.List;

import static java.util.Objects.isNull;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CREATE_SDO;
import static uk.gov.hmcts.reform.civil.enums.AllocatedTrack.FAST_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.AllocatedTrack.SMALL_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.civil.utils.HearingUtils.getHearingNotes;

@Component
@AllArgsConstructor
@Slf4j
public class SubmitSDO implements CaseTask {

    private final FeatureToggleService featureToggleService;
    private final ObjectMapper objectMapper;

    public CallbackResponse execute(CallbackParams callbackParams) {
        log.info("Executing SubmitSDO callback for case {}", callbackParams.getCaseData().getCcdCaseReference());
        CaseData.CaseDataBuilder<?, ?> dataBuilder = getSharedData(callbackParams);
        CaseData caseData = callbackParams.getCaseData();

        handleGeneratedDocuments(caseData, dataBuilder);
        dataBuilder.hearingNotes(getHearingNotes(caseData));
        handleCourtLocation(caseData, dataBuilder);
        handleHearingMethods(caseData, dataBuilder);
        handleSdoR2SmallClaimsHearing(caseData, dataBuilder);
        setClaimsTrackBasedOnJudgeSelection(dataBuilder, caseData);
        handleSdoR2Trial(caseData, dataBuilder);

        log.info("SubmitSDO callback executed successfully for case {}", caseData.getCcdCaseReference());
        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(dataBuilder.build().toMap(objectMapper))
            .build();
    }

    private void handleGeneratedDocuments(CaseData caseData, CaseData.CaseDataBuilder<?, ?> dataBuilder) {
        CaseDocument document = caseData.getSdoOrderDocument();
        if (document != null) {
            log.debug("Adding generated document to systemGeneratedCaseDocuments for case {}", caseData.getCcdCaseReference());
            List<Element<CaseDocument>> generatedDocuments = caseData.getSystemGeneratedCaseDocuments();
            generatedDocuments.add(element(document));
            dataBuilder.systemGeneratedCaseDocuments(generatedDocuments);
        }
        dataBuilder.sdoOrderDocument(null);
    }

    private void handleCourtLocation(CaseData caseData, CaseData.CaseDataBuilder<?, ?> dataBuilder) {
        if (!sdoSubmittedPreCPForLiPCase(caseData)
            && featureToggleService.isPartOfNationalRollout(caseData.getCaseManagementLocation().getBaseLocation())) {
            log.info("Case {} is whitelisted for case progression.", caseData.getCcdCaseReference());
            dataBuilder.eaCourtLocation(YES);

            if (featureToggleService.isHmcEnabled()
                && !caseData.isApplicantLiP()
                && !caseData.isRespondent1LiP()
                && !caseData.isRespondent2LiP()) {
                dataBuilder.hmcEaCourtLocation(featureToggleService.isLocationWhiteListedForCaseProgression(
                    caseData.getCaseManagementLocation().getBaseLocation()) ? YES : NO);
            }
        } else {
            log.info("Case {} is NOT whitelisted for case progression.", caseData.getCcdCaseReference());
            dataBuilder.eaCourtLocation(NO);
        }
    }

    private void handleHearingMethods(CaseData caseData, CaseData.CaseDataBuilder<?, ?> dataBuilder) {
        log.debug("Handling hearing methods for case {}", caseData.getCcdCaseReference());
        dataBuilder.disposalHearingMethodInPerson(deleteLocationList(caseData.getDisposalHearingMethodInPerson()));
        dataBuilder.fastTrackMethodInPerson(deleteLocationList(caseData.getFastTrackMethodInPerson()));
        dataBuilder.smallClaimsMethodInPerson(deleteLocationList(caseData.getSmallClaimsMethodInPerson()));
    }

    private void handleSdoR2SmallClaimsHearing(CaseData caseData, CaseData.CaseDataBuilder<?, ?> dataBuilder) {
        if (featureToggleService.isSdoR2Enabled() && SdoHelper.isSDOR2ScreenForDRHSmallClaim(caseData)
            && caseData.getSdoR2SmallClaimsHearing() != null) {
            log.debug("Handling SDO R2 Small Claims Hearing for case {}", caseData.getCcdCaseReference());
            dataBuilder.sdoR2SmallClaimsHearing(updateHearingAfterDeletingLocationList(caseData.getSdoR2SmallClaimsHearing()));
        }
    }

    private void handleSdoR2Trial(CaseData caseData, CaseData.CaseDataBuilder<?, ?> dataBuilder) {
        if (featureToggleService.isSdoR2Enabled() && caseData.getSdoR2Trial() != null) {
            log.debug("Handling SDO R2 Trial for case {}", caseData.getCcdCaseReference());
            SdoR2Trial sdoR2Trial = caseData.getSdoR2Trial();
            if (caseData.getSdoR2Trial().getHearingCourtLocationList() != null) {
                sdoR2Trial.setHearingCourtLocationList(DynamicList.builder().value(
                    caseData.getSdoR2Trial().getHearingCourtLocationList().getValue()).build());
            }
            if (caseData.getSdoR2Trial().getAltHearingCourtLocationList() != null) {
                sdoR2Trial.setAltHearingCourtLocationList(DynamicList.builder().value(
                    caseData.getSdoR2Trial().getAltHearingCourtLocationList().getValue()).build());
            }
            dataBuilder.sdoR2Trial(sdoR2Trial);
        }
    }

    private CaseData.CaseDataBuilder<?, ?> getSharedData(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        CaseData.CaseDataBuilder<?, ?> dataBuilder = caseData.toBuilder();

        dataBuilder.businessProcess(BusinessProcess.ready(CREATE_SDO));
        log.debug("Shared data prepared for case {}", caseData.getCcdCaseReference());

        return dataBuilder;
    }

    private boolean sdoSubmittedPreCPForLiPCase(CaseData caseData) {
        return !featureToggleService.isCaseProgressionEnabled()
            && (caseData.isRespondent1LiP() || caseData.isRespondent2LiP() || caseData.isApplicantNotRepresented());
    }

    private DynamicList deleteLocationList(DynamicList list) {
        if (isNull(list)) {
            return null;
        }
        return DynamicList.builder().value(list.getValue()).build();
    }

    private SdoR2SmallClaimsHearing updateHearingAfterDeletingLocationList(SdoR2SmallClaimsHearing sdoR2SmallClaimsHearing) {
        if (sdoR2SmallClaimsHearing.getHearingCourtLocationList() != null) {
            sdoR2SmallClaimsHearing.setHearingCourtLocationList(deleteLocationList(sdoR2SmallClaimsHearing.getHearingCourtLocationList()));
        }
        if (sdoR2SmallClaimsHearing.getAltHearingCourtLocationList() != null) {
            sdoR2SmallClaimsHearing.setAltHearingCourtLocationList(deleteLocationList(sdoR2SmallClaimsHearing.getAltHearingCourtLocationList()));
        }
        return sdoR2SmallClaimsHearing;
    }

    private void setClaimsTrackBasedOnJudgeSelection(CaseData.CaseDataBuilder<?, ?> dataBuilder, CaseData caseData) {
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
