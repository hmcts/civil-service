package uk.gov.hmcts.reform.civil.handler.callback.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.autoconfigure.validation.ValidationAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.ccd.model.OrganisationPolicy;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.config.ClaimUrlsConfiguration;
import uk.gov.hmcts.reform.civil.config.ExitSurveyConfiguration;
import uk.gov.hmcts.reform.civil.config.MockDatabaseConfiguration;
import uk.gov.hmcts.reform.civil.config.ToggleConfiguration;
import uk.gov.hmcts.reform.civil.enums.CaseCategory;
import uk.gov.hmcts.reform.civil.enums.CaseRole;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.handler.callback.user.task.createclaim.CalculateFeeTask;
import uk.gov.hmcts.reform.civil.handler.callback.user.task.createclaim.CalculateSpecFeeTask;
import uk.gov.hmcts.reform.civil.handler.callback.user.task.createclaim.CalculateTotalClaimAmountTask;
import uk.gov.hmcts.reform.civil.handler.callback.user.task.createclaim.GetAirlineListTask;
import uk.gov.hmcts.reform.civil.handler.callback.user.task.createclaim.SpecValidateClaimInterestDateTask;
import uk.gov.hmcts.reform.civil.handler.callback.user.task.createclaim.SpecValidateClaimTimelineDateTask;
import uk.gov.hmcts.reform.civil.handler.callback.user.task.createclaim.SubmitClaimTask;
import uk.gov.hmcts.reform.civil.handler.callback.user.task.createclaim.ValidateClaimantDetailsTask;
import uk.gov.hmcts.reform.civil.handler.callback.user.task.createclaim.ValidateRespondentDetailsTask;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.Address;
import uk.gov.hmcts.reform.civil.model.AirlineEpimsId;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.ClaimAmountBreakup;
import uk.gov.hmcts.reform.civil.model.ClaimAmountBreakupDetails;
import uk.gov.hmcts.reform.civil.model.CorrectEmail;
import uk.gov.hmcts.reform.civil.model.DefendantPinToPostLRspec;
import uk.gov.hmcts.reform.civil.model.Fee;
import uk.gov.hmcts.reform.civil.model.FlightDelayDetails;
import uk.gov.hmcts.reform.civil.model.IdamUserDetails;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.Party.Type;
import uk.gov.hmcts.reform.civil.model.ServedDocumentFiles;
import uk.gov.hmcts.reform.civil.model.SolicitorOrganisationDetails;
import uk.gov.hmcts.reform.civil.model.StatementOfTruth;
import uk.gov.hmcts.reform.civil.model.TimelineOfEventDetails;
import uk.gov.hmcts.reform.civil.model.TimelineOfEvents;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.common.DynamicListElement;
import uk.gov.hmcts.reform.civil.model.interestcalc.InterestClaimFromType;
import uk.gov.hmcts.reform.civil.model.interestcalc.InterestClaimOptions;
import uk.gov.hmcts.reform.civil.model.interestcalc.InterestClaimUntilType;
import uk.gov.hmcts.reform.civil.model.interestcalc.SameRateInterestSelection;
import uk.gov.hmcts.reform.civil.model.interestcalc.SameRateInterestType;
import uk.gov.hmcts.reform.civil.prd.model.Organisation;
import uk.gov.hmcts.reform.civil.referencedata.model.LocationRefData;
import uk.gov.hmcts.reform.civil.sampledata.AddressBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CallbackParamsBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.sampledata.LocationRefSampleDataBuilder;
import uk.gov.hmcts.reform.civil.sampledata.PartyBuilder;
import uk.gov.hmcts.reform.civil.service.AirlineEpimsDataLoader;
import uk.gov.hmcts.reform.civil.service.AirlineEpimsService;
import uk.gov.hmcts.reform.civil.service.ExitSurveyContentService;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.FeesService;
import uk.gov.hmcts.reform.civil.service.OrganisationService;
import uk.gov.hmcts.reform.civil.service.Time;
import uk.gov.hmcts.reform.civil.service.flowstate.SimpleStateFlowEngine;
import uk.gov.hmcts.reform.civil.service.flowstate.TransitionsTestConfiguration;
import uk.gov.hmcts.reform.civil.service.pininpost.DefendantPinToPostLRspecService;
import uk.gov.hmcts.reform.civil.service.referencedata.LocationReferenceDataService;
import uk.gov.hmcts.reform.civil.stateflow.simplegrammar.SimpleStateFlowBuilder;
import uk.gov.hmcts.reform.civil.utils.AccessCodeGenerator;
import uk.gov.hmcts.reform.civil.utils.CaseFlagsInitialiser;
import uk.gov.hmcts.reform.civil.utils.InterestCalculator;
import uk.gov.hmcts.reform.civil.validation.DateOfBirthValidator;
import uk.gov.hmcts.reform.civil.validation.OrgPolicyValidator;
import uk.gov.hmcts.reform.civil.validation.PartyValidator;
import uk.gov.hmcts.reform.civil.validation.PostcodeValidator;
import uk.gov.hmcts.reform.civil.validation.ValidateEmailService;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static java.lang.String.format;
import static java.time.LocalDate.now;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.MID;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.SUBMITTED;
import static uk.gov.hmcts.reform.civil.callback.CallbackVersion.V_1;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CREATE_CLAIM_SPEC;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CREATE_SERVICE_REQUEST_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.handler.callback.user.CreateClaimSpecCallbackHandler.CONFIRMATION_SUMMARY_PBA_V3;
import static uk.gov.hmcts.reform.civil.handler.callback.user.CreateClaimSpecCallbackHandler.INTEREST_FROM_PAGE_ID;
import static uk.gov.hmcts.reform.civil.handler.callback.user.CreateClaimSpecCallbackHandler.SPEC_CONFIRMATION_SUMMARY_PBA_V3;
import static uk.gov.hmcts.reform.civil.handler.callback.user.CreateClaimSpecCallbackHandler.SPEC_LIP_CONFIRMATION_BODY_PBAV3;
import static uk.gov.hmcts.reform.civil.utils.PartyUtils.getPartyNameBasedOnType;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {
    CreateClaimSpecCallbackHandler.class,
    JacksonAutoConfiguration.class,
    CaseDetailsConverter.class,
    ClaimUrlsConfiguration.class,
    ExitSurveyConfiguration.class,
    ExitSurveyContentService.class,
    MockDatabaseConfiguration.class,
    ValidationAutoConfiguration.class,
    DateOfBirthValidator.class,
    OrgPolicyValidator.class,
    PostcodeValidator.class,
    InterestCalculator.class,
    SimpleStateFlowEngine.class,
    SimpleStateFlowBuilder.class,
    TransitionsTestConfiguration.class,
    ValidateEmailService.class,
    PartyValidator.class,
    CalculateFeeTask.class,
    CalculateSpecFeeTask.class,
    CalculateTotalClaimAmountTask.class,
    GetAirlineListTask.class,
    SpecValidateClaimInterestDateTask.class,
    SpecValidateClaimTimelineDateTask.class,
    ValidateRespondentDetailsTask.class,
    ValidateClaimantDetailsTask.class,
    SubmitClaimTask.class
},
    properties = {"reference.database.enabled=false"})
class  CreateClaimSpecCallbackHandlerTest extends BaseCallbackHandlerTest {

    public static final String REFERENCE_NUMBER = "000MC001";

    public static final String LIP_CONFIRMATION_SCREEN = "<br />Your claim will not be issued"
        + " until payment is confirmed."
        + " Once payment is confirmed you will receive an email. The claim will then progress offline."
        + "%n%nTo continue the claim you need to send the <a href=\"%s\" target=\"_blank\">sealed claim form</a>, "
        + "a <a href=\"%s\" target=\"_blank\">response pack</a> and any supporting documents to "
        + "the defendant within 4 months. "
        + "%n%nOnce you have served the claim, send the Certificate of Service and supporting documents to the County"
        + " Court Claims Centre.";

    public static final String SPEC_LIP_CONFIRMATION_SCREEN = "<br />When the payment is confirmed your claim will be issued "
        + "and youll be notified by email. The claim will then progress offline."
        + "%n%nOnce the claim has been issued, you will need to serve the claim upon the "
        + "defendant which must include a response pack"
        + "%n%nYou will need to send the following:<ul style=\"margin-bottom : 0px;\"> <li> <a href=\"%s\" target=\"_blank\">sealed claim form</a> "
        +
        "</li><li><a href=\"%s\" target=\"_blank\">response pack</a></li><ul style=\"list-style-type:circle\"><li><a href=\"%s\" target=\"_blank\">N9A</a></li>"
        + "<li><a href=\"%s\" target=\"_blank\">N9B</a></li></ul><li>and any supporting documents</li></ul>"
        + "to the defendant within 4 months."
        + "%n%nFollowing this, you will need to file a Certificate of Service and supporting documents "
        + "to : <a href=\"mailto:contactocmc@justice.gov.uk\">contactocmc@justice.gov.uk</a>. The Certificate of Service form can be found here:"
        + "%n%n<ul><li><a href=\"%s\" target=\"_blank\">N215</a></li></ul>";

    private final Organisation bulkOrganisation = new Organisation()
        .setPaymentAccount(List.of("12345", "98765"));

    @MockBean
    private Time time;
    @MockBean
    private DefendantPinToPostLRspecService defendantPinToPostLRspecService;
    @MockBean
    private FeesService feesService;
    @MockBean
    private OrganisationService organisationService;

    @Autowired
    private ValidateEmailService validateEmailService;

    @Autowired
    private CreateClaimSpecCallbackHandler handler;

    @Autowired
    private ExitSurveyContentService exitSurveyContentService;

    @Autowired
    private ObjectMapper objMapper;

    @MockBean
    private PostcodeValidator postcodeValidator;

    @Autowired
    private PartyValidator partyValidator;

    @MockBean
    private InterestCalculator interestCalculator;

    @Value("${civil.response-pack-url}")
    private String responsePackLink;

    @Value("${civil.n9a-url}")
    private String n9aLink;

    @Value("${civil.n9b-url}")
    private String n9bLink;

    @Value("${civil.n215-url}")
    private String n215Link;

    @MockBean
    private CaseFlagsInitialiser caseFlagInitialiser;

    @MockBean
    private FeatureToggleService toggleService;

    @MockBean
    private ToggleConfiguration toggleConfiguration;

    @MockBean
    protected LocationReferenceDataService locationRefDataService;

    @MockBean
    private AirlineEpimsDataLoader airlineEpimsDataLoader;

    @MockBean
    private AirlineEpimsService airlineEpimsService;

    @Nested
    class AboutToStartCallback {

        @Test
        void shouldReturnNoError_WhenAboutToStartIsInvoked() {
            // Given
            CaseData caseData = CaseDataBuilder.builder()
                .atStatePendingClaimIssued()
                .build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_START);

            // When
            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            // Then
            assertThat(response.getErrors()).isNull();
        }
    }

    @Nested
    class MidEventEligibilityCallback {

        private static final String PAGE_ID = "eligibilityCheck";

        @Test
        void shouldNotReturnError_whenOrganisationIsNotRegistered() {
            // Given
            CaseData caseData = CaseDataBuilder.builder().build();
            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);

            // When
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            // Then
            assertThat(response.getErrors()).isEmpty();
        }

        @Test
        void shouldNotReturnError_whenOrganisationIsRegistered() {
            // Given
            CaseData caseData = CaseDataBuilder.builder().build();
            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);

            // When
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            // Then
            assertThat(response.getErrors()).isEmpty();
        }

    }

    @Nested
    class MidEventApplicantCallback {

        private static final String PAGE_ID = "applicant";

        @Test
        void shouldReturnError_whenIndividualDateOfBirthIsInTheFuture() {
            // Given
            when(toggleService.isJudgmentOnlineLive()).thenReturn(true);

            CaseData caseData = CaseDataBuilder.builder().atStateClaimDraft()
                .applicant1(PartyBuilder.builder().individual()
                                .individualDateOfBirth(now().plusDays(1))
                                .build())
                .build();
            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);

            // When
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            // Then
            assertThat(response.getErrors()).containsExactly("The date entered cannot be in the future");
        }

        @Test
        void shouldReturnError_whenSoleTraderDateOfBirthIsInTheFuture() {
            // Given
            when(toggleService.isJudgmentOnlineLive()).thenReturn(true);

            CaseData caseData = CaseDataBuilder.builder().atStateClaimDraft()
                .applicant1(PartyBuilder.builder().individual()
                                .soleTraderDateOfBirth(now().plusDays(1))
                                .build())
                .build();
            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);

            // When
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            // Then
            assertThat(response.getErrors()).containsExactly("The date entered cannot be in the future");
        }

        @Test
        void shouldReturnNoError_whenIndividualDateOfBirthIsInThePast() {
            // Given
            when(toggleService.isJudgmentOnlineLive()).thenReturn(true);

            CaseData caseData = CaseDataBuilder.builder().atStateClaimDraft()
                .applicant1(PartyBuilder.builder().individual()
                                .individualDateOfBirth(now().minusDays(1))
                                .build())
                .build();
            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);

            // When
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            // Then
            assertThat(response.getErrors()).isEmpty();
        }

        @Test
        void shouldReturnNoError_whenSoleTraderDateOfBirthIsInThePast() {
            // Given
            when(toggleService.isJudgmentOnlineLive()).thenReturn(true);

            CaseData caseData = CaseDataBuilder.builder().atStateClaimDraft()
                .applicant1(PartyBuilder.builder().individual()
                                .soleTraderDateOfBirth(now().minusDays(1))
                                .build())
                .build();
            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);

            // When
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            // Then
            assertThat(response.getErrors()).isEmpty();
        }

        @Test
        void shouldReturnError_when_address_exceeds_max_length() {
            // Given
            when(toggleService.isJudgmentOnlineLive()).thenReturn(true);

            Address address = new Address();
            address.setAddressLine1("Line 1 test again for more than 35 characters");
            address.setAddressLine2("Line 1 test again for more than 35 characters");
            address.setAddressLine3("Line 1 test again for more than 35 characters");
            address.setCounty("Line 1 test again for more than 35 characters");
            address.setPostCode("Line 1 test again for more than 35 characters");
            address.setPostTown("Line 1 test again for more than 35 characters");
            Party party = new Party();
            party.setType(Party.Type.ORGANISATION);
            party.setPrimaryAddress(address);
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDraft()
                .applicant1(party)
                .build();
            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);

            // When
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            // Then
            assertThat(response.getErrors()).isNotEmpty();
            assertThat(response.getErrors()).hasSize(6);

        }

        @Test
        void shouldReturnError_when_address_exceeds_max_length_in_Company_name() {
            // Given
            when(toggleService.isJudgmentOnlineLive()).thenReturn(true);

            Address address = new Address();
            address.setAddressLine1("TEST");
            Party party = new Party();
            party.setType(Party.Type.COMPANY);
            party.setPrimaryAddress(address);
            party.setCompanyName("MR This is very long nam exceeds 70 characters to throw"
                                 + " error for max length allowed");
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDraft()
                .applicant1(party)
                .build();
            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);

            // When
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            // Then
            assertThat(response.getErrors()).isNotEmpty();
        }

        @Test
        void shouldReturnError_when_address_exceeds_max_length_in_Individual_name() {
            // Given
            when(toggleService.isJudgmentOnlineLive()).thenReturn(true);

            Address address = new Address();
            address.setAddressLine1("Address line 1");
            Party party = new Party();
            party.setType(Party.Type.INDIVIDUAL);
            party.setIndividualFirstName("This is very long name");
            party.setIndividualTitle("MR");
            party.setIndividualLastName("exceeds 70 characters to throw error for max length allowed");
            party.setPrimaryAddress(address);
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDraft()
                .applicant1(party)
                .build();
            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);

            // When
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            // Then
            assertThat(response.getErrors()).isNotEmpty();
        }

        @Test
        void shouldReturnError_when_address_exceeds_max_length_in_sole_trader_name() {
            // Given
            when(toggleService.isJudgmentOnlineLive()).thenReturn(true);

            Address address = new Address();
            address.setAddressLine1("Address line 1");
            Party party = new Party();
            party.setType(Party.Type.SOLE_TRADER);
            party.setSoleTraderFirstName("This is very long name");
            party.setSoleTraderTitle("MR");
            party.setSoleTraderLastName("exceeds 70 characters to throw error for max length allowed");
            party.setPrimaryAddress(address);
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDraft()
                .applicant1(party)
                .build();
            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);

            // When
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            // Then
            assertThat(response.getErrors()).isNotEmpty();
        }

        @Test
        void shouldReturnError_when_address_exceeds_max_length_in_org_name() {
            // Given
            when(toggleService.isJudgmentOnlineLive()).thenReturn(true);

            Address address = new Address();
            address.setAddressLine1("Address line 1");
            Party party = new Party();
            party.setType(Party.Type.ORGANISATION);
            party.setOrganisationName("This is very long name exceeds 70 characters "
                                                      + " to throw error for max length allowed");
            party.setPrimaryAddress(address);
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDraft()
                .applicant1(party)
                .build();
            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);

            // When
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            // Then
            assertThat(response.getErrors()).isNotEmpty();
        }

        @Test
        void shouldNotError_when_address_exceeds_max_length_in_org_name_when_flag_in_off() {
            // Given
            when(toggleService.isJudgmentOnlineLive()).thenReturn(false);

            Party party = new Party();
            party.setType(Party.Type.ORGANISATION);
            party.setOrganisationName("This is very long name exceeds 70 characters "
                                          + " to throw error for max length allowed");
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDraft()
                .applicant1(party)
                .build();
            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);

            // When
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            // Then
            assertThat(response.getErrors()).isEmpty();
        }
    }

    @Nested
    class MidEventApplicant2Callback {

        private static final String PAGE_ID = "applicant2";

        @Test
        void shouldReturnError_whenIndividualDateOfBirthIsInTheFuture() {
            // Given
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDraft()
                .applicant2(PartyBuilder.builder().individual()
                                .individualDateOfBirth(now().plusDays(1))
                                .build())
                .build();
            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);

            // When
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            // Then
            assertThat(response.getErrors()).containsExactly("The date entered cannot be in the future");
        }

        @Test
        void shouldReturnError_whenSoleTraderDateOfBirthIsInTheFuture() {
            // Given
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDraft()
                .applicant2(PartyBuilder.builder().individual()
                                .soleTraderDateOfBirth(now().plusDays(1))
                                .build())
                .build();
            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);

            // When
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            // Then
            assertThat(response.getErrors()).containsExactly("The date entered cannot be in the future");
        }

        @Test
        void shouldReturnNoError_whenIndividualDateOfBirthIsInThePast() {
            // Given
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDraft()
                .applicant2(PartyBuilder.builder().individual()
                                .individualDateOfBirth(now().minusDays(1))
                                .build())
                .build();
            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);

            // When
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            // Then
            assertThat(response.getErrors()).isEmpty();
        }

        @Test
        void shouldReturnNoError_whenSoleTraderDateOfBirthIsInThePast() {
            // Given
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDraft()
                .applicant2(PartyBuilder.builder().individual()
                                .soleTraderDateOfBirth(now().minusDays(1))
                                .build())
                .build();
            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);

            // When
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            // Then
            assertThat(response.getErrors()).isEmpty();
        }

        @Test
        void shouldReturnError_when_address_exceeds_max_length() {
            // Given
            when(toggleService.isJudgmentOnlineLive()).thenReturn(true);

            Address address = new Address();
            address.setAddressLine1("Line 1 test again for more than 35 characters");
            address.setAddressLine2("Line 1 test again for more than 35 characters");
            address.setAddressLine3("Line 1 test again for more than 35 characters");
            address.setCounty("Line 1 test again for more than 35 characters");
            address.setPostCode("Line 1 test again for more than 35 characters");
            address.setPostTown("Line 1 test again for more than 35 characters");
            Party party = new Party();
            party.setType(Party.Type.INDIVIDUAL);
            party.setPrimaryAddress(address);
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDraft()
                .applicant2(party)
                .build();
            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);

            // When
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            // Then
            assertThat(response.getErrors()).isNotEmpty();
            assertThat(response.getErrors()).hasSize(6);

        }

        @Test
        void shouldReturnError_when_address_exceeds_max_length_in_Company_name() {
            // Given
            when(toggleService.isJudgmentOnlineLive()).thenReturn(true);

            Address address = new Address();
            address.setAddressLine1("TEST");
            Party party = new Party();
            party.setType(Party.Type.COMPANY);
            party.setPrimaryAddress(address);
            party.setCompanyName("MR This is very long nam exceeds 70 characters to throw"
                                 + " error for max length allowed");
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDraft()
                .applicant2(party)
                .build();
            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);

            // When
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            // Then
            assertThat(response.getErrors()).isNotEmpty();
        }

        @Test
        void shouldReturnError_when_address_exceeds_max_length_in_Individual_name() {
            // Given
            when(toggleService.isJudgmentOnlineLive()).thenReturn(true);

            Address address = new Address();
            address.setAddressLine1("Address line 1");
            Party party = new Party();
            party.setType(Party.Type.INDIVIDUAL);
            party.setIndividualFirstName("This is very long name");
            party.setIndividualTitle("MR");
            party.setIndividualLastName("exceeds 70 characters to throw error for max length allowed");
            party.setPrimaryAddress(address);
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDraft()
                .applicant2(party)
                .build();
            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);

            // When
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            // Then
            assertThat(response.getErrors()).isNotEmpty();
        }

        @Test
        void shouldReturnError_when_address_exceeds_max_length_in_sole_trader_name() {
            // Given
            when(toggleService.isJudgmentOnlineLive()).thenReturn(true);

            Address address = new Address();
            address.setAddressLine1("Address line 1");
            Party party = new Party();
            party.setType(Type.SOLE_TRADER);
            party.setSoleTraderFirstName("This is very long name");
            party.setSoleTraderTitle("MR");
            party.setSoleTraderLastName("exceeds 70 characters to throw error for max length allowed");
            party.setPrimaryAddress(address);
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDraft()
                .applicant2(party)
                .build();
            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);

            // When
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            // Then
            assertThat(response.getErrors()).isNotEmpty();
        }

        @Test
        void shouldReturnError_when_address_exceeds_max_length_in_org_name() {
            // Given
            when(toggleService.isJudgmentOnlineLive()).thenReturn(true);

            Address address = new Address();
            address.setAddressLine1("Address line 1");
            Party party = new Party();
            party.setType(Party.Type.ORGANISATION);
            party.setOrganisationName("This is very long name exceeds 70 characters "
                                      + " to throw error for max length allowed");
            party.setPrimaryAddress(address);
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDraft()
                .applicant2(party)
                .build();
            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);

            // When
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            // Then
            assertThat(response.getErrors()).isNotEmpty();
        }
    }

    @Nested
    class MidEventParticularsOfClaimCallback {

        private final String pageId = "particulars-of-claim";
        private final CaseData baseCaseData = CaseDataBuilder.builder().atStateClaimDraft().build();

        private CaseData freshCaseData() {
            return objMapper.convertValue(
                objMapper.convertValue(baseCaseData, java.util.Map.class),
                CaseData.class
                );
        }

        @Test
        void shouldReturnErrors_whenNoDocuments() {
            // Given
            CaseData caseData = freshCaseData();
            CallbackParams params = callbackParamsOf(caseData, MID, pageId);

            // When
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            // Then
            assertThat(response.getErrors()).containsOnly("You must add Particulars of claim details");
        }

        @Test
        void shouldReturnErrors_whenParticularsOfClaimFieldsAreInErrorState() {
            // Given
            CaseData caseData = freshCaseData();
            ServedDocumentFiles servedDocumentFiles = new ServedDocumentFiles();
            caseData.setServedDocumentFiles(servedDocumentFiles);
            CallbackParams params = callbackParamsOf(caseData, MID, pageId);

            // When
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            // Then
            assertThat(response.getErrors()).containsOnly("You must add Particulars of claim details");
        }

        @Test
        void shouldReturnNoErrors_whenParticularOfClaimsFieldsAreValid() {
            // Given
            CaseData caseData = freshCaseData();
            ServedDocumentFiles servedDocumentFiles = new ServedDocumentFiles();
            servedDocumentFiles.setParticularsOfClaimText("Some string");
            caseData.setServedDocumentFiles(servedDocumentFiles);
            CallbackParams params = callbackParamsOf(caseData, MID, pageId);

            // When
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            // Then
            assertThat(response.getErrors()).isEmpty();
        }

    }

    @Nested
    class MidEventFeeCallback {

        private final String pageId = "fee";
        private Fee feeData;
        private final Organisation organisation = new Organisation()
            .setPaymentAccount(List.of("12345", "98765"));
        private final ObjectMapper mapper = new ObjectMapper();

        @BeforeEach
        void setup() {
            feeData = new Fee();
            feeData.setCode("CODE");
            feeData.setCalculatedAmountInPence(BigDecimal.valueOf(100));
            given(feesService.getFeeDataByClaimValue(any())).willReturn(feeData);
        }

        @Test
        void shouldCalculateClaimFeeAndAddPbaNumbers_whenCalledAndOrgExistsInPrd() {
            // Given
            given(organisationService.findOrganisation(any())).willReturn(Optional.of(organisation));

            CaseData caseData = CaseDataBuilder.builder().atStateClaimDraft().build();
            CallbackParams params = callbackParamsOf(caseData, MID, pageId);

            // When
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            // Then
            assertThat(response.getData())
                .extracting("claimFee")
                .extracting("calculatedAmountInPence", "code")
                .containsExactly(
                    String.valueOf(feeData.getCalculatedAmountInPence()),
                    feeData.getCode()
                ).doesNotHaveToString("version");

            assertThat(response.getData())
                .extracting("claimIssuedPaymentDetails")
                .extracting("customerReference")
                .isEqualTo("12345");

            DynamicList dynamicList = getDynamicList(response);

            List<String> actualPbas = dynamicList.getListItems().stream()
                .map(DynamicListElement::getLabel)
                .toList();

            assertThat(actualPbas).containsOnly("12345", "98765");
            assertThat(dynamicList.getValue()).isEqualTo(DynamicListElement.EMPTY);
        }

        @Test
        void shouldCalculateClaimFee_whenCalledAndOrgDoesNotExistInPrd() {
            // Given
            given(organisationService.findOrganisation(any())).willReturn(Optional.empty());

            CaseData caseData = CaseDataBuilder.builder().atStateClaimDraft().build();
            CallbackParams params = callbackParamsOf(caseData, MID, pageId);

            // When
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            // Then
            assertThat(response.getData())
                .extracting("claimFee")
                .extracting("calculatedAmountInPence", "code")
                .containsExactly(
                    String.valueOf(feeData.getCalculatedAmountInPence()),
                    feeData.getCode()
                ).doesNotHaveToString("version");

            assertThat(response.getData())
                .extracting("claimIssuedPaymentDetails")
                .extracting("customerReference")
                .isEqualTo("12345");

            DynamicList expectedList = new DynamicList();
            expectedList.setValue(DynamicListElement.EMPTY);
            assertThat(getDynamicList(response))
                .isEqualTo(expectedList);
        }

        @Test
        void shouldSetPBAv3SpecFlagOn_whenPBAv3IsActivated() {
            // Given
            given(organisationService.findOrganisation(any())).willReturn(Optional.empty());

            CaseData caseData = CaseDataBuilder.builder().atStateClaimDraft().build();
            CallbackParams params = callbackParamsOf(caseData, MID, pageId);

            // When
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            // Then
            assertThat(response.getData())
                .extracting("paymentTypePBASpec").isEqualTo("PBAv3");
        }

        private DynamicList getDynamicList(AboutToStartOrSubmitCallbackResponse response) {
            return mapper.convertValue(response.getData().get("applicantSolicitor1PbaAccounts"), DynamicList.class);
        }
    }

    @Nested
    class MidEventGetIdamEmailCallback {

        private static final String PAGE_ID = "idam-email";

        @Test
        void shouldAddEmailLabelToData_whenInvoked() {
            // Given
            String userId = UUID.randomUUID().toString();
            String email = "example@email.com";

            given(userService.getUserDetails(any()))
                .willReturn(UserDetails.builder().email(email).id(userId).build());

            CaseData caseData = CaseDataBuilder.builder().atStateClaimDraft().build();
            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);

            // When
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            CaseData updatedData = objMapper.convertValue(response.getData(), CaseData.class);

            // Then
            assertThat(updatedData.getApplicantSolicitor1CheckEmail()).isNotNull();
            assertThat(updatedData.getApplicantSolicitor1CheckEmail().getEmail()).isEqualTo(email);
        }

        @Test
        void shouldRemoveExistingEmail_whenOneHasAlreadyBeenEntered() {
            // Given
            String userId = UUID.randomUUID().toString();
            String email = "example@email.com";

            given(userService.getUserDetails(any()))
                .willReturn(UserDetails.builder().email(email).id(userId).build());

            IdamUserDetails idamUserDetails = new IdamUserDetails(userId, email);
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDraft().build();
            caseData.setApplicantSolicitor1UserDetails(idamUserDetails);

            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);

            // When
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            // Then
            assertThat(response.getData())
                .extracting("applicantSolicitor1CheckEmail")
                .extracting("email")
                .isEqualTo(email);
            assertThat(response.getData())
                .doesNotHaveToString("applicantSolicitor1UserDetails")
                .doesNotHaveToString("email");
        }
    }

    @Nested
    class MidEventApplicant1OrgPolicyCallback {

        private static final String PAGE_ID = "appOrgPolicy";

        @Test
        void shouldReturnError_whenOrganisationPolicyIsNull() {
            // Given
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDraft()
                .applicant1OrganisationPolicy(null)
                .build();
            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);

            // When
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            // Then
            assertThat(response.getErrors()).containsExactly("No Organisation selected");
        }

        @Test
        void shouldReturnError_whenOrganisationIsNull() {
            // Given
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDraft()
                .applicant1OrganisationPolicy(new OrganisationPolicy().setOrganisation(null))
                .build();
            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);

            // When
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            // Then
            assertThat(response.getErrors()).containsExactly("No Organisation selected");
        }

        @Test
        void shouldReturnError_whenOrganisationIdIsNull() {
            // Given
            uk.gov.hmcts.reform.ccd.model.Organisation organisation
                = new uk.gov.hmcts.reform.ccd.model.Organisation().setOrganisationID(null);

            CaseData caseData = CaseDataBuilder.builder().atStateClaimDraft()
                .applicant1OrganisationPolicy(new OrganisationPolicy().setOrganisation(organisation))
                .build();
            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);

            // When
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            // Then
            assertThat(response.getErrors()).containsExactly("No Organisation selected");
        }

        @Test
        void shouldBeSuccessful_whenValid() {
            // Given
            uk.gov.hmcts.reform.ccd.model.Organisation organisation
                = new uk.gov.hmcts.reform.ccd.model.Organisation().setOrganisationID("orgId");

            CaseData caseData = CaseDataBuilder.builder().atStateClaimDraft()
                .applicant1OrganisationPolicy(new OrganisationPolicy().setOrganisation(organisation))
                .build();
            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);

            // When
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            // Then
            assertThat(response.getErrors()).isEmpty();
        }

    }

    @Nested
    class MidEventRespondent1OrgPolicyCallback {

        private static final String PAGE_ID = "repOrgPolicy";

        @Test
        void shouldReturnError_whenOrganisationPolicyIsNull() {
            // Given
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDraft()
                .respondent1OrganisationPolicy(null)
                .respondent1OrgRegistered(YES)
                .build();
            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);

            // When
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            // Then
            assertThat(response.getErrors()).containsExactly("No Organisation selected");
        }

        @Test
        void shouldReturnError_whenOrganisationIsNull() {
            // Given
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDraft()
                .respondent1OrganisationPolicy(new OrganisationPolicy().setOrganisation(null))
                .respondent1OrgRegistered(YES)
                .build();
            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);

            // When
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            // Then
            assertThat(response.getErrors()).containsExactly("No Organisation selected");
        }

        @Test
        void shouldReturnError_whenOrganisationIdIsNull() {
            // Given
            uk.gov.hmcts.reform.ccd.model.Organisation organisation
                = new uk.gov.hmcts.reform.ccd.model.Organisation().setOrganisationID(null);

            CaseData caseData = CaseDataBuilder.builder().atStateClaimDraft()
                .respondent1OrganisationPolicy(new OrganisationPolicy().setOrganisation(organisation))
                .respondent1OrgRegistered(YES)
                .build();
            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);

            // When
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            // Then
            assertThat(response.getErrors()).containsExactly("No Organisation selected");
        }

        @Test
        void shouldBeSuccessful_whenValid() {
            // Given
            uk.gov.hmcts.reform.ccd.model.Organisation organisation
                = new uk.gov.hmcts.reform.ccd.model.Organisation().setOrganisationID("orgId");

            CaseData caseData = CaseDataBuilder.builder().atStateClaimDraft()
                .respondent1OrganisationPolicy(new OrganisationPolicy().setOrganisation(organisation))
                .respondent1OrgRegistered(YES)
                .build();
            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);

            // When
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            // Then
            assertThat(response.getErrors()).isEmpty();
        }
    }

    @Nested
    class MidEventRespondent2OrgPolicyCallback {

        private static final String PAGE_ID = "rep2OrgPolicy";

        @Test
        void shouldReturnError_whenOrganisationPolicyIsNull() {
            // Given
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDraft()
                .respondent2OrganisationPolicy(null)
                .respondent2OrgRegistered(YES)
                .build();
            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);

            // When
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            // Then
            assertThat(response.getErrors()).containsExactly("No Organisation selected");
        }

        @Test
        void shouldReturnError_whenOrganisationIsNull() {
            // Given
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDraft()
                .respondent2OrganisationPolicy(new OrganisationPolicy().setOrganisation(null))
                .respondent2OrgRegistered(YES)
                .build();
            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);

            // When
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            // Then
            assertThat(response.getErrors()).containsExactly("No Organisation selected");
        }

        @Test
        void shouldReturnError_whenOrganisationIdIsNull() {
            // Given
            uk.gov.hmcts.reform.ccd.model.Organisation organisation
                = new uk.gov.hmcts.reform.ccd.model.Organisation().setOrganisationID(null);

            CaseData caseData = CaseDataBuilder.builder().atStateClaimDraft()
                .respondent2OrganisationPolicy(new OrganisationPolicy().setOrganisation(organisation))
                .respondent2OrgRegistered(YES)
                .build();
            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);

            // When
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            // Then
            assertThat(response.getErrors()).containsExactly("No Organisation selected");
        }

        @Test
        void shouldBeSuccessful_whenValid() {
            // Given
            uk.gov.hmcts.reform.ccd.model.Organisation organisation
                = new uk.gov.hmcts.reform.ccd.model.Organisation().setOrganisationID("orgId");

            CaseData caseData = CaseDataBuilder.builder().atStateClaimDraft()
                .respondent2OrganisationPolicy(new OrganisationPolicy().setOrganisation(organisation))
                .respondent2OrgRegistered(YES)
                .build();
            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);

            // When
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            // Then
            assertThat(response.getErrors()).isEmpty();
        }
    }

    @Nested
    class MidStatementOfTruth {

        @Test
        void shouldSetEmptyCallbackResponse_whenStatementOfTruthMidEventIsCalled() {
            // Given
            String name = "John Smith";
            String role = "Solicitor";

            StatementOfTruth statementOfTruth = new StatementOfTruth();
            statementOfTruth.setName(name);
            statementOfTruth.setRole(role);
            CaseData caseData = CaseDataBuilder.builder()
                .uiStatementOfTruth(statementOfTruth)
                .build();

            CallbackParams params = callbackParamsOf(caseData, MID, "statement-of-truth");

            // When
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            // Then
            assertThat(response.getData()).isNull();
        }
    }

    @Nested
    class MidAmountBreakup {

        @Test
        void shouldCalculateAmountBreakup_whenCalled() {
            // Given
            ClaimAmountBreakupDetails breakupDetails1 = new ClaimAmountBreakupDetails();
            breakupDetails1.setClaimAmount(new BigDecimal(1000));
            breakupDetails1.setClaimReason("Test reason1");
            ClaimAmountBreakup breakup1 = new ClaimAmountBreakup();
            breakup1.setValue(breakupDetails1);
            List<ClaimAmountBreakup> claimAmountBreakup = new ArrayList<>();
            claimAmountBreakup.add(breakup1);

            ClaimAmountBreakupDetails breakupDetails2 = new ClaimAmountBreakupDetails();
            breakupDetails2.setClaimAmount(new BigDecimal(2000));
            breakupDetails2.setClaimReason("Test reason2");
            ClaimAmountBreakup breakup2 = new ClaimAmountBreakup();
            breakup2.setValue(breakupDetails2);
            claimAmountBreakup.add(breakup2);
            ClaimAmountBreakupDetails claimAmountBreakupDetails = new ClaimAmountBreakupDetails();
            claimAmountBreakupDetails.setClaimAmount(new BigDecimal(1000));
            claimAmountBreakupDetails.setClaimReason("Test reason1");
            ClaimAmountBreakup breakup = new ClaimAmountBreakup();
            breakup.setValue(claimAmountBreakupDetails);
            claimAmountBreakup.add(breakup);

            ClaimAmountBreakupDetails claimAmountBreakupDetails2 = new ClaimAmountBreakupDetails();
            claimAmountBreakupDetails2.setClaimAmount(new BigDecimal(2000));
            claimAmountBreakupDetails2.setClaimReason("Test reason2");
            ClaimAmountBreakup breakupAdditional = new ClaimAmountBreakup();
            breakupAdditional.setValue(claimAmountBreakupDetails2);
            claimAmountBreakup.add(breakupAdditional);

            CaseData caseData = CaseDataBuilder.builder().build();
            caseData.setClaimAmountBreakup(claimAmountBreakup);

            CallbackParams params = callbackParamsOf(caseData, MID, "amount-breakup");

            // When
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            // Then
            String expectedSummary = " | Description | Amount | \n"
                + " |---|---| \n"
                + " | Test reason1 | £ 10.00 |\n"
                + " | Test reason2 | £ 20.00 |\n"
                + " | Test reason1 | £ 10.00 |\n"
                + " | Test reason2 | £ 20.00 |\n"
                + " | **Total** | £ 60.00 | ";

            assertThat(response.getData())
                .containsEntry("claimAmountBreakupSummaryObject", expectedSummary);
        }

        @Test
        void shouldCalculateAmount_AndReturnNoErrorWhenAbove25kAndToggleActive() {
            // Given
            when(toggleService.isMultiOrIntermediateTrackEnabled(any())).thenReturn(true);
            ClaimAmountBreakupDetails highAmountDetails = new ClaimAmountBreakupDetails();
            highAmountDetails.setClaimAmount(new BigDecimal(10000000));
            highAmountDetails.setClaimReason("Test reason1");
            ClaimAmountBreakup highAmountBreakup = new ClaimAmountBreakup();
            highAmountBreakup.setValue(highAmountDetails);
            List<ClaimAmountBreakup> claimAmountBreakup = new ArrayList<>();
            claimAmountBreakup.add(highAmountBreakup);
            CaseData caseData = CaseDataBuilder.builder().build();
            caseData.setClaimAmountBreakup(claimAmountBreakup);
            CallbackParams params = callbackParamsOf(caseData, MID, "amount-breakup");
            // When
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            // Then
            assertThat(response.getErrors()).isNull();
        }
    }

    @Nested
    class MidCalculateInterest {

        @Test
        void shouldCalculateInterest_whenPopulated() {
            // Given
            SameRateInterestSelection sameRateInterestSelection = new SameRateInterestSelection();
            sameRateInterestSelection.setSameRateInterestType(SameRateInterestType.SAME_RATE_INTEREST_8_PC);
            CaseData caseData = CaseDataBuilder.builder().build();
            caseData.setClaimInterest(YES);
            caseData.setInterestClaimOptions(InterestClaimOptions.SAME_RATE_INTEREST);
            caseData.setSameRateInterestSelection(sameRateInterestSelection);
            caseData.setInterestClaimFrom(InterestClaimFromType.FROM_CLAIM_SUBMIT_DATE);
            caseData.setTotalClaimAmount(new BigDecimal(1000));

            when(interestCalculator.calculateInterest(caseData)).thenReturn(new BigDecimal(0));
            CallbackParams params = callbackParamsOf(caseData, MID, "interest-calc");

            // When
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            CaseData updatedData = objMapper.convertValue(response.getData(), CaseData.class);

            // Then
            assertThat(updatedData.getCalculatedInterest()).isNotNull();
            assertThat(updatedData.getCalculatedInterest()).contains("Claim amount | £ 1000.00");
            assertThat(updatedData.getCalculatedInterest()).contains("Interest amount | £ 0.00");
            assertThat(updatedData.getCalculatedInterest()).contains("Total amount | £ 1000.00");
        }

        @Test
        void shouldDefaultInterestUntil_whenInterestFromIsSubmittedDate() {
            // Given
            SameRateInterestSelection sameRateSelection = new SameRateInterestSelection();
            sameRateSelection.setSameRateInterestType(SameRateInterestType.SAME_RATE_INTEREST_8_PC);
            CaseData caseData = CaseDataBuilder.builder().build();
            caseData.setClaimInterest(YES);
            caseData.setInterestClaimOptions(InterestClaimOptions.SAME_RATE_INTEREST);
            caseData.setSameRateInterestSelection(sameRateSelection);
            caseData.setInterestClaimFrom(InterestClaimFromType.FROM_CLAIM_SUBMIT_DATE);
            caseData.setTotalClaimAmount(new BigDecimal(1000));

            when(interestCalculator.calculateInterest(any(CaseData.class))).thenReturn(new BigDecimal(0));
            CallbackParams params = callbackParamsOf(caseData, MID, INTEREST_FROM_PAGE_ID);

            // When
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            CaseData updatedData = objMapper.convertValue(response.getData(), CaseData.class);

            // Then
            assertThat(updatedData.getInterestClaimUntil()).isEqualTo(InterestClaimUntilType.UNTIL_SETTLED_OR_JUDGEMENT_MADE);
            assertThat(updatedData.getCalculatedInterest()).isNotNull();
            assertThat(updatedData.getCalculatedInterest()).contains("Claim amount | £ 1000.00");
            assertThat(updatedData.getCalculatedInterest()).contains("Interest amount | £ 0.00");
            assertThat(updatedData.getCalculatedInterest()).contains("Total amount | £ 1000.00");
        }
    }

    @Nested
    class MidSpecValidateClaimInterestDate {

        @Test
        void shouldValidateClaimInterestDate_whenPopulated() {
            // Given
            CaseData caseData = CaseDataBuilder.builder().build();
            caseData.setInterestFromSpecificDate(LocalDate.now().minusDays(1));

            CallbackParams params = callbackParamsOf(caseData, MID, "ValidateClaimInterestDate");
            params.getRequest().setEventId("CREATE_CLAIM_SPEC");
            when(interestCalculator.calculateInterest(caseData)).thenReturn(new BigDecimal(0));
            // When
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            // Then
            assertThat(response.getErrors()).isEmpty();
        }

        @Test
        void shouldReturnErrorWhenValidateClaimInterestDatePopulatedWithFutureDate() {
            // Given
            CaseData caseData = CaseDataBuilder.builder().build();
            caseData.setInterestFromSpecificDate(LocalDate.now().plusDays(1));

            CallbackParams params = callbackParamsOf(caseData, MID, "ValidateClaimInterestDate");
            params.getRequest().setEventId("CREATE_CLAIM_SPEC");
            when(interestCalculator.calculateInterest(caseData)).thenReturn(new BigDecimal(0));
            // When
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            // Then
            assertThat(response.getErrors()).contains("Correct the date. You can’t use a future date.");
        }
    }

    @Nested
    class MidSpecValidateClaimTimelineDate {

        @Test
        void shouldValidateClaimTimelineDate_whenPopulated() {
            // Given
            List<TimelineOfEvents> timelineOfEvents = new ArrayList<>();
            TimelineOfEventDetails pastEventDetails = new TimelineOfEventDetails();
            pastEventDetails.setTimelineDate(LocalDate.now().minusDays(1));
            TimelineOfEvents pastEvent = new TimelineOfEvents();
            pastEvent.setValue(pastEventDetails);
            timelineOfEvents.add(pastEvent);
            CaseData caseData = CaseDataBuilder.builder().build();
            caseData.setTimelineOfEvents(timelineOfEvents);

            CallbackParams params = callbackParamsOf(caseData, MID, "ValidateClaimTimelineDate");
            // When
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            // Then
            assertThat(response.getErrors()).isEmpty();
        }

        @Test
        void shouldReturnErrorWhenTimelineDatePopulatedWithFutureDate() {
            // Given
            List<TimelineOfEvents> timelineOfEvents = new ArrayList<>();
            TimelineOfEventDetails futureEventDetails = new TimelineOfEventDetails();
            futureEventDetails.setTimelineDate(LocalDate.now().plusDays(1));
            TimelineOfEvents futureEvent = new TimelineOfEvents();
            futureEvent.setValue(futureEventDetails);
            timelineOfEvents.add(futureEvent);
            CaseData caseData = CaseDataBuilder.builder().build();
            caseData.setTimelineOfEvents(timelineOfEvents);

            CallbackParams params = callbackParamsOf(caseData, MID, "ValidateClaimTimelineDate");
            // When
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            // Then
            assertThat(response.getErrors()).contains("Correct the date. You can’t use a future date.");
        }
    }

    @Nested
    class MidSpecCalculateInterest {

        @Test
        void shouldValidateClaimTimelineDate_whenPopulated() {
            // Given
            List<TimelineOfEvents> timelineOfEvents = new ArrayList<>();
            TimelineOfEventDetails specEventDetails = new TimelineOfEventDetails();
            specEventDetails.setTimelineDate(LocalDate.now().minusDays(1));
            TimelineOfEvents specEvent = new TimelineOfEvents();
            specEvent.setValue(specEventDetails);
            timelineOfEvents.add(specEvent);
            CaseData caseData = CaseDataBuilder.builder().build();
            caseData.setTotalClaimAmount(new BigDecimal(1000));

            CallbackParams params = callbackParamsOf(caseData, MID, "ClaimInterest");
            // When
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            // Then
            assertThat(response.getData()).containsEntry(
                "calculatedInterest", " | Description | Amount | \n" +
                    " |---|---| \n" +
                    " | Claim amount | £ 1000.00 | \n" +
                    " | Interest amount | £ 0 | \n" +
                " | Total amount | £ 1000.00 |"
            );
        }
    }

    @Nested
    class MidCalculateSpecFee {

        @Test
        void shouldCalculateSpecFee_whenPopulated() {
            // Given
            List<TimelineOfEvents> timelineOfEvents = new ArrayList<>();
            TimelineOfEventDetails feeTimelineDetails = new TimelineOfEventDetails();
            feeTimelineDetails.setTimelineDate(LocalDate.now().minusDays(1));
            TimelineOfEvents feeTimelineEvent = new TimelineOfEvents();
            feeTimelineEvent.setValue(feeTimelineDetails);
            timelineOfEvents.add(feeTimelineEvent);
            SameRateInterestSelection specSameRateSelection = new SameRateInterestSelection();
            specSameRateSelection.setSameRateInterestType(SameRateInterestType.SAME_RATE_INTEREST_8_PC);
            CaseData caseData = CaseDataBuilder.builder().build();
            caseData.setClaimInterest(YES);
            caseData.setInterestClaimOptions(InterestClaimOptions.SAME_RATE_INTEREST);
            caseData.setSameRateInterestSelection(specSameRateSelection);
            caseData.setInterestClaimFrom(InterestClaimFromType.FROM_CLAIM_SUBMIT_DATE);
            caseData.setTotalClaimAmount(new BigDecimal(1000));
            when(interestCalculator.calculateInterest(caseData)).thenReturn(new BigDecimal(0));
            CallbackParams params = callbackParamsOf(caseData, MID, "spec-fee");
            // When
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            // Then
            assertThat(response.getData()).containsEntry("applicantSolicitor1PbaAccountsIsEmpty", "Yes");
        }
    }

    @Nested
    class MidRespondent2SameLegalRepToNo {

        @Test
        void shouldSetRespondent2SameLegalRepToNo_whenPopulated() {
            // Given
            CaseData caseData = CaseDataBuilder.builder().build();
            caseData.setSpecRespondent1Represented(NO);

            CallbackParams params = callbackParamsOf(caseData, MID, "setRespondent2SameLegalRepresentativeToNo");
            // When
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            CaseData updatedData = objMapper.convertValue(response.getData(), CaseData.class);

            // Then
            assertThat(updatedData.getRespondent2SameLegalRepresentative()).isEqualTo(NO);
        }
    }

    @Nested
    class ValidateEmails {

        @Nested
        class ClaimantRepEmail {

            @Test
            void shouldReturnNoErrors_whenIdamEmailIsCorrect() {
                // Given
                CorrectEmail correctEmail = new CorrectEmail();
                correctEmail.setCorrect(YES);
                CaseData caseData = CaseDataBuilder.builder().build();
                caseData.setApplicantSolicitor1CheckEmail(correctEmail);

                CallbackParams params = callbackParamsOf(caseData, MID, "validate-claimant-legal-rep-email");

                // When
                var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

                // Then
                assertThat(response.getErrors()).isNull();
            }

            @Test
            void shouldReturnNoErrors_whenIdamEmailIsNotCorrectButAdditionalEmailIsValid() {
                // Given
                String validEmail = "john@example.com";

                CorrectEmail incorrectIdamEmail = new CorrectEmail();
                incorrectIdamEmail.setCorrect(NO);
                IdamUserDetails userDetails = new IdamUserDetails();
                userDetails.setEmail(validEmail);
                CaseData caseData = CaseDataBuilder.builder().build();
                caseData.setApplicantSolicitor1CheckEmail(incorrectIdamEmail);
                caseData.setApplicantSolicitor1UserDetails(userDetails);

                CallbackParams params = callbackParamsOf(caseData, MID, "validate-claimant-legal-rep-email");

                // When
                var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

                // Then
                assertThat(response.getErrors()).isEmpty();
            }

            @Test
            void shouldReturnErrors_whenIdamEmailIsNotCorrectAndAdditionalEmailIsInvalid() {
                // Given
                String invalidEmail = "a@a";

                CorrectEmail incorrectEmail = new CorrectEmail();
                incorrectEmail.setCorrect(NO);
                IdamUserDetails idamUserDetails = new IdamUserDetails();
                idamUserDetails.setEmail(invalidEmail);
                CaseData caseData = CaseDataBuilder.builder().build();
                caseData.setApplicantSolicitor1CheckEmail(incorrectEmail);
                caseData.setApplicantSolicitor1UserDetails(idamUserDetails);

                CallbackParams params = callbackParamsOf(caseData, MID, "validate-claimant-legal-rep-email");

                // When
                var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

                // Then
                assertThat(response.getErrors()).containsExactly("Enter an email address in the correct format,"
                                                                     + " for example john.smith@example.com");
            }
        }

        @Nested
        class DefendantRepEmail {

            @Test
            void shouldReturnNoErrors_whenEmailIsValid() {
                // Given
                String validEmail = "john@example.com";

                CaseData caseData = CaseDataBuilder.builder().build();
                caseData.setRespondentSolicitor1EmailAddress(validEmail);

                CallbackParams params = callbackParamsOf(caseData, MID, "validate-defendant-legal-rep-email");

                // When
                var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

                // Then
                assertThat(response.getErrors()).isEmpty();
            }

            @Test
            void shouldReturnErrors_whenEmailIsInvalid() {
                // Given
                String invalidEmail = "a@a";

                CaseData caseData = CaseDataBuilder.builder().build();
                caseData.setRespondentSolicitor1EmailAddress(invalidEmail);

                CallbackParams params = callbackParamsOf(caseData, MID, "validate-defendant-legal-rep-email");

                // When
                var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

                // Then
                assertThat(response.getErrors()).containsExactly("Enter an email address in the correct format,"
                                                                     + " for example john.smith@example.com");
            }

            @Test
            void shouldReturnNoErrors_whenEmailIsValidForSpecDefendant1() {
                // Given
                String validEmail = "john@example.com";

                CaseData caseData = CaseDataBuilder.builder().build();
                caseData.setRespondentSolicitor1EmailAddress(validEmail);

                CallbackParams params = callbackParamsOf(caseData, MID, "validate-spec-defendant-legal-rep-email");

                // When
                var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

                // Then
                assertThat(response.getErrors()).isEmpty();
            }

            @Test
            void shouldReturnErrors_whenEmailIsInvalidForSpecDefendant1() {
                // Given
                String invalidEmail = "a@a";

                CaseData caseData = CaseDataBuilder.builder().build();
                caseData.setRespondentSolicitor1EmailAddress(invalidEmail);

                CallbackParams params = callbackParamsOf(caseData, MID, "validate-spec-defendant-legal-rep-email");

                // When
                var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

                // Then
                assertThat(response.getErrors()).containsExactly("Enter an email address in the correct format,"
                                                                     + " for example john.smith@example.com");
            }

            @Test
            void shouldReturnNoErrors_whenEmailIsValidForSpecDefendant2() {
                // Given
                String validEmail = "john@example.com";

                CaseData caseData = CaseDataBuilder.builder().build();
                caseData.setRespondentSolicitor2EmailAddress(validEmail);

                CallbackParams params = callbackParamsOf(caseData, MID, "validate-spec-defendant2-legal-rep-email");

                // When
                var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

                // Then
                assertThat(response.getErrors()).isEmpty();
            }

            @Test
            void shouldReturnErrors_whenEmailIsInvalidForSpecDefendant2() {
                // Given
                String invalidEmail = "a@a";

                CaseData caseData = CaseDataBuilder.builder().build();
                caseData.setRespondentSolicitor2EmailAddress(invalidEmail);

                CallbackParams params = callbackParamsOf(caseData, MID, "validate-spec-defendant2-legal-rep-email");

                // When
                var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

                // Then
                assertThat(response.getErrors()).containsExactly("Enter an email address in the correct format,"
                                                                     + " for example john.smith@example.com");
            }

        }
    }

    @Nested
    class ValidateAddress {

        @Nested
        class Respondent1Address {

            @BeforeEach
            void setup() {
                when(toggleService.isJudgmentOnlineLive()).thenReturn(true);
            }

            @Test
            void shouldReturnNoErrors_whenRespondent1AddressValid() {
                // Given
                Party respondent1 = PartyBuilder.builder().company().build();

                CaseData caseData = CaseDataBuilder.builder().build();
                caseData.setRespondent1(respondent1);

                CallbackParams params = callbackParamsOf(caseData, MID, "respondent1");

                given(postcodeValidator.validate(any())).willReturn(List.of());

                // When
                AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                    .handle(params);

                // Then
                assertThat(response).isNotNull();
                assertThat(response.getData()).isNotNull();
                assertEquals(0, response.getErrors().size());
            }

            @Test
            void shouldReturnErrors_whenRespondent1AddressNotValid() {
                // Given
                Address invalidAddress = new Address();
                invalidAddress.setAddressLine1("Line 1 test again for more than 35 characters");
                invalidAddress.setAddressLine2("Line 1 test again for more than 35 characters");
                invalidAddress.setAddressLine3("Line 1 test again for more than 35 characters");
                invalidAddress.setCounty("Line 1 test again for more than 35 characters");
                invalidAddress.setPostCode("PostCode test more than 8 characters");
                invalidAddress.setPostTown("Line 1 test again for more than 35 characters");
                Party respondent1 = new Party();
                respondent1.setType(Party.Type.ORGANISATION);
                respondent1.setPrimaryAddress(invalidAddress);
                CaseData caseData = CaseDataBuilder.builder().atStateClaimDraft()
                    .respondent1(respondent1).build();

                CallbackParams params = callbackParamsOf(caseData, MID, "respondent1");

                // When
                var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

                // Then
                assertThat(response.getErrors()).isNotEmpty();
                assertThat(response.getErrors()).hasSize(6);
            }
        }

        @Nested
        class Respondent2Address {

            @BeforeEach
            void setup() {
                when(toggleService.isJudgmentOnlineLive()).thenReturn(true);
            }

            @Test
            void shouldReturnNoErrors_whenRespondent2AddressValid() {
                // Given
                Party respondent2 = PartyBuilder.builder().company().build();

                CaseData caseData = CaseDataBuilder.builder().build();
                caseData.setRespondent2(respondent2);

                CallbackParams params = callbackParamsOf(caseData, MID, "respondent2");

                given(postcodeValidator.validate(any())).willReturn(List.of());

                // When
                AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                    .handle(params);

                // Then
                assertThat(response).isNotNull();
                assertThat(response.getData()).isNotNull();
                assertEquals(0, response.getErrors().size());
            }

            @Test
            void shouldReturnErrors_whenRespondent2AddressNotValid() {
                // Given
                Address invalidAddress = new Address();
                invalidAddress.setAddressLine1("Line 1 test again for more than 35 characters");
                invalidAddress.setAddressLine2("Line 1 test again for more than 35 characters");
                invalidAddress.setAddressLine3("Line 1 test again for more than 35 characters");
                invalidAddress.setCounty("Line 1 test again for more than 35 characters");
                invalidAddress.setPostCode("PostCode test more than 8 characters");
                invalidAddress.setPostTown("Line 1 test again for more than 35 characters");
                Party respondent2 = new Party();
                respondent2.setType(Party.Type.ORGANISATION);
                respondent2.setPrimaryAddress(invalidAddress);
                CaseData caseData = CaseDataBuilder.builder().atStateClaimDraft()
                    .respondent2(respondent2).build();

                CallbackParams params = callbackParamsOf(caseData, MID, "respondent2");

                // When
                AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                    .handle(params);

                // Then
                assertThat(response.getErrors()).isNotEmpty();
                assertThat(response.getErrors()).hasSize(6);
            }
        }

        @Nested
        class RespondentSolicitor1Address {

            @Test
            void shouldReturnNoErrors_whenSolicitor1AddressValid() {
                // Given
                SolicitorOrganisationDetails respondentSolicitor1OrganisationDetails =
                    new SolicitorOrganisationDetails();
                respondentSolicitor1OrganisationDetails.setAddress(AddressBuilder.defaults().build());

                CaseData caseData = CaseDataBuilder.builder().build();
                caseData.setRespondentSolicitor1OrganisationDetails(respondentSolicitor1OrganisationDetails);

                CallbackParams params = callbackParamsOf(caseData, MID, "respondentSolicitor1");

                given(postcodeValidator.validate(any())).willReturn(List.of());

                // When
                AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                    .handle(params);

                // Then
                assertThat(response).isNotNull();
                assertThat(response.getData()).isNull();
                assertThat(response.getErrors()).isNotNull();
                assertEquals(0, response.getErrors().size());
            }

            @Test
            void shouldReturnErrors_whenSolicitor1AddressNotValid() {
                // Given
                Address invalidSolicitorAddress = new Address();
                invalidSolicitorAddress.setPostCode(null);
                SolicitorOrganisationDetails respondentSolicitor1OrganisationDetails =
                    new SolicitorOrganisationDetails();
                respondentSolicitor1OrganisationDetails.setAddress(invalidSolicitorAddress);

                CaseData caseData = CaseDataBuilder.builder().build();
                caseData.setRespondentSolicitor1OrganisationDetails(respondentSolicitor1OrganisationDetails);

                CallbackParams params = callbackParamsOf(caseData, MID, "respondentSolicitor1");

                given(postcodeValidator.validate(any()))
                    .willReturn(List.of("Please enter Postcode"));

                // When
                AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                    .handle(params);

                // Then
                assertThat(response).isNotNull();
                assertThat(response.getData()).isNull();
                assertThat(response.getErrors()).isNotNull();
                assertEquals(1, response.getErrors().size());
                assertEquals("Please enter Postcode", response.getErrors().get(0));
            }
        }

        @Nested
        class RespondentSolicitor2Address {

            @Test
            void shouldReturnNoErrors_whenSolicitor2AddressValid() {
                // Given
                SolicitorOrganisationDetails respondentSolicitor2OrganisationDetails =
                    new SolicitorOrganisationDetails();
                respondentSolicitor2OrganisationDetails.setAddress(AddressBuilder.defaults().build());

                CaseData caseData = CaseDataBuilder.builder().build();
                caseData.setRespondentSolicitor2OrganisationDetails(respondentSolicitor2OrganisationDetails);

                CallbackParams params = callbackParamsOf(caseData, MID, "respondentSolicitor2");

                given(postcodeValidator.validate(any())).willReturn(List.of());

                // When
                AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                    .handle(params);

                // Then
                assertThat(response).isNotNull();
                assertThat(response.getData()).isNull();
                assertThat(response.getErrors()).isNotNull();
                assertEquals(0, response.getErrors().size());
            }

            @Test
            void shouldReturnErrors_whenSolicitor2AddressNotValid() {
                // Given
                Address invalidSolicitor2Address = new Address();
                invalidSolicitor2Address.setPostCode(null);
                SolicitorOrganisationDetails respondentSolicitor2OrganisationDetails =
                    new SolicitorOrganisationDetails();
                respondentSolicitor2OrganisationDetails.setAddress(invalidSolicitor2Address);

                CaseData caseData = CaseDataBuilder.builder().build();
                caseData.setRespondentSolicitor2OrganisationDetails(respondentSolicitor2OrganisationDetails);

                CallbackParams params = callbackParamsOf(caseData, MID, "respondentSolicitor2");

                given(postcodeValidator.validate(any()))
                    .willReturn(List.of("Please enter Postcode"));

                // When
                AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                    .handle(params);

                // Then
                assertThat(response).isNotNull();
                assertThat(response.getData()).isNull();
                assertThat(response.getErrors()).isNotNull();
                assertEquals(1, response.getErrors().size());
                assertEquals("Please enter Postcode", response.getErrors().get(0));
            }
        }

        @Nested
        class CorrespondentApplicantAddress {

            @Test
            void shouldReturnNoErrors_whenRequiredAddressIsNo() {
                // Given
                CaseData caseData = CaseDataBuilder.builder().build();
                caseData.setSpecApplicantCorrespondenceAddressRequired(NO);

                CallbackParams params = callbackParamsOf(caseData, MID, "specCorrespondenceAddress");

                given(postcodeValidator.validate(any())).willReturn(List.of());

                // When
                AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                    .handle(params);

                // Then
                assertThat(response).isNotNull();
                assertThat(response.getData()).isNull();
                assertThat(response.getErrors()).isNull();
            }

            @Test
            void shouldReturnNoErrors_whenRequiredAddressIsYesAndValid() {
                // Given
                CaseData caseData = CaseDataBuilder.builder().build();
                caseData.setSpecApplicantCorrespondenceAddressRequired(YES);
                caseData.setSpecApplicantCorrespondenceAddressdetails(AddressBuilder.defaults().build());

                CallbackParams params = callbackParamsOf(caseData, MID, "specCorrespondenceAddress");

                given(postcodeValidator.validate(any())).willReturn(List.of());

                // When
                AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                    .handle(params);

                // Then
                assertThat(response).isNotNull();
                assertThat(response.getData()).isNull();
                assertThat(response.getErrors()).isNotNull();
                assertEquals(0, response.getErrors().size());
            }

            @Test
            void shouldReturnErrors_whenRequiredAddressIsYesAndNotValid() {
                // Given
                Address invalidApplicantAddress = new Address();
                invalidApplicantAddress.setPostCode(null);
                CaseData caseData = CaseDataBuilder.builder().build();
                caseData.setSpecApplicantCorrespondenceAddressRequired(YES);
                caseData.setSpecApplicantCorrespondenceAddressdetails(invalidApplicantAddress);

                CallbackParams params = callbackParamsOf(caseData, MID, "specCorrespondenceAddress");

                given(postcodeValidator.validate(any()))
                    .willReturn(List.of("Please enter Postcode"));

                // When
                AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                    .handle(params);

                // Then
                assertThat(response).isNotNull();
                assertThat(response.getData()).isNull();
                assertThat(response.getErrors()).isNotNull();
                assertEquals(1, response.getErrors().size());
                assertEquals("Please enter Postcode", response.getErrors().get(0));
            }
        }

        @Nested
        class CorrespondentRespondentAddress {

            @Test
            void shouldReturnNoErrors_whenRequiredAddressIsNo() {
                // Given
                CaseData caseData = CaseDataBuilder.builder().build();
                caseData.setSpecRespondentCorrespondenceAddressRequired(NO);

                CallbackParams params = callbackParamsOf(caseData, MID, "specRespondentCorrespondenceAddress");

                given(postcodeValidator.validate(any())).willReturn(List.of());

                // When
                AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                    .handle(params);

                // Then
                assertThat(response).isNotNull();
                assertThat(response.getData()).isNull();
                assertThat(response.getErrors()).isNull();
            }

            @Test
            void shouldReturnNoErrors_whenRequiredAddressIsYesAndValid() {
                // Given
                CaseData caseData = CaseDataBuilder.builder().build();
                caseData.setSpecRespondentCorrespondenceAddressRequired(YES);
                caseData.setSpecRespondentCorrespondenceAddressdetails(AddressBuilder.defaults().build());

                CallbackParams params = callbackParamsOf(caseData, MID, "specRespondentCorrespondenceAddress");

                given(postcodeValidator.validate(any())).willReturn(List.of());

                // When
                AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                    .handle(params);

                // Then
                assertThat(response).isNotNull();
                assertThat(response.getData()).isNull();
                assertThat(response.getErrors()).isNotNull();
                assertEquals(0, response.getErrors().size());
            }

            @Test
            void shouldReturnErrors_whenRequiredAddressIsYesAndNotValid() {
                // Given
                Address invalidRespondentAddress = new Address();
                invalidRespondentAddress.setPostCode(null);
                CaseData caseData = CaseDataBuilder.builder().build();
                caseData.setSpecRespondentCorrespondenceAddressRequired(YES);
                caseData.setSpecRespondentCorrespondenceAddressdetails(invalidRespondentAddress);

                CallbackParams params = callbackParamsOf(caseData, MID, "specRespondentCorrespondenceAddress");

                given(postcodeValidator.validate(any()))
                    .willReturn(List.of("Please enter Postcode"));

                // When
                AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                    .handle(params);

                // Then
                assertThat(response).isNotNull();
                assertThat(response.getData()).isNull();
                assertThat(response.getErrors()).isNotNull();
                assertEquals(1, response.getErrors().size());
                assertEquals("Please enter Postcode", response.getErrors().get(0));
            }
        }
    }

    @Nested
    class ValidatePartyName {

        @Test
        void shouldReturnErrors_whenRespondent1PartyNameNotValid() {
            // Given
            when(toggleService.isJudgmentOnlineLive()).thenReturn(true);

            Party respondent1 = new Party();
            respondent1.setType(Party.Type.ORGANISATION);
            respondent1.setOrganisationName(
                "Line 1 test again for more than 70 characters on the company party name");
            respondent1.setPrimaryAddress(AddressBuilder.defaults().build());
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDraft()
                .respondent1(respondent1).build();

            CallbackParams params = callbackParamsOf(caseData, MID, "respondent1");

            // When
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            // Then
            assertThat(response.getErrors()).isNotEmpty();
            assertThat(response.getErrors()).hasSize(1);
        }

        @Test
        void shouldReturnErrors_whenRespondent2PartyNameNotValid() {
            // Given
            when(toggleService.isJudgmentOnlineLive()).thenReturn(true);

            Party respondent2 = new Party();
            respondent2.setType(Party.Type.INDIVIDUAL);
            respondent2.setIndividualTitle(
                "Title test again for more than 70 characters on the company party name");
            respondent2.setIndividualFirstName(
                "Line 1 test again for more than 70 characters on the company party name");
            respondent2.setIndividualLastName(
                "Line 1 test again for more than 70 characters on the company party name");
            respondent2.setPrimaryAddress(AddressBuilder.defaults().build());
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDraft()
                .respondent2(respondent2).build();

            CallbackParams params = callbackParamsOf(caseData, MID, "respondent2");

            // When
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            // Then
            assertThat(response.getErrors()).isNotEmpty();
            assertThat(response.getErrors()).hasSize(1);
        }
    }

    @Nested
    class FlightDelayDetailsMidCallbacks {

        @Test
        void shouldGetAirlineList_whenRequired() {
            // Given
            List<AirlineEpimsId> airlineEpimsIDList = new ArrayList<>();
            AirlineEpimsId baAirline = new AirlineEpimsId("BA/Cityflyer", "111000");
            airlineEpimsIDList.add(baAirline);
            AirlineEpimsId otherAirline = new AirlineEpimsId("OTHER", "111111");
            airlineEpimsIDList.add(otherAirline);

            given(airlineEpimsDataLoader.getAirlineEpimsIDList())
                .willReturn(airlineEpimsIDList);

            CaseData caseData = CaseDataBuilder.builder().build();
            CallbackParams params = callbackParamsOf(caseData, MID, "get-airline-list");

            // When
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            // Then
            assertThat(response.getData()).extracting("flightDelayDetails").extracting("airlineList")
                .extracting("list_items").asList().extracting("label")
                .contains("BA/Cityflyer");

            assertThat(response.getData()).extracting("flightDelayDetails").extracting("airlineList")
                .extracting("list_items").asList().extracting("label")
                .contains("OTHER");
        }

        @Test
        void shouldReturnErrorWhenDateOfFlightIsInTheFuture() {
            // Given
            FlightDelayDetails flightDelayDetails = new FlightDelayDetails();
            flightDelayDetails.setScheduledDate(now().plusDays(1));
            CaseData caseData = CaseDataBuilder.builder().build();
            caseData.setIsFlightDelayClaim(YES);
            caseData.setFlightDelayDetails(flightDelayDetails);

            CallbackParams params = callbackParamsOf(caseData, MID, "validateFlightDelayDate");
            // When
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            // Then
            assertThat(response.getErrors()).contains("Scheduled date of flight must be today or in the past");
        }

        @ParameterizedTest
        @ValueSource(ints = {0, 1})
        void shouldNotReturnErrorWhenDateOfFlightIsTodayOrInThePast(Integer days) {
            // Given
            FlightDelayDetails flightDelayDetails = new FlightDelayDetails();
            flightDelayDetails.setScheduledDate(now().minusDays(days));
            CaseData caseData = CaseDataBuilder.builder().build();
            caseData.setIsFlightDelayClaim(YES);
            caseData.setFlightDelayDetails(flightDelayDetails);

            CallbackParams params = callbackParamsOf(caseData, MID, "validateFlightDelayDate");
            // When
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            // Then
            assertThat(response.getErrors()).isEmpty();
        }
    }

    @Nested
    class AboutToSubmitCallbackV1 {

        private CallbackParams params;
        private CaseData caseData;
        private String userId;

        private static final String EMAIL = "example@email.com";
        private static final String DIFFERENT_EMAIL = "other_example@email.com";
        private final LocalDateTime submittedDate = LocalDateTime.now();

        @BeforeEach
        void setup() {
            caseData = CaseDataBuilder.builder().atStateClaimDraft().build();
            params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
            userId = UUID.randomUUID().toString();

            given(userService.getUserDetails(any()))
                .willReturn(UserDetails.builder().email(EMAIL).id(userId).build());

            given(time.now()).willReturn(submittedDate);
            given(toggleConfiguration.getFeatureToggle()).willReturn("WA 4");
            DefendantPinToPostLRspec pinToPost = new DefendantPinToPostLRspec();
            pinToPost.setAccessCode(AccessCodeGenerator.generateAccessCode());
            pinToPost.setRespondentCaseRole(CaseRole.RESPONDENTSOLICITORONE.getFormattedName());
            pinToPost.setExpiryDate(LocalDate.now().plusDays(180));
            given(defendantPinToPostLRspecService.buildDefendantPinToPost())
                .willReturn(pinToPost);
        }

        @Test
        void shouldSetClaimFee_whenInvokedAndBulkClaim() {
            // Given
            Fee feeData = new Fee();
            feeData.setCode("FeeCode");
            feeData.setCalculatedAmountInPence(BigDecimal.valueOf(19990));
            CaseData localCaseData = CaseDataBuilder.builder().atStatePendingClaimIssued().build();
            localCaseData.setSdtRequestIdFromSdt("sdtRequestIdFromSdt");
            localCaseData.setTotalClaimAmount(BigDecimal.valueOf(1999));
            given(feesService.getFeeDataByTotalClaimAmount(any())).willReturn(feeData);
            when(interestCalculator.calculateBulkInterest(localCaseData)).thenReturn(new BigDecimal(0));
            given(organisationService.findOrganisation(any())).willReturn(Optional.of(bulkOrganisation));
            CallbackParams localParams = callbackParamsOf(localCaseData, ABOUT_TO_SUBMIT);
            // When
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(localParams);
            // Then
            assertThat(response.getData()).extracting("claimFee").extracting("calculatedAmountInPence", "code")
                .containsExactly(String.valueOf(feeData.getCalculatedAmountInPence()), feeData.getCode());
        }

        @Test
        void shouldAddSdtRequestId_whenInvokedAndBulkClaim() {
            // Given
            CaseData localCaseData = CaseDataBuilder.builder().atStatePendingClaimIssued().build();
            localCaseData.setSdtRequestIdFromSdt("sdtRequestIdFromSdt");
            localCaseData.setTotalClaimAmount(BigDecimal.valueOf(1999));
            when(interestCalculator.calculateBulkInterest(localCaseData)).thenReturn(new BigDecimal(0));
            given(organisationService.findOrganisation(any())).willReturn(Optional.of(bulkOrganisation));
            CallbackParams localParams = callbackParamsOf(localCaseData, ABOUT_TO_SUBMIT);
            // When
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(localParams);
            // Then
            assertThat(response.getData()).extracting("sdtRequestId").asString().contains("sdtRequestIdFromSdt");
        }

        @Test
        void shouldNotAddSdtRequestId_whenInvokedAndNotBulkClaim() {
            // Given
            CaseData localCaseData = CaseDataBuilder.builder().atStatePendingClaimIssued().build();
            CallbackParams localParams = callbackParamsOf(localCaseData, ABOUT_TO_SUBMIT);
            // When
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(localParams);
            // Then
            assertThat(response.getData()).doesNotHaveToString("sdtRequestId");
        }

        @Test
        void shouldAssignFirstPbaNumber_whenInvokedAndBulkClaim() {
            // Given
            CaseData localCaseData = CaseDataBuilder.builder().atStatePendingClaimIssued().build();
            localCaseData.setSdtRequestIdFromSdt("sdtRequestIdFromSdt");
            localCaseData.setTotalClaimAmount(BigDecimal.valueOf(1999));
            when(interestCalculator.calculateBulkInterest(localCaseData)).thenReturn(new BigDecimal(0));
            given(organisationService.findOrganisation(any())).willReturn(Optional.of(bulkOrganisation));
            CallbackParams localParams = callbackParamsOf(localCaseData, ABOUT_TO_SUBMIT);
            // When
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(localParams);
            // Then
            System.out.println(response.getData().get("applicantSolicitor1PbaAccounts"));
            assertThat(response.getData()).extracting("applicantSolicitor1PbaAccounts").asString().contains("12345");
            assertThat(response.getData()).extracting("applicantSolicitor1PbaAccounts").asString().doesNotContain(
                "98765");
        }

        @Test
        void shouldSetCaseCategoryToSpec_whenInvoked() {
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getData())
                .containsEntry("CaseAccessCategory", CaseCategory.SPEC_CLAIM.toString());
        }

        @Test
        void shouldAddPartyIdsToPartyFields_whenInvoked() {
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getData()).extracting("applicant1").hasFieldOrProperty("partyID");
            assertThat(response.getData()).extracting("respondent1").hasFieldOrProperty("partyID");
        }

        @Test
        void shouldPopulateCasenamePublic_whenInvoked() {
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            assertThat(response.getData()).extracting("caseNamePublic").isEqualTo("John Rambo v Sole Trader");
        }

        @Test
        void shouldAddCaseReferenceSubmittedDateAndAllocatedTrack_whenInvoked() {
            // Given
            CallbackParams localParams = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                    CallbackRequest.builder().eventId(CREATE_SERVICE_REQUEST_CLAIM.name()).build())
                .build();

            // When
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(localParams);

            // Then
            assertThat(response.getData())
                .containsEntry("legacyCaseReference", REFERENCE_NUMBER)
                .containsEntry("submittedDate", submittedDate.format(DateTimeFormatter.ISO_DATE_TIME));

        }

        @Test
        void shouldUpdateRespondentAndApplicantWithPartyNameAndPartyTypeDisplayValue_whenInvoked() {
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            Party respondent1 = caseData.getRespondent1();
            Party applicant1 = caseData.getApplicant1();

            assertThat(response.getData())
                .extracting("respondent1")
                .extracting("partyName", "partyTypeDisplayValue")
                .containsExactly(getPartyNameBasedOnType(respondent1), respondent1.getType().getDisplayValue());

            assertThat(response.getData())
                .extracting("applicant1")
                .extracting("partyName", "partyTypeDisplayValue")
                .containsExactly(getPartyNameBasedOnType(applicant1), applicant1.getType().getDisplayValue());
        }

        @Test
        void shouldAssignCaseName1v2_whenCaseIs1v2() {
            // Given
            CaseData localCaseData = CaseDataBuilder.builder().atStateClaimNotified_1v2_andNotifyBothSolicitors().build();
            CallbackParams localParams = callbackParamsOf(V_1, localCaseData, ABOUT_TO_SUBMIT);

            // When
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(localParams);

            // Then
            assertThat(response.getData())
                .containsEntry("caseNameHmctsInternal", "John Rambo v Sole Trader, John Rambo");
            assertThat(response.getData().get("caseManagementCategory")).extracting("value")
                .extracting("code").isEqualTo("Civil");
        }

        @Test
        void shouldAssignCaseName2v1_whenCaseIs2v1() {
            // Given
            CaseData localCaseData = CaseDataBuilder.builder().atStateClaimSubmitted2v1RespondentRegistered().build();
            CallbackParams localParams = callbackParamsOf(V_1, localCaseData, ABOUT_TO_SUBMIT);

            // When
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(localParams);

            // Then
            assertThat(response.getData())
                .containsEntry("caseNameHmctsInternal", "John Rambo, Jason Rambo v Sole Trader");
            assertThat(response.getData().get("caseManagementCategory")).extracting("value")
                .extracting("code").isEqualTo("Civil");
        }

        @Test
        void shouldAssignCaseName1v1_whenCaseIs1v1() {
            // Given
            CaseData localCaseData = CaseDataBuilder.builder().atStateClaimNotified_1v1().build();
            CallbackParams localParams = callbackParamsOf(V_1, localCaseData, ABOUT_TO_SUBMIT);

            // When
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(localParams);

            // Then
            assertThat(response.getData().get("caseNameHmctsInternal"))
                .isEqualTo("John Rambo v Sole Trader");
            assertThat(response.getData().get("caseManagementCategory")).extracting("value")
                .extracting("code").isEqualTo("Civil");
        }

        @Test
        void shouldCopyRespondent1OrgPolicyReferenceForSameRegisteredSolicitorScenario_whenInvoked() {
            Party respondent2 = new Party();
            respondent2.setType(Party.Type.COMPANY);
            respondent2.setCompanyName("Company 3");
            caseData = CaseDataBuilder.builder().atStateClaimIssued1v2AndSameRepresentative()
                .respondent2(respondent2)
                .build();
            caseData.setSpecRespondentCorrespondenceAddressRequired(YES);
            Address correspondenceAddress = new Address();
            correspondenceAddress.setPostCode("Postcode");
            correspondenceAddress.setAddressLine1("Address");
            caseData.setSpecRespondentCorrespondenceAddressdetails(correspondenceAddress);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(
                callbackParamsOf(
                    caseData,
                    ABOUT_TO_SUBMIT
                ));
            var respondent2OrgPolicy = response.getData().get("respondent2OrganisationPolicy");
            var respondentSolicitor2EmailAddress = response.getData().get("respondentSolicitor2EmailAddress");

            assertThat(respondent2OrgPolicy).extracting("OrgPolicyReference").isEqualTo("org1PolicyReference");
            assertThat(respondent2OrgPolicy)
                .extracting("Organisation").extracting("OrganisationID")
                .isEqualTo("org1");
            assertThat(respondentSolicitor2EmailAddress).isEqualTo("respondentsolicitor@example.com");

            assertEquals(
                response.getData().get("specRespondentCorrespondenceAddressRequired"),
                response.getData().get("specRespondent2CorrespondenceAddressRequired")
            );
            assertEquals(
                response.getData().get("specRespondentCorrespondenceAddressdetails"),
                response.getData().get("specRespondent2CorrespondenceAddressdetails")
            );
        }

        @Test
        void shouldCopyRespondent1SolicitorReferenceSameRegisteredSolicitorScenario_whenInvoked() {
            caseData = CaseDataBuilder.builder().atStateClaimIssued1v2AndSameRepresentative()
                .respondentSolicitor1ServiceAddressRequired(YesOrNo.NO)
                .respondentSolicitor1ServiceAddress(null)
                .build();
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(
                callbackParamsOf(
                    V_1,
                    caseData,
                    ABOUT_TO_SUBMIT
                ));
            var respondentSolicitor2Reference = response.getData().get("solicitorReferences");
            var respondentSolicitor2ServiceAddressRequired = response.getData().get(
                "respondentSolicitor2ServiceAddressRequired");
            var respondentSolicitor2ServiceAddress =
                response.getData().get("respondentSolicitor2ServiceAddress");

            assertThat(respondentSolicitor2Reference)
                .extracting("respondentSolicitor2Reference").isEqualTo("6789");
            assertThat(respondentSolicitor2ServiceAddressRequired).isEqualTo("No");
            assertThat(respondentSolicitor2ServiceAddress).isNull();
        }

        @Test
        void shouldUpdateCaseManagementLocation_whenInvoked() {
            // When
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            // Then
            assertThat(response.getData())
                .extracting("caseManagementLocation")
                .extracting("region", "baseLocation")
                .containsExactly("2", "420219");
        }

        @Test
        void shouldAddMissingRespondent1OrgPolicyWithCaseRole_whenInvoked() {
            // Given
            CaseData modifiedCaseData = objMapper.convertValue(params.getCaseData(), CaseData.class);
            modifiedCaseData.setRespondent1OrganisationPolicy(null);
            var callbackParams = params.copy()
                .caseData(modifiedCaseData);

            // When
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(callbackParams);

            // Then
            assertThat(response.getData())
                .extracting("respondent1OrganisationPolicy")
                .extracting("OrgPolicyCaseAssignedRole").isEqualTo("[RESPONDENTSOLICITORONE]");
        }

        @Test
        void shouldAddMissingRespondent2OrgPolicyWithCaseRole_whenInvoked() {
            // Given
            var callbackParams = params.copy()
                .caseData(params.getCaseData());

            // When
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(callbackParams);

            // Then
            assertThat(response.getData())
                .extracting("respondent2OrganisationPolicy")
                .extracting("OrgPolicyCaseAssignedRole").isEqualTo("[RESPONDENTSOLICITORTWO]");
        }

        @Nested
        class IdamEmail {

            @BeforeEach
            void setup() {
                caseData = CaseDataBuilder.builder().atStateClaimDraft().build();
                userId = UUID.randomUUID().toString();

                given(userService.getUserDetails(any()))
                    .willReturn(UserDetails.builder().email(EMAIL).id(userId).build());
            }

            @Test
            void shouldAddIdamEmailToIdamDetails_whenIdamEmailIsCorrect() {
                // Given
                CaseData localCaseData = CaseDataBuilder.builder().atStateClaimDraft().build();
                CorrectEmail correctEmail = new CorrectEmail();
                correctEmail.setEmail(EMAIL);
                correctEmail.setCorrect(YES);
                localCaseData.setApplicantSolicitor1CheckEmail(correctEmail);
                IdamUserDetails idamDetails = new IdamUserDetails();
                idamDetails.setEmail(DIFFERENT_EMAIL);
                localCaseData.setApplicantSolicitor1UserDetails(idamDetails);

                params = callbackParamsOf(V_1, localCaseData, ABOUT_TO_SUBMIT);

                // When
                var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

                // Then
                assertThat(response.getData())
                    .doesNotHaveToString("applicantSolicitor1CheckEmail")
                    .doesNotHaveToString("email");

                assertThat(response.getData())
                    .extracting("applicantSolicitor1UserDetails")
                    .extracting("id", "email")
                    .containsExactly(userId, EMAIL);
            }

            @Test
            void shouldAddDifferentEmailToIdamDetails_whenIdamEmailIsNotCorrect() {
                // Given
                userId = UUID.randomUUID().toString();
                given(userService.getUserDetails(any()))
                    .willReturn(UserDetails.builder().email(EMAIL).id(userId).build());

                CaseData localCaseData = CaseDataBuilder.builder().atStateClaimDraft().build();
                CorrectEmail incorrectEmail = new CorrectEmail();
                incorrectEmail.setEmail(EMAIL);
                incorrectEmail.setCorrect(NO);
                localCaseData.setApplicantSolicitor1CheckEmail(incorrectEmail);
                IdamUserDetails alternateDetails = new IdamUserDetails();
                alternateDetails.setEmail(DIFFERENT_EMAIL);
                localCaseData.setApplicantSolicitor1UserDetails(alternateDetails);

                CallbackParams localParams = callbackParamsOf(V_1, localCaseData, ABOUT_TO_SUBMIT);

                // When
                var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(localParams);

                // Then
                assertThat(response.getData())
                    .doesNotHaveToString("applicantSolicitor1CheckEmail")
                    .doesNotHaveToString("email");

                assertThat(response.getData())
                    .extracting("applicantSolicitor1UserDetails")
                    .extracting("id", "email")
                    .containsExactly(userId, DIFFERENT_EMAIL);
            }
        }

        @Nested
        class ResetStatementOfTruth {

            @Test
            void shouldMoveStatementOfTruthToCorrectFieldAndResetUIField_whenInvoked() {
                // Given
                String name = "John Smith";
                String role = "Solicitor";

                StatementOfTruth statementOfTruth = new StatementOfTruth();
                statementOfTruth.setName(name);
                statementOfTruth.setRole(role);
                CaseData data = objMapper.convertValue(caseData, CaseData.class);
                data.setUiStatementOfTruth(statementOfTruth);

                // When
                var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(
                    callbackParamsOf(
                        V_1,
                        data,
                        ABOUT_TO_SUBMIT
                    ));

                // Then
                assertThat(response.getData())
                    .extracting("applicantSolicitor1ClaimStatementOfTruth")
                    .extracting("name", "role")
                    .containsExactly(name, role);

                assertThat(response.getData())
                    .extracting("uiStatementOfTruth")
                    .doesNotHaveToString("name")
                    .doesNotHaveToString("role");
            }
        }

        @Nested
        class GetAirlineCourtLocation extends LocationRefSampleDataBuilder {

            @BeforeEach
            void mockAirlineEpimsData() {
                given(airlineEpimsService.getEpimsIdForAirline("GULF_AIR"))
                    .willReturn("36791");

                List<LocationRefData> locations = new ArrayList<>();
                locations.add(new LocationRefData().setRegionId("Site Name").setEpimmsId("36791"));
                given(locationRefDataService.getCourtLocationsForDefaultJudgments(any()))
                    .willReturn(locations);
            }

            @Test
            void shouldReturnExpectedCourtLocation_whenAirlineExists() {
                // Given

                DynamicListElement gulfAirElement = new DynamicListElement("GULF_AIR", "Gulf Air");
                DynamicList airlineList = new DynamicList();
                airlineList.setValue(gulfAirElement);
                FlightDelayDetails flightDelayDetails = new FlightDelayDetails();
                flightDelayDetails.setAirlineList(airlineList);
                CaseData localCaseData = CaseDataBuilder.builder().atStateClaimDraft()
                    .isFlightDelayClaim(YES)
                    .flightDelay(flightDelayDetails).build();
                CallbackParams localParams = callbackParamsOf(localCaseData, ABOUT_TO_SUBMIT);

                // When
                var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(localParams);

                // Then
                assertThat(response.getData()).extracting("flightDelayDetails").extracting("flightCourtLocation").extracting(
                        "region")
                    .isEqualTo("Site Name");
            }

            @Test
            void shouldReturnExpectedCourtLocation_whenOtherAirlineSelected() {
                // Given
                DynamicListElement otherElement = new DynamicListElement("OTHER", "OTHER");
                DynamicList otherAirlineList = new DynamicList();
                otherAirlineList.setValue(otherElement);
                FlightDelayDetails otherFlightDelayDetails = new FlightDelayDetails();
                otherFlightDelayDetails.setAirlineList(otherAirlineList);
                CaseData localCaseData = CaseDataBuilder.builder().atStateClaimDraft()
                    .flightDelay(otherFlightDelayDetails).build();
                CallbackParams localParams = callbackParamsOf(localCaseData, ABOUT_TO_SUBMIT);

                given(locationRefDataService.getCourtLocationsForDefaultJudgments(any()))
                    .willReturn(getSampleCourLocationsRefObject());

                // When
                var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(localParams);

                // Then
                assertThat(response.getData()).extracting("flightDelayDetails").doesNotHaveToString(
                    "flightCourtLocation");
            }
        }
    }

    @Nested
    class SubmittedCallback {

        @Nested
        class Respondent1HasLegalRepresentation {

            @Test
            void shouldReturnExpectedSubmittedCallbackResponse_whenRespondent1HasRepresentationAndPBAv3IsOn() {
                // Given
                CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified()
                    .legacyCaseReference("000MC001")
                    .build();
                CallbackParams params = CallbackParamsBuilder.builder().of(SUBMITTED, caseData).version(V_1).request(
                        CallbackRequest.builder().eventId(CREATE_CLAIM_SPEC.name()).build())
                    .build();

                // When
                SubmittedCallbackResponse response = (SubmittedCallbackResponse) handler.handle(params);

                String body = format(
                    SPEC_CONFIRMATION_SUMMARY_PBA_V3,
                    format("/cases/case-details/%s#Service%%20Request", CASE_ID)
                ) + exitSurveyContentService.applicantSurvey();

                // Then
                assertThat(response).usingRecursiveComparison().isEqualTo(
                    SubmittedCallbackResponse.builder()
                        .confirmationHeader(format("# Please now pay your claim fee%n# using the link below"
                        ))
                        .confirmationBody(body)
                        .build());
            }

            @Test
            void shouldReturnExpectedSubmittedCallbackResponse_whenRespondent1HasRepresentationAndPBAv3IsOnAndEventIdMissing() {
                // Given
                CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified()
                    .legacyCaseReference("000MC001")
                    .build();
                CallbackParams params = callbackParamsOf(V_1, caseData, SUBMITTED);

                // When
                SubmittedCallbackResponse response = (SubmittedCallbackResponse) handler.handle(params);

                String body = format(
                    CONFIRMATION_SUMMARY_PBA_V3,
                    format("/cases/case-details/%s#Service%%20Request", CASE_ID)
                ) + exitSurveyContentService.applicantSurvey();

                // Then
                assertThat(response).usingRecursiveComparison().isEqualTo(
                    SubmittedCallbackResponse.builder()
                        .confirmationHeader(format(
                            "# Please now pay your claim fee%n# using the link below"
                        ))
                        .confirmationBody(body)
                        .build());
            }
        }

        @Test
        void shouldReturnExpectedConfirmationPageForPBAV3AndNotRegisteredOrg() {
            // Given
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified()
                .respondent1Represented(YES)
                .respondent1OrgRegistered(NO)
                .legacyCaseReference("000MC001")
                .build();
            CallbackParams params = CallbackParamsBuilder.builder().of(SUBMITTED, caseData).request(
                    CallbackRequest.builder().eventId(CREATE_CLAIM_SPEC.name()).build())
                .build();
            // When
            SubmittedCallbackResponse response = (SubmittedCallbackResponse) handler.handle(params);

            // Then
            assertThat(response).usingRecursiveComparison().isEqualTo(
                SubmittedCallbackResponse.builder()
                    .confirmationHeader(format("# Please now pay your claim fee%n# using the link below"))
                    .confirmationBody(
                        format(
                            SPEC_LIP_CONFIRMATION_BODY_PBAV3,
                            format("/cases/case-details/%s#Service%%20Request", caseData.getCcdCaseReference()),
                            format("/cases/case-details/%s#CaseDocuments", CASE_ID),
                            responsePackLink,
                            n9aLink,
                            n9bLink,
                            n215Link
                        ) + exitSurveyContentService.applicantSurvey())
                    .build());
        }
    }

    @Test
    void shouldReturnPbaHeader_whenCaseIsMatched() {
        // Given
        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setRespondent1Represented(NO);
        caseData.setAddRespondent2(NO);
        caseData.setAddApplicant2(NO);
        caseData.setLegacyCaseReference("000MC001");
        caseData.setCcdCaseReference(123L);

        CallbackParams params = new CallbackParams()
            .caseData(caseData)
            .request(CallbackRequest.builder().eventId("CREATE_CLAIM_SPEC").build());

        // When
        SubmittedCallbackResponse response = handler.buildConfirmation(params);

        // Then
        Assertions.assertTrue(response.getConfirmationHeader().contains("Please now pay your claim fee"));
        Assertions.assertTrue(response.getConfirmationBody().contains("Pay your claim fee"));
    }

    @Test
    void shouldReturnPbaHeader_whenRespondentsAreRepresentedAndRegistered() {
        // Given
        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setRespondent1Represented(YES);
        caseData.setRespondent1OrgRegistered(YES);
        caseData.setRespondent2Represented(YES);
        caseData.setRespondent2OrgRegistered(YES);
        caseData.setLegacyCaseReference("000MC001");
        caseData.setCcdCaseReference(123L);

        CallbackParams params = new CallbackParams()
            .caseData(caseData)
            .request(CallbackRequest.builder().eventId("CREATE_CLAIM_SPEC").build());

        // When
        SubmittedCallbackResponse response = handler.buildConfirmation(params);

        // Then
        Assertions.assertTrue(response.getConfirmationBody().contains("Pay your claim fee"));
    }

    @Test
    void shouldReturnLipConfirmationBody_whenNotMatchedAndNotRepresented() {
        // Given
        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setRespondent1Represented(NO);
        caseData.setAddRespondent2(YES);
        caseData.setAddApplicant2(YES);
        caseData.setLegacyCaseReference("000MC001");
        caseData.setCcdCaseReference(123L);

        CallbackParams params = new CallbackParams()
            .caseData(caseData)
            .request(CallbackRequest.builder().eventId("CREATE_CLAIM_SPEC").build());

        // When
        SubmittedCallbackResponse response = handler.buildConfirmation(params);

        // Then
        Assertions.assertTrue(response.getConfirmationBody().contains(
            "Your claim will not be issued until payment is confirmed"));
        Assertions.assertTrue(response.getConfirmationBody().contains("sealed claim form"));
    }

    @Test
    void shouldReturnOfflineHeader_whenNotMatchedAndNotRepresented() {
        // Given
        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setRespondent1Represented(NO);
        caseData.setRespondent1OrgRegistered(NO);
        caseData.setRespondent2Represented(NO);
        caseData.setRespondent2OrgRegistered(YES);
        caseData.setAddRespondent2(YES);
        caseData.setAddApplicant2(YES);
        caseData.setLegacyCaseReference("000MC001");
        caseData.setCcdCaseReference(123L);

        CallbackParams params = new CallbackParams()
            .caseData(caseData)
            .request(CallbackRequest.builder().eventId(null).build());

        // When
        SubmittedCallbackResponse response = handler.buildConfirmation(params);

        // Then
        System.out.print(response.getConfirmationHeader());
        Assertions.assertTrue(response.getConfirmationHeader().contains("Your claim has been received and will progress offline"));
        Assertions.assertTrue(response.getConfirmationHeader().contains("000MC001"));
    }

    @Test
    void handleEventsReturnsTheExpectedCallbackEvent() {
        assertThat(handler.handledEvents()).contains(CREATE_CLAIM_SPEC);
    }
}
