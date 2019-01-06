package inflationmeter.shops;

import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Data
@RequiredArgsConstructor
public class Shop {
    @NonNull
    private String shopName;
    private List<Category> categories;
}
