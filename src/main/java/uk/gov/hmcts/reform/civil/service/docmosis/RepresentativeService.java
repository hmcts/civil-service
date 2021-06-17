package uk.gov.hmcts.reform.civil.service.docmosis;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.Address;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.SolicitorServiceAddress;
import uk.gov.hmcts.reform.civil.model.docmosis.sealedclaim.Representative;
import uk.gov.hmcts.reform.civil.service.OrganisationService;
import uk.gov.hmcts.reform.civil.service.flowstate.StateFlowEngine;

import static java.util.Optional.ofNullable;
import static uk.gov.hmcts.reform.civil.model.docmosis.sealedclaim.Representative.fromOrganisation;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.PENDING_CLAIM_ISSUED_UNREGISTERED_DEFENDANT;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.PENDING_CLAIM_ISSUED_UNREPRESENTED_DEFENDANT;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.fromFullName;

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
            var providedServiceAddress = caseData.getRespondentSolicitor1ServiceAddress();

            return representative.toBuilder()
                .emailAddress(caseData.getRespondentSolicitor1EmailAddress())
                .serviceAddress(fromProvidedAddress(representative.getServiceAddress(), providedServiceAddress))
                .build();
        }
        return ofNullable(caseData.getRespondentSolicitor1OrganisationDetails())
            .map(Representative::fromSolicitorOrganisationDetails)
            .orElse(Representative.builder().build());
    }

    public Representative getApplicantRepresentative(CaseData caseData) {
        var organisationId = caseData.getApplicant1OrganisationPolicy().getOrganisation().getOrganisationID();
        var representative = fromOrganisation(organisationService.findOrganisationById(organisationId)
                                                  .orElseThrow(RuntimeException::new));

        var providedSolicitor1ServiceAddress = caseData.getApplicantSolicitor1ServiceAddress();

        return representative.toBuilder()
            .emailAddress(caseData.getApplicantSolicitor1UserDetails().getEmail())
            .serviceAddress(fromProvidedAddress(representative.getServiceAddress(), providedSolicitor1ServiceAddress))
            .build();
    }

    private Address fromProvidedAddress(
        Address registeredServiceAddress, SolicitorServiceAddress providedServiceAddress) {
        if (providedServiceAddress != null && providedServiceAddress.getRequired().equals(YesOrNo.YES)) {
            return providedServiceAddress.getAddress();
        }
        return registeredServiceAddress;
    }

    private boolean organisationPicked(CaseData caseData) {
        var flowState = fromFullName(stateFlowEngine.evaluate(caseData).getState().getName());
        return flowState != PENDING_CLAIM_ISSUED_UNREPRESENTED_DEFENDANT
            && flowState != PENDING_CLAIM_ISSUED_UNREGISTERED_DEFENDANT;
    }
}
