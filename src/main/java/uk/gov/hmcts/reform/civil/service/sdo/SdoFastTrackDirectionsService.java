package uk.gov.hmcts.reform.civil.service.sdo;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.enums.sdo.FastTrack;
import uk.gov.hmcts.reform.civil.model.CaseData;

import java.util.List;

@Service
public class SdoFastTrackDirectionsService {

    public boolean hasFastAdditionalDirections(CaseData caseData, FastTrack direction) {
        List<FastTrack> selections = caseData.getTrialAdditionalDirectionsForFastTrack() != null
            ? caseData.getTrialAdditionalDirectionsForFastTrack()
            : caseData.getFastClaims();

        return selections != null
            && direction != null
            && selections.contains(direction);
    }

    public boolean hasFastTrackVariable(CaseData caseData, FastTrackVariable variable) {
        return variable.isEnabled(caseData);
    }

}
