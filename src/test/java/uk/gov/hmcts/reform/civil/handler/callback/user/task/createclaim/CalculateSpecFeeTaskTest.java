package uk.gov.hmcts.reform.civil.handler.callback.user.task.createclaim;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.handler.callback.user.task.createclaim.CalculateSpecFeeTask;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.TimelineOfEventDetails;
import uk.gov.hmcts.reform.civil.model.TimelineOfEvents;
import uk.gov.hmcts.reform.civil.model.interestcalc.InterestClaimFromType;
import uk.gov.hmcts.reform.civil.model.interestcalc.InterestClaimOptions;
import uk.gov.hmcts.reform.civil.model.interestcalc.SameRateInterestSelection;
import uk.gov.hmcts.reform.civil.model.interestcalc.SameRateInterestType;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.FeesService;
import uk.gov.hmcts.reform.civil.service.OrganisationService;
import uk.gov.hmcts.reform.civil.utils.InterestCalculator;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;

@ExtendWith(MockitoExtension.class)
class CalculateSpecFeeTaskTest extends BaseCallbackHandlerTest {

    private CalculateSpecFeeTask calculateSpecFeeTask;

    @Mock
    private InterestCalculator interestCalculator;

    @Mock
    private FeesService feesService;

    @Mock
    private FeatureToggleService featureToggleService;

    @Mock
    private OrganisationService organisationService;

    String authTokenGenerator = "authTokenGenerator";

    @BeforeEach
    public void setUp() {
        calculateSpecFeeTask = new CalculateSpecFeeTask(interestCalculator, featureToggleService, feesService, new ObjectMapper(), organisationService);
    }

    @Test
    void shouldCalculateSpecFee_whenPopulated() {
        List<TimelineOfEvents> timelineOfEvents = new ArrayList<>();
        timelineOfEvents.add(
            TimelineOfEvents.builder().value(TimelineOfEventDetails.builder().timelineDate(LocalDate.now().minusDays(1)).build()).build());
        CaseData caseData = CaseData.builder().claimInterest(YES)
            .interestClaimOptions(InterestClaimOptions.SAME_RATE_INTEREST)
            .sameRateInterestSelection(SameRateInterestSelection.builder()
                                           .sameRateInterestType(SameRateInterestType.SAME_RATE_INTEREST_8_PC).build())
            .interestClaimFrom(InterestClaimFromType.FROM_CLAIM_SUBMIT_DATE)
            .totalClaimAmount(new BigDecimal(1000))
            .build();
        when(interestCalculator.calculateInterest(caseData)).thenReturn(new BigDecimal(0));
        var response = (AboutToStartOrSubmitCallbackResponse) calculateSpecFeeTask.calculateSpecFee(caseData, authTokenGenerator);

        assertThat(response.getData()).containsEntry("applicantSolicitor1PbaAccountsIsEmpty", "Yes");
    }
}
