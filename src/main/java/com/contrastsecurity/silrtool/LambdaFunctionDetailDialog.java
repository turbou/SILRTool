/*
 * MIT License
 * Copyright (c) 2015-2019 Tabocom
 *
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
 */

package com.contrastsecurity.silrtool;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

import com.contrastsecurity.silrtool.model.LambdaFunction;

import software.amazon.awssdk.services.lambda.model.EnvironmentResponse;
import software.amazon.awssdk.services.lambda.model.Layer;

public class LambdaFunctionDetailDialog extends Dialog {

    private LambdaFunction func;
    private CTabFolder tabFolder;

    public LambdaFunctionDetailDialog(Shell parentShell, LambdaFunction func) {
        super(parentShell);
        this.func = func;
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        Composite composite = (Composite) super.createDialogArea(parent);
        GridLayout compositeLt = new GridLayout(1, false);
        compositeLt.marginWidth = 25;
        compositeLt.marginHeight = 5;
        compositeLt.horizontalSpacing = 5;
        composite.setLayout(compositeLt);
        GridData compositeGrDt = new GridData(GridData.FILL_BOTH);
        composite.setLayoutData(compositeGrDt);

        tabFolder = new CTabFolder(composite, SWT.NONE);
        GridData tabFolderGrDt = new GridData(GridData.FILL_BOTH);
        tabFolder.setLayoutData(tabFolderGrDt);
        tabFolder.setSelectionBackground(
                new Color[] { getShell().getDisplay().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND), getShell().getDisplay().getSystemColor(SWT.COLOR_WIDGET_LIGHT_SHADOW) },
                new int[] { 100 }, true);

        CTabItem layerTabItem = new CTabItem(tabFolder, SWT.NONE);
        layerTabItem.setText("レイヤー");
        Composite layerShell = new Composite(tabFolder, SWT.NONE);
        layerShell.setLayout(new GridLayout(1, false));
        Table layerTable = new Table(layerShell, SWT.BORDER);
        GridData layerTableGrDt = new GridData(GridData.FILL_BOTH);
        layerTable.setLayoutData(layerTableGrDt);
        layerTable.setLinesVisible(true);
        layerTable.setHeaderVisible(true);
        TableColumn layerCol1 = new TableColumn(layerTable, SWT.LEFT);
        layerCol1.setWidth(800);
        layerCol1.setText("ARN");
        for (Layer layer : this.func.getLatestLayers()) {
            TableItem item = new TableItem(layerTable, SWT.LEFT);
            item.setText(0, layer.arn());
        }
        layerTabItem.setControl(layerShell);

        CTabItem envTabItem = new CTabItem(tabFolder, SWT.NONE);
        envTabItem.setText("環境変数");
        Composite envShell = new Composite(tabFolder, SWT.NONE);
        envShell.setLayout(new GridLayout(1, false));
        Table envTable = new Table(envShell, SWT.BORDER);
        GridData envTableGrDt = new GridData(GridData.FILL_BOTH);
        envTable.setLayoutData(envTableGrDt);
        envTable.setLinesVisible(true);
        envTable.setHeaderVisible(true);
        TableColumn envCol1 = new TableColumn(envTable, SWT.LEFT);
        envCol1.setWidth(400);
        envCol1.setText("変数名");
        TableColumn envCol2 = new TableColumn(envTable, SWT.LEFT);
        envCol2.setWidth(400);
        envCol2.setText("値");
        EnvironmentResponse envRes = func.getLatestEnvironment();
        envRes.variables().forEach((k, v) -> {
            TableItem item = new TableItem(envTable, SWT.CENTER);
            item.setText(0, k);
            item.setText(1, v);
        });
        envTabItem.setControl(envShell);

        return composite;
    }

    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        Button okButton = createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
        okButton.setEnabled(true);
    }

    @Override
    protected void okPressed() {
        super.okPressed();
    }

    @Override
    protected void setShellStyle(int newShellStyle) {
        super.setShellStyle(SWT.CLOSE | SWT.TITLE | SWT.RESIZE | SWT.APPLICATION_MODAL);
    }

    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText(String.format("%s", this.func.getName()));
    }

}
