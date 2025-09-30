package uk.gov.hmcts.reform.civil.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.genapplication.GASolicitorDetailsGAspec;

import java.util.List;

import static uk.gov.hmcts.reform.civil.enums.CaseRole.DEFENDANT;
import static uk.gov.hmcts.reform.civil.enums.CaseRole.RESPONDENTSOLICITORONE;
import static uk.gov.hmcts.reform.civil.enums.CaseRole.RESPONDENTSOLICITORTWO;

@Service
@RequiredArgsConstructor
@Slf4j
public class AssignCaseToResopondentSolHelper {

    private final CoreCaseUserService coreCaseUserService;
    private final GaForLipService gaForLipService;

    private static final int FIRST_SOLICITOR = 0;

    public void assignCaseToRespondentSolicitor(CaseData caseData, String caseId) {

        /*
         * Assign case respondent solicitors if judge uncloak the application
         * */
        if (!CollectionUtils.isEmpty(caseData.getGeneralAppRespondentSolicitors())) {

            if (!gaForLipService.isLipResp(caseData)) {

                List<Element<GASolicitorDetailsGAspec>> respondentSolList = caseData.getGeneralAppRespondentSolicitors().stream()
                    .filter(userOrgId -> !(userOrgId.getValue().getOrganisationIdentifier()
                        .equalsIgnoreCase(caseData.getGeneralAppApplnSolicitor().getOrganisationIdentifier()))).toList();
                GASolicitorDetailsGAspec respondentSolicitor1 =
                    respondentSolList.get(FIRST_SOLICITOR).getValue();
                log.info("Assigning case {} to first respondent solicitor 1: {}", caseId, respondentSolicitor1.getId());
                coreCaseUserService.assignCase(caseId, respondentSolicitor1.getId(),
                    respondentSolicitor1.getOrganisationIdentifier(), RESPONDENTSOLICITORONE);
                for (Element<GASolicitorDetailsGAspec> respSolElement : respondentSolList) {
                    if ((respondentSolicitor1.getOrganisationIdentifier() != null && respondentSolicitor1.getOrganisationIdentifier()
                        .equalsIgnoreCase(respSolElement.getValue().getOrganisationIdentifier()))) {
                        log.info("Assigning case {} to respondent solicitor: {}", caseId, respSolElement.getValue().getId());
                        coreCaseUserService
                            .assignCase(caseId, respSolElement.getValue().getId(),
                                respSolElement.getValue().getOrganisationIdentifier(),
                                RESPONDENTSOLICITORONE);
                    } else if (caseData.getIsMultiParty().equals(YesOrNo.YES)
                        && !(respondentSolicitor1.getOrganisationIdentifier() != null && respondentSolicitor1.getOrganisationIdentifier()
                        .equalsIgnoreCase(respSolElement.getValue().getOrganisationIdentifier()))) {
                        log.info("Assigning case {} to respondent solicitor2: {}", caseId, respSolElement.getValue().getId());
                        coreCaseUserService
                            .assignCase(caseId, respSolElement.getValue().getId(),
                                respSolElement.getValue().getOrganisationIdentifier(),
                                RESPONDENTSOLICITORTWO);
                    }

                }
            } else {
                /* GA for Lip*/
                GASolicitorDetailsGAspec respondentSolicitor1
                    = caseData.getGeneralAppRespondentSolicitors().get(FIRST_SOLICITOR).getValue();
                log.info("Assigning GA for Lip case {} to first respondent solicitor 1: {}", caseId, respondentSolicitor1.getId());
                coreCaseUserService.assignCase(caseId, respondentSolicitor1.getId(), null, DEFENDANT);
            }
        }
    }
}
