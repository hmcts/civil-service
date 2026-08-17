package uk.gov.hmcts.reform.civil.model.mediation;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.api.ComplexType;

@ComplexType(name = "NonAttendanceMediationStatement", generate = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MediationNonAttendanceStatement {

    @CCD(label = "Your name", searchable = false)
    private String yourName;
    @CCD(label = "Date statement was written", hint = "For example, 27 3 2007", searchable = false)
    private LocalDate documentDate;
    @CCD(
            label = "Upload a file",
            hint = "Each document must be less than 100MB. You can upload the following file types: Word, Excel, PowerPoint, PDF, RTF, TXT, CSV, JPG, JPEG, PNG, BMG, TIF, TIFF",
            regex = ".pdf,.txt,.doc,.dot,.docx,.rtf,.xls,.xlt,.xla,.xlsx,.xltx,.xlsb,.ppt,.pot,.pps,.ppa,.pptx,.potx,.ppsx,.jpg,.jpeg,.bmp,.tif,.tiff,.png",
            searchable = false
    )
    private Document document;
    @CCD(label = "Document Uploaded DateTime", showCondition = "document = \"DO NOT SHOW IN UI\"", searchable = false)
    private LocalDateTime documentUploadedDatetime = LocalDateTime.now(ZoneId.of("Europe/London"));
}
