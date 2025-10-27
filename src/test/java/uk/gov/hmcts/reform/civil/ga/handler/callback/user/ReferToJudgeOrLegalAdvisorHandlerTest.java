package uk.gov.hmcts.reform.civil.ga.handler.callback.user;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CallbackType;
import uk.gov.hmcts.reform.civil.enums.BusinessProcessStatus;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.ga.enums.GAJudicialHearingType;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.enums.dq.GAHearingType;
import uk.gov.hmcts.reform.civil.enums.dq.GeneralApplicationTypes;
import uk.gov.hmcts.reform.civil.ga.handler.GeneralApplicationBaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.BusinessProcess;
import uk.gov.hmcts.reform.civil.ga.model.GARespondentRepresentative;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.common.DynamicListElement;
import uk.gov.hmcts.reform.civil.model.genapplication.GAApplicationType;
import uk.gov.hmcts.reform.civil.model.genapplication.GAHearingDetails;
import uk.gov.hmcts.reform.civil.ga.model.genapplication.GAJudgesHearingListGAspec;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.REFER_TO_JUDGE;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.REFER_TO_LEGAL_ADVISOR;
import static uk.gov.hmcts.reform.civil.enums.CaseState.ADDITIONAL_RESPONSE_TIME_EXPIRED;
import static uk.gov.hmcts.reform.civil.enums.CaseState.APPLICATION_SUBMITTED_AWAITING_JUDICIAL_DECISION;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.model.common.DynamicList.fromList;

@SpringBootTest(classes = {
    CaseDetailsConverter.class,
    ReferToJudgeOrLegalAdvisorHandler.class,
    JacksonAutoConfiguration.class,
    },
    properties = {"reference.database.enabled=false"})
public class ReferToJudgeOrLegalAdvisorHandlerTest extends GeneralApplicationBaseCallbackHandlerTest {

    @Autowired
    ReferToJudgeOrLegalAdvisorHandler handler;

    @Autowired
    CaseDetailsConverter caseDetailsConverter;

    public static final String COURT_ASSIGNE_ERROR_MESSAGE = "A Court has already been assigned";

    @Test
    void handleEventsReturnsTheExpectedCallbackEventReferToJudge() {
        assertThat(handler.handledEvents()).contains(REFER_TO_JUDGE);
    }

    @Test
    void handleEventsReturnsTheExpectedCallbackEventReferToLegalAdvisor() {
        assertThat(handler.handledEvents()).contains(REFER_TO_LEGAL_ADVISOR);
    }

    @Test
    void aboutToStartCallbackShouldThrowErrorCourtValidationState_referToLegalAdvisor() {
        CallbackParams params = callbackParamsOf(
            getCase(APPLICATION_SUBMITTED_AWAITING_JUDICIAL_DECISION, NO),
            CallbackType.ABOUT_TO_START
        ).toBuilder().request(CallbackRequest.builder().eventId("REFER_TO_LEGAL_ADVISOR").build()).build();
        List<String> errors = new ArrayList<>();
        errors.add(COURT_ASSIGNE_ERROR_MESSAGE);
        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
        assertThat(response).isNotNull();
        assertThat(response.getErrors()).isEqualTo(errors);
    }

    @Test
    void aboutToStartCallbackShouldNotThrowErrorCourtValidationState_BeforeSDO_ReferToJudge() {
        CallbackParams params = callbackParamsOf(
            getCase(APPLICATION_SUBMITTED_AWAITING_JUDICIAL_DECISION, YES),
            CallbackType.ABOUT_TO_START
        ).toBuilder().request(CallbackRequest.builder().eventId("REFER_TO_JUDGE").build()).build();
        List<String> errors = new ArrayList<>();
        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
        assertThat(response).isNotNull();
        assertThat(response.getErrors()).isEqualTo(errors);
    }

    @Test
    void aboutToStartCallbackShouldNotThrowErrorCourtValidationState_AfterSDO_ReferToJudge() {
        CallbackParams params = callbackParamsOf(
            getCase(APPLICATION_SUBMITTED_AWAITING_JUDICIAL_DECISION, NO),
            CallbackType.ABOUT_TO_START
        ).toBuilder().request(CallbackRequest.builder().eventId("REFER_TO_JUDGE").build()).build();
        List<String> errors = new ArrayList<>();
        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
        assertThat(response).isNotNull();
        assertThat(response.getErrors()).isEqualTo(errors);
    }

    @Test
    void aboutToStartCallbackShouldNotThrowErrorCourtValidationState_BeforeSDO_WithReferToLegalAdvisor() {
        CallbackParams params = callbackParamsOf(
            getCase(APPLICATION_SUBMITTED_AWAITING_JUDICIAL_DECISION, YES),
            CallbackType.ABOUT_TO_START
        ).toBuilder().request(CallbackRequest.builder().eventId("REFER_TO_LEGAL_ADVISOR").build()).build();
        List<String> errors = new ArrayList<>();
        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
        assertThat(response).isNotNull();
        assertThat(response.getErrors()).isEqualTo(errors);
    }

    @Test
    void aboutToStartCallbackShouldThrowErrorCourtValidationState_AfterSDO_WithReferToLegalAdvisor() {
        CallbackParams params = callbackParamsOf(
            getCase(APPLICATION_SUBMITTED_AWAITING_JUDICIAL_DECISION, NO),
            CallbackType.ABOUT_TO_START
        ).toBuilder().request(CallbackRequest.builder().eventId("REFER_TO_LEGAL_ADVISOR").build()).build();
        List<String> errors = new ArrayList<>();
        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
        errors.add(COURT_ASSIGNE_ERROR_MESSAGE);
        assertThat(response).isNotNull();
        assertThat(response.getErrors()).isEqualTo(errors);
    }

    @Test
    void aboutToStartCallbackTimeExpiredShouldNotThrowErrorCourtValidationState() {
        CallbackParams params = callbackParamsOf(
            getCase(ADDITIONAL_RESPONSE_TIME_EXPIRED, YES),
            CallbackType.ABOUT_TO_START
        ).toBuilder().request(CallbackRequest.builder().eventId("REFER_TO_JUDGE").build()).build();
        List<String> errors = new ArrayList<>();
        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
        assertThat(response).isNotNull();
        assertThat(response.getErrors()).isEqualTo(errors);
    }

    @Test
    void aboutToStartCallbackTimeExpiredShouldThrowErrorCourtValidationState() {
        CallbackParams params = callbackParamsOf(
            getCase(ADDITIONAL_RESPONSE_TIME_EXPIRED, NO),
            CallbackType.ABOUT_TO_START
        ).toBuilder().request(CallbackRequest.builder().eventId("REFER_TO_LEGAL_ADVISOR").build()).build();
        List<String> errors = new ArrayList<>();
        errors.add(COURT_ASSIGNE_ERROR_MESSAGE);
        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
        assertThat(response).isNotNull();
        assertThat(response.getErrors()).isEqualTo(errors);
    }

    private GeneralApplicationCaseData getCase(CaseState state, YesOrNo courtAssigned) {
        List<GeneralApplicationTypes> types = List.of(
            (GeneralApplicationTypes.SUMMARY_JUDGEMENT));
        DynamicList dynamicListTest = fromList(getSampleCourLocations());
        Optional<DynamicListElement> first = dynamicListTest.getListItems().stream().findFirst();
        first.ifPresent(dynamicListTest::setValue);

        return GeneralApplicationCaseData.builder()
            .generalAppRespondent1Representative(
                GARespondentRepresentative.builder()
                    .generalAppRespondent1Representative(YES)
                    .build())
            .judicialListForHearing(GAJudgesHearingListGAspec.builder()
                                        .hearingPreferredLocation(dynamicListTest)
                                        .hearingPreferencesPreferredType(GAJudicialHearingType.IN_PERSON)
                                        .build())
            .hearingDetailsResp(GAHearingDetails.builder()
                                    .hearingPreferredLocation(dynamicListTest)
                                    .hearingPreferencesPreferredType(GAHearingType.IN_PERSON)
                                    .build())
            .generalAppType(
                GAApplicationType
                    .builder()
                    .types(types).build())
            .businessProcess(BusinessProcess
                                 .builder()
                                 .camundaEvent("INITIATE_GENERAL_APPLICATION")
                                 .processInstanceId("11111")
                                 .status(BusinessProcessStatus.FINISHED)
                                 .activityId("anyActivity")
                                 .build())
            .isCcmccLocation(courtAssigned)
            .ccdState(state)
            .build();
    }

    protected List<String> getSampleCourLocations() {
        return new ArrayList<>(Arrays.asList("ABCD - RG0 0AL", "PQRS - GU0 0EE", "WXYZ - EW0 0HE", "LMNO - NE0 0BH"));
    }
}
