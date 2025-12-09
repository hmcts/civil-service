package uk.gov.hmcts.reform.civil.handler.callback.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.helpers.LocationHelper;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.CaseLocationCivil;
import uk.gov.hmcts.reform.civil.model.dq.RequestedCourt;
import uk.gov.hmcts.reform.civil.model.referencedata.LocationRefData;
import uk.gov.hmcts.reform.civil.sampledata.CallbackParamsBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDetailsBuilder;
import uk.gov.hmcts.reform.civil.sampledata.PartyBuilder;
import uk.gov.hmcts.reform.civil.service.referencedata.LocationReferenceDataService;

import java.util.Optional;

import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.SUBMITTED;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.REFER_TO_JUDGE;
import static uk.gov.hmcts.reform.civil.handler.callback.user.CreateReferToJudgeCallbackHandler.CONFIRMATION_HEADER;

@ExtendWith(MockitoExtension.class)
public class CreateReferToJudgeCallbackHandlerTest extends BaseCallbackHandlerTest {

    @Mock
    private LocationHelper helper;

    @Mock
    private LocationReferenceDataService locationService;

    private CreateReferToJudgeCallbackHandler handler;
    private ObjectMapper objectMapper;

    public static final String REFERENCE_NUMBER = "000DC001";

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());
        handler = new CreateReferToJudgeCallbackHandler(locationService, helper, objectMapper);
    }

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

        @Test
        void shouldReturnExpectedAboutToSubmitResponse() {
            CaseData localCaseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build();
            CallbackParams localParams = callbackParamsOf(localCaseData, ABOUT_TO_SUBMIT);
            AboutToStartOrSubmitCallbackResponse response =
                (AboutToStartOrSubmitCallbackResponse) handler.handle(localParams);
            CaseData updatedData = objectMapper.convertValue(response.getData(), CaseData.class);

            assertThat(response).isNotNull();
            assertThat(updatedData.getIsReferToJudgeClaim()).isEqualTo(YesOrNo.YES);
        }

        @Test
        void shouldReturnExpectedAboutToSubmitResponseForLessThanThousandsPoundScenerio1() {
            RequestedCourt requestedCourt = new RequestedCourt();
            requestedCourt.setResponseCourtCode("123");
            given(helper.getClaimantRequestedCourt(any()))
                .willReturn(Optional.of(requestedCourt));

            given(helper.getMatching(any(), any()))
                .willReturn(Optional.of(LocationRefData.builder().courtLocationCode("123").build()));

            CaseData localCaseData = CaseDataBuilder.builder().atStateClaimDetailsNotified()
                .atStateClaimSubmittedSmallClaim()
                .setClaimTypeToUnspecClaim()
                .respondent1(PartyBuilder.builder().individual().build().toBuilder().partyID("res-1-party-id").build())
                .build();

            CallbackParams localParams = callbackParamsOf(localCaseData, ABOUT_TO_SUBMIT);
            AboutToStartOrSubmitCallbackResponse response =
                (AboutToStartOrSubmitCallbackResponse) handler.handle(localParams);
            CaseData updatedData = objectMapper.convertValue(response.getData(), CaseData.class);

            assertThat(response).isNotNull();
            assertThat(updatedData.getIsReferToJudgeClaim()).isEqualTo(YesOrNo.YES);
        }

        @Test
        void shouldReturnExpectedAboutToSubmitResponseForLessThanThousandsPoundScenerio2() {
            RequestedCourt requestedCourt = new RequestedCourt();
            requestedCourt.setResponseCourtCode("123");
            given(helper.getClaimantRequestedCourt(any()))
                .willReturn(Optional.of(requestedCourt));

            given(helper.getMatching(any(), any()))
                .willReturn(Optional.of(LocationRefData.builder().courtLocationCode("123").build()));

            CaseData localCaseData = CaseDataBuilder.builder().atStateClaimDetailsNotified()
                .atStateClaimSubmittedSmallClaim()
                .setClaimTypeToUnspecClaim()
                .respondent2(PartyBuilder.builder().individual().build().toBuilder().partyID("res-2-party-id").build())
                .build();

            CallbackParams localParams = callbackParamsOf(localCaseData, ABOUT_TO_SUBMIT);
            AboutToStartOrSubmitCallbackResponse response =
                (AboutToStartOrSubmitCallbackResponse) handler.handle(localParams);
            CaseData updatedData = objectMapper.convertValue(response.getData(), CaseData.class);

            assertThat(response).isNotNull();
            assertThat(updatedData.getIsReferToJudgeClaim()).isEqualTo(YesOrNo.YES);
        }

        @Test
        void shouldReturnExpectedAboutToSubmitResponseForLessThanThousandsPoundScenerio3() {
            CaseData localCaseData = CaseDataBuilder.builder().atStateClaimDetailsNotified()
                .atStateClaimSubmitted()
                .setClaimTypeToSpecClaim()
                .respondent1(PartyBuilder.builder().individual().build().toBuilder().partyID("res-1-party-id").build())
                .build();

            CallbackParams localParams = callbackParamsOf(localCaseData, ABOUT_TO_SUBMIT);
            AboutToStartOrSubmitCallbackResponse response =
                (AboutToStartOrSubmitCallbackResponse) handler.handle(localParams);
            CaseData updatedData = objectMapper.convertValue(response.getData(), CaseData.class);

            assertThat(response).isNotNull();
            assertThat(updatedData.getIsReferToJudgeClaim()).isEqualTo(YesOrNo.YES);
        }

        @Test
        void thereIsAMatchingLocation() {
            CaseData updatedData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build();

            LocationHelper.updateWithLocation(updatedData, LocationRefData.builder()
                .courtLocationCode("123").regionId("regionId").region("region name").epimmsId("epimms").build());

            CaseLocationCivil caseLocationCivil = new CaseLocationCivil();
            caseLocationCivil.setRegion("regionId");
            caseLocationCivil.setBaseLocation("epimms");
            Assertions.assertThat(updatedData.getCaseManagementLocation())
                .isNotNull()
                .isEqualTo(caseLocationCivil);
        }

        @Test
        void shouldReturnExpectedAboutToSubmitResponseForLiP() {
            CaseData localCaseData = CaseDataBuilder.builder().atStateClaimDetailsNotified()
                .atStateClaimSubmitted()
                .setClaimTypeToSpecClaim()
                .respondent1Represented(YesOrNo.NO)
                .build();

            CallbackParams localParams = callbackParamsOf(localCaseData, ABOUT_TO_SUBMIT);
            AboutToStartOrSubmitCallbackResponse response =
                (AboutToStartOrSubmitCallbackResponse) handler.handle(localParams);
            CaseData updatedData = objectMapper.convertValue(response.getData(), CaseData.class);

            assertThat(response).isNotNull();
            assertThat(updatedData.getIsReferToJudgeClaim()).isEqualTo(YesOrNo.YES);
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
