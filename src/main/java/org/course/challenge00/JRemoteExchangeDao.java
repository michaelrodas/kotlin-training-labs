package org.course.challenge00;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;

public class JRemoteExchangeDao {

    private final static Logger logger = LoggerFactory.getLogger(JRemoteExchangeDao.class);
    private final List<JExchange> exchanges;

    public JRemoteExchangeDao() {
        exchanges = loadExchanges("/exchanges.json");
    }

    public JRemoteExchangeDao(String path) {
        exchanges = loadExchanges(path);
    }

    public List<JExchange> findAll() {
        return exchanges;
    }

    public Optional<JExchange> findById(String exchangeId) {
        return this.exchanges.stream().filter(e -> e.getId().equals(exchangeId)).findFirst();
    }

    public static List<JExchange> loadExchanges(String path) {
        ObjectMapper mapper = new ObjectMapper();
        InputStream inputStream = JRemoteExchangeDao.class.getResourceAsStream(path);
        try {
            if (inputStream == null) throw new IllegalArgumentException(path + " is invalid");
            return mapper.readValue(inputStream, new TypeReference<List<JExchange>>() {});
        } catch (IOException e) {
            throw new IllegalArgumentException("Could not parse " + path, e);
        }
    }

}
