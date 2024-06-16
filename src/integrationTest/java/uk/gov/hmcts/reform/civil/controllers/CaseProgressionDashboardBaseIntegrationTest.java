package uk.gov.hmcts.reform.civil.controllers;

import org.junit.jupiter.api.BeforeEach;

import static org.mockito.Mockito.when;

public class CaseProgressionDashboardBaseIntegrationTest extends DashboardBaseIntegrationTest {

    @BeforeEach
    public void before() {
        when(featureToggleService.isCaseProgressionEnabled()).thenReturn(true);
    }
}
