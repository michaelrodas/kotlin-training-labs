package org.course.challenge00;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

public class JRemoteExchangeService {

    private final static Logger logger = LoggerFactory.getLogger(JRemoteExchangeService.class);

    private final JRemoteStockPriceDao stockPriceDao;
    private final JRemoteExchangeDao exchangeDao;

    public JRemoteExchangeService(JRemoteStockPriceDao stockPriceDao, JRemoteExchangeDao exchangeDao) {
        this.stockPriceDao = stockPriceDao;
        this.exchangeDao = exchangeDao;
    }

    public Optional<JStockPrice> getStockPrice(String exchangeId, String symbol) {
        for(JStockPrice stockPrice:stockPriceDao.findById(exchangeId)) {
            if(stockPrice.getSymbol().equals(symbol)) {
                return Optional.of(stockPrice);
            }
        }
        return Optional.empty();
    }


    public void addStockPrice(String exchangeId, JStockPrice newStockPrice) {
        //validate
        if(exchangeDao.findById(exchangeId).isEmpty()) {
            String message = exchangeId + "does not exist";
            logger.error(message);
            throw new IllegalArgumentException(message);
        }

        Optional<JStockPrice> currentStockPrice  = getStockPrice(exchangeId, newStockPrice.getSymbol());
        stockPriceDao.save(exchangeId, defineStockPrice(currentStockPrice, newStockPrice));
    }


    private JStockPrice defineStockPrice(Optional<JStockPrice> currentStockPriceOpt, JStockPrice newStockPrice) {
        JStockPrice changedStockPrice = null;
        if(currentStockPriceOpt.isEmpty()) {
            logger.info("new stock: {}",newStockPrice);
            changedStockPrice = newStockPrice;
        } else {
            JStockPrice currentStockPrice = currentStockPriceOpt.get();
            if(currentStockPrice.getPrice() > newStockPrice.getPrice()) {
                changedStockPrice = JStockPrice.Builder.withNewPrice(currentStockPrice, newStockPrice.getPrice()).build();
                logger.info("stock decreased: {}", changedStockPrice);
            } else if(currentStockPrice.getPrice() < newStockPrice.getPrice()){
                changedStockPrice = JStockPrice.Builder.withNewPrice(currentStockPrice, newStockPrice.getPrice()).build();
                logger.info("stock increased: {}", changedStockPrice);
            } else {
                changedStockPrice = currentStockPrice;
                logger.info("stock no change");
            }
        }
        return changedStockPrice;
    }

}
