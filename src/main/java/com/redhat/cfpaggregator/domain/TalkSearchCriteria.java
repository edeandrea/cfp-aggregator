package com.redhat.cfpaggregator.domain;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * Search criteria for searching an event for speakers from a set of companies and
 * talks with certain keywords.
 *
 * @author Eric Deandrea
 */
public final class TalkSearchCriteria {
  private final Set<String> talkKeywords = new LinkedHashSet<>();
  private final Set<String> speakerCompanies = new LinkedHashSet<>();

  private TalkSearchCriteria() {
    // Private constructor to enforce builder usage
  }

  private TalkSearchCriteria(Builder builder) {
    this.talkKeywords.addAll(builder.talkKeywords);
    this.speakerCompanies.addAll(builder.speakerCompanies);
  }

  public boolean hasTalkKeywords() {
    return !this.talkKeywords.isEmpty();
  }

  public boolean hasSpeakerCompanies() {
    return !this.speakerCompanies.isEmpty();
  }

  public Collection<String> getTalkKeywords() {
    return Set.copyOf(talkKeywords);
  }

  public Collection<String> getSpeakerCompanies() {
    return Set.copyOf(speakerCompanies);
  }

  public static Builder builder() {
    return new Builder();
  }

  public Builder toBuilder() {
    return new Builder(this);
  }

  @Override
  public String toString() {
    return "TalkSearchCriteria{" +
        "talkKeywords=" + talkKeywords +
        ", speakerCompanies=" + speakerCompanies +
        '}';
  }

  public static class Builder {
    private final Set<String> talkKeywords = new LinkedHashSet<>();
    private final Set<String> speakerCompanies = new LinkedHashSet<>();

    private Builder() {
      // Default constructor
    }

    private Builder(TalkSearchCriteria criteria) {
      this.talkKeywords.addAll(criteria.talkKeywords);
      this.speakerCompanies.addAll(criteria.speakerCompanies);
    }

    public Builder talkKeywords(String... keywords) {
      return talkKeywords(
          Optional.ofNullable(keywords)
              .map(Set::of)
              .orElseGet(Set::of)
      );
    }

    public Builder talkKeywords(Collection<String> keywords) {
      if (keywords != null) {
        keywords.stream()
            .filter(Objects::nonNull)
            .map(String::strip)
            .forEach(this.talkKeywords::add);
      }

      return this;
    }

    public Builder speakerCompanies(String... companies) {
      return speakerCompanies(
          Optional.ofNullable(companies)
              .map(Set::of)
              .orElseGet(Set::of)
      );
    }

    public Builder speakerCompanies(Collection<String> companies) {
      if (companies != null) {
        companies.stream()
            .filter(Objects::nonNull)
            .map(String::strip)
            .forEach(this.speakerCompanies::add);
      }

      return this;
    }

    public Builder clearTalkKeywords() {
      this.talkKeywords.clear();
      return this;
    }

    public Builder clearSpeakerCompanies() {
      this.speakerCompanies.clear();
      return this;
    }

    public TalkSearchCriteria build() {
      return new TalkSearchCriteria(this);
    }
  }
}