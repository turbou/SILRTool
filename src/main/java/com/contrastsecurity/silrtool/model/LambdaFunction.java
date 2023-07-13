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

package com.contrastsecurity.silrtool.model;

import java.util.ArrayList;
import java.util.List;

import software.amazon.awssdk.services.lambda.model.EnvironmentResponse;
import software.amazon.awssdk.services.lambda.model.FunctionConfiguration;
import software.amazon.awssdk.services.lambda.model.Layer;
import software.amazon.awssdk.services.lambda.model.UpdateFunctionConfigurationResponse;

public class LambdaFunction {
    private String name;
    private String runtime;
    private boolean valid;
    private FunctionConfiguration config;
    private UpdateFunctionConfigurationResponse response;

    public LambdaFunction(FunctionConfiguration config) {
        this.name = config.functionName();
        this.runtime = config.runtimeAsString();
        this.config = config;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRuntime() {
        return runtime;
    }

    public void setRuntime(String runtime) {
        this.runtime = runtime;
    }

    public String hasContrastLayerStr() {
        if (this.hasContrastLayer()) {
            return "Y"; //$NON-NLS-1$
        } else {
            return "N"; //$NON-NLS-1$
        }
    }

    public boolean hasContrastLayer() {
        if (this.response != null) {
            for (Layer layer : this.response.layers()) {
                String layerName = layer.arn().split(":")[layer.arn().split(":").length - 2]; //$NON-NLS-1$ //$NON-NLS-2$
                if (layerName.startsWith("contrast-instrumentation-extension")) { //$NON-NLS-1$
                    return true;
                }
            }
        } else {
            for (Layer layer : this.config.layers()) {
                String layerName = layer.arn().split(":")[layer.arn().split(":").length - 2]; //$NON-NLS-1$ //$NON-NLS-2$
                if (layerName.startsWith("contrast-instrumentation-extension")) { //$NON-NLS-1$
                    return true;
                }
            }
        }
        return false;
    }

    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }

    public List<Layer> getLatestLayers() {
        if (this.response != null) {
            return this.response.layers();
        } else {
            return this.config.layers();
        }
    }

    public EnvironmentResponse getLatestEnvironment() {
        if (this.response != null) {
            return this.response.environment();
        } else {
            return this.config.environment();
        }
    }

    public FunctionConfiguration getConfig() {
        return config;
    }

    public void setConfig(FunctionConfiguration config) {
        this.config = config;
    }

    public UpdateFunctionConfigurationResponse getResponse() {
        return response;
    }

    public void setResponse(UpdateFunctionConfigurationResponse response) {
        this.response = response;
    }

    @Override
    public String toString() {
        List<String> strList = new ArrayList<String>();
        strList.add("name: " + this.name); //$NON-NLS-1$
        return String.join(", ", strList); //$NON-NLS-1$
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof LambdaFunction) {
            LambdaFunction other = (LambdaFunction) obj;
            return other.name.equals(this.name);
        }
        return false;
    }

}
