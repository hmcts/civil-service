package uk.gov.hmcts.reform.civil.service.docmosis.caseprogression.helpers;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.docmosis.casepogression.JudgeFinalOrderForm;
import uk.gov.hmcts.reform.civil.referencedata.model.LocationRefData;
import uk.gov.hmcts.reform.civil.service.referencedata.LocationReferenceDataService;
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

        var finalOrderForm = new JudgeFinalOrderForm();
        String hearingLocationText = getHearingLocationText(caseData, caseManagementLocationDetails);

        judgeCourtDetailsPopulator.populateJudgeCourtDetails(finalOrderForm, userDetails,
                                                             caseManagementLocationDetails, hearingLocationText);
        caseInfoPopulator.populateCaseInfo(finalOrderForm, caseData);
        costDetailsPopulator.populateCostsDetails(finalOrderForm, caseData);
        appealInitiativePopulator.populateAppealDetails(finalOrderForm, caseData);
        appealInitiativePopulator.populateInitiativeOrWithoutNoticeDetails(finalOrderForm, caseData);
        attendeesRepresentationPopulator.populateAttendeesDetails(finalOrderForm, caseData);
        orderDetailsPopulator.populateAssistedOrderDetails(finalOrderForm, caseData);
        hearingDetailsPopulator.populateHearingDetails(finalOrderForm, caseData, caseManagementLocationDetails);

        return finalOrderForm;
    }

    public JudgeFinalOrderForm populateFreeFormOrder(CaseData caseData,
                                                     LocationRefData caseManagementLocationDetails,
                                                     UserDetails userDetails) {
        var orderForm = new JudgeFinalOrderForm();
        String hearingLocationText = getHearingLocationText(caseData, caseManagementLocationDetails);

        caseInfoPopulator.populateCaseInfo(orderForm, caseData);
        orderDetailsPopulator.populateOrderDetails(orderForm, caseData);
        judgeCourtDetailsPopulator.populateJudgeCourtDetails(orderForm, userDetails,
                                                             caseManagementLocationDetails, hearingLocationText);

        return orderForm;
    }

    private String getHearingLocationText(CaseData caseData,
                                          LocationRefData caseManagementLocationDetails) {
        return caseData.getHearingLocationText() != null ? caseData.getHearingLocationText()
            : LocationReferenceDataService.getDisplayEntry(caseManagementLocationDetails);
    }
}
