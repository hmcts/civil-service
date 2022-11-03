package uk.gov.hmcts.reform.civil.handler.callback.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackHandler;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CallbackType;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.IdamUserDetails;
import uk.gov.hmcts.reform.civil.model.caseprogression.UploadEvidenceDate;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.common.DynamicListElement;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.sampledata.CallbackParamsBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.sampledata.PartyBuilder;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.Map.entry;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.*;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.EVIDENCE_UPLOAD;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.element;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {
    EvidenceUploadHandler.class,
    JacksonAutoConfiguration.class
})
public class EvidenceUploadHandlerTest extends BaseCallbackHandlerTest {

    @Autowired
    private EvidenceUploadHandler handler;

    @Autowired
    private final ObjectMapper mapper = new ObjectMapper();

    @MockBean
    private IdamClient idamClient;

    @MockBean
    private UserDetails userDetails;

    @MockBean
    private IdamUserDetails idamUserDetails;

    private UploadEvidenceDate uploadEvidenceDate = new UploadEvidenceDate();

    @Test
    void shouldReturnTrueWhenCaseIs1v1_respondent() {
        // if case is 1v1 the logged in as respondent, other party name should be applicant Mr. John Rambo
        when(idamClient.getUserDetails(any()))
            .thenReturn(UserDetails.builder().email("respondentsolicitor@example.com").build());
        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
            .build();

        CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_START, caseData).build();
        AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
            .handle(params);

        assertThat(response.getData()).extracting("documentUploadExpert4")
            .asString().contains("Mr. John Rambo");
    }

    @Test
    void shouldReturnTrueWhenCaseIs2v1_respondent() {
        // if case is 2v1 the logged in as respondent, other party should contain Mr. John Rambo,
        // Mr. Jason Rambo and Both
        when(idamClient.getUserDetails(any()))
            .thenReturn(UserDetails.builder().email("respondentsolicitor2@example.com").build());

        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged()
            .multiPartyClaimTwoApplicants()
            .build();
        CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_START, caseData).build();
        AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
            .handle(params);

        assertThat(response.getData()).extracting("documentUploadExpert4")
            .asString().contains("Mr. Jason Rambo");
        assertThat(response.getData()).extracting("documentUploadExpert4")
            .asString().contains("Both");
    }

    @Test
    void shouldReturnTrueWhenCaseIs1v1_applicant() {
        // if case is 1v1 the logged in as applicant, other party should contain Mr. Sole Trader,
        when(idamClient.getUserDetails(any()))
            .thenReturn(UserDetails.builder().email("applicantsolicitor@example.com").build());

        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build();

        CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_START, caseData).build();
        AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
            .handle(params);

        assertThat(response.getData()).extracting("documentUploadExpert4")
            .asString().contains("Mr. Sole Trader");
    }

    @Test
    void shouldReturnTrueWhenCaseIs1v2_applicant() {
        // if case is 1v2 the logged in as applicant, other party should contain Mr. Sole Trader, Mr. John Rambo and
        //both
        when(idamClient.getUserDetails(any()))
            .thenReturn(UserDetails.builder().email("applicantsolicitor@example.com").build());

        CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build().toBuilder()
            .respondent2(PartyBuilder.builder().individual().build())
            .addRespondent2(YesOrNo.YES)
            .respondent2SameLegalRepresentative(YES)
            .build();

        CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_START, caseData).build();
        AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
            .handle(params);

        assertThat(response.getData()).extracting("documentUploadExpert4")
            .asString().contains("Mr. Sole Trader");
        assertThat(response.getData()).extracting("documentUploadExpert4")
            .asString().contains("Mr. John Rambo");

    }

    @Nested
    class MidEventValidateValuesCallback {
        private static final String PAGE_ID = "validateValues";

        @Test
        void shouldReturnError_whenExpertOption1UploadDateFuture() {
            List<Element<UploadEvidenceDate>> date = newArrayList();
            date.add(0, element(uploadEvidenceDate.toBuilder()
                                                .expertOption1UploadDate(LocalDate.now().plusWeeks(1)).build()));

            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
                .documentUploadExpert1(date)
                .build();
            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).contains("Invalid date: date entered must not be in the future.");
        }

        @Test
        void shouldNotReturnError_whenExpertOption1UploadDatePastOrPresent() {
            List<Element<UploadEvidenceDate>> date = newArrayList();
            date.add(0, element(uploadEvidenceDate.toBuilder()
                                                .expertOption1UploadDate(LocalDate.now()).build()));

            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
                .documentUploadExpert1(date)
                .build();
            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).isEmpty();
        }

        @Test
        void shouldReturnError_whenOneDateIsInFuture() {
            //documentUploadWitness1 represents a collection so can have multiple dates entered at any time,
            // these dates should all should be in past, otherwise an error will be populated
            List<Element<UploadEvidenceDate>> date = newArrayList();
            date.add(0, element(uploadEvidenceDate.toBuilder()
                                    .witnessOption1UploadDate(LocalDate.now().minusWeeks(1)).build()));
            date.add(1, element(uploadEvidenceDate.toBuilder()
                                    .witnessOption1UploadDate(LocalDate.now().minusWeeks(1)).build()));
            //dates above represent valid past dates, date below represents invalid future date.
            date.add(2, element(uploadEvidenceDate.toBuilder()
                                    .witnessOption1UploadDate(LocalDate.now().plusWeeks(1)).build()));

            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
                .documentUploadWitness1(date)
                .build();
            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).contains("Invalid date: date entered must not be in the future.");
        }
    }

    @Test
    void givenAboutToSubmitThenReturnsAboutToStartOrSubmitCallbackResponse() {
        CaseData caseData = CaseDataBuilder.builder().build();
        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

        CallbackResponse response = handler.handle(params);

        assertThat(response).isInstanceOf(AboutToStartOrSubmitCallbackResponse.class);
    }

    @Test
    void givenSubmittedThenReturnsSubmittedCallbackResponse() {
        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build();
        CallbackParams params = callbackParamsOf(caseData, CallbackType.SUBMITTED);

        CallbackResponse response = handler.handle(params);

        assertThat(response).isInstanceOf(SubmittedCallbackResponse.class);
    }

    @Test
    void whenRegisterCalledThenReturnEvidenceUploadCaseEvent() {
        Map<String, CallbackHandler> registerTarget = new HashMap<>();
        handler.register(registerTarget);

        assertThat(registerTarget).containsExactly(entry(EVIDENCE_UPLOAD.name(), handler));
    }

}

