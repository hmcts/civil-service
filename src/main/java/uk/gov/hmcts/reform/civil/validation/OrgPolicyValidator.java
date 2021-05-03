package uk.gov.hmcts.reform.civil.validation;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.model.OrganisationPolicy;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;

import java.util.ArrayList;
import java.util.List;

@Component
public class OrgPolicyValidator {

    public List<String> validate(OrganisationPolicy organisationPolicy, YesOrNo solicitorFirmRegistered) {
        List<String> errors = new ArrayList<>();

        if (solicitorFirmRegistered == YesOrNo.YES && (organisationPolicy == null
            || organisationPolicy.getOrganisation() == null
            || organisationPolicy.getOrganisation().getOrganisationID() == null)) {
            errors.add("No Organisation selected");
        }

        return errors;
    }
}
