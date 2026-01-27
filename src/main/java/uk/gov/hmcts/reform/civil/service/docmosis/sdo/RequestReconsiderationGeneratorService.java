package uk.gov.hmcts.reform.civil.service.docmosis.sdo;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.documentmanagement.DocumentManagementService;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType;
import uk.gov.hmcts.reform.civil.documentmanagement.model.PDF;
import uk.gov.hmcts.reform.civil.helpers.sdo.SdoHelper;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.common.MappableObject;
import uk.gov.hmcts.reform.civil.model.docmosis.DocmosisDocument;
import uk.gov.hmcts.reform.civil.model.docmosis.sdo.DesicionOnReconsiderationDocumentForm;
import uk.gov.hmcts.reform.civil.service.UserService;
import uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates;
import uk.gov.hmcts.reform.civil.service.docmosis.DocumentGeneratorService;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class RequestReconsiderationGeneratorService {

    private final DocumentGeneratorService documentGeneratorService;
    private final DocumentManagementService documentManagementService;
    private final UserService userService;

    public CaseDocument generate(CaseData caseData, String authorisation) {
        MappableObject templateData;
        DocmosisTemplates docmosisTemplate;

        UserDetails userDetails = userService.getUserDetails(authorisation);
        String judgeName = userDetails.getFullName();

        boolean isJudge = false;

        if (userDetails.getRoles() != null) {
            isJudge = userDetails.getRoles().stream()
                .anyMatch(s -> s != null && s.toLowerCase().contains("judge"));
        }

        docmosisTemplate = DocmosisTemplates.RECONSIDERATION_UPHELD_DECISION_OUTPUT_PDF;
        templateData = getTemplateData(caseData, judgeName, isJudge);

        DocmosisDocument docmosisDocument = documentGeneratorService.generateDocmosisDocument(
            templateData,
            docmosisTemplate
        );

        return documentManagementService.uploadDocument(
            authorisation,
            new PDF(
                getFileName(docmosisTemplate, caseData),
                docmosisDocument.getBytes(),
                DocumentType.DECISION_MADE_ON_APPLICATIONS
            )
        );
    }

    private String getFileName(DocmosisTemplates docmosisTemplate, CaseData caseData) {
        return String.format(docmosisTemplate.getDocumentTitle(), caseData.getLegacyCaseReference());
    }

    private DesicionOnReconsiderationDocumentForm getTemplateData(CaseData caseData, String judgeName, boolean isJudge) {

        return new DesicionOnReconsiderationDocumentForm()
        .setWrittenByJudge(isJudge)
        .setCurrentDate(LocalDate.now())
        .setJudgeName(judgeName)
        .setCaseNumber(caseData.getLegacyCaseReference())
        .setApplicant1(caseData.getApplicant1())
        .setHasApplicant2(
            SdoHelper.hasSharedVariable(caseData, "applicant2")
        )
        .setApplicant2(caseData.getApplicant2())
        .setRespondent1(caseData.getRespondent1())
        .setHasRespondent2(
            SdoHelper.hasSharedVariable(caseData, "respondent2")
        )
        .setRespondent2(caseData.getRespondent2())
        .setUpholdingPreviousOrderReason(caseData.getUpholdingPreviousOrderReason());
    }

}
