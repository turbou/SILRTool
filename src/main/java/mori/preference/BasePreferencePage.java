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

package mori.preference;

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

import mori.Messages;
import mori.ServerLessToolShell;
import software.amazon.awssdk.regions.Region;

public class BasePreferencePage extends PreferencePage {

    private ServerLessToolShell shell;
    private Combo regionCombo;
    private Text connectionTimeoutTxt;
    private Text socketTimeoutTxt;
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

        Composite regionComp = new Composite(composite, SWT.NONE);
        GridLayout regionCompLt = new GridLayout(2, false);
        regionComp.setLayout(regionCompLt);
        GridData regionCompGrDt = new GridData(GridData.FILL_HORIZONTAL);
        regionComp.setLayoutData(regionCompGrDt);

        new Label(regionComp, SWT.LEFT).setText("リージョン:");

        regionCombo = new Combo(regionComp, SWT.READ_ONLY);
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
        new Label(timeoutGrp, SWT.LEFT).setText("Python:");
        connectionTimeoutTxt = new Text(timeoutGrp, SWT.BORDER);
        connectionTimeoutTxt.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        connectionTimeoutTxt.setText(ps.getString(PreferenceConstants.LAYER_ARN_PYTHON));
        connectionTimeoutTxt.addListener(SWT.FocusIn, new Listener() {
            public void handleEvent(Event e) {
                connectionTimeoutTxt.selectAll();
            }
        });

        // ========== NodeJS ========== //
        new Label(timeoutGrp, SWT.LEFT).setText("NodeJS:");
        socketTimeoutTxt = new Text(timeoutGrp, SWT.BORDER);
        socketTimeoutTxt.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        socketTimeoutTxt.setText(ps.getString(PreferenceConstants.LAYER_ARN_NODEJS));
        socketTimeoutTxt.addListener(SWT.FocusIn, new Listener() {
            public void handleEvent(Event e) {
                socketTimeoutTxt.selectAll();
            }
        });

        Group environmentGrp = new Group(composite, SWT.NONE);
        GridLayout environmentGrpGrpLt = new GridLayout(2, false);
        environmentGrpGrpLt.marginWidth = 15;
        environmentGrpGrpLt.horizontalSpacing = 10;
        environmentGrp.setLayout(environmentGrpGrpLt);
        GridData environmentGrpGrDt = new GridData(GridData.FILL_HORIZONTAL);
        environmentGrp.setLayoutData(environmentGrpGrDt);
        environmentGrp.setText("環境変数");

        // ========== AWS_LAMBDA_EXEC_WRAPPER ========== //
        new Label(environmentGrp, SWT.LEFT).setText("AWS_LAMBDA_EXEC_WRAPPER:");
        envExecWrapperTxt = new Text(environmentGrp, SWT.BORDER);
        envExecWrapperTxt.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        envExecWrapperTxt.setText(ps.getString(PreferenceConstants.ENV_EXEC_WRAPPER));
        envExecWrapperTxt.addListener(SWT.FocusIn, new Listener() {
            public void handleEvent(Event e) {
                envExecWrapperTxt.selectAll();
            }
        });

        // ========== CONTRAST_BUCKET ========== //
        new Label(environmentGrp, SWT.LEFT).setText("CONTRAST_BUCKET:");
        envS3BucketTxt = new Text(environmentGrp, SWT.BORDER);
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

        Button applyBtn = new Button(composite, SWT.NULL);
        GridData applyBtnGrDt = new GridData(SWT.RIGHT, SWT.BOTTOM, true, true, 1, 1);
        applyBtnGrDt.minimumWidth = 90;
        applyBtnGrDt.horizontalSpan = 2;
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

        if (this.regionCombo.getText().isEmpty()) {
            errors.add(Messages.getString("connectionpreferencepage.message.dialog.connection.timeout.empty.error.message")); //$NON-NLS-1$
        } else {
            ps.setValue(PreferenceConstants.REGION, this.regionCombo.getText());
        }

        if (this.connectionTimeoutTxt.getText().isEmpty()) {
            errors.add(Messages.getString("connectionpreferencepage.message.dialog.connection.timeout.empty.error.message")); //$NON-NLS-1$
        } else {
            ps.setValue(PreferenceConstants.LAYER_ARN_PYTHON, this.connectionTimeoutTxt.getText());
        }

        if (this.socketTimeoutTxt.getText().isEmpty()) {
            errors.add(Messages.getString("connectionpreferencepage.message.dialog.socket.timeout.empty.error.message")); //$NON-NLS-1$
        } else {
            ps.setValue(PreferenceConstants.LAYER_ARN_NODEJS, this.socketTimeoutTxt.getText());
        }

        if (this.envExecWrapperTxt.getText().isEmpty()) {
            errors.add(Messages.getString("connectionpreferencepage.message.dialog.connection.timeout.empty.error.message")); //$NON-NLS-1$
        } else {
            ps.setValue(PreferenceConstants.ENV_EXEC_WRAPPER, this.envExecWrapperTxt.getText());
        }

        if (this.envS3BucketTxt.getText().isEmpty()) {
            errors.add(Messages.getString("connectionpreferencepage.message.dialog.socket.timeout.empty.error.message")); //$NON-NLS-1$
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
