package uk.gov.hmcts.reform.civil.service.sdo;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.sdo.FastTrackPersonalInjury;

import static uk.gov.hmcts.reform.civil.service.directionsorder.DirectionsOrderSpecialistTextLibrary.PERSONAL_INJURY_PERMISSION_SDO;

/**
 * Builds the default expert-evidence paragraph used when pre-populating the fast-track
 * section of the SDO.  Keeping the strings and deadline wiring here prevents
 * {@link SdoTrackDefaultsService} from rebuilding the same structure inline.
 */
@Service
@RequiredArgsConstructor
public class SdoExpertEvidenceFieldsService {

    private final SdoDeadlineService sdoDeadlineService;

    public void populateFastTrackExpertEvidence(CaseData.CaseDataBuilder<?, ?> updatedData) {
        FastTrackPersonalInjury expertEvidence = FastTrackPersonalInjury.builder()
            .input1(PERSONAL_INJURY_PERMISSION_SDO)
            .input2("The Defendant(s) may ask questions of the Claimant's expert which must be sent to the expert "
                        + "directly and uploaded to the Digital Portal by 4pm on")
            .date2(sdoDeadlineService.nextWorkingDayFromNowDays(14))
            .input3("The answers to the questions shall be answered by the Expert by")
            .date3(sdoDeadlineService.nextWorkingDayFromNowDays(42))
            .input4("and uploaded to the Digital Portal by the party who has asked the question by")
            .date4(sdoDeadlineService.nextWorkingDayFromNowDays(49))
            .build();

        updatedData.fastTrackPersonalInjury(expertEvidence).build();
    }
}
