package uk.gov.hmcts.reform.civil.handler.tasks;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.client.exception.ValueMapperException;
import org.camunda.bpm.client.task.ExternalTask;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;
import uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.exceptions.InvalidCaseDataException;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.ExternalTaskData;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.genapplication.GADetailsRespondentSol;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.data.ExternalTaskInput;
import uk.gov.hmcts.reform.civil.utils.CaseDataContentConverter;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;

import static com.google.common.collect.Lists.newArrayList;
import static java.lang.Long.parseLong;
import static java.util.Objects.isNull;
import static java.util.Optional.ofNullable;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;

@RequiredArgsConstructor
@Component
@Slf4j
public class UpdateFromGACaseEventTaskHandler extends BaseExternalTaskHandler {

    private static final String GA_DOC_SUFFIX = "Document";
    private static final String GA_ADDL_DOC_SUFFIX = "Doc";
    private static final String CIVIL_DOC_STAFF_SUFFIX = "DocStaff";
    private static final String CIVIL_DOC_CLAIMANT_SUFFIX = "DocClaimant";
    private static final String CIVIL_DOC_RESPONDENT_SOL_SUFFIX = "DocRespondentSol";
    private static final String CIVIL_DOC_RESPONDENT_SOL_TWO_SUFFIX = "DocRespondentSolTwo";
    private static final List<String> GA_RESPONDENT_VIEW_DOC_TYPES = List.of("generalOrder", "dismissalOrder", "directionOrder",
                                                                          "hearingNotice", "hearingOrder", "requestForInfo");
    private static final String GA_DRAFT_DOCUMENT = "gaDraftDocument";
    private static final String GA_DRAFT = "gaDraft";
    private final CoreCaseDataService coreCaseDataService;
    private final CaseDetailsConverter caseDetailsConverter;
    private final ObjectMapper mapper;
    private final FeatureToggleService featureToggleService;

    @Override
    public ExternalTaskData handleTask(ExternalTask externalTask) {
        try {
            ExternalTaskInput variables = mapper.convertValue(externalTask.getAllVariables(), ExternalTaskInput.class);

            String generalAppCaseId =
                ofNullable(variables.getCaseId())
                    .orElseThrow(() -> new InvalidCaseDataException("The caseId was not provided"));
            String civilCaseId =
                ofNullable(variables.getGeneralAppParentCaseLink())
                    .orElseThrow(() -> new InvalidCaseDataException(
                        "General application parent case link not found"));

            var generalAppCaseData = caseDetailsConverter.toGACaseData(coreCaseDataService
                .getCase(parseLong(generalAppCaseId)));

            StartEventResponse startEventResponse = coreCaseDataService.startUpdate(
                civilCaseId,
                variables.getCaseEvent()
            );
            var caseData = caseDetailsConverter.toCaseData(startEventResponse.getCaseDetails());

            coreCaseDataService.submitUpdate(
                civilCaseId,
                CaseDataContentConverter.caseDataContentFromStartEventResponse(
                    startEventResponse,
                    getUpdatedCaseData(caseData, generalAppCaseData)
                )
            );
            return new ExternalTaskData().setCaseData(caseData).setGeneralApplicationData(generalAppCaseData);
        } catch (NumberFormatException ne) {
            throw new InvalidCaseDataException(
                "Conversion to long datatype failed for general application for a case ", ne
            );
        } catch (IllegalArgumentException | ValueMapperException e) {
            throw new InvalidCaseDataException("Mapper conversion failed due to incompatible types", e);
        }
    }

    private Map<String, Object> getUpdatedCaseData(CaseData civilCaseData, CaseData generalAppCaseData) {
        Map<String, Object> output = civilCaseData.toMap(mapper);
        try {
            updateDocCollectionField(output, civilCaseData, generalAppCaseData, "generalOrder");
            updateDocCollectionField(output, civilCaseData, generalAppCaseData, "dismissalOrder");
            updateDocCollectionField(output, civilCaseData, generalAppCaseData, "directionOrder");
            updateDocCollectionField(output, civilCaseData, generalAppCaseData, "hearingNotice");
            updateDocCollectionField(output, civilCaseData, generalAppCaseData, "generalAppEvidence");
            updateDocCollectionField(output, civilCaseData, generalAppCaseData, "hearingOrder");
            updateDocCollectionField(output, civilCaseData, generalAppCaseData, "requestForInformation");
            updateDocCollectionField(output, civilCaseData, generalAppCaseData, "writtenRepSequential");
            updateDocCollectionField(output, civilCaseData, generalAppCaseData, "writtenRepConcurrent");
            updateDocCollectionField(output, civilCaseData, generalAppCaseData, "consentOrder");
            updateDocCollectionField(output, civilCaseData, generalAppCaseData, GA_DRAFT);
            updateDocCollectionField(output, civilCaseData, generalAppCaseData, "gaResp");
            updateDocCollection(output, generalAppCaseData, "gaRespondDoc", civilCaseData, "gaRespondDoc");
            generalAppCaseData = mergeBundle(generalAppCaseData);
            updateDocCollectionField(output, civilCaseData, generalAppCaseData, "gaAddl");
            if (featureToggleService.isGaForWelshEnabled() && (civilCaseData.isClaimantBilingual() || civilCaseData.isRespondentResponseBilingual())) {
                if (generalAppCaseData.getParentClaimantIsApplicant() == YES) {
                    updateDocCollection(output, generalAppCaseData, "preTranslationGaDocsApplicant", civilCaseData, "gaAddlDocClaimant");
                } else {
                    updateDocCollection(output, generalAppCaseData, "preTranslationGaDocsRespondent", civilCaseData, "gaAddlDocRespondentSol");
                }
            }
        } catch (Exception e) {
            log.info("civilCaseData case Id {} and generalAppCaseData case Id {} ", civilCaseData.getCcdCaseReference(), generalAppCaseData.getCcdCaseReference());
            log.error(e.getMessage());
        }
        return output;
    }

    protected CaseData mergeBundle(CaseData generalAppCaseData) {
        if (Objects.nonNull(generalAppCaseData.getGaAddlDocBundle())) {
            List<Element<CaseDocument>> newGaAddlDoc = generalAppCaseData.getGaAddlDoc();
            if (Objects.isNull(newGaAddlDoc)) {
                newGaAddlDoc = new ArrayList<>();
            }
            newGaAddlDoc.addAll(generalAppCaseData.getGaAddlDocBundle());
            return generalAppCaseData.setGaAddlDoc(newGaAddlDoc);
        }
        return generalAppCaseData;
    }

    protected <T> int checkIfDocumentExists(List<Element<T>> civilCaseDocumentList,
                                            List<Element<T>> gaCaseDocumentlist) {
        return countExistingDocuments(civilCaseDocumentList, gaCaseDocumentlist, this::getDocumentUrl);
    }

    protected void updateDocCollectionField(Map<String, Object> output, CaseData civilCaseData, CaseData generalAppCaseData, String docFieldName)
        throws Exception {
        String civilDocPrefix = getCivilDocPrefix(docFieldName);
        String fromGaList = getFromGaList(docFieldName, civilDocPrefix);

        updateDocCollection(output, generalAppCaseData, fromGaList, civilCaseData, civilDocPrefix + CIVIL_DOC_STAFF_SUFFIX);
        updateClaimantCollection(output, civilCaseData, generalAppCaseData, civilDocPrefix, fromGaList);
        updateRespondentCollection(output, civilCaseData, generalAppCaseData, civilDocPrefix, fromGaList, "1", CIVIL_DOC_RESPONDENT_SOL_SUFFIX);
        updateRespondentCollection(output, civilCaseData, generalAppCaseData, civilDocPrefix, fromGaList, "2", CIVIL_DOC_RESPONDENT_SOL_TWO_SUFFIX);
    }

    private String getCivilDocPrefix(String docFieldName) {
        return switch (docFieldName) {
            case "generalAppEvidence" -> "gaEvidence";
            case "requestForInformation" -> "requestForInfo";
            case "writtenRepSequential" -> "writtenRepSeq";
            case "writtenRepConcurrent" -> "writtenRepCon";
            default -> docFieldName;
        };
    }

    private String getFromGaList(String docFieldName, String civilDocPrefix) {
        return civilDocPrefix.equals("gaAddl") ? docFieldName + GA_ADDL_DOC_SUFFIX : docFieldName + GA_DOC_SUFFIX;
    }

    private void updateClaimantCollection(
        Map<String, Object> output,
        CaseData civilCaseData,
        CaseData generalAppCaseData,
        String civilDocPrefix,
        String fromGaList
    ) throws Exception {
        if (canViewClaimant(civilCaseData, generalAppCaseData, civilDocPrefix)) {
            updateDocCollection(output, generalAppCaseData, fromGaList, civilCaseData, civilDocPrefix + CIVIL_DOC_CLAIMANT_SUFFIX);
        }
    }

    private void updateRespondentCollection(
        Map<String, Object> output,
        CaseData civilCaseData,
        CaseData generalAppCaseData,
        String civilDocPrefix,
        String fromGaList,
        String respondent,
        String civilSuffix
    ) throws Exception {
        if (canViewResp(civilCaseData, generalAppCaseData, civilDocPrefix, respondent)) {
            updateDocCollection(output, generalAppCaseData, fromGaList, civilCaseData, civilDocPrefix + civilSuffix);
        }
    }

    /**
     * Update GA document collection at civil case.
     *
     * @param output             output map for update civil case.
     * @param civilCaseData      civil case data.
     * @param generalAppCaseData GA case data.
     * @param fromGaList         base ga field name.
     *                           when get from GA data,
     *                           add 'get' to the name then call getter to access related GA document field.
     * @param toCivilList        base civil field name.
     *                           when get from Civil data,
     *                           add 'get' to the name then call getter to access related Civil document field.
     *                           when update output, use name as key to hold to-be-update collection
     */
    @SuppressWarnings("java:S3776")
    protected void updateDocCollection(Map<String, Object> output, CaseData generalAppCaseData, String fromGaList,
                                       CaseData civilCaseData, String toCivilList) throws ReflectiveOperationException {
        Method gaGetter = getCaseDataGetter(fromGaList);
        Method civilGetter = getCaseDataGetter(toCivilList);
        Method referenceGetter = getReferenceGetter(gaGetter, civilGetter);

        if (referenceGetter == null) {
            output.put(toCivilList, null);
            return;
        }

        if (isCaseDocumentCollection(referenceGetter)) {
            updateCaseDocumentCollection(output, generalAppCaseData, fromGaList, civilCaseData, toCivilList, gaGetter, civilGetter);
        } else {
            updateDocumentCollection(output, generalAppCaseData, fromGaList, civilCaseData, toCivilList, gaGetter, civilGetter);
        }
    }

    protected <T> List<Element<T>> checkDraftDocumentsInMainCase(List<Element<T>> civilDocs, List<Element<T>> gaDocs) {
        List<UUID> ids = gaDocs.stream().map(Element::getId).toList();
        List<Element<T>> civilDocsCopy = newArrayList();

        for (Element<T> civilDoc : civilDocs) {
            if (!ids.contains(civilDoc.getId())) {
                civilDocsCopy.add(civilDoc);
            }
        }

        List<UUID> civilIds = civilDocs.stream().map(Element::getId).toList();
        for (Element<T> gaDoc : gaDocs) {
            if (!civilIds.contains(gaDoc.getId())) {
                civilDocsCopy.add(gaDoc);
            }
        }

        civilDocs.clear();
        civilDocs.addAll(civilDocsCopy);
        civilDocsCopy.clear();

        return civilDocs;
    }

    private Method getCaseDataGetter(String fieldName) {
        return ReflectionUtils.findMethod(CaseData.class, "get" + StringUtils.capitalize(fieldName));
    }

    private Method getReferenceGetter(Method gaGetter, Method civilGetter) {
        return gaGetter != null ? gaGetter : civilGetter;
    }

    private boolean isCaseDocumentCollection(Method getter) {
        Type returnType = getter.getGenericReturnType();
        if (returnType instanceof ParameterizedType listType) {
            Type elementType = listType.getActualTypeArguments()[0];
            if (elementType instanceof ParameterizedType parameterizedElementType) {
                return CaseDocument.class.equals(parameterizedElementType.getActualTypeArguments()[0]);
            }
        }
        return false;
    }

    private void updateCaseDocumentCollection(
        Map<String, Object> output,
        CaseData generalAppCaseData,
        String fromGaList,
        CaseData civilCaseData,
        String toCivilList,
        Method gaGetter,
        Method civilGetter
    ) throws ReflectiveOperationException {
        List<Element<CaseDocument>> gaDocs = getCaseDocumentCollection(gaGetter, generalAppCaseData);
        List<Element<CaseDocument>> civilDocs = getCaseDocumentCollectionOrEmpty(civilGetter, civilCaseData);

        if (gaDocs != null && !fromGaList.equals(GA_DRAFT_DOCUMENT)) {
            addMissingCaseDocuments(generalAppCaseData, toCivilList, civilDocs, gaDocs);
        } else if (gaDocs != null && isDraftDocumentUpdate(civilCaseData, fromGaList)) {
            checkDraftDocumentsInMainCase(civilDocs, gaDocs);
        } else if (gaDocs != null && gaDocs.size() == 1 && checkIfDocumentExists(civilDocs, gaDocs) < 1) {
            civilDocs.addAll(gaDocs);
        }

        output.put(toCivilList, civilDocs.isEmpty() ? null : civilDocs);
    }

    private void updateDocumentCollection(
        Map<String, Object> output,
        CaseData generalAppCaseData,
        String fromGaList,
        CaseData civilCaseData,
        String toCivilList,
        Method gaGetter,
        Method civilGetter
    ) throws ReflectiveOperationException {
        List<Element<Document>> gaDocs = getDocumentCollection(gaGetter, generalAppCaseData);
        List<Element<Document>> civilDocs = getDocumentCollectionOrEmpty(civilGetter, civilCaseData);

        if (gaDocs != null && !fromGaList.equals(GA_DRAFT_DOCUMENT)) {
            addMissingDocumentsById(civilDocs, gaDocs);
        } else if (gaDocs != null && isDraftDocumentUpdate(civilCaseData, fromGaList)) {
            checkDraftDocumentsInMainCase(civilDocs, gaDocs);
        } else if (gaDocs != null && gaDocs.size() == 1 && checkIfDocumentExists(civilDocs, gaDocs) < 1) {
            civilDocs.addAll(gaDocs);
        }

        output.put(toCivilList, civilDocs.isEmpty() ? null : civilDocs);
    }

    private boolean isDraftDocumentUpdate(CaseData civilCaseData, String fromGaList) {
        return (civilCaseData.isRespondent1LiP() || civilCaseData.isRespondent2LiP() || civilCaseData.isApplicantNotRepresented())
            && fromGaList.equals(GA_DRAFT_DOCUMENT);
    }

    private void addMissingCaseDocuments(
        CaseData generalAppCaseData,
        String toCivilList,
        List<Element<CaseDocument>> civilDocs,
        List<Element<CaseDocument>> gaDocs
    ) {
        List<UUID> ids = civilDocs.stream().map(Element::getId).toList();
        String gaRespondentMainClaimUser = generalAppCaseData.getParentClaimantIsApplicant() == YES ? "Respondent" : "Claimant";
        for (Element<CaseDocument> gaDoc : gaDocs) {
            CaseDocument caseDocument = gaDoc.getValue();
            if (!ids.contains(gaDoc.getId())
                && (!toCivilList.contains(gaRespondentMainClaimUser)
                || caseDocument == null
                || caseDocument.getDocumentType() != DocumentType.SEND_APP_TO_OTHER_PARTY)) {
                civilDocs.add(gaDoc);
            }
        }
    }

    private <T> void addMissingDocumentsById(List<Element<T>> civilDocs, List<Element<T>> gaDocs) {
        List<UUID> ids = civilDocs.stream().map(Element::getId).toList();
        for (Element<T> gaDoc : gaDocs) {
            if (!ids.contains(gaDoc.getId())) {
                civilDocs.add(gaDoc);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private List<Element<CaseDocument>> getCaseDocumentCollection(Method getter, CaseData caseData)
        throws IllegalAccessException, InvocationTargetException {
        return (List<Element<CaseDocument>>) (getter != null ? getter.invoke(caseData) : null);
    }

    private List<Element<CaseDocument>> getCaseDocumentCollectionOrEmpty(Method getter, CaseData caseData)
        throws IllegalAccessException, InvocationTargetException {
        return ofNullable(getCaseDocumentCollection(getter, caseData)).orElse(newArrayList());
    }

    @SuppressWarnings("unchecked")
    private List<Element<Document>> getDocumentCollection(Method getter, CaseData caseData)
        throws IllegalAccessException, InvocationTargetException {
        return (List<Element<Document>>) (getter != null ? getter.invoke(caseData) : null);
    }

    private List<Element<Document>> getDocumentCollectionOrEmpty(Method getter, CaseData caseData)
        throws IllegalAccessException, InvocationTargetException {
        return ofNullable(getDocumentCollection(getter, caseData)).orElse(newArrayList());
    }

    private <T> int countExistingDocuments(
        List<Element<T>> civilCaseDocumentList,
        List<Element<T>> gaCaseDocumentList,
        Function<T, String> documentUrlExtractor
    ) {
        return (int) civilCaseDocumentList.stream()
            .filter(civilDocument -> gaCaseDocumentList.parallelStream()
                .anyMatch(gaDocument -> documentUrlExtractor.apply(gaDocument.getValue())
                    .equals(documentUrlExtractor.apply(civilDocument.getValue()))))
            .count();
    }

    private <T> String getDocumentUrl(T document) {
        if (document instanceof CaseDocument caseDocument) {
            return caseDocument.getDocumentLink().getDocumentUrl();
        }
        if (document instanceof Document gaDocument) {
            return gaDocument.getDocumentUrl();
        }
        throw new IllegalArgumentException("Unsupported document type " + document.getClass().getName());
    }

    protected boolean canViewClaimant(CaseData civilCaseData, CaseData generalAppCaseData, String civilDocPrefix) {
        return isRespondentViewDocumentType(civilDocPrefix)
            || isVisibleToClaimantByNoticeOrAgreement(generalAppCaseData)
            || hasMatchingCaseReference(
            civilCaseData.getClaimantGaAppDetails(),
            generalAppCaseData.getCcdCaseReference(),
            claimantDetails -> claimantDetails.getCaseLink().getCaseReference()
        );
    }

    protected boolean canViewResp(CaseData civilCaseData, CaseData generalAppCaseData, String civilDocPrefix, String respondent) {
        return isRespondentViewDocumentType(civilDocPrefix)
            || isVisibleToRespondentByNoticeOrAgreement(generalAppCaseData)
            || hasMatchingCaseReference(
            getRespondentGaAppDetails(civilCaseData, respondent),
            generalAppCaseData.getCcdCaseReference(),
            respondentDetails -> respondentDetails.getCaseLink().getCaseReference()
        );
    }

    private boolean isRespondentViewDocumentType(String civilDocPrefix) {
        return GA_RESPONDENT_VIEW_DOC_TYPES.contains(civilDocPrefix);
    }

    private boolean isVisibleToClaimantByNoticeOrAgreement(CaseData generalAppCaseData) {
        return generalAppCaseData.getParentClaimantIsApplicant() == NO && isWithNoticeOrAgreed(generalAppCaseData);
    }

    private boolean isVisibleToRespondentByNoticeOrAgreement(CaseData generalAppCaseData) {
        return generalAppCaseData.getParentClaimantIsApplicant() == YesOrNo.YES && isWithNoticeOrAgreed(generalAppCaseData);
    }

    private boolean isWithNoticeOrAgreed(CaseData generalAppCaseData) {
        return (generalAppCaseData.getGeneralAppInformOtherParty() != null
            && YES.equals(generalAppCaseData.getGeneralAppInformOtherParty().getIsWithNotice()))
            || (generalAppCaseData.getGeneralAppRespondentAgreement() != null
            && YES.equals(generalAppCaseData.getGeneralAppRespondentAgreement().getHasAgreed()));
    }

    private List<Element<GADetailsRespondentSol>> getRespondentGaAppDetails(CaseData civilCaseData, String respondent) {
        return respondent.equals("2") ? civilCaseData.getRespondentSolTwoGaAppDetails() : civilCaseData.getRespondentSolGaAppDetails();
    }

    private <T> boolean hasMatchingCaseReference(
        List<Element<T>> gaAppDetails,
        Long ccdCaseReference,
        Function<T, String> caseReferenceExtractor
    ) {
        if (isNull(gaAppDetails)) {
            return false;
        }

        return gaAppDetails.stream()
            .anyMatch(civilGaData -> ccdCaseReference.equals(parseLong(caseReferenceExtractor.apply(civilGaData.getValue()))));
    }
}
