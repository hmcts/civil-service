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
        caseViewFields.add(injectRespondentAssigned(caseId));
        GetCaseCallbackResponse getCaseCallbackResponse = new GetCaseCallbackResponse();
        getCaseCallbackResponse.setMetadataFields(caseViewFields);

        return getCaseCallbackResponse;
    }

    private CaseViewField injectRespondentAssigned(String caseId) {
        CaseAssignedUserRolesResource userRoles = caseAccessDataStoreApi.getUserRoles(
                getCaaAccessToken(),
                authTokenGenerator.generate(),
                List.of(caseId)
        );
        YesOrNo respondentAssigned = userRoles.getCaseAssignedUserRoles() == null ? NO
                : userRoles.getCaseAssignedUserRoles().stream().anyMatch(a -> a.getCaseRole() != null
                && isRespondent(a.getCaseRole()))
                ? YES : NO;

        CaseViewField caseViewField = new CaseViewField();
        caseViewField.setId("[INJECTED_DATA.respondentAssigned]");
        caseViewField.setValue(respondentAssigned);
        caseViewField.setMetadata(true);

        FieldTypeDefinition fieldTypeDefinition = new FieldTypeDefinition();
        fieldTypeDefinition.setId("respondentAssigned");
        fieldTypeDefinition.setType("YesOrNo");

        caseViewField.setFieldTypeDefinition(fieldTypeDefinition);
        return caseViewField;
    }

    private String getCaaAccessToken() {
        return userService.getAccessToken(
                crossAccessUserConfiguration.getUserName(),
                crossAccessUserConfiguration.getPassword()
        );
    }

    private boolean isRespondent(String caseRole) {
        return caseRole.contains("[RESPONDENTSOLICITORONE]") || caseRole.contains("[RESPONDENTSOLICITORTWO]");
    }
}