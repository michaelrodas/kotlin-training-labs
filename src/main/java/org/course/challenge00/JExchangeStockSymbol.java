package org.course.challenge00;

import java.util.Objects;

public class JExchangeStockSymbol {

    private String exchange;
    private String symbol;

    public JExchangeStockSymbol(String exchange, String symbol) {
        this.exchange = exchange;
        this.symbol = symbol;
    }

    public String getExchange() {
        return exchange;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setExchange(String exchange) {
        this.exchange = exchange;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        JExchangeStockSymbol that = (JExchangeStockSymbol) o;
        return Objects.equals(exchange, that.exchange) && Objects.equals(symbol, that.symbol);
    }

    @Override
    public int hashCode() {
        return Objects.hash(exchange, symbol);
    }

    @Override
    public String toString() {
        return "JStockExchangeKey{" +
                "exchange='" + exchange + '\'' +
                ", symbol='" + symbol + '\'' +
                '}';
    }
}


