package uk.gov.hmcts.reform.civil.handler.callback.user.createsdocallbackhandlertests.generatesdoordertests;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.handler.callback.user.createsdocallbackhandler.generatesdoorder.WitnessStatementsValidator;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2SmallClaimsImpNotes;
import uk.gov.hmcts.reform.civil.model.sdo.SmallClaimsWitnessStatement;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.constants.CreateSDOText.ERROR_MESSAGE_NUMBER_CANNOT_BE_LESS_THAN_ZERO;

@ExtendWith(MockitoExtension.class)
class WitnessStatementsValidatorTest {

    @Mock
    private FeatureToggleService featureToggleService;

    @InjectMocks
    private WitnessStatementsValidator validator;

    @Test
    void shouldValidateSmallClaimsWitnessStatement() {
        CaseData caseData = CaseData.builder()
                .smallClaimsWitnessStatement(SmallClaimsWitnessStatement.builder()
                        .input2("1")
                        .input3("2")
                        .build())
                .build();

        List<String> errors = new ArrayList<>();
        validator.validate(caseData, errors);

        assertThat(errors).isEmpty();
    }

    @Test
    void shouldAddErrorForNegativeWitnessValues() {
        CaseData caseData = CaseData.builder()
                .smallClaimsWitnessStatement(SmallClaimsWitnessStatement.builder()
                        .input2("-1")
                        .input3("-2")
                        .build())
                .build();

        List<String> errors = new ArrayList<>();
        validator.validate(caseData, errors);

        assertThat(errors).contains(ERROR_MESSAGE_NUMBER_CANNOT_BE_LESS_THAN_ZERO);
    }

    @Test
    void shouldValidateDRHFields() {
        when(featureToggleService.isSdoR2Enabled()).thenReturn(true);
        CaseData caseData = CaseData.builder()
                .sdoR2SmallClaimsImpNotes(SdoR2SmallClaimsImpNotes.builder()
                        .date(LocalDate.now().plusDays(1))
                        .build())
                .build();

        List<String> errors = new ArrayList<>();
        validator.validate(caseData, errors);

        assertThat(errors).isEmpty();
    }
}