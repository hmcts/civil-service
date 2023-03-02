package uk.gov.hmcts.reform.civil.handler.tasks;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.camunda.bpm.client.exception.ValueMapperException;
import org.camunda.bpm.client.task.ExternalTask;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.civil.exceptions.InvalidCaseDataException;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.documents.CaseDocument;
import uk.gov.hmcts.reform.civil.model.genapplication.GADetailsRespondentSol;
import uk.gov.hmcts.reform.civil.model.genapplication.GeneralApplicationsDetails;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;
import uk.gov.hmcts.reform.civil.service.data.ExternalTaskInput;
import uk.gov.hmcts.reform.civil.utils.CaseDataContentConverter;

import static com.google.common.collect.Lists.newArrayList;
import static java.lang.Long.parseLong;
import static java.util.Optional.ofNullable;

@RequiredArgsConstructor
@Component
public class UpdateFromGACaseEventTaskHandler implements BaseExternalTaskHandler {

    private static final String gaDocSuffix = "Document";
    private static final String civilDocStaffSuffix = "DocStaff";
    private static final String civilDocClaimantSuffix = "DocClaimant";
    private static final String civilDocRespondentSolSuffix = "DocRespondentSol";
    private static final String civilDocRespondentSolTwoSuffix = "DocRespondentSolTwo";
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
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        return output;
    }

    private int checkIfDocumentExists(List<Element<CaseDocument>> civilCaseDocumentList,
                                      List<Element<CaseDocument>> gaCaseDocumentlist) {
        return civilCaseDocumentList.stream().filter(civilDocument -> gaCaseDocumentlist
              .parallelStream().anyMatch(gaDocument -> gaDocument.getId().equals(civilDocument.getId())))
            .collect(Collectors.toList()).size();
    }

    protected void updateDocCollectionField(Map<String, Object> output, CaseData civilCaseData, CaseData generalAppCaseData, String docFieldName) throws Exception {
        //staff collection will hold ga doc accessible for judge and staff
        String fromGaList = docFieldName + gaDocSuffix;
        String toCivilStaffList = docFieldName + civilDocStaffSuffix;
        updateDocCollection(output, generalAppCaseData, fromGaList,
                civilCaseData, toCivilStaffList);
        //Claimant collection will hold ga doc accessible for Claimant
        String toCivilClaimantList = docFieldName + civilDocClaimantSuffix;
        if (canViewClaimant(civilCaseData, generalAppCaseData)) {
            updateDocCollection(output, generalAppCaseData, fromGaList,
                    civilCaseData, toCivilClaimantList);
        }
        //RespondentSol collection will hold ga doc accessible for RespondentSol1
        String toCivilRespondentSol1List = docFieldName + civilDocRespondentSolSuffix;
        if (canViewResp(civilCaseData, generalAppCaseData, "1")) {
            updateDocCollection(output, generalAppCaseData, fromGaList,
                    civilCaseData, toCivilRespondentSol1List);
        }
        //Respondent2Sol collection will hold ga doc accessible for RespondentSol2
        String toCivilRespondentSol2List = docFieldName + civilDocRespondentSolTwoSuffix;
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
        Method gaGetter = ReflectionUtils.findMethod(CaseData.class, "get" + StringUtils.capitalize(fromGaList));
        List<Element<CaseDocument>> gaDocs =
                (List<Element<CaseDocument>>) (gaGetter != null ? gaGetter.invoke(generalAppCaseData) : null);
        Method civilGetter = ReflectionUtils.findMethod(CaseData.class, "get" + StringUtils.capitalize(toCivilList));
        List<Element<CaseDocument>> civilDocs =
                (List<Element<CaseDocument>>) ofNullable(civilGetter != null ? civilGetter.invoke(civilCaseData) : null)
                        .orElse(newArrayList());

        if (gaDocs != null
                && checkIfDocumentExists(civilDocs, gaDocs) < 1) {
            civilDocs.addAll(gaDocs);
        }

        output.put(toCivilList, civilDocs.isEmpty() ? null : civilDocs);
    }

    protected boolean canViewClaimant(CaseData civilCaseData, CaseData generalAppCaseData) {
        List<Element<GeneralApplicationsDetails>> gaAppDetails = civilCaseData.getClaimantGaAppDetails();
        if (Objects.isNull(gaAppDetails)) {
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
        if (Objects.isNull(gaAppDetails)) {
            return false;
        }
        return gaAppDetails.stream()
                .anyMatch(civilGaData -> generalAppCaseData.getCcdCaseReference()
                        .equals(parseLong(civilGaData.getValue().getCaseLink().getCaseReference())));
    }
}
