package api.test.utilities;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import api.test.interfaces.DataFetchInterface;

public class ExcelFetchUtil implements DataFetchInterface {

	PropertyUtil pu = new PropertyUtil();
	private Sheet sheet;
	private Workbook workbook;
	private FileInputStream fis;
	private Integer maxRowIndex;
	private Integer maxColumnIndex;
	private DataFormatter dataFormatter = new DataFormatter();
	private ArrayList<String> columnList = new ArrayList<>();
	private Integer contentIndex = pu.getPropAsInt("CONTENT_INDEX");
	private String fileName = pu.getPropAsString("EXCEL_FILE");
	private String PUBLIC_RESOURCE_PATH = "src/test/resources/public/";

	public ExcelFetchUtil(String sheetName) {
		try {

			File file = new File(PUBLIC_RESOURCE_PATH + fileName + ".xlsx");
			this.fis = new FileInputStream(file.getPath());
			this.workbook = new XSSFWorkbook(fis);
			this.sheet = workbook.getSheet(sheetName);
			setConfig();

		}
		catch (Exception e) {
			System.err.println("\n[ExcelFetchUtil] Error: " + e.getMessage());
		}
	}

	@Override
	public void fetchingData(Integer currentIndex) {

	}

	@Override
	public void setConfig() {
		try {
			String columnAndRowRegex = "Rows,Columns";
			Integer columnIndex = this.contentIndex - 2;
			// Integer columnAmount =
			// this.sheet.getRow(columnIndex).getPhysicalNumberOfCells();

			for (Integer rowIndex = 0; rowIndex <= columnIndex; rowIndex++) {
				for (Integer colIndex = 0; colIndex < 15; colIndex++) {
					Cell cell = this.sheet.getRow(rowIndex).getCell(colIndex);
					if (cell != null) {
						if (cell.getStringCellValue().contains(columnAndRowRegex)) {
							String rowAndColumn = cell.getStringCellValue().trim().split(" ")[1];
							this.maxColumnIndex = Integer.parseInt(rowAndColumn.split(",")[1]);
							this.maxRowIndex = Integer.parseInt(rowAndColumn.split(",")[0]);
						} else if (rowIndex == columnIndex) {
							this.columnList.add(cell.getStringCellValue());

						}
					}
				}
			}

		}
		catch (Exception e) {
			System.err
					.println("[ExcelFetchUtil] Error while setting config columns' index: " + e.getMessage());

		}
	}

	@Override
	public String[] getRowCellsData(Integer rowIndex, String... colNames) {
		Integer colIndex = 0;
		try {
			dataFormatter.setUseCachedValuesForFormulaCells(true);
			List<String> result = new ArrayList<>();
			for (String colName : colNames) {
				colIndex = this.columnList.indexOf(colName);
				if (colIndex == -1) {
					throw new Exception("Can not get index of column: " + colName);
				}
				if (!isCellNull(rowIndex + this.contentIndex - 2, colIndex)) {
					Cell cell = this.sheet.getRow(rowIndex + this.contentIndex - 2).getCell(colIndex);

					switch (cell.getCellType()) {
					case NUMERIC:
						result.add(String.valueOf((int) cell.getNumericCellValue()));
						break;
					case FORMULA:
						result.add(dataFormatter.formatCellValue(cell));
						break;
					case STRING:
						result.add(cell.getRichStringCellValue().getString());
						break;
					default:
						result.add("");
						break;
					}

				} else {
					result.add("");
				}
			}
			return result.toArray(new String[0]);
		}
		catch (Exception e) {
			System.err.println("\n[ExcelFetchUtil] Error while getting cell [" + rowIndex + "," + colIndex
					+ "]: " + e.getMessage());
			return null;

		}
	}

	@Override
	public Boolean isCellNull(Integer rowIndex, Integer colIndex) {
		try {
			this.sheet.getRow(rowIndex).getCell(colIndex);
			return false;
		}
		catch (Exception e) {
			return true;
		}
	}

	@Override
	public Boolean isFetchingEnd(Integer rowIndex) {
		try {
			if (rowIndex + 1 == this.maxRowIndex) {
				return true;
			} else {
				return false;
			}

		}
		catch (Exception e) {
			System.err.println("\n[ExcelFetchUtil] Error while checking if fetching end: " + e.getMessage());
			return false;
		}
	}

	@Override
	public void fetchClose() {
		try {
			this.sheet = null;
			this.workbook.close();
			this.fis.close();
		}
		catch (Exception e) {
			System.err.println("\n[ExcelFetchUtil] Error while closing: " + e.getMessage());
		}
	}
}
