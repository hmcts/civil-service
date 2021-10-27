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

import java.util.ArrayList;
import java.util.List;

import static java.util.Optional.ofNullable;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.N9;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.N9_MULTIPARTY_SAME_SOL;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.*;

@Service
@RequiredArgsConstructor
public class AcknowledgementOfClaimGenerator implements TemplateDataGenerator<AcknowledgementOfClaimForm> {

    private final DocumentManagementService documentManagementService;
    private final DocumentGeneratorService documentGeneratorService;
    private final RepresentativeService representativeService;

    public CaseDocument generate(CaseData caseData, String authorisation) {
        AcknowledgementOfClaimForm templateData = getTemplateData(caseData);
        MultiPartyScenario multiPartyScenario = getMultiPartyScenario(caseData);
        DocmosisTemplates docmosisTemplate = multiPartyScenario == ONE_V_TWO_ONE_LEGAL_REP ? N9_MULTIPARTY_SAME_SOL : N9;
        DocmosisDocument docmosisDocument = documentGeneratorService.generateDocmosisDocument(templateData, docmosisTemplate);
        return documentManagementService.uploadDocument(
            authorisation,
            new PDF(getFileName(caseData, docmosisTemplate), docmosisDocument.getBytes(), DocumentType.ACKNOWLEDGEMENT_OF_CLAIM)
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
            .solicitorReferences(DocmosisTemplateDataUtils.fetchSolicitorReferences(caseData.getSolicitorReferences()))
            .issueDate(caseData.getIssueDate())
            .responseDeadline(caseData.getRespondent1ResponseDeadline().toLocalDate())
            .respondent(prepareRespondentMultiParty(caseData,multiPartyScenario))
            //.respondent(prepareRespondent(caseData))
            .build();
    }

    private Party prepareRespondent(CaseData caseData) {
        var respondent = caseData.getRespondent1();
        return Party.builder()
            .name(respondent.getPartyName())
            .primaryAddress(respondent.getPrimaryAddress())
            .representative(representativeService.getRespondentRepresentative(caseData))
            .litigationFriendName(
                ofNullable(caseData.getRespondent1LitigationFriend())
                    .map(LitigationFriend::getFullName)
                    .orElse(""))
            .build();
    }
    private List<Party> prepareRespondentMultiParty(CaseData caseData, MultiPartyScenario multiPartyScenario) {
        {
            var respondent = caseData.getRespondent1();

            var respondentParties = new ArrayList<>(List.of(
                Party.builder()
                    .name(respondent.getPartyName())
                    .primaryAddress(respondent.getPrimaryAddress())
                    .representative(representativeService.getRespondentRepresentative(caseData))
                    .litigationFriendName(
                        ofNullable(caseData.getRespondent1LitigationFriend())
                            .map(LitigationFriend::getFullName)
                            .orElse(""))
                    .build()));

            if (multiPartyScenario == ONE_V_TWO_ONE_LEGAL_REP) {
                var respondent2 = caseData.getRespondent2();
                respondentParties.add(Party.builder()
                                          .name(respondent2.getPartyName())
                                          .primaryAddress(respondent2.getPrimaryAddress())
                                          .representative(representativeService.getRespondentRepresentative(caseData))
                                          .litigationFriendName(
                                              ofNullable(caseData.getRespondent1LitigationFriend())
                                                  .map(LitigationFriend::getFullName)
                                                  .orElse(""))
                                          .build());
            } else if (multiPartyScenario == ONE_V_TWO_TWO_LEGAL_REP) {
                var respondent2 = caseData.getRespondent2();
                respondentParties.add(Party.builder()
                                          .name(respondent2.getPartyName())
                                          .primaryAddress(respondent2.getPrimaryAddress())
                                          .representative(representativeService.getRespondentRepresentative(caseData))
                                          .litigationFriendName(
                                              ofNullable(caseData.getRespondent1LitigationFriend())
                                                  .map(LitigationFriend::getFullName)
                                                  .orElse(""))
                                          .build());
            }

            return respondentParties;
        }
    }
    }
