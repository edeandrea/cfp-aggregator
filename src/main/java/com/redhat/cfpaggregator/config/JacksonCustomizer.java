package com.redhat.cfpaggregator.config;

import java.io.IOException;

import jakarta.inject.Singleton;

import io.quarkus.jackson.ObjectMapperCustomizer;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.deser.std.StringDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;

@Singleton
public class JacksonCustomizer implements ObjectMapperCustomizer {
  @Override
  public void customize(ObjectMapper objectMapper) {
    objectMapper.enable(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT);

    var module = new SimpleModule();
    module.addDeserializer(String.class, new EmptyStringToNullDeserializer());
    objectMapper.registerModule(module);
  }

  private class EmptyStringToNullDeserializer extends StdDeserializer<String> {
    protected EmptyStringToNullDeserializer() {
      super(String.class);
    }

    @Override
    public String deserialize(JsonParser jsonParser, DeserializationContext ctx) throws IOException, JacksonException {
      var result = StringDeserializer.instance.deserialize(jsonParser, ctx);

      return (result == null || result.strip().isEmpty()) ?
          null :
          result;
    }
  }
}
