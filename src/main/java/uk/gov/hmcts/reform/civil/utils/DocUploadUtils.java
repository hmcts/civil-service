package uk.gov.hmcts.reform.civil.utils;

import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;

import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;
import uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.genapplication.GASolicitorDetailsGAspec;
import uk.gov.hmcts.reform.civil.model.genapplication.UploadDocumentByType;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class DocUploadUtils {

    public static final String APPLICANT = "Applicant";
    public static final String RESPONDENT_ONE = "Respondent One";
    public static final String RESPONDENT_TWO = "Respondent Two";

    private DocUploadUtils() {

    }

    public static void addUploadDocumentByTypeToAddl(CaseData caseData, CaseData.CaseDataBuilder<?, ?> caseDataBuilder,
                                                     List<Element<UploadDocumentByType>> source, String role,
                                                     boolean updateScheduler) {
        caseDataBuilder.isDocumentVisible(DocUploadUtils.isDocumentVisible(caseData));
        List<Element<CaseDocument>> docs = prepareUploadDocumentByType(source, role);
        addToAddl(caseData, caseDataBuilder, docs, role, updateScheduler);
    }

    public static List<Element<CaseDocument>> prepareUploadDocumentByType(List<Element<UploadDocumentByType>> source,
                                                                          final String role) {
        return source.stream()
                .map(uploadDocumentByTypeElement -> ElementUtils.element(CaseDocument.builder()
                        .documentLink(uploadDocumentByTypeElement.getValue()
                                .getAdditionalDocument().toBuilder().categoryID(AssignCategoryId.APPLICATIONS)
                                .build())
                        .documentName(uploadDocumentByTypeElement.getValue().getDocumentType())
                        .createdBy(role)
                        .createdDatetime(LocalDateTime.now()).build()))
                .toList();
    }

    public static void addDocumentToAddl(CaseData caseData, CaseData.CaseDataBuilder<?, ?> caseDataBuilder,
                                         List<Element<Document>> source, String role, CaseEvent event,
                                         boolean updateScheduler) {
        if (Objects.isNull(source) || source.isEmpty()) {
            return;
        }
        caseDataBuilder.isDocumentVisible(DocUploadUtils.isDocumentVisible(caseData));
        List<Element<CaseDocument>> docs = prepareDocuments(source, role, event);
        addToAddl(caseData, caseDataBuilder, docs, role, updateScheduler);
    }

    public static void addToAddl(CaseData caseData, CaseData.CaseDataBuilder<?, ?> caseDataBuilder,
                                 List<Element<CaseDocument>> tobeAdded, String role,
                                 boolean updateScheduler) {
        if (role.equals(DocUploadUtils.APPLICANT)) {
            caseDataBuilder.gaAddlDocClaimant(addDocuments(tobeAdded, caseData.getGaAddlDocClaimant()));
            if (updateScheduler) {
                caseDataBuilder.caseDocumentUploadDate(LocalDateTime.now());
            }
        } else if (role.equals(DocUploadUtils.RESPONDENT_ONE)) {
            caseDataBuilder.gaAddlDocRespondentSol(addDocuments(tobeAdded, caseData.getGaAddlDocRespondentSol()));
            if (updateScheduler) {
                caseDataBuilder.caseDocumentUploadDateRes(LocalDateTime.now());
            }
        } else {
            caseDataBuilder.gaAddlDocRespondentSolTwo(addDocuments(tobeAdded, caseData.getGaAddlDocRespondentSolTwo()));
            if (updateScheduler) {
                caseDataBuilder.caseDocumentUploadDateRes(LocalDateTime.now());
            }
        }
        caseDataBuilder.gaAddlDoc(addDocuments(tobeAdded, caseData.getGaAddlDoc()));
        caseDataBuilder.gaAddlDocStaff(addDocuments(tobeAdded, caseData.getGaAddlDocStaff()));
    }

    public static void addDocumentToPreTranslation(CaseData caseData, CaseData.CaseDataBuilder<?, ?> caseDataBuilder,
                                                   List<Element<Document>> source, String role, CaseEvent event) {
        if (Objects.isNull(source) || source.isEmpty()) {
            return;
        }
        caseDataBuilder.isDocumentVisible(DocUploadUtils.isDocumentVisible(caseData));
        List<Element<CaseDocument>> docs = prepareDocuments(source, role, event);
        if (docs.size() > 1) {
            addToAddl(caseData, caseDataBuilder, docs.subList(0, docs.size() - 1), role, false);
        }
        caseDataBuilder.preTranslationGaDocuments(addDocuments(docs.subList(docs.size() - 1, docs.size()), caseData.getPreTranslationGaDocuments()));
        if (role.equals(APPLICANT)) {
            caseDataBuilder.preTranslationGaDocsApplicant(addDocuments(docs.subList(docs.size() - 1, docs.size()), caseData.getPreTranslationGaDocsApplicant()));
        } else if (role.equals(RESPONDENT_ONE)) {
            caseDataBuilder.preTranslationGaDocsRespondent(addDocuments(docs.subList(docs.size() - 1, docs.size()), caseData.getPreTranslationGaDocsRespondent()));
        }
    }

    public static String getUserRole(CaseData caseData, String userId) {
        if (caseData.getParentClaimantIsApplicant().equals(YesOrNo.YES) && caseData.getGeneralAppApplnSolicitor().getId().equals(userId)
                || (caseData.getParentClaimantIsApplicant().equals(YesOrNo.NO) && caseData.getGeneralAppApplnSolicitor().getId().equals(userId))
                || (caseData.getGeneralAppApplicantAddlSolicitors() != null
                && caseData.getGeneralAppApplicantAddlSolicitors().stream().filter(appSolUser -> appSolUser.getValue().getId()
                .equals(userId)).toList().size() == 1)) {
            return APPLICANT;
        } else if (caseData.getGeneralAppRespondentSolicitors() != null && isLipRespondent(caseData)) {
            return RESPONDENT_ONE;
        } else if (caseData.getGeneralAppRespondentSolicitors() != null) {
            String orgID = caseData.getGeneralAppRespondentSolicitors().get(0).getValue().getOrganisationIdentifier();
            List<Element<GASolicitorDetailsGAspec>> resp1SolList = caseData.getGeneralAppRespondentSolicitors().stream()
                    .filter(gaRespondentSolElement -> gaRespondentSolElement.getValue().getOrganisationIdentifier()
                            .equals(orgID)).toList();

            if (resp1SolList.stream().filter(respSolicitorUser -> respSolicitorUser.getValue().getId().equals(userId)).toList().size() == 1) {
                return RESPONDENT_ONE;
            } else {
                return RESPONDENT_TWO;
            }
        }
        throw new RuntimeException("Unknown Role");
    }

    public static List<Element<CaseDocument>> addDocuments(List<Element<CaseDocument>> source,
                                                           List<Element<CaseDocument>> target) {
        if (Objects.isNull(source) || source.isEmpty()) {
            return target;
        }
        if (Objects.isNull(target)) {
            target = new ArrayList<>();
        }
        List<UUID> ids = target.stream().map(Element::getId).toList();
        List<Element<CaseDocument>> newDocs = source.stream().filter(doc -> !ids.contains(doc.getId())).toList();
        target.addAll(newDocs);
        return target;
    }

    public static List<Element<CaseDocument>> prepareDocuments(List<Element<Document>> source,
                                                               String role, CaseEvent event) {
        if (Objects.isNull(source)) {
            return null;
        }
        DocumentType documentType = getDocumentType(event);
        String documentName = getDocumentName(event);
        return source.stream()
                .map(doc -> ElementUtils.element(CaseDocument.builder()
                        .documentLink(doc.getValue().toBuilder()
                                .categoryID(AssignCategoryId.APPLICATIONS).build())
                        .documentName(documentName)
                        .documentType(documentType)
                        .createdBy(role)
                        .createdDatetime(LocalDateTime.now()).build()))
                .toList();
    }

    public static DocumentType getDocumentType(CaseEvent event) {
        switch (event) {

            case RESPOND_TO_JUDGE_ADDITIONAL_INFO:
                return DocumentType.REQUEST_FOR_INFORMATION;
            case RESPOND_TO_JUDGE_WRITTEN_REPRESENTATION:
                return DocumentType.WRITTEN_REPRESENTATION_SEQUENTIAL;
            default:
                return null;
        }
    }

    public static String getDocumentName(CaseEvent event) {
        switch (event) {
            case INITIATE_GENERAL_APPLICATION:
                return "Supporting evidence";
            case RESPOND_TO_JUDGE_ADDITIONAL_INFO:
                return "Additional information";
            case RESPOND_TO_JUDGE_DIRECTIONS:
                return "Directions order";
            case RESPOND_TO_JUDGE_WRITTEN_REPRESENTATION:
                return "Written representation";
            case RESPOND_TO_APPLICATION:
                return "Respond evidence";
            default:
                return "Unsupported event";
        }
    }

    public static YesOrNo isDocumentVisible(CaseData caseData) {
        if (JudicialDecisionNotificationUtil.isWithNotice(caseData) || JudicialDecisionNotificationUtil.isNonUrgent(caseData)
                || JudicialDecisionNotificationUtil.isGeneralAppConsentOrder(caseData)
                || (Objects.nonNull(caseData.getApplicationIsCloaked()) && caseData.getApplicationIsCloaked().equals(NO))) {
            return YesOrNo.YES;
        } else {
            return YesOrNo.NO;
        }
    }

    private static boolean isLipRespondent(CaseData caseData) {
        return Objects.nonNull(caseData.getIsGaRespondentOneLip()) && caseData.getIsGaRespondentOneLip().equals(YES);
    }

    public static void setRespondedValues(CaseData.CaseDataBuilder<?, ?> caseDataBuilder, String role) {
        if (role.equals(DocUploadUtils.APPLICANT)) {
            caseDataBuilder.isApplicantResponded(YES);
            caseDataBuilder.isRespondentResponded(null);
        } else {
            caseDataBuilder.isRespondentResponded(YES);
            caseDataBuilder.isApplicantResponded(null);
        }
    }

    public static boolean uploadedDocumentAwaitingTranslation(CaseData caseData, String role, String documentName) {
        if (caseData.getPreTranslationGaDocuments() == null) {
            return false;
        }
        return caseData.getPreTranslationGaDocuments().stream().anyMatch(
                element -> role.equals(element.getValue().getCreatedBy()) && documentName.equals(element.getValue().getDocumentName()));
    }
}
