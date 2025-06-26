package com.redhat.cfpaggregator.mapping;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants.ComponentModel;

import com.redhat.cfpaggregator.config.CfpPortalsConfig.DefaultSearchCriteria;
import com.redhat.cfpaggregator.domain.TalkSearchCriteria;

@Mapper(componentModel = ComponentModel.JAKARTA_CDI)
public interface TalkSearchCriteriaMapper {
  @Mapping(target = "speakerCompanies", expression = "java(configSearchCriteria.companies())")
  @Mapping(target = "talkKeywords", expression = "java(configSearchCriteria.talkKeywords())")
  TalkSearchCriteria fromConfig(DefaultSearchCriteria configSearchCriteria);
}
