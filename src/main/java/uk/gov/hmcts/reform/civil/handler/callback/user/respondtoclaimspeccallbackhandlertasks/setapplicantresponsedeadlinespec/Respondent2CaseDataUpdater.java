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
            log.debug(
                "Respondent2 has the same legal representative and response is same for caseId: {}",
                caseData.getCcdCaseReference()
            );
            caseData.setRespondent2ClaimResponseTypeForSpec(caseData.getRespondent1ClaimResponseTypeForSpec());
            caseData.setRespondent2ResponseDate(responseDate);
        }

        if (ofNullable(caseData.getRespondent2()).isPresent()
            && ofNullable(caseData.getRespondent2Copy()).isPresent()) {
            log.debug("Updating Respondent2 primary address and flags for caseId: {}", caseData.getCcdCaseReference());
            caseData.getRespondent2().setPrimaryAddress(caseData.getRespondent2Copy().getPrimaryAddress());
            caseData.getRespondent2().setFlags(caseData.getRespondent2Copy().getFlags());
            caseData.setRespondent2Copy(null);
            Party respondent2DetailsForTab = Party.builder()
                .partyID(caseData.getRespondent2().getPartyID())
                .type(caseData.getRespondent2().getType())
                .individualTitle(caseData.getRespondent2().getIndividualTitle())
                .individualFirstName(caseData.getRespondent2().getIndividualFirstName())
                .individualLastName(caseData.getRespondent2().getIndividualLastName())
                .individualDateOfBirth(caseData.getRespondent2().getIndividualDateOfBirth())
                .companyName(caseData.getRespondent2().getCompanyName())
                .organisationName(caseData.getRespondent2().getOrganisationName())
                .soleTraderTitle(caseData.getRespondent2().getSoleTraderTitle())
                .soleTraderFirstName(caseData.getRespondent2().getSoleTraderFirstName())
                .soleTraderLastName(caseData.getRespondent2().getSoleTraderLastName())
                .soleTraderTradingAs(caseData.getRespondent2().getSoleTraderTradingAs())
                .soleTraderDateOfBirth(caseData.getRespondent2().getSoleTraderDateOfBirth())
                .primaryAddress(caseData.getRespondent2().getPrimaryAddress())
                .partyName(caseData.getRespondent2().getPartyName())
                .bulkClaimPartyName(caseData.getRespondent2().getBulkClaimPartyName())
                .partyTypeDisplayValue(caseData.getRespondent2().getPartyTypeDisplayValue())
                .partyEmail(caseData.getRespondent2().getPartyEmail())
                .partyPhone(caseData.getRespondent2().getPartyPhone())
                .legalRepHeading(caseData.getRespondent2().getLegalRepHeading())
                .unavailableDates(caseData.getRespondent2().getUnavailableDates())
                .flags(null)
                .build();
            caseData.setRespondent2DetailsForClaimDetailsTab(respondent2DetailsForTab);
        }

        log.debug(
            "Updating case list display defendant solicitor references for caseId: {}",
            caseData.getCcdCaseReference()
        );
        caseData.setCaseListDisplayDefendantSolicitorReferences(getAllDefendantSolicitorReferencesSpec(caseData));
    }
}
