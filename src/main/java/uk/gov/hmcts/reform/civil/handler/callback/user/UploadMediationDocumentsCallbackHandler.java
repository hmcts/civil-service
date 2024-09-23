package uk.gov.hmcts.reform.civil.handler.callback.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.Callback;
import uk.gov.hmcts.reform.civil.callback.CallbackException;
import uk.gov.hmcts.reform.civil.callback.CallbackHandler;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.common.DynamicListElement;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.mediation.MediationDocumentsReferredInStatement;
import uk.gov.hmcts.reform.civil.model.mediation.MediationDocumentsType;
import uk.gov.hmcts.reform.civil.model.mediation.MediationNonAttendanceStatement;
import uk.gov.hmcts.reform.civil.model.mediation.UploadMediationDocumentsForm;
import uk.gov.hmcts.reform.civil.service.CoreCaseUserService;
import uk.gov.hmcts.reform.civil.service.Time;
import uk.gov.hmcts.reform.civil.service.UserService;
import uk.gov.hmcts.reform.civil.service.mediation.UploadMediationService;
import uk.gov.hmcts.reform.civil.utils.AssignCategoryId;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static java.lang.String.format;
import static uk.gov.hmcts.reform.civil.callback.CallbackParams.Params.BEARER_TOKEN;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.MID;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.SUBMITTED;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.UPLOAD_MEDIATION_DOCUMENTS;
import static uk.gov.hmcts.reform.civil.model.mediation.MediationDocumentsType.NON_ATTENDANCE_STATEMENT;
import static uk.gov.hmcts.reform.civil.model.mediation.MediationDocumentsType.REFERRED_DOCUMENTS;
import static uk.gov.hmcts.reform.civil.utils.UploadMediationDocumentsUtils.CLAIMANTS_ID;
import static uk.gov.hmcts.reform.civil.utils.UploadMediationDocumentsUtils.CLAIMANT_ONE_ID;
import static uk.gov.hmcts.reform.civil.utils.UploadMediationDocumentsUtils.CLAIMANT_TWO_ID;
import static uk.gov.hmcts.reform.civil.utils.UploadMediationDocumentsUtils.DEFENDANTS_ID;
import static uk.gov.hmcts.reform.civil.utils.UploadMediationDocumentsUtils.DEFENDANT_ONE_ID;
import static uk.gov.hmcts.reform.civil.utils.UploadMediationDocumentsUtils.DEFENDANT_TWO_ID;
import static uk.gov.hmcts.reform.civil.utils.UploadMediationDocumentsUtils.addApplicantOptions;
import static uk.gov.hmcts.reform.civil.utils.UploadMediationDocumentsUtils.addDefendant1Option;
import static uk.gov.hmcts.reform.civil.utils.UploadMediationDocumentsUtils.addDefendant2Option;
import static uk.gov.hmcts.reform.civil.utils.UploadMediationDocumentsUtils.addSameSolicitorDefendantOptions;
import static uk.gov.hmcts.reform.civil.utils.UserRoleUtils.isApplicantSolicitor;
import static uk.gov.hmcts.reform.civil.utils.UserRoleUtils.isRespondentSolicitorOne;
import static uk.gov.hmcts.reform.civil.utils.UserRoleUtils.isRespondentSolicitorTwo;

@Service
@RequiredArgsConstructor
@Slf4j
public class UploadMediationDocumentsCallbackHandler extends CallbackHandler {

    private static final List<CaseEvent> EVENTS = List.of(UPLOAD_MEDIATION_DOCUMENTS);

    public static final String DOC_DATE_IN_FUTURE = "Document date cannot be in the future";

    public static final String INVALID_PARTY_OPTION = "Invalid party option selected";

    public static final String CLAIMANT_ONE_CATEGORY_ID = "ClaimantOneMediationDocs";

    public static final String CLAIMANT_TWO_CATEGORY_ID = "ClaimantTwoMediationDocs";

    public static final String DEFENDANT_ONE_CATEGORY_ID = "DefendantOneMediationDocs";

    public static final String DEFENDANT_TWO_CATEGORY_ID = "DefendantTwoMediationDocs";

    private final CoreCaseUserService coreCaseUserService;
    private final UserService userService;
    private final ObjectMapper objectMapper;
    private final Time time;
    private final AssignCategoryId assignCategoryId;
    private final UploadMediationService uploadMediationService;

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(
            callbackKey(ABOUT_TO_START), this::emptyCallbackResponse,
            callbackKey(MID, "populate-party-options"), this::populatePartyOptions,
            callbackKey(MID, "validate-dates"), this::validateDocumentDate,
            callbackKey(ABOUT_TO_SUBMIT), this::submitData,
            callbackKey(SUBMITTED), this::buildConfirmation
        );
    }

    private CallbackResponse populatePartyOptions(CallbackParams callbackParams) {
        String authToken = callbackParams.getParams().get(BEARER_TOKEN).toString();
        UserInfo userInfo = userService.getUserInfo(authToken);
        List<String> roles = coreCaseUserService.getUserCaseRoles(
            callbackParams.getCaseData().getCcdCaseReference().toString(),
            userInfo.getUid()
        );

        CaseData caseData = callbackParams.getCaseData();
        CaseData.CaseDataBuilder<?, ?> builder = caseData.toBuilder();

        List<DynamicListElement> dynamicListOptions = new ArrayList<>();

        if (isApplicantSolicitor(roles)) {
            addApplicantOptions(dynamicListOptions, caseData);
        } else if (isRespondentSolicitorOne(roles) && !isRespondentSolicitorTwo(roles)) {
            // 1v1 or 1v2DS respondent 1 solicitor
            addDefendant1Option(dynamicListOptions, caseData);
        } else if (!isRespondentSolicitorOne(roles) && isRespondentSolicitorTwo(roles)) {
            // 1v2 DS respondent 2 solicitor
            addDefendant2Option(dynamicListOptions, caseData);
        } else {
            // 1v2 SS
            addSameSolicitorDefendantOptions(dynamicListOptions, caseData);
        }

        builder.uploadMediationDocumentsForm(UploadMediationDocumentsForm.builder()
                                                 .uploadMediationDocumentsPartyChosen(DynamicList.fromDynamicListElementList(dynamicListOptions))
                                                 .build());

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(builder.build().toMap(objectMapper))
            .build();
    }

    private CallbackResponse submitData(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        CaseData.CaseDataBuilder<?, ?> builder = caseData.toBuilder();
        UploadMediationDocumentsForm uploadMediationDocumentsForm = caseData.getUploadMediationDocumentsForm();

        String partyChosen = uploadMediationDocumentsForm.getUploadMediationDocumentsPartyChosen().getValue().getCode();
        List<MediationDocumentsType> documentsType = uploadMediationDocumentsForm.getMediationDocumentsType();

        if (documentsType.contains(NON_ATTENDANCE_STATEMENT)) {
            addOrUpdateNonAttendanceStatements(caseData, builder, uploadMediationDocumentsForm, partyChosen);
        }

        if (documentsType.contains(REFERRED_DOCUMENTS)) {
            addOrUpdateDocumentsReferred(caseData, builder, uploadMediationDocumentsForm, partyChosen);
        }

        // clear form
        builder.uploadMediationDocumentsForm(null);

        //create dashboard scenarios
        uploadMediationService.uploadMediationDocumentsTaskList(callbackParams);
        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(builder.build().toMap(objectMapper))
            .build();
    }

    private void addOrUpdateDocumentsReferred(CaseData caseData, CaseData.CaseDataBuilder<?, ?> builder,
                                              UploadMediationDocumentsForm uploadMediationDocumentsForm,
                                              String partyChosen) {
        List<Element<MediationDocumentsReferredInStatement>> newDocumentsReferred = uploadMediationDocumentsForm.getDocumentsReferredForm();
        switch (partyChosen) {
            case CLAIMANT_ONE_ID:
                List<Element<MediationDocumentsReferredInStatement>> app1MediationDocsReferred = caseData.getApp1MediationDocumentsReferred() == null
                    ? new ArrayList<>() : caseData.getApp1MediationDocumentsReferred();
                List<Element<MediationDocumentsReferredInStatement>> app1DocRefElements = assignCategoryId.copyCaseDocumentListWithCategoryIdMediationDocRef(
                    newDocumentsReferred,
                    CLAIMANT_ONE_CATEGORY_ID
                );
                app1MediationDocsReferred.addAll(app1DocRefElements);
                builder.app1MediationDocumentsReferred(app1MediationDocsReferred);
                break;
            case CLAIMANT_TWO_ID:
                List<Element<MediationDocumentsReferredInStatement>> app2MediationDocsReferred = caseData.getApp2MediationDocumentsReferred() == null
                    ? new ArrayList<>() : caseData.getApp2MediationDocumentsReferred();
                List<Element<MediationDocumentsReferredInStatement>> app2DocRefElements = assignCategoryId.copyCaseDocumentListWithCategoryIdMediationDocRef(
                    newDocumentsReferred,
                    CLAIMANT_TWO_CATEGORY_ID
                );
                app2MediationDocsReferred.addAll(app2DocRefElements);
                builder.app2MediationDocumentsReferred(app2MediationDocsReferred);
                break;
            // 2v1 where mediation docs referred are uploaded for both app1 and app2
            // copies the document into parties' case data so will show in both app1 and app2 folders in Case File Viewer
            case CLAIMANTS_ID:
                List<Element<MediationDocumentsReferredInStatement>> app1MediationDocsReferred2v1 = caseData.getApp1MediationDocumentsReferred() == null
                    ? new ArrayList<>() : caseData.getApp1MediationDocumentsReferred();
                List<Element<MediationDocumentsReferredInStatement>> app1DocRefElements2v1 = assignCategoryId.copyCaseDocumentListWithCategoryIdMediationDocRef(
                    newDocumentsReferred,
                    CLAIMANT_ONE_CATEGORY_ID
                );
                app1MediationDocsReferred2v1.addAll(app1DocRefElements2v1);
                builder.app1MediationDocumentsReferred(app1MediationDocsReferred2v1);
                List<Element<MediationDocumentsReferredInStatement>> app2MediationDocsReferred1v2 = caseData.getApp2MediationDocumentsReferred() == null
                    ? new ArrayList<>() : caseData.getApp2MediationDocumentsReferred();
                List<Element<MediationDocumentsReferredInStatement>> app2DocRefElements2v1 = assignCategoryId.copyCaseDocumentListWithCategoryIdMediationDocRef(
                    newDocumentsReferred,
                    CLAIMANT_TWO_CATEGORY_ID
                );
                app2MediationDocsReferred1v2.addAll(app2DocRefElements2v1);
                builder.app2MediationDocumentsReferred(app2MediationDocsReferred1v2);
                break;
            case DEFENDANT_ONE_ID:
                List<Element<MediationDocumentsReferredInStatement>> res1MediationDocsReferred = caseData.getRes1MediationDocumentsReferred() == null
                    ? new ArrayList<>() : caseData.getRes1MediationDocumentsReferred();
                List<Element<MediationDocumentsReferredInStatement>> res1DocRefElements = assignCategoryId.copyCaseDocumentListWithCategoryIdMediationDocRef(
                    newDocumentsReferred,
                    DEFENDANT_ONE_CATEGORY_ID
                );
                res1MediationDocsReferred.addAll(res1DocRefElements);
                builder.res1MediationDocumentsReferred(res1MediationDocsReferred);
                break;

            case DEFENDANT_TWO_ID:
                List<Element<MediationDocumentsReferredInStatement>> res2MediationDocsReferred = caseData.getRes2MediationDocumentsReferred() == null
                    ? new ArrayList<>() : caseData.getRes2MediationDocumentsReferred();
                List<Element<MediationDocumentsReferredInStatement>> res2DocRefElements = assignCategoryId.copyCaseDocumentListWithCategoryIdMediationDocRef(
                    newDocumentsReferred,
                    DEFENDANT_TWO_CATEGORY_ID
                );
                res2MediationDocsReferred.addAll(res2DocRefElements);
                builder.res2MediationDocumentsReferred(res2MediationDocsReferred);
                break;
            // 1v2SS where mediation docs referred are uploaded for both res1 and res2
            // copies the document into both parties' case data so will show in both res1 and res2 folders in Case File Viewer
            case DEFENDANTS_ID:
                List<Element<MediationDocumentsReferredInStatement>> res1MediationDocsReferred1v2SS = caseData.getRes1MediationDocumentsReferred() == null
                    ? new ArrayList<>() : caseData.getRes1MediationDocumentsReferred();
                List<Element<MediationDocumentsReferredInStatement>> res1DocRefElements1v2SS = assignCategoryId.copyCaseDocumentListWithCategoryIdMediationDocRef(
                    newDocumentsReferred,
                    DEFENDANT_ONE_CATEGORY_ID
                );
                res1MediationDocsReferred1v2SS.addAll(res1DocRefElements1v2SS);
                builder.res1MediationDocumentsReferred(res1MediationDocsReferred1v2SS);
                List<Element<MediationDocumentsReferredInStatement>> res2MediationDocsReferred1v2SS = caseData.getRes2MediationDocumentsReferred() == null
                    ? new ArrayList<>() : caseData.getRes2MediationDocumentsReferred();
                List<Element<MediationDocumentsReferredInStatement>> res2DocRefElements1v2SS = assignCategoryId.copyCaseDocumentListWithCategoryIdMediationDocRef(
                    newDocumentsReferred,
                    DEFENDANT_TWO_CATEGORY_ID
                );
                res2MediationDocsReferred1v2SS.addAll(res2DocRefElements1v2SS);
                builder.res2MediationDocumentsReferred(res2MediationDocsReferred1v2SS);
                break;
            default: throw new CallbackException(INVALID_PARTY_OPTION);
        }
    }

    private void addOrUpdateNonAttendanceStatements(CaseData caseData, CaseData.CaseDataBuilder<?, ?> builder,
                                                    UploadMediationDocumentsForm uploadMediationDocumentsForm,
                                                    String partyChosen) {
        List<Element<MediationNonAttendanceStatement>> newNonAttendanceDocs = uploadMediationDocumentsForm.getNonAttendanceStatementForm();
        switch (partyChosen) {
            case CLAIMANT_ONE_ID:
                List<Element<MediationNonAttendanceStatement>> app1MediationNonAttendanceDocs = caseData.getApp1MediationNonAttendanceDocs() == null
                    ? new ArrayList<>() : caseData.getApp1MediationNonAttendanceDocs();
                List<Element<MediationNonAttendanceStatement>> app1NonAttElements = assignCategoryId.copyCaseDocumentListWithCategoryIdMediationNonAtt(
                    newNonAttendanceDocs,
                    CLAIMANT_ONE_CATEGORY_ID
                );
                app1MediationNonAttendanceDocs.addAll(app1NonAttElements);
                builder.app1MediationNonAttendanceDocs(app1MediationNonAttendanceDocs);
                break;
            case CLAIMANT_TWO_ID:
                List<Element<MediationNonAttendanceStatement>> app2MediationNonAttendanceDocs = caseData.getApp2MediationNonAttendanceDocs() == null
                    ? new ArrayList<>() : caseData.getApp2MediationNonAttendanceDocs();
                List<Element<MediationNonAttendanceStatement>> app2NonAttElements = assignCategoryId.copyCaseDocumentListWithCategoryIdMediationNonAtt(
                    newNonAttendanceDocs,
                    CLAIMANT_TWO_CATEGORY_ID
                );
                app2MediationNonAttendanceDocs.addAll(app2NonAttElements);
                builder.app2MediationNonAttendanceDocs(app2MediationNonAttendanceDocs);
                break;
            // 2v1 where mediation non-attendance docs are uploaded for both app1 and app2
            // copies the document into parties' case data so will show in both app1 and app2 folders in Case File Viewer
            case CLAIMANTS_ID:
                List<Element<MediationNonAttendanceStatement>> app1MediationNonAttendanceDocs1v2 = caseData.getApp1MediationNonAttendanceDocs() == null
                    ? new ArrayList<>() : caseData.getApp1MediationNonAttendanceDocs();
                List<Element<MediationNonAttendanceStatement>> app1NonAttElements2v1 = assignCategoryId.copyCaseDocumentListWithCategoryIdMediationNonAtt(
                    newNonAttendanceDocs,
                    CLAIMANT_ONE_CATEGORY_ID
                );
                app1MediationNonAttendanceDocs1v2.addAll(app1NonAttElements2v1);
                builder.app1MediationNonAttendanceDocs(app1MediationNonAttendanceDocs1v2);
                List<Element<MediationNonAttendanceStatement>> app2MediationNonAttendanceDocs1v2 = caseData.getApp2MediationNonAttendanceDocs() == null
                    ? new ArrayList<>() : caseData.getApp2MediationNonAttendanceDocs();
                List<Element<MediationNonAttendanceStatement>> app2NonAttElements2v1 = assignCategoryId.copyCaseDocumentListWithCategoryIdMediationNonAtt(
                    newNonAttendanceDocs,
                    CLAIMANT_TWO_CATEGORY_ID
                );
                app2MediationNonAttendanceDocs1v2.addAll(app2NonAttElements2v1);
                builder.app2MediationNonAttendanceDocs(app2MediationNonAttendanceDocs1v2);
                break;
            case DEFENDANT_ONE_ID:
                List<Element<MediationNonAttendanceStatement>> res1MediationNonAttendanceDocs = caseData.getRes1MediationNonAttendanceDocs() == null
                    ? new ArrayList<>() : caseData.getRes1MediationNonAttendanceDocs();
                List<Element<MediationNonAttendanceStatement>> res1NonAttElements = assignCategoryId.copyCaseDocumentListWithCategoryIdMediationNonAtt(
                    newNonAttendanceDocs,
                    DEFENDANT_ONE_CATEGORY_ID
                );
                res1MediationNonAttendanceDocs.addAll(res1NonAttElements);
                builder.res1MediationNonAttendanceDocs(res1MediationNonAttendanceDocs);
                break;
            case DEFENDANT_TWO_ID:
                List<Element<MediationNonAttendanceStatement>> res2MediationNonAttendanceDocs = caseData.getRes2MediationNonAttendanceDocs() == null
                    ? new ArrayList<>() : caseData.getRes2MediationNonAttendanceDocs();
                List<Element<MediationNonAttendanceStatement>> res2NonAttElements = assignCategoryId.copyCaseDocumentListWithCategoryIdMediationNonAtt(
                    newNonAttendanceDocs,
                    DEFENDANT_TWO_CATEGORY_ID
                );
                res2MediationNonAttendanceDocs.addAll(res2NonAttElements);
                builder.res2MediationNonAttendanceDocs(res2MediationNonAttendanceDocs);
                break;
            // 1v2SS where mediation non-attendance docs are uploaded for both res1 and res2
            // copies the document into both parties' case data so will show in both res1 and res2 folders in Case File Viewer
            case DEFENDANTS_ID:
                List<Element<MediationNonAttendanceStatement>> res1MediationNonAttendanceDocs1v2SS = caseData.getRes1MediationNonAttendanceDocs() == null
                    ? new ArrayList<>() : caseData.getRes1MediationNonAttendanceDocs();
                List<Element<MediationNonAttendanceStatement>> res1NonAttElements1v2SS = assignCategoryId.copyCaseDocumentListWithCategoryIdMediationNonAtt(
                    newNonAttendanceDocs,
                    DEFENDANT_ONE_CATEGORY_ID
                );
                res1MediationNonAttendanceDocs1v2SS.addAll(res1NonAttElements1v2SS);
                builder.res1MediationNonAttendanceDocs(res1MediationNonAttendanceDocs1v2SS);
                List<Element<MediationNonAttendanceStatement>> res2MediationNonAttendanceDocs1v2SS = caseData.getRes2MediationNonAttendanceDocs() == null
                    ? new ArrayList<>() : caseData.getRes2MediationNonAttendanceDocs();
                List<Element<MediationNonAttendanceStatement>> res2NonAttElements1v2SS = assignCategoryId.copyCaseDocumentListWithCategoryIdMediationNonAtt(
                    newNonAttendanceDocs,
                    DEFENDANT_TWO_CATEGORY_ID
                );
                res2MediationNonAttendanceDocs1v2SS.addAll(res2NonAttElements1v2SS);
                builder.res2MediationNonAttendanceDocs(res2MediationNonAttendanceDocs1v2SS);
                break;
            default: throw new CallbackException(INVALID_PARTY_OPTION);
        }
    }

    private CallbackResponse validateDocumentDate(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        List<String> errors = new ArrayList<>();

        UploadMediationDocumentsForm uploadMediationDocumentsForm = caseData.getUploadMediationDocumentsForm();
        List<MediationDocumentsType> documentsType = uploadMediationDocumentsForm.getMediationDocumentsType();

        if (documentsType.contains(NON_ATTENDANCE_STATEMENT)) {
            for (Element<MediationNonAttendanceStatement> element : uploadMediationDocumentsForm.getNonAttendanceStatementForm()) {
                isDocumentDateInFuture(errors, element.getValue().getDocumentDate());
            }
        }

        if (documentsType.contains(REFERRED_DOCUMENTS)) {
            for (Element<MediationDocumentsReferredInStatement> element : uploadMediationDocumentsForm.getDocumentsReferredForm()) {
                isDocumentDateInFuture(errors, element.getValue().getDocumentDate());
            }
        }

        if (errors.isEmpty()) {
            return AboutToStartOrSubmitCallbackResponse.builder().build();
        } else {
            return AboutToStartOrSubmitCallbackResponse.builder()
                .errors(errors)
                .build();
        }
    }

    private SubmittedCallbackResponse buildConfirmation(CallbackParams callbackParams) {
        String body = "<br /> %n%n You can continue uploading documents or return later. To upload more documents, go to Next steps and select 'Document upload'.";

        return SubmittedCallbackResponse.builder()
            .confirmationHeader("# Documents uploaded")
            .confirmationBody(format(body))
            .build();
    }

    private void isDocumentDateInFuture(List<String> errors, LocalDate date) {
        if (time.now().toLocalDate().isBefore(date)) {
            errors.add(DOC_DATE_IN_FUTURE);
        }
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }
}
