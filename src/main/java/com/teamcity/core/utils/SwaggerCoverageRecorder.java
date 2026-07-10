package com.teamcity.core.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.filter.Filter;
import io.restassured.filter.FilterContext;
import io.restassured.response.Response;
import io.restassured.specification.FilterableRequestSpecification;
import io.restassured.specification.FilterableResponseSpecification;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
public class SwaggerCoverageRecorder implements Filter {

    private static final ObjectMapper mapper = new ObjectMapper();
    private static final File RESULTS_DIR = new File("target/swagger-coverage");

    static {
        if (!RESULTS_DIR.exists()) {
            RESULTS_DIR.mkdirs();
        }
    }

    @Override
    public Response filter(FilterableRequestSpecification requestSpec,
                           FilterableResponseSpecification responseSpec,
                           FilterContext ctx) {
        Response response = ctx.next(requestSpec, responseSpec);

        try {
            Map<String, Object> data = new HashMap<>();
            data.put("path", requestSpec.getURI().replaceAll("^.*?/app/rest", "/app/rest"));
            data.put("method", requestSpec.getMethod());
            data.put("statusCode", response.getStatusCode());
            data.put("timestamp", System.currentTimeMillis());

            String fileName = "coverage-" + UUID.randomUUID().toString() + ".json";
            File file = new File(RESULTS_DIR, fileName);

            mapper.writeValue(file, data);
            log.debug("📝 Recorded: {} {}", requestSpec.getMethod(), requestSpec.getURI());

        } catch (Exception e) {
            log.warn("Failed to record coverage data: {}", e.getMessage());
        }

        return response;
    }
}