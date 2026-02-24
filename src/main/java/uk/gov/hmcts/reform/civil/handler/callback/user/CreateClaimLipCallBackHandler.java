package uk.gov.hmcts.reform.civil.handler.callback.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.ccd.model.OrganisationPolicy;
import uk.gov.hmcts.reform.civil.callback.Callback;
import uk.gov.hmcts.reform.civil.callback.CallbackHandler;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.enums.ClaimType;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.BusinessProcess;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.CaseLocationCivil;
import uk.gov.hmcts.reform.civil.model.welshenhancements.PreferredLanguage;
import uk.gov.hmcts.reform.civil.referencedata.model.LocationRefData;
import uk.gov.hmcts.reform.civil.repositories.CasemanReferenceNumberRepository;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.Time;
import uk.gov.hmcts.reform.civil.service.citizenui.HelpWithFeesForTabService;
import uk.gov.hmcts.reform.civil.service.pininpost.DefendantPinToPostLRspecService;
import uk.gov.hmcts.reform.civil.service.referencedata.LocationReferenceDataService;
import uk.gov.hmcts.reform.civil.utils.CaseFlagsInitialiser;
import uk.gov.hmcts.reform.civil.utils.OrgPolicyUtils;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static uk.gov.hmcts.reform.civil.callback.CallbackParams.Params.BEARER_TOKEN;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.SUBMITTED;
import static uk.gov.hmcts.reform.civil.callback.CallbackVersion.V_1;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CREATE_LIP_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.CaseCategory.SPEC_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.CaseRole.APPLICANTSOLICITORONE;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.utils.CaseNameUtils.buildCaseName;
import static uk.gov.hmcts.reform.civil.utils.PartyUtils.getAllPartyNames;
import static uk.gov.hmcts.reform.civil.utils.PartyUtils.populateWithPartyIds;

@Slf4j
@Service
@RequiredArgsConstructor
public class CreateClaimLipCallBackHandler extends CallbackHandler {

    private final CasemanReferenceNumberRepository casemanReferenceNumberRepository;
    private final Time time;
    private final ObjectMapper objectMapper;
    private final DefendantPinToPostLRspecService defendantPinToPostLRspecService;
    private final CaseFlagsInitialiser caseFlagsInitialiser;
    private final HelpWithFeesForTabService helpWithFeesForTabService;
    private final FeatureToggleService featureToggleService;
    private final LocationReferenceDataService locationRefDataService;

    @Value("${court-location.specified-claim.epimms-id}")
    private String epimmsId;

    @Override
    protected Map<String, Callback> callbacks() {
        return new ImmutableMap.Builder<String, Callback>()
            .put(callbackKey(ABOUT_TO_START), this::lipClaimInitialState)
            .put(callbackKey(V_1, ABOUT_TO_START), this::lipClaimInitialState)
            .put(callbackKey(ABOUT_TO_SUBMIT), this::submitClaim)
            .put(callbackKey(V_1, ABOUT_TO_SUBMIT), this::submitClaim)
            .put(callbackKey(SUBMITTED), this::emptySubmittedCallbackResponse)
            .build();
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return Collections.singletonList(
            CaseEvent.CREATE_LIP_CLAIM
        );
    }

    private CallbackResponse lipClaimInitialState(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        caseData.setCaseAccessCategory(SPEC_CLAIM);
        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseData.toMap(objectMapper))
            .build();
    }

    private CallbackResponse submitClaim(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        caseData.setSubmittedDate(time.now());
        // Add back Pip in post to temporary pass the email event
        caseData.setRespondent1PinToPostLRspec(defendantPinToPostLRspecService.buildDefendantPinToPost());
        if (Optional.ofNullable(callbackParams.getRequest()).map(CallbackRequest::getEventId).isPresent()) {
            caseData.setLegacyCaseReference(casemanReferenceNumberRepository.next("spec"));
            caseData.setBusinessProcess(BusinessProcess.ready(CREATE_LIP_CLAIM));
            Party respondent1Clone = objectMapper.convertValue(caseData.getRespondent1(), Party.class);
            respondent1Clone.setFlags(null);
            caseData.setRespondent1DetailsForClaimDetailsTab(respondent1Clone);

            caseFlagsInitialiser.initialiseCaseFlags(CREATE_LIP_CLAIM, caseData);
            populateWithPartyIds(caseData);
            OrgPolicyUtils.addMissingOrgPolicies(caseData);
        }
        setUpHelpWithFees(caseData);
        addOrginsationPoliciesforClaimantLip(caseData);
        caseData.setCaseNameHmctsInternal(buildCaseName(caseData));
        caseData.setCaseNamePublic(buildCaseName(caseData));
        caseData.setAllPartyNames(getAllPartyNames(caseData));

        caseData.setAnyRepresented(NO);

        if (caseData.getIsFlightDelayClaim() == YesOrNo.YES) {
            caseData.setClaimType(ClaimType.FLIGHT_DELAY);
        }

        String authToken = callbackParams.getParams().get(BEARER_TOKEN).toString();
        List<LocationRefData> locations = (locationRefDataService
            .getCourtLocationsByEpimmsIdAndCourtType(authToken, epimmsId));

        if (!locations.isEmpty()) {
            LocationRefData locationRefData = locations.get(0);
            CaseLocationCivil caseLocationCivil = new CaseLocationCivil();
            caseLocationCivil.setRegion(locationRefData.getRegionId());
            caseLocationCivil.setBaseLocation(locationRefData.getEpimmsId());
            caseData.setCaseManagementLocation(caseLocationCivil);
            caseData.setLocationName(locationRefData.getSiteName());
        }
        if (featureToggleService.isWelshEnabledForMainCase()) {
            caseData.setClaimantLanguagePreferenceDisplay(PreferredLanguage.fromString(caseData.getClaimantBilingualLanguagePreference()));
        }
        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseData.toMap(objectMapper))
            .build();
    }

    private void addOrginsationPoliciesforClaimantLip(CaseData caseData) {
        //         LiP are not represented or registered
        if (caseData.getApplicant1OrganisationPolicy() == null) {
            caseData.setApplicant1OrganisationPolicy(new OrganisationPolicy().setOrgPolicyCaseAssignedRole(APPLICANTSOLICITORONE.getFormattedName()));
        }

        OrgPolicyUtils.addMissingOrgPolicies(caseData);
    }

    private void setUpHelpWithFees(CaseData caseData) {
        helpWithFeesForTabService.setUpHelpWithFeeTab(caseData);
    }
}
