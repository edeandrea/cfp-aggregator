package com.redhat.cfpaggregator.mapping;

import java.util.Optional;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants.ComponentModel;

import com.redhat.cfpaggregator.config.CfpPortalsConfig.CfpPortalConfig;
import com.redhat.cfpaggregator.domain.Portal;

@Mapper(componentModel = ComponentModel.JAKARTA_CDI)
public interface PortalMapper {
  @Mapping(target = "portalName", source = "portalName")
  Portal fromConfig(String portalName, CfpPortalConfig portalConfig);

  default <T> T fromOptional(Optional<T> optional) {
    return optional.orElse(null);
  }
}
