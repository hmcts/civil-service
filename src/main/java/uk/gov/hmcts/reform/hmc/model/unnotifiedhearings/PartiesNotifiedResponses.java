package uk.gov.hmcts.reform.hmc.model.unnotifiedhearings;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class PartiesNotifiedResponses {

    private String hearingID;

    private List<PartiesNotifiedResponse> responses;

}

