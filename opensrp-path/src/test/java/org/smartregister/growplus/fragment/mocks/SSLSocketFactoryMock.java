package org.smartregister.path.fragment.mocks;

import org.apache.http.conn.scheme.HostNameResolver;
import org.apache.http.conn.ssl.SSLSocketFactory;

import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.UnrecoverableKeyException;

/**
 * Created by kaderchowdhury on 12/12/17.
 */

public class SSLSocketFactoryMock extends SSLSocketFactory {
    public SSLSocketFactoryMock(String algorithm, KeyStore keystore, String keystorePassword, KeyStore truststore, SecureRandom random, HostNameResolver nameResolver) throws NoSuchAlgorithmException, KeyManagementException, KeyStoreException, UnrecoverableKeyException {
        super(algorithm, keystore, keystorePassword, truststore, random, nameResolver);
    }

    public SSLSocketFactoryMock(KeyStore keystore, String keystorePassword, KeyStore truststore) throws NoSuchAlgorithmException, KeyManagementException, KeyStoreException, UnrecoverableKeyException {
        super(keystore, keystorePassword, truststore);
    }

    public SSLSocketFactoryMock(KeyStore keystore, String keystorePassword) throws NoSuchAlgorithmException, KeyManagementException, KeyStoreException, UnrecoverableKeyException {
        super(keystore, keystorePassword);
    }

    public SSLSocketFactoryMock(KeyStore truststore) throws NoSuchAlgorithmException, KeyManagementException, KeyStoreException, UnrecoverableKeyException {
        super(truststore);
    }

}
