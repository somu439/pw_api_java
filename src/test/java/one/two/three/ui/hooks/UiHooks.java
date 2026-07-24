package one.two.three.ui.hooks;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.Tracing;
import com.microsoft.playwright.Video;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import one.two.three.support.Config;
import one.two.three.ui.support.UiContext;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class UiHooks {

    private static final Path VIDEO_DIR = Paths.get("target/pw-videos");
    private static final Path TRACE_DIR = Paths.get("target/pw-traces");

    private final UiContext ctx;
    private Video video;

    public UiHooks(UiContext ctx) {
        this.ctx = ctx;
    }

    @Before("@ui")
    public void setUp(Scenario scenario) throws IOException {
        Files.createDirectories(VIDEO_DIR);
        Files.createDirectories(TRACE_DIR);

        ctx.scenario = scenario;
        ctx.playwright = Playwright.create();
        ctx.browser = ctx.playwright.chromium().launch(
            new BrowserType.LaunchOptions().setHeadless(Config.HEADLESS)
        );
        ctx.browserContext = ctx.browser.newContext(
            new Browser.NewContextOptions()
                .setRecordVideoDir(VIDEO_DIR)
                .setRecordVideoSize(1280, 720)
        );
        ctx.browserContext.tracing().start(
            new Tracing.StartOptions().setScreenshots(true).setSnapshots(true).setSources(true)
        );
        ctx.page = ctx.browserContext.newPage();
        video = ctx.page.video();
    }

    @After("@ui")
    public void tearDown(Scenario scenario) {
        boolean failed = scenario.isFailed();

        if (failed && ctx.page != null && !ctx.page.isClosed()) {
            byte[] screenshot = ctx.page.screenshot(new Page.ScreenshotOptions().setFullPage(true));
            scenario.attach(screenshot, "image/png", "Screenshot");
        }

        String safeName = scenario.getName().replaceAll("[^a-zA-Z0-9_-]", "_") + "-" + System.currentTimeMillis();
        Path tracePath = TRACE_DIR.resolve(safeName + "-trace.zip");

        if (ctx.browserContext != null) {
            ctx.browserContext.tracing().stop(new Tracing.StopOptions().setPath(tracePath));
            ctx.browserContext.close();
        }
        if (ctx.browser != null) {
            ctx.browser.close();
        }

        Path videoPath = video != null ? video.path() : null;

        if (failed) {
            attachIfExists(scenario, videoPath, "video/webm", "Video");
            attachIfExists(scenario, tracePath, "application/zip", "Trace");
        } else {
            deleteQuietly(videoPath);
            deleteQuietly(tracePath);
        }

        if (ctx.playwright != null) {
            ctx.playwright.close();
        }
    }

    private void attachIfExists(Scenario scenario, Path path, String mimeType, String name) {
        if (path == null || !Files.exists(path)) return;
        try {
            scenario.attach(Files.readAllBytes(path), mimeType, name);
        } catch (IOException e) {
            System.err.println("[UiHooks] Failed to attach " + name + ": " + e.getMessage());
        }
    }

    private void deleteQuietly(Path path) {
        if (path == null) return;
        try {
            Files.deleteIfExists(path);
        } catch (IOException ignored) {
        }
    }
}
