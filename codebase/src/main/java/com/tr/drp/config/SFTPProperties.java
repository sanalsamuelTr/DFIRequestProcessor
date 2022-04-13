package com.tr.drp.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.sftp.session.JschProxyFactoryBean;

import java.io.File;

@Configuration
@Getter
@Setter
public class SFTPProperties {
    @Value("${sftp.host}")
    private String url;
    @Value("${sftp.port}")
    private Integer port;
    @Value("${sftp.username}")
    private String username;
    @Value("#{principalCryptor.decrypt('${sftp.password}')}")
    private String password;

    @Value("${sftp.proxy.type}")
    private String proxyType;
    @Value("${sftp.proxy.host}")
    private String proxyHost;
    @Value("${sftp.proxy.port}")
    private Integer proxyPort;
    @Value("${sftp.proxy.username}")
    private String proxyUsername;
    @Value("${sftp.proxy.password}'}")
    private String proxyPassword;

    @Value("${sftp.directory.inbound.remote}")
    private String remoteInboundDirectory;

    @Value("${sftp.directory.outbound.remote}")
    private String remoteOutboundDirectory;

    //    @Value("${sftp.directory.local}")
    private String localDirectory = File.separator + "storage" + File.separator + "vatreturn";

    public JschProxyFactoryBean.Type getProxyEnumType() {
        switch (getProxyType()) {
            case "http" : return JschProxyFactoryBean.Type.HTTP;
            case "socks4" : return JschProxyFactoryBean.Type.SOCKS4;
            case "socks5" : return JschProxyFactoryBean.Type.SOCKS5;
        }
        return null;
    }
}
