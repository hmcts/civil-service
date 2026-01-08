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

    static final String ORGANISATION_NAME = "Organisation name";

    public Representative getRespondent1Representative(CaseData caseData) {
        if (doesOrganisationPolicyExist(caseData.getRespondent1OrganisationPolicy(), caseData.getRespondent1OrganisationIDCopy())) {
            var organisationId = caseData.getRespondent1OrganisationIDCopy();

            if (organisationId == null || organisationId.isEmpty()) {
                organisationId = caseData.getRespondent1OrganisationPolicy().getOrganisation().getOrganisationID();
            }

            log.info("organisation1ID: " + organisationId);
            var representative = fromOrganisation(organisationService.findOrganisationById(organisationId)
                                                      .orElseThrow(RuntimeException::new));

            Optional.ofNullable(caseData.getRespondentSolicitor1ServiceAddress())
                .ifPresent(representative::setServiceAddress);
            representative.setLegalRepHeading(caseData.getCaseAccessCategory().equals(SPEC_CLAIM)
                                                      ? "Name" : ORGANISATION_NAME);
            if (SPEC_CLAIM.equals(caseData.getCaseAccessCategory())
                && caseData.getSpecRespondentCorrespondenceAddressdetails() != null) {
                representative.setServiceAddress(caseData.getSpecRespondentCorrespondenceAddressdetails());
            }

            representative.setEmailAddress(caseData.getRespondentSolicitor1EmailAddress());
            return representative;
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

            representative.setLegalRepHeading(caseData.getCaseAccessCategory().equals(SPEC_CLAIM)
                                                      ? "Name" : ORGANISATION_NAME);

            Optional.ofNullable(caseData.getRespondentSolicitor2ServiceAddress())
                .ifPresent(representative::setServiceAddress);

            if (SPEC_CLAIM.equals(caseData.getCaseAccessCategory())
                && caseData.getSpecRespondent2CorrespondenceAddressdetails() != null) {
                representative.setServiceAddress(caseData.getSpecRespondent2CorrespondenceAddressdetails());
            }

            representative.setEmailAddress(caseData.getRespondentSolicitor2EmailAddress());
            return representative;
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

        Optional.ofNullable(caseData.getApplicantSolicitor1ServiceAddress())
            .ifPresent(representative::setServiceAddress);
        representative.setLegalRepHeading(caseData.getCaseAccessCategory().equals(SPEC_CLAIM)
                                                  ? "Name" : ORGANISATION_NAME);
        if (SPEC_CLAIM.equals(caseData.getCaseAccessCategory())
            && caseData.getSpecApplicantCorrespondenceAddressdetails() != null) {
            representative.setServiceAddress(caseData.getSpecApplicantCorrespondenceAddressdetails());
        }

        representative.setEmailAddress(caseData.getApplicantSolicitor1UserDetails().getEmail());
        return representative;
    }
}
