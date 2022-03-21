package uk.gov.hmcts.reform.civil.service.docmosis.aos;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.LitigationFriend;
import uk.gov.hmcts.reform.civil.model.docmosis.DocmosisDocument;
import uk.gov.hmcts.reform.civil.model.docmosis.aos.AcknowledgementOfClaimFormForSpec;
import uk.gov.hmcts.reform.civil.model.docmosis.common.Party;
import uk.gov.hmcts.reform.civil.model.documents.CaseDocument;
import uk.gov.hmcts.reform.civil.model.documents.DocumentType;
import uk.gov.hmcts.reform.civil.model.documents.PDF;
import uk.gov.hmcts.reform.civil.service.docmosis.DocumentGeneratorService;
import uk.gov.hmcts.reform.civil.service.docmosis.RepresentativeService;
import uk.gov.hmcts.reform.civil.service.docmosis.TemplateDataGenerator;
import uk.gov.hmcts.reform.civil.service.documentmanagement.DocumentManagementService;
import uk.gov.hmcts.reform.civil.utils.DocmosisTemplateDataUtils;

import static java.util.Optional.ofNullable;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.N10;

@Service
@RequiredArgsConstructor
public class AcknowledgementOfClaimGeneratorForSpec
    implements TemplateDataGenerator<AcknowledgementOfClaimFormForSpec> {

    private final DocumentManagementService documentManagementService;
    private final DocumentGeneratorService documentGeneratorService;
    private final RepresentativeService representativeService;

    public CaseDocument generate(CaseData caseData, String authorisation) {
        AcknowledgementOfClaimFormForSpec templateData = getTemplateData(caseData);

        DocmosisDocument docmosisDocument = documentGeneratorService.generateDocmosisDocument(templateData, N10);
        return documentManagementService.uploadDocument(
            authorisation,
            new PDF(getFileName(caseData), docmosisDocument.getBytes(), DocumentType.ACKNOWLEDGEMENT_OF_SERVICE)
        );
    }

    private String getFileName(CaseData caseData) {
        return String.format(N10.getDocumentTitle(), caseData.getLegacyCaseReference());
    }

    @Override
    public AcknowledgementOfClaimFormForSpec getTemplateData(CaseData caseData) {
        return AcknowledgementOfClaimFormForSpec.builder()
            .caseName(DocmosisTemplateDataUtils.toCaseName.apply(caseData))
            .referenceNumber(caseData.getLegacyCaseReference())
            .solicitorReferences(DocmosisTemplateDataUtils.fetchSolicitorReferences(caseData))
            .issueDate(caseData.getIssueDate())
            .responseDeadline(caseData.getRespondent1ResponseDeadline().toLocalDate())
            .submittedOn(caseData.getSubmittedDate().toLocalDate())
            .respondent(prepareRespondent(caseData))
            .build();
    }

    private Party prepareRespondent(CaseData caseData) {
        var respondent = caseData.getRespondent1();
        return Party.builder()
            .name(respondent.getPartyName())
            .primaryAddress(respondent.getPrimaryAddress())
            .representative(representativeService.getRespondent1Representative(caseData))
            .litigationFriendName(
                ofNullable(caseData.getRespondent1LitigationFriend())
                    .map(LitigationFriend::getFullName)
                    .orElse(""))
            .build();
    }
}
