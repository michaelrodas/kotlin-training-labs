package org.course.challenge00;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Currency;

public class JExchange {
    private final String id;
    private final String name;
    private final String location;
    private final Currency currency;

    @JsonCreator
    public JExchange( @JsonProperty("id") String id, @JsonProperty("name") String name, @JsonProperty("location") String location, @JsonProperty("currency") Currency currency) {
        this.id = id;
        this.name = name;
        this.location = location;
        this.currency = currency;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getLocation() {
        return location;
    }

    public Currency getCurrency() {
        return currency;
    }

    @Override
    public String toString() {
        return "JExchange{" +
                "name='" + name + '\'' +
                ", location='" + location + '\'' +
                ", currency=" + currency +
                ", id='" + id + '\'' +
                '}';
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String id;
        private String name;
        private String location;
        private Currency currency;

        public Builder withId(String id) {
            this.id = id;
            return this;
        }

        public Builder withName(String name) {
            this.name = name;
            return this;
        }

        public Builder withLocation(String location) {
            this.location = location;
            return this;
        }

        public Builder withCurrency(Currency currency) {
            this.currency = currency;
            return this;
        }


        public JExchange build() {
            return new JExchange(id, name, location, currency);
        }
    }
}
