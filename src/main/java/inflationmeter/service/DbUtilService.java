package inflationmeter.service;

import com.google.common.collect.Lists;
import inflationmeter.domain.FreeProxy;
import inflationmeter.exception.CounterFullException;
import inflationmeter.parse.Connect;
import inflationmeter.repository.ProxyRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.net.InetSocketAddress;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static inflationmeter.utils.Inet.isProxyAvailable;

@Slf4j
@Service
public class DbUtilService {

    private final ProxyParseService proxyParseService;
    private final ProxyRepository proxyRepository;
    private final Connect connect;

    @Autowired
    public DbUtilService(ProxyParseService proxyParseService, ProxyRepository proxyRepository, @Lazy Connect connect) {
        this.proxyParseService = proxyParseService;
        this.proxyRepository = proxyRepository;
        this.connect = connect;
    }

    public InetSocketAddress getRandomAddress() {
        return getRandomAddress(Collections.emptyList());
    }

    public InetSocketAddress getRandomAddress(List<InetSocketAddress> excludeAddresses) {
        ArrayList<FreeProxy> freeProxies = Lists.newArrayList(proxyRepository.findAll());
        InetSocketAddress socketAddress;
        Random r = new Random();
        while (true) {
            FreeProxy freeProxy = freeProxies.get(r.nextInt(freeProxies.size() - 1));
            boolean isExclude = excludeAddresses.stream()
                    .anyMatch(a -> a.getHostName().equals(freeProxy.getIp()));
            if (isExclude) {
                continue;
            }
            if (isProxyAvailable(freeProxy.getIp(), freeProxy.getPort(), 1000)) {
                socketAddress = new InetSocketAddress(freeProxy.getIp(), freeProxy.getPort());
                break;
            }
        }
        return socketAddress;
    }

    void updateProxyDb() throws CounterFullException {
        int fullSaveCounter = 0;
        int fullUpdateCounter = 0;
        while (true) {
            int updateCounter = 0;
            int jointCounter = 0;
            int saveCounter = 0;
            Supplier<Stream<FreeProxy>> dbProxiesSupplier = () -> StreamSupport.stream(proxyRepository.findAll().spliterator(), false);
            List<FreeProxy> listProxies = proxyParseService.getListProxies(connect);
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
