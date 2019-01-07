package inflationmeter.parse;

import inflationmeter.exception.CounterFullException;
import inflationmeter.service.DbUtilService;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Connection;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriverService;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.time.LocalDateTime;
import java.util.ArrayList;

import static inflationmeter.utils.Maths.rnd;
import static inflationmeter.utils.Wait.freeze;

@Slf4j
@Component
public class Connect {

    private WebDriver driver;
    private Connection connect;
    private static final int COUNT_ATTEMPS = 10;
    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/71.0.3578.98 Safari/537.36";
    private ConnectionType contextConnectionType;
    private int counter = 0;
    private ArrayList<InetSocketAddress> excludeAddresses = new ArrayList<>();

    private final DbUtilService dbUtilService;

    @Autowired
    public Connect(DbUtilService dbUtilService) {
        this.dbUtilService = dbUtilService;
    }

    public Element pretend(String page, ConnectionType connectionType) throws CounterFullException {
        return pretend(page, connectionType, false);
    }

    public Element pretend(String page, ConnectionType connectionType, boolean changeProxy) throws CounterFullException {
        Element doc;
        contextConnectionType = connectionType;
        if (counter >= COUNT_ATTEMPS) {
            throw new CounterFullException("Попытки создать Connection закончились");
        }
        doc = connectionType.equals(ConnectionType.JSOUP) ? byConnection(page, changeProxy) : byDriver(page, changeProxy);
        counter = 0;
        return doc;
    }

    private Element byDriver(String page, boolean changeProxy) throws CounterFullException {
        Element doc;
        InetSocketAddress address = null;
        try {
            if (driver == null || changeProxy) {
                address = dbUtilService.getRandomAddress(excludeAddresses);
                log.info("Соединяемся через Connection. Proxy: " + address.getHostString() + ":" + address.getPort());
                DesiredCapabilities caps = createCaps(address);
                driver = new PhantomJSDriver(caps);
            }
            driver.get(page);
        } catch (Exception e) {
            errorHandle(e, page, address);
            log.info("Что-то совершенно неведомиое");
            e.printStackTrace();
        }
        String pageSource = driver.getPageSource();
        doc = Jsoup.parseBodyFragment(pageSource);
        return doc;
    }

    private Element byConnection(String page, boolean changeProxy) throws CounterFullException {
        Element doc = null;
        InetSocketAddress address = null;
        try {
            if (connect == null || changeProxy) {
                address = dbUtilService.getRandomAddress(excludeAddresses);
                log.info("Соединяемся через Connection. Proxy: " + address.getHostString() + ":" + address.getPort());
                connect = Jsoup.connect(page).proxy(address.getHostString(), address.getPort());
            }
            doc = connect
                    .timeout(14000)
                    .ignoreContentType(true)
                    .followRedirects(true)
                    .userAgent(USER_AGENT)
                    .referrer("http://www.google.nl")
                    .get();
        } catch (Exception e) {
            errorHandle(e, page, address);
            log.info("Что-то совершенно неведомиое");
            e.printStackTrace();
        }
        return doc;
    }

    private DesiredCapabilities createCaps(InetSocketAddress address) {
        ArrayList<String> cliArgsCap = new ArrayList<>();
        cliArgsCap.add("--proxy=" + address.getHostString() + ":" + address.getPort());
        // cliArgsCap.add("--proxy-auth=username:password");
        cliArgsCap.add("--proxy-type=http");
        DesiredCapabilities caps = new DesiredCapabilities();
        caps.setJavascriptEnabled(true);
        caps.setCapability(PhantomJSDriverService.PHANTOMJS_CLI_ARGS, cliArgsCap);
        caps.setCapability("phantomjs.page.settings.userAgent", USER_AGENT);
        caps.setCapability(PhantomJSDriverService.PHANTOMJS_EXECUTABLE_PATH_PROPERTY, "d:\\code\\Java\\phantomjs-2.1.1-windows\\bin\\phantomjs.exe");
        return caps;
    }

    private void errorHandle(Exception e, String page, InetSocketAddress address) throws CounterFullException {
        if (e instanceof SocketTimeoutException) {
            handlingException("SocketTimeoutException", page, address, 10000, 30000);
        } else if (e instanceof HttpStatusException) {
            handlingException("HttpStatusException", page, address, 30000, 50000);
        } else if (e instanceof IllegalArgumentException) {
            handlingException("IllegalArgumentException", page, address, 5000, 10000);
        } else if (e instanceof SocketException) {
            handlingException("SocketException", page, address, 5000, 20000);
        } else if (e instanceof IOException) {
            handlingException("IOException", page, address, 5000, 15000);
        }
    }

    private void handlingException(String eName, String page, InetSocketAddress address, int minMills, int maxMills) throws CounterFullException {
        log.info(eName);
        log.info("On page: " + page);
        log.info("Время ошибки: " + LocalDateTime.now());
        counter++;
        excludeAddresses.add(address);
        freeze(rnd(minMills, maxMills));
        pretend(page, contextConnectionType, true);
    }

}
