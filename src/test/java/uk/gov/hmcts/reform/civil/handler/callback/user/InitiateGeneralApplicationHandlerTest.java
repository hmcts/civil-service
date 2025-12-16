package uk.gov.hmcts.reform.civil.handler.callback.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.Answer;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.civil.bankholidays.WorkingDayIndicator;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.enums.BusinessProcessStatus;
import uk.gov.hmcts.reform.civil.enums.CaseCategory;
import uk.gov.hmcts.reform.civil.enums.CaseRole;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.enums.dq.GeneralApplicationTypes;
import uk.gov.hmcts.reform.civil.enums.dq.GeneralApplicationTypesLR;
import uk.gov.hmcts.reform.civil.enums.dq.Language;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.BusinessProcess;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Fee;
import uk.gov.hmcts.reform.civil.model.IdamUserDetails;
import uk.gov.hmcts.reform.civil.model.citizenui.CaseDataLiP;
import uk.gov.hmcts.reform.civil.model.citizenui.RespondentLiPResponse;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.CaseLocationCivil;
import uk.gov.hmcts.reform.civil.model.genapplication.GAApplicationType;
import uk.gov.hmcts.reform.civil.model.genapplication.GAApplicationTypeLR;
import uk.gov.hmcts.reform.civil.model.genapplication.GAHearingDateGAspec;
import uk.gov.hmcts.reform.civil.model.genapplication.GAHearingDetails;
import uk.gov.hmcts.reform.civil.model.genapplication.GAInformOtherParty;
import uk.gov.hmcts.reform.civil.model.genapplication.GAPbaDetails;
import uk.gov.hmcts.reform.civil.model.genapplication.GARespondentOrderAgreement;
import uk.gov.hmcts.reform.civil.model.genapplication.GAUnavailabilityDates;
import uk.gov.hmcts.reform.civil.model.genapplication.GAUrgencyRequirement;
import uk.gov.hmcts.reform.civil.model.genapplication.GeneralApplication;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.sampledata.GeneralApplicationDetailsBuilder;
import uk.gov.hmcts.reform.civil.sampledata.LocationRefSampleDataBuilder;
import uk.gov.hmcts.reform.civil.service.CoreCaseUserService;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.GeneralAppFeesService;
import uk.gov.hmcts.reform.civil.service.InitiateGeneralApplicationService;
import uk.gov.hmcts.reform.civil.service.UserService;
import uk.gov.hmcts.reform.civil.service.referencedata.LocationReferenceDataService;
import uk.gov.hmcts.reform.civil.service.validation.GeneralApplicationValidator;
import uk.gov.hmcts.reform.civil.utils.UserRoleCaching;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static java.lang.String.format;
import static java.time.LocalDate.EPOCH;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.MID;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.SUBMITTED;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.INITIATE_GENERAL_APPLICATION;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.INITIATE_GENERAL_APPLICATION_COSC;
import static uk.gov.hmcts.reform.civil.enums.CaseState.AWAITING_APPLICANT_INTENTION;
import static uk.gov.hmcts.reform.civil.enums.CaseState.CASE_PROGRESSION;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.enums.dq.GAHearingDuration.OTHER;
import static uk.gov.hmcts.reform.civil.enums.dq.GAHearingSupportRequirements.OTHER_SUPPORT;
import static uk.gov.hmcts.reform.civil.enums.dq.GAHearingType.IN_PERSON;
import static uk.gov.hmcts.reform.civil.enums.dq.GeneralApplicationTypes.EXTEND_TIME;
import static uk.gov.hmcts.reform.civil.enums.dq.GeneralApplicationTypes.SETTLE_BY_CONSENT;
import static uk.gov.hmcts.reform.civil.enums.dq.GeneralApplicationTypes.STAY_THE_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.dq.GeneralApplicationTypes.STRIKE_OUT;
import static uk.gov.hmcts.reform.civil.enums.dq.GeneralApplicationTypes.SUMMARY_JUDGEMENT;
import static uk.gov.hmcts.reform.civil.enums.dq.GeneralApplicationTypes.VARY_ORDER;
import static uk.gov.hmcts.reform.civil.enums.dq.GeneralApplicationTypes.VARY_PAYMENT_TERMS_OF_JUDGMENT;
import static uk.gov.hmcts.reform.civil.handler.callback.user.InitiateGeneralApplicationHandler.NOT_ALLOWED_SETTLE_DISCONTINUE;
import static uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder.CUSTOMER_REFERENCE;
import static uk.gov.hmcts.reform.civil.service.InitiateGeneralApplicationServiceConstants.INVALID_SETTLE_BY_CONSENT;
import static uk.gov.hmcts.reform.civil.service.InitiateGeneralApplicationServiceConstants.INVALID_UNAVAILABILITY_RANGE;
import static uk.gov.hmcts.reform.civil.service.validation.GeneralApplicationValidator.INVALID_TRIAL_DATE_RANGE;
import static uk.gov.hmcts.reform.civil.service.validation.GeneralApplicationValidator.TRIAL_DATE_FROM_REQUIRED;
import static uk.gov.hmcts.reform.civil.service.validation.GeneralApplicationValidator.UNAVAILABLE_DATE_RANGE_MISSING;
import static uk.gov.hmcts.reform.civil.service.validation.GeneralApplicationValidator.UNAVAILABLE_FROM_MUST_BE_PROVIDED;
import static uk.gov.hmcts.reform.civil.service.validation.GeneralApplicationValidator.URGENCY_DATE_CANNOT_BE_IN_PAST;
import static uk.gov.hmcts.reform.civil.service.validation.GeneralApplicationValidator.URGENCY_DATE_REQUIRED;
import static uk.gov.hmcts.reform.civil.service.validation.GeneralApplicationValidator.URGENCY_DATE_SHOULD_NOT_BE_PROVIDED;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.unwrapElements;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.wrapElements;

@ExtendWith(MockitoExtension.class)
class InitiateGeneralApplicationHandlerTest extends BaseCallbackHandlerTest {

    private InitiateGeneralApplicationHandler handler;

    private ObjectMapper objectMapper;

    @Mock
    private InitiateGeneralApplicationService initiateGeneralAppService;

    @Mock
    private GeneralApplicationValidator generalApplicationValidator;

    @Mock
    protected GeneralAppFeesService feesService;

    @Mock
    protected LocationReferenceDataService locationRefDataService;

    @Mock
    private UserService theUserService;

    @Mock
    private WorkingDayIndicator workingDayIndicator;

    @Mock
    protected UserRoleCaching userRoleCaching;

    @Mock
    protected FeatureToggleService featureToggleService;

    @Mock
    protected CoreCaseUserService coreCaseUserService;

    @Mock
    protected GeneralAppFeesService generalAppFeesService;

    public static final String APPLICANT_EMAIL_ID_CONSTANT = "testUser@gmail.com";

    private static final String SET_FEES_AND_PBA = "ga-fees-and-pba";
    private final BigDecimal fee108 = new BigDecimal("10800");
    private final BigDecimal fee14 = new BigDecimal("1400");
    private final BigDecimal fee275 = new BigDecimal("27500");
    private static final String FEE_CODE = "test_fee_code";
    private static final String FEE_VERSION = "1";

    private static final String STRING_CONSTANT = "this is a string";
    private static final LocalDate APP_DATE_EPOCH = EPOCH;
    private static final String CONFIRMATION_BODY_FREE = "<br/> <p> The court will make a decision"
        + " on this application."
        + "<br/> <p>  The other party's legal representative has been notified that you have"
        + " submitted this application";
    private static final Fee FEE275 = Fee.builder().calculatedAmountInPence(
        BigDecimal.valueOf(27500)).code("FEE0444").version("1").build();

    @BeforeEach
    void setup() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        handler = new InitiateGeneralApplicationHandler(initiateGeneralAppService, generalApplicationValidator, objectMapper, theUserService,
                                                        feesService, locationRefDataService,
                                                        featureToggleService, coreCaseUserService, generalAppFeesService);
    }

    @Test
    void shouldThrowError_whenDiscontinuedQMOnNoPreviousCcdState() {
        CaseData caseData = CaseDataBuilder.builder()
            .atStateClaimIssued()
            .caseAccessCategory(CaseCategory.UNSPEC_CLAIM)
            .caseManagementLocation(createCaseLocationCivil())
            .build();
        caseData.setCcdState(CaseState.CASE_DISCONTINUED);

        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_START);
        params.getRequest().setEventId(INITIATE_GENERAL_APPLICATION.name());
        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
        assertThat(response.getErrors()).isNotNull();
        assertThat(response.getErrors()).containsOnly(NOT_ALLOWED_SETTLE_DISCONTINUE);
    }

    @Test
    void shouldThrowError_whenSettledQMOnNoPreviousCcdState() {
        CaseData caseData = CaseDataBuilder.builder()
            .atStateClaimIssued()
            .caseAccessCategory(CaseCategory.UNSPEC_CLAIM)
            .caseManagementLocation(createCaseLocationCivil())
            .build();
        caseData.setCcdState(CaseState.CASE_SETTLED);

        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_START);
        params.getRequest().setEventId(INITIATE_GENERAL_APPLICATION.name());
        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
        assertThat(response.getErrors()).isNotNull();
        assertThat(response.getErrors()).containsOnly(NOT_ALLOWED_SETTLE_DISCONTINUE);
    }

    @Test
    void shouldNotThrowError_whenSettledQMOnPreviousCcdState() {
        CaseData caseData = CaseDataBuilder.builder()
            .atStateClaimIssued()
            .caseAccessCategory(CaseCategory.UNSPEC_CLAIM)
            .caseManagementLocation(createCaseLocationCivil())
            .build();
        caseData.setCcdState(CaseState.CASE_SETTLED);
        caseData.setPreviousCCDState(CaseState.JUDICIAL_REFERRAL);

        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_START);
        params.getRequest().setEventId(INITIATE_GENERAL_APPLICATION.name());
        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
        assertThat(response.getErrors()).isNotNull();
    }

    @Test
    void shouldNotThrowError_whenDiscontinuedQMOnPreviousCcdState() {
        CaseData caseData = CaseDataBuilder.builder()
            .atStateClaimIssued()
            .caseAccessCategory(CaseCategory.UNSPEC_CLAIM)
            .caseManagementLocation(createCaseLocationCivil())
            .build();
        caseData.setCcdState(CaseState.CASE_DISCONTINUED);
        caseData.setPreviousCCDState(CaseState.JUDICIAL_REFERRAL);

        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_START);
        params.getRequest().setEventId(INITIATE_GENERAL_APPLICATION.name());
        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
        assertThat(response.getErrors()).isNotNull();
    }

    @Test
    void shouldThrowError_whenLRVsLiP() {

        CaseData caseData = CaseDataBuilder.builder()
            .atStateClaimIssued1v1LiP()
            .caseAccessCategory(CaseCategory.UNSPEC_CLAIM)
            .caseManagementLocation(createCaseLocationCivil())
            .build();

        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_START);
        params.getRequest().setEventId(INITIATE_GENERAL_APPLICATION.name());
        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
        assertThat(response.getErrors()).isNotNull();
    }

    @Test
    void shouldNotThrowError_whenLipVsLrAndDefendantLiPIsBilingual() {

        CaseData caseData = CaseDataBuilder.builder()
            .atStateClaimIssued1v1LiP()
            .caseAccessCategory(CaseCategory.SPEC_CLAIM)
            .caseManagementLocation(createCaseLocationCivil())
            .respondent1Represented(YES)
            .specRespondent1Represented(YES)
            .applicant1Represented(NO)
            .defendantUserDetails(createDefendantUserDetails())
            .caseDataLip(createBilingualCaseDataLiP())
            .build();

        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_START);
        params.getRequest().setEventId(INITIATE_GENERAL_APPLICATION.name());
        given(initiateGeneralAppService.caseContainsLiP(any())).willReturn(true);
        given(initiateGeneralAppService.respondentAssigned(any())).willReturn(true);
        given(featureToggleService.isLocationWhiteListed(any())).willReturn(true);
        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
        assertThat(response.getErrors()).isEmpty();
    }

    @Test
    void shouldNotThrowError_whenLipVsLrAndDefendantLiPIsBilingualForCosc() {

        CaseData caseData = CaseDataBuilder.builder()
            .atStateClaimIssued1v1LiP()
            .caseAccessCategory(CaseCategory.SPEC_CLAIM)
            .caseManagementLocation(createCaseLocationCivil())
            .respondent1Represented(YES)
            .specRespondent1Represented(YES)
            .applicant1Represented(NO)
            .defendantUserDetails(createDefendantUserDetails())
            .caseDataLip(createBilingualCaseDataLiP())
            .build();

        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_START);
        params.getRequest().setEventId(INITIATE_GENERAL_APPLICATION_COSC.name());
        given(initiateGeneralAppService.caseContainsLiP(any())).willReturn(true);
        given(initiateGeneralAppService.respondentAssigned(any())).willReturn(true);
        given(featureToggleService.isLocationWhiteListed(any())).willReturn(true);
        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
        assertThat(response.getErrors()).isEmpty();
    }

    @Test
    void shouldThrowError_whenLipVsLrAndClaimantLiPIsBilingual() {

        CaseData caseData = CaseDataBuilder.builder()
            .atStateClaimIssued1v1LiP()
            .caseAccessCategory(CaseCategory.SPEC_CLAIM)
            .caseManagementLocation(createCaseLocationCivil())
            .respondent1Represented(YES)
            .specRespondent1Represented(YES)
            .applicant1Represented(NO)
            .defendantUserDetails(createDefendantUserDetails())
            .claimantBilingualLanguagePreference(Language.BOTH.toString())
            .build();

        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_START);
        params.getRequest().setEventId(INITIATE_GENERAL_APPLICATION.name());
        given(initiateGeneralAppService.caseContainsLiP(any())).willReturn(true);
        given(initiateGeneralAppService.respondentAssigned(any())).willReturn(true);
        given(featureToggleService.isDefendantNoCOnlineForCase(any())).willReturn(true);
        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
        assertThat(response.getErrors()).isNotNull();
    }

    @Test
    void shouldNotThrowError_whenLipVsLrAndClaimantLiPIsBilingualAndWelshGaToggleEnabled() {

        CaseData caseData = CaseDataBuilder.builder()
            .atStateClaimIssued1v1LiP()
            .caseAccessCategory(CaseCategory.SPEC_CLAIM)
            .caseManagementLocation(createCaseLocationCivil())
            .respondent1Represented(YES)
            .specRespondent1Represented(YES)
            .applicant1Represented(NO)
            .defendantUserDetails(createDefendantUserDetails())
            .claimantBilingualLanguagePreference(Language.BOTH.toString())
            .build();

        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_START);
        params.getRequest().setEventId(INITIATE_GENERAL_APPLICATION.name());
        given(initiateGeneralAppService.caseContainsLiP(any())).willReturn(true);
        given(featureToggleService.isGaForWelshEnabled()).willReturn(true);
        given(featureToggleService.isLocationWhiteListed(any())).willReturn(true);
        given(initiateGeneralAppService.respondentAssigned(any())).willReturn(true);
        given(featureToggleService.isDefendantNoCOnlineForCase(any())).willReturn(true);
        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
        assertThat(response.getErrors()).isEmpty();
    }

    @Test
    void shouldThrowError_whenLRVsLiPCourtIsNotWhitelisted() {

        CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimIssued1v1LiP()
                .caseAccessCategory(CaseCategory.SPEC_CLAIM)
                .caseManagementLocation(CaseLocationCivil.builder()
                        .baseLocation("45678")
                        .region("4").build())
                .build();

        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_START);
        params.getRequest().setEventId(INITIATE_GENERAL_APPLICATION.name());
        given(initiateGeneralAppService.caseContainsLiP(any())).willReturn(true);
        given(featureToggleService.isLocationWhiteListed(any())).willReturn(false);
        given(initiateGeneralAppService.respondentAssigned(any())).willReturn(true);

        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
        assertThat(response.getErrors()).isNotNull();
    }

    @Test
    void shouldThrowError_whenLRVsLiPAndLiPIsBilingual() {

        CaseData caseData = CaseDataBuilder.builder()
            .atStateClaimIssued1v1LiP()
            .caseAccessCategory(CaseCategory.SPEC_CLAIM)
            .caseManagementLocation(createCaseLocationCivil())
            .caseDataLip(createBilingualCaseDataLiP())
            .build();

        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_START);
        params.getRequest().setEventId(INITIATE_GENERAL_APPLICATION.name());
        given(initiateGeneralAppService.caseContainsLiP(any())).willReturn(true);

        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
        assertThat(response.getErrors()).isNotNull();
    }

    @Test
    void shouldNotThrowError_whenLRVsLiPAndLiPIsBilingualGaForWelshEnabled() {

        CaseData caseData = CaseDataBuilder.builder()
            .atStateClaimIssued1v1LiP()
            .caseAccessCategory(CaseCategory.SPEC_CLAIM)
            .defendantUserDetails(createDefendantUserDetails())
            .caseManagementLocation(createCaseLocationCivil())
            .caseDataLip(createBilingualCaseDataLiP())
            .build();

        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_START);
        params.getRequest().setEventId(INITIATE_GENERAL_APPLICATION.name());
        given(initiateGeneralAppService.caseContainsLiP(any())).willReturn(true);
        given(featureToggleService.isGaForWelshEnabled()).willReturn(true);
        given(featureToggleService.isLocationWhiteListed(any())).willReturn(true);
        given(initiateGeneralAppService.respondentAssigned(any())).willReturn(true);

        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
        assertThat(response.getErrors()).isEmpty();
    }

    @Test
    void shouldNotThrowError_whenLRVsLiPAndLiPIsBilingualGaForWelshEnabledForNro() {

        CaseData caseData = CaseDataBuilder.builder()
            .atStateClaimIssued1v1LiP()
            .caseAccessCategory(CaseCategory.SPEC_CLAIM)
            .defendantUserDetails(createDefendantUserDetails())
            .caseManagementLocation(createCaseLocationCivil())
            .caseDataLip(createBilingualCaseDataLiP())
            .build();

        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_START);
        params.getRequest().setEventId(INITIATE_GENERAL_APPLICATION.name());
        given(initiateGeneralAppService.caseContainsLiP(any())).willReturn(true);
        given(featureToggleService.isGaForWelshEnabled()).willReturn(true);
        given(featureToggleService.isLocationWhiteListed(any())).willReturn(false);
        given(featureToggleService.isCuiGaNroEnabled()).willReturn(true);
        given(initiateGeneralAppService.respondentAssigned(any())).willReturn(true);

        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
        assertThat(response.getErrors()).isEmpty();
    }

    @Test
    void shouldNotThrowError_whenLipVsLrAndDefendantLiPIsNotAssigned() {

        CaseData caseData = CaseDataBuilder.builder()
            .atStateClaimIssued1v1LiP()
            .caseAccessCategory(CaseCategory.SPEC_CLAIM)
            .caseManagementLocation(createCaseLocationCivil())
            .respondent1Represented(YES)
            .specRespondent1Represented(YES)
            .applicant1Represented(NO)
            .build();

        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_START);
        params.getRequest().setEventId(INITIATE_GENERAL_APPLICATION.name());

        given(initiateGeneralAppService.caseContainsLiP(any())).willReturn(true);
        given(initiateGeneralAppService.respondentAssigned(any())).willReturn(true);
        given(featureToggleService.isLocationWhiteListed(any())).willReturn(true);
        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
        assertThat(response.getErrors()).isEmpty();
    }

    @Test
    void shouldThrowError_whenLRVsLiPAndLipsNotEnabledAndWhiteListed() {

        CaseData caseData = CaseDataBuilder.builder()
            .atStateClaimIssued1v1LiP()
            .caseAccessCategory(CaseCategory.SPEC_CLAIM)
            .caseManagementLocation(createCaseLocationCivil())
            .build();

        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_START);
        params.getRequest().setEventId(INITIATE_GENERAL_APPLICATION.name());
        given(initiateGeneralAppService.caseContainsLiP(any())).willReturn(true);
        given(featureToggleService.isLocationWhiteListed(any())).willReturn(true);
        given(initiateGeneralAppService.respondentAssigned(any())).willReturn(false);
        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
        assertThat(response.getErrors()).isNotNull();
    }

    @Test
    void shouldNotThrowError_whenEpimsIdIsValidRegionPreSdoNationalRollout() {
        // National rollout applies to all courts pre sdo
        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setCcdState(AWAITING_APPLICANT_INTENTION);
        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_START);
        params.getRequest().setEventId(INITIATE_GENERAL_APPLICATION.name());
        given(initiateGeneralAppService.respondentAssigned(any())).willReturn(true);

        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
        assertThat(response.getErrors()).isEmpty();
    }

    @Test
    void shouldNotThrowError_whenEpimsIdIsValidRegionPostSdoNationalRollout() {
        // National rollout applies to all courts  post sdo, except Birmingham
        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setCcdState(CASE_PROGRESSION);
        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_START);
        params.getRequest().setEventId(INITIATE_GENERAL_APPLICATION.name());
        given(initiateGeneralAppService.respondentAssigned(any())).willReturn(true);

        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
        assertThat(response.getErrors()).isEmpty();
    }

    @Nested
    class MidEventForUrgencyCheck extends LocationRefSampleDataBuilder {

        private static final String VALIDATE_URGENCY_DATE_PAGE = "ga-validate-urgency-date";

        @Test
        void shouldNotCauseAnyErrors_whenApplicationDetailsNotProvided() {
            CaseData caseData = CaseDataBuilder.builder().build();
            CallbackParams params = callbackParamsOf(caseData, MID, VALIDATE_URGENCY_DATE_PAGE);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).isEmpty();
        }

        @Test
        void shouldReturnErrors_whenApplicationIsUrgentButConsiderationDateIsNotProvided() {
            CaseData caseData = GeneralApplicationDetailsBuilder.builder()
                .getTestCaseDataForUrgencyCheckMidEvent(CaseDataBuilder.builder().build(),
                                                        true, null);

            CallbackParams params = callbackParamsOf(caseData, MID, VALIDATE_URGENCY_DATE_PAGE);
            when(generalApplicationValidator.validateUrgencyDates(any())).thenCallRealMethod();

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).isNotEmpty();
            assertThat(response.getErrors()).contains(URGENCY_DATE_REQUIRED);
        }

        @Test
        void shouldReturnErrors_whenApplicationIsNotUrgentButConsiderationDateIsProvided() {
            CaseData caseData = GeneralApplicationDetailsBuilder.builder()
                .getTestCaseDataForUrgencyCheckMidEvent(CaseDataBuilder.builder().build(),
                                                        false, LocalDate.now());

            CallbackParams params = callbackParamsOf(caseData, MID, VALIDATE_URGENCY_DATE_PAGE);
            when(generalApplicationValidator.validateUrgencyDates(any())).thenCallRealMethod();

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).isNotEmpty();
            assertThat(response.getErrors()).contains(
                URGENCY_DATE_SHOULD_NOT_BE_PROVIDED);
        }

        @Test
        void shouldReturnErrors_whenUrgencyConsiderationDateIsInPastForUrgentApplication() {
            CaseData caseData = GeneralApplicationDetailsBuilder.builder()
                .getTestCaseDataForUrgencyCheckMidEvent(CaseDataBuilder.builder().build(),
                                                        true, LocalDate.now().minusDays(1));

            CallbackParams params = callbackParamsOf(caseData, MID, VALIDATE_URGENCY_DATE_PAGE);
            when(generalApplicationValidator.validateUrgencyDates(any())).thenCallRealMethod();

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).isNotEmpty();
            assertThat(response.getErrors()).contains(URGENCY_DATE_CANNOT_BE_IN_PAST);
        }

        @Test
        void shouldNotCauseAnyErrors_whenUrgencyConsiderationDateIsInFutureForUrgentApplication() {
            CaseData caseData = GeneralApplicationDetailsBuilder.builder()
                .getTestCaseDataForUrgencyCheckMidEvent(CaseDataBuilder.builder().build(),
                                                        true, LocalDate.now());

            CallbackParams params = callbackParamsOf(caseData, MID, VALIDATE_URGENCY_DATE_PAGE);
            when(generalApplicationValidator.validateUrgencyDates(any())).thenCallRealMethod();

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).isEmpty();
        }

        @Test
        void shouldNotCauseAnyErrors_whenApplicationIsNotUrgentAndConsiderationDateIsNotProvided() {
            CaseData caseData = GeneralApplicationDetailsBuilder.builder()
                .getTestCaseDataForUrgencyCheckMidEvent(CaseDataBuilder.builder().build(),
                                                        false, null);

            CallbackParams params = callbackParamsOf(caseData, MID, VALIDATE_URGENCY_DATE_PAGE);
            when(generalApplicationValidator.validateUrgencyDates(any())).thenCallRealMethod();

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).isEmpty();
        }
    }

    public CaseData getCaseData(AboutToStartOrSubmitCallbackResponse response) {
        return objectMapper.convertValue(response.getData(), CaseData.class);
    }

    @Nested
    class MidEventForVaryJudgement extends LocationRefSampleDataBuilder {

        private static final String VALIDATE_GA_TYPE = "ga-validate-type";

        @Test
        void shouldNotCauseAnyErrors_whenGaTypeIsNotVaryJudgement() {

            List<GeneralApplicationTypes> types = List.of(STRIKE_OUT, SUMMARY_JUDGEMENT);
            CaseData caseData = CaseDataBuilder
                .builder().ccdCaseReference(1234L).generalAppType(createGAApplicationType(types)).build();

            CallbackParams params = callbackParamsOf(caseData, MID, VALIDATE_GA_TYPE);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            CaseData responseCaseData = getCaseData(response);

            assertThat(responseCaseData.getGeneralAppVaryJudgementType()).isEqualTo(NO);
            assertThat(response.getErrors()).isEmpty();
        }

        @Test
        void shouldNotCauseAnyErrorsWhenGaTypeIsVaryJudgement() {
            List<GeneralApplicationTypes> types = List.of(VARY_PAYMENT_TERMS_OF_JUDGMENT);
            CaseData caseData = CaseDataBuilder
                .builder().ccdCaseReference(1234L).generalAppType(createGAApplicationType(types)).build();

            CallbackParams params = callbackParamsOf(caseData, MID, VALIDATE_GA_TYPE);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            CaseData responseCaseData = getCaseData(response);

            assertThat(responseCaseData.getGeneralAppVaryJudgementType()).isEqualTo(YES);
            assertThat(response.getErrors()).isEmpty();
        }

        @Test
        void shouldNotCauseAnyErrorsWhenGaTypeIsMultipleTypeWithVaryJudgement() {
            List<GeneralApplicationTypes> types = List.of(STRIKE_OUT, SUMMARY_JUDGEMENT, VARY_PAYMENT_TERMS_OF_JUDGMENT);
            CaseData caseData = CaseDataBuilder
                .builder().ccdCaseReference(1234L).generalAppType(createGAApplicationType(types)).build();

            CallbackParams params = callbackParamsOf(caseData, MID, VALIDATE_GA_TYPE);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            CaseData responseCaseData = getCaseData(response);

            assertThat(responseCaseData.getGeneralAppVaryJudgementType()).isEqualTo(NO);
            assertThat(response.getErrors().size()).isEqualTo(1);
            assertThat(response.getErrors().get(0).equals("It is not possible to select an additional application type when applying to vary payment terms of judgment"));
        }

        @Test
        void shouldNotCauseAnyErrorsWhenGaTypeIsMultipleTypeWithSettleOrDiscontinueConsent() {
            List<GeneralApplicationTypes> types = List.of(STRIKE_OUT, SUMMARY_JUDGEMENT,
                    SETTLE_BY_CONSENT);
            CaseData caseData = CaseDataBuilder
                    .builder().ccdCaseReference(1234L).generalAppType(createGAApplicationType(types)).build();

            CallbackParams params = callbackParamsOf(caseData, MID, VALIDATE_GA_TYPE);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            CaseData responseCaseData = getCaseData(response);

            assertThat(responseCaseData.getGeneralAppVaryJudgementType()).isEqualTo(NO);
            assertThat(response.getErrors().size()).isEqualTo(1);
            assertThat(response.getErrors().get(0).equals("It is not possible to select an additional application type " +
                    "when applying to Settle by consent"));
        }
    }

    @Nested
    class MidEventForHearingDateValidation extends LocationRefSampleDataBuilder {

        private static final String INVALID_HEARING_DATE = "The hearing date must be in the future";
        private static final String VALIDATE_HEARING_DATE = "ga-validate-hearing-date";

        @Test
        void shouldThrowErrorsWhenHearingDateIsPast() {
            List<GeneralApplicationTypes> types = List.of(VARY_PAYMENT_TERMS_OF_JUDGMENT);

            CaseData caseData = CaseDataBuilder
                .builder()
                .ccdCaseReference(1234L)
                .generalAppHearingDate(createGAHearingDateGAspec(LocalDate.now().minusDays(3)))
                .generalAppType(createGAApplicationType(types)).build();

            CallbackParams params = callbackParamsOf(caseData, MID, VALIDATE_HEARING_DATE);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors().size()).isEqualTo(1);

            assertThat(response.getErrors().get(0)).isEqualTo(INVALID_HEARING_DATE);
        }

        @Test
        void shouldNotCauseAnyErrorsWhenHearingDateIsPresent() {
            List<GeneralApplicationTypes> types = List.of(STRIKE_OUT, SUMMARY_JUDGEMENT, VARY_PAYMENT_TERMS_OF_JUDGMENT);

            CaseData caseData = CaseDataBuilder
                .builder()
                .generalAppHearingDate(createGAHearingDateGAspec(LocalDate.now()))
                .ccdCaseReference(1234L)
                .generalAppType(createGAApplicationType(types)).build();

            CallbackParams params = callbackParamsOf(caseData, MID, VALIDATE_HEARING_DATE);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).isEmpty();
        }

        @Test
        void shouldNotCauseAnyErrorsWhenHearingDateIsFuture() {
            List<GeneralApplicationTypes> types = List.of(STRIKE_OUT, SUMMARY_JUDGEMENT, VARY_PAYMENT_TERMS_OF_JUDGMENT);

            CaseData caseData = CaseDataBuilder
                .builder()
                .ccdCaseReference(1234L)
                .generalAppHearingDate(createGAHearingDateGAspec(LocalDate.now().plusDays(4)))
                .generalAppType(createGAApplicationType(types)).build();

            CallbackParams params = callbackParamsOf(caseData, MID, VALIDATE_HEARING_DATE);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).isEmpty();
        }
    }

    @Nested
    class MidEventForConsentOrder extends LocationRefSampleDataBuilder {

        private static final String VALIDATE_GA_CONSENT = "ga-validate-consent";

        @Test
        void shouldNotCauseAnyErrors_whenGaTypeIsNotSettleOrDiscontinueConsent() {

            List<GeneralApplicationTypes> types = List.of(STRIKE_OUT, SUMMARY_JUDGEMENT);
            CaseData caseData = CaseDataBuilder
                .builder().generalAppType(createGAApplicationType(types))
                .build();
            caseData.setCcdCaseReference(1234L);
            caseData.setGeneralAppRespondentAgreement(createRespondentNoAgreement());

            CallbackParams params = callbackParamsOf(caseData, MID, VALIDATE_GA_CONSENT);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            assertThat(response.getErrors()).isEmpty();
        }

        @Test
        void shouldNotCauseAnyErrors_whenGaTypeIsNotSettleOrDiscontinueConsentYes() {

            List<GeneralApplicationTypes> types = List.of(SETTLE_BY_CONSENT);
            CaseData caseData = CaseDataBuilder
                .builder().generalAppType(createGAApplicationType(types))
                .build();
            caseData.setCcdCaseReference(1234L);
            caseData.setGeneralAppRespondentAgreement(createRespondentYesAgreement());

            CallbackParams params = callbackParamsOf(caseData, MID, VALIDATE_GA_CONSENT);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            assertThat(response.getErrors()).isEmpty();
        }

        @Test
        void shouldCauseError_whenGaTypeIsNotSettleOrDiscontinueConsentNo() {

            List<GeneralApplicationTypes> types = List.of(SETTLE_BY_CONSENT);
            CaseData caseData = CaseDataBuilder
                .builder().generalAppType(createGAApplicationType(types))
                .build();
            caseData.setCcdCaseReference(1234L);
            caseData.setGeneralAppRespondentAgreement(createRespondentNoAgreement());

            CallbackParams params = callbackParamsOf(caseData, MID, VALIDATE_GA_CONSENT);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            assertThat(response.getErrors()).isNotEmpty();
            assertThat(response.getErrors()).contains(INVALID_SETTLE_BY_CONSENT.getValue());
        }

        @Test
        void shouldNotCauseAnyErrors_whenGaTypeIsNotSettleOrDiscontinueConsentCoscEnabled() {

            when(theUserService.getUserInfo(anyString())).thenReturn(createUserInfo("uid"));

            when(coreCaseUserService.getUserCaseRoles(anyString(), anyString()))
                .thenReturn(List.of(CaseRole.APPLICANTSOLICITORONE.getFormattedName()));
            List<GeneralApplicationTypesLR> typesLR = List.of(GeneralApplicationTypesLR.STRIKE_OUT, GeneralApplicationTypesLR.SUMMARY_JUDGEMENT);
            CaseData caseData = CaseDataBuilder
                .builder()
                .generalAppTypeLR(createGAApplicationTypeLR(typesLR))
                .build();
            caseData.setCcdCaseReference(1234L);
            caseData.setGeneralAppRespondentAgreement(createRespondentNoAgreement());
            caseData.setApplicant1Represented(YES);

            CallbackParams params = callbackParamsOf(caseData, MID, VALIDATE_GA_CONSENT);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).isEmpty();
        }

        @Test
        void shouldNotCauseAnyErrors_whenGaTypeIsNotSettleOrDiscontinueConsentYesCoscEnabled() {

            List<GeneralApplicationTypesLR> typesLR = List.of(GeneralApplicationTypesLR.SETTLE_BY_CONSENT);
            when(theUserService.getUserInfo(anyString())).thenReturn(createUserInfo("uid"));

            when(coreCaseUserService.getUserCaseRoles(anyString(), anyString()))
                .thenReturn(List.of(CaseRole.APPLICANTSOLICITORONE.getFormattedName()));
            CaseData caseData = CaseDataBuilder
                .builder()
                .ccdCaseReference(1234L)
                .generalAppTypeLR(createGAApplicationTypeLR(typesLR))
                .build();
            caseData.setGeneralAppRespondentAgreement(createRespondentYesAgreement());
            caseData.setApplicant1Represented(YES);

            CallbackParams params = callbackParamsOf(caseData, MID, VALIDATE_GA_CONSENT);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).isEmpty();
        }

        @Test
        void shouldCauseError_whenGaTypeIsNotSettleOrDiscontinueConsentNoCoscEnabled() {

            when(theUserService.getUserInfo(anyString())).thenReturn(createUserInfo("uid"));

            when(coreCaseUserService.getUserCaseRoles(anyString(), anyString()))
                .thenReturn(List.of(CaseRole.APPLICANTSOLICITORONE.getFormattedName()));
            List<GeneralApplicationTypesLR> typesLR = List.of(GeneralApplicationTypesLR.SETTLE_BY_CONSENT);
            CaseData caseData = CaseDataBuilder
                .builder()
                .ccdCaseReference(1234L)
                .generalAppTypeLR(createGAApplicationTypeLR(typesLR))
                .build();
            caseData.setGeneralAppRespondentAgreement(createRespondentNoAgreement());
            caseData.setApplicant1Represented(YES);

            CallbackParams params = callbackParamsOf(caseData, MID, VALIDATE_GA_CONSENT);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).isNotEmpty();
            assertThat(response.getErrors()).contains(INVALID_SETTLE_BY_CONSENT.getValue());
        }
    }

    @Nested
    class MidEventForHearingScreenValidation extends LocationRefSampleDataBuilder {

        private static final String VALIDATE_HEARING_PAGE = "ga-hearing-screen-validation";

        @Test
        void shouldNotReturnErrors_whenHearingDataIsNotPresent() {
            CaseData caseData = CaseDataBuilder.builder().build();
            CallbackParams params = callbackParamsOf(caseData, MID, VALIDATE_HEARING_PAGE);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).isEmpty();
        }

        //Trial Dates validations
        @Test
        void shouldReturnErrors_whenTrialIsScheduledButTrialDateFromIsNull() {
            CaseData caseData = getTestCaseDataForHearingMidEvent(CaseDataBuilder.builder().build(), true,
                    null, null, true, getValidUnavailableDateList());
            CallbackParams params = callbackParamsOf(caseData, MID, VALIDATE_HEARING_PAGE);
            when(generalApplicationValidator.validateHearingScreen(any())).thenCallRealMethod();

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).isNotEmpty();
            assertThat(response.getErrors()).contains(TRIAL_DATE_FROM_REQUIRED);
        }

        @Test
        void shouldReturnErrors_whenTrialIsScheduledAndTrialDateFromIsProvidedWithTrialDateToBeforeIt() {
            CaseData caseData = getTestCaseDataForHearingMidEvent(CaseDataBuilder.builder().build(), true,
                    LocalDate.now(), LocalDate.now().minusDays(1), true, getValidUnavailableDateList());
            CallbackParams params = callbackParamsOf(caseData, MID, VALIDATE_HEARING_PAGE);
            when(generalApplicationValidator.validateHearingScreen(any())).thenCallRealMethod();

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).isNotEmpty();
            assertThat(response.getErrors()).contains(INVALID_TRIAL_DATE_RANGE);
        }

        @Test
        void shouldNotReturnErrors_whenTrialIsScheduledAndTrialDateFromIsProvidedWithNullTrialDateTo() {
            CaseData caseData = getTestCaseDataForHearingMidEvent(CaseDataBuilder.builder().build(), true,
                    LocalDate.now(), null, true, getValidUnavailableDateList());
            CallbackParams params = callbackParamsOf(caseData, MID, VALIDATE_HEARING_PAGE);
            when(generalApplicationValidator.validateHearingScreen(any())).thenCallRealMethod();

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).isEmpty();
        }

        @Test
        void shouldNotReturnErrors_whenTrialIsScheduledAndTrialDateFromIsProvidedWithTrialDateToAfterIt() {
            CaseData caseData = getTestCaseDataForHearingMidEvent(CaseDataBuilder.builder().build(), true,
                    LocalDate.now(), LocalDate.now().plusDays(1), true, getValidUnavailableDateList());
            CallbackParams params = callbackParamsOf(caseData, MID, VALIDATE_HEARING_PAGE);
            when(generalApplicationValidator.validateHearingScreen(any())).thenCallRealMethod();

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).isEmpty();
        }

        @Test
        void shouldNotReturnErrors_whenTrialIsScheduledAndTrialDateFromIsProvidedAndTrialDateToAreSame() {
            CaseData caseData = getTestCaseDataForHearingMidEvent(CaseDataBuilder.builder().build(), true,
                    LocalDate.now(), LocalDate.now(), true, getValidUnavailableDateList());
            CallbackParams params = callbackParamsOf(caseData, MID, VALIDATE_HEARING_PAGE);
            when(generalApplicationValidator.validateHearingScreen(any())).thenCallRealMethod();

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).isEmpty();
        }

        @Test
        void shouldNotReturnErrors_whenTrialIsNotScheduled() {
            CaseData caseData = getTestCaseDataForHearingMidEvent(CaseDataBuilder.builder().build(), false,
                    null, null, true, getValidUnavailableDateList());
            CallbackParams params = callbackParamsOf(caseData, MID, VALIDATE_HEARING_PAGE);
            when(generalApplicationValidator.validateHearingScreen(any())).thenCallRealMethod();

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).isEmpty();
        }

        //Unavailability Dates validations
        @Test
        void shouldReturnErrors_whenUnavailabilityIsSetButNullDateRangeProvided() {
            CaseData caseData = getTestCaseDataForHearingMidEvent(CaseDataBuilder.builder().build(), true,
                    LocalDate.now(), null, true, null);
            CallbackParams params = callbackParamsOf(caseData, MID, VALIDATE_HEARING_PAGE);
            when(generalApplicationValidator.validateHearingScreen(any())).thenCallRealMethod();

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).isNotEmpty();
            assertThat(response.getErrors()).contains(UNAVAILABLE_DATE_RANGE_MISSING);
        }

        @Test
        void shouldReturnErrors_whenUnavailabilityIsSetButDateRangeProvidedHasNullDateFrom() {
            GAUnavailabilityDates range1 = createGAUnavailabilityDates(null, null);

            CaseData caseData = getTestCaseDataForHearingMidEvent(CaseDataBuilder.builder().build(), true,
                    LocalDate.now(), null, true, wrapElements(range1));
            CallbackParams params = callbackParamsOf(caseData, MID, VALIDATE_HEARING_PAGE);
            when(generalApplicationValidator.validateHearingScreen(any())).thenCallRealMethod();

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).isNotEmpty();
            assertThat(response.getErrors()).contains(UNAVAILABLE_FROM_MUST_BE_PROVIDED);
        }

        @Test
        void shouldReturnErrors_whenUnavailabilityIsSetButDateRangeProvidedHasDateFromAfterDateTo() {
            GAUnavailabilityDates range1 = createGAUnavailabilityDates(LocalDate.now().plusDays(1), LocalDate.now());

            CaseData caseData = getTestCaseDataForHearingMidEvent(CaseDataBuilder.builder().build(), true,
                    LocalDate.now(), null, true, wrapElements(range1));
            CallbackParams params = callbackParamsOf(caseData, MID, VALIDATE_HEARING_PAGE);
            when(generalApplicationValidator.validateHearingScreen(any())).thenCallRealMethod();

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).isNotEmpty();
            assertThat(response.getErrors()).contains(INVALID_UNAVAILABILITY_RANGE.getValue());
        }

        @Test
        void shouldNotReturnErrors_whenUnavailabilityIsNotSet() {
            CaseData caseData = getTestCaseDataForHearingMidEvent(CaseDataBuilder.builder().build(), false,
                    null, null, false, null);
            CallbackParams params = callbackParamsOf(caseData, MID, VALIDATE_HEARING_PAGE);
            when(generalApplicationValidator.validateHearingScreen(any())).thenCallRealMethod();

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).isEmpty();
        }

        @Test
        void shouldNotReturnErrors_whenUnavailabilityIsSetAndDateFromIsValidWithNullDateTo() {
            GAUnavailabilityDates range1 = createGAUnavailabilityDates(LocalDate.now(), null);

            CaseData caseData = getTestCaseDataForHearingMidEvent(CaseDataBuilder.builder().build(), false,
                    null, null, false, wrapElements(range1));
            CallbackParams params = callbackParamsOf(caseData, MID, VALIDATE_HEARING_PAGE);
            when(generalApplicationValidator.validateHearingScreen(any())).thenCallRealMethod();

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).isEmpty();
        }

        @Test
        void shouldNotReturnErrors_whenUnavailabilityIsSetAndDateFromIsValidWithSameDateTo() {
            GAUnavailabilityDates range1 = createGAUnavailabilityDates(LocalDate.now(), LocalDate.now());

            CaseData caseData = getTestCaseDataForHearingMidEvent(CaseDataBuilder.builder().build(), false,
                    null, null, false, wrapElements(range1));
            CallbackParams params = callbackParamsOf(caseData, MID, VALIDATE_HEARING_PAGE);
            when(generalApplicationValidator.validateHearingScreen(any())).thenCallRealMethod();

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).isEmpty();
        }

        @Test
        void shouldNotReturnErrors_whenUnavailabilityIsSetAndDateFromIsBeforeDateTo() {
            GAUnavailabilityDates range1 = createGAUnavailabilityDates(LocalDate.now(), LocalDate.now().plusDays(1));

            CaseData caseData = getTestCaseDataForHearingMidEvent(CaseDataBuilder.builder().build(), false,
                    null, null, false, wrapElements(range1));
            CallbackParams params = callbackParamsOf(caseData, MID, VALIDATE_HEARING_PAGE);
            when(generalApplicationValidator.validateHearingScreen(any())).thenCallRealMethod();

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).isEmpty();
        }
    }

    @Nested
    class MidEventForSettingFeeAndPBA extends LocationRefSampleDataBuilder {

        @Test
        void shouldSetAddPbaNumbers_whenCalledAndOrgExistsInPrd() {
            given(feesService.getFeeForGA(any(CaseData.class)))
                    .willReturn(createFee(FEE_CODE, fee108, FEE_VERSION));
            CaseData caseData = CaseDataBuilder.builder().ccdCaseReference(1234L).atStateClaimDraft().build();
            CallbackParams params = callbackParamsOf(caseData, MID, SET_FEES_AND_PBA);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).isNull();
        }

        @Test
        void shouldNotResultInErrors_whenCalledAndOrgDoesNotExistInPrd() {
            given(feesService.getFeeForGA(any(CaseData.class)))
                    .willReturn(createFee(FEE_CODE, fee108, FEE_VERSION));

            CaseData caseData = CaseDataBuilder.builder()
                .ccdCaseReference(1234L)
                .atStateClaimDraft()
                .build();
            CallbackParams params = callbackParamsOf(caseData, MID, SET_FEES_AND_PBA);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).isNull();
        }

        @Test
        void shouldReturnNoError_whenNoOrgDetailsObtained() {
            given(feesService.getFeeForGA(any(CaseData.class)))
                    .willReturn(createFee(FEE_CODE, fee108, FEE_VERSION));
            CaseData caseData = CaseDataBuilder.builder().ccdCaseReference(1234L).atStateClaimIssued().build();
            CallbackParams params = callbackParamsOf(caseData, MID, SET_FEES_AND_PBA);

            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                    .handle(params);

            assertThat(response.getErrors()).isNull();
        }

        @Test
        void shouldSet108Fees_whenApplicationIsConsented() {
            given(feesService.getFeeForGA(any(CaseData.class)))
                    .willReturn(Fee.builder()
                            .code(FEE_CODE)
                            .calculatedAmountInPence(fee108)
                            .version(FEE_VERSION).build());
            CaseData caseData = GeneralApplicationDetailsBuilder.builder().getTestCaseDataForApplicationFee(
                    CaseDataBuilder.builder().build(), true, false);
            CallbackParams params = callbackParamsOf(caseData, MID, SET_FEES_AND_PBA);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).isNull();
            assertThat(getPBADetails(response).getFee()).isNotNull();
            assertThat(getPBADetails(response).getFee().getCalculatedAmountInPence()).isEqualTo("10800");
        }

        @Test
        void shouldSet108Fees_whenApplicationIsUnConsentedWithoutNotice() {
            given(feesService.getFeeForGA(any(CaseData.class)))
                    .willReturn(Fee.builder()
                            .code(FEE_CODE)
                            .calculatedAmountInPence(fee108)
                            .version(FEE_VERSION).build());
            CaseData caseData = GeneralApplicationDetailsBuilder.builder().getTestCaseDataForApplicationFee(
                    CaseDataBuilder.builder().build(), false, false);
            CallbackParams params = callbackParamsOf(caseData, MID, SET_FEES_AND_PBA);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).isNull();
            assertThat(getPBADetails(response).getFee()).isNotNull();
            assertThat(getPBADetails(response).getFee().getCalculatedAmountInPence()).isEqualTo("10800");
        }

        @Test
        void shouldSet275Fees_whenApplicationIsUnConsentedWithNotice() {
            given(feesService.getFeeForGA(any(CaseData.class)))
                    .willReturn(Fee.builder()
                            .code(FEE_CODE)
                            .calculatedAmountInPence(fee275)
                            .version(FEE_VERSION).build());
            CaseData caseData = GeneralApplicationDetailsBuilder.builder().getTestCaseDataForApplicationFee(
                    CaseDataBuilder.builder().build(), false, true);
            CallbackParams params = callbackParamsOf(caseData, MID, SET_FEES_AND_PBA);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).isNull();
            assertThat(getPBADetails(response).getFee()).isNotNull();
            assertThat(getPBADetails(response).getFee().getCalculatedAmountInPence()).isEqualTo("27500");
        }

        @Test
        void shouldSet275Fees_whenVaryApplicationIsUnConsented() {
            given(feesService.getFeeForGA(any(CaseData.class)))
                    .willReturn(Fee.builder()
                            .code(FEE_CODE)
                            .calculatedAmountInPence(fee275)
                            .version(FEE_VERSION).build());
            List<GeneralApplicationTypes> types = List.of(VARY_PAYMENT_TERMS_OF_JUDGMENT);
            GAApplicationType gaApplicationType = new GAApplicationType();
            gaApplicationType.setTypes(types);
            CaseData caseData = CaseDataBuilder
                .builder().generalAppType(gaApplicationType)
                .build();
            caseData.setGeneralAppRespondentAgreement(createRespondentNoAgreement());
            caseData.setCcdCaseReference(1234L);
            CallbackParams params = callbackParamsOf(caseData, MID, SET_FEES_AND_PBA);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).isNull();
            assertThat(getPBADetails(response).getFee()).isNotNull();
            assertThat(getPBADetails(response).getFee().getCalculatedAmountInPence()).isEqualTo("27500");
        }

        @Test
        void shouldSet275Fees_whenVaryApplicationIsUnConsentedCoscEnabled() {
            //Add cosc tests
            given(feesService.getFeeForGA(any(CaseData.class)))
                .willReturn(Fee.builder()
                                .code(FEE_CODE)
                                .calculatedAmountInPence(fee275)
                                .version(FEE_VERSION).build());
            when(theUserService.getUserInfo(anyString())).thenReturn(UserInfo.builder().uid("uid").build());

            when(coreCaseUserService.getUserCaseRoles(anyString(), anyString()))
                .thenReturn(List.of(CaseRole.APPLICANTSOLICITORONE.getFormattedName()));
            List<GeneralApplicationTypesLR> typesLR = List.of(GeneralApplicationTypesLR.VARY_PAYMENT_TERMS_OF_JUDGMENT);
            CaseData caseData = CaseDataBuilder
                .builder()
                .generalAppTypeLR(GAApplicationTypeLR.builder().types(typesLR).build())
                .ccdCaseReference(1234L)
                .build();
            caseData.setGeneralAppRespondentAgreement(createRespondentNoAgreement());
            caseData.setApplicant1Represented(YES);
            CallbackParams params = callbackParamsOf(caseData, MID, SET_FEES_AND_PBA);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).isNull();
            assertThat(getPBADetails(response).getFee()).isNotNull();
            assertThat(getPBADetails(response).getFee().getCalculatedAmountInPence()).isEqualTo("27500");
        }

        @Test
        void shouldSet14Fees_whenApplicationIsVaryOrder() {
            given(feesService.getFeeForGA(any(CaseData.class)))
                .willReturn(Fee.builder()
                                .code(FEE_CODE)
                                .calculatedAmountInPence(fee14)
                                .build());
            CaseData caseData = GeneralApplicationDetailsBuilder.builder().getTestCaseDataForApplicationFee(
                CaseDataBuilder.builder().build(), false, false);
            GAApplicationType gaApplicationType = new GAApplicationType();
            gaApplicationType.setTypes(singletonList(VARY_ORDER));
            caseData.setGeneralAppType(gaApplicationType);
            CallbackParams params = callbackParamsOf(caseData, MID, SET_FEES_AND_PBA);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).isNull();
            assertThat(getPBADetails(response).getFee()).isNotNull();
            assertThat(getPBADetails(response).getFee().getCalculatedAmountInPence()).isEqualTo("1400");
        }

        @Test
        void shouldSet14Fees_whenApplicationIsVaryOrderCoscEnabled() {
            given(feesService.getFeeForGA(any(CaseData.class)))
                .willReturn(Fee.builder()
                                .code(FEE_CODE)
                                .calculatedAmountInPence(fee14)
                                .build());
            CaseData caseData = GeneralApplicationDetailsBuilder.builder().getTestCaseDataForApplicationFee(
                CaseDataBuilder.builder().build(), false, false);

            when(theUserService.getUserInfo(anyString())).thenReturn(UserInfo.builder().uid("uid").build());

            when(coreCaseUserService.getUserCaseRoles(anyString(), anyString()))
                .thenReturn(List.of(CaseRole.APPLICANTSOLICITORONE.getFormattedName()));
            List<GeneralApplicationTypesLR> typesLR = List.of(GeneralApplicationTypesLR.VARY_ORDER);
            caseData.setGeneralAppTypeLR(GAApplicationTypeLR.builder().types(typesLR).build());
            caseData.setApplicant1Represented(YES);
            CallbackParams params = callbackParamsOf(caseData, MID, SET_FEES_AND_PBA);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).isNull();
            assertThat(getPBADetails(response).getFee()).isNotNull();
            assertThat(getPBADetails(response).getFee().getCalculatedAmountInPence()).isEqualTo("1400");
        }

        @Test
        void shouldSet14Fees_whenApplicationIsVaryOrderWithMultipleTypes() {
            given(feesService.getFeeForGA(any(CaseData.class)))
                .willReturn(Fee.builder()
                                .code(FEE_CODE)
                                .calculatedAmountInPence(fee14).build());
            CaseData caseData = GeneralApplicationDetailsBuilder.builder().getTestCaseDataForApplicationFee(
                CaseDataBuilder.builder().build(), false, false);
            List<GeneralApplicationTypes> types = List.of(VARY_ORDER, STAY_THE_CLAIM);
            GAApplicationType gaApplicationType = new GAApplicationType();
            gaApplicationType.setTypes(types);
            caseData.setGeneralAppType(gaApplicationType);
            CallbackParams params = callbackParamsOf(caseData, MID, SET_FEES_AND_PBA);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).isNull();
            assertThat(getPBADetails(response).getFee()).isNotNull();
            assertThat(getPBADetails(response).getFee().getCalculatedAmountInPence()).isEqualTo("1400");
        }

        @Test
        void shouldSet14Fees_whenApplicationIsVaryOrderWithMultipleTypesCoscEnabled() {
            given(feesService.getFeeForGA(any(CaseData.class)))
                .willReturn(Fee.builder()
                                .code(FEE_CODE)
                                .calculatedAmountInPence(fee14).build());
            CaseData caseData = GeneralApplicationDetailsBuilder.builder().getTestCaseDataForApplicationFee(
                CaseDataBuilder.builder().build(), false, false);

            when(theUserService.getUserInfo(anyString())).thenReturn(UserInfo.builder().uid("uid").build());
            when(coreCaseUserService.getUserCaseRoles(anyString(), anyString()))
                .thenReturn(List.of(CaseRole.APPLICANTSOLICITORONE.getFormattedName()));
            List<GeneralApplicationTypesLR> typesLR = List.of(GeneralApplicationTypesLR.VARY_ORDER, GeneralApplicationTypesLR.STAY_THE_CLAIM);
            caseData.setGeneralAppTypeLR(GAApplicationTypeLR.builder().types(typesLR).build());
            caseData.setApplicant1Represented(YES);
            CallbackParams params = callbackParamsOf(caseData, MID, SET_FEES_AND_PBA);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).isNull();
            assertThat(getPBADetails(response).getFee()).isNotNull();
            assertThat(getPBADetails(response).getFee().getCalculatedAmountInPence()).isEqualTo("1400");
        }

        private GAPbaDetails getPBADetails(AboutToStartOrSubmitCallbackResponse response) {
            CaseData responseCaseData = objectMapper.convertValue(response.getData(), CaseData.class);
            return responseCaseData.getGeneralAppPBADetails();
        }
    }

    @Nested
    class AboutToSubmit extends LocationRefSampleDataBuilder {

        private final Fee feeFromFeeService = Fee.builder().code(FEE_CODE).calculatedAmountInPence(fee108)
                .version(FEE_VERSION).build();

        @Test
        void shouldAddNewApplicationToList_whenInvoked() {
            CaseData caseData = GeneralApplicationDetailsBuilder.builder()
                .getTestCaseData(CaseData.builder().build());

            when(theUserService.getUserDetails(anyString())).thenReturn(UserDetails.builder().id(STRING_CONSTANT)
                                                                      .email(APPLICANT_EMAIL_ID_CONSTANT)
                                                                      .build());
            when(initiateGeneralAppService.buildCaseData(any(CaseData.class), any(UserDetails.class), anyString()))
                .thenReturn(caseData);
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).isNull();
            assertResponse(objectMapper.convertValue(response.getData(), CaseData.class));
        }

        @Test
        void shouldSetAppropriateFees_whenFeesAreUnsetByCCD() {
            CaseData caseData = GeneralApplicationDetailsBuilder.builder()
                    .getTestCaseData(CaseData.builder().build());
            when(theUserService.getUserDetails(anyString())).thenReturn(UserDetails.builder().id(STRING_CONSTANT)
                    .email(APPLICANT_EMAIL_ID_CONSTANT)
                    .build());

            when(initiateGeneralAppService.buildCaseData(any(CaseData.class), any(UserDetails.class), anyString()))
                    .thenReturn(getMockServiceData(caseData));

            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).isNull();
            CaseData responseCaseData = objectMapper.convertValue(response.getData(), CaseData.class);
            GeneralApplication application = unwrapElements(responseCaseData.getGeneralApplications()).get(0);
            assertThat(application.getGeneralAppPBADetails()).isNotNull();
            assertThat(application.getGeneralAppPBADetails().getFee()).isNotNull();
        }

        @Test
        void handleEventsReturnsTheExpectedCallbackEvent() {
            assertThat(handler.handledEvents()).contains(INITIATE_GENERAL_APPLICATION);
        }

        private CaseData getMockServiceData(CaseData caseData) {
            GAPbaDetails pbaDetails = caseData.getGeneralAppPBADetails();
            if (pbaDetails == null) {
                pbaDetails = new GAPbaDetails();
                caseData.setGeneralAppPBADetails(pbaDetails);
            }
            pbaDetails.setFee(feeFromFeeService);
            return caseData;
        }

        private void assertResponse(CaseData responseData) {
            assertThat(responseData)
                .extracting("generalApplications")
                .isNotNull();
            GeneralApplication application = unwrapElements(responseData.getGeneralApplications()).get(0);
            assertThat(application.getGeneralAppType().getTypes().contains(EXTEND_TIME)).isTrue();
            assertThat(application.getGeneralAppRespondentAgreement().getHasAgreed()).isEqualTo(NO);
            assertThat(application.getGeneralAppDetailsOfOrder()).isEqualTo(STRING_CONSTANT);
            assertThat(application.getGeneralAppReasonsOfOrder()).isEqualTo(STRING_CONSTANT);
            assertThat(application.getGeneralAppInformOtherParty().getReasonsForWithoutNotice())
                .isEqualTo(STRING_CONSTANT);
            assertThat(application.getGeneralAppUrgencyRequirement().getUrgentAppConsiderationDate())
                .isEqualTo(APP_DATE_EPOCH);
            assertThat(application.getGeneralAppStatementOfTruth().getName()).isEqualTo(STRING_CONSTANT);
            assertThat(unwrapElements(application.getGeneralAppEvidenceDocument()).get(0).getDocumentUrl())
                .isEqualTo(STRING_CONSTANT);
            assertThat(application.getGeneralAppHearingDetails().getSupportRequirement()
                           .contains(OTHER_SUPPORT)).isTrue();
            assertThat(application.getGeneralAppHearingDetails().getHearingDuration()).isEqualTo(OTHER);
            assertThat(application.getGeneralAppHearingDetails().getGeneralAppHearingDays()).isEqualTo("1");
            assertThat(application.getGeneralAppHearingDetails().getGeneralAppHearingHours()).isEqualTo("2");
            assertThat(application.getGeneralAppHearingDetails().getGeneralAppHearingMinutes()).isEqualTo("30");
            assertThat(application.getGeneralAppHearingDetails().getHearingPreferencesPreferredType())
                .isEqualTo(IN_PERSON);
            assertThat(application.getIsMultiParty()).isEqualTo(NO);
        }

        @Test
        void shouldSetDynamicListWhenPreferredLocationValueIsNull() {

            CaseData caseData = GeneralApplicationDetailsBuilder.builder()
                .getTestCaseDataWithEmptyPreferredLocation(CaseData.builder().ccdCaseReference(1234L).build());
            when(theUserService.getUserDetails(anyString())).thenReturn(UserDetails.builder().id(STRING_CONSTANT)
                                                                        .email(APPLICANT_EMAIL_ID_CONSTANT)
                                                                        .build());
            when(initiateGeneralAppService.buildCaseData(any(CaseData.class), any(UserDetails.class), anyString()))
                .thenReturn(getMockServiceData(caseData));

            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            CaseData data = objectMapper.convertValue(response.getData(), CaseData.class);
            DynamicList dynamicList = getLocationDynamicList(data);
            assertThat(data.getGeneralAppHearingDetails()).isNotNull();
            assertThat(dynamicList).satisfiesAnyOf(
                list -> assertThat(list).isNull(),
                list -> assertThat(list.getListItems()).isNullOrEmpty()
            );
        }

        @Test
        void shouldWithNotice_whenVaryApplicationIsUnConsentedLiP() {
            GAPbaDetails generalAppPBADetails = GAPbaDetails.builder().fee(feeFromFeeService).build();

            List<GeneralApplicationTypes> types = List.of(VARY_PAYMENT_TERMS_OF_JUDGMENT);
            GAApplicationType gaApplicationType = new GAApplicationType();
            gaApplicationType.setTypes(types);
            CaseData caseData = CaseDataBuilder
                    .builder().generalAppType(gaApplicationType)
                    .build();
            caseData.setCcdCaseReference(1234L);
            caseData.setGeneralAppPBADetails(generalAppPBADetails);
            caseData.setGeneralAppHearingDetails(GAHearingDetails.builder().build());
            caseData.setGeneralAppRespondentAgreement(createRespondentNoAgreement());
            when(theUserService.getUserDetails(anyString())).thenReturn(UserDetails.builder().id(STRING_CONSTANT)
                    .email(APPLICANT_EMAIL_ID_CONSTANT)
                    .build());

            when(initiateGeneralAppService.buildCaseData(
                any(CaseData.class), any(UserDetails.class), anyString())).thenAnswer((Answer) invocation -> invocation.getArguments()[0]
            );

            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
            params.getRequest().setEventId(INITIATE_GENERAL_APPLICATION.name());
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            CaseData data = objectMapper.convertValue(response.getData(), CaseData.class);
            assertThat(response.getErrors()).isNull();
            assertThat(data.getGeneralAppInformOtherParty().getIsWithNotice()).isEqualTo(YES);
        }

        @Test
        void shouldWithNotice_whenVaryApplicationIsUnConsentedLR() {
            GAPbaDetails generalAppPBADetails = GAPbaDetails.builder().fee(feeFromFeeService).build();

            List<GeneralApplicationTypesLR> types = List.of(GeneralApplicationTypesLR.VARY_PAYMENT_TERMS_OF_JUDGMENT);
            CaseData caseData = CaseDataBuilder
                .builder().generalAppTypeLR(GAApplicationTypeLR.builder().types(types).build())
                .build();
            caseData.setCcdCaseReference(1234L);
            caseData.setGeneralAppPBADetails(generalAppPBADetails);
            caseData.setGeneralAppHearingDetails(GAHearingDetails.builder().build());
            caseData.setGeneralAppRespondentAgreement(createRespondentNoAgreement());
            when(theUserService.getUserDetails(anyString())).thenReturn(UserDetails.builder().id(STRING_CONSTANT)
                                                                            .email(APPLICANT_EMAIL_ID_CONSTANT)
                                                                            .build());
            when(theUserService.getUserInfo(anyString())).thenReturn(UserInfo.builder().uid("uid").build());
            when(initiateGeneralAppService.buildCaseData(
                any(CaseData.class), any(UserDetails.class), anyString())).thenAnswer((Answer) invocation -> invocation.getArguments()[0]
            );

            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            CaseData data = objectMapper.convertValue(response.getData(), CaseData.class);
            assertThat(response.getErrors()).isNull();
            assertThat(data.getGeneralAppInformOtherParty().getIsWithNotice()).isEqualTo(YES);
        }
    }

    @Nested
    class SubmittedCallback {

        @Test
        void handleEventsReturnsTheExpectedCallbackEvent() {
            assertThat(handler.handledEvents()).contains(INITIATE_GENERAL_APPLICATION);
        }

        @Test
        void shouldReturnExpectedSubmittedCallbackResponse_whengaLips_is_enable() {
            CaseData caseData = getReadyTestCaseData(
                CaseDataBuilder.builder().ccdCaseReference(CASE_ID).build(), true);
            CallbackParams params = callbackParamsOf(caseData, SUBMITTED);
            GeneralApplication genapp = caseData.getGeneralApplications().get(0).getValue();
            when(generalAppFeesService.isFreeGa(any())).thenReturn(false);
            String body = format(
                confirmationBodyBasedOnToggle(true),
                genapp.getGeneralAppPBADetails().getFee().toPounds(),
                format("/cases/case-details/%s#Applications", CASE_ID)
            );

            var response = (SubmittedCallbackResponse) handler.handle(params);
            assertThat(response).isNotNull();
            assertThat(response).usingRecursiveComparison().isEqualTo(
                SubmittedCallbackResponse.builder()
                    .confirmationHeader(
                        "# You have submitted an application")
                    .confirmationBody(body)
                    .build());
            assertThat(response).isNotNull();
            assertThat(response.getConfirmationBody()).isEqualTo(body);
        }

        @Test
        void shouldReturnFreeGAConfirmationBodyBody_whenFreeGA() {
            CaseData caseData = getReadyTestCaseData(
                CaseDataBuilder.builder().ccdCaseReference(CASE_ID).build(), true);
            CallbackParams params = callbackParamsOf(caseData, SUBMITTED);
            when(generalAppFeesService.isFreeGa(any())).thenReturn(true);

            var response = (SubmittedCallbackResponse) handler.handle(params);
            assertThat(response).isNotNull();
            assertThat(response).usingRecursiveComparison().isEqualTo(
                SubmittedCallbackResponse.builder()
                    .confirmationHeader(
                        "# You have submitted an application")
                    .confirmationBody(CONFIRMATION_BODY_FREE)
                    .build());
            assertThat(response).isNotNull();
            assertThat(response.getConfirmationBody()).isEqualTo(CONFIRMATION_BODY_FREE);
        }

        @Test
        void shouldNotReturnBuildConfirmationIfGeneralApplicationIsEmpty() {
            CaseData caseData = getEmptyTestCase(CaseDataBuilder.builder().build());
            CallbackParams params = callbackParamsOf(caseData, SUBMITTED);

            var response = (SubmittedCallbackResponse) handler.handle(params);
            assertThat(response).isNotNull();
            assertThat(response.getConfirmationBody()).isNull();
        }
    }

    private CaseData getEmptyTestCase(CaseData caseData) {
        return caseData.toBuilder()
            .build();
    }

    private CaseData getReadyTestCaseData(CaseData caseData, boolean multipleGenAppTypes) {
        GAInformOtherParty withOrWithoutNotice = GAInformOtherParty.builder()
            .isWithNotice(YES)
            .reasonsForWithoutNotice(STRING_CONSTANT)
            .build();
        GARespondentOrderAgreement withOrWithoutConsent = GARespondentOrderAgreement.builder()
            .hasAgreed(NO).build();

        return getReadyTestCaseData(caseData, multipleGenAppTypes, withOrWithoutConsent, withOrWithoutNotice);
    }

    private CaseData getReadyTestCaseData(CaseData caseData,
                                          boolean multipleGenAppTypes,
                                          GARespondentOrderAgreement hasAgreed,
                                          GAInformOtherParty withOrWithoutNotice) {
        GeneralApplication.GeneralApplicationBuilder builder = GeneralApplication.builder();
        if (multipleGenAppTypes) {
            builder.generalAppType(GAApplicationType.builder()
                                       .types(Arrays.asList(EXTEND_TIME, SUMMARY_JUDGEMENT))
                                       .build());
        } else {
            builder.generalAppType(GAApplicationType.builder()
                                       .types(singletonList(EXTEND_TIME))
                                       .build());
        }
        GeneralApplication application = builder

            .generalAppInformOtherParty(withOrWithoutNotice)
            .generalAppRespondentAgreement(hasAgreed)
            .generalAppPBADetails(
                GAPbaDetails.builder()
                    .fee(FEE275)
                    .serviceReqReference(CUSTOMER_REFERENCE).build())
            .generalAppUrgencyRequirement(GAUrgencyRequirement.builder()
                                              .generalAppUrgency(YES)
                                              .reasonsForUrgency(STRING_CONSTANT)
                                              .urgentAppConsiderationDate(APP_DATE_EPOCH)
                                              .build())
            .isMultiParty(NO)
            .businessProcess(BusinessProcess.builder()
                                 .status(BusinessProcessStatus.READY)
                                 .build())
            .build();
        return getEmptyTestCase(caseData)
            .toBuilder()
            .generalApplications(wrapElements(application))
            .build();
    }

    private static CaseLocationCivil createCaseLocationCivil() {
        CaseLocationCivil location = new CaseLocationCivil();
        location.setBaseLocation("45678");
        location.setRegion("4");
        return location;
    }

    private static IdamUserDetails createDefendantUserDetails() {
        IdamUserDetails userDetails = new IdamUserDetails();
        userDetails.setEmail("abc@defendant");
        return userDetails;
    }

    private static CaseDataLiP createBilingualCaseDataLiP() {
        RespondentLiPResponse lipResponse = new RespondentLiPResponse();
        lipResponse.setRespondent1ResponseLanguage("BOTH");
        CaseDataLiP caseDataLiP = new CaseDataLiP();
        caseDataLiP.setRespondent1LiPResponse(lipResponse);
        return caseDataLiP;
    }

    private static GARespondentOrderAgreement createRespondentNoAgreement() {
        return GARespondentOrderAgreement.builder()
            .hasAgreed(NO)
            .build();
    }

    private static GARespondentOrderAgreement createRespondentYesAgreement() {
        return GARespondentOrderAgreement.builder()
            .hasAgreed(YES)
            .build();
    }

    private static GAApplicationType createGAApplicationType(List<GeneralApplicationTypes> types) {
        GAApplicationType gaApplicationType = new GAApplicationType();
        gaApplicationType.setTypes(types);
        return gaApplicationType;
    }

    private static GAApplicationTypeLR createGAApplicationTypeLR(List<GeneralApplicationTypesLR> types) {
        return GAApplicationTypeLR.builder()
            .types(types)
            .build();
    }

    private static GAHearingDateGAspec createGAHearingDateGAspec(LocalDate hearingDate) {
        return GAHearingDateGAspec.builder()
            .hearingScheduledPreferenceYesNo(YES)
            .hearingScheduledDate(hearingDate)
            .build();
    }

    private static GAUnavailabilityDates createGAUnavailabilityDates(LocalDate from, LocalDate to) {
        return GAUnavailabilityDates.builder()
            .unavailableTrialDateFrom(from)
            .unavailableTrialDateTo(to)
            .build();
    }

    private static UserInfo createUserInfo(String uid) {
        return UserInfo.builder()
            .uid(uid)
            .build();
    }

    private static Fee createFee(String code, BigDecimal calculatedAmountInPence, String version) {
        return Fee.builder()
            .code(code)
            .calculatedAmountInPence(calculatedAmountInPence)
            .version(version)
            .build();
    }

    private String confirmationBodyBasedOnToggle(Boolean isGaForLipsEnabled) {
        StringBuilder bodyConfirmation = new StringBuilder();
        bodyConfirmation.append("<br/>");
        bodyConfirmation.append("<p class=\"govuk-body govuk-!-font-weight-bold\"> Your application fee of £%s"
                                    + " is now due for payment. Your application will not be processed further"
                                    + " until this fee is paid.</p>");
        bodyConfirmation.append("%n%n To pay this fee, click the link below, or else open your application from the"
                                    + " Applications tab of this case listing and then click on the service request tab.");

        if (isGaForLipsEnabled) {
            bodyConfirmation.append("%n%n If necessary, all documents relating to this application, "
                                        + "including any response from the court, will be translated."
                                        + " You will be notified when these are available.");
        }

        bodyConfirmation.append("%n%n <a href=\"%s\" target=\"_blank\">Pay your application fee </a> %n");
        return bodyConfirmation.toString();
    }
}
