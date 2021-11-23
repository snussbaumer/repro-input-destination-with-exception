package com.example.reproinputdestinationwithexception;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.function.Consumer;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.stream.binder.test.InputDestination;
import org.springframework.cloud.stream.binder.test.TestChannelBinderConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.support.MessageBuilder;

@SpringBootTest(properties = { "spring.cloud.function.definition=consumerThatCanFail",
        "spring.cloud.stream.bindings.consumerThatCanFail-in-0.destination=consumerThatCanFail",
        "spring.cloud.stream.bindings.consumerThatCanFail-in-0.consumer.max-attempts=1" })
@Import(TestChannelBinderConfiguration.class)
class ReproInputDestinationWithExceptionApplicationTests {

    @Autowired
    private InputDestination inputDestination;

    @Test
    void test() {
        MessagingException e = assertThrows(MessagingException.class, //
                () -> inputDestination.send(MessageBuilder.withPayload("dummy").build()));
        assertThat(e.getCause()).isInstanceOfAny(RuntimeException.class);
        assertThat(e.getCause().getMessage()).isEqualTo("Dummy error");
    }

    @SpringBootApplication
    public static class TestApplication {

        private final static Logger logger = LoggerFactory.getLogger(TestApplication.class);

        public static void main(String[] args) {
            SpringApplication.run(TestApplication.class, args);
        }

        @Bean
        public Consumer<String> consumerThatCanFail() {
            return s -> {
                logger.info("Got a message : {}", s);
                if (s.equals("dummy")) {
                    throw new RuntimeException("Dummy error");
                }
            };
        }

    }

}
