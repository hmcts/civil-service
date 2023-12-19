package uk.gov.hmcts.reform.civil.handler.callback.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.enums.dq.GeneralApplicationTypes;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Fee;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.genapplication.GAApplicationType;
import uk.gov.hmcts.reform.civil.model.genapplication.GAHearingDateGAspec;
import uk.gov.hmcts.reform.civil.model.genapplication.GAHearingDetails;
import uk.gov.hmcts.reform.civil.model.genapplication.GAPbaDetails;
import uk.gov.hmcts.reform.civil.model.genapplication.GARespondentOrderAgreement;
import uk.gov.hmcts.reform.civil.model.genapplication.GAUnavailabilityDates;
import uk.gov.hmcts.reform.civil.model.genapplication.GeneralApplication;
import uk.gov.hmcts.reform.civil.referencedata.model.LocationRefData;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.sampledata.GeneralApplicationDetailsBuilder;
import uk.gov.hmcts.reform.civil.sampledata.LocationRefSampleDataBuilder;
import uk.gov.hmcts.reform.civil.service.GeneralAppFeesService;
import uk.gov.hmcts.reform.civil.service.InitiateGeneralApplicationService;
import uk.gov.hmcts.reform.civil.service.InitiateGeneralApplicationServiceHelper;
import uk.gov.hmcts.reform.civil.service.OrganisationService;
import uk.gov.hmcts.reform.civil.referencedata.LocationRefDataService;
import uk.gov.hmcts.reform.civil.utils.UserRoleCaching;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;
import uk.gov.hmcts.reform.civil.prd.model.Organisation;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
import static uk.gov.hmcts.reform.civil.enums.dq.GeneralApplicationTypes.VARY_JUDGEMENT;
import static uk.gov.hmcts.reform.civil.enums.dq.GeneralApplicationTypes.VARY_ORDER;
import static uk.gov.hmcts.reform.civil.service.InitiateGeneralApplicationService.INVALID_SETTLE_BY_CONSENT;
import static uk.gov.hmcts.reform.civil.service.InitiateGeneralApplicationService.INVALID_TRIAL_DATE_RANGE;
import static uk.gov.hmcts.reform.civil.service.InitiateGeneralApplicationService.INVALID_UNAVAILABILITY_RANGE;
import static uk.gov.hmcts.reform.civil.service.InitiateGeneralApplicationService.TRIAL_DATE_FROM_REQUIRED;
import static uk.gov.hmcts.reform.civil.service.InitiateGeneralApplicationService.UNAVAILABLE_DATE_RANGE_MISSING;
import static uk.gov.hmcts.reform.civil.service.InitiateGeneralApplicationService.UNAVAILABLE_FROM_MUST_BE_PROVIDED;
import static uk.gov.hmcts.reform.civil.service.InitiateGeneralApplicationService.URGENCY_DATE_CANNOT_BE_IN_PAST;
import static uk.gov.hmcts.reform.civil.service.InitiateGeneralApplicationService.URGENCY_DATE_REQUIRED;
import static uk.gov.hmcts.reform.civil.service.InitiateGeneralApplicationService.URGENCY_DATE_SHOULD_NOT_BE_PROVIDED;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.unwrapElements;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.wrapElements;

@SpringBootTest(classes = {
    InitiateGeneralApplicationHandler.class,
    JacksonAutoConfiguration.class,
})
class InitiateGeneralApplicationHandlerTest extends BaseCallbackHandlerTest {

    @Autowired
    private InitiateGeneralApplicationHandler handler;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private InitiateGeneralApplicationService initiateGeneralAppService;

    @MockBean
    private InitiateGeneralApplicationServiceHelper helper;

    @MockBean
    private OrganisationService organisationService;

    @MockBean
    protected IdamClient idamClient;

    @MockBean
    protected GeneralAppFeesService feesService;

    @MockBean
    protected LocationRefDataService locationRefDataService;

    @MockBean
    protected UserRoleCaching userRoleCaching;

    public static final String APPLICANT_EMAIL_ID_CONSTANT = "testUser@gmail.com";
    public static final String RESPONDENT_EMAIL_ID_CONSTANT = "respondent@gmail.com";

    private static final String SET_FEES_AND_PBA = "ga-fees-and-pba";
    private final BigDecimal fee108 = new BigDecimal("10800");
    private final BigDecimal fee14 = new BigDecimal("1400");
    private final BigDecimal fee275 = new BigDecimal("27500");
    private static final String FEE_CODE = "test_fee_code";
    private static final String FEE_VERSION = "1";

    @Nested
    class MidEventForUrgencyCheck extends LocationRefSampleDataBuilder {

        private static final String VALIDATE_URGENCY_DATE_PAGE = "ga-validate-urgency-date";

        @Test
        void shouldNotCauseAnyErrors_whenApplicationDetailsNotProvided() {
            CaseData caseData = CaseDataBuilder.builder().build();
            CallbackParams params = callbackParamsOf(caseData, MID, VALIDATE_URGENCY_DATE_PAGE);
            when(initiateGeneralAppService.validateUrgencyDates(any())).thenCallRealMethod();

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).isEmpty();
        }

        @Test
        void shouldReturnErrors_whenApplicationIsUrgentButConsiderationDateIsNotProvided() {
            CaseData caseData = GeneralApplicationDetailsBuilder.builder()
                .getTestCaseDataForUrgencyCheckMidEvent(CaseDataBuilder.builder().build(),
                                                        true, null);

            CallbackParams params = callbackParamsOf(caseData, MID, VALIDATE_URGENCY_DATE_PAGE);
            when(initiateGeneralAppService.validateUrgencyDates(any())).thenCallRealMethod();

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
            when(initiateGeneralAppService.validateUrgencyDates(any())).thenCallRealMethod();

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
            when(initiateGeneralAppService.validateUrgencyDates(any())).thenCallRealMethod();

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
            when(initiateGeneralAppService.validateUrgencyDates(any())).thenCallRealMethod();

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).isEmpty();
        }

        @Test
        void shouldNotCauseAnyErrors_whenApplicationIsNotUrgentAndConsiderationDateIsNotProvided() {
            CaseData caseData = GeneralApplicationDetailsBuilder.builder()
                .getTestCaseDataForUrgencyCheckMidEvent(CaseDataBuilder.builder().build(),
                                                        false, null);

            CallbackParams params = callbackParamsOf(caseData, MID, VALIDATE_URGENCY_DATE_PAGE);
            when(initiateGeneralAppService.validateUrgencyDates(any())).thenCallRealMethod();

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
                .builder().generalAppType(GAApplicationType.builder().types(types).build()).build();

            CallbackParams params = callbackParamsOf(caseData, MID, VALIDATE_GA_TYPE);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            CaseData responseCaseData = getCaseData(response);

            assertThat(responseCaseData.getGeneralAppVaryJudgementType()).isEqualTo(NO);
            assertThat(response.getErrors()).isEmpty();
        }

        @Test
        void shouldNotCauseAnyErrorsWhenGaTypeIsVaryJudgement() {
            List<GeneralApplicationTypes> types = List.of(VARY_JUDGEMENT);
            CaseData caseData = CaseDataBuilder
                .builder().generalAppType(GAApplicationType.builder().types(types).build()).build();

            CallbackParams params = callbackParamsOf(caseData, MID, VALIDATE_GA_TYPE);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            CaseData responseCaseData = getCaseData(response);

            assertThat(responseCaseData.getGeneralAppVaryJudgementType()).isEqualTo(YES);
            assertThat(response.getErrors()).isEmpty();
        }

        @Test
        void shouldNotCauseAnyErrorsWhenGaTypeIsMultipleTypeWithVaryJudgement() {
            List<GeneralApplicationTypes> types = List.of(STRIKE_OUT, SUMMARY_JUDGEMENT, VARY_JUDGEMENT);
            CaseData caseData = CaseDataBuilder
                .builder().generalAppType(GAApplicationType.builder().types(types).build()).build();

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
                    .builder().generalAppType(GAApplicationType.builder().types(types).build()).build();

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
            List<GeneralApplicationTypes> types = List.of(VARY_JUDGEMENT);
            CaseData caseData = CaseDataBuilder
                .builder()
                .generalAppHearingDate(GAHearingDateGAspec.builder().hearingScheduledPreferenceYesNo(YES)
                                           .hearingScheduledDate(LocalDate.now().minusDays(3))
                                           .build())
                .generalAppType(GAApplicationType.builder().types(types).build()).build();

            CallbackParams params = callbackParamsOf(caseData, MID, VALIDATE_HEARING_DATE);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors().size()).isEqualTo(1);

            assertThat(response.getErrors().get(0)).isEqualTo(INVALID_HEARING_DATE);
        }

        @Test
        void shouldNotCauseAnyErrorsWhenHearingDateIsPresent() {
            List<GeneralApplicationTypes> types = List.of(STRIKE_OUT, SUMMARY_JUDGEMENT, VARY_JUDGEMENT);

            CaseData caseData = CaseDataBuilder
                .builder()
                .generalAppHearingDate(GAHearingDateGAspec.builder().hearingScheduledPreferenceYesNo(YES)
                                           .hearingScheduledDate(LocalDate.now())
                                           .build())
                .generalAppType(GAApplicationType.builder().types(types).build()).build();

            CallbackParams params = callbackParamsOf(caseData, MID, VALIDATE_HEARING_DATE);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).isEmpty();
        }

        @Test
        void shouldNotCauseAnyErrorsWhenHearingDateIsFuture() {
            List<GeneralApplicationTypes> types = List.of(STRIKE_OUT, SUMMARY_JUDGEMENT, VARY_JUDGEMENT);

            CaseData caseData = CaseDataBuilder
                .builder()
                .generalAppHearingDate(GAHearingDateGAspec.builder().hearingScheduledPreferenceYesNo(YES)
                                           .hearingScheduledDate(LocalDate.now().plusDays(4))
                                           .build())
                .generalAppType(GAApplicationType.builder().types(types).build()).build();

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
                    .builder().generalAppType(GAApplicationType.builder().types(types).build())
                    .build().toBuilder()
                    .generalAppRespondentAgreement(GARespondentOrderAgreement
                            .builder().hasAgreed(NO).build())
                    .build();

            CallbackParams params = callbackParamsOf(caseData, MID, VALIDATE_GA_CONSENT);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).isEmpty();
        }

        @Test
        void shouldNotCauseAnyErrors_whenGaTypeIsNotSettleOrDiscontinueConsentYes() {

            List<GeneralApplicationTypes> types = List.of(SETTLE_BY_CONSENT);
            CaseData caseData = CaseDataBuilder
                    .builder().generalAppType(GAApplicationType.builder().types(types).build())
                    .build().toBuilder()
                    .generalAppRespondentAgreement(GARespondentOrderAgreement
                            .builder().hasAgreed(YES).build())
                    .build();

            CallbackParams params = callbackParamsOf(caseData, MID, VALIDATE_GA_CONSENT);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).isEmpty();
        }

        @Test
        void shouldCauseError_whenGaTypeIsNotSettleOrDiscontinueConsentNo() {

            List<GeneralApplicationTypes> types = List.of(SETTLE_BY_CONSENT);
            CaseData caseData = CaseDataBuilder
                    .builder().generalAppType(GAApplicationType.builder().types(types).build())
                    .build().toBuilder()
                    .generalAppRespondentAgreement(GARespondentOrderAgreement
                            .builder().hasAgreed(NO).build())
                    .build();

            CallbackParams params = callbackParamsOf(caseData, MID, VALIDATE_GA_CONSENT);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).isNotEmpty();
            assertThat(response.getErrors()).contains(INVALID_SETTLE_BY_CONSENT);
        }
    }

    @Nested
    class MidEventForHearingScreenValidation extends LocationRefSampleDataBuilder {

        private static final String VALIDATE_HEARING_PAGE = "ga-hearing-screen-validation";

        @Test
        void shouldNotReturnErrors_whenHearingDataIsNotPresent() {
            CaseData caseData = CaseDataBuilder.builder().build();
            CallbackParams params = callbackParamsOf(caseData, MID, VALIDATE_HEARING_PAGE);
            when(initiateGeneralAppService.validateHearingScreen(any())).thenCallRealMethod();

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).isEmpty();
        }

        //Trial Dates validations
        @Test
        void shouldReturnErrors_whenTrialIsScheduledButTrialDateFromIsNull() {
            CaseData caseData = getTestCaseDataForHearingMidEvent(CaseDataBuilder.builder().build(), true,
                    null, null, true, getValidUnavailableDateList());
            CallbackParams params = callbackParamsOf(caseData, MID, VALIDATE_HEARING_PAGE);
            when(initiateGeneralAppService.validateHearingScreen(any())).thenCallRealMethod();

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).isNotEmpty();
            assertThat(response.getErrors()).contains(TRIAL_DATE_FROM_REQUIRED);
        }

        @Test
        void shouldReturnErrors_whenTrialIsScheduledAndTrialDateFromIsProvidedWithTrialDateToBeforeIt() {
            CaseData caseData = getTestCaseDataForHearingMidEvent(CaseDataBuilder.builder().build(), true,
                    LocalDate.now(), LocalDate.now().minusDays(1), true, getValidUnavailableDateList());
            CallbackParams params = callbackParamsOf(caseData, MID, VALIDATE_HEARING_PAGE);
            when(initiateGeneralAppService.validateHearingScreen(any())).thenCallRealMethod();

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).isNotEmpty();
            assertThat(response.getErrors()).contains(INVALID_TRIAL_DATE_RANGE);
        }

        @Test
        void shouldNotReturnErrors_whenTrialIsScheduledAndTrialDateFromIsProvidedWithNullTrialDateTo() {
            CaseData caseData = getTestCaseDataForHearingMidEvent(CaseDataBuilder.builder().build(), true,
                    LocalDate.now(), null, true, getValidUnavailableDateList());
            CallbackParams params = callbackParamsOf(caseData, MID, VALIDATE_HEARING_PAGE);
            when(initiateGeneralAppService.validateHearingScreen(any())).thenCallRealMethod();

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).isEmpty();
        }

        @Test
        void shouldNotReturnErrors_whenTrialIsScheduledAndTrialDateFromIsProvidedWithTrialDateToAfterIt() {
            CaseData caseData = getTestCaseDataForHearingMidEvent(CaseDataBuilder.builder().build(), true,
                    LocalDate.now(), LocalDate.now().plusDays(1), true, getValidUnavailableDateList());
            CallbackParams params = callbackParamsOf(caseData, MID, VALIDATE_HEARING_PAGE);
            when(initiateGeneralAppService.validateHearingScreen(any())).thenCallRealMethod();

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).isEmpty();
        }

        @Test
        void shouldNotReturnErrors_whenTrialIsScheduledAndTrialDateFromIsProvidedAndTrialDateToAreSame() {
            CaseData caseData = getTestCaseDataForHearingMidEvent(CaseDataBuilder.builder().build(), true,
                    LocalDate.now(), LocalDate.now(), true, getValidUnavailableDateList());
            CallbackParams params = callbackParamsOf(caseData, MID, VALIDATE_HEARING_PAGE);
            when(initiateGeneralAppService.validateHearingScreen(any())).thenCallRealMethod();

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).isEmpty();
        }

        @Test
        void shouldNotReturnErrors_whenTrialIsNotScheduled() {
            CaseData caseData = getTestCaseDataForHearingMidEvent(CaseDataBuilder.builder().build(), false,
                    null, null, true, getValidUnavailableDateList());
            CallbackParams params = callbackParamsOf(caseData, MID, VALIDATE_HEARING_PAGE);
            when(initiateGeneralAppService.validateHearingScreen(any())).thenCallRealMethod();

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).isEmpty();
        }

        //Unavailability Dates validations
        @Test
        void shouldReturnErrors_whenUnavailabilityIsSetButNullDateRangeProvided() {
            CaseData caseData = getTestCaseDataForHearingMidEvent(CaseDataBuilder.builder().build(), true,
                    LocalDate.now(), null, true, null);
            CallbackParams params = callbackParamsOf(caseData, MID, VALIDATE_HEARING_PAGE);
            when(initiateGeneralAppService.validateHearingScreen(any())).thenCallRealMethod();

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).isNotEmpty();
            assertThat(response.getErrors()).contains(UNAVAILABLE_DATE_RANGE_MISSING);
        }

        @Test
        void shouldReturnErrors_whenUnavailabilityIsSetButDateRangeProvidedHasNullDateFrom() {
            GAUnavailabilityDates range1 = GAUnavailabilityDates.builder()
                    .unavailableTrialDateFrom(null)
                    .unavailableTrialDateTo(null)
                    .build();

            CaseData caseData = getTestCaseDataForHearingMidEvent(CaseDataBuilder.builder().build(), true,
                    LocalDate.now(), null, true, wrapElements(range1));
            CallbackParams params = callbackParamsOf(caseData, MID, VALIDATE_HEARING_PAGE);
            when(initiateGeneralAppService.validateHearingScreen(any())).thenCallRealMethod();

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).isNotEmpty();
            assertThat(response.getErrors()).contains(UNAVAILABLE_FROM_MUST_BE_PROVIDED);
        }

        @Test
        void shouldReturnErrors_whenUnavailabilityIsSetButDateRangeProvidedHasDateFromAfterDateTo() {
            GAUnavailabilityDates range1 = GAUnavailabilityDates.builder()
                    .unavailableTrialDateFrom(LocalDate.now().plusDays(1))
                    .unavailableTrialDateTo(LocalDate.now())
                    .build();

            CaseData caseData = getTestCaseDataForHearingMidEvent(CaseDataBuilder.builder().build(), true,
                    LocalDate.now(), null, true, wrapElements(range1));
            CallbackParams params = callbackParamsOf(caseData, MID, VALIDATE_HEARING_PAGE);
            when(initiateGeneralAppService.validateHearingScreen(any())).thenCallRealMethod();

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).isNotEmpty();
            assertThat(response.getErrors()).contains(INVALID_UNAVAILABILITY_RANGE);
        }

        @Test
        void shouldNotReturnErrors_whenUnavailabilityIsNotSet() {
            CaseData caseData = getTestCaseDataForHearingMidEvent(CaseDataBuilder.builder().build(), false,
                    null, null, false, null);
            CallbackParams params = callbackParamsOf(caseData, MID, VALIDATE_HEARING_PAGE);
            when(initiateGeneralAppService.validateHearingScreen(any())).thenCallRealMethod();

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).isEmpty();
        }

        @Test
        void shouldNotReturnErrors_whenUnavailabilityIsSetAndDateFromIsValidWithNullDateTo() {
            GAUnavailabilityDates range1 = GAUnavailabilityDates.builder()
                    .unavailableTrialDateFrom(LocalDate.now())
                    .unavailableTrialDateTo(null)
                    .build();

            CaseData caseData = getTestCaseDataForHearingMidEvent(CaseDataBuilder.builder().build(), false,
                    null, null, false, wrapElements(range1));
            CallbackParams params = callbackParamsOf(caseData, MID, VALIDATE_HEARING_PAGE);
            when(initiateGeneralAppService.validateHearingScreen(any())).thenCallRealMethod();

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).isEmpty();
        }

        @Test
        void shouldNotReturnErrors_whenUnavailabilityIsSetAndDateFromIsValidWithSameDateTo() {
            GAUnavailabilityDates range1 = GAUnavailabilityDates.builder()
                    .unavailableTrialDateFrom(LocalDate.now())
                    .unavailableTrialDateTo(LocalDate.now())
                    .build();

            CaseData caseData = getTestCaseDataForHearingMidEvent(CaseDataBuilder.builder().build(), false,
                    null, null, false, wrapElements(range1));
            CallbackParams params = callbackParamsOf(caseData, MID, VALIDATE_HEARING_PAGE);
            when(initiateGeneralAppService.validateHearingScreen(any())).thenCallRealMethod();

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).isEmpty();
        }

        @Test
        void shouldNotReturnErrors_whenUnavailabilityIsSetAndDateFromIsBeforeDateTo() {
            GAUnavailabilityDates range1 = GAUnavailabilityDates.builder()
                    .unavailableTrialDateFrom(LocalDate.now())
                    .unavailableTrialDateTo(LocalDate.now().plusDays(1))
                    .build();

            CaseData caseData = getTestCaseDataForHearingMidEvent(CaseDataBuilder.builder().build(), false,
                    null, null, false, wrapElements(range1));
            CallbackParams params = callbackParamsOf(caseData, MID, VALIDATE_HEARING_PAGE);
            when(initiateGeneralAppService.validateHearingScreen(any())).thenCallRealMethod();

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).isEmpty();
        }
    }

    @Nested
    class MidEventForSettingFeeAndPBA extends LocationRefSampleDataBuilder {

        private final Organisation organisation = Organisation.builder()
                .paymentAccount(List.of("12345", "98765"))
                .build();

        @Test
        void shouldSetAddPbaNumbers_whenCalledAndOrgExistsInPrd() {
            given(organisationService.findOrganisation(any())).willReturn(Optional.of(organisation));
            given(feesService.getFeeForGA(any()))
                    .willReturn(Fee.builder().code(FEE_CODE).calculatedAmountInPence(fee108)
                            .version(FEE_VERSION).build());
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDraft().build();
            CallbackParams params = callbackParamsOf(caseData, MID, SET_FEES_AND_PBA);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).isNull();
        }

        @Test
        void shouldNotResultInErrors_whenCalledAndOrgDoesNotExistInPrd() {
            given(organisationService.findOrganisation(any())).willReturn(Optional.empty());
            given(feesService.getFeeForGA(any()))
                    .willReturn(Fee.builder()
                            .code(FEE_CODE)
                            .calculatedAmountInPence(fee108)
                            .version(FEE_VERSION).build());

            CaseData caseData = CaseDataBuilder.builder().atStateClaimDraft().build();
            CallbackParams params = callbackParamsOf(caseData, MID, SET_FEES_AND_PBA);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).isNull();
        }

        @Test
        void shouldReturnNoError_whenNoOrgDetailsObtained() {
            given(feesService.getFeeForGA(any()))
                    .willReturn(Fee.builder()
                            .code(FEE_CODE)
                            .calculatedAmountInPence(fee108)
                            .version(FEE_VERSION).build());
            CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued().build();
            CallbackParams params = callbackParamsOf(caseData, MID, SET_FEES_AND_PBA);

            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                    .handle(params);

            assertThat(response.getErrors()).isNull();
        }

        @Test
        void shouldSet108Fees_whenApplicationIsConsented() {
            given(feesService.getFeeForGA(any()))
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
            given(feesService.getFeeForGA(any()))
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
            given(feesService.getFeeForGA(any()))
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
            given(feesService.getFeeForGA(any()))
                    .willReturn(Fee.builder()
                            .code(FEE_CODE)
                            .calculatedAmountInPence(fee275)
                            .version(FEE_VERSION).build());
            List<GeneralApplicationTypes> types = List.of(VARY_JUDGEMENT);
            CaseData caseData = CaseDataBuilder
                    .builder().generalAppType(GAApplicationType.builder().types(types).build())
                    .build()
                    .toBuilder()
                    .generalAppRespondentAgreement(GARespondentOrderAgreement.builder().hasAgreed(NO).build())
                    .build();
            CallbackParams params = callbackParamsOf(caseData, MID, SET_FEES_AND_PBA);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).isNull();
            assertThat(getPBADetails(response).getFee()).isNotNull();
            assertThat(getPBADetails(response).getFee().getCalculatedAmountInPence()).isEqualTo("27500");
        }

        @Test
        void shouldSet14Fees_whenApplicationIsVaryOrder() {
            given(feesService.getFeeForGA(any()))
                .willReturn(Fee.builder()
                                .code(FEE_CODE)
                                .calculatedAmountInPence(fee14)
                                .build());
            CaseData caseData = GeneralApplicationDetailsBuilder.builder().getTestCaseDataForApplicationFee(
                CaseDataBuilder.builder().build(), false, false);
            CaseData.CaseDataBuilder caseDataBuilder = caseData.toBuilder();
            caseDataBuilder.generalAppType(GAApplicationType.builder()
                                               .types(singletonList(VARY_ORDER))
                                               .build());
            CallbackParams params = callbackParamsOf(caseDataBuilder.build(), MID, SET_FEES_AND_PBA);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).isNull();
            assertThat(getPBADetails(response).getFee()).isNotNull();
            assertThat(getPBADetails(response).getFee().getCalculatedAmountInPence()).isEqualTo("1400");
        }

        @Test
        void shouldSet14Fees_whenApplicationIsVaryOrderWithMultipleTypes() {
            given(feesService.getFeeForGA(any()))
                .willReturn(Fee.builder()
                                .code(FEE_CODE)
                                .calculatedAmountInPence(fee14).build());
            CaseData caseData = GeneralApplicationDetailsBuilder.builder().getTestCaseDataForApplicationFee(
                CaseDataBuilder.builder().build(), false, false);
            CaseData.CaseDataBuilder caseDataBuilder = caseData.toBuilder();
            List<GeneralApplicationTypes> types = List.of(VARY_ORDER, STAY_THE_CLAIM);
            caseDataBuilder.generalAppType(GAApplicationType.builder()
                                               .types(types)
                                               .build());
            CallbackParams params = callbackParamsOf(caseDataBuilder.build(), MID, SET_FEES_AND_PBA);

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

            when(idamClient.getUserDetails(anyString())).thenReturn(UserDetails.builder().id(STRING_CONSTANT)
                                                                      .email(APPLICANT_EMAIL_ID_CONSTANT)
                                                                      .build());
            when(initiateGeneralAppService.buildCaseData(any(CaseData.CaseDataBuilder.class),
                                                         any(CaseData.class), any(UserDetails.class), anyString()))
                .thenReturn(caseData);

            when(helper.setRespondentDetailsIfPresent(any(CaseData.CaseDataBuilder.class), any(GeneralApplication.class),
                                                                any(CaseData.class), any(UserDetails.class)))
                .thenReturn(GeneralApplicationDetailsBuilder.builder().getGeneralApplication());

            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).isNull();
            assertResponse(objectMapper.convertValue(response.getData(), CaseData.class));
        }

        @Test
        void shouldSetAppropriateFees_whenFeesAreUnsetByCCD() {
            CaseData caseData = GeneralApplicationDetailsBuilder.builder()
                    .getTestCaseData(CaseData.builder().build());
            when(feesService.getFeeForGA(any())).thenReturn(feeFromFeeService);
            when(idamClient.getUserDetails(anyString())).thenReturn(UserDetails.builder().id(STRING_CONSTANT)
                    .email(APPLICANT_EMAIL_ID_CONSTANT)
                    .build());
            when(initiateGeneralAppService.buildCaseData(any(CaseData.CaseDataBuilder.class),
                    any(CaseData.class), any(UserDetails.class), anyString()))
                    .thenReturn(getMockServiceData(caseData));

            when(helper.setRespondentDetailsIfPresent(any(CaseData.CaseDataBuilder.class), any(GeneralApplication.class),
                    any(CaseData.class), any(UserDetails.class)))
                    .thenReturn(GeneralApplicationDetailsBuilder.builder().getGeneralApplication());

            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).isNull();
            assertThat(caseData.getGeneralAppPBADetails()).isNotNull();
            assertThat(caseData.getGeneralAppPBADetails().getFee()).isNull();
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
            GAPbaDetails pbaDetails = caseData.getGeneralAppPBADetails().toBuilder().fee(feeFromFeeService).build();
            return caseData.toBuilder().generalAppPBADetails(pbaDetails).build();
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
                .getTestCaseDataWithEmptyPreferredLocation(CaseData.builder().build());
            when(feesService.getFeeForGA(any())).thenReturn(feeFromFeeService);
            when(idamClient.getUserDetails(anyString())).thenReturn(UserDetails.builder().id(STRING_CONSTANT)
                                                                        .email(APPLICANT_EMAIL_ID_CONSTANT)
                                                                        .build());
            when(initiateGeneralAppService.buildCaseData(any(CaseData.CaseDataBuilder.class),
                                                         any(CaseData.class), any(UserDetails.class), anyString()))
                .thenReturn(getMockServiceData(caseData));

            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            CaseData data = objectMapper.convertValue(response.getData(), CaseData.class);
            DynamicList dynamicList = getLocationDynamicList(data);
            assertThat(data.getGeneralAppHearingDetails()).isNotNull();
            assertThat(dynamicList).isNull();
        }

        @Test
        void shouldWithNotice_whenVaryApplicationIsUnConsented() {
            GAPbaDetails generalAppPBADetails = GAPbaDetails.builder().fee(feeFromFeeService).build();

            List<GeneralApplicationTypes> types = List.of(VARY_JUDGEMENT);
            CaseData caseData = CaseDataBuilder
                    .builder().generalAppType(GAApplicationType.builder().types(types).build())
                    .build()
                    .toBuilder()
                    .generalAppPBADetails(generalAppPBADetails)
                    .generalAppHearingDetails(GAHearingDetails.builder().build())
                    .generalAppRespondentAgreement(GARespondentOrderAgreement.builder().hasAgreed(NO).build())
                    .build();
            when(idamClient.getUserDetails(anyString())).thenReturn(UserDetails.builder().id(STRING_CONSTANT)
                    .email(APPLICANT_EMAIL_ID_CONSTANT)
                    .build());
            when(initiateGeneralAppService.buildCaseData(any(CaseData.CaseDataBuilder.class),
                    any(CaseData.class), any(UserDetails.class), anyString())).thenAnswer(new Answer() {
                        public Object answer(InvocationOnMock invocation) {
                            return invocation.getArguments()[1];
                        }
                    }
            );

            when(helper.setRespondentDetailsIfPresent(any(CaseData.CaseDataBuilder.class), any(GeneralApplication.class),
                    any(CaseData.class), any(UserDetails.class)))
                    .thenReturn(GeneralApplicationDetailsBuilder.builder().getGeneralApplication());
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
        void shouldReturnEmptyResponse_whenInvoked() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued()
                .build();
            CallbackParams params = callbackParamsOf(caseData, SUBMITTED);

            SubmittedCallbackResponse response = (SubmittedCallbackResponse) handler.handle(params);

            assertThat(response).isEqualTo(SubmittedCallbackResponse.builder().build());
        }
    }

    @Nested
    class AboutToStartCallbackHandling extends LocationRefSampleDataBuilder {
        private static final String ERROR = "Application cannot be created until all the required respondent "
                + "solicitor are assigned to the case.";

        @Test
        void shouldNotReturnErrors_whenRespondentSolAssignedToCase() {

            List<LocationRefData> locations = new ArrayList<>();
            locations.add(LocationRefData.builder().siteName("siteName").courtAddress("court Address").postcode("post code")
                              .courtName("Court Name").region("Region").build());
            given(locationRefDataService.getCourtLocationsForGeneralApplication(any())).willReturn(locations);

            CaseData caseData = CaseDataBuilder.builder().atStateClaimDraft().build();
            given(initiateGeneralAppService.respondentAssigned(any(), any())).willReturn(true);

            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_START);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            CaseData data = objectMapper.convertValue(response.getData(), CaseData.class);

            DynamicList dynamicList = getLocationDynamicList(data);

            assertThat(response.getErrors()).isEmpty();
            assertThat(data.getGeneralAppHearingDetails()).isNotNull();
            assertThat(dynamicList).isNotNull();
            assertThat(locationsFromDynamicList(dynamicList))
                    .containsOnly("siteName - court Address - post code");
        }

        @Test
        void shouldReturnErrors_whenNoRespondentSolAssignedToCase() {

            List<LocationRefData> locations = new ArrayList<>();
            locations.add(LocationRefData.builder().siteName("siteName").courtAddress("court Address").postcode("post code")
                              .courtName("Court Name").region("Region").build());
            given(locationRefDataService.getCourtLocationsForGeneralApplication(any())).willReturn(locations);
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDraft().build();
            given(initiateGeneralAppService.respondentAssigned(any(), any())).willReturn(false);

            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_START);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).isNotEmpty();
            assertThat(response.getErrors()).contains(ERROR);
        }
    }
}
