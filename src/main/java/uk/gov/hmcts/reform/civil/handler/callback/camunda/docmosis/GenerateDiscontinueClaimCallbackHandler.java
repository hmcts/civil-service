package uk.gov.hmcts.reform.civil.handler.callback.camunda.docmosis;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.camunda.bpm.engine.RuntimeService;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.civil.callback.Callback;
import uk.gov.hmcts.reform.civil.callback.CallbackHandler;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.enums.CaseCategory;
import uk.gov.hmcts.reform.civil.enums.DocCategory;
import uk.gov.hmcts.reform.civil.model.Address;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.prd.model.Organisation;
import uk.gov.hmcts.reform.civil.service.OrganisationService;
import uk.gov.hmcts.reform.civil.service.docmosis.settlediscontinue.NoticeOfDiscontinuanceFormGenerator;
import uk.gov.hmcts.reform.civil.utils.AssignCategoryId;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.Objects.nonNull;
import static uk.gov.hmcts.reform.civil.callback.CallbackParams.Params.BEARER_TOKEN;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.GEN_NOTICE_OF_DISCONTINUANCE;
import static uk.gov.hmcts.reform.civil.enums.CaseCategory.SPEC_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;

@Service
@RequiredArgsConstructor
@SuppressWarnings("unchecked")
public class GenerateDiscontinueClaimCallbackHandler extends CallbackHandler {

    private static final List<CaseEvent> EVENTS = List.of(
        GEN_NOTICE_OF_DISCONTINUANCE
    );
    private static final String TASK_ID = "GenerateNoticeOfDiscontinueClaim";
    private final ObjectMapper objectMapper;
    private final AssignCategoryId assignCategoryId;
    private final NoticeOfDiscontinuanceFormGenerator formGenerator;
    private final RuntimeService runTimeService;
    private final OrganisationService organisationService;

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(callbackKey(ABOUT_TO_SUBMIT), this::aboutToSubmit);
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    @Override
    public String camundaActivityId(CallbackParams callbackParams) {
        return TASK_ID;
    }

    private CallbackResponse aboutToSubmit(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        updateCamundaVars(caseData);
        CaseData.CaseDataBuilder<?, ?> caseDataBuilder = caseData.toBuilder();
        buildDocuments(callbackParams, caseDataBuilder);

        return AboutToStartOrSubmitCallbackResponse.builder()
                .data(caseDataBuilder.build().toMap(objectMapper))
                .build();
    }

    private void buildDocuments(CallbackParams callbackParams, CaseData.CaseDataBuilder<?, ?> caseDataBuilder) {
        CaseData caseData = callbackParams.getCaseData();

        Optional<Organisation> applicantLegalOrganisation = getLegalOrganization(caseData.getApplicant1OrganisationPolicy()
                                                                                     .getOrganisation().getOrganisationID());
        String appSolOrgName = getLegalName(applicantLegalOrganisation,
                                            caseData.getApplicantSolicitor1ClaimStatementOfTruth().getName());
        Address applicant1SolicitorAddress = getLegalAddress(applicantLegalOrganisation,
                                                             caseData.getApplicantSolicitor1ServiceAddress(),
                                                             caseData.getSpecApplicantCorrespondenceAddressdetails(),
                                                             caseData.getCaseAccessCategory());

        String respondent1Name;
        Address respondent1Address;
        if (!caseData.isRespondent1LiP()) {
            Optional<Organisation> respondentLegalOrganisation = getLegalOrganization(caseData.getRespondent1OrganisationPolicy()
                                                                                         .getOrganisation().getOrganisationID());
            respondent1Name = getLegalName(respondentLegalOrganisation, caseData.getRespondent1DQ() != null
                && caseData.getRespondent1DQ().getRespondent1DQStatementOfTruth() != null
                ? caseData.getRespondent1DQ().getRespondent1DQStatementOfTruth().getName()
                : null);
            respondent1Address = getLegalAddress(respondentLegalOrganisation,
                                                 caseData.getRespondentSolicitor1ServiceAddress(),
                                                 caseData.getSpecRespondentCorrespondenceAddressdetails(),
                                                 caseData.getCaseAccessCategory());
        } else {
            respondent1Name = caseData.getRespondent1().getPartyName();
            respondent1Address = caseData.getRespondent1().getPrimaryAddress();
        }

        CaseDocument applicant1DiscontinueDoc = generateForm(appSolOrgName,
                                                             applicant1SolicitorAddress,
                                                             "claimant",
                                                             callbackParams);
        boolean generateRespondent2Form = (YES.equals(caseData.getAddRespondent2())
            && !YES.equals(caseData.getRespondent2SameLegalRepresentative()));
        CaseDocument respondent1DiscontinueDoc = generateForm(respondent1Name, respondent1Address,
                                                              generateRespondent2Form ? "defendant1" : "defendant", callbackParams);
        CaseDocument respondent2DiscontinueDoc = null;

        if (generateRespondent2Form) {
            String respondent2Name;
            Address respondent2Address;
            if (!caseData.isRespondent2LiP()) {
                Optional<Organisation> respondentLegalOrganisation = getLegalOrganization(caseData.getRespondent2OrganisationPolicy()
                                                                                              .getOrganisation().getOrganisationID());
                respondent2Name = getLegalName(respondentLegalOrganisation, caseData.getRespondent2DQ() != null
                    && caseData.getRespondent2DQ().getRespondent2DQStatementOfTruth() != null
                    ? caseData.getRespondent2DQ().getRespondent2DQStatementOfTruth().getName()
                    : null);
                respondent2Address = getLegalAddress(respondentLegalOrganisation,
                                                     caseData.getRespondentSolicitor2ServiceAddress(),
                                                     caseData.getSpecRespondent2CorrespondenceAddressdetails(),
                                                     caseData.getCaseAccessCategory());
            } else {
                respondent2Name = caseData.getRespondent2().getPartyName();
                respondent2Address = caseData.getRespondent2().getPrimaryAddress();
            }
            respondent2DiscontinueDoc = generateForm(respondent2Name, respondent2Address, "defendant2", callbackParams);
        }

        if (caseData.isJudgeOrderVerificationRequired()) {
            caseDataBuilder.applicant1NoticeOfDiscontinueCWViewDoc(applicant1DiscontinueDoc);
            caseDataBuilder.respondent1NoticeOfDiscontinueCWViewDoc(respondent1DiscontinueDoc);
            assignDiscontinuanceCategoryId(caseDataBuilder.build().getApplicant1NoticeOfDiscontinueCWViewDoc());
            assignDiscontinuanceCategoryId(caseDataBuilder.build().getRespondent1NoticeOfDiscontinueCWViewDoc());

            if (respondent2DiscontinueDoc != null) {
                caseDataBuilder.respondent2NoticeOfDiscontinueCWViewDoc(respondent2DiscontinueDoc);
                assignDiscontinuanceCategoryId(caseDataBuilder.build().getRespondent2NoticeOfDiscontinueCWViewDoc());
            }
        } else {
            caseDataBuilder.applicant1NoticeOfDiscontinueAllPartyViewDoc(applicant1DiscontinueDoc);
            caseDataBuilder.respondent1NoticeOfDiscontinueAllPartyViewDoc(respondent1DiscontinueDoc);
            assignDiscontinuanceCategoryId(caseDataBuilder.build().getApplicant1NoticeOfDiscontinueAllPartyViewDoc());
            assignDiscontinuanceCategoryId(caseDataBuilder.build().getRespondent1NoticeOfDiscontinueAllPartyViewDoc());

            if (respondent2DiscontinueDoc != null) {
                caseDataBuilder.respondent2NoticeOfDiscontinueAllPartyViewDoc(respondent2DiscontinueDoc);
                assignDiscontinuanceCategoryId(caseDataBuilder.build().getRespondent2NoticeOfDiscontinueAllPartyViewDoc());
            }
        }
    }

    private CaseDocument generateForm(String partyName, Address address, String partyType, CallbackParams callbackParams) {
        return formGenerator.generateDocs(
            callbackParams.getCaseData(), partyName, address, partyType, callbackParams.getParams().get(BEARER_TOKEN).toString());
    }

    private void assignDiscontinuanceCategoryId(CaseDocument caseDocument) {
        assignCategoryId.assignCategoryIdToCaseDocument(caseDocument, DocCategory.NOTICE_OF_DISCONTINUE.getValue());
    }

    private void updateCamundaVars(CaseData caseData) {
        runTimeService.setVariable(
            caseData.getBusinessProcess().getProcessInstanceId(),
            "JUDGE_ORDER_VERIFICATION_REQUIRED",
            caseData.isJudgeOrderVerificationRequired()
        );
    }

    public Optional<Organisation> getLegalOrganization(String id) {
        return organisationService.findOrganisationById(id);
    }

    protected String getLegalName(Optional<Organisation> organisation, String statementOfTruthName) {
        String respondentLegalOrganizationName = nonNull(statementOfTruthName) ? statementOfTruthName : "";
        if (organisation.isPresent() && nonNull(organisation.get().getName())) {
            respondentLegalOrganizationName = organisation.get().getName();
        }
        return respondentLegalOrganizationName;
    }

    protected Address getLegalAddress(Optional<Organisation> organisation,
                                     Address serviceAddress,
                                     Address correspondenceAddress, CaseCategory caseCategory) {
        Address legalAddress = Address.builder().build();
        if (organisation.isPresent()
            && nonNull(organisation.get().getContactInformation())
            && !organisation.get().getContactInformation().isEmpty()) {
            legalAddress = Address.fromContactInformation(organisation.get().getContactInformation().get(0));
        }

        if (nonNull(serviceAddress) && nonNull(serviceAddress.getAddressLine1())) {
            legalAddress = serviceAddress;
        }

        if (SPEC_CLAIM.equals(caseCategory)
            && nonNull(correspondenceAddress) && nonNull(correspondenceAddress.getAddressLine1())) {
            legalAddress = correspondenceAddress;
        }
        return legalAddress;
    }
}
