package uk.gov.hmcts.reform.civil.handler.event;

    import lombok.RequiredArgsConstructor;
    import lombok.extern.slf4j.Slf4j;
    import org.springframework.context.event.EventListener;
    import org.springframework.stereotype.Service;
    import uk.gov.hmcts.reform.civil.event.BundleCreationTriggerEvent;

@Slf4j
@Service
@RequiredArgsConstructor
public class BundleCreationTriggerEventHandler {

    @EventListener
    public void sendBundleCreationTrigger(BundleCreationTriggerEvent event) {

    }
}
