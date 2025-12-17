package uk.gov.hmcts.reform.civil.handler.callback.user.respondtoclaimspeccallbackhandlertasks.setapplicantresponsedeadlinespec;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.handler.callback.user.respondtoclaimspeccallbackhandlertasks.RespondToClaimSpecUtils;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.service.Time;

import java.time.LocalDateTime;

import static java.util.Optional.ofNullable;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.utils.CaseListSolicitorReferenceUtils.getAllDefendantSolicitorReferencesSpec;

@Component
@RequiredArgsConstructor
@Slf4j
public class Respondent2CaseDataUpdater implements SetApplicantResponseDeadlineCaseDataUpdater {

    private final RespondToClaimSpecUtils respondToClaimSpecUtils;
    private final Time time;

    @Override
    public void update(CaseData caseData) {
        log.info("Updating Respondent2CaseData for caseId: {}", caseData.getCcdCaseReference());

        LocalDateTime responseDate = time.now();
        if (respondToClaimSpecUtils.isRespondent2HasSameLegalRep(caseData)
            && caseData.getRespondentResponseIsSame() != null && caseData.getRespondentResponseIsSame() == YES) {
            log.info(
                "Respondent2 has the same legal representative and response is same for caseId: {}",
                caseData.getCcdCaseReference()
            );
            caseData.setRespondent2ClaimResponseTypeForSpec(caseData.getRespondent1ClaimResponseTypeForSpec());
            caseData.setRespondent2ResponseDate(responseDate);
        }

        if (ofNullable(caseData.getRespondent2()).isPresent()
            && ofNullable(caseData.getRespondent2Copy()).isPresent()) {
            log.info("Updating Respondent2 primary address and flags for caseId: {}", caseData.getCcdCaseReference());
            caseData.getRespondent2().setPrimaryAddress(caseData.getRespondent2Copy().getPrimaryAddress());
            caseData.getRespondent2().setFlags(caseData.getRespondent2Copy().getFlags());
            caseData.setRespondent2Copy(null);
            Party respondent2DetailsForTab = new Party();
            BeanUtils.copyProperties(caseData.getRespondent2(), respondent2DetailsForTab);
            respondent2DetailsForTab.setFlags(null);
            caseData.setRespondent2DetailsForClaimDetailsTab(respondent2DetailsForTab);
        }

        log.info(
            "Updating case list display defendant solicitor references for caseId: {}",
            caseData.getCcdCaseReference()
        );
        caseData.setCaseListDisplayDefendantSolicitorReferences(getAllDefendantSolicitorReferencesSpec(caseData));
    }
}
