package uk.gov.hmcts.reform.civil.service.docmosis.dj;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.documentmanagement.DocumentDownloadException;
import uk.gov.hmcts.reform.civil.documentmanagement.DocumentManagementService;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.documentmanagement.model.PDF;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.LitigationFriend;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.docmosis.DocmosisDocument;
import uk.gov.hmcts.reform.civil.model.docmosis.judgmentonline.JudgementCoverLetter;
import uk.gov.hmcts.reform.civil.model.documents.DocumentMetaData;
import uk.gov.hmcts.reform.civil.service.docmosis.DocumentGeneratorService;
import uk.gov.hmcts.reform.civil.service.documentmanagement.DocumentDownloadService;
import uk.gov.hmcts.reform.civil.stitch.service.CivilStitchService;
import uk.gov.hmcts.reform.civil.utils.PartyUtils;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType.COVER_LETTER;
import static uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType.DEFAULT_JUDGMENT_CLAIMANT1;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.DEFAULT_JUDGMENT_COVER_LETTER;

@Slf4j
@RequiredArgsConstructor
@Service
public class CoverLetterService {

    private final DocumentGeneratorService documentGeneratorService;
    private final DocumentManagementService documentManagementService;
    private final DocumentDownloadService documentDownloadService;
    private final CivilStitchService civilStitchService;

    public CaseDocument generateDocumentWithCoverLetter(Party party,
                                                        LitigationFriend litigationFriend,
                                                        CaseData caseData, List<DocumentMetaData> documentsToAttach,
                                                        String documentName, String auth) {
        CaseDocument coverLetter = generateDefaultJudgementCoverLetter(party, litigationFriend, caseData, auth);

        List<DocumentMetaData> allDocuments = Stream.concat(
            Stream.of(new DocumentMetaData(
                coverLetter.getDocumentLink(),
                "Cover letter",
                LocalDate.now().toString()
            )),
            documentsToAttach.stream()
        ).collect(Collectors.toList());

        return civilStitchService.generateStitchedCaseDocument(
            allDocuments,
            documentName,
            caseData.getCcdCaseReference(),
            DEFAULT_JUDGMENT_CLAIMANT1,
            auth
        );
    }

    public byte[] generateDocumentWithCoverLetterBinary(Party party,
                                                        LitigationFriend litigationFriend,
                                                        CaseData caseData,
                                                        List<DocumentMetaData> documentsToAttach,
                                                        String documentName,
                                                        String auth) {
        CaseDocument documentWithCoverLetter = generateDocumentWithCoverLetter(
            party,
            litigationFriend,
            caseData,
            documentsToAttach,
            documentName,
            auth
        );
        String documentUrl = documentWithCoverLetter.getDocumentLink().getDocumentUrl();
        String documentId = documentUrl.substring(documentUrl.lastIndexOf("/") + 1);

        try {
            return documentDownloadService.downloadDocument(
                auth,
                documentId
            ).file().getInputStream().readAllBytes();
        } catch (IOException e) {
            log.error("Failed getting letter content");
            throw new DocumentDownloadException(documentWithCoverLetter.getDocumentLink().getDocumentFileName(), e);
        }
    }

    public CaseDocument generateDefaultJudgementCoverLetter(Party party, LitigationFriend litigationFriend, CaseData caseData, String authorisation) {
        DocmosisDocument coverLetter = generate(party, litigationFriend, caseData);
        return documentManagementService.uploadDocument(
            authorisation,
            new PDF(
                DEFAULT_JUDGMENT_COVER_LETTER.getDocumentTitle(),
                coverLetter.getBytes(),
                COVER_LETTER
            )
        );
    }

    private DocmosisDocument generate(Party party, LitigationFriend litigationFriend, CaseData caseData) {
        return documentGeneratorService.generateDocmosisDocument(
            buildTemplateData(party, litigationFriend, caseData),
            DEFAULT_JUDGMENT_COVER_LETTER
        );
    }

    public JudgementCoverLetter buildTemplateData(Party party, LitigationFriend litigationFriend, CaseData caseData) {
        return new JudgementCoverLetter()
            .setCcdCaseReference(String.valueOf(caseData.getCcdCaseReference()))
            .setClaimNumber(caseData.getLegacyCaseReference())
            .setAddress(party.getPrimaryAddress())
            .setPartyName(PartyUtils.getPartyNameWithLitigiousFriend(party, litigationFriend));
    }
}
