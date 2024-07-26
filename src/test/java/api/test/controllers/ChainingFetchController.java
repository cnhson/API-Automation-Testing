package api.test.controllers;

import java.util.Arrays;
import java.util.HashSet;
import java.util.function.BiConsumer;
import com.google.gson.JsonObject;

import api.test.enums.ModeEnum;
import api.utilities.ExcelFetchUtil;
import api.utilities.GoogleDriveFetchUtil;
import api.utilities.GoogleSheetsFetchUtil;
import api.utilities.PropertyUtil;
import entities.AccountEntity;
import entities.ChainingEntity;
import interfaces.DataFetchInterface;

public class ChainingFetchController {

    private PropertyUtil pu = new PropertyUtil();
    private GoogleDriveFetchUtil gdfu = new GoogleDriveFetchUtil();
    private Integer currentCellIndex = 1; // Cell index start from 1, Array index start from 0
    private Boolean isRunning = true;
    private ModeEnum modeChosen;
    private HashSet<String> variableList = new HashSet<String>();
    private HashSet<String> verifyVarList = new HashSet<String>();
    private JsonObject jsonVariableAndValue = new JsonObject();
    private Boolean isFinalStep = false;

    public ChainingFetchController() {
        try {
            Boolean switchMode = Boolean.parseBoolean(pu.getPropAsString("AUTO_SWITCH_MODE"));
            this.modeChosen = ModeEnum.ONLINE;

            if (switchMode) {
                Boolean isModified = gdfu.checkIfRecentlyModified();
                if (isModified == null) {
                    gdfu.downloadSpreadSheetAsync().thenRun(() -> {
                        System.out.println("---Download completed---");
                    });
                    System.out.println(
                            "Overwrite to [ONLINE] mode since can't get the lastest modified timestamp from local file");
                } else if (isModified == true) {
                    gdfu.downloadSpreadSheetAsync().thenRun(() -> {
                        System.out.println("---Download completed---");
                    });
                    System.out.println("Overwrite to [ONLINE] mode since new modified timestampe found");
                } else {
                    System.out.println("Overwrite to [OFFLINE] mode because it haven't been modified since last time");
                    this.modeChosen = ModeEnum.OFFLINE;
                }
            } else
                System.out.println("Default [ONLINE] mode");

        } catch (Exception e) {
            System.err.println("Error while adjusting mode: " + e.getMessage());
            System.err.println("Switch to [Online] mode (default)");
            this.modeChosen = ModeEnum.ONLINE;
        }
    }

    public DataFetchInterface sheetNameFetching(String sheetName) {
        switch (this.modeChosen) {
            case OFFLINE:
                return new ExcelFetchUtil(sheetName);
            case ONLINE:
                return new GoogleSheetsFetchUtil(sheetName);
            default:
                return new ExcelFetchUtil(sheetName);
        }
    }

    public Boolean getIsRunning() {
        return this.isRunning;
    }

    public void setIsRunning(Boolean isRunning) {
        this.isRunning = isRunning;
    }

    public void currentCellIndexIncrease() {
        this.currentCellIndex += 1;
    }

    public Integer getCurrentCellIndex() {
        return this.currentCellIndex;
    }

    public void loopFetchingDataTest(String apiSheetName, String accountSheetName,
            BiConsumer<ChainingEntity, AccountEntity> innerFunction) {
        try {

            //
            DataFetchInterface apiIE = this.sheetNameFetching(apiSheetName);
            DataFetchInterface accountIE = this.sheetNameFetching(accountSheetName);
            //
            accountIE.fetchingData(1);
            String[] accountInfo = accountIE.getRowCellsData(1, "USERNAME", "PASSWORD");
            accountIE.fetchClose();

            while (isRunning) {
                if (apiIE.isFetchingEnd(this.currentCellIndex)) {
                    System.out.println("Fetching end");
                    apiIE.fetchClose();
                    isRunning = false;
                    break;
                } else {
                    if (apiIE.isChainingNewGroup(this.currentCellIndex)) {
                        this.currentCellIndexIncrease();
                        System.out.println("Empty line found -> Skip and go to New group");
                        continue;
                    }
                    apiIE.fetchingData(this.currentCellIndex);

                    String[] result = apiIE.getRowCellsData(this.currentCellIndex, "PATH", "QUERY_PARAM",
                            "LOGIN_REQUIRED", "VARIABLE", "VERIFY_VARIABLE");
                    String reqUrl = result[0];
                    String payload = result[1];
                    String authenRequire = result[2];
                    String variable = result[3];
                    String verifyVariable = result[4];

                    if (!variable.isBlank() && !variable.isEmpty()) {
                        variableList = convertExcelVariableToList(variable);
                        isFinalStep = false;
                    } else {
                        verifyVarList = convertExcelVariableToList(verifyVariable);
                        isFinalStep = true;
                    }

                    AccountEntity ae = new AccountEntity(accountInfo[0], accountInfo[1]);
                    ChainingEntity ce = new ChainingEntity(this.modeChosen.name(), reqUrl, isFinalStep, payload,
                            authenRequire, variableList, verifyVarList, jsonVariableAndValue);
                    innerFunction.accept(ce, ae);

                    this.currentCellIndexIncrease();
                }
            }
        } catch (Exception e) {
            System.err
                    .println("Error in fetchController (index:" + this.getCurrentCellIndex() + "): "
                            + e.getMessage());
        }
    }

    public HashSet<String> convertExcelVariableToList(String excelVariable) {
        HashSet<String> tempList = new HashSet<String>();
        if (excelVariable.contains(",")) {
            String[] varArray = excelVariable.split(",\\s*");
            tempList.addAll(Arrays.asList(varArray));
        } else {
            tempList.add(excelVariable);
        }
        return tempList;
    }
}
