package uk.gov.hmcts.reform.civil.handler.callback.user.createsdocallbackhandlertests.integrationtests;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
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
import uk.gov.hmcts.reform.civil.enums.sdo.ClaimsTrack;
import uk.gov.hmcts.reform.civil.enums.sdo.HearingOnRadioOptions;
import uk.gov.hmcts.reform.civil.enums.sdo.SmallTrack;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.handler.callback.user.CreateSDOCallbackHandler;
import uk.gov.hmcts.reform.civil.handler.callback.user.createsdocallbackhandlertests.CreateSDOCallbackHandlerTestConfig;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.helpers.LocationHelper;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2SmallClaimsHearing;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2SmallClaimsHearingFirstOpenDateAfter;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2SmallClaimsHearingWindow;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2SmallClaimsImpNotes;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2SmallClaimsPPI;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2SmallClaimsRestrictWitness;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2SmallClaimsWitnessStatements;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.CategoryService;
import uk.gov.hmcts.reform.civil.service.DeadlinesCalculator;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.docmosis.sdo.SdoGeneratorService;
import uk.gov.hmcts.reform.civil.service.referencedata.LocationReferenceDataService;
import uk.gov.hmcts.reform.civil.utils.AssignCategoryId;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.MID;
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
public class CreateSDOMidEventNegativeNumberOfWitnessTest extends BaseCallbackHandlerTest {

    private static final String PAGE_ID = "generate-sdo-order";

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

    private CallbackParams buildCallbackParams(CaseData caseData) {
        return callbackParamsOf(CallbackVersion.V_1, caseData, MID, PAGE_ID);
    }

    private AboutToStartOrSubmitCallbackResponse invokeHandler(CaseData caseData) {
        return (AboutToStartOrSubmitCallbackResponse) handler.handle(buildCallbackParams(caseData));
    }

    private CaseData buildCaseDataWithNegativeWitnesses(ClaimsTrack track) {
        return switch (track) {
            case smallClaimsTrack -> CaseDataBuilder.builder()
                    .atSmallClaimsWitnessStatementWithNegativeInputs()
                    .build()
                    .toBuilder()
                    .claimsTrack(track)
                    .build();
            case fastTrack -> CaseDataBuilder.builder()
                    .atFastTrackWitnessOfFactWithNegativeInputs()
                    .build()
                    .toBuilder()
                    .claimsTrack(track)
                    .build();
        };
    }

    private CaseData buildCaseDataWithPositiveWitnesses(ClaimsTrack track) {
        return switch (track) {
            case smallClaimsTrack -> CaseDataBuilder.builder()
                    .atSmallClaimsWitnessStatementWithPositiveInputs()
                    .build()
                    .toBuilder()
                    .claimsTrack(track)
                    .build();
            case fastTrack -> CaseDataBuilder.builder()
                    .atFastTrackWitnessOfFactWithPositiveInputs()
                    .build()
                    .toBuilder()
                    .claimsTrack(track)
                    .build();
        };
    }

    @ParameterizedTest
    @EnumSource(value = ClaimsTrack.class, names = {"smallClaimsTrack", "fastTrack"})
    void shouldThrowErrorWhenEnteringNegativeNumberOfWitness(ClaimsTrack track) {
        CaseData caseData = buildCaseDataWithNegativeWitnesses(track);
        AboutToStartOrSubmitCallbackResponse response = invokeHandler(caseData);
        assertThat(response.getErrors()).containsExactly("The number entered cannot be less than zero");
    }

    @ParameterizedTest
    @EnumSource(value = ClaimsTrack.class, names = {"smallClaimsTrack", "fastTrack"})
    void shouldNotThrowErrorWhenEnteringPositiveNumberOfWitness(ClaimsTrack track) {
        CaseData caseData = buildCaseDataWithPositiveWitnesses(track);
        AboutToStartOrSubmitCallbackResponse response = invokeHandler(caseData);
        assertThat(response).isNotNull();
        assertThat(response.getErrors()).isEmpty();
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void shouldValidateDRHFields(boolean valid) {
        LocalDate testDate = valid ? LocalDate.now().plusDays(1) : LocalDate.now().minusDays(2);
        int countWitness = valid ? 0 : -1;
        CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimIssued()
                .build()
                .toBuilder()
                .claimsTrack(ClaimsTrack.smallClaimsTrack)
                .drawDirectionsOrderRequired(NO)
                .smallClaims(List.of(SmallTrack.smallClaimDisputeResolutionHearing))
                .sdoR2SmallClaimsPPI(SdoR2SmallClaimsPPI.builder().ppiDate(testDate).build())
                .sdoR2SmallClaimsHearing(SdoR2SmallClaimsHearing.builder()
                        .trialOnOptions(HearingOnRadioOptions.OPEN_DATE)
                        .sdoR2SmallClaimsHearingFirstOpenDateAfter(
                                SdoR2SmallClaimsHearingFirstOpenDateAfter.builder().listFrom(testDate).build()
                        )
                        .build())
                .sdoR2SmallClaimsImpNotes(SdoR2SmallClaimsImpNotes.builder().date(testDate).build())
                .sdoR2SmallClaimsWitnessStatements(SdoR2SmallClaimsWitnessStatements.builder()
                        .isRestrictWitness(YES)
                        .sdoR2SmallClaimsRestrictWitness(SdoR2SmallClaimsRestrictWitness.builder()
                                .noOfWitnessClaimant(countWitness)
                                .noOfWitnessDefendant(countWitness)
                                .build())
                        .build())
                .build();
        when(featureToggleService.isSdoR2Enabled()).thenReturn(true);
        AboutToStartOrSubmitCallbackResponse response = invokeHandler(caseData);
        if (valid) {
            assertThat(response.getErrors()).isEmpty();
        } else {
            assertThat(response.getErrors()).hasSize(5);
        }
    }

    @Test
    void shouldNotThrowErrorIfDRHOptionalFieldsAreNull() {
        CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimIssued()
                .build()
                .toBuilder()
                .claimsTrack(ClaimsTrack.smallClaimsTrack)
                .drawDirectionsOrderRequired(NO)
                .smallClaims(List.of(SmallTrack.smallClaimDisputeResolutionHearing))
                .sdoR2SmallClaimsImpNotes(SdoR2SmallClaimsImpNotes.builder()
                        .date(LocalDate.now().plusDays(2))
                        .build())
                .build();
        when(featureToggleService.isSdoR2Enabled()).thenReturn(true);
        AboutToStartOrSubmitCallbackResponse response = invokeHandler(caseData);
        assertThat(response.getErrors()).isEmpty();
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void shouldThrowValidationErrorForDRHHearingWindow(boolean valid) {
        LocalDate testDate = valid ? LocalDate.now().plusDays(1) : LocalDate.now().minusDays(2);
        CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimIssued()
                .build()
                .toBuilder()
                .claimsTrack(ClaimsTrack.smallClaimsTrack)
                .drawDirectionsOrderRequired(NO)
                .smallClaims(List.of(SmallTrack.smallClaimDisputeResolutionHearing))
                .sdoR2SmallClaimsHearing(SdoR2SmallClaimsHearing.builder()
                        .trialOnOptions(HearingOnRadioOptions.HEARING_WINDOW)
                        .sdoR2SmallClaimsHearingWindow(SdoR2SmallClaimsHearingWindow.builder()
                                .listFrom(testDate)
                                .dateTo(testDate)
                                .build())
                        .build())
                .build();
        when(featureToggleService.isSdoR2Enabled()).thenReturn(true);
        AboutToStartOrSubmitCallbackResponse response = invokeHandler(caseData);
        if (valid) {
            assertThat(response.getErrors()).isEmpty();
        } else {
            assertThat(response.getErrors()).hasSize(2);
        }
    }
}
