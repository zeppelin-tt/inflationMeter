package inflationmeter.parse;

import inflationmeter.exception.CounterFullException;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Connection;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriverService;
import org.openqa.selenium.remote.DesiredCapabilities;

import java.io.IOException;
import java.net.Proxy;
import java.net.SocketTimeoutException;
import java.time.LocalDateTime;
import java.util.ArrayList;

import static inflationmeter.utils.Maths.rnd;
import static inflationmeter.utils.Wait.freeze;

@Slf4j
public class Connect {

    private static final int COUNT_ATTEMPS = 5;
    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/71.0.3578.98 Safari/537.36";
    private ConnectionType contextConnectionType;
    private int counter = 0;


    public Element pretend(String page, ConnectionType connectionType) throws CounterFullException, IOException {
        return pretend(page, null, connectionType);
    }

    public Element pretend(String page, Proxy proxy, ConnectionType connectionType) throws CounterFullException, IOException {
        Element doc;
        contextConnectionType = connectionType;
        if (counter >= COUNT_ATTEMPS) {
            throw new CounterFullException("Попытки создать Connection закончились");
        }
        doc = connectionType.equals(ConnectionType.CONNECTION) ? byConnection(page, proxy) : byDriver(page, proxy);
        counter = 0;
        return doc;
    }

    private Element byDriver(String page, Proxy proxy) throws CounterFullException, IOException {
        Element doc;
        DesiredCapabilities caps = createCaps(proxy);
        WebDriver driver = new PhantomJSDriver(caps);
        try {
            driver.get(page);
        } catch (Exception e) {
            errorHandle(e, page, proxy);
            log.info("Что-то совершенно неведомиое");
            e.printStackTrace();
        }
        String pageSource = driver.getPageSource();
        doc = Jsoup.parseBodyFragment(pageSource);
        return doc;
    }

    private Element byConnection(String page, Proxy proxy) throws CounterFullException, IOException {
        Element doc = null;
        Connection connect;
        try {
            connect = proxy == null ? Jsoup.connect(page) : Jsoup.connect(page).proxy(proxy);
            doc = connect
                    .timeout(14000)
                    .ignoreContentType(true)
                    .proxy(proxy)
                    .followRedirects(true)
                    .userAgent(USER_AGENT)
                    .referrer("http://www.google.nl")
                    .get();
        } catch (Exception e) {
            errorHandle(e, page, proxy);
            log.info("Что-то совершенно неведомиое");
            e.printStackTrace();
        }
        return doc;
    }

    private DesiredCapabilities createCaps(Proxy proxy) {
        ArrayList<String> cliArgsCap = new ArrayList<>();
        cliArgsCap.add("--proxy=" + proxy.address().toString().replaceAll("/", ""));
        // cliArgsCap.add("--proxy-auth=username:password");
        cliArgsCap.add("--proxy-type=http");
        DesiredCapabilities caps = new DesiredCapabilities();
        caps.setJavascriptEnabled(true);
        if (proxy != null) {
            caps.setCapability(PhantomJSDriverService.PHANTOMJS_CLI_ARGS, cliArgsCap);
        }
        caps.setCapability("phantomjs.page.settings.userAgent", USER_AGENT);
        caps.setCapability(PhantomJSDriverService.PHANTOMJS_EXECUTABLE_PATH_PROPERTY, "d:\\code\\Java\\phantomjs-2.1.1-windows\\bin\\phantomjs.exe");
        return caps;
    }

    private void errorHandle(Exception e, String page, Proxy proxy) throws CounterFullException, IOException {
        if (e instanceof SocketTimeoutException) {
            log.info("SocketTimeoutException\r\nВремя ошибки: " + LocalDateTime.now());
            freeze(rnd(10000, 80000));
            counter++;
            pretend(page, proxy, contextConnectionType);
        } else if (e instanceof HttpStatusException) {
            log.info("Пытались загрузить: " + page);
            log.info("HttpStatusException\r\nВремя ошибки: " + LocalDateTime.now());
            freeze(60000);
            counter++;
            pretend(page, proxy, contextConnectionType);
        } else if (e instanceof IllegalArgumentException) {
            log.info("IllegalArgumentException\r\nOn page" + page + "\r\nВремя ошибки: " + LocalDateTime.now());
            counter++;
            pretend(page, proxy, contextConnectionType);
            freeze(15000);
        } else if (e instanceof IOException) {
            log.info("Что-то неведомиое");
            e.printStackTrace();
            throw new IOException();
        }
    }
}
