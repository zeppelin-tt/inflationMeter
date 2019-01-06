package inflationmeter.repository;


import inflationmeter.domain.FreeProxy;
import org.springframework.data.repository.CrudRepository;

public interface ProxyRepository extends CrudRepository<FreeProxy, Long> {
}
