package uk.gov.hmcts.reform.civil.validation;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.service.postcode.PostcodeLookupService;

import java.util.ArrayList;
import java.util.List;

@Component
public class PostcodeValidator {

    private final PostcodeLookupService postcodeLookupService;

    public PostcodeValidator(PostcodeLookupService postcodeLookupService) {
        this.postcodeLookupService = postcodeLookupService;
    }

    public List<String> validatePostCodeForDefendant(String postcode) {
        List<String> errors = new ArrayList<>();
        if (postcode != null) {
            /*CreateClaimCallbackHandlerTest
            Lookup to the PostCode service. Currently, the Postcode lookup service
            returns England for Northern Ireland Postcodes starting with BT.
            Hence, added below check for Northern Ireland postcodes.
             If new regions apart from BT are added for Northern Ireland,
            then this check needs to be modified accordingly.
            For more details, refer to https://tools.hmcts.net/jira/browse/ROC-9113
            */
            if (postcode.toUpperCase().trim().startsWith("BT") || !(postcodeLookupService.validatePostCodeForDefendant(
                postcode))) {
                errors.add("Defendant should be part of England and Wales");
            }
        } else {
            errors.add("Please enter Postcode");
        }
        System.out.println("errors ---" + errors);
        return errors;
    }
}
