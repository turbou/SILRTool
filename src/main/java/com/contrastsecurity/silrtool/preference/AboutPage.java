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

import java.awt.Desktop;
import java.net.URI;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;

import com.contrastsecurity.silrtool.Main;
import com.contrastsecurity.silrtool.Messages;

public class AboutPage extends PreferencePage {

    public AboutPage() {
        super(Messages.getString("aboutpage.about.silrtool")); //$NON-NLS-1$
    }

    @Override
    protected Control createContents(Composite parent) {

        GridLayout parentGrLt = new GridLayout(1, false);
        parentGrLt.verticalSpacing = 20;
        parent.setLayout(parentGrLt);

        Composite composite = new Composite(parent, SWT.NONE);
        composite.setLayout(new GridLayout(2, false));
        GridData compositeGrDt = new GridData(GridData.FILL_HORIZONTAL);
        composite.setLayoutData(compositeGrDt);

        Label icon = new Label(composite, SWT.NONE);
        GridData iconGrDt = new GridData();
        iconGrDt.horizontalSpan = 2;
        iconGrDt.minimumHeight = 80;
        iconGrDt.minimumWidth = 300;
        icon.setLayoutData(iconGrDt);
        Image iconImg = new Image(parent.getDisplay(), Main.class.getClassLoader().getResourceAsStream("banner.png")); //$NON-NLS-1$
        icon.setImage(iconImg);

        Label blankLbl = new Label(composite, SWT.NONE);
        GridData blankLblGrDt = new GridData();
        blankLblGrDt.horizontalSpan = 2;
        blankLbl.setLayoutData(blankLblGrDt);

        Label fullAppNameTitleLbl = new Label(composite, SWT.NONE);
        GridData fullAppNameTitleLblGrDt = new GridData();
        fullAppNameTitleLblGrDt.minimumHeight = 100;
        fullAppNameTitleLbl.setLayoutData(fullAppNameTitleLblGrDt);
        fullAppNameTitleLbl.setText("SILRTool:"); //$NON-NLS-1$
        Label fullAppNameLbl = new Label(composite, SWT.NONE);
        GridData fullAppNameLblGrDt = new GridData();
        fullAppNameLblGrDt.minimumHeight = 100;
        fullAppNameLbl.setLayoutData(fullAppNameLblGrDt);
        fullAppNameLbl.setText("Serverless Instrumentation Layers Registration Tool"); //$NON-NLS-1$

        Label versionTitleLbl = new Label(composite, SWT.NONE);
        GridData versionTitleLblGrDt = new GridData();
        versionTitleLblGrDt.minimumHeight = 100;
        versionTitleLbl.setLayoutData(versionTitleLblGrDt);
        versionTitleLbl.setText("Version:"); //$NON-NLS-1$
        Label versionValueLbl = new Label(composite, SWT.NONE);
        GridData versionValueLblGrDt = new GridData();
        versionValueLbl.setLayoutData(versionValueLblGrDt);
        versionValueLbl.setText("1.0.3"); //$NON-NLS-1$

        Label copyrightLbl = new Label(composite, SWT.NONE);
        GridData copyrightLblGrDt = new GridData();
        copyrightLblGrDt.horizontalSpan = 2;
        copyrightLbl.setLayoutData(copyrightLblGrDt);
        copyrightLbl.setText("Copyright (c) 2023 Contrast Security Japan G.K."); //$NON-NLS-1$

        Composite licenseGrp = new Composite(parent, SWT.NONE);
        GridLayout licenseGrpGrLt = new GridLayout(1, false);
        licenseGrp.setLayout(licenseGrpGrLt);
        GridData licenseGroupGrDt = new GridData(GridData.FILL_BOTH);
        licenseGrp.setLayoutData(licenseGroupGrDt);

        Link licenseLinkLbl = new Link(licenseGrp, SWT.NONE);
        licenseLinkLbl.setText("This software includes the work that is distributed in the <a>Apache License 2.0</a>"); //$NON-NLS-1$
        licenseLinkLbl.addSelectionListener(new org.eclipse.swt.events.SelectionAdapter() {
            public void widgetSelected(org.eclipse.swt.events.SelectionEvent event) {
                try {
                    if (Desktop.isDesktopSupported()) {
                        Desktop.getDesktop().browse(new URI("http://www.apache.org/licenses/LICENSE-2.0")); //$NON-NLS-1$
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        // AWS SDK for Java
        new Link(licenseGrp, SWT.NONE).setText("- software.amazon.awssdk:s3 2.20.90"); //$NON-NLS-1$
        new Link(licenseGrp, SWT.NONE).setText("- software.amazon.awssdk:sso 2.20.90"); //$NON-NLS-1$
        new Link(licenseGrp, SWT.NONE).setText("- software.amazon.awssdk:ssooidc 2.20.90"); //$NON-NLS-1$
        new Link(licenseGrp, SWT.NONE).setText("- software.amazon.awssdk:regions 2.20.90"); //$NON-NLS-1$
        new Link(licenseGrp, SWT.NONE).setText("- software.amazon.awssdk:lambda 2.20.90"); //$NON-NLS-1$
        new Link(licenseGrp, SWT.NONE).setText("- software.amazon.awssdk:apache-client 2.20.90"); //$NON-NLS-1$

        new Link(licenseGrp, SWT.NONE).setText("- commons-exec 1.3"); //$NON-NLS-1$
        new Link(licenseGrp, SWT.NONE).setText("- commons-lang3 3.4"); //$NON-NLS-1$
        new Link(licenseGrp, SWT.NONE).setText("- jasypt 1.9.3"); //$NON-NLS-1$

        Link mitLicenseLinkLbl = new Link(licenseGrp, SWT.NONE);
        mitLicenseLinkLbl.setText("This software includes the work that is distributed in the <a>MIT</a>"); //$NON-NLS-1$
        mitLicenseLinkLbl.addSelectionListener(new org.eclipse.swt.events.SelectionAdapter() {
            public void widgetSelected(org.eclipse.swt.events.SelectionEvent event) {
                try {
                    if (Desktop.isDesktopSupported()) {
                        Desktop.getDesktop().browse(new URI("https://opensource.org/licenses/mit-license.php")); //$NON-NLS-1$
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        new Link(licenseGrp, SWT.NONE).setText("- slf4j-simple:2.0.5"); //$NON-NLS-1$

        Link eplLicenseLinkLbl = new Link(licenseGrp, SWT.NONE);
        eplLicenseLinkLbl.setText("This software includes the work that is distributed in the <a>Eclipse Public License 1.0</a>"); //$NON-NLS-1$
        eplLicenseLinkLbl.addSelectionListener(new org.eclipse.swt.events.SelectionAdapter() {
            public void widgetSelected(org.eclipse.swt.events.SelectionEvent event) {
                try {
                    if (Desktop.isDesktopSupported()) {
                        Desktop.getDesktop().browse(new URI("https://www.eclipse.org/legal/epl-v10.html")); //$NON-NLS-1$
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        new Link(licenseGrp, SWT.NONE).setText("- commands 3.3.0-I20070605-0010"); //$NON-NLS-1$
        new Link(licenseGrp, SWT.NONE).setText("- common 3.3.0-v20070426"); //$NON-NLS-1$
        new Link(licenseGrp, SWT.NONE).setText("- ide 3.3.0-I20070620"); //$NON-NLS-1$
        new Link(licenseGrp, SWT.NONE).setText("- workbench 3.3.0-I20070608-1100"); //$NON-NLS-1$
        new Link(licenseGrp, SWT.NONE).setText("- jface 3.3.0-I20070606-0010"); //$NON-NLS-1$
        new Link(licenseGrp, SWT.NONE).setText("- org.eclipse.swt.win32.win32.x86 4.3"); //$NON-NLS-1$

        Link epl2LicenseLinkLbl = new Link(licenseGrp, SWT.NONE);
        epl2LicenseLinkLbl.setText("This software includes the work that is distributed in the <a>Eclipse Public License 2.0</a>"); //$NON-NLS-1$
        epl2LicenseLinkLbl.addSelectionListener(new org.eclipse.swt.events.SelectionAdapter() {
            public void widgetSelected(org.eclipse.swt.events.SelectionEvent event) {
                try {
                    if (Desktop.isDesktopSupported()) {
                        Desktop.getDesktop().browse(new URI("https://www.eclipse.org/legal/epl-v20.html")); //$NON-NLS-1$
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        new Link(licenseGrp, SWT.NONE).setText("- org.eclipse.swt.cocoa.macosx.x86_64 3.109.0"); //$NON-NLS-1$

        noDefaultAndApplyButton();
        return parent;
    }

    @Override
    public boolean performOk() {
        IPreferenceStore ps = getPreferenceStore();
        if (ps == null) {
            return true;
        }
        return true;
    }
}
