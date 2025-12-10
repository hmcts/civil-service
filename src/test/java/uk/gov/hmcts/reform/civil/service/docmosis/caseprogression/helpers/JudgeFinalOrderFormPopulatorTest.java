package uk.gov.hmcts.reform.civil.service.docmosis.caseprogression.helpers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.common.DynamicListElement;
import uk.gov.hmcts.reform.civil.model.docmosis.casepogression.JudgeFinalOrderForm;
import uk.gov.hmcts.reform.civil.referencedata.model.LocationRefData;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

@ExtendWith(SpringExtension.class)
public class JudgeFinalOrderFormPopulatorTest {

    @InjectMocks
    private JudgeFinalOrderFormPopulator judgeFinalOrderFormPopulator;

    @Mock
    private AppealInitiativePopulator appealInitiativePopulator;
    @Mock
    private OrderDetailsPopulator orderDetailsPopulator;
    @Mock
    private HearingDetailsPopulator hearingDetailsPopulator;
    @Mock
    private CostDetailsPopulator costDetailsPopulator;
    @Mock
    private AttendeesRepresentationPopulator attendeesRepresentationPopulator;
    @Mock
    private CaseInfoPopulator caseInfoPopulator;
    @Mock
    private JudgeCourtDetailsPopulator judgeCourtDetailsPopulator;
    @Mock
    private UserDetails userDetails;

    @Mock
    private LocationRefData caseManagementLocationDetails;

    private CaseData caseData;

    @BeforeEach
    void setUp() {
        caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
            .hearingLocation(DynamicList.builder()
                                 .value(DynamicListElement.dynamicElement("hearing-location")).build()).build();
    }

    @Test
    void shouldPopulateFinalOrderForm() {

        JudgeFinalOrderForm finalOrderForm = judgeFinalOrderFormPopulator.populateFinalOrderForm(caseData, caseManagementLocationDetails, userDetails);

        assertNotNull(finalOrderForm);

        verify(caseInfoPopulator).populateCaseInfo(any(), any(CaseData.class));
        verify(costDetailsPopulator).populateCostsDetails(any(), any(CaseData.class));
        verify(appealInitiativePopulator).populateAppealDetails(any(), any(CaseData.class));
        verify(appealInitiativePopulator).populateInitiativeOrWithoutNoticeDetails(any(), any(CaseData.class));
        verify(attendeesRepresentationPopulator).populateAttendeesDetails(any(), any(CaseData.class));
        verify(orderDetailsPopulator).populateAssistedOrderDetails(any(), any(CaseData.class));
        verify(hearingDetailsPopulator).populateHearingDetails(any(), any(CaseData.class), any(LocationRefData.class));
        verify(judgeCourtDetailsPopulator).populateJudgeCourtDetails(any(), any(UserDetails.class), any(LocationRefData.class), any(String.class));

    }

    @Test
    void shouldPopulateFreeFormOrder() {

        JudgeFinalOrderForm freeFormOrder = judgeFinalOrderFormPopulator.populateFreeFormOrder(caseData, caseManagementLocationDetails, userDetails);

        assertNotNull(freeFormOrder);
        verify(caseInfoPopulator).populateCaseInfo(any(), any(CaseData.class));
        verify(orderDetailsPopulator).populateOrderDetails(any(), any(CaseData.class));
        verify(judgeCourtDetailsPopulator).populateJudgeCourtDetails(any(), any(UserDetails.class), any(LocationRefData.class), any(String.class));

    }

}
