package uk.gov.hmcts.reform.civil.scheduler.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
public class CivilSchedulerRepository {

    private final Map<String, CivilScheduler> schedulers;
    private final TaskExecutor taskExecutor;

    public CivilSchedulerRepository(List<CivilScheduler> schedulers,
                                    @Qualifier("asyncHandlerExecutor") TaskExecutor taskExecutor) {
        this.schedulers = schedulers.stream()
            .collect(Collectors.toMap(
                CivilScheduler::getName,
                Function.identity(),
                (existing, replacement) -> {
                    log.warn("Duplicate scheduler name found: {}. Keeping existing.", existing.getName());
                    return existing;
                }
            ));
        this.taskExecutor = taskExecutor;
    }

    public boolean runScheduler(String name) {
        return getScheduler(name)
            .map(scheduler -> {
                log.info("Triggering scheduler: {}", name);
                taskExecutor.execute(scheduler::runScheduledTask);
                return true;
            })
            .orElseGet(() -> {
                log.warn("Scheduler not found: {}", name);
                return false;
            });
    }

    public List<String> getSchedulerNames() {
        return List.copyOf(schedulers.keySet());
    }

    private Optional<CivilScheduler> getScheduler(String name) {
        return Optional.ofNullable(schedulers.get(name));
    }
}
