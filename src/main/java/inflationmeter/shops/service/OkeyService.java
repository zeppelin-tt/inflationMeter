package inflationmeter.shops.service;

import inflationmeter.exception.CounterFullException;
import inflationmeter.parse.Connect;
import inflationmeter.parse.ConnectionType;
import inflationmeter.shops.model.Category;
import inflationmeter.shops.model.Shop;
import inflationmeter.shops.model.SubCategory;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class OkeyService {

    private static final String SHOP_URL = "https://www.okeydostavka.ru";
    private static final String CATEGORIES_URL = "/spb/catalog";

    private final Connect connect;

    @Autowired
    public OkeyService(Connect connect) {
        this.connect = connect;
    }

    public void aroundTheStore() throws CounterFullException, IOException {
        Shop shop = new Shop("OKEY", SHOP_URL + CATEGORIES_URL);
        List<Category> categories = getCategories();
        for (Category category : categories) {
            getSubCategories(category);
        }

    }


    private List<Category> getCategories() throws CounterFullException, IOException {
        List<Category> categories = new ArrayList<>();
        Element doc = connect.pretend(SHOP_URL + CATEGORIES_URL, ConnectionType.JSOUP);
        Elements elementCategories = doc.getElementsByClass("row categories").get(0).getElementsByClass("col-xs-3 col-lg-2 col-xl-special");
        for (Element element : elementCategories) {
            categories.add(new Category(
                            element.getElementsByTag("h2").get(0).getElementsByTag("a").get(0).ownText(),
                            element.getElementsByTag("h2").get(0).getElementsByTag("a").get(0).attributes().get("href")
                    )
            );
        }
        log.info("Категории успешно загрузились");
        return categories;
    }

    private void getSubCategories(Category category) throws CounterFullException, IOException {
        List<SubCategory> subCategories = new ArrayList<>();
        Element doc = connect.pretend(SHOP_URL + category.getUrl(), ConnectionType.JSOUP);
        Elements elementSubCategories = doc.getElementsByClass("col-xs-3 col-lg-2 col-xl-special");
        for (Element element : elementSubCategories) {
            element.ownText();
        }

    }
}
