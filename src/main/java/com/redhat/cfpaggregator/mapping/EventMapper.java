package com.redhat.cfpaggregator.mapping;

import java.util.Optional;
import java.util.TimeZone;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants.ComponentModel;

import com.redhat.cfpaggregator.client.cfpdev.CfpDevEventDetails;
import com.redhat.cfpaggregator.config.CfpPortalsConfig.PortalType;
import com.redhat.cfpaggregator.domain.Event;

@Mapper(componentModel = ComponentModel.JAKARTA_CDI)
public interface EventMapper {
  @Mapping(target = "portalName", source = "portalName")
  @Mapping(target = "portalType", source = "portalType")
  Event fromCfpDev(String portalName, PortalType portalType, CfpDevEventDetails eventDetails);

  default String fromTimeZone(TimeZone timeZone) {
    return Optional.ofNullable(timeZone)
        .map(TimeZone::getDisplayName)
        .orElse(null);
  }
}
