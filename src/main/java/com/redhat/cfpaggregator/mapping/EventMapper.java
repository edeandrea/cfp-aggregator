package com.redhat.cfpaggregator.mapping;

import java.util.Optional;
import java.util.TimeZone;

import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants.ComponentModel;

import com.redhat.cfpaggregator.client.cfpdev.CfpDevEventDetails;
import com.redhat.cfpaggregator.domain.Event;

@Mapper(componentModel = ComponentModel.JAKARTA_CDI)
public interface EventMapper {
  Event fromCfpDev(CfpDevEventDetails eventDetails);

  default String fromTimeZone(TimeZone timeZone) {
    return Optional.ofNullable(timeZone)
        .map(TimeZone::getDisplayName)
        .orElse(null);
  }
}
