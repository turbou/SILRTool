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

import java.util.List;

import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.preference.PreferenceStore;
import org.eclipse.swt.widgets.Shell;

import com.contrastsecurity.silrtool.model.LambdaFunction;
import com.contrastsecurity.silrtool.preference.PreferenceConstants;

import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.lambda.LambdaClient;
import software.amazon.awssdk.services.lambda.model.Environment;
import software.amazon.awssdk.services.lambda.model.LambdaException;
import software.amazon.awssdk.services.lambda.model.UpdateFunctionConfigurationRequest;
import software.amazon.awssdk.services.lambda.model.UpdateFunctionConfigurationResponse;

public abstract class LayerWithProgress implements IRunnableWithProgress {

    protected PreferenceStore ps;
    protected Shell shell;
    protected List<LambdaFunction> orgs;

    public LayerWithProgress(Shell shell, PreferenceStore ps, List<LambdaFunction> orgs) {
        this.shell = shell;
        this.ps = ps;
        this.orgs = orgs;
    }

    protected void updateFunctionConfiguration(String functionName, Environment environment, List<String> layers) {
        try {
            Region region = Region.of(ps.getString(PreferenceConstants.REGION));
            LambdaClient awsLambda = LambdaClient.builder().region(region).credentialsProvider(ProfileCredentialsProvider.create()).build();
            UpdateFunctionConfigurationRequest configurationRequest = UpdateFunctionConfigurationRequest.builder().functionName(functionName).environment(environment)
                    .layers(layers).build();
            UpdateFunctionConfigurationResponse response = awsLambda.updateFunctionConfiguration(configurationRequest);
            System.out.println(response);
            awsLambda.close();
        } catch (LambdaException le) {
            le.printStackTrace();
            System.err.println(le.getMessage());
            System.exit(1);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
