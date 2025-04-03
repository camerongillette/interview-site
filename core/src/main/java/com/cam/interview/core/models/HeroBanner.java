package com.cam.interview.core.models;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.Default;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.ValueMapValue;

import javax.annotation.PostConstruct;

@Model(
    adaptables = Resource.class,
    defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL
)
public class HeroBanner {

    @ValueMapValue
    private String title;

    @ValueMapValue
    private String subtitle;

    @ValueMapValue
    private String fileReference;

    @ValueMapValue
    private String linkURL;

    @ValueMapValue
    @Default(values = "light") // Default text color
    private String textColor;

    private boolean hasLink;

    @PostConstruct
    protected void init() {
        hasLink = (linkURL != null && !linkURL.isEmpty());
    }

    public String getTitle() {
        return title;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public String getFileReference() {
        return fileReference;
    }

    public String getLinkURL() {
        return linkURL;
    }

    public String getTextColor() {
        return textColor;
    }

    public boolean hasLink() {
        return hasLink;
    }
}
