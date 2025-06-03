package com.HyundaiAutoever.ATS.config;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Configuration
public class JacksonConfig {

    private static final String DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern(DATE_TIME_FORMAT);

    @Bean
    @Primary
    public ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        
        // Configure JavaTimeModule with custom formatters
        JavaTimeModule javaTimeModule = new JavaTimeModule();
        
        // Custom LocalDateTime serializer and deserializer
        javaTimeModule.addSerializer(LocalDateTime.class, 
                new LocalDateTimeSerializer(DATE_TIME_FORMATTER));
        javaTimeModule.addDeserializer(LocalDateTime.class,
                new LocalDateTimeDeserializer(DATE_TIME_FORMATTER));
        
        objectMapper.registerModule(javaTimeModule);
        
        // Register custom module for BaseEntity fields
        objectMapper.registerModule(baseEntityModule());
        
        // Disable writing dates as timestamps (numeric values)
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        
        // Configure to prevent serialization failures
        objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        
        return objectMapper;
    }
    
    /**
     * Create a custom module for handling BaseEntity date fields consistently
     */
    private SimpleModule baseEntityModule() {
        SimpleModule module = new SimpleModule("BaseEntityModule");
        
        // Custom serializer for LocalDateTime that strictly follows our format
        module.addSerializer(LocalDateTime.class, new StdSerializer<LocalDateTime>(LocalDateTime.class) {
            @Override
            public void serialize(LocalDateTime value, JsonGenerator gen, SerializerProvider provider) throws IOException {
                if (value != null) {
                    gen.writeString(DATE_TIME_FORMATTER.format(value));
                } else {
                    gen.writeNull();
                }
            }
        });
        
        // Custom deserializer for LocalDateTime
        module.addDeserializer(LocalDateTime.class, new StdDeserializer<LocalDateTime>(LocalDateTime.class) {
            @Override
            public LocalDateTime deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
                String dateString = p.getText();
                if (dateString != null && !dateString.isEmpty()) {
                    return LocalDateTime.parse(dateString, DATE_TIME_FORMATTER);
                }
                return null;
            }
        });
        
        return module;
    }
} 