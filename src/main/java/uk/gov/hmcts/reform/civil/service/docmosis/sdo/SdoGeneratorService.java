package uk.gov.hmcts.reform.civil.service.docmosis.sdo;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.documentmanagement.DocumentManagementService;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType;
import uk.gov.hmcts.reform.civil.documentmanagement.model.PDF;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.common.MappableObject;
import uk.gov.hmcts.reform.civil.model.docmosis.DocmosisDocument;
import uk.gov.hmcts.reform.civil.service.UserService;
import uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates;
import uk.gov.hmcts.reform.civil.service.docmosis.DocumentGeneratorService;
import uk.gov.hmcts.reform.civil.service.sdo.SdoCaseClassificationService;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import java.time.LocalDate;

@Slf4j
@Service
@RequiredArgsConstructor
public class SdoGeneratorService {

    private final DocumentGeneratorService documentGeneratorService;
    private final DocumentManagementService documentManagementService;
    private final UserService userService;
    private final SdoCaseClassificationService sdoCaseClassificationService;
    private final SdoFastTrackTemplateService sdoFastTrackTemplateService;
    private final SdoNihlTemplateService sdoNihlTemplateService;
    private final SdoDisposalTemplateService sdoDisposalTemplateService;
    private final SdoSmallClaimsTemplateService sdoSmallClaimsTemplateService;
    private final SdoSmallClaimsDrhTemplateService sdoSmallClaimsDrhTemplateService;

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

        log.info("Selecting SDO template for caseId {} (track: {}, drhSmallClaim: {}, nihlFastTrack: {}, fastTrack: {})",
                 caseData.getCcdCaseReference(),
                 caseData.getAllocatedTrack(),
                 sdoCaseClassificationService.isDrhSmallClaim(caseData),
                 sdoCaseClassificationService.isNihlFastTrack(caseData),
                 sdoCaseClassificationService.isFastTrack(caseData));

        if (sdoCaseClassificationService.isDrhSmallClaim(caseData)) {
            docmosisTemplate = DocmosisTemplates.SDO_SMALL_DRH;
            templateData = sdoSmallClaimsDrhTemplateService.buildTemplate(caseData, judgeName, isJudge, authorisation);
        } else if (sdoCaseClassificationService.isSmallClaimsTrack(caseData)) {
            docmosisTemplate = DocmosisTemplates.SDO_SMALL_R2;
            templateData = sdoSmallClaimsTemplateService.buildTemplate(caseData, judgeName, isJudge, authorisation);
        } else if (sdoCaseClassificationService.isNihlFastTrack(caseData)) {
            docmosisTemplate = DocmosisTemplates.SDO_FAST_TRACK_NIHL;
            templateData = sdoNihlTemplateService.buildTemplate(caseData, judgeName, isJudge, authorisation);
        } else if (sdoCaseClassificationService.isFastTrack(caseData)) {
            docmosisTemplate = DocmosisTemplates.SDO_FAST_FAST_TRACK_INT_R2;
            templateData = sdoFastTrackTemplateService.buildTemplate(caseData, judgeName, isJudge, authorisation);
        } else {
            docmosisTemplate = DocmosisTemplates.SDO_R2_DISPOSAL;
            templateData = sdoDisposalTemplateService.buildTemplate(caseData, judgeName, isJudge, authorisation);
        }

        log.info("SDO docmosisTemplate: {} for caseId {} legacyCaseReference{}",
                 docmosisTemplate.getTemplate(), caseData.getCcdCaseReference(), caseData.getLegacyCaseReference());

        DocmosisDocument docmosisDocument = documentGeneratorService.generateDocmosisDocument(
            templateData,
            docmosisTemplate
        );

        return documentManagementService.uploadDocument(
            authorisation,
            new PDF(
                getFileName(judgeName),
                docmosisDocument.getBytes(),
                DocumentType.SDO_ORDER
            )
        );
    }

    private String getFileName(String judgeName) {

        return LocalDate.now() + "_" + judgeName + ".pdf";
    }

}
