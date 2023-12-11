package uk.gov.hmcts.reform.civil.handler.callback.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.helpers.LocationHelper;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.citizenui.CaseDataLiP;
import uk.gov.hmcts.reform.civil.model.citizenui.RespondentLiPResponse;
import uk.gov.hmcts.reform.civil.referencedata.LocationRefDataService;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.CaseLocationCivil;
import uk.gov.hmcts.reform.civil.model.dq.RequestedCourt;
import uk.gov.hmcts.reform.civil.model.dq.Respondent1DQ;
import uk.gov.hmcts.reform.civil.referencedata.model.LocationRefData;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.DeadlinesCalculator;
import uk.gov.hmcts.reform.civil.service.Time;
import uk.gov.hmcts.reform.civil.service.citizen.UpdateCaseManagementDetailsService;
import uk.gov.hmcts.reform.civil.utils.CourtLocationUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.DEFENDANT_RESPONSE_CUI;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {
    RespondToClaimCuiCallbackHandler.class,
    JacksonAutoConfiguration.class,
    CourtLocationUtils.class,
    LocationRefDataService.class,
    LocationHelper.class,
    UpdateCaseManagementDetailsService.class
})
class RespondToClaimCuiCallbackHandlerTest extends BaseCallbackHandlerTest {

    @MockBean
    private Time time;
    @MockBean
    private DeadlinesCalculator deadlinesCalculator;
    @Autowired
    private RespondToClaimCuiCallbackHandler handler;
    @MockBean
    private LocationRefDataService locationRefDataService;

    @Autowired
    private final ObjectMapper mapper = new ObjectMapper();

    @Nested
    class AboutToStartCallback {

        @Test
        void shouldReturnNoError_WhenAboutToStartIsInvoked() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued().build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_START);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).isNull();
        }
    }

    @Nested
    class AboutToSubmitCallback {
        LocalDateTime now;
        private final LocalDateTime respondToDeadline = LocalDateTime.of(
            2023,
            1,
            1,
            0,
            0,
            0);

        @BeforeEach
        void setup() {
            now = LocalDateTime.now();
            given(time.now()).willReturn(now);
            given(deadlinesCalculator.calculateApplicantResponseDeadline(any(), any())).willReturn(respondToDeadline);
        }

        @Test
        void shouldUpdateBusinessProcessAndClaimStatus_whenDefendantResponseLangIsEnglish() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimIssued()
                .caseDataLip(CaseDataLiP.builder().respondent1LiPResponse(RespondentLiPResponse.builder().respondent1ResponseLanguage("ENGLISH").build()).build())
                .build();

            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getData())
                .extracting("businessProcess")
                .extracting("camundaEvent")
                .isEqualTo(DEFENDANT_RESPONSE_CUI.name());
            assertThat(response.getData())
                .extracting("businessProcess")
                .extracting("status")
                .isEqualTo("READY");
            assertThat(response.getState()).isEqualTo(CaseState.AWAITING_APPLICANT_INTENTION.name());
            CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);
        }

        @Test
        void shouldOnlyUpdateClaimStatus_whenDefendantResponseLangIsBilingual() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimIssued()
                .caseDataLip(CaseDataLiP.builder().respondent1LiPResponse(RespondentLiPResponse.builder().respondent1ResponseLanguage("BOTH").build()).build())
                .build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getData())
                .extracting("businessProcess")
                .extracting("camundaEvent")
                .isEqualTo(DEFENDANT_RESPONSE_CUI.name());
            assertThat(response.getData())
                .extracting("businessProcess")
                .extracting("status")
                .isEqualTo("READY");
            assertThat(response.getState()).isNull();
        }

        @Test
        void shouldUpdateBusinessProcessAndClaimStatus_whenDefendantResponseWithDQ() {
            Respondent1DQ respondent1DQ =
                    Respondent1DQ.builder().respondent1DQRequestedCourt(RequestedCourt.builder()
                            .responseCourtCode("court1")
                            .caseLocation(CaseLocationCivil.builder()
                                    .region("Site1 - Adr 1 - N3 1BA")
                                    .baseLocation("Site1 - Adr 1 - N3 1BA")
                                    .build())
                            .build()).build();

            given(locationRefDataService.getCourtLocationsForDefaultJudgments(any()))
                    .willReturn(getSampleCourLocationsRefObject());

            CaseData caseData = CaseDataBuilder.builder()
                    .atStateClaimIssued()
                    .caseDataLip(CaseDataLiP.builder()
                            .respondent1LiPResponse(
                                    RespondentLiPResponse.builder()
                                            .respondent1ResponseLanguage("ENGLISH")
                                            .build())
                            .build())
                    .respondent1DQ(respondent1DQ)
                    .build();

            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            assertThat(response.getData())
                    .extracting("businessProcess")
                    .extracting("camundaEvent")
                    .isEqualTo(DEFENDANT_RESPONSE_CUI.name());
            assertThat(response.getData())
                    .extracting("businessProcess")
                    .extracting("status")
                    .isEqualTo("READY");
            CaseData data = mapper.convertValue(response.getData(), CaseData.class);
            assertThat(data.getRespondent1DQ().getRespondent1DQRequestedCourt().getResponseCourtCode()).isEqualTo("court1");
            assertThat(response.getState()).isEqualTo(CaseState.AWAITING_APPLICANT_INTENTION.name());
        }

        public List<LocationRefData> getSampleCourLocationsRefObject() {
            return new ArrayList<>(List.of(
                    LocationRefData.builder()
                            .epimmsId("111").siteName("Site1").courtAddress("Adr 1").postcode("N3 1BA")
                            .courtLocationCode("court1").build()
            ));
        }
    }
}
