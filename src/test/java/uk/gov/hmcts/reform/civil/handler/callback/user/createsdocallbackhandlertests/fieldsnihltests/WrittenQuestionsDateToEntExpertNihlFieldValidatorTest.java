package uk.gov.hmcts.reform.civil.handler.callback.user.createsdocallbackhandlertests.fieldsnihltests;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.handler.callback.user.createsdocallbackhandler.fieldsnihl.FieldsNihlUtils;
import uk.gov.hmcts.reform.civil.handler.callback.user.createsdocallbackhandler.fieldsnihl.WrittenQuestionsDateToEntExpertNihlFieldValidator;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2QuestionsToEntExpert;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WrittenQuestionsDateToEntExpertNihlFieldValidatorTest {

    @Mock
    private FieldsNihlUtils fieldsNihlUtils;

    @InjectMocks
    private WrittenQuestionsDateToEntExpertNihlFieldValidator validator;

    @Test
    void shouldAddErrorWhenWrittenQuestionsDateIsInThePast() {
        CaseData caseData = CaseData.builder()
                .sdoR2QuestionsToEntExpert(SdoR2QuestionsToEntExpert.builder()
                        .sdoWrittenQuestionsDate(LocalDate.now().minusDays(1))
                        .build())
                .build();

        List<String> errors = new ArrayList<>();
        when(fieldsNihlUtils.validateFutureDate(caseData.getSdoR2QuestionsToEntExpert().getSdoWrittenQuestionsDate()))
                .thenReturn(Optional.of("Written questions date to ENT expert must be in the future"));

        validator.validate(caseData, errors);

        assertEquals(1, errors.size());
        assertEquals("Written questions date to ENT expert must be in the future", errors.get(0));
    }

    @Test
    void shouldNotAddErrorWhenWrittenQuestionsDateIsInTheFuture() {
        CaseData caseData = CaseData.builder()
                .sdoR2QuestionsToEntExpert(SdoR2QuestionsToEntExpert.builder()
                        .sdoWrittenQuestionsDate(LocalDate.now().plusDays(1))
                        .build())
                .build();

        List<String> errors = new ArrayList<>();
        when(fieldsNihlUtils.validateFutureDate(caseData.getSdoR2QuestionsToEntExpert().getSdoWrittenQuestionsDate()))
                .thenReturn(Optional.empty());

        validator.validate(caseData, errors);

        assertEquals(0, errors.size());
    }

    @Test
    void shouldNotAddErrorWhenWrittenQuestionsDateIsNull() {
        CaseData caseData = CaseData.builder()
                .sdoR2QuestionsToEntExpert(SdoR2QuestionsToEntExpert.builder()
                        .sdoWrittenQuestionsDate(null)
                        .build())
                .build();

        List<String> errors = new ArrayList<>();

        validator.validate(caseData, errors);

        assertEquals(0, errors.size());
    }
}