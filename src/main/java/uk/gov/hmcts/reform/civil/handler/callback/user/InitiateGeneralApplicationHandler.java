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
import uk.gov.hmcts.reform.civil.referencedata.model.LocationRefData;
import uk.gov.hmcts.reform.civil.service.CoreCaseUserService;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.GeneralAppFeesService;
import uk.gov.hmcts.reform.civil.service.InitiateGeneralApplicationService;
import uk.gov.hmcts.reform.civil.service.UserService;
import uk.gov.hmcts.reform.civil.service.referencedata.LocationReferenceDataService;
import uk.gov.hmcts.reform.civil.utils.UserRoleCaching;
import uk.gov.hmcts.reform.civil.utils.UserRoleUtils;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static uk.gov.hmcts.reform.civil.callback.CallbackParams.Params.BEARER_TOKEN;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.MID;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.SUBMITTED;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.INITIATE_GENERAL_APPLICATION;
import static uk.gov.hmcts.reform.civil.enums.CaseState.AWAITING_APPLICANT_INTENTION;
import static uk.gov.hmcts.reform.civil.enums.CaseState.AWAITING_CASE_DETAILS_NOTIFICATION;
import static uk.gov.hmcts.reform.civil.enums.CaseState.AWAITING_RESPONDENT_ACKNOWLEDGEMENT;
import static uk.gov.hmcts.reform.civil.enums.CaseState.CASE_DISMISSED;
import static uk.gov.hmcts.reform.civil.enums.CaseState.CASE_ISSUED;
import static uk.gov.hmcts.reform.civil.enums.CaseState.IN_MEDIATION;
import static uk.gov.hmcts.reform.civil.enums.CaseState.PENDING_CASE_ISSUED;
import static uk.gov.hmcts.reform.civil.enums.CaseState.PROCEEDS_IN_HERITAGE_SYSTEM;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.model.common.DynamicList.fromList;
import static uk.gov.hmcts.reform.civil.service.InitiateGeneralApplicationService.INVALID_SETTLE_BY_CONSENT;

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
    private static final List<CaseEvent> EVENTS = Collections.singletonList(INITIATE_GENERAL_APPLICATION);
    private static final String RESP_NOT_ASSIGNED_ERROR = "Application cannot be created until all the required "
            + "respondent solicitor are assigned to the case.";
    private static final String RESP_NOT_ASSIGNED_ERROR_LIP = "Application cannot be created until the Defendant "
        + "is assigned to the case.";
    public static final String NOT_IN_EA_REGION = "Sorry this service is not available in the current case management location, please raise an application manually.";
    private static final String LR_VS_LIP = "Sorry this service is not available, please raise an application manually.";
    private final InitiateGeneralApplicationService initiateGeneralApplicationService;
    private final ObjectMapper objectMapper;
    private final UserService userService;
    private final UserRoleCaching userRoleCaching;
    private final GeneralAppFeesService feesService;
    private final LocationReferenceDataService locationRefDataService;
    private final FeatureToggleService featureToggleService;
    private final CoreCaseUserService coreCaseUserService;
    private static final List<CaseState> stateAfterJudicialReferral = Arrays.asList(PENDING_CASE_ISSUED, CASE_ISSUED,
                                                                         AWAITING_CASE_DETAILS_NOTIFICATION, AWAITING_RESPONDENT_ACKNOWLEDGEMENT, CASE_DISMISSED,
                                                                         AWAITING_APPLICANT_INTENTION, PROCEEDS_IN_HERITAGE_SYSTEM, IN_MEDIATION);

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

        if (!initiateGeneralApplicationService.respondentAssigned(caseData)) {
            errors.add(RESP_NOT_ASSIGNED_ERROR);
        }
        CaseData.CaseDataBuilder<?, ?> caseDataBuilder = caseData.toBuilder();

        if (initiateGeneralApplicationService.caseContainsLiP(caseData)) {
            if (!featureToggleService.isGaForLipsEnabled()) {
                errors.add(LR_VS_LIP);
            } else if (!(featureToggleService.isGaForLipsEnabledAndLocationWhiteListed(caseData
                                                              .getCaseManagementLocation().getBaseLocation()))) {
                errors.add(NOT_IN_EA_REGION);
            } else {
                /*
                 * General Application can only be initiated if Defendant is assigned to the case
                 * */
                if (Objects.isNull(caseData.getDefendantUserDetails())) {
                    errors.add(RESP_NOT_ASSIGNED_ERROR_LIP);
                }
            }
        }
        String authToken = callbackParams.getParams().get(BEARER_TOKEN).toString();
        caseDataBuilder
                .generalAppHearingDetails(
                    GAHearingDetails
                        .builder()
                        .hearingPreferredLocation(getLocationsFromList(locationRefDataService
                                                               .getCourtLocationsForGeneralApplication(authToken)))
                        .build());
        return AboutToStartOrSubmitCallbackResponse.builder()
                .errors(errors)
                .data(caseDataBuilder.build().toMap(objectMapper))
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
        CaseData.CaseDataBuilder<?, ?> caseDataBuilder = caseData.toBuilder();

        List<GeneralApplicationTypes> generalAppTypes = getGeneralApplicationTypes(callbackParams, caseData);

        var consent = Objects.nonNull(caseData.getGeneralAppRespondentAgreement())
                                && YES.equals(caseData.getGeneralAppRespondentAgreement().getHasAgreed());
        List<String> errors = new ArrayList<>();
        if (generalAppTypes.size() == 1
                && generalAppTypes.contains(GeneralApplicationTypes.SETTLE_BY_CONSENT)
                && !consent) {
            errors.add(INVALID_SETTLE_BY_CONSENT);
        }
        return AboutToStartOrSubmitCallbackResponse.builder()
                .data(caseDataBuilder.build().toMap(objectMapper))
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
        if (featureToggleService.isCoSCEnabled()) {
            UserInfo userInfo = userService.getUserInfo(callbackParams.getParams().get(BEARER_TOKEN).toString());
            List<String> roles = coreCaseUserService.getUserCaseRoles(
                callbackParams.getCaseData().getCcdCaseReference().toString(),
                userInfo.getUid()
            );
            return !(UserRoleUtils.isLIPDefendant(roles) || UserRoleUtils.isLIPClaimant(roles));
        } else {
            return false;
        }
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
        CaseData.CaseDataBuilder<?, ?> caseDataBuilder = caseData.toBuilder();
        if (generalAppTypes.size() == 1
            && generalAppTypes.contains(GeneralApplicationTypes.VARY_PAYMENT_TERMS_OF_JUDGMENT)) {
            caseDataBuilder.generalAppVaryJudgementType(YesOrNo.YES)
                    .generalAppInformOtherParty(
                            GAInformOtherParty.builder().isWithNotice(YesOrNo.YES).build());
        } else {
            caseDataBuilder.generalAppVaryJudgementType(YesOrNo.NO);
        }
        String token = callbackParams.getParams().get(BEARER_TOKEN).toString();
        boolean isGAApplicantSameAsParentCaseClaimant = initiateGeneralApplicationService
                .isGAApplicantSameAsParentCaseClaimant(caseData, token);
        caseDataBuilder
            .generalAppParentClaimantIsApplicant(isGAApplicantSameAsParentCaseClaimant ? YES : YesOrNo.NO).build();

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDataBuilder.build().toMap(objectMapper))
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

    private CallbackResponse setFeesAndPBA(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();

        if (caseData.getGeneralAppTypeLR() != null && isCoscEnabledAndUserNotLip(callbackParams)) {
            caseData = caseData.toBuilder().generalAppType(GAApplicationType.builder().types(GATypeHelper.getGATypes(
                caseData.getGeneralAppTypeLR().getTypes())).build()).build();
        }
        caseData = setWithNoticeByType(caseData);
        CaseData.CaseDataBuilder<?, ?> caseDataBuilder = caseData.toBuilder();
        Fee feeForGA = feesService.getFeeForGA(caseData);
        caseDataBuilder.generalAppPBADetails(GAPbaDetails.builder()
                .generalAppFeeToPayInText(POUND_SYMBOL + feeForGA.toPounds().toString())
                .fee(feeForGA)
                .build());

        return AboutToStartOrSubmitCallbackResponse.builder()
                .data(caseDataBuilder.build().toMap(objectMapper))
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
            GAPbaDetails generalAppPBADetails = GAPbaDetails.builder().build();
            CaseData newCaseData = caseData.toBuilder().generalAppPBADetails(generalAppPBADetails).build();
            caseData = newCaseData;
        }
        if (caseData.getGeneralAppPBADetails().getFee() == null) {
            Fee feeForGA = feesService.getFeeForGA(caseData);
            GAPbaDetails generalAppPBADetails = caseData.getGeneralAppPBADetails().toBuilder().fee(feeForGA).build();
            CaseData newCaseData = caseData.toBuilder().generalAppPBADetails(generalAppPBADetails).build();
            caseData = newCaseData;
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
            GAHearingDetails generalAppHearingDetails = caseData.getGeneralAppHearingDetails().toBuilder()
                .hearingPreferredLocation(dynamicLocationList).build();
            CaseData updatedCaseData = caseData.toBuilder()
                .generalAppHearingDetails(generalAppHearingDetails)
                .generalAppParentClaimantIsApplicant(null)
                .build();
            caseData = updatedCaseData;
        } else {
            GAHearingDetails generalAppHearingDetails = caseData.getGeneralAppHearingDetails().toBuilder()
                .hearingPreferredLocation(DynamicList.builder().build()).build();
            CaseData updatedCaseData = caseData.toBuilder()
                .generalAppHearingDetails(generalAppHearingDetails)
                .generalAppParentClaimantIsApplicant(null)
                .build();
            caseData = updatedCaseData;
        }
        if (caseData.getGeneralAppTypeLR() != null && isCoscEnabledAndUserNotLip(callbackParams)) {
            var generalAppTypes = GATypeHelper.getGATypes(caseData.getGeneralAppTypeLR().getTypes());
            CaseData updatedCaseData = caseData.toBuilder()
                .generalAppType(GAApplicationType.builder().types(generalAppTypes).build())
                .build();
            caseData = updatedCaseData;
        }

        Map<String, Object> data = initiateGeneralApplicationService
                .buildCaseData(dataBuilder, caseData, userDetails, callbackParams.getParams().get(BEARER_TOKEN)
                        .toString(), feesService).toMap(objectMapper);
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
            caseData = caseData.toBuilder()
                    .generalAppInformOtherParty(
                            GAInformOtherParty.builder().isWithNotice(YesOrNo.YES).build()).build();
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

    private boolean inStateAfterJudicialReferral(CaseState state) {
        return !stateAfterJudicialReferral.contains(state);
    }

}
