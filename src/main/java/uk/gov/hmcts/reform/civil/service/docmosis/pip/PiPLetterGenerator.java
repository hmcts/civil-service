package uk.gov.hmcts.reform.civil.service.docmosis.pip;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.config.PinInPostConfiguration;
import uk.gov.hmcts.reform.civil.documentmanagement.DocumentManagementService;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType;
import uk.gov.hmcts.reform.civil.documentmanagement.model.PDF;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.docmosis.DocmosisDocument;
import uk.gov.hmcts.reform.civil.model.docmosis.pip.PiPLetter;
import uk.gov.hmcts.reform.civil.model.documents.DocumentMetaData;
import uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates;
import uk.gov.hmcts.reform.civil.service.docmosis.DocumentGeneratorService;
import uk.gov.hmcts.reform.civil.service.docmosis.TemplateDataGenerator;
import uk.gov.hmcts.reform.civil.service.stitching.CivilDocumentStitchingService;
import uk.gov.hmcts.reform.civil.utils.AssignCategoryId;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.PIN_IN_THE_POST_LETTER;

@Slf4j
@RequiredArgsConstructor
@Service
public class PiPLetterGenerator implements TemplateDataGenerator<PiPLetter> {

    private final DocumentGeneratorService documentGeneratorService;
    private final PinInPostConfiguration pipInPostConfiguration;
    private final CivilDocumentStitchingService civilDocumentStitchingService;
    private final DocumentManagementService documentManagementService;
    private final AssignCategoryId assignCategoryId;
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
                DocmosisTemplates.PIN_IN_THE_POST_LETTER.getDocumentTitle(),
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
        assignCategoryId.assignCategoryIdToCaseDocument(stitchedDocument, "detailsOfClaim");
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

        if (Objects.nonNull(caseData.getServedDocumentFiles())) {
            if (Objects.nonNull(caseData.getServedDocumentFiles().getTimelineEventUpload())) {
                log.info("Claim Has TimeLine doc" + caseData.getServedDocumentFiles().getTimelineEventUpload().size());
                documentMetaDataList.addAll(caseData.getServedDocumentFiles().getTimelineEventUpload().stream()
                                                .map(doc -> new DocumentMetaData(
                                                    doc.getValue(),
                                                    "Claim timeline",
                                                    LocalDate.now().toString()
                                                ))
                                                .collect(Collectors.toList()));
            }

            if (Objects.nonNull(caseData.getServedDocumentFiles().getParticularsOfClaimDocument())) {
                log.info("Claim Has Claim details doc" + caseData.getServedDocumentFiles().getParticularsOfClaimDocument().size());
                documentMetaDataList.addAll(caseData.getServedDocumentFiles().getParticularsOfClaimDocument().stream()
                                                .map(doc -> new DocumentMetaData(
                                                    doc.getValue(),
                                                    "Supported docs",
                                                    LocalDate.now().toString()
                                                ))
                                                .collect(Collectors.toList()));
            }
        }

        return documentMetaDataList;
    }
}
