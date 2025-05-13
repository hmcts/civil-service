package uk.gov.hmcts.reform.civil.notification.handlers.notifyclaimandclaimdetails.notifyunspecclaimdetails;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.OrganisationService;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotifyClaimDetailsAppSolEmailDTOGeneratorTest {

    @Mock
    private OrganisationService organisationService;

    @Mock
    private NotifyClaimDetailsHelper notifyClaimDetailsHelper;

    @InjectMocks
    private NotifyClaimDetailsAppSolEmailDTOGenerator generator;

    private final CaseData caseData = CaseData.builder().build();

    @Test
    void shouldReturnCorrectEmailTemplateId() {
        String expectedTemplateId = "template-id-xyz456";
        when(notifyClaimDetailsHelper.getEmailTemplate()).thenReturn(expectedTemplateId);

        String actualTemplateId = generator.getEmailTemplateId(caseData);

        assertEquals(expectedTemplateId, actualTemplateId);
        verify(notifyClaimDetailsHelper).getEmailTemplate();  // Ensure interaction with mock
    }

    @Test
    void shouldReturnCorrectReferenceTemplate() {
        String referenceTemplate = generator.getReferenceTemplate();

        assertEquals(NotifyClaimDetailsHelper.REFERENCE_TEMPLATE, referenceTemplate);
    }

    @Test
    void shouldAddCustomProperties() {
        Map<String, String> baseProperties = new HashMap<>();
        Map<String, String> customProperties = Map.of("key1", "value1", "key2", "value2");
        when(notifyClaimDetailsHelper.getCustomProperties(caseData)).thenReturn(customProperties);

        Map<String, String> result = generator.addCustomProperties(baseProperties, caseData);

        assertEquals(2, result.size());
        assertEquals("value1", result.get("key1"));
        assertEquals("value2", result.get("key2"));
        verify(notifyClaimDetailsHelper).getCustomProperties(caseData);  // Verify interaction with mock
    }
}
