package uk.gov.hmcts.reform.civil.notification.handlers.claimcontinuingonlinespec;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.model.Organisation;
import uk.gov.hmcts.reform.ccd.model.OrganisationPolicy;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notification.handlers.EmailDTO;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.service.OrganisationService;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIM_LEGAL_ORG_NAME_SPEC;
import static uk.gov.hmcts.reform.civil.prd.model.Organisation.builder;

@ExtendWith(MockitoExtension.class)
class ClaimContinuingOnlineSpecRespSolOneEmailDTOGeneratorTest {

    public static final String RESP_TEMPLATE = "resp-template";
    public static final String CLAIM_CONTINUING_ONLINE_NOTIFICATION = "claim-continuing-online-notification-%s";
    public static final String ID = "Org123";
    public static final long CCD_CASE_REFERENCE = 1234L;
    public static final String LEGACY_CASE_REFERENCE = "000DC001";
    public static final String ORG_NAME = "Resp1 Org";
    private final LocalDateTime deadline = LocalDateTime.of(2025, 5, 12, 10, 30);

    @Mock
    private NotificationsProperties notificationsProperties;

    @Mock
    private OrganisationService organisationService;

    @InjectMocks
    private ClaimContinuingOnlineSpecRespSolOneEmailDTOGenerator emailGenerator;

    @BeforeEach
    void setUp() {
        when(notificationsProperties.getRespondentSolicitorClaimContinuingOnlineForSpec()).thenReturn(RESP_TEMPLATE);
    }

    @Test
    void shouldReturnTemplateAndReference() {
        CaseData caseData = CaseData.builder().build();

        assertThat(emailGenerator.getEmailTemplateId(caseData)).isEqualTo(RESP_TEMPLATE);
        assertThat(emailGenerator.getReferenceTemplate()).isEqualTo(CLAIM_CONTINUING_ONLINE_NOTIFICATION);
    }

    @Test
    void shouldIncludeLegalOrgName() {
        Organisation organisation = Organisation.builder()
                .organisationID(ID)
                .build();

        CaseData caseData = CaseData.builder()
                .ccdCaseReference(CCD_CASE_REFERENCE)
                .legacyCaseReference(LEGACY_CASE_REFERENCE)
                .respondent1ResponseDeadline(deadline)
                .respondent1OrganisationPolicy(OrganisationPolicy.builder().organisation(organisation).build())
                .build();

        String orgName = ORG_NAME;

        when(organisationService.findOrganisationById(anyString())).thenReturn(Optional.of(builder().name(orgName).build()));

        EmailDTO dto = emailGenerator.buildEmailDTO(caseData);
        Map<String, String> params = dto.getParameters();

        assertThat(params).containsEntry(CLAIM_LEGAL_ORG_NAME_SPEC, orgName);
    }
}
