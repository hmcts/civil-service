package uk.gov.hmcts.reform.civil.service.docmosis.aos;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.enums.MultiPartyScenario;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.LitigationFriend;
import uk.gov.hmcts.reform.civil.model.docmosis.DocmosisDocument;
import uk.gov.hmcts.reform.civil.model.docmosis.aos.AcknowledgementOfClaimForm;
import uk.gov.hmcts.reform.civil.model.docmosis.common.Party;
import uk.gov.hmcts.reform.civil.model.documents.CaseDocument;
import uk.gov.hmcts.reform.civil.model.documents.DocumentType;
import uk.gov.hmcts.reform.civil.model.documents.PDF;
import uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates;
import uk.gov.hmcts.reform.civil.service.docmosis.DocumentGeneratorService;
import uk.gov.hmcts.reform.civil.service.docmosis.RepresentativeService;
import uk.gov.hmcts.reform.civil.service.docmosis.TemplateDataGenerator;
import uk.gov.hmcts.reform.civil.service.documentmanagement.DocumentManagementService;
import uk.gov.hmcts.reform.civil.utils.DocmosisTemplateDataUtils;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static java.util.Optional.ofNullable;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.ONE_V_TWO_ONE_LEGAL_REP;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.ONE_V_TWO_TWO_LEGAL_REP;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.getMultiPartyScenario;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.N11;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.N9_MULTIPARTY_SAME_SOL;

@Service
@RequiredArgsConstructor
public class AcknowledgementOfClaimGenerator implements TemplateDataGenerator<AcknowledgementOfClaimForm> {

    private final DocumentManagementService documentManagementService;
    private final DocumentGeneratorService documentGeneratorService;
    private final RepresentativeService representativeService;

    public CaseDocument generate(CaseData caseData, String authorisation) {
        AcknowledgementOfClaimForm templateData = getTemplateDataForAcknowldgeClaim(caseData);
        DocmosisTemplates docmosisTemplate =
            getMultiPartyScenario(caseData) == ONE_V_TWO_ONE_LEGAL_REP ? N9_MULTIPARTY_SAME_SOL : N11;
        DocmosisDocument docmosisDocument =
            documentGeneratorService.generateDocmosisDocument(templateData, docmosisTemplate);

        return documentManagementService.uploadDocument(
            authorisation,
            new PDF(
                getFileName(caseData, docmosisTemplate),
                docmosisDocument.getBytes(),
                DocumentType.ACKNOWLEDGEMENT_OF_CLAIM
            )
        );
    }

    private String getFileName(CaseData caseData, DocmosisTemplates docmosisTemplate) {
        return String.format(docmosisTemplate.getDocumentTitle(), caseData.getLegacyCaseReference());
    }

    @Override
    public AcknowledgementOfClaimForm getTemplateData(CaseData caseData) {
        MultiPartyScenario multiPartyScenario = getMultiPartyScenario(caseData);
        return AcknowledgementOfClaimForm.builder()
            .caseName(DocmosisTemplateDataUtils.toCaseName.apply(caseData))
            .referenceNumber(caseData.getLegacyCaseReference())
            .solicitorReferences(DocmosisTemplateDataUtils.fetchSolicitorReferences(caseData))
            .issueDate(caseData.getIssueDate())
            .responseDeadline(caseData.getRespondent1ResponseDeadline().toLocalDate())
            .respondent(prepareRespondentMultiParty(caseData, multiPartyScenario))
            .build();
    }

    private List<Party> prepareRespondentMultiParty(CaseData caseData, MultiPartyScenario multiPartyScenario) {

        var respondent = caseData.getRespondent1();

        var respondentParties = new ArrayList<>(List.of(
            Party.builder()
                .name(respondent.getPartyName())
                .primaryAddress(respondent.getPrimaryAddress())
                .representative(representativeService.getRespondent1Representative(caseData))
                .litigationFriendName(
                    ofNullable(caseData.getRespondent1LitigationFriend())
                        .map(LitigationFriend::getFullName)
                        .orElse(""))
                .build()));
        if ((multiPartyScenario == ONE_V_TWO_ONE_LEGAL_REP) || (multiPartyScenario == ONE_V_TWO_TWO_LEGAL_REP)) {
            var respondent2 = caseData.getRespondent2();
            respondentParties.add(Party.builder()
                                      .name(respondent2.getPartyName())
                                      .primaryAddress(respondent2.getPrimaryAddress())
                                      .representative(representativeService.getRespondent2Representative(caseData))
                                      .litigationFriendName(
                                          ofNullable(caseData.getRespondent2LitigationFriend())
                                              .map(LitigationFriend::getFullName)
                                              .orElse(""))
                                      .build());

        }
        if (multiPartyScenario == ONE_V_TWO_TWO_LEGAL_REP) {
            if ((caseData.getRespondent1AcknowledgeNotificationDate() == null)
                && (caseData.getRespondent2AcknowledgeNotificationDate() != null)) {
                respondentParties.remove(0);
            } else if ((caseData.getRespondent1AcknowledgeNotificationDate() != null)
                && (caseData.getRespondent2AcknowledgeNotificationDate() != null)) {
                if (caseData.getRespondent2AcknowledgeNotificationDate()
                    .isAfter(caseData.getRespondent1AcknowledgeNotificationDate())) {
                    respondentParties.remove(0);
                } else {
                    respondentParties.remove(1);
                }
            }
        }
        return respondentParties;
    }

    public AcknowledgementOfClaimForm getTemplateDataForAcknowldgeClaim(CaseData caseData) {
        MultiPartyScenario multiPartyScenario = getMultiPartyScenario(caseData);
        LocalDate responseDeadline = caseData.getRespondent1ResponseDeadline().toLocalDate();
        if (multiPartyScenario == ONE_V_TWO_TWO_LEGAL_REP) {
            if ((caseData.getRespondent1AcknowledgeNotificationDate() == null)
                    && (caseData.getRespondent2AcknowledgeNotificationDate() != null)) {
                responseDeadline = caseData.getRespondent2ResponseDeadline().toLocalDate();
            } else if ((caseData.getRespondent1AcknowledgeNotificationDate() != null)
                    && (caseData.getRespondent2AcknowledgeNotificationDate() != null)) {
                if (caseData.getRespondent2AcknowledgeNotificationDate()
                        .isAfter(caseData.getRespondent1AcknowledgeNotificationDate())) {
                    responseDeadline = caseData.getRespondent2ResponseDeadline().toLocalDate();
                } else {
                    responseDeadline = caseData.getRespondent1ResponseDeadline().toLocalDate();
                }
            }
        }
        return AcknowledgementOfClaimForm.builder()
                .caseName(DocmosisTemplateDataUtils.toCaseName.apply(caseData))
                .referenceNumber(caseData.getLegacyCaseReference())
                .solicitorReferences(DocmosisTemplateDataUtils.fetchSolicitorReferencesMultiparty(caseData))
                .issueDate(caseData.getIssueDate())
                .responseDeadline(responseDeadline)
                .respondent(prepareRespondentMultiParty(caseData, multiPartyScenario))
                .build();

    }
}
