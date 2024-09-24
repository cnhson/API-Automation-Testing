package api.test.controllers;

import java.util.Arrays;
import java.util.HashSet;
import org.apache.commons.lang3.function.TriConsumer;

import api.test.entities.AccountEntity;
import api.test.entities.ChainingEntity;
import api.test.enums.ModeEnum;
import api.test.interfaces.DataFetchInterface;
import api.test.utilities.ExcelFetchUtil;
import api.test.utilities.GoogleDriveFetchUtil;
import api.test.utilities.GoogleSheetsFetchUtil;
import api.test.utilities.PropertyUtil;

public class ChainingFetchController {

	private PropertyUtil pu = new PropertyUtil();
	private GoogleDriveFetchUtil gdfu = new GoogleDriveFetchUtil();
	private Integer currentCellIndex = 1; // Cell index start from 1, Array index start from 0
	private Boolean isRunning = true;
	private ModeEnum modeChosen;
	private HashSet<String> variableList = new HashSet<String>();
	private HashSet<String> verifyVarList = new HashSet<String>();
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
					System.out.println(
							"Overwrite to [OFFLINE] mode because it haven't been modified since last time");
					this.modeChosen = ModeEnum.OFFLINE;
				}
			} else
				System.out.println("Default [ONLINE] mode");

		}
		catch (Exception e) {
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

	public String getFetchMode() {
		return this.modeChosen.name();
	}

	public void loopFetchingDataTest(String apiSheetName,
			TriConsumer<ChainingEntity, AccountEntity, Object[]> innerFunction) {
		try {
			//
			DataFetchInterface apiIE = this.sheetNameFetching(apiSheetName);

			//
			String username = pu.getPropAsString("USERNAME");
			String password = pu.getPropAsString("PASSWORD");
			AccountEntity ae = new AccountEntity(username, password);
			//

			Object[] groupInfo = new Object[2];
			String groupDes = "";
			Integer groupId = 1;
			ChainingEntity ce;
			while (isRunning) {
				if (apiIE.isFetchingEnd(this.getCurrentCellIndex())) {
					System.out.println("[ChainingFetchController] Fetching end");
					apiIE.fetchClose();
					isRunning = false;
					ce = null;
				} else {
					apiIE.fetchingData(this.currentCellIndex);
					String[] result = apiIE.getRowCellsData(this.currentCellIndex, "ID", "DESCRIPTION",
							"PATH", "QUERY_PARAM", "LOGIN_REQUIRED", "VARIABLE", "VERIFY_VARIABLE");
					if (result[0] == null || result[0].isEmpty()) {
						ce = null;
					} else {
						if (!result[1].isEmpty() && groupDes != result[1]) {
							groupDes = result[1];
						}
						String id = result[0];
						String reqUrl = result[2];
						String payload = result[3];
						String authenRequire = result[4];
						String variable = result[5];
						String verifyVariable = result[6];
						if (!variable.isBlank() && !variable.isEmpty()) {
							variableList = convertExcelVariableToList(variable);
							isFinalStep = false;
						} else {
							verifyVarList = convertExcelVariableToList(verifyVariable);
							isFinalStep = true;
						}
						ce = new ChainingEntity(id, reqUrl, isFinalStep, payload, authenRequire, variableList,
								verifyVarList);
					}
					this.currentCellIndexIncrease();
				}
				groupInfo[0] = groupId;
				groupInfo[1] = groupDes;
				innerFunction.accept(ce, ae, groupInfo);
				if (ce == null)
					groupId++;
			}

		}
		catch (Exception e) {
			System.err.println("[ChainingFetchController] error (index:" + this.getCurrentCellIndex() + "): "
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
