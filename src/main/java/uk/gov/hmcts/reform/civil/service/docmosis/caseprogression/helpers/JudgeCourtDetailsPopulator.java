package uk.gov.hmcts.reform.civil.service.docmosis.caseprogression.helpers;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.docmosis.casepogression.JudgeFinalOrderForm;
import uk.gov.hmcts.reform.civil.model.docmosis.casepogression.JudgeFinalOrderForm.JudgeFinalOrderFormBuilder;
import uk.gov.hmcts.reform.civil.referencedata.model.LocationRefData;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

@Component
public class JudgeCourtDetailsPopulator {

    private LocationRefData caseManagementLocationDetails;

    public JudgeFinalOrderForm.JudgeFinalOrderFormBuilder populateJudgeCourtDetails(JudgeFinalOrderFormBuilder builder,
                                                                                    UserDetails userDetails, LocationRefData caseManagementLocationDetails,
                                                                                    String courtLocation) {
        return builder.judgeNameTitle(userDetails.getFullName())
            .courtName(caseManagementLocationDetails.getExternalShortName())
            .courtLocation(courtLocation);
    }
}
