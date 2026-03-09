package uk.gov.hmcts.reform.civil.ga.handler.callback.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.enums.PaymentStatus;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.enums.dq.GeneralApplicationTypes;
import uk.gov.hmcts.reform.civil.ga.handler.GeneralApplicationBaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.ga.model.genapplication.GeneralApplicationPbaDetails;
import uk.gov.hmcts.reform.civil.model.Fee;
import uk.gov.hmcts.reform.civil.model.GeneralAppParentCaseLink;
import uk.gov.hmcts.reform.civil.model.PaymentDetails;
import uk.gov.hmcts.reform.civil.model.genapplication.GAApplicationType;
import uk.gov.hmcts.reform.civil.model.genapplication.GAHearingDetails;
import uk.gov.hmcts.reform.civil.model.genapplication.GAInformOtherParty;
import uk.gov.hmcts.reform.civil.model.genapplication.GAPbaDetails;
import uk.gov.hmcts.reform.civil.model.genapplication.GARespondentOrderAgreement;
import uk.gov.hmcts.reform.civil.model.genapplication.GASolicitorDetailsGAspec;
import uk.gov.hmcts.reform.civil.model.genapplication.GAStatementOfTruth;
import uk.gov.hmcts.reform.civil.model.genapplication.GAUrgencyRequirement;
import uk.gov.hmcts.reform.civil.model.genapplication.GeneralApplication;
import uk.gov.hmcts.reform.civil.ga.service.GaForLipService;
import uk.gov.hmcts.reform.civil.sampledata.GeneralApplicationCaseDataBuilder;
import uk.gov.hmcts.reform.civil.testutils.ObjectMapperFactory;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.INITIATE_COSC_APPLICATION_AFTER_PAYMENT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.INITIATE_GENERAL_APPLICATION_AFTER_PAYMENT;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.enums.dq.GeneralApplicationTypes.RELIEF_FROM_SANCTIONS;
import static uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder.CUSTOMER_REFERENCE;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.wrapElements;

@ExtendWith(MockitoExtension.class)
public class GeneralApplicationAfterPaymentCallbackHandlerTest extends GeneralApplicationBaseCallbackHandlerTest {

    @Spy
    private ObjectMapper objectMapper = ObjectMapperFactory.instance();

    @InjectMocks
    private GeneralApplicationAfterPaymentCallbackHandler handler;

    @Mock
    private GaForLipService gaForLipService;

    private static final String STRING_CONSTANT = "STRING_CONSTANT";
    private static final Long CHILD_CCD_REF = 1646003133062762L;
    private static final Long PARENT_CCD_REF = 1645779506193000L;

    @Test
    void shouldTriggerCoscBusinessProcess() {
        GeneralApplicationCaseData caseData = getSampleGeneralApplicationCaseData(NO, YES);
        caseData = addGeneralAppType(caseData, GeneralApplicationTypes.CONFIRM_CCJ_DEBT_PAID);
        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
        when(gaForLipService.isLipApp(any(GeneralApplicationCaseData.class))).thenReturn(false);
        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
        GeneralApplicationCaseData responseCaseData = objectMapper.convertValue(response.getData(), GeneralApplicationCaseData.class);
        assertThat(responseCaseData.getBusinessProcess().getCamundaEvent()).isEqualTo(INITIATE_COSC_APPLICATION_AFTER_PAYMENT.name());
    }

    @Test
    void shouldTriggerTheEventAndAboutToSubmit() {
        GeneralApplicationCaseData caseData = getSampleGeneralApplicationCaseData(NO, YES);
        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
        when(gaForLipService.isLipApp(any(GeneralApplicationCaseData.class))).thenReturn(false);
        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
        GeneralApplicationCaseData responseCaseData = objectMapper.convertValue(response.getData(), GeneralApplicationCaseData.class);
        assertThat(responseCaseData.getBusinessProcess().getCamundaEvent()).isEqualTo(INITIATE_GENERAL_APPLICATION_AFTER_PAYMENT.name());
    }

    @Test
    void shouldTriggerTheEventAndAboutToSubmitWithoutBusinessProcess() {
        GeneralApplicationCaseData caseData = getSampleGeneralApplicationCaseData(NO, YES);
        caseData = addPaymentStatusToGAPbaDetails(caseData, PaymentStatus.FAILED);

        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
        when(gaForLipService.isLipApp(any(GeneralApplicationCaseData.class))).thenReturn(true);
        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
        GeneralApplicationCaseData responseCaseData = objectMapper.convertValue(response.getData(), GeneralApplicationCaseData.class);
        assertThat(responseCaseData.getBusinessProcess()).isNull();
    }

    @Test
    void shouldTriggerTheEventAndAboutToSubmitWithBusinessProcess() {
        GeneralApplicationCaseData caseData = getSampleGeneralApplicationCaseData(NO, YES);
        caseData = addPaymentStatusToGAPbaDetails(caseData, PaymentStatus.SUCCESS);

        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
        when(gaForLipService.isLipApp(any(GeneralApplicationCaseData.class))).thenReturn(true);
        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
        GeneralApplicationCaseData responseCaseData = objectMapper.convertValue(response.getData(), GeneralApplicationCaseData.class);
        assertThat(responseCaseData.getBusinessProcess().getCamundaEvent()).isEqualTo(INITIATE_GENERAL_APPLICATION_AFTER_PAYMENT.name());
    }

    @Test
    void handleEventsReturnsTheExpectedCallbackEvent() {
        assertThat(handler.handledEvents()).contains(
            INITIATE_GENERAL_APPLICATION_AFTER_PAYMENT);
    }

    private GeneralApplicationCaseData getSampleGeneralApplicationCaseData(YesOrNo isConsented, YesOrNo isTobeNotified) {
        return GeneralApplicationCaseDataBuilder.builder().buildCaseDateBaseOnGeneralApplication(
                getGeneralApplicationBeforePayment(isConsented, isTobeNotified))
            .copy().ccdCaseReference(CHILD_CCD_REF).build();
    }

    private GeneralApplication getGeneralApplicationBeforePayment(YesOrNo isConsented, YesOrNo isTobeNotified) {
        return GeneralApplication.builder()
            .generalAppType(GAApplicationType.builder().types(List.of(RELIEF_FROM_SANCTIONS)).build())
            .generalAppRespondentAgreement(GARespondentOrderAgreement.builder().hasAgreed(isConsented).build())
            .generalAppInformOtherParty(GAInformOtherParty.builder().isWithNotice(isTobeNotified).build())
            .generalAppPBADetails(
                GAPbaDetails.builder()
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
            .parentClaimantIsApplicant(YES)
            .generalAppParentCaseLink(new GeneralAppParentCaseLink()
                                          .setCaseReference(PARENT_CCD_REF.toString()))
            .build();
    }

    private GeneralApplicationCaseData addPaymentStatusToGAPbaDetails(GeneralApplicationCaseData caseData, PaymentStatus status) {
        GeneralApplicationPbaDetails pbaDetails = caseData.getGeneralAppPBADetails();
        GeneralApplicationPbaDetails updatedPbaDetails = pbaDetails == null
            ? new GeneralApplicationPbaDetails()
            : pbaDetails.copy();

        PaymentDetails paymentDetails = new PaymentDetails()
            .setStatus(status)
            ;
        updatedPbaDetails.setPaymentDetails(paymentDetails);
        return caseData.copy()
            .generalAppPBADetails(updatedPbaDetails)
            .build();
    }

    private GeneralApplicationCaseData addGeneralAppType(GeneralApplicationCaseData caseData, GeneralApplicationTypes generalApplicationTypes) {
        return caseData.copy().generalAppType(
                GAApplicationType.builder().types(List.of(generalApplicationTypes))
                    .build())
            .build();
    }
}
