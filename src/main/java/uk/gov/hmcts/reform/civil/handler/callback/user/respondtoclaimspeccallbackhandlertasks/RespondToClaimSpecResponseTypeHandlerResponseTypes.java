package uk.gov.hmcts.reform.civil.handler.callback.user.respondtoclaimspeccallbackhandlertasks;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.enums.MultiPartyResponseTypeFlags;
import uk.gov.hmcts.reform.civil.enums.MultiPartyScenario;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec;
import uk.gov.hmcts.reform.civil.enums.TimelineUploadTypeSpec;
import uk.gov.hmcts.reform.civil.handler.callback.user.spec.show.DefendantResponseShowTag;
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
import static uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec.COUNTER_CLAIM;
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
import static uk.gov.hmcts.reform.civil.handler.callback.user.spec.show.DefendantResponseShowTag.TIMELINE_MANUALLY;
import static uk.gov.hmcts.reform.civil.handler.callback.user.spec.show.DefendantResponseShowTag.TIMELINE_UPLOAD;

@Component
@RequiredArgsConstructor
public class RespondToClaimSpecResponseTypeHandlerResponseTypes {

    private final ObjectMapper objectMapper;
    private final UserService userService;
    private final CoreCaseUserService coreCaseUserService;

    public CallbackResponse handleRespondentResponseTypeForSpec(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        if (caseData.getRespondent1ClaimResponseTypeForSpec() != FULL_ADMISSION
            || caseData.getRespondent2ClaimResponseTypeForSpec() != FULL_ADMISSION) {
            caseData = caseData.toBuilder().specDefenceFullAdmittedRequired(NO).build();
        }
        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseData.toMap(objectMapper))
            .build();
    }

    public CallbackResponse setGenericResponseTypeFlag(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        CaseData.CaseDataBuilder<?, ?> updatedData = caseData.toBuilder()
            .multiPartyResponseTypeFlags(MultiPartyResponseTypeFlags.NOT_FULL_DEFENCE);

        updateResponseTypeFlags(caseData, updatedData);
        updateMultiPartyScenarioFlags(caseData, updatedData, callbackParams);
        updateShowConditions(caseData, updatedData);

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(updatedData.build().toMap(objectMapper))
            .build();
    }

    private void updateResponseTypeFlags(CaseData caseData, CaseData.CaseDataBuilder<?, ?> updatedData) {
        MultiPartyScenario multiPartyScenario = getMultiPartyScenario(caseData);

        if (isFullAdmissionOrPartAdmission(caseData)) {
            updatedData.multiPartyResponseTypeFlags(MultiPartyResponseTypeFlags.COUNTER_ADMIT_OR_ADMIT_PART);
        }
        if (ONE_V_ONE.equals(multiPartyScenario)) {
            updatedData.respondentClaimResponseTypeForSpecGeneric(caseData.getRespondent1ClaimResponseTypeForSpec());
            if (FULL_DEFENCE.equals(caseData.getRespondent1ClaimResponseTypeForSpec())) {
                updatedData.multiPartyResponseTypeFlags(MultiPartyResponseTypeFlags.FULL_DEFENCE);
            } else if (COUNTER_CLAIM.equals(caseData.getRespondent1ClaimResponseTypeForSpec())) {
                updatedData.multiPartyResponseTypeFlags(MultiPartyResponseTypeFlags.COUNTER_ADMIT_OR_ADMIT_PART);
            } else if (FULL_ADMISSION.equals(caseData.getRespondent1ClaimResponseTypeForSpec())
                || FULL_ADMISSION.equals(caseData.getRespondent2ClaimResponseTypeForSpec())) {
                updatedData.multiPartyResponseTypeFlags(MultiPartyResponseTypeFlags.FULL_ADMISSION);
            }
        }
    }

    private boolean isFullAdmissionOrPartAdmission(CaseData caseData) {
        return (FULL_ADMISSION.equals(caseData.getClaimant1ClaimResponseTypeForSpec())
            || PART_ADMISSION.equals(caseData.getClaimant1ClaimResponseTypeForSpec())
            || COUNTER_CLAIM.equals(caseData.getClaimant1ClaimResponseTypeForSpec()))
            && (FULL_ADMISSION.equals(caseData.getClaimant2ClaimResponseTypeForSpec())
            || PART_ADMISSION.equals(caseData.getClaimant2ClaimResponseTypeForSpec())
            || COUNTER_CLAIM.equals(caseData.getClaimant2ClaimResponseTypeForSpec()));
    }

    private void updateMultiPartyScenarioFlags(CaseData caseData, CaseData.CaseDataBuilder<?, ?> updatedData, CallbackParams callbackParams) {
        MultiPartyScenario multiPartyScenario = getMultiPartyScenario(caseData);

        if (TWO_V_ONE.equals(multiPartyScenario)
            && containsAdmission(caseData, PART_ADMISSION, FULL_ADMISSION)) {
            updatedData.specFullAdmissionOrPartAdmission(YES);
        } else {
            updatedData.specFullAdmissionOrPartAdmission(NO);
        }

        handleOneVsTwoOneLegalRep(caseData, updatedData);
        handleOneVsTwoTwoLegalRep(caseData, updatedData, callbackParams);
        handlePartOrFullAdmission(caseData, updatedData);

        if (containsFullDefence(caseData)) {
            updatedData.multiPartyResponseTypeFlags(MultiPartyResponseTypeFlags.FULL_DEFENCE);
        }

        if (containsAdmission(caseData, PART_ADMISSION, FULL_ADMISSION)) {
            updatedData.specFullDefenceOrPartAdmission1V1(YES);
            updatedData.specFullDefenceOrPartAdmission(YES);
        } else {
            updatedData.specFullDefenceOrPartAdmission(NO);
        }

        if (containsFullAdmission(caseData)) {
            updatedData.specDefenceFullAdmittedRequired(NO);
        }
    }

    private void handleOneVsTwoOneLegalRep(CaseData caseData, CaseData.CaseDataBuilder<?, ?> updatedData) {
        MultiPartyScenario multiPartyScenario = getMultiPartyScenario(caseData);

        if (ONE_V_TWO_ONE_LEGAL_REP.equals(multiPartyScenario)) {
            if (Objects.equals(caseData.getRespondent1ClaimResponseTypeForSpec(), caseData.getRespondent2ClaimResponseTypeForSpec())) {
                updatedData.respondentResponseIsSame(YES);
                caseData = caseData.toBuilder().respondentResponseIsSame(YES).build();
            }
            if (NO.equals(caseData.getRespondentResponseIsSame())) {
                updatedData.sameSolicitorSameResponse(NO);
                if (FULL_DEFENCE.equals(caseData.getRespondent1ClaimResponseTypeForSpec())
                    || FULL_DEFENCE.equals(caseData.getRespondent2ClaimResponseTypeForSpec())) {
                    updatedData.respondentClaimResponseTypeForSpecGeneric(FULL_DEFENCE);
                }
            } else if (YES.equals(caseData.getRespondentResponseIsSame())) {
                updatedData.sameSolicitorSameResponse(YES);
                updatedData.respondentClaimResponseTypeForSpecGeneric(caseData.getRespondent1ClaimResponseTypeForSpec());
                if (FULL_DEFENCE.equals(caseData.getRespondent1ClaimResponseTypeForSpec())
                    || COUNTER_CLAIM.equals(caseData.getRespondent1ClaimResponseTypeForSpec())) {
                    updatedData.multiPartyResponseTypeFlags(MultiPartyResponseTypeFlags.FULL_DEFENCE);
                }
            } else {
                updatedData.respondentClaimResponseTypeForSpecGeneric(caseData.getRespondent1ClaimResponseTypeForSpec());
            }
        }
    }

    private void handleOneVsTwoTwoLegalRep(CaseData caseData, CaseData.CaseDataBuilder<?, ?> updatedData, CallbackParams callbackParams) {
        MultiPartyScenario multiPartyScenario = getMultiPartyScenario(caseData);
        UserInfo userInfo = userService.getUserInfo(callbackParams.getParams().get(BEARER_TOKEN).toString());

        if (ONE_V_TWO_TWO_LEGAL_REP.equals(multiPartyScenario)) {
            if (coreCaseUserService.userHasCaseRole(caseData.getCcdCaseReference().toString(), userInfo.getUid(), RESPONDENTSOLICITORTWO)) {
                updatedData.respondentClaimResponseTypeForSpecGeneric(caseData.getRespondent2ClaimResponseTypeForSpec());
            } else {
                updatedData.respondentClaimResponseTypeForSpecGeneric(caseData.getRespondent1ClaimResponseTypeForSpec());
            }
        }

        if (ONE_V_TWO_TWO_LEGAL_REP.equals(multiPartyScenario) && isRespondentPartAdmission(caseData)) {
            updatedData.multiPartyResponseTypeFlags(MultiPartyResponseTypeFlags.PART_ADMISSION);
        }
    }

    private void handlePartOrFullAdmission(CaseData caseData, CaseData.CaseDataBuilder<?, ?> updatedData) {
        if ((YES.equals(caseData.getIsRespondent1()) && containsAdmission(caseData, PART_ADMISSION, FULL_ADMISSION))
            || (YES.equals(caseData.getIsRespondent2()) && containsAdmission(caseData, PART_ADMISSION, FULL_ADMISSION))) {
            updatedData.specFullAdmissionOrPartAdmission(YES);
        }

        if (containsAdmission(caseData, FULL_ADMISSION, PART_ADMISSION, COUNTER_CLAIM)) {
            updatedData.multiPartyResponseTypeFlags(MultiPartyResponseTypeFlags.COUNTER_ADMIT_OR_ADMIT_PART);
        }
    }

    private boolean containsAdmission(CaseData caseData, RespondentResponseTypeSpec... types) {
        for (RespondentResponseTypeSpec type : types) {
            if (type.equals(caseData.getRespondent1ClaimResponseTypeForSpec())
                || type.equals(caseData.getRespondent2ClaimResponseTypeForSpec())) {
                return true;
            }
        }
        return false;
    }

    private boolean containsFullDefence(CaseData caseData) {
        return FULL_DEFENCE.equals(caseData.getRespondent1ClaimResponseTypeForSpec())
            || FULL_DEFENCE.equals(caseData.getRespondent2ClaimResponseTypeForSpec())
            || FULL_DEFENCE.equals(caseData.getClaimant1ClaimResponseTypeForSpec())
            || FULL_DEFENCE.equals(caseData.getClaimant2ClaimResponseTypeForSpec());
    }

    private boolean containsFullAdmission(CaseData caseData) {
        return FULL_ADMISSION.equals(caseData.getRespondent1ClaimResponseTypeForSpec())
            || FULL_ADMISSION.equals(caseData.getRespondent2ClaimResponseTypeForSpec());
    }

    private boolean isRespondentPartAdmission(CaseData caseData) {
        return (YES.equals(caseData.getIsRespondent1()) && PART_ADMISSION.equals(caseData.getRespondent1ClaimResponseTypeForSpec()))
            || (YES.equals(caseData.getIsRespondent2()) && PART_ADMISSION.equals(caseData.getRespondent2ClaimResponseTypeForSpec()));
    }

    private void updateShowConditions(CaseData caseData, CaseData.CaseDataBuilder<?, ?> updatedData) {
        Set<DefendantResponseShowTag> updatedShowConditions = RespondToClaimSpecUtilsDisputeDetails.whoDisputesPartAdmission(caseData);
        EnumSet<RespondentResponseTypeSpec> anyAdmission = EnumSet.of(PART_ADMISSION, FULL_ADMISSION);

        if (updatedShowConditions.contains(CAN_ANSWER_RESPONDENT_1) && anyAdmission.contains(caseData.getRespondent1ClaimResponseTypeForSpec())) {
            updatedShowConditions.add(RESPONDENT_1_ADMITS_PART_OR_FULL);
            if (caseData.getRespondentResponseIsSame() == YES) {
                updatedShowConditions.add(RESPONDENT_2_ADMITS_PART_OR_FULL);
            }
        }

        if (caseData.getShowConditionFlags().contains(CAN_ANSWER_RESPONDENT_2) && anyAdmission.contains(caseData.getRespondent2ClaimResponseTypeForSpec())) {
            updatedShowConditions.add(RESPONDENT_2_ADMITS_PART_OR_FULL);
        }

        if (RespondToClaimSpecUtilsDisputeDetails.someoneDisputes(caseData)) {
            updatedShowConditions.add(SOMEONE_DISPUTES);
        }

        if ((anyAdmission.contains(caseData.getRespondent1ClaimResponseTypeForSpec()) && YES.equals(caseData.getIsRespondent1()))
            || (anyAdmission.contains(caseData.getRespondent2ClaimResponseTypeForSpec()) && YES.equals(caseData.getIsRespondent2()))) {
            updatedShowConditions.remove(CURRENT_ADMITS_PART_OR_FULL);
            updatedShowConditions.add(CURRENT_ADMITS_PART_OR_FULL);
        }

        updatedData.showConditionFlags(updatedShowConditions);
    }

    public CallbackResponse setUploadTimelineTypeFlag(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        CaseData.CaseDataBuilder<?, ?> updatedData = caseData.toBuilder();
        Set<DefendantResponseShowTag> updatedShowConditions = new HashSet<>(caseData.getShowConditionFlags());

        updatedShowConditions.removeAll(EnumSet.of(TIMELINE_UPLOAD, TIMELINE_MANUALLY));

        if ((YES.equals(caseData.getIsRespondent1()) && caseData.getSpecClaimResponseTimelineList() == TimelineUploadTypeSpec.UPLOAD)
            || (YES.equals(caseData.getIsRespondent2()) && caseData.getSpecClaimResponseTimelineList2() == TimelineUploadTypeSpec.UPLOAD)) {
            updatedShowConditions.add(TIMELINE_UPLOAD);
        } else if ((YES.equals(caseData.getIsRespondent1()) && caseData.getSpecClaimResponseTimelineList() == TimelineUploadTypeSpec.MANUAL)
            || (YES.equals(caseData.getIsRespondent2()) && caseData.getSpecClaimResponseTimelineList2() == TimelineUploadTypeSpec.MANUAL)) {
            updatedShowConditions.add(TIMELINE_MANUALLY);
        }

        updatedData.showConditionFlags(updatedShowConditions);

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(updatedData.build().toMap(objectMapper))
            .build();
    }
}
