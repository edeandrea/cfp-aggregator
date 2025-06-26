package com.redhat.cfpaggregator.mapping;

import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants.ComponentModel;

import com.redhat.cfpaggregator.client.cfpdev.CfpDevSpeakerDetails;
import com.redhat.cfpaggregator.domain.Speaker;

@Mapper(componentModel = ComponentModel.JAKARTA_CDI)
public interface SpeakerMapper {
  Speaker fromCfpDev(CfpDevSpeakerDetails speakerDetails);
}
