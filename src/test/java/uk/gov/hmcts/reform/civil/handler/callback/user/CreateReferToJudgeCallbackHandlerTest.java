package uk.gov.hmcts.reform.civil.handler.callback.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.helpers.LocationHelper;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.CaseLocationCivil;
import uk.gov.hmcts.reform.civil.model.dq.RequestedCourt;
import uk.gov.hmcts.reform.civil.referencedata.model.LocationRefData;
import uk.gov.hmcts.reform.civil.sampledata.CallbackParamsBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDetailsBuilder;
import uk.gov.hmcts.reform.civil.sampledata.PartyBuilder;
import uk.gov.hmcts.reform.civil.service.Time;
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
    private Time time;

    @InjectMocks
    private CreateReferToJudgeCallbackHandler handler;

    @Mock
    private LocationReferenceDataService locationService;

    @Mock
    private ObjectMapper objectMapper;

    public static final String REFERENCE_NUMBER = "000DC001";

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
            assertThat(response).isNotNull();
        }

        @Test
        void shouldReturnExpectedAboutToSubmitResponseForLessThanThousandsPoundScenerio1() {
            CaseData localCaseData = CaseDataBuilder.builder().atStateClaimDetailsNotified()
                .atStateClaimSubmittedSmallClaim()
                .setClaimTypeToUnspecClaim()
                .respondent1(PartyBuilder.builder().individual().build().toBuilder().partyID("res-1-party-id").build())
                .build();

            given(helper.getClaimantRequestedCourt(any()))
                .willReturn(Optional.of(RequestedCourt.builder().responseCourtCode("123").build()));

            given(helper.getMatching(any(), any()))
                .willReturn(Optional.of(LocationRefData.builder().courtLocationCode("123").build()));

            CallbackParams localParams = callbackParamsOf(localCaseData, ABOUT_TO_SUBMIT);
            AboutToStartOrSubmitCallbackResponse response =
                (AboutToStartOrSubmitCallbackResponse) handler.handle(localParams);
            assertThat(response).isNotNull();
        }

        @Test
        void shouldReturnExpectedAboutToSubmitResponseForLessThanThousandsPoundScenerio2() {
            CaseData localCaseData = CaseDataBuilder.builder().atStateClaimDetailsNotified()
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

            CallbackParams localParams = callbackParamsOf(localCaseData, ABOUT_TO_SUBMIT);
            AboutToStartOrSubmitCallbackResponse response =
                (AboutToStartOrSubmitCallbackResponse) handler.handle(localParams);
            assertThat(response).isNotNull();
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
            assertThat(response).isNotNull();
        }

        @Test
        void thereIsAMatchingLocation() {
            CaseData.CaseDataBuilder<?, ?> updatedData = CaseData.builder();

            LocationHelper.updateWithLocation(updatedData, LocationRefData.builder()
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
