package uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.DocumentHandler;
import uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.LegalRepresentativeOneDocumentHandler;
import uk.gov.hmcts.reform.civil.model.BusinessProcess;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;

import java.time.LocalDateTime;
import java.util.List;

import static java.util.Objects.nonNull;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.EVIDENCE_UPLOADED;

@Component
public abstract class DocumentUploadTask<L1 extends LegalRepresentativeOneDocumentHandler, L2 extends DocumentHandler> {

    private final ObjectMapper objectMapper;
    protected final List<L1> legalRepresentativeOneDocumentHandlers;
    protected final List<L2> legalRepresentativeTwoDocumentHandlers;

    public DocumentUploadTask(FeatureToggleService featureToggleService,
                              ObjectMapper objectMapper,
                              List<L1> legalRepresentativeOneDocumentHandlers,
                              List<L2> legalRepresentativeTwoDocumentHandlers) {
        this.objectMapper = objectMapper;
        this.legalRepresentativeOneDocumentHandlers = legalRepresentativeOneDocumentHandlers;
        this.legalRepresentativeTwoDocumentHandlers = legalRepresentativeTwoDocumentHandlers;
    }

    abstract void applyDocumentUploadDate(CaseData caseData, LocalDateTime now);

    private void doUpdateDocumentListUploadedAfterBundle(CaseData caseData) {
        legalRepresentativeOneDocumentHandlers.forEach(handler -> handler.addUploadDocList(caseData));
        legalRepresentativeTwoDocumentHandlers.forEach(handler -> handler.addUploadDocList(caseData));
    }

    private void updateDocumentListUploadedAfterBundle(CaseData caseData) {
        if (nonNull(caseData.getCaseBundles()) && !caseData.getCaseBundles().isEmpty()) {
            doUpdateDocumentListUploadedAfterBundle(caseData);
        }
    }

    abstract String getSolicitorOneRole();

    abstract String getSolicitorTwoRole();

    abstract String getLegalRepresentativeTypeString(String selectedRole);

    abstract String getSelectedValueForBoth();

    private StringBuilder initiateNotificationTextBuilder(CaseData caseData) {
        StringBuilder notificationString = new StringBuilder();
        if (caseData.getNotificationText() != null) {
            notificationString = new StringBuilder(caseData.getNotificationText());
        }
        return notificationString;
    }

    public CallbackResponse uploadDocuments(CaseData caseData, CaseData caseDataBefore, String selectedRole) {
        caseData.setEvidenceUploadNotificationSent(YesOrNo.NO);
        // If notification has already been populated in current day, we want to append to that existing notification
        StringBuilder notificationTextBuilder = initiateNotificationTextBuilder(caseData);
        applyDocumentUploadDate(caseData, LocalDateTime.now());
        updateDocumentListUploadedAfterBundle(caseData);

        String litigantTypeString = getLegalRepresentativeTypeString(selectedRole);
        if (selectedRole.equals(getSolicitorOneRole()) || selectedRole.equals(getSelectedValueForBoth())) {
            for (LegalRepresentativeOneDocumentHandler handler : legalRepresentativeOneDocumentHandlers) {
                handler.handleDocuments(caseData, litigantTypeString, notificationTextBuilder);
                if (selectedRole.equals(getSelectedValueForBoth()) && handler.shouldCopyDocumentsToLegalRep2()) {
                    caseData = handler.copyLegalRep1ChangesToLegalRep2(caseData, caseDataBefore);
                }
            }
        }
        if (selectedRole.equals(getSolicitorTwoRole())) {

            for (DocumentHandler handler : legalRepresentativeTwoDocumentHandlers) {
                handler.handleDocuments(caseData, litigantTypeString, notificationTextBuilder);
            }
        }
        // null the values of the lists, so that on future retriggers of the event, they are blank
        clearDocumentCollections(caseData);
        caseData.setNotificationText(notificationTextBuilder.toString());

        caseData.setBusinessProcess(BusinessProcess.ready(EVIDENCE_UPLOADED));

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseData.toMap(objectMapper))
            .build();
    }

    private static void clearDocumentCollections(CaseData caseData) {
        caseData.setDisclosureSelectionEvidence(null);
        caseData.setDisclosureSelectionEvidenceRes(null);
        caseData.setWitnessSelectionEvidence(null);
        caseData.setWitnessSelectionEvidenceSmallClaim(null);
        caseData.setWitnessSelectionEvidenceRes(null);
        caseData.setWitnessSelectionEvidenceSmallClaimRes(null);
        caseData.setExpertSelectionEvidenceRes(null);
        caseData.setExpertSelectionEvidence(null);
        caseData.setExpertSelectionEvidenceSmallClaim(null);
        caseData.setExpertSelectionEvidenceSmallClaimRes(null);
        caseData.setTrialSelectionEvidence(null);
        caseData.setTrialSelectionEvidenceSmallClaim(null);
        caseData.setTrialSelectionEvidenceRes(null);
        caseData.setTrialSelectionEvidenceSmallClaimRes(null);
    }

}
