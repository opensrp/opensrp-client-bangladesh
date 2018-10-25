package org.smartregister.growplus.fragment.mocks;

import android.content.Context;

import org.apache.http.conn.scheme.SocketFactory;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.mockito.Mockito;
import org.robolectric.annotation.Implements;
import org.robolectric.shadow.api.Shadow;
import org.smartregister.DristhiConfiguration;
import org.smartregister.ssl.OpensrpSSLHelper;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.security.KeyStore;

/**
 * Created by kaderchowdhury on 12/12/17.
 */
@Implements(OpensrpSSLHelper.class)
public class ShadowOpensrpSSLHelper extends Shadow {

    public void __constructor__(Context context_, DristhiConfiguration configuration_) {

    }

    public SocketFactory getSslSocketFactoryWithOpenSrpCertificate() throws Exception {
        ClassLoader classLoader = this.getClass().getClassLoader();
        URL resource = classLoader.getResource("my-key.keystore");
        File file = new File(resource.getPath());
        InputStream inputStream = new FileInputStream(file);

        KeyStore keyStore = KeyStore.getInstance("JKS");
        keyStore.load(inputStream,"123456".toCharArray());
        SSLSocketFactory socketFactory = new SSLSocketFactory(keyStore);
//        inputStream.close();
        return socketFactory;
    }
}
