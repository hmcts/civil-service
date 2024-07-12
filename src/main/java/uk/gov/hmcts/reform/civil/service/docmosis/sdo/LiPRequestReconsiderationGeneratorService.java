package uk.gov.hmcts.reform.civil.service.docmosis.sdo;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.documentmanagement.DocumentManagementService;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType;
import uk.gov.hmcts.reform.civil.documentmanagement.model.PDF;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.common.MappableObject;
import uk.gov.hmcts.reform.civil.model.docmosis.DocmosisDocument;
import uk.gov.hmcts.reform.civil.model.docmosis.sdo.LiPRequestForReconsiderationForm;
import uk.gov.hmcts.reform.civil.referencedata.model.LocationRefData;
import uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates;
import uk.gov.hmcts.reform.civil.service.docmosis.DocumentGeneratorService;
import uk.gov.hmcts.reform.civil.service.docmosis.DocumentHearingLocationHelper;

import java.time.LocalDate;

import static java.util.Objects.nonNull;

@Service
@RequiredArgsConstructor
public class LiPRequestReconsiderationGeneratorService {

    private final DocumentGeneratorService documentGeneratorService;
    private final DocumentManagementService documentManagementService;
    private final DocumentHearingLocationHelper locationHelper;

    public CaseDocument generateLiPDocument(CaseData caseData, String authorisation, boolean isApplicant) {
        MappableObject templateData;
        DocmosisTemplates docmosisTemplate;

        docmosisTemplate = DocmosisTemplates.REQUEST_FOR_RECONSIDERATION;
        templateData = getTemplateData(caseData, authorisation, isApplicant);

        DocmosisDocument docmosisDocument = documentGeneratorService.generateDocmosisDocument(
            templateData,
            docmosisTemplate
        );

        return documentManagementService.uploadDocument(
            authorisation,
            new PDF(
                getFileName(docmosisTemplate, isApplicant),
                docmosisDocument.getBytes(),
                DocumentType.REQUEST_FOR_RECONSIDERATION
            )
        );
    }

    private String getFileName(DocmosisTemplates docmosisTemplate, boolean isApplicant) {
        return String.format(docmosisTemplate.getDocumentTitle(), isApplicant ? "claimant" : "defendant");
    }

    private LiPRequestForReconsiderationForm getTemplateData(CaseData caseData, String authorisation, boolean isApplicant) {

        LocationRefData locationData = locationHelper.getHearingLocation(null, caseData, authorisation);
        String venueName = nonNull(locationData) ? locationData.getVenueName() : "Online Civil Claims";

        LiPRequestForReconsiderationForm.LiPRequestForReconsiderationFormBuilder
            liPRequestForReconsiderationFormBuilder = LiPRequestForReconsiderationForm.builder()
            .currentDate(LocalDate.now())
            .caseNumber(caseData.getLegacyCaseReference())
            .countyCourt(venueName)
            .partyName(isApplicant
                           ? caseData.getApplicant1().getPartyName()
                           : caseData.getRespondent1().getPartyName())
            .partyAddress(isApplicant
                              ? caseData.getApplicant1().getPrimaryAddress()
                              : caseData.getRespondent1().getPrimaryAddress())
            .requestReason(getReasonForReconsiderationClaimant(caseData, isApplicant));

        return liPRequestForReconsiderationFormBuilder
            .build();
    }

    private String getReasonForReconsiderationClaimant(CaseData caseData, boolean isApplicant) {
        if (isApplicant) {
            if (nonNull(caseData.getReasonForReconsiderationApplicant())) {
                return caseData.getReasonForReconsiderationApplicant().getReasonForReconsiderationTxt();
            }
            return caseData.getCaseDataLiP().getRequestForReviewCommentsClaimant();
        } else {
            if (nonNull(caseData.getReasonForReconsiderationRespondent1())) {
                return caseData.getReasonForReconsiderationRespondent1().getReasonForReconsiderationTxt();
            }
            return caseData.getCaseDataLiP().getRequestForReviewCommentsDefendant();
        }
    }
}
