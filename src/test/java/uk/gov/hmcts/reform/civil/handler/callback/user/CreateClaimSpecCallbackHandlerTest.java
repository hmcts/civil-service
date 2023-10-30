package uk.gov.hmcts.reform.civil.handler.callback.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mockito;
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
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.model.Address;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.ClaimAmountBreakup;
import uk.gov.hmcts.reform.civil.model.ClaimAmountBreakupDetails;
import uk.gov.hmcts.reform.civil.model.CorrectEmail;
import uk.gov.hmcts.reform.civil.model.DefendantPinToPostLRspec;
import uk.gov.hmcts.reform.civil.model.Fee;
import uk.gov.hmcts.reform.civil.model.IdamUserDetails;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.ServedDocumentFiles;
import uk.gov.hmcts.reform.civil.model.SolicitorOrganisationDetails;
import uk.gov.hmcts.reform.civil.model.StatementOfTruth;
import uk.gov.hmcts.reform.civil.model.TimelineOfEvents;
import uk.gov.hmcts.reform.civil.model.TimelineOfEventDetails;
import uk.gov.hmcts.reform.civil.model.interestcalc.InterestClaimFromType;
import uk.gov.hmcts.reform.civil.model.interestcalc.InterestClaimOptions;
import uk.gov.hmcts.reform.civil.model.interestcalc.SameRateInterestSelection;
import uk.gov.hmcts.reform.civil.model.interestcalc.SameRateInterestType;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.common.DynamicListElement;
import uk.gov.hmcts.reform.civil.sampledata.AddressBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CallbackParamsBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.sampledata.PartyBuilder;
import uk.gov.hmcts.reform.civil.service.ExitSurveyContentService;
import uk.gov.hmcts.reform.civil.service.FeesService;
import uk.gov.hmcts.reform.civil.service.OrganisationService;
import uk.gov.hmcts.reform.civil.service.Time;
import uk.gov.hmcts.reform.civil.service.flowstate.StateFlowEngine;
import uk.gov.hmcts.reform.civil.service.pininpost.DefendantPinToPostLRspecService;
import uk.gov.hmcts.reform.civil.utils.AccessCodeGenerator;
import uk.gov.hmcts.reform.civil.utils.CaseFlagsInitialiser;
import uk.gov.hmcts.reform.civil.utils.InterestCalculator;
import uk.gov.hmcts.reform.civil.validation.DateOfBirthValidator;
import uk.gov.hmcts.reform.civil.validation.OrgPolicyValidator;
import uk.gov.hmcts.reform.civil.validation.PostcodeValidator;
import uk.gov.hmcts.reform.civil.validation.ValidateEmailService;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;
import uk.gov.hmcts.reform.civil.prd.model.Organisation;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

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
import static uk.gov.hmcts.reform.civil.handler.callback.user.CreateClaimSpecCallbackHandler.CONFIRMATION_SUMMARY;
import static uk.gov.hmcts.reform.civil.handler.callback.user.CreateClaimSpecCallbackHandler.CONFIRMATION_SUMMARY_PBA_V3;
import static uk.gov.hmcts.reform.civil.handler.callback.user.CreateClaimSpecCallbackHandler.LIP_CONFIRMATION_BODY;
import static uk.gov.hmcts.reform.civil.handler.callback.user.CreateClaimSpecCallbackHandler.SPEC_CONFIRMATION_SUMMARY;
import static uk.gov.hmcts.reform.civil.handler.callback.user.CreateClaimSpecCallbackHandler.SPEC_CONFIRMATION_SUMMARY_PBA_V3;
import static uk.gov.hmcts.reform.civil.handler.callback.user.CreateClaimSpecCallbackHandler.SPEC_LIP_CONFIRMATION_BODY_PBAV3;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.DATE_TIME_AT;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.formatLocalDateTime;
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
    StateFlowEngine.class,
    PostcodeValidator.class,
    InterestCalculator.class,
    StateFlowEngine.class,
    ValidateEmailService.class,
    },
    properties = {"reference.database.enabled=false"})
class CreateClaimSpecCallbackHandlerTest extends BaseCallbackHandlerTest {

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
        + "and you'll be notified by email. The claim will then progress offline."
        + "%n%nOnce the claim has been issued, you will need to serve the claim upon the "
        + "defendant which must include a response pack"
        + "%n%nYou will need to send the following:<ul style=\"margin-bottom : 0px;\"> <li> <a href=\"%s\" target=\"_blank\">sealed claim form</a> "
        + "</li><li><a href=\"%s\" target=\"_blank\">response pack</a></li><ul style=\"list-style-type:circle\"><li><a href=\"%s\" target=\"_blank\">N9A</a></li>"
        + "<li><a href=\"%s\" target=\"_blank\">N9B</a></li></ul><li>and any supporting documents</li></ul>"
        + "to the defendant within 4 months."
        + "%n%nFollowing this, you will need to file a Certificate of Service and supporting documents "
        + "to : <a href=\"mailto:OCMCNton@justice.gov.uk\">OCMCNton@justice.gov.uk</a>. The Certificate of Service form can be found here:"
        + "%n%n<ul><li><a href=\"%s\" target=\"_blank\">N215</a></li></ul>";

    private final Organisation bulkOrganisation = Organisation.builder()
        .paymentAccount(List.of("12345", "98765"))
        .build();

    @MockBean
    private Time time;
    @MockBean
    private DefendantPinToPostLRspecService defendantPinToPostLRspecService;
    @MockBean
    private FeesService feesService;
    @MockBean
    private OrganisationService organisationService;

    @MockBean
    private IdamClient idamClient;

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
    }

    @Nested
    class MidEventParticularsOfClaimCallback {

        private final String pageId = "particulars-of-claim";
        private final CaseData.CaseDataBuilder caseDataBuilder =
            CaseDataBuilder.builder().atStateClaimDraft().build().toBuilder();

        @Test
        void shouldReturnErrors_whenNoDocuments() {
            // Given
            CaseData caseData = caseDataBuilder.build();
            CallbackParams params = callbackParamsOf(caseData, MID, pageId);

            // When
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            // Then
            assertThat(response.getErrors()).containsOnly("You must add Particulars of claim details");
        }

        @Test
        void shouldReturnErrors_whenParticularsOfClaimFieldsAreInErrorState() {
            // Given
            CaseData caseData = caseDataBuilder.servedDocumentFiles(ServedDocumentFiles.builder().build()).build();
            CallbackParams params = callbackParamsOf(caseData, MID, pageId);

            // When
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            // Then
            assertThat(response.getErrors()).containsOnly("You must add Particulars of claim details");
        }

        @Test
        void shouldReturnNoErrors_whenParticularOfClaimsFieldsAreValid() {
            // Given
            CaseData caseData = caseDataBuilder.servedDocumentFiles(ServedDocumentFiles.builder()
                                                                        .particularsOfClaimText("Some string")
                                                                        .build()).build();
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
                .collect(Collectors.toList());

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

            assertThat(getDynamicList(response))
                .isEqualTo(DynamicList.builder().value(DynamicListElement.EMPTY).build());
        }

        @Test
        void shouldSetPBAv3SpecFlagOn_whenPBAv3IsActivated() {
            // Given
            given(organisationService.findOrganisation(any())).willReturn(Optional.empty());
            when(toggleService.isPbaV3Enabled()).thenReturn(true);

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

            given(idamClient.getUserDetails(any()))
                .willReturn(UserDetails.builder().email(email).id(userId).build());

            CaseData caseData = CaseDataBuilder.builder().atStateClaimDraft().build();
            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);

            // When
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            // Then
            assertThat(response.getData())
                .extracting("applicantSolicitor1CheckEmail")
                .extracting("email")
                .isEqualTo(email);
        }

        @Test
        void shouldRemoveExistingEmail_whenOneHasAlreadyBeenEntered() {
            // Given
            String userId = UUID.randomUUID().toString();
            String email = "example@email.com";

            given(idamClient.getUserDetails(any()))
                .willReturn(UserDetails.builder().email(email).id(userId).build());

            CaseData caseData = CaseDataBuilder.builder().atStateClaimDraft().build().toBuilder()
                .applicantSolicitor1UserDetails(IdamUserDetails.builder()
                                                    .email("email@example.com")
                                                    .build())
                .build();
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
                .applicant1OrganisationPolicy(OrganisationPolicy.builder().organisation(null).build())
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
                = uk.gov.hmcts.reform.ccd.model.Organisation.builder()
                .organisationID(null)
                .build();

            CaseData caseData = CaseDataBuilder.builder().atStateClaimDraft()
                .applicant1OrganisationPolicy(OrganisationPolicy.builder().organisation(organisation).build())
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
                = uk.gov.hmcts.reform.ccd.model.Organisation.builder()
                .organisationID("orgId")
                .build();

            CaseData caseData = CaseDataBuilder.builder().atStateClaimDraft()
                .applicant1OrganisationPolicy(OrganisationPolicy.builder().organisation(organisation).build())
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
                .respondent1OrganisationPolicy(OrganisationPolicy.builder().organisation(null).build())
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
                = uk.gov.hmcts.reform.ccd.model.Organisation.builder()
                .organisationID(null)
                .build();

            CaseData caseData = CaseDataBuilder.builder().atStateClaimDraft()
                .respondent1OrganisationPolicy(OrganisationPolicy.builder().organisation(organisation).build())
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
                = uk.gov.hmcts.reform.ccd.model.Organisation.builder()
                .organisationID("orgId")
                .build();

            CaseData caseData = CaseDataBuilder.builder().atStateClaimDraft()
                .respondent1OrganisationPolicy(OrganisationPolicy.builder().organisation(organisation).build())
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
                .respondent2OrganisationPolicy(OrganisationPolicy.builder().organisation(null).build())
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
                = uk.gov.hmcts.reform.ccd.model.Organisation.builder()
                .organisationID(null)
                .build();

            CaseData caseData = CaseDataBuilder.builder().atStateClaimDraft()
                .respondent2OrganisationPolicy(OrganisationPolicy.builder().organisation(organisation).build())
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
                = uk.gov.hmcts.reform.ccd.model.Organisation.builder()
                .organisationID("orgId")
                .build();

            CaseData caseData = CaseDataBuilder.builder().atStateClaimDraft()
                .respondent2OrganisationPolicy(OrganisationPolicy.builder().organisation(organisation).build())
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
        void shouldSetStatementOfTruthToNull_whenPopulated() {
            // Given
            String name = "John Smith";
            String role = "Solicitor";

            CaseData caseData = CaseDataBuilder.builder()
                .uiStatementOfTruth(StatementOfTruth.builder().name(name).role(role).build())
                .build();

            CallbackParams params = callbackParamsOf(caseData, MID, "statement-of-truth");

            // When
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            // Then
            assertThat(response.getData())
                .doesNotHaveToString("uiStatementOfTruth");
        }
    }

    @Nested
    class MidAmountBreakup {

        @Test
        void shouldCalculateAmoutBreakup_whenCalled() {
            // Given
            List<ClaimAmountBreakup> claimAmountBreakup = new ArrayList<>();
            claimAmountBreakup.add(ClaimAmountBreakup.builder()
                                       .value(ClaimAmountBreakupDetails.builder()
                                                  .claimAmount(new BigDecimal(1000)).claimReason("Test reason1").build()).build());

            claimAmountBreakup.add(ClaimAmountBreakup.builder()
                                       .value(ClaimAmountBreakupDetails.builder()
                                                  .claimAmount(new BigDecimal(2000)).claimReason("Test reason2").build()).build());

            CaseData caseData = CaseData.builder().claimAmountBreakup(claimAmountBreakup).build();

            CallbackParams params = callbackParamsOf(caseData, MID, "amount-breakup");

            // When
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            // Then
            assertThat(response.getData())
                .containsEntry("claimAmountBreakupSummaryObject", " | Description | Amount | \n" +
                    " |---|---| \n" +
                    " | Test reason1 | £ 10.00 |\n" +
                    " Test reason2 | £ 20.00 |\n" +
                    "  | **Total** | £ 30.00 | ");
        }
    }

    @Nested
    class MidCalculateInterest {

        @Test
        void shouldCalculateInterest_whenPopulated() {
            // Given
            CaseData caseData = CaseData.builder().claimInterest(YES)
                .interestClaimOptions(InterestClaimOptions.SAME_RATE_INTEREST)
                .sameRateInterestSelection(SameRateInterestSelection.builder()
                                               .sameRateInterestType(SameRateInterestType.SAME_RATE_INTEREST_8_PC).build())
                .interestClaimFrom(InterestClaimFromType.FROM_CLAIM_SUBMIT_DATE)
                .totalClaimAmount(new BigDecimal(1000)).build();

            when(interestCalculator.calculateInterest(caseData)).thenReturn(new BigDecimal(0));
            CallbackParams params = callbackParamsOf(caseData, MID, "interest-calc");

            // When
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            // Then
            assertThat(response.getData()).containsEntry("calculatedInterest", " | Description | Amount | \n" +
                " |---|---| \n" +
                " | Claim amount | £ 1000 | \n" +
                " | Interest amount | £ 0 | \n" +
                " | Total amount | £ 1000 |");
        }
    }

    @Nested
    class MidSpecValidateClaimInterestDate {

        @Test
        void shouldValidateClaimInterestDate_whenPopulated() {
            // Given
            CaseData caseData = CaseData.builder().interestFromSpecificDate(LocalDate.now().minusDays(1))
                .build();

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
            CaseData caseData = CaseData.builder().interestFromSpecificDate(LocalDate.now().plusDays(1))
                .build();

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
            timelineOfEvents.add(TimelineOfEvents.builder().value(TimelineOfEventDetails.builder().timelineDate(LocalDate.now().minusDays(1)).build()).build());
            CaseData caseData = CaseData.builder().timelineOfEvents(timelineOfEvents)
                .build();

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
            timelineOfEvents.add(TimelineOfEvents.builder().value(TimelineOfEventDetails.builder().timelineDate(LocalDate.now().plusDays(1)).build()).build());
            CaseData caseData = CaseData.builder().timelineOfEvents(timelineOfEvents)
                .build();

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
            timelineOfEvents.add(TimelineOfEvents.builder().value(TimelineOfEventDetails.builder().timelineDate(LocalDate.now().minusDays(1)).build()).build());
            CaseData caseData = CaseData.builder().totalClaimAmount(new BigDecimal(1000))
                .build();

            CallbackParams params = callbackParamsOf(caseData, MID, "ClaimInterest");
            // When
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            // Then
            assertThat(response.getData()).containsEntry("calculatedInterest", " | Description | Amount | \n" +
                " |---|---| \n" +
                " | Claim amount | £ 1000 | \n" +
                " | Interest amount | £ 0 | \n" +
                " | Total amount | £ 1000 |");
        }
    }

    @Nested
    class MidCalculateSpecFee {

        @Test
        void shouldCalculateSpecFee_whenPopulated() {
            // Given
            List<TimelineOfEvents> timelineOfEvents = new ArrayList<>();
            timelineOfEvents.add(TimelineOfEvents.builder().value(TimelineOfEventDetails.builder().timelineDate(LocalDate.now().minusDays(1)).build()).build());
            CaseData caseData = CaseData.builder().claimInterest(YES)
                .interestClaimOptions(InterestClaimOptions.SAME_RATE_INTEREST)
                .sameRateInterestSelection(SameRateInterestSelection.builder()
                                               .sameRateInterestType(SameRateInterestType.SAME_RATE_INTEREST_8_PC).build())
                .interestClaimFrom(InterestClaimFromType.FROM_CLAIM_SUBMIT_DATE)
                .totalClaimAmount(new BigDecimal(1000))
                .build();
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
            CaseData caseData = CaseData.builder().specRespondent1Represented(NO)
                .build();

            CallbackParams params = callbackParamsOf(caseData, MID, "setRespondent2SameLegalRepresentativeToNo");
            // When
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            // Then
            assertThat(response.getData()).containsEntry("respondent2SameLegalRepresentative", "No");
        }
    }

    @Nested
    class ValidateEmails {

        @Nested
        class ClaimantRepEmail {

            @Test
            void shouldReturnNoErrors_whenIdamEmailIsCorrect() {
                // Given
                CaseData caseData = CaseData.builder()
                    .applicantSolicitor1CheckEmail(CorrectEmail.builder().correct(YES).build())
                    .build();

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

                CaseData caseData = CaseData.builder()
                    .applicantSolicitor1CheckEmail(CorrectEmail.builder().correct(NO).build())
                    .applicantSolicitor1UserDetails(IdamUserDetails.builder().email(validEmail).build())
                    .build();

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

                CaseData caseData = CaseData.builder()
                    .applicantSolicitor1CheckEmail(CorrectEmail.builder().correct(NO).build())
                    .applicantSolicitor1UserDetails(IdamUserDetails.builder().email(invalidEmail).build())
                    .build();

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

                CaseData caseData = CaseData.builder()
                    .respondentSolicitor1EmailAddress(validEmail)
                    .build();

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

                CaseData caseData = CaseData.builder()
                    .respondentSolicitor1EmailAddress(invalidEmail)
                    .build();

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

                CaseData caseData = CaseData.builder()
                    .respondentSolicitor1EmailAddress(validEmail)
                    .build();

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

                CaseData caseData = CaseData.builder()
                    .respondentSolicitor1EmailAddress(invalidEmail)
                    .build();

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

                CaseData caseData = CaseData.builder()
                    .respondentSolicitor2EmailAddress(validEmail)
                    .build();

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

                CaseData caseData = CaseData.builder()
                    .respondentSolicitor2EmailAddress(invalidEmail)
                    .build();

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

            @Test
            void shouldReturnNoErrors_whenRespondent1AddressValid() {
                // Given
                Party respondent1 = PartyBuilder.builder().company().build();

                CaseData caseData = CaseData.builder().respondent1(respondent1).build();

                CallbackParams params = callbackParamsOf(caseData, MID, "respondent1");

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
            void shouldReturnErrors_whenRespondent1AddressNotValid() {
                // Given
                Party respondent1 = Party.builder().primaryAddress(Address.builder().postCode(null).build()).build();

                CaseData caseData = CaseData.builder().respondent1(respondent1).build();

                CallbackParams params = callbackParamsOf(caseData, MID, "respondent1");

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
        class Respondent2Address {

            @Test
            void shouldReturnNoErrors_whenRespondent2AddressValid() {
                // Given
                Party respondent2 = PartyBuilder.builder().company().build();

                CaseData caseData = CaseData.builder().respondent2(respondent2).build();

                CallbackParams params = callbackParamsOf(caseData, MID, "respondent2");

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
            void shouldReturnErrors_whenRespondent2AddressNotValid() {
                // Given
                Party respondent2 = Party.builder().primaryAddress(Address.builder().postCode(null).build()).build();

                CaseData caseData = CaseData.builder().respondent2(respondent2).build();

                CallbackParams params = callbackParamsOf(caseData, MID, "respondent2");

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
        class RespondentSolicitor1Address {

            @Test
            void shouldReturnNoErrors_whenSolicitor1AddressValid() {
                // Given
                SolicitorOrganisationDetails respondentSolicitor1OrganisationDetails =
                    SolicitorOrganisationDetails.builder().address(AddressBuilder.defaults().build()).build();

                CaseData caseData = CaseData.builder()
                    .respondentSolicitor1OrganisationDetails(respondentSolicitor1OrganisationDetails).build();

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
                SolicitorOrganisationDetails respondentSolicitor1OrganisationDetails =
                    SolicitorOrganisationDetails.builder()
                        .address(Address.builder().postCode(null).build())
                        .build();

                CaseData caseData = CaseData.builder()
                    .respondentSolicitor1OrganisationDetails(respondentSolicitor1OrganisationDetails)
                    .build();

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
                    SolicitorOrganisationDetails.builder()
                        .address(AddressBuilder.defaults().build())
                        .build();

                CaseData caseData = CaseData.builder()
                    .respondentSolicitor2OrganisationDetails(respondentSolicitor2OrganisationDetails)
                    .build();

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
                SolicitorOrganisationDetails respondentSolicitor2OrganisationDetails =
                    SolicitorOrganisationDetails.builder().address(Address.builder().postCode(null).build()).build();

                CaseData caseData = CaseData.builder()
                    .respondentSolicitor2OrganisationDetails(respondentSolicitor2OrganisationDetails).build();

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
                CaseData caseData = CaseData.builder()
                    .specApplicantCorrespondenceAddressRequired(NO)
                    .build();

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
                CaseData caseData = CaseData.builder()
                    .specApplicantCorrespondenceAddressRequired(YES)
                    .specApplicantCorrespondenceAddressdetails(AddressBuilder.defaults().build())
                    .build();

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
                CaseData caseData = CaseData.builder()
                    .specApplicantCorrespondenceAddressRequired(YES)
                    .specApplicantCorrespondenceAddressdetails(Address.builder().postCode(null).build())
                    .build();

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
                CaseData caseData = CaseData.builder()
                    .specRespondentCorrespondenceAddressRequired(NO)
                    .build();

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
                CaseData caseData = CaseData.builder()
                    .specRespondentCorrespondenceAddressRequired(YES)
                    .specRespondentCorrespondenceAddressdetails(AddressBuilder.defaults().build())
                    .build();

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
                CaseData caseData = CaseData.builder()
                    .specRespondentCorrespondenceAddressRequired(YES)
                    .specRespondentCorrespondenceAddressdetails(Address.builder().postCode(null).build())
                    .build();

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

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
        void shouldSetIsFlightDelayClaim_whenPopulated(Boolean toggleStat) {
            // Given
            YesOrNo yesOrNo = toggleStat?YES:NO;
            CaseData caseData = CaseData.builder().isFlightDelayClaim(yesOrNo)
                .build();

            CallbackParams params = callbackParamsOf(caseData, MID, "is-flight-delay-claim");
            // When
            when(toggleService.isSdoR2Enabled()).thenReturn(true);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            // Then
            assertThat(response.getData()).containsEntry("isFlightDelayClaim", toggleStat?"Yes":"No");
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

            given(idamClient.getUserDetails(any()))
                .willReturn(UserDetails.builder().email(EMAIL).id(userId).build());

            given(time.now()).willReturn(submittedDate);
            given(toggleConfiguration.getFeatureToggle()).willReturn("WA 4");
            given(defendantPinToPostLRspecService.buildDefendantPinToPost())
                .willReturn(DefendantPinToPostLRspec.builder()
                                .accessCode(
                                    AccessCodeGenerator.generateAccessCode())
                                .respondentCaseRole(
                                    CaseRole.RESPONDENTSOLICITORONE.getFormattedName())
                                .expiryDate(LocalDate.now().plusDays(
                                    180))
                                .build());

            Organisation organisation = Organisation.builder()
                .paymentAccount(List.of("12345", "98765"))
                .build();
        }

        @Test
        void shouldSetClaimFee_whenInvokedAndBulkClaim() {
            // Given
            Fee feeData = Fee.builder()
                .code("FeeCode")
                .calculatedAmountInPence(BigDecimal.valueOf(19990))
                .build();
            CaseData caseData = CaseDataBuilder.builder().atStatePendingClaimIssued().build().toBuilder()
                .sdtRequestIdFromSdt("sdtRequestIdFromSdt")
                .totalClaimAmount(BigDecimal.valueOf(1999))
                .build();
            given(feesService.getFeeDataByTotalClaimAmount(any())).willReturn(feeData);
            when(interestCalculator.calculateInterest(caseData)).thenReturn(new BigDecimal(0));
            given(organisationService.findOrganisation(any())).willReturn(Optional.of(bulkOrganisation));
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
            // When
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            // Then
            assertThat(response.getData()).extracting("claimFee").extracting("calculatedAmountInPence", "code")
                .containsExactly(String.valueOf(feeData.getCalculatedAmountInPence()), feeData.getCode());
        }

        @Test
        void shouldAddSdtRequestId_whenInvokedAndBulkClaim() {
            // Given
            CaseData caseData = CaseDataBuilder.builder().atStatePendingClaimIssued().build().toBuilder()
                .sdtRequestIdFromSdt("sdtRequestIdFromSdt")
                .totalClaimAmount(BigDecimal.valueOf(1999))
                .build();
            when(interestCalculator.calculateInterest(caseData)).thenReturn(new BigDecimal(0));
            given(organisationService.findOrganisation(any())).willReturn(Optional.of(bulkOrganisation));
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
            // When
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            // Then
            assertThat(response.getData()).extracting("sdtRequestId").asString().contains("sdtRequestIdFromSdt");
        }

        @Test
        void shouldNotAddSdtRequestId_whenInvokedAndNotBulkClaim() {
            // Given
            CaseData caseData = CaseDataBuilder.builder().atStatePendingClaimIssued().build().toBuilder()
                .build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
            // When
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            // Then
            assertThat(response.getData()).extracting("sdtRequestId").isNull();
        }

        @Test
        void shouldAssignFirstPbaNumber_whenInvokedAndBulkClaim() {
            // Given
            CaseData caseData = CaseDataBuilder.builder().atStatePendingClaimIssued().build().toBuilder()
                .sdtRequestIdFromSdt("sdtRequestIdFromSdt")
                .totalClaimAmount(BigDecimal.valueOf(1999))
                .build();
            when(interestCalculator.calculateInterest(caseData)).thenReturn(new BigDecimal(0));
            given(organisationService.findOrganisation(any())).willReturn(Optional.of(bulkOrganisation));
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
            // When
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            // Then
            System.out.println(response.getData().get("applicantSolicitor1PbaAccounts"));
            assertThat(response.getData()).extracting("applicantSolicitor1PbaAccounts").asString().contains("12345");
            assertThat(response.getData()).extracting("applicantSolicitor1PbaAccounts").asString().doesNotContain("98765");
        }

        //TODO implement tests for bulk claims that have interest added.

        @Test
        void shouldSetCaseCategoryToSpec_whenInvoked() {
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getData())
                .containsEntry("CaseAccessCategory", CaseCategory.SPEC_CLAIM.toString());
        }

        @Test
        void shouldAddPartyIdsToPartyFields_whenInvoked() {
            when(toggleService.isHmcEnabled()).thenReturn(true);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getData()).extracting("applicant1").hasFieldOrProperty("partyID");
            assertThat(response.getData()).extracting("respondent1").hasFieldOrProperty("partyID");
        }

        @Test
        void shouldNotAddPartyIdsToPartyFields_whenInvokedWithHMCToggleOff() {
            when(toggleService.isHmcEnabled()).thenReturn(false);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getData()).extracting("applicant1")
                .isEqualTo(objMapper.convertValue(caseData.getApplicant1(), HashMap.class));
            assertThat(response.getData()).extracting("respondent1")
                .isEqualTo(objMapper.convertValue(caseData.getRespondent1(), HashMap.class));
        }

        @Test
        void shouldAddCaseReferenceSubmittedDateAndAllocatedTrack_whenInvoked() {
            // Given
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                    CallbackRequest.builder().eventId(CREATE_SERVICE_REQUEST_CLAIM.name()).build())
                .build();

            // When
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

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
            CaseData caseData = CaseDataBuilder.builder().atStateClaimNotified_1v2_andNotifyBothSolicitors().build();
            CallbackParams params = callbackParamsOf(V_1, caseData, ABOUT_TO_SUBMIT);

            // When
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            // Then
            assertThat(response.getData().get("caseNameHmctsInternal"))
                .isEqualTo("Mr. John Rambo v Mr. Sole Trader and Mr. John Rambo");
            assertThat(response.getData().get("caseManagementCategory")).extracting("value")
                .extracting("code").isEqualTo("Civil");
        }

        @Test
        void shouldAssignCaseName2v1_whenCaseIs2v1() {
            // Given
            CaseData caseData = CaseDataBuilder.builder().atStateClaimSubmitted2v1RespondentRegistered().build();
            CallbackParams params = callbackParamsOf(V_1, caseData, ABOUT_TO_SUBMIT);

            // When
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            // Then
            assertThat(response.getData().get("caseNameHmctsInternal"))
                .isEqualTo("Mr. John Rambo and Mr. Jason Rambo v Mr. Sole Trader");
            assertThat(response.getData().get("caseManagementCategory")).extracting("value")
                .extracting("code").isEqualTo("Civil");
        }

        @Test
        void shouldAssignCaseName1v1_whenCaseIs1v1() {
            // Given
            CaseData caseData = CaseDataBuilder.builder().atStateClaimNotified_1v1().build();
            CallbackParams params = callbackParamsOf(V_1, caseData, ABOUT_TO_SUBMIT);

            // When
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            // Then
            assertThat(response.getData().get("caseNameHmctsInternal"))
                .isEqualTo("Mr. John Rambo v Mr. Sole Trader");
            assertThat(response.getData().get("caseManagementCategory")).extracting("value")
                .extracting("code").isEqualTo("Civil");
        }

        @Test
        void shouldCopyRespondent1OrgPolicyReferenceForSameRegisteredSolicitorScenario_whenInvoked() {
            caseData = CaseDataBuilder.builder().atStateClaimIssued1v2AndSameRepresentative()
                .respondent2(Party.builder()
                                 .type(Party.Type.COMPANY)
                                 .companyName("Company 3")
                                 .build())
                .build().toBuilder()
                .specRespondentCorrespondenceAddressRequired(YES)
                .specRespondentCorrespondenceAddressdetails(Address.builder()
                                                                .postCode("Postcode")
                                                                .addressLine1("Address")
                                                                .build())
                .build();
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
            var callbackParams = params.toBuilder()
                .caseData(params.getCaseData().toBuilder()
                              .respondent1OrganisationPolicy(null)
                              .build())
                .build();
            when(toggleService.isNoticeOfChangeEnabled()).thenReturn(true);

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
            var callbackParams = params.toBuilder()
                .caseData(params.getCaseData().toBuilder().build())
                .build();
            when(toggleService.isNoticeOfChangeEnabled()).thenReturn(true);

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

                given(idamClient.getUserDetails(any()))
                    .willReturn(UserDetails.builder().email(EMAIL).id(userId).build());
            }

            @Test
            void shouldAddIdamEmailToIdamDetails_whenIdamEmailIsCorrect() {
                // Given
                CaseData caseData = CaseDataBuilder.builder().atStateClaimDraft().build().toBuilder()
                    .applicantSolicitor1CheckEmail(CorrectEmail.builder()
                                                       .email(EMAIL)
                                                       .correct(YES)
                                                       .build())
                    .applicantSolicitor1UserDetails(IdamUserDetails.builder()
                                                        .email(DIFFERENT_EMAIL)
                                                        .build())
                    .build();

                params = callbackParamsOf(V_1, caseData, ABOUT_TO_SUBMIT);

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
                given(idamClient.getUserDetails(any()))
                    .willReturn(UserDetails.builder().email(EMAIL).id(userId).build());

                CaseData caseData = CaseDataBuilder.builder().atStateClaimDraft().build().toBuilder()
                    .applicantSolicitor1CheckEmail(CorrectEmail.builder()
                                                       .email(EMAIL)
                                                       .correct(NO)
                                                       .build())
                    .applicantSolicitor1UserDetails(IdamUserDetails.builder()
                                                        .email(DIFFERENT_EMAIL)
                                                        .build())
                    .build();

                CallbackParams params = callbackParamsOf(V_1, caseData, ABOUT_TO_SUBMIT);

                // When
                var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

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

                CaseData data = caseData.toBuilder()
                    .uiStatementOfTruth(StatementOfTruth.builder().name(name).role(role).build())
                    .build();

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
    }

    @Nested
    class SubmittedCallback {

        @Nested
        class Respondent1DoesNotHaveLegalRepresentation {

            @Test
            void shouldReturnExpectedSubmittedCallbackResponse_whenRespondent1DoesNotHaveRepresentation() {
                // Given
                CaseData caseData = CaseDataBuilder.builder().atStateClaimIssuedUnrepresentedDefendants()
                    .legacyCaseReference("000MC001")
                    .build();
                CallbackParams params = callbackParamsOf(caseData, SUBMITTED);

                // When
                SubmittedCallbackResponse response = (SubmittedCallbackResponse) handler.handle(params);

                LocalDateTime serviceDeadline = now().plusDays(112).atTime(23, 59);

                String body = format(
                    LIP_CONFIRMATION_BODY,
                    format("/cases/case-details/%s#CaseDocuments", CASE_ID),
                    responsePackLink,
                    formatLocalDateTime(serviceDeadline, DATE_TIME_AT)
                ) + exitSurveyContentService.applicantSurvey();

                // Then
                assertThat(response).usingRecursiveComparison().isEqualTo(
                    SubmittedCallbackResponse.builder()
                        .confirmationHeader(format(
                            "# Your claim has been received and will progress offline%n## Claim number: %s",
                            REFERENCE_NUMBER
                        ))
                        .confirmationBody(body)
                        .build());
            }

            @Test
            void shouldReturnExpectedSubmittedCallbackResponse_whenRespondent1DoesNotHaveRepresentationAndPinPostEnabled() {
                // Given
                CaseData caseData = CaseDataBuilder.builder().atStateClaimIssuedUnrepresentedDefendants()
                    .legacyCaseReference("000MC001")
                    .addRespondent2(NO)
                    .addApplicant2(NO)
                    .build();
                CallbackParams params = callbackParamsOf(caseData, SUBMITTED);
                Mockito.when(toggleService.isPinInPostEnabled()).thenReturn(true);

                // When
                SubmittedCallbackResponse response = (SubmittedCallbackResponse) handler.handle(params);

                LocalDateTime serviceDeadline = now().plusDays(112).atTime(23, 59);

                String body = format(
                    CONFIRMATION_SUMMARY,
                    format("/cases/case-details/%s#CaseDocuments", CASE_ID),
                    responsePackLink,
                    formatLocalDateTime(serviceDeadline, DATE_TIME_AT)
                ) + exitSurveyContentService.applicantSurvey();

                // Then
                assertThat(response).usingRecursiveComparison().isEqualTo(
                    SubmittedCallbackResponse.builder()
                        .confirmationHeader(format(
                            "# Your claim has been received%n## Claim number: %s",
                            REFERENCE_NUMBER
                        ))
                        .confirmationBody(body)
                        .build());
            }
        }

        @Nested
        class Respondent1HasLegalRepresentation {

            @Test
            void shouldReturnExpectedSubmittedCallbackResponse_whenRespondent1HasRepresentation() {
                // Given
                Mockito.when(toggleService.isPbaV3Enabled()).thenReturn(false);
                CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified()
                    .legacyCaseReference("000MC001")
                    .build();
                CallbackParams params = CallbackParamsBuilder.builder().of(SUBMITTED, caseData).request(
                        CallbackRequest.builder().eventId(CREATE_CLAIM_SPEC.name()).build())
                    .build();

                // When
                SubmittedCallbackResponse response = (SubmittedCallbackResponse) handler.handle(params);

                String body = format(
                    SPEC_CONFIRMATION_SUMMARY,
                    format("/cases/case-details/%s#CaseDocuments", CASE_ID)
                ) + exitSurveyContentService.applicantSurvey();

                // Then
                assertThat(response).usingRecursiveComparison().isEqualTo(
                    SubmittedCallbackResponse.builder()
                        .confirmationHeader(format(
                            "# Your claim has been received%n## Claim number: %s",
                            REFERENCE_NUMBER
                        ))
                        .confirmationBody(body)
                        .build());
            }

            @Test
            void shouldReturnExpectedSubmittedCallbackResponse_whenRespondent1HasRepresentationAndPBAv3IsOn() {
                // Given
                Mockito.when(toggleService.isPbaV3Enabled()).thenReturn(true);
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
            void shouldReturnExpectedSubmittedCallbackResponse_whenRespondent1HasRepresentationAndEventIdMissing() {
                // Given
                Mockito.when(toggleService.isPbaV3Enabled()).thenReturn(false);
                CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified()
                    .legacyCaseReference("000MC001")
                    .build();
                CallbackParams params = callbackParamsOf(caseData, SUBMITTED);

                // When
                SubmittedCallbackResponse response = (SubmittedCallbackResponse) handler.handle(params);
                String body = format(
                    CONFIRMATION_SUMMARY,
                    format("/cases/case-details/%s#CaseDocuments", CASE_ID)
                ) + exitSurveyContentService.applicantSurvey();

                // Then
                assertThat(response).usingRecursiveComparison().isEqualTo(
                    SubmittedCallbackResponse.builder()
                        .confirmationHeader(format(
                            "# Your claim has been received%n## Claim number: %s",
                            REFERENCE_NUMBER
                        ))
                        .confirmationBody(body)
                        .build());
            }

            @Test
            void shouldReturnExpectedSubmittedCallbackResponse_whenRespondent1HasRepresentationAndPBAv3IsOnAndEventIdMissing() {
                // Given
                Mockito.when(toggleService.isPbaV3Enabled()).thenReturn(true);
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

        @Nested
        class Respondent1SolicitorOrgNotRegisteredInMyHmcts {

            @Test
            void shouldReturnExpectedSubmittedCallbackResponse_whenRespondent1SolicitorNotRegisteredInMyHmcts() {
                // Given
                CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified()
                    .respondent1Represented(YES)
                    .respondent1OrgRegistered(NO)
                    .legacyCaseReference("000MC001")
                    .build();
                CallbackParams params = callbackParamsOf(caseData, SUBMITTED);

                // When
                SubmittedCallbackResponse response = (SubmittedCallbackResponse) handler.handle(params);

                // Then
                assertThat(response).usingRecursiveComparison().isEqualTo(
                    SubmittedCallbackResponse.builder()
                        .confirmationHeader(format("# Your claim has been received and will progress offline%n## "
                                                       + "Claim number: %s", REFERENCE_NUMBER))
                        .confirmationBody(format(
                            LIP_CONFIRMATION_SCREEN,
                            format("/cases/case-details/%s#CaseDocuments", CASE_ID),
                            responsePackLink
                        ) + exitSurveyContentService.applicantSurvey())
                        .build());
            }
        }

        @Test
        void shouldReturnExpectedConfirmationPage() {
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
                    .confirmationHeader(format("# Your claim has been received and will progress offline%n## "
                                                   + "Claim number: %s", REFERENCE_NUMBER))
                    .confirmationBody(format(
                        SPEC_LIP_CONFIRMATION_SCREEN,
                        format("/cases/case-details/%s#CaseDocuments", CASE_ID),
                        responsePackLink,
                        n9aLink,
                        n9bLink,
                        n215Link
                    ) + exitSurveyContentService.applicantSurvey())
                    .build());
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
            when(toggleService.isPbaV3Enabled()).thenReturn(true);
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
}
