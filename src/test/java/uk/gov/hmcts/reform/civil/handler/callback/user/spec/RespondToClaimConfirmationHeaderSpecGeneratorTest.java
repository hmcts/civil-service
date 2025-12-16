package uk.gov.hmcts.reform.civil.handler.callback.user.spec;

import org.apache.commons.lang3.tuple.Pair;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.handler.callback.user.spec.response.confirmation.header.CounterClaimConfirmationHeaderText;
import uk.gov.hmcts.reform.civil.handler.callback.user.spec.response.confirmation.header.SpecResponse1v2DivergentHeaderText;
import uk.gov.hmcts.reform.civil.handler.callback.user.spec.response.confirmation.header.SpecResponse2v1DifferentHeaderText;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;

import java.util.ArrayList;
import java.util.List;

public class RespondToClaimConfirmationHeaderSpecGeneratorTest
    implements CaseDataToTextGeneratorTest.CaseDataToTextGeneratorIntentionConfig
    <RespondToClaimConfirmationHeaderSpecGenerator> {

    @Override
    public Class<RespondToClaimConfirmationHeaderSpecGenerator> getIntentionInterface() {
        return RespondToClaimConfirmationHeaderSpecGenerator.class;
    }

    @Override
    public List<Pair<CaseData,
        Class<? extends RespondToClaimConfirmationHeaderSpecGenerator>>> getCasesToExpectedImplementation() {
        List<Pair<CaseData, Class<? extends RespondToClaimConfirmationHeaderSpecGenerator>>> list = new ArrayList<>(
            List.of(
                Pair.of(getCounterClaim(), CounterClaimConfirmationHeaderText.class)
            ));
        get2v1DifferentResponseCase().forEach(caseData -> list.add(
            Pair.of(caseData, SpecResponse2v1DifferentHeaderText.class))
        );
        get1v2DivergentResponseCase().forEach(caseData ->
            list.add(Pair.of(caseData, SpecResponse1v2DivergentHeaderText.class)));
        return list;
    }

    /**
     * Creates the cases expected to be handled by 2v1 different responses.
     *
     * @return cases to check 2v1 different response.
     */
    private List<CaseData> get2v1DifferentResponseCase() {
        Party party = new Party();
        List<CaseData> cases = new ArrayList<>();
        for (RespondentResponseTypeSpec r1 : RespondentResponseTypeSpec.values()) {
            for (RespondentResponseTypeSpec r2 : RespondentResponseTypeSpec.values()) {
                if (!r1.equals(r2)) {
                    CaseData caseData = CaseDataBuilder.builder().build();
                    caseData.setApplicant1(party);
                    caseData.setApplicant2(party);
                    caseData.setClaimant1ClaimResponseTypeForSpec(r1);
                    caseData.setClaimant2ClaimResponseTypeForSpec(r2);
                    cases.add(caseData);
                }
            }
        }
        return cases;
    }

    private List<CaseData> get1v2DivergentResponseCase() {
        Party party = new Party();

        List<CaseData> cases = new ArrayList<>();
        for (RespondentResponseTypeSpec r1 : RespondentResponseTypeSpec.values()) {
            for (RespondentResponseTypeSpec r2 : RespondentResponseTypeSpec.values()) {
                if (!r1.equals(r2)) {
                    CaseData caseData = CaseDataBuilder.builder().build();
                    caseData.setApplicant1(party);
                    caseData.setRespondent1(party);
                    caseData.setRespondent2(party);
                    caseData.setRespondent2SameLegalRepresentative(YesOrNo.YES);
                    caseData.setRespondentResponseIsSame(YesOrNo.NO);
                    caseData.setRespondent1ClaimResponseTypeForSpec(r1);
                    caseData.setRespondent2ClaimResponseTypeForSpec(r2);
                    cases.add(caseData);
                }
            }
        }
        return cases;
    }

    private CaseData getCounterClaim() {
        return CaseDataBuilder.builder()
            .atStateRespondentCounterClaim()
            .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.COUNTER_CLAIM)
            .build();
    }

}
