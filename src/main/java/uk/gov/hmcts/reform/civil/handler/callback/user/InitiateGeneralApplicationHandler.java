package uk.gov.hmcts.reform.civil.handler.callback.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.Callback;
import uk.gov.hmcts.reform.civil.callback.CallbackHandler;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.enums.dq.GeneralApplicationTypes;
import uk.gov.hmcts.reform.civil.enums.dq.GeneralApplicationTypesLR;
import uk.gov.hmcts.reform.civil.helpers.GATypeHelper;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Fee;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.common.DynamicListElement;
import uk.gov.hmcts.reform.civil.model.genapplication.GAApplicationType;
import uk.gov.hmcts.reform.civil.model.genapplication.GAHearingDetails;
import uk.gov.hmcts.reform.civil.model.genapplication.GAInformOtherParty;
import uk.gov.hmcts.reform.civil.model.genapplication.GAPbaDetails;
import uk.gov.hmcts.reform.civil.model.genapplication.GAUrgencyRequirement;
import uk.gov.hmcts.reform.civil.model.referencedata.LocationRefData;
import uk.gov.hmcts.reform.civil.service.CoreCaseUserService;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.GeneralAppFeesService;
import uk.gov.hmcts.reform.civil.service.InitiateGeneralApplicationService;
import uk.gov.hmcts.reform.civil.service.UserService;
import uk.gov.hmcts.reform.civil.service.referencedata.LocationReferenceDataService;
import uk.gov.hmcts.reform.civil.service.validation.GeneralApplicationValidator;
import uk.gov.hmcts.reform.civil.utils.UserRoleUtils;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import static uk.gov.hmcts.reform.civil.callback.CallbackParams.Params.BEARER_TOKEN;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.MID;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.SUBMITTED;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.INITIATE_GENERAL_APPLICATION;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.INITIATE_GENERAL_APPLICATION_COSC;
import static uk.gov.hmcts.reform.civil.enums.CaseState.CASE_DISCONTINUED;
import static uk.gov.hmcts.reform.civil.enums.CaseState.CASE_SETTLED;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.model.common.DynamicList.fromList;
import static uk.gov.hmcts.reform.civil.service.InitiateGeneralApplicationServiceConstants.INVALID_SETTLE_BY_CONSENT;

@Service
@RequiredArgsConstructor
@Slf4j
public class InitiateGeneralApplicationHandler extends CallbackHandler {

    private static final String VALIDATE_URGENCY_DATE_PAGE = "ga-validate-urgency-date";
    private static final String VALIDATE_GA_CONSENT = "ga-validate-consent";
    private static final String VALIDATE_GA_TYPE = "ga-validate-type";
    private static final String VALIDATE_HEARING_DATE = "ga-validate-hearing-date";
    private static final String VALIDATE_HEARING_PAGE = "ga-hearing-screen-validation";
    private static final String INVALID_HEARING_DATE = "The hearing date must be in the future";
    private static final String SET_FEES_AND_PBA = "ga-fees-and-pba";
    private static final String POUND_SYMBOL = "£";
    private static final List<CaseEvent> EVENTS = List.of(INITIATE_GENERAL_APPLICATION, INITIATE_GENERAL_APPLICATION_COSC);
    public static final Set<CaseState> settleDiscontinueStates = EnumSet.of(CASE_SETTLED,
                                                                            CASE_DISCONTINUED);
    private static final String RESP_NOT_ASSIGNED_ERROR = "Application cannot be created until all the required "
            + "respondent solicitor are assigned to the case.";
    private static final String RESP_NOT_ASSIGNED_ERROR_LIP = "Application cannot be created until the Defendant "
        + "is assigned to the case.";
    public static final String NOT_IN_EA_REGION = "Sorry this service is not available in the current case management location, please raise an application manually.";
    public static final String NOT_ALLOWED_SETTLE_DISCONTINUE = "Sorry this service is not available, please raise an application manually.";
    private static final String LR_VS_LIP = "Sorry this service is not available, please raise an application manually.";
    private final InitiateGeneralApplicationService initiateGeneralApplicationService;
    private final GeneralApplicationValidator generalApplicationValidator;
    private final ObjectMapper objectMapper;
    private final UserService userService;
    private final GeneralAppFeesService feesService;
    private final LocationReferenceDataService locationRefDataService;
    private final FeatureToggleService featureToggleService;
    private final CoreCaseUserService coreCaseUserService;

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(
            callbackKey(ABOUT_TO_START), this::aboutToStartValidationAndSetup,
            callbackKey(MID, VALIDATE_GA_TYPE), this::gaValidateType,
            callbackKey(MID, VALIDATE_HEARING_DATE), this::gaValidateHearingDate,
            callbackKey(MID, VALIDATE_GA_CONSENT), this::gaValidateConsent,
            callbackKey(MID, VALIDATE_URGENCY_DATE_PAGE), this::gaValidateUrgencyDate,
            callbackKey(MID, VALIDATE_HEARING_PAGE), this::gaValidateHearingScreen,
            callbackKey(MID, SET_FEES_AND_PBA), this::setFeesAndPBA,
            callbackKey(ABOUT_TO_SUBMIT), this::submitApplication,
            callbackKey(SUBMITTED), this::emptySubmittedCallbackResponse
        );
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    private CallbackResponse aboutToStartValidationAndSetup(CallbackParams callbackParams) {
        List<String> errors = new ArrayList<>();
        CaseData caseData = callbackParams.getCaseData();
        log.info("initiating general application callback for caseId {}", caseData.getCcdCaseReference());
        if (settleDiscontinueStates.contains(caseData.getCcdState())
            && caseData.getPreviousCCDState() == null) {
            return AboutToStartOrSubmitCallbackResponse.builder()
                .errors(List.of(NOT_ALLOWED_SETTLE_DISCONTINUE))
                .build();
        }

        if (!initiateGeneralApplicationService.respondentAssigned(caseData)) {
            log.info("initiating general application not allowed for caseId {}", caseData.getCcdCaseReference());
            errors.add(RESP_NOT_ASSIGNED_ERROR);
        }
        log.info("initiating general application allowed for caseId {}", caseData.getCcdCaseReference());
        CaseData.CaseDataBuilder<?, ?> caseDataBuilder = caseData.toBuilder();
        CaseEvent caseEvent = CaseEvent.valueOf(callbackParams.getRequest().getEventId());

        if (initiateGeneralApplicationService.caseContainsLiP(caseData)) {
            if ((caseData.isRespondentResponseBilingual() && !featureToggleService.isGaForWelshEnabled() && !caseData.isLipvLROneVOne()
                && !(caseEvent == INITIATE_GENERAL_APPLICATION_COSC))) {
                errors.add(LR_VS_LIP);
            } else if (featureToggleService.isDefendantNoCOnlineForCase(caseData) && caseData.isLipvLROneVOne()
                && caseData.isClaimantBilingual() && !featureToggleService.isGaForWelshEnabled()) {
                errors.add(LR_VS_LIP);
            } else if (
                !(featureToggleService.isLocationWhiteListed(caseData.getCaseManagementLocation()
                                                                                    .getBaseLocation()))
                    && !(featureToggleService.isCuiGaNroEnabled())) {
                errors.add(NOT_IN_EA_REGION);
            }
        }
        String authToken = callbackParams.getParams().get(BEARER_TOKEN).toString();
        GAHearingDetails generalAppHearingDetails = new GAHearingDetails();
        generalAppHearingDetails.setHearingPreferredLocation(getLocationsFromList(locationRefDataService
            .getCourtLocationsForGeneralApplication(authToken)));
        caseData.setGeneralAppHearingDetails(generalAppHearingDetails);
        return AboutToStartOrSubmitCallbackResponse.builder()
                .errors(errors)
                .data(caseData.toMap(objectMapper))
                .build();
    }

    private DynamicList getLocationsFromList(final List<LocationRefData> locations) {
        return fromList(locations.stream().map(location -> new StringBuilder().append(location.getSiteName())
                .append(" - ").append(location.getCourtAddress())
                .append(" - ").append(location.getPostcode()).toString())
                            .toList());
    }

    private CallbackResponse gaValidateConsent(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();

        List<GeneralApplicationTypes> generalAppTypes = getGeneralApplicationTypes(callbackParams, caseData);

        var consent = Objects.nonNull(caseData.getGeneralAppRespondentAgreement())
                                && YES.equals(caseData.getGeneralAppRespondentAgreement().getHasAgreed());
        List<String> errors = new ArrayList<>();
        if (generalAppTypes.size() == 1
                && generalAppTypes.contains(GeneralApplicationTypes.SETTLE_BY_CONSENT)
                && !consent) {
            errors.add(INVALID_SETTLE_BY_CONSENT.getValue());
        }
        return AboutToStartOrSubmitCallbackResponse.builder()
                .data(caseData.toMap(objectMapper))
                .errors(errors)
                .build();
    }

    private List<GeneralApplicationTypes> getGeneralApplicationTypes(CallbackParams callbackParams, CaseData caseData) {
        List<GeneralApplicationTypes> generalAppTypes;
        if (caseData.getGeneralAppTypeLR() != null && isCoscEnabledAndUserNotLip(callbackParams)) {
            generalAppTypes = GATypeHelper.getGATypes(caseData.getGeneralAppTypeLR().getTypes());
        } else {
            generalAppTypes = caseData.getGeneralAppType().getTypes();
        }
        return generalAppTypes;
    }

    private boolean isCoscEnabledAndUserNotLip(CallbackParams callbackParams) {
        UserInfo userInfo = userService.getUserInfo(callbackParams.getParams().get(BEARER_TOKEN).toString());
        List<String> roles = coreCaseUserService.getUserCaseRoles(
                callbackParams.getCaseData().getCcdCaseReference().toString(),
                userInfo.getUid()
        );
        return !(UserRoleUtils.isLIPDefendant(roles) || UserRoleUtils.isLIPClaimant(roles));
    }

    private CallbackResponse gaValidateType(CallbackParams callbackParams) {

        CaseData caseData = callbackParams.getCaseData();
        List<String> errors = new ArrayList<>();

        List<GeneralApplicationTypes> generalAppTypes = getGeneralApplicationTypes(callbackParams, caseData);

        if (generalAppTypes.size() > 1
            && generalAppTypes.contains(GeneralApplicationTypes.VARY_PAYMENT_TERMS_OF_JUDGMENT)) {
            errors.add("It is not possible to select an additional application type when applying to vary payment terms of judgment");
        }
        if (generalAppTypes.size() > 1
                && generalAppTypes.contains(GeneralApplicationTypes.SETTLE_BY_CONSENT)) {
            errors.add("It is not possible to select an additional application type " +
                    "when applying to Settle by consent");
        }
        if (generalAppTypes.size() == 1
            && generalAppTypes.contains(GeneralApplicationTypes.VARY_PAYMENT_TERMS_OF_JUDGMENT)) {
            caseData.setGeneralAppVaryJudgementType(YesOrNo.YES);
            GAInformOtherParty generalAppInformOtherParty = new GAInformOtherParty();
            generalAppInformOtherParty.setIsWithNotice(YesOrNo.YES);
            caseData.setGeneralAppInformOtherParty(generalAppInformOtherParty);
        } else {
            caseData.setGeneralAppVaryJudgementType(YesOrNo.NO);
        }
        String token = callbackParams.getParams().get(BEARER_TOKEN).toString();
        boolean isGAApplicantSameAsParentCaseClaimant = initiateGeneralApplicationService
                .isGAApplicantSameAsParentCaseClaimant(caseData, token);
        caseData.setGeneralAppParentClaimantIsApplicant(isGAApplicantSameAsParentCaseClaimant ? YES : YesOrNo.NO);

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseData.toMap(objectMapper))
            .errors(errors)
            .build();
    }

    private CallbackResponse gaValidateHearingDate(CallbackParams callbackParams) {
        List<String> errors = new ArrayList<>();

        CaseData caseData = callbackParams.getCaseData();
        if (caseData.getGeneralAppHearingDate() != null
            && caseData.getGeneralAppHearingDate().getHearingScheduledPreferenceYesNo().equals(YesOrNo.YES)
            && caseData.getGeneralAppHearingDate().getHearingScheduledDate().isBefore(LocalDate.now())) {
            errors.add(INVALID_HEARING_DATE);
        }

        return AboutToStartOrSubmitCallbackResponse.builder()
            .errors(errors)
            .build();
    }

    private CallbackResponse gaValidateUrgencyDate(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        GAUrgencyRequirement generalAppUrgencyRequirement = caseData.getGeneralAppUrgencyRequirement();
        List<String> errors = generalAppUrgencyRequirement != null
            ? generalApplicationValidator.validateUrgencyDates(generalAppUrgencyRequirement)
            : Collections.emptyList();

        return AboutToStartOrSubmitCallbackResponse.builder()
            .errors(errors)
            .build();
    }

    private CallbackResponse gaValidateHearingScreen(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        GAHearingDetails hearingDetails = caseData.getGeneralAppHearingDetails();
        List<String> errors = hearingDetails != null
            ? generalApplicationValidator.validateHearingScreen(hearingDetails)
            : Collections.emptyList();

        return AboutToStartOrSubmitCallbackResponse.builder()
            .errors(errors)
            .build();
    }

    private CallbackResponse setFeesAndPBA(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();

        if (caseData.getGeneralAppTypeLR() != null && isCoscEnabledAndUserNotLip(callbackParams)) {
            caseData.setGeneralAppType(GAApplicationType.builder().types(GATypeHelper.getGATypes(
                caseData.getGeneralAppTypeLR().getTypes())).build());
        }
        caseData = setWithNoticeByType(caseData);
        Fee feeForGA = feesService.getFeeForGA(caseData);
        GAPbaDetails generalAppPBADetails = new GAPbaDetails();
        generalAppPBADetails.setGeneralAppFeeToPayInText(POUND_SYMBOL + feeForGA.toPounds().toString());
        generalAppPBADetails.setFee(feeForGA);
        caseData.setGeneralAppPBADetails(generalAppPBADetails);

        return AboutToStartOrSubmitCallbackResponse.builder()
                .data(caseData.toMap(objectMapper))
                .build();
    }

    private CaseData.CaseDataBuilder<?, ?> getSharedData(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        // second idam call is workaround for null pointer when hiding field in getIdamEmail callback
        return caseData.toBuilder();
    }

    private CallbackResponse submitApplication(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        caseData = setWithNoticeByType(caseData);
        final UserDetails userDetails = userService.getUserDetails(callbackParams.getParams().get(BEARER_TOKEN).toString());

        // second idam call is workaround for null pointer when hiding field in getIdamEmail callback
        final CaseData.CaseDataBuilder<?, ?> dataBuilder = getSharedData(callbackParams);

        if (caseData.getGeneralAppPBADetails() == null) {
            GAPbaDetails generalAppPBADetails = new GAPbaDetails();
            caseData.setGeneralAppPBADetails(generalAppPBADetails);
        }
        if (caseData.getGeneralAppPBADetails().getFee() == null) {
            Fee feeForGA = feesService.getFeeForGA(caseData);
            GAPbaDetails generalAppPBADetails = caseData.getGeneralAppPBADetails();
            generalAppPBADetails.setFee(feeForGA);
        }

        if (Objects.nonNull(caseData.getGeneralAppHearingDetails().getHearingPreferredLocation())
            && Objects.nonNull(caseData.getGeneralAppHearingDetails().getHearingPreferredLocation().getValue())) {
            List<String> applicationLocationList = List.of(caseData.getGeneralAppHearingDetails()
                                                               .getHearingPreferredLocation()
                                                               .getValue().getLabel());
            DynamicList dynamicLocationList = fromList(applicationLocationList);
            Optional<DynamicListElement> first = dynamicLocationList.getListItems().stream()
                .filter(l -> l.getLabel().equals(applicationLocationList.get(0))).findFirst();
            first.ifPresent(dynamicLocationList::setValue);
            GAHearingDetails generalAppHearingDetails = caseData.getGeneralAppHearingDetails();
            generalAppHearingDetails.setHearingPreferredLocation(dynamicLocationList);
            caseData.setGeneralAppParentClaimantIsApplicant(null);
        } else {
            GAHearingDetails generalAppHearingDetails = caseData.getGeneralAppHearingDetails();
            DynamicList dynamicList = new DynamicList();
            generalAppHearingDetails.setHearingPreferredLocation(dynamicList);
            caseData.setGeneralAppParentClaimantIsApplicant(null);
        }
        if (caseData.getGeneralAppTypeLR() != null && isCoscEnabledAndUserNotLip(callbackParams)) {
            var generalAppTypes = GATypeHelper.getGATypes(caseData.getGeneralAppTypeLR().getTypes());
            caseData.setGeneralAppType(GAApplicationType.builder().types(generalAppTypes).build());
        }

        Map<String, Object> data = initiateGeneralApplicationService
                .buildCaseData(caseData, userDetails, callbackParams.getParams().get(BEARER_TOKEN)
                        .toString()).toMap(objectMapper);
        return AboutToStartOrSubmitCallbackResponse.builder()
                .data(data).build();
    }

    /**
     * Returns empty submitted callback response. Used by events that set business process to ready, but doesn't have
     * any submitted callback logic (making callback is still required to trigger EventEmitterAspect)
     *
     * @param callbackParams This parameter is required as this is passed as reference for execute method in CallBack
     * @return empty submitted callback response
     */
    @Override
    protected CallbackResponse emptySubmittedCallbackResponse(CallbackParams callbackParams) {
        return SubmittedCallbackResponse.builder().build();
    }

    private CaseData setWithNoticeByType(CaseData caseData) {
        if (isSingleAppTypeVaryJudgment(caseData)) {
            GAInformOtherParty generalAppInformOtherParty = new GAInformOtherParty();
            generalAppInformOtherParty.setIsWithNotice(YesOrNo.YES);
            caseData.setGeneralAppInformOtherParty(generalAppInformOtherParty);
            return caseData;
        }
        return caseData;
    }

    private boolean isSingleAppTypeVaryJudgment(CaseData caseData) {
        if (Objects.nonNull(caseData.getGeneralAppType())) {
            return caseData.getGeneralAppType().getTypes().size() == 1
                && caseData.getGeneralAppType().getTypes().contains(GeneralApplicationTypes.VARY_PAYMENT_TERMS_OF_JUDGMENT);
        }
        return Objects.nonNull(caseData.getGeneralAppTypeLR())
            && caseData.getGeneralAppTypeLR().getTypes().size() == 1
            && caseData.getGeneralAppTypeLR().getTypes().contains(GeneralApplicationTypesLR.VARY_PAYMENT_TERMS_OF_JUDGMENT);
    }
}
