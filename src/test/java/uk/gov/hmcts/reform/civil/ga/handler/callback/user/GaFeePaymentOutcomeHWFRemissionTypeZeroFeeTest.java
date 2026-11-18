package uk.gov.hmcts.reform.civil.ga.handler.callback.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.enums.FeeType;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.ga.handler.GeneralApplicationBaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.ga.model.genapplication.HelpWithFeesDetails;
import uk.gov.hmcts.reform.civil.ga.service.GaPaymentRequestUpdateCallbackService;
import uk.gov.hmcts.reform.civil.ga.service.HwfNotificationService;
import uk.gov.hmcts.reform.civil.model.citizenui.FeePaymentOutcomeDetails;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.testutils.ObjectMapperFactory;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.MID;

@ExtendWith(MockitoExtension.class)
class GaFeePaymentOutcomeHWFRemissionTypeZeroFeeTest extends GeneralApplicationBaseCallbackHandlerTest {

    private static final String REMISSION_TYPE_PAGE = "remission-type";
    private static final String WRONG_REMISSION_TYPE_SELECTED = "Incorrect remission type selected";

    @Spy
    private ObjectMapper mapper = ObjectMapperFactory.instance();
    @InjectMocks
    private GaFeePaymentOutcomeHWFCallBackHandler handler;
    @Mock
    private GaPaymentRequestUpdateCallbackService service;
    @Mock
    private HwfNotificationService hwfNotificationService;
    @Mock
    private FeatureToggleService featureToggleService;

    @ParameterizedTest
    @ValueSource(strings = {"0", "0.00"})
    void shouldNotReturnError_whenGaFullRemissionAndOutstandingFeeIsZero(String outstandingFee) {
        GeneralApplicationCaseData caseData = applicationCase(new BigDecimal(outstandingFee));

        assertThat(validateRemissionType(caseData).getErrors()).isEmpty();
    }

    @ParameterizedTest
    @ValueSource(strings = {"0", "0.00"})
    void shouldNotReturnError_whenAdditionalFullRemissionAndOutstandingFeeIsZero(String outstandingFee) {
        GeneralApplicationCaseData caseData = additionalCase(new BigDecimal(outstandingFee));

        assertThat(validateRemissionType(caseData).getErrors()).isEmpty();
    }

    @ParameterizedTest
    @ValueSource(strings = {"0.01", "100.00"})
    void shouldStillReturnError_whenGaFullRemissionAndOutstandingFeeIsNotZero(String outstandingFee) {
        GeneralApplicationCaseData caseData = applicationCase(new BigDecimal(outstandingFee));

        assertThat(validateRemissionType(caseData).getErrors()).containsExactly(WRONG_REMISSION_TYPE_SELECTED);
    }

    @ParameterizedTest
    @ValueSource(strings = {"0.01", "100.00"})
    void shouldStillReturnError_whenAdditionalFullRemissionAndOutstandingFeeIsNotZero(String outstandingFee) {
        GeneralApplicationCaseData caseData = additionalCase(new BigDecimal(outstandingFee));

        assertThat(validateRemissionType(caseData).getErrors()).containsExactly(WRONG_REMISSION_TYPE_SELECTED);
    }

    private AboutToStartOrSubmitCallbackResponse validateRemissionType(GeneralApplicationCaseData caseData) {
        CallbackParams params = callbackParamsOf(caseData, MID, REMISSION_TYPE_PAGE);
        return (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
    }

    private static GeneralApplicationCaseData applicationCase(BigDecimal outstandingFee) {
        return new GeneralApplicationCaseData()
            .feePaymentOutcomeDetails(new FeePaymentOutcomeDetails()
                                          .setHwfFullRemissionGrantedForGa(YesOrNo.YES))
            .hwfFeeType(FeeType.APPLICATION)
            .gaHwfDetails(new HelpWithFeesDetails().setOutstandingFee(outstandingFee))
            .build();
    }

    private static GeneralApplicationCaseData additionalCase(BigDecimal outstandingFee) {
        return new GeneralApplicationCaseData()
            .feePaymentOutcomeDetails(new FeePaymentOutcomeDetails()
                                          .setHwfFullRemissionGrantedForAdditionalFee(YesOrNo.YES))
            .hwfFeeType(FeeType.ADDITIONAL)
            .additionalHwfDetails(new HelpWithFeesDetails().setOutstandingFee(outstandingFee))
            .build();
    }
}
