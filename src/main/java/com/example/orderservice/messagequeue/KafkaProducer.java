package com.example.orderservice.messagequeue;

import com.example.orderservice.dto.OrderDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class KafkaProducer {

    private KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    public KafkaProducer(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public OrderDto send(String topic, OrderDto orderDto) {
        ObjectMapper mapper = new ObjectMapper();

        String jsonInString = "";
        try {
            //해당 데이터의 JSON 타입을 전달 하기전 String 값으로 변환시켜 준다.
            jsonInString = mapper.writeValueAsString(orderDto);

        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        // kafkaTemplate 을 이용하여 전송
        kafkaTemplate.send(topic, jsonInString);
        log.info("kafka Producer sent data from the Order microservice : " + orderDto);
        return orderDto;
    }
}
