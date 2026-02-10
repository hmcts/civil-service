package uk.gov.hmcts.reform.civil.notification.handlers.notifyclaimandclaimdetails.notifyunspecclaim;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.OrganisationService;
import uk.gov.hmcts.reform.civil.utils.PartyUtils;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotifyClaimAppSolEmailDTOGeneratorTest {

    private static final String RESPONDENT_NAME = "defendantName";

    @Mock
    private OrganisationService organisationService;

    @Mock
    private NotifyClaimHelper notifyClaimHelper;

    @InjectMocks
    private NotifyClaimAppSolEmailDTOGenerator generator;

    private final CaseData caseData = CaseData.builder().build();

    @Test
    void shouldReturnCorrectEmailTemplateId() {
        String expectedTemplateId = "template-id-abc123";
        when(notifyClaimHelper.getNotifyClaimEmailTemplate()).thenReturn(expectedTemplateId);

        String actualTemplateId = generator.getEmailTemplateId(caseData);

        assertEquals(expectedTemplateId, actualTemplateId);
        verify(notifyClaimHelper).getNotifyClaimEmailTemplate();
    }

    @Test
    void shouldReturnCorrectReferenceTemplate() {
        String referenceTemplate = generator.getReferenceTemplate();
        assertEquals(NotifyClaimHelper.NOTIFY_REFERENCE_TEMPLATE, referenceTemplate);
    }

    @Test
    void shouldAddCustomPropertiesIncludingRespondentName() {
        Map<String, String> baseProperties = new HashMap<>();
        Map<String, String> customProperties = Map.of("key1", "value1", "key2", "value2");

        when(notifyClaimHelper.retrieveCustomProperties(caseData)).thenReturn(customProperties);

        try (MockedStatic<PartyUtils> mockedStatic = mockStatic(PartyUtils.class)) {
            mockedStatic.when(() -> PartyUtils.getPartyNameBasedOnType(caseData.getRespondent1()))
                .thenReturn("John Doe");

            Map<String, String> result = generator.addCustomProperties(baseProperties, caseData);

            assertEquals("John Doe", result.get(RESPONDENT_NAME));

            assertEquals("value1", result.get("key1"));
            assertEquals("value2", result.get("key2"));

            verify(notifyClaimHelper).retrieveCustomProperties(caseData);
        }
    }
}
