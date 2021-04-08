package uk.gov.hmcts.reform.unspec.service.docmosis;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.unspec.model.CaseData;
import uk.gov.hmcts.reform.unspec.model.docmosis.sealedclaim.Representative;
import uk.gov.hmcts.reform.unspec.service.OrganisationService;
import uk.gov.hmcts.reform.unspec.service.flowstate.StateFlowEngine;

import static java.util.Optional.ofNullable;
import static uk.gov.hmcts.reform.unspec.model.docmosis.sealedclaim.Representative.fromOrganisation;
import static uk.gov.hmcts.reform.unspec.service.flowstate.FlowState.Main.PENDING_CLAIM_ISSUED_UNREGISTERED_DEFENDANT;
import static uk.gov.hmcts.reform.unspec.service.flowstate.FlowState.Main.PROCEEDS_OFFLINE_UNREPRESENTED_DEFENDANT;
import static uk.gov.hmcts.reform.unspec.service.flowstate.FlowState.fromFullName;

@Service
@RequiredArgsConstructor
public class RepresentativeService {

    private final StateFlowEngine stateFlowEngine;
    private final OrganisationService organisationService;

    public Representative getRespondentRepresentative(CaseData caseData) {
        if (organisationPicked(caseData)) {
            var organisationId = caseData.getRespondent1OrganisationPolicy().getOrganisation().getOrganisationID();
            var representative = fromOrganisation(organisationService.findOrganisationById(organisationId)
                                                      .orElseThrow(RuntimeException::new));
            return representative.toBuilder()
                .emailAddress(caseData.getRespondentSolicitor1EmailAddress())
                .build();
        }
        return ofNullable(caseData.getRespondentSolicitor1OrganisationDetails())
            .map(Representative::fromSolicitorOrganisationDetails)
            .orElse(Representative.builder().build());
    }

    private boolean organisationPicked(CaseData caseData) {
        var flowState = fromFullName(stateFlowEngine.evaluate(caseData).getState().getName());
        return flowState != PROCEEDS_OFFLINE_UNREPRESENTED_DEFENDANT
            && flowState != PENDING_CLAIM_ISSUED_UNREGISTERED_DEFENDANT;
    }
}
