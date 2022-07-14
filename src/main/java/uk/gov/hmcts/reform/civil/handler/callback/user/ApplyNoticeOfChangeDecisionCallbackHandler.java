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
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.BusinessProcess;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.ChangeOfRepresentation;
import uk.gov.hmcts.reform.civil.model.noc.ChangeOrganisationRequest;
import uk.gov.hmcts.reform.civil.model.noc.DecisionRequest;
import uk.gov.hmcts.reform.civil.service.UserService;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

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
    private final IdamClient idamClient;
    private final UserService userService;

    private static final String CHANGE_ORGANISATION_REQUEST = "changeOrganisationRequestField";

    private static final String ORD_ID_FOR_AUTO_APPROVAL = "org id to persist updated change organisation request field";

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

        UserDetails userDetails = idamClient.getUserDetails(authToken);
        System.out.println("apply_noc_decision email " + userDetails.getEmail());

        UserInfo userInfo = userService.getUserInfo(authToken);
        System.out.println("apply_noc_decision ID " + userInfo.getUid());
        System.out.println("apply_noc_decision name " + userInfo.getName() + userInfo.getGivenName() + userInfo.getFamilyName());

        updateOrgPoliciesForLiP(callbackParams.getRequest().getCaseDetails());

        AboutToStartOrSubmitCallbackResponse applyDecision = caseAssignmentApi.applyDecision(
            authToken,
            authTokenGenerator.generate(),
            DecisionRequest.decisionRequest(caseDetails)
        );

        CaseData updatedCaseData = objectMapper.convertValue(applyDecision.getData(), CaseData.class);
        CaseData.CaseDataBuilder<?, ?> updatedcaseDataBuilder = updatedCaseData.toBuilder();

        updateChangeOrganisationRequestFieldAfterNoCDecisionApplied(updatedCaseData, updatedcaseDataBuilder);

        updatedcaseDataBuilder.businessProcess(BusinessProcess.ready(APPLY_NOC_DECISION))
            .changeOfRepresentation(getChangeOfRepresentation(corFieldBeforeNoC));

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(updatedcaseDataBuilder.build().toMap(objectMapper)).build();
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

    /**todo java doc
     *
     * @param updatedCaseData
     * @param updatedcaseDataBuilder
     */
    private void updateChangeOrganisationRequestFieldAfterNoCDecisionApplied(CaseData updatedCaseData, CaseData.CaseDataBuilder<?, ?> updatedcaseDataBuilder) {
        ChangeOrganisationRequest updatedcor = updatedCaseData.getChangeOrganisationRequestField();

        // aac checks for:
        // 1. ChangeOrganisationRequest field in case data, it does this by looking for the orgtoadd node
        // 2. checks if caseroleID field is null
        // if those evaluate to true, then the noc request is auto approved
        // since we can't persist null values in db, this is a workaround by forcing the field to exist with a fake org id
        // the value will be removed on the next callback that updates case data after noc deicison has been applied.
        if (updatedcor == null) {
            updatedcaseDataBuilder.changeOrganisationRequestField(ChangeOrganisationRequest.builder()
                                                                      .organisationToAdd(Organisation.builder()
                                                                                             .organisationID(ORD_ID_FOR_AUTO_APPROVAL).build())
                                                                      .build());
        }
    }

    /** todo java doc
     *
     * @param caseDetails
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
