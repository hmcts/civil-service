package uk.gov.hmcts.reform.civil.handler.callback.user.task.managecontactinformation;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.BusinessProcess;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.ContactDetailsUpdatedEvent;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.PartyFlagStructure;
import uk.gov.hmcts.reform.civil.model.UpdateDetailsForm;
import uk.gov.hmcts.reform.civil.model.UpdatePartyDetailsForm;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.dq.Applicant1DQ;
import uk.gov.hmcts.reform.civil.model.dq.Applicant2DQ;
import uk.gov.hmcts.reform.civil.model.dq.Expert;
import uk.gov.hmcts.reform.civil.model.dq.Experts;
import uk.gov.hmcts.reform.civil.model.dq.Respondent1DQ;
import uk.gov.hmcts.reform.civil.model.dq.Respondent2DQ;
import uk.gov.hmcts.reform.civil.model.dq.Witness;
import uk.gov.hmcts.reform.civil.model.dq.Witnesses;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;
import uk.gov.hmcts.reform.civil.service.UserService;
import uk.gov.hmcts.reform.civil.utils.CaseFlagsInitialiser;
import uk.gov.hmcts.reform.civil.utils.CaseNameUtils;
import uk.gov.hmcts.reform.civil.utils.PartyDetailsChangedUtil;

import java.util.List;
import java.util.Map;

import static java.util.Optional.ofNullable;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.MANAGE_CONTACT_INFORMATION;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.UPDATE_GA_CASE_DATA;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.utils.CaseNameUtils.buildCaseName;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.unwrapElements;
import static uk.gov.hmcts.reform.civil.utils.ManageContactInformationUtils.CLAIMANT_ONE_EXPERTS_ID;
import static uk.gov.hmcts.reform.civil.utils.ManageContactInformationUtils.CLAIMANT_ONE_ID;
import static uk.gov.hmcts.reform.civil.utils.ManageContactInformationUtils.CLAIMANT_ONE_LEGAL_REP_INDIVIDUALS_ID;
import static uk.gov.hmcts.reform.civil.utils.ManageContactInformationUtils.CLAIMANT_ONE_LITIGATION_FRIEND_ID;
import static uk.gov.hmcts.reform.civil.utils.ManageContactInformationUtils.CLAIMANT_ONE_ORG_INDIVIDUALS_ID;
import static uk.gov.hmcts.reform.civil.utils.ManageContactInformationUtils.CLAIMANT_ONE_WITNESSES_ID;
import static uk.gov.hmcts.reform.civil.utils.ManageContactInformationUtils.CLAIMANT_TWO_ID;
import static uk.gov.hmcts.reform.civil.utils.ManageContactInformationUtils.CLAIMANT_TWO_LITIGATION_FRIEND_ID;
import static uk.gov.hmcts.reform.civil.utils.ManageContactInformationUtils.CLAIMANT_TWO_ORG_INDIVIDUALS_ID;
import static uk.gov.hmcts.reform.civil.utils.ManageContactInformationUtils.DEFENDANT_ONE_EXPERTS_ID;
import static uk.gov.hmcts.reform.civil.utils.ManageContactInformationUtils.DEFENDANT_ONE_ID;
import static uk.gov.hmcts.reform.civil.utils.ManageContactInformationUtils.DEFENDANT_ONE_LEGAL_REP_INDIVIDUALS_ID;
import static uk.gov.hmcts.reform.civil.utils.ManageContactInformationUtils.DEFENDANT_ONE_LITIGATION_FRIEND_ID;
import static uk.gov.hmcts.reform.civil.utils.ManageContactInformationUtils.DEFENDANT_ONE_ORG_INDIVIDUALS_ID;
import static uk.gov.hmcts.reform.civil.utils.ManageContactInformationUtils.DEFENDANT_ONE_WITNESSES_ID;
import static uk.gov.hmcts.reform.civil.utils.ManageContactInformationUtils.DEFENDANT_TWO_EXPERTS_ID;
import static uk.gov.hmcts.reform.civil.utils.ManageContactInformationUtils.DEFENDANT_TWO_ID;
import static uk.gov.hmcts.reform.civil.utils.ManageContactInformationUtils.DEFENDANT_TWO_LEGAL_REP_INDIVIDUALS_ID;
import static uk.gov.hmcts.reform.civil.utils.ManageContactInformationUtils.DEFENDANT_TWO_LITIGATION_FRIEND_ID;
import static uk.gov.hmcts.reform.civil.utils.ManageContactInformationUtils.DEFENDANT_TWO_ORG_INDIVIDUALS_ID;
import static uk.gov.hmcts.reform.civil.utils.ManageContactInformationUtils.DEFENDANT_TWO_WITNESSES_ID;
import static uk.gov.hmcts.reform.civil.utils.ManageContactInformationUtils.mapFormDataToIndividualsData;
import static uk.gov.hmcts.reform.civil.utils.ManageContactInformationUtils.mapUpdatePartyDetailsFormToDQExperts;
import static uk.gov.hmcts.reform.civil.utils.ManageContactInformationUtils.mapUpdatePartyDetailsFormToDQWitnesses;
import static uk.gov.hmcts.reform.civil.utils.ManageContactInformationUtils.updatePartyDQExperts;
import static uk.gov.hmcts.reform.civil.utils.ManageContactInformationUtils.updatePartyDQWitnesses;
import static uk.gov.hmcts.reform.civil.utils.PartyUtils.populatePartyIndividuals;

@Component
@Slf4j
public class SubmitChangesTask {

    private final CaseDetailsConverter caseDetailsConverter;
    private final ObjectMapper objectMapper;
    private final UserService userService;
    private final CaseFlagsInitialiser caseFlagsInitialiser;
    private final PartyDetailsChangedUtil partyDetailsChangedUtil;
    private final CoreCaseDataService coreCaseDataService;

    private static final List<String> ADMIN_ROLES = List.of(
        "caseworker-civil-admin", "caseworker-civil-staff");

    public SubmitChangesTask(CaseDetailsConverter caseDetailsConverter, ObjectMapper objectMapper,
                             UserService userService, CaseFlagsInitialiser caseFlagsInitialiser,
                             PartyDetailsChangedUtil partyDetailsChangedUtil, CoreCaseDataService coreCaseDataService) {
        this.caseDetailsConverter = caseDetailsConverter;
        this.objectMapper = objectMapper;
        this.userService = userService;
        this.caseFlagsInitialiser = caseFlagsInitialiser;
        this.partyDetailsChangedUtil = partyDetailsChangedUtil;
        this.coreCaseDataService = coreCaseDataService;
    }

    public CallbackResponse submitChanges(CaseData caseData, CaseDetails caseDetailsBefore, String authToken) {
        log.info("Submit changes for case ID {}", caseData.getCcdCaseReference());

        String partyChosenId = caseData.getUpdateDetailsForm().getPartyChosenId();

        updateExperts(partyChosenId, caseData);
        updateWitnesses(partyChosenId, caseData);
        updateLRIndividuals(partyChosenId, caseData);
        updateOrgIndividuals(partyChosenId, caseData);
        updateGaCaseName(caseData);

        if (isParty(partyChosenId) || isLitigationFriend(partyChosenId)) {
            // update case name for hmc if applicant/respondent/litigation friend was updated
            caseData.setCaseNameHmctsInternal(CaseNameUtils.buildCaseName(caseData));
            caseData.setCaseNamePublic(buildCaseName(caseData));
        }

        // last step before clearing update details form
        caseFlagsInitialiser.initialiseCaseFlags(MANAGE_CONTACT_INFORMATION, caseData);

        // clear updateDetailsForm
        UpdateDetailsForm updateDetailsForm = new UpdateDetailsForm();
        updateDetailsForm.setManageContactDetailsEventUsed(YES);
        caseData.setUpdateDetailsForm(updateDetailsForm);

        // update claim details tab
        updateClaimDetailsTab(caseData);

        CaseData current = caseDetailsConverter.toCaseData(caseDetailsBefore);
        ContactDetailsUpdatedEvent changesEvent = partyDetailsChangedUtil.buildChangesEvent(current, caseData);
        //Populate individuals with partyID if they do not exist
        populatePartyIndividuals(caseData);

        if (changesEvent == null) {
            return AboutToStartOrSubmitCallbackResponse.builder()
                .data(caseData.toMap(objectMapper))
                .build();
        }

        YesOrNo submittedByCaseworker = isAdmin(authToken) ? YES : NO;

        caseData.setBusinessProcess(BusinessProcess.ready(MANAGE_CONTACT_INFORMATION));
        changesEvent.setSubmittedByCaseworker(submittedByCaseworker);
        caseData.setContactDetailsUpdatedEvent(changesEvent);

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseData.toMap(objectMapper))
            .build();
    }

    private void updateGaCaseName(CaseData caseData) {
        if (ofNullable(caseData.getGeneralApplications()).isPresent()) {
            caseData.getGeneralApplications().forEach(app -> coreCaseDataService
                .triggerGeneralApplicationEvent(Long.parseLong(app.getValue().getCaseLink().getCaseReference()),
                                                UPDATE_GA_CASE_DATA,
                                                Map.of("caseNameGaInternal", CaseNameUtils.buildCaseName(caseData))));
        }
    }

    private boolean isAdmin(String userAuthToken) {
        return userService.getUserInfo(userAuthToken).getRoles()
            .stream().anyMatch(ADMIN_ROLES::contains);
    }

    // wip can't be tested yet because need to get ids from new ticket: CIV-10382
    // have to delete experts (yes/no etc) if the experts are removed, same as witnesses
    private void updateExperts(String partyId, CaseData caseData) {
        List<Element<UpdatePartyDetailsForm>> formData = caseData.getUpdateDetailsForm().getUpdateExpertsDetailsForm();
        List<Element<Expert>> mappedExperts;

        if (partyId.equals(CLAIMANT_ONE_EXPERTS_ID)) {
            mappedExperts = mapUpdatePartyDetailsFormToDQExperts(
                caseData.getApplicant1DQ().getApplicant1DQExperts(), formData);
            Applicant1DQ applicant1DQ = caseData.getApplicant1DQ();
            applicant1DQ.setApplicant1DQExperts(
                buildExperts(applicant1DQ.getApplicant1DQExperts(), mappedExperts));
            caseData.setApplicant1DQ(applicant1DQ);
            List<Element<PartyFlagStructure>> updatedApplicantExperts = updatePartyDQExperts(
                unwrapElements(caseData.getApplicantExperts()),
                unwrapElements(mappedExperts)
            );
            caseData.setApplicantExperts(updatedApplicantExperts);

            // copy in applicant 2 for single response
            if (shouldCopyToApplicant2(caseData)) {
                Applicant2DQ applicant2DQ = caseData.getApplicant2DQ();
                applicant2DQ.setApplicant2DQExperts(
                    buildExperts(applicant1DQ.getApplicant1DQExperts(), mappedExperts));
                caseData.setApplicant2DQ(applicant2DQ);
            }

        } else if (partyId.equals(DEFENDANT_ONE_EXPERTS_ID)) {
            mappedExperts = mapUpdatePartyDetailsFormToDQExperts(
                caseData.getRespondent1DQ().getRespondent1DQExperts(), formData);
            Respondent1DQ respondent1DQ = caseData.getRespondent1DQ();
            respondent1DQ.setRespondent1DQExperts(
                buildExperts(respondent1DQ.getRespondent1DQExperts(), mappedExperts));
            caseData.setRespondent1DQ(respondent1DQ);
            List<Element<PartyFlagStructure>> updatedRespondent1Experts = updatePartyDQExperts(
                unwrapElements(caseData.getRespondent1Experts()),
                unwrapElements(mappedExperts)
            );
            caseData.setRespondent1Experts(updatedRespondent1Experts);

            // copy in respondent2 for 1v2SS single response
            if (shouldCopyToRespondent2(caseData)) {
                Respondent2DQ respondent2DQ = caseData.getRespondent2DQ();
                respondent2DQ.setRespondent2DQExperts(
                    buildExperts(respondent1DQ.getRespondent1DQExperts(), mappedExperts));
                caseData.setRespondent2DQ(respondent2DQ);
                caseData.setRespondent2Experts(updatedRespondent1Experts);
            }
        } else if (partyId.equals(DEFENDANT_TWO_EXPERTS_ID)) {
            mappedExperts = mapUpdatePartyDetailsFormToDQExperts(
                caseData.getRespondent2DQ().getRespondent2DQExperts(), formData);
            Respondent2DQ respondent2DQ = caseData.getRespondent2DQ();
            respondent2DQ.setRespondent2DQExperts(
                buildExperts(respondent2DQ.getRespondent2DQExperts(), mappedExperts));
            caseData.setRespondent2DQ(respondent2DQ);
            List<Element<PartyFlagStructure>> updatedRespondent2Experts = updatePartyDQExperts(
                unwrapElements(caseData.getRespondent2Experts()),
                unwrapElements(mappedExperts)
            );
            caseData.setRespondent2Experts(updatedRespondent2Experts);
        }
    }

    private void updateWitnesses(String partyId, CaseData caseData) {
        List<Element<UpdatePartyDetailsForm>> formData = caseData.getUpdateDetailsForm().getUpdateWitnessesDetailsForm();
        List<Element<Witness>> mappedWitnesses;

        if (partyId.equals(CLAIMANT_ONE_WITNESSES_ID)) {
            mappedWitnesses = mapUpdatePartyDetailsFormToDQWitnesses(
                caseData.getApplicant1DQ().getApplicant1DQWitnesses(), formData);
            Applicant1DQ applicant1DQ = caseData.getApplicant1DQ();
            applicant1DQ.setApplicant1DQWitnesses(
                buildWitnesses(applicant1DQ.getApplicant1DQWitnesses(), mappedWitnesses));
            caseData.setApplicant1DQ(applicant1DQ);
            List<Element<PartyFlagStructure>> updatedApplicantWitnesses = updatePartyDQWitnesses(
                unwrapElements(caseData.getApplicantWitnesses()),
                unwrapElements(mappedWitnesses)
            );
            caseData.setApplicantWitnesses(updatedApplicantWitnesses);

            // copy in applicant 2 for single response
            if (shouldCopyToApplicant2(caseData)) {
                Applicant2DQ applicant2DQ = caseData.getApplicant2DQ();
                applicant2DQ.setApplicant2DQWitnesses(
                    buildWitnesses(applicant1DQ.getApplicant1DQWitnesses(), mappedWitnesses));
                caseData.setApplicant2DQ(applicant2DQ);
            }
        } else if (partyId.equals(DEFENDANT_ONE_WITNESSES_ID)) {
            mappedWitnesses = mapUpdatePartyDetailsFormToDQWitnesses(
                caseData.getRespondent1DQ().getRespondent1DQWitnesses(), formData);
            Respondent1DQ respondent1DQ = caseData.getRespondent1DQ();
            respondent1DQ.setRespondent1DQWitnesses(
                buildWitnesses(respondent1DQ.getRespondent1DQWitnesses(), mappedWitnesses));
            caseData.setRespondent1DQ(respondent1DQ);
            List<Element<PartyFlagStructure>> updatedRespondent1Witnesses = updatePartyDQWitnesses(
                unwrapElements(caseData.getRespondent1Witnesses()),
                unwrapElements(mappedWitnesses)
            );
            caseData.setRespondent1Witnesses(updatedRespondent1Witnesses);

            // copy in respondent2 for 1v2SS single response
            if (shouldCopyToRespondent2(caseData)) {
                Respondent2DQ respondent2DQ = caseData.getRespondent2DQ();
                respondent2DQ.setRespondent2DQWitnesses(
                    buildWitnesses(respondent1DQ.getRespondent1DQWitnesses(), mappedWitnesses));
                caseData.setRespondent2DQ(respondent2DQ);
                caseData.setRespondent2Witnesses(updatedRespondent1Witnesses);
            }
        } else if (partyId.equals(DEFENDANT_TWO_WITNESSES_ID)) {
            mappedWitnesses = mapUpdatePartyDetailsFormToDQWitnesses(
                caseData.getRespondent2DQ().getRespondent2DQWitnesses(), formData);
            Respondent2DQ respondent2DQ = caseData.getRespondent2DQ();
            respondent2DQ.setRespondent2DQWitnesses(
                buildWitnesses(respondent2DQ.getRespondent2DQWitnesses(), mappedWitnesses));
            caseData.setRespondent2DQ(respondent2DQ);
            List<Element<PartyFlagStructure>> updatedRespondent2Witnesses = updatePartyDQWitnesses(
                unwrapElements(caseData.getRespondent2Witnesses()),
                unwrapElements(mappedWitnesses)
            );
            caseData.setRespondent2Witnesses(updatedRespondent2Witnesses);
        }
    }

    private void updateLRIndividuals(String partyId, CaseData caseData) {
        switch (partyId) {
            case CLAIMANT_ONE_LEGAL_REP_INDIVIDUALS_ID: {
                caseData.setApplicant1LRIndividuals(mapFormDataToIndividualsData(caseData.getApplicant1LRIndividuals(),
                                                                             caseData.getUpdateDetailsForm().getUpdateLRIndividualsForm()));
                return;
            }
            case DEFENDANT_ONE_LEGAL_REP_INDIVIDUALS_ID: {
                caseData.setRespondent1LRIndividuals(mapFormDataToIndividualsData(caseData.getRespondent1LRIndividuals(),
                                                                              caseData.getUpdateDetailsForm().getUpdateLRIndividualsForm()));
                return;
            }
            case DEFENDANT_TWO_LEGAL_REP_INDIVIDUALS_ID: {
                caseData.setRespondent2LRIndividuals(mapFormDataToIndividualsData(caseData.getRespondent2LRIndividuals(),
                                                                              caseData.getUpdateDetailsForm().getUpdateLRIndividualsForm()));
                return;
            }
            default:
        }
    }

    private void updateOrgIndividuals(String partyId, CaseData caseData) {
        switch (partyId) {
            case CLAIMANT_ONE_ORG_INDIVIDUALS_ID: {
                caseData.setApplicant1OrgIndividuals(mapFormDataToIndividualsData(caseData.getApplicant1OrgIndividuals(),
                                                                              caseData.getUpdateDetailsForm().getUpdateOrgIndividualsForm()));
                return;
            }
            case CLAIMANT_TWO_ORG_INDIVIDUALS_ID: {
                caseData.setApplicant2OrgIndividuals(mapFormDataToIndividualsData(caseData.getApplicant2OrgIndividuals(),
                                                                              caseData.getUpdateDetailsForm().getUpdateOrgIndividualsForm()));
                return;
            }
            case DEFENDANT_ONE_ORG_INDIVIDUALS_ID: {
                caseData.setRespondent1OrgIndividuals(mapFormDataToIndividualsData(caseData.getRespondent1OrgIndividuals(),
                                                                               caseData.getUpdateDetailsForm().getUpdateOrgIndividualsForm()));
                return;
            }
            case DEFENDANT_TWO_ORG_INDIVIDUALS_ID: {
                caseData.setRespondent2OrgIndividuals(mapFormDataToIndividualsData(caseData.getRespondent2OrgIndividuals(),
                                                                               caseData.getUpdateDetailsForm().getUpdateOrgIndividualsForm()));
                return;
            }
            default:
        }
    }

    private Boolean isParty(String partyChosen) {
        return CLAIMANT_ONE_ID.equals(partyChosen)
            || CLAIMANT_TWO_ID.equals(partyChosen)
            || DEFENDANT_ONE_ID.equals(partyChosen)
            || DEFENDANT_TWO_ID.equals(partyChosen);
    }

    private Boolean isLitigationFriend(String partyChosen) {
        return CLAIMANT_ONE_LITIGATION_FRIEND_ID.equals(partyChosen)
            || CLAIMANT_TWO_LITIGATION_FRIEND_ID.equals(partyChosen)
            || DEFENDANT_ONE_LITIGATION_FRIEND_ID.equals(partyChosen)
            || DEFENDANT_TWO_LITIGATION_FRIEND_ID.equals(partyChosen);
    }

    private void updateClaimDetailsTab(CaseData caseData) {
        Party respondent1Clone = objectMapper.convertValue(caseData.getRespondent1(), Party.class);
        respondent1Clone.setFlags(null);
        caseData.setRespondent1DetailsForClaimDetailsTab(respondent1Clone);
        //Make an copy and set the flag
        if (ofNullable(caseData.getRespondent2()).isPresent()) {
            Party respondent2Clone = objectMapper.convertValue(caseData.getRespondent2(), Party.class);
            respondent2Clone.setFlags(null);
            caseData.setRespondent2DetailsForClaimDetailsTab(respondent2Clone);
        }
    }

    private Experts buildExperts(Experts experts, List<Element<Expert>> mappedExperts) {
        Experts result = ofNullable(experts).orElse(new Experts());

        result.setExpertRequired(mappedExperts != null && !mappedExperts.isEmpty() ? YES : NO);
        result.setDetails(mappedExperts);

        return result;
    }

    private boolean shouldCopyToApplicant2(CaseData caseData) {
        return caseData.getApplicant2() != null
            && ((YES.equals(caseData.getApplicant1ProceedWithClaimMultiParty2v1())
            && YES.equals(caseData.getApplicant2ProceedWithClaimMultiParty2v1()))
            || YES.equals(caseData.getApplicant1ProceedWithClaimSpec2v1()));
    }

    private Witnesses buildWitnesses(Witnesses witnesses, List<Element<Witness>> mappedWitnesses) {
        Witnesses result = ofNullable(witnesses)
            .orElse(new Witnesses());

        result.setWitnessesToAppear(mappedWitnesses != null && mappedWitnesses.size() >= 1 ? YES : NO);
        result.setDetails(mappedWitnesses);

        return result;
    }

    private boolean shouldCopyToRespondent2(CaseData caseData) {
        return caseData.getRespondent2() != null
            && YES.equals(caseData.getRespondent2SameLegalRepresentative())
            && YES.equals(caseData.getRespondentResponseIsSame());
    }

}
