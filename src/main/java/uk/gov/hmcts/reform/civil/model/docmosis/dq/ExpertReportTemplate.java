package uk.gov.hmcts.reform.civil.model.docmosis.dq;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.civil.model.citizenui.ExpertReportLiP;

import java.time.LocalDate;

@Data
@Builder
public class ExpertReportTemplate {

    private String expertName;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd MMMM yyyy")
    private LocalDate reportDate;

    @JsonIgnore
    public static ExpertReportTemplate toExpertReportTemplate(ExpertReportLiP details) {
        return ExpertReportTemplate.builder()
            .expertName(details.getExpertName())
            .reportDate(details.getReportDate())
            .build();
    }
}
