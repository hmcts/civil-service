package uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
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

    private final FeatureToggleService featureToggleService;
    private final ObjectMapper objectMapper;
    protected final List<L1> legalRepresentativeOneDocumentHandlers;
    protected final List<L2> legalRepresentativeTwoDocumentHandlers;

    public DocumentUploadTask(FeatureToggleService featureToggleService,
                              ObjectMapper objectMapper,
                              List<L1> legalRepresentativeOneDocumentHandlers,
                              List<L2> legalRepresentativeTwoDocumentHandlers) {
        this.featureToggleService = featureToggleService;
        this.objectMapper = objectMapper;
        this.legalRepresentativeOneDocumentHandlers = legalRepresentativeOneDocumentHandlers;
        this.legalRepresentativeTwoDocumentHandlers = legalRepresentativeTwoDocumentHandlers;
    }

    abstract void applyDocumentUploadDate(CaseData.CaseDataBuilder caseDataBuilder, LocalDateTime now);

    private void updateDocumentListUploadedAfterBundle(CaseData.CaseDataBuilder caseDataBuilder, CaseData caseData) {
        legalRepresentativeOneDocumentHandlers.forEach(handler -> handler.addUploadDocList(caseDataBuilder, caseData));
        legalRepresentativeTwoDocumentHandlers.forEach(handler -> handler.addUploadDocList(caseDataBuilder, caseData));
    }

    private void updateDocumentListUploadedAfterBundle(CaseData caseData, CaseData.CaseDataBuilder caseDataBuilder) {
        if (nonNull(caseData.getCaseBundles()) && !caseData.getCaseBundles().isEmpty()) {
            updateDocumentListUploadedAfterBundle(caseDataBuilder, caseData);
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

        CaseData.CaseDataBuilder<?, ?> caseDataBuilder = caseData.toBuilder();
        // If notification has already been populated in current day, we want to append to that existing notification
        StringBuilder notificationTextBuilder = initiateNotificationTextBuilder(caseData);
        applyDocumentUploadDate(caseDataBuilder, LocalDateTime.now());
        updateDocumentListUploadedAfterBundle(caseData, caseDataBuilder);

        String litigantTypeString = getLegalRepresentativeTypeString(selectedRole);
        if (selectedRole.equals(getSolicitorOneRole()) || selectedRole.equals(getSelectedValueForBoth())) {
            for (LegalRepresentativeOneDocumentHandler handler : legalRepresentativeOneDocumentHandlers) {
                handler.handleDocuments(caseData, litigantTypeString, notificationTextBuilder);
                if (selectedRole.equals(getSelectedValueForBoth()) && handler.shouldCopyDocumentsToLegalRep2()) {
                    caseData = handler.copyLegalRep1ChangesToLegalRep2(caseData, caseDataBefore, caseDataBuilder);
                }
            }
        }
        if (selectedRole.equals(getSolicitorTwoRole())) {

            for (DocumentHandler handler : legalRepresentativeTwoDocumentHandlers) {
                handler.handleDocuments(caseData, litigantTypeString, notificationTextBuilder);
            }
        }
        // null the values of the lists, so that on future retriggers of the event, they are blank
        clearDocumentCollections(caseDataBuilder);
        caseDataBuilder.notificationText(notificationTextBuilder.toString());

        if (featureToggleService.isCaseProgressionEnabled()) {
            caseDataBuilder.businessProcess(BusinessProcess.ready(EVIDENCE_UPLOADED));
        }

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDataBuilder.build().toMap(objectMapper))
            .build();
    }

    private static void clearDocumentCollections(CaseData.CaseDataBuilder<?, ?> caseDataBuilder) {
        caseDataBuilder.disclosureSelectionEvidence(null);
        caseDataBuilder.disclosureSelectionEvidenceRes(null);
        caseDataBuilder.witnessSelectionEvidence(null);
        caseDataBuilder.witnessSelectionEvidenceSmallClaim(null);
        caseDataBuilder.witnessSelectionEvidenceRes(null);
        caseDataBuilder.witnessSelectionEvidenceSmallClaimRes(null);
        caseDataBuilder.expertSelectionEvidenceRes(null);
        caseDataBuilder.expertSelectionEvidence(null);
        caseDataBuilder.expertSelectionEvidenceSmallClaim(null);
        caseDataBuilder.expertSelectionEvidenceSmallClaimRes(null);
        caseDataBuilder.trialSelectionEvidence(null);
        caseDataBuilder.trialSelectionEvidenceSmallClaim(null);
        caseDataBuilder.trialSelectionEvidenceRes(null);
        caseDataBuilder.trialSelectionEvidenceSmallClaimRes(null);
    }

}
