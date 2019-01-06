package inflationmeter.shops;

import lombok.*;

import java.util.List;

@Data
@RequiredArgsConstructor
public class City {
    @NonNull
    private String cityName;
    @Singular
    private List<Shop> shops;
}
