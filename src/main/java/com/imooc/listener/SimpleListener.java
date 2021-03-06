package com.imooc.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.imooc.model.RedRecord;
import com.rabbitmq.client.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.core.ChannelAwareMessageListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("simpleListener")
public class SimpleListener implements ChannelAwareMessageListener {
    private static final Logger logger = LoggerFactory.getLogger(SimpleListener.class);

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public void onMessage(Message message, Channel channel) throws Exception {
        long tag = message.getMessageProperties().getDeliveryTag();

        try {
            byte[] msg = message.getBody();
            RedRecord user = objectMapper.readValue(msg, RedRecord.class);
            logger.info("简单消息监听确认机制监听到消息： {} ", user);

            /*
                deliveryTag（消息的唯一标识 ID）
                multiple：为了减少网络流量，手动确认可以被批处理，当该参数为 true 时，则可以一次性确认 delivery_tag 小于等于传入值的所有消息
             */
            channel.basicAck(tag, true);
        } catch (Exception e) {
            logger.error("简单消息监听确认机制发生异常：", e.fillInStackTrace());

            channel.basicReject(tag, false);
        }
    }
}
