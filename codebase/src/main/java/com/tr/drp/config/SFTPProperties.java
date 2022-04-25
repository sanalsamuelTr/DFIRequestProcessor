package com.tr.drp.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@Getter
@Setter
public class SFTPProperties {
    @Value("${app.sftp.host}")
    private String url;
    @Value("${app.sftp.port}")
    private Integer port;
    @Value("${app.sftp.username}")
    private String username;
    @Value("#{principalCryptor.decrypt('${app.sftp.password}')}")
    private String password;

    @Value("${app.sftp.directory.inbound}")
    private String remoteInboundDirectory;

    @Value("${app.sftp.directory.outbound}")
    private String remoteOutboundDirectory;
}
