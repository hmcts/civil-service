package uk.gov.hmcts.reform.civil.service.docmosis.sealedclaim;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.documentmanagement.DocumentManagementService;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType;
import uk.gov.hmcts.reform.civil.documentmanagement.model.PDF;
import uk.gov.hmcts.reform.civil.enums.MultiPartyScenario;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.SolicitorReferences;
import uk.gov.hmcts.reform.civil.model.docmosis.DocmosisDocument;
import uk.gov.hmcts.reform.civil.model.docmosis.common.Party;
import uk.gov.hmcts.reform.civil.model.docmosis.sealedclaim.SealedClaimForm;
import uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates;
import uk.gov.hmcts.reform.civil.service.docmosis.DocumentGeneratorService;
import uk.gov.hmcts.reform.civil.service.docmosis.RepresentativeService;
import uk.gov.hmcts.reform.civil.service.docmosis.TemplateDataGeneratorWithAuth;
import uk.gov.hmcts.reform.civil.utils.DocmosisTemplateDataUtils;
import uk.gov.hmcts.reform.civil.utils.LocationRefDataUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static java.util.Objects.nonNull;
import static java.util.Optional.ofNullable;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.ONE_V_TWO_ONE_LEGAL_REP;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.ONE_V_TWO_TWO_LEGAL_REP;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.TWO_V_ONE;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.getMultiPartyScenario;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.N1;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.N1_MULTIPARTY_SAME_SOL;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.N1_MULTIPARTY_SAME_SOL_OTHER_REMEDY;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.N1_OTHER_REMEDY;
import static uk.gov.hmcts.reform.civil.utils.DocmosisTemplateDataUtils.formatCcdCaseReference;

@Service
@RequiredArgsConstructor
public class SealedClaimFormGenerator implements TemplateDataGeneratorWithAuth<SealedClaimForm> {

    private final DocumentManagementService documentManagementService;
    private final DocumentGeneratorService documentGeneratorService;
    private final RepresentativeService representativeService;
    private final LocationRefDataUtil locationRefDataUtil;

    public CaseDocument generate(CaseData caseData, String authorisation) {
        SealedClaimForm templateData = getTemplateData(caseData, authorisation);

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
    public SealedClaimForm getTemplateData(CaseData caseData, String authorisation) {
        Optional<SolicitorReferences> solicitorReferences = ofNullable(caseData.getSolicitorReferences());
        MultiPartyScenario multiPartyScenario = getMultiPartyScenario(caseData);
        String hearingCourtLocation = locationRefDataUtil.getPreferredCourtData(
            caseData, authorisation, false);
        List<Party> applicants = getApplicants(caseData, multiPartyScenario);

        SealedClaimForm sealedClaimForm = new SealedClaimForm()
            .setApplicants(applicants)
            .setRespondents(getRespondents(caseData, multiPartyScenario))
            .setClaimValue(caseData.getClaimValue().formData())
            .setStatementOfTruth(caseData.getApplicantSolicitor1ClaimStatementOfTruth())
            .setClaimDetails(caseData.getDetailsOfClaim())
            .setHearingCourtLocation(hearingCourtLocation)
            .setReferenceNumber(caseData.getLegacyCaseReference())
            .setCcdCaseReference(formatCcdCaseReference(caseData))
            .setIssueDate(caseData.getIssueDate())
            .setSubmittedOn(caseData.getSubmittedDate().toLocalDate())
            .setApplicantExternalReference(solicitorReferences
                                               .map(SolicitorReferences::getApplicantSolicitor1Reference)
                                               .orElse(""))
            .setRespondent1ExternalReference(solicitorReferences
                                                 .map(SolicitorReferences::getRespondentSolicitor1Reference)
                                                 .orElse(""))
            .setCaseName(DocmosisTemplateDataUtils.toCaseName.apply(caseData))
            .setApplicantRepresentativeOrganisationName(applicants.get(0).getRepresentative().getOrganisationName())
            .setIsClaimDeclarationAdded(caseData.getIsClaimDeclarationAdded())
            .setClaimDeclarationDescription(caseData.getClaimDeclarationDescription())
            .setIsHumanRightsActIssues(caseData.getIsHumanRightsActIssues())
            .setCourtFee(caseData.getClaimFee().formData())
            .setOtherRemedyFee(YES.equals(caseData.getIsClaimDeclarationAdded()) && nonNull(caseData.getOtherRemedyFee()) ? caseData.getOtherRemedyFee().formData() : null);

        if (multiPartyScenario == ONE_V_TWO_TWO_LEGAL_REP) {
            sealedClaimForm.setRespondent2ExternalReference(caseData.getRespondentSolicitor2Reference());
        }

        return sealedClaimForm;
    }

    private DocmosisTemplates getDocmosisTemplate(CaseData caseData) {
        return switch (getMultiPartyScenario(caseData)) {
            case ONE_V_ONE, ONE_V_TWO_TWO_LEGAL_REP -> caseData.isOtherRemedyClaim() ? N1_OTHER_REMEDY : N1;
            case TWO_V_ONE, ONE_V_TWO_ONE_LEGAL_REP -> caseData.isOtherRemedyClaim() ? N1_MULTIPARTY_SAME_SOL_OTHER_REMEDY  : N1_MULTIPARTY_SAME_SOL;
            default -> throw new IllegalArgumentException("Multiparty scenario doesn't exist");
        };
    }

    private List<Party> getRespondents(CaseData caseData, MultiPartyScenario multiPartyScenario) {
        var respondent = caseData.getRespondent1();
        var respondent1Representative = representativeService.getRespondent1Representative(caseData);
        var respondentParties = new ArrayList<>(List.of(
            new Party()
                .setType(respondent.getType().getDisplayValue())
                .setSoleTraderTradingAs(DocmosisTemplateDataUtils.fetchSoleTraderCompany(respondent))
                .setName(respondent.getPartyName())
                .setPrimaryAddress(respondent.getPrimaryAddress())
                .setRepresentative(respondent1Representative)));

        if (multiPartyScenario == ONE_V_TWO_ONE_LEGAL_REP) {
            var respondent2 = caseData.getRespondent2();
            respondentParties.add(new Party()
                                      .setType(respondent2.getType().getDisplayValue())
                                      .setSoleTraderTradingAs(
                                          DocmosisTemplateDataUtils.fetchSoleTraderCompany(respondent2))
                                      .setName(respondent2.getPartyName())
                                      .setPrimaryAddress(respondent2.getPrimaryAddress())
                                      .setRepresentative(respondent1Representative));
        } else if (multiPartyScenario == ONE_V_TWO_TWO_LEGAL_REP) {
            var respondent2 = caseData.getRespondent2();
            respondentParties.add(new Party()
                                      .setType(respondent2.getType().getDisplayValue())
                                      .setSoleTraderTradingAs(
                                          DocmosisTemplateDataUtils.fetchSoleTraderCompany(respondent2))
                                      .setName(respondent2.getPartyName())
                                      .setPrimaryAddress(respondent2.getPrimaryAddress())
                                      .setRepresentative(representativeService.getRespondent2Representative(caseData)));
        }

        return respondentParties;
    }

    private List<Party> getApplicants(CaseData caseData, MultiPartyScenario multiPartyScenario) {
        var applicant = caseData.getApplicant1();
        var applicantRepresentative = representativeService.getApplicantRepresentative(caseData);
        var litigationFriend1 = caseData.getApplicant1LitigationFriend();
        var applicantParties = new ArrayList<>(List.of(
            new Party()
                .setType(applicant.getType().getDisplayValue())
                .setSoleTraderTradingAs(DocmosisTemplateDataUtils.fetchSoleTraderCompany(applicant))
                .setName(applicant.getPartyName())
                .setPrimaryAddress(applicant.getPrimaryAddress())
                .setLitigationFriendName(litigationFriend1 != null ? litigationFriend1.getFirstName() + " "
                    + litigationFriend1.getLastName() : "")
                .setRepresentative(applicantRepresentative)));

        if (multiPartyScenario == TWO_V_ONE) {
            var applicant2 = caseData.getApplicant2();
            var litigationFriend2 = caseData.getApplicant2LitigationFriend();
            applicantParties.add(
                new Party()
                    .setType(applicant2.getType().getDisplayValue())
                    .setSoleTraderTradingAs(DocmosisTemplateDataUtils.fetchSoleTraderCompany(applicant2))
                    .setName(applicant2.getPartyName())
                    .setPrimaryAddress(applicant2.getPrimaryAddress())
                    .setLitigationFriendName(litigationFriend2 != null ? litigationFriend2.getFirstName() + " "
                        + litigationFriend2.getLastName() : "")
                    .representative(applicantRepresentative)
                    .build());
        }

        return applicantParties;
    }
}
