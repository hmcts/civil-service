package uk.gov.hmcts.reform.civil.handler.callback.user;

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
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.config.ClaimUrlsConfiguration;
import uk.gov.hmcts.reform.civil.config.MockDatabaseConfiguration;
import uk.gov.hmcts.reform.civil.enums.ClaimType;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.repositories.SpecReferenceNumberRepository;
import uk.gov.hmcts.reform.civil.sampledata.CallbackParamsBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.DeadlinesCalculator;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.Time;
import uk.gov.hmcts.reform.civil.service.citizenui.HelpWithFeesForTabService;
import uk.gov.hmcts.reform.civil.service.flowstate.StateFlowEngine;
import uk.gov.hmcts.reform.civil.service.pininpost.DefendantPinToPostLRspecService;
import uk.gov.hmcts.reform.civil.utils.CaseFlagsInitialiser;
import uk.gov.hmcts.reform.civil.utils.InterestCalculator;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CREATE_LIP_CLAIM;

@SpringBootTest(classes = {
    CreateClaimLipCallBackHandler.class,
    JacksonAutoConfiguration.class,
    CaseDetailsConverter.class,
    ClaimUrlsConfiguration.class,
    MockDatabaseConfiguration.class,
    ValidationAutoConfiguration.class,
    StateFlowEngine.class,
    InterestCalculator.class,
    StateFlowEngine.class,
    },
    properties = {"reference.database.enabled=false"})
class CreateClaimLipCallbackHandlerTest extends BaseCallbackHandlerTest {

    @MockBean
    private DeadlinesCalculator deadlinesCalculator;

    @MockBean
    private FeatureToggleService toggleService;

    @MockBean
    private SpecReferenceNumberRepository specReferenceNumberRepository;

    @MockBean
    private CaseFlagsInitialiser caseFlagInitialiser;

    @MockBean
    private Time time;

    @MockBean
    private DefendantPinToPostLRspecService defendantPinToPostLRspecService;

    @MockBean
    private HelpWithFeesForTabService hwfForTabService;

    @Autowired
    private CreateClaimLipCallBackHandler handler;

    public static final String REFERENCE_NUMBER = "000MC001";

    @Autowired
    private ObjectMapper mapper;

    @Nested
    class AboutToStatCallback {

        @Test
        void shouldNotHaveErrors_whenAboutToStart() {
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
    class AboutToSubmitCallback {

        private CallbackParams params;
        private CaseData caseData;
        private static final String DEFENDANT_EMAIL_ADDRESS = "defendantmail@hmcts.net";
        private static final String DEFENDANT_PARTY_NAME = "ABC ABC";

        private final LocalDateTime submittedDate = LocalDateTime.now();

        @BeforeEach
        void setup() {
            caseData = CaseDataBuilder.builder().atStateClaimDraft().applicant1OrganisationPolicy(null).build();
            params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
            given(time.now()).willReturn(submittedDate);
            given(specReferenceNumberRepository.getSpecReferenceNumber()).willReturn(REFERENCE_NUMBER);
            given(deadlinesCalculator.plus28DaysAt4pmDeadline(any())).willReturn(submittedDate);
            when(toggleService.isHmcEnabled()).thenReturn(false);
        }

        @Test
        void shouldInitializePartyID_whenInvoked() {
            when(toggleService.isHmcEnabled()).thenReturn(true);
            caseData = CaseDataBuilder.builder()
                .respondent1(Party.builder()
                                 .type(Party.Type.INDIVIDUAL)
                                 .partyName(DEFENDANT_PARTY_NAME)
                                 .partyEmail(DEFENDANT_EMAIL_ADDRESS).build())
                .applicant1(Party.builder()
                                 .type(Party.Type.ORGANISATION)
                                 .partyName("Test Inc")
                                 .partyEmail("claimant@email.com").build())
                .build();

            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                    CallbackRequest.builder().eventId(CREATE_LIP_CLAIM.name()).build())
                .build();
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);
            assertThat(updatedData.getRespondent1().getPartyID()).isNotNull();
            assertThat(updatedData.getApplicant1().getPartyID()).isNotNull();
        }

        @Test
        void shouldAddCaseReferenceSubmittedDateAndAllocatedTrack_whenInvoked() {
            caseData = CaseDataBuilder.builder()
                .respondent1(Party.builder()
                                 .type(Party.Type.INDIVIDUAL)
                                 .partyName(DEFENDANT_PARTY_NAME)
                                 .partyEmail(DEFENDANT_EMAIL_ADDRESS).build())
                .build();

            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                    CallbackRequest.builder().eventId(CREATE_LIP_CLAIM.name()).build())
                .build();
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getData())
                .containsEntry("legacyCaseReference", REFERENCE_NUMBER)
                .containsEntry("submittedDate", submittedDate.format(DateTimeFormatter.ISO_DATE_TIME));

            CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);
            assertThat(updatedData.getRespondent1DetailsForClaimDetailsTab().getPartyName().equals(DEFENDANT_PARTY_NAME));
            assertThat(updatedData.getRespondent1DetailsForClaimDetailsTab().getType().equals(Party.Type.INDIVIDUAL));
        }

        @Test
        void shouldSetOrganisationPolicies_whenInvoked() {
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                    CallbackRequest.builder().eventId(CREATE_LIP_CLAIM.name()).build())
                .build();
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getData())
                .extracting("respondent1OrganisationPolicy.OrgPolicyCaseAssignedRole")
                .isEqualTo("[RESPONDENTSOLICITORONE]");
            assertThat(response.getData())
                .extracting("applicant1OrganisationPolicy.OrgPolicyCaseAssignedRole")
                .isEqualTo("[APPLICANTSOLICITORONE]");
        }

        @Test
        void shouldSetCaseManagementLocation() {
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                    CallbackRequest.builder().eventId(CREATE_LIP_CLAIM.name()).build())
                .build();
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getData())
                .extracting("caseManagementLocation.region")
                .isEqualTo("2");
            assertThat(response.getData())
                .extracting("caseManagementLocation.baseLocation")
                .isEqualTo("420219");
        }

        @Test
        void shouldSetFlightDelayType() {
            caseData.setIsFlightDelayClaim(YesOrNo.YES);
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                    CallbackRequest.builder().eventId(CREATE_LIP_CLAIM.name()).build())
                .build();
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getData())
                .extracting("claimType")
                .isEqualTo(ClaimType.FLIGHT_DELAY.name());
        }
    }
}
