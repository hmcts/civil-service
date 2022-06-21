package uk.gov.hmcts.reform.civil.handler.callback.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.Callback;
import uk.gov.hmcts.reform.civil.callback.CallbackException;
import uk.gov.hmcts.reform.civil.callback.CallbackHandler;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.enums.MultiPartyScenario;
import uk.gov.hmcts.reform.civil.model.BusinessProcess;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.sdo.DisposalHearingBundle;
import uk.gov.hmcts.reform.civil.model.sdo.DisposalHearingDisclosureOfDocuments;
import uk.gov.hmcts.reform.civil.model.sdo.DisposalHearingFinalDisposalHearing;
import uk.gov.hmcts.reform.civil.model.sdo.DisposalHearingJudgesRecital;
import uk.gov.hmcts.reform.civil.model.sdo.DisposalHearingMedicalEvidence;
import uk.gov.hmcts.reform.civil.model.sdo.DisposalHearingNotes;
import uk.gov.hmcts.reform.civil.model.sdo.DisposalHearingQuestionsToExperts;
import uk.gov.hmcts.reform.civil.model.sdo.DisposalHearingSchedulesOfLoss;
import uk.gov.hmcts.reform.civil.model.sdo.DisposalHearingStandardDisposalOrder;
import uk.gov.hmcts.reform.civil.model.sdo.DisposalHearingWitnessOfFact;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static java.lang.String.format;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.MID;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.SUBMITTED;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.STANDARD_DIRECTION_ORDER_DJ;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.getMultiPartyScenario;

@Service
@RequiredArgsConstructor
public class StandardDirectionOrderDJ extends CallbackHandler {

    private static final List<CaseEvent> EVENTS = Collections.singletonList(STANDARD_DIRECTION_ORDER_DJ);
    private final ObjectMapper objectMapper;
    String participantString;
    public static final String ORDER_1_CLAI = "The directions order has been sent to: "
        + "%n%n ## Claimant 1 %n%n %s";
    public static final String ORDER_1_DEF = "%n%n ## Defendant 1 %n%n %s";
    public static final String ORDER_2_DEF = "%n%n ## Defendant 2 %n%n %s";
    public static final String ORDER_ISSUED = "# Your order has been issued %n%n ## Claim number %n%n # %s";

    @Override
    protected Map<String, Callback> callbacks() {
        return new ImmutableMap.Builder<String, Callback>()
            .put(callbackKey(ABOUT_TO_START), this::initiateSDO)
            .put(callbackKey(MID, "disposal-screen"), this::populateDisposalScreen)
            .put(callbackKey(ABOUT_TO_SUBMIT), this::generateSDONotifications)
            .put(callbackKey(SUBMITTED), this::buildConfirmation)
            .build();
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    private String getBody(CaseData caseData) {
        if (caseData.getRespondent2() != null
            && caseData.getDefendantDetails().getValue()
            .getLabel().startsWith("Both")) {
            return format(ORDER_1_CLAI, caseData.getApplicant1().getPartyName())
                + format(ORDER_1_DEF, caseData.getRespondent1().getPartyName())
                + format(ORDER_2_DEF, caseData.getRespondent2().getPartyName());

        } else {
            return format(ORDER_1_CLAI, caseData.getApplicant1().getPartyName())
                + format(ORDER_1_DEF, caseData.getRespondent1().getPartyName());
        }
    }

    private String getHeader(CaseData caseData) {
        return format(ORDER_ISSUED, caseData.getLegacyCaseReference());
    }

    private CallbackResponse initiateSDO(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        CaseData.CaseDataBuilder caseDataBuilder = caseData.toBuilder();

        caseDataBuilder.applicantVRespondentText(caseParticipants(caseData));

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDataBuilder.build().toMap(objectMapper))
            .build();

    }

    public String caseParticipants(CaseData caseData) {
        MultiPartyScenario multiPartyScenario = getMultiPartyScenario(caseData);
        switch (multiPartyScenario) {

            case ONE_V_ONE:
                participantString = (caseData.getApplicant1().getPartyName() + " v " + caseData.getRespondent1()
                    .getPartyName());
                break;
            case ONE_V_TWO_ONE_LEGAL_REP:
                participantString = (caseData.getApplicant1().getPartyName() + " v " + caseData.getRespondent1()
                    .getPartyName() + " and " + caseData.getRespondent2().getPartyName());
                break;

            case TWO_V_ONE:
                participantString = (caseData.getApplicant1().getPartyName() + " and " + caseData.getApplicant2()
                    .getPartyName() + " v " + caseData.getRespondent1().getPartyName());
                break;
            default:
                throw new CallbackException(format("Invalid participants"));
        }
        return participantString;

    }

    private CallbackResponse populateDisposalScreen(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        CaseData.CaseDataBuilder caseDataBuilder = caseData.toBuilder();

        caseDataBuilder
            .disposalHearingJudgesRecitalDJ(DisposalHearingJudgesRecital
                                                           .builder()
                                                           .input("Upon considering the claim Form and "
                                                                      + "Particulars of Claim/statements of case"
                                                                      + " [and the directions questionnaires] "
                                                                      + "\n\nIT IS ORDERED that:-").build());
        caseDataBuilder
            .disposalHearingDisclosureOfDocumentsDJ(DisposalHearingDisclosureOfDocuments
                                                                   .builder()
                                                                   .input("The parties shall serve on each other "
                                                                              + "copies of the documents upon which "
                                                                              + "reliance is to be"
                                                                              + " placed at the disposal hearing "
                                                                              + "by 4pm on")
                                                        .date(LocalDate.now().plusWeeks(4))
                                                        .build());

        caseDataBuilder
            .disposalHearingWitnessOfFactDJ(DisposalHearingWitnessOfFact
                                                .builder()
                                                .input1("The claimant shall serve on every other party the witness "
                                                            + "statements of all witnesses of fact"
                                                            + " on whose evidence reliance is to be placed by 4pm on")
                                                .date1(LocalDate.now().plusWeeks(4))
                                                .input2("The provisions of CPR 32.6 apply to such evidence.")
                                                .input3("Any application by the defendant/s pursuant to CPR 32.7 "
                                                            + "must be made by 4pm on")
                                                .date2(LocalDate.now().plusWeeks(2))
                                                .input4("and must be accompanied by proposed directions for "
                                                            + "allocation and listing for trial on quantum as"
                                                            + " cross-examination will result in the hearing "
                                                            + "exceeding the 30 minute maximum time estimate"
                                                            + " for a disposal hearing")
                                                .build());

        caseDataBuilder.disposalHearingMedicalEvidenceDJ(DisposalHearingMedicalEvidence
                                                             .builder()
                                                             .input1("The claimant has permission to rely upon the"
                                                                         + " written expert evidence served with the"
                                                                         + " Particulars of Claim to be disclosed "
                                                                         + "by 4pm")
                                                             .date1(LocalDate.now().plusWeeks(4))
                                                             .input2("and any associated correspondence and/or "
                                                                         + "updating report disclosed not later "
                                                                         + "than 4pm on the")
                                                             .date2(LocalDate.now().plusWeeks(4))
                                                             .build());

        caseDataBuilder.disposalHearingQuestionsToExpertsDJ(DisposalHearingQuestionsToExperts
                                                                .builder()
                                                                .date(LocalDate.now().plusWeeks(6))
                                                                .build());

        caseDataBuilder.disposalHearingSchedulesOfLossDJ(DisposalHearingSchedulesOfLoss
                                                             .builder()
                                                             .input1("If there is a claim for ongoing/future loss "
                                                                         + "in the original schedule of losses then"
                                                                         + " the claimant"
                                                                         + " must send an up to date schedule of "
                                                                         + "loss to the defendant by 4pm on the")
                                                             .date1(LocalDate.now().plusWeeks(10))
                                                             .input2("The defendant, in the event of challenge, "
                                                                         + "must send an up to date counter-schedule "
                                                                         + "of loss"
                                                                         + " to the claimant by 4pm on the")
                                                             .date2(LocalDate.now().plusWeeks(12))
                                                             .build());

        caseDataBuilder.disposalHearingStandardDisposalOrderDJ(DisposalHearingStandardDisposalOrder
                                                                   .builder()
                                                                   .input("input")
                                                                   .build());

        caseDataBuilder.disposalHearingFinalDisposalHearingDJ(DisposalHearingFinalDisposalHearing
                                                                  .builder()
                                                                  .input("This claim be listed for final "
                                                                             + "disposal before a Judge on the first "
                                                                             + "available date after.")
                                                                  .date(LocalDate.now().plusWeeks(16))
                                                                  .build());

        caseDataBuilder.disposalHearingBundleDJ(DisposalHearingBundle
                                                    .builder()
                                                    .input("The claimant must lodge at court at least 7 "
                                                               + "days before the disposal")
                                                    .build());

        caseDataBuilder.disposalHearingNotesDJ(DisposalHearingNotes
                                                 .builder()
                                                 .input("This Order has been made without a hearing. Each party "
                                                            + "has the right to apply to have this Order"
                                                            + " set aside or varied. Any such application must be "
                                                            + "received by the Court"
                                                            + " (together with the appropriate fee) by 4pm on")
                                                 .date(LocalDate.now().plusWeeks(1))
                                                 .build());

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDataBuilder.build().toMap(objectMapper))
            .build();
    }

    private CallbackResponse generateSDONotifications(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        CaseData.CaseDataBuilder<?, ?> caseDataBuilder = caseData.toBuilder();
        caseDataBuilder.businessProcess(BusinessProcess.ready(STANDARD_DIRECTION_ORDER_DJ));

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDataBuilder.build().toMap(objectMapper))
            .build();
    }

    private SubmittedCallbackResponse buildConfirmation(CallbackParams callbackParams) {
        var caseData = callbackParams.getCaseData();
        return SubmittedCallbackResponse.builder()
            .confirmationHeader(getHeader(caseData))
            .confirmationBody(getBody(caseData))
            .build();
    }

}
