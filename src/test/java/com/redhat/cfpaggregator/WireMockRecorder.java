package com.redhat.cfpaggregator;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import io.quarkus.test.common.QuarkusTestResource;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@QuarkusTestResource(value = WiremockRecorderTestResourceManager.class, restrictToAnnotatedClass = true)
public @interface WireMockRecorder {
  String baseUrl();
  String portalName();
}
