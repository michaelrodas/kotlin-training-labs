package org.course.challenge00;

public class JStockQuoteDTO {
    final String symbol;
    final Double currentPrice;

    public JStockQuoteDTO(String symbol, Double currentPrice) {
        this.symbol = symbol;
        this.currentPrice = currentPrice;
    }

    public String getSymbol() {
        return symbol;
    }

    public Double getCurrentPrice() {
        return currentPrice;
    }

    @Override
    public String toString() {
        return "JStockQuoteDto{" +
                "symbol='" + symbol + '\'' +
                ", currentPrice=" + currentPrice +
                '}';
    }


    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String symbol;
        private Double currentPrice;


        public Builder withPrice(Double price) {
            this.currentPrice = price;
            return this;
        }

        public Builder withSymbol(String symbol) {
            this.symbol = symbol;
            return this;
        }

        public JStockQuoteDTO build() {
            return new JStockQuoteDTO(symbol, currentPrice);
        }
    }
}
