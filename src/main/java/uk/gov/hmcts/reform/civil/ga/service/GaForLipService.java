package uk.gov.hmcts.reform.civil.ga.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.ga.utils.JudicialDecisionNotificationUtil;
import uk.gov.hmcts.reform.civil.model.IdamUserDetails;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;

import java.util.Objects;

import static java.util.Optional.ofNullable;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;

@RequiredArgsConstructor
@Service
public class GaForLipService {

    private final FeatureToggleService featureToggleService;

    public boolean isGaForLip(GeneralApplicationCaseData caseData) {
        return featureToggleService.isGaForLipsEnabled() && (Objects.nonNull(caseData.getIsGaApplicantLip())
            && caseData.getIsGaApplicantLip().equals(YES))
            || (Objects.nonNull(caseData.getIsGaRespondentOneLip())
            && caseData.getIsGaRespondentOneLip().equals(YES))
            || (caseData.getIsMultiParty().equals(YES)
            && Objects.nonNull(caseData.getIsGaRespondentTwoLip())
            && caseData.getIsGaRespondentTwoLip().equals(YES));
    }

    public boolean isLipApp(GeneralApplicationCaseData caseData) {
        return featureToggleService.isGaForLipsEnabled()
            && Objects.nonNull(caseData.getIsGaApplicantLip())
            && caseData.getIsGaApplicantLip().equals(YES);
    }

    public boolean isLipResp(GeneralApplicationCaseData caseData) {
        return featureToggleService.isGaForLipsEnabled()
            && Objects.nonNull(caseData.getIsGaRespondentOneLip())
            && caseData.getIsGaRespondentOneLip().equals(YES);
    }

    public boolean anyWelsh(GeneralApplicationCaseData caseData) {
        if (featureToggleService.isGaForLipsEnabled()) {
            return caseData.isApplicantBilingual()
                || caseData.isRespondentBilingual();
        }
        return false;
    }

    public boolean anyWelshNotice(GeneralApplicationCaseData caseData) {
        if (featureToggleService.isGaForLipsEnabled()) {
            if (!JudicialDecisionNotificationUtil.isWithNotice(caseData)) {
                return caseData.isApplicantBilingual();
            }
            return caseData.isApplicantBilingual()
                || caseData.isRespondentBilingual();
        }
        return false;
    }

    public String getApplicant1Email(GeneralApplicationCaseData civilCaseData) {
        return ofNullable(civilCaseData.getClaimantUserDetails())
            .map(IdamUserDetails::getEmail)
            .or(() -> ofNullable(civilCaseData.getApplicantSolicitor1UserDetails())
                .map(IdamUserDetails::getEmail))
            .orElse(null);
    }

    public String getDefendant1Email(GeneralApplicationCaseData civilCaseData) {
        return ofNullable(civilCaseData.getDefendantUserDetails())
            .map(IdamUserDetails::getEmail)
            .or(() -> ofNullable(civilCaseData.getRespondentSolicitor1EmailAddress())
                .map(String::toString)).orElse(null);
    }
}
