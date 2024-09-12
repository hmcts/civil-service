package uk.gov.hmcts.reform.civil.handler.callback.user.respondtoclaimspeccallbackhandlertaskstests;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.enums.MultiPartyResponseTypeFlags;
import uk.gov.hmcts.reform.civil.enums.MultiPartyScenario;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec;
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
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.*;
import static uk.gov.hmcts.reform.civil.enums.RespondentResponsePartAdmissionPaymentTimeLRspec.IMMEDIATELY;
import static uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec.*;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.handler.callback.user.spec.show.DefendantResponseShowTag.*;

@Component
@RequiredArgsConstructor
public class SetGenericResponseTypeFlag implements CaseTask {

    private final ObjectMapper objectMapper;
    private final UserService userService;
    private final CoreCaseUserService coreCaseUserService;
    private final RespondToClaimSpecUtils respondToClaimSpecUtilsDisputeDetails;

    @Override
    public CallbackResponse execute(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        CaseData.CaseDataBuilder<?, ?> updatedData = initialiseCaseData(caseData);

        handleAdmissionResponseTypeFlags(caseData, updatedData);

        MultiPartyScenario multiPartyScenario = getMultiPartyScenario(caseData);

        handleOneVOneScenario(caseData, updatedData, multiPartyScenario);
        handleTwoVOneScenario(caseData, updatedData, multiPartyScenario);
        handleOneVTwoOneLegalRepScenario(caseData, updatedData, multiPartyScenario);
        handleOneVTwoTwoLegalRepScenario(callbackParams, caseData, updatedData, multiPartyScenario);

        handleRespondentResponses(caseData, updatedData);
        handleAdmissionPaymentRoutes(caseData, updatedData);
        handleDefendantResponseShowTags(caseData, updatedData);

        return buildCallbackResponse(updatedData);
    }

    private CaseData.CaseDataBuilder<?, ?> initialiseCaseData(CaseData caseData) {
        return caseData.toBuilder().multiPartyResponseTypeFlags(MultiPartyResponseTypeFlags.NOT_FULL_DEFENCE);
    }

    private void handleAdmissionResponseTypeFlags(CaseData caseData, CaseData.CaseDataBuilder<?, ?> updatedData) {
        if ((FULL_ADMISSION.equals(caseData.getClaimant1ClaimResponseTypeForSpec())
            || PART_ADMISSION.equals(caseData.getClaimant1ClaimResponseTypeForSpec())
            || RespondentResponseTypeSpec.COUNTER_CLAIM.equals(caseData.getClaimant1ClaimResponseTypeForSpec()))
            && (FULL_ADMISSION.equals(caseData.getClaimant2ClaimResponseTypeForSpec())
            || PART_ADMISSION.equals(caseData.getClaimant2ClaimResponseTypeForSpec())
            || RespondentResponseTypeSpec.COUNTER_CLAIM.equals(caseData.getClaimant2ClaimResponseTypeForSpec()))) {
            updatedData.multiPartyResponseTypeFlags(MultiPartyResponseTypeFlags.COUNTER_ADMIT_OR_ADMIT_PART);
        }
    }

    private void handleOneVOneScenario(CaseData caseData, CaseData.CaseDataBuilder<?, ?> updatedData,
                                       MultiPartyScenario multiPartyScenario) {
        if (ONE_V_ONE.equals(multiPartyScenario)) {
            updatedData.respondentClaimResponseTypeForSpecGeneric(caseData.getRespondent1ClaimResponseTypeForSpec());
            updateMultiPartyResponseTypeFlags(caseData, updatedData);
        }
    }

    private void updateMultiPartyResponseTypeFlags(CaseData caseData, CaseData.CaseDataBuilder<?, ?> updatedData) {
        RespondentResponseTypeSpec responseType = caseData.getRespondent1ClaimResponseTypeForSpec();
        if (responseType == FULL_DEFENCE) {
            setFullDefenceFlag(updatedData);
        } else if (responseType == RespondentResponseTypeSpec.COUNTER_CLAIM) {
            setCounterClaimFlag(updatedData);
        } else if (isFullAdmission(caseData)) {
            setFullAdmissionFlag(updatedData);
        }
    }

    private void setFullDefenceFlag(CaseData.CaseDataBuilder<?, ?> updatedData) {
        updatedData.multiPartyResponseTypeFlags(MultiPartyResponseTypeFlags.FULL_DEFENCE);
    }

    private void setCounterClaimFlag(CaseData.CaseDataBuilder<?, ?> updatedData) {
        updatedData.multiPartyResponseTypeFlags(MultiPartyResponseTypeFlags.COUNTER_ADMIT_OR_ADMIT_PART);
    }

    private boolean isFullAdmission(CaseData caseData) {
        return caseData.getRespondent1ClaimResponseTypeForSpec() == RespondentResponseTypeSpec.FULL_ADMISSION
            || caseData.getRespondent2ClaimResponseTypeForSpec() == RespondentResponseTypeSpec.FULL_ADMISSION;
    }

    private void setFullAdmissionFlag(CaseData.CaseDataBuilder<?, ?> updatedData) {
        updatedData.multiPartyResponseTypeFlags(MultiPartyResponseTypeFlags.FULL_ADMISSION);
    }

    private void handleTwoVOneScenario(CaseData caseData, CaseData.CaseDataBuilder<?, ?> updatedData,
                                       MultiPartyScenario multiPartyScenario) {
        Set<RespondentResponseTypeSpec> someAdmission = EnumSet.of(PART_ADMISSION, FULL_ADMISSION);
        if (TWO_V_ONE.equals(multiPartyScenario)
            && someAdmission.contains(caseData.getRespondent1ClaimResponseTypeForSpec())
            && someAdmission.contains(caseData.getRespondent2ClaimResponseTypeForSpec())) {
            updatedData.specFullAdmissionOrPartAdmission(YES);
        } else {
            updatedData.specFullAdmissionOrPartAdmission(NO);
        }
    }

    private void handleOneVTwoOneLegalRepScenario(CaseData caseData, CaseData.CaseDataBuilder<?, ?> updatedData,
                                                  MultiPartyScenario multiPartyScenario) {
        if (ONE_V_TWO_ONE_LEGAL_REP.equals(multiPartyScenario)) {
            if (Objects.equals(caseData.getRespondent1ClaimResponseTypeForSpec(),
                               caseData.getRespondent2ClaimResponseTypeForSpec())) {
                handleSameResponseForOneVTwoOneLegalRep(caseData, updatedData);
            } else {
                handleDifferentResponseForOneVTwoOneLegalRep(caseData, updatedData);
            }
        }
    }

    private void handleSameResponseForOneVTwoOneLegalRep(CaseData caseData, CaseData.CaseDataBuilder<?, ?> updatedData) {
        updatedData.respondentResponseIsSame(YES);
        caseData = caseData.toBuilder().respondentResponseIsSame(YES).build();
        updatedData.sameSolicitorSameResponse(YES);
        updatedData.respondentClaimResponseTypeForSpecGeneric(caseData.getRespondent1ClaimResponseTypeForSpec());
        if (FULL_DEFENCE.equals(caseData.getRespondent1ClaimResponseTypeForSpec())
            || RespondentResponseTypeSpec.COUNTER_CLAIM.equals(caseData.getRespondent1ClaimResponseTypeForSpec())) {
            updatedData.multiPartyResponseTypeFlags(MultiPartyResponseTypeFlags.FULL_DEFENCE);
        }
    }

    private void handleDifferentResponseForOneVTwoOneLegalRep(CaseData caseData, CaseData.CaseDataBuilder<?, ?> updatedData) {
        updatedData.respondentResponseIsSame(NO);
        updatedData.sameSolicitorSameResponse(NO);
        if (FULL_DEFENCE.equals(caseData.getRespondent1ClaimResponseTypeForSpec())
            || FULL_DEFENCE.equals(caseData.getRespondent2ClaimResponseTypeForSpec())) {
            updatedData.respondentClaimResponseTypeForSpecGeneric(FULL_DEFENCE);
        } else {
            updatedData.respondentClaimResponseTypeForSpecGeneric(caseData.getRespondent1ClaimResponseTypeForSpec());
        }
    }

    private void handleOneVTwoTwoLegalRepScenario(CallbackParams callbackParams, CaseData caseData,
                                                  CaseData.CaseDataBuilder<?, ?> updatedData,
                                                  MultiPartyScenario multiPartyScenario) {
        UserInfo userInfo = userService.getUserInfo(callbackParams.getParams().get(BEARER_TOKEN).toString());
        if (ONE_V_TWO_TWO_LEGAL_REP.equals(multiPartyScenario)) {
            handleLegalRepScenario(caseData, updatedData, userInfo);
        }

        if (isPartAdmissionScenario(caseData, multiPartyScenario)) {
            updatedData.multiPartyResponseTypeFlags(MultiPartyResponseTypeFlags.PART_ADMISSION);
        }

        if (YES.equals(caseData.getIsRespondent2())) {
            updatedData.respondentClaimResponseTypeForSpecGeneric(caseData.getRespondent2ClaimResponseTypeForSpec());
        }
    }

    private void handleLegalRepScenario(CaseData caseData,
                                        CaseData.CaseDataBuilder<?, ?> updatedData, UserInfo userInfo) {
        if (coreCaseUserService.userHasCaseRole(
            caseData.getCcdCaseReference().toString(),
            userInfo.getUid(),
            RESPONDENTSOLICITORTWO)) {
            updatedData.respondentClaimResponseTypeForSpecGeneric(caseData.getRespondent2ClaimResponseTypeForSpec());
        } else {
            updatedData.respondentClaimResponseTypeForSpecGeneric(caseData.getRespondent1ClaimResponseTypeForSpec());
        }
    }

    private boolean isPartAdmissionScenario(CaseData caseData, MultiPartyScenario multiPartyScenario) {
        return ONE_V_TWO_TWO_LEGAL_REP.equals(multiPartyScenario)
            && ((YES.equals(caseData.getIsRespondent1())
            && RespondentResponseTypeSpec.PART_ADMISSION.equals(caseData.getRespondent1ClaimResponseTypeForSpec()))
            || (YES.equals(caseData.getIsRespondent2())
            && RespondentResponseTypeSpec.PART_ADMISSION.equals(caseData.getRespondent2ClaimResponseTypeForSpec())));
    }

    private void handleRespondentResponses(CaseData caseData, CaseData.CaseDataBuilder<?, ?> updatedData) {
        if (isFullDefenceResponse(caseData)) {
            setFullDefenceFlag(updatedData);
        }

        if (isSpecFullAdmissionOrPartAdmission(caseData)) {
            updatedData.specFullAdmissionOrPartAdmission(YES);
        }

        if (isCounterAdmitOrAdmitPart(caseData)) {
            updatedData.multiPartyResponseTypeFlags(MultiPartyResponseTypeFlags.COUNTER_ADMIT_OR_ADMIT_PART);
        }
    }

    private boolean isFullDefenceResponse(CaseData caseData) {
        return isRespondent1FullDefence(caseData)
            || isRespondent2FullDefence(caseData)
            || isClaimant1FullDefence(caseData)
            || isClaimant2FullDefence(caseData);
    }

    private boolean isRespondent1FullDefence(CaseData caseData) {
        return caseData.getRespondent1ClaimResponseTypeForSpec() == FULL_DEFENCE;
    }

    private boolean isRespondent2FullDefence(CaseData caseData) {
        return caseData.getRespondent2ClaimResponseTypeForSpec() == FULL_DEFENCE;
    }

    private boolean isClaimant1FullDefence(CaseData caseData) {
        return caseData.getClaimant1ClaimResponseTypeForSpec() == FULL_DEFENCE;
    }

    private boolean isClaimant2FullDefence(CaseData caseData) {
        return caseData.getClaimant2ClaimResponseTypeForSpec() == FULL_DEFENCE;
    }

    private boolean isSpecFullAdmissionOrPartAdmission(CaseData caseData) {
        return isRespondent1FullOrPartAdmission(caseData) || isRespondent2FullOrPartAdmission(caseData);
    }

    private boolean isRespondent1FullOrPartAdmission(CaseData caseData) {
        return YES.equals(caseData.getIsRespondent1())
            && (caseData.getRespondent1ClaimResponseTypeForSpec() == RespondentResponseTypeSpec.PART_ADMISSION
            || caseData.getRespondent1ClaimResponseTypeForSpec() == RespondentResponseTypeSpec.FULL_ADMISSION);
    }

    private boolean isRespondent2FullOrPartAdmission(CaseData caseData) {
        return YES.equals(caseData.getIsRespondent2())
            && (caseData.getRespondent2ClaimResponseTypeForSpec() == RespondentResponseTypeSpec.PART_ADMISSION
            || caseData.getRespondent2ClaimResponseTypeForSpec() == RespondentResponseTypeSpec.FULL_ADMISSION);
    }

    private boolean isCounterAdmitOrAdmitPart(CaseData caseData) {
        return RespondentResponseTypeSpec.FULL_ADMISSION.equals(caseData.getRespondent2ClaimResponseTypeForSpec())
            || RespondentResponseTypeSpec.PART_ADMISSION.equals(caseData.getRespondent2ClaimResponseTypeForSpec())
            || RespondentResponseTypeSpec.COUNTER_CLAIM.equals(caseData.getRespondent2ClaimResponseTypeForSpec());
    }

    private void handleAdmissionPaymentRoutes(CaseData caseData, CaseData.CaseDataBuilder<?, ?> updatedData) {
        if (YES.equals(caseData.getIsRespondent1()) && caseData.getDefenceAdmitPartPaymentTimeRouteRequired() != null) {
            updatedData.defenceAdmitPartPaymentTimeRouteGeneric(
                caseData.getDefenceAdmitPartPaymentTimeRouteRequired());
        } else if (YES.equals(caseData.getIsRespondent2())
            && caseData.getDefenceAdmitPartPaymentTimeRouteRequired2() != null) {
            updatedData.defenceAdmitPartPaymentTimeRouteGeneric(
                caseData.getDefenceAdmitPartPaymentTimeRouteRequired2());
        } else {
            updatedData.defenceAdmitPartPaymentTimeRouteGeneric(IMMEDIATELY);
        }
    }

    private void handleDefendantResponseShowTags(CaseData caseData, CaseData.CaseDataBuilder<?, ?> updatedData) {
        Set<DefendantResponseShowTag> updatedShowConditions = whoDisputesPartAdmission(caseData);
        EnumSet<RespondentResponseTypeSpec> anyAdmission = EnumSet.of(
            RespondentResponseTypeSpec.PART_ADMISSION,
            RespondentResponseTypeSpec.FULL_ADMISSION
        );

        addRespondent1AdmissionTags(caseData, updatedShowConditions, anyAdmission);
        addRespondent2AdmissionTags(caseData, updatedShowConditions, anyAdmission);
        addDisputeTags(caseData, updatedShowConditions);
        addCurrentAdmissionTags(caseData, updatedShowConditions, anyAdmission);

        updatedData.showConditionFlags(updatedShowConditions);
    }

    private void addRespondent1AdmissionTags(CaseData caseData, Set<DefendantResponseShowTag> updatedShowConditions,
                                             EnumSet<RespondentResponseTypeSpec> anyAdmission) {
        if (updatedShowConditions.contains(CAN_ANSWER_RESPONDENT_1)
            && anyAdmission.contains(caseData.getRespondent1ClaimResponseTypeForSpec())) {
            updatedShowConditions.add(RESPONDENT_1_ADMITS_PART_OR_FULL);
            if (caseData.getRespondentResponseIsSame() == YES) {
                updatedShowConditions.add(RESPONDENT_2_ADMITS_PART_OR_FULL);
            }
        }
    }

    private void addRespondent2AdmissionTags(CaseData caseData, Set<DefendantResponseShowTag> updatedShowConditions,
                                             EnumSet<RespondentResponseTypeSpec> anyAdmission) {
        if (caseData.getShowConditionFlags().contains(CAN_ANSWER_RESPONDENT_2)
            && anyAdmission.contains(caseData.getRespondent2ClaimResponseTypeForSpec())) {
            updatedShowConditions.add(RESPONDENT_2_ADMITS_PART_OR_FULL);
        }
    }

    private void addDisputeTags(CaseData caseData, Set<DefendantResponseShowTag> updatedShowConditions) {
        if (respondToClaimSpecUtilsDisputeDetails.someoneDisputes(caseData)) {
            updatedShowConditions.add(SOMEONE_DISPUTES);
        }
    }

    private void addCurrentAdmissionTags(CaseData caseData, Set<DefendantResponseShowTag> updatedShowConditions,
                                         EnumSet<RespondentResponseTypeSpec> anyAdmission) {
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
        respondToClaimSpecUtilsDisputeDetails.removeWhoDisputesAndWhoPaidLess(tags);
        tags.addAll(respondToClaimSpecUtilsDisputeDetails.whoDisputesBcoPartAdmission(caseData));
        return tags;
    }

    private CallbackResponse buildCallbackResponse(CaseData.CaseDataBuilder<?, ?> updatedData) {
        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(updatedData.build().toMap(objectMapper))
            .build();
    }
}
