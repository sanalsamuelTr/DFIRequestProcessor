package com.tr.drp.service.crypt;

import org.apache.commons.lang3.StringUtils;
import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component("principalCryptor")
public class DefaultPrincipalCryptor implements PrincipalCryptor {


    private StandardPBEStringEncryptor encryptor;

    public DefaultPrincipalCryptor(@Autowired Environment env) {
        encryptor = new StandardPBEStringEncryptor();
        encryptor.setPassword(env.getRequiredProperty("encryptor.common.pwd"));
    }

    @Override
    public String encrypt(String text) {
        return encryptor.encrypt(text);
    }

    @Override
    public String decrypt(String data) {
        if (StringUtils.isBlank(data)) {
            return data;
        }
        return encryptor.decrypt(data);
    }
}
