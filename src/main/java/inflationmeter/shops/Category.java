package inflationmeter.shops;

import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Data
@RequiredArgsConstructor
public class Category {
    @NonNull
    private String categoryName;
    private List<SubCategory> subCategories;
}
