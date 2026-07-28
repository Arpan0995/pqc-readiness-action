package fixture;

import java.security.KeyPairGenerator;
import java.security.Signature;
import java.security.interfaces.RSAPublicKey;

/** Deliberately quantum-vulnerable fixture the CI self-test scans. */
public class Crypto {
    RSAPublicKey key;

    void establish() throws Exception {
        KeyPairGenerator.getInstance("RSA");
        Signature.getInstance("SHA256withECDSA");
    }
}
