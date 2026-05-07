package uk.gov.hmcts.reform.civil.handler.callback.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.SUBMITTED;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.REFER_TO_JUDGE;

import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.Event;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
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
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;
import uk.gov.hmcts.reform.civil.service.referencedata.LocationReferenceDataService;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Map;

@ExtendWith(MockitoExtension.class)
public class CreateReferToJudgeCallbackHandlerTest extends BaseCallbackHandlerTest {

    @Mock
    private LocationHelper helper;

    @Mock
    private LocationReferenceDataService locationService;

    private CreateReferToJudgeCallbackHandler handler;
    private ObjectMapper objectMapper;

    public static final String REFERENCE_NUMBER = "000DC001";

    @Mock
    private CoreCaseDataService coreCaseDataService;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());
        handler = new CreateReferToJudgeCallbackHandler(locationService, helper, coreCaseDataService, objectMapper);
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
            //assertThat(updatedData.getIsReferToJudgeClaim()).isEqualTo(YesOrNo.YES);
        }

        @Test
        void shouldReturnExpectedAboutToSubmitResponseForLessThanThousandsPoundScenerio1() {
            RequestedCourt requestedCourt = new RequestedCourt();
            requestedCourt.setResponseCourtCode("123");
            //          given(helper.getClaimantRequestedCourt(any()))
            //              .willReturn(Optional.of(requestedCourt));
            //
            //          given(helper.getMatching(any(), any()))
            //              .willReturn(Optional.of(new LocationRefData().setCourtLocationCode("123")));

            CaseData localCaseData = CaseDataBuilder.builder().atStateClaimDetailsNotified()
                .atStateClaimSubmittedSmallClaim()
                .setClaimTypeToUnspecClaim()
                .respondent1(new PartyBuilder().individual().build().setPartyID("res-1-party-id"))
                .build();

            CallbackParams localParams = callbackParamsOf(localCaseData, ABOUT_TO_SUBMIT);
            AboutToStartOrSubmitCallbackResponse response =
                (AboutToStartOrSubmitCallbackResponse) handler.handle(localParams);
            CaseData updatedData = objectMapper.convertValue(response.getData(), CaseData.class);

            assertThat(response).isNotNull();
            //assertThat(updatedData.getIsReferToJudgeClaim()).isEqualTo(YesOrNo.YES);
        }

        @Test
        void shouldReturnExpectedAboutToSubmitResponseForLessThanThousandsPoundScenerio2() {
            RequestedCourt requestedCourt = new RequestedCourt();
            requestedCourt.setResponseCourtCode("123");
            //          given(helper.getClaimantRequestedCourt(any()))
            //              .willReturn(Optional.of(requestedCourt));
            //
            //          given(helper.getMatching(any(), any()))
            //              .willReturn(Optional.of(new LocationRefData().setCourtLocationCode("123")));

            CaseData localCaseData = CaseDataBuilder.builder().atStateClaimDetailsNotified()
                .atStateClaimSubmittedSmallClaim()
                .setClaimTypeToUnspecClaim()
                .respondent2(new PartyBuilder().individual().build().setPartyID("res-2-party-id"))
                .build();

            CallbackParams localParams = callbackParamsOf(localCaseData, ABOUT_TO_SUBMIT);
            AboutToStartOrSubmitCallbackResponse response =
                (AboutToStartOrSubmitCallbackResponse) handler.handle(localParams);
            CaseData updatedData = objectMapper.convertValue(response.getData(), CaseData.class);

            assertThat(response).isNotNull();
            //assertThat(updatedData.getIsReferToJudgeClaim()).isEqualTo(YesOrNo.YES);
        }

        @Test
        void shouldReturnExpectedAboutToSubmitResponseForLessThanThousandsPoundScenerio3() {
            CaseData localCaseData = CaseDataBuilder.builder().atStateClaimDetailsNotified()
                .atStateClaimSubmitted()
                .setClaimTypeToSpecClaim()
                .respondent1(new PartyBuilder().individual().build().setPartyID("res-1-party-id"))
                .build();

            CallbackParams localParams = callbackParamsOf(localCaseData, ABOUT_TO_SUBMIT);
            AboutToStartOrSubmitCallbackResponse response =
                (AboutToStartOrSubmitCallbackResponse) handler.handle(localParams);
            CaseData updatedData = objectMapper.convertValue(response.getData(), CaseData.class);

            assertThat(response).isNotNull();
            //assertThat(updatedData.getIsReferToJudgeClaim()).isEqualTo(YesOrNo.YES);
        }

        @Test
        void thereIsAMatchingLocation() {
            CaseData updatedData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build();

            LocationHelper.updateWithLocation(
                updatedData, new LocationRefData()
                    .setCourtLocationCode("123").setRegionId("regionId").setRegion("region name").setEpimmsId("epimms")
            );

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
            //assertThat(updatedData.getIsReferToJudgeClaim()).isEqualTo(YesOrNo.YES);
        }
    }

    //    @Nested
    //    class SubmittedCallback {
    //        @Test
    //        void shouldReturnExpectedSubmittedCallbackResponse() {
    //            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build();
    //            CallbackParams params = callbackParamsOf(caseData, SUBMITTED);
    //            CaseDetails caseDetails = new CaseDetailsBuilder().data(caseData).build();
    //            StartEventResponse startEventResponse = StartEventResponse.builder()
    //                .token("1594901956117591")
    //                .eventId(ADD_PDF_TO_MAIN_CASE.name())
    //                .caseDetails(caseDetails)
    //                .build();
    //            when(coreCaseDataService.startUpdate(caseData.getCcdCaseReference().toString(), REFER_TO_JUDGE)).thenReturn(startEventResponse);
    //            SubmittedCallbackResponse response = (SubmittedCallbackResponse) handler.handle(params);
    //
    //            String header = format(
    //                CONFIRMATION_HEADER,
    //                REFERENCE_NUMBER
    //            );
    //
    //            assertThat(response).usingRecursiveComparison().isEqualTo(
    //                SubmittedCallbackResponse.builder()
    //                    .confirmationHeader(header)
    //                    .confirmationBody("<p>&nbsp;</p>")
    //                    .build());
    //        }
    //  }

    @Test
    void handleEventsReturnsTheExpectedCallbackEvent() {
        assertThat(handler.handledEvents()).contains(REFER_TO_JUDGE);
    }

    @Test
    void shouldBuildConfirmationOnSubmitted_andSubmitReferToJudgeEvent() {
        CaseData caseData = CaseDataBuilder.builder()
            .atStateClaimDetailsNotified()
            .ccdCaseReference(CASE_ID)
            .legacyCaseReference(REFERENCE_NUMBER)
            .build();
        caseData.setEventDescription("Some summary");
        caseData.setAdditionalInformation(null);

        Map<String, Object> startData = new HashMap<>();
        CaseDetails startCaseDetails = CaseDetails.builder().data(startData).build();
        StartEventResponse startEventResponse = StartEventResponse.builder()
            .token("TOKEN")
            .eventId("EVENT_ID")
            .caseDetails(startCaseDetails)
            .build();

        CaseData submittedCaseData = CaseDataBuilder.builder()
            .legacyCaseReference(REFERENCE_NUMBER)
            .ccdCaseReference(CASE_ID)
            .build();

        when(coreCaseDataService.startUpdate(CASE_ID.toString(), REFER_TO_JUDGE)).thenReturn(startEventResponse);
        when(coreCaseDataService.submitUpdate(eq(CASE_ID.toString()), any(CaseDataContent.class))).thenReturn(submittedCaseData);

        CallbackParams params = callbackParamsOf(caseData, SUBMITTED);

        SubmittedCallbackResponse response = (SubmittedCallbackResponse) handler.handle(params);

        assertThat(response.getConfirmationHeader()).isEqualTo(
            String.format(CreateReferToJudgeCallbackHandler.CONFIRMATION_HEADER, REFERENCE_NUMBER)
        );
        assertThat(response.getConfirmationBody()).isEqualTo("<p>&nbsp;</p>");

        ArgumentCaptor<CaseDataContent> captor = ArgumentCaptor.forClass(CaseDataContent.class);
        verify(coreCaseDataService).submitUpdate(eq(CASE_ID.toString()), captor.capture());

        CaseDataContent content = captor.getValue();
        assertThat(content.getEventToken()).isEqualTo("TOKEN");
        Event event = content.getEvent();
        assertThat(event.getId()).isEqualTo("EVENT_ID");
        assertThat(event.getSummary()).isEqualTo("Some summary");
        assertThat(event.getDescription()).isNull();

        // Data should include ReferToJudge flag
        assertThat(content.getData()).hasFieldOrPropertyWithValue("isReferToJudgeClaim", YesOrNo.YES);
    }

    @Test
    void shouldIncludeAdditionalInformationInSubmittedEventDescription_whenProvided() {
        CaseData caseData = CaseDataBuilder.builder()
            .atStateClaimDetailsNotified()
            .ccdCaseReference(CASE_ID)
            .legacyCaseReference(REFERENCE_NUMBER)
            .build();
        caseData.setEventDescription("Summary");
        caseData.setAdditionalInformation("Extra details");

        Map<String, Object> startData = new HashMap<>();
        CaseDetails startCaseDetails = CaseDetails.builder().data(startData).build();
        StartEventResponse startEventResponse = StartEventResponse.builder()
            .token("TOKEN")
            .eventId("EVENT_ID")
            .caseDetails(startCaseDetails)
            .build();

        CaseData submittedCaseData = CaseDataBuilder.builder()
            .legacyCaseReference(REFERENCE_NUMBER)
            .ccdCaseReference(CASE_ID)
            .build();

        when(coreCaseDataService.startUpdate(CASE_ID.toString(), REFER_TO_JUDGE)).thenReturn(startEventResponse);
        when(coreCaseDataService.submitUpdate(eq(CASE_ID.toString()), any(CaseDataContent.class))).thenReturn(submittedCaseData);

        CallbackParams params = callbackParamsOf(caseData, SUBMITTED);

        handler.handle(params);

        ArgumentCaptor<CaseDataContent> captor = ArgumentCaptor.forClass(CaseDataContent.class);
        verify(coreCaseDataService).submitUpdate(eq(CASE_ID.toString()), captor.capture());

        CaseDataContent content = captor.getValue();
        assertThat(content.getEvent().getDescription()).isEqualTo("Extra details");
    }
}
