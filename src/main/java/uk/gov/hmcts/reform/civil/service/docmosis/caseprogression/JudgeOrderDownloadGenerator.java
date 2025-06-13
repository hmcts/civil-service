package uk.gov.hmcts.reform.civil.service.docmosis.caseprogression;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.documentmanagement.DocumentManagementService;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType;
import uk.gov.hmcts.reform.civil.documentmanagement.model.PDF;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.docmosis.DocmosisDocument;
import uk.gov.hmcts.reform.civil.model.docmosis.casepogression.JudgeFinalOrderForm;
import uk.gov.hmcts.reform.civil.referencedata.model.LocationRefData;
import uk.gov.hmcts.reform.civil.service.UserService;
import uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates;
import uk.gov.hmcts.reform.civil.service.docmosis.DocumentGeneratorService;
import uk.gov.hmcts.reform.civil.service.docmosis.DocumentHearingLocationHelper;
import uk.gov.hmcts.reform.civil.service.docmosis.TemplateDataGenerator;
import uk.gov.hmcts.reform.civil.service.docmosis.caseprogression.helpers.JudgeFinalOrderFormPopulator;
import uk.gov.hmcts.reform.civil.service.referencedata.LocationReferenceDataService;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import static java.lang.String.format;
import static java.util.Objects.nonNull;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.formatLocalDate;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.BLANK_TEMPLATE_AFTER_HEARING_DOCX;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.BLANK_TEMPLATE_BEFORE_HEARING_DOCX;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.FIX_DATE_CCMC_DOCX;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.FIX_DATE_CMC_DOCX;

@Service
public class JudgeOrderDownloadGenerator extends JudgeFinalOrderGenerator implements TemplateDataGenerator<JudgeFinalOrderForm> {

    public static final String BLANK_TEMPLATE_TO_BE_USED_AFTER_A_HEARING = "Blank template to be used after a hearing";
    public static final String BLANK_TEMPLATE_TO_BE_USED_BEFORE_A_HEARING_BOX_WORK = "Blank template to be used before a hearing/box work";
    public static final String FIX_A_DATE_FOR_CCMC = "Fix a date for CCMC";
    public static final String FIX_A_DATE_FOR_CMC = "Fix a date for CMC";
    private final DocumentManagementService documentManagementService;
    private final DocumentGeneratorService documentGeneratorService;
    private final UserService userService;
    private final LocationReferenceDataService locationRefDataService;
    private final DocumentHearingLocationHelper documentHearingLocationHelper;
    public DocmosisTemplates docmosisTemplate;
    private static final String DATE_FORMAT = "dd-MM-yyyy";
    public static final String INTERMEDIATE_NO_BAND_NO_REASON = "This case is allocated to the Intermediate Track and is not assigned a complexity band.";
    public static final String INTERMEDIATE_NO_BAND_WITH_REASON = "This case is allocated to the Intermediate Track and is not assigned a complexity band because %s.";
    public static final String INTERMEDIATE_WITH_BAND_NO_REASON = "This case is allocated to the Intermediate Track and is assigned to complexity band %s.";
    public static final String INTERMEDIATE_WITH_BAND_WITH_REASON = "This case is allocated to the Intermediate Track and is assigned to complexity band %s because %s.";
    public static final String ORDER_AFTER_HEARING_ON = "This Order is made following a hearing on %s.";
    public static final String ORDER_AFTER_HEARING_BETWEEN = "This Order is made following a hearing between %s and %s.";
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d MMMM yyyy");

    public JudgeOrderDownloadGenerator(DocumentManagementService documentManagementService, DocumentGeneratorService documentGeneratorService,
                                       UserService userService, LocationReferenceDataService locationRefDataService,
                                       DocumentHearingLocationHelper documentHearingLocationHelper, JudgeFinalOrderFormPopulator judgeFinalOrderFormPopulator) {
        super(documentManagementService, documentGeneratorService, userService,
              locationRefDataService, documentHearingLocationHelper, judgeFinalOrderFormPopulator);
        this.documentManagementService = documentManagementService;
        this.documentGeneratorService = documentGeneratorService;
        this.userService = userService;
        this.locationRefDataService = locationRefDataService;
        this.documentHearingLocationHelper = documentHearingLocationHelper;
    }

    @Override
    public CaseDocument generate(CaseData caseData, String authorisation) {
        JudgeFinalOrderForm templateData = getDownloadTemplate(caseData, authorisation);
        DocmosisDocument docmosisDocument =
            documentGeneratorService.generateDocmosisDocument(templateData, docmosisTemplate, "docx");
        return documentManagementService.uploadDocument(
            authorisation,
            new PDF(
                getDownloadFileName(docmosisTemplate),
                docmosisDocument.getBytes(),
                DocumentType.JUDGE_FINAL_ORDER
            )
        );
    }

    private String getDownloadFileName(DocmosisTemplates docmosisTemplate) {
        return format(docmosisTemplate.getDocumentTitle(),  formatLocalDate(LocalDate.now(), DATE_FORMAT));
    }

    public JudgeFinalOrderForm getDownloadTemplate(CaseData caseData, String authorisation) {

        switch (caseData.getFinalOrderDownloadTemplateOptions().getValue().getLabel()) {
            case BLANK_TEMPLATE_TO_BE_USED_AFTER_A_HEARING:
                docmosisTemplate = BLANK_TEMPLATE_AFTER_HEARING_DOCX;
                return getBlankAfterHearing(caseData, authorisation);
            case BLANK_TEMPLATE_TO_BE_USED_BEFORE_A_HEARING_BOX_WORK:
                docmosisTemplate = BLANK_TEMPLATE_BEFORE_HEARING_DOCX;
                return getBlankBeforeHearing(caseData, authorisation);
            case FIX_A_DATE_FOR_CCMC:
                docmosisTemplate = FIX_DATE_CCMC_DOCX;
                return getFixDateCcmc(caseData, authorisation);
            case FIX_A_DATE_FOR_CMC:
                docmosisTemplate = FIX_DATE_CMC_DOCX;
                return getFixDateCmc(caseData, authorisation);
            default:
                return null;
        }
    }

    public JudgeFinalOrderForm getBlankAfterHearing(CaseData caseData, String authorisation) {
        return getBaseTemplateData(caseData, authorisation)
            .orderAfterHearingDate(getOrderAfterHearingDateText(caseData))
            .build();
    }

    public JudgeFinalOrderForm getBlankBeforeHearing(CaseData caseData, String authorisation) {
        return getBaseTemplateData(caseData, authorisation)
            .claimTrack(caseData.getFinalOrderAllocateToTrack())
            .trackAndComplexityBandText(getTrackAndComplexityText(caseData))
            .build();
    }

    public JudgeFinalOrderForm getFixDateCcmc(CaseData caseData, String authorisation) {
        return getBaseTemplateData(caseData, authorisation).build();
    }

    public JudgeFinalOrderForm getFixDateCmc(CaseData caseData, String authorisation) {
        return getBaseTemplateData(caseData, authorisation)
            .claimTrack(caseData.getFinalOrderAllocateToTrack())
            .trackAndComplexityBandText(getTrackAndComplexityText(caseData))
            .build();
    }

    private JudgeFinalOrderForm.JudgeFinalOrderFormBuilder getBaseTemplateData(CaseData caseData,
                                                                               String authorisation) {
        UserDetails userDetails = userService.getUserDetails(authorisation);
        LocationRefData caseManagementLocationDetails = documentHearingLocationHelper
            .getCaseManagementLocationDetailsNro(caseData, locationRefDataService, authorisation);

        return JudgeFinalOrderForm.builder()
            .judgeNameTitle(userDetails.getFullName())
            .courtName(caseManagementLocationDetails.getExternalShortName())
            .caseNumber(nonNull(caseData.getCcdCaseReference()) ? caseData.getCcdCaseReference().toString() : null)
            .claimant1Name(caseData.getApplicant1().getPartyName())
            .claimant2Name(nonNull(caseData.getApplicant2()) ? caseData.getApplicant2().getPartyName() : null)
            .defendant1Name(caseData.getRespondent1().getPartyName())
            .defendant2Name(nonNull(caseData.getRespondent2()) ? caseData.getRespondent2().getPartyName() : null)
            .claimantNum(nonNull(caseData.getApplicant2()) ? "Claimant 1" : "Claimant")
            .defendantNum(nonNull(caseData.getRespondent2()) ? "Defendant 1" : "Defendant")
            .dateNowPlus7(LocalDate.now().plusDays(7).format(formatter));
    }

    public String getTrackAndComplexityText(CaseData caseData) {
        if (nonNull(caseData.getFinalOrderAllocateToTrack())
            && caseData.getFinalOrderAllocateToTrack().equals(YES)) {
            return switch (caseData.getFinalOrderTrackAllocation()) {
                case INTERMEDIATE_CLAIM -> getIntermediateClaimTrackAndComplexityText(caseData);
                case MULTI_CLAIM -> "This case is allocated to the multi-track.";
                default -> null;
            };
        }
        return null;
    }

    private String getIntermediateClaimTrackAndComplexityText(CaseData caseData) {
        String complexityBand = getComplexityBand(caseData);
        if (caseData.getFinalOrderIntermediateTrackComplexityBand().getAssignComplexityBand().equals(NO)) {
            return nonNull(caseData.getFinalOrderIntermediateTrackComplexityBand().getReasons())
                ? format(INTERMEDIATE_NO_BAND_WITH_REASON, caseData.getFinalOrderIntermediateTrackComplexityBand().getReasons())
                : INTERMEDIATE_NO_BAND_NO_REASON;
        } else {
            return  nonNull(caseData.getFinalOrderIntermediateTrackComplexityBand().getReasons())
                ? format(INTERMEDIATE_WITH_BAND_WITH_REASON, complexityBand, caseData.getFinalOrderIntermediateTrackComplexityBand().getReasons())
                : format(INTERMEDIATE_WITH_BAND_NO_REASON, complexityBand);
        }
    }

    public String getComplexityBand(CaseData caseData) {
        if (nonNull(caseData.getFinalOrderIntermediateTrackComplexityBand())
            && caseData.getFinalOrderIntermediateTrackComplexityBand().getAssignComplexityBand().equals(YES)) {
            return switch (caseData.getFinalOrderIntermediateTrackComplexityBand().getBand()) {
                case BAND_1 -> "1";
                case BAND_2 -> "2";
                case BAND_3 -> "3";
                case BAND_4 -> "4";
                default -> null;
            };
        }
        return null;
    }

    private String getOrderAfterHearingDateText(CaseData caseData) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMMM yyyy", Locale.ENGLISH);
        return switch (caseData.getOrderAfterHearingDate().getDateType()) {
            case SINGLE_DATE -> format(ORDER_AFTER_HEARING_ON, caseData.getOrderAfterHearingDate().getDate().format(formatter));
            case DATE_RANGE -> format(ORDER_AFTER_HEARING_BETWEEN, caseData.getOrderAfterHearingDate().getFromDate().format(formatter),
                                      caseData.getOrderAfterHearingDate().getToDate().format(formatter));
            case BESPOKE_RANGE -> format(ORDER_AFTER_HEARING_ON, caseData.getOrderAfterHearingDate().getBespokeDates());
        };
    }
}
