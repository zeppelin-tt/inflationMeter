package inflationmeter.service;

import inflationmeter.domain.FreeProxy;
import inflationmeter.exception.CounterFullException;
import inflationmeter.parse.Connect;
import inflationmeter.parse.ConnectionType;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.Proxy;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class ProxyParseService {

    private static final String PROXY_URL = "https://www.proxynova.com/proxy-server-list/country-ru/";

    public List<FreeProxy> getListProxies(Connect connect, Proxy proxy) throws CounterFullException, IOException {
        Element doc = connect.pretend(PROXY_URL, proxy, ConnectionType.DRIVER);
        Element tableElement = doc.getElementById("tbl_proxy_list");
        Elements elementsByTag = tableElement.getElementsByTag("tr");
        List<FreeProxy> freeProxies = new ArrayList<>();
        for (int i = 1; i < elementsByTag.size(); i++) {
            FreeProxy freeProxy = new FreeProxy();
            try {
                freeProxy.setIp(elementsByTag.get(i).getElementsByTag("td").get(0).getElementsByTag("abbr").get(0).text());
                freeProxy.setPort(Integer.parseInt(elementsByTag.get(i).getElementsByTag("td").get(1).text()));
                String location = elementsByTag.get(i).getElementsByTag("td").get(5).text();
                freeProxy = parseLocation(freeProxy, location);
                String dateString = elementsByTag.get(i).getElementsByTag("td").get(2).getElementsByTag("time").get(0).attributes().get("datetime").replace("Z", "");
                freeProxy.setSpeedMs(Integer.valueOf(elementsByTag.get(i).getElementsByTag("td").get(3).getElementsByClass("progress-bar").get(0).attributes().get("title")));
                freeProxy.setLastCheck(LocalDateTime.parse(dateString, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                String uptimeString = elementsByTag.get(i).getElementsByTag("td").get(4).text();
                freeProxy.setUptime(Integer.parseInt(uptimeString.substring(0, uptimeString.indexOf("%"))));
                freeProxy.setAnonymity(elementsByTag.get(i).getElementsByTag("td").get(6).text());
                freeProxies.add(freeProxy);
            } catch (IndexOutOfBoundsException e) {
                log.info("Строка " + i + " не прочитана");
            }
        }
        return freeProxies;
    }

    private FreeProxy parseLocation(FreeProxy freeProxy, String location) {
        if (location.contains(" - ")) {
            freeProxy.setCountry(location.substring(0, location.indexOf("-") - 1));
            freeProxy.setCity(location.substring(location.indexOf("-") + 2));
        } else {
            freeProxy.setCountry(location);
        }
        return freeProxy;
    }
}
