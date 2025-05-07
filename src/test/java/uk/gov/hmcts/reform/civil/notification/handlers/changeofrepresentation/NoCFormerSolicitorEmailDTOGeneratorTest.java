package uk.gov.hmcts.reform.civil.notification.handlers.changeofrepresentation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.ChangeOfRepresentation;
import uk.gov.hmcts.reform.civil.notification.handlers.EmailDTO;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.utils.NocNotificationUtils;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

class NoCFormerSolicitorEmailDTOGeneratorTest {

    private static final String FORMER_SOLICITOR_EMAIL = "solicitor@example.com";
    private static final String TEMPLATE_ID = "template-id-123";
    private static final String CASE_REFERENCE = "000DC001";

    private NotificationsProperties notificationsProperties;
    private NoCHelper noCHelper;
    private NoCFormerSolicitorEmailDTOGenerator generator;
    private CaseData caseData;

    @BeforeEach
    void setUp() {
        notificationsProperties = mock(NotificationsProperties.class);
        noCHelper = mock(NoCHelper.class);
        generator = new NoCFormerSolicitorEmailDTOGenerator(notificationsProperties, noCHelper);
        caseData = mock(CaseData.class);
    }

    @Test
    void shouldNotify_WhenOrganisationToRemoveIdIsPresent() {
        ChangeOfRepresentation change = mock(ChangeOfRepresentation.class);
        when(caseData.getChangeOfRepresentation()).thenReturn(change);
        when(change.getOrganisationToRemoveID()).thenReturn("OrgToRemove");

        Boolean result = generator.getShouldNotify(caseData);

        assertThat(result).isTrue();
    }

    @Test
    void shouldNotNotify_WhenOrganisationToRemoveIdIsNull() {
        ChangeOfRepresentation change = mock(ChangeOfRepresentation.class);
        when(caseData.getChangeOfRepresentation()).thenReturn(change);
        when(change.getOrganisationToRemoveID()).thenReturn(null);

        Boolean result = generator.getShouldNotify(caseData);

        assertThat(result).isFalse();
    }

    @Test
    void shouldBuildEmailDTO_WithExpectedValues() {
        ChangeOfRepresentation change = mock(ChangeOfRepresentation.class);
        when(caseData.getChangeOfRepresentation()).thenReturn(change);
        when(change.getOrganisationToRemoveID()).thenReturn("OrgToRemove");

        when(notificationsProperties.getNoticeOfChangeFormerSolicitor()).thenReturn(TEMPLATE_ID);
        when(caseData.getLegacyCaseReference()).thenReturn(CASE_REFERENCE);
        mockStatic(NocNotificationUtils.class).when(() ->
                                    NocNotificationUtils.getPreviousSolicitorEmail(caseData))
            .thenReturn(FORMER_SOLICITOR_EMAIL);

        Map<String, String> customProps = Map.of("key", "value");
        when(noCHelper.getProperties(caseData, false)).thenReturn(customProps);

        EmailDTO emailDTO = generator.buildEmailDTO(caseData);

        assertThat(emailDTO.getTargetEmail()).isEqualTo(FORMER_SOLICITOR_EMAIL);
        assertThat(emailDTO.getEmailTemplate()).isEqualTo(TEMPLATE_ID);
        assertThat(emailDTO.getReference()).isEqualTo("notice-of-change-" + CASE_REFERENCE);
        assertThat(emailDTO.getParameters()).containsEntry("key", "value");
    }
}
