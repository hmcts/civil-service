package uk.gov.hmcts.reform.civil.handler.callback.user.createsdo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.handler.callback.user.createsdo.fasttrack.FastTrackPopulator;
import uk.gov.hmcts.reform.civil.model.CaseData;

import java.util.List;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

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
