package uk.gov.hmcts.reform.civil.notification.handlers.changeofrepresentation.otherflow;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.notification.handlers.AllPartiesEmailGenerator;
import uk.gov.hmcts.reform.civil.notification.handlers.claimantliphelpwithfees.ClaimantLipHelpWithFeesAllPartiesEmailGenerator;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class AllNoCPartiesEmailGeneratorTest {

    @Mock
    private NoCFormerSolicitorEmailDTOGenerator formerSolicitorEmailDTOGenerator;

    @Mock
    private NoCOtherSolicitorOneEmailDTOGenerator otherSolicitorOneEmailDTOGenerator;

    @Mock
    private NoCOtherSolicitorTwoEmailDTOGenerator otherSolicitorTwoEmailDTOGenerator;

    @Mock
    private NoCHearingFeeUnpaidAppSolEmailDTOGenerator hearingFeeUnpaidAppSolEmailDTOGenerator;

    @Mock
    private NoCClaimantLipEmailDTOGenerator claimantLipEmailDTOGenerator;

    @Mock
    private NoCLipVLRNewDefendantEmailDTOGenerator newDefendantEmailDTOGenerator;

    @InjectMocks
    private AllNoCPartiesEmailGenerator emailGenerator;

    @Test
    void shouldInitializeParentClassWithCorrectArguments() {
        assertThat(emailGenerator).isInstanceOf(AllPartiesEmailGenerator.class);
    }
}

