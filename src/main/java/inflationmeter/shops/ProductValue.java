package inflationmeter.shops;

import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;

@Data
@RequiredArgsConstructor
public class ProductValue {
    @NonNull
    private String name;
    private BigDecimal price;
    private BigDecimal weight;
}
