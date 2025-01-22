package uk.gov.hmcts.reform.civil.handler.callback.user.createsdocallbackhandlertests.integrationtests;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
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
import uk.gov.hmcts.reform.civil.callback.CallbackVersion;
import uk.gov.hmcts.reform.civil.config.ClaimUrlsConfiguration;
import uk.gov.hmcts.reform.civil.config.MockDatabaseConfiguration;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.enums.sdo.ClaimsTrack;
import uk.gov.hmcts.reform.civil.enums.sdo.FastTrack;
import uk.gov.hmcts.reform.civil.enums.sdo.SmallTrack;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.handler.callback.user.CreateSDOCallbackHandler;
import uk.gov.hmcts.reform.civil.handler.callback.user.createsdocallbackhandlertests.CreateSDOCallbackHandlerTestConfig;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.helpers.LocationHelper;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2AddendumReport;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2ApplicationToRelyOnFurther;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2ApplicationToRelyOnFurtherDetails;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2DisclosureOfDocuments;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2EvidenceAcousticEngineer;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2FurtherAudiogram;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2PermissionToRelyOnExpert;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2QuestionsClaimantExpert;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2QuestionsToEntExpert;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2RestrictNoOfWitnessDetails;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2RestrictWitness;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2ScheduleOfLoss;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2Trial;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2TrialFirstOpenDateAfter;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2TrialWindow;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2WitnessOfFact;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.CategoryService;
import uk.gov.hmcts.reform.civil.service.DeadlinesCalculator;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.docmosis.sdo.SdoGeneratorService;
import uk.gov.hmcts.reform.civil.service.referencedata.LocationReferenceDataService;
import uk.gov.hmcts.reform.civil.utils.AssignCategoryId;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.MID;
import static uk.gov.hmcts.reform.civil.constants.CreateSDOText.ERROR_MESSAGE_DATE_MUST_BE_IN_THE_FUTURE;
import static uk.gov.hmcts.reform.civil.constants.CreateSDOText.ERROR_MESSAGE_NUMBER_CANNOT_BE_LESS_THAN_ZERO;
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
class CreateSDOMidEventGenerateSdoOrderCallbackTest extends BaseCallbackHandlerTest {

    @MockBean
    private SdoGeneratorService sdoGeneratorService;

    @MockBean
    private PublicHolidaysCollection publicHolidaysCollection;

    @MockBean
    private NonWorkingDaysCollection nonWorkingDaysCollection;

    @MockBean
    private CategoryService categoryService;

    @MockBean
    private FeatureToggleService featureToggleService;

    @MockBean
    protected LocationReferenceDataService locationRefDataService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CreateSDOCallbackHandler handler;

    private static final String PAGE_ID = "generate-sdo-order";

    @ParameterizedTest
    @MethodSource("provideCaseDataForSdoOrderGeneration")
    void shouldGenerateAndSaveSdoOrder_forVariousStates(CaseData caseData) {
        CallbackParams params = callbackParamsOf(CallbackVersion.V_1, caseData, MID, PAGE_ID);
        CaseDocument order = CaseDocument.builder()
                .documentLink(Document.builder().documentUrl("url").build())
                .build();
        when(sdoGeneratorService.generate(any(), any())).thenReturn(order);
        AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
        assertThat(response.getData()).extracting("sdoOrderDocument").isNotNull();
    }

    private static Stream<CaseData> provideCaseDataForSdoOrderGeneration() {
        return Stream.of(
                CaseDataBuilder.builder().atStateClaimDraft()
                        .atStateClaimIssuedDisposalHearingSDOInPersonHearing().build(),
                CaseDataBuilder.builder().atStateClaimDraft()
                        .atStateClaimIssuedDisposalHearingSDOTelephoneHearing().build(),
                CaseDataBuilder.builder().atStateClaimDraft()
                        .atStateClaimIssuedDisposalHearingSDOVideoHearing().build(),
                CaseDataBuilder.builder().atStateClaimDraft()
                        .atStateClaimIssuedFastTrackSDOInPersonHearing().build(),
                CaseDataBuilder.builder().atStateClaimDraft()
                        .atStateClaimIssuedFastTrackSDOTelephoneHearing().build(),
                CaseDataBuilder.builder().atStateClaimDraft()
                        .atStateClaimIssuedFastTrackSDOVideoHearing().build(),
                CaseDataBuilder.builder().atStateClaimDraft()
                        .atStateClaimIssuedSmallClaimsSDOInPersonHearing().build(),
                CaseDataBuilder.builder().atStateClaimDraft()
                        .atStateClaimIssuedSmallClaimsSDOTelephoneHearing().build(),
                CaseDataBuilder.builder().atStateClaimDraft()
                        .atStateClaimIssuedSmallClaimsSDOVideoHearing().build()
        );
    }

    @Test
    void shouldAssignCategoryId_whenInvoked() {
        CaseData caseData = CaseDataBuilder.builder().atStateClaimDraft().build();

        CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);
        CaseDocument order = CaseDocument.builder()
                .documentLink(Document.builder().documentUrl("url").build())
                .build();
        when(sdoGeneratorService.generate(any(), any())).thenReturn(order);
        AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
        CaseData updatedData = objectMapper.convertValue(response.getData(), CaseData.class);
        assertThat(updatedData.getSdoOrderDocument().getDocumentLink().getCategoryID()).isEqualTo("caseManagementOrders");
    }

    @Test
    void shouldGenerateAndSaveSdoOrder_whenNihl() {
        when(featureToggleService.isSdoR2Enabled()).thenReturn(true);
        List<FastTrack> fastTrackList = new ArrayList<>();
        fastTrackList.add(FastTrack.fastClaimNoiseInducedHearingLoss);
        CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimDraft()
                .build()
                .toBuilder()
                .drawDirectionsOrderRequired(YesOrNo.NO)
                .claimsTrack(ClaimsTrack.fastTrack)
                .fastClaims(fastTrackList)
                .build();

        CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);
        CaseDocument order = CaseDocument.builder()
                .documentLink(Document.builder().documentUrl("url").build())
                .build();
        when(sdoGeneratorService.generate(any(), any())).thenReturn(order);
        AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
        CaseData updatedData = objectMapper.convertValue(response.getData(), CaseData.class);
        assertThat(updatedData.getSdoOrderDocument().getDocumentLink().getCategoryID()).isEqualTo("caseManagementOrders");
    }

    @ParameterizedTest
    @MethodSource("provideCaseDataForNihlValidation")
    void shouldValidateFieldsForNihl(boolean valid) {
        when(featureToggleService.isSdoR2Enabled()).thenReturn(true);

        List<FastTrack> fastTrackList = new ArrayList<>();
        fastTrackList.add(FastTrack.fastClaimNoiseInducedHearingLoss);

        LocalDate testDate = valid ? LocalDate.now().plusDays(1) : LocalDate.now();
        Integer testWitnesses = valid ? 0 : -1;

        CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimDraft()
                .build()
                .toBuilder()
                .drawDirectionsOrderRequired(NO)
                .claimsTrack(ClaimsTrack.fastTrack)
                .fastClaims(fastTrackList)
                .sdoR2DisclosureOfDocuments(SdoR2DisclosureOfDocuments.builder()
                        .standardDisclosureDate(testDate)
                        .inspectionDate(testDate)
                        .build())
                .sdoR2AddendumReport(SdoR2AddendumReport.builder()
                        .sdoAddendumReportDate(testDate)
                        .build())
                .sdoR2FurtherAudiogram(SdoR2FurtherAudiogram.builder()
                        .sdoClaimantShallUndergoDate(testDate)
                        .sdoServiceReportDate(testDate)
                        .build())
                .sdoR2QuestionsClaimantExpert(SdoR2QuestionsClaimantExpert.builder()
                        .sdoDefendantMayAskDate(testDate)
                        .sdoQuestionsShallBeAnsweredDate(testDate)
                        .sdoApplicationToRelyOnFurther(SdoR2ApplicationToRelyOnFurther.builder()
                                .applicationToRelyOnFurtherDetails(SdoR2ApplicationToRelyOnFurtherDetails.builder()
                                        .applicationToRelyDetailsDate(testDate)
                                        .build())
                                .build())
                        .build())
                .sdoR2PermissionToRelyOnExpert(SdoR2PermissionToRelyOnExpert.builder()
                        .sdoPermissionToRelyOnExpertDate(testDate)
                        .sdoJointMeetingOfExpertsDate(testDate)
                        .build())
                .sdoR2EvidenceAcousticEngineer(SdoR2EvidenceAcousticEngineer.builder()
                        .sdoInstructionOfTheExpertDate(testDate)
                        .sdoExpertReportDate(testDate)
                        .sdoWrittenQuestionsDate(testDate)
                        .sdoRepliesDate(testDate)
                        .build())
                .sdoR2QuestionsToEntExpert(SdoR2QuestionsToEntExpert.builder()
                        .sdoQuestionsShallBeAnsweredDate(testDate)
                        .sdoWrittenQuestionsDate(testDate)
                        .build())
                .sdoR2ScheduleOfLoss(SdoR2ScheduleOfLoss.builder()
                        .sdoR2ScheduleOfLossClaimantDate(testDate)
                        .sdoR2ScheduleOfLossDefendantDate(testDate)
                        .build())
                .sdoR2Trial(SdoR2Trial.builder()
                        .sdoR2TrialFirstOpenDateAfter(SdoR2TrialFirstOpenDateAfter.builder()
                                .listFrom(testDate)
                                .build())
                        .sdoR2TrialWindow(SdoR2TrialWindow.builder()
                                .listFrom(testDate)
                                .dateTo(testDate)
                                .build())
                        .build())
                .sdoR2ImportantNotesDate(testDate)
                .sdoR2WitnessesOfFact(SdoR2WitnessOfFact.builder()
                        .sdoWitnessDeadlineDate(testDate)
                        .sdoR2RestrictWitness(SdoR2RestrictWitness.builder()
                                .restrictNoOfWitnessDetails(SdoR2RestrictNoOfWitnessDetails.builder()
                                        .noOfWitnessClaimant(testWitnesses)
                                        .noOfWitnessDefendant(testWitnesses)
                                        .build())
                                .build())
                        .build())
                .build();

        CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);

        AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

        if (valid) {
            assertThat(response.getErrors()).isEmpty();
        } else {
            assertThat(response.getErrors()).hasSize(25)
                    .contains(ERROR_MESSAGE_DATE_MUST_BE_IN_THE_FUTURE, ERROR_MESSAGE_NUMBER_CANNOT_BE_LESS_THAN_ZERO);
        }
    }

    private static Stream<Boolean> provideCaseDataForNihlValidation() {
        return Stream.of(true, false);
    }

    @Test
    void shouldGenerateAndSaveSdoOrder_whenDrhIsSelected() {
        when(featureToggleService.isSdoR2Enabled()).thenReturn(true);
        CaseData caseData = CaseDataBuilder.builder().atStateClaimDraft()
                .atStateClaimIssued()
                .build()
                .toBuilder()
                .claimsTrack(ClaimsTrack.smallClaimsTrack)
                .drawDirectionsOrderRequired(NO)
                .smallClaims(List.of(SmallTrack.smallClaimDisputeResolutionHearing))
                .build();

        CallbackParams params = callbackParamsOf(CallbackVersion.V_1, caseData, MID, PAGE_ID);
        CaseDocument order = CaseDocument.builder()
                .documentLink(Document.builder().documentUrl("url").build())
                .build();
        when(sdoGeneratorService.generate(any(), any())).thenReturn(order);
        AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
        assertThat(response.getData()).extracting("sdoOrderDocument").isNotNull();
    }
}
