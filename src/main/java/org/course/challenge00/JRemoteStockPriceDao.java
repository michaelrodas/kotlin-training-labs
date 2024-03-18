package org.course.challenge00;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class JRemoteStockPriceDao {

    private final static Logger logger = LoggerFactory.getLogger(JRemoteStockPriceDao.class);

    private final ConcurrentHashMap<JExchangeStockSymbol, JStockPrice> stockPriceMemRepo = new ConcurrentHashMap<>();


    public List<JStockPrice> findById(String exchangeId) {
        return stockPriceMemRepo.keySet().stream().filter(p -> p.getExchange().equals(exchangeId)).map(stockPriceMemRepo::get).collect(Collectors.toList());
    }

    public void save(String exchange, JStockPrice newStockPrice) {
        stockPriceMemRepo.put(new JExchangeStockSymbol(exchange, newStockPrice.getSymbol()), newStockPrice);
    }


    public Set<JExchangeStockSymbol> getAllStockSymbolsWithExchange() {
        return stockPriceMemRepo.keySet();
    }


    public void clearMemRepo() {
        stockPriceMemRepo.clear();
    }

}
