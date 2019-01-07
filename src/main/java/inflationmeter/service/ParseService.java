package inflationmeter.service;

import inflationmeter.exception.CounterFullException;
import inflationmeter.parse.Connect;
import inflationmeter.shops.service.OkeyService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Slf4j
@Service
public class ParseService {

    private final DbUtilService dbUtilService;
    private final Connect connect;
    private final OkeyService okeyService;

    @Autowired
    public ParseService(DbUtilService dbUtilService, Connect connect, OkeyService service) {
        this.dbUtilService = dbUtilService;
        this.connect = connect;
        this.okeyService = service;
    }


    public String startParse() throws CounterFullException {
        dbUtilService.updateProxyDb();

//        okeyService.aroundTheStore();


        return null;
    }


}
