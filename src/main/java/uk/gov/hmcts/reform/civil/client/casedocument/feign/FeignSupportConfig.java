package uk.gov.hmcts.reform.civil.client.casedocument.feign;

import feign.codec.Decoder;
import feign.codec.Encoder;
import feign.form.spring.SpringFormEncoder;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.cloud.openfeign.support.FeignHttpMessageConverters;
import org.springframework.cloud.openfeign.support.ResponseEntityDecoder;
import org.springframework.cloud.openfeign.support.SpringDecoder;
import org.springframework.cloud.openfeign.support.SpringEncoder;
import org.springframework.context.annotation.Bean;

public class FeignSupportConfig {

    @Bean
    public Encoder multipartFormEncoder(ObjectProvider<FeignHttpMessageConverters> messageConverters) {
        return new SpringFormEncoder(new SpringEncoder(messageConverters));
    }

    @Bean
    public Decoder customDecoder(ObjectProvider<FeignHttpMessageConverters> messageConverters) {
        Decoder decoder = (response, type) -> new SpringDecoder(messageConverters).decode(response, type);
        return new ResponseEntityDecoder(decoder);
    }
}
