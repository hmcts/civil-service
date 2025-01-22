package uk.gov.hmcts.reform.civil.handler.callback.user.createsdocallbackhandlertests.integrationtests;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
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
import uk.gov.hmcts.reform.civil.constants.SdoR2UiConstantSmallClaim;
import uk.gov.hmcts.reform.civil.crd.model.Category;
import uk.gov.hmcts.reform.civil.crd.model.CategorySearchResult;
import uk.gov.hmcts.reform.civil.enums.sdo.HearingMethod;
import uk.gov.hmcts.reform.civil.enums.sdo.HearingOnRadioOptions;
import uk.gov.hmcts.reform.civil.enums.sdo.IncludeInOrderToggle;
import uk.gov.hmcts.reform.civil.enums.sdo.OrderType;
import uk.gov.hmcts.reform.civil.enums.sdo.SmallClaimsSdoR2PhysicalTrialBundleOptions;
import uk.gov.hmcts.reform.civil.enums.sdo.SmallClaimsSdoR2TimeEstimate;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.handler.callback.user.CreateSDOCallbackHandler;
import uk.gov.hmcts.reform.civil.handler.callback.user.createsdocallbackhandlertests.CreateSDOCallbackHandlerTestConfig;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.helpers.LocationHelper;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.common.DynamicListElement;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.CaseLocationCivil;
import uk.gov.hmcts.reform.civil.model.sdo.JudgementSum;
import uk.gov.hmcts.reform.civil.referencedata.model.LocationRefData;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.sampledata.LocationRefSampleDataBuilder;
import uk.gov.hmcts.reform.civil.service.CategoryService;
import uk.gov.hmcts.reform.civil.service.DeadlinesCalculator;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.Time;
import uk.gov.hmcts.reform.civil.service.docmosis.sdo.SdoGeneratorService;
import uk.gov.hmcts.reform.civil.service.referencedata.LocationReferenceDataService;
import uk.gov.hmcts.reform.civil.utils.AssignCategoryId;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;

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
public class CreateSDOMidEventPrePopulateOrderDetailsPagesCallbackTest extends BaseCallbackHandlerTest {

    @MockBean
    private Time time;

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
    private WorkingDayIndicator workingDayIndicator;

    @MockBean
    private DeadlinesCalculator deadlinesCalculator;

    @MockBean
    protected LocationReferenceDataService locationRefDataService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CreateSDOCallbackHandler handler;

    private CategorySearchResult getInPersonCategorySearchResult() {
        Category category = Category.builder()
                .categoryKey("HearingChannel")
                .key("INTER")
                .valueEn("In Person")
                .activeFlag("Y")
                .build();
        return CategorySearchResult.builder().categories(List.of(category)).build();
    }

    private CallbackParams buildParams(CaseData caseData) {
        return callbackParamsOf(caseData, ABOUT_TO_START);
    }

    @Nested
    class MidEventPrePopulateOrderDetailsPagesCallback extends LocationRefSampleDataBuilder {

        private LocalDate nextWorkingDayDate;

        @BeforeEach
        void setup() {
            nextWorkingDayDate = LocalDate.of(2023, 12, 15);
            LocalDateTime localDateTime = LocalDateTime.of(2020, 1, 1, 12, 0, 0);
            when(time.now()).thenReturn(localDateTime);
            when(workingDayIndicator.getNextWorkingDay(any(LocalDate.class))).thenReturn(nextWorkingDayDate);
            LocalDate newDate = LocalDate.of(2020, 1, 15);
            when(deadlinesCalculator.plusWorkingDays(any(LocalDate.class), anyInt())).thenReturn(newDate);
            when(deadlinesCalculator.getOrderSetAsideOrVariedApplicationDeadline(any(LocalDateTime.class))).thenReturn(newDate);
            when(locationRefDataService.getHearingCourtLocations(any()))
                    .thenReturn(getSampleCourLocationsRefObjectToSort());
        }

        private void setupCategoryMocks() {
            when(categoryService.findCategoryByCategoryIdAndServiceId(any(), any(), any()))
                    .thenReturn(Optional.of(getInPersonCategorySearchResult()));
        }

        @Test
        void shouldPrePopulateOrderDetailsPages() {
            setupCategoryMocks();
            given(featureToggleService.isCarmEnabledForCase(any())).willReturn(true);
            CaseData caseData = CaseDataBuilder.builder()
                    .setClaimTypeToSpecClaim()
                    .atStateClaimDraft()
                    .totalClaimAmount(BigDecimal.valueOf(15000))
                    .applicant1DQWithLocation()
                    .caseManagementLocation(CaseLocationCivil.builder().baseLocation("00000").build())
                    .build();
            CallbackParams params = buildParams(caseData);
            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            CaseData data = objectMapper.convertValue(response.getData(), CaseData.class);
            DynamicList dynamicList = getLocationDynamicListInPersonHearing(data);

            assertThat(dynamicList).isNotNull();
            assertThat(locationsFromDynamicList(dynamicList)).containsExactly(
                    "A Site 3 - Adr 3 - AAA 111",
                    "Site 1 - Adr 1 - VVV 111",
                    "Site 2 - Adr 2 - BBB 222",
                    "Site 3 - Adr 3 - CCC 333"
            );
            Optional<LocationRefData> selected = getSampleCourLocationsRefObjectToSort().stream()
                    .filter(loc -> loc.getCourtLocationCode().equals(
                            caseData.getApplicant1DQ().getApplicant1DQRequestedCourt().getResponseCourtCode()))
                    .findFirst();
            assertThat(selected).isPresent();
            assertThat(dynamicList.getValue()).isNotNull()
                    .extracting("label")
                    .isEqualTo(LocationReferenceDataService.getDisplayEntry(selected.get()));

            assertThat(response.getData()).extracting("disposalHearingNotes").extracting("input")
                    .isEqualTo("This Order has been made without a hearing. Each party has the right to apply to have this Order set aside or varied. Any such application must " +
                            "be uploaded to the Digital Portal together with the appropriate fee, by 4pm on");
            assertThat(response.getData()).extracting("showCarmFields").isEqualTo("Yes");
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
            assertThat(response.getData()).extracting("smallClaimsMediationSectionToggle").isNotNull();
            assertThat(response.getData()).extracting("caseManagementLocation").isNotNull();
        }

        @Test
        void shouldPrePopulateOrderDetailsPagesCarmNotEnabled() {
            setupCategoryMocks();
            given(featureToggleService.isCarmEnabledForCase(any())).willReturn(false);
            CaseData caseData = CaseDataBuilder.builder()
                    .setClaimTypeToSpecClaim()
                    .atStateClaimDraft()
                    .totalClaimAmount(BigDecimal.valueOf(15000))
                    .applicant1DQWithLocation().build();
            CallbackParams params = buildParams(caseData);
            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            assertThat(response.getData()).doesNotHaveToString("sdoR2SmallClaimsMediationSectionStatement");
            assertThat(response.getData()).extracting("showCarmFields").isEqualTo("No");
        }

        @Test
        void shouldPrePopulateOrderDetailsPagesWithSmallClaimFlightDelay() {
            setupCategoryMocks();
            when(featureToggleService.isSdoR2Enabled()).thenReturn(true);
            CaseData caseData = CaseDataBuilder.builder()
                    .setClaimTypeToSpecClaim()
                    .atStateClaimDraft()
                    .totalClaimAmount(BigDecimal.valueOf(15000))
                    .applicant1DQWithLocation().build();
            CallbackParams params = buildParams(caseData);
            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            assertThat(response.getData()).extracting("smallClaimsFlightDelay").extracting("relatedClaimsInput")
                    .isEqualTo("In the event that the Claimant(s) or Defendant(s) are aware if other \nclaims relating to the same flight they must notify the court \nwhere the " +
                            "claim is being managed within 14 days of receipt of \nthis Order providing all relevant details of those claims including \ncase number(s), hearing " +
                            "date(s) and copy final substantive order(s) \nif any, to assist the Court with ongoing case management which may \ninclude the cases being heard " +
                            "together.");
            assertThat(response.getData()).extracting("smallClaimsFlightDelay").extracting("legalDocumentsInput")
                    .isEqualTo("Any arguments as to the law to be applied to this claim, together with \ncopies of legal authorities or precedents relied on, shall be uploaded " +
                            "\nto the Digital Portal not later than 3 full working days before the \nfinal hearing date.");
        }

        @Test
        void shouldPrePopulateOrderDetailsPagesWithUpdatedExpertEvidenceDataForR2() {
            setupCategoryMocks();
            when(featureToggleService.isSdoR2Enabled()).thenReturn(true);
            CaseData caseData = CaseDataBuilder.builder()
                    .setClaimTypeToSpecClaim()
                    .atStateClaimDraft()
                    .totalClaimAmount(BigDecimal.valueOf(15000))
                    .applicant1DQWithLocation().build();
            CallbackParams params = buildParams(caseData);
            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            assertThat(response.getData()).extracting("fastTrackPersonalInjury").extracting("input1")
                    .isEqualTo("The Claimant has permission to rely upon the written expert evidence already uploaded to the Digital Portal with the particulars of claim");
            assertThat(response.getData()).extracting("fastTrackPersonalInjury").doesNotHaveToString("date1");
            assertThat(response.getData()).extracting("fastTrackPersonalInjury").extracting("input2")
                    .isEqualTo("The Defendant(s) may ask questions of the Claimant's expert which must be sent to the expert directly and uploaded to the Digital Portal by 4pm " +
                            "on");
            assertThat(response.getData()).extracting("fastTrackPersonalInjury").extracting("date2")
                    .isEqualTo(nextWorkingDayDate.toString());
            assertThat(response.getData()).extracting("fastTrackPersonalInjury").extracting("input3")
                    .isEqualTo("The answers to the questions shall be answered by the Expert by");
            assertThat(response.getData()).extracting("fastTrackPersonalInjury").extracting("date3")
                    .isEqualTo(nextWorkingDayDate.toString());
            assertThat(response.getData()).extracting("fastTrackPersonalInjury").extracting("input4")
                    .isEqualTo("and uploaded to the Digital Portal by the party who has asked the question by");
            assertThat(response.getData()).extracting("fastTrackPersonalInjury").extracting("date4")
                    .isEqualTo(nextWorkingDayDate.toString());
        }

        @Test
        void shouldPrePopulateDRHFields() {
            String preSelectedCourt = "214320";
            List<LocationRefData> locations = List.of(
                    LocationRefData.builder().epimmsId("00001").courtLocationCode("00001")
                            .siteName("court 1").courtAddress("1 address").postcode("Y01 7RB").build(),
                    LocationRefData.builder().epimmsId(preSelectedCourt).courtLocationCode(preSelectedCourt)
                            .siteName("court 2").courtAddress("2 address").postcode("Y02 7RB").build(),
                    LocationRefData.builder().epimmsId("00003").courtLocationCode("00003")
                            .siteName("court 3").courtAddress("3 address").postcode("Y03 7RB").build()
            );

            when(locationRefDataService.getHearingCourtLocations(any(String.class))).thenReturn(locations);
            setupCategoryMocks();
            when(featureToggleService.isSdoR2Enabled()).thenReturn(true);
            given(featureToggleService.isCarmEnabledForCase(any())).willReturn(true);

            CaseData caseData = CaseDataBuilder.builder()
                    .atStateClaimDraft()
                    .atStateClaimIssuedDisposalHearingSDOInPersonHearing()
                    .build();
            CallbackParams params = buildParams(caseData);
            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            CaseData data = objectMapper.convertValue(response.getData(), CaseData.class);
            DynamicList expected = DynamicList.builder()
                    .listItems(List.of(
                            DynamicListElement.builder().code("00001").label("court 1 - 1 address - Y01 7RB").build(),
                            DynamicListElement.builder().code(preSelectedCourt).label("court 2 - 2 address - Y02 7RB").build(),
                            DynamicListElement.builder().code("00003").label("court 3 - 3 address - Y03 7RB").build()
                    ))
                    .build();
            assertThat(response.getData()).extracting("showCarmFields").isEqualTo("Yes");
            assertThat(data.getSdoR2SmallClaimsJudgesRecital().getInput()).isEqualTo(SdoR2UiConstantSmallClaim.JUDGE_RECITAL);
            assertThat(data.getSdoR2SmallClaimsPPI().getPpiDate()).isEqualTo(LocalDate.now().plusDays(21));
            assertThat(data.getSdoR2SmallClaimsPPI().getText()).isEqualTo(SdoR2UiConstantSmallClaim.PPI_DESCRIPTION);
            assertThat(data.getSdoR2SmallClaimsUploadDoc().getSdoUploadOfDocumentsTxt()).isEqualTo(SdoR2UiConstantSmallClaim.UPLOAD_DOC_DESCRIPTION);
            assertThat(data.getSdoR2SmallClaimsWitnessStatements().getText()).isEqualTo(SdoR2UiConstantSmallClaim.WITNESS_DESCRIPTION_TEXT);
            assertThat(data.getSdoR2SmallClaimsWitnessStatements().getSdoStatementOfWitness()).isEqualTo(SdoR2UiConstantSmallClaim.WITNESS_STATEMENT_TEXT);
            assertThat(data.getSdoR2SmallClaimsWitnessStatements().getIsRestrictWitness()).isEqualTo(NO);
            assertThat(data.getSdoR2SmallClaimsWitnessStatements().getIsRestrictPages()).isEqualTo(NO);
            assertThat(data.getSdoR2SmallClaimsWitnessStatements().getSdoR2SmallClaimsRestrictWitness()
                    .getPartyIsCountedAsWitnessTxt()).isEqualTo(SdoR2UiConstantSmallClaim.RESTRICT_WITNESS_TEXT);
            assertThat(data.getSdoR2SmallClaimsWitnessStatements().getSdoR2SmallClaimsRestrictPages().getFontDetails())
                    .isEqualTo(SdoR2UiConstantSmallClaim.RESTRICT_NUMBER_PAGES_TEXT2);
            assertThat(data.getSdoR2SmallClaimsWitnessStatements().getSdoR2SmallClaimsRestrictPages().getWitnessShouldNotMoreThanTxt())
                    .isEqualTo(SdoR2UiConstantSmallClaim.RESTRICT_NUMBER_PAGES_TEXT1);
            assertThat(data.getSdoR2SmallClaimsWitnessStatements().getSdoR2SmallClaimsRestrictPages().getNoOfPages()).isEqualTo(12);
            assertThat(data.getSdoR2SmallClaimsHearing().getTrialOnOptions()).isEqualTo(HearingOnRadioOptions.OPEN_DATE);
            DynamicList hearingMethodValuesDRH = data.getSdoR2SmallClaimsHearing().getMethodOfHearing();
            List<String> hearingMethodValuesDRHActual = hearingMethodValuesDRH.getListItems().stream()
                    .map(DynamicListElement::getLabel)
                    .toList();
            assertThat(hearingMethodValuesDRHActual).containsOnly(HearingMethod.IN_PERSON.getLabel());
            assertThat(data.getSdoR2SmallClaimsHearing().getLengthList()).isEqualTo(SmallClaimsSdoR2TimeEstimate.THIRTY_MINUTES);
            assertThat(data.getSdoR2SmallClaimsHearing().getPhysicalBundleOptions())
                    .isEqualTo(SmallClaimsSdoR2PhysicalTrialBundleOptions.PARTY);
            assertThat(data.getSdoR2SmallClaimsHearing().getSdoR2SmallClaimsHearingFirstOpenDateAfter().getListFrom())
                    .isEqualTo(LocalDate.now().plusDays(56));
            assertThat(data.getSdoR2SmallClaimsHearing().getSdoR2SmallClaimsHearingWindow().getListFrom())
                    .isEqualTo(LocalDate.now().plusDays(56));
            assertThat(data.getSdoR2SmallClaimsHearing().getSdoR2SmallClaimsHearingWindow().getDateTo())
                    .isEqualTo(LocalDate.now().plusDays(70));
            assertThat(data.getSdoR2SmallClaimsHearing().getAltHearingCourtLocationList()).isEqualTo(expected);
            assertThat(data.getSdoR2SmallClaimsHearing().getHearingCourtLocationList().getValue().getCode())
                    .isEqualTo(preSelectedCourt);
            assertThat(data.getSdoR2SmallClaimsHearing().getSdoR2SmallClaimsBundleOfDocs().getPhysicalBundlePartyTxt())
                    .isEqualTo(SdoR2UiConstantSmallClaim.BUNDLE_TEXT);
            assertThat(data.getSdoR2SmallClaimsImpNotes().getText()).isEqualTo(SdoR2UiConstantSmallClaim.IMP_NOTES_TEXT);
            assertThat(data.getSdoR2SmallClaimsImpNotes().getDate()).isEqualTo(LocalDate.now().plusDays(7));
            assertThat(data.getSdoR2SmallClaimsMediationSectionStatement().getInput())
                    .isEqualTo(SdoR2UiConstantSmallClaim.CARM_MEDIATION_TEXT);
        }

        @Test
        void shouldPrePopulateDRHFields_CarmNotEnabled() {
            setupCategoryMocks();
            when(featureToggleService.isSdoR2Enabled()).thenReturn(true);
            given(featureToggleService.isCarmEnabledForCase(any())).willReturn(false);
            CaseData caseData = CaseDataBuilder.builder()
                    .atStateClaimDraft()
                    .atStateClaimIssuedDisposalHearingSDOInPersonHearing()
                    .build();
            CallbackParams params = buildParams(caseData);
            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            assertThat(response.getData()).doesNotHaveToString("sdoR2SmallClaimsMediationSectionStatement");
            assertThat(response.getData()).extracting("showCarmFields").isEqualTo("No");
        }

        @Test
        void shouldPrePopulateOrderDetailsPagesWithUpdatedDisclosureOfDocumentDataForR2() {
            setupCategoryMocks();
            when(featureToggleService.isSdoR2Enabled()).thenReturn(true);
            CaseData caseData = CaseDataBuilder.builder()
                    .setClaimTypeToSpecClaim()
                    .atStateClaimDraft()
                    .totalClaimAmount(BigDecimal.valueOf(15000))
                    .applicant1DQWithLocation().build();
            CallbackParams params = buildParams(caseData);
            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            assertThat(response.getData()).extracting("fastTrackDisclosureOfDocuments").extracting("input1")
                    .isEqualTo("Standard disclosure shall be provided by the parties by uploading to the Digital Portal their list of documents by 4pm on");
            assertThat(response.getData()).extracting("fastTrackDisclosureOfDocuments").extracting("date1")
                    .isEqualTo(nextWorkingDayDate.toString());
            assertThat(response.getData()).extracting("fastTrackDisclosureOfDocuments").extracting("input2")
                    .isEqualTo("Any request to inspect a document, or for a copy of a document, shall be made directly to the other party by 4pm on");
            assertThat(response.getData()).extracting("fastTrackDisclosureOfDocuments").extracting("date2")
                    .isEqualTo(nextWorkingDayDate.toString());
            assertThat(response.getData()).extracting("fastTrackDisclosureOfDocuments").extracting("input3")
                    .isEqualTo("Requests will be complied with within 7 days of the receipt of the request.");
            assertThat(response.getData()).extracting("fastTrackDisclosureOfDocuments").extracting("input4")
                    .isEqualTo("Each party must upload to the Digital Portal copies of those documents on which they wish to rely at trial by 4pm on");
            assertThat(response.getData()).extracting("fastTrackDisclosureOfDocuments").extracting("date3")
                    .isEqualTo(nextWorkingDayDate.toString());
        }

        @Test
        void shouldPrePopulateToggleDRHFields_CarmEnabled() {
            setupCategoryMocks();
            when(featureToggleService.isSdoR2Enabled()).thenReturn(true);
            given(featureToggleService.isCarmEnabledForCase(any())).willReturn(true);
            CaseData caseData = CaseDataBuilder.builder()
                    .atStateClaimDraft()
                    .atStateClaimIssuedDisposalHearingSDOInPersonHearing()
                    .build();
            String preSelectedCourt = "214320";
            List<LocationRefData> locations = List.of(
                    LocationRefData.builder().epimmsId("00001").courtLocationCode("00001")
                            .siteName("court 1").courtAddress("1 address").postcode("Y01 7RB").build(),
                    LocationRefData.builder().epimmsId(preSelectedCourt).courtLocationCode(preSelectedCourt)
                            .siteName("court 2").courtAddress("2 address").postcode("Y02 7RB").build(),
                    LocationRefData.builder().epimmsId("00003").courtLocationCode("00003")
                            .siteName("court 3").courtAddress("3 address").postcode("Y03 7RB").build()
            );
            when(locationRefDataService.getHearingCourtLocations(any(String.class))).thenReturn(locations);
            CallbackParams params = buildParams(caseData);
            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            CaseData data = objectMapper.convertValue(response.getData(), CaseData.class);
            assertThat(data.getSdoR2SmallClaimsUploadDocToggle()).isEqualTo(Collections.singletonList(IncludeInOrderToggle.INCLUDE));
            assertThat(data.getSdoR2SmallClaimsHearingToggle()).isEqualTo(Collections.singletonList(IncludeInOrderToggle.INCLUDE));
            assertThat(data.getSdoR2SmallClaimsWitnessStatementsToggle()).isEqualTo(Collections.singletonList(IncludeInOrderToggle.INCLUDE));
            assertThat(data.getSdoR2SmallClaimsPPIToggle()).isNull();
            assertThat(response.getData()).extracting("sdoR2SmallClaimsMediationSectionToggle").isNotNull();
        }

        @Test
        void testSDOSortsLocationListThroughOrganisationPartyType() {
            setupCategoryMocks();
            CaseData caseData = CaseDataBuilder.builder()
                    .setClaimTypeToSpecClaim()
                    .atStateClaimDraft()
                    .totalClaimAmount(BigDecimal.valueOf(10000))
                    .respondent1DQWithLocation()
                    .applicant1DQWithLocation()
                    .applicant1(Party.builder()
                            .type(Party.Type.ORGANISATION)
                            .individualTitle("Mr.")
                            .individualFirstName("Alex")
                            .individualLastName("Richards")
                            .partyName("Mr. Alex Richards")
                            .build())
                    .build();
            CallbackParams params = buildParams(caseData);
            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
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
            setupCategoryMocks();
            CaseData caseData = CaseDataBuilder.builder()
                    .respondent1DQWithLocation()
                    .applicant1DQWithLocation()
                    .setClaimTypeToSpecClaim()
                    .atStateClaimDraft()
                    .totalClaimAmount(BigDecimal.valueOf(10000))
                    .build()
                    .toBuilder()
                    .orderType(OrderType.DECIDE_DAMAGES)
                    .applicant1(Party.builder()
                            .type(Party.Type.ORGANISATION)
                            .individualTitle("Mr.")
                            .individualFirstName("Alex")
                            .individualLastName("Richards")
                            .partyName("Mr. Alex Richards")
                            .build())
                    .build();
            CallbackParams params = buildParams(caseData);
            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
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
            CallbackParams params = buildParams(caseData);
            when(deadlinesCalculator.plusWorkingDays(LocalDate.now(), 5))
                    .thenReturn(LocalDate.now().plusDays(5));
            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            assertThat(response.getData()).extracting("disposalHearingJudgementDeductionValue").extracting("value")
                    .isEqualTo("12.0%");
            assertThat(response.getData()).extracting("fastTrackJudgementDeductionValue").extracting("value")
                    .isEqualTo("12.0%");
            assertThat(response.getData()).extracting("smallClaimsJudgementDeductionValue").extracting("value")
                    .isEqualTo("12.0%");
        }
    }
}
