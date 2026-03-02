package uk.gov.hmcts.reform.hmc.model.unnotifiedhearings;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class PartiesNotified {

    private PartiesNotifiedServiceData serviceData;
}
