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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class HelpDialog extends Dialog {

    public HelpDialog(Shell parentShell) {
        super(parentShell);
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        Composite composite = (Composite) super.createDialogArea(parent);
        composite.setLayout(new GridLayout(2, false));
        Text text = new Text(composite, SWT.MULTI | SWT.WRAP | SWT.READ_ONLY);
        text.setBackground(parent.getDisplay().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND));
        text.setLayoutData(new GridData(GridData.FILL_BOTH));
        List<String> strBuffer = new ArrayList<String>();
        strBuffer.add("[設定について]");
        strBuffer.add(" ・基本設定");
        strBuffer.add("    ・プロファイルが複数ある場合は使用するプロファイル名を設定します。ブランクの場合はdefaultプロファイルを使用します。");
        strBuffer.add("    ・リージョンを指定します。デフォルトはap-northeast-1");
        strBuffer.add("    ・Python,NodeJSの追加レイヤー、環境変数を設定します。");
        strBuffer.add("        Contrastサーバレスの設定タブのIAST解析のところで設定値を確認できます。");
        strBuffer.add(" ・接続設定");
        strBuffer.add("    ・プロキシを経由する場合、SSL検証回避をする場合はここで設定を行ってください。");
        strBuffer.add("");
        strBuffer.add("[使い方]");
        strBuffer.add(" 1. [関数の読み込み]をクリックして、デプロイされているラムダ関数がリストとして現れることを確認する。");
        strBuffer.add(" 2. [設定]をクリックして、[基本設定]よりリージョン、ARN、環境変数が適切に入力されているかを確認する。");
        strBuffer.add("     Contrast UIの\"サーバレス\"-\"設定\"-\"IAST解析の手順\"で表れているパラメータを見比べて");
        strBuffer.add("     入力されていない、間違っている場合は新しく書き直して[適用]-[OK]とクリックする。");
        strBuffer.add("");
        strBuffer.add("[制限事項]");
        strBuffer.add(" ・登録レイヤー数５（最大）となっているラムダ関数へのSILRTool経由でのレイヤー登録及び削除動作は保証しない。");
        strBuffer.add(" ・複数レイヤー登録/削除中にエラーが出た場合、エラーが出るまでに行ったレイヤー登録/削除は対応され、遡久しない。");
        text.setText(String.join("\r\n", strBuffer));
        return composite;
    }

    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
    }

    @Override
    protected void okPressed() {
        super.okPressed();
    }

    @Override
    protected void setShellStyle(int newShellStyle) {
        super.setShellStyle(SWT.CLOSE | SWT.TITLE | SWT.RESIZE);
    }

    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText("使い方");
    }
}
