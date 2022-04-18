package tools;

import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;
import org.junit.jupiter.api.Test;

public class KeyGen {
    @Test
    public void genKey() {
        StandardPBEStringEncryptor enc = new StandardPBEStringEncryptor();
        enc.setPassword("ONESOURCEFVAT");
        System.out.println(enc.decrypt("siTr8itrtabfejKahkLv5F4mDsnwABx3"));
    }
}
