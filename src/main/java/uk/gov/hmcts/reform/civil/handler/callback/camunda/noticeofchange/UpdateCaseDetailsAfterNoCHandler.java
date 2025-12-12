package uk.gov.hmcts.reform.civil.handler.callback.camunda.noticeofchange;

import com.fasterxml.jackson.databind.ObjectMapper;
import feign.FeignException;
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
import uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType;
import uk.gov.hmcts.reform.civil.enums.CaseRole;
import uk.gov.hmcts.reform.civil.enums.MultiPartyScenario;
import uk.gov.hmcts.reform.civil.model.Address;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.IdamUserDetails;
import uk.gov.hmcts.reform.civil.model.RespondentSolicitorDetails;
import uk.gov.hmcts.reform.civil.model.SolicitorReferences;
import uk.gov.hmcts.reform.civil.prd.model.ContactInformation;
import uk.gov.hmcts.reform.civil.prd.model.Organisation;
import uk.gov.hmcts.reform.civil.service.CoreCaseUserService;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.OrganisationService;
import uk.gov.hmcts.reform.civil.utils.OrgPolicyUtils;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static java.util.Objects.nonNull;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.UPDATE_CASE_DETAILS_AFTER_NOC;
import static uk.gov.hmcts.reform.civil.enums.CaseCategory.UNSPEC_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.CaseRole.RESPONDENTSOLICITORONE;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.ONE_V_TWO_ONE_LEGAL_REP;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.ONE_V_TWO_TWO_LEGAL_REP;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.getMultiPartyScenario;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.isOneVOne;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.utils.CaseListSolicitorReferenceUtils.getAllDefendantSolicitorReferences;
import static uk.gov.hmcts.reform.civil.utils.CaseListSolicitorReferenceUtils.getAllOrganisationPolicyReferences;
import static uk.gov.hmcts.reform.civil.utils.CaseQueriesUtil.updateQueryCollectionPartyName;

@Slf4j
@Service
@RequiredArgsConstructor
public class UpdateCaseDetailsAfterNoCHandler extends CallbackHandler {

    private static final List<CaseEvent> EVENTS = Collections.singletonList(UPDATE_CASE_DETAILS_AFTER_NOC);

    public static final String TASK_ID = "UpdateCaseDetailsAfterNoC";

    private final ObjectMapper objectMapper;
    private final CoreCaseUserService coreCaseUserService;
    private final FeatureToggleService featureToggleService;
    private final OrganisationService organisationService;

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

        // Store original multiPartyScenario before any modifications
        final var originalMultiPartyScenario = getMultiPartyScenario(caseData);

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

        UserDetails addedSolicitorDetails = UserDetails.builder()
            .email(caseData.getChangeOrganisationRequestField().getCreatedBy())
            .build();

        // nullify this field since it was persisted to auto approve noc
        caseData.setChangeOrganisationRequestField(null);

        String replacedSolicitorCaseRole = caseData.getChangeOfRepresentation().getCaseRole();

        boolean isApplicantSolicitorRole = isApplicantOrRespondent(replacedSolicitorCaseRole);

        if (isApplicantSolicitorRole) {
            unAssignCaseFromClaimantLip(caseData);
            updateApplicantSolicitorDetails(caseData, addedSolicitorDetails);
        } else {
            unassignCaseFromDefendantLip(caseData);
            if (replacedSolicitorCaseRole.equals(CaseRole.RESPONDENTSOLICITORONE.getFormattedName())) {
                updateRespondentSolicitor1Details(caseData, addedOrganisation, addedSolicitorDetails);
            } else {
                if (replacedSolicitorCaseRole.equals(CaseRole.RESPONDENTSOLICITORTWO.getFormattedName())) {
                    updateRespondentSolicitor2Details(caseData, addedOrganisation, addedSolicitorDetails);
                }
            }
        }

        updateSolicitorReferences(caseData, replacedSolicitorCaseRole);

        updateOrgPolicyReferences(caseData, replacedSolicitorCaseRole);

        if (!is1v1(caseData)) {
            if (isSameSolicitorScenario(caseData)) {
                caseData.setRespondent2SameLegalRepresentative(YES);
            } else {
                caseData.setRespondent2SameLegalRepresentative(NO);
            }
        }

        if (featureToggleService.isJudgmentOnlineLive()) {
            caseData.setPreviousCCDState(callbackParams.getCaseData().getCcdState());
        }

        updateDefendantQueryCollectionPartyName(caseData);
        clearLRIndividuals(replacedSolicitorCaseRole, caseData, originalMultiPartyScenario);

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseData.toMap(objectMapper))
            .build();
    }

    private void updateDefendantQueryCollectionPartyName(CaseData caseData) {
        if (nonNull(caseData.getQmRespondentSolicitor1Queries())
            && getMultiPartyScenario(caseData).equals(ONE_V_TWO_TWO_LEGAL_REP)) {
            updateQueryCollectionPartyName(
                List.of(RESPONDENTSOLICITORONE.getFormattedName()),
                ONE_V_TWO_TWO_LEGAL_REP,
                caseData
            );
        }
    }

    private Consumer<Organisation> setRespondentSolicitorDetails(CaseData caseData) {
        return organisation -> {
            List<ContactInformation> contactInformation = organisation.getContactInformation();
            RespondentSolicitorDetails respondentSolicitorDetails = new RespondentSolicitorDetails();
            respondentSolicitorDetails.setOrgName(organisation.getName());
            respondentSolicitorDetails.setAddress(Address.fromContactInformation(contactInformation.get(0)));
            caseData.setRespondentSolicitorDetails(respondentSolicitorDetails);
        };
    }

    private void unassignCaseFromDefendantLip(CaseData caseData) {
        if (caseData.isRespondent1LiP() && caseData.getDefendantUserDetails() != null) {
            coreCaseUserService.unassignCase(caseData.getCcdCaseReference().toString(), caseData.getDefendantUserDetails().getId(), null, CaseRole.DEFENDANT);
        }
    }

    private void unAssignCaseFromClaimantLip(CaseData caseData) {
        if (caseData.isApplicantLiP() && caseData.getClaimantUserDetails() != null) {
            coreCaseUserService.unassignCase(
                caseData.getCcdCaseReference().toString(),
                caseData.getClaimantUserDetails().getId(),
                null,
                CaseRole.CLAIMANT
            );
            caseData.setApplicant1Represented(YES);
        }
    }

    private void clearLRIndividuals(String replacedSolicitorCaseRole, CaseData caseData, MultiPartyScenario originalMultiPartyScenario) {
        if (CaseRole.APPLICANTSOLICITORONE.getFormattedName().equals(replacedSolicitorCaseRole)) {
            caseData.setApplicant1LRIndividuals(null);
        } else if (CaseRole.RESPONDENTSOLICITORONE.getFormattedName().equals(replacedSolicitorCaseRole)) {
            if (ONE_V_TWO_ONE_LEGAL_REP.equals(getMultiPartyScenario(caseData))) {
                // if it's diff to same sol after noc, copy from defendant 2
                caseData.setRespondent1LRIndividuals(caseData.getRespondent2LRIndividuals());
                caseData.setRespondent2LRIndividuals(null);
            } else {
                // if it's same to diff sol after noc, copy into def 2 and clear def 1
                if (ONE_V_TWO_TWO_LEGAL_REP.equals(getMultiPartyScenario(caseData))
                    && ONE_V_TWO_ONE_LEGAL_REP.equals(originalMultiPartyScenario)) {
                    caseData.setRespondent2LRIndividuals(caseData.getRespondent1LRIndividuals());
                }
                caseData.setRespondent1LRIndividuals(null);
            }
        } else if (CaseRole.RESPONDENTSOLICITORTWO.getFormattedName().equals(replacedSolicitorCaseRole)) {
            // always clear for defendant 2 because even if it goes from DS to SS,
            // LR individuals are always stored against defendant 1
            caseData.setRespondent2LRIndividuals(null);
        }
    }

    private void updateOrgPolicyReferences(CaseData caseData,
                                           String replacedSolicitorCaseRole) {
        if (CaseRole.RESPONDENTSOLICITORONE.getFormattedName().equals(replacedSolicitorCaseRole)) {
            OrganisationPolicy respondent1OrganisationPolicy = caseData.getRespondent1OrganisationPolicy();
            respondent1OrganisationPolicy.setOrgPolicyReference(null);
        } else if (CaseRole.RESPONDENTSOLICITORTWO.getFormattedName().equals(replacedSolicitorCaseRole)) {
            OrganisationPolicy respondent2OrganisationPolicy = caseData.getRespondent2OrganisationPolicy();
            respondent2OrganisationPolicy.setOrgPolicyReference(null);
        } else {
            OrganisationPolicy applicant1OrganisationPolicy = caseData.getApplicant1OrganisationPolicy();
            applicant1OrganisationPolicy.setOrgPolicyReference(null);
        }
        if (caseData.getUnassignedCaseListDisplayOrganisationReferences() != null) {
            caseData.setUnassignedCaseListDisplayOrganisationReferences(
                getAllOrganisationPolicyReferences(caseData));
        }
    }

    private void updateRespondentSolicitor2Details(
        CaseData caseData,
        String addedOrganisation,
        UserDetails addedSolicitorDetails) {

        caseData.setRespondent2OrgRegistered(YES);
        caseData.setRespondentSolicitor2OrganisationDetails(null);
        caseData.setRespondent2Represented(YES);
        caseData.setDefendant2LIPAtClaimIssued(NO);

        caseData.setAnyRepresented(YES);

        if (UNSPEC_CLAIM.equals(caseData.getCaseAccessCategory())) {
            caseData.setRespondentSolicitor2ServiceAddress(null);
            caseData.setRespondentSolicitor2ServiceAddressRequired(NO);
            caseData.setRespondent2OrganisationIDCopy(addedOrganisation);
        } else {
            caseData.setSpecRespondent2Represented(YES);
            caseData.setSpecRespondent2CorrespondenceAddressRequired(null);
            caseData.setSpecRespondent2CorrespondenceAddressdetails(Address.builder().build());
            caseData.setSpecAoSRespondentCorrespondenceAddressdetails(null);
        }

        if (addedSolicitorDetails != null) {
            caseData.setRespondentSolicitor2EmailAddress(addedSolicitorDetails.getEmail());
        } else {
            caseData.setRespondentSolicitor2EmailAddress(null);
        }
    }

    private void updateRespondentSolicitor1Details(CaseData caseData,
                                                   String addedOrganisation, UserDetails addedSolicitorDetails) {

        caseData.setRespondent1OrgRegistered(YES);
        caseData.setRespondentSolicitor1OrganisationDetails(null);
        caseData.setRespondent1Represented(YES);
        caseData.setDefendant1LIPAtClaimIssued(NO);

        caseData.setAnyRepresented(YES);

        if (UNSPEC_CLAIM.equals(caseData.getCaseAccessCategory())) {
            caseData.setRespondentSolicitor1ServiceAddress(null);
            caseData.setRespondentSolicitor1ServiceAddressRequired(NO);
            caseData.setRespondent1OrganisationIDCopy(addedOrganisation);
        } else {
            caseData.setSpecRespondentCorrespondenceAddressdetails(null);
            caseData.setSpecAoSRespondentCorrespondenceAddressdetails(null);
            caseData.setSpecAoSRespondentCorrespondenceAddressRequired(null);
            caseData.setSpecRespondent1Represented(YES);
        }

        if (addedSolicitorDetails != null) {
            caseData.setRespondentSolicitor1EmailAddress(addedSolicitorDetails.getEmail());
        } else {
            caseData.setRespondentSolicitor1EmailAddress(null);
        }

        String organisationId = OrgPolicyUtils.getRespondent1SolicitorOrgId(caseData);
        if (organisationId != null
            && featureToggleService.isDefendantNoCOnlineForCase(caseData)
            && isOneVOne(caseData)) {
            try {
                organisationService.findOrganisationById(organisationId)
                    .ifPresent(setRespondentSolicitorDetails(caseData));
                caseData.getSystemGeneratedCaseDocuments().removeIf(e -> e.getValue().getDocumentType().equals(
                    DocumentType.CLAIMANT_CLAIM_FORM));
            } catch (FeignException e) {
                log.error("Error recovering org id " + organisationId
                              + " for case id " + caseData.getLegacyCaseReference(), e);
            }
        }
    }

    private void updateApplicantSolicitorDetails(CaseData caseData,
                                                 UserDetails addedSolicitorDetails) {

        caseData.setApplicantSolicitor1PbaAccounts(null);
        caseData.setApplicantSolicitor1PbaAccountsIsEmpty(YES);

        caseData.setAnyRepresented(YES);

        if (UNSPEC_CLAIM.equals(caseData.getCaseAccessCategory())) {
            caseData.setApplicantSolicitor1ServiceAddressRequired(NO);
            caseData.setApplicantSolicitor1ServiceAddress(null);
        } else {
            caseData.setSpecApplicantCorrespondenceAddressdetails(null);
            caseData.setSpecApplicantCorrespondenceAddressRequired(NO);
        }

        if (addedSolicitorDetails != null) {
            IdamUserDetails idamUserDetails = new IdamUserDetails();
            idamUserDetails.setId(addedSolicitorDetails.getId());
            idamUserDetails.setEmail(addedSolicitorDetails.getEmail());
            caseData.setApplicantSolicitor1UserDetails(idamUserDetails);
        } else {
            caseData.setApplicantSolicitor1UserDetails(null);
        }
    }

    private void updateSolicitorReferences(CaseData caseData, String replacedCaseRole) {
        if (caseData.getSolicitorReferences() != null) {
            SolicitorReferences solicitorReferences = caseData.getSolicitorReferences();
            String applicantReference = replacedCaseRole.equals(CaseRole.APPLICANTSOLICITORONE.getFormattedName())
                ? null : solicitorReferences.getApplicantSolicitor1Reference();

            String respondent1Reference = replacedCaseRole.equals(CaseRole.RESPONDENTSOLICITORONE.getFormattedName())
                ? null : solicitorReferences.getRespondentSolicitor1Reference();

            String respondent2Reference = replacedCaseRole.equals(CaseRole.RESPONDENTSOLICITORTWO.getFormattedName())
                ? null : solicitorReferences.getRespondentSolicitor2Reference();

            SolicitorReferences updatedSolicitorReferences = new SolicitorReferences();
            updatedSolicitorReferences.setApplicantSolicitor1Reference(applicantReference);
            updatedSolicitorReferences.setRespondentSolicitor1Reference(respondent1Reference);
            updatedSolicitorReferences.setRespondentSolicitor2Reference(respondent2Reference);

            caseData.setSolicitorReferences(updatedSolicitorReferences);

            // only update if it's been created during acknowledge claim
            if (caseData.getSolicitorReferencesCopy() != null) {
                caseData.setSolicitorReferencesCopy(updatedSolicitorReferences);
            }

            // also update this field because it exists
            if (replacedCaseRole.equals(CaseRole.RESPONDENTSOLICITORTWO.getFormattedName())) {
                caseData.setRespondentSolicitor2Reference(null);
            }

            if (caseData.getCaseListDisplayDefendantSolicitorReferences() != null) {
                caseData.setCaseListDisplayDefendantSolicitorReferences(
                    getAllDefendantSolicitorReferences(caseData));
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
