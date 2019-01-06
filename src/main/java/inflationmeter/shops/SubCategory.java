package inflationmeter.shops;

import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Data
@RequiredArgsConstructor
public class SubCategory {
    @NonNull
    private String subCategoryName;
    private List<ProductValue> productValues;
}
