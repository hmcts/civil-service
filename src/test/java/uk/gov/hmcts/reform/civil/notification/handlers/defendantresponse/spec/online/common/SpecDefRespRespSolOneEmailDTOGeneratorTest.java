package uk.gov.hmcts.reform.civil.notification.handlers.defendantresponse.spec.online.common;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.service.OrganisationService;
import uk.gov.hmcts.reform.civil.utils.NotificationUtils;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIM_LEGAL_ORG_NAME_SPEC;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.RESPONDENT_NAME;
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.getLegalOrganizationNameForRespondent;

class SpecDefRespRespSolOneEmailDTOGeneratorTest {

    private SpecDefRespEmailHelper specDefRespEmailHelper;
    private OrganisationService organisationService;
    private SpecDefRespRespSolOneEmailDTOGenerator generator;

    @BeforeEach
    void setUp() {
        specDefRespEmailHelper = mock(SpecDefRespEmailHelper.class);
        organisationService = mock(OrganisationService.class);
        generator = new SpecDefRespRespSolOneEmailDTOGenerator(specDefRespEmailHelper, organisationService);
    }

    @Test
    void shouldAddCustomPropertiesWithLegalOrgNameAndRespondentName() {
        Party respondent = Party.builder().type(Party.Type.INDIVIDUAL)
            .individualFirstName("John")
            .individualLastName("Doe").build();
        CaseData caseData = CaseData.builder()
            .respondent1(respondent)
            .build();

        try (var utilsMock = mockStatic(NotificationUtils.class)) {

            utilsMock.when(() -> getLegalOrganizationNameForRespondent(caseData, true, organisationService))
                .thenReturn("Defendant Org");

            Map<String, String> result = generator.addCustomProperties(new HashMap<>(), caseData);

            assertThat(result)
                .containsEntry(RESPONDENT_NAME, "John Doe")
                .containsEntry(CLAIM_LEGAL_ORG_NAME_SPEC, "Defendant Org");
        }
    }

    @Test
    void shouldReturnCorrectTemplateIdFromHelper() {
        CaseData caseData = mock(CaseData.class);
        when(specDefRespEmailHelper.getRespondentTemplate(caseData)).thenReturn("some-template-id");

        String templateId = generator.getEmailTemplateId(caseData);

        assertThat(templateId).isEqualTo("some-template-id");
    }

    @Test
    void shouldReturnCorrectReferenceTemplate() {
        assertThat(generator.getReferenceTemplate())
            .isEqualTo(SpecDefRespEmailHelper.REFERENCE_TEMPLATE);
    }
}
