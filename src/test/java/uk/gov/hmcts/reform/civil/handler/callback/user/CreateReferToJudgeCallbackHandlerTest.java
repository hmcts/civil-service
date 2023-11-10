package uk.gov.hmcts.reform.civil.handler.callback.user;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.autoconfigure.validation.ValidationAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.config.ClaimUrlsConfiguration;
import uk.gov.hmcts.reform.civil.config.MockDatabaseConfiguration;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.helpers.LocationHelper;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.CaseLocationCivil;
import uk.gov.hmcts.reform.civil.model.dq.RequestedCourt;
import uk.gov.hmcts.reform.civil.referencedata.LocationRefDataService;
import uk.gov.hmcts.reform.civil.referencedata.model.LocationRefData;
import uk.gov.hmcts.reform.civil.sampledata.CallbackParamsBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDetailsBuilder;
import uk.gov.hmcts.reform.civil.sampledata.PartyBuilder;
import uk.gov.hmcts.reform.civil.service.Time;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.SUBMITTED;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.REFER_TO_JUDGE;
import static uk.gov.hmcts.reform.civil.handler.callback.user.CreateReferToJudgeCallbackHandler.CONFIRMATION_HEADER;

@SpringBootTest(classes = {
    CreateReferToJudgeCallbackHandler.class,
    JacksonAutoConfiguration.class,
    CaseDetailsConverter.class,
    ClaimUrlsConfiguration.class,
    MockDatabaseConfiguration.class,
    ValidationAutoConfiguration.class},
    properties = {"reference.database.enabled=false"})
public class CreateReferToJudgeCallbackHandlerTest extends BaseCallbackHandlerTest {

    public static final String REFERENCE_NUMBER = "000DC001";
    @MockBean
    private LocationHelper helper;
    @MockBean
    private Time time;
    @MockBean
    private IdamClient idamClient;
    @Autowired
    private CreateReferToJudgeCallbackHandler handler;
    @MockBean
    private LocationRefDataService locationService;

    @Nested
    class AboutToStartCallback {

        @Test
        void shouldReturnNoError_WhenAboutToStartIsInvoked() {
            CaseDetails caseDetails = CaseDetailsBuilder.builder().atStatePendingClaimIssued().build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_START, caseDetails).build();

            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            assertThat(response.getErrors()).isNull();
        }
    }

    @Nested
    class AboutToSubmitCallback {

        private static final String EMAIL = "example@email.com";
        private final LocalDateTime submittedDate = LocalDateTime.now();
        private CallbackParams params;
        private CaseData caseData;
        private String userId;

        @BeforeEach
        void setup() {
            caseData = CaseDataBuilder.builder().atStateClaimDraft().build();
            params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
            userId = UUID.randomUUID().toString();

            given(helper.leadDefendantIs1(any()))
                    .willReturn(true);

            given(idamClient.getUserDetails(any()))
                .willReturn(UserDetails.builder().email(EMAIL).id(userId).build());

            given(time.now()).willReturn(submittedDate);
        }

        @Test
        void shouldReturnExpectedAboutToSubmitResponse() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
            AboutToStartOrSubmitCallbackResponse response =
                (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            assertThat(response).isNotNull();

        }

        @Test
        void shouldReturnExpectedAboutToSubmitResponseForLessThanThousandsPoundScenerio1() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified()
                .atStateClaimSubmittedSmallClaim()
                .setClaimTypeToUnspecClaim()
                .respondent1(PartyBuilder.builder().individual().build().toBuilder().partyID("res-1-party-id").build())
                .build();

            given(helper.getClaimantRequestedCourt(any()))
                .willReturn(Optional.of(RequestedCourt.builder().responseCourtCode("123").build()));

            given(helper.getMatching(any(), any()))
                .willReturn(Optional.of(LocationRefData.builder().courtLocationCode("123").build()));

            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
            AboutToStartOrSubmitCallbackResponse response =
                (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            assertThat(response).isNotNull();

        }

        @Test
        void shouldReturnExpectedAboutToSubmitResponseForLessThanThousandsPoundScenerio2() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified()
                .atStateClaimSubmittedSmallClaim()
                .setClaimTypeToUnspecClaim()
                .respondent2(PartyBuilder.builder().individual().build().toBuilder().partyID("res-2-party-id").build())
                .build();

            given(helper.leadDefendantIs1(any()))
                .willReturn(false);

            given(helper.getClaimantRequestedCourt(any()))
                .willReturn(Optional.of(RequestedCourt.builder().responseCourtCode("123").build()));

            given(helper.getMatching(any(), any()))
                .willReturn(Optional.of(LocationRefData.builder().courtLocationCode("123").build()));

            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
            AboutToStartOrSubmitCallbackResponse response =
                (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            assertThat(response).isNotNull();

        }

        @Test
        void shouldReturnExpectedAboutToSubmitResponseForLessThanThousandsPoundScenerio3() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified()
                .atStateClaimSubmitted()
                .setClaimTypeToSpecClaim()
                .respondent1(PartyBuilder.builder().individual().build().toBuilder().partyID("res-1-party-id").build())
                .build();

            given(helper.getClaimantRequestedCourt(any()))
                .willReturn(Optional.of(RequestedCourt.builder().responseCourtCode("123").build()));

            given(helper.getMatching(any(), any()))
                .willReturn(Optional.of(LocationRefData.builder().courtLocationCode("123").build()));

            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
            AboutToStartOrSubmitCallbackResponse response =
                (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            assertThat(response).isNotNull();

        }

        @Test
        public void thereIsAMatchingLocation() {
            CaseData.CaseDataBuilder<?, ?> updatedData = CaseData.builder();

            helper.updateWithLocation(updatedData, LocationRefData.builder()
                .courtLocationCode("123").regionId("regionId").region("region name").epimmsId("epimms").build());

            Assertions.assertThat(updatedData.build().getCaseManagementLocation())
                .isNotNull()
                .isEqualTo(CaseLocationCivil.builder()
                               .region("regionId")
                               .baseLocation("epimms")
                               .build());
        }
    }

    @Nested
    class SubmittedCallback {
        @Test
        void shouldReturnExpectedSubmittedCallbackResponse() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build();
            CallbackParams params = callbackParamsOf(caseData, SUBMITTED);
            SubmittedCallbackResponse response = (SubmittedCallbackResponse) handler.handle(params);

            String header = format(
                CONFIRMATION_HEADER,
                REFERENCE_NUMBER
            );

            assertThat(response).usingRecursiveComparison().isEqualTo(
                SubmittedCallbackResponse.builder()
                    .confirmationHeader(header)
                    .confirmationBody("<p>&nbsp;</p>")
                    .build());
        }
    }

    @Test
    void handleEventsReturnsTheExpectedCallbackEvent() {
        assertThat(handler.handledEvents()).contains(REFER_TO_JUDGE);
    }
}
