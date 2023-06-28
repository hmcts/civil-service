package uk.gov.hmcts.reform.civil.service.docmosis.hearing;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.documentmanagement.DocumentManagementService;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType;
import uk.gov.hmcts.reform.civil.documentmanagement.model.PDF;
import uk.gov.hmcts.reform.civil.enums.PaymentStatus;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.docmosis.DocmosisDocument;
import uk.gov.hmcts.reform.civil.model.docmosis.hearing.HearingNoticeHmc;
import uk.gov.hmcts.reform.civil.referencedata.LocationRefDataService;
import uk.gov.hmcts.reform.civil.referencedata.model.LocationRefData;
import uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates;
import uk.gov.hmcts.reform.civil.service.docmosis.DocumentGeneratorService;
import uk.gov.hmcts.reform.civil.service.docmosis.TemplateDataGenerator;
import uk.gov.hmcts.reform.civil.service.hearings.HearingFeesService;
import uk.gov.hmcts.reform.civil.utils.HearingFeeUtils;
import uk.gov.hmcts.reform.civil.utils.HearingUtils;
import uk.gov.hmcts.reform.civil.utils.HmcDataUtils;
import uk.gov.hmcts.reform.hmc.model.hearing.HearingGetResponse;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static java.util.Objects.nonNull;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.HEARING_NOTICE_HMC;
import static uk.gov.hmcts.reform.civil.utils.HmcDataUtils.getHearingDaysText;
import static uk.gov.hmcts.reform.civil.utils.HmcDataUtils.getTotalHearingDurationText;

@Service
@RequiredArgsConstructor
public class HearingNoticeHmcGenerator implements TemplateDataGenerator<HearingNoticeHmc> {

    private final DocumentManagementService documentManagementService;
    private final DocumentGeneratorService documentGeneratorService;
    private final LocationRefDataService locationRefDataService;
    private final HearingFeesService hearingFeesService;

    public List<CaseDocument> generate(CaseData caseData, HearingGetResponse hearing, String authorisation) {

        List<CaseDocument> caseDocuments = new ArrayList<>();
        HearingNoticeHmc templateData = getHearingNoticeTemplateData(caseData, hearing, authorisation);
        DocmosisTemplates template = getTemplate(caseData);
        DocmosisDocument document =
            documentGeneratorService.generateDocmosisDocument(templateData, template);
        PDF pdf =  new PDF(
            getFileName(caseData, template),
            document.getBytes(),
            DocumentType.HEARING_FORM
        );
        caseDocuments.add(documentManagementService.uploadDocument(authorisation, pdf));
        return caseDocuments;
    }

    public HearingNoticeHmc getHearingNoticeTemplateData(CaseData caseData, HearingGetResponse hearing, String bearerToken) {
        var paymentFailed = caseData.getHearingFeePaymentDetails() == null
            || caseData.getHearingFeePaymentDetails().getStatus().equals(PaymentStatus.FAILED);
        var feeAmount = paymentFailed
            ? HearingUtils.formatHearingFee(HearingFeeUtils.calculateAndApplyFee(hearingFeesService, caseData, caseData.getAllocatedTrack())) : null;
        var hearingDueDate = paymentFailed ? HearingFeeUtils
            .calculateHearingDueDate(LocalDate.now(), HmcDataUtils.getHearingStartDay(hearing)
                .getHearingStartDateTime().toLocalDate()) : null;

        return HearingNoticeHmc.builder()
            .hearingLocation(getHearingLocation(HmcDataUtils.getHearingStartDay(hearing).getHearingVenueId(), bearerToken))
            .caseNumber(caseData.getCcdCaseReference())
            .creationDate(LocalDate.now())
            .hearingType(getHearingType(hearing))
            .claimant(caseData.getApplicant1().getPartyName())
            .claimantReference(caseData.getSolicitorReferences().getApplicantSolicitor1Reference())
            .claimant2(nonNull(caseData.getApplicant2()) ? caseData.getApplicant2().getPartyName() : null)
            .claimant2Reference(nonNull(caseData.getApplicant2()) ? caseData.getSolicitorReferences().getApplicantSolicitor1Reference() : null)
            .defendant(caseData.getRespondent1().getPartyName())
            .defendantReference(caseData.getSolicitorReferences().getRespondentSolicitor1Reference())
            .defendant2(nonNull(caseData.getRespondent2()) ? caseData.getRespondent2().getPartyName() : null)
            .defendant2Reference(caseData.getSolicitorReferences().getRespondentSolicitor2Reference())
            .hearingDays(getHearingDaysText(hearing))
            .totalHearingDuration(getTotalHearingDurationText(hearing))
            .feeAmount(feeAmount)
            .hearingDueDate(hearingDueDate)
            .hearingFeePaymentDetails(caseData.getHearingFeePaymentDetails())
            .build();
    }

    private String getFileName(CaseData caseData, DocmosisTemplates template) {
        return String.format(template.getDocumentTitle(), caseData.getLegacyCaseReference());
    }

    private DocmosisTemplates getTemplate(CaseData caseData) {
        return HEARING_NOTICE_HMC;
    }

    private String getHearingLocation(String venueId, String bearerToken) {
        List<LocationRefData> locations = locationRefDataService.getCourtLocationsForDefaultJudgments(bearerToken);
        var location = locations.stream().filter(loc -> loc.getEpimmsId().equals(venueId)).toList();

        return LocationRefDataService.getDisplayEntry(location.get(0));
    }

    private String getHearingType(HearingGetResponse hearing) {
        if (hearing.getHearingDetails().getHearingType().contains("TRI")) {
            return "trial";
        } else if (hearing.getHearingDetails().getHearingType().contains("DIS")) {
            return "hearing";
        }
        return null;
    }

}

