package com.redhat.cfpaggregator.mapping;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants.ComponentModel;
import org.mapstruct.MappingTarget;

import com.redhat.cfpaggregator.client.cfpdev.CfpDevSpeakerDetails;
import com.redhat.cfpaggregator.client.sessionize.SessionizeSpeakerDetails;
import com.redhat.cfpaggregator.client.sessionize.SessionizeSpeakerDetails.Link;
import com.redhat.cfpaggregator.domain.Speaker;

@Mapper(componentModel = ComponentModel.JAKARTA_CDI)
public abstract class SpeakerMapper {
  public abstract Speaker fromCfpDev(CfpDevSpeakerDetails speakerDetails);

  @Mapping(target = "imageUrl", source = "profilePicture")
  public abstract Speaker fromSessionize(SessionizeSpeakerDetails speakerDetails);

  @AfterMapping
  protected void fillInLinks(SessionizeSpeakerDetails speakerDetails, @MappingTarget Speaker speaker) {
    var links = speakerDetails.links();

    if (links != null) {
      speaker.setLinkedInUsername(getUsernameFromLink(findLink(links, "LinkedIn")));
      speaker.setTwitterHandle(
          getUsernameFromLink(
              findLink(links, "Twitter")
                  .or(() -> findLink(links, "X"))
          )
      );
      speaker.setBlueskyUsername(
          getUsernameFromLink(
              findLink(links, "Bluesky")
                  .or(() -> findLink(links, "Blue Sky"))
          )
      );
    }
  }

  private String getUsernameFromLink(Optional<Link> link) {
    return link.map(l -> Path.of(l.url().getPath()).getFileName().toString())
        .orElse(null);
  }

  private Optional<Link> findLink(List<Link> links, String type) {
    return links.stream()
        .filter(link ->
            Optional.ofNullable(link.linkType()).orElse("")
                .toLowerCase()
                .contains(type.toLowerCase())
        )
        .findFirst();
  }
}
