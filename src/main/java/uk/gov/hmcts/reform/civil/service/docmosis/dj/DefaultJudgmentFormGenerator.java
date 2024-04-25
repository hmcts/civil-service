package uk.gov.hmcts.reform.civil.service.docmosis.dj;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.model.Organisation;
import uk.gov.hmcts.reform.ccd.model.OrganisationPolicy;
import uk.gov.hmcts.reform.civil.documentmanagement.DocumentManagementService;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType;
import uk.gov.hmcts.reform.civil.documentmanagement.model.PDF;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.Address;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.docmosis.DocmosisDocument;
import uk.gov.hmcts.reform.civil.model.docmosis.common.Party;
import uk.gov.hmcts.reform.civil.model.docmosis.dj.DefaultJudgmentForm;
import uk.gov.hmcts.reform.civil.prd.model.ContactInformation;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.FeesService;
import uk.gov.hmcts.reform.civil.service.OrganisationService;
import uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates;
import uk.gov.hmcts.reform.civil.service.docmosis.DocumentGeneratorService;
import uk.gov.hmcts.reform.civil.service.docmosis.TemplateDataGenerator;
import uk.gov.hmcts.reform.civil.utils.InterestCalculator;
import uk.gov.hmcts.reform.civil.utils.MonetaryConversions;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static uk.gov.hmcts.reform.civil.callback.CaseEvent.GENERATE_DJ_FORM_SPEC;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.GEN_DJ_FORM_NON_DIVERGENT_SPEC_CLAIMANT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.GEN_DJ_FORM_NON_DIVERGENT_SPEC_DEFENDANT;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.N121;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.N121_SPEC;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.N121_SPEC_CLAIMANT;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.N121_SPEC_DEFENDANT;
import static uk.gov.hmcts.reform.civil.utils.DefaultJudgmentUtils.calculateFixedCosts;

@Service
@RequiredArgsConstructor
public class DefaultJudgmentFormGenerator implements TemplateDataGenerator<DefaultJudgmentForm> {

    private final DocumentManagementService documentManagementService;
    private final DocumentGeneratorService documentGeneratorService;
    private final OrganisationService organisationService;
    private final FeesService feesService;
    private final InterestCalculator interestCalculator;
    private final FeatureToggleService featureToggleService;

    public List<CaseDocument> generate(CaseData caseData, String authorisation, String event) {
        List<CaseDocument> caseDocuments = new ArrayList<>();
        DocmosisDocument docmosisDocument2;
        List<DefaultJudgmentForm> templateData = getDefaultJudgmentForms(caseData, event);
        DocmosisTemplates docmosisTemplate = getDocmosisTemplate(event);
        DocmosisDocument docmosisDocument1 =
            documentGeneratorService.generateDocmosisDocument(templateData.get(0), docmosisTemplate);
        caseDocuments.add(documentManagementService.uploadDocument(
            authorisation,
            new PDF(
                getFileName(caseData, docmosisTemplate),
                docmosisDocument1.getBytes(),
                DocumentType.DEFAULT_JUDGMENT
            )
        ));
        if (templateData.size() > 1) {
            docmosisDocument2 =
                documentGeneratorService.generateDocmosisDocument(templateData.get(1), docmosisTemplate);
            caseDocuments.add(documentManagementService.uploadDocument(
                authorisation,
                new PDF(
                    getFileName(caseData, docmosisTemplate),
                    docmosisDocument2.getBytes(),
                    DocumentType.DEFAULT_JUDGMENT
                )
            ));
        }
        return caseDocuments;
    }

    @Override
    public DefaultJudgmentForm getTemplateData(CaseData caseData) throws IOException {

        return null;

    }

    private String getFileName(CaseData caseData, DocmosisTemplates docmosisTemplate) {
        return String.format(docmosisTemplate.getDocumentTitle(), caseData.getLegacyCaseReference());
    }

    private List<DefaultJudgmentForm> getDefaultJudgmentForms(CaseData caseData, String event) {
        List<DefaultJudgmentForm> defaultJudgmentForms = new ArrayList<>();

        defaultJudgmentForms.add(getDefaultJudgmentForm(caseData, caseData.getRespondent1(), event));
        if (caseData.getRespondent2() != null) {

            defaultJudgmentForms.add(getDefaultJudgmentForm(caseData, caseData.getRespondent2(), event));
        }

        return defaultJudgmentForms;

    }

    private DefaultJudgmentForm getDefaultJudgmentForm(CaseData caseData,
                                                       uk.gov.hmcts.reform.civil.model.Party party,
                                                       String event) {
        BigDecimal debtAmount =
            event.equals(GENERATE_DJ_FORM_SPEC.name()) || event.equals(GEN_DJ_FORM_NON_DIVERGENT_SPEC_CLAIMANT.name())
            || event.equals(GEN_DJ_FORM_NON_DIVERGENT_SPEC_DEFENDANT.name())
            ? getDebtAmount(caseData).setScale(2) : new BigDecimal(0);
        BigDecimal cost =
            event.equals(GENERATE_DJ_FORM_SPEC.name()) || event.equals(GEN_DJ_FORM_NON_DIVERGENT_SPEC_CLAIMANT.name())
            || event.equals(GEN_DJ_FORM_NON_DIVERGENT_SPEC_DEFENDANT.name())
            ? getClaimFee(caseData).setScale(2) : new BigDecimal(0);

        DefaultJudgmentForm.DefaultJudgmentFormBuilder builder = DefaultJudgmentForm.builder();
        builder
            .caseNumber(caseData.getLegacyCaseReference())
            .formText("No response,")
            .applicant(getApplicant(caseData.getApplicant1(), caseData.getApplicant2()))
            .respondent(getPartyDetails(party))
            .claimantLR(Objects.nonNull(caseData.getApplicant1OrganisationPolicy())
                            ? getApplicantOrgDetails(caseData.getApplicant1OrganisationPolicy()) : null)
            .debt(debtAmount.toString())
            .costs(cost.toString())
            .totalCost(debtAmount.add(cost).setScale(2).toString())
            .applicantReference(getApplicantSolicitorRef(caseData))
            .respondentReference(getRespondent1SolicitorRef(caseData)).build();
        if (featureToggleService.isJudgmentOnlineLive()) {
            builder = addNonDivergentTemplateFields(builder, caseData, party);
        }
        return builder.build();
    }

    private String getApplicantSolicitorRef(CaseData caseData) {
        if (caseData.getSolicitorReferences() != null && caseData.getSolicitorReferences()
            .getApplicantSolicitor1Reference() != null) {
            return caseData.getSolicitorReferences().getApplicantSolicitor1Reference();
        }
        return null;
    }

    private String getRespondent1SolicitorRef(CaseData caseData) {
        if (caseData.getSolicitorReferences() != null && caseData.getSolicitorReferences()
            .getRespondentSolicitor1Reference() != null) {
            return caseData.getSolicitorReferences().getRespondentSolicitor1Reference();
        }
        return null;
    }

    private String getRespondent2SolicitorRef(CaseData caseData) {
        if (caseData.getSolicitorReferences() != null && caseData.getSolicitorReferences()
            .getRespondentSolicitor2Reference() != null) {
            return caseData.getSolicitorReferences().getRespondentSolicitor2Reference();
        }
        return null;
    }

    private DefaultJudgmentForm.DefaultJudgmentFormBuilder addNonDivergentTemplateFields(DefaultJudgmentForm.DefaultJudgmentFormBuilder builder,
                                                                                         CaseData caseData, uk.gov.hmcts.reform.civil.model.Party party) {
        return builder.respondent1Name(caseData.getRespondent1().getPartyName())
            .respondent2Name(Objects.isNull(caseData.getRespondent2()) ? null : caseData.getRespondent2().getPartyName())
            .respondent1Ref(getRespondent1SolicitorRef(caseData))
            .respondent2Ref(getRespondent2SolicitorRef(caseData))
            .claimantLR(getClaimantLipOrLRDetailsForAddress(caseData))
            .applicantDetails(getPartyDetails(party));
    }

    private Party getClaimantLipOrLRDetailsForAddress(CaseData caseData) {
        if (caseData.isApplicantLiP()) {
            return getPartyDetails(caseData.getApplicant1());
        } else {
            if (caseData.getApplicant1OrganisationPolicy() != null) {
                return getApplicantOrgDetails(caseData.getApplicant1OrganisationPolicy());
            } else {
                return null;
            }
        }
    }

    private DocmosisTemplates getDocmosisTemplate(String event) {
        if (event.equals(GENERATE_DJ_FORM_SPEC.name())) {
            return N121_SPEC;
        } else if (event.equals(GEN_DJ_FORM_NON_DIVERGENT_SPEC_CLAIMANT.name())) {
            return N121_SPEC_CLAIMANT;
        } else if (event.equals(GEN_DJ_FORM_NON_DIVERGENT_SPEC_DEFENDANT.name())) {
            return N121_SPEC_DEFENDANT;
        } else {
            return N121;
        }
    }

    private Party getPartyDetails(uk.gov.hmcts.reform.civil.model.Party party) {
        return Party.builder()
            .name(party.getPartyName())
            .primaryAddress(party.getPrimaryAddress())
            .build();
    }

    private List<Party> getApplicant(uk.gov.hmcts.reform.civil.model.Party applicant1,
                                     uk.gov.hmcts.reform.civil.model.Party applicant2) {

        List<Party> applicants = new ArrayList<>();
        applicants.add(Party.builder()
                           .name(applicant1.getPartyName())
                           .primaryAddress(applicant1.getPrimaryAddress())
                           .build());
        if (applicant2 != null) {
            applicants.add(Party.builder()
                               .name(applicant2.getPartyName())
                               .primaryAddress(applicant2.getPrimaryAddress())
                               .build());
        }
        return applicants;
    }

    private Party getApplicantOrgDetails(OrganisationPolicy organisationPolicy) {

        return Optional.ofNullable(organisationPolicy)
            .map(OrganisationPolicy::getOrganisation)
            .map(Organisation::getOrganisationID)
            .map(organisationService::findOrganisationById)
            .flatMap(value -> value.map(o -> Party.builder()
                .name(o.getName())
                .primaryAddress(getAddress(o.getContactInformation().get(0)))
                .build())).orElse(null);
    }

    private Address getAddress(ContactInformation address) {
        return Address.builder().addressLine1(address.getAddressLine1())
            .addressLine2(address.getAddressLine1())
            .addressLine3(address.getAddressLine1())
            .country(address.getCountry())
            .county(address.getCounty())
            .postCode(address.getPostCode())
            .postTown(address.getTownCity())
            .build();
    }

    private BigDecimal getClaimFee(CaseData caseData) {
        var claimfee = feesService.getFeeDataByTotalClaimAmount(caseData.getTotalClaimAmount());
        var claimFeePounds = MonetaryConversions.penniesToPounds(claimfee.getCalculatedAmountInPence());
        if (caseData.getPaymentConfirmationDecisionSpec() == YesOrNo.YES) {
            claimFeePounds = claimFeePounds.add(calculateFixedCosts(caseData));
        }
        return claimFeePounds;
    }

    private BigDecimal getDebtAmount(CaseData caseData) {
        BigDecimal interest = interestCalculator.calculateInterest(caseData);
        var subTotal = caseData.getTotalClaimAmount()
            .add(interest);
        subTotal = subTotal.subtract(getPartialPayment(caseData));

        return subTotal;
    }

    private BigDecimal getPartialPayment(CaseData caseData) {

        BigDecimal partialPaymentPounds = new BigDecimal(0);
        //Check if partial payment was selected by user, and assign value if so.
        if (caseData.getPartialPaymentAmount() != null) {
            var partialPaymentPennies = new BigDecimal(caseData.getPartialPaymentAmount());
            partialPaymentPounds = MonetaryConversions.penniesToPounds(partialPaymentPennies);
        }
        return partialPaymentPounds;
    }

    public List<CaseDocument> generateNonDivergentDocs(CaseData caseData, String authorisation, String event) {
        List<DefaultJudgmentForm> defaultJudgmentForms = new ArrayList<>();
        if (event.equals(GEN_DJ_FORM_NON_DIVERGENT_SPEC_CLAIMANT.name())) {
            defaultJudgmentForms.add(getDefaultJudgmentForm(caseData, caseData.getApplicant1(), event));
            if (caseData.getApplicant2() != null) {
                defaultJudgmentForms.add(getDefaultJudgmentForm(caseData, caseData.getApplicant2(), event));
            }
        } else {
            defaultJudgmentForms.add(getDefaultJudgmentForm(caseData, caseData.getRespondent1(), event));
            if (caseData.getRespondent2() != null) {
                defaultJudgmentForms.add(getDefaultJudgmentForm(caseData, caseData.getRespondent2(), event));
            }
        }
        return generateDocmosisDocsForNonDivergent(defaultJudgmentForms, authorisation, caseData, event);
    }

    private List<CaseDocument> generateDocmosisDocsForNonDivergent(List<DefaultJudgmentForm> defaultJudgmentForms,
                                                   String authorisation, CaseData caseData, String event) {
        List<CaseDocument> caseDocuments = new ArrayList<>();
        for (int i = 0; i < defaultJudgmentForms.size(); i++) {
            DefaultJudgmentForm defaultJudgmentForm = defaultJudgmentForms.get(i);
            DocumentType documentType = getDocumentTypeBasedOnEvent(i, event);
            DocmosisTemplates docmosisTemplate = getDocmosisTemplate(event);
            DocmosisDocument docmosisDocument = documentGeneratorService.generateDocmosisDocument(defaultJudgmentForm,
                                                                                                  docmosisTemplate);
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

    private DocumentType getDocumentTypeBasedOnEvent(int i, String event) {
        if (event.equals(GEN_DJ_FORM_NON_DIVERGENT_SPEC_CLAIMANT.name())) {
            if (i == 0) {
                return DocumentType.DEFAULT_JUDGMENT_CLAIMANT1;
            } else {
                return DocumentType.DEFAULT_JUDGMENT_CLAIMANT2;
            }
        } else {
            if (i == 0) {
                return DocumentType.DEFAULT_JUDGMENT_DEFENDANT1;
            } else {
                return DocumentType.DEFAULT_JUDGMENT_DEFENDANT2;
            }
        }
    }
}
