package com.redhat.cfpaggregator.client;

import org.mapstruct.factory.Mappers;

import com.redhat.cfpaggregator.domain.Event;
import com.redhat.cfpaggregator.domain.Portal;
import com.redhat.cfpaggregator.domain.TalkSearchCriteria;
import com.redhat.cfpaggregator.mapping.EventMapper;
import com.redhat.cfpaggregator.mapping.SpeakerMapper;
import com.redhat.cfpaggregator.mapping.TalkMapper;

public interface CfpClient {
  String PORTAL_NAME_HEADER = "X-Portal-Name";
  EventMapper EVENT_MAPPER = Mappers.getMapper(EventMapper.class);
  TalkMapper TALK_MAPPER = Mappers.getMapper(TalkMapper.class);
  SpeakerMapper SPEAKER_MAPPER = Mappers.getMapper(SpeakerMapper.class);

  Event createEvent(Portal portal, TalkSearchCriteria talkSearchCriteria);
}
