package uk.gov.hmcts.reform.civil.handler.callback.user.createsdocallbackhandlertests.integrationtests;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.autoconfigure.validation.ValidationAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.civil.bankholidays.WorkingDayIndicator;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.config.ClaimUrlsConfiguration;
import uk.gov.hmcts.reform.civil.config.MockDatabaseConfiguration;
import uk.gov.hmcts.reform.civil.enums.AllocatedTrack;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.enums.sdo.ClaimsTrack;
import uk.gov.hmcts.reform.civil.enums.sdo.DisposalHearingMethod;
import uk.gov.hmcts.reform.civil.enums.sdo.OrderDetailsPagesSectionsToggle;
import uk.gov.hmcts.reform.civil.enums.sdo.OrderType;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.handler.callback.user.CreateSDOCallbackHandler;
import uk.gov.hmcts.reform.civil.handler.callback.user.createsdocallbackhandler.SetOrderDetailsFlags;
import uk.gov.hmcts.reform.civil.handler.callback.user.createsdocallbackhandler.generatesdoorder.GenerateSdoOrder;
import uk.gov.hmcts.reform.civil.handler.callback.user.createsdocallbackhandler.prepopulateorderdetailspages.PrePopulateOrderDetailsPages;
import uk.gov.hmcts.reform.civil.handler.callback.user.createsdocallbackhandler.submitsdo.SubmitSDO;
import uk.gov.hmcts.reform.civil.handler.callback.user.createsdocallbackhandlertests.CreateSDOCallbackHandlerTestConfig;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.helpers.LocationHelper;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.common.DynamicListElement;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.CaseLocationCivil;
import uk.gov.hmcts.reform.civil.referencedata.model.LocationRefData;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.CategoryService;
import uk.gov.hmcts.reform.civil.service.DeadlinesCalculator;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.camunda.UpdateWaCourtLocationsService;
import uk.gov.hmcts.reform.civil.service.docmosis.sdo.SdoGeneratorService;
import uk.gov.hmcts.reform.civil.service.referencedata.LocationReferenceDataService;
import uk.gov.hmcts.reform.civil.utils.AssignCategoryId;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CREATE_SDO;
import static uk.gov.hmcts.reform.civil.enums.CaseCategory.SPEC_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.CaseCategory.UNSPEC_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;

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
public class CreateSDOCallbackHandlerIntegrationTest extends BaseCallbackHandlerTest {

    public static final String REFERENCE_NUMBER = "000DC001";

    private static final DynamicList DEFAULT_OPTIONS = DynamicList.builder()
            .listItems(List.of(
                    DynamicListElement.builder().code("00001").label("court 1 - 1 address - Y01 7RB").build(),
                    DynamicListElement.builder().code("00002").label("court 2 - 2 address - Y02 7RB").build(),
                    DynamicListElement.builder().code("00003").label("court 3 - 3 address - Y03 7RB").build()
            )).build();

    @MockBean
    private FeatureToggleService featureToggleService;

    @Autowired
    private CreateSDOCallbackHandler handler;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    protected LocationReferenceDataService locationRefDataService;

    @MockBean
    private WorkingDayIndicator workingDayIndicator;

    @MockBean
    private DeadlinesCalculator deadlinesCalculator;

    @MockBean
    private SdoGeneratorService sdoGeneratorService;

    @MockBean
    private CategoryService categoryService;

    @Mock
    private GenerateSdoOrder generateSdoOrder;

    @Mock
    private PrePopulateOrderDetailsPages prePopulateOrderDetailsPages;

    @Mock
    private SubmitSDO submitSDO;

    @Mock
    private SetOrderDetailsFlags setOrderDetailsFlags;

    @MockBean
    private UpdateWaCourtLocationsService updateWaCourtLocationsService;

    private DynamicListElement getSelectedCourt() {
        return DynamicListElement.builder()
                .code("00002")
                .label("court 2 - 2 address - Y02 7RB")
                .build();
    }

    private CaseData buildCaseData(CaseDataBuilder builder) {
        return builder.atStateClaimDraft().build().toBuilder()
                .caseManagementLocation(CaseLocationCivil.builder().baseLocation(getSelectedCourt().getCode()).build())
                .disposalHearingMethod(DisposalHearingMethod.disposalHearingMethodInPerson)
                .disposalHearingMethodInPerson(DEFAULT_OPTIONS.toBuilder().value(getSelectedCourt()).build())
                .fastTrackMethodInPerson(DEFAULT_OPTIONS)
                .smallClaimsMethodInPerson(DEFAULT_OPTIONS)
                .disposalHearingMethodInPerson(DEFAULT_OPTIONS.toBuilder().value(getSelectedCourt()).build())
                .disposalHearingMethodToggle(Collections.singletonList(OrderDetailsPagesSectionsToggle.SHOW))
                .orderType(OrderType.DISPOSAL)
                .build();
    }

    private CallbackParams createParams(CaseData caseData) {
        return callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
    }

    private AboutToStartOrSubmitCallbackResponse handleCallback(CallbackParams params) {
        return (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
    }

    private void setupLocationMock(String code, boolean isWhiteListed, CallbackParams params) {
        when(featureToggleService.isLocationWhiteListedForCaseProgression(code))
                .thenReturn(isWhiteListed);
        when(locationRefDataService.getLocationMatchingLabel(eq(code), eq(params.getParams().get(
                CallbackParams.Params.BEARER_TOKEN).toString())))
                .thenReturn(Optional.of(LocationRefData.builder()
                        .regionId("region id")
                        .epimmsId("epimms id")
                        .siteName("site name")
                        .build()));
    }

    @ParameterizedTest
    @CsvSource({"true", "false"})
    void shouldSetEarlyAdoptersFlagToFalse_WhenLiP(Boolean isLocationWhiteListed) {
        CaseData caseData = buildCaseData(CaseDataBuilder.builder());
        caseData = caseData.toBuilder()
                .respondent1Represented(NO)
                .build();
        CallbackParams params = createParams(caseData);
        setupLocationMock(getSelectedCourt().getCode(), isLocationWhiteListed, params);

        AboutToStartOrSubmitCallbackResponse response = handleCallback(params);
        CaseData responseCaseData = objectMapper.convertValue(response.getData(), CaseData.class);

        assertThat(responseCaseData.getEaCourtLocation()).isEqualTo(NO);
    }

    @ParameterizedTest
    @CsvSource({"true", "false"})
    void shouldPopulateHmcEarlyAdoptersFlag_whenHmcIsEnabled(Boolean isLocationWhiteListed) {
        CaseData caseData = buildCaseData(CaseDataBuilder.builder());
        CallbackParams params = createParams(caseData);
        setupLocationMock(getSelectedCourt().getCode(), isLocationWhiteListed, params);

        AboutToStartOrSubmitCallbackResponse response = handleCallback(params);
        CaseData responseCaseData = objectMapper.convertValue(response.getData(), CaseData.class);

        assertThat(responseCaseData.getHmcEaCourtLocation()).isEqualTo(isLocationWhiteListed ? YES : NO);
    }

    @ParameterizedTest
    @CsvSource({"true, NO, NO, YES", "false, NO, NO, NO", "true, YES, NO, YES"
    })
    void shouldSetEaCourtLocationBasedOnConditions(boolean isLocationWhiteListed, YesOrNo applicant1Represented, YesOrNo respondent1Represented, YesOrNo expectedEaCourtLocation) {
        CaseData caseData = buildCaseData(CaseDataBuilder.builder())
                .toBuilder()
                .respondent1Represented(respondent1Represented)
                .applicant1Represented(applicant1Represented)
                .build();
        CallbackParams params = createParams(caseData);
        when(featureToggleService.isCaseProgressionEnabledAndLocationWhiteListed(any())).thenReturn(isLocationWhiteListed);

        AboutToStartOrSubmitCallbackResponse response = handleCallback(params);
        CaseData responseCaseData = objectMapper.convertValue(response.getData(), CaseData.class);

        assertEquals(expectedEaCourtLocation, responseCaseData.getEaCourtLocation());
    }

    @Test
    void shouldNotPopulateHmcEarlyAdoptersFlag_whenLiP() {
        CaseData caseData = buildCaseData(CaseDataBuilder.builder());
        CallbackParams params = createParams(caseData.toBuilder()
                .applicant1Represented(YesOrNo.NO)
                .build());
        setupLocationMock(getSelectedCourt().getCode(), true, params);

        AboutToStartOrSubmitCallbackResponse response = handleCallback(params);
        CaseData responseCaseData = objectMapper.convertValue(response.getData(), CaseData.class);

        assertThat(responseCaseData.getHmcEaCourtLocation()).isNull();

        params = createParams(caseData.toBuilder()
                .respondent1Represented(YesOrNo.NO)
                .build());

        response = handleCallback(params);
        responseCaseData = objectMapper.convertValue(response.getData(), CaseData.class);

        assertThat(responseCaseData.getHmcEaCourtLocation()).isNull();
    }

    @Test
    void shouldNotCallUpdateWaCourtLocationsServiceWhenNotPresent_AndMintiEnabled() {
        when(featureToggleService.isMultiOrIntermediateTrackEnabled(any())).thenReturn(true);

        CreateSDOCallbackHandler localHandler =
                new CreateSDOCallbackHandler(
                        generateSdoOrder,
                        prePopulateOrderDetailsPages,
                        new SubmitSDO(objectMapper, List.of(), featureToggleService, Optional.empty()),
                        setOrderDetailsFlags
                );

        CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimDraft()
                .build()
                .toBuilder()
                .caseManagementLocation(CaseLocationCivil.builder().baseLocation("123456").build())
                .build();

        CallbackParams params = callbackParamsOf(caseData, CREATE_SDO, ABOUT_TO_SUBMIT);

        AboutToStartOrSubmitCallbackResponse response =
                (AboutToStartOrSubmitCallbackResponse) localHandler.handle(params);

        verifyNoInteractions(updateWaCourtLocationsService);

        assertThat(response.getData()).doesNotContainKey("sdoOrderDocument");
        assertThat(response.getData()).containsEntry("businessProcess", Map.of(
                "camundaEvent", CREATE_SDO.name(),
                "status", "READY",
                "readyOn", ((Map<?, ?>) response.getData().get("businessProcess")).get("readyOn")
        ));
    }

    @Test
    void shouldCallUpdateWaCourtLocationsServiceWhenPresent_AndMintiEnabled() {
        when(featureToggleService.isMultiOrIntermediateTrackEnabled(any())).thenReturn(true);

        CaseData caseData = buildCaseData(CaseDataBuilder.builder())
                .toBuilder()
                .caseManagementLocation(CaseLocationCivil.builder().baseLocation("123456").build())
                .build();
        CallbackParams params = createParams(caseData);

        AboutToStartOrSubmitCallbackResponse response = handleCallback(params);
        CaseData responseCaseData = objectMapper.convertValue(response.getData(), CaseData.class);

        verify(updateWaCourtLocationsService).updateCourtListingWALocations(any(), any());

        assertThat(responseCaseData.getCaseManagementLocation().getBaseLocation()).isEqualTo("123456");
        assertThat(responseCaseData.getBusinessProcess().getCamundaEvent()).isEqualTo(CREATE_SDO.name());
        assertThat(responseCaseData.getBusinessProcess().getStatus().toString()).isEqualTo("READY");
    }

    @ParameterizedTest
    @MethodSource("testDataUnspec")
    void whenClaimUnspecAndJudgeSelects_changeTrackOrMaintainAllocatedTrack(CaseData caseData, AllocatedTrack expectedAllocatedTrack) {
        CallbackParams params = createParams(caseData);
        AboutToStartOrSubmitCallbackResponse response = handleCallback(params);

        assertThat(response.getData()).containsEntry("allocatedTrack", expectedAllocatedTrack.name());
    }

    static Stream<Arguments> testDataUnspec() {
        DynamicListElement selectedCourt = DynamicListElement.builder()
                .code("00002").label("court 2 - 2 address - Y02 7RB").build();
        DynamicList options = DEFAULT_OPTIONS;
        return Stream.of(
                Arguments.of(
                        CaseDataBuilder.builder().atStateClaimDraft().build().toBuilder()
                                .caseAccessCategory(UNSPEC_CLAIM)
                                .allocatedTrack(AllocatedTrack.SMALL_CLAIM)
                                .fastTrackMethodInPerson(options.toBuilder().value(selectedCourt).build())
                                .drawDirectionsOrderRequired(NO)
                                .claimsTrack(ClaimsTrack.fastTrack)
                                .caseManagementLocation(CaseLocationCivil.builder().baseLocation("00000").build())
                                .build(),
                        AllocatedTrack.FAST_CLAIM
                ),
                Arguments.of(
                        CaseDataBuilder.builder().atStateClaimDraft().build().toBuilder()
                                .caseAccessCategory(UNSPEC_CLAIM)
                                .allocatedTrack(AllocatedTrack.SMALL_CLAIM)
                                .fastTrackMethodInPerson(options.toBuilder().value(selectedCourt).build())
                                .drawDirectionsOrderRequired(YES)
                                .drawDirectionsOrderSmallClaims(NO)
                                .orderType(OrderType.DECIDE_DAMAGES)
                                .caseManagementLocation(CaseLocationCivil.builder().baseLocation("00000").build())
                                .build(),
                        AllocatedTrack.FAST_CLAIM
                ),
                Arguments.of(
                        CaseDataBuilder.builder().atStateClaimDraft().build().toBuilder()
                                .caseAccessCategory(UNSPEC_CLAIM)
                                .allocatedTrack(AllocatedTrack.FAST_CLAIM)
                                .disposalHearingMethodInPerson(options.toBuilder().value(selectedCourt).build())
                                .drawDirectionsOrderRequired(YES)
                                .drawDirectionsOrderSmallClaims(NO)
                                .orderType(OrderType.DISPOSAL)
                                .caseManagementLocation(CaseLocationCivil.builder().baseLocation("00000").build())
                                .build(),
                        AllocatedTrack.FAST_CLAIM
                ),
                Arguments.of(
                        CaseDataBuilder.builder().atStateClaimDraft().build().toBuilder()
                                .caseAccessCategory(UNSPEC_CLAIM)
                                .allocatedTrack(AllocatedTrack.FAST_CLAIM)
                                .smallClaimsMethodInPerson(options.toBuilder().value(selectedCourt).build())
                                .drawDirectionsOrderRequired(NO)
                                .claimsTrack(ClaimsTrack.smallClaimsTrack)
                                .caseManagementLocation(CaseLocationCivil.builder().baseLocation("00000").build())
                                .build(),
                        AllocatedTrack.SMALL_CLAIM
                ),
                Arguments.of(
                        CaseDataBuilder.builder().atStateClaimDraft().build().toBuilder()
                                .caseAccessCategory(UNSPEC_CLAIM)
                                .allocatedTrack(AllocatedTrack.FAST_CLAIM)
                                .smallClaimsMethodInPerson(options.toBuilder().value(selectedCourt).build())
                                .drawDirectionsOrderRequired(YES)
                                .drawDirectionsOrderSmallClaims(YES)
                                .orderType(OrderType.DECIDE_DAMAGES)
                                .caseManagementLocation(CaseLocationCivil.builder().baseLocation("00000").build())
                                .build(),
                        AllocatedTrack.SMALL_CLAIM
                ),
                Arguments.of(
                        CaseDataBuilder.builder().atStateClaimDraft().build().toBuilder()
                                .caseAccessCategory(UNSPEC_CLAIM)
                                .allocatedTrack(AllocatedTrack.SMALL_CLAIM)
                                .disposalHearingMethodInPerson(options.toBuilder().value(selectedCourt).build())
                                .drawDirectionsOrderRequired(YES)
                                .drawDirectionsOrderSmallClaims(NO)
                                .orderType(OrderType.DISPOSAL)
                                .caseManagementLocation(CaseLocationCivil.builder().baseLocation("00000").build())
                                .build(),
                        AllocatedTrack.SMALL_CLAIM
                )
        );
    }

    @ParameterizedTest
    @MethodSource("testDataSpec")
    void whenClaimSpecAndJudgeSelects_changeTrackOrMaintainClaimResponseTrack(CaseData caseData, String expectedClaimResponseTrack) {
        CallbackParams params = createParams(caseData);
        AboutToStartOrSubmitCallbackResponse response = handleCallback(params);

        assertThat(response.getData()).containsEntry("responseClaimTrack", expectedClaimResponseTrack);
    }

    static Stream<Arguments> testDataSpec() {
        DynamicListElement selectedCourt = DynamicListElement.builder()
                .code("00002").label("court 2 - 2 address - Y02 7RB").build();
        DynamicList options = DEFAULT_OPTIONS;
        return Stream.of(
                Arguments.of(
                        CaseDataBuilder.builder().atStateClaimDraft().build().toBuilder()
                                .caseAccessCategory(SPEC_CLAIM)
                                .responseClaimTrack("SMALL_CLAIM")
                                .fastTrackMethodInPerson(options.toBuilder().value(selectedCourt).build())
                                .drawDirectionsOrderRequired(NO)
                                .claimsTrack(ClaimsTrack.fastTrack)
                                .caseManagementLocation(CaseLocationCivil.builder().baseLocation("00000").build())
                                .build(),
                        "FAST_CLAIM"
                ),
                Arguments.of(
                        CaseDataBuilder.builder().atStateClaimDraft().build().toBuilder()
                                .caseAccessCategory(SPEC_CLAIM)
                                .responseClaimTrack("SMALL_CLAIM")
                                .fastTrackMethodInPerson(options.toBuilder().value(selectedCourt).build())
                                .drawDirectionsOrderRequired(YES)
                                .drawDirectionsOrderSmallClaims(NO)
                                .orderType(OrderType.DECIDE_DAMAGES)
                                .caseManagementLocation(CaseLocationCivil.builder().baseLocation("00000").build())
                                .build(),
                        "FAST_CLAIM"
                ),
                Arguments.of(
                        CaseDataBuilder.builder().atStateClaimDraft().build().toBuilder()
                                .caseAccessCategory(SPEC_CLAIM)
                                .responseClaimTrack("SMALL_CLAIM")
                                .drawDirectionsOrderRequired(YES)
                                .drawDirectionsOrderSmallClaims(NO)
                                .caseManagementLocation(CaseLocationCivil.builder().baseLocation("00000").build())
                                .orderType(OrderType.DECIDE_DAMAGES)
                                .build(),
                        "FAST_CLAIM"
                ),
                Arguments.of(
                        CaseDataBuilder.builder().atStateClaimDraft().build().toBuilder()
                                .caseAccessCategory(SPEC_CLAIM)
                                .responseClaimTrack("FAST_CLAIM")
                                .disposalHearingMethodInPerson(options.toBuilder().value(selectedCourt).build())
                                .drawDirectionsOrderRequired(YES)
                                .drawDirectionsOrderSmallClaims(NO)
                                .orderType(OrderType.DISPOSAL)
                                .caseManagementLocation(CaseLocationCivil.builder().baseLocation("00000").build())
                                .build(),
                        "FAST_CLAIM"
                ),
                Arguments.of(
                        CaseDataBuilder.builder().atStateClaimDraft().build().toBuilder()
                                .caseAccessCategory(SPEC_CLAIM)
                                .responseClaimTrack("FAST_CLAIM")
                                .drawDirectionsOrderRequired(YES)
                                .drawDirectionsOrderSmallClaims(NO)
                                .orderType(OrderType.DISPOSAL)
                                .caseManagementLocation(CaseLocationCivil.builder().baseLocation("00000").build())
                                .build(),
                        "FAST_CLAIM"
                ),
                Arguments.of(
                        CaseDataBuilder.builder().atStateClaimDraft().build().toBuilder()
                                .caseAccessCategory(SPEC_CLAIM)
                                .responseClaimTrack("FAST_CLAIM")
                                .smallClaimsMethodInPerson(options.toBuilder().value(selectedCourt).build())
                                .drawDirectionsOrderRequired(NO)
                                .claimsTrack(ClaimsTrack.smallClaimsTrack)
                                .caseManagementLocation(CaseLocationCivil.builder().baseLocation("00000").build())
                                .build(),
                        "SMALL_CLAIM"
                ),
                Arguments.of(
                        CaseDataBuilder.builder().atStateClaimDraft().build().toBuilder()
                                .caseAccessCategory(SPEC_CLAIM)
                                .responseClaimTrack("FAST_CLAIM")
                                .smallClaimsMethodInPerson(options.toBuilder().value(selectedCourt).build())
                                .drawDirectionsOrderRequired(YES)
                                .drawDirectionsOrderSmallClaims(YES)
                                .orderType(OrderType.DECIDE_DAMAGES)
                                .caseManagementLocation(CaseLocationCivil.builder().baseLocation("00000").build())
                                .build(),
                        "SMALL_CLAIM"
                ),
                Arguments.of(
                        CaseDataBuilder.builder().atStateClaimDraft().build().toBuilder()
                                .caseAccessCategory(SPEC_CLAIM)
                                .responseClaimTrack("FAST_CLAIM")
                                .drawDirectionsOrderRequired(YES)
                                .drawDirectionsOrderSmallClaims(YES)
                                .orderType(OrderType.DECIDE_DAMAGES)
                                .caseManagementLocation(CaseLocationCivil.builder().baseLocation("00000").build())
                                .build(),
                        "SMALL_CLAIM"
                ),
                Arguments.of(
                        CaseDataBuilder.builder().atStateClaimDraft().build().toBuilder()
                                .caseAccessCategory(SPEC_CLAIM)
                                .responseClaimTrack("SMALL_CLAIM")
                                .disposalHearingMethodInPerson(options.toBuilder().value(selectedCourt).build())
                                .drawDirectionsOrderRequired(YES)
                                .drawDirectionsOrderSmallClaims(NO)
                                .caseManagementLocation(CaseLocationCivil.builder().baseLocation("00000").build())
                                .orderType(OrderType.DISPOSAL)
                                .build(),
                        "SMALL_CLAIM"
                )
        );
    }

    @Test
    void handleEventsReturnsTheExpectedCallbackEvent() {
        assertThat(handler.handledEvents()).contains(CREATE_SDO);
    }
}
