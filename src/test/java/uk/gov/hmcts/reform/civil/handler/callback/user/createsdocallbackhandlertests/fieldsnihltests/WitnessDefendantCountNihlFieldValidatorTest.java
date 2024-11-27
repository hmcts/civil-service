package uk.gov.hmcts.reform.civil.handler.callback.user.createsdocallbackhandlertests.fieldsnihltests;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.handler.callback.user.createsdocallbackhandler.fieldsnihl.FieldsNihlUtils;
import uk.gov.hmcts.reform.civil.handler.callback.user.createsdocallbackhandler.fieldsnihl.WitnessDefendantCountNihlFieldValidator;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2RestrictNoOfWitnessDetails;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2RestrictWitness;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2WitnessOfFact;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WitnessDefendantCountNihlFieldValidatorTest {

    @Mock
    private FieldsNihlUtils fieldsNihlUtils;

    @InjectMocks
    private WitnessDefendantCountNihlFieldValidator validator;

    @Test
    void shouldAddErrorWhenWitnessDefendantCountIsNegative() {
        CaseData caseData = CaseData.builder()
                .sdoR2WitnessesOfFact(SdoR2WitnessOfFact.builder()
                        .sdoR2RestrictWitness(SdoR2RestrictWitness.builder()
                                .restrictNoOfWitnessDetails(SdoR2RestrictNoOfWitnessDetails.builder()
                                        .noOfWitnessDefendant(-1)
                                        .build())
                                .build())
                        .build())
                .build();

        List<String> errors = new ArrayList<>();
        when(fieldsNihlUtils.validateGreaterOrEqualZero(caseData.getSdoR2WitnessesOfFact()
                .getSdoR2RestrictWitness()
                .getRestrictNoOfWitnessDetails()
                .getNoOfWitnessDefendant()))
                .thenReturn(Optional.of("Number of witness defendant must be greater or equal to zero"));

        validator.validate(caseData, errors);

        assertEquals(1, errors.size());
        assertEquals("Number of witness defendant must be greater or equal to zero", errors.get(0));
    }

    @Test
    void shouldNotAddErrorWhenWitnessDefendantCountIsZero() {
        CaseData caseData = CaseData.builder()
                .sdoR2WitnessesOfFact(SdoR2WitnessOfFact.builder()
                        .sdoR2RestrictWitness(SdoR2RestrictWitness.builder()
                                .restrictNoOfWitnessDetails(SdoR2RestrictNoOfWitnessDetails.builder()
                                        .noOfWitnessDefendant(0)
                                        .build())
                                .build())
                        .build())
                .build();

        List<String> errors = new ArrayList<>();
        when(fieldsNihlUtils.validateGreaterOrEqualZero(caseData.getSdoR2WitnessesOfFact()
                .getSdoR2RestrictWitness()
                .getRestrictNoOfWitnessDetails()
                .getNoOfWitnessDefendant()))
                .thenReturn(Optional.empty());

        validator.validate(caseData, errors);

        assertEquals(0, errors.size());
    }

    @Test
    void shouldNotAddErrorWhenWitnessDefendantCountIsPositive() {
        CaseData caseData = CaseData.builder()
                .sdoR2WitnessesOfFact(SdoR2WitnessOfFact.builder()
                        .sdoR2RestrictWitness(SdoR2RestrictWitness.builder()
                                .restrictNoOfWitnessDetails(SdoR2RestrictNoOfWitnessDetails.builder()
                                        .noOfWitnessDefendant(1)
                                        .build())
                                .build())
                        .build())
                .build();

        List<String> errors = new ArrayList<>();
        when(fieldsNihlUtils.validateGreaterOrEqualZero(caseData.getSdoR2WitnessesOfFact()
                .getSdoR2RestrictWitness()
                .getRestrictNoOfWitnessDetails()
                .getNoOfWitnessDefendant()))
                .thenReturn(Optional.empty());

        validator.validate(caseData, errors);

        assertEquals(0, errors.size());
    }

    @Test
    void shouldNotAddErrorWhenWitnessDefendantCountIsNull() {
        CaseData caseData = CaseData.builder()
                .sdoR2WitnessesOfFact(SdoR2WitnessOfFact.builder()
                        .sdoR2RestrictWitness(SdoR2RestrictWitness.builder()
                                .restrictNoOfWitnessDetails(SdoR2RestrictNoOfWitnessDetails.builder()
                                        .noOfWitnessDefendant(null)
                                        .build())
                                .build())
                        .build())
                .build();

        List<String> errors = new ArrayList<>();

        validator.validate(caseData, errors);

        assertEquals(0, errors.size());
    }
}