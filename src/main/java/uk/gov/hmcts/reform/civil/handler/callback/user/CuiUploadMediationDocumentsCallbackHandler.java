package uk.gov.hmcts.reform.civil.handler.callback.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.civil.callback.*;
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
import uk.gov.hmcts.reform.civil.utils.AssignCategoryId;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.civil.callback.CallbackParams.Params.BEARER_TOKEN;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.*;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CUI_UPLOAD_MEDIATION_DOCUMENTS;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.UPLOAD_MEDIATION_DOCUMENTS;
import static uk.gov.hmcts.reform.civil.model.mediation.MediationDocumentsType.NON_ATTENDANCE_STATEMENT;
import static uk.gov.hmcts.reform.civil.model.mediation.MediationDocumentsType.REFERRED_DOCUMENTS;
import static uk.gov.hmcts.reform.civil.utils.UploadMediationDocumentsUtils.*;
import static uk.gov.hmcts.reform.civil.utils.UserRoleUtils.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class CuiUploadMediationDocumentsCallbackHandler extends CallbackHandler {

    private static final List<CaseEvent> EVENTS = List.of(CUI_UPLOAD_MEDIATION_DOCUMENTS);


    private final CoreCaseUserService coreCaseUserService;
    private final UserService userService;
    private final ObjectMapper objectMapper;
    private final Time time;
    private final AssignCategoryId assignCategoryId;

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(
            callbackKey(ABOUT_TO_START), this::emptyCallbackResponse,
            callbackKey(ABOUT_TO_SUBMIT), this::submitData
        );
    }


    private CallbackResponse submitData(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();

        CaseData.CaseDataBuilder<?, ?> builder = caseData.toBuilder();

        //addOrUpdateNonAttendanceStatements(caseData, builder);
        //addOrUpdateDocumentsReferred(caseData, builder, uploadMediationDocumentsForm, partyChosen);

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(builder.build().toMap(objectMapper))
            .build();
    }

   /* private void addOrUpdateNonAttendanceStatements(CaseData caseData, CaseData.CaseDataBuilder<?, ?> builder) {
        List<Element<MediationNonAttendanceStatement>> newNonAttendanceDocs = builder.build().getRes1MediationNonAttendanceDocs();
        List<Element<MediationNonAttendanceStatement>> app1MediationNonAttendanceDocs = caseData.getApp1MediationNonAttendanceDocs() == null
            ? new ArrayList<>() : caseData.getApp1MediationNonAttendanceDocs();

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

    private void addOrUpdateNonAttendanceStatements(CaseData caseData, CaseData.CaseDataBuilder<?, ?> builder,
                                                    UploadMediationDocumentsForm uploadMediationDocumentsForm,
                                                    String partyChosen) {
        List<Element<MediationNonAttendanceStatement>> newNonAttendanceDocs = uploadMediationDocumentsForm.getNonAttendanceStatementForm();
    }
*/
    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }
}
