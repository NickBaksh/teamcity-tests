package com.teamcity.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.teamcity.core.endpoints.Endpoint;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
public class SwaggerCoverageGenerator {

    private static final String RESULTS_DIR = "target/swagger-coverage";
    private static final String REPORT_DIR = "target/swagger-coverage-report";

    public static void main(String[] args) {
        generateReport();
    }

    public static void generateReport() {
        try {
            File resultsDir = new File(RESULTS_DIR);
            if (!resultsDir.exists() || resultsDir.listFiles() == null || resultsDir.listFiles().length == 0) {
                log.warn("⚠️ No swagger coverage results found. Run tests first!");
                log.info("Run: mvn clean test");
                return;
            }

            log.info("📊 Generating Swagger Coverage report...");

            // 1. Собираем все эндпоинты из Enum
            Map<String, Set<String>> allEndpoints = getAllEndpointsFromEnum();
            log.info("📋 Total endpoints from Enum: {}", allEndpoints.size());

            // 2. Собираем реально вызванные эндпоинты из тестов
            Map<String, Set<String>> coveredEndpoints = getCoveredEndpoints(resultsDir);
            log.info("✅ Covered endpoints: {}", coveredEndpoints.size());

            // 3. Сравниваем и генерируем отчет
            Map<String, CoverageInfo> coverageMap = new LinkedHashMap<>();

            for (Map.Entry<String, Set<String>> entry : allEndpoints.entrySet()) {
                String path = entry.getKey();
                Set<String> allMethods = entry.getValue();
                Set<String> coveredMethods = coveredEndpoints.getOrDefault(path, new HashSet<>());

                CoverageInfo info = new CoverageInfo(path);
                for (String method : allMethods) {
                    info.addMethod(method, coveredMethods.contains(method));
                }
                coverageMap.put(path, info);
            }

            generateHtmlReport(coverageMap);

            log.info("✅ Swagger Coverage report generated successfully!");
            log.info("📊 Open the report: file://{}/index.html",
                    Paths.get(REPORT_DIR).toAbsolutePath());

            openReport();

        } catch (Exception e) {
            log.error("❌ Failed to generate Swagger Coverage report", e);
            e.printStackTrace();
        }
    }

    private static Map<String, Set<String>> getAllEndpointsFromEnum() {
        Map<String, Set<String>> endpoints = new LinkedHashMap<>();

        for (Endpoint endpoint : Endpoint.values()) {
            String path = endpoint.getPath();
            // Определяем возможные HTTP методы для каждого эндпоинта
            Set<String> methods = getPossibleMethods(endpoint);
            endpoints.put(path, methods);
        }

        return endpoints;
    }

    private static Set<String> getPossibleMethods(Endpoint endpoint) {
        Set<String> methods = new HashSet<>();
        String path = endpoint.getPath();
        String name = endpoint.name();

        // ===== BUILD TYPES =====
        if (path.equals("/app/rest/buildTypes")) {
            methods.add("GET");
            methods.add("POST");
            return methods;
        }
        if (path.matches("/app/rest/buildTypes/\\{.*\\}")) {
            methods.add("GET");
            methods.add("PUT");
            methods.add("DELETE");
            return methods;
        }
        if (path.contains("/buildTypes/") && path.contains("/name")) {
            methods.add("GET");
            methods.add("PUT");
            return methods;
        }
        if (path.contains("/buildTypes/") && path.contains("/paused")) {
            methods.add("GET");
            methods.add("PUT");
            return methods;
        }
        if (path.contains("/buildTypes/") && (path.contains("/builds") || path.contains("/branches"))) {
            methods.add("GET");
            return methods;
        }

        // ===== BUILDS =====
        if (path.equals("/app/rest/builds")) {
            methods.add("GET");
            return methods;
        }
        if (path.matches("/app/rest/builds/\\{.*\\}")) {
            methods.add("GET");
            methods.add("DELETE");
            return methods;
        }
        if (path.contains("/builds/") && path.contains("/status")) {
            methods.add("GET");
            return methods;
        }
        if (path.contains("/builds/") && path.contains("/comment")) {
            methods.add("GET");
            methods.add("PUT");
            return methods;
        }

        // ===== BUILD QUEUE =====
        if (path.equals("/app/rest/buildQueue")) {
            methods.add("GET");
            methods.add("POST");
            return methods;
        }
        if (path.matches("/app/rest/buildQueue/\\{.*\\}")) {
            methods.add("GET");
            methods.add("DELETE");
            return methods;
        }

        // ===== PROJECTS =====
        if (path.equals("/app/rest/projects")) {
            methods.add("GET");
            methods.add("POST");
            return methods;
        }
        if (path.matches("/app/rest/projects/\\{.*\\}")) {
            methods.add("GET");
            methods.add("PUT");
            methods.add("DELETE");
            return methods;
        }
        if (path.contains("/projects/") && path.contains("/name")) {
            methods.add("GET");
            methods.add("PUT");
            return methods;
        }
        if (path.contains("/projects/") && path.contains("/description")) {
            methods.add("GET");
            methods.add("PUT");
            return methods;
        }

        // ===== USERS =====
        if (path.equals("/app/rest/users")) {
            methods.add("GET");
            methods.add("POST");
            return methods;
        }
        if (path.matches("/app/rest/users/\\{.*\\}")) {
            methods.add("GET");
            methods.add("PUT");
            methods.add("DELETE");
            return methods;
        }

        // ===== AGENTS =====
        if (path.equals("/app/rest/agents")) {
            methods.add("GET");
            return methods;
        }
        if (path.matches("/app/rest/agents/\\{.*\\}")) {
            methods.add("GET");
            return methods;
        }

        // ===== VCS ROOTS =====
        if (path.equals("/app/rest/vcs-roots")) {
            methods.add("GET");
            methods.add("POST");
            return methods;
        }
        if (path.matches("/app/rest/vcs-roots/\\{.*\\}")) {
            methods.add("GET");
            methods.add("PUT");
            methods.add("DELETE");
            return methods;
        }

        // ===== CHANGES =====
        if (path.equals("/app/rest/changes")) {
            methods.add("GET");
            return methods;
        }
        if (path.matches("/app/rest/changes/\\{.*\\}")) {
            methods.add("GET");
            return methods;
        }

        // ===== INVESTIGATIONS =====
        if (path.equals("/app/rest/investigations")) {
            methods.add("GET");
            methods.add("POST");
            return methods;
        }
        if (path.matches("/app/rest/investigations/\\{.*\\}")) {
            methods.add("GET");
            methods.add("DELETE");
            return methods;
        }

        // ===== PROBLEMS =====
        if (path.equals("/app/rest/problems")) {
            methods.add("GET");
            return methods;
        }
        if (path.matches("/app/rest/problems/\\{.*\\}")) {
            methods.add("GET");
            return methods;
        }

        // ===== TESTS =====
        if (path.equals("/app/rest/tests")) {
            methods.add("GET");
            return methods;
        }
        if (path.matches("/app/rest/tests/\\{.*\\}")) {
            methods.add("GET");
            return methods;
        }

        // ===== MUTES =====
        if (path.equals("/app/rest/mutes")) {
            methods.add("GET");
            methods.add("POST");
            return methods;
        }
        if (path.matches("/app/rest/mutes/\\{.*\\}")) {
            methods.add("GET");
            methods.add("DELETE");
            return methods;
        }

        // ===== SERVER =====
        if (path.equals("/app/rest/server")) {
            methods.add("GET");
            return methods;
        }

        // ===== HEALTH =====
        if (path.equals("/app/rest/health") || path.startsWith("/app/rest/health/")) {
            methods.add("GET");
            return methods;
        }

        // ===== AGENT POOLS =====
        if (path.equals("/app/rest/agentPools")) {
            methods.add("GET");
            methods.add("POST");
            return methods;
        }
        if (path.matches("/app/rest/agentPools/\\{.*\\}")) {
            methods.add("GET");
            methods.add("PUT");
            methods.add("DELETE");
            return methods;
        }

        // ===== ROLES =====
        if (path.equals("/app/rest/roles")) {
            methods.add("GET");
            return methods;
        }

        // ===== USER GROUPS =====
        if (path.equals("/app/rest/userGroups")) {
            methods.add("GET");
            methods.add("POST");
            return methods;
        }
        if (path.matches("/app/rest/userGroups/\\{.*\\}")) {
            methods.add("GET");
            methods.add("PUT");
            methods.add("DELETE");
            return methods;
        }

        // ===== CLOUD =====
        if (path.startsWith("/app/rest/cloud/")) {
            methods.add("GET");
            return methods;
        }

        // ===== DEPLOYMENT DASHBOARD =====
        if (path.startsWith("/app/rest/deploymentDashboards")) {
            methods.add("GET");
            return methods;
        }

        // ===== AVATAR =====
        if (path.startsWith("/app/rest/avatars/")) {
            methods.add("GET");
            return methods;
        }

        // ===== AUDIT =====
        if (path.startsWith("/app/rest/audit")) {
            methods.add("GET");
            return methods;
        }

        // ===== STATISTICS =====
        if (path.equals("/app/rest/statistics")) {
            methods.add("GET");
            return methods;
        }

        // ===== ROOT =====
        if (path.equals("/app/rest") || path.equals("/app/rest/apiVersion") ||
                path.equals("/app/rest/version") || path.equals("/app/rest/info")) {
            methods.add("GET");
            return methods;
        }

        // ===== DEFAULT - если ничего не подошло =====
        // Для коллекций - GET и POST
        if (path.endsWith("s") || path.endsWith("/")) {
            methods.add("GET");
            methods.add("POST");
        }
        // Для конкретных ресурсов - GET, PUT, DELETE
        if (path.contains("{") && !path.endsWith("s")) {
            methods.add("GET");
            methods.add("PUT");
            methods.add("DELETE");
        }
        // Если ничего не подошло - только GET
        if (methods.isEmpty()) {
            methods.add("GET");
        }

        return methods;
    }

    private static Map<String, Set<String>> getCoveredEndpoints(File resultsDir) throws IOException {
        Map<String, Set<String>> covered = new HashMap<>();
        ObjectMapper mapper = new ObjectMapper();

        for (File file : resultsDir.listFiles()) {
            if (!file.getName().endsWith(".json")) continue;
            try {
                Map<String, Object> data = mapper.readValue(file, Map.class);
                String path = (String) data.get("path");
                String method = (String) data.get("method");

                if (path != null && method != null) {
                    // Сохраняем путь как есть, без нормализации
                    covered.computeIfAbsent(path, k -> new HashSet<>()).add(method.toUpperCase());
                }
            } catch (Exception e) {
                // Игнорируем
            }
        }

        return covered;
    }

    private static void generateHtmlReport(Map<String, CoverageInfo> coverageMap) throws IOException {
        StringBuilder html = new StringBuilder();

        long totalEndpoints = coverageMap.size();
        long fullyCovered = coverageMap.values().stream()
                .filter(CoverageInfo::isFullyCovered)
                .count();
        long partiallyCovered = coverageMap.values().stream()
                .filter(CoverageInfo::isPartiallyCovered)
                .count();
        long notCovered = totalEndpoints - fullyCovered - partiallyCovered;

        double coveragePercent = totalEndpoints > 0 ?
                (fullyCovered * 100.0 / totalEndpoints) : 0;

        html.append("<!DOCTYPE html>\n")
                .append("<html>\n")
                .append("<head>\n")
                .append("    <meta charset=\"UTF-8\">\n")
                .append("    <title>Swagger Coverage Report - TeamCity Builds API</title>\n")
                .append("    <style>\n")
                .append("        * { box-sizing: border-box; }\n")
                .append("        body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif; margin: 0; padding: 20px; background: #f5f7fa; }\n")
                .append("        .container { max-width: 1400px; margin: 0 auto; }\n")
                .append("        .header { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: white; padding: 30px; border-radius: 12px; margin-bottom: 30px; }\n")
                .append("        .header h1 { margin: 0; font-size: 28px; }\n")
                .append("        .header p { margin: 10px 0 0; opacity: 0.9; }\n")
                .append("        .summary { display: grid; grid-template-columns: repeat(auto-fit, minmax(200px, 1fr)); gap: 20px; margin-bottom: 30px; }\n")
                .append("        .summary .card { background: white; padding: 20px; border-radius: 10px; box-shadow: 0 2px 8px rgba(0,0,0,0.08); text-align: center; }\n")
                .append("        .summary .card .number { font-size: 32px; font-weight: bold; }\n")
                .append("        .summary .card .label { color: #666; font-size: 14px; margin-top: 5px; }\n")
                .append("        .summary .card.green .number { color: #4CAF50; }\n")
                .append("        .summary .card.orange .number { color: #FF9800; }\n")
                .append("        .summary .card.red .number { color: #f44336; }\n")
                .append("        .summary .card.blue .number { color: #2196F3; }\n")
                .append("        .coverage-bar { background: #e0e0e0; height: 30px; border-radius: 15px; overflow: hidden; margin-bottom: 30px; position: relative; }\n")
                .append("        .coverage-bar .filled { height: 100%; background: linear-gradient(90deg, #4CAF50, #8BC34A); transition: width 0.5s; display: flex; align-items: center; justify-content: flex-end; padding-right: 10px; color: white; font-weight: bold; font-size: 14px; }\n")
                .append("        .coverage-bar .filled.medium { background: linear-gradient(90deg, #FFC107, #FF9800); }\n")
                .append("        .coverage-bar .filled.low { background: linear-gradient(90deg, #f44336, #FF5722); }\n")
                .append("        .coverage-bar .label { position: absolute; width: 100%; text-align: center; line-height: 30px; color: #333; font-weight: bold; font-size: 14px; }\n")
                .append("        .endpoint-list { background: white; border-radius: 10px; box-shadow: 0 2px 8px rgba(0,0,0,0.08); overflow: hidden; max-height: 70vh; overflow-y: auto; }\n")
                .append("        .endpoint { padding: 10px 20px; border-bottom: 1px solid #eee; display: flex; justify-content: space-between; align-items: center; }\n")
                .append("        .endpoint:last-child { border-bottom: none; }\n")
                .append("        .endpoint .path { font-family: 'Courier New', monospace; font-weight: 600; color: #333; font-size: 13px; flex: 1; word-break: break-all; }\n")
                .append("        .endpoint .methods { display: flex; gap: 4px; flex-wrap: wrap; }\n")
                .append("        .endpoint .method { padding: 2px 8px; border-radius: 3px; font-size: 11px; font-weight: 600; text-transform: uppercase; }\n")
                .append("        .endpoint .method.covered { background: #4CAF50; color: white; }\n")
                .append("        .endpoint .method.not-covered { background: #f44336; color: white; }\n")
                .append("        .endpoint .method.partial { background: #FF9800; color: white; }\n")
                .append("        .endpoint .status { font-size: 18px; margin-left: 10px; min-width: 30px; text-align: center; }\n")
                .append("        .endpoint.fully-covered { border-left: 4px solid #4CAF50; }\n")
                .append("        .endpoint.partial { border-left: 4px solid #FF9800; }\n")
                .append("        .endpoint.not-covered { border-left: 4px solid #f44336; }\n")
                .append("        .filter { margin-bottom: 20px; display: flex; gap: 10px; flex-wrap: wrap; }\n")
                .append("        .filter button { padding: 8px 16px; border: 2px solid #ddd; border-radius: 6px; background: white; cursor: pointer; font-weight: 500; transition: all 0.2s; }\n")
                .append("        .filter button:hover { border-color: #667eea; }\n")
                .append("        .filter button.active { background: #667eea; color: white; border-color: #667eea; }\n")
                .append("        .search { margin-bottom: 20px; }\n")
                .append("        .search input { padding: 10px 15px; border: 2px solid #ddd; border-radius: 6px; width: 100%; max-width: 400px; font-size: 14px; }\n")
                .append("        .stats-extra { display: flex; gap: 20px; margin-top: 10px; font-size: 14px; color: #666; flex-wrap: wrap; }\n")
                .append("        @media (max-width: 768px) { .endpoint { flex-direction: column; align-items: flex-start; gap: 10px; } }\n")
                .append("    </style>\n")
                .append("</head>\n")
                .append("<body>\n")
                .append("    <div class=\"container\">\n");

        // Header
        html.append("        <div class=\"header\">\n")
                .append("            <h1>📊 Swagger Coverage Report</h1>\n")
                .append("            <p>TeamCity Builds API - Coverage analysis of REST API tests</p>\n")
                .append("            <div class=\"stats-extra\">\n")
                .append("                <span>📌 Total endpoints: <strong>").append(totalEndpoints).append("</strong></span>\n")
                .append("                <span>✅ Fully covered: <strong style=\"color: #4CAF50;\">").append(fullyCovered).append("</strong></span>\n")
                .append("                <span>🟡 Partial: <strong style=\"color: #FF9800;\">").append(partiallyCovered).append("</strong></span>\n")
                .append("                <span>❌ Not covered: <strong style=\"color: #f44336;\">").append(notCovered).append("</strong></span>\n")
                .append("            </div>\n")
                .append("        </div>\n");

        // Summary Cards
        String barClass = coveragePercent >= 70 ? "" : coveragePercent >= 50 ? "medium" : "low";

        html.append("        <div class=\"summary\">\n")
                .append("            <div class=\"card blue\"><div class=\"number\">").append(totalEndpoints).append("</div><div class=\"label\">Total Endpoints</div></div>\n")
                .append("            <div class=\"card green\"><div class=\"number\">").append(fullyCovered).append("</div><div class=\"label\">✅ Fully Covered</div></div>\n")
                .append("            <div class=\"card orange\"><div class=\"number\">").append(partiallyCovered).append("</div><div class=\"label\">🟡 Partial</div></div>\n")
                .append("            <div class=\"card red\"><div class=\"number\">").append(notCovered).append("</div><div class=\"label\">❌ Not Covered</div></div>\n")
                .append("        </div>\n");

        // Coverage Bar
        html.append("        <div class=\"coverage-bar\">\n")
                .append("            <div class=\"filled ").append(barClass).append("\" style=\"width: ").append(coveragePercent).append("%;\">\n")
                .append("                ").append(String.format("%.1f", coveragePercent)).append("%\n")
                .append("            </div>\n")
                .append("            <div class=\"label\">Overall Coverage</div>\n")
                .append("        </div>\n");

        // Filters and Search
        html.append("        <div class=\"filter\">\n")
                .append("            <button class=\"active\" onclick=\"filter('all')\">All</button>\n")
                .append("            <button onclick=\"filter('fully-covered')\">✅ Fully Covered</button>\n")
                .append("            <button onclick=\"filter('partial')\">🟡 Partial</button>\n")
                .append("            <button onclick=\"filter('not-covered')\">❌ Not Covered</button>\n")
                .append("        </div>\n")
                .append("        <div class=\"search\">\n")
                .append("            <input type=\"text\" id=\"searchInput\" placeholder=\"🔍 Search endpoints...\" onkeyup=\"searchEndpoints()\">\n")
                .append("        </div>\n")
                .append("        <h2>📋 Endpoints</h2>\n")
                .append("        <div class=\"endpoint-list\">\n");

        // Sort: not covered first, then partial, then covered
        List<Map.Entry<String, CoverageInfo>> sorted = coverageMap.entrySet().stream()
                .sorted((a, b) -> {
                    int aScore = a.getValue().isFullyCovered() ? 2 :
                            a.getValue().isPartiallyCovered() ? 1 : 0;
                    int bScore = b.getValue().isFullyCovered() ? 2 :
                            b.getValue().isPartiallyCovered() ? 1 : 0;
                    return Integer.compare(aScore, bScore);
                })
                .collect(Collectors.toList());

        for (Map.Entry<String, CoverageInfo> entry : sorted) {
            String path = entry.getKey();
            CoverageInfo info = entry.getValue();

            String statusClass = info.isFullyCovered() ? "fully-covered" :
                    info.isPartiallyCovered() ? "partial" : "not-covered";
            String statusIcon = info.isFullyCovered() ? "✅" :
                    info.isPartiallyCovered() ? "🟡" : "❌";
            String statusText = info.isFullyCovered() ? "Fully Covered" :
                    info.isPartiallyCovered() ? "Partial" : "Not Covered";

            // Подсчитываем покрытые методы
            long coveredMethods = info.getMethods().values().stream().filter(v -> v).count();
            long totalMethods = info.getMethods().size();

            html.append("            <div class=\"endpoint ").append(statusClass).append("\" data-status=\"").append(statusClass).append("\">\n")
                    .append("                <div class=\"path\">").append(path).append("</div>\n")
                    .append("                <div style=\"display: flex; align-items: center; gap: 15px;\">\n")
                    .append("                    <div class=\"methods\">\n");

            // Show all methods with their status
            String[] allMethods = {"GET", "POST", "PUT", "DELETE", "PATCH"};
            for (String method : allMethods) {
                Boolean covered = info.getMethods().get(method);
                String methodClass = covered != null && covered ? "covered" : "not-covered";
                html.append("                        <span class=\"method ").append(methodClass).append("\">")
                        .append(method)
                        .append("</span>\n");
            }

            html.append("                    </div>\n")
                    .append("                    <div style=\"display: flex; align-items: center; gap: 8px;\">\n")
                    .append("                        <span style=\"font-size: 12px; color: #666;\">").append(coveredMethods).append("/").append(totalMethods).append("</span>\n")
                    .append("                        <div class=\"status\" title=\"").append(statusText).append("\">").append(statusIcon).append("</div>\n")
                    .append("                    </div>\n")
                    .append("                </div>\n")
                    .append("            </div>\n");
        }

        html.append("        </div>\n")
                .append("    </div>\n")
                .append("    <script>\n")
                .append("        function filter(status) {\n")
                .append("            document.querySelectorAll('.filter button').forEach(b => b.classList.remove('active'));\n")
                .append("            event.target.classList.add('active');\n")
                .append("            document.querySelectorAll('.endpoint').forEach(el => {\n")
                .append("                if (status === 'all' || el.dataset.status === status) {\n")
                .append("                    el.style.display = 'flex';\n")
                .append("                } else {\n")
                .append("                    el.style.display = 'none';\n")
                .append("                }\n")
                .append("            });\n")
                .append("        }\n")
                .append("        function searchEndpoints() {\n")
                .append("            let input = document.getElementById('searchInput').value.toLowerCase();\n")
                .append("            document.querySelectorAll('.endpoint').forEach(el => {\n")
                .append("                let path = el.querySelector('.path').textContent.toLowerCase();\n")
                .append("                if (path.includes(input)) {\n")
                .append("                    el.style.display = 'flex';\n")
                .append("                } else {\n")
                .append("                    el.style.display = 'none';\n")
                .append("                }\n")
                .append("            });\n")
                .append("        }\n")
                .append("    </script>\n")
                .append("</body>\n")
                .append("</html>\n");

        // Save HTML report
        File reportDir = new File(REPORT_DIR);
        if (!reportDir.exists()) reportDir.mkdirs();

        try (FileWriter writer = new FileWriter(new File(reportDir, "index.html"))) {
            writer.write(html.toString());
        }
    }

    private static void openReport() {
        try {
            String os = System.getProperty("os.name").toLowerCase();
            String reportPath = Paths.get(REPORT_DIR, "index.html").toAbsolutePath().toString();

            if (os.contains("mac")) {
                Runtime.getRuntime().exec(new String[]{"open", reportPath});
            } else if (os.contains("win")) {
                Runtime.getRuntime().exec(new String[]{"cmd", "/c", "start", reportPath});
            } else if (os.contains("nix") || os.contains("nux")) {
                Runtime.getRuntime().exec(new String[]{"xdg-open", reportPath});
            }
        } catch (Exception e) {
            // Игнорируем
        }
    }

    // =========================================================================
    // ВСПОМОГАТЕЛЬНЫЙ КЛАСС
    // =========================================================================

    private static class CoverageInfo {
        private final String path;
        private final Map<String, Boolean> methods = new LinkedHashMap<>();

        public CoverageInfo(String path) {
            this.path = path;
        }

        public void addMethod(String method, boolean covered) {
            methods.put(method, covered);
        }

        public Map<String, Boolean> getMethods() {
            return methods;
        }

        public boolean isFullyCovered() {
            return !methods.isEmpty() && methods.values().stream().allMatch(v -> v);
        }

        public boolean isPartiallyCovered() {
            return methods.values().stream().anyMatch(v -> v) &&
                    methods.values().stream().anyMatch(v -> !v);
        }
    }
}