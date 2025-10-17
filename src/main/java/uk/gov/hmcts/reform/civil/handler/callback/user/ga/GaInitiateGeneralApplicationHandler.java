package uk.gov.hmcts.reform.civil.handler.callback.user.ga;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
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
import uk.gov.hmcts.reform.civil.callback.GeneralApplicationCallbackHandler;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.enums.dq.GeneralApplicationTypes;
import uk.gov.hmcts.reform.civil.enums.dq.GeneralApplicationTypesLR;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
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
import uk.gov.hmcts.reform.civil.service.UserService;
import uk.gov.hmcts.reform.civil.service.ga.GaInitiateGeneralApplicationService;
import uk.gov.hmcts.reform.civil.service.referencedata.LocationReferenceDataService;
import uk.gov.hmcts.reform.civil.service.validation.GeneralApplicationValidator;
import uk.gov.hmcts.reform.civil.utils.UserRoleUtils;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import static uk.gov.hmcts.reform.civil.callback.CallbackParams.Params.BEARER_TOKEN;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.MID;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.SUBMITTED;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.INITIATE_GENERAL_APPLICATION;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.INITIATE_GENERAL_APPLICATION_COSC;
import static uk.gov.hmcts.reform.civil.enums.CaseState.CASE_DISCONTINUED;
import static uk.gov.hmcts.reform.civil.enums.CaseState.CASE_SETTLED;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.model.common.DynamicList.fromList;
import static uk.gov.hmcts.reform.civil.service.InitiateGeneralApplicationServiceConstants.INVALID_SETTLE_BY_CONSENT;

@Service
@RequiredArgsConstructor
@Slf4j
public class GaInitiateGeneralApplicationHandler extends CallbackHandler implements GeneralApplicationCallbackHandler {

    private static final String VALIDATE_URGENCY_DATE_PAGE = "ga-validate-urgency-date";
    private static final String VALIDATE_GA_CONSENT = "ga-validate-consent";
    private static final String VALIDATE_GA_TYPE = "ga-validate-type";
    private static final String VALIDATE_HEARING_DATE = "ga-validate-hearing-date";
    private static final String VALIDATE_HEARING_PAGE = "ga-hearing-screen-validation";
    private static final String SET_FEES_AND_PBA = "ga-fees-and-pba";
    private static final String INVALID_HEARING_DATE = "The hearing date must be in the future";
    private static final String POUND_SYMBOL = "£";
    private static final String LR_VS_LIP = "Sorry this service is not available, please raise an application manually.";
    public static final String NOT_IN_EA_REGION = "Sorry this service is not available in the current case management location, please raise an application manually.";
    public static final String NOT_ALLOWED_SETTLE_DISCONTINUE = "Sorry this service is not available, please raise an application manually.";
    private static final List<CaseEvent> EVENTS = List.of(
        INITIATE_GENERAL_APPLICATION,
        INITIATE_GENERAL_APPLICATION_COSC
    );
    private static final Set<CaseState> SETTLE_DISCONTINUE_STATES = EnumSet.of(CASE_SETTLED, CASE_DISCONTINUED);
    private static final String RESP_NOT_ASSIGNED_ERROR = "Application cannot be created until all the required "
        + "respondent solicitor are assigned to the case.";
    private static final String RESP_NOT_ASSIGNED_ERROR_LIP = "Application cannot be created until the Defendant "
        + "is assigned to the case.";

    private final GaInitiateGeneralApplicationService gaInitiateGeneralApplicationService;
    private final GeneralApplicationValidator generalApplicationValidator;
    private final ObjectMapper objectMapper;
    private final UserService userService;
    private final GeneralAppFeesService feesService;
    private final LocationReferenceDataService locationRefDataService;
    private final FeatureToggleService featureToggleService;
    private final CoreCaseUserService coreCaseUserService;
    private final GaInitiateGeneralApplicationSubmittedHandler submittedHandler;

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(
            callbackKey(ABOUT_TO_START), this::aboutToStart,
            callbackKey(MID, VALIDATE_GA_TYPE), this::gaValidateType,
            callbackKey(MID, VALIDATE_HEARING_DATE), this::gaValidateHearingDate,
            callbackKey(MID, VALIDATE_GA_CONSENT), this::gaValidateConsent,
            callbackKey(MID, VALIDATE_URGENCY_DATE_PAGE), this::gaValidateUrgencyDate,
            callbackKey(MID, VALIDATE_HEARING_PAGE), this::gaValidateHearingScreen,
            callbackKey(MID, SET_FEES_AND_PBA), this::setFeesAndPba,
            callbackKey(ABOUT_TO_SUBMIT), this::submitApplication,
            callbackKey(SUBMITTED), this::submitted
        );
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    @Override
    public void register(Map<String, CallbackHandler> handlers) {
        // GA handlers are registered via the marker-aware CallbackHandlerFactory
    }

    private CallbackResponse aboutToStart(CallbackParams callbackParams) {
        GeneralApplicationCaseData gaCaseData = callbackParams.getGaCaseData();
        if (gaCaseData == null) {
            return AboutToStartOrSubmitCallbackResponse.builder()
                .errors(List.of("Missing GA case data"))
                .build();
        }

        CaseData caseDataView = gaInitiateGeneralApplicationService.asCaseData(gaCaseData);
        log.info("initiating general application callback for caseId {}", caseDataView.getCcdCaseReference());
        if (SETTLE_DISCONTINUE_STATES.contains(caseDataView.getCcdState())
            && caseDataView.getPreviousCCDState() == null) {
            return AboutToStartOrSubmitCallbackResponse.builder()
                .errors(List.of(NOT_ALLOWED_SETTLE_DISCONTINUE))
                .build();
        }

        List<String> errors = new ArrayList<>();

        if (!gaInitiateGeneralApplicationService.respondentAssigned(gaCaseData)) {
            log.info("initiating general application not allowed for caseId {}", caseDataView.getCcdCaseReference());
            if (gaInitiateGeneralApplicationService.caseContainsLip(gaCaseData)) {
                errors.add(RESP_NOT_ASSIGNED_ERROR_LIP);
            } else {
                errors.add(RESP_NOT_ASSIGNED_ERROR);
            }
        }

        CaseEvent caseEvent = callbackParams.getRequest() != null && callbackParams.getRequest().getEventId() != null
            ? CaseEvent.valueOf(callbackParams.getRequest().getEventId())
            : INITIATE_GENERAL_APPLICATION;

        if (gaInitiateGeneralApplicationService.caseContainsLip(gaCaseData)) {
            if (!featureToggleService.isGaForLipsEnabled()
                || (caseDataView.isRespondentResponseBilingual() && !featureToggleService.isGaForWelshEnabled()
                && !caseDataView.isLipvLROneVOne()
                && caseEvent != INITIATE_GENERAL_APPLICATION_COSC)) {
                errors.add(LR_VS_LIP);
            } else if (featureToggleService.isDefendantNoCOnlineForCase(caseDataView) && caseDataView.isLipvLROneVOne()
                && caseDataView.isClaimantBilingual() && !featureToggleService.isGaForWelshEnabled()) {
                errors.add(LR_VS_LIP);
            } else if (
                !(featureToggleService.isGaForLipsEnabledAndLocationWhiteListed(
                    caseDataView.getCaseManagementLocation().getBaseLocation()))
                    && !(featureToggleService.isCuiGaNroEnabled())) {
                errors.add(NOT_IN_EA_REGION);
            }
        }

        String authToken = callbackParams.getParams().get(BEARER_TOKEN).toString();
        GAHearingDetails hearingDetails = GAHearingDetails.builder()
            .hearingPreferredLocation(
                getLocationsFromList(locationRefDataService.getCourtLocationsForGeneralApplication(authToken)))
            .build();

        GeneralApplicationCaseData updatedGa = gaCaseData.toBuilder()
            .generalAppHearingDetails(hearingDetails)
            .build();
        updatedGa = gaInitiateGeneralApplicationService.ensureDefaults(updatedGa);

        return AboutToStartOrSubmitCallbackResponse.builder()
            .errors(errors)
            .data(updatedGa.toMap(objectMapper))
            .build();
    }

    private CallbackResponse gaValidateConsent(CallbackParams callbackParams) {
        GeneralApplicationCaseData gaCaseData = callbackParams.getGaCaseData();
        CaseData caseDataView = gaInitiateGeneralApplicationService.asCaseData(gaCaseData);

        List<GeneralApplicationTypes> generalAppTypes = getGeneralApplicationTypes(callbackParams, caseDataView, gaCaseData);

        boolean consent = Objects.nonNull(caseDataView.getGeneralAppRespondentAgreement())
            && YES.equals(caseDataView.getGeneralAppRespondentAgreement().getHasAgreed());
        List<String> errors = new ArrayList<>();
        if (generalAppTypes.size() == 1
            && generalAppTypes.contains(GeneralApplicationTypes.SETTLE_BY_CONSENT)
            && !consent) {
            errors.add(INVALID_SETTLE_BY_CONSENT.getValue());
        }

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(gaCaseData.toMap(objectMapper))
            .errors(errors)
            .build();
    }

    private List<GeneralApplicationTypes> getGeneralApplicationTypes(CallbackParams callbackParams,
                                                                     CaseData caseDataView,
                                                                     GeneralApplicationCaseData gaCaseData) {
        if (gaCaseData.getGeneralAppTypeLR() != null && isCoscEnabledAndUserNotLip(callbackParams, caseDataView)) {
            return GATypeHelper.getGATypes(gaCaseData.getGeneralAppTypeLR().getTypes());
        }
        return gaCaseData.getGeneralAppType().getTypes();
    }

    private boolean isCoscEnabledAndUserNotLip(CallbackParams callbackParams, CaseData caseDataView) {
        UserInfo userInfo = userService.getUserInfo(callbackParams.getParams().get(BEARER_TOKEN).toString());
        List<String> roles = coreCaseUserService.getUserCaseRoles(
            caseDataView.getCcdCaseReference().toString(),
            userInfo.getUid()
        );
        return !(UserRoleUtils.isLIPDefendant(roles) || UserRoleUtils.isLIPClaimant(roles));
    }

    private CallbackResponse gaValidateType(CallbackParams callbackParams) {
        GeneralApplicationCaseData gaCaseData = callbackParams.getGaCaseData();
        CaseData caseDataView = gaInitiateGeneralApplicationService.asCaseData(gaCaseData);
        List<String> errors = new ArrayList<>();

        List<GeneralApplicationTypes> generalAppTypes = getGeneralApplicationTypes(callbackParams, caseDataView, gaCaseData);

        GeneralApplicationCaseData.GeneralApplicationCaseDataBuilder builder = gaCaseData.toBuilder();
        if (generalAppTypes.size() > 1
            && generalAppTypes.contains(GeneralApplicationTypes.VARY_PAYMENT_TERMS_OF_JUDGMENT)) {
            errors.add("It is not possible to select an additional application type when applying to vary payment terms of judgment");
        }
        if (generalAppTypes.size() > 1
            && generalAppTypes.contains(GeneralApplicationTypes.SETTLE_BY_CONSENT)) {
            errors.add("It is not possible to select an additional application type "
                + "when applying to Settle by consent");
        }

        if (generalAppTypes.size() == 1
            && generalAppTypes.contains(GeneralApplicationTypes.VARY_PAYMENT_TERMS_OF_JUDGMENT)) {
            builder.generalAppVaryJudgementType(YesOrNo.YES)
                .generalAppInformOtherParty(GAInformOtherParty.builder().isWithNotice(YesOrNo.YES).build());
        } else {
            builder.generalAppVaryJudgementType(NO);
        }

        String token = callbackParams.getParams().get(BEARER_TOKEN).toString();
        boolean isGAApplicantSameAsParentCaseClaimant = gaInitiateGeneralApplicationService
            .isGaApplicantSameAsParentCaseClaimant(gaCaseData, token);
        builder.parentClaimantIsApplicant(isGAApplicantSameAsParentCaseClaimant ? YES : NO);

        GeneralApplicationCaseData updated = builder.build();

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(updated.toMap(objectMapper))
            .errors(errors)
            .build();
    }

    private CallbackResponse gaValidateHearingDate(CallbackParams callbackParams) {
        GeneralApplicationCaseData gaCaseData = callbackParams.getGaCaseData();
        List<String> errors = new ArrayList<>();

        if (gaCaseData.getGeneralAppHearingDate() != null
            && gaCaseData.getGeneralAppHearingDate().getHearingScheduledPreferenceYesNo().equals(YesOrNo.YES)
            && gaCaseData.getGeneralAppHearingDate().getHearingScheduledDate().isBefore(LocalDate.now())) {
            errors.add(INVALID_HEARING_DATE);
        }

        return AboutToStartOrSubmitCallbackResponse.builder()
            .errors(errors)
            .build();
    }

    private CallbackResponse gaValidateUrgencyDate(CallbackParams callbackParams) {
        GeneralApplicationCaseData gaCaseData = callbackParams.getGaCaseData();
        GAUrgencyRequirement generalAppUrgencyRequirement = gaCaseData.getGeneralAppUrgencyRequirement();
        List<String> errors = generalAppUrgencyRequirement != null
            ? generalApplicationValidator.validateUrgencyDates(generalAppUrgencyRequirement)
            : List.of();

        return AboutToStartOrSubmitCallbackResponse.builder()
            .errors(errors)
            .build();
    }

    private CallbackResponse gaValidateHearingScreen(CallbackParams callbackParams) {
        GeneralApplicationCaseData gaCaseData = callbackParams.getGaCaseData();
        GAHearingDetails hearingDetails = gaCaseData.getGeneralAppHearingDetails();
        List<String> errors = hearingDetails != null
            ? generalApplicationValidator.validateHearingScreen(hearingDetails)
            : List.of();

        return AboutToStartOrSubmitCallbackResponse.builder()
            .errors(errors)
            .build();
    }

    private CallbackResponse setFeesAndPba(CallbackParams callbackParams) {
        GeneralApplicationCaseData gaCaseData = callbackParams.getGaCaseData();
        CaseData caseDataView = gaInitiateGeneralApplicationService.asCaseData(gaCaseData);

        GeneralApplicationCaseData updatedGa = gaCaseData;
        if (gaCaseData.getGeneralAppTypeLR() != null && isCoscEnabledAndUserNotLip(callbackParams, caseDataView)) {
            var generalAppTypes = GATypeHelper.getGATypes(gaCaseData.getGeneralAppTypeLR().getTypes());
            updatedGa = gaCaseData.toBuilder()
                .generalAppType(GAApplicationType.builder().types(generalAppTypes).build())
                .build();
            caseDataView = gaInitiateGeneralApplicationService.asCaseData(updatedGa);
        }

        updatedGa = setWithNoticeByType(updatedGa);
        caseDataView = gaInitiateGeneralApplicationService.asCaseData(updatedGa);

        Fee feeForGA = feesService.getFeeForGA(updatedGa);
        GeneralApplicationCaseData finalGa = updatedGa.toBuilder()
            .generalAppPBADetails(GAPbaDetails.builder()
                .generalAppFeeToPayInText(POUND_SYMBOL + feeForGA.toPounds())
                .fee(feeForGA)
                .build())
            .build();
        finalGa = gaInitiateGeneralApplicationService.ensureDefaults(finalGa);

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(finalGa.toMap(objectMapper))
            .build();
    }

    private GeneralApplicationCaseData setWithNoticeByType(GeneralApplicationCaseData gaCaseData) {
        if (isSingleAppTypeVaryJudgment(gaCaseData)) {
            return gaCaseData.toBuilder()
                .generalAppInformOtherParty(GAInformOtherParty.builder().isWithNotice(YesOrNo.YES).build())
                .build();
        }
        return gaCaseData;
    }

    private boolean isSingleAppTypeVaryJudgment(GeneralApplicationCaseData gaCaseData) {
        if (Objects.nonNull(gaCaseData.getGeneralAppType())) {
            return gaCaseData.getGeneralAppType().getTypes().size() == 1
                && gaCaseData.getGeneralAppType().getTypes().contains(GeneralApplicationTypes.VARY_PAYMENT_TERMS_OF_JUDGMENT);
        }
        return Objects.nonNull(gaCaseData.getGeneralAppTypeLR())
            && gaCaseData.getGeneralAppTypeLR().getTypes().size() == 1
            && gaCaseData.getGeneralAppTypeLR().getTypes().contains(GeneralApplicationTypesLR.VARY_PAYMENT_TERMS_OF_JUDGMENT);
    }

    private CallbackResponse submitApplication(CallbackParams callbackParams) {
        GeneralApplicationCaseData gaCaseData = callbackParams.getGaCaseData();
        GeneralApplicationCaseData updatedGa = gaCaseData;

        if (updatedGa.getGeneralAppPBADetails() == null) {
            updatedGa = updatedGa.toBuilder()
                .generalAppPBADetails(GAPbaDetails.builder().build())
                .build();
        }

        if (updatedGa.getGeneralAppPBADetails().getFee() == null) {
            Fee feeForGA = feesService.getFeeForGA(updatedGa);
            GAPbaDetails details = updatedGa.getGeneralAppPBADetails().toBuilder()
                .fee(feeForGA)
                .build();
            updatedGa = updatedGa.toBuilder()
                .generalAppPBADetails(details)
                .build();
        }

        updatedGa = normalisePreferredLocation(updatedGa);

        if (updatedGa.getGeneralAppTypeLR() != null
            && isCoscEnabledAndUserNotLip(callbackParams, gaInitiateGeneralApplicationService.asCaseData(updatedGa))) {
            var generalAppTypes = GATypeHelper.getGATypes(updatedGa.getGeneralAppTypeLR().getTypes());
            updatedGa = updatedGa.toBuilder()
                .generalAppType(GAApplicationType.builder().types(generalAppTypes).build())
                .build();
        }

        updatedGa = setWithNoticeByType(updatedGa);

        updatedGa = gaInitiateGeneralApplicationService.ensureDefaults(updatedGa);
        String bearerToken = callbackParams.getParams().get(BEARER_TOKEN).toString();
        UserDetails userDetails = userService.getUserDetails(bearerToken);
        GeneralApplicationCaseData builtGa = gaInitiateGeneralApplicationService.buildCaseData(updatedGa, userDetails, bearerToken);

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(builtGa.toMap(objectMapper))
            .build();
    }

    private GeneralApplicationCaseData normalisePreferredLocation(GeneralApplicationCaseData gaCaseData) {
        if (gaCaseData.getGeneralAppHearingDetails() == null) {
            return gaCaseData;
        }

        GAHearingDetails details = gaCaseData.getGeneralAppHearingDetails();
        if (details.getHearingPreferredLocation() != null
            && details.getHearingPreferredLocation().getValue() != null) {
            List<String> applicationLocationList = List.of(
                details.getHearingPreferredLocation().getValue().getLabel());
            DynamicList dynamicLocationList = fromList(applicationLocationList);
            Optional<DynamicListElement> first = dynamicLocationList.getListItems().stream()
                .filter(l -> l.getLabel().equals(applicationLocationList.get(0))).findFirst();
            first.ifPresent(dynamicLocationList::setValue);
            details = details.toBuilder()
                .hearingPreferredLocation(dynamicLocationList)
                .build();
        } else {
            details = details.toBuilder()
                .hearingPreferredLocation(DynamicList.builder().build())
                .build();
        }

        return gaCaseData.toBuilder()
            .generalAppHearingDetails(details)
            .parentClaimantIsApplicant(null)
            .build();
    }

    private SubmittedCallbackResponse submitted(CallbackParams callbackParams) {
        return (SubmittedCallbackResponse) submittedHandler.handle(callbackParams);
    }

    private DynamicList getLocationsFromList(final List<LocationRefData> locations) {
        return fromList(locations.stream()
            .map(location -> new StringBuilder()
                .append(location.getSiteName())
                .append(" - ")
                .append(location.getCourtAddress())
                .append(" - ")
                .append(location.getPostcode())
                .toString())
            .toList());
    }
}
