package uk.gov.hmcts.reform.civil.ga.handler.callback.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.enums.PaymentStatus;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.ga.handler.GeneralApplicationBaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.model.genapplication.CaseLink;
import uk.gov.hmcts.reform.civil.model.Fee;
import uk.gov.hmcts.reform.civil.model.GeneralAppParentCaseLink;
import uk.gov.hmcts.reform.civil.referencedata.model.LocationRefData;
import uk.gov.hmcts.reform.civil.model.PaymentDetails;
import uk.gov.hmcts.reform.civil.model.genapplication.GAApplicationType;
import uk.gov.hmcts.reform.civil.model.genapplication.CaseLocationCivil;
import uk.gov.hmcts.reform.civil.model.genapplication.GADetailsRespondentSol;
import uk.gov.hmcts.reform.civil.model.genapplication.GAHearingDetails;
import uk.gov.hmcts.reform.civil.model.genapplication.GAInformOtherParty;
import uk.gov.hmcts.reform.civil.model.genapplication.GAPbaDetails;
import uk.gov.hmcts.reform.civil.model.genapplication.GARespondentOrderAgreement;
import uk.gov.hmcts.reform.civil.model.genapplication.GASolicitorDetailsGAspec;
import uk.gov.hmcts.reform.civil.model.genapplication.GAStatementOfTruth;
import uk.gov.hmcts.reform.civil.model.genapplication.GAUrgencyRequirement;
import uk.gov.hmcts.reform.civil.model.genapplication.GeneralApplication;
import uk.gov.hmcts.reform.civil.model.genapplication.GeneralApplicationsDetails;
import uk.gov.hmcts.reform.civil.sampledata.CallbackParamsBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDetailsBuilder;
import uk.gov.hmcts.reform.civil.sampledata.GeneralApplicationCaseDataBuilder;
import uk.gov.hmcts.reform.civil.testutils.ObjectMapperFactory;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;
import uk.gov.hmcts.reform.civil.ga.service.GeneralAppLocationRefDataService;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.TRIGGER_LOCATION_UPDATE;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.TRIGGER_TASK_RECONFIG;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.enums.dq.GeneralApplicationTypes.RELIEF_FROM_SANCTIONS;
import static uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder.CUSTOMER_REFERENCE;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.wrapElements;

@ExtendWith(MockitoExtension.class)
 class UpdateGaLocationCallbackHandlerTest extends GeneralApplicationBaseCallbackHandlerTest {

    @Spy
    private ObjectMapper objectMapper = ObjectMapperFactory.instance();

    @Spy
    private CaseDetailsConverter caseDetailsConverter = new CaseDetailsConverter(objectMapper);

    @InjectMocks
    private UpdateGaLocationCallbackHandler handler;

    @Mock
    private CoreCaseDataService coreCaseDataService;

    @Mock
    private GeneralAppLocationRefDataService locationRefDataService;
    private static final Long CHILD_CCD_REF = 1646003133062762L;
    private static final Long PARENT_CCD_REF = 1645779506193000L;
    private static final String STRING_CONSTANT = "STRING_CONSTANT";

    @Test
    void handleEventsReturnsTheExpectedCallbackEvent() {
        assertThat(handler.handledEvents()).contains(TRIGGER_LOCATION_UPDATE);
    }

    @Nested
    class AboutToSubmitCallback {

        @Test
        void shouldRespondAndUpdateCaseManagementLocation() {
            GeneralApplicationCaseData caseData = getSampleGeneralApplicationCaseData(NO, YES);
            CaseDetails parentCaseDetails = CaseDetailsBuilder.builder().data(
                    getParentCaseDataAfterUpdateFromCivilService(NO, YES))
                .id(1645779506193000L)
                .build();
            when(locationRefDataService.getCourtLocationsByEpimmsId(any(), any())).thenReturn(getSampleCourLocationsRefObject());
            when(coreCaseDataService.getCase(PARENT_CCD_REF)).thenReturn(parentCaseDetails);
            when(caseDetailsConverter.toGeneralApplicationCaseData(parentCaseDetails))
                .thenReturn(getParentCaseDataAfterUpdateFromCivilService(NO, YES));
            CallbackParams params = CallbackParamsBuilder.builder()
                .of(ABOUT_TO_SUBMIT, caseData)
                .request(CallbackRequest.builder()
                             .eventId(TRIGGER_LOCATION_UPDATE.name())
                             .build())
                .build();
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).isNull();

            assertThat(response.getData()).extracting("businessProcess")
                    .extracting("status").isEqualTo("FINISHED");

            assertThat(response.getData()).extracting("businessProcess")
                    .extracting("camundaEvent").isEqualTo(
                        "TRIGGER_LOCATION_UPDATE");

            assertThat(response.getData()).containsEntry(
                    "isCcmccLocation",
                    "No");
            assertThat(response.getData()).containsEntry(
                "locationName",
                "locationForRegion2");
            assertThat(response.getData()).containsEntry(
                "caseManagementLocation",
                Map.of(
                    "region", "2",
                    "baseLocation", "00000",
                    "siteName", "locationOfRegion2",
                    "address", "Prince William House, Peel Cross Road, Salford",
                    "postcode", "M5 4RR"
                ));
        }

        protected List<LocationRefData> getSampleCourLocationsRefObject() {
            return new ArrayList<>(List.of(
                new LocationRefData()
                    .setEpimmsId("00000").setSiteName("locationOfRegion2").setCourtAddress("Prince William House, Peel Cross Road, Salford")
                    .setPostcode("M5 4RR")
                    .setCourtLocationCode("court1")
            ));
        }

        @Test
        void shouldRespondAndUpdateCaseManagementLocationForTaskReconfig() {
            GeneralApplicationCaseData caseData = getSampleGeneralApplicationCaseData(NO, YES);
            CaseDetails parentCaseDetails = CaseDetailsBuilder.builder().data(
                    getParentCaseDataAfterUpdateFromCivilService(NO, YES))
                .id(1645779506193000L)
                .build();
            when(locationRefDataService.getCourtLocationsByEpimmsId(any(), any())).thenReturn(getSampleCourLocationsRefObject());
            when(coreCaseDataService.getCase(PARENT_CCD_REF)).thenReturn(parentCaseDetails);
            when(caseDetailsConverter.toGeneralApplicationCaseData(parentCaseDetails))
                .thenReturn(getParentCaseDataAfterUpdateFromCivilService(NO, YES));
            CallbackParams params = CallbackParamsBuilder.builder()
                .of(ABOUT_TO_SUBMIT, caseData)
                .request(CallbackRequest.builder()
                             .eventId(TRIGGER_TASK_RECONFIG.name())
                             .build())
                .build();
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).isNull();

            assertThat(response.getData()).extracting("businessProcess")
                .extracting("status").isEqualTo("FINISHED");

            assertThat(response.getData()).extracting("businessProcess")
                .extracting("camundaEvent").isEqualTo(
                    "TRIGGER_TASK_RECONFIG");

            assertThat(response.getData()).containsEntry(
                "isCcmccLocation",
                "No");
            assertThat(response.getData()).containsEntry(
                "locationName",
                "locationForRegion2");
            assertThat(response.getData()).containsEntry(
                "caseManagementLocation",
                Map.of(
                    "region", "2",
                    "baseLocation", "00000",
                    "siteName", "locationOfRegion2",
                    "address", "Prince William House, Peel Cross Road, Salford",
                    "postcode", "M5 4RR"
                ));
        }

        private GeneralApplication getGeneralApplication(YesOrNo isConsented, YesOrNo isTobeNotified) {
            return GeneralApplication.builder()
                .generalAppType(GAApplicationType.builder().types(List.of(RELIEF_FROM_SANCTIONS)).build())
                .generalAppRespondentAgreement(GARespondentOrderAgreement.builder().hasAgreed(isConsented).build())
                .generalAppInformOtherParty(GAInformOtherParty.builder().isWithNotice(isTobeNotified).build())
                .generalAppPBADetails(
                    GAPbaDetails.builder()
                        .paymentDetails(new PaymentDetails()
                                            .setStatus(PaymentStatus.SUCCESS)
                                            .setReference("RC-1658-4258-2679-9795")
                                            .setCustomerReference(CUSTOMER_REFERENCE)
                                            )
                        .fee(
                            new Fee()
                                .setCode("FE203")
                                .setCalculatedAmountInPence(BigDecimal.valueOf(27500))
                                .setVersion("1")
                                )
                        .serviceReqReference(CUSTOMER_REFERENCE).build())
                .generalAppDetailsOfOrder(STRING_CONSTANT)
                .generalAppReasonsOfOrder(STRING_CONSTANT)
                .generalAppUrgencyRequirement(GAUrgencyRequirement.builder().generalAppUrgency(NO).build())
                .generalAppStatementOfTruth(GAStatementOfTruth.builder().build())
                .generalAppHearingDetails(GAHearingDetails.builder().build())
                .generalAppRespondentSolicitors(wrapElements(GASolicitorDetailsGAspec.builder()
                                                                 .email("abc@gmail.com").build()))
                .isMultiParty(NO)
                .isCcmccLocation(YES)
                .caseManagementLocation(CaseLocationCivil.builder()
                                            .baseLocation("687686")
                                            .region("4").build())
                .parentClaimantIsApplicant(YES)
                .generalAppParentCaseLink(new GeneralAppParentCaseLink()
                                              .setCaseReference(PARENT_CCD_REF.toString()))
                .build();
        }

        private GeneralApplicationCaseData getParentCaseDataAfterUpdateFromCivilService(YesOrNo isConsented, YesOrNo isTobeNotified) {
            return new GeneralApplicationCaseData()
                .generalApplications(wrapElements(getGeneralApplication(isConsented, isTobeNotified)))
                .caseManagementLocation(CaseLocationCivil.builder()
                                            .baseLocation("00000")
                                            .region("2")
                                            .siteName("locationForRegion2").build())
                .locationName("locationForRegion2")
                .claimantGaAppDetails(wrapElements(GeneralApplicationsDetails.builder()
                                                       .caseLink(CaseLink.builder()
                                                                     .caseReference(CHILD_CCD_REF.toString())
                                                                     .build())
                                                       .caseState("Awaiting Respondent Response")
                                                       .build()))
                .gaDetailsMasterCollection(wrapElements(GeneralApplicationsDetails.builder()
                                                            .caseLink(CaseLink.builder()
                                                                          .caseReference(CHILD_CCD_REF.toString())
                                                                          .build())
                                                            .caseState("Awaiting Respondent Response")
                                                            .build()))
                .respondentSolGaAppDetails(wrapElements(GADetailsRespondentSol.builder()
                                                            .caseLink(CaseLink.builder()
                                                                          .caseReference(CHILD_CCD_REF.toString())
                                                                          .build())
                                                            .caseState("Awaiting Respondent Response")
                                                            .build()))
                .respondentSolTwoGaAppDetails(wrapElements(GADetailsRespondentSol.builder()
                                                               .caseLink(CaseLink.builder()
                                                                             .caseReference(CHILD_CCD_REF.toString())
                                                                             .build())
                                                               .caseState("Awaiting Respondent Response")
                                                               .build()))
                .build();
        }

        private GeneralApplicationCaseData getSampleGeneralApplicationCaseData(YesOrNo isConsented, YesOrNo isTobeNotified) {
            return GeneralApplicationCaseDataBuilder.builder().buildCaseDateBaseOnGeneralApplication(
                    getGeneralApplication(isConsented, isTobeNotified))
                .copy().ccdCaseReference(CHILD_CCD_REF).build();
        }
    }
}
