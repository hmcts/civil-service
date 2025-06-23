package uk.gov.hmcts.reform.civil.notification.handlers.defendantresponse.spec.common;

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

class SpecDefRespRespSolTwoEmailDTOGeneratorTest {

    private SpecDefRespEmailHelper specDefRespEmailHelper;
    private OrganisationService organisationService;
    private SpecDefRespRespSolTwoEmailDTOGenerator generator;

    @BeforeEach
    void setUp() {
        specDefRespEmailHelper = mock(SpecDefRespEmailHelper.class);
        organisationService = mock(OrganisationService.class);
        generator = new SpecDefRespRespSolTwoEmailDTOGenerator(specDefRespEmailHelper, organisationService);
    }

    @Test
    void shouldAddCustomPropertiesWithLegalOrgNameAndRespondent2Name() {
        Party respondent2 = Party.builder().type(Party.Type.INDIVIDUAL)
            .individualFirstName("Joe")
            .individualLastName("Brown").build();
        CaseData caseData = CaseData.builder()
            .respondent2(respondent2)
            .build();

        try (var utilsMock = mockStatic(NotificationUtils.class)) {

            utilsMock.when(() -> getLegalOrganizationNameForRespondent(caseData, false, organisationService))
                .thenReturn("Defendant2 Org");

            Map<String, String> result = generator.addCustomProperties(new HashMap<>(), caseData);

            assertThat(result)
                .containsEntry(RESPONDENT_NAME, "Joe Brown")
                .containsEntry(CLAIM_LEGAL_ORG_NAME_SPEC, "Defendant2 Org");
        }
    }

    @Test
    void shouldReturnCorrectTemplateIdFromHelper() {
        CaseData caseData = mock(CaseData.class);
        when(specDefRespEmailHelper.getRespondentTemplate(caseData)).thenReturn("template-id-456");

        String templateId = generator.getEmailTemplateId(caseData);

        assertThat(templateId).isEqualTo("template-id-456");
    }

    @Test
    void shouldReturnReferenceTemplateConstant() {
        assertThat(generator.getReferenceTemplate())
            .isEqualTo(SpecDefRespEmailHelper.REFERENCE_TEMPLATE);
    }
}
