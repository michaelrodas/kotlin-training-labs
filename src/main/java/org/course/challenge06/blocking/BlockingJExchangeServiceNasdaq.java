package org.course.challenge06.blocking;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class BlockingJExchangeServiceNasdaq extends BlockingJExchangeService {

    public BlockingJExchangeServiceNasdaq(@Value("${remote.service.url}") String baseUrl) {
        super(baseUrl, "nasdaq");
    }
}
