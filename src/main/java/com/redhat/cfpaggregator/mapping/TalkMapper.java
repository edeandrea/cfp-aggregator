package com.redhat.cfpaggregator.mapping;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants.ComponentModel;

import com.redhat.cfpaggregator.client.cfpdev.CfpDevTalkDetails;
import com.redhat.cfpaggregator.domain.Talk;

@Mapper(componentModel = ComponentModel.JAKARTA_CDI)
public interface TalkMapper {
  @Mapping(target = "speakers", ignore = true)
  Talk fromCfpDev(CfpDevTalkDetails talkDetails);
}
