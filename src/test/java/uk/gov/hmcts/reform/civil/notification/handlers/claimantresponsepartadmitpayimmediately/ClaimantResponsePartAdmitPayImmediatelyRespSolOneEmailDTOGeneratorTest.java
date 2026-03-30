package uk.gov.hmcts.reform.civil.notification.handlers.claimantresponsepartadmitpayimmediately;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.service.OrganisationService;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.model.CaseData;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

public class ClaimantResponsePartAdmitPayImmediatelyRespSolOneEmailDTOGeneratorTest {

    @Mock
    private OrganisationService organisationService;

    @Mock
    private NotificationsProperties notificationsProperties;

    @InjectMocks
    private ClaimantResponsePartAdmitPayImmediatelyRespSolOneEmailDTOGenerator generator;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void shouldReturnRespondentTemplateId() {
        CaseData caseData = new CaseDataBuilder().build();
        when(notificationsProperties.getPartAdmitPayImmediatelyAgreedDefendant()).thenReturn("template-id");

        assertThat(generator.getEmailTemplateId(caseData)).isEqualTo("template-id");
    }

    @Test
    void shouldReturnReferenceTemplate() {
        assertThat(generator.getReferenceTemplate())
            .isEqualTo("part-admit-immediately-agreed-respondent-notification-%s");
    }
}
