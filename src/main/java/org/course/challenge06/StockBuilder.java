package org.course.challenge06;

public class StockBuilder {
    private Stock newStock;

    private StockBuilder(Stock Stock) {
        this.newStock = Stock;
    }

    public static StockBuilder from(Stock Stock) {
        Stock newStock = new Stock(Stock.getId(), Stock.getSymbol(), Stock.getPrice());
        return new StockBuilder(newStock);
    }

    public StockBuilder withPrice(Double price) {
        newStock = new Stock(newStock.getId(), newStock.getSymbol(), price);
        return this;
    }

    public Stock build() {
        return newStock;
    }
}
