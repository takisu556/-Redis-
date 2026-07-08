package com.example.cart;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

@Configuration
public class JacksonConfig {
    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        // 保持原有设置：不转义非 ASCII 字符
        mapper.getFactory().configure(JsonGenerator.Feature.ESCAPE_NON_ASCII, false);
        // 注册 JavaTime 模块以支持 java.time.* 类型（例如 Instant）序列化/反序列化
        mapper.registerModule(new JavaTimeModule());
        // 使用 ISO-8601 字符串而不是时间戳
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return mapper;
    }
}
