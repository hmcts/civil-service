package uk.gov.hmcts.reform.civil.model.mediation;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.civil.enums.mediation.MediationAppointmentTimeWindow;

import java.time.LocalDate;

@Data
@NoArgsConstructor
public class MediationAppointment {

    private LocalDate appointmentDate;
    private MediationAppointmentTimeWindow appointmentTimeWindow;

    @JsonCreator
    MediationAppointment(@JsonProperty("appointmentDate") LocalDate appointmentDate,
                         @JsonProperty("appointmentTimeWindow") MediationAppointmentTimeWindow appointmentTimeWindow) {
        this.appointmentDate = appointmentDate;
        this.appointmentTimeWindow = appointmentTimeWindow;
    }
}
