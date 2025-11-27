package uk.gov.hmcts.reform.civil.service.sdo;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.enums.CaseCategory;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.BusinessProcess;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2Trial;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2SmallClaimsHearing;
import uk.gov.hmcts.reform.civil.service.directionsorder.DirectionsOrderCaseProgressionService;
import uk.gov.hmcts.reform.civil.utils.ElementUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static uk.gov.hmcts.reform.civil.enums.AllocatedTrack.FAST_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.AllocatedTrack.SMALL_CLAIM;
import static uk.gov.hmcts.reform.civil.utils.HearingUtils.getHearingNotes;

@Service
@RequiredArgsConstructor
@Slf4j
public class SdoSubmissionService {

    private final SdoFeatureToggleService featureToggleService;
    private final SdoLocationService sdoLocationService;
    private final DirectionsOrderCaseProgressionService directionsOrderCaseProgressionService;
    private final SdoCaseClassificationService caseClassificationService;

    public CaseData prepareSubmission(CaseData caseData, String authToken) {
        log.info("Preparing SDO submission payload for caseId {}", caseData.getCcdCaseReference());
        caseData.setBusinessProcess(BusinessProcess.ready(CaseEvent.CREATE_SDO));
        caseData.setHearingNotes(getHearingNotes(caseData));

        moveGeneratedDocument(caseData);
        updateClaimsTrack(caseData);
        directionsOrderCaseProgressionService.applyCaseProgressionRouting(caseData, authToken, false, true);
        trimMethodLocations(caseData);
        updateSmallClaimsHearing(caseData);
        updateTrialLocations(caseData);

        return caseData;
    }

    private void moveGeneratedDocument(CaseData caseData) {
        CaseDocument document = caseData.getSdoOrderDocument();
        if (document == null) {
            return;
        }

        if (featureToggleService.isWelshJourneyEnabled(caseData)) {
            log.info("Moving SDO document to pre-translation for caseId {}", caseData.getCcdCaseReference());
            List<uk.gov.hmcts.reform.civil.model.common.Element<CaseDocument>> preTranslation =
                new ArrayList<>(Optional.ofNullable(caseData.getPreTranslationDocuments()).orElseGet(ArrayList::new));
            preTranslation.add(ElementUtils.element(document));
            caseData.setPreTranslationDocuments(preTranslation);
        } else {
            log.info("Moving SDO document to system generated collection for caseId {}", caseData.getCcdCaseReference());
            List<uk.gov.hmcts.reform.civil.model.common.Element<CaseDocument>> generatedDocuments =
                new ArrayList<>(Optional.ofNullable(caseData.getSystemGeneratedCaseDocuments()).orElseGet(ArrayList::new));
            generatedDocuments.add(ElementUtils.element(document));
            caseData.setSystemGeneratedCaseDocuments(generatedDocuments);
        }

        caseData.setSdoOrderDocument(null);
    }

    private void trimMethodLocations(CaseData caseData) {
        Optional.ofNullable(caseData.getDisposalHearingMethodInPerson())
            .map(sdoLocationService::trimListItems)
            .ifPresent(caseData::setDisposalHearingMethodInPerson);
        Optional.ofNullable(caseData.getFastTrackMethodInPerson())
            .map(sdoLocationService::trimListItems)
            .ifPresent(caseData::setFastTrackMethodInPerson);
        Optional.ofNullable(caseData.getSmallClaimsMethodInPerson())
            .map(sdoLocationService::trimListItems)
            .ifPresent(caseData::setSmallClaimsMethodInPerson);
        log.info("Trimmed hearing method locations for caseId {}", caseData.getCcdCaseReference());
    }

    private void updateSmallClaimsHearing(CaseData caseData) {
        if (!caseClassificationService.isDrhSmallClaim(caseData) || caseData.getSdoR2SmallClaimsHearing() == null) {
            return;
        }

        SdoR2SmallClaimsHearing hearing = caseData.getSdoR2SmallClaimsHearing();
        SdoR2SmallClaimsHearing.SdoR2SmallClaimsHearingBuilder hearingBuilder = hearing.toBuilder();

        Optional.ofNullable(hearing.getHearingCourtLocationList())
            .map(sdoLocationService::trimListItems)
            .ifPresent(hearingBuilder::hearingCourtLocationList);
        Optional.ofNullable(hearing.getAltHearingCourtLocationList())
            .map(sdoLocationService::trimListItems)
            .ifPresent(hearingBuilder::altHearingCourtLocationList);

        caseData.setSdoR2SmallClaimsHearing(hearingBuilder.build());
    }

    private void updateClaimsTrack(CaseData caseData) {
        CaseCategory caseCategory = caseData.getCaseAccessCategory();
        switch (caseCategory) {
            case UNSPEC_CLAIM:
                if (caseClassificationService.isSmallClaimsTrack(caseData)) {
                    caseData.setAllocatedTrack(SMALL_CLAIM);
                } else if (caseClassificationService.isFastTrack(caseData)) {
                    caseData.setAllocatedTrack(FAST_CLAIM);
                }
                break;
            case SPEC_CLAIM:
                if (caseClassificationService.isSmallClaimsTrack(caseData)) {
                    caseData.setResponseClaimTrack(SMALL_CLAIM.name());
                } else if (caseClassificationService.isFastTrack(caseData)) {
                    caseData.setResponseClaimTrack(FAST_CLAIM.name());
                }
                break;
            default:
                break;
        }
    }

    private void updateTrialLocations(CaseData caseData) {
        if (caseData.getSdoR2Trial() == null) {
            return;
        }

        SdoR2Trial trial = caseData.getSdoR2Trial();
        SdoR2Trial.SdoR2TrialBuilder trialBuilder = trial.toBuilder();

        Optional.ofNullable(trial.getHearingCourtLocationList())
            .map(sdoLocationService::trimListItems)
            .ifPresent(trialBuilder::hearingCourtLocationList);
        Optional.ofNullable(trial.getAltHearingCourtLocationList())
            .map(sdoLocationService::trimListItems)
            .ifPresent(trialBuilder::altHearingCourtLocationList);

        caseData.setSdoR2Trial(trialBuilder.build());
    }

}
