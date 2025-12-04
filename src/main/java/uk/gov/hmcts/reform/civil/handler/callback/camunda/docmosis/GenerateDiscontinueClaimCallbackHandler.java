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
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.enums.settlediscontinue.SettleDiscontinueYesOrNoList;
import uk.gov.hmcts.reform.civil.model.Address;
import uk.gov.hmcts.reform.civil.enums.settlediscontinue.DiscontinuanceTypeList;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.prd.model.Organisation;
import uk.gov.hmcts.reform.civil.service.OrganisationService;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.welshenhancements.PreTranslationDocumentType;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
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
import static uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType.NOTICE_OF_DISCONTINUANCE_DEFENDANT;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.element;

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
    private final FeatureToggleService featureToggleService;

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
        buildDocuments(callbackParams);

        return AboutToStartOrSubmitCallbackResponse.builder()
                .data(caseData.toMap(objectMapper))
                .build();
    }

    private void buildDocuments(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        boolean isRespondentLiP = false;
        Optional<Organisation> applicantLegalOrganisation = getLegalOrganization(caseData.getApplicant1OrganisationPolicy()
                                                                                     .getOrganisation().getOrganisationID());
        String appSolOrgName = getLegalName(applicantLegalOrganisation,
                                            caseData.getApplicantSolicitor1ClaimStatementOfTruth() != null
                                                ? caseData.getApplicantSolicitor1ClaimStatementOfTruth().getName()
                                                : null);
        Address applicant1SolicitorAddress = getLegalAddress(applicantLegalOrganisation,
                                                             caseData.getApplicantSolicitor1ServiceAddress(),
                                                             caseData.getSpecApplicantCorrespondenceAddressdetails(),
                                                             caseData.getCaseAccessCategory());

        String respondent1Name;
        Address respondent1Address;
        if (!caseData.isRespondent1LiP()) {
            isRespondentLiP = false;
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
            isRespondentLiP = true;
            respondent1Name = caseData.getRespondent1().getPartyName();
            respondent1Address = caseData.getRespondent1().getPrimaryAddress();
        }

        CaseDocument applicant1DiscontinueDoc = generateForm(appSolOrgName,
                                                             applicant1SolicitorAddress,
                                                             "claimant",
                                                             callbackParams, false);
        boolean generateRespondent2Form = (YES.equals(caseData.getAddRespondent2())
            && !YES.equals(caseData.getRespondent2SameLegalRepresentative()));
        CaseDocument respondent1DiscontinueDoc = generateForm(respondent1Name, respondent1Address,
                                                              generateRespondent2Form ? "defendant1" : "defendant", callbackParams, isRespondentLiP);
        CaseDocument respondent2DiscontinueDoc = null;

        if (generateRespondent2Form) {
            String respondent2Name;
            Address respondent2Address;
            if (!caseData.isRespondent2LiP()) {
                isRespondentLiP = false;
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
                isRespondentLiP = true;
                respondent2Name = caseData.getRespondent2().getPartyName();
                respondent2Address = caseData.getRespondent2().getPrimaryAddress();
            }
            respondent2DiscontinueDoc = generateForm(respondent2Name, respondent2Address, "defendant2", callbackParams, isRespondentLiP);
        }
        if (featureToggleService.isWelshEnabledForMainCase()
            && caseData.isRespondent1LiP()
            && caseData.getTypeOfDiscontinuance().equals(DiscontinuanceTypeList.PART_DISCONTINUANCE)
            && caseData.isRespondentResponseBilingual()
            && SettleDiscontinueYesOrNoList.NO.equals(caseData.getCourtPermissionNeeded())) {
            respondent1DiscontinueDoc.setDocumentType(NOTICE_OF_DISCONTINUANCE_DEFENDANT);
            List<Element<CaseDocument>> translatedDocuments = callbackParams.getCaseData()
                .getPreTranslationDocuments();
            assignDiscontinuanceCategoryId(applicant1DiscontinueDoc);
            assignDiscontinuanceCategoryId(respondent1DiscontinueDoc);
            translatedDocuments.add(element(respondent1DiscontinueDoc));
            caseData.setBilingualHint(YesOrNo.YES);
            caseData.setPreTranslationDocuments(translatedDocuments);
            caseData.setPreTranslationDocumentType(PreTranslationDocumentType.NOTICE_OF_DISCONTINUANCE);
            caseData.setApplicant1NoticeOfDiscontinueCWViewDoc(applicant1DiscontinueDoc);
        } else if (caseData.isJudgeOrderVerificationRequired()) {
            caseData.setApplicant1NoticeOfDiscontinueCWViewDoc(applicant1DiscontinueDoc);
            caseData.setRespondent1NoticeOfDiscontinueCWViewDoc(respondent1DiscontinueDoc);
            assignDiscontinuanceCategoryId(caseData.getApplicant1NoticeOfDiscontinueCWViewDoc());
            assignDiscontinuanceCategoryId(caseData.getRespondent1NoticeOfDiscontinueCWViewDoc());

            if (respondent2DiscontinueDoc != null) {
                caseData.setRespondent2NoticeOfDiscontinueCWViewDoc(respondent2DiscontinueDoc);
                assignDiscontinuanceCategoryId(caseData.getRespondent2NoticeOfDiscontinueCWViewDoc());
            }
        } else {
            caseData.setApplicant1NoticeOfDiscontinueAllPartyViewDoc(applicant1DiscontinueDoc);
            caseData.setRespondent1NoticeOfDiscontinueAllPartyViewDoc(respondent1DiscontinueDoc);
            assignDiscontinuanceCategoryId(caseData.getApplicant1NoticeOfDiscontinueAllPartyViewDoc());
            assignDiscontinuanceCategoryId(caseData.getRespondent1NoticeOfDiscontinueAllPartyViewDoc());

            if (respondent2DiscontinueDoc != null) {
                caseData.setRespondent2NoticeOfDiscontinueAllPartyViewDoc(respondent2DiscontinueDoc);
                assignDiscontinuanceCategoryId(caseData.getRespondent2NoticeOfDiscontinueAllPartyViewDoc());
            }
        }
    }

    private CaseDocument generateForm(String partyName, Address address, String partyType, CallbackParams callbackParams, boolean isRespondentLiP) {
        return formGenerator.generateDocs(
            callbackParams.getCaseData(), partyName, address, partyType, callbackParams.getParams().get(BEARER_TOKEN).toString(), isRespondentLiP);
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
        runTimeService.setVariable(
            caseData.getBusinessProcess().getProcessInstanceId(),
            "WELSH_ENABLED",
            featureToggleService.isWelshEnabledForMainCase()
                && caseData.isRespondent1LiP()
                && caseData.getTypeOfDiscontinuance().equals(DiscontinuanceTypeList.PART_DISCONTINUANCE)
                && caseData.isRespondentResponseBilingual()
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
        Address legalAddress = new Address();
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
