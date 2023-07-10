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

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.preference.PreferenceStore;
import org.eclipse.swt.widgets.Shell;

import com.contrastsecurity.silrtool.model.LambdaFunction;
import com.contrastsecurity.silrtool.preference.PreferenceConstants;

import software.amazon.awssdk.services.lambda.model.Environment;
import software.amazon.awssdk.services.lambda.model.EnvironmentResponse;
import software.amazon.awssdk.services.lambda.model.Layer;

public class AddLayerWithProgress extends LayerWithProgress {

    public AddLayerWithProgress(Shell shell, PreferenceStore ps, List<LambdaFunction> orgs) {
        super(shell, ps, orgs);
    }

    @Override
    public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
        monitor.beginTask("レイヤー登録", this.orgs.size()); //$NON-NLS-1$
        for (LambdaFunction func : this.orgs) {
            monitor.setTaskName(func.getName());
            // SubProgressMonitor sub1Monitor = new SubProgressMonitor(monitor, 100);
            EnvironmentResponse envRes = func.getConfig().environment();
            Map<String, String> valueMap = envRes.variables();
            Map<String, String> valueMap2 = new HashMap<String, String>(valueMap);
            if (valueMap2.containsKey("AWS_LAMBDA_EXEC_WRAPPER")) {
                String value = valueMap2.get("AWS_LAMBDA_EXEC_WRAPPER");
                if (value.equals(ps.getString(PreferenceConstants.ENV_EXEC_WRAPPER))) {
                    System.out.println("there is already a wrapper on this function ... skipping");
                } else {
                    valueMap2.remove("AWS_LAMBDA_EXEC_WRAPPER");
                }
            }
            valueMap2.putIfAbsent("AWS_LAMBDA_EXEC_WRAPPER", ps.getString(PreferenceConstants.ENV_EXEC_WRAPPER));
            valueMap2.putIfAbsent("CONTRAST_BUCKET", ps.getString(PreferenceConstants.ENV_S3_BUCKET));
            Environment environment = Environment.builder().variables(valueMap2).build();
            // sub1Monitor.worked(15);

            List<Layer> layers = func.getConfig().layers();
            List<String> layerArns = new ArrayList<String>();
            for (Layer layer : layers) {
                String layerName = layer.arn().split(":")[layer.arn().split(":").length - 2];
                System.out.println(layerName);
                if (!layerName.startsWith("contrast-instrumentation-extension")) {
                    layerArns.add(layer.arn());
                }
            }
            if (func.getRuntime().toLowerCase().startsWith("nodejs")) {
                layerArns.add(ps.getString(PreferenceConstants.LAYER_ARN_NODEJS));
            } else if (func.getRuntime().toLowerCase().startsWith("python")) {
                layerArns.add(ps.getString(PreferenceConstants.LAYER_ARN_PYTHON));
            } else {
                System.out.println(String.format("Layer not found for runtime %s", func.getRuntime()));
            }
            updateFunctionConfiguration(func.getName(), environment, layerArns);
            monitor.worked(1);
            Thread.sleep(1000);
        }
        monitor.done();
    }
}
