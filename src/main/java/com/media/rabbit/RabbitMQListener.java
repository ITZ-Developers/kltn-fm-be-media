package com.media.rabbit;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.media.form.rabbit.BaseSendMsgForm;
import com.media.form.rabbit.DeleteMediaRestaurantForm;
import com.media.constant.MediaConstant;
import com.media.service.MediaApiService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class RabbitMQListener {
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private MediaApiService orgMediaApiService;
    @Value("${rabbitmq.media.queue}")
    private String queueName;

    @RabbitListener(queues = "${rabbitmq.media.queue}")
    public void receiveMessage(String message) {
        try {
            BaseSendMsgForm<DeleteMediaRestaurantForm> baseMessageForm = objectMapper.readValue(message, new TypeReference<>() {});
            System.out.println("======> Received message from " + queueName + ": " + message);
            if (baseMessageForm.getCmd().equals(MediaConstant.CMD_DELETE_MEDIA_TENANT) && baseMessageForm.getData().getTenantId() != null) {
                String tenantId = baseMessageForm.getData().getTenantId();
                orgMediaApiService.deleteFolder(tenantId);
            }
        } catch (Exception e) {
            log.error("Error processing received message: {}", e.getMessage(), e);
        }
    }
}
