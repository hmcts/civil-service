package uk.gov.hmcts.reform.civil.notification.handlers.notifyclaimandclaimdetails.caseproceedsoffline;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.service.OrganisationService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CaseProceedOfflineAppSolEmailDTOGeneratorTest {

    @Mock
    private OrganisationService organisationService;

    @Mock
    private NotificationsProperties notificationsProperties;

    @InjectMocks
    private CaseProceedOfflineAppSolEmailDTOGenerator generator;

    @Test
    void shouldReturnCorrectEmailTemplateId() {
        String expectedTemplateId = "template-id-123";
        when(notificationsProperties.getSolicitorCaseTakenOffline()).thenReturn(expectedTemplateId);

        String actualTemplateId = generator.getEmailTemplateId(CaseData.builder().build());

        assertEquals(expectedTemplateId, actualTemplateId);
    }

    @Test
    void shouldReturnCorrectReferenceTemplate() {
        String referenceTemplate = generator.getReferenceTemplate();

        assertEquals("case-proceeds-in-caseman-applicant-notification-%s", referenceTemplate);
    }
}
