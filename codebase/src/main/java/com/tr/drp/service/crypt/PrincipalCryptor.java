package com.tr.drp.service.crypt;

public interface PrincipalCryptor {
    String encrypt(String text);

    String decrypt(String data);
}
