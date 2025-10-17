package uk.gov.hmcts.reform.civil.service.docmosis;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.enums.dq.GeneralApplicationTypes;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.model.CaseData;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ListGeneratorService {

    public String applicationType(CaseData caseData) {
        return applicationType(caseData.getGeneralAppType().getTypes());
    }

    public String applicationType(GeneralApplicationCaseData caseData) {
        return applicationType(caseData.getGeneralAppType().getTypes());
    }

    private String applicationType(List<GeneralApplicationTypes> types) {
        return types.stream()
            .map(GeneralApplicationTypes::getDisplayedValue)
            .collect(Collectors.joining(", "));
    }

    public String defendantsName(CaseData caseData) {
        return joinNames(caseData.getDefendant1PartyName(), caseData.getDefendant2PartyName());
    }

    public String defendantsName(GeneralApplicationCaseData caseData) {
        return joinNames(caseData.getDefendant1PartyName(), caseData.getDefendant2PartyName());
    }

    public String claimantsName(CaseData caseData) {
        return joinNames(caseData.getClaimant1PartyName(), caseData.getClaimant2PartyName());
    }

    public String claimantsName(GeneralApplicationCaseData caseData) {
        return joinNames(caseData.getClaimant1PartyName(), caseData.getClaimant2PartyName());
    }

    private String joinNames(String primary, String secondary) {
        List<String> names = new ArrayList<>();
        names.add(primary);
        if (secondary != null) {
            names.add(secondary);
        }
        return String.join(", ", names);
    }
}
