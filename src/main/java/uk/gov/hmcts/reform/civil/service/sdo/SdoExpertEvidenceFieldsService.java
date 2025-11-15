package uk.gov.hmcts.reform.civil.service.sdo;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.sdo.FastTrackPersonalInjury;

import static uk.gov.hmcts.reform.civil.service.directionsorder.DirectionsOrderSpecialistTextLibrary.PERSONAL_INJURY_ANSWERS;
import static uk.gov.hmcts.reform.civil.service.directionsorder.DirectionsOrderSpecialistTextLibrary.PERSONAL_INJURY_DEFENDANT_QUESTIONS;
import static uk.gov.hmcts.reform.civil.service.directionsorder.DirectionsOrderSpecialistTextLibrary.PERSONAL_INJURY_PERMISSION_SDO;
import static uk.gov.hmcts.reform.civil.service.directionsorder.DirectionsOrderSpecialistTextLibrary.PERSONAL_INJURY_UPLOAD_BY_ASKING_PARTY;

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
            .input2(PERSONAL_INJURY_DEFENDANT_QUESTIONS)
            .date2(sdoDeadlineService.nextWorkingDayFromNowDays(14))
            .input3(PERSONAL_INJURY_ANSWERS)
            .date3(sdoDeadlineService.nextWorkingDayFromNowDays(42))
            .input4(PERSONAL_INJURY_UPLOAD_BY_ASKING_PARTY)
            .date4(sdoDeadlineService.nextWorkingDayFromNowDays(49))
            .build();

        updatedData.fastTrackPersonalInjury(expertEvidence).build();
    }
}
