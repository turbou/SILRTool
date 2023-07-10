package com.contrastsecurity.silrtool;

import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.swt.widgets.Shell;

public class LayerProgressMonitorDialog extends ProgressMonitorDialog {

    private String title;

    public LayerProgressMonitorDialog(Shell parent, String title) {
        super(parent);
        this.title = title;
    }

    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText(this.title);
    }

}
