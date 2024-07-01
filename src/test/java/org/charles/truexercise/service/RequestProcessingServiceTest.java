package org.charles.truexercise.service;

import org.charles.truexercise.TruExerciseApplication;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = TruExerciseApplication.class)
public class RequestProcessingServiceTest {
    @Autowired
    private RequestProcessingService requestProcessingService;

    @Autowired
    private RestTemplate restTemplate;

    private MockRestServiceServer mockServer;

    @BeforeEach
    public void init() {
        mockServer = MockRestServiceServer.createServer(restTemplate);
    }

    //todo - add unit tests to :-
    // - verify local db searched first when company number provided
    // - verify local db not searched first when company number not provided
    // - verify truProxyApi called when company number not provided or if not found in local db
    // and so on. Time is fleeting

}

