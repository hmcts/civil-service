package uk.gov.hmcts.reform.cmc.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.minidev.json.annotate.JsonIgnore;

import java.time.LocalDate;

import static uk.gov.hmcts.reform.civil.model.citizenui.DtoFieldFormat.DATE_FORMAT;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PaymentIntention {

    private PaymentOption paymentOption;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DATE_FORMAT)
    @JsonSerialize(using = LocalDateSerializer.class)
    @JsonDeserialize(using = LocalDateDeserializer.class)
    private LocalDate paymentDate;

    @JsonIgnore
    public boolean isPayImmediately() {
        return paymentOption == PaymentOption.IMMEDIATELY;
    }

    @JsonIgnore
    public boolean isPayByDate() {
        return paymentOption == PaymentOption.BY_SPECIFIED_DATE;
    }

    @JsonIgnore
    public boolean isPayByInstallments() {
        return paymentOption == PaymentOption.INSTALMENTS;
    }

    public boolean hasAlreadyPaid() {
        return paymentDate != null && paymentDate.isBefore(LocalDate.now());
    }

}
