
    package org.course.challenge06.blocking;

    import org.course.challenge06.StockQuoteDto;
    import org.jetbrains.annotations.Nullable;
    import org.springframework.boot.web.client.RestTemplateBuilder;
    import org.springframework.web.client.RestTemplate;

    abstract public class BlockingJExchangeService {

        private RestTemplate restTemplate;
        public final String baseUrl;
        private final String exchangeId;

        public BlockingJExchangeService(String baseUrl, String exchangeId) {
            this.baseUrl = baseUrl;
            this.exchangeId = exchangeId;
        }

        public StockQuoteDto getStockQuote(String stockSymbol, @Nullable Long delay) {
            var url = "/quotes?symbol=" + stockSymbol + "&exchange=" + exchangeId + (delay != null ? "&delay=" + delay : "");
            return getRestTemplate().getForEntity(url, StockQuoteDto.class).getBody();
        }


        public String getExchangeId() {
            return exchangeId;
        }

        private RestTemplate getRestTemplate() {
            if (restTemplate == null) {
                this.restTemplate = new RestTemplateBuilder().rootUri(baseUrl).build();
            }
            return restTemplate;
        }


}
