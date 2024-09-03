package uk.gov.hmcts.reform.civil.service.docmosis.caseprogression;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.documentmanagement.DocumentManagementService;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType;
import uk.gov.hmcts.reform.civil.documentmanagement.model.PDF;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.docmosis.DocmosisDocument;
import uk.gov.hmcts.reform.civil.model.docmosis.casepogression.JudgeFinalOrderForm;
import uk.gov.hmcts.reform.civil.referencedata.model.LocationRefData;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.UserService;
import uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates;
import uk.gov.hmcts.reform.civil.service.docmosis.DocumentGeneratorService;
import uk.gov.hmcts.reform.civil.service.docmosis.DocumentHearingLocationHelper;
import uk.gov.hmcts.reform.civil.service.docmosis.TemplateDataGenerator;
import uk.gov.hmcts.reform.civil.service.referencedata.LocationReferenceDataService;

import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.BLANK_TEMPLATE_AFTER_HEARING_DOCX;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.BLANK_TEMPLATE_BEFORE_HEARING_DOCX;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.FIX_DATE_CCMC_DOCX;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.FIX_DATE_CMC_DOCX;

@Slf4j
@Service
public class JudgeOrderDownloadGenerator extends JudgeFinalOrderGenerator implements TemplateDataGenerator<JudgeFinalOrderForm> {

    private final DocumentManagementService documentManagementService;
    private final DocumentGeneratorService documentGeneratorService;
    private final UserService userService;
    private final LocationReferenceDataService locationRefDataService;
    private final FeatureToggleService featureToggleService;
    private final DocumentHearingLocationHelper documentHearingLocationHelper;
    private LocationRefData caseManagementLocationDetails;
    private static final String DATE_FORMAT = "dd/MM/yyyy";
    private DocmosisTemplates docmosisTemplate;

    public JudgeOrderDownloadGenerator(DocumentManagementService documentManagementService, DocumentGeneratorService documentGeneratorService, UserService userService, LocationReferenceDataService locationRefDataService, FeatureToggleService featureToggleService, DocumentHearingLocationHelper documentHearingLocationHelper) {
        super(documentManagementService, documentGeneratorService, userService, locationRefDataService, featureToggleService, documentHearingLocationHelper);
        this.documentManagementService = documentManagementService;
        this.documentGeneratorService = documentGeneratorService;
        this.userService = userService;
        this.locationRefDataService = locationRefDataService;
        this.featureToggleService = featureToggleService;
        this.documentHearingLocationHelper = documentHearingLocationHelper;
    }

    public CaseDocument generate(CaseData caseData, String authorisation) {
        JudgeFinalOrderForm templateData = getDownloadTemplate(caseData, authorisation);
        DocmosisDocument docmosisDocument =
            documentGeneratorService.generateDocmosisDocument(templateData, docmosisTemplate, "docx");
        return documentManagementService.uploadDocument(
            authorisation,
            new PDF(
                getFileName(docmosisTemplate),
                docmosisDocument.getBytes(),
                DocumentType.JUDGE_FINAL_ORDER
            )
        );
    }

    private String getFileName(DocmosisTemplates docmosisTemplate) {
        return null;
    }

    private JudgeFinalOrderForm getDownloadTemplate(CaseData caseData, String authorisation) {

        switch (caseData.getFinalOrderDownloadTemplateOptions().getValue().getLabel()) {
            case "Blank template to be used after a hearing":
                docmosisTemplate = BLANK_TEMPLATE_AFTER_HEARING_DOCX;
                return getBlankAfterHearing(caseData, authorisation);
            case "Blank template to be used before a hearing/box work":
                docmosisTemplate = BLANK_TEMPLATE_BEFORE_HEARING_DOCX;
                return getBlankBeforeHearing(caseData, authorisation);
            case "Fix a date for a CCMC":
                docmosisTemplate = FIX_DATE_CCMC_DOCX;
                return getFixDateCcmc(caseData, authorisation);
            case "Fix a date for a CMC":
                docmosisTemplate = FIX_DATE_CMC_DOCX;
                return getFixDateCmc(caseData, authorisation);
            default:
                return null;
        }
    }

    private JudgeFinalOrderForm getBlankAfterHearing(CaseData caseData, String authorisation) {
        var blankAfterHearingBuilder = JudgeFinalOrderForm.builder();

        return blankAfterHearingBuilder.build();
    }

    private JudgeFinalOrderForm getBlankBeforeHearing(CaseData caseData, String authorisation) {
        var blankBeforerHearingBuilder = JudgeFinalOrderForm.builder();

        return blankBeforerHearingBuilder.build();
    }

    private JudgeFinalOrderForm getFixDateCcmc(CaseData caseData, String authorisation) {
        var fixdateCcmcBuilder = JudgeFinalOrderForm.builder();

        return fixdateCcmcBuilder.build();
    }

    private JudgeFinalOrderForm getFixDateCmc(CaseData caseData, String authorisation) {
        var fixdateCmcBuilder = JudgeFinalOrderForm.builder();

        return fixdateCmcBuilder.build();
    }

}
