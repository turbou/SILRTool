package mori;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.apache.commons.exec.OS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.lambda.LambdaClient;
import software.amazon.awssdk.services.lambda.model.Environment;
import software.amazon.awssdk.services.lambda.model.EnvironmentResponse;
import software.amazon.awssdk.services.lambda.model.FunctionConfiguration;
import software.amazon.awssdk.services.lambda.model.LambdaException;
import software.amazon.awssdk.services.lambda.model.Layer;
import software.amazon.awssdk.services.lambda.model.ListFunctionsResponse;
import software.amazon.awssdk.services.lambda.model.UpdateFunctionConfigurationRequest;
import software.amazon.awssdk.services.lambda.model.UpdateFunctionConfigurationResponse;

public class Main {

    private ServerLessToolShell shell;
    public static final int MINIMUM_SIZE_WIDTH = 720;
    public static final int MINIMUM_SIZE_WIDTH_MAC = 720;
    public static final int MINIMUM_SIZE_HEIGHT = 670;

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
    // private LambdaClient awsLambda;

    private Map<String, LambdaFunction> fullAppMap;
    private List<String> srcApps = new ArrayList<String>();

    public static void main(String[] args) {
        Main main = new Main();
        main.createPart();
    }

    private void createPart() {
        Display display = new Display();
        shell = new ServerLessToolShell(display, this);
        if (OS.isFamilyMac()) {
            shell.setMinimumSize(MINIMUM_SIZE_WIDTH_MAC, MINIMUM_SIZE_HEIGHT);
        } else {
            shell.setMinimumSize(MINIMUM_SIZE_WIDTH, MINIMUM_SIZE_HEIGHT);
        }
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

        appLoadBtn = new Button(assessShell, SWT.PUSH);
        GridData appLoadBtnGrDt = new GridData(GridData.FILL_HORIZONTAL);
        appLoadBtnGrDt.horizontalSpan = 3;
        appLoadBtn.setLayoutData(appLoadBtnGrDt);
        appLoadBtn.setText("Load Function");
        appLoadBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                Region region = Region.AP_NORTHEAST_1;
                LambdaClient awsLambda = LambdaClient.builder().region(region).credentialsProvider(ProfileCredentialsProvider.create()).build();
                System.out.println("start");
                listFunctions(awsLambda);
                System.out.println("end");
                awsLambda.close();
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
        orgTableGrp.setText("Function List");

        srcListFilter = new Text(orgTableGrp, SWT.BORDER);
        GridData srcListFilterGrDt = new GridData(GridData.FILL_HORIZONTAL);
        // srcListFilterGrDt.horizontalSpan = 1;
        srcListFilter.setLayoutData(srcListFilterGrDt);
        srcListFilter.setMessage("Filter...");
        srcListFilter.addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent event) {
                srcListFilterUpdate();
            }
        });
        new Label(orgTableGrp, SWT.LEFT).setText(""); //$NON-NLS-1$

        table = new Table(orgTableGrp, SWT.BORDER | SWT.MULTI | SWT.FULL_SELECTION);
        GridData tableGrDt = new GridData(GridData.FILL_BOTH);
        table.setLayoutData(tableGrDt);
        table.setLinesVisible(true);
        table.setHeaderVisible(true);
        TableColumn column0 = new TableColumn(table, SWT.NONE);
        column0.setWidth(0);
        column0.setResizable(false);
        TableColumn column1 = new TableColumn(table, SWT.CENTER);
        column1.setWidth(50);
        column1.setText("");
        TableColumn column2 = new TableColumn(table, SWT.LEFT);
        column2.setWidth(600);
        column2.setText("Function Name");
        TableColumn column3 = new TableColumn(table, SWT.LEFT);
        column3.setWidth(150);
        column3.setText("Runtime");

        Composite buttonGrp = new Composite(orgTableGrp, SWT.NONE);
        buttonGrp.setLayoutData(new GridData(GridData.FILL_VERTICAL));
        buttonGrp.setLayout(new GridLayout(1, true));

        bulkOnBtn = new Button(buttonGrp, SWT.NULL);
        bulkOnBtn.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        bulkOnBtn.setText("All On");
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
        bulkOffBtn.setText("All Off");
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
        addBtn.setText("Add Layer");
        addBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                List<LambdaFunction> targetFuncs = new ArrayList<LambdaFunction>();
                for (Button chkBtn : checkBoxList) {
                    if (chkBtn.getSelection()) {
                        targetFuncs.add(funcList.get(checkBoxList.indexOf(chkBtn)));
                    }
                }
                for (LambdaFunction func : targetFuncs) {
                    EnvironmentResponse envRes = func.getConfig().environment();
                    Map<String, String> valueMap = envRes.variables();
                    Map<String, String> valueMap2 = new HashMap<String, String>(valueMap);
                    if (valueMap2.containsKey("AWS_LAMBDA_EXEC_WRAPPER")) {
                        String value = valueMap2.get("AWS_LAMBDA_EXEC_WRAPPER");
                        if (value.equals("/opt/otel-handler")) {
                            System.out.println("there is already a wrapper on this function ... skipping");
                        } else {
                            valueMap2.remove("AWS_LAMBDA_EXEC_WRAPPER");
                        }
                    }
                    valueMap2.putIfAbsent("AWS_LAMBDA_EXEC_WRAPPER", "/opt/otel-handler");
                    valueMap2.putIfAbsent("CONTRAST_BUCKET", "contrast-4u3yh-contrasts3bucket-103dyfnbq1dn4");
                    Environment environment = Environment.builder().variables(valueMap2).build();

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
                        layerArns.add("arn:aws:lambda:ap-northeast-1:570099478530:layer:contrast-instrumentation-extension-nodejs-x86_64-v1-3-0:1");
                    } else if (func.getRuntime().toLowerCase().startsWith("python")) {
                        layerArns.add("arn:aws:lambda:ap-northeast-1:570099478530:layer:contrast-instrumentation-extension-python-x86_64-v1-3-0:1");
                    } else {
                        System.out.println(String.format("Layer not found for runtime %s", func.getRuntime()));
                    }
                    Region region = Region.AP_NORTHEAST_1;
                    LambdaClient awsLambda = LambdaClient.builder().region(region).credentialsProvider(ProfileCredentialsProvider.create()).build();
                    updateFunctionConfiguration(awsLambda, func.getName(), environment, layerArns);
                }
            }
        });

        final Button rmvBtn = new Button(buttonGrp, SWT.NULL);
        rmvBtn.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        rmvBtn.setText("Remove Layer");
        rmvBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                List<LambdaFunction> targetFuncs = new ArrayList<LambdaFunction>();
                for (Button chkBtn : checkBoxList) {
                    if (chkBtn.getSelection()) {
                        targetFuncs.add(funcList.get(checkBoxList.indexOf(chkBtn)));
                    }
                }
                for (LambdaFunction func : targetFuncs) {
                    EnvironmentResponse envRes = func.getConfig().environment();
                    Map<String, String> valueMap = envRes.variables();
                    Map<String, String> valueMap2 = new HashMap<String, String>(valueMap);
                    if (valueMap2.containsKey("AWS_LAMBDA_EXEC_WRAPPER")) {
                        valueMap2.remove("AWS_LAMBDA_EXEC_WRAPPER");
                    }
                    if (valueMap2.containsKey("CONTRAST_BUCKET")) {
                        valueMap2.remove("CONTRAST_BUCKET");
                    }
                    Environment environment = Environment.builder().variables(valueMap2).build();

                    List<Layer> layers = func.getConfig().layers();
                    List<String> layerArns = new ArrayList<String>();
                    for (Layer layer : layers) {
                        String layerName = layer.arn().split(":")[layer.arn().split(":").length - 2];
                        if (!layerName.startsWith("contrast-instrumentation-extension")) {
                            layerArns.add(layer.arn());
                        }
                    }

                    Region region = Region.AP_NORTHEAST_1;
                    LambdaClient awsLambda = LambdaClient.builder().region(region).credentialsProvider(ProfileCredentialsProvider.create()).build();
                    updateFunctionConfiguration(awsLambda, func.getName(), environment, layerArns);
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
        new Label(orgTableGrp, SWT.LEFT).setText(""); //$NON-NLS-1$

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
        item.setText(3, org.getRuntime());
    }

    public void listFunctions(LambdaClient awsLambda) {
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
            ListFunctionsResponse functionResult = awsLambda.listFunctions();
            List<FunctionConfiguration> list = functionResult.functions();
            List<FunctionConfiguration> sorted = list.stream().sorted(Comparator.comparing(FunctionConfiguration::functionName)).collect(Collectors.toList());
            for (FunctionConfiguration config : sorted) {
                System.out.println("The function name is " + config.functionName());
                // function_data = lambda_client.get_function(FunctionName=function_name).get("Configuration")
                LambdaFunction func = new LambdaFunction(config);
                funcList.add(func);
                fullAppMap.put(func.getName(), func);
            }
            for (LambdaFunction func : funcList) {
                addFuncToTable(func);
            }
            srcCount.setText(String.valueOf(funcList.size()));
        } catch (LambdaException e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }
    }

    private void updateFunctionConfiguration(LambdaClient awsLambda, String functionName, Environment environment, List<String> layers) {
        try {
            UpdateFunctionConfigurationRequest configurationRequest = UpdateFunctionConfigurationRequest.builder().functionName(functionName).environment(environment)
                    .layers(layers).build();
            UpdateFunctionConfigurationResponse response = awsLambda.updateFunctionConfiguration(configurationRequest);
            System.out.println(response);
        } catch (LambdaException le) {
            le.printStackTrace();
            System.err.println(le.getMessage());
            System.exit(1);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
