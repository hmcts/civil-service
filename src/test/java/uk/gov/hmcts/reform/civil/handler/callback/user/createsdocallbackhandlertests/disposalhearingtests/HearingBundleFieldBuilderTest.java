package uk.gov.hmcts.reform.civil.handler.callback.user.createsdocallbackhandlertests.disposalhearingtests;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.handler.callback.user.createsdocallbackhandler.disposalhearing.HearingBundleFieldBuilder;
import uk.gov.hmcts.reform.civil.model.CaseData;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class HearingBundleFieldBuilderTest {

    @InjectMocks
    private HearingBundleFieldBuilder hearingBundleFieldBuilder;

    @Test
    void shouldSetHearingBundle() {
        CaseData.CaseDataBuilder<?, ?> updatedData = CaseData.builder();

        hearingBundleFieldBuilder.build(updatedData);

        CaseData result = updatedData.build();
        assertThat(result.getDisposalHearingBundle()).isNotNull();
        assertThat(result.getDisposalHearingBundle().getInput())
                .isEqualTo("At least 7 days before the disposal hearing, the claimant must file and serve");
    }
}