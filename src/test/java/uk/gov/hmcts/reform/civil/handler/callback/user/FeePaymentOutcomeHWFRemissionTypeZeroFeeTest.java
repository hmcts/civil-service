package uk.gov.hmcts.reform.civil.handler.callback.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CallbackType;
import uk.gov.hmcts.reform.civil.enums.FeeType;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.citizenui.FeePaymentOutcomeDetails;
import uk.gov.hmcts.reform.civil.model.citizenui.HelpWithFeesDetails;
import uk.gov.hmcts.reform.civil.service.citizen.HWFFeePaymentOutcomeService;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.civil.handler.callback.user.FeePaymentOutcomeHWFCallBackHandler.WRONG_REMISSION_TYPE_SELECTED;

@ExtendWith(MockitoExtension.class)
class FeePaymentOutcomeHWFRemissionTypeZeroFeeTest extends BaseCallbackHandlerTest {

    private static final String REMISSION_TYPE_PAGE = "remission-type";

    @Mock
    private HWFFeePaymentOutcomeService hwfService;

    private FeePaymentOutcomeHWFCallBackHandler handler;

    @BeforeEach
    void setUp() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();
        handler = new FeePaymentOutcomeHWFCallBackHandler(objectMapper, hwfService);
    }

    @ParameterizedTest
    @ValueSource(strings = {"0", "0.00"})
    void shouldNotReturnError_whenClaimIssueFullRemissionAndOutstandingFeeIsZero(String outstandingFee) {
        CaseData caseData = claimIssueCase(new BigDecimal(outstandingFee));

        assertThat(validateRemissionType(caseData).getErrors()).isEmpty();
    }

    @ParameterizedTest
    @ValueSource(strings = {"0", "0.00"})
    void shouldNotReturnError_whenHearingFullRemissionAndOutstandingFeeIsZero(String outstandingFee) {
        CaseData caseData = hearingCase(new BigDecimal(outstandingFee));

        assertThat(validateRemissionType(caseData).getErrors()).isEmpty();
    }

    @ParameterizedTest
    @ValueSource(strings = {"0.01", "100.00"})
    void shouldStillReturnError_whenClaimIssueFullRemissionAndOutstandingFeeIsNotZero(String outstandingFee) {
        CaseData caseData = claimIssueCase(new BigDecimal(outstandingFee));

        assertThat(validateRemissionType(caseData).getErrors()).containsExactly(WRONG_REMISSION_TYPE_SELECTED);
    }

    @ParameterizedTest
    @ValueSource(strings = {"0.01", "100.00"})
    void shouldStillReturnError_whenHearingFullRemissionAndOutstandingFeeIsNotZero(String outstandingFee) {
        CaseData caseData = hearingCase(new BigDecimal(outstandingFee));

        assertThat(validateRemissionType(caseData).getErrors()).containsExactly(WRONG_REMISSION_TYPE_SELECTED);
    }

    private AboutToStartOrSubmitCallbackResponse validateRemissionType(CaseData caseData) {
        CallbackParams params = callbackParamsOf(caseData, CallbackType.MID, REMISSION_TYPE_PAGE);
        return (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
    }

    private static CaseData claimIssueCase(BigDecimal outstandingFee) {
        FeePaymentOutcomeDetails feePaymentDetails = new FeePaymentOutcomeDetails();
        feePaymentDetails.setHwfFullRemissionGrantedForClaimIssue(YesOrNo.YES);
        HelpWithFeesDetails hwfDetails = new HelpWithFeesDetails();
        hwfDetails.setOutstandingFeeInPounds(outstandingFee);

        CaseData caseData = CaseData.builder().build();
        caseData.setFeePaymentOutcomeDetails(feePaymentDetails);
        caseData.setHwfFeeType(FeeType.CLAIMISSUED);
        caseData.setClaimIssuedHwfDetails(hwfDetails);
        return caseData;
    }

    private static CaseData hearingCase(BigDecimal outstandingFee) {
        FeePaymentOutcomeDetails feePaymentDetails = new FeePaymentOutcomeDetails();
        feePaymentDetails.setHwfFullRemissionGrantedForHearingFee(YesOrNo.YES);
        HelpWithFeesDetails hwfDetails = new HelpWithFeesDetails();
        hwfDetails.setOutstandingFeeInPounds(outstandingFee);

        CaseData caseData = CaseData.builder().build();
        caseData.setFeePaymentOutcomeDetails(feePaymentDetails);
        caseData.setHwfFeeType(FeeType.HEARING);
        caseData.setHearingHwfDetails(hwfDetails);
        return caseData;
    }
}
