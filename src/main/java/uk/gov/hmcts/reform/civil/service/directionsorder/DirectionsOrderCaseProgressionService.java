package uk.gov.hmcts.reform.civil.service.directionsorder;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.sdo.SdoFeatureToggleService;
import uk.gov.hmcts.reform.civil.service.sdo.SdoJourneyToggleService;
import uk.gov.hmcts.reform.civil.service.sdo.SdoLocationService;

/**
 * Shared helper used by both SDO and DJ submission flows to keep EA-court toggles
 * and WA location updates consistent while handler-level services remain slim.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DirectionsOrderCaseProgressionService {

    private final SdoJourneyToggleService sdoJourneyToggleService;
    private final SdoFeatureToggleService featureToggleService;
    private final SdoLocationService sdoLocationService;

    public void applyEaCourtLocation(CaseData caseData, boolean allowLipvLrWithNoC) {
        YesOrNo resolvedEaCourt = sdoJourneyToggleService.resolveEaCourtLocation(caseData, allowLipvLrWithNoC);
        if (resolvedEaCourt != null) {
            log.info("Setting EA court location={} for caseId {}", resolvedEaCourt, caseData.getCcdCaseReference());
            caseData.setEaCourtLocation(resolvedEaCourt);
        }
    }

    public void applyCaseProgressionRouting(CaseData caseData,
                                            String authToken,
                                            boolean allowLipvLrWithNoC) {
        applyCaseProgressionRouting(caseData, authToken, false, allowLipvLrWithNoC);
    }

    public void applyCaseProgressionRouting(CaseData caseData,
                                            String authToken,
                                            boolean clearWaMetadataWhenDisabled,
                                            boolean allowLipvLrWithNoC) {
        applyEaCourtLocation(caseData, allowLipvLrWithNoC);
        updateWaLocationsIfEnabled(caseData, authToken, clearWaMetadataWhenDisabled);
    }

    public void updateWaLocationsIfEnabled(CaseData caseData,
                                           String authToken) {
        updateWaLocationsIfEnabled(caseData, authToken, false);
    }

    public void updateWaLocationsIfEnabled(CaseData caseData,
                                           String authToken,
                                           boolean clearWaMetadataWhenDisabled) {
        if (!featureToggleService.isMultiOrIntermediateTrackEnabled(caseData)) {
            if (clearWaMetadataWhenDisabled) {
                log.info("Clearing WA location metadata for caseId {}", caseData.getCcdCaseReference());
                sdoLocationService.clearWaLocationMetadata(caseData);
            }
            return;
        }
        log.info("Updating WA locations for caseId {}", caseData.getCcdCaseReference());
        sdoLocationService.updateWaLocationsIfRequired(caseData, authToken);
    }
}
