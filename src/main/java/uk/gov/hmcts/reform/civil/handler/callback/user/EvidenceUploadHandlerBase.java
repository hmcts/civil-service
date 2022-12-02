package uk.gov.hmcts.reform.civil.handler.callback.user;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.Callback;
import uk.gov.hmcts.reform.civil.callback.CallbackHandler;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.enums.MultiPartyScenario;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.caseprogression.UploadEvidenceExpert;
import uk.gov.hmcts.reform.civil.model.caseprogression.UploadEvidenceWitness;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.service.CoreCaseUserService;
import uk.gov.hmcts.reform.civil.service.Time;
import uk.gov.hmcts.reform.civil.service.UserService;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import static uk.gov.hmcts.reform.civil.callback.CallbackParams.Params.BEARER_TOKEN;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.MID;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.SUBMITTED;
import static uk.gov.hmcts.reform.civil.enums.CaseRole.RESPONDENTSOLICITORTWO;

public abstract class EvidenceUploadHandlerBase extends CallbackHandler {

    private final List<CaseEvent> events;
    private final String pageId;
    private final ObjectMapper objectMapper;
    private final Time time;
    private MultiPartyScenario multiPartyScenario;
    private final CoreCaseUserService coreCaseUserService;
    private final UserService userService;

    protected EvidenceUploadHandlerBase(UserService userService, CoreCaseUserService coreCaseUserService,ObjectMapper objectMapper, Time time, List<CaseEvent> events, String pageId) {
        this.objectMapper = objectMapper;
        this.time = time;
        this.events = events;
        this.pageId = pageId;
        this.coreCaseUserService = coreCaseUserService;
        this.userService = userService;

    }

    abstract CallbackResponse validateValues(CaseData caseData);
    abstract CallbackResponse caseType(CaseData caseData, CallbackParams callbackParams);

    abstract void applyDocumentUploadDate(CaseData.CaseDataBuilder<?, ?> caseDataBuilder, LocalDateTime now);

    @Override
    public List<CaseEvent> handledEvents() {
        return events;
    }

    @Override
    protected Map<String, Callback> callbacks() {
        return new ImmutableMap.Builder<String, Callback>()
            .put(callbackKey(ABOUT_TO_START), this::caseType)
            .put(callbackKey(MID, pageId), this::validate)
            .put(callbackKey(ABOUT_TO_SUBMIT), this::documentUploadTime)
            .put(callbackKey(SUBMITTED), this::buildConfirmation)
            .build();
    }

    CallbackResponse caseType(CallbackParams callbackParams) {
        return caseTypeDetermine(callbackParams.getCaseData(), callbackParams);

    }

    CallbackResponse caseTypeDetermine(CaseData caseData, CallbackParams callbackParams) {
        CaseData.CaseDataBuilder<?, ?> caseDataBuilder = caseData.toBuilder();

        UserInfo userInfo = userService.getUserInfo(callbackParams.getParams().get(BEARER_TOKEN).toString());

        if (!multiPartyScenario.getMultiPartyScenario(caseData).equals(MultiPartyScenario.ONE_V_TWO_TWO_LEGAL_REP)) {
              caseDataBuilder.caseTypeFlag("NotMultiParty");
        } else {


            if(coreCaseUserService.userHasCaseRole(caseData
                                                       .getCcdCaseReference()
                                                       .toString(),userInfo.getUid(),RESPONDENTSOLICITORTWO)){
                caseDataBuilder.caseTypeFlag("MultiParty");
            }

        }
        System.out.println(coreCaseUserService.getUserCaseRoles(caseData
                                                                    .getCcdCaseReference().toString(),userInfo.getUid()));

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDataBuilder.build().toMap(objectMapper))
            .build();
    }

    CallbackResponse validate(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        System.out.println(caseData.getCaseTypeFlag());
        return validateValues(callbackParams.getCaseData());

    }

    CallbackResponse validateValuesParty(List<Element<UploadEvidenceWitness>> uploadEvidenceWitness1,
                                         List<Element<UploadEvidenceWitness>> uploadEvidenceWitness3,
                                         List<Element<UploadEvidenceExpert>> uploadEvidenceExpert1,
                                         List<Element<UploadEvidenceExpert>> uploadEvidenceExpert2,
                                         List<Element<UploadEvidenceExpert>> uploadEvidenceExpert3,
                                         List<Element<UploadEvidenceExpert>> uploadEvidenceExpert4) {
        List<String> errors = new ArrayList<>();

        checkDateCorrectness(time, errors, uploadEvidenceWitness1, date -> date.getValue()
                                 .getWitnessOptionUploadDate(),
                             "Invalid date: \"witness statement\" "
                                 + "date entered must not be in the future (1).");
        checkDateCorrectness(time, errors, uploadEvidenceWitness3, date -> date.getValue()
                                 .getWitnessOptionUploadDate(),
                             "Invalid date: \"Notice of the intention to rely on hearsay evidence\" "
                                 + "date entered must not be in the future (2).");

        checkDateCorrectness(time, errors, uploadEvidenceExpert1, date -> date.getValue()
                                 .getExpertOptionUploadDate(),
                             "Invalid date: \"Expert's report\""
                                 + " date entered must not be in the future (3).");
        checkDateCorrectness(time, errors, uploadEvidenceExpert2, date -> date.getValue()
                                 .getExpertOptionUploadDate(),
                             "Invalid date: \"Joint statement of experts\" "
                                 + "date entered must not be in the future (4).");
        checkDateCorrectness(time, errors, uploadEvidenceExpert3, date -> date.getValue()
                                 .getExpertOptionUploadDate(),
                             "Invalid date: \"Questions for other party's expert or joint experts\" "
                                 + "expert statement date entered must not be in the future (5).");
        checkDateCorrectness(time, errors, uploadEvidenceExpert4, date -> date.getValue()
                                 .getExpertOptionUploadDate(),
                             "Invalid date: \"Answers to questions asked by the other party\" "
                                 + "date entered must not be in the future (6).");

        return AboutToStartOrSubmitCallbackResponse.builder()
            .errors(errors)
            .build();
    }

    <T> void checkDateCorrectness(Time time, List<String> errors, List<Element<T>> documentUpload,
                                  Function<Element<T>, LocalDate> dateExtractor, String errorMessage) {
        if (documentUpload == null) {
            return;
        }
        documentUpload.forEach(date -> {
            LocalDate dateToCheck = dateExtractor.apply(date);
            if (dateToCheck.isAfter(time.now().toLocalDate())) {
                errors.add(errorMessage);
            }
        });
    }

    CallbackResponse documentUploadTime(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        CaseData.CaseDataBuilder<?, ?> caseDataBuilder = caseData.toBuilder();
        applyDocumentUploadDate(caseDataBuilder, time.now());
        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDataBuilder.build().toMap(objectMapper))
            .build();
    }

    SubmittedCallbackResponse buildConfirmation(CallbackParams callbackParams) {
        return SubmittedCallbackResponse.builder()
            .confirmationHeader("# Documents uploaded")
            .confirmationBody("You can continue uploading documents or return later. To upload more "
                                  + "documents, go to Next step and select \"Document Upload\".")
            .build();
    }
}
