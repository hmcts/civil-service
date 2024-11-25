package uk.gov.hmcts.reform.civil.handler.callback.user.createsdocallbackhandlertests.prepopulateddor2andnihlfieldstests;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.constants.SdoR2UiConstantFastTrack;
import uk.gov.hmcts.reform.civil.handler.callback.user.createsdocallbackhandler.prepopulateddor2andnihlfields.SdoR2AndNihlWitnessesOfFactFieldBuilder;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2WitnessOfFact;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;

@ExtendWith(MockitoExtension.class)
class SdoR2AndNihlWitnessesOfFactFieldBuilderTest {

    @InjectMocks
    private SdoR2AndNihlWitnessesOfFactFieldBuilder sdoR2AndNihlWitnessesOfFactFieldBuilder;

    @Test
    void shouldBuildSdoR2WitnessesOfFact() {
        CaseData.CaseDataBuilder<?, ?> caseDataBuilder = CaseData.builder();

        sdoR2AndNihlWitnessesOfFactFieldBuilder.build(caseDataBuilder);

        CaseData caseData = caseDataBuilder.build();
        SdoR2WitnessOfFact witnessesOfFact = caseData.getSdoR2WitnessesOfFact();

        assertEquals(SdoR2UiConstantFastTrack.STATEMENT_WITNESS, witnessesOfFact.getSdoStatementOfWitness());
        assertEquals(NO, witnessesOfFact.getSdoR2RestrictWitness().getIsRestrictWitness());
        assertEquals(3, witnessesOfFact.getSdoR2RestrictWitness().getRestrictNoOfWitnessDetails().getNoOfWitnessClaimant());
        assertEquals(3, witnessesOfFact.getSdoR2RestrictWitness().getRestrictNoOfWitnessDetails().getNoOfWitnessDefendant());
        assertEquals(SdoR2UiConstantFastTrack.RESTRICT_WITNESS_TEXT, witnessesOfFact.getSdoR2RestrictWitness().getRestrictNoOfWitnessDetails().getPartyIsCountedAsWitnessTxt());
        assertEquals(NO, witnessesOfFact.getSdoRestrictPages().getIsRestrictPages());
        assertEquals(SdoR2UiConstantFastTrack.RESTRICT_NUMBER_PAGES_TEXT1, witnessesOfFact.getSdoRestrictPages().getRestrictNoOfPagesDetails().getWitnessShouldNotMoreThanTxt());
        assertEquals(12, witnessesOfFact.getSdoRestrictPages().getRestrictNoOfPagesDetails().getNoOfPages());
        assertEquals(SdoR2UiConstantFastTrack.RESTRICT_NUMBER_PAGES_TEXT2, witnessesOfFact.getSdoRestrictPages().getRestrictNoOfPagesDetails().getFontDetails());
        assertEquals(SdoR2UiConstantFastTrack.DEADLINE, witnessesOfFact.getSdoWitnessDeadline());
        assertEquals(LocalDate.now().plusDays(70), witnessesOfFact.getSdoWitnessDeadlineDate());
        assertEquals(SdoR2UiConstantFastTrack.DEADLINE_EVIDENCE, witnessesOfFact.getSdoWitnessDeadlineText());
    }
}