package one.two.three.ui.support;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import io.cucumber.java.Scenario;
import one.two.three.support.Config;

public class UiContext {

    public Playwright playwright;
    public Browser browser;
    public BrowserContext browserContext;
    public Page page;
    public String baseUrl = Config.PARABANK_BASE_URL;
    public Scenario scenario;
}
