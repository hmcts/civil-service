package uk.gov.hmcts.reform.civil.service.docmosis.hearing;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.docmosis.DocmosisDocument;
import uk.gov.hmcts.reform.civil.model.docmosis.hearing.HearingForm;
import uk.gov.hmcts.reform.civil.model.documents.CaseDocument;
import uk.gov.hmcts.reform.civil.model.documents.DocumentType;
import uk.gov.hmcts.reform.civil.model.documents.PDF;
import uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates;
import uk.gov.hmcts.reform.civil.service.docmosis.DocumentGeneratorService;
import uk.gov.hmcts.reform.civil.service.docmosis.TemplateDataGenerator;
import uk.gov.hmcts.reform.civil.service.documentmanagement.DocumentManagementService;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.HEARING_APPLICATION;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.HEARING_FAST_TRACK;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.HEARING_OTHER;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.HEARING_SMALL_CLAIMS;
import static uk.gov.hmcts.reform.civil.utils.HearingUtils.getHearingDuration;
import static uk.gov.hmcts.reform.civil.utils.HearingUtils.getHearingType;

@Service
@RequiredArgsConstructor
public class HearingFormGenerator implements TemplateDataGenerator<HearingForm> {

    private final DocumentManagementService documentManagementService;
    private final DocumentGeneratorService documentGeneratorService;

    public List<CaseDocument> generate(CaseData caseData, String authorisation, String event) {

        List<CaseDocument> caseDocuments = new ArrayList<>();
        List<HearingForm> templateData = getHearingForms(caseData, event);
        DocmosisTemplates template = getTemplate(caseData);
        DocmosisDocument document =
            documentGeneratorService.generateDocmosisDocument(templateData.get(0), template);
        caseDocuments.add(documentManagementService.uploadDocument(
            authorisation,
            new PDF(
                getFileName(caseData, template),
                document.getBytes(),
                DocumentType.HEARING_FORM
            )
        ));
        return caseDocuments;
    }

    @Override
    public HearingForm getTemplateData(CaseData caseData) throws IOException {

        return HearingForm.builder()
            .court(caseData.getHearingLocation().getValue().getLabel())
            .caseNumber(caseData.getLegacyCaseReference())
            .creationDate(LocalDate.now())
            .claimant(caseData.getApplicant1().getPartyName())
            .nthClaimant("1st Claimant")
            .claimantReference("TBC")
            .defendant(caseData.getRespondent1().getPartyName())
            .defendantReference("TBC")
            .hearingDate(caseData.getDateOfApplication())
            .hearingTime(caseData.getHearingTimeHourMinute())
            .hearingType(getHearingType(caseData))
            .duration(getHearingDuration(caseData))
            .additionalInfo(caseData.getHearingAdditionalInformation())
            .feeAmount(caseData.getHearingFee())
            .hearingDueDate(caseData.getHearingDueDate())
            .additionalText(caseData.getHearingNoticeListOther()).build();

    }

    private String getFileName(CaseData caseData, DocmosisTemplates template) {
        return String.format(template.getDocumentTitle(), caseData.getLegacyCaseReference());
    }

    private List<HearingForm> getHearingForms(CaseData caseData, String event) {
        List<HearingForm> hearingForms = new ArrayList<>();
        hearingForms.add(getHearingForm(caseData, caseData.getRespondent1(), event));

        return hearingForms;
    }

    private HearingForm getHearingForm(CaseData caseData,
                                                       uk.gov.hmcts.reform.civil.model.Party respondent,
                                                       String event) {
        return HearingForm.builder()
            .caseNumber(caseData.getLegacyCaseReference()).build();

    }

    private DocmosisTemplates getTemplate(CaseData caseData) {
        switch (caseData.getHearingNoticeList()) {
            case SMALL_CLAIMS:
                return HEARING_SMALL_CLAIMS;
            case FAST_TRACK_TRIAL:
                return HEARING_FAST_TRACK;
            case HEARING_OF_APPLICATION:
                return HEARING_APPLICATION;
            default:
                return HEARING_OTHER;
        }
    }


}

