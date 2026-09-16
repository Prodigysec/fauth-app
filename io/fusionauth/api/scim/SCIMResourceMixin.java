package io.fusionauth.api.scim;

import com.fasterxml.jackson.annotation.JsonFilter;

@JsonFilter("excludedAttributes")
public interface SCIMResourceMixin {}
