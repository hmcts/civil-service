package uk.gov.hmcts.reform.civil.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.genapplication.CaseLink;
import uk.gov.hmcts.reform.civil.model.genapplication.GeneralApplication;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static uk.gov.hmcts.reform.civil.callback.CaseEvent.UPDATE_GA_CASE_DATA;

@Slf4j
@Service
@RequiredArgsConstructor
public class GeneralApplicationCaseNameService {

    private final CoreCaseDataService coreCaseDataService;

    public void updateGeneralApplicationsIfCaseNameChanged(CaseData savedCaseData, CaseData caseDataBefore) {
        String savedCaseName = savedCaseData.getCaseNameHmctsInternal();
        String caseNameBefore = Optional.ofNullable(caseDataBefore).map(CaseData::getCaseNameHmctsInternal).orElse(null);

        if (savedCaseName == null || savedCaseName.equals(caseNameBefore)) {
            return;
        }

        Optional.ofNullable(savedCaseData.getGeneralApplications()).orElse(List.of()).stream()
            .map(Element::getValue)
            .map(GeneralApplication::getCaseLink)
            .filter(Objects::nonNull)
            .map(CaseLink::getCaseReference)
            .filter(Objects::nonNull)
            .forEach(gaCaseReference -> updateCaseName(gaCaseReference, savedCaseName, savedCaseData.getCcdCaseReference()));
    }

    private void updateCaseName(String gaCaseReference, String caseName, Long parentCaseReference) {
        try {
            coreCaseDataService.triggerGeneralApplicationEvent(
                Long.parseLong(gaCaseReference),
                UPDATE_GA_CASE_DATA,
                Map.of("caseNameGaInternal", caseName)
            );
        } catch (RuntimeException e) {
            log.error("Failed to update case name on general application {} for parent case {}",
                      gaCaseReference, parentCaseReference, e);
        }
    }
}
