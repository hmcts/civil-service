package uk.gov.hmcts.reform.civil.handler.callback.user.createsdocallbackhandlertests.integrationtests;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.autoconfigure.validation.ValidationAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.civil.bankholidays.NonWorkingDaysCollection;
import uk.gov.hmcts.reform.civil.bankholidays.PublicHolidaysCollection;
import uk.gov.hmcts.reform.civil.bankholidays.WorkingDayIndicator;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.config.ClaimUrlsConfiguration;
import uk.gov.hmcts.reform.civil.config.MockDatabaseConfiguration;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.enums.sdo.ClaimsTrack;
import uk.gov.hmcts.reform.civil.enums.sdo.FastTrack;
import uk.gov.hmcts.reform.civil.enums.sdo.OrderType;
import uk.gov.hmcts.reform.civil.enums.sdo.SmallTrack;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.handler.callback.user.CreateSDOCallbackHandler;
import uk.gov.hmcts.reform.civil.handler.callback.user.createsdocallbackhandlertests.CreateSDOCallbackHandlerTestConfig;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.helpers.LocationHelper;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.CategoryService;
import uk.gov.hmcts.reform.civil.service.DeadlinesCalculator;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.docmosis.sdo.SdoGeneratorService;
import uk.gov.hmcts.reform.civil.service.referencedata.LocationReferenceDataService;
import uk.gov.hmcts.reform.civil.utils.AssignCategoryId;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.MID;

@SpringBootTest(classes = {
    CreateSDOCallbackHandler.class,
    JacksonAutoConfiguration.class,
    CaseDetailsConverter.class,
    ClaimUrlsConfiguration.class,
    MockDatabaseConfiguration.class,
    WorkingDayIndicator.class,
    DeadlinesCalculator.class,
    ValidationAutoConfiguration.class,
    LocationHelper.class,
    AssignCategoryId.class,
    CreateSDOCallbackHandlerTestConfig.class},
    properties = {"reference.database.enabled=false"})
class CreateSDOMidEventSetOrderDetailsFlagsTest extends BaseCallbackHandlerTest {

    @MockBean
    private FeatureToggleService featureToggleService;

    @MockBean
    private SdoGeneratorService sdoGeneratorService;

    @MockBean
    private PublicHolidaysCollection publicHolidaysCollection;

    @MockBean
    private NonWorkingDaysCollection nonWorkingDaysCollection;

    @MockBean
    private CategoryService categoryService;

    @MockBean
    protected LocationReferenceDataService locationRefDataService;

    @Autowired
    private CreateSDOCallbackHandler handler;

    private static final String PAGE_ID = "order-details-navigation";

    private AboutToStartOrSubmitCallbackResponse executeHandler(CaseData caseData) {
        CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);
        return (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
    }

    @Test
    void smallClaimsFlagAndFastTrackFlagSetToNo() {
        CaseData caseData = CaseDataBuilder.builder().atStateClaimDraft().build();
        AboutToStartOrSubmitCallbackResponse response = executeHandler(caseData);
        assertThat(response.getData()).extracting("setSmallClaimsFlag").isEqualTo("No");
        assertThat(response.getData()).extracting("setFastTrackFlag").isEqualTo("No");
    }

    @Test
    void smallClaimsFlagSetToYesPathOne() {
        CaseData caseData = CaseDataBuilder.builder().atStateClaimDraft().build().toBuilder()
                .drawDirectionsOrderRequired(YesOrNo.NO)
                .claimsTrack(ClaimsTrack.smallClaimsTrack)
                .build();
        AboutToStartOrSubmitCallbackResponse response = executeHandler(caseData);
        assertThat(response.getData()).extracting("setSmallClaimsFlag").isEqualTo("Yes");
        assertThat(response.getData()).extracting("setFastTrackFlag").isEqualTo("No");
    }

    @Test
    void smallClaimsFlagSetToYesPathTwo() {
        CaseData caseData = CaseDataBuilder.builder().atStateClaimDraft().build().toBuilder()
                .drawDirectionsOrderRequired(YesOrNo.YES)
                .drawDirectionsOrderSmallClaims(YesOrNo.YES)
                .build();
        AboutToStartOrSubmitCallbackResponse response = executeHandler(caseData);
        assertThat(response.getData()).extracting("setSmallClaimsFlag").isEqualTo("Yes");
        assertThat(response.getData()).extracting("setFastTrackFlag").isEqualTo("No");
    }

    @Test
    void fastTrackFlagSetToYesPathOne() {
        CaseData caseData = CaseDataBuilder.builder().atStateClaimDraft().build().toBuilder()
                .drawDirectionsOrderRequired(YesOrNo.NO)
                .claimsTrack(ClaimsTrack.fastTrack)
                .fastClaims(List.of(FastTrack.fastClaimBuildingDispute))
                .build();
        AboutToStartOrSubmitCallbackResponse response = executeHandler(caseData);
        assertThat(response.getData()).extracting("setSmallClaimsFlag").isEqualTo("No");
        assertThat(response.getData()).extracting("setFastTrackFlag").isEqualTo("Yes");
    }

    @Test
    void fastTrackFlagSetToYesPathTwo() {
        CaseData caseData = CaseDataBuilder.builder().atStateClaimDraft().build().toBuilder()
                .drawDirectionsOrderRequired(YesOrNo.YES)
                .drawDirectionsOrderSmallClaims(YesOrNo.NO)
                .orderType(OrderType.DECIDE_DAMAGES)
                .claimsTrack(ClaimsTrack.fastTrack)
                .fastClaims(List.of(FastTrack.fastClaimBuildingDispute))
                .build();
        AboutToStartOrSubmitCallbackResponse response = executeHandler(caseData);
        assertThat(response.getData()).extracting("setSmallClaimsFlag").isEqualTo("No");
        assertThat(response.getData()).extracting("setFastTrackFlag").isEqualTo("Yes");
    }

    @Test
    void fastTRackSdoR2NihlPathTwo() {
        when(featureToggleService.isSdoR2Enabled()).thenReturn(true);
        List<FastTrack> fastTrackList = new ArrayList<>();
        fastTrackList.add(FastTrack.fastClaimBuildingDispute);
        fastTrackList.add(FastTrack.fastClaimNoiseInducedHearingLoss);
        CaseData caseData = CaseDataBuilder.builder().atStateClaimDraft().build().toBuilder()
                .drawDirectionsOrderRequired(YesOrNo.YES)
                .drawDirectionsOrderSmallClaims(YesOrNo.NO)
                .orderType(OrderType.DECIDE_DAMAGES)
                .claimsTrack(ClaimsTrack.fastTrack)
                .trialAdditionalDirectionsForFastTrack(fastTrackList)
                .build();
        AboutToStartOrSubmitCallbackResponse response = executeHandler(caseData);
        assertThat(response.getData()).extracting("setSmallClaimsFlag").isEqualTo("No");
        assertThat(response.getData()).extracting("setFastTrackFlag").isEqualTo("Yes");
        assertThat(response.getData()).extracting("isSdoR2NewScreen").isEqualTo("Yes");
    }

    @Test
    void fastTrackFlagSetToYesNihlPathOne() {
        when(featureToggleService.isSdoR2Enabled()).thenReturn(true);
        List<FastTrack> fastTrackList = new ArrayList<>();
        fastTrackList.add(FastTrack.fastClaimBuildingDispute);
        fastTrackList.add(FastTrack.fastClaimNoiseInducedHearingLoss);
        CaseData caseData = CaseDataBuilder.builder().atStateClaimDraft().build().toBuilder()
                .drawDirectionsOrderRequired(YesOrNo.NO)
                .claimsTrack(ClaimsTrack.fastTrack)
                .fastClaims(fastTrackList)
                .build();
        AboutToStartOrSubmitCallbackResponse response = executeHandler(caseData);
        assertThat(response.getData()).extracting("setSmallClaimsFlag").isEqualTo("No");
        assertThat(response.getData()).extracting("setFastTrackFlag").isEqualTo("Yes");
        assertThat(response.getData()).extracting("isSdoR2NewScreen").isEqualTo("Yes");
    }

    @Test
    void smallClaimsSdoR2FlagSetToYesPathOne() {
        when(featureToggleService.isSdoR2Enabled()).thenReturn(true);
        CaseData caseData = CaseDataBuilder.builder().atStateClaimDraft().build().toBuilder()
                .drawDirectionsOrderRequired(YesOrNo.NO)
                .claimsTrack(ClaimsTrack.smallClaimsTrack)
                .smallClaims(List.of(SmallTrack.smallClaimDisputeResolutionHearing))
                .build();
        AboutToStartOrSubmitCallbackResponse response = executeHandler(caseData);
        assertThat(response.getData()).extracting("setSmallClaimsFlag").isEqualTo("Yes");
        assertThat(response.getData()).extracting("setFastTrackFlag").isEqualTo("No");
        assertThat(response.getData()).extracting("isSdoR2NewScreen").isEqualTo("Yes");
    }

    @Test
    void smallClaimsSdoR2FlagSetToYesPathTwo() {
        when(featureToggleService.isSdoR2Enabled()).thenReturn(true);
        CaseData caseData = CaseDataBuilder.builder().atStateClaimDraft().build().toBuilder()
                .drawDirectionsOrderRequired(YesOrNo.YES)
                .drawDirectionsOrderSmallClaims(YesOrNo.YES)
                .drawDirectionsOrderSmallClaimsAdditionalDirections(List.of(SmallTrack.smallClaimDisputeResolutionHearing))
                .build();
        AboutToStartOrSubmitCallbackResponse response = executeHandler(caseData);
        assertThat(response.getData()).extracting("setSmallClaimsFlag").isEqualTo("Yes");
        assertThat(response.getData()).extracting("setFastTrackFlag").isEqualTo("No");
        assertThat(response.getData()).extracting("isSdoR2NewScreen").isEqualTo("Yes");
    }
}
