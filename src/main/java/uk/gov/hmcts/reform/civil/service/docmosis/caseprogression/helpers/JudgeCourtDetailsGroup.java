package uk.gov.hmcts.reform.civil.service.docmosis.caseprogression.helpers;

import uk.gov.hmcts.reform.civil.model.CaseManagementCategory;
import uk.gov.hmcts.reform.civil.model.docmosis.casepogression.JudgeFinalOrderForm;
import uk.gov.hmcts.reform.civil.referencedata.model.LocationRefData;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

public class JudgeCourtDetailsGroup {

    private LocationRefData caseManagementLocationDetails;

    public void populateJudgeCourtDetails(JudgeFinalOrderForm.JudgeFinalOrderFormBuilder builder,
                                          UserDetails userDetails, LocationRefData caseManagementLocationDetails,
                                          String courtLocation) {
        builder.judgeNameTitle(userDetails.getFullName())
            .courtName(caseManagementLocationDetails.getExternalShortName())
            .courtLocation(courtLocation);
    }
}
