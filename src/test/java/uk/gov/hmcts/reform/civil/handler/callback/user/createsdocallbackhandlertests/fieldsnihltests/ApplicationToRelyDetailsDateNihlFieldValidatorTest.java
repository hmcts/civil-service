package uk.gov.hmcts.reform.civil.handler.callback.user.createsdocallbackhandlertests.fieldsnihltests;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.handler.callback.user.createsdocallbackhandler.fieldsnihl.ApplicationToRelyDetailsDateNihlFieldValidator;
import uk.gov.hmcts.reform.civil.handler.callback.user.createsdocallbackhandler.fieldsnihl.FieldsNihlUtils;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2ApplicationToRelyOnFurther;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2ApplicationToRelyOnFurtherDetails;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2QuestionsClaimantExpert;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ApplicationToRelyDetailsDateNihlFieldValidatorTest {

    @Mock
    private FieldsNihlUtils fieldsNihlUtils;

    @InjectMocks
    private ApplicationToRelyDetailsDateNihlFieldValidator validator;

    @Test
    void shouldAddErrorWhenApplicationToRelyDetailsDateIsInThePast() {
        CaseData caseData = CaseData.builder()
                .sdoR2QuestionsClaimantExpert(SdoR2QuestionsClaimantExpert.builder()
                        .sdoApplicationToRelyOnFurther(SdoR2ApplicationToRelyOnFurther.builder()
                                .applicationToRelyOnFurtherDetails(SdoR2ApplicationToRelyOnFurtherDetails.builder()
                                        .applicationToRelyDetailsDate(LocalDate.now().minusDays(1))
                                        .build())
                                .build())
                        .build())
                .build();

        List<String> errors = new ArrayList<>();
        when(fieldsNihlUtils.validateFutureDate(caseData.getSdoR2QuestionsClaimantExpert()
                .getSdoApplicationToRelyOnFurther()
                .getApplicationToRelyOnFurtherDetails()
                .getApplicationToRelyDetailsDate()))
                .thenReturn(Optional.of("Application to rely details date must be in the future"));

        validator.validate(caseData, errors);

        assertEquals(1, errors.size());
        assertEquals("Application to rely details date must be in the future", errors.get(0));
    }

    @Test
    void shouldNotAddErrorWhenApplicationToRelyDetailsDateIsInTheFuture() {
        CaseData caseData = CaseData.builder()
                .sdoR2QuestionsClaimantExpert(SdoR2QuestionsClaimantExpert.builder()
                        .sdoApplicationToRelyOnFurther(SdoR2ApplicationToRelyOnFurther.builder()
                                .applicationToRelyOnFurtherDetails(SdoR2ApplicationToRelyOnFurtherDetails.builder()
                                        .applicationToRelyDetailsDate(LocalDate.now().plusDays(1))
                                        .build())
                                .build())
                        .build())
                .build();

        List<String> errors = new ArrayList<>();
        when(fieldsNihlUtils.validateFutureDate(caseData.getSdoR2QuestionsClaimantExpert()
                .getSdoApplicationToRelyOnFurther()
                .getApplicationToRelyOnFurtherDetails()
                .getApplicationToRelyDetailsDate()))
                .thenReturn(Optional.empty());

        validator.validate(caseData, errors);

        assertEquals(0, errors.size());
    }

    @Test
    void shouldNotAddErrorWhenApplicationToRelyDetailsDateIsNull() {
        CaseData caseData = CaseData.builder()
                .sdoR2QuestionsClaimantExpert(SdoR2QuestionsClaimantExpert.builder()
                        .sdoApplicationToRelyOnFurther(SdoR2ApplicationToRelyOnFurther.builder()
                                .applicationToRelyOnFurtherDetails(SdoR2ApplicationToRelyOnFurtherDetails.builder()
                                        .applicationToRelyDetailsDate(null)
                                        .build())
                                .build())
                        .build())
                .build();

        List<String> errors = new ArrayList<>();

        validator.validate(caseData, errors);

        assertEquals(0, errors.size());
    }
}