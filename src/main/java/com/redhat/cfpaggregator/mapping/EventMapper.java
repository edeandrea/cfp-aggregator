package com.redhat.cfpaggregator.mapping;

import java.util.Optional;
import java.util.TimeZone;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants.ComponentModel;

import com.redhat.cfpaggregator.client.cfpdev.CfpDevEventDetails;
import com.redhat.cfpaggregator.domain.Event;

@Mapper(componentModel = ComponentModel.JAKARTA_CDI)
public interface EventMapper {
  @Mapping(target = "portalName", source = "portalName")
  Event fromCfpDev(String portalName, CfpDevEventDetails eventDetails);

  default String fromTimeZone(TimeZone timeZone) {
    return Optional.ofNullable(timeZone)
        .map(TimeZone::getID)
        .orElse(null);
  }
}
