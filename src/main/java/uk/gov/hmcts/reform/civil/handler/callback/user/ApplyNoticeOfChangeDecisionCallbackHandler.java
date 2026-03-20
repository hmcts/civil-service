package uk.gov.hmcts.reform.civil.handler.callback.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.model.Organisation;
import uk.gov.hmcts.reform.civil.callback.Callback;
import uk.gov.hmcts.reform.civil.callback.CallbackHandler;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.cas.client.CaseAssignmentApi;
import uk.gov.hmcts.reform.civil.enums.CaseRole;
import uk.gov.hmcts.reform.civil.model.BusinessProcess;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.ChangeOfRepresentation;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.common.DynamicListElement;
import uk.gov.hmcts.reform.civil.model.noc.ChangeOrganisationRequest;
import uk.gov.hmcts.reform.civil.cas.model.DecisionRequest;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static uk.gov.hmcts.reform.civil.callback.CallbackParams.Params.BEARER_TOKEN;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.SUBMITTED;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.APPLY_NOC_DECISION;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.APPLY_NOC_DECISION_DEFENDANT_LIP;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.APPLY_NOC_DECISION_LIP;

@Service
@RequiredArgsConstructor
public class ApplyNoticeOfChangeDecisionCallbackHandler extends CallbackHandler {

    private static final List<CaseEvent> EVENTS =
        List.of(APPLY_NOC_DECISION, APPLY_NOC_DECISION_LIP);

    private final AuthTokenGenerator authTokenGenerator;
    private final CaseAssignmentApi caseAssignmentApi;
    private final ObjectMapper objectMapper;
    private final FeatureToggleService featureToggleService;

    private static final String CHANGE_ORGANISATION_REQUEST = "changeOrganisationRequestField";
    private static final String ORG_ID_FOR_AUTO_APPROVAL =
        "org id to persist updated change organisation request field";

    @Override
    protected Map<String, Callback> callbacks() {
        return new ImmutableMap.Builder<String, Callback>()
            .put(callbackKey(ABOUT_TO_SUBMIT), this::applyNoticeOfChangeDecision)
            .put(callbackKey(SUBMITTED), this::emptySubmittedCallbackResponse)
            .build();
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    private CallbackResponse applyNoticeOfChangeDecision(CallbackParams callbackParams) {
        CaseDetails caseDetails = callbackParams.getRequest().getCaseDetails();
        // Keep the original CoR payload before NoC decision mutates/nullifies parts of the request.
        CaseData preDecisionData = objectMapper.convertValue(caseDetails.getData(), CaseData.class);
        String authToken = callbackParams.getParams().get(BEARER_TOKEN).toString();
        String selectedCaseRole = callbackParams.getCaseData().getChangeOrganisationRequestField().getCaseRoleId().getValue().getCode();

        updateOrgPoliciesForLiP(callbackParams.getRequest().getCaseDetails());

        AboutToStartOrSubmitCallbackResponse decisionResponse = caseAssignmentApi.applyDecision(
            authToken,
            authTokenGenerator.generate(),
            DecisionRequest.decisionRequest(caseDetails)
        );

        CaseData postDecisionData = objectMapper.convertValue(decisionResponse.getData(), CaseData.class);

        clearAddLegalRepDeadlineForRole(postDecisionData, selectedCaseRole);

        updateChangeOrganisationRequestFieldAfterNoCDecisionApplied(
            postDecisionData,
            preDecisionData.getChangeOrganisationRequestField()
        );

        postDecisionData.setBusinessProcess(BusinessProcess.ready(getBusinessProcessEvent(postDecisionData, selectedCaseRole)));
        postDecisionData.setChangeOfRepresentation(getChangeOfRepresentation(
            callbackParams.getCaseData().getChangeOrganisationRequestField(), postDecisionData));

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(postDecisionData.toMap(objectMapper)).build();
    }

    private ChangeOfRepresentation getChangeOfRepresentation(ChangeOrganisationRequest corFieldBeforeDecision,
                                                             CaseData caseData) {
        ChangeOfRepresentation changeOfRepresentation = new ChangeOfRepresentation()
            .setOrganisationToRemoveID(getChangedOrg(caseData, corFieldBeforeDecision))
            .setOrganisationToAddID(corFieldBeforeDecision.getOrganisationToAdd().getOrganisationID())
            .setCaseRole(corFieldBeforeDecision.getCaseRoleId().getValue().getCode())
            .setTimestamp(corFieldBeforeDecision.getRequestTimestamp())
            .setFormerRepresentationEmailAddress(
                getFormerEmail(corFieldBeforeDecision.getCaseRoleId().getValue().getCode(), caseData));

        if (corFieldBeforeDecision.getOrganisationToRemove() != null) {
            changeOfRepresentation.setOrganisationToRemoveID(
                corFieldBeforeDecision.getOrganisationToRemove().getOrganisationID());
        }
        return changeOfRepresentation;
    }

    /** After applying the NoC decision the ChangeOrganisationRequest field is nullified
     * To auto assign the case to the new user, Assign case access checks for:
     * 1. ChangeOrganisationRequest field in case data, it does this by looking for the OrganisationToAdd node
     * 2. checks if caseroleID field is null
     *
     * <p>If the two checks above return true, then the NoC request is auto approved and new user is auto assigned.
     * However we cannot persist null values to the db since the objectMapper is
     * initialized with setSerializationInclusion(JsonInclude.Include.NON_EMPTY)
     * To get around this, the field is forced to persist by setting a value for the OrganisationToAdd node</p>
     *
     * <p>This value will be deleted in the next callback UpdateCaseDetailsAfterNoCHandler</p>
     *
     * @param caseData caseData
     * @param preDecisionCor preDecisionCor
     */
    private void updateChangeOrganisationRequestFieldAfterNoCDecisionApplied(
        CaseData caseData,
        ChangeOrganisationRequest preDecisionCor) {
        ChangeOrganisationRequest request = new ChangeOrganisationRequest();
        request.setCreatedBy(preDecisionCor.getCreatedBy());
        request.setOrganisationToAdd(new Organisation().setOrganisationID(ORG_ID_FOR_AUTO_APPROVAL));
        caseData.setChangeOrganisationRequestField(request);

    }

    /** The ChangeOrganisationRequest field has a node called OrganisationToRemove.
     * If the litigant is a litigant in person, then the value for this node will be null.
     * However we cannot persist null values to the db since the objectMapper is initialized
     * with setSerializationInclusion(JsonInclude.Include.NON_EMPTY)
     * so it won't be available in the request and cause errors as the field doesn't exist
     * The workaround is to add the field is added manually to the request in case of LiP scenario
     * Additionally, the org id in the orgpolicy field is removed before Notify Claim as part of a
     * previous ticket. If NoC is requested at this point, the ChangeOrganisationRequest will have
     * a null value. To get around this, there is also a check to see if the org ID copy field is
     * not null and use that instead.
     *
     * @param caseDetails caseDetails
     */
    private void updateOrgPoliciesForLiP(CaseDetails caseDetails) {
        ChangeOrganisationRequest changeOrganisationRequestField =
            objectMapper.convertValue(caseDetails.getData().get(CHANGE_ORGANISATION_REQUEST), ChangeOrganisationRequest.class);
        if (changeOrganisationRequestField.getOrganisationToRemove() != null) {
            return;
        }

        ChangeOrganisationRequest updatedRequest = new ChangeOrganisationRequest();
        updatedRequest.setOrganisationToAdd(changeOrganisationRequestField.getOrganisationToAdd());
        updatedRequest.setApprovalStatus(changeOrganisationRequestField.getApprovalStatus());
        updatedRequest.setRequestTimestamp(changeOrganisationRequestField.getRequestTimestamp());
        DynamicList caseRoleId = changeOrganisationRequestField.getCaseRoleId();
        updatedRequest.setCaseRoleId(caseRoleId);
        // Preserve OrganisationToRemove node for persistence even when the value is logically null.
        String orgIdCopyIfExists = getOrgIdCopyIfExists(caseDetails, caseRoleId.getValue().getCode());
        updatedRequest.setOrganisationToRemove(new Organisation().setOrganisationID(orgIdCopyIfExists));
        caseDetails.getData().put(CHANGE_ORGANISATION_REQUEST, updatedRequest);
    }

    private String getOrgIdCopyIfExists(CaseDetails caseDetails, String caseRole) {
        if (isApplicant(caseRole)) {
            return null;
        }

        if (CaseRole.RESPONDENTSOLICITORONE.getFormattedName().equals(caseRole)) {
            return objectMapper.convertValue(caseDetails.getData().get(
                "respondent1OrganisationIDCopy"), String.class);
        }
        if (CaseRole.RESPONDENTSOLICITORTWO.getFormattedName().equals(caseRole)) {
            return objectMapper.convertValue(caseDetails.getData().get(
                "respondent2OrganisationIDCopy"), String.class);
        }
        return null;
    }

    private void clearAddLegalRepDeadlineForRole(CaseData caseData, String caseRole) {
        if (CaseRole.RESPONDENTSOLICITORONE.getFormattedName().equals(caseRole)) {
            caseData.setAddLegalRepDeadlineRes1(null);
        } else if (CaseRole.RESPONDENTSOLICITORTWO.getFormattedName().equals(caseRole)) {
            caseData.setAddLegalRepDeadlineRes2(null);
        }
    }

    private String getFormerEmail(String caseRole, CaseData caseData) {
        if (CaseRole.APPLICANTSOLICITORONE.getFormattedName().equals(caseRole)) {
            return Optional.ofNullable(caseData.getApplicantSolicitor1UserDetails())
                .map(uk.gov.hmcts.reform.civil.model.IdamUserDetails::getEmail)
                .orElse(null);
        }
        if (CaseRole.RESPONDENTSOLICITORONE.getFormattedName().equals(caseRole)) {
            return caseData.getRespondentSolicitor1EmailAddress();
        }
        if (CaseRole.RESPONDENTSOLICITORTWO.getFormattedName().equals(caseRole)) {
            return caseData.getRespondentSolicitor2EmailAddress();
        }
        return null;
    }

    private boolean isApplicant(String caseRole) {
        return CaseRole.APPLICANTSOLICITORONE.getFormattedName().equals(caseRole);
    }

    private CaseEvent getBusinessProcessEvent(CaseData postDecisionCaseData, String caseRole) {
        if (isApplicant(caseRole)
            && postDecisionCaseData.isApplicantLiP()) {
            return APPLY_NOC_DECISION_LIP;
        } else if (CaseRole.RESPONDENTSOLICITORONE.getFormattedName().equals(caseRole)
            && postDecisionCaseData.isRespondent1LiP()
            && featureToggleService.isLipVLipEnabled()) {
            return APPLY_NOC_DECISION_DEFENDANT_LIP;
        }
        return APPLY_NOC_DECISION;
    }

    /**
     * Checks if the Change organisation request object has a null org to remove, then gets the organisation from
     * caseData organisation copy, else returns the existing object in Change organisation request.
     * @param caseData Case Data
     * @param request Change Organisation Request
     * @return string of org to remove id
     */
    public String getChangedOrg(CaseData caseData, ChangeOrganisationRequest request) {
        if (request.getOrganisationToRemove() != null) {
            return request.getOrganisationToRemove().getOrganisationID();
        }

        String caseRole = Optional.ofNullable(request.getCaseRoleId())
            .map(DynamicList::getValue)
            .map(DynamicListElement::getCode)
            .orElse(null);

        if (CaseRole.RESPONDENTSOLICITORONE.getFormattedName().equals(caseRole)) {
            return caseData.getRespondent1OrganisationIDCopy();
        } else if (CaseRole.RESPONDENTSOLICITORTWO.getFormattedName().equals(caseRole)) {
            return caseData.getRespondent2OrganisationIDCopy();
        }

        return null;
    }
}
