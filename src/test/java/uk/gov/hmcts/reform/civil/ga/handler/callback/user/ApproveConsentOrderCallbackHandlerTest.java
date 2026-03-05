package uk.gov.hmcts.reform.civil.ga.handler.callback.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.enums.BusinessProcessStatus;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.enums.dq.GeneralApplicationTypes;
import uk.gov.hmcts.reform.civil.ga.handler.GeneralApplicationBaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.BusinessProcess;
import uk.gov.hmcts.reform.civil.ga.model.GARespondentRepresentative;
import uk.gov.hmcts.reform.civil.model.genapplication.GAApplicationType;
import uk.gov.hmcts.reform.civil.ga.model.genapplication.GAApproveConsentOrder;
import uk.gov.hmcts.reform.civil.model.genapplication.GAInformOtherParty;
import uk.gov.hmcts.reform.civil.model.genapplication.GARespondentOrderAgreement;
import uk.gov.hmcts.reform.civil.testutils.ObjectMapperFactory;
import uk.gov.hmcts.reform.civil.sampledata.PDFBuilder;
import uk.gov.hmcts.reform.civil.ga.service.docmosis.consentorder.ConsentOrderGenerator;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.MID;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.SUBMITTED;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.APPROVE_CONSENT_ORDER;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;

@ExtendWith(MockitoExtension.class)
 class ApproveConsentOrderCallbackHandlerTest extends GeneralApplicationBaseCallbackHandlerTest {

    @Spy
    private ObjectMapper mapper = ObjectMapperFactory.instance();

    @Spy
    private CaseDetailsConverter caseDetailsConverter = new CaseDetailsConverter(mapper);

    private static final String CAMUNDA_EVENT = "APPROVE_CONSENT_ORDER";
    private static final String BUSINESS_PROCESS_INSTANCE_ID = "11111";
    private static final String ACTIVITY_ID = "anyActivity";
    public static final String ORDER_DATE_IN_PAST = "The date, by which the order to end"
        + " should be given, cannot be in past.";

    @InjectMocks
    private ApproveConsentOrderCallbackHandler handler;

    @Mock
    private ConsentOrderGenerator consentOrderGenerator;

    @Test
    void handleEventsReturnsTheExpectedCallbackEventApproveConsentOrder() {
        assertThat(handler.handledEvents()).contains(APPROVE_CONSENT_ORDER);
    }

    @Nested
    class AboutToStartCallbackHandling {

        @Test
        void shouldReturnApproveConsentOrderEndDateEnableWhenApplicationTypeContainsStayTheClaim() {
            List<GeneralApplicationTypes> types = List.of(
                (GeneralApplicationTypes.STAY_THE_CLAIM), (GeneralApplicationTypes.SUMMARY_JUDGEMENT));
            CallbackParams params = callbackParamsOf(getGeneralAppCaseData(types), ABOUT_TO_START);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            assertThat(response).isNotNull();
            GeneralApplicationCaseData data = mapper.convertValue(response.getData(), GeneralApplicationCaseData.class);
            assertThat(data.getGeneralAppDetailsOfOrder()).isEqualTo(data.getApproveConsentOrder().getConsentOrderDescription());
            assertThat(data.getApproveConsentOrder().getShowConsentOrderDate()).isEqualTo(YES);
        }

        @Test
        void shouldNotReturnApproveConsentOrderEndDateWhenApplicationTypeDoesNotContainsStayTheClaim() {
            List<GeneralApplicationTypes> types = List.of(
                (GeneralApplicationTypes.EXTEND_TIME), (GeneralApplicationTypes.SUMMARY_JUDGEMENT));
            CallbackParams params = callbackParamsOf(getGeneralAppCaseData(types), ABOUT_TO_START);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            assertThat(response).isNotNull();
            GeneralApplicationCaseData data = mapper.convertValue(response.getData(), GeneralApplicationCaseData.class);
            assertThat(data.getGeneralAppDetailsOfOrder()).isEqualTo(data.getApproveConsentOrder().getConsentOrderDescription());
            assertThat(data.getApproveConsentOrder().getShowConsentOrderDate()).isEqualTo(NO);
        }

        @Test
        void shouldNotAutoCompleteConsentOrderWhenApplicationOrderDetailsEmpty() {
            List<GeneralApplicationTypes> types = List.of(
                (GeneralApplicationTypes.EXTEND_TIME), (GeneralApplicationTypes.SUMMARY_JUDGEMENT));
            CallbackParams params = callbackParamsOf(getGeneralAppCaseDataWithoutOrderDetails(types), ABOUT_TO_START);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            assertThat(response).isNotNull();
            GeneralApplicationCaseData data = mapper.convertValue(response.getData(), GeneralApplicationCaseData.class);
            assertThat(data.getApproveConsentOrder()).isNull();
        }
    }

    @Nested
    class MidEventToValidate {

        @BeforeEach
        void setup() {

            when(consentOrderGenerator.generate(any(), any()))
                .thenReturn(PDFBuilder.CONSENT_ORDER_DOCUMENT);
        }

        private static final String VALIDATE_CONSENT_ORDER = "populate-consent-order-doc";

        @Test
        void shouldGenerateConsentOrderDocument() {
            List<GeneralApplicationTypes> types = List.of(
                                                          (GeneralApplicationTypes.EXTEND_TIME), (GeneralApplicationTypes.SUMMARY_JUDGEMENT));
            CallbackParams params = callbackParamsOf(getGeneralAppCaseData(types), MID, VALIDATE_CONSENT_ORDER);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            GeneralApplicationCaseData updatedData = mapper.convertValue(response.getData(), GeneralApplicationCaseData.class);
            assertThat(response).isNotNull();
        }

        @Test
        void shouldThrowErrorsWhileValidatingDate() {
            List<GeneralApplicationTypes> types = List.of(
                (GeneralApplicationTypes.EXTEND_TIME), (GeneralApplicationTypes.STAY_THE_CLAIM));
            CallbackParams params = callbackParamsOf(getGeneralAppCaseDataForGeneratingDocument(types,
                                                                                                LocalDate.now().minusDays(1),
                                                                                                YES),
                                                     MID, "populate-consent-order-doc");
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            assertThat(response.getErrors()).isNotEmpty();
            assertThat(response.getErrors()).contains(ORDER_DATE_IN_PAST);
        }

        @Test
        void shouldNotThrowErrorsWhileValidatingDate() {
            List<GeneralApplicationTypes> types = List.of(
                (GeneralApplicationTypes.EXTEND_TIME), (GeneralApplicationTypes.STAY_THE_CLAIM));
            CallbackParams params = callbackParamsOf(getGeneralAppCaseDataForGeneratingDocument(types,
                                                                                                LocalDate.now().plusDays(1),
                                                                                                YES),
                                                     MID, "populate-consent-order-doc");
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            assertThat(response.getErrors()).isEmpty();
        }

        @Test
        void shouldNotThrowErrorsWhileValidatingDateWithOrderDateAsNull() {
            List<GeneralApplicationTypes> types = List.of(
                (GeneralApplicationTypes.EXTEND_TIME), (GeneralApplicationTypes.STAY_THE_CLAIM));
            CallbackParams params = callbackParamsOf(getGeneralAppCaseDataForGeneratingDocument(types,
                                                                                                null,
                                                                                                YES),
                                                     MID, "populate-consent-order-doc");
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            assertThat(response.getErrors()).isEmpty();
        }

        @Test
        void shouldNotThrowErrorsWhileValidatingDateWithOrderDateAsNullAndDisplayIsNotVisible() {
            List<GeneralApplicationTypes> types = List.of(
                (GeneralApplicationTypes.EXTEND_TIME), (GeneralApplicationTypes.SUMMARY_JUDGEMENT));
            CallbackParams params = callbackParamsOf(getGeneralAppCaseDataForGeneratingDocument(types,
                                                                                                null,
                                                                                                NO),
                                                     MID, "populate-consent-order-doc");
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            assertThat(response.getErrors()).isEmpty();
        }
    }

    @Nested
    class AboutToSubmitHandling {

        @Test
        void shouldValidateBusinessProcess() {

            List<GeneralApplicationTypes> types = List.of(
                (GeneralApplicationTypes.EXTEND_TIME), (GeneralApplicationTypes.STAY_THE_CLAIM));
            CallbackParams params = callbackParamsOf(getGeneralAppCaseData(types), ABOUT_TO_SUBMIT);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            assertThat(response.getData()).extracting("businessProcess").extracting("camundaEvent").isEqualTo(
                "APPROVE_CONSENT_ORDER");
        }

    }

    @Nested
    class SubmittedCallbackHandling {

        @Test
        void callbackHandlingForMakeAnOrder() {

            List<GeneralApplicationTypes> types = List.of(
                (GeneralApplicationTypes.EXTEND_TIME), (GeneralApplicationTypes.SUMMARY_JUDGEMENT));
            CallbackParams params = callbackParamsOf(getGeneralAppCaseData(types), SUBMITTED);
            var response = (SubmittedCallbackResponse) handler.handle(params);

            assertThat(response.getConfirmationHeader()).isEqualTo("# Your order has been made");
            assertThat(response.getConfirmationBody()).isEqualTo("<br/><br/>");
        }
    }

    public GeneralApplicationCaseData getGeneralAppCaseData(List<GeneralApplicationTypes> types) {

        return new GeneralApplicationCaseData()
            .generalAppDetailsOfOrder("Testing prepopulated text")
            .generalAppRespondentAgreement(GARespondentOrderAgreement.builder().hasAgreed(NO).build())
            .generalAppRespondentAgreement(GARespondentOrderAgreement.builder().hasAgreed(YES).build())
            .generalAppInformOtherParty(GAInformOtherParty.builder().isWithNotice(YES).build())
            .createdDate(LocalDateTime.of(2022, 1, 15, 0, 0, 0))
            .applicantPartyName("ApplicantPartyName")
            .generalAppRespondent1Representative(
                new GARespondentRepresentative()
                    .setGeneralAppRespondent1Representative(YES)
                    )
            .generalAppType(
                GAApplicationType
                    .builder()
                    .types(types).build())
            .businessProcess(new BusinessProcess()
                                 .setCamundaEvent(CAMUNDA_EVENT)
                                 .setProcessInstanceId(BUSINESS_PROCESS_INSTANCE_ID)
                                 .setStatus(BusinessProcessStatus.STARTED)
                                 .setActivityId(ACTIVITY_ID))
            .ccdState(CaseState.APPLICATION_SUBMITTED_AWAITING_JUDICIAL_DECISION)
            .build();
    }

    public GeneralApplicationCaseData getGeneralAppCaseDataForGeneratingDocument(List<GeneralApplicationTypes> types, LocalDate orderDate,
                                                               YesOrNo showConsent) {

        return new GeneralApplicationCaseData()
            .generalAppDetailsOfOrder("Testing prepopulated text")
            .approveConsentOrder(new GAApproveConsentOrder().setConsentOrderDescription("Testing prepopulated text")
                                     .setShowConsentOrderDate(showConsent)
                                     .setConsentOrderDateToEnd(orderDate)
                                    )
            .generalAppRespondentAgreement(GARespondentOrderAgreement.builder().hasAgreed(NO).build())
            .generalAppRespondentAgreement(GARespondentOrderAgreement.builder().hasAgreed(YES).build())
            .generalAppInformOtherParty(GAInformOtherParty.builder().isWithNotice(YES).build())
            .createdDate(LocalDateTime.of(2022, 1, 15, 0, 0, 0))
            .applicantPartyName("ApplicantPartyName")
            .generalAppRespondent1Representative(
                new GARespondentRepresentative()
                    .setGeneralAppRespondent1Representative(YES)
                    )
            .generalAppType(
                GAApplicationType
                    .builder()
                    .types(types).build())
            .businessProcess(new BusinessProcess()
                                 .setCamundaEvent(CAMUNDA_EVENT)
                                 .setProcessInstanceId(BUSINESS_PROCESS_INSTANCE_ID)
                                 .setStatus(BusinessProcessStatus.STARTED)
                                 .setActivityId(ACTIVITY_ID))
            .ccdState(CaseState.APPLICATION_SUBMITTED_AWAITING_JUDICIAL_DECISION)
            .build();
    }

    public GeneralApplicationCaseData getGeneralAppCaseDataWithoutOrderDetails(List<GeneralApplicationTypes> types) {

        return new GeneralApplicationCaseData()
            .generalAppRespondentAgreement(GARespondentOrderAgreement.builder().hasAgreed(NO).build())
            .generalAppRespondentAgreement(GARespondentOrderAgreement.builder().hasAgreed(YES).build())
            .generalAppInformOtherParty(GAInformOtherParty.builder().isWithNotice(YES).build())
            .createdDate(LocalDateTime.of(2022, 1, 15, 0, 0, 0))
            .applicantPartyName("ApplicantPartyName")
            .generalAppRespondent1Representative(
                new GARespondentRepresentative()
                    .setGeneralAppRespondent1Representative(YES)
                    )
            .generalAppType(
                GAApplicationType
                    .builder()
                    .types(types).build())
            .businessProcess(new BusinessProcess()
                                 .setCamundaEvent(CAMUNDA_EVENT)
                                 .setProcessInstanceId(BUSINESS_PROCESS_INSTANCE_ID)
                                 .setStatus(BusinessProcessStatus.STARTED)
                                 .setActivityId(ACTIVITY_ID))
            .ccdState(CaseState.APPLICATION_SUBMITTED_AWAITING_JUDICIAL_DECISION)
            .build();
    }
}
