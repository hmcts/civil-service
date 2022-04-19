package uk.gov.hmcts.reform.civil.handler.callback.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.Callback;
import uk.gov.hmcts.reform.civil.callback.CallbackHandler;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Fee;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.genapplication.GAHearingDetails;
import uk.gov.hmcts.reform.civil.model.genapplication.GAPbaDetails;
import uk.gov.hmcts.reform.civil.model.genapplication.GAUrgencyRequirement;
import uk.gov.hmcts.reform.civil.service.InitiateGeneralApplicationService;
import uk.gov.hmcts.reform.civil.service.OrganisationService;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;
import uk.gov.hmcts.reform.prd.model.Organisation;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static java.util.Collections.emptyList;
import static uk.gov.hmcts.reform.civil.callback.CallbackParams.Params.BEARER_TOKEN;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.MID;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.SUBMITTED;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.INITIATE_GENERAL_APPLICATION;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;

@Service
@RequiredArgsConstructor
public class InitiateGeneralApplicationHandler extends CallbackHandler {

    private static final String VALIDATE_URGENCY_DATE_PAGE = "ga-validate-urgency-date";
    private static final String VALIDATE_HEARING_PAGE = "ga-hearing-screen-validation";
    private static final String SET_FEES_FOR_APPLICATION = "ga-set-application-fees";
    private static final List<CaseEvent> EVENTS = Collections.singletonList(INITIATE_GENERAL_APPLICATION);
    private static final BigDecimal PENCE_PER_POUND = BigDecimal.valueOf(100);

    private final InitiateGeneralApplicationService initiateGeneralApplicationService;
    private final ObjectMapper objectMapper;
    private final OrganisationService organisationService;
    private final IdamClient idamClient;

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(
            callbackKey(ABOUT_TO_START), this::getPbaAccounts,
            callbackKey(MID, VALIDATE_URGENCY_DATE_PAGE), this::gaValidateUrgencyDate,
            callbackKey(MID, VALIDATE_HEARING_PAGE), this::gaValidateHearingScreen,
            callbackKey(MID, SET_FEES_FOR_APPLICATION), this::setApplicationFees,
            callbackKey(ABOUT_TO_SUBMIT), this::submitClaim,
            callbackKey(SUBMITTED), this::emptySubmittedCallbackResponse
        );
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    private CallbackResponse getPbaAccounts(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        CaseData.CaseDataBuilder caseDataBuilder = caseData.toBuilder();
        List<String> pbaNumbers = getPbaAccounts(callbackParams.getParams().get(BEARER_TOKEN).toString());

        caseDataBuilder.generalAppPBADetails(GAPbaDetails.builder()
                                                 .applicantsPbaAccounts(DynamicList.fromList(pbaNumbers)).build());
        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDataBuilder.build().toMap(objectMapper))
            .build();
    }

    private List<String> getPbaAccounts(String authToken) {
        return organisationService.findOrganisation(authToken)
            .map(Organisation::getPaymentAccount)
            .orElse(emptyList());
    }

    private CallbackResponse gaValidateUrgencyDate(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        GAUrgencyRequirement generalAppUrgencyRequirement = caseData.getGeneralAppUrgencyRequirement();
        List<String> errors = generalAppUrgencyRequirement != null
            ? initiateGeneralApplicationService.validateUrgencyDates(generalAppUrgencyRequirement)
            : Collections.emptyList();

        return AboutToStartOrSubmitCallbackResponse.builder()
            .errors(errors)
            .build();
    }

    private CallbackResponse gaValidateHearingScreen(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        GAHearingDetails hearingDetails = caseData.getGeneralAppHearingDetails();
        List<String> errors = hearingDetails != null
            ? initiateGeneralApplicationService.validateHearingScreen(hearingDetails)
            : Collections.emptyList();

        return AboutToStartOrSubmitCallbackResponse.builder()
            .errors(errors)
            .build();
    }

    private CallbackResponse setApplicationFees(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        CaseData.CaseDataBuilder caseDataBuilder = caseData.toBuilder();
        GAPbaDetails pbaDetails = caseData.getGeneralAppPBADetails();
        Fee applicationFees = Fee.builder().code("FEE0210").build();
        boolean isNotified = caseData.getGeneralAppRespondentAgreement() != null
            && NO.equals(caseData.getGeneralAppRespondentAgreement().getHasAgreed())
            && caseData.getGeneralAppInformOtherParty() != null
            && YES.equals(caseData.getGeneralAppInformOtherParty().getIsWithNotice());

        if (isNotified) {
            applicationFees.setCalculatedAmountInPence(getFeeInPence(275));
        } else {
            applicationFees.setCalculatedAmountInPence(getFeeInPence(108));
        }

        caseDataBuilder.generalAppPBADetails(pbaDetails.toBuilder().fee(applicationFees).build());

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDataBuilder.build().toMap(objectMapper))
            .errors(Collections.emptyList())
            .build();
    }

    private BigDecimal getFeeInPence(int fee) {
        return BigDecimal.valueOf(fee).multiply(PENCE_PER_POUND)
            .setScale(0, RoundingMode.UNNECESSARY);
    }

    private CaseData.CaseDataBuilder getSharedData(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        // second idam call is workaround for null pointer when hiding field in getIdamEmail callback
        return caseData.toBuilder();
    }

    private CallbackResponse submitClaim(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        UserDetails userDetails = idamClient.getUserDetails(callbackParams.getParams().get(BEARER_TOKEN).toString());

        // second idam call is workaround for null pointer when hiding field in getIdamEmail callback
        CaseData.CaseDataBuilder dataBuilder = getSharedData(callbackParams);

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(initiateGeneralApplicationService
                      .buildCaseData(dataBuilder, caseData, userDetails, callbackParams.getParams().get(BEARER_TOKEN)
                          .toString()).toMap(objectMapper)).build();
    }

    /**
     * Returns empty submitted callback response. Used by events that set business process to ready, but doesn't have
     * any submitted callback logic (making callback is still required to trigger EventEmitterAspect)
     *
     * @param callbackParams This parameter is required as this is passed as reference for execute method in CallBack
     * @return empty submitted callback response
     */
    protected CallbackResponse emptySubmittedCallbackResponse(CallbackParams callbackParams) {
        return SubmittedCallbackResponse.builder().build();
    }
}
