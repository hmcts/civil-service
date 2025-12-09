package uk.gov.hmcts.reform.civil.service.docmosis.caseprogression.helpers;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.civil.model.docmosis.casepogression.JudgeFinalOrderForm;
import uk.gov.hmcts.reform.civil.referencedata.model.LocationRefData;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
public class JudgeCourtDetailsGroupTest {

    @InjectMocks
    private JudgeCourtDetailsPopulator judgeCourtDetailsPopulator;
    @Mock
    private UserDetails userDetails;

    @Mock
    private LocationRefData caseManagementLocationDetails;

    @Test
    void shouldPopulateJudgeCourtDetails_WhenAllFieldsArePresent() {
        String expectedJudgeName = "Judge John Smith";
        String expectedCourtName = "Central Court";
        String expectedCourtLocation = "London";

        when(userDetails.getFullName()).thenReturn(expectedJudgeName);
        when(caseManagementLocationDetails.getExternalShortName()).thenReturn(expectedCourtName);

        JudgeFinalOrderForm.JudgeFinalOrderFormBuilder builder = JudgeFinalOrderForm.builder();

        builder = judgeCourtDetailsPopulator.populateJudgeCourtDetails(builder, userDetails, caseManagementLocationDetails, expectedCourtLocation);

        JudgeFinalOrderForm result = builder.build();
        Assertions.assertEquals(expectedJudgeName, result.getJudgeNameTitle());
        Assertions.assertEquals(expectedCourtName, result.getCourtName());
        Assertions.assertEquals(expectedCourtLocation, result.getCourtLocation());
    }
}
