package uk.gov.hmcts.reform.civil.service.docmosis;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.model.OrganisationPolicy;
import uk.gov.hmcts.reform.civil.enums.SuperClaimType;
import uk.gov.hmcts.reform.civil.launchdarkly.FeatureToggleService;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.docmosis.sealedclaim.Representative;
import uk.gov.hmcts.reform.civil.service.OrganisationService;

import java.util.Optional;

import static java.util.Optional.ofNullable;
import static uk.gov.hmcts.reform.civil.model.docmosis.sealedclaim.Representative.fromOrganisation;

@Service
@RequiredArgsConstructor
public class RepresentativeService {

    private final OrganisationService organisationService;
    private final FeatureToggleService featureToggleService;

    private boolean doesOrganisationPolicyExist(OrganisationPolicy organisationPolicy) {
        if (featureToggleService.isNoticeOfChangeEnabled()) {
            return organisationPolicy != null
                && organisationPolicy.getOrganisation() != null
                && organisationPolicy.getOrganisation().getOrganisationID() != null;
        }
        return organisationPolicy != null;
    }

    public Representative getRespondent1Representative(CaseData caseData) {
        if (doesOrganisationPolicyExist(caseData.getRespondent1OrganisationPolicy())) {
            var organisationId = caseData.getRespondent1OrganisationPolicy().getOrganisation().getOrganisationID();
            var representative = fromOrganisation(organisationService.findOrganisationById(organisationId)
                                                      .orElseThrow(RuntimeException::new));

            var representativeBuilder = representative.toBuilder();

            Optional.ofNullable(caseData.getRespondentSolicitor1ServiceAddress())
                .ifPresent(representativeBuilder::serviceAddress);
            if (SuperClaimType.SPEC_CLAIM == caseData.getSuperClaimType()
                && caseData.getSpecRespondentCorrespondenceAddressdetails() != null) {
                representativeBuilder.serviceAddress(caseData.getSpecRespondentCorrespondenceAddressdetails());
            }

            return representativeBuilder
                .emailAddress(caseData.getRespondentSolicitor1EmailAddress())
                .build();
        }
        return ofNullable(caseData.getRespondentSolicitor1OrganisationDetails())
            .map(Representative::fromSolicitorOrganisationDetails)
            .orElse(Representative.builder().build());
    }

    public Representative getRespondent2Representative(CaseData caseData) {
        if (doesOrganisationPolicyExist(caseData.getRespondent2OrganisationPolicy())) {
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
        // all applicants share solicitor
        var organisationId = caseData.getApplicant1OrganisationPolicy().getOrganisation().getOrganisationID();
        var representative = fromOrganisation(organisationService.findOrganisationById(organisationId)
                                                  .orElseThrow(RuntimeException::new));

        var representativeBuilder = representative.toBuilder();
        Optional.ofNullable(caseData.getApplicantSolicitor1ServiceAddress())
            .ifPresent(representativeBuilder::serviceAddress);
        if (SuperClaimType.SPEC_CLAIM == caseData.getSuperClaimType()
            && caseData.getSpecApplicantCorrespondenceAddressdetails() != null) {
            representativeBuilder.serviceAddress(caseData.getSpecApplicantCorrespondenceAddressdetails());
        }

        return representativeBuilder
            .emailAddress(caseData.getApplicantSolicitor1UserDetails().getEmail())
            .build();
    }
}
