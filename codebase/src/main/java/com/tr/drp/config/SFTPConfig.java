package com.tr.drp.config;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.SocketFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.expression.common.LiteralExpression;
import org.springframework.integration.annotation.Gateway;
import org.springframework.integration.annotation.IntegrationComponentScan;
import org.springframework.integration.annotation.MessagingGateway;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.config.EnableIntegration;
import org.springframework.integration.file.remote.session.CachingSessionFactory;
import org.springframework.integration.file.remote.session.SessionFactory;
import org.springframework.integration.handler.advice.RequestHandlerRetryAdvice;
import org.springframework.integration.sftp.outbound.SftpMessageHandler;
import org.springframework.integration.sftp.session.DefaultSftpSessionFactory;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.retry.RetryPolicy;
import org.springframework.retry.backoff.FixedBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Collections;

@Configuration
@IntegrationComponentScan
@EnableIntegration
public class SFTPConfig {

    private static final Logger log = LoggerFactory.getLogger(SFTPConfig.class);

    @Autowired
    private SFTPProperties ftpProperties;

    @Bean
    public SessionFactory<ChannelSftp.LsEntry> sftpSessionFactory() {
        log.info("## SFTP session factory initialization ###");
        DefaultSftpSessionFactory factory = new DefaultSftpSessionFactory(true);
        factory.setHost(ftpProperties.getUrl());
        factory.setPort(ftpProperties.getPort());
        factory.setUser(ftpProperties.getUsername());
        factory.setPassword(ftpProperties.getPassword());
        factory.setAllowUnknownKeys(true);
        factory.setSocketFactory(new ConfSocketFactory(10_000));

        CachingSessionFactory cachingSessionFactory= new CachingSessionFactory<>(factory, 20);

        log.info("## SFTP session factory created ###");

        return cachingSessionFactory;
    }

    private class ConfSocketFactory implements SocketFactory {
        private int soTimeout = 0;

        public ConfSocketFactory(int soTimeout) {
            this.soTimeout = soTimeout;
        }

        @Override
        public Socket createSocket(String s, int i) throws IOException, UnknownHostException {
            Socket socket = new Socket(s, i);
            socket.setSoTimeout(soTimeout);
            return socket;
        }

        @Override
        public InputStream getInputStream(Socket socket) throws IOException {
            return socket.getInputStream();
        }

        @Override
        public OutputStream getOutputStream(Socket socket) throws IOException {
            return socket.getOutputStream();
        }
    }





    @Bean(name = "retryAdvice")
    public RequestHandlerRetryAdvice requestHandlerRetryAdvice(final SessionFactory<ChannelSftp.LsEntry> sftpSessionFactory) {
        RequestHandlerRetryAdvice requestHandlerRetryAdvice = new RequestHandlerRetryAdvice();
        RetryTemplate retryTemplate = new RetryTemplate();
        //setup retry rules as max attempt to process message, exceptions to be retried
        RetryPolicy retryPolicy =
                new SimpleRetryPolicy(5, Collections.<Class<? extends Throwable>, Boolean> singletonMap(Exception.class, true));
        retryTemplate.setRetryPolicy(retryPolicy);
        // setup backoff policy which will be applied between failed attempts
        retryTemplate.setBackOffPolicy(new FixedBackOffPolicy());
        requestHandlerRetryAdvice.setRetryTemplate(retryTemplate);
        //setup recovery behavior after retry a fail
        requestHandlerRetryAdvice.setRecoveryCallback(context ->  {
            if (sftpSessionFactory instanceof CachingSessionFactory) {
                log.info("recovery after fail, reset sessionFactory..");
                ((CachingSessionFactory)sftpSessionFactory).resetCache();
            }
            return null;
        });
        return requestHandlerRetryAdvice;
    }

    @Bean
    @ServiceActivator(inputChannel = "toSftpChannel", adviceChain = "retryAdvice")
    public MessageHandler handlerTo(SessionFactory<ChannelSftp.LsEntry> sftpSessionFactory) {
        SftpMessageHandler handler = new SftpMessageHandler(sftpSessionFactory);
        handler.setRemoteDirectoryExpression(new LiteralExpression(ftpProperties.getRemoteOutboundDirectory()));
        handler.setFileNameGenerator(message -> (message.getHeaders().get("targetFileName").toString()));
        return handler;
    }

    @MessagingGateway
    public interface SFTPGateway {
        @Gateway(requestChannel = "toSftpChannel")
        void sendToSftp(File file, @Header("targetFileName") String targetFileName);
        @Gateway(requestChannel = "toSftpChannel")
        void sendToSftp(byte[] file, @Header("targetFileName") String targetFileName);
    }
}
