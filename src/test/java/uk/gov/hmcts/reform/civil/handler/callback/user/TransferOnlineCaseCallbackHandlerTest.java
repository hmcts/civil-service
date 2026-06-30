package uk.gov.hmcts.reform.civil.handler.callback.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.assertj.core.api.InstanceOfAssertFactories;
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
                .asInstanceOf(InstanceOfAssertFactories.LIST).hasSize(4);
        }

        @Test
        void shouldHandleNullCaseManagementLocation() {
            // Covering line 176 and 171

            LocationRefData locationRefData = new LocationRefData();
            locationRefData.setEpimmsId("111");
            locationRefData.setSiteName("Site 1");
            locationRefData.setCourtAddress("Adr 1");
            locationRefData.setPostcode("AAA 111");
            locationRefData.setCourtLocationCode("court1");
            given(courtLocationUtils.findPreferredLocationData(any(), any()))
                .willReturn(locationRefData);

            CaseData caseData = CaseDataBuilder.builder().atStateApplicantRespondToDefenceAndProceed()
                .caseManagementLocation(null).build();
            CallbackParams params = callbackParamsOf(caseData, MID, "validate-court-location");
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            assertThat(response.getErrors()).isEmpty();
        }

        @Test
        void shouldHandleNoMatchedLocations() {
            // Covering line 178
            CaseLocationCivil caseLocation = new CaseLocationCivil();
            caseLocation.setRegion("2");
            caseLocation.setBaseLocation("999"); // EpimmsId that doesn't exist in sample data

            LocationRefData locationRefData = new LocationRefData();
            locationRefData.setEpimmsId("111");
            locationRefData.setSiteName("Site 1");
            locationRefData.setCourtAddress("Adr 1");
            locationRefData.setPostcode("AAA 111");
            locationRefData.setCourtLocationCode("court1");
            given(courtLocationUtils.findPreferredLocationData(any(), any()))
                .willReturn(locationRefData);

            CaseData caseData = CaseDataBuilder.builder().atStateApplicantRespondToDefenceAndProceed()
                .caseManagementLocation(caseLocation).build();
            CallbackParams params = callbackParamsOf(caseData, MID, "validate-court-location");
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            assertThat(response.getErrors()).isEmpty();
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

            CaseLocationCivil caseLocation = new CaseLocationCivil();
            caseLocation.setRegion("2");
            caseLocation.setBaseLocation("111");
            DynamicListElement transferCourtElement = new DynamicListElement(null, "Site 1 - Adr 1 - AAA 111");
            DynamicList transferCourtList = new DynamicList();
            transferCourtList.setValue(transferCourtElement);
            LocationRefData locationRefData = new LocationRefData();
            locationRefData.setEpimmsId("111");
            locationRefData.setSiteName("Site 1");
            locationRefData.setCourtAddress("Adr 1");
            locationRefData.setPostcode("AAA 111");
            locationRefData.setCourtLocationCode("court1");
            given(courtLocationUtils.findPreferredLocationData(any(), any()))
                .willReturn(locationRefData);

            CaseData caseData = CaseDataBuilder.builder().atStateApplicantRespondToDefenceAndProceed()
                .caseManagementLocation(caseLocation)
                .transferCourtLocationList(transferCourtList).build();
            CallbackParams params = callbackParamsOf(caseData, MID, "validate-court-location");
            //When: handler is called with MID event
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            assertThat(response.getErrors()).contains("Select a different hearing court location to transfer!");
        }

        @Test
        void shouldGiveErrorIfNoCourtLocationSelected() {

            DynamicListElement transferCourtElement = new DynamicListElement(null, "Site 1 - Adr 1 - AAA 111");
            DynamicList transferCourtList = new DynamicList();
            transferCourtList.setValue(transferCourtElement);
            LocationRefData locationRefData = new LocationRefData();
            locationRefData.setEpimmsId("111");
            locationRefData.setSiteName("Site 1");
            locationRefData.setCourtAddress("Adr 1");
            locationRefData.setPostcode("AAA 111");
            locationRefData.setCourtLocationCode("court1");
            given(courtLocationUtils.findPreferredLocationData(any(), any()))
                .willReturn(locationRefData);

            CaseData caseData = CaseDataBuilder.builder().atStateApplicantRespondToDefenceAndProceed()
                .transferCourtLocationList(transferCourtList).build();
            CallbackParams params = callbackParamsOf(caseData, MID, "validate-court-location");
            //When: handler is called with MID event
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            assertThat(response.getErrors()).isEmpty();
        }

        @Test
        void shouldNotGiveErrorIfDifferentCourtLocationSelected() {

            CaseLocationCivil caseLocation = new CaseLocationCivil();
            caseLocation.setRegion("2");
            caseLocation.setBaseLocation("111");
            DynamicListElement transferCourtElement = new DynamicListElement(null, "Site 1 - Adr 1 - AAA 111");
            DynamicList transferCourtList = new DynamicList();
            transferCourtList.setValue(transferCourtElement);
            LocationRefData locationRefData = new LocationRefData();
            locationRefData.setEpimmsId("111");
            locationRefData.setSiteName("Site 1");
            locationRefData.setCourtAddress("Adr 1");
            locationRefData.setPostcode("AAA 111");
            locationRefData.setCourtLocationCode("other code");
            given(courtLocationUtils.findPreferredLocationData(any(), any()))
                .willReturn(locationRefData);
            CaseData caseData = CaseDataBuilder.builder().atStateApplicantRespondToDefenceAndProceed()
                .caseManagementLocation(caseLocation)
                .transferCourtLocationList(transferCourtList).build();
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
            CaseLocationCivil caseLocation = new CaseLocationCivil();
            caseLocation.setRegion("2");
            caseLocation.setBaseLocation("111");
            DynamicListElement transferCourtElement = new DynamicListElement(null, "Site 1 - Adr 1 - AAA 111");
            DynamicList transferCourtList = new DynamicList();
            transferCourtList.setValue(transferCourtElement);
            LocationRefData locationRefData = new LocationRefData();
            locationRefData.setEpimmsId("222");
            locationRefData.setSiteName("Site 2");
            locationRefData.setCourtAddress("Adr 2");
            locationRefData.setPostcode("BBB 222");
            locationRefData.setCourtLocationCode("other code");
            given(courtLocationUtils.findPreferredLocationData(any(), any()))
                .willReturn(locationRefData);
            CaseData caseData = CaseDataBuilder.builder().atStateApplicantRespondToDefenceAndProceed()
                .caseManagementLocation(caseLocation)
                .reasonForTransfer("Reason")
                .transferCourtLocationList(transferCourtList).build();
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
            CaseLocationCivil caseLocation = new CaseLocationCivil();
            caseLocation.setRegion("2");
            caseLocation.setBaseLocation("111");
            DynamicListElement transferCourtElement = new DynamicListElement(null, "Site 1 - Adr 1 - AAA 111");
            DynamicList transferCourtList = new DynamicList();
            transferCourtList.setValue(transferCourtElement);
            CaseData caseData = CaseDataBuilder.builder().atStateApplicantRespondToDefenceAndProceed()
                .caseManagementLocation(caseLocation)
                .transferCourtLocationList(transferCourtList).build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            assertThat(response.getData())
                .extracting("caseManagementLocation")
                .extracting("baseLocation")
                .isEqualTo("111");
        }

        @ParameterizedTest
        @CsvSource({
            "YES, YES",
            "NO, YES",
            "YES, NO",
            "NO, NO"
        })
        void shouldPopulateEaCourtLocationAsYesWhenLipAndHmcLipEnabled(YesOrNo applicantRepresented,
                                                                       YesOrNo respondent1Represented) {
            LocationRefData locationRefData = new LocationRefData();
            locationRefData.setEpimmsId("222");
            locationRefData.setSiteName("Site 2");
            locationRefData.setCourtAddress("Adr 2");
            locationRefData.setPostcode("BBB 222");
            locationRefData.setCourtLocationCode("other code");
            when(courtLocationUtils.findPreferredLocationData(any(), any()))
                .thenReturn(locationRefData);
            CaseLocationCivil caseLocation = new CaseLocationCivil();
            caseLocation.setRegion("2");
            caseLocation.setBaseLocation("111");
            DynamicListElement transferCourtElement = new DynamicListElement(null, "Site 1 - Adr 1 - AAA 111");
            DynamicList transferCourtList = new DynamicList();
            transferCourtList.setValue(transferCourtElement);
            CaseData caseData = CaseDataBuilder.builder().atStateApplicantRespondToDefenceAndProceed()
                .caseManagementLocation(caseLocation)
                .transferCourtLocationList(transferCourtList).build();
            caseData.setApplicant1Represented(applicantRepresented);
            caseData.setRespondent1Represented(respondent1Represented);

            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            CaseData responseCaseData = objectMapper.convertValue(response.getData(), CaseData.class);

            assertEquals(YES, responseCaseData.getEaCourtLocation());
        }

        @ParameterizedTest
        @CsvSource({
            "YES, YES",
            "NO, YES",
            "YES, NO",
            "NO, NO"
        })
        void shouldPopulateEaCourtLocationAsYes(YesOrNo applicantRepresented,
                                                YesOrNo respondent1Represented) {
            LocationRefData locationRefData = new LocationRefData();
            locationRefData.setEpimmsId("222");
            locationRefData.setSiteName("Site 2");
            locationRefData.setCourtAddress("Adr 2");
            locationRefData.setPostcode("BBB 222");
            locationRefData.setCourtLocationCode("other code");
            when(courtLocationUtils.findPreferredLocationData(any(), any()))
                .thenReturn(locationRefData);
            CaseLocationCivil caseLocation = new CaseLocationCivil();
            caseLocation.setRegion("2");
            caseLocation.setBaseLocation("111");
            DynamicListElement transferCourtElement = new DynamicListElement(null, "Site 1 - Adr 1 - AAA 111");
            DynamicList transferCourtList = new DynamicList();
            transferCourtList.setValue(transferCourtElement);
            CaseData caseData = CaseDataBuilder.builder().atStateApplicantRespondToDefenceAndProceed()
                .caseManagementLocation(caseLocation)
                .transferCourtLocationList(transferCourtList).build();
            caseData.setApplicant1Represented(applicantRepresented);
            caseData.setRespondent1Represented(respondent1Represented);

            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            CaseData responseCaseData = objectMapper.convertValue(response.getData(), CaseData.class);

            assertEquals(YES, responseCaseData.getEaCourtLocation());
        }

        @Test
        void shouldPopulateEaCourtLocationWhenRespondent1IsLip() {
            LocationRefData locationRefData = new LocationRefData();
            locationRefData.setEpimmsId("222");
            locationRefData.setSiteName("Site 2");
            locationRefData.setCourtAddress("Adr 2");
            locationRefData.setPostcode("BBB 222");
            locationRefData.setCourtLocationCode("other code");
            when(courtLocationUtils.findPreferredLocationData(any(), any()))
                .thenReturn(locationRefData);

            DynamicList transferCourtList = new DynamicList();
            DynamicListElement transferCourtElement = new DynamicListElement()
                .setLabel("Site 1 - Adr 1 - AAA 111");
            transferCourtList.setValue(transferCourtElement);

            // Using respondent1Represented(NO)
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimSubmitted()
                .respondent1Represented(NO)
                .applicant1Represented(YES)
                .transferCourtLocationList(transferCourtList)
                .build();
            caseData.setRespondent1Represented(NO);

            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            CaseData responseCaseData = objectMapper.convertValue(response.getData(), CaseData.class);

            assertEquals(YES, responseCaseData.getEaCourtLocation());
        }

        @Test
        void shouldPopulateEaCourtLocationWhenLipvLRAndDefendantNoCOnline() {
            LocationRefData locationRefData = new LocationRefData();
            locationRefData.setEpimmsId("222");
            locationRefData.setSiteName("Site 2");
            locationRefData.setCourtAddress("Adr 2");
            locationRefData.setPostcode("BBB 222");
            locationRefData.setCourtLocationCode("other code");
            when(courtLocationUtils.findPreferredLocationData(any(), any()))
                .thenReturn(locationRefData);

            DynamicList transferCourtList = new DynamicList();
            DynamicListElement transferCourtElement = new DynamicListElement()
                .setLabel("Site 1 - Adr 1 - AAA 111");
            transferCourtList.setValue(transferCourtElement);
            CaseData caseData = CaseDataBuilder.builder().atStateApplicantRespondToDefenceAndProceed()
                .applicant1Represented(NO)
                .respondent1Represented(YES)
                .transferCourtLocationList(transferCourtList)
                .build();

            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            CaseData responseCaseData = objectMapper.convertValue(response.getData(), CaseData.class);

            assertEquals(YES, responseCaseData.getEaCourtLocation());
        }

        @Test
        void shouldPopulateWhiteListing_whenNationalRolloutEnabled() {
            LocationRefData locationRefData = new LocationRefData();
            locationRefData.setEpimmsId("222");
            locationRefData.setSiteName("Site 2");
            locationRefData.setCourtAddress("Adr 2");
            locationRefData.setPostcode("BBB 222");
            locationRefData.setCourtLocationCode("other code");
            given(courtLocationUtils.findPreferredLocationData(any(), any()))
                .willReturn(locationRefData);

            CaseLocationCivil caseLocation = new CaseLocationCivil();
            caseLocation.setRegion("2");
            caseLocation.setBaseLocation("111");
            DynamicListElement transferCourtElement = new DynamicListElement(null, "Site 1 - Adr 1 - AAA 111");
            DynamicList transferCourtList = new DynamicList();
            transferCourtList.setValue(transferCourtElement);
            CaseData caseData = CaseDataBuilder.builder().atStateApplicantRespondToDefenceAndProceed()
                .caseManagementLocation(caseLocation)
                .transferCourtLocationList(transferCourtList).build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            CaseData responseCaseData = objectMapper.convertValue(response.getData(), CaseData.class);

            assertThat(responseCaseData.getEaCourtLocation()).isEqualTo(YES);
        }

        @ParameterizedTest
        @CsvSource({
            "NO, NO",
            "YES, NO",
            "NO, YES"
        })
        void shouldSetEaCourtLocationToYes(YesOrNo applicant1Represented,
                                           YesOrNo respondent1Represented) {
            DynamicListElement selectedCourt = new DynamicListElement("00002", "court 2 - 2 address - Y02 7RB");
            CaseLocationCivil caseLocation = new CaseLocationCivil();
            caseLocation.setBaseLocation(selectedCourt.getCode());
            DynamicListElement transferCourtElement = new DynamicListElement(null, "Site 1 - Adr 1 - AAA 111");
            DynamicList transferCourtList = new DynamicList();
            transferCourtList.setValue(transferCourtElement);
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDraft().build();
            caseData.setCaseManagementLocation(caseLocation);
            caseData.setRespondent1Represented(respondent1Represented);
            caseData.setApplicant1Represented(applicant1Represented);
            caseData.setTransferCourtLocationList(transferCourtList);
            LocationRefData locationRefData = new LocationRefData();
            locationRefData.setEpimmsId("222");
            locationRefData.setSiteName("Site 2");
            locationRefData.setCourtAddress("Adr 2");
            locationRefData.setPostcode("BBB 222");
            locationRefData.setCourtLocationCode("other code");
            given(courtLocationUtils.findPreferredLocationData(any(), any()))
                .willReturn(locationRefData);

            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            CaseData responseCaseData = objectMapper.convertValue(response.getData(), CaseData.class);

            assertEquals(YES, responseCaseData.getEaCourtLocation());
        }

        @Test
        void shouldCallUpdateWaCourtLocationsServiceWhenPresent_AndMintiEnabled() {
            when(featureToggleService.isMultiOrIntermediateTrackEnabled(any())).thenReturn(true);
            LocationRefData locationRefData = new LocationRefData();
            locationRefData.setEpimmsId("222");
            locationRefData.setSiteName("Site 2");
            locationRefData.setCourtAddress("Adr 2");
            locationRefData.setPostcode("BBB 222");
            locationRefData.setCourtLocationCode("other code");
            given(courtLocationUtils.findPreferredLocationData(any(), any()))
                .willReturn(locationRefData);

            CaseLocationCivil caseLocation = new CaseLocationCivil();
            caseLocation.setRegion("2");
            caseLocation.setBaseLocation("111");
            DynamicListElement transferCourtElement = new DynamicListElement(null, "Site 1 - Adr 1 - AAA 111");
            DynamicList transferCourtList = new DynamicList();
            transferCourtList.setValue(transferCourtElement);
            CaseData caseData = CaseDataBuilder.builder().atStateApplicantRespondToDefenceAndProceed()
                .caseManagementLocation(caseLocation)
                .transferCourtLocationList(transferCourtList).build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
            handler.handle(params);
            verify(updateWaCourtLocationsService).updateCourtListingWALocations(any(), any());
        }

        @Test
        void shouldNotCallUpdateWaCourtLocationsServiceWhenNotPresent_AndMintiEnabled() {
            handler = new TransferOnlineCaseCallbackHandler(objectMapper, locationRefDataService, courtLocationUtils,
                                                            featureToggleService,
                                                            Optional.empty());
            when(featureToggleService.isMultiOrIntermediateTrackEnabled(any())).thenReturn(true);
            LocationRefData locationRefData = new LocationRefData();
            locationRefData.setEpimmsId("222");
            locationRefData.setSiteName("Site 2");
            locationRefData.setCourtAddress("Adr 2");
            locationRefData.setPostcode("BBB 222");
            locationRefData.setCourtLocationCode("other code");
            given(courtLocationUtils.findPreferredLocationData(any(), any()))
                .willReturn(locationRefData);

            CaseLocationCivil caseLocation = new CaseLocationCivil();
            caseLocation.setRegion("2");
            caseLocation.setBaseLocation("111");
            DynamicListElement transferCourtElement = new DynamicListElement(null, "Site 1 - Adr 1 - AAA 111");
            DynamicList transferCourtList = new DynamicList();
            transferCourtList.setValue(transferCourtElement);
            CaseData caseData = CaseDataBuilder.builder().atStateApplicantRespondToDefenceAndProceed()
                .caseManagementLocation(caseLocation)
                .transferCourtLocationList(transferCourtList).build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
            handler.handle(params);
            verifyNoInteractions(updateWaCourtLocationsService);
        }
    }

    @Nested
    class SubmittedCallback {
        @Test
        void shouldReturnExpectedSubmittedCallbackResponse() {
            String newCourtLocationSiteName = "Site 2";
            LocationRefData locationRefData = new LocationRefData();
            locationRefData.setEpimmsId("222");
            locationRefData.setSiteName(newCourtLocationSiteName);
            locationRefData.setCourtAddress("Adr 2");
            locationRefData.setPostcode("BBB 222");
            locationRefData.setCourtLocationCode("other code");
            given(courtLocationUtils.findPreferredLocationData(any(), any()))
                .willReturn(locationRefData);

            CaseData caseData = CaseDataBuilder.builder().atStateApplicantRespondToDefenceAndProceed().build();
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
