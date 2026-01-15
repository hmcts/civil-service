package uk.gov.hmcts.reform.civil.service.robotics.support;

import uk.gov.hmcts.reform.civil.model.Address;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;

public final class StrategyTestDataFactory {

    private StrategyTestDataFactory() {
        // utility
    }

    public static CaseDataBuilder unspecTwoDefendantSolicitorsCase() {
        CaseDataBuilder builder = CaseDataBuilder.builder();
        builder.respondent1DQ();
        builder.respondent2DQ();
        builder.multiPartyClaimTwoDefendantSolicitors();
        builder.respondent1(individualRespondent("One"));
        builder.respondent2(individualRespondent("Two"));
        return builder;
    }

    public static CaseDataBuilder specTwoDefendantSolicitorsCase() {
        CaseDataBuilder builder = CaseDataBuilder.builder();
        builder.respondent1DQ();
        builder.respondent2DQ();
        builder.setClaimTypeToSpecClaim();
        builder.multiPartyClaimTwoDefendantSolicitorsSpec();
        builder.respondent1(individualRespondent("One"));
        builder.respondent2(individualRespondent("Two"));
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

    private static Party individualRespondent(String lastName) {
        Party party = new Party();
        party.setType(Party.Type.INDIVIDUAL);
        party.setIndividualTitle("Mr.");
        party.setIndividualFirstName("Respondent");
        party.setIndividualLastName(lastName);
        party.setPartyName("Respondent" + " " + lastName);
        Address address = new Address();
        address.setAddressLine1("1 Example Street");
        address.setPostCode("EX1 1EX");
        party.setPrimaryAddress(address);
        return party;
    }
}
