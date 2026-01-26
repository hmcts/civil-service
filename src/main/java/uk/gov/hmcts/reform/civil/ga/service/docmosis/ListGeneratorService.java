package uk.gov.hmcts.reform.civil.ga.service.docmosis;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.enums.dq.GeneralApplicationTypes;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ListGeneratorService {

    public String applicationType(GeneralApplicationCaseData caseData) {
        List<GeneralApplicationTypes> types = caseData.getGeneralAppType().getTypes();
        String collect = types.stream()
            .map(GeneralApplicationTypes::getDisplayedValue).collect(Collectors.joining(", "));
        return collect;
    }

    public String defendantsName(GeneralApplicationCaseData caseData) {
        List<String> defendentNames = new ArrayList<>();
        defendentNames.add(caseData.getDefendant1PartyName());
        if (caseData.getDefendant2PartyName() != null) {
            defendentNames.add(caseData.getDefendant2PartyName());
        }
        return String.join(", ", defendentNames);
    }

    public String claimantsName(GeneralApplicationCaseData caseData) {
        List<String> claimantNames = new ArrayList<>();
        claimantNames.add(caseData.getClaimant1PartyName());
        if (caseData.getClaimant2PartyName() != null) {
            claimantNames.add(caseData.getClaimant2PartyName());
        }
        return String.join(", ", claimantNames);
    }
}
