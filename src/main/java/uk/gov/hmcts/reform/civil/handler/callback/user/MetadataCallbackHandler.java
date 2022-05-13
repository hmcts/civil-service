package uk.gov.hmcts.reform.civil.handler.callback.user;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CaseAccessDataStoreApi;
import uk.gov.hmcts.reform.ccd.client.model.GetCaseCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.metadatafields.CaseViewField;
import uk.gov.hmcts.reform.ccd.client.model.metadatafields.definition.FieldTypeDefinition;
import uk.gov.hmcts.reform.ccd.model.CaseAssignedUserRolesResource;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.config.CrossAccessUserConfiguration;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.UserService;

import java.util.ArrayList;
import java.util.List;

import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;

@Service
@RequiredArgsConstructor
public class MetadataCallbackHandler {
    private final CaseAccessDataStoreApi caseAccessDataStoreApi;
    private final AuthTokenGenerator authTokenGenerator;
    private final UserService userService;
    private final CrossAccessUserConfiguration crossAccessUserConfiguration;

    public GetCaseCallbackResponse injectMetaData(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        String caseId = caseData.getCcdCaseReference().toString();
        List<CaseViewField> caseViewFields = new ArrayList<>();
        caseViewFields.add(injectRespondentAssigned(caseData, caseId));
        GetCaseCallbackResponse getCaseCallbackResponse = new GetCaseCallbackResponse();
        getCaseCallbackResponse.setMetadataFields(caseViewFields);

        return getCaseCallbackResponse;
    }

    private CaseViewField injectRespondentAssigned(CaseData caseData, String caseId) {

        CaseViewField caseViewField = new CaseViewField();
        caseViewField.setId("[INJECTED_DATA.respondentAssigned]");
        caseViewField.setValue(respondentAssigned(getUserRolesOnCase(caseId), getRespondentCaseRoles(caseData)));
        caseViewField.setMetadata(true);

        FieldTypeDefinition fieldTypeDefinition = new FieldTypeDefinition();
        fieldTypeDefinition.setId("respondentAssigned");
        fieldTypeDefinition.setType("Text");

        caseViewField.setFieldTypeDefinition(fieldTypeDefinition);
        return caseViewField;
    }

    private String getCaaAccessToken() {
        return userService.getAccessToken(
                crossAccessUserConfiguration.getUserName(),
                crossAccessUserConfiguration.getPassword()
        );
    }

    private String respondentAssigned(CaseAssignedUserRolesResource userRoles, List<String> respondentCaseRoles) {
        return userRoles.getCaseAssignedUserRoles() == null ? "No"
                : userRoles.getCaseAssignedUserRoles().stream().anyMatch(a -> a.getCaseRole() != null
                && isRespondent(respondentCaseRoles, a.getCaseRole()))
                ? "Yes" : "No";
    }

    private CaseAssignedUserRolesResource getUserRolesOnCase(String caseId) {
        return caseAccessDataStoreApi.getUserRoles(
                getCaaAccessToken(),
                authTokenGenerator.generate(),
                List.of(caseId)
        );
    }

    private List<String> getRespondentCaseRoles(CaseData caseData) {
        List<String> respondentCaseRoles = new ArrayList<>();
        respondentCaseRoles.add(caseData.getRespondent1OrganisationPolicy().getOrgPolicyCaseAssignedRole());
        if (caseData.getRespondent2OrganisationPolicy() != null) {
            respondentCaseRoles.add(caseData.getRespondent2OrganisationPolicy().getOrgPolicyCaseAssignedRole());
        }
        return respondentCaseRoles;
    }

    private boolean isRespondent(List<String> respondentRoles, String userRole) {
        return respondentRoles.stream().anyMatch(a -> a.equals(userRole));
    }
}