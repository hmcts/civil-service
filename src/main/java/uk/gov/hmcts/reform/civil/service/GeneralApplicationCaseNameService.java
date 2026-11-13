package uk.gov.hmcts.reform.civil.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.genapplication.CaseLink;
import uk.gov.hmcts.reform.civil.model.genapplication.GeneralApplication;
import uk.gov.hmcts.reform.civil.model.genapplication.GeneralApplicationsDetails;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import static uk.gov.hmcts.reform.civil.callback.CaseEvent.UPDATE_GA_CASE_DATA;

@Slf4j
@Service
@RequiredArgsConstructor
public class GeneralApplicationCaseNameService {

    private final CoreCaseDataService coreCaseDataService;

    public void updateGeneralApplicationsIfCaseNameChanged(CaseData savedCaseData, CaseData caseDataBefore) {
        String caseNameBefore = Optional.ofNullable(caseDataBefore).map(CaseData::getCaseNameHmctsInternal).orElse(null);

        if (!Objects.equals(savedCaseData.getCaseNameHmctsInternal(), caseNameBefore)) {
            updateGeneralApplications(savedCaseData);
        }
    }

    public List<String> updateGeneralApplications(CaseData parentCaseData) {
        String caseName = parentCaseData.getCaseNameHmctsInternal();
        if (caseName == null) {
            return List.of();
        }

        return linkedGeneralApplicationReferences(parentCaseData).stream()
            .filter(gaCaseReference -> !updateCaseName(gaCaseReference, caseName, parentCaseData.getCcdCaseReference()))
            .toList();
    }

    private List<String> linkedGeneralApplicationReferences(CaseData parentCaseData) {
        Stream<CaseLink> applicationLinks = Optional.ofNullable(parentCaseData.getGeneralApplications())
            .orElse(List.of()).stream()
            .map(Element::getValue)
            .map(GeneralApplication::getCaseLink);
        Stream<CaseLink> masterCollectionLinks = Optional.ofNullable(parentCaseData.getGaDetailsMasterCollection())
            .orElse(List.of()).stream()
            .map(Element::getValue)
            .map(GeneralApplicationsDetails::getCaseLink);

        return Stream.concat(applicationLinks, masterCollectionLinks)
            .filter(Objects::nonNull)
            .map(CaseLink::getCaseReference)
            .filter(Objects::nonNull)
            .distinct()
            .toList();
    }

    private boolean updateCaseName(String gaCaseReference, String caseName, Long parentCaseReference) {
        try {
            coreCaseDataService.triggerGeneralApplicationEvent(
                Long.parseLong(gaCaseReference),
                UPDATE_GA_CASE_DATA,
                Map.of("caseNameGaInternal", caseName)
            );
            return true;
        } catch (RuntimeException e) {
            log.error("Failed to update case name on general application {} for parent case {}",
                      gaCaseReference, parentCaseReference, e);
            return false;
        }
    }
}
