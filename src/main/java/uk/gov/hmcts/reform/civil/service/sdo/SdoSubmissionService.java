package uk.gov.hmcts.reform.civil.service.sdo;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.enums.CaseCategory;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.CaseData.CaseDataBuilder;
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
        CaseDataBuilder<?, ?> builder = caseData.toBuilder()
            .businessProcess(BusinessProcess.ready(CaseEvent.CREATE_SDO))
            .hearingNotes(getHearingNotes(caseData));

        moveGeneratedDocument(caseData, builder);
        directionsOrderCaseProgressionService.applyCaseProgressionRouting(caseData, builder, authToken, false, true);
        trimMethodLocations(caseData, builder);
        updateSmallClaimsHearing(caseData, builder);
        updateClaimsTrack(caseData, builder);
        updateTrialLocations(caseData, builder);

        return builder.build();
    }

    private void moveGeneratedDocument(CaseData caseData, CaseDataBuilder<?, ?> builder) {
        CaseDocument document = caseData.getSdoOrderDocument();
        if (document == null) {
            return;
        }

        if (featureToggleService.isWelshJourneyEnabled(caseData)) {
            log.info("Moving SDO document to pre-translation for caseId {}", caseData.getCcdCaseReference());
            List<uk.gov.hmcts.reform.civil.model.common.Element<CaseDocument>> preTranslation =
                new ArrayList<>(Optional.ofNullable(caseData.getPreTranslationDocuments()).orElseGet(ArrayList::new));
            preTranslation.add(ElementUtils.element(document));
            builder.preTranslationDocuments(preTranslation);
        } else {
            log.info("Moving SDO document to system generated collection for caseId {}", caseData.getCcdCaseReference());
            List<uk.gov.hmcts.reform.civil.model.common.Element<CaseDocument>> generatedDocuments =
                new ArrayList<>(Optional.ofNullable(caseData.getSystemGeneratedCaseDocuments()).orElseGet(ArrayList::new));
            generatedDocuments.add(ElementUtils.element(document));
            builder.systemGeneratedCaseDocuments(generatedDocuments);
        }

        builder.sdoOrderDocument(null);
    }

    private void trimMethodLocations(CaseData caseData, CaseDataBuilder<?, ?> builder) {
        Optional.ofNullable(caseData.getDisposalHearingMethodInPerson())
            .map(sdoLocationService::trimListItems)
            .ifPresent(builder::disposalHearingMethodInPerson);
        Optional.ofNullable(caseData.getFastTrackMethodInPerson())
            .map(sdoLocationService::trimListItems)
            .ifPresent(builder::fastTrackMethodInPerson);
        Optional.ofNullable(caseData.getSmallClaimsMethodInPerson())
            .map(sdoLocationService::trimListItems)
            .ifPresent(builder::smallClaimsMethodInPerson);
        log.info("Trimmed hearing method locations for caseId {}", caseData.getCcdCaseReference());
    }

    private void updateSmallClaimsHearing(CaseData caseData, CaseDataBuilder<?, ?> builder) {
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

        builder.sdoR2SmallClaimsHearing(hearingBuilder.build());
    }

    private void updateClaimsTrack(CaseData caseData, CaseDataBuilder<?, ?> builder) {
        CaseCategory caseCategory = caseData.getCaseAccessCategory();
        switch (caseCategory) {
            case UNSPEC_CLAIM:
                if (caseClassificationService.isSmallClaimsTrack(caseData)) {
                    builder.allocatedTrack(SMALL_CLAIM);
                } else if (caseClassificationService.isFastTrack(caseData)) {
                    builder.allocatedTrack(FAST_CLAIM);
                }
                break;
            case SPEC_CLAIM:
                if (caseClassificationService.isSmallClaimsTrack(caseData)) {
                    builder.responseClaimTrack(SMALL_CLAIM.name());
                } else if (caseClassificationService.isFastTrack(caseData)) {
                    builder.responseClaimTrack(FAST_CLAIM.name());
                }
                break;
            default:
                break;
        }
    }

    private void updateTrialLocations(CaseData caseData, CaseDataBuilder<?, ?> builder) {
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

        builder.sdoR2Trial(trialBuilder.build());
    }

}
