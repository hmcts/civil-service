package uk.gov.hmcts.reform.civil.handler.callback.camunda.noticeofchange;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.ccd.model.OrganisationPolicy;
import uk.gov.hmcts.reform.civil.callback.Callback;
import uk.gov.hmcts.reform.civil.callback.CallbackHandler;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.enums.CaseRole;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.IdamUserDetails;
import uk.gov.hmcts.reform.civil.model.SolicitorReferences;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.UPDATE_CASE_DETAILS_AFTER_NOC;
import static uk.gov.hmcts.reform.civil.enums.CaseCategory.UNSPEC_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.utils.CaseListSolicitorReferenceUtils.getAllDefendantSolicitorReferences;
import static uk.gov.hmcts.reform.civil.utils.CaseListSolicitorReferenceUtils.getAllOrganisationPolicyReferences;

@Slf4j
@Service
@RequiredArgsConstructor
public class UpdateCaseDetailsAfterNoCHandler extends CallbackHandler {

    private static final List<CaseEvent> EVENTS = Collections.singletonList(UPDATE_CASE_DETAILS_AFTER_NOC);

    public static final String TASK_ID = "UpdateCaseDetailsAfterNoC";

    private final ObjectMapper objectMapper;

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(
            callbackKey(ABOUT_TO_SUBMIT), this::updateCaseDetails
        );
    }

    @Override
    public String camundaActivityId(CallbackParams callbackParams) {
        return TASK_ID;
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    private CallbackResponse updateCaseDetails(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();

        CaseData.CaseDataBuilder<?, ?> caseDataBuilder = caseData.toBuilder();

        // nullify this field since it was persisted to auto approve noc
        caseDataBuilder.changeOrganisationRequestField(null);

        if (caseData.getChangeOfRepresentation() == null) {
            return AboutToStartOrSubmitCallbackResponse.builder()
                .errors(List.of("No Notice of Change events recorded"))
                .build();
        }

        String addedOrganisation = caseData.getChangeOfRepresentation().getOrganisationToAddID();

        if (addedOrganisation == null) {
            return AboutToStartOrSubmitCallbackResponse.builder()
                .errors(List.of("Organisation to add is null"))
                .build();
        }

        // TODO waiting on CCD-3538 to update the user email
        UserDetails addedSolicitorDetails = null;

        String replacedSolicitorCaseRole = caseData.getChangeOfRepresentation().getCaseRole();

        boolean isApplicantSolicitorRole = isApplicantOrRespondent(replacedSolicitorCaseRole);

        if (isApplicantSolicitorRole) {
            updateApplicantSolicitorDetails(caseDataBuilder, addedSolicitorDetails);
        } else {
            if (replacedSolicitorCaseRole.equals(CaseRole.RESPONDENTSOLICITORONE.getFormattedName())) {
                updateRespondentSolicitor1Details(caseDataBuilder, addedOrganisation, addedSolicitorDetails);
            } else {
                if (replacedSolicitorCaseRole.equals(CaseRole.RESPONDENTSOLICITORTWO.getFormattedName())) {
                    updateRespondentSolicitor2Details(caseDataBuilder, addedOrganisation, addedSolicitorDetails);
                }
            }
        }

        updateSolicitorReferences(caseData, caseDataBuilder, replacedSolicitorCaseRole);

        updateOrgPolicyReferences(caseData, caseDataBuilder, replacedSolicitorCaseRole);

        CaseData tempUpdatedCaseData = caseDataBuilder.build();

        if (!is1v1(tempUpdatedCaseData)) {
            if (isSameSolicitorScenario(tempUpdatedCaseData)) {
                caseDataBuilder.respondent2SameLegalRepresentative(YES);
            } else {
                caseDataBuilder.respondent2SameLegalRepresentative(NO);
            }
        }

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDataBuilder.build().toMap(objectMapper))
            .build();
    }

    private void updateOrgPolicyReferences(CaseData caseData,
                                           CaseData.CaseDataBuilder<?, ?> caseDataBuilder,
                                           String replacedSolicitorCaseRole) {
        if (CaseRole.RESPONDENTSOLICITORONE.getFormattedName().equals(replacedSolicitorCaseRole)) {
            OrganisationPolicy respondent1OrganisationPolicy = caseData.getRespondent1OrganisationPolicy();
            OrganisationPolicy updatedOrgPolicy =
                respondent1OrganisationPolicy.toBuilder().orgPolicyReference(null).build();
            caseDataBuilder.respondent1OrganisationPolicy(updatedOrgPolicy);
        } else if (CaseRole.RESPONDENTSOLICITORTWO.getFormattedName().equals(replacedSolicitorCaseRole)) {
            OrganisationPolicy respondent2OrganisationPolicy = caseData.getRespondent2OrganisationPolicy();
            OrganisationPolicy updatedOrgPolicy =
                respondent2OrganisationPolicy.toBuilder().orgPolicyReference(null).build();
            caseDataBuilder.respondent2OrganisationPolicy(updatedOrgPolicy);
        } else {
            OrganisationPolicy applicant1OrganisationPolicy = caseData.getApplicant1OrganisationPolicy();
            OrganisationPolicy updatedOrgPolicy =
                applicant1OrganisationPolicy.toBuilder().orgPolicyReference(null).build();
            caseDataBuilder.applicant1OrganisationPolicy(updatedOrgPolicy);
        }
        if (caseData.getUnassignedCaseListDisplayOrganisationReferences() != null) {
            caseDataBuilder.unassignedCaseListDisplayOrganisationReferences(
                getAllOrganisationPolicyReferences(caseDataBuilder.build()));
        }
    }

    private void updateRespondentSolicitor2Details(
        CaseData.CaseDataBuilder<?, ?> caseDataBuilder,
        String addedOrganisation,
        UserDetails addedSolicitorDetails) {
        CaseData caseData = caseDataBuilder.build();

        caseDataBuilder.respondent2OrgRegistered(YES)
            .respondentSolicitor2OrganisationDetails(null);

        if (UNSPEC_CLAIM.equals(caseData.getCaseAccessCategory())) {
            caseDataBuilder.respondentSolicitor2ServiceAddress(null)
                .respondentSolicitor2ServiceAddressRequired(NO)
                .respondentSolicitor2ServiceAddress(null)
                .respondent2OrganisationIDCopy(addedOrganisation)
                .respondent2Represented(YES);
        } else {
            caseDataBuilder.specRespondent2Represented(YES)
                .specAoSRespondentCorrespondenceAddressdetails(null);
        }

        if (addedSolicitorDetails != null) {
            caseDataBuilder.respondentSolicitor2EmailAddress(addedSolicitorDetails.getEmail());
        } else {
            caseDataBuilder.respondentSolicitor2EmailAddress(null);
        }
    }

    private void updateRespondentSolicitor1Details(CaseData.CaseDataBuilder<?, ?> caseDataBuilder,
                                                   String addedOrganisation, UserDetails addedSolicitorDetails) {
        CaseData caseData = caseDataBuilder.build();

        caseDataBuilder.respondent1OrgRegistered(YES)
            .respondentSolicitor1OrganisationDetails(null);

        if (UNSPEC_CLAIM.equals(caseData.getCaseAccessCategory())) {
            caseDataBuilder.respondentSolicitor1ServiceAddress(null)
                .respondentSolicitor1ServiceAddressRequired(NO)
                .respondent1OrganisationIDCopy(addedOrganisation)
                .respondent1Represented(YES);
        } else {
            caseDataBuilder.specApplicantCorrespondenceAddressRequired(NO)
                .specRespondentCorrespondenceAddressdetails(null)
                .specAoSRespondentCorrespondenceAddressdetails(null)
                .specAoSRespondentCorrespondenceAddressRequired(NO)
                .specRespondent1Represented(YES);
        }

        if (addedSolicitorDetails != null) {
            caseDataBuilder.respondentSolicitor1EmailAddress(addedSolicitorDetails.getEmail());
        } else {
            caseDataBuilder.respondentSolicitor1EmailAddress(null);
        }
    }

    private void updateApplicantSolicitorDetails(CaseData.CaseDataBuilder<?, ?> caseDataBuilder,
                                                 UserDetails addedSolicitorDetails) {
        CaseData caseData = caseDataBuilder.build();

        caseDataBuilder.applicantSolicitor1PbaAccounts(null)
            .applicantSolicitor1PbaAccountsIsEmpty(YES);

        if (UNSPEC_CLAIM.equals(caseData.getCaseAccessCategory())) {
            caseDataBuilder
                .applicantSolicitor1ServiceAddressRequired(NO)
                .applicantSolicitor1ServiceAddress(null);
        } else {
            caseDataBuilder
                .specApplicantCorrespondenceAddressdetails(null)
                .specApplicantCorrespondenceAddressRequired(NO)
                .specAoSApplicantCorrespondenceAddressRequired(NO)
                .specAoSApplicantCorrespondenceAddressdetails(null);
        }

        if (addedSolicitorDetails != null) {
            caseDataBuilder.applicantSolicitor1UserDetails(
                IdamUserDetails.builder()
                    .id(addedSolicitorDetails.getId())
                    .email(addedSolicitorDetails.getEmail())
                    .build()
            );
        } else {
            caseDataBuilder.applicantSolicitor1UserDetails(null);
        }
    }

    private void updateSolicitorReferences(CaseData caseData,
                                           CaseData.CaseDataBuilder<?, ?> caseDataBuilder, String replacedCaseRole) {
        if (caseData.getSolicitorReferences() != null) {
            SolicitorReferences solicitorReferences = caseData.getSolicitorReferences();
            String applicantReference = replacedCaseRole.equals(CaseRole.APPLICANTSOLICITORONE.getFormattedName())
                ? null : solicitorReferences.getApplicantSolicitor1Reference();

            String respondent1Reference = replacedCaseRole.equals(CaseRole.RESPONDENTSOLICITORONE.getFormattedName())
                ? null : solicitorReferences.getRespondentSolicitor1Reference();

            String respondent2Reference = replacedCaseRole.equals(CaseRole.RESPONDENTSOLICITORTWO.getFormattedName())
                ? null : solicitorReferences.getRespondentSolicitor2Reference();

            SolicitorReferences updatedSolicitorReferences = SolicitorReferences.builder()
                .applicantSolicitor1Reference(applicantReference)
                .respondentSolicitor1Reference(respondent1Reference)
                .respondentSolicitor2Reference(respondent2Reference)
                .build();

            caseDataBuilder.solicitorReferences(updatedSolicitorReferences);

            // only update if it's been created during acknowledge claim
            if (caseData.getSolicitorReferencesCopy() != null) {
                caseDataBuilder.solicitorReferencesCopy(updatedSolicitorReferences);
            }

            // also update this field because it exists
            if (replacedCaseRole.equals(CaseRole.RESPONDENTSOLICITORTWO.getFormattedName())) {
                caseDataBuilder.respondentSolicitor2Reference(null);
            }

            if (caseData.getCaseListDisplayDefendantSolicitorReferences() != null) {
                caseDataBuilder.caseListDisplayDefendantSolicitorReferences(
                    getAllDefendantSolicitorReferences(caseDataBuilder.build()));
            }
        }
    }

    private boolean isApplicantOrRespondent(String addedSolicitorRole) {
        return addedSolicitorRole.equals(CaseRole.APPLICANTSOLICITORONE.getFormattedName());
    }

    private boolean is1v1(CaseData caseData) {
        return caseData.getRespondent2() == null;
    }

    private boolean isSameSolicitorScenario(CaseData caseData) {
        return (caseData.getRespondent2() != null
            && UNSPEC_CLAIM.equals(caseData.getCaseAccessCategory())
            && caseData.getRespondent1OrganisationIDCopy() != null
            && caseData.getRespondent2OrganisationIDCopy() != null
            // need to check ID because orgID is null after create claim for unspec
            && caseData.getRespondent1OrganisationIDCopy().equals(
            caseData.getRespondent2OrganisationIDCopy()))
            || (caseData.getRespondent2() != null
            && caseData.getRespondent1OrganisationPolicy().getOrganisation() != null
            && caseData.getRespondent2OrganisationPolicy().getOrganisation() != null
            && caseData.getRespondent1OrganisationPolicy().getOrganisation().getOrganisationID().equals(
            caseData.getRespondent2OrganisationPolicy().getOrganisation().getOrganisationID()));
    }
}
