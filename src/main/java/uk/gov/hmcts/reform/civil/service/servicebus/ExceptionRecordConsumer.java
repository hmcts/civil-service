package uk.gov.hmcts.reform.civil.service.servicebus;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class ExceptionRecordConsumer implements Runnable {

    protected static final Map<Integer, Integer> RETRY_COUNT_TO_DELAY_MAP = new ConcurrentHashMap<>();

    static {
        RETRY_COUNT_TO_DELAY_MAP.put(1, 5);
        RETRY_COUNT_TO_DELAY_MAP.put(2, 15);
        RETRY_COUNT_TO_DELAY_MAP.put(3, 30);
        RETRY_COUNT_TO_DELAY_MAP.put(4, 60);
        RETRY_COUNT_TO_DELAY_MAP.put(5, 300);
        RETRY_COUNT_TO_DELAY_MAP.put(6, 900);
        RETRY_COUNT_TO_DELAY_MAP.put(7, 1800);
        RETRY_COUNT_TO_DELAY_MAP.put(8, 3600);
    }
    @Override
    public void run() {

    }
}
