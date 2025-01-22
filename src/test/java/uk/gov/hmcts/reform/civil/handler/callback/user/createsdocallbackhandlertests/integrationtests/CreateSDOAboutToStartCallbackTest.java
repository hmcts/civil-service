package uk.gov.hmcts.reform.civil.handler.callback.user.createsdocallbackhandlertests.integrationtests;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.autoconfigure.validation.ValidationAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.civil.bankholidays.WorkingDayIndicator;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CallbackVersion;
import uk.gov.hmcts.reform.civil.config.ClaimUrlsConfiguration;
import uk.gov.hmcts.reform.civil.config.MockDatabaseConfiguration;
import uk.gov.hmcts.reform.civil.constants.SdoR2UiConstantFastTrack;
import uk.gov.hmcts.reform.civil.constants.SdoR2UiConstantSmallClaim;
import uk.gov.hmcts.reform.civil.crd.model.Category;
import uk.gov.hmcts.reform.civil.crd.model.CategorySearchResult;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;
import uk.gov.hmcts.reform.civil.enums.DecisionOnRequestReconsiderationOptions;
import uk.gov.hmcts.reform.civil.enums.sdo.ClaimsTrack;
import uk.gov.hmcts.reform.civil.enums.sdo.FastTrack;
import uk.gov.hmcts.reform.civil.enums.sdo.HearingMethod;
import uk.gov.hmcts.reform.civil.enums.sdo.OrderType;
import uk.gov.hmcts.reform.civil.enums.sdo.PhysicalTrialBundleOptions;
import uk.gov.hmcts.reform.civil.enums.sdo.SmallTrack;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.handler.callback.user.CreateSDOCallbackHandler;
import uk.gov.hmcts.reform.civil.handler.callback.user.createsdocallbackhandlertests.CreateSDOCallbackHandlerTestConfig;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.helpers.LocationHelper;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.SDOHearingNotes;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.common.DynamicListElement;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.CaseLocationCivil;
import uk.gov.hmcts.reform.civil.model.dq.Applicant1DQ;
import uk.gov.hmcts.reform.civil.model.dq.RequestedCourt;
import uk.gov.hmcts.reform.civil.model.dq.Respondent1DQ;
import uk.gov.hmcts.reform.civil.model.sdo.DisposalHearingAddNewDirections;
import uk.gov.hmcts.reform.civil.model.sdo.FastTrackAddNewDirections;
import uk.gov.hmcts.reform.civil.model.sdo.FastTrackAllocation;
import uk.gov.hmcts.reform.civil.model.sdo.FastTrackHearingNotes;
import uk.gov.hmcts.reform.civil.model.sdo.SmallClaimsAddNewDirections;
import uk.gov.hmcts.reform.civil.referencedata.model.LocationRefData;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.sampledata.LocationRefSampleDataBuilder;
import uk.gov.hmcts.reform.civil.service.CategoryService;
import uk.gov.hmcts.reform.civil.service.DeadlinesCalculator;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.docmosis.sdo.SdoGeneratorService;
import uk.gov.hmcts.reform.civil.service.referencedata.LocationReferenceDataService;
import uk.gov.hmcts.reform.civil.utils.AssignCategoryId;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.civil.constants.SdoR2UiConstantFastTrack.DEADLINE;
import static uk.gov.hmcts.reform.civil.constants.SdoR2UiConstantFastTrack.DEADLINE_EVIDENCE;
import static uk.gov.hmcts.reform.civil.constants.SdoR2UiConstantFastTrack.IMPORTANT_NOTES;
import static uk.gov.hmcts.reform.civil.constants.SdoR2UiConstantFastTrack.INSPECTION;
import static uk.gov.hmcts.reform.civil.constants.SdoR2UiConstantFastTrack.PECUNIARY_LOSS;
import static uk.gov.hmcts.reform.civil.constants.SdoR2UiConstantFastTrack.PHYSICAL_TRIAL_BUNDLE;
import static uk.gov.hmcts.reform.civil.constants.SdoR2UiConstantFastTrack.REQUEST_COMPILED_WITH;
import static uk.gov.hmcts.reform.civil.constants.SdoR2UiConstantFastTrack.RESTRICT_NUMBER_PAGES_TEXT1;
import static uk.gov.hmcts.reform.civil.constants.SdoR2UiConstantFastTrack.RESTRICT_NUMBER_PAGES_TEXT2;
import static uk.gov.hmcts.reform.civil.constants.SdoR2UiConstantFastTrack.RESTRICT_WITNESS_TEXT;
import static uk.gov.hmcts.reform.civil.constants.SdoR2UiConstantFastTrack.SCHEDULE_OF_LOSS_CLAIMANT;
import static uk.gov.hmcts.reform.civil.constants.SdoR2UiConstantFastTrack.SCHEDULE_OF_LOSS_DEFENDANT;
import static uk.gov.hmcts.reform.civil.constants.SdoR2UiConstantFastTrack.STANDARD_DISCLOSURE;
import static uk.gov.hmcts.reform.civil.constants.SdoR2UiConstantFastTrack.STATEMENT_WITNESS;
import static uk.gov.hmcts.reform.civil.constants.SdoR2UiConstantFastTrack.WELSH_LANG_DESCRIPTION;
import static uk.gov.hmcts.reform.civil.enums.CaseCategory.SPEC_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.CaseCategory.UNSPEC_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.CaseState.CASE_PROGRESSION;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.enums.sdo.FastTrackHearingTimeEstimate.FIVE_HOURS;
import static uk.gov.hmcts.reform.civil.enums.sdo.TrialOnRadioOptions.OPEN_DATE;

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
public class CreateSDOAboutToStartCallbackTest extends BaseCallbackHandlerTest {

    @MockBean
    protected LocationReferenceDataService locationRefDataService;

    @MockBean
    private WorkingDayIndicator workingDayIndicator;

    @MockBean
    private DeadlinesCalculator deadlinesCalculator;

    @MockBean
    private CategoryService categoryService;

    @MockBean
    private SdoGeneratorService sdoGeneratorService;

    @MockBean
    private FeatureToggleService featureToggleService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CreateSDOCallbackHandler handler;

    @Nested
    class AboutToStartCallback extends LocationRefSampleDataBuilder {
        private final String trialFirstOpenDateAfter = LocalDate.now().plusDays(434).toString();
        private final String trialWindowFrom = LocalDate.now().plusDays(434).toString();
        private final String trialWindowTo = LocalDate.now().plusDays(455).toString();
        private final String importantNotesDate = LocalDate.now().plusDays(7).toString();
        private final String disclosureStandardDate = LocalDate.now().plusDays(28).toString();
        private final String disclosureInspectionDate = LocalDate.now().plusDays(42).toString();

        @BeforeEach
        void setup() {
            given(locationRefDataService.getHearingCourtLocations(any())).willReturn(getSampleCourLocationsRefObject());
            when(workingDayIndicator.getNextWorkingDay(LocalDate.now())).thenReturn(LocalDate.now().plusDays(1));
            when(deadlinesCalculator.plusWorkingDays(LocalDate.now(), 5)).thenReturn(LocalDate.now().plusDays(5));
            when(deadlinesCalculator.getOrderSetAsideOrVariedApplicationDeadline(ArgumentMatchers.any(LocalDateTime.class)))
                    .thenReturn(LocalDate.now().plusDays(7));
        }

        private Category createCategory() {
            return Category.builder().categoryKey("HearingChannel").key("INTER").valueEn("In Person").activeFlag("Y").build();
        }

        private CategorySearchResult createCategorySearchResult() {
            return CategorySearchResult.builder().categories(List.of(createCategory())).build();
        }

        private List<LocationRefData> createLocations() {
            return List.of(
                    LocationRefData.builder().epimmsId("00001").courtLocationCode("00001").siteName("court 1").courtAddress("1 address").postcode("Y01 7RB").build(),
                    LocationRefData.builder().epimmsId("214320").courtLocationCode("214320").siteName("court 2").courtAddress("2 address").postcode("Y02 7RB").build(),
                    LocationRefData.builder().epimmsId("00003").courtLocationCode("00003").siteName("court 3").courtAddress("3 address").postcode("Y03 7RB").build()
            );
        }

        private void mockCategoryService() {
            when(categoryService.findCategoryByCategoryIdAndServiceId(any(), any(), any())).thenReturn(Optional.of(createCategorySearchResult()));
        }

        private void mockLocationRefDataService() {
            when(locationRefDataService.getHearingCourtLocations(anyString())).thenReturn(createLocations());
        }

        private AboutToStartOrSubmitCallbackResponse handleCallback(CaseData caseData) {
            CallbackParams params = callbackParamsOf(CallbackVersion.V_1, caseData, ABOUT_TO_START);
            CaseDocument order = CaseDocument.builder().documentLink(Document.builder().documentUrl("url").build()).build();
            when(sdoGeneratorService.generate(any(), any())).thenReturn(order);
            return (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
        }

        private DynamicList buildExpectedHearingCourtLocationList() {
            return DynamicList.builder()
                    .value(DynamicListElement.builder().code("214320").label("court 2 - 2 address - Y02 7RB").build())
                    .listItems(List.of(
                            DynamicListElement.builder().code("214320").label("court 2 - 2 address - Y02 7RB").build(),
                            DynamicListElement.builder().code("OTHER_LOCATION").label("Other location").build()
                    ))
                    .build();
        }

        private DynamicList buildExpectedAltHearingCourtLocationList() {
            List<DynamicListElement> items = createLocations().stream()
                    .map(loc -> DynamicListElement.builder()
                            .code(loc.getCourtLocationCode())
                            .label(loc.getSiteName() + " - " + loc.getCourtAddress() + " - " + loc.getPostcode())
                            .build())
                    .toList();
            return DynamicList.builder().listItems(items).build();
        }

        @Test
        void shouldPopulateLocationListsWithPreselectedCourt() {
            mockLocationRefDataService();
            mockCategoryService();
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDraft()
                    .atStateClaimIssuedDisposalHearingSDOInPersonHearing()
                    .caseAccessCategory(UNSPEC_CLAIM)
                    .build();
            AboutToStartOrSubmitCallbackResponse response = handleCallback(caseData);
            CaseData responseCaseData = objectMapper.convertValue(response.getData(), CaseData.class);
            String preSelectedCourt = "214320";
            DynamicList expected = DynamicList.builder()
                    .listItems(List.of(
                            DynamicListElement.builder().code("00001").label("court 1 - 1 address - Y01 7RB").build(),
                            DynamicListElement.builder().code(preSelectedCourt).label("court 2 - 2 address - Y02 7RB").build(),
                            DynamicListElement.builder().code("00003").label("court 3 - 3 address - Y03 7RB").build()
                    ))
                    .value(DynamicListElement.builder().code(preSelectedCourt).label("court 2 - 2 address - Y02 7RB").build())
                    .build();

            assertThat(responseCaseData.getDisposalHearingMethodInPerson()).isEqualTo(expected);
            assertThat(responseCaseData.getFastTrackMethodInPerson()).isEqualTo(expected);
            assertThat(responseCaseData.getSmallClaimsMethodInPerson()).isEqualTo(expected);
        }

        @Test
        void shouldGenerateDynamicListsCorrectly() {
            mockCategoryService();
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDraft()
                    .atStateClaimIssuedDisposalHearingSDOInPersonHearing().build();
            AboutToStartOrSubmitCallbackResponse response = handleCallback(caseData);
            CaseData responseCaseData = objectMapper.convertValue(response.getData(), CaseData.class);
            DynamicList hearingMethodValuesFastTrack = responseCaseData.getHearingMethodValuesFastTrack();
            DynamicList hearingMethodValuesDisposalHearing = responseCaseData.getHearingMethodValuesDisposalHearing();
            DynamicList hearingMethodValuesSmallClaims = responseCaseData.getHearingMethodValuesSmallClaims();

            assertThat(hearingMethodValuesFastTrack.getListItems()).extracting(DynamicListElement::getLabel).containsOnly("In Person");
            assertThat(hearingMethodValuesDisposalHearing.getListItems()).extracting(DynamicListElement::getLabel).containsOnly("In Person");
            assertThat(hearingMethodValuesSmallClaims.getListItems()).extracting(DynamicListElement::getLabel).containsOnly("In Person");

        }

        @Test
        void shouldClearDataIfStateIsCaseProgression() {
            when(featureToggleService.isSdoR2Enabled()).thenReturn(true);
            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
                    .drawDirectionsOrderRequired(YES)
                    .drawDirectionsOrderSmallClaims(YES)
                    .fastClaims(List.of(FastTrack.fastClaimBuildingDispute))
                    .smallClaims(List.of(SmallTrack.smallClaimCreditHire))
                    .claimsTrack(ClaimsTrack.smallClaimsTrack)
                    .orderType(OrderType.DECIDE_DAMAGES)
                    .trialAdditionalDirectionsForFastTrack(List.of(FastTrack.fastClaimBuildingDispute))
                    .drawDirectionsOrderSmallClaimsAdditionalDirections(List.of(SmallTrack.smallClaimCreditHire))
                    .fastTrackAllocation(FastTrackAllocation.builder().assignComplexityBand(YES).build())
                    .disposalHearingAddNewDirections(List.of(Element.<DisposalHearingAddNewDirections>builder()
                            .value(DisposalHearingAddNewDirections.builder().directionComment("test").build())
                            .build()))
                    .smallClaimsAddNewDirections(List.of(Element.<SmallClaimsAddNewDirections>builder()
                            .value(SmallClaimsAddNewDirections.builder().directionComment("test").build())
                            .build()))
                    .fastTrackAddNewDirections(List.of(Element.<FastTrackAddNewDirections>builder()
                            .value(FastTrackAddNewDirections.builder().directionComment("test").build())
                            .build()))
                    .sdoHearingNotes(SDOHearingNotes.builder().input("TEST").build())
                    .fastTrackHearingNotes(FastTrackHearingNotes.builder().input("TEST").build())
                    .disposalHearingHearingNotes("TEST")
                    .ccdState(CASE_PROGRESSION)
                    .decisionOnRequestReconsiderationOptions(DecisionOnRequestReconsiderationOptions.CREATE_SDO)
                    .isSdoR2NewScreen(NO)
                    .build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_START);
            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            CaseData responseCaseData = objectMapper.convertValue(response.getData(), CaseData.class);

            assertThat(responseCaseData.getDrawDirectionsOrderRequired()).isNull();
            assertThat(responseCaseData.getDrawDirectionsOrderSmallClaims()).isNull();
            assertThat(responseCaseData.getFastClaims()).isNull();
            assertThat(responseCaseData.getSmallClaims()).isNull();
            assertThat(responseCaseData.getClaimsTrack()).isNull();
            assertThat(responseCaseData.getOrderType()).isNull();
            assertThat(responseCaseData.getTrialAdditionalDirectionsForFastTrack()).isNull();
            assertThat(responseCaseData.getDrawDirectionsOrderSmallClaimsAdditionalDirections()).isNull();
            assertThat(responseCaseData.getFastTrackAllocation()).isNull();
            assertThat(responseCaseData.getDisposalHearingAddNewDirections()).isNull();
            assertThat(responseCaseData.getSmallClaimsAddNewDirections()).isNull();
            assertThat(responseCaseData.getFastTrackAddNewDirections()).isNull();
            assertThat(responseCaseData.getSdoHearingNotes()).isNull();
            assertThat(responseCaseData.getFastTrackHearingNotes()).isNull();

        }

        @Test
        void shouldPopulateHearingCourtLocationForNihl() {
            when(featureToggleService.isSdoR2Enabled()).thenReturn(true);
            mockLocationRefDataService();
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDraft().build().toBuilder()
                    .drawDirectionsOrderRequired(NO)
                    .claimsTrack(ClaimsTrack.fastTrack)
                    .fastClaims(List.of(FastTrack.fastClaimNoiseInducedHearingLoss))
                    .build();
            AboutToStartOrSubmitCallbackResponse response = handleCallback(caseData);
            CaseData responseCaseData = objectMapper.convertValue(response.getData(), CaseData.class);
            DynamicList expectedHearingCourtLocationList = buildExpectedHearingCourtLocationList();
            DynamicList expectedAltHearingCourtLocationList = buildExpectedAltHearingCourtLocationList();

            assertThat(responseCaseData.getSdoR2Trial().getHearingCourtLocationList()).isEqualTo(expectedHearingCourtLocationList);
            assertThat(responseCaseData.getSdoR2Trial().getAltHearingCourtLocationList()).isEqualTo(expectedAltHearingCourtLocationList);
        }

        @Test
        void shouldPopulateDefaultFieldsForNihl() {
            when(featureToggleService.isSdoR2Enabled()).thenReturn(true);
            List<FastTrack> fastTrackList = List.of(FastTrack.fastClaimNoiseInducedHearingLoss);
            mockLocationRefDataService();
            mockCategoryService();
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDraft().build().toBuilder()
                    .drawDirectionsOrderRequired(NO)
                    .claimsTrack(ClaimsTrack.fastTrack)
                    .fastClaims(fastTrackList)
                    .build();
            AboutToStartOrSubmitCallbackResponse response = handleCallback(caseData);
            CaseData responseCaseData = objectMapper.convertValue(response.getData(), CaseData.class);

            assertThat(responseCaseData.getSdoR2Trial().getTrialOnOptions()).isEqualTo(OPEN_DATE);
            assertThat(responseCaseData.getSdoR2Trial().getLengthList()).isEqualTo(FIVE_HOURS);
            assertThat(responseCaseData.getSdoR2Trial().getMethodOfHearing().getValue().getLabel()).isEqualTo(HearingMethod.IN_PERSON.getLabel());
            assertThat(responseCaseData.getSdoR2Trial().getPhysicalBundleOptions()).isEqualTo(PhysicalTrialBundleOptions.PARTY);
            assertThat(responseCaseData.getSdoR2Trial().getSdoR2TrialFirstOpenDateAfter().getListFrom()).isEqualTo(trialFirstOpenDateAfter);
            assertThat(responseCaseData.getSdoR2Trial().getSdoR2TrialWindow().getListFrom()).isEqualTo(trialWindowFrom);
            assertThat(responseCaseData.getSdoR2Trial().getSdoR2TrialWindow().getDateTo()).isEqualTo(trialWindowTo);
            assertThat(responseCaseData.getSdoR2Trial().getHearingCourtLocationList()).isEqualTo(buildExpectedHearingCourtLocationList());
            assertThat(responseCaseData.getSdoR2Trial().getAltHearingCourtLocationList()).isEqualTo(buildExpectedAltHearingCourtLocationList());
            assertThat(responseCaseData.getSdoR2Trial().getPhysicalBundlePartyTxt()).isEqualTo(PHYSICAL_TRIAL_BUNDLE);
            assertThat(responseCaseData.getSdoR2ImportantNotesTxt()).isEqualTo(IMPORTANT_NOTES);
            assertThat(responseCaseData.getSdoR2ImportantNotesDate()).isEqualTo(importantNotesDate);
            assertThat(responseCaseData.getSdoR2DisclosureOfDocuments().getStandardDisclosureTxt()).isEqualTo(STANDARD_DISCLOSURE);
            assertThat(responseCaseData.getSdoR2DisclosureOfDocuments().getStandardDisclosureDate()).isEqualTo(disclosureStandardDate);
            assertThat(responseCaseData.getSdoR2DisclosureOfDocuments().getInspectionTxt()).isEqualTo(INSPECTION);
            assertThat(responseCaseData.getSdoR2DisclosureOfDocuments().getInspectionDate()).isEqualTo(disclosureInspectionDate);
            assertThat(responseCaseData.getSdoR2DisclosureOfDocuments().getRequestsWillBeCompiledLabel()).isEqualTo(REQUEST_COMPILED_WITH);
            assertThat(responseCaseData.getSdoR2WitnessesOfFact().getSdoStatementOfWitness()).isEqualTo(STATEMENT_WITNESS);
            assertThat(responseCaseData.getSdoR2WitnessesOfFact().getSdoR2RestrictWitness().getIsRestrictWitness().toString()).isEqualTo("NO");
            assertThat(responseCaseData.getSdoR2WitnessesOfFact().getSdoR2RestrictWitness().getRestrictNoOfWitnessDetails().getNoOfWitnessClaimant().toString()).isEqualTo("3");
            assertThat(responseCaseData.getSdoR2WitnessesOfFact().getSdoR2RestrictWitness().getRestrictNoOfWitnessDetails().getNoOfWitnessDefendant().toString()).isEqualTo("3");
            assertThat(responseCaseData.getSdoR2WitnessesOfFact().getSdoR2RestrictWitness().getRestrictNoOfWitnessDetails()
                    .getPartyIsCountedAsWitnessTxt()).isEqualTo(RESTRICT_WITNESS_TEXT);
            assertThat(responseCaseData.getSdoR2WitnessesOfFact().getSdoRestrictPages().getIsRestrictPages().toString()).isEqualTo("NO");
            assertThat(responseCaseData.getSdoR2WitnessesOfFact().getSdoRestrictPages().getRestrictNoOfPagesDetails()
                    .getWitnessShouldNotMoreThanTxt()).isEqualTo(RESTRICT_NUMBER_PAGES_TEXT1);
            assertThat(responseCaseData.getSdoR2WitnessesOfFact().getSdoRestrictPages().getRestrictNoOfPagesDetails().getNoOfPages().toString()).isEqualTo("12");
            assertThat(responseCaseData.getSdoR2WitnessesOfFact().getSdoRestrictPages().getRestrictNoOfPagesDetails().getFontDetails()).isEqualTo(RESTRICT_NUMBER_PAGES_TEXT2);
            assertThat(responseCaseData.getSdoR2WitnessesOfFact().getSdoWitnessDeadline()).isEqualTo(DEADLINE);
            assertThat(responseCaseData.getSdoR2WitnessesOfFact().getSdoWitnessDeadlineDate()).isEqualTo(LocalDate.now().plusDays(70).toString());
            assertThat(responseCaseData.getSdoR2WitnessesOfFact().getSdoWitnessDeadlineText()).isEqualTo(DEADLINE_EVIDENCE);
            assertThat(responseCaseData.getSdoR2ScheduleOfLoss().getSdoR2ScheduleOfLossClaimantText()).isEqualTo(SCHEDULE_OF_LOSS_CLAIMANT);
            assertThat(responseCaseData.getSdoR2ScheduleOfLoss().getIsClaimForPecuniaryLoss().toString()).isEqualTo("NO");
            assertThat(responseCaseData.getSdoR2ScheduleOfLoss().getSdoR2ScheduleOfLossClaimantDate()).isEqualTo(LocalDate.now().plusDays(364).toString());
            assertThat(responseCaseData.getSdoR2ScheduleOfLoss().getSdoR2ScheduleOfLossDefendantText()).isEqualTo(SCHEDULE_OF_LOSS_DEFENDANT);
            assertThat(responseCaseData.getSdoR2ScheduleOfLoss().getSdoR2ScheduleOfLossDefendantDate()).isEqualTo(LocalDate.now().plusDays(378).toString());
            assertThat(responseCaseData.getSdoR2ScheduleOfLoss().getSdoR2ScheduleOfLossPecuniaryLossTxt()).isEqualTo(PECUNIARY_LOSS);
        }

        @Test
        void shouldPrePopulateUpdatedWitnessSectionsForSDOR2() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDraft()
                    .atStateClaimIssuedDisposalHearingSDOInPersonHearing().build();
            when(featureToggleService.isSdoR2Enabled()).thenReturn(true);
            AboutToStartOrSubmitCallbackResponse response = handleCallback(caseData);
            CaseData data = objectMapper.convertValue(response.getData(), CaseData.class);

            assertThat(data.getSdoR2SmallClaimsWitnessStatementOther().getText()).isEqualTo(SdoR2UiConstantSmallClaim.WITNESS_DESCRIPTION_TEXT);
            assertThat(data.getSdoR2SmallClaimsWitnessStatementOther().getSdoStatementOfWitness())
                    .isEqualTo("Each party must upload to the Digital Portal copies of all witness statements of the witnesses upon whose evidence they intend to rely at" +
                            " the hearing not less than 21 days before the hearing.");
            assertThat(data.getSdoR2SmallClaimsWitnessStatementOther().getIsRestrictWitness()).isEqualTo(NO);
            assertThat(data.getSdoR2SmallClaimsWitnessStatementOther().getIsRestrictPages()).isEqualTo(NO);
            assertThat(data.getSdoR2SmallClaimsWitnessStatementOther().getSdoR2SmallClaimsRestrictWitness()
                    .getPartyIsCountedAsWitnessTxt()).isEqualTo(SdoR2UiConstantSmallClaim.RESTRICT_WITNESS_TEXT);
            assertThat(data.getSdoR2SmallClaimsWitnessStatementOther().getSdoR2SmallClaimsRestrictPages()
                    .getFontDetails()).isEqualTo(SdoR2UiConstantSmallClaim.RESTRICT_NUMBER_PAGES_TEXT2);
            assertThat(data.getSdoR2SmallClaimsWitnessStatementOther().getSdoR2SmallClaimsRestrictPages()
                    .getWitnessShouldNotMoreThanTxt()).isEqualTo(SdoR2UiConstantSmallClaim.RESTRICT_NUMBER_PAGES_TEXT1);
            assertThat(data.getSdoR2SmallClaimsWitnessStatementOther().getSdoR2SmallClaimsRestrictPages().getNoOfPages()).isEqualTo(12);
            assertThat(data.getSdoR2FastTrackWitnessOfFact().getSdoStatementOfWitness()).isEqualTo(SdoR2UiConstantFastTrack.STATEMENT_WITNESS);
            assertThat(data.getSdoR2FastTrackWitnessOfFact().getSdoR2RestrictWitness().getIsRestrictWitness()).isEqualTo(NO);
            assertThat(data.getSdoR2FastTrackWitnessOfFact().getSdoR2RestrictWitness().getRestrictNoOfWitnessDetails().getNoOfWitnessClaimant()).isEqualTo(3);
            assertThat(data.getSdoR2FastTrackWitnessOfFact().getSdoR2RestrictWitness().getRestrictNoOfWitnessDetails().getNoOfWitnessDefendant()).isEqualTo(3);
            assertThat(data.getSdoR2FastTrackWitnessOfFact().getSdoR2RestrictWitness().getRestrictNoOfWitnessDetails()
                    .getPartyIsCountedAsWitnessTxt()).isEqualTo(SdoR2UiConstantFastTrack.RESTRICT_WITNESS_TEXT);
            assertThat(data.getSdoR2FastTrackWitnessOfFact().getSdoRestrictPages().getIsRestrictPages()).isEqualTo(NO);
            assertThat(data.getSdoR2FastTrackWitnessOfFact().getSdoRestrictPages().getRestrictNoOfPagesDetails()
                    .getWitnessShouldNotMoreThanTxt()).isEqualTo(SdoR2UiConstantFastTrack.RESTRICT_NUMBER_PAGES_TEXT1);
            assertThat(data.getSdoR2FastTrackWitnessOfFact().getSdoRestrictPages().getRestrictNoOfPagesDetails().getNoOfPages()).isEqualTo(12);
            assertThat(data.getSdoR2FastTrackWitnessOfFact().getSdoRestrictPages().getRestrictNoOfPagesDetails()
                    .getFontDetails()).isEqualTo(SdoR2UiConstantFastTrack.RESTRICT_NUMBER_PAGES_TEXT2);
            assertThat(data.getSdoR2FastTrackWitnessOfFact().getSdoWitnessDeadline()).isEqualTo(SdoR2UiConstantFastTrack.DEADLINE);
            assertThat(data.getSdoR2FastTrackWitnessOfFact().getSdoWitnessDeadlineDate()).isEqualTo(LocalDate.now().plusDays(70));
            assertThat(data.getSdoR2FastTrackWitnessOfFact().getSdoWitnessDeadlineText()).isEqualTo(SdoR2UiConstantFastTrack.DEADLINE_EVIDENCE);
        }

        @ParameterizedTest
        @ValueSource(booleans = {true, false})
        void shouldPopulateWelshSectionForSDOR2(boolean valid) {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued().build().toBuilder()
                    .claimsTrack(ClaimsTrack.smallClaimsTrack)
                    .drawDirectionsOrderRequired(NO)
                    .build();
            when(featureToggleService.isSdoR2Enabled()).thenReturn(valid);
            AboutToStartOrSubmitCallbackResponse response = handleCallback(caseData);
            CaseData responseCaseData = objectMapper.convertValue(response.getData(), CaseData.class);

            if (valid) {
                assertThat(responseCaseData.getSdoR2FastTrackUseOfWelshLanguage().getDescription()).isEqualTo(WELSH_LANG_DESCRIPTION);
                assertThat(responseCaseData.getSdoR2SmallClaimsUseOfWelshLanguage().getDescription()).isEqualTo(WELSH_LANG_DESCRIPTION);
                assertThat(responseCaseData.getSdoR2DisposalHearingUseOfWelshLanguage().getDescription()).isEqualTo(WELSH_LANG_DESCRIPTION);
                assertThat(responseCaseData.getSdoR2DrhUseOfWelshLanguage().getDescription()).isEqualTo(WELSH_LANG_DESCRIPTION);
                assertThat(responseCaseData.getSdoR2NihlUseOfWelshLanguage().getDescription()).isEqualTo(WELSH_LANG_DESCRIPTION);
            } else {
                assertThat(responseCaseData.getSdoR2FastTrackUseOfWelshLanguage()).isNull();
                assertThat(responseCaseData.getSdoR2SmallClaimsUseOfWelshLanguage()).isNull();
                assertThat(responseCaseData.getSdoR2DisposalHearingUseOfWelshLanguage()).isNull();
                assertThat(responseCaseData.getSdoR2DrhUseOfWelshLanguage()).isNull();
                assertThat(responseCaseData.getSdoR2NihlUseOfWelshLanguage()).isNull();
            }
        }

        @Test
        void shouldUpdateCaseManagementLocation_whenUnder1000SpecCcmcc() {
            String ccmccEpimsId = handler.ccmccEpimsId;
            CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued().build().toBuilder()
                    .caseAccessCategory(SPEC_CLAIM)
                    .caseManagementLocation(CaseLocationCivil.builder().baseLocation(ccmccEpimsId).region("ccmcRegion").build())
                    .totalClaimAmount(BigDecimal.valueOf(999))
                    .claimsTrack(ClaimsTrack.smallClaimsTrack)
                    .applicant1(Party.builder().type(Party.Type.INDIVIDUAL).build())
                    .applicant1DQ(Applicant1DQ.builder()
                            .applicant1DQRequestedCourt(RequestedCourt.builder()
                                    .caseLocation(CaseLocationCivil.builder().baseLocation("app court requested epimm").region("app court request region").build())
                                    .responseCourtCode("123").build())
                            .build())
                    .respondent1(Party.builder().type(Party.Type.INDIVIDUAL).build())
                    .respondent1DQ(Respondent1DQ.builder()
                            .respondent1DQRequestedCourt(RequestedCourt.builder()
                                    .caseLocation(CaseLocationCivil.builder().baseLocation("def court requested epimm").region("def court request region").build())
                                    .responseCourtCode("321").build())
                            .build())
                    .build();
            AboutToStartOrSubmitCallbackResponse response = handleCallback(caseData);
            CaseData responseCaseData = objectMapper.convertValue(response.getData(), CaseData.class);

            assertThat(responseCaseData.getCaseManagementLocation().getRegion()).isEqualTo("def court request region");
            assertThat(responseCaseData.getCaseManagementLocation().getBaseLocation()).isEqualTo("def court requested epimm");
        }

        @Test
        void shouldNotUpdateCaseManagementLocation_whenNotUnder1000SpecCcmcc() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued().build().toBuilder()
                    .caseAccessCategory(SPEC_CLAIM)
                    .caseManagementLocation(CaseLocationCivil.builder().baseLocation("1010101").region("orange").build())
                    .totalClaimAmount(BigDecimal.valueOf(1999))
                    .claimsTrack(ClaimsTrack.smallClaimsTrack)
                    .applicant1(Party.builder().type(Party.Type.INDIVIDUAL).build())
                    .applicant1DQ(Applicant1DQ.builder()
                            .applicant1DQRequestedCourt(RequestedCourt.builder()
                                    .caseLocation(CaseLocationCivil.builder().baseLocation("app court requested epimm").region("app court request region").build())
                                    .responseCourtCode("123").build())
                            .build())
                    .respondent1(Party.builder().type(Party.Type.INDIVIDUAL).build())
                    .respondent1DQ(Respondent1DQ.builder()
                            .respondent1DQRequestedCourt(RequestedCourt.builder()
                                    .caseLocation(CaseLocationCivil.builder().baseLocation("def court requested epimm").region("def court request region").build())
                                    .responseCourtCode("321").build())
                            .build())
                    .build();
            AboutToStartOrSubmitCallbackResponse response = handleCallback(caseData);
            CaseData responseCaseData = objectMapper.convertValue(response.getData(), CaseData.class);

            assertThat(responseCaseData.getCaseManagementLocation().getRegion()).isEqualTo("orange");
            assertThat(responseCaseData.getCaseManagementLocation().getBaseLocation()).isEqualTo("1010101");

        }
    }
}
