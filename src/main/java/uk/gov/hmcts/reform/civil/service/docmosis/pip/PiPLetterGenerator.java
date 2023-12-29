package uk.gov.hmcts.reform.civil.service.docmosis.pip;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.config.PinInPostConfiguration;
import uk.gov.hmcts.reform.civil.documentmanagement.DocumentManagementService;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType;
import uk.gov.hmcts.reform.civil.documentmanagement.model.PDF;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.docmosis.DocmosisDocument;
import uk.gov.hmcts.reform.civil.model.docmosis.pip.PiPLetter;
import uk.gov.hmcts.reform.civil.model.documents.DocumentMetaData;
import uk.gov.hmcts.reform.civil.service.docmosis.DocumentGeneratorService;
import uk.gov.hmcts.reform.civil.service.docmosis.TemplateDataGenerator;
import uk.gov.hmcts.reform.civil.service.docmosis.sealedclaim.LitigantInPersonFormGenerator;
import uk.gov.hmcts.reform.civil.service.stitching.CivilDocumentStitchingService;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.PIN_IN_THE_POST_LETTER;

@RequiredArgsConstructor
@Service
public class PiPLetterGenerator implements TemplateDataGenerator<PiPLetter> {

    private final DocumentGeneratorService documentGeneratorService;
    private final PinInPostConfiguration pipInPostConfiguration;
    private final CivilDocumentStitchingService civilDocumentStitchingService;
    private final LitigantInPersonFormGenerator litigantInPersonFormGenerator;
    private final DocumentManagementService documentManagementService;
    @Value("${stitching.enabled}")
    private boolean stitchEnabled;

    private DocmosisDocument generate(CaseData caseData) {
        return documentGeneratorService.generateDocmosisDocument(
            getTemplateData(caseData),
            PIN_IN_THE_POST_LETTER
        );
    }

    public CaseDocument downloadLetter(CaseData caseData, String authorisation) {
        DocmosisDocument pipLetter = generate(caseData);
        CaseDocument pipLetterCaseDocument =  documentManagementService.uploadDocument(
            authorisation,
            new PDF(
                "pinInPostLetteer",
                pipLetter.getBytes(),
                DocumentType.ACKNOWLEDGEMENT_OF_CLAIM
            )
        );
        List<DocumentMetaData> documentMetaDataList = fetchDocumentsFromCaseData(caseData, pipLetterCaseDocument, authorisation);
        CaseDocument stitchedDocument = civilDocumentStitchingService.bundle(
            documentMetaDataList,
            authorisation,
            pipLetterCaseDocument.getDocumentName(),
            pipLetterCaseDocument.getDocumentName(),
            caseData
        );
        return stitchedDocument;
    }

    @Override
    public PiPLetter getTemplateData(CaseData caseData) {
        return PiPLetter
            .builder()
            .pin(caseData.getRespondent1PinToPostLRspec().getAccessCode())
            .claimReferenceNumber(caseData.getLegacyCaseReference())
            .claimantName(caseData.getApplicant1().getPartyName())
            .defendant(caseData.getRespondent1())
            .responseDeadline(caseData.getRespondent1ResponseDeadline().toLocalDate())
            .totalAmountOfClaim(caseData.getTotalClaimAmount())
            .respondToClaimUrl(pipInPostConfiguration.getRespondToClaimUrl())
            .issueDate(LocalDate.now())
            .build();
    }

    private List<DocumentMetaData> fetchDocumentsFromCaseData(CaseData caseData, CaseDocument caseDocument, String authorisation) {
        List<DocumentMetaData> documentMetaDataList = new ArrayList<>();

        documentMetaDataList.add(new DocumentMetaData(caseDocument.getDocumentLink(),
                                                      "PiP Letter",
                                                      LocalDate.now().toString()));

        Optional<Element<CaseDocument>> optionalSealedDocument = caseData.getSystemGeneratedCaseDocuments().stream()
            .filter(systemGeneratedCaseDocument -> systemGeneratedCaseDocument.getValue()
                .getDocumentType().equals(DocumentType.SEALED_CLAIM)).findAny();

        if (optionalSealedDocument.isPresent()) {
            documentMetaDataList.add(new DocumentMetaData(optionalSealedDocument.get().getValue().getDocumentLink(),
                                                          "Sealed Claim form",
                                                          LocalDate.now().toString()));
        }

        if (stitchEnabled) {
            if (YesOrNo.NO.equals(caseData.getRespondent1Represented())) {

                CaseDocument lipForm = litigantInPersonFormGenerator.generate(
                    caseData,
                    authorisation
                );

                documentMetaDataList.add(new DocumentMetaData(
                    lipForm.getDocumentLink(),
                    "Litigant in person claim form",
                    LocalDate.now().toString()
                ));

            }
        }

        if (caseData.getSpecClaimTemplateDocumentFiles() != null) {
            documentMetaDataList.add(new DocumentMetaData(
                caseData.getSpecClaimTemplateDocumentFiles(),
                "Claim timeline",
                LocalDate.now().toString()
            ));
        }
        if (caseData.getSpecClaimDetailsDocumentFiles() != null) {
            documentMetaDataList.add(new DocumentMetaData(
                caseData.getSpecClaimDetailsDocumentFiles(),
                "Supported docs",
                LocalDate.now().toString()
            ));
        }

        return documentMetaDataList;
    }
}
