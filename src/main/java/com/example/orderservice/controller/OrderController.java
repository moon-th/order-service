package com.example.orderservice.controller;

import com.example.orderservice.dto.OrderDto;
import com.example.orderservice.entity.OrderEntity;
import com.example.orderservice.messagequeue.KafkaProducer;
import com.example.orderservice.messagequeue.OrderProducer;
import com.example.orderservice.service.OrderService;
import com.example.orderservice.vo.RequestOrder;
import com.example.orderservice.vo.ResponseOrder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/order-service")
public class OrderController {

    private final OrderService orderService;

    private final Environment env;

    private final KafkaProducer kafkaProducer;

    private final OrderProducer orderProducer;

    /**
     * 통신 체크
     * @return
     */
    @GetMapping("/health_check")
    public String status(){
        return String.format("It's Working in Order Service on PORT %s",
                env.getProperty("local.server.port"));
    }

    /**
     * order 생성
     * @param requestOrder
     * @param userId
     * @return
     */
    @PostMapping("/{userId}/orders")
    public ResponseEntity<ResponseOrder> createOrder(@RequestBody RequestOrder requestOrder,
                                                     @PathVariable("userId") String userId){
        log.info("Before retrieve order data ");
        ModelMapper mapper = new ModelMapper();

        mapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);


        OrderDto orderDto = mapper.map(requestOrder, OrderDto.class);
        orderDto.setUserId(userId);

        /* jpa */
        OrderDto resultOrder = orderService.createOrder(orderDto);
        ResponseOrder responseOrder = mapper.map(resultOrder, ResponseOrder.class);

        /* kafka */
//        orderDto.setOrderId(UUID.randomUUID().toString()); // ID
//        orderDto.setTotalPrice(requestOrder.getQty() * requestOrder.getUnitPrice());// 전체금액



        /* send this order to the kafka */
        kafkaProducer.send("example-catalog-topic",orderDto);
//        orderProducer.send("orders", orderDto);

//        ResponseOrder responseOrder = mapper.map(orderDto, ResponseOrder.class);
        log.info("After added order data ");
        return ResponseEntity.status(HttpStatus.CREATED).body(responseOrder);
    }

    /**
     * order 조회
     * @param userId
     * @return
     */
    @GetMapping("/{userId}/orders")
    public ResponseEntity<List<ResponseOrder>> getOrder(@PathVariable("userId") String userId) throws Exception {
        log.info("Before retrieve order data ");
        Iterable<OrderEntity> orderList = orderService.getOrderByUserId(userId);


        List<ResponseOrder> responseOrderList = new ArrayList<>();
        orderList.forEach(ol -> {
            responseOrderList.add(new ModelMapper().map(ol, ResponseOrder.class));
        });


//        try {
//            Thread.sleep(1000);
//            throw new Exception(" 장애 발생 ");
//
//        }catch(InterruptedException e){
//            log.warn(e.getMessage());
//        }

        log.info("After added order data ");

        return ResponseEntity.status(HttpStatus.OK).body(responseOrderList);
    }
}
