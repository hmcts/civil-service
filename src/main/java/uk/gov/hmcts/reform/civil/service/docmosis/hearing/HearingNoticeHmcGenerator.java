package uk.gov.hmcts.reform.civil.service.docmosis.hearing;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.documentmanagement.DocumentManagementService;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType;
import uk.gov.hmcts.reform.civil.documentmanagement.model.PDF;
import uk.gov.hmcts.reform.civil.enums.DocCategory;
import uk.gov.hmcts.reform.civil.enums.PaymentStatus;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.docmosis.DocmosisDocument;
import uk.gov.hmcts.reform.civil.model.docmosis.hearing.HearingNoticeHmc;
import uk.gov.hmcts.reform.civil.referencedata.model.LocationRefData;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates;
import uk.gov.hmcts.reform.civil.service.docmosis.DocumentGeneratorService;
import uk.gov.hmcts.reform.civil.service.docmosis.TemplateDataGenerator;
import uk.gov.hmcts.reform.civil.service.hearings.HearingFeesService;
import uk.gov.hmcts.reform.civil.service.referencedata.LocationReferenceDataService;
import uk.gov.hmcts.reform.civil.utils.AssignCategoryId;
import uk.gov.hmcts.reform.civil.utils.HearingFeeUtils;
import uk.gov.hmcts.reform.civil.utils.HearingUtils;
import uk.gov.hmcts.reform.civil.utils.HmcDataUtils;
import uk.gov.hmcts.reform.hmc.model.hearing.HearingGetResponse;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static java.util.Objects.nonNull;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.HEARING_NOTICE_HMC;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.HEARING_NOTICE_HMC_WELSH;
import static uk.gov.hmcts.reform.civil.utils.DateUtils.formatDateInWelsh;
import static uk.gov.hmcts.reform.civil.utils.HearingUtils.hearingFeeRequired;
import static uk.gov.hmcts.reform.civil.utils.HmcDataUtils.getHearingDaysText;
import static uk.gov.hmcts.reform.civil.utils.HmcDataUtils.getHearingTypeContentText;
import static uk.gov.hmcts.reform.civil.utils.HmcDataUtils.getHearingTypeTitleText;
import static uk.gov.hmcts.reform.civil.utils.HmcDataUtils.getInPersonAttendeeNames;
import static uk.gov.hmcts.reform.civil.utils.HmcDataUtils.getLocationRefData;
import static uk.gov.hmcts.reform.civil.utils.HmcDataUtils.getPhoneAttendeeNames;
import static uk.gov.hmcts.reform.civil.utils.HmcDataUtils.getPluralHearingTypeTextWelsh;
import static uk.gov.hmcts.reform.civil.utils.HmcDataUtils.getTotalHearingDurationText;
import static uk.gov.hmcts.reform.civil.utils.HmcDataUtils.getVideoAttendeesNames;

@Service
@RequiredArgsConstructor
public class HearingNoticeHmcGenerator implements TemplateDataGenerator<HearingNoticeHmc> {

    private final DocumentManagementService documentManagementService;
    private final DocumentGeneratorService documentGeneratorService;
    private final LocationReferenceDataService locationRefDataService;
    private final HearingFeesService hearingFeesService;
    private final AssignCategoryId assignCategoryId;
    private final FeatureToggleService featureToggleService;

    public List<CaseDocument> generate(CaseData caseData, HearingGetResponse hearing, String authorisation, String hearingLocation, String hearingId, DocmosisTemplates template) {

        List<CaseDocument> caseDocuments = new ArrayList<>();
        HearingNoticeHmc templateData = getHearingNoticeTemplateData(caseData, hearing, authorisation, hearingLocation, hearingId, template);
        DocmosisDocument document =
            documentGeneratorService.generateDocmosisDocument(templateData, template);
        PDF pdf =  new PDF(
            getFileName(caseData, template),
            document.getBytes(),
            HEARING_NOTICE_HMC.equals(template) ? DocumentType.HEARING_FORM : DocumentType.HEARING_FORM_WELSH
        );
        CaseDocument caseDocument = documentManagementService.uploadDocument(authorisation, pdf);
        assignCategoryId.assignCategoryIdToCaseDocument(caseDocument, DocCategory.HEARING_NOTICES.getValue());
        caseDocuments.add(caseDocument);

        return caseDocuments;
    }

    public HearingNoticeHmc getHearingNoticeTemplateData(CaseData caseData, HearingGetResponse hearing, String bearerToken,
                                                         String hearingLocation, String hearingId, DocmosisTemplates template) {
        var paymentFailed = (caseData.getHearingFeePaymentDetails() == null
            || caseData.getHearingFeePaymentDetails().getStatus().equals(PaymentStatus.FAILED))
            && !caseData.hearingFeePaymentDoneWithHWF();
        var hearingType = hearing.getHearingDetails().getHearingType();
        var feeAmount = paymentFailed && hearingFeeRequired(hearingType)
            ? HearingUtils.formatHearingFee(HearingFeeUtils.calculateAndApplyFee(hearingFeesService, caseData, caseData.getAssignedTrack())) : null;
        var hearingDueDate = paymentFailed && hearingFeeRequired(hearingType) ? HearingFeeUtils
            .calculateHearingDueDate(LocalDate.now(), HmcDataUtils.getHearingStartDay(hearing)
                .getHearingStartDateTime().toLocalDate()) : null;
        var isWelsh = HEARING_NOTICE_HMC_WELSH.equals(template);
        var creationDate = LocalDate.now();

        LocationRefData caseManagementLocation =
            getLocationRefData(hearingId, caseData.getCaseManagementLocation().getBaseLocation(), bearerToken, locationRefDataService);

        String caseManagementLocationText = "";
        if (nonNull(caseManagementLocation) && isWelsh) {
            caseManagementLocationText = LocationReferenceDataService.getDisplayEntryWelsh(caseManagementLocation);
        } else if (nonNull(caseManagementLocation)) {
            caseManagementLocationText = LocationReferenceDataService.getDisplayEntry(caseManagementLocation);
        }

        return new HearingNoticeHmc()
            .setTitle(getHearingTypeTitleText(caseData, hearing, isWelsh ? true : false))
            .setHearingSiteName(nonNull(caseManagementLocation) ? getExternalShortName(template, caseManagementLocation) : null)
            .setCaseManagementLocation(caseManagementLocationText)
            .setHearingLocation(hearingLocation)
            .setCaseNumber(caseData.getCcdCaseReference())
            .setCreationDate(creationDate)
            .setCreationDateWelshText(isWelsh ? formatDateInWelsh(creationDate, true) : null)
            .setHearingType(getHearingTypeContentText(caseData, hearing, isWelsh))
            .setHearingTypePluralWelsh(isWelsh ? getPluralHearingTypeTextWelsh(caseData, hearing) : null)
            .setClaimant(caseData.getApplicant1().getPartyName())
            .setClaimantReference(nonNull(caseData.getSolicitorReferences())
                                   ? caseData.getSolicitorReferences().getApplicantSolicitor1Reference() : null)
            .setClaimant2(nonNull(caseData.getApplicant2()) ? caseData.getApplicant2().getPartyName() : null)
            .setClaimant2Reference(nonNull(caseData.getApplicant2())
                    && nonNull(caseData.getSolicitorReferences()) ? caseData.getSolicitorReferences().getApplicantSolicitor1Reference() : null)
            .setDefendant(caseData.getRespondent1().getPartyName())
            .setDefendantReference(nonNull(caseData.getSolicitorReferences())
                                    ? caseData.getSolicitorReferences().getRespondentSolicitor1Reference() : null)
            .setDefendant2(nonNull(caseData.getRespondent2()) ? caseData.getRespondent2().getPartyName() : null)
            .setDefendant2Reference(caseData.getRespondentSolicitor2Reference())
            .setHearingDays(getHearingDaysText(hearing, isWelsh))
            .setTotalHearingDuration(getTotalHearingDurationText(hearing, isWelsh))
            .setFeeAmount(feeAmount)
            .setHearingDueDate(hearingDueDate)
            .setHearingDueDateWelshText(isWelsh && nonNull(hearingDueDate)
                                         ? formatDateInWelsh(hearingDueDate, true)
                                         : null)
            .setHearingFeePaymentDetails(caseData.getHearingFeePaymentDetails())
            .setPartiesAttendingInPerson(getInPersonAttendeeNames(hearing))
            .setPartiesAttendingByTelephone(getPhoneAttendeeNames(hearing))
            .setPartiesAttendingByVideo(getVideoAttendeesNames(hearing));
    }

    private String getFileName(CaseData caseData, DocmosisTemplates template) {
        return String.format(template.getDocumentTitle(), caseData.getLegacyCaseReference());
    }

    private String getExternalShortName(DocmosisTemplates template, LocationRefData caseManagementLocation) {
        if (HEARING_NOTICE_HMC_WELSH.equals(template)
            && caseManagementLocation.getWelshExternalShortName() != null
            && !caseManagementLocation.getWelshExternalShortName().isEmpty()) {
            return caseManagementLocation.getWelshExternalShortName();
        }
        if (nonNull(caseManagementLocation)) {
            return caseManagementLocation.getExternalShortName();
        }
        return null;
    }
}

