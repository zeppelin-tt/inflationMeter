package inflationmeter.shops.model;

import lombok.Data;
import lombok.NonNull;

import java.util.List;

@Data
public class Category {
    @NonNull
    private String categoryName;
    @NonNull
    private String url;
    private List<SubCategory> subCategories;
}
