package uk.gov.hmcts.reform.civil.service.mediation;

import uk.gov.hmcts.reform.civil.prd.model.Organisation;

import java.util.Optional;

public abstract class LrContactDetails {

    protected String getRepresentativeContactNumber(Optional<Organisation> organisation) {
        return organisation.map(Organisation::getCompanyNumber).orElse("");
    }

}
