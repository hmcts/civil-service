package uk.gov.hmcts.reform.civil.model.mediation;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.civil.model.documents.Document;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class MediationNonAttendanceStatement {

    private String yourName;
    private LocalDate documentDate;
    private Document document;
    @Builder.Default
    private LocalDateTime documentUploadedDatetime = LocalDateTime.now(ZoneId.of("Europe/London"));
}
