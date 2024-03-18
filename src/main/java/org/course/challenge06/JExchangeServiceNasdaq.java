package org.course.challenge06;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JExchangeServiceNasdaq extends JExchangeService {

    public JExchangeServiceNasdaq(@Value("${remote.service.url}") String baseUrl) {
        super(baseUrl, "nasdaq");
    }
}
