package inflationmeter.shops.model;

import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Data
@RequiredArgsConstructor
public class Shop {
    @NonNull
    private String shopName;
    @NonNull
    private String url;
    private List<Category> categories;
}
