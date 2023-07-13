package com.contrastsecurity.silrtool;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.apache.commons.exec.OS;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.jface.preference.PreferenceManager;
import org.eclipse.jface.preference.PreferenceNode;
import org.eclipse.jface.preference.PreferenceStore;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.events.ShellListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.jasypt.util.text.BasicTextEncryptor;

import com.contrastsecurity.silrtool.exception.SILRLambdaException;
import com.contrastsecurity.silrtool.model.LambdaFunction;
import com.contrastsecurity.silrtool.preference.AboutPage;
import com.contrastsecurity.silrtool.preference.BasePreferencePage;
import com.contrastsecurity.silrtool.preference.ConnectionPreferencePage;
import com.contrastsecurity.silrtool.preference.MyPreferenceDialog;
import com.contrastsecurity.silrtool.preference.PreferenceConstants;

import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider;
import software.amazon.awssdk.http.SdkHttpClient;
import software.amazon.awssdk.http.SdkHttpConfigurationOption;
import software.amazon.awssdk.http.apache.ApacheHttpClient;
import software.amazon.awssdk.http.apache.ProxyConfiguration;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.lambda.LambdaClient;
import software.amazon.awssdk.services.lambda.LambdaClientBuilder;
import software.amazon.awssdk.services.lambda.model.FunctionConfiguration;
import software.amazon.awssdk.services.lambda.model.ListFunctionsResponse;
import software.amazon.awssdk.utils.AttributeMap;

public class Main {

    private ServerLessToolShell shell;
    public static final String WINDOW_TITLE = "SILRTool"; //$NON-NLS-1$
    public static final int MINIMUM_SIZE_WIDTH = 720;
    public static final int MINIMUM_SIZE_WIDTH_MAC = 720;
    public static final int MINIMUM_SIZE_HEIGHT = 670;

    public static final String MASTER_PASSWORD = "changeme!"; //$NON-NLS-1$

    private Button funcLoadBtn;
    private List<LambdaFunction> funcList;
    private List<Button> checkBoxList = new ArrayList<Button>();
    private List<Integer> selectedIdxes = new ArrayList<Integer>();
    private Text funcListFilter;
    private Label funcCount;
    private Table table;
    private Button bulkOnBtn;
    private Button bulkOffBtn;
    private Button addLayerBtn;
    private Button rmvLayerBtn;
    private Button settingsBtn;

    private ColumnOrder column2Order;
    private ColumnOrder column3Order;
    private ColumnOrder column4Order;

    private PreferenceStore ps;
    private PreferenceDialog preferenceDialog;

    private Map<String, LambdaFunction> fullFuncMap;

    public enum ColumnOrder {
        ASC,
        DESC,
        NONE
    }

    public static void main(String[] args) {
        Main main = new Main();
        main.initialize();
        main.createPart();
    }

    private void initialize() {
        try {
            String homeDir = System.getProperty("user.home"); //$NON-NLS-1$
            this.ps = new PreferenceStore(homeDir + "\\silrtool.properties"); //$NON-NLS-1$
            if (OS.isFamilyMac()) {
                this.ps = new PreferenceStore(homeDir + "/silrtool.properties"); //$NON-NLS-1$
            }
            try {
                this.ps.load();
            } catch (FileNotFoundException fnfe) {
                this.ps = new PreferenceStore("silrtool.properties"); //$NON-NLS-1$
                this.ps.load();
            }
        } catch (FileNotFoundException fnfe) {
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            this.ps.setDefault(PreferenceConstants.PROXY_AUTH, "none"); //$NON-NLS-1$
            this.ps.setDefault(PreferenceConstants.REGION, "ap-northeast-1"); //$NON-NLS-1$
            this.ps.setDefault(PreferenceConstants.ENV_EXEC_WRAPPER, "/opt/otel-handler"); //$NON-NLS-1$
        } catch (Exception e) {
            // e.printStackTrace();
        }
    }

    private void createPart() {
        Display display = new Display();
        shell = new ServerLessToolShell(display, this);
        if (OS.isFamilyMac()) {
            shell.setMinimumSize(MINIMUM_SIZE_WIDTH_MAC, MINIMUM_SIZE_HEIGHT);
        } else {
            shell.setMinimumSize(MINIMUM_SIZE_WIDTH, MINIMUM_SIZE_HEIGHT);
        }
        Image[] imageArray = new Image[5];
        imageArray[0] = new Image(display, Main.class.getClassLoader().getResourceAsStream("icon16.png")); //$NON-NLS-1$
        imageArray[1] = new Image(display, Main.class.getClassLoader().getResourceAsStream("icon24.png")); //$NON-NLS-1$
        imageArray[2] = new Image(display, Main.class.getClassLoader().getResourceAsStream("icon32.png")); //$NON-NLS-1$
        imageArray[3] = new Image(display, Main.class.getClassLoader().getResourceAsStream("icon48.png")); //$NON-NLS-1$
        imageArray[4] = new Image(display, Main.class.getClassLoader().getResourceAsStream("icon128.png")); //$NON-NLS-1$
        shell.setImages(imageArray);
        Window.setDefaultImages(imageArray);
        setWindowTitle();
        shell.addShellListener(new ShellListener() {
            @Override
            public void shellIconified(ShellEvent event) {
            }

            @Override
            public void shellDeiconified(ShellEvent event) {
            }

            @Override
            public void shellDeactivated(ShellEvent event) {
            }

            @Override
            public void shellClosed(ShellEvent event) {
                ps.setValue(PreferenceConstants.MEM_WIDTH, shell.getSize().x);
                ps.setValue(PreferenceConstants.MEM_HEIGHT, shell.getSize().y);
                ps.setValue(PreferenceConstants.PROXY_TMP_USER, ""); //$NON-NLS-1$
                ps.setValue(PreferenceConstants.PROXY_TMP_PASS, ""); //$NON-NLS-1$
                try {
                    ps.save();
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                }
            }

            @Override
            public void shellActivated(ShellEvent event) {
                boolean ngRequiredFields = false;
                String layerArnPython = ps.getString(PreferenceConstants.LAYER_ARN_PYTHON);
                String layerArnNodeJS = ps.getString(PreferenceConstants.LAYER_ARN_NODEJS);
                String envExecWrapper = ps.getString(PreferenceConstants.ENV_EXEC_WRAPPER);
                String envS3Bucket = ps.getString(PreferenceConstants.ENV_S3_BUCKET);
                if (layerArnPython.isEmpty() || layerArnNodeJS.isEmpty() || envExecWrapper.isEmpty() || envS3Bucket.isEmpty()) {
                    ngRequiredFields = true;
                }
                if (ngRequiredFields) {
                    addLayerBtn.setEnabled(false);
                    rmvLayerBtn.setEnabled(false);
                } else {
                    addLayerBtn.setEnabled(true);
                    rmvLayerBtn.setEnabled(true);
                }
                setWindowTitle();
                if (ps.getBoolean(PreferenceConstants.PROXY_YUKO) && ps.getString(PreferenceConstants.PROXY_AUTH).equals("input")) { //$NON-NLS-1$
                    String proxy_usr = ps.getString(PreferenceConstants.PROXY_TMP_USER);
                    String proxy_pwd = ps.getString(PreferenceConstants.PROXY_TMP_PASS);
                    if (proxy_usr == null || proxy_usr.isEmpty() || proxy_pwd == null || proxy_pwd.isEmpty()) {
                        ProxyAuthDialog proxyAuthDialog = new ProxyAuthDialog(shell);
                        int result = proxyAuthDialog.open();
                        if (IDialogConstants.CANCEL_ID == result) {
                            ps.setValue(PreferenceConstants.PROXY_AUTH, "none"); //$NON-NLS-1$
                        } else {
                            ps.setValue(PreferenceConstants.PROXY_TMP_USER, proxyAuthDialog.getUsername());
                            ps.setValue(PreferenceConstants.PROXY_TMP_PASS, proxyAuthDialog.getPassword());
                        }
                    }
                }
            }
        });

        fullFuncMap = new TreeMap<String, LambdaFunction>();
        funcList = new ArrayList<LambdaFunction>();

        GridLayout baseLayout = new GridLayout(1, false);
        baseLayout.marginWidth = 8;
        baseLayout.marginBottom = 0;
        baseLayout.verticalSpacing = 8;
        shell.setLayout(baseLayout);

        Composite composite = new Composite(shell, SWT.NONE);
        composite.setLayout(new GridLayout(1, false));
        GridData compositeGrDt = new GridData(GridData.FILL_BOTH);
        composite.setLayoutData(compositeGrDt);

        Font bigFont = new Font(display, "Arial", 20, SWT.NORMAL);
        funcLoadBtn = new Button(composite, SWT.PUSH);
        GC gc = new GC(funcLoadBtn);
        gc.setFont(bigFont);
        Point bigBtnSize = gc.textExtent(Messages.getString("main.vul.export.button.title"));
        gc.dispose();
        GridData funcLoadBtnGrDt = new GridData(GridData.FILL_HORIZONTAL);
        funcLoadBtnGrDt.minimumHeight = 36;
        funcLoadBtnGrDt.heightHint = bigBtnSize.y + 16;
        funcLoadBtnGrDt.horizontalSpan = 3;
        funcLoadBtn.setLayoutData(funcLoadBtnGrDt);
        funcLoadBtn.setText("関数の読み込み");
        funcLoadBtn.setFont(new Font(display, "Arial", 16, SWT.NORMAL)); //$NON-NLS-1$
        funcLoadBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                listFunctions();
            }
        });

        Group funcTableGrp = new Group(composite, SWT.NONE);
        GridLayout funcTableGrpLt = new GridLayout(2, false);
        funcTableGrpLt.marginWidth = 10;
        funcTableGrpLt.horizontalSpacing = 10;
        funcTableGrp.setLayout(funcTableGrpLt);
        GridData funcTableGrpGrDt = new GridData(GridData.FILL_BOTH);
        funcTableGrp.setLayoutData(funcTableGrpGrDt);
        funcTableGrp.setText("関数一覧");

        funcListFilter = new Text(funcTableGrp, SWT.BORDER);
        GridData funcListFilterGrDt = new GridData(GridData.FILL_HORIZONTAL);
        funcListFilter.setLayoutData(funcListFilterGrDt);
        funcListFilter.setMessage("フィルタ...");
        funcListFilter.addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent event) {
                funcListFilterUpdate();
            }
        });
        new Label(funcTableGrp, SWT.LEFT).setText(""); //$NON-NLS-1$

        table = new Table(funcTableGrp, SWT.BORDER | SWT.FULL_SELECTION);
        GridData tableGrDt = new GridData(GridData.FILL_BOTH);
        table.setLayoutData(tableGrDt);
        table.setLinesVisible(true);
        table.setHeaderVisible(true);
        table.addListener(SWT.MouseDoubleClick, new Listener() {
            @Override
            public void handleEvent(Event event) {
                int index = table.getSelectionIndex();
                LambdaFunction func = funcList.get(index);
                LambdaFunctionDetailDialog filterDialog = new LambdaFunctionDetailDialog(shell, func);
                filterDialog.open();
            }
        });

        Menu menuTable = new Menu(table);
        table.setMenu(menuTable);

        MenuItem miDetail = new MenuItem(menuTable, SWT.NONE);
        miDetail.setText("詳細を見る");
        miDetail.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                int index = table.getSelectionIndex();
                LambdaFunction func = funcList.get(index);
                LambdaFunctionDetailDialog filterDialog = new LambdaFunctionDetailDialog(shell, func);
                filterDialog.open();
            }
        });

        TableColumn column0 = new TableColumn(table, SWT.NONE);
        column0.setWidth(0);
        column0.setResizable(false);
        TableColumn column1 = new TableColumn(table, SWT.CENTER);
        column1.setWidth(50);
        column1.setText("");
        TableColumn column2 = new TableColumn(table, SWT.LEFT);
        column2.setWidth(300);
        column2.setText("関数名");
        column2Order = ColumnOrder.ASC;
        column2.addListener(SWT.Selection, new Listener() {
            @Override
            public void handleEvent(Event event) {
                if (column2Order == ColumnOrder.DESC || column2Order == ColumnOrder.NONE) {
                    funcList = funcList.stream().sorted(Comparator.comparing(LambdaFunction::getName)).collect(Collectors.toList());
                    column2Order = ColumnOrder.ASC;
                    column3Order = ColumnOrder.NONE;
                    column4Order = ColumnOrder.NONE;
                } else {
                    funcList = funcList.stream().sorted(Comparator.comparing(LambdaFunction::getName).reversed()).collect(Collectors.toList());
                    column2Order = ColumnOrder.DESC;
                    column3Order = ColumnOrder.NONE;
                    column4Order = ColumnOrder.NONE;
                }
                updateTable();
            }
        });

        TableColumn column3 = new TableColumn(table, SWT.CENTER);
        column3.setWidth(60);
        column3.setText("Contrast");
        column3Order = ColumnOrder.NONE;
        column3.addListener(SWT.Selection, new Listener() {
            @Override
            public void handleEvent(Event event) {
                if (column3Order == ColumnOrder.DESC || column3Order == ColumnOrder.NONE) {
                    funcList = funcList.stream().sorted(Comparator.comparing(LambdaFunction::hasContrastLayerStr)).collect(Collectors.toList());
                    column2Order = ColumnOrder.NONE;
                    column3Order = ColumnOrder.ASC;
                    column4Order = ColumnOrder.NONE;
                } else {
                    funcList = funcList.stream().sorted(Comparator.comparing(LambdaFunction::hasContrastLayerStr).reversed()).collect(Collectors.toList());
                    column2Order = ColumnOrder.NONE;
                    column3Order = ColumnOrder.DESC;
                    column4Order = ColumnOrder.NONE;
                }
                updateTable();
            }
        });

        TableColumn column4 = new TableColumn(table, SWT.LEFT);
        column4.setWidth(120);
        column4.setText("ランタイム");
        column4Order = ColumnOrder.NONE;
        column4.addListener(SWT.Selection, new Listener() {
            @Override
            public void handleEvent(Event event) {
                if (column4Order == ColumnOrder.DESC || column4Order == ColumnOrder.NONE) {
                    funcList = funcList.stream().sorted(Comparator.comparing(LambdaFunction::getRuntime)).collect(Collectors.toList());
                    column2Order = ColumnOrder.NONE;
                    column3Order = ColumnOrder.NONE;
                    column4Order = ColumnOrder.ASC;
                } else {
                    funcList = funcList.stream().sorted(Comparator.comparing(LambdaFunction::getRuntime).reversed()).collect(Collectors.toList());
                    column2Order = ColumnOrder.NONE;
                    column3Order = ColumnOrder.NONE;
                    column4Order = ColumnOrder.DESC;
                }
                updateTable();
            }
        });

        Composite buttonGrp = new Composite(funcTableGrp, SWT.NONE);
        GridData buttonGrpGrDt = new GridData(GridData.FILL_VERTICAL);
        buttonGrp.setLayoutData(buttonGrpGrDt);
        buttonGrp.setLayout(new GridLayout(1, true));

        bulkOnBtn = new Button(buttonGrp, SWT.NULL);
        bulkOnBtn.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        bulkOnBtn.setText("全て On");
        bulkOnBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                for (LambdaFunction org : funcList) {
                    org.setValid(true);
                }
                selectedIdxes.clear();
                for (Button button : checkBoxList) {
                    button.setSelection(true);
                    selectedIdxes.add(checkBoxList.indexOf(button));
                }
            }
        });

        bulkOffBtn = new Button(buttonGrp, SWT.NULL);
        bulkOffBtn.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        bulkOffBtn.setText("全て Off");
        bulkOffBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                for (LambdaFunction org : funcList) {
                    org.setValid(false);
                }
                selectedIdxes.clear();
                for (Button button : checkBoxList) {
                    button.setSelection(false);
                }
            }
        });

        addLayerBtn = new Button(buttonGrp, SWT.NULL);
        addLayerBtn.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        addLayerBtn.setText("レイヤー登録");
        addLayerBtn.setEnabled(false);
        addLayerBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                List<LambdaFunction> targetFuncs = new ArrayList<LambdaFunction>();
                for (Button chkBtn : checkBoxList) {
                    if (chkBtn.getSelection()) {
                        targetFuncs.add(funcList.get(checkBoxList.indexOf(chkBtn)));
                    }
                }
                if (targetFuncs.isEmpty()) {
                    return;
                }
                LayerWithProgress progress = new AddLayerWithProgress(shell, ps, targetFuncs);
                ProgressMonitorDialog progDialog = new LayerProgressMonitorDialog(shell, "レイヤー登録");
                try {
                    progDialog.run(true, true, progress);
                } catch (InvocationTargetException e) {
                    if (e.getTargetException() instanceof SILRLambdaException) {
                        SILRLambdaException sle = (SILRLambdaException) e.getTargetException();
                        MessageDialog.openError(shell, sle.getFuncName(), sle.getMessage());
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

        rmvLayerBtn = new Button(buttonGrp, SWT.NULL);
        rmvLayerBtn.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        rmvLayerBtn.setText("レイヤー削除");
        rmvLayerBtn.setEnabled(false);
        rmvLayerBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                List<LambdaFunction> targetFuncs = new ArrayList<LambdaFunction>();
                for (Button chkBtn : checkBoxList) {
                    if (chkBtn.getSelection()) {
                        targetFuncs.add(funcList.get(checkBoxList.indexOf(chkBtn)));
                    }
                }
                if (targetFuncs.isEmpty()) {
                    return;
                }
                LayerWithProgress progress = new RmvLayerWithProgress(shell, ps, targetFuncs);
                ProgressMonitorDialog progDialog = new LayerProgressMonitorDialog(shell, "レイヤー削除");
                try {
                    progDialog.run(true, true, progress);
                } catch (InvocationTargetException e) {
                    if (e.getTargetException() instanceof SILRLambdaException) {
                        SILRLambdaException sle = (SILRLambdaException) e.getTargetException();
                        MessageDialog.openError(shell, sle.getFuncName(), sle.getMessage());
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

        settingsBtn = new Button(buttonGrp, SWT.NULL);
        GridData settingsBtnGrDt = new GridData(GridData.FILL_BOTH);
        settingsBtnGrDt.verticalAlignment = SWT.BOTTOM;
        settingsBtn.setLayoutData(settingsBtnGrDt);
        settingsBtn.setText("設定");
        settingsBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                PreferenceManager mgr = new PreferenceManager();
                PreferenceNode baseNode = new PreferenceNode("base", new BasePreferencePage(shell)); //$NON-NLS-1$
                PreferenceNode connectionNode = new PreferenceNode("connection", new ConnectionPreferencePage()); //$NON-NLS-1$
                mgr.addToRoot(baseNode);
                mgr.addToRoot(connectionNode);
                PreferenceNode aboutNode = new PreferenceNode("about", new AboutPage()); //$NON-NLS-1$
                mgr.addToRoot(aboutNode);
                preferenceDialog = new MyPreferenceDialog(shell, mgr);
                preferenceDialog.setPreferenceStore(ps);
                preferenceDialog.open();
                try {
                    ps.save();
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                }
            }
        });

        this.funcCount = new Label(funcTableGrp, SWT.RIGHT);
        GridData funcCountGrDt = new GridData(GridData.FILL_HORIZONTAL);
        funcCountGrDt.minimumHeight = 16;
        this.funcCount.setLayoutData(funcCountGrDt);
        this.funcCount.setFont(new Font(display, "Arial", 12, SWT.NORMAL)); //$NON-NLS-1$
        this.funcCount.setText("0"); //$NON-NLS-1$
        this.funcCount.setForeground(shell.getDisplay().getSystemColor(SWT.COLOR_DARK_GRAY));

        Label icon = new Label(funcTableGrp, SWT.NONE);
        GridData iconGrDt = new GridData(GridData.FILL_HORIZONTAL);
        iconGrDt.minimumHeight = 16;
        iconGrDt.horizontalAlignment = GridData.END;
        icon.setLayoutData(iconGrDt);
        Image iconImg = new Image(shell.getDisplay(), Main.class.getClassLoader().getResourceAsStream("help.png")); //$NON-NLS-1$
        icon.setImage(iconImg);
        icon.setToolTipText("使い方"); //$NON-NLS-1$
        icon.addListener(SWT.MouseUp, new Listener() {
            @Override
            public void handleEvent(Event event) {
                new HelpDialog(shell).open();
            }
        });

        int width = this.ps.getInt(PreferenceConstants.MEM_WIDTH);
        int height = this.ps.getInt(PreferenceConstants.MEM_HEIGHT);
        if (width > 0 && height > 0) {
            shell.setSize(width, height);
        } else {
            shell.setSize(MINIMUM_SIZE_WIDTH, MINIMUM_SIZE_HEIGHT);
            // shell.pack();
        }
        shell.open();
        try {
            while (!shell.isDisposed()) {
                if (!display.readAndDispatch()) {
                    display.sleep();
                }
            }
        } catch (Exception e) {
            StringWriter stringWriter = new StringWriter();
            PrintWriter printWriter = new PrintWriter(stringWriter);
            e.printStackTrace(printWriter);
            String trace = stringWriter.toString();
        }
        display.dispose();
    }

    private void funcListFilterUpdate() {
        for (Button button : checkBoxList) {
            button.dispose();
        }
        checkBoxList.clear();
        table.clearAll();
        table.removeAll();
        funcList.clear();
        String keyword = funcListFilter.getText().trim();
        if (keyword.isEmpty()) {
            for (String appLabel : fullFuncMap.keySet()) {
                funcList.add(fullFuncMap.get(appLabel));
            }
        } else {
            for (String appLabel : fullFuncMap.keySet()) {
                boolean isKeywordValid = true;
                if (!keyword.isEmpty()) {
                    if (!appLabel.toLowerCase().contains(keyword.toLowerCase())) {
                        isKeywordValid = false;
                    }
                }
                if (isKeywordValid) {
                    funcList.add(fullFuncMap.get(appLabel));
                }
            }
        }
        for (LambdaFunction func : funcList) {
            addFuncToTable(func);
        }
        funcCount.setText(String.valueOf(funcList.size()));
    }

    public void updateTableItem(LambdaFunction func) {
        int index = funcList.indexOf(func);
        TableItem item = table.getItem(index);
        item.setText(3, func.hasContrastLayerStr());
    }

    private void addFuncToTable(LambdaFunction org) {
        if (org == null) {
            return;
        }
        TableEditor editor = new TableEditor(table);
        Button button = new Button(table, SWT.CHECK);
        button.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                selectedIdxes.clear();
                for (Button button : checkBoxList) {
                    if (button.getSelection()) {
                        selectedIdxes.add(checkBoxList.indexOf(button));
                    }
                }
            }
        });
        button.pack();
        TableItem item = new TableItem(table, SWT.CENTER);
        editor.minimumWidth = button.getSize().x;
        editor.horizontalAlignment = SWT.CENTER;
        editor.setEditor(button, item, 1);
        checkBoxList.add(button);
        item.setText(2, org.getName());
        item.setText(3, org.hasContrastLayerStr());
        item.setText(4, org.getRuntime());
    }

    public void updateTable() {
        for (Button button : checkBoxList) {
            button.dispose();
        }
        checkBoxList.clear();
        table.clearAll();
        table.removeAll();
        for (LambdaFunction func : funcList) {
            addFuncToTable(func);
        }
        funcCount.setText(String.valueOf(funcList.size()));
    }

    public void listFunctions() {
        funcListFilter.setText("");
        for (Button button : checkBoxList) {
            button.dispose();
        }
        checkBoxList.clear();
        table.clearAll();
        table.removeAll();
        fullFuncMap.clear();
        funcList.clear();
        try {
            Region region = Region.of(ps.getString(PreferenceConstants.REGION));
            AttributeMap attrMap = null;
            if (this.ps.getBoolean(PreferenceConstants.IGNORE_SSLCERT_CHECK)) {
                attrMap = AttributeMap.builder().put(SdkHttpConfigurationOption.TRUST_ALL_CERTIFICATES, true).build();
            }
            SdkHttpClient httpClient = null;
            if (ps.getBoolean(PreferenceConstants.PROXY_YUKO)) {
                String proxyHostPort = String.format("%s:%s", ps.getString(PreferenceConstants.PROXY_HOST), ps.getString(PreferenceConstants.PROXY_PORT));
                ProxyConfiguration.Builder proxyBuilder = ProxyConfiguration.builder();
                proxyBuilder.endpoint(URI.create(proxyHostPort));
                if (!this.ps.getString(PreferenceConstants.PROXY_AUTH).equals("none")) { //$NON-NLS-1$
                    // プロキシ認証あり
                    if (this.ps.getString(PreferenceConstants.PROXY_AUTH).equals("input")) { //$NON-NLS-1$
                        proxyBuilder.username(ps.getString(PreferenceConstants.PROXY_TMP_USER));
                        proxyBuilder.password(ps.getString(PreferenceConstants.PROXY_TMP_PASS));
                    } else {
                        BasicTextEncryptor encryptor = new BasicTextEncryptor();
                        encryptor.setPassword(Main.MASTER_PASSWORD);
                        try {
                            String proxy_pass = encryptor.decrypt(this.ps.getString(PreferenceConstants.PROXY_PASS));
                            proxyBuilder.username(ps.getString(PreferenceConstants.PROXY_USER));
                            proxyBuilder.password(proxy_pass);
                        } catch (Exception e) {
                            throw new Exception("プロキシパスワードの復号化に失敗しました。\\r\\nパスワードの設定をやり直してください。");
                        }
                    }
                }
                if (attrMap != null) {
                    httpClient = ApacheHttpClient.builder().proxyConfiguration(proxyBuilder.build()).buildWithDefaults(attrMap);
                } else {
                    httpClient = ApacheHttpClient.builder().proxyConfiguration(proxyBuilder.build()).build();
                }
            } else {
                if (attrMap != null) {
                    httpClient = ApacheHttpClient.builder().buildWithDefaults(attrMap);
                }
            }
            LambdaClientBuilder clientBuilder = LambdaClient.builder();
            ProfileCredentialsProvider profileProvider = null;
            if (ps.getString(PreferenceConstants.PROFILE_NAME).isEmpty()) {
                profileProvider = ProfileCredentialsProvider.create();
            } else {
                profileProvider = ProfileCredentialsProvider.create(ps.getString(PreferenceConstants.PROFILE_NAME));
            }
            clientBuilder.region(region).credentialsProvider(profileProvider);
            if (httpClient != null) {
                clientBuilder.httpClient(httpClient);
            }
            LambdaClient awsLambda = clientBuilder.build();
            ListFunctionsResponse functionResult = awsLambda.listFunctions();
            List<FunctionConfiguration> list = functionResult.functions();
            List<FunctionConfiguration> sorted = list.stream().sorted(Comparator.comparing(FunctionConfiguration::functionName)).collect(Collectors.toList());
            for (FunctionConfiguration config : sorted) {
                if (config.functionName().toLowerCase().startsWith("contrast-")) {
                    continue;
                }
                LambdaFunction func = new LambdaFunction(config);
                funcList.add(func);
                fullFuncMap.put(func.getName(), func);
            }
            awsLambda.close();
            for (LambdaFunction func : funcList) {
                addFuncToTable(func);
            }
            funcCount.setText(String.valueOf(funcList.size()));
        } catch (Exception e) {
            MessageDialog.openError(shell, "関数の読み込み", e.getMessage());
        }
    }

    public void setWindowTitle() {
        this.shell.setText(String.format(WINDOW_TITLE));
    }
}
