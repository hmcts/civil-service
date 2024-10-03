package uk.gov.hmcts.reform.civil.handler.callback.user.respondtoclaimspeccallbackhandlertaskstests;

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
    private final RespondToClaimSpecUtils respondToClaimSpecUtilsDisputeDetails;

    public CallbackResponse execute(CallbackParams callbackParams) {
        log.info("Executing SetGenericResponseTypeFlag task");
        log.debug("Received CallbackParams: {}", callbackParams);

        CaseData caseData = callbackParams.getCaseData();
        log.debug("Initial CaseData: {}", caseData);

        CaseData.CaseDataBuilder<?, ?> updatedData = initialiseCaseData(caseData);
        log.debug("Initialised updatedData with multiPartyResponseTypeFlags set to NOT_FULL_DEFENCE");

        handleAdmissionResponseTypeFlags(caseData, updatedData);
        log.debug("Handled admission response type flags");

        MultiPartyScenario multiPartyScenario = getMultiPartyScenario(caseData);
        log.info("Determined MultiPartyScenario: {}", multiPartyScenario);

        handleOneVOneScenario(caseData, updatedData, multiPartyScenario);
        log.debug("Handled One V One scenario");

        handleTwoVOneScenario(caseData, updatedData, multiPartyScenario);
        log.debug("Handled Two V One scenario");

        handleOneVTwoOneLegalRepScenario(caseData, updatedData, multiPartyScenario);
        log.debug("Handled One V Two One Legal Representative scenario");

        handleOneVTwoTwoLegalRepScenario(callbackParams, caseData, updatedData, multiPartyScenario);
        log.debug("Handled One V Two Two Legal Representative scenario");

        handleRespondentResponses(caseData, updatedData);
        log.debug("Handled respondent responses");

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

        handleAdmissionPaymentRoutes(caseData, updatedData);
        log.debug("Handled admission payment routes");

        handleDefendantResponseShowTags(caseData, updatedData);
        log.debug("Handled defendant response show tags");

        CallbackResponse response = buildCallbackResponse(updatedData);
        log.info("Built CallbackResponse");

        log.debug("Final updatedData: {}", updatedData.build());
        return response;
    }

    private CaseData.CaseDataBuilder<?, ?> initialiseCaseData(CaseData caseData) {
        log.debug("Initialising CaseData builder with multiPartyResponseTypeFlags set to NOT_FULL_DEFENCE");
        return caseData.toBuilder().multiPartyResponseTypeFlags(MultiPartyResponseTypeFlags.NOT_FULL_DEFENCE);
    }

    private void handleAdmissionResponseTypeFlags(CaseData caseData, CaseData.CaseDataBuilder<?, ?> updatedData) {
        log.debug("Checking admission response types for Claimant1 and Claimant2");
        if ((FULL_ADMISSION.equals(caseData.getClaimant1ClaimResponseTypeForSpec())
            || PART_ADMISSION.equals(caseData.getClaimant1ClaimResponseTypeForSpec())
            || RespondentResponseTypeSpec.COUNTER_CLAIM.equals(caseData.getClaimant1ClaimResponseTypeForSpec()))
            && (FULL_ADMISSION.equals(caseData.getClaimant2ClaimResponseTypeForSpec())
            || PART_ADMISSION.equals(caseData.getClaimant2ClaimResponseTypeForSpec())
            || RespondentResponseTypeSpec.COUNTER_CLAIM.equals(caseData.getClaimant2ClaimResponseTypeForSpec()))) {
            updatedData.multiPartyResponseTypeFlags(MultiPartyResponseTypeFlags.COUNTER_ADMIT_OR_ADMIT_PART);
            log.debug("Set multiPartyResponseTypeFlags to COUNTER_ADMIT_OR_ADMIT_PART");
        }
    }

    private void handleOneVOneScenario(CaseData caseData, CaseData.CaseDataBuilder<?, ?> updatedData,
                                       MultiPartyScenario multiPartyScenario) {
        if (ONE_V_ONE.equals(multiPartyScenario)) {
            log.debug("Handling One V One scenario");
            updatedData.respondentClaimResponseTypeForSpecGeneric(caseData.getRespondent1ClaimResponseTypeForSpec());
            log.debug("Set respondentClaimResponseTypeForSpecGeneric to {}", caseData.getRespondent1ClaimResponseTypeForSpec());
            updateMultiPartyResponseTypeFlags(caseData, updatedData);
            log.debug("Updated multiPartyResponseTypeFlags based on respondent1 response type");
        }
    }

    private void updateMultiPartyResponseTypeFlags(CaseData caseData, CaseData.CaseDataBuilder<?, ?> updatedData) {
        RespondentResponseTypeSpec responseType = caseData.getRespondent1ClaimResponseTypeForSpec();
        log.debug("Updating multiPartyResponseTypeFlags based on Respondent1ResponseTypeSpec: {}", responseType);
        if (responseType == FULL_DEFENCE) {
            setFullDefenceFlag(updatedData);
            log.debug("Set multiPartyResponseTypeFlags to FULL_DEFENCE");
        } else if (responseType == RespondentResponseTypeSpec.COUNTER_CLAIM) {
            setCounterClaimFlag(updatedData);
            log.debug("Set multiPartyResponseTypeFlags to COUNTER_ADMIT_OR_ADMIT_PART");
        } else if (isFullAdmission(caseData)) {
            setFullAdmissionFlag(updatedData);
            log.debug("Set multiPartyResponseTypeFlags to FULL_ADMISSION");
        }
    }

    private void setFullDefenceFlag(CaseData.CaseDataBuilder<?, ?> updatedData) {
        updatedData.multiPartyResponseTypeFlags(MultiPartyResponseTypeFlags.FULL_DEFENCE);
    }

    private void setCounterClaimFlag(CaseData.CaseDataBuilder<?, ?> updatedData) {
        updatedData.multiPartyResponseTypeFlags(MultiPartyResponseTypeFlags.COUNTER_ADMIT_OR_ADMIT_PART);
    }

    private boolean isFullAdmission(CaseData caseData) {
        boolean result = caseData.getRespondent1ClaimResponseTypeForSpec() == RespondentResponseTypeSpec.FULL_ADMISSION
            || caseData.getRespondent2ClaimResponseTypeForSpec() == RespondentResponseTypeSpec.FULL_ADMISSION;
        log.debug("isFullAdmission check result: {}", result);
        return result;
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
            log.debug("Two V One scenario with both respondents admitting fully or partially. Set specFullAdmissionOrPartAdmission to YES");
        } else {
            updatedData.specFullAdmissionOrPartAdmission(NO);
            log.debug("Two V One scenario without both respondents admitting fully or partially. Set specFullAdmissionOrPartAdmission to NO");
        }
    }

    private void handleOneVTwoOneLegalRepScenario(CaseData caseData, CaseData.CaseDataBuilder<?, ?> updatedData,
                                                  MultiPartyScenario multiPartyScenario) {
        if (ONE_V_TWO_ONE_LEGAL_REP.equals(multiPartyScenario)) {
            log.debug("Handling One V Two One Legal Representative scenario");
            if (Objects.equals(caseData.getRespondent1ClaimResponseTypeForSpec(),
                               caseData.getRespondent2ClaimResponseTypeForSpec())) {
                handleSameResponseForOneVTwoOneLegalRep(caseData, updatedData);
                log.debug("Both respondents have the same response type");
            } else {
                handleDifferentResponseForOneVTwoOneLegalRep(caseData, updatedData);
                log.debug("Respondents have different response types");
            }
        }
    }

    private void handleSameResponseForOneVTwoOneLegalRep(CaseData caseData, CaseData.CaseDataBuilder<?, ?> updatedData) {
        updatedData.respondentResponseIsSame(YES);
        log.debug("Set respondentResponseIsSame to YES");
        caseData = caseData.toBuilder().respondentResponseIsSame(YES).build();
        updatedData.sameSolicitorSameResponse(YES);
        log.debug("Set sameSolicitorSameResponse to YES");
        updatedData.respondentClaimResponseTypeForSpecGeneric(caseData.getRespondent1ClaimResponseTypeForSpec());
        log.debug("Set respondentClaimResponseTypeForSpecGeneric to {}", caseData.getRespondent1ClaimResponseTypeForSpec());
        if (FULL_DEFENCE.equals(caseData.getRespondent1ClaimResponseTypeForSpec())
            || RespondentResponseTypeSpec.COUNTER_CLAIM.equals(caseData.getRespondent1ClaimResponseTypeForSpec())) {
            updatedData.multiPartyResponseTypeFlags(MultiPartyResponseTypeFlags.FULL_DEFENCE);
            log.debug("Set multiPartyResponseTypeFlags to FULL_DEFENCE based on Respondent1 response");
        }
    }

    private void handleDifferentResponseForOneVTwoOneLegalRep(CaseData caseData, CaseData.CaseDataBuilder<?, ?> updatedData) {
        updatedData.respondentResponseIsSame(NO);
        log.debug("Set respondentResponseIsSame to NO");
        updatedData.sameSolicitorSameResponse(NO);
        log.debug("Set sameSolicitorSameResponse to NO");
        if (FULL_DEFENCE.equals(caseData.getRespondent1ClaimResponseTypeForSpec())
            || FULL_DEFENCE.equals(caseData.getRespondent2ClaimResponseTypeForSpec())) {
            updatedData.respondentClaimResponseTypeForSpecGeneric(FULL_DEFENCE);
            log.debug("At least one respondent has FULL_DEFENCE. Set respondentClaimResponseTypeForSpecGeneric to FULL_DEFENCE");
        } else {
            updatedData.respondentClaimResponseTypeForSpecGeneric(caseData.getRespondent1ClaimResponseTypeForSpec());
            log.debug("Set respondentClaimResponseTypeForSpecGeneric to Respondent1's response type: {}", caseData.getRespondent1ClaimResponseTypeForSpec());
        }
    }

    private void handleOneVTwoTwoLegalRepScenario(CallbackParams callbackParams, CaseData caseData,
                                                  CaseData.CaseDataBuilder<?, ?> updatedData,
                                                  MultiPartyScenario multiPartyScenario) {
        UserInfo userInfo = userService.getUserInfo(callbackParams.getParams().get(BEARER_TOKEN).toString());
        log.debug("Retrieved UserInfo: {}", userInfo);

        if (ONE_V_TWO_TWO_LEGAL_REP.equals(multiPartyScenario)) {
            log.debug("Handling One V Two Two Legal Representative scenario");
            handleLegalRepScenario(caseData, updatedData, userInfo);
        }

        if (isPartAdmissionScenario(caseData, multiPartyScenario)) {
            updatedData.multiPartyResponseTypeFlags(MultiPartyResponseTypeFlags.PART_ADMISSION);
            log.debug("Set multiPartyResponseTypeFlags to PART_ADMISSION based on part admission scenario");
        }

        if (YES.equals(caseData.getIsRespondent2())) {
            updatedData.respondentClaimResponseTypeForSpecGeneric(caseData.getRespondent2ClaimResponseTypeForSpec());
            log.debug("Respondent2 is present. Set respondentClaimResponseTypeForSpecGeneric to Respondent2's response type: {}",
                      caseData.getRespondent2ClaimResponseTypeForSpec());
        }
    }

    private void handleLegalRepScenario(CaseData caseData,
                                        CaseData.CaseDataBuilder<?, ?> updatedData, UserInfo userInfo) {
        if (coreCaseUserService.userHasCaseRole(
            caseData.getCcdCaseReference().toString(),
            userInfo.getUid(),
            RESPONDENTSOLICITORTWO)) {
            updatedData.respondentClaimResponseTypeForSpecGeneric(caseData.getRespondent2ClaimResponseTypeForSpec());
            log.debug("User has RESPONDENTSOLICITORTWO role. Set respondentClaimResponseTypeForSpecGeneric to Respondent2's response type: {}",
                      caseData.getRespondent2ClaimResponseTypeForSpec());
        } else {
            updatedData.respondentClaimResponseTypeForSpecGeneric(caseData.getRespondent1ClaimResponseTypeForSpec());
            log.debug("User does not have RESPONDENTSOLICITORTWO role. Set respondentClaimResponseTypeForSpecGeneric to Respondent1's response type: {}",
                      caseData.getRespondent1ClaimResponseTypeForSpec());
        }
    }

    private boolean isPartAdmissionScenario(CaseData caseData, MultiPartyScenario multiPartyScenario) {
        boolean result = ONE_V_TWO_TWO_LEGAL_REP.equals(multiPartyScenario)
            && ((YES.equals(caseData.getIsRespondent1())
            && RespondentResponseTypeSpec.PART_ADMISSION.equals(caseData.getRespondent1ClaimResponseTypeForSpec()))
            || (YES.equals(caseData.getIsRespondent2())
            && RespondentResponseTypeSpec.PART_ADMISSION.equals(caseData.getRespondent2ClaimResponseTypeForSpec())));
        log.debug("isPartAdmissionScenario check result: {}", result);
        return result;
    }

    private void handleRespondentResponses(CaseData caseData, CaseData.CaseDataBuilder<?, ?> updatedData) {
        if (isFullDefenceResponse(caseData)) {
            setFullDefenceFlag(updatedData);
            log.debug("Respondent has FULL_DEFENCE. Set multiPartyResponseTypeFlags to FULL_DEFENCE");
        }

        if (isSpecFullAdmissionOrPartAdmission(caseData)) {
            updatedData.specFullAdmissionOrPartAdmission(YES);
            log.debug("Set specFullAdmissionOrPartAdmission to YES based on full or part admission");
        }

        if (isCounterAdmitOrAdmitPart(caseData)) {
            updatedData.multiPartyResponseTypeFlags(MultiPartyResponseTypeFlags.COUNTER_ADMIT_OR_ADMIT_PART);
            log.debug("Set multiPartyResponseTypeFlags to COUNTER_ADMIT_OR_ADMIT_PART based on counter admit or admit part");
        }
    }

    private boolean isFullDefenceResponse(CaseData caseData) {
        boolean result = isRespondent1FullDefence(caseData)
            || isRespondent2FullDefence(caseData)
            || isClaimant1FullDefence(caseData)
            || isClaimant2FullDefence(caseData);
        log.debug("isFullDefenceResponse check result: {}", result);
        return result;
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
        boolean result = isRespondent1FullOrPartAdmission(caseData) || isRespondent2FullOrPartAdmission(caseData);
        log.debug("isSpecFullAdmissionOrPartAdmission check result: {}", result);
        return result;
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
        boolean result = RespondentResponseTypeSpec.FULL_ADMISSION.equals(caseData.getRespondent2ClaimResponseTypeForSpec())
            || RespondentResponseTypeSpec.PART_ADMISSION.equals(caseData.getRespondent2ClaimResponseTypeForSpec())
            || RespondentResponseTypeSpec.COUNTER_CLAIM.equals(caseData.getRespondent2ClaimResponseTypeForSpec());
        log.debug("isCounterAdmitOrAdmitPart check result: {}", result);
        return result;
    }

    private void handleAdmissionPaymentRoutes(CaseData caseData, CaseData.CaseDataBuilder<?, ?> updatedData) {
        if (YES.equals(caseData.getIsRespondent1()) && caseData.getDefenceAdmitPartPaymentTimeRouteRequired() != null) {
            updatedData.defenceAdmitPartPaymentTimeRouteGeneric(
                caseData.getDefenceAdmitPartPaymentTimeRouteRequired());
            log.debug("Respondent1 is present and has Defence Admit Part Payment Time Route Required. Set defenceAdmitPartPaymentTimeRouteGeneric accordingly");
        } else if (YES.equals(caseData.getIsRespondent2())
            && caseData.getDefenceAdmitPartPaymentTimeRouteRequired2() != null) {
            updatedData.defenceAdmitPartPaymentTimeRouteGeneric(
                caseData.getDefenceAdmitPartPaymentTimeRouteRequired2());
            log.debug("Respondent2 is present and has Defence Admit Part Payment Time Route Required. Set defenceAdmitPartPaymentTimeRouteGeneric accordingly");
        } else {
            updatedData.defenceAdmitPartPaymentTimeRouteGeneric(IMMEDIATELY);
            log.debug("No specific Defence Admit Part Payment Time Route Required. Set defenceAdmitPartPaymentTimeRouteGeneric to IMMEDIATELY");
        }
    }

    private void handleDefendantResponseShowTags(CaseData caseData, CaseData.CaseDataBuilder<?, ?> updatedData) {
        log.debug("Handling defendant response show tags");
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
        log.debug("Updated showConditionFlags: {}", updatedShowConditions);
    }

    private void addRespondent1AdmissionTags(CaseData caseData, Set<DefendantResponseShowTag> updatedShowConditions,
                                             EnumSet<RespondentResponseTypeSpec> anyAdmission) {
        if (updatedShowConditions.contains(CAN_ANSWER_RESPONDENT_1)
            && anyAdmission.contains(caseData.getRespondent1ClaimResponseTypeForSpec())) {
            updatedShowConditions.add(RESPONDENT_1_ADMITS_PART_OR_FULL);
            log.debug("Added RESPONDENT_1_ADMITS_PART_OR_FULL to showConditionFlags");
            if (caseData.getRespondentResponseIsSame() == YES) {
                updatedShowConditions.add(RESPONDENT_2_ADMITS_PART_OR_FULL);
                log.debug("Respondent responses are same. Added RESPONDENT_2_ADMITS_PART_OR_FULL to showConditionFlags");
            }
        }
    }

    private void addRespondent2AdmissionTags(CaseData caseData, Set<DefendantResponseShowTag> updatedShowConditions,
                                             EnumSet<RespondentResponseTypeSpec> anyAdmission) {
        if (caseData.getShowConditionFlags().contains(CAN_ANSWER_RESPONDENT_2)
            && anyAdmission.contains(caseData.getRespondent2ClaimResponseTypeForSpec())) {
            updatedShowConditions.add(RESPONDENT_2_ADMITS_PART_OR_FULL);
            log.debug("Added RESPONDENT_2_ADMITS_PART_OR_FULL to showConditionFlags");
        }
    }

    private void addDisputeTags(CaseData caseData, Set<DefendantResponseShowTag> updatedShowConditions) {
        if (respondToClaimSpecUtilsDisputeDetails.isSomeoneDisputes(caseData)) {
            updatedShowConditions.add(SOMEONE_DISPUTES);
            log.debug("Added SOMEONE_DISPUTES to showConditionFlags");
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
            log.debug("Added CURRENT_ADMITS_PART_OR_FULL to showConditionFlags");
        }
    }

    private Set<DefendantResponseShowTag> whoDisputesPartAdmission(CaseData caseData) {
        Set<DefendantResponseShowTag> tags = new HashSet<>(caseData.getShowConditionFlags());
        log.debug("Initial showConditionFlags: {}", tags);
        respondToClaimSpecUtilsDisputeDetails.removeWhoDisputesAndWhoPaidLess(tags);
        log.debug("Removed whoDisputes and whoPaidLess from showConditionFlags");
        tags.addAll(respondToClaimSpecUtilsDisputeDetails.whoDisputesBcoPartAdmission(caseData));
        log.debug("Added whoDisputesBcoPartAdmission tags to showConditionFlags: {}", tags);
        return tags;
    }

    private CallbackResponse buildCallbackResponse(CaseData.CaseDataBuilder<?, ?> updatedData) {
        log.debug("Building CallbackResponse with updated data");
        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(updatedData.build().toMap(objectMapper))
            .build();
    }
}
