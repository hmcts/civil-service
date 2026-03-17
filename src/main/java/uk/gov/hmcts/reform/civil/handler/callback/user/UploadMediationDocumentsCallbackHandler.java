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
import java.util.function.BiConsumer;
import java.util.function.Function;

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

    public static final String CLAIMANT_CATEGORY_SUBSTRING = "Claimant";

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

        UploadMediationDocumentsForm uploadMediationDocumentsForm = new UploadMediationDocumentsForm();
        uploadMediationDocumentsForm.setUploadMediationDocumentsPartyChosen(DynamicList.fromDynamicListElementList(dynamicListOptions));

        caseData.setUploadMediationDocumentsForm(uploadMediationDocumentsForm);

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseData.toMap(objectMapper))
            .build();
    }

    private CallbackResponse submitData(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        UploadMediationDocumentsForm uploadMediationDocumentsForm = caseData.getUploadMediationDocumentsForm();

        String partyChosen = uploadMediationDocumentsForm.getUploadMediationDocumentsPartyChosen().getValue().getCode();
        List<MediationDocumentsType> documentsType = uploadMediationDocumentsForm.getMediationDocumentsType();

        if (documentsType.contains(NON_ATTENDANCE_STATEMENT)) {
            addOrUpdateNonAttendanceStatements(caseData, uploadMediationDocumentsForm, partyChosen);
        }

        if (documentsType.contains(REFERRED_DOCUMENTS)) {
            addOrUpdateDocumentsReferred(caseData, uploadMediationDocumentsForm, partyChosen);
        }

        // clear form
        caseData.setUploadMediationDocumentsForm(null);

        //create dashboard scenarios
        uploadMediationService.uploadMediationDocumentsTaskList(callbackParams);
        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseData.toMap(objectMapper))
            .build();
    }

    private void addOrUpdateDocumentsReferred(CaseData caseData,
                                              UploadMediationDocumentsForm uploadMediationDocumentsForm,
                                              String partyChosen) {
        addOrUpdateMediationDocuments(
            caseData,
            partyChosen,
            uploadMediationDocumentsForm.getDocumentsReferredForm(),
            assignCategoryId::copyCaseDocumentListWithCategoryIdMediationDocRef,
            this::getMediationDocumentsReferred,
            this::setMediationDocumentsReferred
        );
    }

    private void addOrUpdateNonAttendanceStatements(CaseData caseData,
                                                    UploadMediationDocumentsForm uploadMediationDocumentsForm,
                                                    String partyChosen) {
        addOrUpdateMediationDocuments(
            caseData,
            partyChosen,
            uploadMediationDocumentsForm.getNonAttendanceStatementForm(),
            assignCategoryId::copyCaseDocumentListWithCategoryIdMediationNonAtt,
            this::getNonAttendanceStatements,
            this::setNonAttendanceStatements
        );
    }

    private <T> void addOrUpdateMediationDocuments(CaseData caseData,
                                                   String partyChosen,
                                                   List<Element<T>> newDocuments,
                                                   java.util.function.BiFunction<List<Element<T>>, String, List<Element<T>>> copyFunction,
                                                   Function<PartyInfo, List<Element<T>>> getFunction,
                                                   BiConsumer<PartyInfo, List<Element<T>>> setFunction) {
        List<PartyInfo> targetParties = getTargetParties(caseData, partyChosen);
        for (PartyInfo partyInfo : targetParties) {
            List<Element<T>> existingDocuments = getFunction.apply(partyInfo);
            if (existingDocuments == null) {
                existingDocuments = new ArrayList<>();
            }
            List<Element<T>> newElements = copyFunction.apply(newDocuments, partyInfo.categoryId());
            existingDocuments.addAll(newElements);
            setFunction.accept(partyInfo, existingDocuments);
        }
    }

    private record PartyInfo(CaseData caseData, String categoryId, int partyNumber) {}

    private List<PartyInfo> getTargetParties(CaseData caseData, String partyChosen) {
        return switch (partyChosen) {
            case CLAIMANT_ONE_ID -> List.of(new PartyInfo(caseData, CLAIMANT_ONE_CATEGORY_ID, 1));
            case CLAIMANT_TWO_ID -> List.of(new PartyInfo(caseData, CLAIMANT_TWO_CATEGORY_ID, 2));
            case CLAIMANTS_ID -> List.of(
                new PartyInfo(caseData, CLAIMANT_ONE_CATEGORY_ID, 1),
                new PartyInfo(caseData, CLAIMANT_TWO_CATEGORY_ID, 2)
            );
            case DEFENDANT_ONE_ID -> List.of(new PartyInfo(caseData, DEFENDANT_ONE_CATEGORY_ID, 1));
            case DEFENDANT_TWO_ID -> List.of(new PartyInfo(caseData, DEFENDANT_TWO_CATEGORY_ID, 2));
            case DEFENDANTS_ID -> List.of(
                new PartyInfo(caseData, DEFENDANT_ONE_CATEGORY_ID, 1),
                new PartyInfo(caseData, DEFENDANT_TWO_CATEGORY_ID, 2)
            );
            default -> throw new CallbackException(INVALID_PARTY_OPTION);
        };
    }

    @SuppressWarnings("unchecked")
    private <T> List<Element<T>> getMediationDocumentsReferred(PartyInfo partyInfo) {
        CaseData caseData = partyInfo.caseData();
        if (partyInfo.categoryId().contains(CLAIMANT_CATEGORY_SUBSTRING)) {
            return (List<Element<T>>) (Object) (partyInfo.partyNumber() == 1
                ? caseData.getApp1MediationDocumentsReferred() : caseData.getApp2MediationDocumentsReferred());
        } else {
            return (List<Element<T>>) (Object) (partyInfo.partyNumber() == 1
                ? caseData.getRes1MediationDocumentsReferred() : caseData.getRes2MediationDocumentsReferred());
        }
    }

    @SuppressWarnings("unchecked")
    private <T> void setMediationDocumentsReferred(PartyInfo partyInfo, List<Element<T>> documents) {
        CaseData caseData = partyInfo.caseData();
        List<Element<MediationDocumentsReferredInStatement>> docs = (List<Element<MediationDocumentsReferredInStatement>>) (Object) documents;
        if (partyInfo.categoryId().contains(CLAIMANT_CATEGORY_SUBSTRING)) {
            if (partyInfo.partyNumber() == 1) {
                caseData.setApp1MediationDocumentsReferred(docs);
            } else {
                caseData.setApp2MediationDocumentsReferred(docs);
            }
        } else {
            if (partyInfo.partyNumber() == 1) {
                caseData.setRes1MediationDocumentsReferred(docs);
            } else {
                caseData.setRes2MediationDocumentsReferred(docs);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private <T> List<Element<T>> getNonAttendanceStatements(PartyInfo partyInfo) {
        CaseData caseData = partyInfo.caseData();
        if (partyInfo.categoryId().contains(CLAIMANT_CATEGORY_SUBSTRING)) {
            return (List<Element<T>>) (Object) (partyInfo.partyNumber() == 1
                ? caseData.getApp1MediationNonAttendanceDocs() : caseData.getApp2MediationNonAttendanceDocs());
        } else {
            return (List<Element<T>>) (Object) (partyInfo.partyNumber() == 1
                ? caseData.getRes1MediationNonAttendanceDocs() : caseData.getRes2MediationNonAttendanceDocs());
        }
    }

    @SuppressWarnings("unchecked")
    private <T> void setNonAttendanceStatements(PartyInfo partyInfo, List<Element<T>> documents) {
        CaseData caseData = partyInfo.caseData();
        List<Element<MediationNonAttendanceStatement>> docs = (List<Element<MediationNonAttendanceStatement>>) (Object) documents;
        if (partyInfo.categoryId().contains(CLAIMANT_CATEGORY_SUBSTRING)) {
            if (partyInfo.partyNumber() == 1) {
                caseData.setApp1MediationNonAttendanceDocs(docs);
            } else {
                caseData.setApp2MediationNonAttendanceDocs(docs);
            }
        } else {
            if (partyInfo.partyNumber() == 1) {
                caseData.setRes1MediationNonAttendanceDocs(docs);
            } else {
                caseData.setRes2MediationNonAttendanceDocs(docs);
            }
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
