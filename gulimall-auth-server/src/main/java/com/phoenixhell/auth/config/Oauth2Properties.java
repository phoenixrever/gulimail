package com.phoenixhell.auth.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix = "github")
@Component
@Data
public class Oauth2Properties {
    private String clientID;
    private String clientSecrets;
    private String authorizeUrl;
    private String callbackUrl;
    private String accessTokenUrl;
    private String userInfoUrl;
}
