package uk.gov.hmcts.reform.civil.handler.callback.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.common.DynamicListElement;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.CaseLocationCivil;
import uk.gov.hmcts.reform.civil.referencedata.model.LocationRefData;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.sampledata.LocationRefSampleDataBuilder;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.camunda.UpdateWaCourtLocationsService;
import uk.gov.hmcts.reform.civil.service.referencedata.LocationReferenceDataService;
import uk.gov.hmcts.reform.civil.utils.CourtLocationUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.MID;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.SUBMITTED;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;

@ExtendWith(MockitoExtension.class)
class TransferOnlineCaseCallbackHandlerTest extends BaseCallbackHandlerTest {

    private static final String CONFIRMATION_HEADER = "# Case transferred to new location";

    private TransferOnlineCaseCallbackHandler handler;

    private ObjectMapper objectMapper;

    @Mock
    protected LocationReferenceDataService locationRefDataService;
    @Mock
    protected CourtLocationUtils courtLocationUtils;
    @Mock
    protected FeatureToggleService featureToggleService;
    @Mock
    protected UpdateWaCourtLocationsService updateWaCourtLocationsService;

    @BeforeEach
    void setup() {
        objectMapper = new ObjectMapper().registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
        handler = new TransferOnlineCaseCallbackHandler(objectMapper, locationRefDataService, courtLocationUtils,
                                                        featureToggleService,
                                                        Optional.ofNullable(updateWaCourtLocationsService)
        );
    }

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
        void shouldGiveErrorIfNoCourtLocationSelected() {

            CaseData caseData = CaseDataBuilder.builder().atStateApplicantRespondToDefenceAndProceed()
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
            assertThat(response.getErrors()).isEmpty();
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
                .doesNotHaveToString("list_items");
            assertThat(response.getData())
                .extracting("caseManagementLocation")
                .extracting("baseLocation")
                .isEqualTo("222");
        }

        @Test
        void shouldPopulateCorrectCaseDataWhenSubmittedAndNoNewCourtLocation() {
            CaseData caseData = CaseDataBuilder.builder().atStateApplicantRespondToDefenceAndProceed()
                .caseManagementLocation(CaseLocationCivil.builder()
                                            .region("2")
                                            .baseLocation("111")
                                            .build())
                .transferCourtLocationList(DynamicList.builder().value(DynamicListElement.builder()
                                                                           .label("Site 1 - Adr 1 - AAA 111").build()).build()).build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            assertThat(response.getData())
                .extracting("caseManagementLocation")
                .extracting("baseLocation")
                .isEqualTo("111");
        }

        @ParameterizedTest
        @CsvSource({
            //LR scenarios trigger and ignore hmcLipEnabled
            "true, YES, YES, YES",
            "false, YES, YES, YES",
            // LiP vs LR - ignore HMC court
            "true,  NO, YES, NO",
            "false,  NO, YES, NO",
            //LR vs LiP - ignore HMC court
            "true, YES, NO, YES",
            "false, YES, NO, NO",
            //LiP vs LiP - ignore HMC court
            "true, NO, NO, YES",
            "false, NO, NO, NO"
        })
        void shouldPopulateHmcLipEnabled_whenLiPAndHmcLipEnabled(boolean isCPAndWhitelisted,
                                                                 YesOrNo applicantRepresented,
                                                                 YesOrNo respondent1Represented,
                                                                 YesOrNo eaCourtLocation) {

            if (NO.equals(respondent1Represented)) {
                when(featureToggleService.isCaseProgressionEnabledAndLocationWhiteListed(any())).thenReturn(isCPAndWhitelisted);
            }

            when(courtLocationUtils.findPreferredLocationData(any(), any()))
                .thenReturn(LocationRefData.builder().siteName("")
                                .epimmsId("222")
                                .siteName("Site 2").courtAddress("Adr 2").postcode("BBB 222")
                                .courtLocationCode("other code").build());
            CaseData caseData = CaseDataBuilder.builder().atStateApplicantRespondToDefenceAndProceed()
                .caseManagementLocation(CaseLocationCivil.builder()
                                            .region("2")
                                            .baseLocation("111")
                                            .build())
                .transferCourtLocationList(DynamicList.builder().value(DynamicListElement.builder()
                                                                           .label("Site 1 - Adr 1 - AAA 111").build()).build()).build();

            CallbackParams params = callbackParamsOf(caseData.toBuilder()
                                                         .applicant1Represented(applicantRepresented)
                                                         .respondent1Represented(respondent1Represented)
                                                         .build(), ABOUT_TO_SUBMIT);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            CaseData responseCaseData = objectMapper.convertValue(response.getData(), CaseData.class);

            assertEquals(eaCourtLocation, responseCaseData.getEaCourtLocation());
        }

        @Test
        void shouldPopulateWhiteListing_whenNationalRolloutEnabled() {
            given(courtLocationUtils.findPreferredLocationData(any(), any()))
                .willReturn(LocationRefData.builder().siteName("")
                                .epimmsId("222")
                                .siteName("Site 2").courtAddress("Adr 2").postcode("BBB 222")
                                .courtLocationCode("other code").build());

            CaseData caseData = CaseDataBuilder.builder().atStateApplicantRespondToDefenceAndProceed()
                .caseManagementLocation(CaseLocationCivil.builder()
                                            .region("2")
                                            .baseLocation("111")
                                            .build())
                .transferCourtLocationList(DynamicList.builder().value(DynamicListElement.builder()
                                                                           .label("Site 1 - Adr 1 - AAA 111").build()).build()).build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            CaseData responseCaseData = objectMapper.convertValue(response.getData(), CaseData.class);

            assertThat(responseCaseData.getEaCourtLocation()).isEqualTo(YES);
        }

        @ParameterizedTest
        @CsvSource({
            "true, NO, NO, YES",
            "false, NO, NO, NO",
            "true, YES, NO, YES"
        })
        void shouldSetEaCourtLocationBasedOnConditions(boolean isLocationWhiteListed,
                                                       YesOrNo applicant1Represented,
                                                       YesOrNo respondent1Represented,
                                                       YesOrNo expectedEaCourtLocation) {
            DynamicListElement selectedCourt = DynamicListElement.builder()
                .code("00002").label("court 2 - 2 address - Y02 7RB").build();
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDraft().build().toBuilder()
                .caseManagementLocation(CaseLocationCivil.builder().baseLocation(selectedCourt.getCode()).build())
                .respondent1Represented(respondent1Represented)
                .applicant1Represented(applicant1Represented)
                .transferCourtLocationList(DynamicList.builder().value(DynamicListElement.builder()
                                                                      .label("Site 1 - Adr 1 - AAA 111").build()).build())
                .build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
            given(courtLocationUtils.findPreferredLocationData(any(), any()))
                .willReturn(LocationRefData.builder().siteName("")
                                .epimmsId("222")
                                .siteName("Site 2").courtAddress("Adr 2").postcode("BBB 222")
                                .courtLocationCode("other code").build());
            when(featureToggleService.isCaseProgressionEnabledAndLocationWhiteListed(any())).thenReturn(isLocationWhiteListed);

            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            CaseData responseCaseData = objectMapper.convertValue(response.getData(), CaseData.class);

            assertEquals(expectedEaCourtLocation, responseCaseData.getEaCourtLocation());
        }

        @Test
        void shouldCallUpdateWaCourtLocationsServiceWhenPresent_AndMintiEnabled() {
            when(featureToggleService.isMultiOrIntermediateTrackEnabled(any())).thenReturn(true);
            given(courtLocationUtils.findPreferredLocationData(any(), any()))
                .willReturn(LocationRefData.builder().siteName("")
                                .epimmsId("222")
                                .siteName("Site 2").courtAddress("Adr 2").postcode("BBB 222")
                                .courtLocationCode("other code").build());

            CaseData caseData = CaseDataBuilder.builder().atStateApplicantRespondToDefenceAndProceed()
                .caseManagementLocation(CaseLocationCivil.builder()
                                            .region("2")
                                            .baseLocation("111")
                                            .build())
                .transferCourtLocationList(DynamicList.builder().value(DynamicListElement.builder()
                                                                           .label("Site 1 - Adr 1 - AAA 111").build()).build()).build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            verify(updateWaCourtLocationsService).updateCourtListingWALocations(any(), any());
        }

        @Test
        void shouldNotCallUpdateWaCourtLocationsServiceWhenNotPresent_AndMintiEnabled() {
            handler = new TransferOnlineCaseCallbackHandler(objectMapper, locationRefDataService, courtLocationUtils,
                                                            featureToggleService,
                                                            Optional.empty());
            when(featureToggleService.isMultiOrIntermediateTrackEnabled(any())).thenReturn(true);
            given(courtLocationUtils.findPreferredLocationData(any(), any()))
                .willReturn(LocationRefData.builder().siteName("")
                                .epimmsId("222")
                                .siteName("Site 2").courtAddress("Adr 2").postcode("BBB 222")
                                .courtLocationCode("other code").build());

            CaseData caseData = CaseDataBuilder.builder().atStateApplicantRespondToDefenceAndProceed()
                .caseManagementLocation(CaseLocationCivil.builder()
                                            .region("2")
                                            .baseLocation("111")
                                            .build())
                .transferCourtLocationList(DynamicList.builder().value(DynamicListElement.builder()
                                                                           .label("Site 1 - Adr 1 - AAA 111").build()).build()).build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            verifyNoInteractions(updateWaCourtLocationsService);
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
