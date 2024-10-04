package uk.gov.hmcts.reform.civil.handler.callback.user.createsdocallbackhandlertests;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.enums.sdo.ClaimsTrack;
import uk.gov.hmcts.reform.civil.enums.sdo.FastTrack;
import uk.gov.hmcts.reform.civil.enums.sdo.OrderType;
import uk.gov.hmcts.reform.civil.enums.sdo.SmallTrack;
import uk.gov.hmcts.reform.civil.handler.callback.user.createsdocallbackhandler.SetOrderDetailsFlags;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.sdo.JudgementSum;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;

@ExtendWith(MockitoExtension.class)
class SetOrderDetailsFlagsTest {

    @Mock
    private FeatureToggleService featureToggleService;

    private SetOrderDetailsFlags setOrderDetailsFlags;

    private CallbackParams callbackParams;

    @BeforeEach
    void setUp() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        setOrderDetailsFlags = new SetOrderDetailsFlags(objectMapper, featureToggleService);
        callbackParams = CallbackParams.builder()
            .caseData(CaseData.builder().build())
            .build();
    }

    @Test
    void shouldSetFlagsToNoWhenFeatureToggleIsEnabled() {
        CaseData caseData = CaseData.builder().build();
        callbackParams = CallbackParams.builder()
            .caseData(caseData)
            .build();

        AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) setOrderDetailsFlags.execute(callbackParams);

        assertEquals("No", response.getData().get("setSmallClaimsFlag"));
        assertEquals("No", response.getData().get("setFastTrackFlag"));
    }

    @Test
    void shouldSetIsSdoR2NewScreenToNoWhenFeatureToggleIsEnabled() {
        CaseData caseData = CaseData.builder().build();
        callbackParams = CallbackParams.builder()
            .caseData(caseData)
            .build();

        when(featureToggleService.isSdoR2Enabled()).thenReturn(true);

        AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) setOrderDetailsFlags.execute(callbackParams);

        assertEquals("No", response.getData().get("isSdoR2NewScreen"));
    }

    @Test
    void shouldSetSmallClaimsFlagToYesWhenSmallClaimsTrack() {
        CaseData caseData = CaseData.builder()
            .drawDirectionsOrderRequired(NO)
            .claimsTrack(ClaimsTrack.smallClaimsTrack)
            .smallClaims(List.of(SmallTrack.smallClaimDisputeResolutionHearing))
            .build();
        CallbackParams params = CallbackParams.builder()
            .caseData(caseData)
            .build();

        when(featureToggleService.isSdoR2Enabled()).thenReturn(true);

        AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) setOrderDetailsFlags.execute(params);

        assertEquals("Yes", response.getData().get("setSmallClaimsFlag"));
        assertEquals("Yes", response.getData().get("isSdoR2NewScreen"));
    }

    @Test
    void shouldSetFastTrackFlagToYesWhenFastTrack() {
        CaseData caseData = CaseData.builder()
            .drawDirectionsOrderRequired(NO)
            .claimsTrack(ClaimsTrack.fastTrack)
            .orderType(OrderType.DECIDE_DAMAGES)
            .fastClaims(List.of(FastTrack.fastClaimNoiseInducedHearingLoss))
            .build();
        CallbackParams params = CallbackParams.builder()
            .caseData(caseData)
            .build();

        when(featureToggleService.isSdoR2Enabled()).thenReturn(true);

        AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) setOrderDetailsFlags.execute(params);

        assertEquals("Yes", response.getData().get("setFastTrackFlag"));
    }

    @Test
    void shouldSetJudgementDeductionValues() {
        CaseData caseData = CaseData.builder()
            .drawDirectionsOrder(JudgementSum.builder().judgementSum(10.0).build())
            .build();
        CallbackParams params = CallbackParams.builder()
            .caseData(caseData)
            .build();

        when(featureToggleService.isSdoR2Enabled()).thenReturn(true);

        AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) setOrderDetailsFlags.execute(params);

        assertThat(response.getData()).extracting("disposalHearingJudgementDeductionValue").extracting("value")
            .isEqualTo("10.0%");
        assertThat(response.getData()).extracting("fastTrackJudgementDeductionValue").extracting("value")
            .isEqualTo("10.0%");
        assertThat(response.getData()).extracting("smallClaimsJudgementDeductionValue").extracting("value")
            .isEqualTo("10.0%");
    }

}
