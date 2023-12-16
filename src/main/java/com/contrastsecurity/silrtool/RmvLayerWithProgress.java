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

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.preference.PreferenceStore;
import org.eclipse.swt.widgets.Shell;

import com.contrastsecurity.silrtool.exception.SILRLambdaException;
import com.contrastsecurity.silrtool.model.LambdaFunction;

import software.amazon.awssdk.services.lambda.model.Environment;
import software.amazon.awssdk.services.lambda.model.EnvironmentResponse;
import software.amazon.awssdk.services.lambda.model.LambdaException;
import software.amazon.awssdk.services.lambda.model.Layer;
import software.amazon.awssdk.services.lambda.model.UpdateFunctionConfigurationResponse;

public class RmvLayerWithProgress extends LayerWithProgress {

    Logger logger = LogManager.getLogger("silrtool"); //$NON-NLS-1$

    public RmvLayerWithProgress(Shell shell, PreferenceStore ps, List<LambdaFunction> funcs) {
        super(shell, ps, funcs);
    }

    @Override
    public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
        monitor.beginTask(Messages.getString("rmvlayerwithprogress.taskname"), this.funcs.size()); //$NON-NLS-1$
        for (LambdaFunction func : this.funcs) {
            if (monitor.isCanceled()) {
                throw new InterruptedException(Messages.getString("layerwithprogress.progress.canceled")); //$NON-NLS-1$
            }
            monitor.setTaskName(String.format("%s (%d/%d)", func.getName(), this.funcs.indexOf(func) + 1, this.funcs.size())); //$NON-NLS-1$
            EnvironmentResponse envRes = func.getConfig().environment();
            Map<String, String> valueMap2 = null;
            if (envRes != null) {
                Map<String, String> valueMap = envRes.variables();
                valueMap2 = new HashMap<String, String>(valueMap);
            } else {
                valueMap2 = new HashMap<String, String>();
            }
            if (valueMap2.containsKey("AWS_LAMBDA_EXEC_WRAPPER")) { //$NON-NLS-1$
                valueMap2.remove("AWS_LAMBDA_EXEC_WRAPPER"); //$NON-NLS-1$
            }
            if (valueMap2.containsKey("CONTRAST_BUCKET")) { //$NON-NLS-1$
                valueMap2.remove("CONTRAST_BUCKET"); //$NON-NLS-1$
            }
            Environment environment = Environment.builder().variables(valueMap2).build();

            List<Layer> layers = func.getConfig().layers();
            List<String> layerArns = new ArrayList<String>();
            for (Layer layer : layers) {
                String layerName = layer.arn().split(":")[layer.arn().split(":").length - 2]; //$NON-NLS-1$ //$NON-NLS-2$
                if (!layerName.startsWith("contrast-instrumentation-extension")) { //$NON-NLS-1$
                    layerArns.add(layer.arn());
                }
            }
            try {
                UpdateFunctionConfigurationResponse response = updateFunctionConfiguration(func.getName(), environment, layerArns);
                func.setResponse(response);
                shell.getDisplay().syncExec(new Runnable() {
                    @Override
                    public void run() {
                        ((ServerLessToolShell) shell).getMain().updateTableItem(func);
                    }
                });
            } catch (LambdaException e) {
                StringWriter stringWriter = new StringWriter();
                PrintWriter printWriter = new PrintWriter(stringWriter);
                e.printStackTrace(printWriter);
                String trace = stringWriter.toString();
                logger.error(trace);
                throw new InvocationTargetException(new SILRLambdaException(func.getName(), e));
            } catch (Exception e) {
                StringWriter stringWriter = new StringWriter();
                PrintWriter printWriter = new PrintWriter(stringWriter);
                e.printStackTrace(printWriter);
                String trace = stringWriter.toString();
                logger.error(trace);
                throw new InvocationTargetException(e);
            }
            monitor.worked(1);
            Thread.sleep(500);
        }
        monitor.done();
    }
}
