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
import uk.gov.hmcts.reform.civil.client.CaseAssignmentApi;
import uk.gov.hmcts.reform.civil.model.BusinessProcess;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.ChangeOfRepresentation;
import uk.gov.hmcts.reform.civil.model.noc.ChangeOrganisationRequest;
import uk.gov.hmcts.reform.civil.model.noc.DecisionRequest;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.civil.callback.CallbackParams.Params.BEARER_TOKEN;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.SUBMITTED;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.APPLY_NOC_DECISION;

@Service
@RequiredArgsConstructor
public class ApplyNoticeOfChangeDecisionCallbackHandler extends CallbackHandler {

    private static final List<CaseEvent> EVENTS = Collections.singletonList(APPLY_NOC_DECISION);

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
        String authToken = callbackParams.getParams().get(BEARER_TOKEN).toString();
        ChangeOrganisationRequest corFieldBeforeNoC = callbackParams.getCaseData().getChangeOrganisationRequestField();

        updateOrgPoliciesForLiP(callbackParams.getRequest().getCaseDetails());

        AboutToStartOrSubmitCallbackResponse applyDecision = caseAssignmentApi.applyDecision(
            authToken,
            authTokenGenerator.generate(),
            DecisionRequest.decisionRequest(caseDetails)
        );

        CaseData updatedCaseData = objectMapper.convertValue(applyDecision.getData(), CaseData.class);
        CaseData.CaseDataBuilder<?, ?> updatedCaseDataBuilder = updatedCaseData.toBuilder();

        updateChangeOrganisationRequestFieldAfterNoCDecisionApplied(updatedCaseData, updatedCaseDataBuilder);

        updatedCaseDataBuilder.businessProcess(BusinessProcess.ready(APPLY_NOC_DECISION))
            .changeOfRepresentation(getChangeOfRepresentation(corFieldBeforeNoC));

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(updatedCaseDataBuilder.build().toMap(objectMapper)).build();
    }

    private ChangeOfRepresentation getChangeOfRepresentation(ChangeOrganisationRequest corFieldBeforeNoC) {
        ChangeOfRepresentation.ChangeOfRepresentationBuilder builder = ChangeOfRepresentation.builder()
            .organisationToAddID(corFieldBeforeNoC.getOrganisationToAdd().getOrganisationID())
            .caseRole(corFieldBeforeNoC.getCaseRoleId().getValue().getCode())
            .timestamp(corFieldBeforeNoC.getRequestTimestamp());

        if (corFieldBeforeNoC.getOrganisationToRemove() != null) {
            builder.organisationToRemoveID(corFieldBeforeNoC.getOrganisationToRemove().getOrganisationID());
        }
        return builder.build();
    }

    /** After applying the NoC decision the ChangeOrganisationRequest field is nullified
     * To auto assigned the case to the new user, Assign case access checks for:
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
     * @param updatedCaseData updatedCaseData
     * @param updatedcaseDataBuilder updatedcaseDataBuilder
     */
    private void updateChangeOrganisationRequestFieldAfterNoCDecisionApplied(
        CaseData updatedCaseData,
        CaseData.CaseDataBuilder<?, ?> updatedcaseDataBuilder) {
        ChangeOrganisationRequest updatedcor = updatedCaseData.getChangeOrganisationRequestField();
        if (updatedcor == null) {
            updatedcaseDataBuilder
                .changeOrganisationRequestField(ChangeOrganisationRequest.builder()
                                                   .organisationToAdd(Organisation.builder()
                                                                          .organisationID(
                                                                              ORG_ID_FOR_AUTO_APPROVAL).build())
                                                                      .build());
        }
    }

    /** The ChangeOrganisationRequest field has a node called OrganisationToRemove.
     * If the litigant is a litigant in person, then the value for this node will be null.
     * However we cannot persist null values to the db since the objectMapper is initialized
     * with setSerializationInclusion(JsonInclude.Include.NON_EMPTY)
     * so it won't be available in the request and cause errors as the field doesn't exist
     * The workaround is to add the field is added manually to the request in case of LiP scenario
     *
     * @param caseDetails caseDetails
     */
    private void updateOrgPoliciesForLiP(CaseDetails caseDetails) {
        ChangeOrganisationRequest changeOrganisationRequestField = objectMapper.convertValue(
            caseDetails.getData().get(CHANGE_ORGANISATION_REQUEST), ChangeOrganisationRequest.class);

        Organisation organisationToRemove = changeOrganisationRequestField.getOrganisationToRemove();

        if (organisationToRemove == null) {
            ChangeOrganisationRequest build = ChangeOrganisationRequest.builder()
                .organisationToAdd(changeOrganisationRequestField.getOrganisationToAdd())
                .organisationToRemove(Organisation.builder()
                                          .organisationID(null)
                                          .build())
                .approvalStatus(changeOrganisationRequestField.getApprovalStatus())
                .caseRoleId(changeOrganisationRequestField.getCaseRoleId())
                .requestTimestamp(changeOrganisationRequestField.getRequestTimestamp())
                .build();
            caseDetails.getData().put(CHANGE_ORGANISATION_REQUEST, build);
        }
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }
}
