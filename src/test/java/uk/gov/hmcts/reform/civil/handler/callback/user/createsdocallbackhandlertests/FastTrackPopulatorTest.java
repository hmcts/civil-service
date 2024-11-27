package uk.gov.hmcts.reform.civil.handler.callback.user.createsdocallbackhandlertests;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.handler.callback.user.createsdocallbackhandler.SdoCaseFieldBuilder;
import uk.gov.hmcts.reform.civil.handler.callback.user.createsdocallbackhandler.fasttrack.FastTrackPopulator;
import uk.gov.hmcts.reform.civil.model.CaseData;

import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
class FastTrackPopulatorTest {

    @Mock
    private List<SdoCaseFieldBuilder> fastTrackBuilders;

    @InjectMocks
    private FastTrackPopulator fastTrackPopulator;

    @BeforeEach
    void setUp() {
        fastTrackPopulator = new FastTrackPopulator(fastTrackBuilders);
    }

    @Test
    void shouldSetFastTrackFields() {
        CaseData.CaseDataBuilder<?, ?> updatedData = CaseData.builder();
        fastTrackPopulator.setFastTrackFields(updatedData);

        fastTrackBuilders.forEach(builder -> verify(builder, times(1)).build(updatedData));
    }
}
