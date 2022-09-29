package uk.gov.hmcts.reform.civil.service.docmosis.hearing;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.Address;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.CorrectEmail;
import uk.gov.hmcts.reform.civil.model.docmosis.DocmosisDocument;
import uk.gov.hmcts.reform.civil.model.docmosis.common.Party;
import uk.gov.hmcts.reform.civil.model.docmosis.dj.DefaultJudgmentForm;
import uk.gov.hmcts.reform.civil.model.docmosis.hearing.HearingForm;
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
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static uk.gov.hmcts.reform.civil.callback.CaseEvent.GENERATE_DJ_FORM_SPEC;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.GENERATE_HEARING_FORM;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.N121;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.N121_SPEC;

@Service
@RequiredArgsConstructor
public class HearingFormGenerator implements TemplateDataGenerator<HearingForm> {
    private final DocumentManagementService documentManagementService;
    private final DocumentGeneratorService documentGeneratorService;
    private final OrganisationService organisationService;

    public List<CaseDocument> generate(CaseData caseData, String authorisation, String event) {
        List<CaseDocument> caseDocuments = new ArrayList<>();
        DocmosisDocument docmosisDocument2;
        List<HearingForm> templateData = getHearingForms(caseData, event);
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
    public HearingForm getTemplateData(CaseData caseData) throws IOException {

        return null;

    }

    private String getFileName(CaseData caseData, DocmosisTemplates docmosisTemplate) {
        return String.format(docmosisTemplate.getDocumentTitle(), caseData.getLegacyCaseReference());
    }

    private List<HearingForm> getHearingForms(CaseData caseData, String event) {
        List<HearingForm> hearingForms = new ArrayList<>();

        hearingForms.add(getHearingForm(caseData, caseData.getRespondent1(), event));
        if (caseData.getRespondent2() != null) {

            hearingForms.add(getHearingForm(caseData, caseData.getRespondent2(), event));
        }

        return hearingForms;

    }

    private HearingForm getHearingForm(CaseData caseData,
                                                       uk.gov.hmcts.reform.civil.model.Party respondent,
                                                       String event) {
        BigDecimal hearingFee = event.equals(GENERATE_HEARING_FORM.name())
            ? getHearingFee(caseData).setScale(2) : new BigDecimal(0);

        return HearingForm.builder()
            .claimReferenceNumber(caseData.getLegacyCaseReference())
            .emailAddress(caseData.getApplicantSolicitor1CheckEmail())
            .hearingFee(caseData.getHearingFee())
            .hearingDate(caseData.getHearingDate())
            .hearingTime(caseData.getHearingTimeHourMinute())
            .deadlineDate(caseData.getHearingDueDate())
            .claimantReferenceNumber(caseData.getSolicitorReferences().getApplicantSolicitor1Reference())
            .defendantReferenceNumber(caseData.getSolicitorReferences().getRespondentSolicitor1Reference())
            .build();
    }

    private BigDecimal getHearingFee(CaseData caseData) {
            return new BigDecimal(caseData.getHearingFee());
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

}

