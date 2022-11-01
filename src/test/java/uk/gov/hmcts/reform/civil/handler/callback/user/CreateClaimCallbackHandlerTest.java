package uk.gov.hmcts.reform.civil.handler.callback.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
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
import uk.gov.hmcts.reform.civil.config.ClaimIssueConfiguration;
import uk.gov.hmcts.reform.civil.config.ExitSurveyConfiguration;
import uk.gov.hmcts.reform.civil.config.MockDatabaseConfiguration;
import uk.gov.hmcts.reform.civil.enums.CaseCategory;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.launchdarkly.FeatureToggleService;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.CorrectEmail;
import uk.gov.hmcts.reform.civil.model.CourtLocation;
import uk.gov.hmcts.reform.civil.model.Fee;
import uk.gov.hmcts.reform.civil.model.IdamUserDetails;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.ServedDocumentFiles;
import uk.gov.hmcts.reform.civil.model.StatementOfTruth;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.common.DynamicListElement;
import uk.gov.hmcts.reform.civil.model.referencedata.response.LocationRefData;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.sampledata.PartyBuilder;
import uk.gov.hmcts.reform.civil.service.DeadlinesCalculator;
import uk.gov.hmcts.reform.civil.service.ExitSurveyContentService;
import uk.gov.hmcts.reform.civil.service.FeesService;
import uk.gov.hmcts.reform.civil.service.OrganisationService;
import uk.gov.hmcts.reform.civil.service.Time;
import uk.gov.hmcts.reform.civil.service.flowstate.StateFlowEngine;
import uk.gov.hmcts.reform.civil.service.referencedata.LocationRefDataService;
import uk.gov.hmcts.reform.civil.utils.CourtLocationUtils;
import uk.gov.hmcts.reform.civil.utils.InterestCalculator;
import uk.gov.hmcts.reform.civil.validation.DateOfBirthValidator;
import uk.gov.hmcts.reform.civil.validation.OrgPolicyValidator;
import uk.gov.hmcts.reform.civil.validation.PostcodeValidator;
import uk.gov.hmcts.reform.civil.validation.ValidateEmailService;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;
import uk.gov.hmcts.reform.prd.model.Organisation;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static java.time.LocalDate.now;
import static java.time.format.DateTimeFormatter.ISO_DATE_TIME;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.MID;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.SUBMITTED;
import static uk.gov.hmcts.reform.civil.callback.CallbackVersion.V_1;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CREATE_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.AllocatedTrack.MULTI_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.handler.callback.user.CreateClaimCallbackHandler.CONFIRMATION_SUMMARY;
import static uk.gov.hmcts.reform.civil.handler.callback.user.CreateClaimCallbackHandler.LIP_CONFIRMATION_BODY;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.DATE_TIME_AT;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.formatLocalDateTime;
import static uk.gov.hmcts.reform.civil.model.common.DynamicList.fromList;
import static uk.gov.hmcts.reform.civil.utils.PartyUtils.getPartyNameBasedOnType;

@SpringBootTest(classes = {
    CreateClaimCallbackHandler.class,
    CaseDetailsConverter.class,
    ClaimIssueConfiguration.class,
    CourtLocationUtils.class,
    DateOfBirthValidator.class,
    DeadlinesCalculator.class,
    ExitSurveyConfiguration.class,
    ExitSurveyContentService.class,
    InterestCalculator.class,
    JacksonAutoConfiguration.class,
    LocationRefDataService.class,
    MockDatabaseConfiguration.class,
    OrgPolicyValidator.class,
    StateFlowEngine.class,
    PostcodeValidator.class,
    StateFlowEngine.class,
    ValidationAutoConfiguration.class,
    ValidateEmailService.class,
    OrganisationService.class},
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
    private IdamClient idamClient;

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
    private LocationRefDataService locationRefDataService;

    @MockBean
    private CourtLocationUtils courtLocationUtility;

    @Value("${civil.response-pack-url}")
    private String responsePackLink;

    @Nested
    class AboutToStartCallbackV0 {

        private static final String SUPER_CLAIM_KEY = "superClaimType";

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

        @Test
        void shouldSetSuperClaimType_WhenAboutToStartIsInvoked() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStatePendingClaimIssued()
                .build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_START);

            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            assertThat(response.getData().get(SUPER_CLAIM_KEY)).isEqualTo("UNSPEC_CLAIM");
        }
    }

    @Nested
    class MidEventApplicantCallback {

        private static final String PAGE_ID = "applicant";

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
    }

    @Nested
    class MidEventApplicant2Callback {

        private static final String PAGE_ID = "applicant2";

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
                .collect(Collectors.toList());

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

        private DynamicList getDynamicList(AboutToStartOrSubmitCallbackResponse response) {
            return mapper.convertValue(response.getData().get("applicantSolicitor1PbaAccounts"), DynamicList.class);
        }
    }

    @Nested
    class MidEventStartClaimCallback {

        private static final String PAGE_ID = "start-claim";

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
                when(featureToggleService.isCourtLocationDynamicListEnabled()).thenReturn(true);

                when(courtLocationUtility.getLocationsFromList(any()))
                    .thenReturn(fromList(List.of("Site 1 - Lane 1 - 123", "Site 2 - Lane 2 - 124")));

                CaseData caseData = CaseDataBuilder.builder()
                    .atStateClaimDetailsNotified()
                    .build();

                CallbackParams callbackParams = callbackParamsOf(V_1, caseData, MID, PAGE_ID);
                var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(callbackParams);

                DynamicList dynamicList = getDynamicList(response);

                List<String> courtlist = dynamicList.getListItems().stream()
                    .map(DynamicListElement::getLabel)
                    .collect(Collectors.toList());

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

            given(idamClient.getUserDetails(any()))
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

            given(idamClient.getUserDetails(any()))
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

            assertThat(response.getErrors()).containsExactly
                ("The legal representative details for the claimant and defendant are the same.  "
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

            assertThat(response.getErrors()).containsExactly
                ("The legal representative details for the claimant and defendant are the same.  "
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

            assertThat(response.getErrors()).containsExactly
                ("The legal representative details for the claimant and defendant are the same.  "
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

    // TODO: move this test case to AboutToSubmitCallbackV0 after release
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
            params = callbackParamsOf(V_1, caseData, ABOUT_TO_SUBMIT);
            userId = UUID.randomUUID().toString();

            given(idamClient.getUserDetails(any()))
                .willReturn(UserDetails.builder().email(EMAIL).id(userId).build());

            given(time.now()).willReturn(submittedDate);
            when(featureToggleService.isAccessProfilesEnabled()).thenReturn(true);
            when(featureToggleService.isCourtLocationDynamicListEnabled()).thenReturn(true);
        }

        //Move this test to AboutToSubmitCallbackV0 after CIV-3521 release and migration
        @Test
        void shouldSetCaseCategoryToUnspec_whenInvoked() {
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getData())
                .containsEntry("CaseAccessCategory", CaseCategory.UNSPEC_CLAIM.toString());
        }
    }

    @Nested
    class AboutToSubmitCallbackV0 {

        private CallbackParams params;
        private CaseData caseData;
        private String userId;

        private static final String EMAIL = "example@email.com";
        private static final String DIFFERENT_EMAIL = "other_example@email.com";
        private final LocalDateTime submittedDate = LocalDateTime.now();

        @BeforeEach
        void setup() {
            caseData = CaseDataBuilder.builder().atStateClaimDraft().build();
            params = callbackParamsOf(V_1, caseData, ABOUT_TO_SUBMIT);
            userId = UUID.randomUUID().toString();

            given(idamClient.getUserDetails(any()))
                .willReturn(UserDetails.builder().email(EMAIL).id(userId).build());

            given(time.now()).willReturn(submittedDate);
            when(featureToggleService.isCourtLocationDynamicListEnabled()).thenReturn(true);
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
                .containsOnly(CREATE_CLAIM.name(), "READY");
        }

        @Test
        void shouldClearClaimStartedFlag_whenInvoked() {
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(
                callbackParamsOf(V_1, caseData.toBuilder().claimStarted(YES).build(), ABOUT_TO_SUBMIT));

            assertThat(response.getData())
                .doesNotContainEntry("claimStarted", YES);
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

            assertThat(respondent2OrgPolicy).extracting("OrgPolicyReference").isEqualTo("org1PolicyReference");
            assertThat(respondent2OrgPolicy)
                .extracting("Organisation").extracting("OrganisationID")
                .isEqualTo("org1");
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

            assertThat(respondent2OrgPolicy).doesNotHaveToString("OrgPolicyReference");
            assertThat(respondent2OrgPolicy).doesNotHaveToString("Organisation");
        }

        @Test
        void shouldAssignCaseName1v2_whenCaseIs1v2GlobalSearchEnabled() {
            when(featureToggleService.isGlobalSearchEnabled()).thenReturn(true);
            CaseData caseData = CaseDataBuilder.builder().atStateClaimNotified_1v2_andNotifyBothSolicitors().build();

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(
                callbackParamsOf(V_1, caseData, ABOUT_TO_SUBMIT));

            assertThat(response.getData().get("caseNameHmctsInternal"))
                .isEqualTo("Mr. John Rambo v Mr. Sole Trader and Mr. John Rambo");
            assertThat(response.getData().get("caseManagementCategory")).extracting("value")
                .extracting("code").isEqualTo("Civil");
        }

        @Test
        void shouldAssignCaseName2v1_whenCaseIs2v1GlobalSearchEnabled() {
            when(featureToggleService.isGlobalSearchEnabled()).thenReturn(true);
            CaseData caseData = CaseDataBuilder.builder().atStateClaimSubmitted2v1RespondentRegistered().build();

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(
                callbackParamsOf(V_1, caseData, ABOUT_TO_SUBMIT));

            assertThat(response.getData().get("caseNameHmctsInternal"))
                .isEqualTo("Mr. John Rambo and Mr. Jason Rambo v Mr. Sole Trader");
            assertThat(response.getData().get("caseManagementCategory")).extracting("value")
                .extracting("code").isEqualTo("Civil");
        }

        @Test
        void shouldAssignCaseName1v1_whenCaseIs1v1GlobalSearchEnabled() {
            when(featureToggleService.isGlobalSearchEnabled()).thenReturn(true);
            CaseData caseData = CaseDataBuilder.builder().atStateClaimNotified_1v1().build();

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(
                callbackParamsOf(V_1, caseData, ABOUT_TO_SUBMIT));

            assertThat(response.getData().get("caseNameHmctsInternal"))
                .isEqualTo("Mr. John Rambo v Mr. Sole Trader");
            assertThat(response.getData().get("caseManagementCategory")).extracting("value")
                .extracting("code").isEqualTo("Civil");
        }

        @Nested
        class AddLegalRepDeadline {
            @Test
            void shouldSetAddLegalRepDeadline_whenInvoked() {
                when(featureToggleService.isNoticeOfChangeEnabled()).thenReturn(true);
                when(deadlinesCalculator.plus14DaysAt4pmDeadline(any())).thenReturn(submittedDate);
                caseData = CaseDataBuilder.builder().atStateClaimIssued1v1UnrepresentedDefendant().build();

                var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(
                    callbackParamsOf(V_1, caseData, ABOUT_TO_SUBMIT));

                assertThat(response.getData()).extracting("addLegalRepDeadline")
                    .isEqualTo(submittedDate.format(ISO_DATE_TIME));
            }

            @Test
            void shouldSetAddLegalRepDeadline_1v2_2LiPs_whenInvoked() {
                when(featureToggleService.isNoticeOfChangeEnabled()).thenReturn(true);
                when(deadlinesCalculator.plus14DaysAt4pmDeadline(any())).thenReturn(submittedDate);
                caseData = CaseDataBuilder.builder().atStateClaimIssuedUnrepresentedDefendants().build();

                var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(
                    callbackParamsOf(V_1, caseData, ABOUT_TO_SUBMIT));

                assertThat(response.getData()).extracting("addLegalRepDeadline")
                    .isEqualTo(submittedDate.format(ISO_DATE_TIME));
            }

            @Test
            void shouldSetAddLegalRepDeadline_1v2_1LiP_whenInvoked() {
                when(featureToggleService.isNoticeOfChangeEnabled()).thenReturn(true);
                when(deadlinesCalculator.plus14DaysAt4pmDeadline(any())).thenReturn(submittedDate);
                caseData = CaseDataBuilder.builder().atStateClaimIssuedUnrepresentedDefendant1().build();

                var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(
                    callbackParamsOf(V_1, caseData, ABOUT_TO_SUBMIT));

                assertThat(response.getData()).extracting("addLegalRepDeadline")
                    .isEqualTo(submittedDate.format(ISO_DATE_TIME));
            }

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
                var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

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
                CaseData caseData = CaseDataBuilder.builder()
                    .atStateClaimDraft()
                    .multiPartyClaimTwoDefendantSolicitors()
                    .build();

                var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(
                    callbackParamsOf(V_1, caseData, ABOUT_TO_SUBMIT));

                assertThat(response.getData())
                    .containsEntry("allPartyNames", "Mr. John Rambo V Mr. Sole Trader, Mr. John Rambo");
            }

            @Test
            void twoVOne() {
                CaseData caseData = CaseDataBuilder.builder()
                    .atStateClaimDraft()
                    .multiPartyClaimTwoApplicants()
                    .build();

                var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(
                    callbackParamsOf(V_1, caseData, ABOUT_TO_SUBMIT));

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
                    .extracting("applicantPreferredCourtLocationList").isNull();

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
                    .containsExactly("4", "192280");
            }
        }

        @Test
        void shouldUpdateCaseListAndUnassignedListData() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimDraft()
                .multiPartyClaimTwoDefendantSolicitors()
                .applicant1OrganisationPolicy(OrganisationPolicy.builder()
                                                  .orgPolicyReference("CLAIMANTREF1")
                                                  .build())
                .respondent1OrganisationPolicy(OrganisationPolicy.builder()
                                                   .orgPolicyReference("DEFENDANTREF1")
                                                   .build())
                .respondent2OrganisationPolicy(OrganisationPolicy.builder()
                                                   .orgPolicyReference("DEFENDANTREF2")
                                                   .build())
                .build();

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(
                callbackParamsOf(V_1, caseData, ABOUT_TO_SUBMIT));

            assertThat(response.getData())
                .containsEntry("unassignedCaseListDisplayOrganisationReferences",
                               "CLAIMANTREF1, DEFENDANTREF1, DEFENDANTREF2");
            assertThat(response.getData())
                .containsEntry("caseListDisplayDefendantSolicitorReferences", "6789, 01234");
        }

        @Test
        void shouldReturnExpectedErrorMessagesInResponse_whenInvokedWithNullCourtLocation() {
            CaseData data = caseData.toBuilder()
                .courtLocation(null)
                .build();

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(
                callbackParamsOf(V_1, data, ABOUT_TO_SUBMIT));

            assertThat(response.getErrors()).containsOnly("Court location code is required");
        }

        @Test
        void shouldReturnExpectedErrorMessagesInResponse_whenInvokedWithNullApplicantPreferredCourt() {
            CaseData data = caseData.toBuilder()
                .courtLocation(CourtLocation.builder().applicantPreferredCourtLocationList(null).build())
                .build();

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(
                callbackParamsOf(V_1, data, ABOUT_TO_SUBMIT));

            assertThat(response.getErrors()).containsOnly("Court location code is required");
        }

        @Nested
        class PopulateBlankOrgPolicies {
            @BeforeEach
            public void setup() {
                when(featureToggleService.isNoticeOfChangeEnabled()).thenReturn(true);
            }

            @Test
            void oneVOne() {
                CaseData caseData = CaseDataBuilder.builder()
                    .atStateClaimDraft()
                    .respondent2OrganisationPolicy(null)
                    .build();

                var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(
                    callbackParamsOf(V_1, caseData, ABOUT_TO_SUBMIT));

                assertThat(response.getData())
                    .extracting("respondent2OrganisationPolicy")
                    .extracting("OrgPolicyCaseAssignedRole")
                    .isEqualTo("[RESPONDENTSOLICITORTWO]");

                assertThat(response.getData())
                    .extracting("respondent2OrganisationPolicy")
                    .extracting("Organisation")
                    .isNull();
            }

            @Test
            void unrepresentedDefendants() {
                CaseData caseData = CaseDataBuilder.builder()
                    .atStateClaimDraft()
                    .respondent1OrganisationPolicy(null)
                    .respondent2OrganisationPolicy(null)
                    .build();

                var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(
                    callbackParamsOf(V_1, caseData, ABOUT_TO_SUBMIT));

                assertThat(response.getData())
                    .extracting("respondent1OrganisationPolicy")
                    .extracting("OrgPolicyCaseAssignedRole")
                    .isEqualTo("[RESPONDENTSOLICITORONE]");

                assertThat(response.getData())
                    .extracting("respondent1OrganisationPolicy")
                    .extracting("Organisation")
                    .isNull();

                assertThat(response.getData())
                    .extracting("respondent2OrganisationPolicy")
                    .extracting("OrgPolicyCaseAssignedRole")
                    .isEqualTo("[RESPONDENTSOLICITORTWO]");

                assertThat(response.getData())
                    .extracting("respondent2OrganisationPolicy")
                    .extracting("Organisation")
                    .isNull();
            }
        }
    }

    @Nested
    class SubmittedCallback {

        @Nested
        class RespondentsDoNotHaveLegalRepresentation {

            @Test
            void shouldReturnExpectedSubmittedCallbackResponse_whenRespondentsDoesNotHaveRepresentation() {
                CaseData caseData = CaseDataBuilder.builder().atStateClaimIssuedUnrepresentedDefendants().build();
                CallbackParams params = callbackParamsOf(caseData, SUBMITTED);
                SubmittedCallbackResponse response = (SubmittedCallbackResponse) handler.handle(params);

                LocalDateTime serviceDeadline = now().plusDays(112).atTime(23, 59);

                String body = format(
                    LIP_CONFIRMATION_BODY,
                    format("/cases/case-details/%s#CaseDocuments", CASE_ID),
                    responsePackLink,
                    formatLocalDateTime(serviceDeadline, DATE_TIME_AT)
                ) + exitSurveyContentService.applicantSurvey();

                assertThat(response).usingRecursiveComparison().isEqualTo(
                    SubmittedCallbackResponse.builder()
                        .confirmationHeader(format(
                            "# Your claim has been received and will progress offline%n## Claim number: %s",
                            REFERENCE_NUMBER
                        ))
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
                        .confirmationHeader(format(
                            "# Your claim has been received%n## Claim number: %s",
                            REFERENCE_NUMBER
                        ))
                        .confirmationBody(body)
                        .build());
            }
        }

        @Nested
        class Respondent1DoesNotHaveLegalRepresentation {

            @Test
            void shouldReturnExpectedSubmittedCallbackResponse_whenRespondentsDoesNotHaveRepresentation() {
                CaseData caseData = CaseDataBuilder.builder().atStateClaimIssuedUnrepresentedDefendant1().build();
                CallbackParams params = callbackParamsOf(caseData, SUBMITTED);
                SubmittedCallbackResponse response = (SubmittedCallbackResponse) handler.handle(params);

                LocalDateTime serviceDeadline = now().plusDays(112).atTime(23, 59);

                String body = format(
                    LIP_CONFIRMATION_BODY,
                    format("/cases/case-details/%s#CaseDocuments", CASE_ID),
                    responsePackLink,
                    formatLocalDateTime(serviceDeadline, DATE_TIME_AT)
                ) + exitSurveyContentService.applicantSurvey();

                assertThat(response).usingRecursiveComparison().isEqualTo(
                    SubmittedCallbackResponse.builder()
                        .confirmationHeader(format(
                            "# Your claim has been received and will progress offline%n## Claim number: %s",
                            REFERENCE_NUMBER
                        ))
                        .confirmationBody(body)
                        .build());
            }
        }

        @Nested
        class Respondent1SolicitorOrgNotRegisteredInMyHmcts {

            @Test
            void shouldReturnExpectedSubmittedCallbackResponse_whenRespondent1SolicitorNotRegisteredInMyHmcts() {
                CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified()
                    .respondent1Represented(YES)
                    .respondent1OrgRegistered(NO)
                    .build();
                CallbackParams params = callbackParamsOf(caseData, SUBMITTED);
                SubmittedCallbackResponse response = (SubmittedCallbackResponse) handler.handle(params);

                assertThat(response).usingRecursiveComparison().isEqualTo(
                    SubmittedCallbackResponse.builder()
                        .confirmationHeader(format("# Your claim has been received and will progress offline%n## "
                                                       + "Claim number: %s", REFERENCE_NUMBER))
                        .confirmationBody(format(
                            LIP_CONFIRMATION_BODY,
                            format("/cases/case-details/%s#CaseDocuments", CASE_ID),
                            responsePackLink
                        ) + exitSurveyContentService.applicantSurvey())
                        .build());
            }
        }

        @Nested
        class Respondent2DoesNotHaveLegalRepresentation {

            @Test
            void shouldReturnExpectedSubmittedCallbackResponse_whenRespondent2DoesNotHaveRepresentation() {
                CaseData caseData = CaseDataBuilder.builder().atStateClaimIssuedUnrepresentedDefendants().build();
                CallbackParams params = callbackParamsOf(caseData, SUBMITTED);
                SubmittedCallbackResponse response = (SubmittedCallbackResponse) handler.handle(params);

                LocalDateTime serviceDeadline = now().plusDays(112).atTime(23, 59);

                String body = format(
                    LIP_CONFIRMATION_BODY,
                    format("/cases/case-details/%s#CaseDocuments", CASE_ID),
                    responsePackLink,
                    formatLocalDateTime(serviceDeadline, DATE_TIME_AT)
                ) + exitSurveyContentService.applicantSurvey();

                assertThat(response).usingRecursiveComparison().isEqualTo(
                    SubmittedCallbackResponse.builder()
                        .confirmationHeader(format(
                            "# Your claim has been received and will progress offline%n## Claim number: %s",
                            REFERENCE_NUMBER
                        ))
                        .confirmationBody(body)
                        .build());
            }
        }

        @Nested
        class Respondent2SolicitorOrgNotRegisteredInMyHmcts {

            @Test
            void shouldReturnExpectedSubmittedCallbackResponse_whenRespondent2SolicitorNotRegisteredInMyHmcts() {
                CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified()
                    .respondent2Represented(YES)
                    .respondent2OrgRegistered(NO)
                    .build();
                CallbackParams params = callbackParamsOf(caseData, SUBMITTED);
                SubmittedCallbackResponse response = (SubmittedCallbackResponse) handler.handle(params);

                assertThat(response).usingRecursiveComparison().isEqualTo(
                    SubmittedCallbackResponse.builder()
                        .confirmationHeader(format("# Your claim has been received and will progress offline%n## "
                                                       + "Claim number: %s", REFERENCE_NUMBER))
                        .confirmationBody(format(
                            LIP_CONFIRMATION_BODY,
                            format("/cases/case-details/%s#CaseDocuments", CASE_ID),
                            responsePackLink
                        ) + exitSurveyContentService.applicantSurvey())
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
                        .confirmationHeader(format(
                            "# Your claim has been received%n## Claim number: %s",
                            REFERENCE_NUMBER
                        ))
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
                        .confirmationHeader(format(
                            "# Your claim has been received%n## Claim number: %s",
                            REFERENCE_NUMBER
                        ))
                        .confirmationBody(body)
                        .build());
            }
        }
    }
}
