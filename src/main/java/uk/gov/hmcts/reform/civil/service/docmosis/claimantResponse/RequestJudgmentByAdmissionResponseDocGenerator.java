package uk.gov.hmcts.reform.civil.service.docmosis.claimantResponse;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.documentmanagement.DocumentManagementService;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.documentmanagement.model.PDF;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.docmosis.DocmosisDocument;
import uk.gov.hmcts.reform.civil.model.docmosis.claimantresponse.JudgmentByAdmission;
import uk.gov.hmcts.reform.civil.model.docmosis.claimantresponse.JudgmentByAdmissionMapper;
import uk.gov.hmcts.reform.civil.service.docmosis.DocumentGeneratorService;
import uk.gov.hmcts.reform.civil.service.docmosis.TemplateDataGenerator;

import static uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType.CCJ_REQUEST_ADMISSION;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.JUDGMENT_BY_ADMISSION;

@Service
@Getter
@RequiredArgsConstructor
public class RequestJudgmentByAdmissionResponseDocGenerator implements TemplateDataGenerator<JudgmentByAdmission> {

    private final JudgmentByAdmissionMapper judgmentByAdmissionMapper;
    private final DocumentManagementService documentManagementService;
    private final DocumentGeneratorService documentGeneratorService;

    public CaseDocument generate(CaseData caseData, String authorisation) {

        DocmosisDocument docmosisDocument = documentGeneratorService.generateDocmosisDocument(
            getTemplateData(caseData),
            JUDGMENT_BY_ADMISSION
        );
        return documentManagementService.uploadDocument(
            authorisation,
            new PDF(String.format(JUDGMENT_BY_ADMISSION.getDocumentTitle(), caseData.getLegacyCaseReference()),
                    docmosisDocument.getBytes(),
                    CCJ_REQUEST_ADMISSION
            )
        );
    }

    @Override
    public JudgmentByAdmission getTemplateData(CaseData caseData) {
        return judgmentByAdmissionMapper.toClaimantResponseForm(caseData);
    }
}
