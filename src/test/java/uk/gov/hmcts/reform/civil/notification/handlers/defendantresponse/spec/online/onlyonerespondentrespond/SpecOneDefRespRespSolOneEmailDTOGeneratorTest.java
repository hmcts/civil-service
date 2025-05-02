package uk.gov.hmcts.reform.civil.notification.handlers.defendantresponse.spec.online.onlyonerespondentrespond;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.dq.Respondent1DQ;
import uk.gov.hmcts.reform.civil.notification.handlers.defendantresponse.spec.online.common.SpecDefRespEmailHelper;
import uk.gov.hmcts.reform.civil.service.OrganisationService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class SpecOneDefRespRespSolOneEmailDTOGeneratorTest {

    private SpecOneDefRespRespSolOneEmailDTOGenerator generator;

    @BeforeEach
    void setUp() {
        SpecDefRespEmailHelper emailHelper = mock(SpecDefRespEmailHelper.class);
        OrganisationService organisationService = mock(OrganisationService.class);
        generator = new SpecOneDefRespRespSolOneEmailDTOGenerator(emailHelper, organisationService);
    }

    @Test
    void shouldNotify_whenDQAndResponseTypeArePresent() {
        CaseData caseData = CaseData.builder()
            .respondent1DQ(Respondent1DQ.builder().build())
            .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_DEFENCE)
            .build();

        assertThat(generator.getShouldNotify(caseData)).isTrue();
    }

    @Test
    void shouldNotNotify_whenDQIsMissing() {
        CaseData caseData = CaseData.builder()
            .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_DEFENCE)
            .build();

        assertThat(generator.getShouldNotify(caseData)).isFalse();
    }

    @Test
    void shouldNotNotify_whenResponseTypeIsMissing() {
        CaseData caseData = CaseData.builder()
            .respondent1DQ(Respondent1DQ.builder().build())
            .build();

        assertThat(generator.getShouldNotify(caseData)).isFalse();
    }

    @Test
    void shouldNotNotify_whenBothDQAndResponseTypeAreMissing() {
        CaseData caseData = CaseData.builder().build();

        assertThat(generator.getShouldNotify(caseData)).isFalse();
    }
}
