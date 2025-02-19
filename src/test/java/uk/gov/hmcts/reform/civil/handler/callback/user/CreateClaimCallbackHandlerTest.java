package uk.gov.hmcts.reform.civil.handler.callback.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.autoconfigure.validation.ValidationAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.ccd.model.OrganisationPolicy;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.config.ClaimUrlsConfiguration;
import uk.gov.hmcts.reform.civil.config.ExitSurveyConfiguration;
import uk.gov.hmcts.reform.civil.config.MockDatabaseConfiguration;
import uk.gov.hmcts.reform.civil.config.ToggleConfiguration;
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;
import uk.gov.hmcts.reform.civil.enums.CaseCategory;
import uk.gov.hmcts.reform.civil.enums.ClaimType;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.Address;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.ClaimValue;
import uk.gov.hmcts.reform.civil.model.CorrectEmail;
import uk.gov.hmcts.reform.civil.model.CourtLocation;
import uk.gov.hmcts.reform.civil.model.DocumentWithRegex;
import uk.gov.hmcts.reform.civil.model.Fee;
import uk.gov.hmcts.reform.civil.model.IdamUserDetails;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.ServedDocumentFiles;
import uk.gov.hmcts.reform.civil.model.StatementOfTruth;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.common.DynamicListElement;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.prd.model.Organisation;
import uk.gov.hmcts.reform.civil.referencedata.model.LocationRefData;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.sampledata.PartyBuilder;
import uk.gov.hmcts.reform.civil.service.CoreCaseEventDataService;
import uk.gov.hmcts.reform.civil.service.DeadlinesCalculator;
import uk.gov.hmcts.reform.civil.service.ExitSurveyContentService;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.FeesService;
import uk.gov.hmcts.reform.civil.service.OrganisationService;
import uk.gov.hmcts.reform.civil.service.Time;
import uk.gov.hmcts.reform.civil.service.flowstate.SimpleStateFlowEngine;
import uk.gov.hmcts.reform.civil.service.flowstate.TransitionsTestConfiguration;
import uk.gov.hmcts.reform.civil.service.referencedata.LocationReferenceDataService;
import uk.gov.hmcts.reform.civil.stateflow.simplegrammar.SimpleStateFlowBuilder;
import uk.gov.hmcts.reform.civil.utils.AssignCategoryId;
import uk.gov.hmcts.reform.civil.utils.CaseFlagsInitialiser;
import uk.gov.hmcts.reform.civil.utils.CourtLocationUtils;
import uk.gov.hmcts.reform.civil.utils.InterestCalculator;
import uk.gov.hmcts.reform.civil.validation.DateOfBirthValidator;
import uk.gov.hmcts.reform.civil.validation.OrgPolicyValidator;
import uk.gov.hmcts.reform.civil.validation.PartyValidator;
import uk.gov.hmcts.reform.civil.validation.PostcodeValidator;
import uk.gov.hmcts.reform.civil.validation.ValidateEmailService;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import static java.lang.String.format;
import static java.time.LocalDate.now;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.MID;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.SUBMITTED;
import static uk.gov.hmcts.reform.civil.callback.CallbackVersion.V_1;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CREATE_CLAIM;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CREATE_SERVICE_REQUEST_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.AllocatedTrack.INTERMEDIATE_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.AllocatedTrack.MULTI_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.handler.callback.user.CreateClaimCallbackHandler.CONFIRMATION_BODY_LIP_COS;
import static uk.gov.hmcts.reform.civil.handler.callback.user.CreateClaimCallbackHandler.CONFIRMATION_SUMMARY;
import static uk.gov.hmcts.reform.civil.model.common.DynamicList.fromList;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.civil.utils.PartyUtils.getPartyNameBasedOnType;

@SpringBootTest(classes = {
    CreateClaimCallbackHandler.class,
    CaseDetailsConverter.class,
    ClaimUrlsConfiguration.class,
    CourtLocationUtils.class,
    DateOfBirthValidator.class,
    DeadlinesCalculator.class,
    ExitSurveyConfiguration.class,
    ExitSurveyContentService.class,
    InterestCalculator.class,
    JacksonAutoConfiguration.class,
    LocationReferenceDataService.class,
    MockDatabaseConfiguration.class,
    OrgPolicyValidator.class,
    SimpleStateFlowEngine.class,
    SimpleStateFlowBuilder.class,
    TransitionsTestConfiguration.class,
    PostcodeValidator.class,
    ValidationAutoConfiguration.class,
    ValidateEmailService.class,
    OrganisationService.class,
    AssignCategoryId.class,
    PartyValidator.class,
    ToggleConfiguration.class},
    properties = {"reference.database.enabled=false"})
class CreateClaimCallbackHandlerTest extends BaseCallbackHandlerTest {

    public static final String REFERENCE_NUMBER = "000DC001";

    @Autowired
    private ObjectMapper objMapper;

    @MockBean
    private Time time;

    @MockBean
    private FeesService feesService;

    @MockBean
    private OrganisationService organisationService;

    @MockBean
    private FeatureToggleService featureToggleService;

    @Autowired
    private ValidateEmailService validateEmailService;

    @Autowired
    private CreateClaimCallbackHandler handler;

    @Autowired
    private ExitSurveyContentService exitSurveyContentService;

    @MockBean
    private PostcodeValidator postcodeValidator;

    @MockBean
    private DeadlinesCalculator deadlinesCalculator;

    @MockBean
    private LocationReferenceDataService locationRefDataService;

    @MockBean
    private CourtLocationUtils courtLocationUtility;

    @MockBean
    private CaseFlagsInitialiser caseFlagInitialiser;

    @MockBean
    CoreCaseEventDataService coreCaseEventDataService;

    @Autowired
    private AssignCategoryId assignCategoryId;

    @Autowired
    private PartyValidator partyValidator;

    @Value("${civil.response-pack-url}")
    private String responsePackLink;

    @MockBean
    private ToggleConfiguration toggleConfiguration;

    @Test
    void handleEventsReturnsTheExpectedCallbackEvent() {
        assertThat(handler.handledEvents()).contains(CREATE_CLAIM);
    }

    @Nested
    class AboutToStartCallback {

        @Test
        void shouldReturnNoError_WhenAboutToStartIsInvoked() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStatePendingClaimIssued()
                .build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_START);

            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            assertThat(response.getErrors()).isNull();
        }
    }

    @Nested
    class MidEventApplicantCallback {

        private static final String PAGE_ID = "applicant";

        @BeforeEach
        void setup() {
            when(featureToggleService.isJudgmentOnlineLive()).thenReturn(false);
        }

        @Test
        void shouldReturnError_whenIndividualDateOfBirthIsInTheFuture() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDraft()
                .applicant1(PartyBuilder.builder().individual()
                    .individualDateOfBirth(now().plusDays(1))
                    .build())
                .build();
            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).containsExactly("The date entered cannot be in the future");
        }

        @Test
        void shouldReturnError_whenSoleTraderDateOfBirthIsInTheFuture() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDraft()
                .applicant1(PartyBuilder.builder().individual()
                    .soleTraderDateOfBirth(now().plusDays(1))
                    .build())
                .build();
            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).containsExactly("The date entered cannot be in the future");
        }

        @Test
        void shouldReturnNoError_whenIndividualDateOfBirthIsInThePast() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDraft()
                .applicant1(PartyBuilder.builder().individual()
                    .individualDateOfBirth(now().minusDays(1))
                    .build())
                .build();
            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).isEmpty();
        }

        @Test
        void shouldReturnNoError_whenSoleTraderDateOfBirthIsInThePast() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDraft()
                .applicant1(PartyBuilder.builder().individual()
                    .soleTraderDateOfBirth(now().minusDays(1))
                    .build())
                .build();
            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).isEmpty();
        }

        @Test
        void shouldReturnError_whenIndividualAddressLengthGreaterThanMaxLimit() {
            when(featureToggleService.isJudgmentOnlineLive()).thenReturn(true);
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDraft()
                .applicant1(Party.builder()
                                .partyName("Party").type(Party.Type.INDIVIDUAL)
                                .individualDateOfBirth(now().minusDays(1))
                                .primaryAddress(Address.builder()
                                                    .addressLine1("Line 1 test again for more than 35 characters")
                                                    .addressLine2("Line 2 test again for more than 35 characters")
                                                    .addressLine3("Line 3 test again for more than 35 characters")
                                                    .county("Line 4 test again for more than 35 characters")
                                                    .postCode("PostCode more than 8 characters")
                                                    .postTown("Line 6 test again for more than 35 characters").build())
                                .build()).build();

            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).isNotEmpty();
            assertThat(response.getErrors()).hasSize(6);
        }

        @Test
        void shouldReturnError_whenOrgNameExceedsMaxLength() {
            // Given
            when(featureToggleService.isJudgmentOnlineLive()).thenReturn(true);

            CaseData caseData = CaseDataBuilder.builder().atStateClaimDraft()
                .applicant1(Party.builder()
                                .type(Party.Type.ORGANISATION)
                                .organisationName("This is very long name exceeds 70 characters "
                                                      + " to throw error for max length allowed")
                                .build())
                .build();
            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);

            // When
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            // Then
            assertThat(response.getErrors()).isNotEmpty();
            assertThat(response.getErrors()).hasSize(1);
        }

        @Test
        void shouldNotReturnError_whenOrgNameExceedsMaxlength_And_flagOff() {
            // Given
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDraft()
                .applicant1(Party.builder()
                                .type(Party.Type.ORGANISATION)
                                .organisationName("This is very long name exceeds 70 characters "
                                                      + " to throw error for max length allowed")
                                .build())
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

        @BeforeEach
        void setup() {
            when(featureToggleService.isJudgmentOnlineLive()).thenReturn(false);
        }

        @Test
        void shouldReturnError_whenIndividualDateOfBirthIsInTheFuture() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDraft()
                .applicant2(PartyBuilder.builder().individual()
                    .individualDateOfBirth(now().plusDays(1))
                    .build())
                .build();
            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).containsExactly("The date entered cannot be in the future");
        }

        @Test
        void shouldReturnError_whenSoleTraderDateOfBirthIsInTheFuture() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDraft()
                .applicant2(PartyBuilder.builder().individual()
                    .soleTraderDateOfBirth(now().plusDays(1))
                    .build())
                .build();
            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).containsExactly("The date entered cannot be in the future");
        }

        @Test
        void shouldReturnNoError_whenIndividualDateOfBirthIsInThePast() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDraft()
                .applicant2(PartyBuilder.builder().individual()
                    .individualDateOfBirth(now().minusDays(1))
                    .build())
                .build();
            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).isEmpty();
        }

        @Test
        void shouldReturnNoError_whenSoleTraderDateOfBirthIsInThePast() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDraft()
                .applicant2(PartyBuilder.builder().individual()
                    .soleTraderDateOfBirth(now().minusDays(1))
                    .build())
                .build();
            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).isEmpty();
        }

        @Test
        void shouldReturnError_whenSoleTraderPostCodeLengthExceedsMaxLimit() {
            when(featureToggleService.isJudgmentOnlineLive()).thenReturn(true);
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDraft()
                .applicant2(Party.builder()
                                .soleTraderTitle("Mr")
                                .soleTraderFirstName("Jacob")
                                .soleTraderLastName("Martin").type(Party.Type.SOLE_TRADER)
                                .soleTraderDateOfBirth(now().minusDays(1))
                                .primaryAddress(Address.builder()
                                                    .addressLine1("Triple street")
                                                    .postCode("00000PCode")
                                                    .build())
                                .build()).build();

            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).isNotEmpty();
            assertThat(response.getErrors()).hasSize(1);
        }

        @Test
        void shouldReturnError_whenCompanyAddressExceedsMaxLength() {
            when(featureToggleService.isJudgmentOnlineLive()).thenReturn(true);
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDraft()
                .applicant2(Party.builder()
                                .companyName("MR companyName is very long nam exceeds 70 characters to throw" +
                                                 " error for max length allowed").type(Party.Type.COMPANY)
                                .primaryAddress(Address.builder()
                                                    .addressLine1("Line 1 test again for more than 35 characters")
                                                    .addressLine2("Line 2 test again for more than 35 characters")
                                                    .addressLine3("Line 3 test again for more than 35 characters")
                                                    .county("Line 4 test again for more than 35 characters")
                                                    .postCode("PostCode more than 8 characters")
                                                    .postTown("Line 6 test again for more than 35 characters").build())
                                .build()).build();

            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).isNotEmpty();
            assertThat(response.getErrors()).hasSize(7);
        }

        @Test
        void shouldNotReturnError_whenOrgNameExceedsMaxlength_And_flagOff() {
            // Given
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDraft()
                .applicant2(Party.builder()
                                .type(Party.Type.ORGANISATION)
                                .organisationName("This is very long name exceeds 70 characters "
                                                      + " to throw error for max length allowed")
                                .build())
                .build();
            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);

            // When
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            // Then
            assertThat(response.getErrors()).isEmpty();
        }
    }

    @Nested
    class MidEventDefendant1Callback {

        private static final String PAGE_ID = "respondent1";

        @BeforeEach
        void setUp() {
            when(featureToggleService.isJudgmentOnlineLive()).thenReturn(true);
        }

        @Test
        void shouldReturnError_whenOrganisationAddressLongerThanMaxLength() {
            // Given
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDraft()
                .respondent1(Party.builder().type(Party.Type.ORGANISATION)
                                 .primaryAddress(Address.builder()
                                                     .addressLine1("Line 1 test again for more than 35 characters")
                                                     .addressLine2("Line 1 test again for more than 35 characters")
                                                     .addressLine3("Line 1 test again for more than 35 characters")
                                                     .county("Line 1 test again for more than 35 characters")
                                                     .postCode("postcode test for more than 8 characters")
                                                     .postTown("Line 1 test again for more than 35 characters").build())
                                 .build())
                .build();
            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);

            // When
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            // Then
            assertThat(response.getErrors()).isNotEmpty();
            assertThat(response.getErrors()).hasSize(6);
        }

        @Test
        void shouldReturnError_whenCompanyNameExceedsMaxLength() {
            // Given
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDraft()
                .respondent1(Party.builder()
                                 .type(Party.Type.COMPANY)
                                 .primaryAddress(Address.builder().addressLine1("TEST").build())
                                 .companyName("MR This is very long nam exceeds 70 characters to throw"
                                                  + " error for max length allowed").build())
                .build();
            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);

            // When
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            // Then
            assertThat(response.getErrors()).isNotEmpty();
        }

        @Test
        void shouldReturnError_whenIndividualNameExceedsMaxLength() {
            // Given
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDraft()
                .respondent1(Party.builder()
                                 .type(Party.Type.INDIVIDUAL)
                                 .individualFirstName("This is very long name")
                                 .individualTitle("MR")
                                 .individualLastName("exceeds 70 characters to throw error for max length allowed")
                                 .build())
                .build();
            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);

            // When
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            // Then
            assertThat(response.getErrors()).isNotEmpty();
        }

        @Test
        void shouldReturnError_whenSoleTraderNameExceedsMaxLength() {
            // Given
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDraft()
                .respondent1(Party.builder()
                                 .type(Party.Type.SOLE_TRADER)
                                 .soleTraderFirstName("This is very long name")
                                 .soleTraderTitle("MR")
                                 .soleTraderLastName("exceeds 70 characters to throw error for max length allowed")
                                 .build())
                .build();
            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);

            // When
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            // Then
            assertThat(response.getErrors()).isNotEmpty();
        }

        @Test
        void shouldReturnError_whenOrgNameExceedsMaxLength() {
            // Given
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDraft()
                .respondent1(Party.builder()
                                 .type(Party.Type.ORGANISATION)
                                 .organisationName("This is very long name exceeds 70 characters "
                                                       + " to throw error for max length allowed")
                                 .build())
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
            when(featureToggleService.isJudgmentOnlineLive()).thenReturn(false);

            CaseData caseData = CaseDataBuilder.builder().atStateClaimDraft()
                .respondent1(Party.builder()
                                 .type(Party.Type.ORGANISATION)
                                 .organisationName("This is very long name exceeds 70 characters "
                                                       + " to throw error for max length allowed")
                                 .build())
                .build();
            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);

            // When
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            // Then
            assertThat(response.getErrors()).isEmpty();
        }
    }

    @Nested
    class MidEventDefendant2Callback {

        private static final String PAGE_ID = "respondent2";

        @BeforeEach
        void setUp() {
            when(featureToggleService.isJudgmentOnlineLive()).thenReturn(true);
        }

        @Test
        void shouldReturnError_whenAddressLongerThanMaxLength() {
            // Given
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDraft()
                .respondent2(Party.builder().type(Party.Type.ORGANISATION)
                                 .primaryAddress(Address.builder()
                                                     .addressLine1("Line 1 test again for more than 35 characters")
                                                     .addressLine2("Line 1 test again for more than 35 characters")
                                                     .addressLine3("Line 1 test again for more than 35 characters")
                                                     .county("Line 1 test again for more than 35 characters")
                                                     .postCode("postcode test for more than 8 characters")
                                                     .postTown("Line 1 test again for more than 35 characters").build())
                                 .build())
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
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDraft()
                .respondent2(Party.builder()
                                 .type(Party.Type.COMPANY)
                                 .primaryAddress(Address.builder().addressLine1("TEST").build())
                                 .companyName("MR This is very long nam exceeds 70 characters to throw"
                                                  + " error for max length allowed").build())
                .build();
            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);

            // When
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            // Then
            assertThat(response.getErrors()).isNotEmpty();
        }

        @Test
        void shouldReturnError_when_individual_name_exceeds_max_length() {
            // Given
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDraft()
                .respondent2(Party.builder()
                                 .type(Party.Type.INDIVIDUAL)
                                 .individualFirstName("This is very long name")
                                 .individualTitle("MR")
                                 .individualLastName("exceeds 70 characters to throw error for max length allowed")
                                 .build())
                .build();
            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);

            // When
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            // Then
            assertThat(response.getErrors()).isNotEmpty();
        }

        @Test
        void shouldReturnError_when_sole_trader_name_exceeds_max_length() {
            // Given
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDraft()
                .respondent2(Party.builder()
                                 .type(Party.Type.SOLE_TRADER)
                                 .soleTraderFirstName("This is very long name")
                                 .soleTraderTitle("MR")
                                 .soleTraderLastName("exceeds 70 characters to throw error for max length allowed")
                                 .build())
                .build();
            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);

            // When
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            // Then
            assertThat(response.getErrors()).isNotEmpty();
        }

        @Test
        void shouldReturnError_when_org_name_exceeds_max_length() {
            // Given
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDraft()
                .respondent2(Party.builder()
                                 .type(Party.Type.ORGANISATION)
                                 .organisationName("This is very long name exceeds 70 characters "
                                                       + " to throw error for max length allowed")
                                 .build())
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
            when(featureToggleService.isJudgmentOnlineLive()).thenReturn(false);

            CaseData caseData = CaseDataBuilder.builder().atStateClaimDraft()
                .respondent1(Party.builder()
                                 .type(Party.Type.ORGANISATION)
                                 .organisationName("This is very long name exceeds 70 characters "
                                                       + " to throw error for max length allowed")
                                 .build())
                .build();
            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);

            // When
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            // Then
            assertThat(response.getErrors()).isEmpty();
        }

    }

    @Nested
    class MidEventParticularsOfClaimCallback {

        private final String pageId = "particulars-of-claim";
        private final CaseData.CaseDataBuilder caseDataBuilder =
            CaseDataBuilder.builder().atStateClaimDraft().build().toBuilder();

        @Test
        void shouldReturnErrors_whenNoDocuments() {
            CaseData caseData = caseDataBuilder.build();
            CallbackParams params = callbackParamsOf(caseData, MID, pageId);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).containsOnly("You must add Particulars of claim details");
        }

        @Test
        void shouldReturnErrors_whenParticularsOfClaimFieldsAreInErrorState() {
            CaseData caseData = caseDataBuilder.servedDocumentFiles(ServedDocumentFiles.builder().build()).build();
            CallbackParams params = callbackParamsOf(caseData, MID, pageId);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).containsOnly("You must add Particulars of claim details");
        }

        @Test
        void shouldReturnNoErrors_whenParticularOfClaimsFieldsAreValid() {
            CaseData caseData = caseDataBuilder.servedDocumentFiles(ServedDocumentFiles.builder()
                .particularsOfClaimText("Some string")
                .build()).build();
            CallbackParams params = callbackParamsOf(caseData, MID, pageId);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).isEmpty();
        }

    }

    @Nested
    class MidEventFeeCallback {

        private final String pageId = "fee";
        private final Fee feeData = Fee.builder()
            .code("CODE")
            .calculatedAmountInPence(BigDecimal.valueOf(100))
            .build();
        private final Organisation organisation = Organisation.builder()
            .paymentAccount(List.of("12345", "98765"))
            .build();
        private final ObjectMapper mapper = new ObjectMapper();

        @BeforeEach
        void setup() {
            given(feesService.getFeeDataByClaimValue(any())).willReturn(feeData);
            given(toggleConfiguration.getFeatureToggle()).willReturn("WA 4");
        }

        @Test
        void shouldCalculateClaimFeeAndAddPbaNumbers_whenCalledAndOrgExistsInPrd() {
            given(organisationService.findOrganisation(any())).willReturn(Optional.of(organisation));

            CaseData caseData = CaseDataBuilder.builder().atStateClaimDraft().build();
            CallbackParams params = callbackParamsOf(caseData, MID, pageId);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

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
            given(organisationService.findOrganisation(any())).willReturn(Optional.empty());

            CaseData caseData = CaseDataBuilder.builder().atStateClaimDraft().build();
            CallbackParams params = callbackParamsOf(caseData, MID, pageId);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

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

            assertThat(getDynamicList(response))
                .isEqualTo(DynamicList.builder().value(DynamicListElement.EMPTY).build());
        }

        @Test
        void shouldSetPBAv3FlagOn_whenPBAv3IsActivated() {
            // Given
            given(organisationService.findOrganisation(any())).willReturn(Optional.empty());
            // When
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDraft().build();
            CallbackParams params = callbackParamsOf(caseData, MID, pageId);
            // Then
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            assertThat(response.getData())
                .extracting("paymentTypePBA").isEqualTo("PBAv3");
        }

        private DynamicList getDynamicList(AboutToStartOrSubmitCallbackResponse response) {
            return mapper.convertValue(response.getData().get("applicantSolicitor1PbaAccounts"), DynamicList.class);
        }
    }

    @Nested
    class MidEventStartClaimCallback {

        private static final String PAGE_ID = "start-claim";

        @BeforeEach
        void setup() {
            given(toggleConfiguration.getFeatureToggle()).willReturn("WA 4");
        }

        @Test
        void shouldAddClaimStartedFlagToData_whenInvoked() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDraft().build();
            CallbackParams params = callbackParamsOf(null, caseData, MID, PAGE_ID);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getData())
                .extracting("claimStarted")
                .isEqualTo("Yes");
        }

        @Nested
        class CourtLocation {

            @Test
            void shouldHandleCourtLocationData() {
                when(courtLocationUtility.getLocationsFromList(any()))
                    .thenReturn(fromList(List.of("Site 1 - Lane 1 - 123", "Site 2 - Lane 2 - 124")));

                CaseData caseData = CaseDataBuilder.builder()
                    .atStateClaimDetailsNotified()
                    .build();

                CallbackParams callbackParams = callbackParamsOf(caseData, MID, PAGE_ID);
                var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(callbackParams);

                DynamicList dynamicList = getDynamicList(response);

                List<String> courtlist = dynamicList.getListItems().stream()
                    .map(DynamicListElement::getLabel)
                    .toList();

                assertThat(courtlist).containsOnly("Site 1 - Lane 1 - 123", "Site 2 - Lane 2 - 124");
            }

            private DynamicList getDynamicList(AboutToStartOrSubmitCallbackResponse response) {
                CaseData responseCaseData = objMapper.convertValue(response.getData(), CaseData.class);
                System.out.println(responseCaseData);
                return responseCaseData.getCourtLocation().getApplicantPreferredCourtLocationList();
            }
        }
    }

    @Nested
    class MidEventGetIdamEmailCallback {

        private static final String PAGE_ID = "idam-email";

        @Test
        void shouldAddEmailLabelToData_whenInvoked() {
            String userId = UUID.randomUUID().toString();
            String email = "example@email.com";

            given(userService.getUserDetails(any()))
                .willReturn(UserDetails.builder().email(email).id(userId).build());

            CaseData caseData = CaseDataBuilder.builder().atStateClaimDraft().build();
            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getData())
                .extracting("applicantSolicitor1CheckEmail")
                .extracting("email")
                .isEqualTo(email);
        }

        @Test
        void shouldRemoveExistingEmail_whenOneHasAlreadyBeenEntered() {
            String userId = UUID.randomUUID().toString();
            String email = "example@email.com";

            given(userService.getUserDetails(any()))
                .willReturn(UserDetails.builder().email(email).id(userId).build());

            CaseData caseData = CaseDataBuilder.builder().atStateClaimDraft().build().toBuilder()
                .applicantSolicitor1UserDetails(IdamUserDetails.builder()
                    .email("email@example.com")
                    .build())
                .build();
            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

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

            CaseData caseData = CaseDataBuilder.builder().atStateClaimDraft()
                .applicant1OrganisationPolicy(null)
                .build();
            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).containsExactly("No Organisation selected");
        }

        @Test
        void shouldReturnError_whenOrganisationIsNull() {

            CaseData caseData = CaseDataBuilder.builder().atStateClaimDraft()
                .applicant1OrganisationPolicy(OrganisationPolicy.builder().organisation(null).build())
                .build();
            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).containsExactly("No Organisation selected");
        }

        @Test
        void shouldReturnError_whenOrganisationIdIsNull() {

            uk.gov.hmcts.reform.ccd.model.Organisation organisation
                = uk.gov.hmcts.reform.ccd.model.Organisation.builder()
                .organisationID(null)
                .build();

            CaseData caseData = CaseDataBuilder.builder().atStateClaimDraft()
                .applicant1OrganisationPolicy(OrganisationPolicy.builder().organisation(organisation).build())
                .build();
            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).containsExactly("No Organisation selected");
        }

        @Test
        void shouldBeSuccessful_whenValid() {

            uk.gov.hmcts.reform.ccd.model.Organisation organisation
                = uk.gov.hmcts.reform.ccd.model.Organisation.builder()
                .organisationID("orgId")
                .build();

            CaseData caseData = CaseDataBuilder.builder().atStateClaimDraft()
                .applicant1OrganisationPolicy(OrganisationPolicy.builder().organisation(organisation).build())
                .build();
            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).isEmpty();
        }

    }

    @Nested
    class MidEventRespondent1OrgPolicyCallback {

        private static final String PAGE_ID = "repOrgPolicy";

        @Test
        void shouldReturnError_whenOrganisationPolicyIsNull() {

            CaseData caseData = CaseDataBuilder.builder().atStateClaimDraft()
                .respondent1OrganisationPolicy(null)
                .respondent1OrgRegistered(YES)
                .build();
            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).containsExactly("No Organisation selected");
        }

        @Test
        void shouldReturnError_whenOrganisationIsNull() {

            CaseData caseData = CaseDataBuilder.builder().atStateClaimDraft()
                .respondent1OrganisationPolicy(OrganisationPolicy.builder().organisation(null).build())
                .respondent1OrgRegistered(YES)
                .build();
            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).containsExactly("No Organisation selected");
        }

        @Test
        void shouldReturnError_whenOrganisationIdIsNull() {

            uk.gov.hmcts.reform.ccd.model.Organisation organisation
                = uk.gov.hmcts.reform.ccd.model.Organisation.builder()
                .organisationID(null)
                .build();

            CaseData caseData = CaseDataBuilder.builder().atStateClaimDraft()
                .respondent1OrganisationPolicy(OrganisationPolicy.builder().organisation(organisation).build())
                .respondent1OrgRegistered(YES)
                .build();
            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).containsExactly("No Organisation selected");
        }

        @Test
        void shouldBeSuccessful_whenValid() {

            uk.gov.hmcts.reform.ccd.model.Organisation organisation
                = uk.gov.hmcts.reform.ccd.model.Organisation.builder()
                .organisationID("orgId")
                .build();

            CaseData caseData = CaseDataBuilder.builder().atStateClaimDraft()
                .respondent1OrganisationPolicy(OrganisationPolicy.builder().organisation(organisation).build())
                .respondent1OrgRegistered(YES)
                .build();
            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).isEmpty();
        }

        @Test
        void shouldReturnError_whenBothSolicitorOrganisationsAreSame1v1() {

            uk.gov.hmcts.reform.ccd.model.Organisation organisation
                = uk.gov.hmcts.reform.ccd.model.Organisation.builder()
                .organisationID("orgId")
                .build();

            CaseData caseData = CaseDataBuilder.builder().atStateClaimDraft()
                .applicant1OrganisationPolicy(OrganisationPolicy.builder().organisation(organisation).build())
                .respondent1OrganisationPolicy(OrganisationPolicy.builder().organisation(organisation).build())
                .build();
            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).containsExactly(
                "The legal representative details for the claimant and defendant are the same.  "
                    + "Please amend accordingly.");
        }

        @Test
        void shouldReturnError_whenApplicantAndRespondent1SolicitorOrganisationsAreSame2v1() {

            uk.gov.hmcts.reform.ccd.model.Organisation organisation
                = uk.gov.hmcts.reform.ccd.model.Organisation.builder()
                .organisationID("orgId")
                .build();

            CaseData caseData = CaseDataBuilder.builder().atStateClaimDraft()
                .multiPartyClaimTwoApplicants()
                .applicant1OrganisationPolicy(OrganisationPolicy.builder().organisation(organisation).build())
                .respondent1OrganisationPolicy(OrganisationPolicy.builder().organisation(organisation).build())
                .build();
            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).containsExactly(
                "The legal representative details for the claimant and defendant are the same.  "
                    + "Please amend accordingly.");
        }
    }

    @Nested
    class MidEventRespondent2OrgPolicyCallback {

        private static final String PAGE_ID = "rep2OrgPolicy";

        @Test
        void shouldReturnError_whenOrganisationPolicyIsNull() {

            CaseData caseData = CaseDataBuilder.builder().atStateClaimDraft()
                .respondent2OrganisationPolicy(null)
                .respondent2OrgRegistered(YES)
                .build();
            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).containsExactly("No Organisation selected");
        }

        @Test
        void shouldReturnError_whenOrganisationIsNull() {

            CaseData caseData = CaseDataBuilder.builder().atStateClaimDraft()
                .respondent2OrganisationPolicy(OrganisationPolicy.builder().organisation(null).build())
                .respondent2OrgRegistered(YES)
                .build();
            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).containsExactly("No Organisation selected");
        }

        @Test
        void shouldReturnError_whenOrganisationIdIsNull() {
            uk.gov.hmcts.reform.ccd.model.Organisation organisation
                = uk.gov.hmcts.reform.ccd.model.Organisation.builder()
                .organisationID(null)
                .build();

            CaseData caseData = CaseDataBuilder.builder().atStateClaimDraft()
                .respondent2OrganisationPolicy(OrganisationPolicy.builder().organisation(organisation).build())
                .respondent2OrgRegistered(YES)
                .build();
            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).containsExactly("No Organisation selected");
        }

        @Test
        void shouldBeSuccessful_whenValid() {
            uk.gov.hmcts.reform.ccd.model.Organisation organisation
                = uk.gov.hmcts.reform.ccd.model.Organisation.builder()
                .organisationID("orgId")
                .build();

            CaseData caseData = CaseDataBuilder.builder().atStateClaimDraft()
                .respondent2OrganisationPolicy(OrganisationPolicy.builder().organisation(organisation).build())
                .respondent2OrgRegistered(YES)
                .build();
            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).isEmpty();
        }

        @Test
        void shouldReturnError_whenApplicantAndRespondent1SolicitorOrganisationsAreSame1v2() {

            uk.gov.hmcts.reform.ccd.model.Organisation organisation
                = uk.gov.hmcts.reform.ccd.model.Organisation.builder()
                .organisationID("orgId")
                .build();

            CaseData caseData = CaseDataBuilder.builder().atStateClaimDraft()
                .multiPartyClaimTwoDefendantSolicitors()
                .respondent2Represented(YES)
                .applicant1OrganisationPolicy(OrganisationPolicy.builder().organisation(organisation).build())
                .respondent1OrganisationPolicy(OrganisationPolicy.builder().organisation(organisation).build())
                .build();
            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).containsExactly(
                "The legal representative details for the claimant and defendant are the same.  "
                    + "Please amend accordingly.");
        }

        @Test
        void shouldReturnError_whenApplicantAndRespondent2SolicitorOrganisationsAreSame1v2() {

            uk.gov.hmcts.reform.ccd.model.Organisation organisation
                = uk.gov.hmcts.reform.ccd.model.Organisation.builder()
                .organisationID("orgId")
                .build();

            CaseData caseData = CaseDataBuilder.builder().atStateClaimDraft()
                .multiPartyClaimTwoDefendantSolicitors()
                .respondent2Represented(YES)
                .applicant1OrganisationPolicy(OrganisationPolicy.builder().organisation(organisation).build())
                .respondent2OrganisationPolicy(OrganisationPolicy.builder().organisation(organisation).build())
                .build();
            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).containsExactly(
                "The legal representative details for the claimant and defendant are the same.  "
                    + "Please amend accordingly.");
        }

        @Test
        void shouldReturnError_whenApplicantAndRespondent1SolicitorOrganisationsAreSame1v2SameSol() {

            uk.gov.hmcts.reform.ccd.model.Organisation organisation
                = uk.gov.hmcts.reform.ccd.model.Organisation.builder()
                .organisationID("orgId")
                .build();

            CaseData caseData = CaseDataBuilder.builder().atStateClaimDraft()
                .multiPartyClaimOneDefendantSolicitor()
                .applicant1OrganisationPolicy(OrganisationPolicy.builder().organisation(organisation).build())
                .respondent1OrganisationPolicy(OrganisationPolicy.builder().organisation(organisation).build())
                .build();
            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).containsExactly(
                "The legal representative details for the claimant and defendant are the same.  "
                    + "Please amend accordingly.");
        }
    }

    @Nested
    class MidStatementOfTruth {

        @Test
        void shouldSetStatementOfTruthToNull_whenPopulated() {
            String name = "John Smith";
            String role = "Solicitor";

            CaseData caseData = CaseDataBuilder.builder()
                .uiStatementOfTruth(StatementOfTruth.builder().name(name).role(role).build())
                .build();

            CallbackParams params = callbackParamsOf(caseData, MID, "statement-of-truth");
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getData())
                .doesNotHaveToString("uiStatementOfTruth");
        }
    }

    @Nested
    class MidPopulateClaimantSolicitor {

        @Test
        void shouldSetOrganisation_WhenPopulated() {

            CaseData caseData = CaseDataBuilder.builder().build();

            Organisation organisation = Organisation.builder()
                .organisationIdentifier("1")
                .companyNumber("1")
                .name("Organisation1")
                .build();

            CallbackParams params = callbackParamsOf(caseData, MID, "populateClaimantSolicitor");

            when(organisationService.findOrganisation(CallbackParams.Params.BEARER_TOKEN.toString()))
                .thenReturn(Optional.ofNullable(organisation));

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getData()).extracting("applicant1OrganisationPolicy")
                .extracting("Organisation")
                .extracting("OrganisationID")
                .isEqualTo(organisation.getOrganisationIdentifier());
        }

    }

    @Nested
    class MidSetRespondent2SameLegalRepToNo {

        @Test
        void shouldsetRespondent2SameLegalRepToNo_WhenRespondent1NotRepresented() {

            CaseData caseData = CaseDataBuilder.builder().respondent1Represented(YesOrNo.NO).build();

            CallbackParams params = callbackParamsOf(caseData, MID, "setRespondent2SameLegalRepresentativeToNo");

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getData()).extracting("respondent2SameLegalRepresentative")
                .isEqualTo("No");
        }

    }

    @Nested
    class ValidateEmails {

        @Nested
        class ClaimantRepEmail {

            @Test
            void shouldReturnNoErrors_whenIdamEmailIsCorrect() {
                CaseData caseData = CaseData.builder()
                    .applicantSolicitor1CheckEmail(CorrectEmail.builder().correct(YES).build())
                    .build();

                CallbackParams params = callbackParamsOf(caseData, MID, "validate-claimant-legal-rep-email");
                var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

                assertThat(response.getErrors()).isNull();
            }

            @Test
            void shouldReturnNoErrors_whenIdamEmailIsNotCorrectButAdditionalEmailIsValid() {
                String validEmail = "john@example.com";

                CaseData caseData = CaseData.builder()
                    .applicantSolicitor1CheckEmail(CorrectEmail.builder().correct(NO).build())
                    .applicantSolicitor1UserDetails(IdamUserDetails.builder().email(validEmail).build())
                    .build();

                CallbackParams params = callbackParamsOf(caseData, MID, "validate-claimant-legal-rep-email");
                var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

                assertThat(response.getErrors()).isEmpty();
            }

            @Test
            void shouldReturnErrors_whenIdamEmailIsNotCorrectAndAdditionalEmailIsInvalid() {
                String invalidEmail = "a@a";

                CaseData caseData = CaseData.builder()
                    .applicantSolicitor1CheckEmail(CorrectEmail.builder().correct(NO).build())
                    .applicantSolicitor1UserDetails(IdamUserDetails.builder().email(invalidEmail).build())
                    .build();

                CallbackParams params = callbackParamsOf(caseData, MID, "validate-claimant-legal-rep-email");
                var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

                assertThat(response.getErrors()).containsExactly("Enter an email address in the correct format,"
                    + " for example john.smith@example.com");
            }
        }

        @Nested
        class DefendantRepEmail {

            @Test
            void shouldReturnNoErrors_whenEmailIsValid() {
                String validEmail = "john@example.com";

                CaseData caseData = CaseData.builder()
                    .respondentSolicitor1EmailAddress(validEmail)
                    .build();

                CallbackParams params = callbackParamsOf(caseData, MID, "validate-defendant-legal-rep-email");
                var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

                assertThat(response.getErrors()).isEmpty();
            }

            @Test
            void shouldReturnErrors_whenEmailIsInvalid() {
                String invalidEmail = "a@a";

                CaseData caseData = CaseData.builder()
                    .respondentSolicitor1EmailAddress(invalidEmail)
                    .build();

                CallbackParams params = callbackParamsOf(caseData, MID, "validate-defendant-legal-rep-email");
                var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

                assertThat(response.getErrors()).containsExactly("Enter an email address in the correct format,"
                    + " for example john.smith@example.com");
            }
        }
    }

    @Nested
    class AboutToSubmitCallback {

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
        }

        @Test
        void shouldAddCaseReferenceSubmittedDateAndAllocatedTrack_whenInvoked() {
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getData())
                .containsEntry("legacyCaseReference", REFERENCE_NUMBER)
                .containsEntry("submittedDate", submittedDate.format(DateTimeFormatter.ISO_DATE_TIME))
                .containsEntry("allocatedTrack", MULTI_CLAIM.name());
        }

        @Test
        void shouldSetIntermediateAllocatedTrack_whenInvoked() {
            // New multi and intermediate track change track logic
            // claim amount is 100000.00, so track is intermediate, as this is the upper limit
            when(featureToggleService.isMultiOrIntermediateTrackEnabled(any())).thenReturn(true);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            assertThat(response.getData()).containsEntry("allocatedTrack", INTERMEDIATE_CLAIM.name());
        }

        @Test
        void shouldSetMultiAllocatedTrack_whenInvoked() {
            // New multi and intermediate track change track logic
            // claim amount is 100000.01, so track is multi
            when(featureToggleService.isMultiOrIntermediateTrackEnabled(any())).thenReturn(true);
            CaseData caseDataUpdated = CaseDataBuilder.builder().atStateClaimDraft()
                .claimValue(ClaimValue.builder()
                    .statementOfValueInPennies(BigDecimal.valueOf(10000001))
                    .build())
                .build();
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(callbackParamsOf(V_1, caseDataUpdated, ABOUT_TO_SUBMIT));
            assertThat(response.getData()).containsEntry("allocatedTrack", MULTI_CLAIM.name());
        }

        @Test
        void shouldAddPartyIdsToPartyFields_whenInvoked() {
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getData()).extracting("applicant1").hasFieldOrProperty("partyID");
            assertThat(response.getData()).extracting("respondent1").hasFieldOrProperty("partyID");
        }

        @ParameterizedTest
        @ValueSource(booleans = {true, false})
        void shouldAddAnyRepresentedAsYes_whenCaseEventsEnabledOnly(boolean caseEventsEnabled) {
            when(featureToggleService.isCaseEventsEnabled()).thenReturn(caseEventsEnabled);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            if (caseEventsEnabled) {
                assertThat(response.getData().get("anyRepresented"))
                    .isEqualTo("Yes");
            } else {
                assertThat(response.getData().get("anyRepresented")).isNull();
            }
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
        void shouldUpdateBusinessProcess_whenInvoked() {
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getData())
                .extracting("businessProcess")
                .extracting("camundaEvent", "status")
                .containsOnly(CREATE_SERVICE_REQUEST_CLAIM.name(), "READY");
        }

        @Test
        void shouldClearClaimStartedFlag_whenInvoked() {
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(
                callbackParamsOf(caseData.toBuilder().claimStarted(YES).build(), ABOUT_TO_SUBMIT));

            assertThat(response.getData())
                .doesNotContainEntry("claimStarted", YES);
        }

        @Test
        void shouldAddCaseNamePublic_whenInvoked() {
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getData()).containsEntry("caseNamePublic", "'John Rambo' v 'Sole Trader'");
        }

        @Test
        void shouldCopyRespondent1OrgPolicyReferenceForSameRegisteredSolicitorScenario_whenInvoked() {
            caseData = CaseDataBuilder.builder().atStateClaimIssued1v2AndSameRepresentative().build();
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(
                callbackParamsOf(
                    V_1,
                    caseData,
                    ABOUT_TO_SUBMIT
                ));
            var respondent2OrgPolicy = response.getData().get("respondent2OrganisationPolicy");
            var respondentSolicitor2EmailAddress = response.getData().get("respondentSolicitor2EmailAddress");

            assertThat(respondent2OrgPolicy).extracting("OrgPolicyReference").isEqualTo("org1PolicyReference");
            assertThat(respondent2OrgPolicy)
                .extracting("Organisation").doesNotHaveToString("OrganisationID");
            assertThat(respondentSolicitor2EmailAddress).isEqualTo("respondentsolicitor@example.com");
            assertThat(response.getData()).extracting("respondent2OrganisationIDCopy").isEqualTo("org1");
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
        void shouldUpdateRespondentOrgRegistered_whenInvoked() {
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getData())
                .containsEntry("respondent1OrgRegistered", "Yes");
        }

        //TO DO remove V_1 when CIV-3278 is released
        @Test
        void shouldSetRespondent2OrgPolicyReferenceFor1v2SSCases() {
            caseData = CaseDataBuilder.builder().atStateClaimIssued1v2AndSameRepresentative().build();
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(
                callbackParamsOf(
                    V_1,
                    caseData,
                    ABOUT_TO_SUBMIT
                ));

            assertThat(response.getData())
                .containsEntry("respondent1OrgRegistered", "Yes");

            assertThat(response.getData())
                .containsEntry("respondent2OrgRegistered", "Yes");
            assertThat(response.getData())
                .containsEntry("respondentSolicitor2EmailAddress", "respondentsolicitor@example.com");
        }

        //TO DO remove V_1 when CIV-3278 is released
        @Test
        void shouldSetRespondent2OrgPolicyReferenceFor1v2DSCases() {
            caseData = CaseDataBuilder.builder().atStateClaimIssued()
                .multiPartyClaimTwoDefendantSolicitors()
                .respondent2Represented(YES)
                .build();
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(
                callbackParamsOf(
                    V_1,
                    caseData,
                    ABOUT_TO_SUBMIT
                ));
            assertThat(response.getData())
                .containsEntry("respondent1OrgRegistered", "Yes");

            assertThat(response.getData())
                .containsEntry("respondent2OrgRegistered", "Yes");
        }

        @Test
        void shouldNotCopyRespondent1OrgPolicyDetailsFor1v2SameUnregisteredSolicitorScenario_whenInvoked() {
            caseData = CaseDataBuilder.builder().atStateClaimIssued1v2AndSameUnregisteredRepresentative().build();

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(
                callbackParamsOf(
                    V_1,
                    caseData,
                    ABOUT_TO_SUBMIT
                ));

            var respondent2OrgPolicy = response.getData().get("respondent2OrganisationPolicy");

            assertThat(respondent2OrgPolicy)
                .doesNotHaveToString("OrgPolicyReference")
                .doesNotHaveToString("Organisation");
        }

        @Test
        void shouldAssignCaseName1v2_whenCaseIs1v2() {
            CaseData localCaseData = CaseDataBuilder.builder().atStateClaimNotified_1v2_andNotifyBothSolicitors().build();

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(
                callbackParamsOf(localCaseData, ABOUT_TO_SUBMIT));

            assertThat(response.getData().get("caseNameHmctsInternal"))
                .isEqualTo("'John Rambo' v 'Sole Trader', 'John Rambo'");
            assertThat(response.getData().get("caseManagementCategory")).extracting("value")
                .extracting("code").isEqualTo("Civil");
        }

        @Test
        void shouldAssignCaseName2v1_whenCaseIs2v1() {
            CaseData localCaseData = CaseDataBuilder.builder().atStateClaimSubmitted2v1RespondentRegistered().build();

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(
                callbackParamsOf(localCaseData, ABOUT_TO_SUBMIT));

            assertThat(response.getData().get("caseNameHmctsInternal"))
                .isEqualTo("'John Rambo', 'Jason Rambo' v 'Sole Trader'");
            assertThat(response.getData().get("caseManagementCategory")).extracting("value")
                .extracting("code").isEqualTo("Civil");
        }

        @Test
        void shouldAssignCaseName1v1_whenCaseIs1v1() {
            CaseData localCaseData = CaseDataBuilder.builder().atStateClaimNotified_1v1().build();

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(
                callbackParamsOf(localCaseData, ABOUT_TO_SUBMIT));

            assertThat(response.getData().get("caseNameHmctsInternal"))
                .isEqualTo("'John Rambo' v 'Sole Trader'");
            assertThat(response.getData().get("caseManagementCategory")).extracting("value")
                .extracting("code").isEqualTo("Civil");
        }

        @Test
        void shouldSetCaseCategoryToUnspec_whenInvoked() {
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getData())
                .containsEntry("CaseAccessCategory", CaseCategory.UNSPEC_CLAIM.toString());
        }

        @Test
        void shouldsetClaimTypeFromClaimTypeUnspec_when_sdoR2Enabled() {

            CaseData localCaseData = CaseDataBuilder.builder().atStateClaimDraft().build();
            when(featureToggleService.isSdoR2Enabled()).thenReturn(true);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(
                callbackParamsOf(localCaseData, ABOUT_TO_SUBMIT));
            assertThat(response.getData())
                .containsEntry("claimType", ClaimType.CLINICAL_NEGLIGENCE.name());
        }

        @Test
        void shouldNotsetClaimTypeFromClaimTypeUnspec_when_sdoR2Disabled() {

            when(featureToggleService.isSdoR2Enabled()).thenReturn(false);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(
                callbackParamsOf(caseData, ABOUT_TO_SUBMIT));
            assertThat(response.getData())
                .containsEntry("claimType", ClaimType.PERSONAL_INJURY.name());
        }

        @Nested
        class DefendantLipAtClaimIssued {
            @Test
            void shouldSetDefend1LipAtClaimIssued_when_defendant1LitigantParty() {
                caseData = CaseDataBuilder.builder().atStateClaimSubmitted1v1AndNoRespondentRepresented().build();
                var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(
                    callbackParamsOf(caseData, ABOUT_TO_SUBMIT));

                assertThat(response.getData()).extracting("defendant1LIPAtClaimIssued")
                    .isEqualTo("Yes");
                assertThat(response.getData()).doesNotHaveToString("defendant2LIPAtClaimIssued");
            }

            @Test
            void shouldSetDefend1LipAtClaimIssued_1v2_defendant2LitigantParty_whenInvoked() {
                caseData = CaseDataBuilder.builder()
                    .atStateClaimSubmitted1v2AndOnlyFirstRespondentIsRepresented()
                    .build();

                var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(
                    callbackParamsOf(caseData, ABOUT_TO_SUBMIT));

                assertThat(response.getData()).extracting("defendant1LIPAtClaimIssued")
                    .isEqualTo("No");
                assertThat(response.getData()).extracting("defendant2LIPAtClaimIssued")
                    .isEqualTo("Yes");
            }

            @Test
            void shouldSetDefend1LipAtClaimIssued_1v2_defendant1LitigantParty_whenInvoked() {
                caseData = CaseDataBuilder.builder()
                    .atStateClaimSubmitted1v2AndOnlySecondRespondentIsRepresented()
                    .build();

                var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(
                    callbackParamsOf(caseData, ABOUT_TO_SUBMIT));

                assertThat(response.getData()).extracting("defendant1LIPAtClaimIssued")
                    .isEqualTo("Yes");
                assertThat(response.getData()).extracting("defendant2LIPAtClaimIssued")
                    .isEqualTo("No");
            }

            @Test
            void shouldSetDefendantLIPAtClaim_1v2_BothDefendantLitigantParty_whenInvoked() {
                caseData = CaseDataBuilder.builder().atStateClaimSubmittedNoRespondentRepresented().build();

                var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(
                    callbackParamsOf(caseData, ABOUT_TO_SUBMIT));

                assertThat(response.getData()).extracting("defendant1LIPAtClaimIssued")
                    .isEqualTo("Yes");
                assertThat(response.getData()).extracting("defendant2LIPAtClaimIssued")
                    .isEqualTo("Yes");
            }

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
                CaseData localCaseData = CaseDataBuilder.builder().atStateClaimDraft().build().toBuilder()
                    .applicantSolicitor1CheckEmail(CorrectEmail.builder()
                        .email(EMAIL)
                        .correct(YES)
                        .build())
                    .applicantSolicitor1UserDetails(IdamUserDetails.builder()
                        .email(DIFFERENT_EMAIL)
                        .build())
                    .build();

                params = callbackParamsOf(localCaseData, ABOUT_TO_SUBMIT);
                var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

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
                userId = UUID.randomUUID().toString();
                given(userService.getUserDetails(any()))
                    .willReturn(UserDetails.builder().email(EMAIL).id(userId).build());

                CaseData localCaseData = CaseDataBuilder.builder().atStateClaimDraft().build().toBuilder()
                    .applicantSolicitor1CheckEmail(CorrectEmail.builder()
                        .email(EMAIL)
                        .correct(NO)
                        .build())
                    .applicantSolicitor1UserDetails(IdamUserDetails.builder()
                        .email(DIFFERENT_EMAIL)
                        .build())
                    .build();

                CallbackParams localParams = callbackParamsOf(localCaseData, ABOUT_TO_SUBMIT);
                var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(localParams);

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
                String name = "John Smith";
                String role = "Solicitor";

                CaseData data = caseData.toBuilder()
                    .uiStatementOfTruth(StatementOfTruth.builder().name(name).role(role).build())
                    .build();

                var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(
                    callbackParamsOf(
                        V_1,
                        data,
                        ABOUT_TO_SUBMIT
                    ));

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
        class GetAllPartyNames {
            @Test
            void oneVOne() {
                var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

                assertThat(response.getData())
                    .containsEntry("allPartyNames", "Mr. John Rambo V Mr. Sole Trader");
            }

            @Test
            void oneVTwo() {
                CaseData localCaseData = CaseDataBuilder.builder()
                    .atStateClaimDraft()
                    .multiPartyClaimTwoDefendantSolicitors()
                    .build();

                var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(
                    callbackParamsOf(localCaseData, ABOUT_TO_SUBMIT));

                assertThat(response.getData())
                    .containsEntry("allPartyNames", "Mr. John Rambo V Mr. Sole Trader, Mr. John Rambo");
            }

            @Test
            void twoVOne() {
                CaseData localCaseData = CaseDataBuilder.builder()
                    .atStateClaimDraft()
                    .multiPartyClaimTwoApplicants()
                    .build();

                var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(
                    callbackParamsOf(localCaseData, ABOUT_TO_SUBMIT));

                assertThat(response.getData())
                    .containsEntry("allPartyNames", "Mr. John Rambo, Mr. Jason Rambo V Mr. Sole Trader");
            }
        }

        @Nested
        class HandleCourtLocation {
            @Test
            void shouldHandleCourtLocationData() {
                LocationRefData locationA = LocationRefData.builder()
                    .regionId("regionId1").epimmsId("epimmsId1").courtLocationCode("312").build();

                given(courtLocationUtility.findPreferredLocationData(any(), any(DynamicList.class)))
                    .willReturn(locationA);

                var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

                assertThat(response.getData())
                    .extracting("courtLocation")
                    .doesNotHaveToString("applicantPreferredCourtLocationList");

                assertThat(response.getData())
                    .extracting("courtLocation")
                    .extracting("caseLocation")
                    .extracting("region", "baseLocation")
                    .containsExactly("regionId1", "epimmsId1");

                assertThat(response.getData())
                    .extracting("courtLocation")
                    .extracting("applicantPreferredCourt").isEqualTo("312");

                assertThat(response.getData())
                    .extracting("caseManagementLocation")
                    .extracting("region", "baseLocation")
                    .containsExactly("2", "420219");
            }
        }

        @Test
        void shouldUpdateCaseListAndUnassignedListData() {
            CaseData localCaseData = CaseDataBuilder.builder()
                .atStateClaimDraft()
                .multiPartyClaimTwoDefendantSolicitors()
                .applicant1OrganisationPolicy(OrganisationPolicy.builder()
                    .orgPolicyReference("CLAIMANTREF1")
                    .build())
                .respondent1OrganisationPolicy(OrganisationPolicy.builder()
                    .orgPolicyReference("DEFENDANTREF1")
                    .organisation(uk.gov.hmcts.reform.ccd.model.Organisation.builder()
                        .organisationID("QWERTY R").build())
                    .build())
                .respondent2OrganisationPolicy(OrganisationPolicy.builder()
                    .orgPolicyReference("DEFENDANTREF2")
                    .organisation(uk.gov.hmcts.reform.ccd.model.Organisation.builder()
                        .organisationID("QWERTY R2").build())
                    .build())
                .build();

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(
                callbackParamsOf(localCaseData, ABOUT_TO_SUBMIT));

            assertThat(response.getData())
                .containsEntry("unassignedCaseListDisplayOrganisationReferences",
                    "CLAIMANTREF1, DEFENDANTREF1, DEFENDANTREF2");
            assertThat(response.getData())
                .containsEntry("caseListDisplayDefendantSolicitorReferences", "6789, 01234");
        }

        @Test
        void shouldUpdateRespondent1Organisation1IDCopySameSol() {
            CaseData localCaseData = CaseDataBuilder.builder()
                .atStateClaimIssued1v2AndSameRepresentative()
                .respondent1OrganisationPolicy(OrganisationPolicy.builder()
                    .orgPolicyReference("DEFENDANTREF1")
                    .organisation(uk.gov.hmcts.reform.ccd.model.Organisation.builder()
                        .organisationID("QWERTY R").build())
                    .build())
                .build();

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(
                callbackParamsOf(localCaseData, ABOUT_TO_SUBMIT));

            assertThat(response.getData())
                .extracting("respondent1OrganisationIDCopy")
                .isEqualTo("QWERTY R");
            assertThat(response.getData())
                .extracting("respondent2OrganisationIDCopy")
                .isEqualTo("QWERTY R");
        }

        @Test
        void shouldUpdateRespondent1And2Organisation1IDCopy() {
            CaseData localCaseData = CaseDataBuilder.builder()
                .atStateClaimDraft()
                .multiPartyClaimTwoDefendantSolicitors()
                .applicant1OrganisationPolicy(OrganisationPolicy.builder()
                    .orgPolicyReference("CLAIMANTREF1")
                    .build())
                .respondent1OrganisationPolicy(OrganisationPolicy.builder()
                    .orgPolicyReference("DEFENDANTREF1")
                    .organisation(uk.gov.hmcts.reform.ccd.model.Organisation.builder()
                        .organisationID("QWERTY R").build())
                    .build())
                .respondent2OrganisationPolicy(OrganisationPolicy.builder()
                    .orgPolicyReference("DEFENDANTREF2")
                    .organisation(uk.gov.hmcts.reform.ccd.model.Organisation.builder()
                        .organisationID("QWERTY R2").build())
                    .build())
                .build();

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(
                callbackParamsOf(localCaseData, ABOUT_TO_SUBMIT));

            assertThat(response.getData())
                .extracting("respondent1OrganisationIDCopy")
                .isEqualTo("QWERTY R");
            assertThat(response.getData())
                .extracting("respondent2OrganisationIDCopy")
                .isEqualTo("QWERTY R2");
        }

        @Test
        void shouldReturnExpectedErrorMessagesInResponse_whenInvokedWithNullCourtLocation() {
            CaseData data = caseData.toBuilder()
                .courtLocation(null)
                .build();

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(
                callbackParamsOf(data, ABOUT_TO_SUBMIT));

            assertThat(response.getErrors()).containsOnly("Court location code is required");
        }

        @Test
        void shouldReturnExpectedErrorMessagesInResponse_whenInvokedWithNullApplicantPreferredCourt() {
            CaseData data = caseData.toBuilder()
                .courtLocation(CourtLocation.builder().applicantPreferredCourtLocationList(null).build())
                .build();

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(
                callbackParamsOf(data, ABOUT_TO_SUBMIT));

            assertThat(response.getErrors()).containsOnly("Court location code is required");
        }

        @Nested
        class PopulateBlankOrgPolicies {

            @Test
            void oneVOne() {
                CaseData localCaseData = CaseDataBuilder.builder()
                    .atStateClaimDraft()
                    .respondent2OrganisationPolicy(null)
                    .build();

                var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(
                    callbackParamsOf(localCaseData, ABOUT_TO_SUBMIT));

                assertThat(response.getData())
                    .extracting("respondent2OrganisationPolicy")
                    .extracting("OrgPolicyCaseAssignedRole")
                    .isEqualTo("[RESPONDENTSOLICITORTWO]");

                assertThat(response.getData())
                    .extracting("respondent2OrganisationPolicy")
                    .doesNotHaveToString("Organisation");
            }

            @Test
            void unrepresentedDefendants() {
                CaseData localCaseData = CaseDataBuilder.builder()
                    .atStateClaimSubmittedNoRespondentRepresented()
                    .respondent1OrganisationPolicy(null)
                    .respondent2OrganisationPolicy(null)
                    .build();

                var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(
                    callbackParamsOf(localCaseData, ABOUT_TO_SUBMIT));

                assertThat(response.getData())
                    .extracting("respondent1OrganisationPolicy")
                    .extracting("OrgPolicyCaseAssignedRole")
                    .isEqualTo("[RESPONDENTSOLICITORONE]");

                assertThat(response.getData())
                    .extracting("respondent1OrganisationPolicy")
                    .doesNotHaveToString("Organisation");

                assertThat(response.getData())
                    .extracting("respondent2OrganisationPolicy")
                    .extracting("OrgPolicyCaseAssignedRole")
                    .isEqualTo("[RESPONDENTSOLICITORTWO]");

                assertThat(response.getData())
                    .extracting("respondent2OrganisationPolicy")
                    .doesNotHaveToString("Organisation");
            }
        }

        static Stream<Arguments> caseDataStream() {
            DocumentWithRegex documentRegex = new DocumentWithRegex(Document.builder()
                .documentUrl("fake-url")
                .documentFileName("file-name")
                .documentBinaryUrl("binary-url")
                .build());
            List<Element<DocumentWithRegex>> documentList = new ArrayList<>();
            List<Element<Document>> documentList2 = new ArrayList<>();
            documentList.add(element(documentRegex));
            documentList2.add(element(Document.builder()
                .documentUrl("fake-url")
                .documentFileName("file-name")
                .documentBinaryUrl("binary-url")
                .build()));

            var documentToUpload = ServedDocumentFiles.builder()
                .particularsOfClaimDocument(documentList2)
                .medicalReport(documentList)
                .scheduleOfLoss(documentList)
                .certificateOfSuitability(documentList)
                .other(documentList).build();

            return Stream.of(
                arguments(CaseDataBuilder.builder().atStateClaimDraft().build().toBuilder()
                    .uploadParticularsOfClaim(YES)
                    .servedDocumentFiles(documentToUpload)
                    .build())
            );
        }

        @ParameterizedTest
        @MethodSource("caseDataStream")
        void shouldAssignCategoryIds_whenDocumentExist(CaseData caseData) {
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(
                callbackParamsOf(caseData, ABOUT_TO_SUBMIT));
            // When
            CaseData updatedData = objMapper.convertValue(response.getData(), CaseData.class);
            // Then
            assertThat(updatedData.getServedDocumentFiles().getParticularsOfClaimDocument().get(0).getValue()
                .getCategoryID()).isEqualTo("particularsOfClaim");
            assertThat(updatedData.getServedDocumentFiles().getMedicalReport().get(0).getValue().getDocument()
                .getCategoryID()).isEqualTo("particularsOfClaim");
            assertThat(updatedData.getServedDocumentFiles().getScheduleOfLoss().get(0).getValue().getDocument()
                .getCategoryID()).isEqualTo("particularsOfClaim");
            assertThat(updatedData.getServedDocumentFiles().getCertificateOfSuitability().get(0).getValue().getDocument()
                .getCategoryID()).isEqualTo("particularsOfClaim");
            assertThat(updatedData.getServedDocumentFiles().getOther().get(0).getValue().getDocument()
                .getCategoryID()).isEqualTo("particularsOfClaim");

        }

        @Test
        void shouldNotAssignCategoryIds_whenDocumentNotExist() {
            //Given
            CaseDataBuilder.builder().atStateClaimDraft().build().toBuilder()
                .uploadParticularsOfClaim(NO)
                .build();
            // When
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(
                callbackParamsOf(caseData, ABOUT_TO_SUBMIT));
            // Then
            assertThat(response.getData()).doesNotContainKey("servedDocumentFiles");
        }

        @Test
        void shouldNotAssignCategoryIds_whenDocumentNotExistAndParticularOfClaimTextExists() {
            //Given
            ServedDocumentFiles servedDocumentFiles = ServedDocumentFiles.builder()
                .particularsOfClaimText("Some string").build();

            CaseData localCaseData = CaseDataBuilder.builder().atStateClaimDraft().build().toBuilder()
                .uploadParticularsOfClaim(YES)
                .servedDocumentFiles(servedDocumentFiles)
                .build();
            // When
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(
                callbackParamsOf(localCaseData, ABOUT_TO_SUBMIT));
            CaseData updatedData = objMapper.convertValue(response.getData(), CaseData.class);
            // Then
            assertThat(updatedData.getServedDocumentFiles().getParticularsOfClaimDocument()).isNull();
            assertThat(updatedData.getServedDocumentFiles().getMedicalReport()).isNull();
            assertThat(updatedData.getServedDocumentFiles().getScheduleOfLoss()).isNull();
            assertThat(updatedData.getServedDocumentFiles().getCertificateOfSuitability()).isNull();
            assertThat(updatedData.getServedDocumentFiles().getOther()).isNull();
        }
    }

    @Nested
    class SubmittedCallback {

        @Nested
        class RespondentsDoNotHaveLegalRepresentation {

            @Test
            void certificateOfService_shouldReturnExpectedResponse_whenRespondentsDoesNotHaveRepresentation() {
                CaseData caseData = CaseDataBuilder.builder().atStateClaimIssuedUnrepresentedDefendants().build();
                CallbackParams params = callbackParamsOf(caseData, SUBMITTED);

                SubmittedCallbackResponse response = (SubmittedCallbackResponse) handler.handle(params);

                String body = format(
                    CONFIRMATION_BODY_LIP_COS,
                    format("/cases/case-details/%s#Service%%20Request", CASE_ID),
                    format("/cases/case-details/%s#CaseDocuments", CASE_ID),
                    responsePackLink
                ) + exitSurveyContentService.applicantSurvey();

                assertThat(response).usingRecursiveComparison().isEqualTo(
                    SubmittedCallbackResponse.builder()
                        .confirmationHeader(format("# Please now pay your claim fee%n# using the link below"))
                        .confirmationBody(body)
                        .build());
            }
        }

        @Nested
        class Respondent1HaveLegalRepresentation1V2SameSolicitor {

            @Test
            void shouldReturnExpectedSubmittedCallbackResponse_whenRespondent1HasRepresentation() {
                CaseData caseData = CaseDataBuilder.builder()
                    .atStateClaimDetailsNotified()
                    .multiPartyClaimOneDefendantSolicitor().build();
                CallbackParams params = callbackParamsOf(caseData, SUBMITTED);
                SubmittedCallbackResponse response = (SubmittedCallbackResponse) handler.handle(params);

                String body = format(
                    CONFIRMATION_SUMMARY,
                    format("/cases/case-details/%s#CaseDocuments", CASE_ID)
                ) + exitSurveyContentService.applicantSurvey();

                assertThat(response).usingRecursiveComparison().isEqualTo(
                    SubmittedCallbackResponse.builder()
                        .confirmationHeader(format("# Please now pay your claim fee%n# using the link below"))
                        .confirmationBody(body)
                        .build());
            }

            @Test
            void shouldReturnExpectedSubmittedCallbackResponse_whenRespondent1HasRepresentationAndPBAv3IsOn() {
                CaseData caseData = CaseDataBuilder.builder()
                    .atStateClaimDetailsNotified()
                    .multiPartyClaimOneDefendantSolicitor().build();
                CallbackParams params = callbackParamsOf(caseData, SUBMITTED);
                SubmittedCallbackResponse response = (SubmittedCallbackResponse) handler.handle(params);

                String body = format(
                    CONFIRMATION_SUMMARY,
                    format("/cases/case-details/%s#CaseDocuments", CASE_ID)
                ) + exitSurveyContentService.applicantSurvey();

                assertThat(response).usingRecursiveComparison().isEqualTo(
                    SubmittedCallbackResponse.builder()
                        .confirmationHeader(format("# Please now pay your claim fee%n# using the link below"))
                        .confirmationBody(body)
                        .build());
            }

            @Test
            void shouldReturnExpectedSubmittedCallbackResponse_whenRespondent1HasRepresentationAndPBAv3AndCOSIsOn() {
                CaseData caseData = CaseDataBuilder.builder()
                    .atStateClaimDetailsNotified()
                    .multiPartyClaimOneDefendantSolicitor().build();
                CallbackParams params = callbackParamsOf(V_1, caseData, SUBMITTED);
                SubmittedCallbackResponse response = (SubmittedCallbackResponse) handler.handle(params);

                String body = format(
                    CONFIRMATION_SUMMARY,
                    format("/cases/case-details/%s#CaseDocuments", CASE_ID)
                ) + exitSurveyContentService.applicantSurvey();

                assertThat(response).usingRecursiveComparison().isEqualTo(
                    SubmittedCallbackResponse.builder()
                        .confirmationHeader(format("# Please now pay your claim fee%n# using the link below"))
                        .confirmationBody(body)
                        .build());
            }
        }

        @Nested
        class Respondent1DoesNotHaveLegalRepresentation {

            @Test
            void certificateOfService_shouldReturnExpectedResponse_whenRespondentsDoesNotHaveRepresentation() {
                CaseData caseData = CaseDataBuilder.builder().atStateClaimIssuedUnrepresentedDefendant1().build();
                CallbackParams params = callbackParamsOf(caseData, SUBMITTED);

                SubmittedCallbackResponse response = (SubmittedCallbackResponse) handler.handle(params);

                String body = format(
                    CONFIRMATION_BODY_LIP_COS,
                    format("/cases/case-details/%s#Service%%20Request", CASE_ID),
                    format("/cases/case-details/%s#CaseDocuments", CASE_ID),
                    responsePackLink
                ) + exitSurveyContentService.applicantSurvey();

                assertThat(response).usingRecursiveComparison().isEqualTo(
                    SubmittedCallbackResponse.builder()
                        .confirmationHeader(format("# Please now pay your claim fee%n# using the link below"))
                        .confirmationBody(body)
                        .build());
            }
        }

        @Nested
        class Respondent1SolicitorOrgNotRegisteredInMyHmcts {

            @Test
            void certificateOfService_shouldReturnExpectedResponse_whenRespondent1SolicitorNotRegisteredInMyHmcts() {
                CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified()
                    .respondent1Represented(YES)
                    .respondent1OrgRegistered(NO)
                    .build();
                CallbackParams params = callbackParamsOf(caseData, SUBMITTED);
                SubmittedCallbackResponse response = (SubmittedCallbackResponse) handler.handle(params);

                assertThat(response).usingRecursiveComparison().isEqualTo(
                    SubmittedCallbackResponse.builder()
                        .confirmationHeader(format("# Please now pay your claim fee%n# using the link below"))
                        .confirmationBody(format(
                            CONFIRMATION_BODY_LIP_COS,
                            format("/cases/case-details/%s#Service%%20Request", CASE_ID),
                            format("/cases/case-details/%s#CaseDocuments", CASE_ID),
                            responsePackLink
                        ) + exitSurveyContentService.applicantSurvey())
                        .build());
            }
        }

        @Nested
        class Respondent2DoesNotHaveLegalRepresentation {

            @Test
            void certificateOfService_shouldReturnExpectedResponse_whenRespondent2DoesNotHaveRepresentation() {
                CaseData caseData = CaseDataBuilder.builder().atStateClaimIssuedUnrepresentedDefendants().build();
                CallbackParams params = callbackParamsOf(caseData, SUBMITTED);
                SubmittedCallbackResponse response = (SubmittedCallbackResponse) handler.handle(params);

                String body = format(
                    CONFIRMATION_BODY_LIP_COS,
                    format("/cases/case-details/%s#Service%%20Request", CASE_ID),
                    format("/cases/case-details/%s#CaseDocuments", CASE_ID),
                    responsePackLink
                ) + exitSurveyContentService.applicantSurvey();

                assertThat(response).usingRecursiveComparison().isEqualTo(
                    SubmittedCallbackResponse.builder()
                        .confirmationHeader(format("# Please now pay your claim fee%n# using the link below"))
                        .confirmationBody(body)
                        .build());
            }
        }

        @Nested
        class RespondentHasLegalRepresentation1v1 {

            @Test
            void shouldReturnExpectedSubmittedCallbackResponse_whenRespondent1HasRepresentation() {
                CaseData caseData = CaseDataBuilder.builder().atStateClaimNotified_1v1().build();
                CallbackParams params = callbackParamsOf(caseData, SUBMITTED);
                SubmittedCallbackResponse response = (SubmittedCallbackResponse) handler.handle(params);

                String body = format(
                    CONFIRMATION_SUMMARY,
                    format("/cases/case-details/%s#CaseDocuments", CASE_ID)
                ) + exitSurveyContentService.applicantSurvey();

                assertThat(response).usingRecursiveComparison().isEqualTo(
                    SubmittedCallbackResponse.builder()
                        .confirmationHeader(format("# Please now pay your claim fee%n# using the link below"))
                        .confirmationBody(body)
                        .build());
            }
        }

        @Nested
        class BothRespondentsHasLegalRepresentation {

            @Test
            void shouldReturnExpectedSubmittedCallbackResponse_whenRespondentsHaveRepresentation() {
                CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build();
                CallbackParams params = callbackParamsOf(caseData, SUBMITTED);
                SubmittedCallbackResponse response = (SubmittedCallbackResponse) handler.handle(params);

                String body = format(
                    CONFIRMATION_SUMMARY,
                    format("/cases/case-details/%s#CaseDocuments", CASE_ID)
                ) + exitSurveyContentService.applicantSurvey();

                assertThat(response).usingRecursiveComparison().isEqualTo(
                    SubmittedCallbackResponse.builder()
                        .confirmationHeader(format("# Please now pay your claim fee%n# using the link below"))
                        .confirmationBody(body)
                        .build());
            }
        }
    }
}
