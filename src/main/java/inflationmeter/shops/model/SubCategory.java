package inflationmeter.shops.model;

import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Data
@RequiredArgsConstructor
public class SubCategory {
    @NonNull
    private String subCategoryName;
    @NonNull
    private String url;
    private List<ProductValue> productValues;
}
