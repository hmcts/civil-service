package uk.gov.hmcts.reform.civil.service.docmosis.caseprogression;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.documentmanagement.DocumentManagementService;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType;
import uk.gov.hmcts.reform.civil.documentmanagement.model.PDF;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.docmosis.DocmosisDocument;
import uk.gov.hmcts.reform.civil.model.docmosis.casepogression.JudgeFinalOrderForm;
import uk.gov.hmcts.reform.civil.referencedata.model.LocationRefData;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.UserService;
import uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates;
import uk.gov.hmcts.reform.civil.service.docmosis.DocumentGeneratorService;
import uk.gov.hmcts.reform.civil.service.docmosis.DocumentHearingLocationHelper;
import uk.gov.hmcts.reform.civil.service.docmosis.TemplateDataGenerator;
import uk.gov.hmcts.reform.civil.service.referencedata.LocationReferenceDataService;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import java.time.LocalDate;

import static java.lang.String.format;
import static java.util.Objects.nonNull;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.formatLocalDate;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.BLANK_TEMPLATE_AFTER_HEARING_DOCX;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.BLANK_TEMPLATE_BEFORE_HEARING_DOCX;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.FIX_DATE_CCMC_DOCX;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.FIX_DATE_CMC_DOCX;

@Slf4j
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
    public static final String INTERMEDIATE_NO_BAND_NO_REASON = "This case is allocated to the Intermediate Track and is not allocated a complexity band.";
    public static final String INTERMEDIATE_NO_BAND_WITH_REASON = "This case is allocated to the Intermediate Track and is not allocated a complexity band because %s.";
    public static final String INTERMEDIATE_WITH_BAND_NO_REASON = "This case is allocated to the Intermediate Track and is allocated to complexity band %s.";
    public static final String INTERMEDIATE_WITH_BAND_WITH_REASON = "This case is allocated to the Intermediate Track and is allocated to complexity band %s because %s.";
    public static final String FAST_NO_BAND_NO_REASON = "This case is allocated to the Fast Track and is not allocated a complexity band.";
    public static final String FAST_NO_BAND_WITH_REASON = "This case is allocated to the Fast Track and is not allocated a complexity band because %s.";
    public static final String FAST_WITH_BAND_NO_REASON = "This case is allocated to the Fast Track and is allocated to complexity band %s.";
    public static final String FAST_WITH_BAND_WITH_REASON = "This case is allocated to the Fast Track and is allocated to complexity band %s because %s.";

    public JudgeOrderDownloadGenerator(DocumentManagementService documentManagementService, DocumentGeneratorService documentGeneratorService,
                                       UserService userService, LocationReferenceDataService locationRefDataService, FeatureToggleService featureToggleService,
                                       DocumentHearingLocationHelper documentHearingLocationHelper) {
        super(documentManagementService, documentGeneratorService, userService, locationRefDataService, featureToggleService, documentHearingLocationHelper);
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
        return getBaseTemplateData(caseData, authorisation).build();
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

        log.info("CCD ref is {}", caseData.getCcdCaseReference());
        return JudgeFinalOrderForm.builder()
            .judgeNameTitle(userDetails.getFullName())
            .courtName(caseManagementLocationDetails.getExternalShortName())
            .caseNumber(nonNull(caseData.getCcdCaseReference()) ? caseData.getCcdCaseReference().toString() : "empty for some reason")
            .claimant1Name(caseData.getApplicant1().getPartyName())
            .claimant2Name(nonNull(caseData.getApplicant2()) ? caseData.getApplicant2().getPartyName() : null)
            .defendant1Name(caseData.getRespondent1().getPartyName())
            .defendant2Name(nonNull(caseData.getRespondent2()) ? caseData.getRespondent2().getPartyName() : null)
            .claimantNum(nonNull(caseData.getApplicant2()) ? "Claimant 1" : "Claimant")
            .defendantNum(nonNull(caseData.getRespondent2()) ? "Defendant 1" : "Defendant");
    }

    public String getTrackAndComplexityText(CaseData caseData) {
        if (nonNull(caseData.getFinalOrderAllocateToTrack())
            && caseData.getFinalOrderAllocateToTrack().equals(YES)) {
            return switch (caseData.getFinalOrderTrackAllocation()) {
                case SMALL_CLAIM -> "This case is allocated to the Small Claims.";
                case FAST_CLAIM -> getFastClaimTrackAndComplexityText(caseData);
                case INTERMEDIATE_CLAIM -> getIntermediateClaimTrackAndComplexityText(caseData);
                case MULTI_CLAIM -> "This case is allocated to the Multi Track.";
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

    private String getFastClaimTrackAndComplexityText(CaseData caseData) {
        String complexityBand = getComplexityBand(caseData);
        if (caseData.getFinalOrderFastTrackComplexityBand().getAssignComplexityBand().equals(NO)) {
            return nonNull(caseData.getFinalOrderFastTrackComplexityBand().getReasons())
                ? format(FAST_NO_BAND_WITH_REASON, caseData.getFinalOrderFastTrackComplexityBand().getReasons())
                : FAST_NO_BAND_NO_REASON;
        } else {
            return  nonNull(caseData.getFinalOrderFastTrackComplexityBand().getReasons())
                ? format(FAST_WITH_BAND_WITH_REASON, complexityBand, caseData.getFinalOrderFastTrackComplexityBand().getReasons())
                : format(FAST_WITH_BAND_NO_REASON, complexityBand);
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
        if (nonNull(caseData.getFinalOrderFastTrackComplexityBand())
            && caseData.getFinalOrderFastTrackComplexityBand().getAssignComplexityBand().equals(YES)) {
            return switch (caseData.getFinalOrderFastTrackComplexityBand().getBand()) {
                case BAND_1 -> "1";
                case BAND_2 -> "2";
                case BAND_3 -> "3";
                case BAND_4 -> "4";
                default -> null;
            };
        }
        return null;
    }

}
