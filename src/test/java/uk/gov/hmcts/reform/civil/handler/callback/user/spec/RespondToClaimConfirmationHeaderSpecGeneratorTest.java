package uk.gov.hmcts.reform.civil.handler.callback.user.spec;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Test;
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

import static org.assertj.core.api.Assertions.assertThat;

public class RespondToClaimConfirmationHeaderSpecGeneratorTest
    implements CaseDataToTextGeneratorTest.CaseDataToTextGeneratorIntentionConfig
    <RespondToClaimConfirmationHeaderSpecGenerator> {

    @Test
    void shouldGenerateCounterClaimHeader() {
        CaseData caseData = getCounterClaim();

        assertThat(new CounterClaimConfirmationHeaderText().generateTextFor(caseData, null)).contains(
            String.format(
                "# You have chosen to counterclaim%n## Claim number: %s",
                caseData.getLegacyCaseReference()
            )
        );
    }

    @Test
    void shouldGenerateHeaderForDifferentResponsesPerClaimant() {
        CaseData caseData = get2v1DifferentResponseCase().getFirst();

        assertThat(new SpecResponse2v1DifferentHeaderText().generateTextFor(caseData, null))
            .contains("The defendant has chosen different responses for each claimant");
    }

    @Test
    void shouldGenerateHeaderFor1v2DivergentResponseCase() {
        CaseData caseData = get1v2DivergentResponseCase().getFirst();

        assertThat(new SpecResponse1v2DivergentHeaderText().generateTextFor(caseData, null)).contains(
            String.format(
                "# The defendants have chosen their responses%n## Claim number <br>%s",
                caseData.getLegacyCaseReference()
            )
        );
    }

    @Test
    void shouldGenerateCounterClaimHeaderWhenBothResponsesAreTheSame() {
        CaseData caseData = getCounterClaim();
        caseData.setRespondent2(new Party());
        caseData.setRespondentResponseIsSame(YesOrNo.YES);

        assertThat(new CounterClaimConfirmationHeaderText().generateTextFor(caseData, null)).contains(
            String.format(
                "# You have chosen to counterclaim%n## Claim number: %s",
                caseData.getLegacyCaseReference()
            )
        );
    }

    @Test
    void shouldGenerateCounterClaimHeaderWhenSecondRespondentAlsoCounterclaims() {
        CaseData caseData = getCounterClaim();
        caseData.setRespondent2(new Party());
        caseData.setRespondentResponseIsSame(YesOrNo.NO);
        caseData.setRespondent2ClaimResponseTypeForSpec(RespondentResponseTypeSpec.COUNTER_CLAIM);

        assertThat(new CounterClaimConfirmationHeaderText().generateTextFor(caseData, null)).contains(
            String.format(
                "# You have chosen to counterclaim%n## Claim number: %s",
                caseData.getLegacyCaseReference()
            )
        );
    }

    @Test
    void shouldReturnEmptyWhenDefendantChoseSingleResponseToBothClaimants() {
        CaseData caseData = get2v1DifferentResponseCase().getFirst();
        caseData.setDefendantSingleResponseToBothClaimants(YesOrNo.YES);

        assertThat(new SpecResponse2v1DifferentHeaderText().generateTextFor(caseData, null)).isEmpty();
    }

    @Test
    void shouldReturnEmptyWhenClaimantResponsesMatch() {
        Party party = new Party();
        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setApplicant1(party);
        caseData.setApplicant2(party);
        caseData.setClaimant1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_ADMISSION);
        caseData.setClaimant2ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_ADMISSION);

        assertThat(new SpecResponse2v1DifferentHeaderText().generateTextFor(caseData, null)).isEmpty();
    }

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
