package org.course.challenge00;

import java.time.Instant;

public class JStockPrice {
    private final String symbol;
    private final Double price;
    private final Double changePercentage;
    private final Instant lastChange;

    public JStockPrice( String symbol,  Double price, Double changePercentage, Instant lastChange) {
        this.symbol = symbol;
        this.price = price;
        this.changePercentage = changePercentage;
        this.lastChange = lastChange;
    }

    public JStockPrice( String symbol,  Double price) {
        this.symbol = symbol;
        this.price = price;
        this.changePercentage = 0.0;
        this.lastChange = Instant.now();
    }

    public String getSymbol() {
        return symbol;
    }

    public Double getPrice() {
        return price;
    }

    public Double getChangePercentage() {
        return changePercentage;
    }

    public Instant getLastChange() {
        return lastChange;
    }

    @Override
    public String toString() {
        return "JStockPrice{" +
                "symbol='" + symbol + '\'' +
                ", price=" + price +
                ", changePercentage=" + changePercentage +
                ", lastChange=" + lastChange +
                '}';
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String symbol;
        private Double price;
        private Double changePercentage = 0.0;
        private Instant lastChange = Instant.now();


        public static Builder withNewPrice(JStockPrice from, Double newPrice) {
            Builder builder = new Builder();
            builder.symbol = from.symbol;
            builder.price = newPrice;
            builder.changePercentage = changePercentage(from.price, newPrice);
            builder.lastChange = from.lastChange;
            return builder;
        }


        public Builder withSymbol(String symbol) {
            this.symbol = symbol;
            return this;
        }

        public Builder withPrice(Double price) {
            this.price = price;
            return this;
        }
        public Builder withLastChange(Instant lastChange) {
            this.lastChange = lastChange;
            return this;
        }


        public JStockPrice build() {
            return new JStockPrice(symbol, price, changePercentage, lastChange);
        }
    }

    public static Double changePercentage(Double newPrice, Double currentPrice) {
        return ((newPrice - currentPrice) * 100) / currentPrice;
    }
}
