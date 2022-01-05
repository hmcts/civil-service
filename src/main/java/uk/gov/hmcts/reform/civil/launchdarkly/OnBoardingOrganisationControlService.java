package uk.gov.hmcts.reform.civil.launchdarkly;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.service.OrganisationService;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OnBoardingOrganisationControlService {

    // Return an empty list for CMC-1874 so orgs are not white listed during beta

    public static final String ORG_NOT_ONBOARDED = "You cannot use this service because your organisation is"
        + " not part of the HMCTS civil damages pilot.";

    private final FeatureToggleService featureToggleService;
    private final OrganisationService organisationService;

    public List<String> validateOrganisation(String userBearer) {
        //        Optional<Organisation> userOrganisation = organisationService.findOrganisation(userBearer);
        //
        //        boolean organisationOnboarded = userOrganisation
        //            .map(userOrg -> featureToggleService.isOrganisationOnboarded(userOrg.getOrganisationIdentifier()))
        //            .orElse(false);
        //
        //        List<String> errors = new ArrayList<>();
        //        if (!organisationOnboarded) {
        //            errors.add(ORG_NOT_ONBOARDED);
        //        }
        //
        //        return errors;
        return new ArrayList<>();
    }

}
