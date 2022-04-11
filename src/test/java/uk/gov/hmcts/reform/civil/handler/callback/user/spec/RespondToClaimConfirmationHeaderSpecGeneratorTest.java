package uk.gov.hmcts.reform.civil.handler.callback.user.spec;

import org.apache.commons.lang3.tuple.Pair;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec;
import uk.gov.hmcts.reform.civil.handler.callback.user.spec.response.confirmation.header.CounterClaimConfirmationHeaderText;
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
        return list;
    }

    /**
     * Creates the cases expected to be handled by 2v1 different responses.
     *
     * @return cases to check 2v1 different response.
     */
    private List<CaseData> get2v1DifferentResponseCase() {
        Party applicant1 = Party.builder().build();
        Party applicant2 = Party.builder().build();
        List<CaseData> cases = new ArrayList<>();
        for (RespondentResponseTypeSpec r1 : RespondentResponseTypeSpec.values()) {
            for (RespondentResponseTypeSpec r2 : RespondentResponseTypeSpec.values()) {
                if (!r1.equals(r2)) {
                    cases.add(CaseData.builder()
                                  .applicant1(applicant1)
                                  .applicant2(applicant2)
                                  .claimant1ClaimResponseTypeForSpec(r1)
                                  .claimant2ClaimResponseTypeForSpec(r2)
                                  .build());
                }
            }
        }
        return cases;
    }

    private CaseData getCounterClaim() {
        return CaseDataBuilder.builder()
            .atStateApplicantRespondToDefenceAndProceed()
            .build().toBuilder()
            .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.COUNTER_CLAIM)
            .build();
    }

}
