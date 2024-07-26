package api.test.controllers;

import java.util.function.BiConsumer;
import api.test.enums.ModeEnum;
import api.utilities.ExcelFetchUtil;
import api.utilities.GoogleDriveFetchUtil;
import api.utilities.GoogleSheetsFetchUtil;
import api.utilities.PropertyUtil;
import entities.IndependentEntity;
import interfaces.DataFetchInterface;
import io.restassured.response.Response;

public class NoValidateFetchController {

    private PropertyUtil pu = new PropertyUtil();
    private GoogleDriveFetchUtil gdfu = new GoogleDriveFetchUtil();
    private Integer currentCellIndex = 1; // Cell index start from 1, Array index start from 0
    private Boolean isRunning = true;
    private ModeEnum modeChosen;

    public NoValidateFetchController() {
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
            }
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
            BiConsumer<IndependentEntity, Response> innerFunction) {
        try {
            // AuthenUtil au = new AuthenUtil();
            //
            DataFetchInterface apiIE = this.sheetNameFetching(apiSheetName);
            // DataFetchInterface accountIE = this.sheetNameFetching(accountSheetName);
            //
            // accountIE.fetchingData(1);
            // String[] accountInfo = accountIE.getRowCellsData(1, "USERNAME", "PASSWORD");
            // accountIE.fetchClose();

            while (isRunning) {

                if (apiIE.isFetchingEnd(this.getCurrentCellIndex())) {
                    System.out.println("Fetching end");
                    apiIE.fetchClose();
                    isRunning = false;
                    break;
                } else {
                    apiIE.fetchingData(this.getCurrentCellIndex());
                    apiIE.isChainingNewGroup(this.getCurrentCellIndex());
                    String[] result = apiIE.getRowCellsData(this.getCurrentCellIndex(), "PATH",
                            "QUERY_PARAM",
                            "SUCCESS_RESPONSE", "LOGIN_REQUIRED");
                    String reqUrl = result[0];
                    String payload = result[1];
                    String expectedRes = result[2];
                    String authenRequire = result[3];

                    IndependentEntity dfe = new IndependentEntity(this.modeChosen.name(), reqUrl, payload, expectedRes,
                            "", authenRequire);
                    innerFunction.accept(dfe, null);

                    this.currentCellIndexIncrease();
                }
            }
        } catch (Exception e) {
            System.err
                    .println("Error in fetchController (index:" + this.getCurrentCellIndex() + "): "
                            + e.getMessage());
        }
    }
}
