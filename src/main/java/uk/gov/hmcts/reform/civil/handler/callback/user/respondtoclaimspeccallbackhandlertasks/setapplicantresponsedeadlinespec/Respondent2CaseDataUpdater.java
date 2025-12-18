package uk.gov.hmcts.reform.civil.handler.callback.user.respondtoclaimspeccallbackhandlertasks.setapplicantresponsedeadlinespec;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
            respondent2DetailsForTab.setPartyID(caseData.getRespondent2().getPartyID());
            respondent2DetailsForTab.setType(caseData.getRespondent2().getType());
            respondent2DetailsForTab.setIndividualTitle(caseData.getRespondent2().getIndividualTitle());
            respondent2DetailsForTab.setIndividualFirstName(caseData.getRespondent2().getIndividualFirstName());
            respondent2DetailsForTab.setIndividualLastName(caseData.getRespondent2().getIndividualLastName());
            respondent2DetailsForTab.setIndividualDateOfBirth(caseData.getRespondent2().getIndividualDateOfBirth());
            respondent2DetailsForTab.setCompanyName(caseData.getRespondent2().getCompanyName());
            respondent2DetailsForTab.setOrganisationName(caseData.getRespondent2().getOrganisationName());
            respondent2DetailsForTab.setSoleTraderTitle(caseData.getRespondent2().getSoleTraderTitle());
            respondent2DetailsForTab.setSoleTraderFirstName(caseData.getRespondent2().getSoleTraderFirstName());
            respondent2DetailsForTab.setSoleTraderLastName(caseData.getRespondent2().getSoleTraderLastName());
            respondent2DetailsForTab.setSoleTraderTradingAs(caseData.getRespondent2().getSoleTraderTradingAs());
            respondent2DetailsForTab.setSoleTraderDateOfBirth(caseData.getRespondent2().getSoleTraderDateOfBirth());
            respondent2DetailsForTab.setPrimaryAddress(caseData.getRespondent2().getPrimaryAddress());
            respondent2DetailsForTab.setPartyName(caseData.getRespondent2().getPartyName());
            respondent2DetailsForTab.setBulkClaimPartyName(caseData.getRespondent2().getBulkClaimPartyName());
            respondent2DetailsForTab.setPartyTypeDisplayValue(caseData.getRespondent2().getPartyTypeDisplayValue());
            respondent2DetailsForTab.setPartyEmail(caseData.getRespondent2().getPartyEmail());
            respondent2DetailsForTab.setPartyPhone(caseData.getRespondent2().getPartyPhone());
            respondent2DetailsForTab.setLegalRepHeading(caseData.getRespondent2().getLegalRepHeading());
            respondent2DetailsForTab.setUnavailableDates(caseData.getRespondent2().getUnavailableDates());
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
