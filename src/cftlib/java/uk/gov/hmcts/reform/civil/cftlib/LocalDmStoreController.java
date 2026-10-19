package uk.gov.hmcts.reform.civil.cftlib;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * An in-memory dm-store for cftlib's ccd-case-document-am-api, which bootWithCCD points here
 * (DM_STORE_BASE_URL=http://localhost:4000). It answers the calls CDAM makes: upload, metadata and binary reads, and
 * the metadata PATCH that attaches documents to a case.
 */
@Slf4j
@RestController
public class LocalDmStoreController {

    static final String PATH = "/documents";

    private record StoredDocument(String fileName, String mimeType, String classification,
                                  Map<String, String> metadata, byte[] content, Instant createdOn) {
    }

    private final Map<String, StoredDocument> documents = new ConcurrentHashMap<>();

    @PostMapping(value = PATH, produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> upload(@RequestParam("classification") String classification,
                                      @RequestParam Map<String, String> params,
                                      @RequestParam("files") List<MultipartFile> files) throws IOException {
        Map<String, String> metadata = new LinkedHashMap<>();
        params.forEach((name, value) -> {
            if (name.startsWith("metadata[") && name.endsWith("]")) {
                metadata.put(name.substring("metadata[".length(), name.length() - 1), value);
            }
        });

        List<Map<String, Object>> uploaded = new ArrayList<>();
        for (MultipartFile file : files) {
            String id = UUID.randomUUID().toString();
            documents.put(id, new StoredDocument(file.getOriginalFilename(), file.getContentType(), classification,
                                                 metadata, file.getBytes(), Instant.now()));
            log.info("Local dm-store stored {} as {}", file.getOriginalFilename(), id);
            uploaded.add(hal(id, documents.get(id)));
        }
        return Map.of("_embedded", Map.of("documents", uploaded));
    }

    @GetMapping(value = PATH + "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> metadata(@PathVariable String id) {
        StoredDocument document = documents.get(id);
        return document == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(hal(id, document));
    }

    @GetMapping(PATH + "/{id}/binary")
    public ResponseEntity<byte[]> binary(@PathVariable String id) {
        StoredDocument document = documents.get(id);
        if (document == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_TYPE, document.mimeType())
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + document.fileName())
            .body(document.content());
    }

    @PatchMapping(value = PATH, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> attach(@RequestBody Map<String, Object> body) {
        if (body.get("documents") instanceof List<?> updates) {
            for (Object update : updates) {
                if (update instanceof Map<?, ?> entry && entry.get("metadata") instanceof Map<?, ?> metadata) {
                    Object id = entry.get("documentId") != null ? entry.get("documentId") : entry.get("document_id");
                    documents.computeIfPresent(String.valueOf(id), (key, document) -> {
                        Map<String, String> merged = new LinkedHashMap<>(document.metadata());
                        metadata.forEach((name, value) -> merged.put(String.valueOf(name), String.valueOf(value)));
                        log.info("Local dm-store attached {}: {}", key, merged);
                        return new StoredDocument(document.fileName(), document.mimeType(), document.classification(),
                                                  merged, document.content(), document.createdOn());
                    });
                }
            }
        }
        return ResponseEntity.ok().build();
    }

    private static Map<String, Object> hal(String id, StoredDocument document) {
        String self = CivilCftlibConfig.SERVICE_URL + PATH + "/" + id;
        Map<String, Object> hal = new LinkedHashMap<>();
        hal.put("classification", document.classification());
        hal.put("size", document.content().length);
        hal.put("mimeType", document.mimeType());
        hal.put("originalDocumentName", document.fileName());
        hal.put("createdOn", document.createdOn().toString());
        hal.put("ttl", document.createdOn().plus(1, ChronoUnit.DAYS).toString());
        hal.put("metadata", document.metadata());
        hal.put("_links", Map.of("self", Map.of("href", self), "binary", Map.of("href", self + "/binary")));
        return hal;
    }
}
