package com.redhat.cfpaggregator.ui.templates;

import java.util.Collection;

import io.quarkus.qute.CheckedTemplate;
import io.quarkus.qute.TemplateInstance;

import com.redhat.cfpaggregator.domain.Portal;

@CheckedTemplate(basePath = "exportConfig", defaultName = CheckedTemplate.HYPHENATED_ELEMENT_NAME)
public class ExportConfigTemplates {
  public static native TemplateInstance portalsConfig(Collection<Portal> portals);
}
