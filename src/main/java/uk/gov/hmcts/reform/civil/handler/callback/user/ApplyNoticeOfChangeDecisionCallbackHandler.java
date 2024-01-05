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
import uk.gov.hmcts.reform.civil.model.noc.ChangeOrganisationRequest;
import uk.gov.hmcts.reform.civil.cas.model.DecisionRequest;

import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.civil.callback.CallbackParams.Params.BEARER_TOKEN;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.SUBMITTED;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.APPLY_NOC_DECISION;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.APPLY_NOC_DECISION_LIP;

@Service
@RequiredArgsConstructor
public class ApplyNoticeOfChangeDecisionCallbackHandler extends CallbackHandler {

    private static final List<CaseEvent> EVENTS =
        List.of(APPLY_NOC_DECISION, APPLY_NOC_DECISION_LIP);

    private final AuthTokenGenerator authTokenGenerator;
    private final CaseAssignmentApi caseAssignmentApi;
    private final ObjectMapper objectMapper;

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

    private CallbackResponse applyNoticeOfChangeDecision(CallbackParams callbackParams) {
        CaseDetails caseDetails = callbackParams.getRequest().getCaseDetails();
        CaseData preDecisionCaseData = objectMapper.convertValue(caseDetails.getData(), CaseData.class);
        String authToken = callbackParams.getParams().get(BEARER_TOKEN).toString();
        String caseRole = callbackParams.getCaseData().getChangeOrganisationRequestField().getCaseRoleId().getValue().getCode();

        updateOrgPoliciesForLiP(callbackParams.getRequest().getCaseDetails());

        AboutToStartOrSubmitCallbackResponse applyDecision = caseAssignmentApi.applyDecision(
            authToken,
            authTokenGenerator.generate(),
            DecisionRequest.decisionRequest(caseDetails)
        );

        CaseData postDecisionCaseData = objectMapper.convertValue(applyDecision.getData(), CaseData.class);
        CaseData.CaseDataBuilder<?, ?> updatedCaseDataBuilder = postDecisionCaseData.toBuilder();

        setAddLegalRepDeadlinesToNull(updatedCaseDataBuilder, caseRole);

        updateChangeOrganisationRequestFieldAfterNoCDecisionApplied(
            updatedCaseDataBuilder,
            preDecisionCaseData.getChangeOrganisationRequestField()
        );

        updatedCaseDataBuilder
            .businessProcess(BusinessProcess.ready(getBussinessProcessEvent(postDecisionCaseData)))
            .changeOfRepresentation(getChangeOfRepresentation(
                    callbackParams.getCaseData().getChangeOrganisationRequestField(), postDecisionCaseData));

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(updatedCaseDataBuilder.build().toMap(objectMapper)).build();
    }

    private ChangeOfRepresentation getChangeOfRepresentation(ChangeOrganisationRequest corFieldBeforeNoC,
                                                             CaseData caseData) {
        ChangeOfRepresentation.ChangeOfRepresentationBuilder builder = ChangeOfRepresentation.builder()
            .organisationToRemoveID(getChangedOrg(caseData, corFieldBeforeNoC))
            .organisationToAddID(corFieldBeforeNoC.getOrganisationToAdd().getOrganisationID())
            .caseRole(corFieldBeforeNoC.getCaseRoleId().getValue().getCode())
            .timestamp(corFieldBeforeNoC.getRequestTimestamp())
            .formerRepresentationEmailAddress(
                getFormerEmail(corFieldBeforeNoC.getCaseRoleId().getValue().getCode(), caseData));

        if (corFieldBeforeNoC.getOrganisationToRemove() != null) {
            builder.organisationToRemoveID(corFieldBeforeNoC.getOrganisationToRemove().getOrganisationID());
        }
        return builder.build();
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
     * @param updatedCaseDataBuilder updatedcaseDataBuilder
     * @param changeOrganisationRequest preDecisionCor
     */
    private void updateChangeOrganisationRequestFieldAfterNoCDecisionApplied(
        CaseData.CaseDataBuilder<?, ?> updatedCaseDataBuilder,
        ChangeOrganisationRequest preDecisionCor) {
        updatedCaseDataBuilder
                .changeOrganisationRequestField(
                    ChangeOrganisationRequest.builder()
                        .createdBy(preDecisionCor.getCreatedBy())
                        .organisationToAdd(
                            Organisation.builder().organisationID(ORG_ID_FOR_AUTO_APPROVAL).build()).build());

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
        ChangeOrganisationRequest changeOrganisationRequestField = objectMapper.convertValue(
            caseDetails.getData().get(CHANGE_ORGANISATION_REQUEST), ChangeOrganisationRequest.class);

        Organisation organisationToRemove = changeOrganisationRequestField.getOrganisationToRemove();
        DynamicList caseRoleId = changeOrganisationRequestField.getCaseRoleId();
        String caseRole = caseRoleId.getValue().getCode();

        String orgIdCopyIfExists = getOrgIdCopyIfExists(caseDetails, caseRole);

        if (organisationToRemove == null) {
            ChangeOrganisationRequest.ChangeOrganisationRequestBuilder changeOrganisationRequestBuilder
                = ChangeOrganisationRequest.builder()
                .organisationToAdd(changeOrganisationRequestField.getOrganisationToAdd())
                .approvalStatus(changeOrganisationRequestField.getApprovalStatus())
                .caseRoleId(caseRoleId)
                .requestTimestamp(changeOrganisationRequestField.getRequestTimestamp());
            if (orgIdCopyIfExists == null) {
                changeOrganisationRequestBuilder
                    .organisationToRemove(Organisation.builder()
                                              .organisationID(null)
                                              .build());
                caseDetails.getData().put(CHANGE_ORGANISATION_REQUEST, changeOrganisationRequestBuilder.build());
            } else {
                changeOrganisationRequestBuilder
                    .organisationToRemove(Organisation.builder()
                                              .organisationID(orgIdCopyIfExists)
                                              .build());
                caseDetails.getData().put(CHANGE_ORGANISATION_REQUEST, changeOrganisationRequestBuilder.build());
            }
        }
    }

    private String getOrgIdCopyIfExists(CaseDetails caseDetails, String caseRole) {
        if (!isApplicant(caseRole)) {
            if (caseRole.equals(CaseRole.RESPONDENTSOLICITORONE.getFormattedName())) {
                String respondent1OrganisationIDCopy = objectMapper.convertValue(caseDetails.getData().get(
                    "respondent1OrganisationIDCopy"), String.class);
                if (respondent1OrganisationIDCopy != null) {
                    return respondent1OrganisationIDCopy;
                }
            } else if (caseRole.equals(CaseRole.RESPONDENTSOLICITORTWO.getFormattedName())) {
                String respondent2OrganisationIDCopy = objectMapper.convertValue(caseDetails.getData().get(
                    "respondent2OrganisationIDCopy"), String.class);
                if (respondent2OrganisationIDCopy != null) {
                    return respondent2OrganisationIDCopy;
                }
            }
        }
        return null;
    }

    private void setAddLegalRepDeadlinesToNull(CaseData.CaseDataBuilder<?, ?> updatedCaseDataBuilder, String caseRole) {
        if (CaseRole.RESPONDENTSOLICITORONE.getFormattedName().equals(caseRole)) {
            updatedCaseDataBuilder.addLegalRepDeadlineRes1(null);
        } else if (CaseRole.RESPONDENTSOLICITORTWO.getFormattedName().equals(caseRole)) {
            updatedCaseDataBuilder.addLegalRepDeadlineRes2(null);
        }
    }

    private String getFormerEmail(String caseRole, CaseData caseData) {
        if (caseRole.equals(CaseRole.APPLICANTSOLICITORONE.getFormattedName())) {
            return caseData.getApplicantSolicitor1UserDetails().getEmail();
        } else if (caseRole.equals(CaseRole.RESPONDENTSOLICITORONE.getFormattedName())) {
            return caseData.getRespondentSolicitor1EmailAddress();
        } else if (caseRole.equals(CaseRole.RESPONDENTSOLICITORTWO.getFormattedName())) {
            return caseData.getRespondentSolicitor2EmailAddress();
        }
        return null;
    }

    private boolean isApplicant(String caseRole) {
        return caseRole.equals(CaseRole.APPLICANTSOLICITORONE.getFormattedName());
    }

    private CaseEvent getBussinessProcessEvent(CaseData postDecisionCaseData) {
        if (postDecisionCaseData.isApplicantLiP()) {
            return APPLY_NOC_DECISION_LIP;
        }
        return APPLY_NOC_DECISION;
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    /**
     * Checks if the Change organisation request object has a null org to remove, then gets the organisation from
     * caseData organisation copy, else returns the existing object in Change organisation request.
     * @param caseData Case Data
     * @param request Change Organisation Request
     * @return string of org to remove id
     */
    public String getChangedOrg(CaseData caseData, ChangeOrganisationRequest request) {
        String caseRole = request.getCaseRoleId().getValue().getCode();
        if (request.getOrganisationToRemove() == null) {
            if (!isApplicant(caseRole)) {
                if (caseRole.equals(CaseRole.RESPONDENTSOLICITORONE.getFormattedName())) {
                    String respondent1OrganisationIDCopy = caseData.getRespondent1OrganisationIDCopy();
                    if (respondent1OrganisationIDCopy != null) {
                        return respondent1OrganisationIDCopy;
                    }
                } else if (caseRole.equals(CaseRole.RESPONDENTSOLICITORTWO.getFormattedName())) {
                    String respondent2OrganisationIDCopy = caseData.getRespondent2OrganisationIDCopy();
                    if (respondent2OrganisationIDCopy != null) {
                        return respondent2OrganisationIDCopy;
                    }
                }
            }
        } else {
            return request.getOrganisationToRemove().getOrganisationID();
        }
        return null;
    }
}
