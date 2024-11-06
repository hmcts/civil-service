package uk.gov.hmcts.reform.civil.handler.callback.user.respondtoclaimspeccallbackhandlertasks;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.enums.MultiPartyResponseTypeFlags;
import uk.gov.hmcts.reform.civil.enums.MultiPartyScenario;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpecPaidStatus;
import uk.gov.hmcts.reform.civil.handler.callback.user.spec.show.DefendantResponseShowTag;
import uk.gov.hmcts.reform.civil.handler.callback.user.task.CaseTask;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.CoreCaseUserService;
import uk.gov.hmcts.reform.civil.service.UserService;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import static uk.gov.hmcts.reform.civil.callback.CallbackParams.Params.BEARER_TOKEN;
import static uk.gov.hmcts.reform.civil.enums.CaseRole.RESPONDENTSOLICITORTWO;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.ONE_V_ONE;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.ONE_V_TWO_ONE_LEGAL_REP;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.ONE_V_TWO_TWO_LEGAL_REP;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.TWO_V_ONE;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.getMultiPartyScenario;
import static uk.gov.hmcts.reform.civil.enums.RespondentResponsePartAdmissionPaymentTimeLRspec.IMMEDIATELY;
import static uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec.FULL_ADMISSION;
import static uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec.FULL_DEFENCE;
import static uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec.PART_ADMISSION;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.handler.callback.user.spec.show.DefendantResponseShowTag.CAN_ANSWER_RESPONDENT_1;
import static uk.gov.hmcts.reform.civil.handler.callback.user.spec.show.DefendantResponseShowTag.CAN_ANSWER_RESPONDENT_2;
import static uk.gov.hmcts.reform.civil.handler.callback.user.spec.show.DefendantResponseShowTag.CURRENT_ADMITS_PART_OR_FULL;
import static uk.gov.hmcts.reform.civil.handler.callback.user.spec.show.DefendantResponseShowTag.RESPONDENT_1_ADMITS_PART_OR_FULL;
import static uk.gov.hmcts.reform.civil.handler.callback.user.spec.show.DefendantResponseShowTag.RESPONDENT_2_ADMITS_PART_OR_FULL;
import static uk.gov.hmcts.reform.civil.handler.callback.user.spec.show.DefendantResponseShowTag.SOMEONE_DISPUTES;

@Component
@RequiredArgsConstructor
@Slf4j
public class SetGenericResponseTypeFlag implements CaseTask {

    private final ObjectMapper objectMapper;
    private final UserService userService;
    private final CoreCaseUserService coreCaseUserService;
    private final RespondToClaimSpecUtils respondToClaimSpecUtils;

    public CallbackResponse execute(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        CaseData.CaseDataBuilder<?, ?> updatedData = caseData.toBuilder()
            .multiPartyResponseTypeFlags(MultiPartyResponseTypeFlags.NOT_FULL_DEFENCE);

        handleClaimantResponseTypeForSpec(caseData, updatedData);
        MultiPartyScenario multiPartyScenario = getMultiPartyScenario(caseData);
        handleOneVOneScenario(caseData, updatedData, multiPartyScenario);
        handleTwoVOneScenario(caseData, updatedData, multiPartyScenario);
        handleOneVTwoOneLegalRepScenario(caseData, updatedData, multiPartyScenario);
        handleOneVTwoTwoLegalRepScenario(callbackParams, caseData, updatedData, multiPartyScenario);
        handleRespondentResponseTypeForSpec(caseData, updatedData);
        handleDefenceAdmitPartPaymentTimeRoute(caseData, updatedData);

        Set<DefendantResponseShowTag> updatedShowConditions = whoDisputesPartAdmission(caseData);
        updateShowConditions(caseData, updatedShowConditions);
        updatedData.showConditionFlags(updatedShowConditions);

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(updatedData.build().toMap(objectMapper))
            .build();
    }

    private void handleClaimantResponseTypeForSpec(CaseData caseData, CaseData.CaseDataBuilder<?, ?> updatedData) {
        if ((RespondentResponseTypeSpec.FULL_ADMISSION.equals(caseData.getClaimant1ClaimResponseTypeForSpec())
            || RespondentResponseTypeSpec.PART_ADMISSION.equals(caseData.getClaimant1ClaimResponseTypeForSpec())
            || RespondentResponseTypeSpec.COUNTER_CLAIM.equals(caseData.getClaimant1ClaimResponseTypeForSpec()))
            &&
            (RespondentResponseTypeSpec.FULL_ADMISSION.equals(caseData.getClaimant2ClaimResponseTypeForSpec())
                || RespondentResponseTypeSpec.PART_ADMISSION.equals(caseData.getClaimant2ClaimResponseTypeForSpec())
                || RespondentResponseTypeSpec.COUNTER_CLAIM.equals(caseData.getClaimant2ClaimResponseTypeForSpec()))) {
            updatedData.multiPartyResponseTypeFlags(MultiPartyResponseTypeFlags.COUNTER_ADMIT_OR_ADMIT_PART);
        }
    }

    private void handleOneVOneScenario(CaseData caseData, CaseData.CaseDataBuilder<?, ?> updatedData, MultiPartyScenario multiPartyScenario) {
        if (ONE_V_ONE.equals(multiPartyScenario)) {
            updatedData.respondentClaimResponseTypeForSpecGeneric(caseData.getRespondent1ClaimResponseTypeForSpec());
            if (caseData.getRespondent1ClaimResponseTypeForSpec() == FULL_DEFENCE) {
                updatedData.multiPartyResponseTypeFlags(MultiPartyResponseTypeFlags.FULL_DEFENCE);
            } else if (caseData.getRespondent1ClaimResponseTypeForSpec() == RespondentResponseTypeSpec.COUNTER_CLAIM) {
                updatedData.multiPartyResponseTypeFlags(MultiPartyResponseTypeFlags.COUNTER_ADMIT_OR_ADMIT_PART);
            } else if (caseData.getRespondent1ClaimResponseTypeForSpec() == RespondentResponseTypeSpec.FULL_ADMISSION
                || caseData.getRespondent2ClaimResponseTypeForSpec() == RespondentResponseTypeSpec.FULL_ADMISSION) {
                updatedData.multiPartyResponseTypeFlags(MultiPartyResponseTypeFlags.FULL_ADMISSION);
            }
        }
    }

    private void handleTwoVOneScenario(CaseData caseData, CaseData.CaseDataBuilder<?, ?> updatedData, MultiPartyScenario multiPartyScenario) {
        Set<RespondentResponseTypeSpec> someAdmission = EnumSet.of(PART_ADMISSION, FULL_ADMISSION);
        if (TWO_V_ONE.equals(multiPartyScenario)
            && someAdmission.contains(caseData.getRespondent1ClaimResponseTypeForSpec())
            && someAdmission.contains(caseData.getRespondent2ClaimResponseTypeForSpec())) {
            updatedData.specFullAdmissionOrPartAdmission(YES);
        } else {
            updatedData.specFullAdmissionOrPartAdmission(NO);
        }
    }

    private void handleOneVTwoOneLegalRepScenario(CaseData caseData, CaseData.CaseDataBuilder<?, ?> updatedData, MultiPartyScenario multiPartyScenario) {
        if (ONE_V_TWO_ONE_LEGAL_REP.equals(multiPartyScenario)
            && Objects.equals(caseData.getRespondent1ClaimResponseTypeForSpec(), caseData.getRespondent2ClaimResponseTypeForSpec())) {
            updatedData.respondentResponseIsSame(YES);
            caseData = caseData.toBuilder().respondentResponseIsSame(YES).build();
        }
        if (ONE_V_TWO_ONE_LEGAL_REP.equals(multiPartyScenario) && caseData.getRespondentResponseIsSame().equals(NO)) {
            updatedData.sameSolicitorSameResponse(NO);
            if (FULL_DEFENCE.equals(caseData.getRespondent1ClaimResponseTypeForSpec())
                || FULL_DEFENCE.equals(caseData.getRespondent2ClaimResponseTypeForSpec())) {
                updatedData.respondentClaimResponseTypeForSpecGeneric(FULL_DEFENCE);
            }
        } else if (ONE_V_TWO_ONE_LEGAL_REP.equals(multiPartyScenario) && caseData.getRespondentResponseIsSame().equals(YES)) {
            updatedData.sameSolicitorSameResponse(YES);
            updatedData.respondentClaimResponseTypeForSpecGeneric(caseData.getRespondent1ClaimResponseTypeForSpec());
            if (FULL_DEFENCE.equals(caseData.getRespondent1ClaimResponseTypeForSpec())
                || RespondentResponseTypeSpec.COUNTER_CLAIM.equals(caseData.getRespondent1ClaimResponseTypeForSpec())) {
                updatedData.multiPartyResponseTypeFlags(MultiPartyResponseTypeFlags.FULL_DEFENCE);
            }
        } else {
            updatedData.respondentClaimResponseTypeForSpecGeneric(caseData.getRespondent1ClaimResponseTypeForSpec());
        }
    }

    private void handleOneVTwoTwoLegalRepScenario(CallbackParams callbackParams, CaseData caseData, CaseData.CaseDataBuilder<?, ?> updatedData, MultiPartyScenario multiPartyScenario) {
        UserInfo userInfo = userService.getUserInfo(callbackParams.getParams().get(BEARER_TOKEN).toString());
        if (ONE_V_TWO_TWO_LEGAL_REP.equals(multiPartyScenario)) {
            if (coreCaseUserService.userHasCaseRole(caseData.getCcdCaseReference().toString(), userInfo.getUid(), RESPONDENTSOLICITORTWO)) {
                updatedData.respondentClaimResponseTypeForSpecGeneric(caseData.getRespondent2ClaimResponseTypeForSpec());
            } else {
                updatedData.respondentClaimResponseTypeForSpecGeneric(caseData.getRespondent1ClaimResponseTypeForSpec());
            }
        }

        if (ONE_V_TWO_TWO_LEGAL_REP.equals(multiPartyScenario)
            && ((YES.equals(caseData.getIsRespondent1())
            && RespondentResponseTypeSpec.PART_ADMISSION.equals(caseData.getRespondent1ClaimResponseTypeForSpec()))
            || (YES.equals(caseData.getIsRespondent2())
            && RespondentResponseTypeSpec.PART_ADMISSION.equals(caseData.getRespondent2ClaimResponseTypeForSpec())))) {
            updatedData.multiPartyResponseTypeFlags(MultiPartyResponseTypeFlags.PART_ADMISSION);
        }
    }

    private void handleRespondentResponseTypeForSpec(CaseData caseData, CaseData.CaseDataBuilder<?, ?> updatedData) {
        if (YES.equals(caseData.getIsRespondent2())) {
            updatedData.respondentClaimResponseTypeForSpecGeneric(caseData.getRespondent2ClaimResponseTypeForSpec());
        }

        if (caseData.getRespondent1ClaimResponseTypeForSpec() == FULL_DEFENCE
            || caseData.getRespondent2ClaimResponseTypeForSpec() == FULL_DEFENCE
            || caseData.getClaimant1ClaimResponseTypeForSpec() == FULL_DEFENCE
            || caseData.getClaimant2ClaimResponseTypeForSpec() == FULL_DEFENCE) {
            updatedData.multiPartyResponseTypeFlags(MultiPartyResponseTypeFlags.FULL_DEFENCE);
        }

        if ((YES.equals(caseData.getIsRespondent1())
            && (caseData.getRespondent1ClaimResponseTypeForSpec() == RespondentResponseTypeSpec.PART_ADMISSION
            || caseData.getRespondent1ClaimResponseTypeForSpec() == RespondentResponseTypeSpec.FULL_ADMISSION))
            || (YES.equals(caseData.getIsRespondent2())
            && (caseData.getRespondent2ClaimResponseTypeForSpec() == RespondentResponseTypeSpec.PART_ADMISSION
            || caseData.getRespondent2ClaimResponseTypeForSpec() == RespondentResponseTypeSpec.FULL_ADMISSION))) {
            updatedData.specFullAdmissionOrPartAdmission(YES);
        }
        if (RespondentResponseTypeSpec.FULL_ADMISSION.equals(caseData.getRespondent2ClaimResponseTypeForSpec())
            || RespondentResponseTypeSpec.PART_ADMISSION.equals(caseData.getRespondent2ClaimResponseTypeForSpec())
            || RespondentResponseTypeSpec.COUNTER_CLAIM.equals(caseData.getRespondent2ClaimResponseTypeForSpec())) {
            updatedData.multiPartyResponseTypeFlags(MultiPartyResponseTypeFlags.COUNTER_ADMIT_OR_ADMIT_PART);
        }
        if (caseData.getRespondent1ClaimResponseTypeForSpec() == RespondentResponseTypeSpec.PART_ADMISSION
            || caseData.getRespondent1ClaimResponseTypeForSpec() == FULL_DEFENCE) {
            updatedData.specFullDefenceOrPartAdmission1V1(YES);
        }
        if (caseData.getRespondent1ClaimResponseTypeForSpec() == RespondentResponseTypeSpec.PART_ADMISSION
            || caseData.getRespondent1ClaimResponseTypeForSpec() == FULL_DEFENCE
            || caseData.getRespondent2ClaimResponseTypeForSpec() == RespondentResponseTypeSpec.PART_ADMISSION
            || caseData.getRespondent2ClaimResponseTypeForSpec() == FULL_DEFENCE) {
            updatedData.specFullDefenceOrPartAdmission(YES);
        } else {
            updatedData.specFullDefenceOrPartAdmission(NO);
        }
        if (caseData.getRespondent1ClaimResponseTypeForSpec() != RespondentResponseTypeSpec.FULL_ADMISSION
            || caseData.getRespondent2ClaimResponseTypeForSpec() != RespondentResponseTypeSpec.FULL_ADMISSION) {
            updatedData.specDefenceFullAdmittedRequired(NO);
        }

        if (YES.equals(caseData.getSpecPaidLessAmountOrDisputesOrPartAdmission())
            && !MultiPartyResponseTypeFlags.COUNTER_ADMIT_OR_ADMIT_PART.equals(caseData.getMultiPartyResponseTypeFlags())
            && (!RespondentResponseTypeSpecPaidStatus.PAID_FULL_OR_MORE_THAN_CLAIMED_AMOUNT.equals(caseData.getRespondent1ClaimResponsePaymentAdmissionForSpec()))) {
            updatedData.showHowToAddTimeLinePage(YES);
        }

        if (YES.equals(caseData.getIsRespondent1())) {
            if (RespondentResponseTypeSpec.COUNTER_CLAIM.equals(caseData.getRespondent1ClaimResponseTypeForSpec())) {
                updatedData.showHowToAddTimeLinePage(NO);
                updatedData.multiPartyResponseTypeFlags(MultiPartyResponseTypeFlags.COUNTER_ADMIT_OR_ADMIT_PART);
            } else if (RespondentResponseTypeSpec.FULL_ADMISSION.equals(caseData.getRespondent1ClaimResponseTypeForSpec())) {
                updatedData.showHowToAddTimeLinePage(NO);
            }
        } else if (YES.equals(caseData.getIsRespondent2())) {
            if (RespondentResponseTypeSpec.COUNTER_CLAIM.equals(caseData.getRespondent2ClaimResponseTypeForSpec())) {
                updatedData.showHowToAddTimeLinePage(NO);
                updatedData.multiPartyResponseTypeFlags(MultiPartyResponseTypeFlags.COUNTER_ADMIT_OR_ADMIT_PART);
            } else if (RespondentResponseTypeSpec.FULL_ADMISSION.equals(caseData.getRespondent2ClaimResponseTypeForSpec())) {
                updatedData.showHowToAddTimeLinePage(NO);
            }
        }

        if (YES.equals(caseData.getIsRespondent2()) && YES.equals(caseData.getSpecDefenceAdmittedRequired())) {
            updatedData.partAdmittedByEitherRespondents(YES);
        } else if (YES.equals(caseData.getIsRespondent1()) && YES.equals(caseData.getSpecDefenceAdmitted2Required())) {
            updatedData.partAdmittedByEitherRespondents(YES);
        } else {
            updatedData.partAdmittedByEitherRespondents(NO);
        }

        if (YES.equals(caseData.getIsRespondent2()) && YES.equals(caseData.getSpecDefenceFullAdmitted2Required())) {
            updatedData.fullAdmissionAndFullAmountPaid(YES);
        } else if (YES.equals(caseData.getIsRespondent1()) && YES.equals(caseData.getSpecDefenceFullAdmittedRequired())) {
            updatedData.fullAdmissionAndFullAmountPaid(YES);
        } else {
            updatedData.fullAdmissionAndFullAmountPaid(NO);
        }
    }

    private void handleDefenceAdmitPartPaymentTimeRoute(CaseData caseData, CaseData.CaseDataBuilder<?, ?> updatedData) {
        if (YES.equals(caseData.getIsRespondent1()) && caseData.getDefenceAdmitPartPaymentTimeRouteRequired() != null) {
            updatedData.defenceAdmitPartPaymentTimeRouteGeneric(caseData.getDefenceAdmitPartPaymentTimeRouteRequired());
        } else if (YES.equals(caseData.getIsRespondent2()) && caseData.getDefenceAdmitPartPaymentTimeRouteRequired2() != null) {
            updatedData.defenceAdmitPartPaymentTimeRouteGeneric(caseData.getDefenceAdmitPartPaymentTimeRouteRequired2());
        } else {
            updatedData.defenceAdmitPartPaymentTimeRouteGeneric(IMMEDIATELY);
        }
    }

    private void updateShowConditions(CaseData caseData, Set<DefendantResponseShowTag> updatedShowConditions) {
        EnumSet<RespondentResponseTypeSpec> anyAdmission = EnumSet.of(
            RespondentResponseTypeSpec.PART_ADMISSION,
            RespondentResponseTypeSpec.FULL_ADMISSION
        );
        if (updatedShowConditions.contains(CAN_ANSWER_RESPONDENT_1)
            && anyAdmission.contains(caseData.getRespondent1ClaimResponseTypeForSpec())) {
            updatedShowConditions.add(RESPONDENT_1_ADMITS_PART_OR_FULL);
            if (caseData.getRespondentResponseIsSame() == YES) {
                updatedShowConditions.add(RESPONDENT_2_ADMITS_PART_OR_FULL);
            }
        }
        if (caseData.getShowConditionFlags().contains(CAN_ANSWER_RESPONDENT_2)
            && anyAdmission.contains(caseData.getRespondent2ClaimResponseTypeForSpec())) {
            updatedShowConditions.add(RESPONDENT_2_ADMITS_PART_OR_FULL);
        }
        if (someoneDisputes(caseData)) {
            updatedShowConditions.add(SOMEONE_DISPUTES);
        }
        if ((anyAdmission.contains(caseData.getRespondent1ClaimResponseTypeForSpec())
            && YES.equals(caseData.getIsRespondent1()))
            || (anyAdmission.contains(caseData.getRespondent2ClaimResponseTypeForSpec())
            && YES.equals(caseData.getIsRespondent2()))) {
            updatedShowConditions.removeIf(EnumSet.of(CURRENT_ADMITS_PART_OR_FULL)::contains);
            updatedShowConditions.add(CURRENT_ADMITS_PART_OR_FULL);
        }
    }

    private Set<DefendantResponseShowTag> whoDisputesPartAdmission(CaseData caseData) {
        Set<DefendantResponseShowTag> tags = new HashSet<>(caseData.getShowConditionFlags());
        respondToClaimSpecUtils.removeWhoDisputesAndWhoPaidLess(tags);
        tags.addAll(respondToClaimSpecUtils.whoDisputesBcoPartAdmission(caseData));
        return tags;
    }

    private boolean someoneDisputes(CaseData caseData) {
        if (TWO_V_ONE.equals(getMultiPartyScenario(caseData))) {
            return ((caseData.getClaimant1ClaimResponseTypeForSpec() == FULL_DEFENCE
                || caseData.getClaimant2ClaimResponseTypeForSpec() == FULL_DEFENCE)
                || caseData.getRespondent1ClaimResponseTypeForSpec() == FULL_DEFENCE
                || caseData.getRespondent1ClaimResponseTypeForSpec() == PART_ADMISSION);
        } else {
            return someoneDisputes(caseData, CAN_ANSWER_RESPONDENT_1,
                                   caseData.getRespondent1ClaimResponseTypeForSpec()
            )
                || someoneDisputes(caseData, CAN_ANSWER_RESPONDENT_2,
                                   caseData.getRespondent2ClaimResponseTypeForSpec()
            );
        }
    }

    private boolean someoneDisputes(CaseData caseData, DefendantResponseShowTag respondent,
                                    RespondentResponseTypeSpec response) {
        return caseData.getShowConditionFlags().contains(respondent)
            && (response == FULL_DEFENCE
            || (response == PART_ADMISSION && !NO.equals(caseData.getRespondentResponseIsSame())));
    }
}
