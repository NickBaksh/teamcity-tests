package com.teamcity.ui.explore;

import com.codeborne.selenide.WebDriverRunner;
import com.teamcity.core.models.BuildConfig;
import com.teamcity.core.models.Project;
import com.teamcity.ui.BaseUiTest;
import com.teamcity.ui.extensions.AdminUiSessionExtension;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.codeborne.selenide.Selenide.open;
import static com.codeborne.selenide.Selenide.sleep;

@Tag("ui-explore")
@ExtendWith(AdminUiSessionExtension.class)
public class UiLocatorExplorerTest extends BaseUiTest {

    private final Path outDir = Path.of("build", "ui-explore");

    @Test
    void dumpLocatorsForFailingFlows() throws Exception {
        Files.createDirectories(outDir);
        Project project = givenProject();
        BuildConfig config = givenBuildConfig(project.getId());

        dump("01-overview", "/overview.html");
        dump("02-project-view", "/project/" + project.getId());
        dump("03-edit-project", "/admin/editProject.html?projectId=" + project.getId());
        dump("04-delete-project", "/admin/deleteObject.html?projectId=" + project.getId());
        dump("05-bc-overview", "/buildConfiguration/" + config.getId());
        dump("06-bc-edit-general", "/admin/editBuild.html?id=buildType:" + config.getId());
        dump("07-bc-runners", "/admin/editBuildRunners.html?id=buildType:" + config.getId());
        dump("08-bc-edit-run-type", "/admin/editRunType.html?id=buildType:" + config.getId() + "&init=1");
        dump("09-create-project", "/admin/createProject.html?projectId=_Root");
        dump("10-create-bc", "/admin/createBuildType.html?projectId=" + project.getId());
        dumpInputs("10-create-bc-inputs");
        dump("11-admin-projects", "/admin/admin.html?item=projects");
        dump("12-pause-hint", "/admin/editBuild.html?id=buildType:" + config.getId() + "&tab=buildTypeStatusDiv");

        open("/admin/createBuildType.html?projectId=" + project.getId());
        sleep(2000);
        dumpInputs("13-create-bc-before-submit");
        String probeName = "probe_bc_" + System.currentTimeMillis();
        String probeId = "ProbeBc" + System.currentTimeMillis();
        try {
            var nameEl = com.codeborne.selenide.Selenide.$("#buildTypeName");
            var idEl = com.codeborne.selenide.Selenide.$("#buildTypeExternalId");
            if (nameEl.exists()) {
                nameEl.setValue(probeName);
            }
            if (idEl.exists()) {
                idEl.clear();
                idEl.setValue(probeId);
            }
            Boolean submitted = com.codeborne.selenide.Selenide.executeJavaScript(
                    "var form = document.querySelector('form');"
                            + "if (!form) return false;"
                            + "var n = document.getElementById('buildTypeName');"
                            + "var i = document.getElementById('buildTypeExternalId');"
                            + "if (n) n.value = arguments[0];"
                            + "if (i) i.value = arguments[1];"
                            + "form.submit(); return true;",
                    probeName, probeId
            );
            System.out.println("Classic form.submit result=" + submitted);
            sleep(3500);
        } catch (Exception e) {
            System.out.println("Create probe failed: " + e.getMessage());
        }
        dumpCurrent("14-create-bc-after-submit");
        dumpInputs("14-create-bc-after-submit-inputs");
        Files.writeString(outDir.resolve("14-create-bc-probe.txt"),
                "name=" + probeName + "\nid=" + probeId + "\nurl=" + WebDriverRunner.url());

        open("/admin/editProject.html?projectId=" + project.getId());
        sleep(2000);
        try {
            var btn = com.codeborne.selenide.Selenide.$x(
                    "//a[contains(.,'Create build configuration')] | //button[contains(.,'Create build configuration')]"
            );
            if (btn.exists()) {
                btn.click();
                sleep(2500);
            }
        } catch (Exception e) {
            System.out.println("New UI create click failed: " + e.getMessage());
        }
        dumpCurrent("16-new-ui-create-modal");
        dumpInputs("16-new-ui-create-modal-inputs");
        Object labels = com.codeborne.selenide.Selenide.executeJavaScript(
                "return Array.from(document.querySelectorAll('input,button,[data-test],label'))"
                        + ".slice(0,120).map(el => ("
                        + "el.tagName + ' id=' + (el.id||'') + ' name=' + (el.name||'')"
                        + " + ' data-test=' + (el.getAttribute('data-test')||'')"
                        + " + ' placeholder=' + (el.getAttribute('placeholder')||'')"
                        + " + ' text=' + ((el.innerText||'').trim().substring(0,60))"
                        + "));"
        );
        Files.writeString(outDir.resolve("16-new-ui-create-modal-attrs.txt"), String.valueOf(labels));

        open("/admin/editRunType.html?id=buildType:" + config.getId()
                + "&runnerId=__NEW_RUNNER__&init=1");
        sleep(2000);
        try {
            var cmd = com.codeborne.selenide.Selenide.$("[data-key='simpleRunner']");
            if (cmd.exists()) {
                cmd.click();
                sleep(2500);
            }
        } catch (Exception e) {
            System.out.println("Command Line click failed: " + e.getMessage());
        }
        dumpCurrent("15-command-line-form");
        dumpInputs("15-command-line-form-inputs");
        Object scriptFields = com.codeborne.selenide.Selenide.executeJavaScript(
                "return Array.from(document.querySelectorAll('textarea,input,.CodeMirror,[name*=script],[id*=script],[name*=command],[id*=command]'))"
                        + ".map(el => el.tagName + ' id=' + (el.id||'') + ' name=' + (el.name||'')"
                        + " + ' class=' + (el.className||'').toString().substring(0,80));"
        );
        Files.writeString(outDir.resolve("15-command-line-script-fields.txt"), String.valueOf(scriptFields));
    }

    private void dumpInputs(String name) throws Exception {
        String js = """
                return Array.from(document.querySelectorAll('input,textarea,select,button'))
                  .slice(0, 80)
                  .map(el => ({
                    tag: el.tagName,
                    type: el.type || '',
                    id: el.id || '',
                    name: el.name || '',
                    value: (el.value || '').substring(0, 80),
                    text: (el.innerText || '').substring(0, 80),
                    visible: !!(el.offsetWidth || el.offsetHeight || el.getClientRects().length)
                  }));
                """;
        Object raw = com.codeborne.selenide.Selenide.executeJavaScript(js);
        Files.writeString(outDir.resolve(name + ".txt"),
                "URL: " + WebDriverRunner.url() + "\n" + String.valueOf(raw));
    }

    private void dump(String name, String path) throws Exception {
        open(path);
        sleep(2500);
        dumpCurrent(name);
    }

    private void dumpCurrent(String name) throws Exception {
        String url = WebDriverRunner.url();
        String title = WebDriverRunner.getWebDriver().getTitle();
        String html = WebDriverRunner.source();

        Set<String> hrefs = extractByPattern(html,
                Pattern.compile("href\\s*=\\s*\"([^\"]+)\"", Pattern.CASE_INSENSITIVE));
        Set<String> texts = new LinkedHashSet<>();
        for (String key : List.of("Pause", "Activate", "Delete", "Remove", "Add build step",
                "Build Steps", "Build steps", "Create project", "Actions", "Command Line")) {
            if (html.contains(key)) {
                texts.add("FOUND_TEXT: " + key);
            }
        }

        List<String> interestingHrefs = new ArrayList<>();
        for (String href : hrefs) {
            String h = href.toLowerCase();
            if (h.contains("pause") || h.contains("delete") || h.contains("remove")
                    || h.contains("runner") || h.contains("runtype") || h.contains("activate")
                    || h.contains("action") || h.contains("editbuild") || h.contains("step")) {
                interestingHrefs.add(href);
            }
        }

        Path txt = outDir.resolve(name + ".txt");
        String body = "URL: " + url + "\nTITLE: " + title
                + "\n\nTEXT_HITS:\n" + String.join("\n", texts)
                + "\n\nINTERESTING_HREFS:\n" + String.join("\n", interestingHrefs)
                + "\n\nHTML_SNIPPETS:\n" + extractSnippets(html);
        Files.writeString(txt, body);

        try {
            byte[] png = ((TakesScreenshot) WebDriverRunner.getWebDriver()).getScreenshotAs(OutputType.BYTES);
            Files.write(outDir.resolve(name + ".png"), png);
        } catch (Exception e) {
            System.out.println("Screenshot skipped for " + name + ": " + e.getMessage());
        }
        System.out.println("Dumped " + name + " -> " + url);
    }

    private static Set<String> extractByPattern(String html, Pattern pattern) {
        Set<String> result = new LinkedHashSet<>();
        Matcher matcher = pattern.matcher(html);
        while (matcher.find()) {
            result.add(matcher.group(1));
            if (result.size() > 200) {
                break;
            }
        }
        return result;
    }

    private static String extractSnippets(String html) {
        String lower = html.toLowerCase();
        StringBuilder sb = new StringBuilder();
        for (String key : List.of("pause", "deleteobject", "delete project", "build step",
                "editbuildrunners", "add build", "command line", "activate")) {
            int idx = lower.indexOf(key);
            int count = 0;
            while (idx >= 0 && count < 2) {
                int from = Math.max(0, idx - 100);
                int to = Math.min(html.length(), idx + 220);
                sb.append("--- ").append(key).append(" #").append(count + 1).append(" ---\n")
                        .append(html, from, to).append("\n\n");
                idx = lower.indexOf(key, idx + key.length());
                count++;
            }
        }
        return sb.toString();
    }
}
