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
import uk.gov.hmcts.reform.civil.model.defaultjudgment.CaseLocationCivil;
import uk.gov.hmcts.reform.civil.referencedata.model.LocationRefData;
import uk.gov.hmcts.reform.civil.repositories.SpecReferenceNumberRepository;
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

    private final SpecReferenceNumberRepository specReferenceNumberRepository;
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
        CaseData.CaseDataBuilder<?, ?> caseDataBuilder = caseData.toBuilder();
        caseDataBuilder.caseAccessCategory(SPEC_CLAIM);
        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDataBuilder.build().toMap(objectMapper))
            .build();
    }

    private CallbackResponse submitClaim(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        CaseData.CaseDataBuilder<?, ?> caseDataBuilder = caseData.toBuilder();
        caseDataBuilder.submittedDate(time.now());
        // Add back Pip in post to temporary pass the email event
        caseDataBuilder.respondent1PinToPostLRspec(defendantPinToPostLRspecService.buildDefendantPinToPost());
        if (Optional.ofNullable(callbackParams.getRequest()).map(CallbackRequest::getEventId).isPresent()) {
            caseDataBuilder.legacyCaseReference(specReferenceNumberRepository.getSpecReferenceNumber());
            caseDataBuilder.businessProcess(BusinessProcess.ready(CREATE_LIP_CLAIM));
            caseDataBuilder.respondent1DetailsForClaimDetailsTab(caseDataBuilder.build().getRespondent1().toBuilder().flags(
                null).build());
            caseFlagsInitialiser.initialiseCaseFlags(CREATE_LIP_CLAIM, caseDataBuilder);
        }
        setUpHelpWithFees(caseDataBuilder);
        addOrginsationPoliciesforClaimantLip(caseDataBuilder);
        caseDataBuilder.caseNameHmctsInternal(buildCaseName(caseData));
        caseDataBuilder.caseNamePublic(buildCaseName(caseData));
        caseDataBuilder
            .allPartyNames(getAllPartyNames(caseData));
        populateWithPartyIds(caseDataBuilder);

        caseDataBuilder.anyRepresented(NO);

        if (caseData.getIsFlightDelayClaim() == YesOrNo.YES) {
            caseDataBuilder.claimType(ClaimType.FLIGHT_DELAY);
        }

        String authToken = callbackParams.getParams().get(BEARER_TOKEN).toString();
        List<LocationRefData> locations = (locationRefDataService
            .getCourtLocationsByEpimmsIdAndCourtType(authToken, epimmsId));

        if (!locations.isEmpty()) {
            LocationRefData locationRefData = locations.get(0);
            caseDataBuilder.caseManagementLocation(CaseLocationCivil.builder()
                                                       .region(locationRefData.getRegionId())
                                                       .baseLocation(locationRefData.getEpimmsId())
                                                       .build());
            caseDataBuilder.locationName(locationRefData.getSiteName());
        }
        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDataBuilder.build().toMap(objectMapper))
            .build();
    }

    private void addOrginsationPoliciesforClaimantLip(CaseData.CaseDataBuilder caseDataBuilder) {
        CaseData caseData = caseDataBuilder.build();
        //         LiP are not represented or registered
        if (caseData.getApplicant1OrganisationPolicy() == null) {
            caseDataBuilder
                .applicant1OrganisationPolicy(OrganisationPolicy.builder()
                                                  .orgPolicyCaseAssignedRole(APPLICANTSOLICITORONE.getFormattedName())
                                                  .build());
        }

        OrgPolicyUtils.addMissingOrgPolicies(caseDataBuilder);
    }

    private void setUpHelpWithFees(CaseData.CaseDataBuilder caseDataBuilder) {
        helpWithFeesForTabService.setUpHelpWithFeeTab(caseDataBuilder);
    }
}
