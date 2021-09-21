package uk.gov.hmcts.reform.civil.service.docmosis.sealedclaim;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.enums.MultiPartyScenario;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.LitigationFriend;
import uk.gov.hmcts.reform.civil.model.SolicitorReferences;
import uk.gov.hmcts.reform.civil.model.docmosis.DocmosisDocument;
import uk.gov.hmcts.reform.civil.model.docmosis.common.Party;
import uk.gov.hmcts.reform.civil.model.docmosis.sealedclaim.SealedClaimForm;
import uk.gov.hmcts.reform.civil.model.documents.CaseDocument;
import uk.gov.hmcts.reform.civil.model.documents.DocumentType;
import uk.gov.hmcts.reform.civil.model.documents.PDF;
import uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates;
import uk.gov.hmcts.reform.civil.service.docmosis.DocumentGeneratorService;
import uk.gov.hmcts.reform.civil.service.docmosis.RepresentativeService;
import uk.gov.hmcts.reform.civil.service.docmosis.TemplateDataGenerator;
import uk.gov.hmcts.reform.civil.service.documentmanagement.DocumentManagementService;
import uk.gov.hmcts.reform.civil.utils.DocmosisTemplateDataUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static java.util.Optional.ofNullable;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.ONE_V_TWO_ONE_LEGAL_REP;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.ONE_V_TWO_TWO_LEGAL_REP;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.TWO_V_ONE;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.getMultiPartyScenario;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.N1;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.N1_MULTIPARTY_SAME_SOL;

@Service
@RequiredArgsConstructor
public class SealedClaimFormGenerator implements TemplateDataGenerator<SealedClaimForm> {

    private final DocumentManagementService documentManagementService;
    private final DocumentGeneratorService documentGeneratorService;
    private final RepresentativeService representativeService;

    public CaseDocument generate(CaseData caseData, String authorisation) {
        SealedClaimForm templateData = getTemplateData(caseData);

        DocmosisTemplates docmosisTemplate = getDocmosisTemplate(caseData);

        DocmosisDocument docmosisDocument = documentGeneratorService.generateDocmosisDocument(
            templateData,
            docmosisTemplate
        );

        return documentManagementService.uploadDocument(
            authorisation,
            new PDF(getFileName(docmosisTemplate, caseData), docmosisDocument.getBytes(), DocumentType.SEALED_CLAIM)
        );
    }

    private String getFileName(DocmosisTemplates docmosisTemplate, CaseData caseData) {
        return String.format(docmosisTemplate.getDocumentTitle(), caseData.getLegacyCaseReference());
    }

    @Override
    public SealedClaimForm getTemplateData(CaseData caseData) {
        Optional<SolicitorReferences> solicitorReferences = ofNullable(caseData.getSolicitorReferences());
        MultiPartyScenario multiPartyScenario = getMultiPartyScenario(caseData);

        SealedClaimForm.SealedClaimFormBuilder sealedClaimFormBuilder = SealedClaimForm.builder()
            .applicants(getApplicants(caseData, multiPartyScenario))
            .respondents(getRespondents(caseData, multiPartyScenario))
            .claimValue(caseData.getClaimValue().formData())
            .statementOfTruth(caseData.getApplicantSolicitor1ClaimStatementOfTruth())
            .claimDetails(caseData.getDetailsOfClaim())
            .hearingCourtLocation(caseData.getCourtLocation().getApplicantPreferredCourt())
            .referenceNumber(caseData.getLegacyCaseReference())
            .issueDate(caseData.getIssueDate())
            .submittedOn(caseData.getSubmittedDate().toLocalDate())
            .applicantExternalReference(solicitorReferences
                                            .map(SolicitorReferences::getApplicantSolicitor1Reference)
                                            .orElse(""))
            .respondent1ExternalReference(solicitorReferences
                                              .map(SolicitorReferences::getRespondentSolicitor1Reference)
                                              .orElse(""))
            .caseName(DocmosisTemplateDataUtils.toCaseName.apply(caseData))
            .courtFee(caseData.getClaimFee().formData());

        if (multiPartyScenario == ONE_V_TWO_TWO_LEGAL_REP) {
            sealedClaimFormBuilder.respondent2ExternalReference(
                solicitorReferences.map(SolicitorReferences::getRespondentSolicitor2Reference).orElse(""));
        }

        return sealedClaimFormBuilder.build();
    }

    private DocmosisTemplates getDocmosisTemplate(CaseData caseData) {
        switch (getMultiPartyScenario(caseData)) {
            case ONE_V_ONE:
            case ONE_V_TWO_TWO_LEGAL_REP:
                return N1;
            case TWO_V_ONE:
            case ONE_V_TWO_ONE_LEGAL_REP:
                return N1_MULTIPARTY_SAME_SOL;
            default:
                throw new IllegalArgumentException("Multiparty scenario doesn't exist");
        }
    }

    private List<Party> getRespondents(CaseData caseData, MultiPartyScenario multiPartyScenario) {
        var respondent = caseData.getRespondent1();
        var respondent1Representative = representativeService.getRespondent1Representative(caseData);
        var respondentParties = new ArrayList<>(List.of(
            Party.builder()
                .name(respondent.getPartyName())
                .primaryAddress(respondent.getPrimaryAddress())
                .representative(respondent1Representative)
                .build()));

        if (multiPartyScenario == ONE_V_TWO_ONE_LEGAL_REP) {
            var respondent2 = caseData.getRespondent2();
            respondentParties.add(Party.builder()
                                      .name(respondent2.getPartyName())
                                      .primaryAddress(respondent2.getPrimaryAddress())
                                      .representative(respondent1Representative)
                                      .build());
        } else if (multiPartyScenario == ONE_V_TWO_TWO_LEGAL_REP) {
            var respondent2 = caseData.getRespondent2();
            respondentParties.add(Party.builder()
                                      .name(respondent2.getPartyName())
                                      .primaryAddress(respondent2.getPrimaryAddress())
                                      .representative(representativeService.getRespondent2Representative(caseData))
                                      .build());
        }

        return respondentParties;
    }

    private List<Party> getApplicants(CaseData caseData, MultiPartyScenario multiPartyScenario) {
        var applicant = caseData.getApplicant1();
        var applicantRepresentative = representativeService.getApplicantRepresentative(caseData);
        var applicantParties = new ArrayList<>(List.of(
            Party.builder()
                .name(applicant.getPartyName())
                .primaryAddress(applicant.getPrimaryAddress())
                .litigationFriendName(
                    ofNullable(caseData.getApplicant1LitigationFriend())
                        .map(LitigationFriend::getFullName)
                        .orElse(""))
                .representative(applicantRepresentative)
                .build()));

        if (multiPartyScenario == TWO_V_ONE) {
            var applicant2 = caseData.getApplicant2();
            applicantParties.add(Party.builder()
                                     .name(applicant2.getPartyName())
                                     .primaryAddress(applicant2.getPrimaryAddress())
                                     .litigationFriendName(
                                         ofNullable(caseData.getApplicant2LitigationFriend())
                                             .map(LitigationFriend::getFullName)
                                             .orElse(""))
                                     .representative(applicantRepresentative)
                                     .build());
        }

        return applicantParties;
    }
}
