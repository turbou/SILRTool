package com.contrastsecurity.silrtool;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.apache.commons.exec.OS;
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

import com.contrastsecurity.silrtool.exception.SILRLambdaException;
import com.contrastsecurity.silrtool.model.LambdaFunction;
import com.contrastsecurity.silrtool.preference.AboutPage;
import com.contrastsecurity.silrtool.preference.BasePreferencePage;
import com.contrastsecurity.silrtool.preference.ConnectionPreferencePage;
import com.contrastsecurity.silrtool.preference.MyPreferenceDialog;
import com.contrastsecurity.silrtool.preference.PreferenceConstants;

import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.lambda.LambdaClient;
import software.amazon.awssdk.services.lambda.model.FunctionConfiguration;
import software.amazon.awssdk.services.lambda.model.LambdaException;
import software.amazon.awssdk.services.lambda.model.ListFunctionsResponse;

public class Main {

    private ServerLessToolShell shell;
    public static final String WINDOW_TITLE = "SILRTool"; //$NON-NLS-1$
    public static final int MINIMUM_SIZE_WIDTH = 720;
    public static final int MINIMUM_SIZE_WIDTH_MAC = 720;
    public static final int MINIMUM_SIZE_HEIGHT = 670;

    public static final String MASTER_PASSWORD = "changeme!"; //$NON-NLS-1$

    private Button appLoadBtn;
    private List<LambdaFunction> funcList;
    private List<Button> checkBoxList = new ArrayList<Button>();
    private List<Integer> selectedIdxes = new ArrayList<Integer>();
    private Text srcListFilter;
    private Label srcCount;
    private Table table;
    private Button bulkOnBtn;
    private Button bulkOffBtn;
    private Button addBtn;
    private Button rmvBtn;
    private Button settingsBtn;

    private PreferenceStore ps;
    private PreferenceDialog preferenceDialog;

    private Map<String, LambdaFunction> fullAppMap;

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
                    addBtn.setEnabled(false);
                    rmvBtn.setEnabled(false);
                } else {
                    addBtn.setEnabled(true);
                    rmvBtn.setEnabled(true);
                }
                setWindowTitle();
            }
        });

        fullAppMap = new TreeMap<String, LambdaFunction>();
        funcList = new ArrayList<LambdaFunction>();

        GridLayout baseLayout = new GridLayout(1, false);
        baseLayout.marginWidth = 8;
        baseLayout.marginBottom = 0;
        baseLayout.verticalSpacing = 8;
        shell.setLayout(baseLayout);

        Composite assessShell = new Composite(shell, SWT.NONE);
        assessShell.setLayout(new GridLayout(1, false));
        GridData appListGrpGrDt = new GridData(GridData.FILL_BOTH);
        assessShell.setLayoutData(appListGrpGrDt);

        Font bigFont = new Font(display, "Arial", 20, SWT.NORMAL);
        appLoadBtn = new Button(assessShell, SWT.PUSH);
        GC gc = new GC(appLoadBtn);
        gc.setFont(bigFont);
        Point bigBtnSize = gc.textExtent(Messages.getString("main.vul.export.button.title"));
        gc.dispose();
        GridData appLoadBtnGrDt = new GridData(GridData.FILL_HORIZONTAL);
        appLoadBtnGrDt.minimumHeight = 36;
        appLoadBtnGrDt.heightHint = bigBtnSize.y + 16;
        appLoadBtnGrDt.horizontalSpan = 3;
        appLoadBtn.setLayoutData(appLoadBtnGrDt);
        appLoadBtn.setText("関数の読み込み");
        appLoadBtn.setFont(new Font(display, "Arial", 16, SWT.NORMAL)); //$NON-NLS-1$
        appLoadBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                listFunctions();
            }
        });

        Group orgTableGrp = new Group(assessShell, SWT.NONE);
        GridLayout orgTableGrpLt = new GridLayout(2, false);
        orgTableGrpLt.marginWidth = 10;
        orgTableGrpLt.horizontalSpacing = 10;
        orgTableGrp.setLayout(orgTableGrpLt);
        GridData orgTableGrpGrDt = new GridData(GridData.FILL_BOTH);
        // orgTableGrpGrDt.horizontalSpan = 3;
        orgTableGrp.setLayoutData(orgTableGrpGrDt);
        orgTableGrp.setText("関数一覧");

        srcListFilter = new Text(orgTableGrp, SWT.BORDER);
        GridData srcListFilterGrDt = new GridData(GridData.FILL_HORIZONTAL);
        // srcListFilterGrDt.horizontalSpan = 1;
        srcListFilter.setLayoutData(srcListFilterGrDt);
        srcListFilter.setMessage("フィルタ...");
        srcListFilter.addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent event) {
                srcListFilterUpdate();
            }
        });
        new Label(orgTableGrp, SWT.LEFT).setText(""); //$NON-NLS-1$

        table = new Table(orgTableGrp, SWT.BORDER);
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

        MenuItem miTag = new MenuItem(menuTable, SWT.NONE);
        miTag.setText("詳細を見る");
        miTag.addSelectionListener(new SelectionAdapter() {
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
        TableColumn column3 = new TableColumn(table, SWT.CENTER);
        column3.setWidth(60);
        column3.setText("Contrast");
        TableColumn column4 = new TableColumn(table, SWT.LEFT);
        column4.setWidth(120);
        column4.setText("ランタイム");

        Composite buttonGrp = new Composite(orgTableGrp, SWT.NONE);
        GridData buttonGrpGrDt = new GridData(GridData.FILL_VERTICAL);
        buttonGrpGrDt.verticalSpan = 2;
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

        addBtn = new Button(buttonGrp, SWT.NULL);
        addBtn.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        addBtn.setText("レイヤー登録");
        addBtn.setEnabled(false);
        addBtn.addSelectionListener(new SelectionAdapter() {
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

        rmvBtn = new Button(buttonGrp, SWT.NULL);
        rmvBtn.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        rmvBtn.setText("レイヤー削除");
        rmvBtn.setEnabled(false);
        rmvBtn.addSelectionListener(new SelectionAdapter() {
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

        this.srcCount = new Label(orgTableGrp, SWT.RIGHT);
        GridData srcCountGrDt = new GridData(GridData.FILL_HORIZONTAL);
        srcCountGrDt.minimumHeight = 16;
        this.srcCount.setLayoutData(srcCountGrDt);
        this.srcCount.setFont(new Font(display, "Arial", 12, SWT.NORMAL)); //$NON-NLS-1$
        this.srcCount.setText("0"); //$NON-NLS-1$
        this.srcCount.setForeground(shell.getDisplay().getSystemColor(SWT.COLOR_DARK_GRAY));

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

    private void srcListFilterUpdate() {
        for (Button button : checkBoxList) {
            button.dispose();
        }
        checkBoxList.clear();
        table.clearAll();
        table.removeAll();
        funcList.clear();
        String keyword = srcListFilter.getText().trim();
        if (keyword.isEmpty()) {
            for (String appLabel : fullAppMap.keySet()) {
                funcList.add(fullAppMap.get(appLabel));
            }
        } else {
            for (String appLabel : fullAppMap.keySet()) {
                boolean isKeywordValid = true;
                if (!keyword.isEmpty()) {
                    if (!appLabel.toLowerCase().contains(keyword.toLowerCase())) {
                        isKeywordValid = false;
                    }
                }
                if (isKeywordValid) {
                    funcList.add(fullAppMap.get(appLabel));
                }
            }
        }
        for (LambdaFunction func : funcList) {
            addFuncToTable(func);
        }
        srcCount.setText(String.valueOf(funcList.size()));
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

    public void listFunctions() {
        srcListFilter.setText("");
        for (Button button : checkBoxList) {
            button.dispose();
        }
        checkBoxList.clear();
        table.clearAll();
        table.removeAll();
        fullAppMap.clear();
        funcList.clear();
        try {
            Region region = Region.of(ps.getString(PreferenceConstants.REGION));
            LambdaClient awsLambda = LambdaClient.builder().region(region).credentialsProvider(ProfileCredentialsProvider.create()).build();
            ListFunctionsResponse functionResult = awsLambda.listFunctions();
            List<FunctionConfiguration> list = functionResult.functions();
            List<FunctionConfiguration> sorted = list.stream().sorted(Comparator.comparing(FunctionConfiguration::functionName)).collect(Collectors.toList());
            for (FunctionConfiguration config : sorted) {
                if (config.functionName().toLowerCase().startsWith("contrast-")) {
                    continue;
                }
                LambdaFunction func = new LambdaFunction(config);
                funcList.add(func);
                fullAppMap.put(func.getName(), func);
            }
            awsLambda.close();
            for (LambdaFunction func : funcList) {
                addFuncToTable(func);
            }
            srcCount.setText(String.valueOf(funcList.size()));
        } catch (LambdaException e) {
            System.err.println(e.getMessage());
        }
    }

    public void setWindowTitle() {
        this.shell.setText(String.format(WINDOW_TITLE));
    }
}
