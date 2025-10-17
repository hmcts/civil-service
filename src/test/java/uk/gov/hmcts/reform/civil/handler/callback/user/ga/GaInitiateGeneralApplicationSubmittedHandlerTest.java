package uk.gov.hmcts.reform.civil.handler.callback.user.ga;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.enums.BusinessProcessStatus;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.model.BusinessProcess;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Fee;
import uk.gov.hmcts.reform.civil.model.genapplication.GAApplicationType;
import uk.gov.hmcts.reform.civil.model.genapplication.GAInformOtherParty;
import uk.gov.hmcts.reform.civil.model.genapplication.GAPbaDetails;
import uk.gov.hmcts.reform.civil.model.genapplication.GARespondentOrderAgreement;
import uk.gov.hmcts.reform.civil.model.genapplication.GAUrgencyRequirement;
import uk.gov.hmcts.reform.civil.model.genapplication.GeneralApplication;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.GeneralAppFeesService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;

import static java.lang.String.format;
import static java.time.LocalDate.EPOCH;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.SUBMITTED;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.INITIATE_GENERAL_APPLICATION;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.enums.dq.GeneralApplicationTypes.EXTEND_TIME;
import static uk.gov.hmcts.reform.civil.enums.dq.GeneralApplicationTypes.SUMMARY_JUDGEMENT;
import static uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder.CUSTOMER_REFERENCE;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.wrapElements;

@SpringBootTest(classes = {
    GaInitiateGeneralApplicationSubmittedHandler.class,
    JacksonAutoConfiguration.class,
},
    properties = {"reference.database.enabled=false"})
class GaInitiateGeneralApplicationSubmittedHandlerTest extends BaseCallbackHandlerTest {

    @Autowired
    private GaInitiateGeneralApplicationSubmittedHandler handler;
    @MockBean
    private GeneralAppFeesService generalAppFeesService;

    @MockBean
    private FeatureToggleService featureToggleService;

    @Value("${civil.response-pack-url}")
    private String responsePackUrl;

    private static final LocalDate APP_DATE_EPOCH = EPOCH;
    private static final String CONFIRMATION_BODY_FREE = "<br/> <p> The court will make a decision"
        + " on this application."
        + "<br/> <p>  The other party's legal representative has been notified that you have"
        + " submitted this application";
    private static final Fee FEE275 = Fee.builder().calculatedAmountInPence(
        BigDecimal.valueOf(27500)).code("FEE0444").version("1").build();

    private CaseData getEmptyTestCase(CaseData caseData) {
        return caseData.toBuilder().build();
    }

    private CaseData getReadyTestCaseData(CaseData caseData, boolean multipleGenAppTypes) {
        GAInformOtherParty withOrWithoutNotice = GAInformOtherParty.builder()
            .isWithNotice(YES)
            .reasonsForWithoutNotice(responsePackUrl)
            .build();
        GARespondentOrderAgreement withOrWithoutConsent = GARespondentOrderAgreement.builder()
            .hasAgreed(NO).build();

        return getReadyTestCaseData(caseData, multipleGenAppTypes, withOrWithoutConsent, withOrWithoutNotice);
    }

    private CaseData getReadyTestCaseData(CaseData caseData,
                                          boolean multipleGenAppTypes,
                                          GARespondentOrderAgreement hasAgreed,
                                          GAInformOtherParty withOrWithoutNotice) {
        GeneralApplication.GeneralApplicationBuilder builder = GeneralApplication.builder();
        if (multipleGenAppTypes) {
            builder.generalAppType(GAApplicationType.builder()
                                   .types(Arrays.asList(EXTEND_TIME, SUMMARY_JUDGEMENT))
                                   .build());
        } else {
            builder.generalAppType(GAApplicationType.builder()
                                   .types(singletonList(EXTEND_TIME))
                                   .build());
        }
        GeneralApplication application = builder
            .generalAppInformOtherParty(withOrWithoutNotice)
            .generalAppRespondentAgreement(hasAgreed)
            .generalAppPBADetails(
                GAPbaDetails.builder()
                    .fee(FEE275)
                    .serviceReqReference(CUSTOMER_REFERENCE).build())
            .generalAppUrgencyRequirement(GAUrgencyRequirement.builder()
                                              .generalAppUrgency(YES)
                                              .reasonsForUrgency(responsePackUrl)
                                              .urgentAppConsiderationDate(APP_DATE_EPOCH)
                                              .build())
            .isMultiParty(NO)
            .businessProcess(BusinessProcess.builder()
                                   .status(BusinessProcessStatus.READY)
                                   .build())
            .build();
        return getEmptyTestCase(caseData)
            .toBuilder()
            .generalApplications(wrapElements(application))
            .build();
    }

    private String confirmationBodyBasedOnToggle(Boolean isGaForLipsEnabled) {
        StringBuilder bodyConfirmation = new StringBuilder();
        bodyConfirmation.append("<br/>");
        bodyConfirmation.append("<p class=\"govuk-body govuk-!-font-weight-bold\"> Your application fee of £%s"
                                    + " is now due for payment. Your application will not be processed further"
                                    + " until this fee is paid.</p>");
        bodyConfirmation.append("%n%n To pay this fee, click the link below, or else open your application from the"
                                    + " Applications tab of this case listing and then click on the service request tab.");

        if (isGaForLipsEnabled) {
            bodyConfirmation.append("%n%n If necessary, all documents relating to this application, "
                                        + "including any response from the court, will be translated."
                                        + " You will be notified when these are available.");
        }

        bodyConfirmation.append("%n%n <a href=\"%s\" target=\"_blank\">Pay your application fee </a> %n");
        return bodyConfirmation.toString();
    }

    @Nested
    class SubmittedCallback {
        @BeforeEach
        void setup() {
            when(featureToggleService.isGaForLipsEnabled()).thenReturn(false);
        }

        @Test
        void handleEventsReturnsTheExpectedCallbackEvent() {
            assertThat(handler.handledEvents()).contains(INITIATE_GENERAL_APPLICATION);
        }

        @Test
        void shouldReturnExpectedSubmittedCallbackResponse_whenRespondentsDoesNotHaveRepresentation() {
            CaseData caseData = getReadyTestCaseData(CaseDataBuilder.builder().ccdCaseReference(CASE_ID).build(), true);
            CallbackParams params = callbackParamsOf(caseData, SUBMITTED);
            GeneralApplication genapp = caseData.getGeneralApplications().get(0).getValue();
            when(generalAppFeesService.isFreeGa(any())).thenReturn(false);
            String body = format(
                confirmationBodyBasedOnToggle(false),
                genapp.getGeneralAppPBADetails().getFee().toPounds(),
                format("/cases/case-details/%s#Applications", CASE_ID));

            var response = (SubmittedCallbackResponse) handler.handle(params);
            assertThat(response).isNotNull();
            assertThat(response).usingRecursiveComparison().isEqualTo(
                SubmittedCallbackResponse.builder()
                    .confirmationHeader(
                        "# You have submitted an application")
                    .confirmationBody(body)
                    .build());
            assertThat(response.getConfirmationBody()).isEqualTo(body);
        }

        @Test
        void shouldReturnExpectedSubmittedCallbackResponse_whengaLips_is_enable() {
            CaseData caseData = getReadyTestCaseData(CaseDataBuilder.builder().ccdCaseReference(CASE_ID).build(), true);
            CallbackParams params = callbackParamsOf(caseData, SUBMITTED);
            GeneralApplication genapp = caseData.getGeneralApplications().get(0).getValue();
            when(generalAppFeesService.isFreeGa(any())).thenReturn(false);
            when(featureToggleService.isGaForLipsEnabled()).thenReturn(true);
            String body = format(
                confirmationBodyBasedOnToggle(true),
                genapp.getGeneralAppPBADetails().getFee().toPounds(),
                format("/cases/case-details/%s#Applications", CASE_ID));

            var response = (SubmittedCallbackResponse) handler.handle(params);
            assertThat(response).isNotNull();
            assertThat(response).usingRecursiveComparison().isEqualTo(
                SubmittedCallbackResponse.builder()
                    .confirmationHeader(
                        "# You have submitted an application")
                    .confirmationBody(body)
                    .build());
            assertThat(response.getConfirmationBody()).isEqualTo(body);
        }

        @Test
        void shouldReturnFreeGAConfirmationBodyBody_whenFreeGA() {
            CaseData caseData = getReadyTestCaseData(CaseDataBuilder.builder().ccdCaseReference(CASE_ID).build(), true);
            CallbackParams params = callbackParamsOf(caseData, SUBMITTED);
            when(generalAppFeesService.isFreeGa(any())).thenReturn(true);

            var response = (SubmittedCallbackResponse) handler.handle(params);
            assertThat(response).isNotNull();
            assertThat(response).usingRecursiveComparison().isEqualTo(
                SubmittedCallbackResponse.builder()
                    .confirmationHeader(
                        "# You have submitted an application")
                    .confirmationBody(CONFIRMATION_BODY_FREE)
                    .build());
            assertThat(response.getConfirmationBody()).isEqualTo(CONFIRMATION_BODY_FREE);
        }

        @Test
        void shouldNotReturnBuildConfirmationIfGeneralApplicationIsEmpty() {
            CaseData caseData = getEmptyTestCase(CaseDataBuilder.builder().build());
            CallbackParams params = callbackParamsOf(caseData, SUBMITTED);

            var response = (SubmittedCallbackResponse) handler.handle(params);
            assertThat(response).isNotNull();
            assertThat(response.getConfirmationBody()).isNull();
        }
    }
}
