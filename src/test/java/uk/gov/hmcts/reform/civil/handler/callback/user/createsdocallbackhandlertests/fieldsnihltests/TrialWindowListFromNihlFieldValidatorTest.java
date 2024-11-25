package uk.gov.hmcts.reform.civil.handler.callback.user.createsdocallbackhandlertests.fieldsnihltests;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.handler.callback.user.createsdocallbackhandler.fieldsnihl.FieldsNihlUtils;
import uk.gov.hmcts.reform.civil.handler.callback.user.createsdocallbackhandler.fieldsnihl.TrialWindowListFromNihlFieldValidator;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2Trial;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2TrialWindow;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TrialWindowListFromNihlFieldValidatorTest {

    @Mock
    private FieldsNihlUtils fieldsNihlUtils;

    @InjectMocks
    private TrialWindowListFromNihlFieldValidator validator;

    @Test
    void shouldAddErrorWhenDateIsInThePast() {
        CaseData caseData = CaseData.builder()
                .sdoR2Trial(SdoR2Trial.builder()
                        .sdoR2TrialWindow(SdoR2TrialWindow.builder()
                                .listFrom(LocalDate.now().minusDays(1))
                                .build())
                        .build())
                .build();

        List<String> errors = new ArrayList<>();
        when(fieldsNihlUtils.validateFutureDate(caseData.getSdoR2Trial().getSdoR2TrialWindow().getListFrom()))
                .thenReturn(java.util.Optional.of("Date must be in the future"));

        validator.validate(caseData, errors);

        assertEquals(1, errors.size());
        assertEquals("Date must be in the future", errors.get(0));
    }

    @Test
    void shouldNotAddErrorWhenDateIsInTheFuture() {
        CaseData caseData = CaseData.builder()
                .sdoR2Trial(SdoR2Trial.builder()
                        .sdoR2TrialWindow(SdoR2TrialWindow.builder()
                                .listFrom(LocalDate.now().plusDays(1))
                                .build())
                        .build())
                .build();

        List<String> errors = new ArrayList<>();
        when(fieldsNihlUtils.validateFutureDate(caseData.getSdoR2Trial().getSdoR2TrialWindow().getListFrom()))
                .thenReturn(java.util.Optional.empty());

        validator.validate(caseData, errors);

        assertEquals(0, errors.size());
    }

    @Test
    void shouldNotAddErrorWhenDateIsNull() {
        CaseData caseData = CaseData.builder()
                .sdoR2Trial(SdoR2Trial.builder()
                        .sdoR2TrialWindow(SdoR2TrialWindow.builder()
                                .listFrom(null)
                                .build())
                        .build())
                .build();

        List<String> errors = new ArrayList<>();

        validator.validate(caseData, errors);

        assertEquals(0, errors.size());
    }
}