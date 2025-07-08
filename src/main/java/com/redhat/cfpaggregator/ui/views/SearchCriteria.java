package com.redhat.cfpaggregator.ui.views;

import java.util.Objects;

public final class SearchCriteria {
  private String keyword;

  public SearchCriteria(String keyword) {
    this.keyword = keyword;
  }

  public SearchCriteria() {
  }

  public String getKeyword() {
    return keyword;
  }

  public void setKeyword(String keyword) {
    this.keyword = keyword;
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof SearchCriteria that)) return false;
    return Objects.equals(keyword, that.keyword);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(keyword);
  }

  @Override
  public String toString() {
    return "SearchCriteria{" +
        "keyword='" + keyword + '\'' +
        '}';
  }
}
