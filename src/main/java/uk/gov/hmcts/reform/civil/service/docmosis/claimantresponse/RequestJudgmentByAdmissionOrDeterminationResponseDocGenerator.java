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
import uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates;
import uk.gov.hmcts.reform.civil.service.docmosis.DocumentGeneratorService;
import uk.gov.hmcts.reform.civil.service.docmosis.TemplateDataGenerator;
import uk.gov.hmcts.reform.civil.utils.AssignCategoryId;

import java.util.ArrayList;
import java.util.List;

import static uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType.CCJ_REQUEST_ADMISSION;
import static uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType.CCJ_REQUEST_DETERMINATION;
import static uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType.JUDGMENT_BY_ADMISSION_CLAIMANT;
import static uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType.JUDGMENT_BY_ADMISSION_DEFENDANT;
import static uk.gov.hmcts.reform.civil.helpers.hearingsmappings.HearingDetailsMapper.isWelshHearingSelected;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.JUDGMENT_BY_ADMISSION_OR_DETERMINATION;

@Service
@Getter
@RequiredArgsConstructor
public class RequestJudgmentByAdmissionOrDeterminationResponseDocGenerator implements TemplateDataGenerator<JudgmentByAdmissionOrDetermination> {

    private final JudgmentByAdmissionOrDeterminationMapper judgmentByAdmissionOrDeterminationMapper;
    private final DocumentManagementService documentManagementService;
    private final DocumentGeneratorService documentGeneratorService;
    private final AssignCategoryId assignCategoryId;

    public CaseDocument generate(CaseEvent caseEvent, CaseData caseData, String authorisation) {

        DocmosisDocument docmosisDocument = documentGeneratorService.generateDocmosisDocument(
            getTemplateData(caseData, caseEvent),
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

    @Override
    public JudgmentByAdmissionOrDetermination getTemplateData(CaseData caseData, CaseEvent caseEvent) {
        return judgmentByAdmissionOrDeterminationMapper.toClaimantResponseForm(caseData, caseEvent);
    }

    private String getJudgmentType(CaseEvent caseEvent) {
        return switch (caseEvent) {
            case GENERATE_JUDGMENT_BY_ADMISSION_RESPONSE_DOC, GENERATE_DEFAULT_JUDGMENT_BY_ADMISSION_RESPONSE_DOC,
                GEN_JUDGMENT_BY_ADMISSION_DOC_CLAIMANT, GEN_JUDGMENT_BY_ADMISSION_DOC_DEFENDANT -> "admission";
            case GENERATE_JUDGMENT_BY_DETERMINATION_RESPONSE_DOC -> "determination";
            default -> throw new IllegalArgumentException(String.format("No DocumentType available for %s event", caseEvent));
        };
    }

    private DocumentType getDocumentType(CaseEvent caseEvent) {
        return switch (caseEvent) {
            case GENERATE_JUDGMENT_BY_ADMISSION_RESPONSE_DOC, GENERATE_DEFAULT_JUDGMENT_BY_ADMISSION_RESPONSE_DOC -> CCJ_REQUEST_ADMISSION;
            case GENERATE_JUDGMENT_BY_DETERMINATION_RESPONSE_DOC -> CCJ_REQUEST_DETERMINATION;
            case GEN_JUDGMENT_BY_ADMISSION_DOC_CLAIMANT -> JUDGMENT_BY_ADMISSION_CLAIMANT;
            case GEN_JUDGMENT_BY_ADMISSION_DOC_DEFENDANT -> JUDGMENT_BY_ADMISSION_DEFENDANT;
            default -> throw new IllegalArgumentException(String.format("No DocumentType available for %s event", caseEvent));
        };
    }

    private DocmosisTemplates getTemplateName(CaseEvent caseEvent, boolean isBilingual) {
        if (caseEvent.name().equals(CaseEvent.GEN_JUDGMENT_BY_ADMISSION_DOC_CLAIMANT.name())) {
            return isBilingual ? DocmosisTemplates.JUDGMENT_BY_ADMISSION_CLAIMANT_BILINGUAL : DocmosisTemplates.JUDGMENT_BY_ADMISSION_CLAIMANT;
        } else {
            return isBilingual ? DocmosisTemplates.JUDGMENT_BY_ADMISSION_DEFENDANT_BILINGUAL : DocmosisTemplates.JUDGMENT_BY_ADMISSION_DEFENDANT;
        }
    }

    public List<CaseDocument> generateNonDivergentDocs(CaseData caseData, String authorisation, CaseEvent caseEvent) {
        List<CaseDocument> list = new ArrayList<>();
        if (caseData.isClaimantBilingual() || isWelshHearingSelected(caseData) || caseData.isRespondentResponseBilingual()) {
            CaseDocument uploadedWelshDocument = generateNonDivergentWelshDocs(caseData, authorisation, caseEvent);
            assignCategoryId.assignCategoryIdToCaseDocument(uploadedWelshDocument, "judgments");
            list.add(uploadedWelshDocument);
        } else {
            DocmosisDocument docmosisDocument = documentGeneratorService.generateDocmosisDocument(
                getTemplateDataForNonDivergentDocs(caseData),
                getTemplateName(caseEvent, false)
            );
            CaseDocument uploadedDocument = documentManagementService.uploadDocument(
                authorisation,
                new PDF(
                    getTemplateName(caseEvent, false).getDocumentTitle(),
                    docmosisDocument.getBytes(),
                    getDocumentType(caseEvent)
                )
            );
            assignCategoryId.assignCategoryIdToCaseDocument(uploadedDocument, "judgments");
            list.add(uploadedDocument);
        }
        return list;
    }

    private JudgmentByAdmissionOrDetermination getTemplateDataForNonDivergentDocs(CaseData caseData) {
        return judgmentByAdmissionOrDeterminationMapper.toNonDivergentDocs(caseData);
    }

    private CaseDocument generateNonDivergentWelshDocs(CaseData caseData, String authorisation, CaseEvent caseEvent) {
        JudgmentByAdmissionOrDetermination templateData = getTemplateDataForNonDivergentDocs(caseData);
        JudgmentByAdmissionOrDetermination welshTemplateData =
            judgmentByAdmissionOrDeterminationMapper.toNonDivergentWelshDocs(caseData, templateData);

        DocmosisDocument welshDocument = documentGeneratorService.generateDocmosisDocument(
            welshTemplateData,
            getTemplateName(caseEvent, true)
        );

        return documentManagementService.uploadDocument(
            authorisation,
            new PDF(
                getTemplateName(caseEvent, true).getDocumentTitle(),
                welshDocument.getBytes(),
                getDocumentType(caseEvent)
            )
        );
    }
}
