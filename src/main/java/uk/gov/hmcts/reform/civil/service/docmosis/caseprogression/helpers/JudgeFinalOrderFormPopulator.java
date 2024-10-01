package uk.gov.hmcts.reform.civil.service.docmosis.caseprogression.helpers;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.docmosis.casepogression.JudgeFinalOrderForm;
import uk.gov.hmcts.reform.civil.referencedata.LocationRefDataService;
import uk.gov.hmcts.reform.civil.referencedata.model.LocationRefData;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

@Component
@RequiredArgsConstructor
public class JudgeFinalOrderFormPopulator {

    private final AppealInitiativePopulator appealInitiativePopulator;
    private final OrderDetailsPopulator orderDetailsPopulator;
    private final HearingDetailsPopulator hearingDetailsPopulator;
    private final CostDetailsPopulator costDetailsPopulator;
    private final AttendeesRepresentationPopulator attendeesRepresentationPopulator;
    private final CaseInfoPopulator caseInfoPopulator;
    private final JudgeCourtDetailsPopulator judgeCourtDetailsPopulator;

    public JudgeFinalOrderForm populateFinalOrderForm(CaseData caseData,
                                                      LocationRefData caseManagementLocationDetails,
                                                      UserDetails userDetails) {

        var finalOrderFormBuilder = JudgeFinalOrderForm.builder();
        String hearingLocationText = getHearingLocationText(caseData, caseManagementLocationDetails);

        judgeCourtDetailsPopulator.populateJudgeCourtDetails(finalOrderFormBuilder, userDetails,
                                                             caseManagementLocationDetails, hearingLocationText);
        caseInfoPopulator.populateCaseInfo(finalOrderFormBuilder, caseData);
        costDetailsPopulator.populateCostsDetails(finalOrderFormBuilder, caseData);
        appealInitiativePopulator.populateAppealDetails(finalOrderFormBuilder, caseData);
        appealInitiativePopulator.populateInitiativeOrWithoutNoticeDetails(finalOrderFormBuilder, caseData);
        attendeesRepresentationPopulator.populateAttendeesDetails(finalOrderFormBuilder, caseData);
        orderDetailsPopulator.populateAssistedOrderDetails(finalOrderFormBuilder, caseData);
        hearingDetailsPopulator.populateHearingDetails(finalOrderFormBuilder, caseData, caseManagementLocationDetails);

        return finalOrderFormBuilder.build();
    }

    public JudgeFinalOrderForm populateFreeFormOrder(CaseData caseData,
                                                     LocationRefData caseManagementLocationDetails,
                                                     UserDetails userDetails) {
        var orderFormBuilder = JudgeFinalOrderForm.builder();
        String hearingLocationText = getHearingLocationText(caseData, caseManagementLocationDetails);

        caseInfoPopulator.populateCaseInfo(orderFormBuilder, caseData);
        orderDetailsPopulator.populateOrderDetails(orderFormBuilder, caseData);
        judgeCourtDetailsPopulator.populateJudgeCourtDetails(orderFormBuilder, userDetails,
                                                             caseManagementLocationDetails, hearingLocationText);

        return orderFormBuilder.build();
    }

    private String getHearingLocationText(CaseData caseData,
                                          LocationRefData caseManagementLocationDetails) {
        return caseData.getHearingLocationText() != null ? caseData.getHearingLocationText()
            : LocationRefDataService.getDisplayEntry(caseManagementLocationDetails);
    }
}
