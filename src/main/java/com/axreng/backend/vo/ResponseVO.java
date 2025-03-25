package com.axreng.backend.vo;

import java.util.HashSet;
import java.util.Set;

public class ResponseVO {

    private String id;
    private Status status;
    private String urlBase;
    private Set<String> urls;

    public ResponseVO() {
        this.urls = new HashSet<>();
    }

    private ResponseVO(Builder builder) {
        this.id = builder.id;
        this.status = builder.status;
        this.urls = builder.urls;
        this.urlBase = builder.urlBase;
    }

    public String getId() {
        return id;
    }

    public String getUrlBase() {
        return urlBase;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public Set<String> getUrls() {
        return urls;
    }

    public void addUrl(String urls) {
        this.urls.add(urls);
    }

    public static Builder Builder() {
        return new Builder();
    }

    public static class Builder {

        private String id;
        private Status status;
        private String urlBase;
        private Set<String> urls;

        public Builder urls(String url) {
            if (this.urls == null || this.urls.isEmpty()) {
                this.urls = new HashSet<>();
            }
            this.urls.add(url);
            return this;
        }

        public Builder urlBase(String urlBase) {
            this.urlBase = urlBase;
            return this;
        }

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder status(Status status) {
            this.status = status;
            return this;
        }

        public ResponseVO build() {
            return new ResponseVO(this);
        }
    }
}
