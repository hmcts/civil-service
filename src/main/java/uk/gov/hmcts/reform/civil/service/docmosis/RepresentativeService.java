package uk.gov.hmcts.reform.civil.service.docmosis;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.model.OrganisationPolicy;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.docmosis.sealedclaim.Representative;
import uk.gov.hmcts.reform.civil.service.OrganisationService;

import java.util.Optional;

import static java.util.Optional.ofNullable;
import static uk.gov.hmcts.reform.civil.enums.CaseCategory.SPEC_CLAIM;
import static uk.gov.hmcts.reform.civil.model.docmosis.sealedclaim.Representative.fromOrganisation;

@Slf4j
@Service
@RequiredArgsConstructor
public class RepresentativeService {

    private final OrganisationService organisationService;
    private final FeatureToggleService featureToggleService;

    private boolean doesOrganisationPolicyExist(OrganisationPolicy organisationPolicy, String organisationIDCopy) {
        return (organisationPolicy != null && organisationPolicy.getOrganisation() != null
            && organisationPolicy.getOrganisation().getOrganisationID() != null)
            || organisationIDCopy != null;
    }

    public Representative getRespondent1Representative(CaseData caseData) {
        if (doesOrganisationPolicyExist(caseData.getRespondent1OrganisationPolicy(), caseData.getRespondent1OrganisationIDCopy())) {
            var organisationId = caseData.getRespondent1OrganisationIDCopy();

            if (organisationId == null || organisationId.isEmpty()) {
                organisationId = caseData.getRespondent1OrganisationPolicy().getOrganisation().getOrganisationID();
            }

            var representative = fromOrganisation(organisationService.findOrganisationById(organisationId)
                                                      .orElseThrow(RuntimeException::new));

            var representativeBuilder = representative.toBuilder();

            Optional.ofNullable(caseData.getRespondentSolicitor1ServiceAddress())
                .ifPresent(representativeBuilder::serviceAddress);
            representativeBuilder.legalRepHeading(caseData.getCaseAccessCategory().equals(SPEC_CLAIM)
                                                      ? "Name" : "Organisation name");
            if (SPEC_CLAIM.equals(caseData.getCaseAccessCategory())
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
        if (doesOrganisationPolicyExist(caseData.getRespondent2OrganisationPolicy(), caseData.getRespondent2OrganisationIDCopy())) {
            var organisationId = caseData.getRespondent2OrganisationIDCopy();

            if (organisationId == null || organisationId.isEmpty()) {
                organisationId = caseData.getRespondent2OrganisationPolicy().getOrganisation().getOrganisationID();
            }

            log.info("organisation2ID: " + organisationId);
            var representative = fromOrganisation(organisationService.findOrganisationById(organisationId)
                                                      .orElseThrow(RuntimeException::new));

            var representativeBuilder = representative.toBuilder();

            representativeBuilder.legalRepHeading(caseData.getCaseAccessCategory().equals(SPEC_CLAIM)
                                                      ? "Name" : "Organisation name");

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
        if (caseData.isApplicantNotRepresented()) {
            return Representative.builder().build();
        }
        // all applicants share solicitor
        var organisationId = caseData.getApplicant1OrganisationPolicy().getOrganisation().getOrganisationID();
        var representative = fromOrganisation(organisationService.findOrganisationById(organisationId)
                                                  .orElseThrow(RuntimeException::new));

        var representativeBuilder = representative.toBuilder();
        Optional.ofNullable(caseData.getApplicantSolicitor1ServiceAddress())
            .ifPresent(representativeBuilder::serviceAddress);
        representativeBuilder.legalRepHeading(caseData.getCaseAccessCategory().equals(SPEC_CLAIM)
                                                  ? "Name" : "Organisation name");
        if (SPEC_CLAIM.equals(caseData.getCaseAccessCategory())
            && caseData.getSpecApplicantCorrespondenceAddressdetails() != null) {
            representativeBuilder.serviceAddress(caseData.getSpecApplicantCorrespondenceAddressdetails());
        }

        return representativeBuilder
            .emailAddress(caseData.getApplicantSolicitor1UserDetails().getEmail())
            .build();
    }
}
