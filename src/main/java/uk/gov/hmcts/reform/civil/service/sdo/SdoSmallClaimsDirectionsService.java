package uk.gov.hmcts.reform.civil.service.sdo;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.enums.sdo.SmallTrack;
import uk.gov.hmcts.reform.civil.model.CaseData;

import java.util.List;

@Service
public class SdoSmallClaimsDirectionsService {

    public boolean hasSmallAdditionalDirections(CaseData caseData, SmallTrack direction) {
        List<SmallTrack> selections = caseData.getDrawDirectionsOrderSmallClaimsAdditionalDirections() != null
            ? caseData.getDrawDirectionsOrderSmallClaimsAdditionalDirections()
            : caseData.getSmallClaims();

        return selections != null
            && direction != null
            && selections.contains(direction);
    }

    public boolean hasSmallClaimsVariable(CaseData caseData, SmallClaimsVariable variable) {
        return variable.isEnabled(caseData);
    }
}
