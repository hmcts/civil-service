package uk.gov.hmcts.reform.civil.handler.tasks;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.camunda.bpm.client.exception.ValueMapperException;
import org.camunda.bpm.client.task.ExternalTask;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;
import uk.gov.hmcts.reform.civil.exceptions.InvalidCaseDataException;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.genapplication.GADetailsRespondentSol;
import uk.gov.hmcts.reform.civil.model.genapplication.GeneralApplicationsDetails;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;
import uk.gov.hmcts.reform.civil.service.data.ExternalTaskInput;
import uk.gov.hmcts.reform.civil.utils.CaseDataContentConverter;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.google.common.collect.Lists.newArrayList;
import static java.lang.Long.parseLong;
import static java.util.Objects.isNull;
import static java.util.Optional.ofNullable;

@RequiredArgsConstructor
@Component
public class UpdateFromGACaseEventTaskHandler implements BaseExternalTaskHandler {

    private static final String gaDocSuffix = "Document";
    private static final String civilDocStaffSuffix = "DocStaff";
    private static final String civilDocClaimantSuffix = "DocClaimant";
    private static final String civilDocRespondentSolSuffix = "DocRespondentSol";
    private static final String civilDocRespondentSolTwoSuffix = "DocRespondentSolTwo";
    private static final String gaDraft = "gaDraft";
    private final CoreCaseDataService coreCaseDataService;
    private final CaseDetailsConverter caseDetailsConverter;
    private final ObjectMapper mapper;

    private CaseData generalAppCaseData;
    private CaseData civilCaseData;
    private CaseData data;

    @Override
    public void handleTask(ExternalTask externalTask) {
        try {
            ExternalTaskInput variables = mapper.convertValue(externalTask.getAllVariables(), ExternalTaskInput.class);

            String generalAppCaseId =
                ofNullable(variables.getCaseId())
                    .orElseThrow(() -> new InvalidCaseDataException("The caseId was not provided"));
            String civilCaseId =
                ofNullable(variables.getGeneralAppParentCaseLink())
                    .orElseThrow(() -> new InvalidCaseDataException(
                        "General application parent case link not found"));

            generalAppCaseData = caseDetailsConverter.toGACaseData(coreCaseDataService
                                                                       .getCase(parseLong(generalAppCaseId)));

            StartEventResponse startEventResponse = coreCaseDataService.startUpdate(
                civilCaseId,
                variables.getCaseEvent()
            );
            civilCaseData = caseDetailsConverter.toCaseData(startEventResponse.getCaseDetails());

            data = coreCaseDataService.submitUpdate(
                civilCaseId,
                CaseDataContentConverter.caseDataContentFromStartEventResponse(
                    startEventResponse,
                    getUpdatedCaseData(civilCaseData, generalAppCaseData)
                )
            );
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
            updateDocCollectionField(output, civilCaseData, generalAppCaseData, gaDraft);
            updateDocCollectionField(output, civilCaseData, generalAppCaseData, "gaResp");
            updateDocCollection(output, generalAppCaseData, "gaRespondDoc", civilCaseData, "gaRespondDoc");

        } catch (Exception e) {
            log.error(e.getMessage());
        }
        return output;
    }

    @SuppressWarnings("unchecked")
    protected int checkIfDocumentExists(List<Element<?>> civilCaseDocumentList,
                                      List<Element<?>> gaCaseDocumentlist) {
        if (gaCaseDocumentlist.get(0).getValue().getClass().equals(CaseDocument.class)) {
            List<Element<CaseDocument>> civilCaseList = civilCaseDocumentList.stream()
                .map(element -> (Element<CaseDocument>) element)
                .toList();
            List<Element<CaseDocument>> gaCaseList = gaCaseDocumentlist.stream()
                .map(element -> (Element<CaseDocument>) element)
                .toList();

            return civilCaseList.stream().filter(civilDocument -> gaCaseList
                .parallelStream().anyMatch(gaDocument -> gaDocument.getValue().getDocumentLink()
                    .equals(civilDocument.getValue().getDocumentLink()))).toList().size();
        } else {
            List<Element<Document>> civilCaseList = civilCaseDocumentList.stream()
                .map(element -> (Element<Document>) element)
                .toList();

            List<Element<Document>> gaCaseList = gaCaseDocumentlist.stream()
                .map(element -> (Element<Document>) element)
                .toList();

            return civilCaseList.stream().filter(civilDocument -> gaCaseList
                .parallelStream().anyMatch(gaDocument -> gaDocument.getValue().getDocumentUrl()
                    .equals(civilDocument.getValue().getDocumentUrl()))).toList().size();
        }
    }

    protected void updateDocCollectionField(Map<String, Object> output, CaseData civilCaseData, CaseData generalAppCaseData, String docFieldName) throws Exception {
        String civilDocPrefix = docFieldName;
        if (civilDocPrefix.equals("generalAppEvidence")) {
            civilDocPrefix = "gaEvidence";
        }

        if (civilDocPrefix.equals("requestForInformation")) {
            civilDocPrefix = "requestForInfo";
        }

        if (civilDocPrefix.equals("writtenRepSequential")) {
            civilDocPrefix = "writtenRepSeq";
        }

        if (civilDocPrefix.equals("writtenRepConcurrent")) {
            civilDocPrefix = "writtenRepCon";
        }

        //staff collection will hold ga doc accessible for judge and staff
        String fromGaList = docFieldName + gaDocSuffix;
        String toCivilStaffList = civilDocPrefix + civilDocStaffSuffix;
        updateDocCollection(output, generalAppCaseData, fromGaList,
                civilCaseData, toCivilStaffList);
        //Claimant collection will hold ga doc accessible for Claimant
        String toCivilClaimantList = civilDocPrefix + civilDocClaimantSuffix;
        if (canViewClaimant(civilCaseData, generalAppCaseData)) {
            updateDocCollection(output, generalAppCaseData, fromGaList,
                    civilCaseData, toCivilClaimantList);
        }
        //RespondentSol collection will hold ga doc accessible for RespondentSol1
        String toCivilRespondentSol1List = civilDocPrefix + civilDocRespondentSolSuffix;
        if (canViewResp(civilCaseData, generalAppCaseData, "1")) {
            updateDocCollection(output, generalAppCaseData, fromGaList,
                    civilCaseData, toCivilRespondentSol1List);
        }
        //Respondent2Sol collection will hold ga doc accessible for RespondentSol2
        String toCivilRespondentSol2List = civilDocPrefix + civilDocRespondentSolTwoSuffix;
        if (canViewResp(civilCaseData, generalAppCaseData, "2")) {
            updateDocCollection(output, generalAppCaseData, fromGaList,
                    civilCaseData, toCivilRespondentSol2List);
        }
    }

    /**
     * Update GA document collection at civil case.
     *
     * @param output      output map for update civil case.
     * @param civilCaseData civil case data.
     * @param generalAppCaseData    GA case data.
     * @param fromGaList base ga field name.
     *                     when get from GA data,
     *                     add 'get' to the name then call getter to access related GA document field.
     * @param toCivilList base civil field name.
     *                     when get from Civil data,
     *                     add 'get' to the name then call getter to access related Civil document field.
     *                    when update output, use name as key to hold to-be-update collection
     */
    @SuppressWarnings("unchecked")
    protected void updateDocCollection(Map<String, Object> output, CaseData generalAppCaseData, String fromGaList,
                        CaseData civilCaseData, String toCivilList) throws Exception {
        Method gaGetter = ReflectionUtils.findMethod(CaseData.class,
                                                     "get" + StringUtils.capitalize(fromGaList));
        List<Element<?>> gaDocs =
            (List<Element<?>>) (gaGetter != null ? gaGetter.invoke(generalAppCaseData) : null);
        Method civilGetter = ReflectionUtils.findMethod(CaseData.class,
                                                        "get" + StringUtils.capitalize(toCivilList));
        List<Element<?>> civilDocs =
            (List<Element<?>>) ofNullable(civilGetter != null ? civilGetter.invoke(civilCaseData) : null)
                .orElse(newArrayList());
        if (gaDocs != null && !(fromGaList.equals("gaDraftDocument"))) {
            List<UUID> ids = civilDocs.stream().map(Element::getId).toList();
            for (Element<?> gaDoc : gaDocs) {
                if (!ids.contains(gaDoc.getId())) {
                    civilDocs.add(gaDoc);
                }
            }
        } else if (gaDocs != null && gaDocs.size() == 1 && checkIfDocumentExists(civilDocs, gaDocs) < 1) {
            civilDocs.addAll(gaDocs);
        }
        output.put(toCivilList, civilDocs.isEmpty() ? null : civilDocs);
    }

    protected boolean canViewClaimant(CaseData civilCaseData, CaseData generalAppCaseData) {
        List<Element<GeneralApplicationsDetails>> gaAppDetails = civilCaseData.getClaimantGaAppDetails();
        if (isNull(gaAppDetails)) {
            return false;
        }
        return gaAppDetails.stream()
                .anyMatch(civilGaData -> generalAppCaseData.getCcdCaseReference()
                        .equals(parseLong(civilGaData.getValue().getCaseLink().getCaseReference())));
    }

    protected boolean canViewResp(CaseData civilCaseData, CaseData generalAppCaseData, String respondent) {
        List<Element<GADetailsRespondentSol>> gaAppDetails;
        if (respondent.equals("2")) {
            gaAppDetails = civilCaseData.getRespondentSolTwoGaAppDetails();
        } else {
            gaAppDetails = civilCaseData.getRespondentSolGaAppDetails();
        }
        if (isNull(gaAppDetails)) {
            return false;
        }
        return gaAppDetails.stream()
                .anyMatch(civilGaData -> generalAppCaseData.getCcdCaseReference()
                        .equals(parseLong(civilGaData.getValue().getCaseLink().getCaseReference())));
    }
}
