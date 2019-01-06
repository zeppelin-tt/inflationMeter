package inflationmeter.service;

import com.google.common.collect.Lists;
import inflationmeter.domain.FreeProxy;
import inflationmeter.exception.CounterFullException;
import inflationmeter.parse.Connect;
import inflationmeter.repository.ProxyRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.ListUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static inflationmeter.utils.Inet.isProxyAvailable;

@Slf4j
@Service
public class ParseService {

    private final ProxyParseService proxyParseService;
    private final ProxyRepository proxyRepository;

    private Connect connect = new Connect();
    private static final String CATEGORIES_URL = "https://www.okeydostavka.ru/spb/catalog";

    @Autowired
    public ParseService(ProxyParseService proxyParseService, ProxyRepository proxyRepository) {
        this.proxyParseService = proxyParseService;
        this.proxyRepository = proxyRepository;
    }


    public String startParse() throws CounterFullException, IOException {
        updateProxyDb(getRandomProxy());

//        Shop shop = new Shop("OKEY");
//
//
//        Document doc = connect.pretend(CATEGORIES_URL);
//        Element categoriesElement = doc.getElementsByClass("row categories").first();
//        List<Element> rows = categoriesElement.getElementsByTag("tr");
//

        return null;
    }

    private Proxy getRandomProxy() {
        ArrayList<FreeProxy> freeProxies = Lists.newArrayList(proxyRepository.findAll());
        InetSocketAddress socketAddress;
        Random r = new Random();
        while (true) {
            FreeProxy proxy = freeProxies.get(r.nextInt(freeProxies.size() - 1));
            if (isProxyAvailable(proxy.getIp(), proxy.getPort(), 1000)) {
                socketAddress = new InetSocketAddress(proxy.getIp(), proxy.getPort());
                break;
            }
        }
        return new Proxy(Proxy.Type.HTTP, socketAddress);
    }

    private void updateProxyDb(Proxy proxy) throws CounterFullException, IOException {
        int fullSaveCounter = 0;
        int fullUpdateCounter = 0;
        while (true) {
            int updateCounter = 0;
            int jointCounter = 0;
            int saveCounter = 0;
            Supplier<Stream<FreeProxy>> dbProxiesSupplier = () -> StreamSupport.stream(proxyRepository.findAll().spliterator(), false);
            List<FreeProxy> listProxies = proxyParseService.getListProxies(connect, proxy);
            for (FreeProxy freeProxy : listProxies) {
                jointCounter++;
                Optional<FreeProxy> targetProxyOpt = dbProxiesSupplier
                        .get()
                        .filter(p -> p.getIp().equals(freeProxy.getIp()))
                        .findFirst();
                if (targetProxyOpt.isPresent()) {
                    FreeProxy targetProxy = targetProxyOpt.get();
                    if (targetProxy.getLastCheck().minusHours(5).isAfter(freeProxy.getLastCheck())) {
                        freeProxy.setId(targetProxy.getId());
                        proxyRepository.save(freeProxy);
                        fullUpdateCounter++;
                        updateCounter++;
                    }
                } else {
                    fullSaveCounter++;
                    saveCounter++;
                    proxyRepository.save(freeProxy);
                }
            }
            if (jointCounter * 0.1 > updateCounter && jointCounter * 0.1 > saveCounter) {
                break;
            }
        }
        if (fullUpdateCounter == 0 && fullSaveCounter == 0) {
            log.info("База FreeProxy актуальна");
        } else {
            log.info("Сохренено новых: " + fullSaveCounter + " FreeProxy");
            log.info("Обновлено: " + fullUpdateCounter + " FreeProxy");
        }
    }

}
