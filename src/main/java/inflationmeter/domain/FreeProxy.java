package inflationmeter.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@EqualsAndHashCode(of = {"ip", "port"})
public class FreeProxy {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String ip;
    private int port;
    private LocalDateTime lastCheck;
    private Integer speedMs;
    private int uptime;
    private String country;
    private String city;
    private String anonymity;

}
