package uk.gov.hmcts.reform.civil.service.docmosis.dj;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.model.Address;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.docmosis.DocmosisDocument;
import uk.gov.hmcts.reform.civil.model.docmosis.common.Party;
import uk.gov.hmcts.reform.civil.model.docmosis.dj.DefaultJudgmentForm;
import uk.gov.hmcts.reform.civil.model.documents.CaseDocument;
import uk.gov.hmcts.reform.civil.model.documents.DocumentType;
import uk.gov.hmcts.reform.civil.model.documents.PDF;
import uk.gov.hmcts.reform.civil.service.FeesService;
import uk.gov.hmcts.reform.civil.service.OrganisationService;
import uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates;
import uk.gov.hmcts.reform.civil.service.docmosis.DocumentGeneratorService;
import uk.gov.hmcts.reform.civil.service.docmosis.TemplateDataGenerator;
import uk.gov.hmcts.reform.civil.service.documentmanagement.DocumentManagementService;
import uk.gov.hmcts.reform.civil.utils.InterestCalculator;
import uk.gov.hmcts.reform.civil.utils.MonetaryConversions;
import uk.gov.hmcts.reform.prd.model.ContactInformation;
import uk.gov.hmcts.reform.prd.model.Organisation;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static uk.gov.hmcts.reform.civil.callback.CaseEvent.GENERATE_DJ_FORM_SPEC;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.N121;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.N121_SPEC;

@Service
@RequiredArgsConstructor
public class DefaultJudgmentFormGenerator implements TemplateDataGenerator<DefaultJudgmentForm> {

    private static final int COMMENCEMENT_FIXED_COST_60 = 60;
    private static final int COMMENCEMENT_FIXED_COST_80 = 80;
    private static final int COMMENCEMENT_FIXED_COST_90 = 90;
    private static final int COMMENCEMENT_FIXED_COST_110 = 110;
    private static final int ENTRY_FIXED_COST_22 = 22;
    private static final int ENTRY_FIXED_COST_30 = 30;
    private final DocumentManagementService documentManagementService;
    private final DocumentGeneratorService documentGeneratorService;
    private final OrganisationService organisationService;
    private final FeesService feesService;
    private final InterestCalculator interestCalculator;

    public List<CaseDocument> generate(CaseData caseData, String authorisation, String event) {
        List<CaseDocument> caseDocuments = new ArrayList<>();
        DocmosisDocument docmosisDocument2;
        List<DefaultJudgmentForm> templateData = getDefaultJudgmentForms(caseData);
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


    private List<DefaultJudgmentForm> getDefaultJudgmentForms(CaseData caseData) {
        List<DefaultJudgmentForm> defaultJudgmentForms = new ArrayList<>();

        defaultJudgmentForms.add(getDefaultJudgmentForm(caseData, caseData.getRespondent1()));
        if (caseData.getRespondent2() != null) {

            defaultJudgmentForms.add(getDefaultJudgmentForm(caseData, caseData.getRespondent2()));
        }
        return defaultJudgmentForms;

    }

    private DefaultJudgmentForm getDefaultJudgmentForm(CaseData caseData,
                                                       uk.gov.hmcts.reform.civil.model.Party respondent) {
        BigDecimal debtAmount = getDebtAmount(caseData);
        BigDecimal cost = getClaimFee(caseData);

        return DefaultJudgmentForm.builder()
            .caseNumber(caseData.getLegacyCaseReference())
            .formText("No Acknowledgement of service")
            .applicant(getApplicant(caseData.getApplicant1(), caseData.getApplicant2()))
            .respondent(getRespondent(respondent))
            .claimantLR(getApplicantOrgDetails(caseData.getApplicant1OrganisationPolicy()
                                                   .getOrganisation().getOrganisationID()))
            .debt(getDebtAmount(caseData))
            .costs(getClaimFee(caseData))
            .totalCost(debtAmount.add(cost))
            .applicantReference(Objects.isNull(caseData.getSolicitorReferences())
                                    ? null : caseData.getSolicitorReferences()
                .getApplicantSolicitor1Reference())
            .respondentReference(Objects.isNull(caseData.getSolicitorReferences())
                                     ? null : caseData.getSolicitorReferences()
                .getRespondentSolicitor1Reference()).build();
    }

    private DocmosisTemplates getDocmosisTemplate(String event) {
        return event.equals(GENERATE_DJ_FORM_SPEC.name()) ? N121_SPEC : N121;

    }

    private Party getRespondent(uk.gov.hmcts.reform.civil.model.Party respondent) {
        return Party.builder()
            .name(respondent.getPartyName())
            .primaryAddress(respondent.getPrimaryAddress())
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

    private Party getApplicantOrgDetails(String id) {
        Optional<Organisation> organisation = organisationService.findOrganisationById(id);
        return organisation.map(value -> Party.builder()
            .name(value.getName())
            .primaryAddress(getAddress(value.getContactInformation().get(0)))
            .build()).orElse(null);
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
        claimFeePounds.add(calculateFixedCosts(caseData));
        return claimFeePounds;
    }

    private BigDecimal getDebtAmount(CaseData caseData) {
        BigDecimal interest = interestCalculator.calculateInterest(caseData);
        var subTotal = caseData.getTotalClaimAmount()
            .add(interest);
        subTotal.subtract(getPartialPayment(caseData));

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

    private BigDecimal calculateFixedCosts(CaseData caseData) {

        int fixedCost = 0;
        int totalClaimAmount = caseData.getTotalClaimAmount().intValue();
        if (totalClaimAmount > 25 && totalClaimAmount <= 5000) {
            if (totalClaimAmount <= 500) {
                fixedCost = COMMENCEMENT_FIXED_COST_60;
            } else if (totalClaimAmount <= 1000) {
                fixedCost = COMMENCEMENT_FIXED_COST_80;
            } else {
                fixedCost = COMMENCEMENT_FIXED_COST_90;
            }
            fixedCost = fixedCost + ENTRY_FIXED_COST_22;
        } else if (totalClaimAmount > 5000) {
            fixedCost = COMMENCEMENT_FIXED_COST_110 + ENTRY_FIXED_COST_30;
        }
        return new BigDecimal(fixedCost);
    }

}

