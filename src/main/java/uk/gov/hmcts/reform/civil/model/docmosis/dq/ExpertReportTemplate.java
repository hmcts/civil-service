package uk.gov.hmcts.reform.civil.model.docmosis.dq;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import uk.gov.hmcts.reform.civil.model.citizenui.ExpertReportLiP;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class ExpertReportTemplate {

    private String expertName;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd MMMM yyyy")
    private LocalDate reportDate;

    @JsonIgnore
    public static ExpertReportTemplate toExpertReportTemplate(ExpertReportLiP details) {
        return new ExpertReportTemplate()
            .setExpertName(details.getExpertName())
            .setReportDate(details.getReportDate());
    }
}
