package uk.gov.hmcts.reform.civil.service.dashboardnotifications;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;

import java.util.HashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class HearingCourtParamsBuilderTest {

    private HearingCourtParamsBuilder builder;

    @BeforeEach
    void setUp() {
        builder = new HearingCourtParamsBuilder();
    }

    @Test
    void shouldAddHearingCourtDetailsWhenHearingLocationIsPresent() {
        CaseData caseData = mock(CaseData.class);
        DynamicList hearingLocation = mock(DynamicList.class);

        when(caseData.getHearingLocation()).thenReturn(hearingLocation);
        when(caseData.getHearingLocationCourtName()).thenReturn("Court Name");

        HashMap<String, Object> params = new HashMap<>();
        builder.addParams(caseData, params);

        assertThat(params).containsEntry("hearingCourtEn", "Court Name");
        assertThat(params).containsEntry("hearingCourtCy", "Court Name");
    }

    @Test
    void shouldNotAddHearingCourtDetailsWhenHearingLocationIsNull() {
        CaseData caseData = mock(CaseData.class);

        when(caseData.getHearingLocation()).thenReturn(null);

        HashMap<String, Object> params = new HashMap<>();
        builder.addParams(caseData, params);

        assertThat(params).isEmpty();
    }
}
