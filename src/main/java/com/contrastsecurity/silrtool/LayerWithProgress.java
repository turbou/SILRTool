/*
 * MIT License
 * Copyright (c) 2020 Contrast Security Japan G.K.
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 * 
 */

package com.contrastsecurity.silrtool;

import java.net.URI;
import java.util.List;

import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.preference.PreferenceStore;
import org.eclipse.swt.widgets.Shell;
import org.jasypt.util.text.BasicTextEncryptor;

import com.contrastsecurity.silrtool.model.LambdaFunction;
import com.contrastsecurity.silrtool.preference.PreferenceConstants;

import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider;
import software.amazon.awssdk.http.SdkHttpClient;
import software.amazon.awssdk.http.SdkHttpConfigurationOption;
import software.amazon.awssdk.http.apache.ApacheHttpClient;
import software.amazon.awssdk.http.apache.ProxyConfiguration;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.lambda.LambdaClient;
import software.amazon.awssdk.services.lambda.LambdaClientBuilder;
import software.amazon.awssdk.services.lambda.model.Environment;
import software.amazon.awssdk.services.lambda.model.UpdateFunctionConfigurationRequest;
import software.amazon.awssdk.services.lambda.model.UpdateFunctionConfigurationResponse;
import software.amazon.awssdk.utils.AttributeMap;

public abstract class LayerWithProgress implements IRunnableWithProgress {

    protected PreferenceStore ps;
    protected Shell shell;
    protected List<LambdaFunction> funcs;

    public LayerWithProgress(Shell shell, PreferenceStore ps, List<LambdaFunction> funcs) {
        this.shell = shell;
        this.ps = ps;
        this.funcs = funcs;
    }

    protected UpdateFunctionConfigurationResponse updateFunctionConfiguration(String functionName, Environment environment, List<String> layers) throws Exception {
        Region region = Region.of(ps.getString(PreferenceConstants.REGION));
        AttributeMap attrMap = null;
        if (this.ps.getBoolean(PreferenceConstants.IGNORE_SSLCERT_CHECK)) {
            attrMap = AttributeMap.builder().put(SdkHttpConfigurationOption.TRUST_ALL_CERTIFICATES, true).build();
        }
        SdkHttpClient httpClient = null;
        if (ps.getBoolean(PreferenceConstants.PROXY_YUKO)) {
            String proxyHostPort = String.format("%s:%s", ps.getString(PreferenceConstants.PROXY_HOST), ps.getString(PreferenceConstants.PROXY_PORT)); //$NON-NLS-1$
            ProxyConfiguration.Builder proxyBuilder = ProxyConfiguration.builder();
            proxyBuilder.endpoint(URI.create(proxyHostPort));
            if (!this.ps.getString(PreferenceConstants.PROXY_AUTH).equals("none")) { //$NON-NLS-1$
                // プロキシ認証あり
                if (this.ps.getString(PreferenceConstants.PROXY_AUTH).equals("input")) { //$NON-NLS-1$
                    proxyBuilder.username(ps.getString(PreferenceConstants.PROXY_TMP_USER));
                    proxyBuilder.password(ps.getString(PreferenceConstants.PROXY_TMP_PASS));
                } else {
                    BasicTextEncryptor encryptor = new BasicTextEncryptor();
                    encryptor.setPassword(Main.MASTER_PASSWORD);
                    try {
                        String proxy_pass = encryptor.decrypt(this.ps.getString(PreferenceConstants.PROXY_PASS));
                        proxyBuilder.username(ps.getString(PreferenceConstants.PROXY_USER));
                        proxyBuilder.password(proxy_pass);
                    } catch (Exception e) {
                        throw new Exception(Messages.getString("layerwithprogress.proxy.password.decrypt.error")); //$NON-NLS-1$
                    }
                }
            }
            if (attrMap != null) {
                httpClient = ApacheHttpClient.builder().proxyConfiguration(proxyBuilder.build()).buildWithDefaults(attrMap);
            } else {
                httpClient = ApacheHttpClient.builder().proxyConfiguration(proxyBuilder.build()).build();
            }
        } else {
            if (attrMap != null) {
                httpClient = ApacheHttpClient.builder().buildWithDefaults(attrMap);
            }
        }
        LambdaClientBuilder clientBuilder = LambdaClient.builder();
        ProfileCredentialsProvider profileProvider = null;
        if (ps.getString(PreferenceConstants.PROFILE_NAME).isEmpty()) {
            profileProvider = ProfileCredentialsProvider.create();
        } else {
            profileProvider = ProfileCredentialsProvider.create(ps.getString(PreferenceConstants.PROFILE_NAME));
        }
        clientBuilder.region(region).credentialsProvider(profileProvider);
        if (httpClient != null) {
            clientBuilder.httpClient(httpClient);
        }
        LambdaClient awsLambda = clientBuilder.build();
        UpdateFunctionConfigurationRequest configurationRequest = UpdateFunctionConfigurationRequest.builder().functionName(functionName).environment(environment).layers(layers)
                .build();
        UpdateFunctionConfigurationResponse response = awsLambda.updateFunctionConfiguration(configurationRequest);
        awsLambda.close();
        return response;
    }
}
