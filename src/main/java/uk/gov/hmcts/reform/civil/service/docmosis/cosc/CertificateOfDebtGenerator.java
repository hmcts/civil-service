package uk.gov.hmcts.reform.civil.service.docmosis.cosc;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.documentmanagement.DocumentManagementService;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType;
import uk.gov.hmcts.reform.civil.documentmanagement.model.PDF;
import uk.gov.hmcts.reform.civil.helpers.DateFormatHelper;
import uk.gov.hmcts.reform.civil.helpers.judgmentsonline.JudgmentsOnlineHelper;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.docmosis.DocmosisDocument;
import uk.gov.hmcts.reform.civil.model.docmosis.cosc.CertificateOfDebtForm;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentDetails;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentState;
import uk.gov.hmcts.reform.civil.referencedata.model.LocationRefData;
import uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates;
import uk.gov.hmcts.reform.civil.service.docmosis.DocumentGeneratorService;
import uk.gov.hmcts.reform.civil.service.docmosis.TemplateDataGenerator;
import uk.gov.hmcts.reform.civil.service.referencedata.LocationReferenceDataService;
import uk.gov.hmcts.reform.civil.utils.AddressUtils;
import uk.gov.hmcts.reform.civil.utils.MonetaryConversions;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

import static uk.gov.hmcts.reform.civil.utils.DateUtils.formatDateInWelsh;

@Slf4j
@Service
@RequiredArgsConstructor
public class CertificateOfDebtGenerator implements TemplateDataGenerator<CertificateOfDebtForm> {

    public static final String MARKED_TO_SHOW_THAT_THE_DEBT_IS_SATISFIED = "Marked to show that the debt is satisfied.";
    public static final String REMOVED_AS_PAYMENT_HAS_BEEN_MADE_IN_FULL_WITHIN_ONE_MONTH_OF_JUDGMENT = "REMOVED (as payment has been made in full within one month of judgment).";
    public static final String MARKED_TO_SHOW_THAT_THE_DEBT_IS_SATISFIED_WELSH = "Wedi'i farcio i ddangos bod y ddyled wedi’i thalu.";
    public static final String REMOVED_AS_PAYMENT_HAS_BEEN_MADE_IN_FULL_WITHIN_ONE_MONTH_OF_JUDGMENT_WELSH
        = "DILËWYD (gan fod taliad wedi'i wneud yn llawn o fewn mis i'r dyfarniad).";
    private final DocumentManagementService documentManagementService;
    private final DocumentGeneratorService documentGeneratorService;
    private final LocationReferenceDataService locationRefDataService;

    public CaseDocument generateDoc(CaseData caseData, String authorisation, DocmosisTemplates docmosisTemplate) {
        CertificateOfDebtForm templateData = getCertificateOfDebtTemplateData(caseData, authorisation);
        DocmosisDocument docmosisDocument =
            documentGeneratorService.generateDocmosisDocument(templateData, docmosisTemplate);

        return documentManagementService.uploadDocument(
            authorisation,
            new PDF(
                getFileName(caseData, docmosisTemplate),
                docmosisDocument.getBytes(),
                DocumentType.CERTIFICATE_OF_DEBT_PAYMENT
            )
        );
    }

    private String getFileName(CaseData caseData, DocmosisTemplates docmosisTemplate) {
        return String.format(docmosisTemplate.getDocumentTitle(), caseData.getLegacyCaseReference());
    }

    private CertificateOfDebtForm getCertificateOfDebtTemplateData(CaseData caseData, String authorisation) {
        var certificateOfDebtForm = new CertificateOfDebtForm()
            .setClaimNumber(caseData.getLegacyCaseReference())
            .setDefendantFullName(caseData.getRespondent1().getPartyName())
            .setDefendantAddress(AddressUtils.formatAddress(caseData.getRespondent1().getPrimaryAddress()))
            .setJudgmentOrderDate(Objects.isNull(caseData.getActiveJudgment().getIssueDate()) ? null : DateFormatHelper.formatLocalDate(
                caseData.getActiveJudgment().getIssueDate(),
                DateFormatHelper.DATE
            ))
            .setJudgmentTotalAmount(getJudgmentAmount(caseData))
            .setDefendantFullNameFromJudgment(caseData.getActiveJudgment().getDefendant1Name())
            .setDefendantAddressFromJudgment(JudgmentsOnlineHelper
                                              .formatAddress(caseData.getActiveJudgment().getDefendant1Address()))
            .setApplicationIssuedDate(DateFormatHelper.formatLocalDate(LocalDate.now(), DateFormatHelper.DATE))
            .setDateFinalPaymentMade(Objects.isNull(caseData.getActiveJudgment().getFullyPaymentMadeDate()) ? null : DateFormatHelper.formatLocalDate(
                caseData.getActiveJudgment().getFullyPaymentMadeDate(),
                DateFormatHelper.DATE
            ))
            .setJudgmentStatusText(getJudgmentText(caseData.getActiveJudgment(), false))
            .setCourtLocationName(getCourtName(caseData, authorisation))
            .setJudgmentStatusWelshText(getJudgmentText(caseData.getActiveJudgment(), true))
            .setJudgmentOrderDateWelsh(getDateInWelsh(caseData.getActiveJudgment().getIssueDate()))
            .setDateFinalPaymentMadeWelsh(getDateInWelsh(caseData.getActiveJudgment().getFullyPaymentMadeDate()))
            .setApplicationIssuedDateWelsh(getDateInWelsh(LocalDate.now()));

        return certificateOfDebtForm;
    }

    private String getJudgmentText(JudgmentDetails activeJudgment, boolean isBilingual) {

        if (JudgmentState.SATISFIED.equals(activeJudgment.getState())) {
            return isBilingual ? MARKED_TO_SHOW_THAT_THE_DEBT_IS_SATISFIED_WELSH
                : MARKED_TO_SHOW_THAT_THE_DEBT_IS_SATISFIED;
        } else if (JudgmentState.CANCELLED.equals(activeJudgment.getState())) {
            return isBilingual ? REMOVED_AS_PAYMENT_HAS_BEEN_MADE_IN_FULL_WITHIN_ONE_MONTH_OF_JUDGMENT_WELSH
                : REMOVED_AS_PAYMENT_HAS_BEEN_MADE_IN_FULL_WITHIN_ONE_MONTH_OF_JUDGMENT;
        }
        return null;
    }

    private String getCourtName(CaseData caseData, String authorisation) {

        if (caseData.getLocationName() != null) {
            return caseData.getLocationName();
        } else {
            List<LocationRefData> locationDetails = locationRefDataService.getCourtLocationsByEpimmsId(authorisation, caseData.getCaseManagementLocation().getBaseLocation());
            if (locationDetails != null && !locationDetails.isEmpty()) {
                return locationDetails.get(0).getCourtName();
            }
        }
        return null;
    }

    private String getJudgmentAmount(CaseData caseData) {
        return Objects.isNull(caseData.getActiveJudgment().getTotalAmount()) ? null : MonetaryConversions.penniesToPounds(
            new BigDecimal(caseData.getActiveJudgment().getTotalAmount())).toString();

    }

    protected String getDateInWelsh(LocalDate date) {
        return Objects.isNull(date) ? null : formatDateInWelsh(date, false);
    }
}
