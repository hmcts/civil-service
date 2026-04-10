package uk.gov.hmcts.reform.civil.notification.handlers.standarddirectionorderdj;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.service.OrganisationService;
import uk.gov.hmcts.reform.civil.utils.NotificationUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

@SuppressWarnings("common-java:DuplicatedBlocks")
abstract class StandardDirectionOrderDJEmailGeneratorBaseTest<T> {

    protected static final String TEMPLATE_ID = "template-id";
    protected static final String LEGAL_ORG_NAME = "legalOrgName";
    protected static final String CLAIM_NUMBER = "claimReferenceNumber";

    protected T generator;
    protected NotificationsProperties notificationsProperties;
    protected OrganisationService organisationService;
    protected MockedStatic<NotificationUtils> notificationUtilsMockedStatic;

    protected abstract T createGenerator(NotificationsProperties notificationsProperties,
                                         OrganisationService organisationService);

    protected abstract String getExpectedReferenceTemplate();

    @BeforeEach
    void setUp() {
        notificationsProperties = mock(NotificationsProperties.class);
        organisationService = mock(OrganisationService.class);
        generator = createGenerator(notificationsProperties, organisationService);
        notificationUtilsMockedStatic = mockStatic(NotificationUtils.class);
        setupAdditionalMocks();
    }

    protected void setupAdditionalMocks() {
        // Override in subclass if needed
    }

    @AfterEach
    void tearDown() {
        if (notificationUtilsMockedStatic != null) {
            notificationUtilsMockedStatic.close();
        }
        tearDownAdditionalMocks();
    }

    protected void tearDownAdditionalMocks() {
        // Override in subclass if needed
    }

    @Test
    void shouldReturnCorrectEmailTemplateId() {
        CaseData caseData = mock(CaseData.class);

        when(notificationsProperties.getStandardDirectionOrderDJTemplate()).thenReturn(TEMPLATE_ID);

        String result = getEmailTemplateId(caseData);

        assertThat(result).isEqualTo(TEMPLATE_ID);
    }

    @Test
    void shouldReturnCorrectReferenceTemplate() {
        String result = getReferenceTemplate();

        assertThat(result).isEqualTo(getExpectedReferenceTemplate());
    }

    protected abstract String getEmailTemplateId(CaseData caseData);

    protected abstract String getReferenceTemplate();
}
