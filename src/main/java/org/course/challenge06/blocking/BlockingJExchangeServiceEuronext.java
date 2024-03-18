package org.course.challenge06.blocking;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class BlockingJExchangeServiceEuronext extends BlockingJExchangeService {

    public BlockingJExchangeServiceEuronext(@Value("${remote.service.url}") String baseUrl) {
        super( baseUrl, "euronext");
    }
}
