package uk.gov.hmcts.reform.civil.service.docmosis.dj;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.documentmanagement.DocumentManagementService;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType;
import uk.gov.hmcts.reform.civil.documentmanagement.model.PDF;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.docmosis.DocmosisDocument;
import uk.gov.hmcts.reform.civil.model.docmosis.dj.DefaultJudgmentSDOOrderForm;
import uk.gov.hmcts.reform.civil.service.UserService;
import uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates;
import uk.gov.hmcts.reform.civil.service.docmosis.DocumentGeneratorService;
import uk.gov.hmcts.reform.civil.service.docmosis.TemplateDataGenerator;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import java.io.IOException;
import java.time.LocalDate;

import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.DJ_SDO_R2_DISPOSAL;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.DJ_SDO_R2_TRIAL;

@Service
@RequiredArgsConstructor
public class DefaultJudgmentOrderFormGenerator implements TemplateDataGenerator<DefaultJudgmentSDOOrderForm> {

    private final DocumentManagementService documentManagementService;
    private final DocumentGeneratorService documentGeneratorService;
    private final UserService userService;
    private final DjDisposalTemplateService djDisposalTemplateService;
    private final DjTrialTemplateService djTrialTemplateService;
    public static final String DISPOSAL_HEARING = "DISPOSAL_HEARING";

    public CaseDocument generate(CaseData caseData, String authorisation) {
        DefaultJudgmentSDOOrderForm templateData = getDefaultJudgmentForms(caseData, authorisation);
        DocmosisTemplates docmosisTemplate = caseData.getCaseManagementOrderSelection().equals(DISPOSAL_HEARING)
            ? getDocmosisTemplate()
            : getDocmosisTemplateTrial();
        DocmosisDocument docmosisDocument =
            documentGeneratorService.generateDocmosisDocument(templateData, docmosisTemplate);

        UserDetails userDetails = userService.getUserDetails(authorisation);
        String judgeName = userDetails.getFullName();

        return documentManagementService.uploadDocument(
            authorisation,
            new PDF(
                getFileName(judgeName),
                docmosisDocument.getBytes(),
                DocumentType.DEFAULT_JUDGMENT_SDO_ORDER
            )
        );
    }

    @Override
    public DefaultJudgmentSDOOrderForm getTemplateData(CaseData caseData) throws IOException {
        return null;
    }

    private String getFileName(String judgeName) {
        return LocalDate.now() + "_" + judgeName + ".pdf";
    }

    private DefaultJudgmentSDOOrderForm getDefaultJudgmentForms(CaseData caseData, String authorisation) {
        return caseData.getCaseManagementOrderSelection().equals(DISPOSAL_HEARING)
            ? djDisposalTemplateService.buildTemplate(caseData, authorisation)
            : djTrialTemplateService.buildTemplate(caseData, authorisation);
    }

    private DocmosisTemplates getDocmosisTemplate() {
        return DJ_SDO_R2_DISPOSAL;
    }

    private DocmosisTemplates getDocmosisTemplateTrial() {
        return DJ_SDO_R2_TRIAL;
    }

}
