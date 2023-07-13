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

package com.contrastsecurity.silrtool.preference;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;

import com.contrastsecurity.silrtool.Messages;
import com.contrastsecurity.silrtool.ServerLessToolShell;

import software.amazon.awssdk.regions.Region;

public class BasePreferencePage extends PreferencePage {

    private ServerLessToolShell shell;
    private Text profileNameTxt;
    private Combo regionCombo;
    private Text layerArnPythonTxt;
    private Text layerArnNodeJSTxt;
    private Text envExecWrapperTxt;
    private Text envS3BucketTxt;

    public BasePreferencePage(ServerLessToolShell shell) {
        super(Messages.getString("basepreferencepage.title")); //$NON-NLS-1$
        this.shell = shell;
    }

    @Override
    protected Control createContents(Composite parent) {
        IPreferenceStore ps = getPreferenceStore();

        final Composite composite = new Composite(parent, SWT.NONE);
        GridLayout compositeLt = new GridLayout(1, false);
        compositeLt.marginHeight = 15;
        compositeLt.marginWidth = 5;
        compositeLt.horizontalSpacing = 10;
        compositeLt.verticalSpacing = 20;
        composite.setLayout(compositeLt);

        Composite commonComp = new Composite(composite, SWT.NONE);
        GridLayout commonCompLt = new GridLayout(2, false);
        commonCompLt.verticalSpacing = 10;
        commonComp.setLayout(commonCompLt);
        GridData commonCompGrDt = new GridData(GridData.FILL_HORIZONTAL);
        commonComp.setLayoutData(commonCompGrDt);

        new Label(commonComp, SWT.LEFT).setText("プロファイル:");
        profileNameTxt = new Text(commonComp, SWT.BORDER);
        profileNameTxt.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        profileNameTxt.setText(ps.getString(PreferenceConstants.PROFILE_NAME));
        profileNameTxt.setMessage("ブランクの場合、defaultプロファイルを使用します。");
        profileNameTxt.addListener(SWT.FocusIn, new Listener() {
            public void handleEvent(Event e) {
                profileNameTxt.selectAll();
            }
        });

        new Label(commonComp, SWT.LEFT).setText("リージョン:");
        regionCombo = new Combo(commonComp, SWT.READ_ONLY);
        GridData regionComboGrDt = new GridData(GridData.FILL_HORIZONTAL);
        regionCombo.setLayoutData(regionComboGrDt);
        List<Region> sorted = Region.regions().stream().sorted(Comparator.comparing(Region::id)).collect(Collectors.toList());
        int selectIdx = -1;
        for (Region region : sorted) {
            if (region.toString().equals(ps.getString(PreferenceConstants.REGION))) {
                selectIdx = sorted.indexOf(region);
            }
            regionCombo.add(region.toString());
        }
        regionCombo.select(selectIdx);

        Group timeoutGrp = new Group(composite, SWT.NONE);
        GridLayout timeoutGrpLt = new GridLayout(2, false);
        timeoutGrpLt.marginWidth = 15;
        timeoutGrpLt.horizontalSpacing = 10;
        timeoutGrp.setLayout(timeoutGrpLt);
        GridData timeoutGrpGrDt = new GridData(GridData.FILL_HORIZONTAL);
        timeoutGrp.setLayoutData(timeoutGrpGrDt);
        timeoutGrp.setText("ContrastテレメトリレイヤーARN");

        // ========== Python ========== //
        new Label(timeoutGrp, SWT.LEFT).setText("Python:"); //$NON-NLS-1$
        layerArnPythonTxt = new Text(timeoutGrp, SWT.BORDER);
        layerArnPythonTxt.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        layerArnPythonTxt.setText(ps.getString(PreferenceConstants.LAYER_ARN_PYTHON));
        layerArnPythonTxt.addListener(SWT.FocusIn, new Listener() {
            public void handleEvent(Event e) {
                layerArnPythonTxt.selectAll();
            }
        });

        // ========== NodeJS ========== //
        new Label(timeoutGrp, SWT.LEFT).setText("NodeJS:"); //$NON-NLS-1$
        layerArnNodeJSTxt = new Text(timeoutGrp, SWT.BORDER);
        layerArnNodeJSTxt.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        layerArnNodeJSTxt.setText(ps.getString(PreferenceConstants.LAYER_ARN_NODEJS));
        layerArnNodeJSTxt.addListener(SWT.FocusIn, new Listener() {
            public void handleEvent(Event e) {
                layerArnNodeJSTxt.selectAll();
            }
        });

        Group envGrp = new Group(composite, SWT.NONE);
        GridLayout envGrpGrpLt = new GridLayout(2, false);
        envGrpGrpLt.marginWidth = 15;
        envGrpGrpLt.horizontalSpacing = 10;
        envGrp.setLayout(envGrpGrpLt);
        GridData envGrpGrDt = new GridData(GridData.FILL_HORIZONTAL);
        envGrp.setLayoutData(envGrpGrDt);
        envGrp.setText("環境変数");

        // ========== AWS_LAMBDA_EXEC_WRAPPER ========== //
        new Label(envGrp, SWT.LEFT).setText("AWS_LAMBDA_EXEC_WRAPPER:"); //$NON-NLS-1$
        envExecWrapperTxt = new Text(envGrp, SWT.BORDER);
        envExecWrapperTxt.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        envExecWrapperTxt.setText(ps.getString(PreferenceConstants.ENV_EXEC_WRAPPER));
        envExecWrapperTxt.addListener(SWT.FocusIn, new Listener() {
            public void handleEvent(Event e) {
                envExecWrapperTxt.selectAll();
            }
        });

        // ========== CONTRAST_BUCKET ========== //
        new Label(envGrp, SWT.LEFT).setText("CONTRAST_BUCKET:"); //$NON-NLS-1$
        envS3BucketTxt = new Text(envGrp, SWT.BORDER);
        envS3BucketTxt.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        envS3BucketTxt.setText(ps.getString(PreferenceConstants.ENV_S3_BUCKET));
        envS3BucketTxt.addListener(SWT.FocusIn, new Listener() {
            public void handleEvent(Event e) {
                envS3BucketTxt.selectAll();
            }
        });

        Label descLabel = new Label(composite, SWT.LEFT);
        GridData descLabelGrDt = new GridData(GridData.FILL_HORIZONTAL);
        descLabel.setLayoutData(descLabelGrDt);
        List<String> descLabelList = new ArrayList<String>();
        descLabelList.add("Contrastサーバレスの設定タブで確認できます。");
        descLabel.setText(String.join("\r\n", descLabelList)); //$NON-NLS-1$

        Composite buttonGrp = new Composite(parent, SWT.NONE);
        GridLayout buttonGrpLt = new GridLayout(2, false);
        buttonGrpLt.marginHeight = 15;
        buttonGrpLt.marginWidth = 5;
        buttonGrpLt.horizontalSpacing = 7;
        buttonGrpLt.verticalSpacing = 20;
        buttonGrp.setLayout(buttonGrpLt);
        GridData buttonGrpGrDt = new GridData(GridData.FILL_HORIZONTAL);
        buttonGrpGrDt.horizontalAlignment = SWT.END;
        buttonGrp.setLayoutData(buttonGrpGrDt);

        Button defaultBtn = new Button(buttonGrp, SWT.NULL);
        GridData defaultBtnGrDt = new GridData(SWT.RIGHT, SWT.BOTTOM, true, true, 1, 1);
        defaultBtnGrDt.minimumWidth = 100;
        defaultBtn.setLayoutData(defaultBtnGrDt);
        defaultBtn.setText(Messages.getString("preferencepage.restoredefaults.button.title")); //$NON-NLS-1$
        defaultBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                envExecWrapperTxt.setText(ps.getDefaultString(PreferenceConstants.ENV_EXEC_WRAPPER));
            }
        });

        Button applyBtn = new Button(buttonGrp, SWT.NULL);
        GridData applyBtnGrDt = new GridData(SWT.RIGHT, SWT.BOTTOM, true, true, 1, 1);
        applyBtnGrDt.minimumWidth = 90;
        applyBtn.setLayoutData(applyBtnGrDt);
        applyBtn.setText(Messages.getString("preferencepage.apply.button.title")); //$NON-NLS-1$
        applyBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                performOk();
            }
        });
        noDefaultAndApplyButton();
        return composite;
    }

    @Override
    public boolean performOk() {
        IPreferenceStore ps = getPreferenceStore();
        if (ps == null) {
            return true;
        }
        List<String> errors = new ArrayList<String>();

        if (!this.layerArnPythonTxt.getText().isEmpty()) {
            ps.setValue(PreferenceConstants.PROFILE_NAME, this.profileNameTxt.getText());
        }

        if (this.regionCombo.getText().isEmpty()) {
            errors.add("リージョン指定は必須です。");
        } else {
            ps.setValue(PreferenceConstants.REGION, this.regionCombo.getText());
        }

        if (this.layerArnPythonTxt.getText().isEmpty()) {
            errors.add("・PythonのテレメトリーレイヤのARNが設定されていません。");
        } else {
            if (!this.layerArnPythonTxt.getText().trim().startsWith("arn:aws:lambda:")) { //$NON-NLS-1$
                errors.add("・LambdaのARNではないようです。");
            }
            ps.setValue(PreferenceConstants.LAYER_ARN_PYTHON, this.layerArnPythonTxt.getText());
        }

        if (this.layerArnNodeJSTxt.getText().isEmpty()) {
            errors.add("・NodeJSのテレメトリーレイヤのARNが設定されていません。");
        } else {
            if (!this.layerArnNodeJSTxt.getText().trim().startsWith("arn:aws:lambda:")) { //$NON-NLS-1$
                errors.add("・LambdaのARNではないようです。");
            }
            ps.setValue(PreferenceConstants.LAYER_ARN_NODEJS, this.layerArnNodeJSTxt.getText());
        }

        if (this.envExecWrapperTxt.getText().isEmpty()) {
            errors.add("・AWS_LAMBDA_EXEC_WRAPPERの値が設定されていません。");
        } else {
            ps.setValue(PreferenceConstants.ENV_EXEC_WRAPPER, this.envExecWrapperTxt.getText());
        }

        if (this.envS3BucketTxt.getText().isEmpty()) {
            errors.add("・CONTRAST_BUCKETの値が設定されていません。");
        } else {
            ps.setValue(PreferenceConstants.ENV_S3_BUCKET, this.envS3BucketTxt.getText());
        }

        if (!errors.isEmpty()) {
            MessageDialog.openError(getShell(), Messages.getString("basepreferencepage.title"), String.join("\r\n", errors)); //$NON-NLS-1$//$NON-NLS-2$
            return false;
        }
        return true;
    }
}
