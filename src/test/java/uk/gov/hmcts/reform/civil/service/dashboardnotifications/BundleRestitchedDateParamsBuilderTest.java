package uk.gov.hmcts.reform.civil.service.dashboardnotifications;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.model.Bundle;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.IdValue;
import uk.gov.hmcts.reform.civil.utils.DateUtils;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class BundleRestitchedDateParamsBuilderTest {

    private BundleRestitchedDateParamsBuilder builder;

    @BeforeEach
    void setUp() {
        builder = new BundleRestitchedDateParamsBuilder();
    }

    @Test
    void shouldAddParamsWhenLatestBundleCreatedOnIsPresent() {
        LocalDateTime latestCreatedOn = LocalDateTime.now();
        Bundle bundle = mock(Bundle.class);
        when(bundle.getCreatedOn()).thenReturn(Optional.of(latestCreatedOn));
        IdValue<Bundle> idValue = new IdValue<>("1", bundle);
        CaseData caseData = mock(CaseData.class);
        when(caseData.getCaseBundles()).thenReturn(List.of(idValue));
        HashMap<String, Object> params = new HashMap<>();

        builder.addParams(caseData, params);

        assertThat(params).containsEntry("bundleRestitchedDateEn", DateUtils.formatDate(latestCreatedOn));
        assertThat(params).containsEntry("bundleRestitchedDateCy", DateUtils.formatDateInWelsh(latestCreatedOn.toLocalDate(), false));
    }

    @Test
    void shouldNotAddParamsWhenNoBundlesArePresent() {
        CaseData caseData = mock(CaseData.class);
        when(caseData.getCaseBundles()).thenReturn(null);
        HashMap<String, Object> params = new HashMap<>();

        builder.addParams(caseData, params);

        assertThat(params).isEmpty();
    }

    @Test
    void shouldNotAddParamsWhenBundlesHaveNoCreatedOnDate() {
        Bundle bundle = mock(Bundle.class);
        when(bundle.getCreatedOn()).thenReturn(Optional.empty());
        IdValue<Bundle> idValue = new IdValue<>("1", bundle);
        CaseData caseData = mock(CaseData.class);
        when(caseData.getCaseBundles()).thenReturn(List.of(idValue));
        HashMap<String, Object> params = new HashMap<>();

        builder.addParams(caseData, params);

        assertThat(params).isEmpty();
    }
}
