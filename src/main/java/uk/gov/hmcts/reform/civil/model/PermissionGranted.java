package uk.gov.hmcts.reform.civil.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.time.LocalDate;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.api.ComplexType;

@ComplexType(name = "PermissionGrantedFields", generate = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class PermissionGranted {

    @CCD(label = "Judge name", searchable = false)
    private String permissionGrantedJudge;
    @CCD(label = "On date", hint = "For example, 16 04 2021", searchable = false)
    private LocalDate permissionGrantedDate;

    public PermissionGranted copy() {
        return new PermissionGranted()
            .setPermissionGrantedJudge(permissionGrantedJudge)
            .setPermissionGrantedDate(permissionGrantedDate);
    }
}
