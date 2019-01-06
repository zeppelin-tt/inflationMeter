package inflationmeter.controller;

import inflationmeter.exception.CounterFullException;
import inflationmeter.service.ParseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.IOException;

@Controller
@RequestMapping("/parse")
public class ParsingController {

    private final ParseService parseService;

    @Autowired
    public ParsingController(ParseService parseService) {
        this.parseService = parseService;
    }

    @GetMapping("/start")
    public ResponseEntity main () throws CounterFullException, IOException {
        parseService.startParse();
        return new ResponseEntity<>(HttpStatus.ACCEPTED);
    }
}
