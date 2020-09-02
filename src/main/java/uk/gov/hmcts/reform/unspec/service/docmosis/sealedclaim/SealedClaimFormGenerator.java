package uk.gov.hmcts.reform.unspec.service.docmosis.sealedclaim;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.unspec.model.Address;
import uk.gov.hmcts.reform.unspec.model.CaseData;
import uk.gov.hmcts.reform.unspec.model.Party;
import uk.gov.hmcts.reform.unspec.model.docmosis.DocmosisDocument;
import uk.gov.hmcts.reform.unspec.model.docmosis.sealedclaim.Claimant;
import uk.gov.hmcts.reform.unspec.model.docmosis.sealedclaim.Defendant;
import uk.gov.hmcts.reform.unspec.model.docmosis.sealedclaim.Representative;
import uk.gov.hmcts.reform.unspec.model.docmosis.sealedclaim.SealedClaimForm;
import uk.gov.hmcts.reform.unspec.model.documents.CaseDocument;
import uk.gov.hmcts.reform.unspec.model.documents.DocumentType;
import uk.gov.hmcts.reform.unspec.model.documents.PDF;
import uk.gov.hmcts.reform.unspec.service.docmosis.DocumentGeneratorService;
import uk.gov.hmcts.reform.unspec.service.docmosis.TemplateDataGenerator;
import uk.gov.hmcts.reform.unspec.service.documentmanagement.DocumentManagementService;
import uk.gov.hmcts.reform.unspec.utils.CaseNameUtils;

import java.util.List;

import static uk.gov.hmcts.reform.unspec.service.docmosis.DocmosisTemplates.N1;

@Service
@RequiredArgsConstructor
public class SealedClaimFormGenerator implements TemplateDataGenerator<SealedClaimForm> {

    public static final String TEMP_CLAIM_DETAILS = "The claimant seeks compensation from injuries and losses arising"
        + " from a road traffic accident which occurred on 1st July 2017 as a result of the negligence of the first "
        + "defendant.The claimant seeks compensation from injuries and losses arising from a road traffic accident "
        + "which occurred on 1st July 2017 as a result of the negligence of the first defendant.";
    //TODO this need ui implementation to capture claim details

    private static final Representative TEMP_REPRESENTATIVE = Representative.builder()
        .contactName("MiguelSpooner")
        .dxAddress("DX 751Newport")
        .organisationName("DBE Law")
        .phoneNumber("0800 206 1592")
        .emailAddress("jim.smith@slatergordon.com")
        .serviceAddress(Address.builder()
                            .addressLine1("AdmiralHouse")
                            .addressLine2("Queensway")
                            .postTown("Newport")
                            .postCode("NP204AG")
                            .build())
        .build(); //TODO Rep details need to be picked from reference data
    public static final String REFERENCE_NUMBER = "000LR095"; //TODO Need to agree a way to get
    private final DocumentManagementService documentManagementService;
    private final DocumentGeneratorService documentGeneratorService;

    public CaseDocument generate(CaseData caseData, String authorisation) {
        SealedClaimForm templateData = getTemplateData(caseData);

        DocmosisDocument docmosisDocument = documentGeneratorService.generateDocmosisDocument(templateData, N1);
        return documentManagementService.uploadDocument(
            authorisation,
            new PDF(getFileName(caseData), docmosisDocument.getBytes(), DocumentType.SEALED_CLAIM)
        );
    }

    private String getFileName(CaseData caseData) {
        return String.format(N1.getDocumentTitle(), caseData.getLegacyCaseReference());
    }

    @Override
    public SealedClaimForm getTemplateData(CaseData caseData) {
        return SealedClaimForm.builder()
            .claimants(getClaimants(caseData))
            .defendants(geDefendants(caseData))
            .claimValue(caseData.getClaimValue().formData())
            .statementOfTruth(caseData.getApplicantSolicitor1ClaimStatementOfTruth())
            .claimDetails(TEMP_CLAIM_DETAILS)
            .hearingCourtLocation(caseData.getCourtLocation().getApplicantPreferredCourt())
            .claimantRepresentative(TEMP_REPRESENTATIVE)
            .referenceNumber(REFERENCE_NUMBER)
            .issueDate(caseData.getClaimIssuedDate())
            .submittedOn(caseData.getClaimSubmittedDateTime().toLocalDate())
            .claimantExternalReference(caseData.getSolicitorReferences().getApplicantSolicitor1Reference())
            .defendantExternalReference(caseData.getSolicitorReferences().getRespondentSolicitor1Reference())
            .caseName(CaseNameUtils.toCaseName.apply(caseData))
            .build();
    }

    private List<Defendant> geDefendants(CaseData caseData) {
        Party respondent = caseData.getRespondent1();
        return List.of(Defendant.builder()
                           .name(respondent.getPartyName())
                           .primaryAddress(respondent.getPrimaryAddress())
                           .representative(TEMP_REPRESENTATIVE)
                           .build());
    }

    private List<Claimant> getClaimants(CaseData caseData) {
        Party applicant = caseData.getApplicant1();
        return List.of(Claimant.builder()
                           .name(applicant.getPartyName())
                           .primaryAddress(applicant.getPrimaryAddress())
                           .build());
    }
}
