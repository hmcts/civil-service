package uk.gov.hmcts.civil.cftlib;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.rse.ccd.lib.ControlPlane;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static java.lang.Boolean.parseBoolean;

@Slf4j
@Component
public class AdditionalComposeServiceRunner {

    public static final String BASE_COMPOSE_PATH = "./src/cftlib/resources/docker/";

    @PostConstruct
    public void startComposeServices() throws IOException, InterruptedException {
        ControlPlane.waitForDB();
        ControlPlane.waitForAuthServer();

        startAdditionalServices(System.getenv("ADDITIONAL_COMPOSE_FILES"), "additional");
        if (parseBoolean(System.getenv("ENABLE_WORK_ALLOCATION"))) {
            startAdditionalServices(System.getenv("ADDITIONAL_COMPOSE_FILES_WA"), "work allocation");
        }
    }

    private void startAdditionalServices(String serviceList, String name) throws IOException, InterruptedException {
        log.info("Starting {} services...", name);

        String[] additionalFiles = Optional.ofNullable(serviceList)
            .map(files -> files.split(","))
            .orElse(new String[] {""});

        if (additionalFiles[0].isBlank()) {
            log.info("No {} services requested during startup", name);
            return;
        }

        for (String additionalService : additionalFiles) {
            String path = BASE_COMPOSE_PATH + additionalService;

            ProcessBuilder processBuilder = new ProcessBuilder(buildComposeCommand(path))
                .inheritIO();

            Process process = processBuilder.start();

            int code = process.waitFor();

            if (code != 0) {
                log.error("****** Failed to start {} services in {} ******", name, additionalService);
                log.info("Exit value: {}", code);
            } else {
                log.info("Successfully started {} services in {}", name, additionalService);
            }
        }
    }

    private List<String> buildComposeCommand(String path) {

        List<String> command = new ArrayList<>(Arrays.asList("docker", "compose", "-f", path, "up", "-d"));

        if (parseBoolean(System.getenv("FORCE_RECREATE_ADDITIONAL_CONTAINERS"))) {
            command.add("--force-recreate");
        }

        return command;
    }
}
