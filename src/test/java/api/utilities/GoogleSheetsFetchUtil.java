package api.utilities;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.services.sheets.v4.model.ValueRange;
import api.test.services.GoogleServices;
import interfaces.DataFetchInterface;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GoogleSheetsFetchUtil implements DataFetchInterface {
    private PropertyUtil pu = new PropertyUtil();
    private GoogleServices gs = new GoogleServices();
    private String spreadSheetID;
    private String sheetName;
    private String maxColumnLetter; // ASCII: 65(A) - 90(Z), store Excel last column's letter
    private Integer maxRowIndex;
    private Integer fetchTimes = 0;
    private List<Object> columnList;
    private Integer offset = null;
    private ValueRange response;
    private NetHttpTransport HTTP_TRANSPORT;
    private Integer chunk = pu.getPropAsInt("FETCH_CHUNK");
    private Integer contentIndex = pu.getPropAsInt("CONTENT_INDEX");

    public GoogleSheetsFetchUtil(String sheetName) {
        try {

            this.HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
            this.sheetName = sheetName;
            this.spreadSheetID = pu.getPropAsString("SHEET_ID");
            setConfig();

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println(e.getMessage());
        }
    }

    @Override
    public void setConfig() {
        try {
            String columnAndRowRegex = "^.*[ ]\\d+,\\d.*$";
            String columnFindRegex = "^.*[A-Z_],[ ].+$";
            Pattern columnAndRowPattern = Pattern.compile(columnAndRowRegex);
            Pattern columnFindPattern = Pattern.compile(columnFindRegex);
            Matcher columnAndRowMatcher;
            Matcher columnFindMatcher;
            Integer index = 0;
            ValueRange getInformationRes = sheetDataRequest(this.sheetName + "!A1:S" + (contentIndex - 1));
            List<List<Object>> rowAndColumnListObj = getInformationRes.getValues();
            // Getting rows, columns amount infomation
            // Getting columns and theirs name into a list
            for (List<Object> list : rowAndColumnListObj) {
                columnAndRowMatcher = columnAndRowPattern.matcher(list.toString());
                columnFindMatcher = columnFindPattern.matcher(list.toString());

                if (columnAndRowMatcher.find()) {
                    for (Object obj : list) {
                        if (obj != null && obj.toString().length() > 0) {
                            String rowAndColumn = obj.toString().trim().split(" ")[1];
                            Integer maxColumnIndex = Integer.parseInt(rowAndColumn.split(",")[1]);
                            this.maxRowIndex = Integer.parseInt(rowAndColumn.split(",")[0]);
                            this.maxColumnLetter = indexToCellLetter(maxColumnIndex);
                        }
                    }
                } else if (columnFindMatcher.find())
                    this.columnList = getInformationRes.getValues().get(index);

                index++;
            }
        } catch (Exception e) {
            System.err.println("Error while setting config: " + e.getMessage());
        }
    }

    private String indexToCellLetter(Integer index) {
        // Add -1 to return correct letter
        return String.valueOf((char) (65 + index - 1));
    }

    @Override
    public void fetchingData(Integer currentIndex) {
        try {
            // System.out.println("offset: " + offset + ", fetchTimes: " + fetchTimes);
            // ofset == null : first time fetching
            // ((currentIndex - 1) % this.chunk == 0 :
            // if current row index - 1 modulus chunk == 0
            if (offset == null || ((currentIndex - 1) % this.chunk == 0)) {
                String sheetRange = this.sheetName + "!A"
                        + (contentIndex + fetchTimes * this.chunk) + ":"
                        + this.maxColumnLetter
                        + (contentIndex + fetchTimes * this.chunk + this.chunk - 1);
                this.response = sheetDataRequest(sheetRange);
                // System.out.println("Formula: " + sheetRange);
                this.offset = fetchTimes * this.chunk;
                this.fetchTimes += 1;
            }
        } catch (Exception e) {
            System.err.println("Error while fetching data: " + e.getMessage());
        }
    }

    @Override
    public String[] getRowCellsData(Integer rowIndex, String... colNames) {
        try {
            // Chunk: 5
            // Response index start from 0, [0,1,2,3,4]
            // Row(Cell) index start from 1, [1,2,3,4,5]
            // Whenever cellIndex is 1 and fetchTime > 1, need to update new range

            // System.out.println("\nIndex current: " + rowIndex);
            // System.out.println("Offset: " + offset + "\n");
            List<String> result = new ArrayList<>();
            rowIndex = (rowIndex - 1) % this.chunk;
            for (String colName : colNames) {
                Integer colIndex = this.columnList.indexOf(colName);
                // System.out.println(this.response.getValues().get(rowIndex).get(colIndex));
                if (isCellNull(rowIndex, colIndex)) {
                    result.add("");
                } else {
                    String cellData = this.response.getValues().get(rowIndex).get(colIndex).toString();
                    if (cellData == null) {
                        cellData = "";
                    }
                    result.add(cellData);
                }
            }
            return result.toArray(new String[0]);
        } catch (Exception e) {
            System.err
                    .println("[Online] Error while getting cell value in sheet (" + sheetName + "): " + e.getMessage());
            return null;
        }
    }

    private ValueRange sheetDataRequest(String range) {
        ValueRange response = null;
        try {
            response = gs.buildSheetService().spreadsheets().values()
                    .get(this.spreadSheetID, range)
                    .execute();
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error while getting sheet data, " + e.getMessage());
        }
        return response;
    }

    @Override
    public Boolean isCellNull(Integer rowIndex, Integer colIndex) {
        try {
            this.response.getValues().get(rowIndex).get(colIndex);
            return false;
        } catch (Exception e) {
            return true;
        }
    }

    @Override
    public Boolean isChainingNewGroup(Integer rowIndex) {
        rowIndex = (rowIndex - 1) % this.chunk;
        if (isCellNull(rowIndex, 0) && !isCellNull(rowIndex + 1, 0)) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public Boolean isFetchingEnd(Integer rowIndex) {
        try {
            // Check if next row index == max row index
            if ((rowIndex + 1) == this.maxRowIndex) {
                return true;
            } else {

                return false;
            }
        } catch (Exception e) {
            System.err.println("Error checking if fetching end: " + e.getMessage());
            System.out.println("\nError in index: " + rowIndex);
            return false;
        }
    }

    @Override
    public void fetchClose() {
        try {
            this.HTTP_TRANSPORT.shutdown();
            this.response = null;
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }
}
