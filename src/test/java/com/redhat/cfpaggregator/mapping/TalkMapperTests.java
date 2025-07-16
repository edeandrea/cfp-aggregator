package com.redhat.cfpaggregator.mapping;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import jakarta.inject.Inject;

import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;

import com.redhat.cfpaggregator.client.cfpdev.CfpDevSpeakerDetails;
import com.redhat.cfpaggregator.client.cfpdev.CfpDevTalkDetails;
import com.redhat.cfpaggregator.client.cfpdev.CfpDevTalkDetails.Keyword;
import com.redhat.cfpaggregator.domain.Talk;

@QuarkusTest
class TalkMapperTests {
  @Inject
  TalkMapper talkMapper;

  @Test
  void talkFromCfpDev() throws MalformedURLException {
    var speakerDetails = new CfpDevSpeakerDetails(
        1234567890,
        "John",
        "Doe",
        "This is John's bio",
        "Fake Company",
        new URL("https://flikr.com/1"),
        "@johndoe",
        "johndoe",
        "johndoe.bsky.social",
        "USA"
    );

    var talkDetails = new CfpDevTalkDetails(
        987654321L,
        "Talk title",
        "Talk description",
        "Talk summary",
        new URL("https://www.youtube.com"),
        List.of(new Keyword("quarkus"), new Keyword("Java")),
        List.of(speakerDetails)
    );

    var expectedTalk = Talk.builder()
        .eventTalkId("987654321")
        .title("Talk title")
        .description("Talk description")
        .summary("Talk summary")
        .videoUrl("https://www.youtube.com")
        .build();

    assertThat(this.talkMapper.fromCfpDev(talkDetails))
        .isNotNull()
        .usingRecursiveComparison()
        .isEqualTo(expectedTalk);
  }
}