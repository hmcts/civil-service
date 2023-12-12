package uk.gov.hmcts.reform.civil.service.docmosis.claimantresponse;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.documentmanagement.DocumentManagementService;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType;
import uk.gov.hmcts.reform.civil.documentmanagement.model.PDF;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.docmosis.DocmosisDocument;
import uk.gov.hmcts.reform.civil.model.docmosis.claimantresponse.JudgmentByAdmissionOrDetermination;
import uk.gov.hmcts.reform.civil.model.docmosis.claimantresponse.JudgmentByAdmissionOrDeterminationMapper;
import uk.gov.hmcts.reform.civil.service.docmosis.DocumentGeneratorService;
import uk.gov.hmcts.reform.civil.service.docmosis.TemplateDataGenerator;

import static uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType.CCJ_REQUEST_ADMISSION;
import static uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType.CCJ_REQUEST_DETERMINATION;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.JUDGMENT_BY_ADMISSION_OR_DETERMINATION;

@Service
@Getter
@RequiredArgsConstructor
public class RequestJudgmentByAdmissionOrDeterminationResponseDocGenerator implements TemplateDataGenerator<JudgmentByAdmissionOrDetermination> {

    private final JudgmentByAdmissionOrDeterminationMapper judgmentByAdmissionOrDeterminationMapper;
    private final DocumentManagementService documentManagementService;
    private final DocumentGeneratorService documentGeneratorService;

    public CaseDocument generate(CaseEvent caseEvent, CaseData caseData, String authorisation) {

        DocmosisDocument docmosisDocument = documentGeneratorService.generateDocmosisDocument(
            getTemplateData(caseData),
                JUDGMENT_BY_ADMISSION_OR_DETERMINATION
        );
        return documentManagementService.uploadDocument(
            authorisation,
            new PDF(String.format(JUDGMENT_BY_ADMISSION_OR_DETERMINATION.getDocumentTitle(), caseData.getLegacyCaseReference(), getJudgmentType(caseEvent)),
                    docmosisDocument.getBytes(),
                    getDocumentType(caseEvent)
            )
        );
    }

    public JudgmentByAdmissionOrDetermination getTemplateData(CaseData caseData) {
        return judgmentByAdmissionOrDeterminationMapper.toClaimantResponseForm(caseData);
    }

    private String getJudgmentType(CaseEvent caseEvent) {
        return switch (caseEvent) {
            case GENERATE_JUDGMENT_BY_ADMISSION_RESPONSE_DOC -> "admission";
            case GENERATE_JUDGMENT_BY_DETERMINATION_RESPONSE_DOC -> "determination";
            default -> null;
        };
    }

    private DocumentType getDocumentType(CaseEvent caseEvent) {
        return switch (caseEvent) {
            case GENERATE_JUDGMENT_BY_ADMISSION_RESPONSE_DOC -> CCJ_REQUEST_ADMISSION;
            case GENERATE_JUDGMENT_BY_DETERMINATION_RESPONSE_DOC -> CCJ_REQUEST_DETERMINATION;
            default -> null;
        };
    }
}
