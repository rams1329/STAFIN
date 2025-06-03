package com.HyundaiAutoever.ATS.config;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.ContextualSerializer;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Custom serializer for LocalDateTime fields that formats them according to a standard pattern.
 * This helps ensure consistent date formatting across the application, especially for auditing fields.
 */
public class FormattedDateTimeSerializer extends StdSerializer<LocalDateTime> implements ContextualSerializer {
    
    private static final String DEFAULT_DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
    private final DateTimeFormatter formatter;
    
    public FormattedDateTimeSerializer() {
        this(DEFAULT_DATE_TIME_FORMAT);
    }
    
    public FormattedDateTimeSerializer(String pattern) {
        super(LocalDateTime.class);
        this.formatter = DateTimeFormatter.ofPattern(pattern);
    }
    
    @Override
    public void serialize(LocalDateTime value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        if (value != null) {
            gen.writeString(formatter.format(value));
        } else {
            gen.writeNull();
        }
    }
    
    @Override
    public JsonSerializer<?> createContextual(SerializerProvider prov, BeanProperty property) throws JsonMappingException {
        return this;
    }
} 