package uk.gov.hmcts.reform.civil.handler.callback.user;

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
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.common.DynamicListElement;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.CaseLocationCivil;
import uk.gov.hmcts.reform.civil.referencedata.LocationRefDataService;
import uk.gov.hmcts.reform.civil.referencedata.model.LocationRefData;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.sampledata.LocationRefSampleDataBuilder;
import uk.gov.hmcts.reform.civil.utils.CourtLocationUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {
    TransferOnlineCaseCallbackHandler.class,
    JacksonAutoConfiguration.class})
class TransferOnlineCaseCallbackHandlerTest extends BaseCallbackHandlerTest {

    private static final String CONFIRMATION_HEADER = "# Case transferred to new location";

    @Autowired
    private TransferOnlineCaseCallbackHandler handler;

    @MockBean
    protected LocationRefDataService locationRefDataService;
    @MockBean
    protected CourtLocationUtils courtLocationUtils;

    @Nested
    class AboutToStartCallback extends LocationRefSampleDataBuilder {

        @BeforeEach
        void setup() {
            given(locationRefDataService.getCourtLocationsForDefaultJudgments(any()))
                .willReturn(getSampleCourLocationsRefObject());
        }

        @Test
        void shouldReturnLocationDataWhenAboutToStartCalled() {
            CaseData caseData = CaseDataBuilder.builder().atStateApplicantRespondToDefenceAndProceed().build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_START);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            assertThat(response.getData())
                .extracting("transferCourtLocationList")
                .extracting("list_items")
                .asList().hasSize(3);
        }
    }

    @Nested
    class MidCallback extends LocationRefSampleDataBuilder {

        @BeforeEach
        void setup() {
            given(locationRefDataService.getCourtLocationsForDefaultJudgments(any()))
                .willReturn(getSampleCourLocationsRefObject());
        }

        @Test
        void shouldGiveErrorIfSameCourtLocationSelected() {

            CaseData caseData = CaseDataBuilder.builder().atStateApplicantRespondToDefenceAndProceed()
                .caseManagementLocation(CaseLocationCivil.builder()
                                            .region("2")
                                            .baseLocation("111")
                                            .build())
                .transferCourtLocationList(DynamicList.builder().value(DynamicListElement.builder()
                                                                           .label("Site 1 - Adr 1 - AAA 111").build()).build()).build();
            given(courtLocationUtils.findPreferredLocationData(any(), any()))
                .willReturn(LocationRefData.builder().siteName("")
                                .epimmsId("111")
                                .siteName("Site 1").courtAddress("Adr 1").postcode("AAA 111")
                                .courtLocationCode("court1").build());

            CallbackParams params = callbackParamsOf(caseData, MID, "validate-court-location");
            //When: handler is called with MID event
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            assertThat(response.getErrors()).contains("Select a different hearing court location to transfer!");
        }

        @Test
        void shouldNotGiveErrorIfDifferentCourtLocationSelected() {

            CaseData caseData = CaseDataBuilder.builder().atStateApplicantRespondToDefenceAndProceed()
                .caseManagementLocation(CaseLocationCivil.builder()
                                            .region("2")
                                            .baseLocation("111")
                                            .build())
                .transferCourtLocationList(DynamicList.builder().value(DynamicListElement.builder()
                                                                           .label("Site 1 - Adr 1 - AAA 111").build()).build()).build();

            given(courtLocationUtils.findPreferredLocationData(any(), any()))
                .willReturn(LocationRefData.builder().siteName("")
                                .epimmsId("111")
                                .siteName("Site 1").courtAddress("Adr 1").postcode("AAA 111")
                                .courtLocationCode("other code").build());
            CallbackParams params = callbackParamsOf(caseData, MID, "validate-court-location");
            //When: handler is called with MID event
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            assertThat(response.getErrors()).isEmpty();
        }
    }

    @Nested
    class AboutToSubmitCallback {
        @Test
        void shouldPopulateCorrectCaseDataWhenSubmitted() {
            CaseData caseData = CaseDataBuilder.builder().atStateApplicantRespondToDefenceAndProceed()
                .caseManagementLocation(CaseLocationCivil.builder()
                                            .region("2")
                                            .baseLocation("111")
                                            .build())
                .reasonForTransfer("Reason")
                .transferCourtLocationList(DynamicList.builder().value(DynamicListElement.builder()
                                                                                   .label("Site 1 - Adr 1 - AAA 111").build()).build()).build();
            given(courtLocationUtils.findPreferredLocationData(any(), any()))
                .willReturn(LocationRefData.builder().siteName("")
                                .epimmsId("222")
                                .siteName("Site 2").courtAddress("Adr 2").postcode("BBB 222")
                                .courtLocationCode("other code").build());
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            assertThat(response.getData())
                .extracting("reasonForTransfer")
                .isEqualTo("Reason");
            assertThat(response.getData())
                .extracting("transferCourtLocationList")
                .extracting("value")
                .extracting("label")
                .isEqualTo("Site 1 - Adr 1 - AAA 111");
            assertThat(response.getData())
                .extracting("transferCourtLocationList")
                .extracting("list_items")
                .isNull();
            assertThat(response.getData())
                .extracting("caseManagementLocation")
                .extracting("baseLocation")
                .isEqualTo("222");
        }
    }

    @Nested
    class SubmittedCallback {
        @Test
        void shouldReturnExpectedSubmittedCallbackResponse() {
            String newCourtLocationSiteName = "Site 2";
            CaseData caseData = CaseDataBuilder.builder().atStateApplicantRespondToDefenceAndProceed().build();
            given(courtLocationUtils.findPreferredLocationData(any(), any()))
                .willReturn(LocationRefData.builder().siteName("")
                                .epimmsId("222")
                                .siteName(newCourtLocationSiteName).courtAddress("Adr 2").postcode("BBB 222")
                                .courtLocationCode("other code").build());

            CallbackParams params = callbackParamsOf(caseData, SUBMITTED);
            SubmittedCallbackResponse response = (SubmittedCallbackResponse) handler.handle(params);

            String body = "<h2 class=\"govuk-heading-m\">What happens next</h2>"
                + "The case has now been transferred to "
                + newCourtLocationSiteName
                + ". If the case has moved out of your region, you will no longer see it.<br><br>";

            assertThat(response).usingRecursiveComparison().isEqualTo(
                SubmittedCallbackResponse.builder()
                    .confirmationHeader(CONFIRMATION_HEADER)
                    .confirmationBody(body)
                    .build());
        }
    }

    @Test
    void handleEventsReturnsTheExpectedCallbackEvent() {
        assertThat(handler.handledEvents()).contains(CaseEvent.TRANSFER_ONLINE_CASE);
    }
}
