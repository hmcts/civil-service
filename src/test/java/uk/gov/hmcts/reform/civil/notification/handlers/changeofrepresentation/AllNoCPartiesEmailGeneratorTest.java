package uk.gov.hmcts.reform.civil.notification.handlers.changeofrepresentation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notification.handlers.EmailDTO;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AllNoCPartiesEmailGeneratorTest {

    private NoCFormerSolicitorEmailDTOGenerator formerSolicitorEmailDTOGenerator;
    private NoCOtherSolicitorOneEmailDTOGenerator otherSolicitorOneEmailDTOGenerator;
    private NoCOtherSolicitorTwoEmailDTOGenerator otherSolicitorTwoEmailDTOGenerator;
    private NoCHearingFeeUnpaidAppSolEmailDTOGenerator hearingFeeUnpaidAppSolEmailDTOGenerator;
    private NoCClaimantLipEmailDTOGenerator claimantLipEmailDTOGenerator;
    private NoCLipVLRNewDefendantEmailDTOGenerator newDefendantEmailDTOGenerator;

    private AllNoCPartiesEmailGenerator generator;

    private final CaseData caseData = mock(CaseData.class);
    private final EmailDTO sampleEmail = mock(EmailDTO.class);

    @BeforeEach
    void setUp() {
        formerSolicitorEmailDTOGenerator = mock(NoCFormerSolicitorEmailDTOGenerator.class);
        otherSolicitorOneEmailDTOGenerator = mock(NoCOtherSolicitorOneEmailDTOGenerator.class);
        otherSolicitorTwoEmailDTOGenerator = mock(NoCOtherSolicitorTwoEmailDTOGenerator.class);
        hearingFeeUnpaidAppSolEmailDTOGenerator = mock(NoCHearingFeeUnpaidAppSolEmailDTOGenerator.class);
        claimantLipEmailDTOGenerator = mock(NoCClaimantLipEmailDTOGenerator.class);
        newDefendantEmailDTOGenerator = mock(NoCLipVLRNewDefendantEmailDTOGenerator.class);

        generator = new AllNoCPartiesEmailGenerator(
            formerSolicitorEmailDTOGenerator,
            otherSolicitorOneEmailDTOGenerator,
            otherSolicitorTwoEmailDTOGenerator,
            hearingFeeUnpaidAppSolEmailDTOGenerator,
            claimantLipEmailDTOGenerator,
            newDefendantEmailDTOGenerator
        );
    }

    @Test
    void shouldReturnEmailDTOsForGeneratorsThatShouldNotify() {
        when(formerSolicitorEmailDTOGenerator.getShouldNotify(caseData)).thenReturn(true);
        when(formerSolicitorEmailDTOGenerator.buildEmailDTO(caseData)).thenReturn(sampleEmail);

        when(otherSolicitorOneEmailDTOGenerator.getShouldNotify(caseData)).thenReturn(false);
        when(otherSolicitorTwoEmailDTOGenerator.getShouldNotify(caseData)).thenReturn(false);
        when(hearingFeeUnpaidAppSolEmailDTOGenerator.getShouldNotify(caseData)).thenReturn(false);
        when(claimantLipEmailDTOGenerator.getShouldNotify(caseData)).thenReturn(false);
        when(newDefendantEmailDTOGenerator.getShouldNotify(caseData)).thenReturn(false);

        Set<EmailDTO> result = generator.getPartiesToNotify(caseData);

        assertThat(result).containsExactly(sampleEmail);

        verify(formerSolicitorEmailDTOGenerator).getShouldNotify(caseData);
        verify(formerSolicitorEmailDTOGenerator).buildEmailDTO(caseData);

        verify(otherSolicitorOneEmailDTOGenerator).getShouldNotify(caseData);
        verify(otherSolicitorTwoEmailDTOGenerator).getShouldNotify(caseData);
        verify(hearingFeeUnpaidAppSolEmailDTOGenerator).getShouldNotify(caseData);
        verify(claimantLipEmailDTOGenerator).getShouldNotify(caseData);
        verify(newDefendantEmailDTOGenerator).getShouldNotify(caseData);

        verify(otherSolicitorOneEmailDTOGenerator, never()).buildEmailDTO(caseData);
        verify(otherSolicitorTwoEmailDTOGenerator, never()).buildEmailDTO(caseData);
        verify(hearingFeeUnpaidAppSolEmailDTOGenerator, never()).buildEmailDTO(caseData);
        verify(claimantLipEmailDTOGenerator, never()).buildEmailDTO(caseData);
        verify(newDefendantEmailDTOGenerator, never()).buildEmailDTO(caseData);
    }

    @Test
    void shouldReturnEmptySetIfNoGeneratorsShouldNotify() {
        when(formerSolicitorEmailDTOGenerator.getShouldNotify(caseData)).thenReturn(false);
        when(otherSolicitorOneEmailDTOGenerator.getShouldNotify(caseData)).thenReturn(false);
        when(otherSolicitorTwoEmailDTOGenerator.getShouldNotify(caseData)).thenReturn(false);
        when(hearingFeeUnpaidAppSolEmailDTOGenerator.getShouldNotify(caseData)).thenReturn(false);
        when(claimantLipEmailDTOGenerator.getShouldNotify(caseData)).thenReturn(false);
        when(newDefendantEmailDTOGenerator.getShouldNotify(caseData)).thenReturn(false);

        Set<EmailDTO> result = generator.getPartiesToNotify(caseData);

        assertThat(result).isEmpty();

        verify(formerSolicitorEmailDTOGenerator).getShouldNotify(caseData);
        verify(otherSolicitorOneEmailDTOGenerator).getShouldNotify(caseData);
        verify(otherSolicitorTwoEmailDTOGenerator).getShouldNotify(caseData);
        verify(hearingFeeUnpaidAppSolEmailDTOGenerator).getShouldNotify(caseData);
        verify(claimantLipEmailDTOGenerator).getShouldNotify(caseData);
        verify(newDefendantEmailDTOGenerator).getShouldNotify(caseData);

        verify(formerSolicitorEmailDTOGenerator, never()).buildEmailDTO(caseData);
        verify(otherSolicitorOneEmailDTOGenerator, never()).buildEmailDTO(caseData);
        verify(otherSolicitorTwoEmailDTOGenerator, never()).buildEmailDTO(caseData);
        verify(hearingFeeUnpaidAppSolEmailDTOGenerator, never()).buildEmailDTO(caseData);
        verify(claimantLipEmailDTOGenerator, never()).buildEmailDTO(caseData);
        verify(newDefendantEmailDTOGenerator, never()).buildEmailDTO(caseData);
    }
}
