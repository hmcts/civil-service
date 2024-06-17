package uk.gov.hmcts.reform.civil.service.docmosis.judgmentonline;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.model.Organisation;
import uk.gov.hmcts.reform.ccd.model.OrganisationPolicy;
import uk.gov.hmcts.reform.civil.documentmanagement.DocumentManagementService;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType;
import uk.gov.hmcts.reform.civil.documentmanagement.model.PDF;
import uk.gov.hmcts.reform.civil.helpers.DateFormatHelper;
import uk.gov.hmcts.reform.civil.helpers.judgmentsonline.JudgmentsOnlineHelper;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.docmosis.DocmosisDocument;
import uk.gov.hmcts.reform.civil.model.docmosis.common.Party;
import uk.gov.hmcts.reform.civil.model.docmosis.judgmentonline.JudgmentByDeterminationDocForm;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentInstalmentDetails;
import uk.gov.hmcts.reform.civil.model.judgmentonline.PaymentFrequency;
import uk.gov.hmcts.reform.civil.service.OrganisationService;
import uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates;
import uk.gov.hmcts.reform.civil.service.docmosis.DocumentGeneratorService;
import uk.gov.hmcts.reform.civil.utils.MonetaryConversions;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static uk.gov.hmcts.reform.civil.callback.CaseEvent.GEN_JUDGMENT_BY_DETERMINATION_DOC_CLAIMANT;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.JUDGMENT_BY_DETERMINATION_CLAIMANT;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.JUDGMENT_BY_DETERMINATION_DEFENDANT;
import static uk.gov.hmcts.reform.civil.utils.JudgmentOnlineUtils.getAddress;
import static uk.gov.hmcts.reform.civil.utils.JudgmentOnlineUtils.getApplicant;
import static uk.gov.hmcts.reform.civil.utils.JudgmentOnlineUtils.getRespondent2SolicitorRef;
import static uk.gov.hmcts.reform.civil.utils.JudgmentOnlineUtils.getRespondent1SolicitorRef;
import static uk.gov.hmcts.reform.civil.utils.JudgmentOnlineUtils.getApplicantSolicitorRef;

@Slf4j
@RequiredArgsConstructor
@Service
public class JudgmentByDeterminationDocGenerator {

    private static final String APPLICANT1 = "applicant1";
    private static final String RESPONDENT1 = "respondent1";
    private static final String RESPONDENT2 = "respondent2";
    private final DocumentManagementService documentManagementService;
    private final DocumentGeneratorService documentGeneratorService;
    private final OrganisationService organisationService;

    public List<CaseDocument> generateDocs(CaseData caseData, String authorisation, String event) {
        List<JudgmentByDeterminationDocForm> judgmentByDeterminationDocFormList = new ArrayList<>();
        if (event.equals(GEN_JUDGMENT_BY_DETERMINATION_DOC_CLAIMANT.name())) {
            judgmentByDeterminationDocFormList.add(
                getJudgmentByDeterminationDocForm(caseData, APPLICANT1));
        } else {
            judgmentByDeterminationDocFormList.add(getJudgmentByDeterminationDocForm(caseData, RESPONDENT1));
            if (caseData.getRespondent2() != null) {
                judgmentByDeterminationDocFormList.add(getJudgmentByDeterminationDocForm(caseData, RESPONDENT2));
            }
        }
        return generateDocmosisDocsForNonDivergent(judgmentByDeterminationDocFormList, authorisation, caseData, event);
    }

    private List<CaseDocument> generateDocmosisDocsForNonDivergent(List<JudgmentByDeterminationDocForm> judgmentByDeterminationDocFormList,
                                                                   String authorisation, CaseData caseData, String event) {
        List<CaseDocument> caseDocuments = new ArrayList<>();
        for (int i = 0; i < judgmentByDeterminationDocFormList.size(); i++) {
            DocumentType documentType = getDocumentTypeBasedOnEvent(event);
            DocmosisTemplates docmosisTemplate = getDocmosisTemplate(event);
            DocmosisDocument docmosisDocument = documentGeneratorService.generateDocmosisDocument(
                judgmentByDeterminationDocFormList.get(i),
                docmosisTemplate
            );
            caseDocuments.add(documentManagementService.uploadDocument(
                authorisation,
                new PDF(
                    getFileName(caseData, docmosisTemplate),
                    docmosisDocument.getBytes(),
                    documentType
                )
            ));
        }
        return caseDocuments;
    }

    private String getFileName(CaseData caseData, DocmosisTemplates docmosisTemplate) {
        return String.format(docmosisTemplate.getDocumentTitle(), caseData.getLegacyCaseReference());
    }

    private DocmosisTemplates getDocmosisTemplate(String event) {
        if (event.equals(GEN_JUDGMENT_BY_DETERMINATION_DOC_CLAIMANT.name())) {
            return JUDGMENT_BY_DETERMINATION_CLAIMANT;
        } else {
            return JUDGMENT_BY_DETERMINATION_DEFENDANT;
        }
    }

    private DocumentType getDocumentTypeBasedOnEvent(String event) {
        if (event.equals(GEN_JUDGMENT_BY_DETERMINATION_DOC_CLAIMANT.name())) {
            return DocumentType.JUDGMENT_BY_DETERMINATION_CLAIMANT;
        } else {
            return DocumentType.JUDGMENT_BY_DETERMINATION_DEFENDANT;
        }
    }

    private JudgmentByDeterminationDocForm getJudgmentByDeterminationDocForm(CaseData caseData, String partyType) {
        BigDecimal orderAmount =
            MonetaryConversions.penniesToPounds(JudgmentsOnlineHelper.getMoneyValue(caseData.getJoAmountOrdered()));
        BigDecimal costs =
            MonetaryConversions.penniesToPounds(JudgmentsOnlineHelper.getMoneyValue(caseData.getJoAmountCostOrdered()));

        JudgmentByDeterminationDocForm.JudgmentByDeterminationDocFormBuilder builder = JudgmentByDeterminationDocForm.builder();
        String payByDate = Objects.isNull(caseData.getJoInstalmentDetails()) ? null : Objects.isNull(caseData.getJoInstalmentDetails().getStartDate()) ? null
            : DateFormatHelper.formatLocalDate(caseData.getJoInstalmentDetails().getStartDate(), DateFormatHelper.DATE);
        String repaymentFrequency = Objects.isNull(caseData.getJoInstalmentDetails()) ? null
            : Objects.isNull(caseData.getJoInstalmentDetails().getPaymentFrequency()) ? null
            : getRepaymentFrequency(caseData.getJoInstalmentDetails().getPaymentFrequency());
        String paymentStr = Objects.isNull(caseData.getJoInstalmentDetails()) ? null
            : Objects.isNull(caseData.getJoInstalmentDetails().getPaymentFrequency())
            ? null : getRepaymentString(caseData.getJoInstalmentDetails().getPaymentFrequency());
        builder
            .claimReferenceNumber(caseData.getLegacyCaseReference())
            .formText("No response,")
            .applicants(getApplicant(caseData.getApplicant1(), caseData.getApplicant2()))
            .respondent(getRespondentLROrLipDetails(caseData, partyType))
            .debt(orderAmount.toString())
            .costs(costs.toString())
            .totalCost(orderAmount.add(costs).setScale(2).toString())
            .applicantReference(getApplicantSolicitorRef(caseData))
            .respondentReference(getRespondent1SolicitorRef(caseData))
            .respondent1Name(caseData.getRespondent1().getPartyName())
            .respondent2Name(Objects.isNull(caseData.getRespondent2()) ? null : caseData.getRespondent2().getPartyName())
            .respondent1Ref(getRespondent1SolicitorRef(caseData))
            .respondent2Ref(getRespondent2SolicitorRef(caseData))
            .claimantLR(getClaimantLipOrLRDetailsForPaymentAddress(caseData))
            .applicant(getClaimantLipOrLRDetailsForPaymentAddress(caseData))
            .paymentPlan(caseData.getJoPaymentPlan().getType().name())
            .payByDate(payByDate)
            .repaymentFrequency(repaymentFrequency)
            .paymentStr(paymentStr)
            .installmentAmount(Objects.isNull(caseData.getJoInstalmentDetails()) ? null
                                   : getInstallmentAmount(caseData.getJoInstalmentDetails()))
            .repaymentDate(payByDate);
        return builder.build();
    }

    private String getRepaymentFrequency(PaymentFrequency paymentFrequency) {
        switch (paymentFrequency) {
            case WEEKLY: return "per week";
            case MONTHLY: return "per month";
            case EVERY_TWO_WEEKS: return "every 2 weeks";
            default: return null;
        }
    }

    private String getInstallmentAmount(JudgmentInstalmentDetails instalmentDetails) {
        if (instalmentDetails.getAmount() != null) {
            String amount = instalmentDetails.getAmount();
            var regularRepaymentAmountPennies = new BigDecimal(amount);
            return String.valueOf(MonetaryConversions.penniesToPounds(regularRepaymentAmountPennies));
        } else {
            return null;
        }
    }

    private String getRepaymentString(PaymentFrequency repaymentFrequency) {
        switch (repaymentFrequency) {
            case WEEKLY : return "each week";
            case MONTHLY: return "each month";
            case EVERY_TWO_WEEKS: return "every 2 weeks";
            default: return null;
        }
    }

    private Party getClaimantLipOrLRDetailsForPaymentAddress(CaseData caseData) {
        if (caseData.isApplicantLiP()) {
            return getPartyDetails(caseData.getApplicant1());
        } else if (caseData.getApplicant1OrganisationPolicy() != null) {
            return getOrgDetails(caseData.getApplicant1OrganisationPolicy());
        } else {
            return null;
        }
    }

    private Party getPartyDetails(uk.gov.hmcts.reform.civil.model.Party party) {
        return Party.builder()
            .name(party.getPartyName())
            .primaryAddress(party.getPrimaryAddress())
            .build();
    }

    private Party getOrgDetails(OrganisationPolicy organisationPolicy) {

        return Optional.ofNullable(organisationPolicy)
            .map(OrganisationPolicy::getOrganisation)
            .map(Organisation::getOrganisationID)
            .map(organisationService::findOrganisationById)
            .flatMap(value -> value.map(o -> Party.builder()
                .name(o.getName())
                .primaryAddress(getAddress(o.getContactInformation().get(0)))
                .build())).orElse(null);
    }

    private Party getRespondentLROrLipDetails(CaseData caseData, String partyType) {
        if (partyType.equals(RESPONDENT1) && caseData.isRespondent1LiP()) {
            return getPartyDetails(caseData.getRespondent1());
        } else if (partyType.equals(RESPONDENT1) && caseData.getRespondent1OrganisationPolicy() != null) {
            return getOrgDetails(caseData.getRespondent1OrganisationPolicy());
        } else if (!partyType.equals(RESPONDENT1) && caseData.isRespondent2LiP()) {
            return getPartyDetails(caseData.getRespondent2());
        } else if (!partyType.equals(RESPONDENT1) && caseData.getRespondent2OrganisationPolicy() != null) {
            return getOrgDetails(caseData.getRespondent2OrganisationPolicy());
        } else {
            return null;
        }
    }
}
