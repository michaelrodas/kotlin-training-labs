package org.course.challenge00;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

//@RestController
public class JRemoteExchangeController {

    private final JRemoteExchangeService exchangeService;

    //@Autowired
    public JRemoteExchangeController(JRemoteExchangeService remoteExchangeService) {
        this.exchangeService = remoteExchangeService;
    }

    //@GetMapping("/jexchanges/{exchangeId}/stocks")
    //@ResponseBody
    public JStockQuoteDTO getStockPrice(@PathVariable("exchangeId") String exchangeId, @RequestParam("symbol") String symbol) {
        Optional<Double> price = exchangeService.getStockPrice(exchangeId, symbol).map(JStockPrice::getPrice);
        if(price.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Symbol: " + symbol + " not found for Exchange with id: " + exchangeId);
        } else {
            return JStockQuoteDTO.builder().withSymbol(symbol).withPrice(price.get()).build();
        }
    }

    //@PutMapping("/jexchanges/{exchangeId}/stocks")
    //@ResponseBody
    public JStockQuoteDTO putStockPrice(@PathVariable("exchangeId") String exchangeId, @RequestBody JStockQuoteDTO stockPrice) {
        exchangeService.addStockPrice(exchangeId, JStockPrice.builder().withSymbol(stockPrice.getSymbol()).withPrice(stockPrice.getCurrentPrice()).build());
        return stockPrice;
    }

}

