package uk.gov.hmcts.reform.civil.handler.callback.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.autoconfigure.validation.ValidationAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CallbackVersion;
import uk.gov.hmcts.reform.civil.config.ClaimUrlsConfiguration;
import uk.gov.hmcts.reform.civil.config.MockDatabaseConfiguration;
import uk.gov.hmcts.reform.civil.crd.model.Category;
import uk.gov.hmcts.reform.civil.crd.model.CategorySearchResult;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.enums.sdo.ClaimsTrack;
import uk.gov.hmcts.reform.civil.enums.sdo.DisposalHearingMethod;
import uk.gov.hmcts.reform.civil.enums.sdo.FastTrackMethod;
import uk.gov.hmcts.reform.civil.enums.sdo.OrderDetailsPagesSectionsToggle;
import uk.gov.hmcts.reform.civil.enums.sdo.OrderType;
import uk.gov.hmcts.reform.civil.enums.sdo.SmallClaimsMethod;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.helpers.DateFormatHelper;
import uk.gov.hmcts.reform.civil.helpers.LocationHelper;
import uk.gov.hmcts.reform.civil.model.common.DynamicListElement;
import uk.gov.hmcts.reform.civil.service.CategoryService;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.CaseLocationCivil;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;
import uk.gov.hmcts.reform.civil.model.dq.Applicant1DQ;
import uk.gov.hmcts.reform.civil.model.dq.RequestedCourt;
import uk.gov.hmcts.reform.civil.referencedata.model.LocationRefData;
import uk.gov.hmcts.reform.civil.model.sdo.JudgementSum;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.sampledata.LocationRefSampleDataBuilder;
import uk.gov.hmcts.reform.civil.service.DeadlinesCalculator;
import uk.gov.hmcts.reform.civil.service.Time;
import uk.gov.hmcts.reform.civil.bankholidays.NonWorkingDaysCollection;
import uk.gov.hmcts.reform.civil.service.docmosis.sdo.SdoGeneratorService;
import uk.gov.hmcts.reform.civil.referencedata.LocationRefDataService;
import uk.gov.hmcts.reform.civil.utils.AssignCategoryId;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.MID;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.SUBMITTED;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CREATE_SDO;
import static uk.gov.hmcts.reform.civil.enums.CaseCategory.SPEC_CLAIM;
import static uk.gov.hmcts.reform.civil.handler.callback.user.CreateSDOCallbackHandler.CONFIRMATION_HEADER;
import static uk.gov.hmcts.reform.civil.handler.callback.user.CreateSDOCallbackHandler.CONFIRMATION_SUMMARY_1v1;
import static uk.gov.hmcts.reform.civil.handler.callback.user.CreateSDOCallbackHandler.CONFIRMATION_SUMMARY_1v2;
import static uk.gov.hmcts.reform.civil.handler.callback.user.CreateSDOCallbackHandler.CONFIRMATION_SUMMARY_2v1;
import static uk.gov.hmcts.reform.civil.handler.callback.user.CreateSDOCallbackHandler.FEEDBACK_LINK;

@SpringBootTest(classes = {
    CreateSDOCallbackHandler.class,
    JacksonAutoConfiguration.class,
    CaseDetailsConverter.class,
    ClaimUrlsConfiguration.class,
    MockDatabaseConfiguration.class,
    DeadlinesCalculator.class,
    ValidationAutoConfiguration.class,
    LocationHelper.class,
    AssignCategoryId.class},
    properties = {"reference.database.enabled=false"})
public class CreateSDOCallbackHandlerTest extends BaseCallbackHandlerTest {

    public static final String REFERENCE_NUMBER = "000DC001";

    @MockBean
    private Time time;

    @MockBean
    private IdamClient idamClient;

    @MockBean
    private FeatureToggleService featureToggleService;

    @Autowired
    private CreateSDOCallbackHandler handler;

    @Autowired
    private AssignCategoryId assignCategoryId;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    protected LocationRefDataService locationRefDataService;

    @MockBean
    private DeadlinesCalculator deadlinesCalculator;

    @MockBean
    private SdoGeneratorService sdoGeneratorService;

    @MockBean
    private NonWorkingDaysCollection nonWorkingDaysCollection;

    @MockBean
    private CategoryService categoryService;

    @Nested
    class AboutToStartCallback extends LocationRefSampleDataBuilder {
        @BeforeEach
        void setup() {
            given(locationRefDataService.getCourtLocationsForDefaultJudgments(any()))
                .willReturn(getSampleCourLocationsRefObject());
            when(deadlinesCalculator.plusWorkingDays(LocalDate.now(), 5))
                .thenReturn(LocalDate.now().plusDays(5));
            when(deadlinesCalculator.getOrderSetAsideOrVariedApplicationDeadline(ArgumentMatchers.any(LocalDateTime.class)))
                .thenReturn(LocalDate.now().plusDays(7));
        }

        @Test
        void shouldGenerateDynamicListsCorrectly() {
            Category category = Category.builder().categoryKey("HearingChannel").key("INTER").valueEn("In Person").activeFlag("Y").build();
            CategorySearchResult categorySearchResult = CategorySearchResult.builder().categories(List.of(category)).build();
            when(categoryService.findCategoryByCategoryIdAndServiceId(any(), any(), any())).thenReturn(Optional.of(categorySearchResult));

            CaseData caseData = CaseDataBuilder.builder().atStateClaimDraft()
                .atStateClaimIssuedDisposalHearingSDOInPersonHearing().build();

            CallbackParams params = callbackParamsOf(CallbackVersion.V_1, caseData, ABOUT_TO_START);
            CaseDocument order = CaseDocument.builder().documentLink(
                    Document.builder().documentUrl("url").build())
                .build();
            when(sdoGeneratorService.generate(any(), any())).thenReturn(order);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            CaseData responseCaseData = objectMapper.convertValue(response.getData(), CaseData.class);

            DynamicList hearingMethodValuesFastTrack = responseCaseData.getHearingMethodValuesFastTrack();
            DynamicList hearingMethodValuesDisposalHearing = responseCaseData.getHearingMethodValuesDisposalHearing();
            DynamicList hearingMethodValuesSmallClaims = responseCaseData.getHearingMethodValuesSmallClaims();

            List<String> hearingMethodValuesFastTrackActual = hearingMethodValuesFastTrack.getListItems().stream()
                .map(DynamicListElement::getLabel)
                .collect(Collectors.toList());

            List<String> hearingMethodValuesDisposalHearingActual = hearingMethodValuesDisposalHearing.getListItems().stream()
                .map(DynamicListElement::getLabel)
                .collect(Collectors.toList());

            List<String> hearingMethodValuesSmallClaimsActual = hearingMethodValuesSmallClaims.getListItems().stream()
                .map(DynamicListElement::getLabel)
                .collect(Collectors.toList());

            assertThat(hearingMethodValuesFastTrackActual).containsOnly("In Person");
            assertThat(hearingMethodValuesDisposalHearingActual).containsOnly("In Person");
            assertThat(hearingMethodValuesSmallClaimsActual).containsOnly("In Person");
        }
    }

    @Nested
    class AboutToSubmitCallback {

        private CallbackParams params;
        private CaseData caseData;
        private String userId;

        private static final String EMAIL = "example@email.com";
        private final LocalDateTime submittedDate = LocalDateTime.now();

        @BeforeEach
        void setup() {
            caseData = CaseDataBuilder.builder().atStateClaimDraft()
                .caseManagementLocation(CaseLocationCivil.builder().baseLocation("00000").build())
                .build();
            params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
            userId = UUID.randomUUID().toString();

            given(idamClient.getUserDetails(any()))
                .willReturn(UserDetails.builder().email(EMAIL).id(userId).build());

            given(time.now()).willReturn(submittedDate);

            given(featureToggleService.isLocationWhiteListedForCaseProgression(anyString())).willReturn(true);
        }

        @Test
        void shouldUpdateBusinessProcess_whenInvoked() {
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getData())
                .extracting("businessProcess")
                .extracting("camundaEvent", "status")
                .containsOnly(CREATE_SDO.name(), "READY");
        }
    }

    @Nested
    class AboutToSubmitCallbackVariableCase {

        private String userId;

        private static final String EMAIL = "example@email.com";
        private final LocalDateTime submittedDate = LocalDateTime.now();

        @BeforeEach
        void setup() {
            userId = UUID.randomUUID().toString();

            given(idamClient.getUserDetails(any()))
                .willReturn(UserDetails.builder().email(EMAIL).id(userId).build());

            given(time.now()).willReturn(submittedDate);
        }

        @Test
        void shouldUpdateCaseLocation_whenDisposal() {
            List<String> items = List.of("label 1", "label 2", "label 3");
            DynamicList options = DynamicList.fromList(items, Object::toString, items.get(0), false);
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDraft().build().toBuilder()
                .caseManagementLocation(CaseLocationCivil.builder().baseLocation("00000").build())
                .disposalHearingMethod(DisposalHearingMethod.disposalHearingMethodInPerson)
                .disposalHearingMethodInPerson(options)
                .disposalHearingMethodToggle(Collections.singletonList(OrderDetailsPagesSectionsToggle.SHOW))
                .build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
            LocationRefData matching = LocationRefData.builder()
                .regionId("region id")
                .epimmsId("epimms id")
                .siteName("site name")
                .build();
            Mockito.when(locationRefDataService.getLocationMatchingLabel("label 1", params.getParams().get(
                    CallbackParams.Params.BEARER_TOKEN).toString()))
                .thenReturn(Optional.of(matching));

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getData())
                .extracting("caseManagementLocation")
                .extracting("region", "baseLocation")
                .containsOnly(matching.getRegionId(), matching.getEpimmsId());
            assertThat(response.getData())
                .extracting("locationName")
                .isEqualTo(matching.getSiteName());
        }

        @Test
        void shouldUpdateCaseLocation_whenFastTrack() {
            List<String> items = List.of("label 1", "label 2", "label 3");
            DynamicList options = DynamicList.fromList(items, Object::toString, items.get(0), false);
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDraft().build().toBuilder()
                .caseManagementLocation(CaseLocationCivil.builder().baseLocation("00000").build())
                .fastTrackMethod(FastTrackMethod.fastTrackMethodInPerson)
                .fastTrackMethodInPerson(options)
                .claimsTrack(ClaimsTrack.fastTrack)
                .build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
            LocationRefData matching = LocationRefData.builder()
                .regionId("region id")
                .epimmsId("epimms id")
                .siteName("location name")
                .build();
            Mockito.when(locationRefDataService.getLocationMatchingLabel("label 1", params.getParams().get(
                    CallbackParams.Params.BEARER_TOKEN).toString()))
                .thenReturn(Optional.of(matching));

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getData())
                .extracting("caseManagementLocation")
                .extracting("region", "baseLocation")
                .containsOnly(matching.getRegionId(), matching.getEpimmsId());
            assertThat(response.getData())
                .extracting("locationName")
                .isEqualTo(matching.getSiteName());
        }

        @Test
        void shouldUpdateCaseLocation_whenSmallClaims() {
            List<String> items = List.of("label 1", "label 2", "label 3");
            DynamicList options = DynamicList.fromList(items, Object::toString, items.get(0), false);
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDraft().build().toBuilder()
                .caseManagementLocation(CaseLocationCivil.builder().baseLocation("00000").build())
                .smallClaimsMethod(SmallClaimsMethod.smallClaimsMethodInPerson)
                .smallClaimsMethodInPerson(options)
                .claimsTrack(ClaimsTrack.smallClaimsTrack)
                .build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
            LocationRefData matching = LocationRefData.builder()
                .regionId("region id")
                .epimmsId("epimms id")
                .siteName("location name")
                .build();
            Mockito.when(locationRefDataService.getLocationMatchingLabel("label 1", params.getParams().get(
                    CallbackParams.Params.BEARER_TOKEN).toString()))
                .thenReturn(Optional.of(matching));

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getData())
                .extracting("caseManagementLocation")
                .extracting("region", "baseLocation")
                .containsOnly(matching.getRegionId(), matching.getEpimmsId());
            assertThat(response.getData())
                .extracting("locationName")
                .isEqualTo(matching.getSiteName());
        }

        @Test
        void shouldUpdateCaseLocation_whenFastTrackAndOrderRequired() {
            List<String> items = List.of("label 1", "label 2", "label 3");
            DynamicList options = DynamicList.fromList(items, Object::toString, items.get(0), false);
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDraft().build().toBuilder()
                .caseManagementLocation(CaseLocationCivil.builder().baseLocation("00000").build())
                .fastTrackMethod(FastTrackMethod.fastTrackMethodInPerson)
                .fastTrackMethodInPerson(options)
                .drawDirectionsOrderRequired(YesOrNo.YES)
                .drawDirectionsOrderSmallClaims(YesOrNo.NO)
                .build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
            LocationRefData matching = LocationRefData.builder()
                .regionId("region id")
                .epimmsId("epimms id")
                .build();
            Mockito.when(locationRefDataService.getLocationMatchingLabel("label 1", params.getParams().get(
                    CallbackParams.Params.BEARER_TOKEN).toString()))
                .thenReturn(Optional.of(matching));

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getData())
                .extracting("caseManagementLocation")
                .extracting("region", "baseLocation")
                .containsOnly(matching.getRegionId(), matching.getEpimmsId());
        }

        @Test
        void shouldUpdateCaseLocation_whenSmallClaimsAndOrderRequired() {
            List<String> items = List.of("label 1", "label 2", "label 3");
            DynamicList options = DynamicList.fromList(items, Object::toString, items.get(0), false);
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDraft().build().toBuilder()
                .caseManagementLocation(CaseLocationCivil.builder().baseLocation("00000").build())
                .smallClaimsMethod(SmallClaimsMethod.smallClaimsMethodInPerson)
                .smallClaimsMethodInPerson(options)
                .drawDirectionsOrderRequired(YesOrNo.YES)
                .drawDirectionsOrderSmallClaims(YesOrNo.YES)
                .build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
            LocationRefData matching = LocationRefData.builder()
                .regionId("region id")
                .epimmsId("epimms id")
                .build();
            Mockito.when(locationRefDataService.getLocationMatchingLabel("label 1", params.getParams().get(
                    CallbackParams.Params.BEARER_TOKEN).toString()))
                .thenReturn(Optional.of(matching));

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getData())
                .extracting("caseManagementLocation")
                .extracting("region", "baseLocation")
                .containsOnly(matching.getRegionId(), matching.getEpimmsId());
        }

        @Test
        void shouldReturnNullDocument_whenInvokedAboutToSubmit() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDraft().build().toBuilder()
                .caseManagementLocation(CaseLocationCivil.builder().baseLocation("00000").build())
                .build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getData()).extracting("sdoOrderDocument").isNull();
        }
    }

    @Nested
    class MidEventDisposalHearingLocationRefDataCallback extends LocationRefSampleDataBuilder {

        @Test
        void shouldPrePopulateDisposalHearingPage() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDraft().build();
            given(locationRefDataService.getCourtLocationsForDefaultJudgments(any()))
                .willReturn(getSampleCourLocationsRefObject());
            when(deadlinesCalculator.plusWorkingDays(LocalDate.now(), 5))
                .thenReturn(LocalDate.now().plusDays(5));
            when(deadlinesCalculator.getOrderSetAsideOrVariedApplicationDeadline(ArgumentMatchers.any(LocalDateTime.class)))
                .thenReturn(LocalDate.now().plusDays(7));

            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_START);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            CaseData data = objectMapper.convertValue(response.getData(), CaseData.class);

            DynamicList dynamicList = getLocationDynamicListInPersonHearing(data);

            assertThat(dynamicList).isNotNull();
            assertThat(locationsFromDynamicList(dynamicList)).containsExactly(
                "Site 1 - Adr 1 - AAA 111",
                "Site 2 - Adr 2 - BBB 222",
                "Site 3 - Adr 3 - CCC 333"
            );
        }

        /**
         * spec claim, but no preferred court location.
         */
        @Test
        void shouldPrePopulateDisposalHearingPageSpec1() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDraft().build()
                .toBuilder()
                .caseAccessCategory(SPEC_CLAIM)
                .totalClaimAmount(BigDecimal.valueOf(10000))
                .build();
            given(locationRefDataService.getCourtLocationsForDefaultJudgments(any()))
                .willReturn(getSampleCourLocationsRefObject());
            when(deadlinesCalculator.plusWorkingDays(LocalDate.now(), 5))
                .thenReturn(LocalDate.now().plusDays(5));
            when(deadlinesCalculator.getOrderSetAsideOrVariedApplicationDeadline(ArgumentMatchers.any(LocalDateTime.class)))
                .thenReturn(LocalDate.now().plusDays(7));

            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_START);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            CaseData data = objectMapper.convertValue(response.getData(), CaseData.class);

            DynamicList dynamicList = getLocationDynamicListInPersonHearing(data);

            assertThat(dynamicList).isNotNull();
            assertThat(locationsFromDynamicList(dynamicList)).containsExactly(
                "Site 1 - Adr 1 - AAA 111",
                "Site 2 - Adr 2 - BBB 222",
                "Site 3 - Adr 3 - CCC 333"
            );
        }

        /**
         * spec claim, specified no preference for court.
         */
        @Test
        void shouldPrePopulateDisposalHearingPageSpec2() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDraft().build()
                .toBuilder()
                .caseAccessCategory(SPEC_CLAIM)
                .totalClaimAmount(BigDecimal.valueOf(10000))
                .applicant1DQ(Applicant1DQ.builder()
                                  .applicant1DQRequestedCourt(
                                      RequestedCourt.builder()
                                          .build()
                                  )
                                  .build())
                .build();
            given(locationRefDataService.getCourtLocationsForDefaultJudgments(any()))
                .willReturn(getSampleCourLocationsRefObject());
            when(deadlinesCalculator.plusWorkingDays(LocalDate.now(), 5))
                .thenReturn(LocalDate.now().plusDays(5));
            when(deadlinesCalculator.getOrderSetAsideOrVariedApplicationDeadline(ArgumentMatchers.any(LocalDateTime.class)))
                .thenReturn(LocalDate.now().plusDays(7));

            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_START);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            CaseData data = objectMapper.convertValue(response.getData(), CaseData.class);

            DynamicList dynamicList = getLocationDynamicListInPersonHearing(data);

            assertThat(dynamicList).isNotNull();
            assertThat(locationsFromDynamicList(dynamicList)).containsExactly(
                "Site 1 - Adr 1 - AAA 111",
                "Site 2 - Adr 2 - BBB 222",
                "Site 3 - Adr 3 - CCC 333"
            );
        }

        /**
         * spec claim, preferred court specified.
         */
        @Test
        void shouldPrePopulateDisposalHearingPageSpec3() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDraft().build()
                .toBuilder()
                .caseAccessCategory(SPEC_CLAIM)
                .totalClaimAmount(BigDecimal.valueOf(10000))
                .applicant1DQ(Applicant1DQ.builder()
                                  .applicant1DQRequestedCourt(
                                      RequestedCourt.builder()
                                          .responseCourtCode("court3")
                                          .caseLocation(
                                              CaseLocationCivil.builder()
                                                  .baseLocation("dummy base")
                                                  .region("dummy region")
                                                  .build()
                                          ).build()
                                  ).build()
                ).build();

            given(locationRefDataService.getCourtLocationsForDefaultJudgments(any()))
                .willReturn(getSampleCourLocationsRefObject());
            when(deadlinesCalculator.plusWorkingDays(LocalDate.now(), 5))
                .thenReturn(LocalDate.now().plusDays(5));
            when(deadlinesCalculator.getOrderSetAsideOrVariedApplicationDeadline(ArgumentMatchers.any(LocalDateTime.class)))
                .thenReturn(LocalDate.now().plusDays(7));

            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_START);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            CaseData data = objectMapper.convertValue(response.getData(), CaseData.class);

            DynamicList dynamicList = getLocationDynamicListInPersonHearing(data);

            assertThat(dynamicList).isNotNull();
            assertThat(locationsFromDynamicList(dynamicList)).containsExactly(
                "Site 1 - Adr 1 - AAA 111",
                "Site 2 - Adr 2 - BBB 222",
                "Site 3 - Adr 3 - CCC 333"
            );
            assertThat(dynamicList.getValue().getLabel()).isEqualTo("Site 3 - Adr 3 - CCC 333");
        }
    }

    @Nested
    class MidEventPrePopulateOrderDetailsPagesCallback extends LocationRefSampleDataBuilder {
        private LocalDate newDate;
        private LocalDateTime localDateTime;

        @BeforeEach
        void setup() {
            newDate = LocalDate.of(2020, 1, 15);
            localDateTime = LocalDateTime.of(2020, 1, 1, 12, 0, 0);
            when(time.now()).thenReturn(localDateTime);
            when(deadlinesCalculator.plusWorkingDays(any(LocalDate.class), anyInt())).thenReturn(newDate);
            when(deadlinesCalculator.getOrderSetAsideOrVariedApplicationDeadline(ArgumentMatchers.any(LocalDateTime.class))).thenReturn(newDate);
        }

        private final LocalDate date = LocalDate.of(2020, 1, 15);

        @Test
        void shouldPrePopulateOrderDetailsPages() {
            CaseData caseData = CaseDataBuilder.builder()
                .setClaimTypeToSpecClaim()
                .atStateClaimDraft()
                .totalClaimAmount(BigDecimal.valueOf(15000))
                .applicant1DQWithLocation().build();
            given(locationRefDataService.getCourtLocationsForDefaultJudgments(any()))
                .willReturn(getSampleCourLocationsRefObjectToSort());
            Category category = Category.builder().categoryKey("HearingChannel").key("INTER").valueEn("In Person").activeFlag("Y").build();
            CategorySearchResult categorySearchResult = CategorySearchResult.builder().categories(List.of(category)).build();
            when(categoryService.findCategoryByCategoryIdAndServiceId(any(), any(), any())).thenReturn(Optional.of(categorySearchResult));

            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_START);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            CaseData data = objectMapper.convertValue(response.getData(), CaseData.class);
            DynamicList dynamicList = getLocationDynamicListInPersonHearing(data);

            assertThat(dynamicList).isNotNull();
            assertThat(locationsFromDynamicList(dynamicList)).containsExactly(
                "A Site 3 - Adr 3 - AAA 111",
                "Site 1 - Adr 1 - VVV 111",
                "Site 2 - Adr 2 - BBB 222",
                "Site 3 - Adr 3 - CCC 333"
            );
            Optional<LocationRefData> shouldBeSelected = getSampleCourLocationsRefObjectToSort().stream()
                .filter(locationRefData -> locationRefData.getCourtLocationCode().equals(
                    caseData.getApplicant1DQ().getApplicant1DQRequestedCourt().getResponseCourtCode()))
                .findFirst();
            assertThat(shouldBeSelected.isPresent()).isTrue();
            assertThat(dynamicList.getValue()).isNotNull()
                .extracting("label").isEqualTo(LocationRefDataService.getDisplayEntry(shouldBeSelected.get()));

            assertThat(response.getData()).extracting("fastTrackAltDisputeResolutionToggle").isNotNull();
            assertThat(response.getData()).extracting("fastTrackVariationOfDirectionsToggle").isNotNull();
            assertThat(response.getData()).extracting("fastTrackSettlementToggle").isNotNull();
            assertThat(response.getData()).extracting("fastTrackDisclosureOfDocumentsToggle").isNotNull();
            assertThat(response.getData()).extracting("fastTrackWitnessOfFactToggle").isNotNull();
            assertThat(response.getData()).extracting("fastTrackSchedulesOfLossToggle").isNotNull();
            assertThat(response.getData()).extracting("fastTrackCostsToggle").isNotNull();
            assertThat(response.getData()).extracting("fastTrackTrialToggle").isNotNull();
            assertThat(response.getData()).extracting("fastTrackMethodToggle").isNotNull();
            assertThat(response.getData()).extracting("disposalHearingDisclosureOfDocumentsToggle").isNotNull();
            assertThat(response.getData()).extracting("disposalHearingWitnessOfFactToggle").isNotNull();
            assertThat(response.getData()).extracting("disposalHearingMedicalEvidenceToggle").isNotNull();
            assertThat(response.getData()).extracting("disposalHearingQuestionsToExpertsToggle").isNotNull();
            assertThat(response.getData()).extracting("disposalHearingSchedulesOfLossToggle").isNotNull();
            assertThat(response.getData()).extracting("disposalHearingFinalDisposalHearingToggle").isNotNull();
            assertThat(response.getData()).extracting("disposalHearingMethodToggle").isNotNull();
            assertThat(response.getData()).extracting("disposalHearingBundleToggle").isNotNull();
            assertThat(response.getData()).extracting("disposalHearingClaimSettlingToggle").isNotNull();
            assertThat(response.getData()).extracting("disposalHearingCostsToggle").isNotNull();
            assertThat(response.getData()).extracting("smallClaimsHearingToggle").isNotNull();
            assertThat(response.getData()).extracting("smallClaimsMethodToggle").isNotNull();
            assertThat(response.getData()).extracting("smallClaimsDocumentsToggle").isNotNull();
            assertThat(response.getData()).extracting("smallClaimsWitnessStatementToggle").isNotNull();
            assertThat(response.getData()).extracting("caseManagementLocation").isNotNull();

            assertThat(response.getData()).extracting("disposalHearingJudgesRecital").extracting("input")
                .isEqualTo("Upon considering the claim form, particulars of claim, statements of case"
                               + " and Directions questionnaires");

            assertThat(response.getData()).extracting("disposalHearingDisclosureOfDocuments").extracting("input1")
                .isEqualTo("The parties shall serve on each other copies of the documents upon which reliance is "
                               + "to be placed at the disposal hearing by 4pm on");
            assertThat(response.getData()).extracting("disposalHearingDisclosureOfDocuments").extracting("date1")
                .isEqualTo(LocalDate.now().plusWeeks(10).toString());
            assertThat(response.getData()).extracting("disposalHearingDisclosureOfDocuments").extracting("input2")
                .isEqualTo(
                    "The parties must upload to the Digital Portal copies of those documents which they wish the "
                        + "court to consider when deciding the amount of damages, by 4pm on");
            assertThat(response.getData()).extracting("disposalHearingDisclosureOfDocuments").extracting("date2")
                .isEqualTo(LocalDate.now().plusWeeks(10).toString());

            assertThat(response.getData()).extracting("disposalHearingWitnessOfFact").extracting("input3")
                .isEqualTo("The claimant must upload to the Digital Portal copies of the witness statements"
                               + " of all witnesses of fact on whose evidence reliance is to be placed by 4pm on");
            assertThat(response.getData()).extracting("disposalHearingWitnessOfFact").extracting("date2")
                .isEqualTo(LocalDate.now().plusWeeks(4).toString());
            assertThat(response.getData()).extracting("disposalHearingWitnessOfFact").extracting("input4")
                .isEqualTo("The provisions of CPR 32.6 apply to such evidence.");
            assertThat(response.getData()).extracting("disposalHearingWitnessOfFact").extracting("input5")
                .isEqualTo("Any application by the defendant in relation to CPR 32.7 must be made by 4pm on");
            assertThat(response.getData()).extracting("disposalHearingWitnessOfFact").extracting("date3")
                .isEqualTo(LocalDate.now().plusWeeks(6).toString());
            assertThat(response.getData()).extracting("disposalHearingWitnessOfFact").extracting("input6")
                .isEqualTo("and must be accompanied by proposed directions for allocation and listing for trial on "
                               + "quantum. This is because cross-examination will cause the hearing to exceed "
                               + "the 30-minute maximum time estimate for a disposal hearing.");

            assertThat(response.getData()).extracting("disposalHearingMedicalEvidence").extracting("input")
                .isEqualTo("The claimant has permission to rely upon the written expert evidence already uploaded "
                               + "to the Digital Portal with the particulars of claim and in addition has permission to"
                               + " rely upon any associated correspondence or updating report which is uploaded"
                               + " to the Digital Portal by 4pm on");
            assertThat(response.getData()).extracting("disposalHearingMedicalEvidence").extracting("date")
                .isEqualTo(LocalDate.now().plusWeeks(4).toString());

            assertThat(response.getData()).extracting("disposalHearingQuestionsToExperts").extracting("date")
                .isEqualTo(LocalDate.now().plusWeeks(6).toString());

            assertThat(response.getData()).extracting("disposalHearingSchedulesOfLoss").extracting("input2")
                .isEqualTo("If there is a claim for ongoing or future loss in the original schedule of losses, "
                               + "the claimant must upload to the Digital Portal an up-to-date schedule of loss "
                               + "by 4pm on");
            assertThat(response.getData()).extracting("disposalHearingSchedulesOfLoss").extracting("date2")
                .isEqualTo(LocalDate.now().plusWeeks(10).toString());
            assertThat(response.getData()).extracting("disposalHearingSchedulesOfLoss").extracting("input3")
                .isEqualTo("If the defendant wants to challenge this claim, "
                               + "they must send an up-to-date counter-schedule of loss "
                               + "to the claimant by 4pm on");
            assertThat(response.getData()).extracting("disposalHearingSchedulesOfLoss").extracting("date3")
                .isEqualTo(LocalDate.now().plusWeeks(12).toString());
            assertThat(response.getData()).extracting("disposalHearingSchedulesOfLoss").extracting("input4")
                .isEqualTo("If the defendant want to challenge the sums claimed in the schedule of loss"
                               + " they must upload to the Digital Portal an updated counter schedule of loss "
                               + "by 4pm on");
            assertThat(response.getData()).extracting("disposalHearingSchedulesOfLoss").extracting("date4")
                .isEqualTo(LocalDate.now().plusWeeks(12).toString());

            assertThat(response.getData()).extracting("disposalHearingFinalDisposalHearing").extracting("input")
                .isEqualTo("This claim will be listed for final disposal "
                               + "before a judge on the first available date after");
            assertThat(response.getData()).extracting("disposalHearingFinalDisposalHearing").extracting("date")
                .isEqualTo(LocalDate.now().plusWeeks(16).toString());

            assertThat(response.getData()).extracting("disposalHearingBundle").extracting("input")
                .isEqualTo("At least 7 days before the disposal hearing, "
                               + "the claimant must file and serve");

            assertThat(response.getData()).extracting("disposalHearingNotes").extracting("input")
                .isEqualTo("This Order has been made without a hearing. Each party has the right to apply to have"
                               + " this Order set aside or varied. Any such application must be uploaded "
                               + "to the Digital Portal together with the appropriate fee, by 4pm on");
            assertThat(response.getData()).extracting("disposalHearingNotes").extracting("date")
                .isEqualTo(LocalDate.now().plusWeeks(1).toString());

            assertThat(response.getData()).doesNotHaveToString("disposalHearingJudgementDeductionValue");

            assertThat(response.getData()).extracting("fastTrackJudgesRecital").extracting("input")
                .isEqualTo("Upon considering the statements of case and the information provided by the parties,");

            assertThat(response.getData()).doesNotHaveToString("fastTrackJudgementDeductionValue");

            assertThat(response.getData()).extracting("fastTrackDisclosureOfDocuments").extracting("input1")
                .isEqualTo("Standard disclosure shall be provided by the parties by uploading to the Digital "
                               + "Portal their list of documents by 4pm on");
            assertThat(response.getData()).extracting("fastTrackDisclosureOfDocuments").extracting("date1")
                .isEqualTo(LocalDate.now().plusWeeks(4).toString());
            assertThat(response.getData()).extracting("fastTrackDisclosureOfDocuments").extracting("input2")
                .isEqualTo("Any request to inspect a document, or for a copy of a document, shall be made directly to "
                               + "the other party by 4pm on");
            assertThat(response.getData()).extracting("fastTrackDisclosureOfDocuments").extracting("date2")
                .isEqualTo(LocalDate.now().plusWeeks(6).toString());
            assertThat(response.getData()).extracting("fastTrackDisclosureOfDocuments").extracting("input3")
                .isEqualTo("Requests will be complied with within 7 days of the receipt of the request.");
            assertThat(response.getData()).extracting("fastTrackDisclosureOfDocuments").extracting("input4")
                .isEqualTo("Each party must upload to the Digital Portal copies of those documents on which they "
                               + "wish to rely at trial by 4pm on");
            assertThat(response.getData()).extracting("fastTrackDisclosureOfDocuments").extracting("date3")
                .isEqualTo(LocalDate.now().plusWeeks(8).toString());

            assertThat(response.getData()).extracting("fastTrackWitnessOfFact").extracting("input1")
                .isEqualTo("Each party must upload to the Digital Portal copies of the statements of all witnesses of "
                               + "fact on whom they intend to rely.");
            assertThat(response.getData()).extracting("fastTrackWitnessOfFact").extracting("input2")
                .isEqualTo("3");
            assertThat(response.getData()).extracting("fastTrackWitnessOfFact").extracting("input3")
                .isEqualTo("3");
            assertThat(response.getData()).extracting("fastTrackWitnessOfFact").extracting("input4")
                .isEqualTo("For this limitation, a party is counted as a witness.");
            assertThat(response.getData()).extracting("fastTrackWitnessOfFact").extracting("input5")
                .isEqualTo("Each witness statement should be no more than");
            assertThat(response.getData()).extracting("fastTrackWitnessOfFact").extracting("input6")
                .isEqualTo("10");
            assertThat(response.getData()).extracting("fastTrackWitnessOfFact").extracting("input7")
                .isEqualTo("A4 pages. Statements should be double spaced using a font size of 12.");
            assertThat(response.getData()).extracting("fastTrackWitnessOfFact").extracting("input8")
                .isEqualTo("Witness statements shall be uploaded to the Digital Portal by 4pm on");
            assertThat(response.getData()).extracting("fastTrackWitnessOfFact").extracting("date")
                .isEqualTo(LocalDate.now().plusWeeks(8).toString());
            assertThat(response.getData()).extracting("fastTrackWitnessOfFact").extracting("input9")
                .isEqualTo("Evidence will not be permitted at trial from a witness"
                               + " whose statement has not been uploaded in accordance with"
                               + " this Order. "
                               + "Evidence not uploaded, or uploaded late, "
                               + "will not be permitted except with permission from the Court.");

            assertThat(response.getData()).extracting("fastTrackSchedulesOfLoss").extracting("input1")
                .isEqualTo("The claimant must upload to the Digital Portal an up-to-date schedule of loss to the "
                               + "defendant by 4pm on");
            assertThat(response.getData()).extracting("fastTrackSchedulesOfLoss").extracting("date1")
                .isEqualTo(LocalDate.now().plusWeeks(10).toString());
            assertThat(response.getData()).extracting("fastTrackSchedulesOfLoss").extracting("input2")
                .isEqualTo("If the defendant wants to challenge this claim, upload to the Digital Portal "
                               + "counter-schedule of loss by 4pm on");
            assertThat(response.getData()).extracting("fastTrackSchedulesOfLoss").extracting("date2")
                .isEqualTo(LocalDate.now().plusWeeks(12).toString());
            assertThat(response.getData()).extracting("fastTrackSchedulesOfLoss").extracting("input3")
                .isEqualTo("If there is a claim for future pecuniary loss and the parties have not already set out "
                               + "their case on periodical payments, they must do so in the respective schedule and "
                               + "counter-schedule.");
            assertThat(response.getData()).extracting("fastTrackTrial").extracting("input1")
                .isEqualTo("The time provisionally allowed for this trial is");
            assertThat(response.getData()).extracting("fastTrackTrial").extracting("date1")
                .isEqualTo(LocalDate.now().plusWeeks(22).toString());
            assertThat(response.getData()).extracting("fastTrackTrial").extracting("date2")
                .isEqualTo(LocalDate.now().plusWeeks(30).toString());
            assertThat(response.getData()).extracting("fastTrackTrial").extracting("input2")
                .isEqualTo("If either party considers that the time estimate is insufficient, they must inform the"
                               + " court within 7 days of the date stated on this order.");
            assertThat(response.getData()).extracting("fastTrackTrial").extracting("input3")
                .isEqualTo("At least 7 days before the trial, the claimant must upload to the Digital Portal");

            assertThat(response.getData()).extracting("fastTrackNotes").extracting("input")
                .isEqualTo("This Order has been made without a hearing. Each party has the right to apply to have this"
                               + " Order set aside or varied. Any application must be received by the Court,"
                               + " together with the appropriate fee by 4pm on");

            assertThat(response.getData()).extracting("fastTrackNotes").extracting("date")
                .isEqualTo(LocalDate.now().plusWeeks(1).toString());

            assertThat(response.getData()).extracting("fastTrackBuildingDispute").extracting("input1")
                .isEqualTo("The claimant must prepare a Scott Schedule of the defects, items of damage, "
                               + "or any other relevant matters");
            assertThat(response.getData()).extracting("fastTrackBuildingDispute").extracting("input2")
                .isEqualTo("The columns should be headed:\n"
                               + "  •  Item\n"
                               + "  •  Alleged defect\n"
                               + "  •  Claimant’s costing\n"
                               + "  •  Defendant’s response\n"
                               + "  •  Defendant’s costing\n"
                               + "  •  Reserved for Judge’s use");
            assertThat(response.getData()).extracting("fastTrackBuildingDispute").extracting("input3")
                .isEqualTo("The claimant must upload to the Digital Portal the Scott Schedule with the relevant columns"
                               + " completed by 4pm on");
            assertThat(response.getData()).extracting("fastTrackBuildingDispute").extracting("date1")
                .isEqualTo(LocalDate.now().plusWeeks(10).toString());
            assertThat(response.getData()).extracting("fastTrackBuildingDispute").extracting("input4")
                .isEqualTo("The defendant must upload to the Digital Portal an amended version of the Scott Schedule "
                               + "with the relevant columns in response completed by 4pm on");
            assertThat(response.getData()).extracting("fastTrackBuildingDispute").extracting("date2")
                .isEqualTo(LocalDate.now().plusWeeks(12).toString());

            assertThat(response.getData()).extracting("fastTrackClinicalNegligence").extracting("input1")
                .isEqualTo("Documents should be retained as follows:");
            assertThat(response.getData()).extracting("fastTrackClinicalNegligence").extracting("input2")
                .isEqualTo("a) The parties must retain all electronically stored documents relating to the issues in"
                               + " this claim.");
            assertThat(response.getData()).extracting("fastTrackClinicalNegligence").extracting("input3")
                .isEqualTo("b) the defendant must retain the original clinical notes relating to the issues in this"
                               + " claim. The defendant must give facilities for inspection by the claimant, the"
                               + " claimant's legal advisers and experts of these original notes on 7 days written"
                               + " notice.");
            assertThat(response.getData()).extracting("fastTrackClinicalNegligence").extracting("input4")
                .isEqualTo("c) Legible copies of the medical and educational records of the claimant are to be placed "
                               + "in a separate paginated bundle by the claimant's solicitors and kept up to date. "
                               + "All references to medical notes are to be made by reference "
                               + "to the pages in that bundle.");

            assertThat(response.getData()).extracting("fastTrackCreditHire").extracting("input1")
                .isEqualTo("If impecuniosity is alleged by the claimant and not admitted by the defendant, the "
                               + "claimant's disclosure as ordered earlier in this Order must include:\n"
                               + "a) Evidence of all income from all sources for a period of 3 months prior to the "
                               + "commencement of hire until the earlier of:\n "
                               + "     i) 3 months after cessation of hire\n"
                               + "     ii) the repair or replacement of the claimant's vehicle\n"
                               + "b) Copies of all bank, credit card, and saving account statements for a period of 3"
                               + " months prior to the commencement of hire until the earlier of:\n"
                               + "     i) 3 months after cessation of hire\n"
                               + "     ii) the repair or replacement of the claimant's vehicle\n"
                               + "c) Evidence of any loan, overdraft or other credit facilities available to the "
                               + "claimant.");
            assertThat(response.getData()).extracting("fastTrackCreditHire").extracting("input2")
                .isEqualTo("The claimant must upload to the Digital Portal a witness statement addressing\n"
                               + "a) the need to hire a replacement vehicle; and\n"
                               + "b) impecuniosity");
            assertThat(response.getData()).extracting("fastTrackCreditHire").extracting("date1")
                .isEqualTo(LocalDate.now().plusWeeks(4).toString());
            assertThat(response.getData()).extracting("fastTrackCreditHire").extracting("input3")
                .isEqualTo("A failure to comply with the paragraph above will result in the claimant being debarred "
                               + "from asserting need or relying on impecuniosity as the case may be at the final "
                               + "hearing, save with permission of the Trial Judge.");
            assertThat(response.getData()).extracting("fastTrackCreditHire").extracting("input4")
                .isEqualTo("The parties are to liaise and use reasonable endeavours to agree the basic hire rate no "
                               + "later than 4pm on");
            assertThat(response.getData()).extracting("fastTrackCreditHire").extracting("date2")
                .isEqualTo(LocalDate.now().plusWeeks(6).toString());
            assertThat(response.getData()).extracting("fastTrackCreditHire").extracting("input5")
                .isEqualTo("If the parties fail to agree rates subject to liability and/or other issues pursuant to"
                               + " the paragraph above, each party may rely upon written evidence by way of witness"
                               + " statement of one witness to provide evidence of basic hire rates available within"
                               + " the claimant's geographical location, from a mainstream supplier, or a local"
                               + " reputable supplier if none is available.");
            assertThat(response.getData()).extracting("fastTrackCreditHire").extracting("input6")
                .isEqualTo("The defendant's evidence is to be uploaded to the Digital Portal by 4pm on");
            assertThat(response.getData()).extracting("fastTrackCreditHire").extracting("date3")
                .isEqualTo(LocalDate.now().plusWeeks(8).toString());
            assertThat(response.getData()).extracting("fastTrackCreditHire").extracting("input7")
                .isEqualTo("and the claimant's evidence in reply if so advised to be uploaded by 4pm on");
            assertThat(response.getData()).extracting("fastTrackCreditHire").extracting("date4")
                .isEqualTo(LocalDate.now().plusWeeks(10).toString());
            assertThat(response.getData()).extracting("fastTrackCreditHire").extracting("input8")
                .isEqualTo("This witness statement is limited to 10 pages per party, including any appendices.");

            assertThat(response.getData()).extracting("fastTrackHousingDisrepair").extracting("input1")
                .isEqualTo("The claimant must prepare a Scott Schedule of the items in disrepair.");
            assertThat(response.getData()).extracting("fastTrackHousingDisrepair").extracting("input2")
                .isEqualTo("The columns should be headed:\n"
                               + "  •  Item\n"
                               + "  •  Alleged disrepair\n"
                               + "  •  Defendant’s response\n"
                               + "  •  Reserved for Judge’s use");
            assertThat(response.getData()).extracting("fastTrackHousingDisrepair").extracting("input3")
                .isEqualTo("The claimant must upload to the Digital Portal the Scott Schedule with the relevant "
                               + "columns completed by 4pm on");
            assertThat(response.getData()).extracting("fastTrackHousingDisrepair").extracting("date1")
                .isEqualTo(LocalDate.now().plusWeeks(10).toString());
            assertThat(response.getData()).extracting("fastTrackHousingDisrepair").extracting("input4")
                .isEqualTo("The defendant must upload to the Digital Portal the amended Scott Schedule with the "
                               + "relevant columns in response completed by 4pm on");
            assertThat(response.getData()).extracting("fastTrackHousingDisrepair").extracting("date2")
                .isEqualTo(LocalDate.now().plusWeeks(12).toString());

            assertThat(response.getData()).extracting("fastTrackPersonalInjury").extracting("input1")
                .isEqualTo("The claimant has permission to rely upon the written expert evidence already uploaded to"
                               + " the Digital Portal with the particulars of claim and in addition has permission to"
                               + " rely upon any associated correspondence or updating report which is uploaded to the"
                               + " Digital Portal by 4pm on");
            assertThat(response.getData()).extracting("fastTrackPersonalInjury").extracting("date1")
                .isEqualTo(LocalDate.now().plusWeeks(4).toString());
            assertThat(response.getData()).extracting("fastTrackPersonalInjury").extracting("input2")
                .isEqualTo("Any questions which are to be addressed to an expert must be sent to the expert directly "
                               + "and uploaded to the Digital Portal by 4pm on");
            assertThat(response.getData()).extracting("fastTrackPersonalInjury").extracting("date2")
                .isEqualTo(LocalDate.now().plusWeeks(4).toString());
            assertThat(response.getData()).extracting("fastTrackPersonalInjury").extracting("input3")
                .isEqualTo("The answers to the questions shall be answered by the Expert by");
            assertThat(response.getData()).extracting("fastTrackPersonalInjury").extracting("date3")
                .isEqualTo(LocalDate.now().plusWeeks(8).toString());
            assertThat(response.getData()).extracting("fastTrackPersonalInjury").extracting("input4")
                .isEqualTo("and uploaded to the Digital Portal by");
            assertThat(response.getData()).extracting("fastTrackPersonalInjury").extracting("date4")
                .isEqualTo(LocalDate.now().plusWeeks(8).toString());

            assertThat(response.getData()).extracting("fastTrackRoadTrafficAccident").extracting("input")
                .isEqualTo("Photographs and/or a place of the accident location shall be prepared and agreed by the "
                               + "parties and uploaded to the Digital Portal by 4pm on");

            assertThat(response.getData()).extracting("smallClaimsJudgesRecital").extracting("input")
                .isEqualTo("Upon considering the statements of case and the information provided by the parties,");

            assertThat(response.getData()).doesNotHaveToString("smallClaimsJudgementDeductionValue");

            assertThat(response.getData()).extracting("smallClaimsHearing").extracting("input1")
                .isEqualTo("The hearing of the claim will be on a date to be notified to you by a separate "
                               + "notification. The hearing will have a time estimate of");
            assertThat(response.getData()).extracting("smallClaimsHearing").extracting("input2")
                .isEqualTo("The claimant must by no later than 4 weeks before the hearing date, pay the court the "
                               + "required hearing fee or submit a fully completed application for Help with Fees. \n"
                               + "If the claimant fails to pay the fee or obtain a fee exemption by that time the "
                               + "claim will be struck without further order.");

            assertThat(response.getData()).extracting("smallClaimsDocuments").extracting("input1")
                .isEqualTo("Each party must upload to the Digital Portal copies of all documents which they wish the"
                               + " court to consider when reaching its decision not less than 14 days before "
                               + "the hearing.");
            assertThat(response.getData()).extracting("smallClaimsDocuments").extracting("input2")
                .isEqualTo("The court may refuse to consider any document which has not been uploaded to the "
                               + "Digital Portal by the above date.");

            assertThat(response.getData()).extracting("smallClaimsNotes").extracting("input")
                .isEqualTo("This order has been made without hearing. "
                               + "Each party has the right to apply to have this Order set aside or varied. "
                               + "Any such application must be received by the Court "
                               + "(together with the appropriate fee) by 4pm on "
                               + DateFormatHelper.formatLocalDate(newDate, DateFormatHelper.DATE));

            assertThat(response.getData()).extracting("smallClaimsWitnessStatement").extracting("input1")
                .isEqualTo("Each party must upload to the Digital Portal copies of all witness statements of the"
                               + " witnesses upon whose evidence they intend to rely at the hearing not less than 14"
                               + " days before the hearing.");
            assertThat(response.getData()).extracting("smallClaimsWitnessStatement").doesNotHaveToString("input2");
            assertThat(response.getData()).extracting("smallClaimsWitnessStatement").doesNotHaveToString("input3");
            assertThat(response.getData()).extracting("smallClaimsWitnessStatement").extracting("input4")
                .isEqualTo("For this limitation, a party is counted as a witness.");
            assertThat(response.getData()).extracting("smallClaimsWitnessStatement").extracting("text")
                .isEqualTo("A witness statement must: \na) Start with the name of the case and the claim number;"
                               + "\nb) State the full name and address of the witness; "
                               + "\nc) Set out the witness's evidence clearly in numbered paragraphs on numbered pages;"
                               + "\nd) End with this paragraph: 'I believe that the facts stated in this witness "
                               + "statement are true. I understand that proceedings for contempt of court may be "
                               + "brought against anyone who makes, or causes to be made, a false statement in a "
                               + "document verified by a statement of truth without an honest belief in its truth'."
                               + "\ne) be signed by the witness and dated."
                               + "\nf) If a witness is unable to read the statement there must be a certificate that "
                               + "it has been read or interpreted to the witness by a suitably qualified person and "
                               + "at the final hearing there must be an independent interpreter who will not be "
                               + "provided by the Court."
                               + "\n\nThe judge may refuse to allow a witness to give evidence or consider any "
                               + "statement of any witness whose statement has not been uploaded to the Digital Portal"
                               + " in accordance with the paragraphs above."
                               + "\n\nA witness whose statement has been uploaded in accordance with the above must"
                               + " attend the hearing. If they do not attend, it will be for the court to decide how"
                               + " much reliance, if any, to place on their evidence.");

            assertThat(response.getData()).extracting("smallClaimsCreditHire").extracting("input1")
                .isEqualTo("If impecuniosity is alleged by the claimant and not admitted by the defendant, the "
                               + "claimant's disclosure as ordered earlier in this Order must include:\n"
                               + "a) Evidence of all income from all sources for a period of 3 months prior to the "
                               + "commencement of hire until the earlier of:\n "
                               + "     i) 3 months after cessation of hire\n"
                               + "     ii) the repair or replacement of the claimant's vehicle\n"
                               + "b) Copies of all bank, credit card, and saving account statements for a period of 3"
                               + " months prior to the commencement of hire until the earlier of:\n"
                               + "     i) 3 months after cessation of hire\n"
                               + "     ii) the repair or replacement of the claimant's vehicle\n"
                               + "c) Evidence of any loan, overdraft or other credit facilities available to the "
                               + "claimant.");
            assertThat(response.getData()).extracting("smallClaimsCreditHire").extracting("input2")
                .isEqualTo("The claimant must upload to the Digital Portal a witness statement addressing\n"
                               + "a) the need to hire a replacement vehicle; and\n"
                               + "b) impecuniosity");
            assertThat(response.getData()).extracting("smallClaimsCreditHire").extracting("date1")
                .isEqualTo(LocalDate.now().plusWeeks(4).toString());
            assertThat(response.getData()).extracting("smallClaimsCreditHire").extracting("input3")
                .isEqualTo("A failure to comply with the paragraph above will result in the claimant being debarred "
                               + "from asserting need or relying on impecuniosity as the case may be at the final "
                               + "hearing, save with permission of the Trial Judge.");
            assertThat(response.getData()).extracting("smallClaimsCreditHire").extracting("input4")
                .isEqualTo("The parties are to liaise and use reasonable endeavours to agree the basic hire rate no "
                               + "later than 4pm on");
            assertThat(response.getData()).extracting("smallClaimsCreditHire").extracting("date2")
                .isEqualTo(LocalDate.now().plusWeeks(6).toString());
            assertThat(response.getData()).extracting("smallClaimsCreditHire").extracting("input5")
                .isEqualTo("If the parties fail to agree rates subject to liability and/or other issues pursuant to"
                               + " the paragraph above, each party may rely upon written evidence by way of witness"
                               + " statement of one witness to provide evidence of basic hire rates available within"
                               + " the claimant's geographical location, from a mainstream supplier, or a local"
                               + " reputable supplier if none is available.");
            assertThat(response.getData()).extracting("smallClaimsCreditHire").extracting("input6")
                .isEqualTo("The defendant's evidence is to be uploaded to the Digital Portal by 4pm on");
            assertThat(response.getData()).extracting("smallClaimsCreditHire").extracting("date3")
                .isEqualTo(LocalDate.now().plusWeeks(8).toString());
            assertThat(response.getData()).extracting("smallClaimsCreditHire").extracting("input7")
                .isEqualTo("and the claimant's evidence in reply if so advised to be uploaded by 4pm on");
            assertThat(response.getData()).extracting("smallClaimsCreditHire").extracting("date4")
                .isEqualTo(LocalDate.now().plusWeeks(10).toString());
            assertThat(response.getData()).extracting("smallClaimsCreditHire").extracting("input11")
                .isEqualTo("This witness statement is limited to 10 pages per party, including any appendices.");

            assertThat(response.getData()).extracting("smallClaimsRoadTrafficAccident").extracting("input")
                .isEqualTo("Photographs and/or a place of the accident location shall be prepared and agreed by the "
                               + "parties and uploaded to the Digital Portal no later than 14 days before the "
                               + "hearing.");
            assertThat(response.getData()).extracting("disposalHearingHearingTime").extracting("input")
                .isEqualTo("This claim will be listed for final disposal before a judge on the first available date "
                               + "after");
            assertThat(response.getData()).extracting("disposalHearingHearingTime").extracting("dateTo")
                .isEqualTo(LocalDate.now().plusWeeks(16).toString());
            assertThat(response.getData()).extracting("disposalOrderWithoutHearing").extracting("input")
                .isEqualTo(String.format("This order has been made without hearing. "
                                             + "Each party has the right to apply to have this Order set aside or varied. "
                                             + "Any such application must be received by the Court (together with the "
                                             + "appropriate fee) by 4pm on %s.",
                    date.format(DateTimeFormatter.ofPattern("dd MMMM yyyy"))
                ));
            assertThat(response.getData()).extracting("fastTrackHearingTime").extracting("helpText1")
                .isEqualTo("If either party considers that the time estimate is insufficient, "
                               + "they must inform the court within 7 days of the date of this order.");
            assertThat(response.getData()).extracting("fastTrackHearingTime").extracting("dateToToggle").isNotNull();
            assertThat(response.getData()).extracting("fastTrackHearingTime").extracting("dateFrom")
                .isEqualTo(LocalDate.now().plusWeeks(22).toString());
            assertThat(response.getData()).extracting("fastTrackHearingTime").extracting("dateTo")
                .isEqualTo(LocalDate.now().plusWeeks(30).toString());
            assertThat(response.getData()).extracting("fastTrackHearingTime").extracting("helpText2")
                .isEqualTo("Not more than seven nor less than three clear days before the trial, "
                               + "the claimant must file at court and serve an indexed and paginated bundle of "
                               + "documents which complies with the requirements of Rule 39.5 Civil Procedure Rules "
                               + "and which complies with requirements of PD32. The parties must endeavour to agree "
                               + "the contents of the bundle before it is filed. The bundle will include a case "
                               + "summary and a chronology.");
            assertThat(response.getData()).extracting("fastTrackOrderWithoutJudgement").extracting("input")
                .isEqualTo(String.format("This order has been made without hearing. "
                                             + "Each party has the right to apply "
                                             + "to have this Order set aside or varied. Any such application must be "
                                             + "received by the Court (together with the appropriate fee) by 4pm "
                                             + "on %s.",
                                         date.format(DateTimeFormatter.ofPattern("dd MMMM yyyy"))));
        }

        @Test
        void testSDOSortsLocationListThroughOrganisationPartyType() {
            CaseData caseData = CaseDataBuilder.builder()
                .setClaimTypeToSpecClaim()
                .atStateClaimDraft()
                .totalClaimAmount(BigDecimal.valueOf(10000))
                .respondent1DQWithLocation().applicant1DQWithLocation().applicant1(Party.builder()
                                                                                       .type(Party.Type.ORGANISATION)
                                                                                       .individualTitle("Mr.")
                                                                                       .individualFirstName("Alex")
                                                                                       .individualLastName("Richards")
                                                                                       .partyName("Mr. Alex Richards")
                                                                                       .build()).build();
            given(locationRefDataService.getCourtLocationsForDefaultJudgments(any()))
                .willReturn(getSampleCourLocationsRefObjectToSort());

            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_START);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            CaseData data = objectMapper.convertValue(response.getData(), CaseData.class);
            DynamicList dynamicList = getLocationDynamicListInPersonHearing(data);

            assertThat(dynamicList).isNotNull();
            assertThat(locationsFromDynamicList(dynamicList)).containsExactly(
                "A Site 3 - Adr 3 - AAA 111",
                "Site 1 - Adr 1 - VVV 111",
                "Site 2 - Adr 2 - BBB 222",
                "Site 3 - Adr 3 - CCC 333"
            );
        }

        @Test
        void testSDOSortsLocationListThroughDecideDamagesOrderType() {
            given(locationRefDataService.getCourtLocationsForDefaultJudgments(any()))
                .willReturn(getSampleCourLocationsRefObjectToSort());
            CaseData caseData = CaseDataBuilder.builder().respondent1DQWithLocation().applicant1DQWithLocation()
                .setClaimTypeToSpecClaim().atStateClaimDraft()
                .totalClaimAmount(BigDecimal.valueOf(10000))
                .build().toBuilder().orderType(OrderType.DECIDE_DAMAGES).applicant1(Party.builder()
                                                                                        .type(Party.Type.ORGANISATION)
                                                                                        .individualTitle("Mr.")
                                                                                        .individualFirstName("Alex")
                                                                                        .individualLastName("Richards")
                                                                                        .partyName("Mr. Alex Richards")
                                                                                        .build()).build();

            // .respondent1DQWithLocation().applicant1DQWithLocation()

            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_START);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            CaseData data = objectMapper.convertValue(response.getData(), CaseData.class);
            DynamicList dynamicList = getLocationDynamicListInPersonHearing(data);

            assertThat(dynamicList).isNotNull();
            assertThat(locationsFromDynamicList(dynamicList)).containsExactly(
                "A Site 3 - Adr 3 - AAA 111",
                "Site 1 - Adr 1 - VVV 111",
                "Site 2 - Adr 2 - BBB 222",
                "Site 3 - Adr 3 - CCC 333"
            );
        }

        @Test
        void shouldPrePopulateDisposalHearingJudgementDeductionValueWhenDrawDirectionsOrderIsNotNull() {
            JudgementSum tempJudgementSum = JudgementSum.builder()
                .judgementSum(12.0)
                .build();

            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimDraft()
                .build()
                .toBuilder()
                .drawDirectionsOrder(tempJudgementSum)
                .build();

            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_START);
            when(deadlinesCalculator.plusWorkingDays(LocalDate.now(), 5))
                .thenReturn(LocalDate.now().plusDays(5));

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getData()).extracting("disposalHearingJudgementDeductionValue").extracting("value")
                .isEqualTo("12.0%");
            assertThat(response.getData()).extracting("fastTrackJudgementDeductionValue").extracting("value")
                .isEqualTo("12.0%");
            assertThat(response.getData()).extracting("smallClaimsJudgementDeductionValue").extracting("value")
                .isEqualTo("12.0%");
        }
    }

    @Nested
    class MidEventSetOrderDetailsFlags {
        private static final String PAGE_ID = "order-details-navigation";

        @Test
        void smallClaimsFlagAndFastTrackFlagSetToNo() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimDraft()
                .build();

            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getData()).extracting("setSmallClaimsFlag").isEqualTo("No");
            assertThat(response.getData()).extracting("setFastTrackFlag").isEqualTo("No");
        }

        @Test
        void smallClaimsFlagSetToYesPathOne() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimDraft()
                .build()
                .toBuilder()
                .drawDirectionsOrderRequired(YesOrNo.NO)
                .claimsTrack(ClaimsTrack.smallClaimsTrack)
                .build();

            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getData()).extracting("setSmallClaimsFlag").isEqualTo("Yes");
            assertThat(response.getData()).extracting("setFastTrackFlag").isEqualTo("No");
        }

        @Test
        void smallClaimsFlagSetToYesPathTwo() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimDraft()
                .build()
                .toBuilder()
                .drawDirectionsOrderRequired(YesOrNo.YES)
                .drawDirectionsOrderSmallClaims(YesOrNo.YES)
                .build();

            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getData()).extracting("setSmallClaimsFlag").isEqualTo("Yes");
            assertThat(response.getData()).extracting("setFastTrackFlag").isEqualTo("No");
        }

        @Test
        void fastTrackFlagSetToYesPathOne() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimDraft()
                .build()
                .toBuilder()
                .drawDirectionsOrderRequired(YesOrNo.NO)
                .claimsTrack(ClaimsTrack.fastTrack)
                .build();

            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getData()).extracting("setSmallClaimsFlag").isEqualTo("No");
            assertThat(response.getData()).extracting("setFastTrackFlag").isEqualTo("Yes");
        }

        @Test
        void fastTrackFlagSetToYesPathTwo() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimDraft()
                .build()
                .toBuilder()
                .drawDirectionsOrderRequired(YesOrNo.YES)
                .drawDirectionsOrderSmallClaims(YesOrNo.NO)
                .orderType(OrderType.DECIDE_DAMAGES)
                .build();

            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getData()).extracting("setSmallClaimsFlag").isEqualTo("No");
            assertThat(response.getData()).extracting("setFastTrackFlag").isEqualTo("Yes");
        }
    }

    @Nested
    class MidEventGenerateSdoOrderCallback {
        private static final String PAGE_ID = "generate-sdo-order";

        @Test
        void shouldGenerateAndSaveSdoOrder_whenStateClaimIssuedDisposalHearingSDOInPersonHearing() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDraft()
                .atStateClaimIssuedDisposalHearingSDOInPersonHearing().build();

            CallbackParams params = callbackParamsOf(CallbackVersion.V_1, caseData, MID, PAGE_ID);
            CaseDocument order = CaseDocument.builder().documentLink(
                    Document.builder().documentUrl("url").build())
                .build();
            when(sdoGeneratorService.generate(any(), any())).thenReturn(order);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            assertThat(response.getData()).extracting("sdoOrderDocument").isNotNull();
        }

        @Test
        void shouldGenerateAndSaveSdoOrder_whenStateClaimIssuedDisposalHearingSDOTelephoneHearing() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDraft()
                .atStateClaimIssuedDisposalHearingSDOTelephoneHearing().build();

            CallbackParams params = callbackParamsOf(CallbackVersion.V_1, caseData, MID, PAGE_ID);
            CaseDocument order = CaseDocument.builder().documentLink(
                    Document.builder().documentUrl("url").build())
                .build();
            when(sdoGeneratorService.generate(any(), any())).thenReturn(order);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            assertThat(response.getData()).extracting("sdoOrderDocument").isNotNull();
        }

        @Test
        void shouldGenerateAndSaveSdoOrder_whenStateClaimIssuedDisposalHearingSDOVideoHearing() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDraft()
                .atStateClaimIssuedDisposalHearingSDOVideoHearing().build();

            CallbackParams params = callbackParamsOf(CallbackVersion.V_1, caseData, MID, PAGE_ID);
            CaseDocument order = CaseDocument.builder().documentLink(
                    Document.builder().documentUrl("url").build())
                .build();
            when(sdoGeneratorService.generate(any(), any())).thenReturn(order);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            assertThat(response.getData()).extracting("sdoOrderDocument").isNotNull();
        }

        @Test
        void shouldGenerateAndSaveSdoOrder_whenStateClaimIssuedFastTrackSDOInPersonHearing() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDraft()
                .atStateClaimIssuedFastTrackSDOInPersonHearing().build();

            CallbackParams params = callbackParamsOf(CallbackVersion.V_1, caseData, MID, PAGE_ID);
            CaseDocument order = CaseDocument.builder().documentLink(
                    Document.builder().documentUrl("url").build())
                .build();
            when(sdoGeneratorService.generate(any(), any())).thenReturn(order);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            assertThat(response.getData()).extracting("sdoOrderDocument").isNotNull();
        }

        @Test
        void shouldGenerateAndSaveSdoOrder_whenStateClaimIssuedFastTrackSDOTelephoneHearing() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDraft()
                .atStateClaimIssuedFastTrackSDOTelephoneHearing().build();

            CallbackParams params = callbackParamsOf(CallbackVersion.V_1, caseData, MID, PAGE_ID);
            CaseDocument order = CaseDocument.builder().documentLink(
                    Document.builder().documentUrl("url").build())
                .build();
            when(sdoGeneratorService.generate(any(), any())).thenReturn(order);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            assertThat(response.getData()).extracting("sdoOrderDocument").isNotNull();
        }

        @Test
        void shouldGenerateAndSaveSdoOrder_whenStateClaimIssuedFastTrackSDOVideoHearing() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDraft()
                .atStateClaimIssuedFastTrackSDOVideoHearing().build();

            CallbackParams params = callbackParamsOf(CallbackVersion.V_1, caseData, MID, PAGE_ID);
            CaseDocument order = CaseDocument.builder().documentLink(
                    Document.builder().documentUrl("url").build())
                .build();
            when(sdoGeneratorService.generate(any(), any())).thenReturn(order);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            assertThat(response.getData()).extracting("sdoOrderDocument").isNotNull();
        }

        @Test
        void shouldGenerateAndSaveSdoOrder_whenStateClaimIssuedSmallClaimsSDOInPersonHearing() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDraft()
                .atStateClaimIssuedSmallClaimsSDOInPersonHearing().build();

            CallbackParams params = callbackParamsOf(CallbackVersion.V_1, caseData, MID, PAGE_ID);
            CaseDocument order = CaseDocument.builder().documentLink(
                    Document.builder().documentUrl("url").build())
                .build();
            when(sdoGeneratorService.generate(any(), any())).thenReturn(order);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            assertThat(response.getData()).extracting("sdoOrderDocument").isNotNull();
        }

        @Test
        void shouldGenerateAndSaveSdoOrder_whenStateClaimIssuedSmallClaimsSDOTelephoneHearing() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDraft()
                .atStateClaimIssuedSmallClaimsSDOTelephoneHearing().build();

            CallbackParams params = callbackParamsOf(CallbackVersion.V_1, caseData, MID, PAGE_ID);
            CaseDocument order = CaseDocument.builder().documentLink(
                    Document.builder().documentUrl("url").build())
                .build();
            when(sdoGeneratorService.generate(any(), any())).thenReturn(order);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            assertThat(response.getData()).extracting("sdoOrderDocument").isNotNull();
        }

        @Test
        void shouldGenerateAndSaveSdoOrder_whenStateClaimIssuedSmallClaimsSDOVideoHearing() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDraft()
                .atStateClaimIssuedSmallClaimsSDOVideoHearing().build();

            CallbackParams params = callbackParamsOf(CallbackVersion.V_1, caseData, MID, PAGE_ID);
            CaseDocument order = CaseDocument.builder().documentLink(
                    Document.builder().documentUrl("url").build())
                .build();
            when(sdoGeneratorService.generate(any(), any())).thenReturn(order);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            assertThat(response.getData()).extracting("sdoOrderDocument").isNotNull();
        }

        @Test
        void shouldAssignCategoryId_whenInvoked() {
            when(featureToggleService.isCaseFileViewEnabled()).thenReturn(true);
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDraft().build();

            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);
            CaseDocument order = CaseDocument.builder().documentLink(
                    Document.builder().documentUrl("url").build())
                .build();
            when(sdoGeneratorService.generate(any(), any())).thenReturn(order);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            CaseData updatedData = objectMapper.convertValue(response.getData(), CaseData.class);
            assertThat(updatedData.getSdoOrderDocument().getDocumentLink().getCategoryID()).isEqualTo("sdo");
        }

    }

    @Nested
    class SubmittedCallback {
        @Test
        void shouldReturnExpectedSubmittedCallbackResponse_1v1() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build();
            CallbackParams params = callbackParamsOf(caseData, SUBMITTED);
            SubmittedCallbackResponse response = (SubmittedCallbackResponse) handler.handle(params);

            String header = format(
                CONFIRMATION_HEADER,
                REFERENCE_NUMBER
            );

            String body = format(
                CONFIRMATION_SUMMARY_1v1,
                "Mr. John Rambo",
                "Mr. Sole Trader"
            ) + format(FEEDBACK_LINK, "Feedback: Please provide judicial feedback");

            assertThat(response).usingRecursiveComparison().isEqualTo(
                SubmittedCallbackResponse.builder()
                    .confirmationHeader(header)
                    .confirmationBody(body)
                    .build());
        }

        @Test
        void shouldReturnExpectedSubmittedCallbackResponse_1v2() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimDetailsNotified()
                .multiPartyClaimTwoDefendantSolicitors()
                .build();
            CallbackParams params = callbackParamsOf(caseData, SUBMITTED);
            SubmittedCallbackResponse response = (SubmittedCallbackResponse) handler.handle(params);

            String header = format(
                CONFIRMATION_HEADER,
                REFERENCE_NUMBER
            );

            String body = format(
                CONFIRMATION_SUMMARY_1v2,
                "Mr. John Rambo",
                "Mr. Sole Trader",
                "Mr. John Rambo"
            ) + format(FEEDBACK_LINK, "Feedback: Please provide judicial feedback");

            assertThat(response).usingRecursiveComparison().isEqualTo(
                SubmittedCallbackResponse.builder()
                    .confirmationHeader(header)
                    .confirmationBody(body)
                    .build());
        }

        @Test
        void shouldReturnExpectedSubmittedCallbackResponse_2v1() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimDetailsNotified()
                .multiPartyClaimTwoApplicants()
                .build();
            CallbackParams params = callbackParamsOf(caseData, SUBMITTED);
            SubmittedCallbackResponse response = (SubmittedCallbackResponse) handler.handle(params);

            String header = format(
                CONFIRMATION_HEADER,
                REFERENCE_NUMBER
            );

            String body = format(
                CONFIRMATION_SUMMARY_2v1,
                "Mr. John Rambo",
                "Mr. Jason Rambo",
                "Mr. Sole Trader"
            ) + format(FEEDBACK_LINK, "Feedback: Please provide judicial feedback");

            assertThat(response).usingRecursiveComparison().isEqualTo(
                SubmittedCallbackResponse.builder()
                    .confirmationHeader(header)
                    .confirmationBody(body)
                    .build());
        }
    }

    @Test
    void handleEventsReturnsTheExpectedCallbackEvent() {
        assertThat(handler.handledEvents()).contains(CREATE_SDO);
    }
}
