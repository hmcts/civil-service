package uk.gov.hmcts.reform.civil.service.docmosis;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.docmosis.sealedclaim.Representative;
import uk.gov.hmcts.reform.civil.service.OrganisationService;
import uk.gov.hmcts.reform.civil.service.flowstate.StateFlowEngine;

import java.util.Optional;

import static java.util.Optional.ofNullable;
import static uk.gov.hmcts.reform.civil.model.docmosis.sealedclaim.Representative.fromOrganisation;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.PENDING_CLAIM_ISSUED_UNREGISTERED_DEFENDANT;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.PENDING_CLAIM_ISSUED_UNREPRESENTED_DEFENDANT;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.PENDING_CLAIM_ISSUED_UNREPRESENTED_UNREGISTERED_DEFENDANT;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.fromFullName;

@Service
@RequiredArgsConstructor
public class RepresentativeService {

    private final StateFlowEngine stateFlowEngine;
    private final OrganisationService organisationService;

    public Representative getRespondent1Representative(CaseData caseData) {
        if (organisationPicked(caseData)) {
            var organisationId = caseData.getRespondent1OrganisationPolicy().getOrganisation().getOrganisationID();
            var representative = fromOrganisation(organisationService.findOrganisationById(organisationId)
                                                      .orElseThrow(RuntimeException::new));

            var representativeBuilder = representative.toBuilder();

            Optional.ofNullable(caseData.getRespondentSolicitor1ServiceAddress())
                .ifPresent(representativeBuilder::serviceAddress);

            return representativeBuilder
                .emailAddress(caseData.getRespondentSolicitor1EmailAddress())
                .build();
        }
        return ofNullable(caseData.getRespondentSolicitor1OrganisationDetails())
            .map(Representative::fromSolicitorOrganisationDetails)
            .orElse(Representative.builder().build());
    }

    public Representative getRespondent2Representative(CaseData caseData) {
        if (caseData.getRespondent2OrganisationPolicy() != null) {
            var organisationId = caseData.getRespondent2OrganisationPolicy().getOrganisation().getOrganisationID();
            var representative = fromOrganisation(organisationService.findOrganisationById(organisationId)
                                                      .orElseThrow(RuntimeException::new));

            var representativeBuilder = representative.toBuilder();

            Optional.ofNullable(caseData.getRespondentSolicitor2ServiceAddress())
                .ifPresent(representativeBuilder::serviceAddress);

            return representativeBuilder
                .emailAddress(caseData.getRespondentSolicitor2EmailAddress())
                .build();
        }
        return ofNullable(caseData.getRespondentSolicitor2OrganisationDetails())
            .map(Representative::fromSolicitorOrganisationDetails)
            .orElse(Representative.builder().build());
    }

    public Representative getApplicantRepresentative(CaseData caseData) {
        var organisationId = caseData.getApplicant1OrganisationPolicy().getOrganisation().getOrganisationID();
        var representative = fromOrganisation(organisationService.findOrganisationById(organisationId)
                                                  .orElseThrow(RuntimeException::new));

        var representativeBuilder = representative.toBuilder();
        Optional.ofNullable(caseData.getApplicantSolicitor1ServiceAddress())
            .ifPresent(representativeBuilder::serviceAddress);

        return representativeBuilder
            .emailAddress(caseData.getApplicantSolicitor1UserDetails().getEmail())
            .build();
    }

    private boolean organisationPicked(CaseData caseData) {
        var flowState = fromFullName(stateFlowEngine.evaluate(caseData).getState().getName());
        return flowState != PENDING_CLAIM_ISSUED_UNREPRESENTED_DEFENDANT
            && flowState != PENDING_CLAIM_ISSUED_UNREGISTERED_DEFENDANT
            && flowState != PENDING_CLAIM_ISSUED_UNREPRESENTED_UNREGISTERED_DEFENDANT;
    }
}
