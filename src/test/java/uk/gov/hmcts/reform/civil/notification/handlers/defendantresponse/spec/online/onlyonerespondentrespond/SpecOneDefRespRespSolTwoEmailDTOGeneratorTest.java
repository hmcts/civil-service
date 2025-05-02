package uk.gov.hmcts.reform.civil.notification.handlers.defendantresponse.spec.online.onlyonerespondentrespond;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.dq.Respondent2DQ;
import uk.gov.hmcts.reform.civil.notification.handlers.defendantresponse.spec.online.common.SpecDefRespEmailHelper;
import uk.gov.hmcts.reform.civil.service.OrganisationService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class SpecOneDefRespRespSolTwoEmailDTOGeneratorTest {

    private SpecOneDefRespRespSolTwoEmailDTOGenerator generator;

    @BeforeEach
    void setUp() {
        SpecDefRespEmailHelper emailHelper = mock(SpecDefRespEmailHelper.class);
        OrganisationService organisationService = mock(OrganisationService.class);
        generator = new SpecOneDefRespRespSolTwoEmailDTOGenerator(emailHelper, organisationService);
    }

    @Test
    void shouldNotify_whenDQAndResponseTypeArePresent() {
        CaseData caseData = CaseData.builder()
            .respondent2DQ(Respondent2DQ.builder().build())
            .respondent2ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_DEFENCE)
            .build();

        assertThat(generator.getShouldNotify(caseData)).isTrue();
    }

    @Test
    void shouldNotNotify_whenDQIsMissing() {
        CaseData caseData = CaseData.builder()
            .respondent2ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_DEFENCE)
            .build();

        assertThat(generator.getShouldNotify(caseData)).isFalse();
    }

    @Test
    void shouldNotNotify_whenResponseTypeIsMissing() {
        CaseData caseData = CaseData.builder()
            .respondent2DQ(Respondent2DQ.builder().build())
            .build();

        assertThat(generator.getShouldNotify(caseData)).isFalse();
    }

    @Test
    void shouldNotNotify_whenBothDQAndResponseTypeAreMissing() {
        CaseData caseData = CaseData.builder().build();

        assertThat(generator.getShouldNotify(caseData)).isFalse();
    }
}
