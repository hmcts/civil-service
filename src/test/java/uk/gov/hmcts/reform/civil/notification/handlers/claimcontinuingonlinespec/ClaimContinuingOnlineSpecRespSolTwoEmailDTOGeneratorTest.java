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
class ClaimContinuingOnlineSpecRespSolTwoEmailDTOGeneratorTest {

    private static final String TEMPLATE_ID = "resp-template-2";
    private static final String REF_TEMPLATE = "claim-continuing-online-notification-%s";
    private static final String ORG_NAME = "Resp2 Org";
    private static final LocalDateTime DEADLINE = LocalDateTime.of(2025, 5, 12, 10, 30);
    public static final String ID = "Org123";
    public static final String LEGACY_CASE_REFERENCE = "000DC002";

    @Mock
    private NotificationsProperties notificationsProperties;

    @Mock
    private OrganisationService organisationService;

    @InjectMocks
    private ClaimContinuingOnlineSpecRespSolTwoEmailDTOGenerator emailGenerator;

    @BeforeEach
    void setUp() {
        when(notificationsProperties.getRespondentSolicitorClaimContinuingOnlineForSpec())
                .thenReturn(TEMPLATE_ID);
    }

    @Test
    void shouldReturnTemplateAndReference() {
        CaseData caseData = CaseData.builder().build();

        assertThat(emailGenerator.getEmailTemplateId(caseData))
                .isEqualTo(TEMPLATE_ID);
        assertThat(emailGenerator.getReferenceTemplate())
                .isEqualTo(REF_TEMPLATE);
    }

    @Test
    void shouldIncludeLegalOrgName_forSecondRespondent() {
        when(organisationService.findOrganisationById(anyString()))
                .thenReturn(Optional.of(
                        builder()
                                .name(ORG_NAME)
                                .build()
                ));

        Organisation organisation = Organisation.builder()
                .organisationID(ID)
                .build();

        CaseData caseData = CaseData.builder()
                .ccdCaseReference(1234L)
                .legacyCaseReference(LEGACY_CASE_REFERENCE)
                .respondent2ResponseDeadline(DEADLINE)
                .respondent2OrganisationPolicy(OrganisationPolicy.builder().organisation(organisation).build())
                .build();

        EmailDTO dto = emailGenerator.buildEmailDTO(caseData);
        Map<String, String> params = dto.getParameters();

        assertThat(params).containsEntry(CLAIM_LEGAL_ORG_NAME_SPEC, ORG_NAME);
    }
}
