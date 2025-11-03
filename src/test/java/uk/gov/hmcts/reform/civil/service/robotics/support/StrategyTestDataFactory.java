package uk.gov.hmcts.reform.civil.service.robotics.support;

import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.sampledata.PartyBuilder;

public final class StrategyTestDataFactory {

    private StrategyTestDataFactory() {
        // utility
    }

    public static CaseDataBuilder unspecTwoDefendantSolicitorsCase() {
        CaseDataBuilder builder = CaseDataBuilder.builder();
        builder.respondent1DQ();
        builder.respondent2DQ();
        builder.multiPartyClaimTwoDefendantSolicitors();
        builder.respondent1(PartyBuilder.builder().individual().build());
        builder.respondent2(PartyBuilder.builder().individual().build());
        return builder;
    }

    public static CaseDataBuilder specTwoDefendantSolicitorsCase() {
        CaseDataBuilder builder = CaseDataBuilder.builder();
        builder.respondent1DQ();
        builder.respondent2DQ();
        builder.setClaimTypeToSpecClaim();
        builder.multiPartyClaimTwoDefendantSolicitorsSpec();
        builder.respondent1(PartyBuilder.builder().individual().build());
        builder.respondent2(PartyBuilder.builder().individual().build());
        return builder;
    }

    public static CaseData defaultJudgment1v1Case() {
        return CaseDataBuilder.builder().getDefaultJudgment1v1Case();
    }

    public static CaseData defaultJudgment1v2Case() {
        return CaseDataBuilder.builder().getDefaultJudgment1v2DivergentCase();
    }

    public static CaseData.CaseDataBuilder<?, ?> defaultJudgment1v1Builder() {
        return defaultJudgment1v1Case().toBuilder();
    }

    public static CaseData.CaseDataBuilder<?, ?> defaultJudgment1v2Builder() {
        return defaultJudgment1v2Case().toBuilder();
    }
}
