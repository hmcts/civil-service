package uk.gov.hmcts.reform.civil.handler.callback.user.createsdocallbackhandlertests.fieldsnihltests;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.handler.callback.user.createsdocallbackhandler.fieldsnihl.AddendumReportDateNihlFieldValidator;
import uk.gov.hmcts.reform.civil.handler.callback.user.createsdocallbackhandler.fieldsnihl.FieldsNihlUtils;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2AddendumReport;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AddendumReportDateNihlFieldValidatorTest {

    @Mock
    private FieldsNihlUtils fieldsNihlUtils;

    @InjectMocks
    private AddendumReportDateNihlFieldValidator validator;

    @Test
    void shouldAddErrorWhenAddendumReportDateIsInThePast() {
        LocalDate pastDate = LocalDate.now().minusDays(1);
        SdoR2AddendumReport addendumReport = SdoR2AddendumReport.builder()
                .sdoAddendumReportDate(pastDate)
                .build();
        CaseData caseData = CaseData.builder()
                .sdoR2AddendumReport(addendumReport)
                .build();
        List<String> errors = new ArrayList<>();
        when(fieldsNihlUtils.validateFutureDate(pastDate)).thenReturn(Optional.of("Date must be in the future"));

        validator.validate(caseData, errors);

        assertEquals(1, errors.size());
        assertEquals("Date must be in the future", errors.get(0));
    }

    @Test
    void shouldNotAddErrorWhenAddendumReportDateIsInTheFuture() {
        LocalDate futureDate = LocalDate.now().plusDays(1);
        SdoR2AddendumReport addendumReport = SdoR2AddendumReport.builder()
                .sdoAddendumReportDate(futureDate)
                .build();
        CaseData caseData = CaseData.builder()
                .sdoR2AddendumReport(addendumReport)
                .build();
        List<String> errors = new ArrayList<>();
        when(fieldsNihlUtils.validateFutureDate(futureDate)).thenReturn(Optional.empty());

        validator.validate(caseData, errors);

        assertEquals(0, errors.size());
    }

    @Test
    void shouldNotAddErrorWhenAddendumReportIsNull() {
        CaseData caseData = CaseData.builder()
                .sdoR2AddendumReport(null)
                .build();
        List<String> errors = new ArrayList<>();

        validator.validate(caseData, errors);

        assertEquals(0, errors.size());
    }

    @Test
    void shouldNotAddErrorWhenAddendumReportDateIsNull() {
        SdoR2AddendumReport addendumReport = SdoR2AddendumReport.builder()
                .sdoAddendumReportDate(null)
                .build();
        CaseData caseData = CaseData.builder()
                .sdoR2AddendumReport(addendumReport)
                .build();
        List<String> errors = new ArrayList<>();

        validator.validate(caseData, errors);

        assertEquals(0, errors.size());
    }
}