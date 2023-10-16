package uk.gov.hmcts.reform.civil.service.citizenui;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.handler.callback.user.spec.show.ResponseOneVOneShowTag;
import uk.gov.hmcts.reform.civil.model.CaseData;

import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.ONE_V_ONE;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.getMultiPartyScenario;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;

@Service
@RequiredArgsConstructor
public class ResponseOneVOneShowTagService {
    public ResponseOneVOneShowTag setUpOneVOneFlow(CaseData caseData) {
        if (ONE_V_ONE.equals(getMultiPartyScenario(caseData))) {
            if (caseData.getRespondent1ClaimResponseTypeForSpec() == null) {
                return null;
            }
            return switch (caseData.getRespondent1ClaimResponseTypeForSpec()) {
                case FULL_DEFENCE -> ResponseOneVOneShowTag.ONE_V_ONE_FULL_DEFENCE;
                case FULL_ADMISSION -> setUpOneVOneFlowForFullAdmit(caseData);
                case PART_ADMISSION -> setUpOneVOneFlowForPartAdmit(caseData);
                case COUNTER_CLAIM -> ResponseOneVOneShowTag.ONE_V_ONE_COUNTER_CLAIM;
                default -> null;
            };
        }
        return null;
    }

    private ResponseOneVOneShowTag setUpOneVOneFlowForPartAdmit(CaseData caseData) {
        if (YES.equals(caseData.getSpecDefenceAdmittedRequired())) {
            return ResponseOneVOneShowTag.ONE_V_ONE_PART_ADMIT_HAS_PAID;
        }
        return switch (caseData.getDefenceAdmitPartPaymentTimeRouteRequired()) {
            case IMMEDIATELY -> ResponseOneVOneShowTag.ONE_V_ONE_PART_ADMIT_PAY_IMMEDIATELY;
            case BY_SET_DATE -> ResponseOneVOneShowTag.ONE_V_ONE_PART_ADMIT_PAY_BY_SET_DATE;
            case SUGGESTION_OF_REPAYMENT_PLAN -> ResponseOneVOneShowTag.ONE_V_ONE_PART_ADMIT_PAY_INSTALMENT;
            default -> ResponseOneVOneShowTag.ONE_V_ONE_PART_ADMIT;
        };
    }

    private ResponseOneVOneShowTag setUpOneVOneFlowForFullAdmit(CaseData caseData) {
        if (YES.equals(caseData.getSpecDefenceFullAdmittedRequired())) {
            return ResponseOneVOneShowTag.ONE_V_ONE_FULL_ADMIT_HAS_PAID;
        }
        return switch (caseData.getDefenceAdmitPartPaymentTimeRouteRequired()) {
            case IMMEDIATELY -> ResponseOneVOneShowTag.ONE_V_ONE_FULL_ADMIT_PAY_IMMEDIATELY;
            case BY_SET_DATE -> ResponseOneVOneShowTag.ONE_V_ONE_FULL_ADMIT_PAY_BY_SET_DATE;
            case SUGGESTION_OF_REPAYMENT_PLAN -> ResponseOneVOneShowTag.ONE_V_ONE_FULL_ADMIT_PAY_INSTALMENT;
            default -> ResponseOneVOneShowTag.ONE_V_ONE_FULL_ADMIT;
        };
    }
}
