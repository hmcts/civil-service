package uk.gov.hmcts.reform.civil.notification.handlers.changeofrepresentation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.ChangeOfRepresentation;
import uk.gov.hmcts.reform.civil.notification.handlers.EmailDTO;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NoCFormerSolicitorEmailDTOGeneratorTest {

    private static final String FORMER_SOLICITOR_EMAIL = "solicitor@example.com";
    private static final String TEMPLATE_ID = "template-id-123";
    private static final String CASE_REFERENCE = "000DC001";

    @Mock
    private NotificationsProperties notificationsProperties;

    @Mock
    private NoCHelper noCHelper;

    @InjectMocks
    private NoCFormerSolicitorEmailDTOGenerator generator;

    @Mock
    private CaseData caseData;

    @Mock
    private ChangeOfRepresentation changeOfRepresentation;

    @BeforeEach
    void setUpMocks() {
        when(caseData.getChangeOfRepresentation()).thenReturn(changeOfRepresentation);
    }

    @Test
    void shouldNotify_WhenOrganisationToRemoveIdIsPresent() {
        when(changeOfRepresentation.getOrganisationToRemoveID()).thenReturn("OrgToRemove");

        Boolean result = generator.getShouldNotify(caseData);

        assertThat(result).isTrue();
    }

    @Test
    void shouldNotNotify_WhenOrganisationToRemoveIdIsNull() {
        when(changeOfRepresentation.getOrganisationToRemoveID()).thenReturn(null);

        Boolean result = generator.getShouldNotify(caseData);

        assertThat(result).isFalse();
    }

    @Test
    void shouldBuildEmailDTO_WithExpectedValues() {
        when(changeOfRepresentation.getOrganisationToRemoveID()).thenReturn("OrgToRemove");
        when(notificationsProperties.getNoticeOfChangeFormerSolicitor()).thenReturn(TEMPLATE_ID);
        when(caseData.getLegacyCaseReference()).thenReturn(CASE_REFERENCE);
        when(noCHelper.getPreviousSolicitorEmail(caseData)).thenReturn(FORMER_SOLICITOR_EMAIL);
        when(noCHelper.getProperties(caseData, false)).thenReturn(Map.of("key", "value"));

        EmailDTO emailDTO = generator.buildEmailDTO(caseData);

        assertThat(emailDTO.getTargetEmail()).isEqualTo(FORMER_SOLICITOR_EMAIL);
        assertThat(emailDTO.getEmailTemplate()).isEqualTo(TEMPLATE_ID);
        assertThat(emailDTO.getReference()).isEqualTo("notice-of-change-" + CASE_REFERENCE);
        assertThat(emailDTO.getParameters()).containsEntry("key", "value");
    }
}
