package api.utilities;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.CompletableFuture;

import api.test.services.GoogleServices;

public class GoogleDriveFetchUtil {
    private GoogleServices gs = new GoogleServices();
    private PropertyUtil pu = new PropertyUtil();
    private String spreadSheetID = pu.getPropAsString("SHEET_ID");
    private String latestFilePath = "src/test/resources/public/lastestModifiedTime.txt";
    private Long onlineModifiedTime;
    private Long savedModifiedTime;

    public GoogleDriveFetchUtil() {
        try {
            getModifiedTime();
            readModifiedTimeFromFile();
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }

    public void getModifiedTime() throws IOException {
        com.google.api.services.drive.model.File file = gs.buildDriveService().files().get(spreadSheetID)
                .setFields("modifiedTime")
                .execute();
        this.onlineModifiedTime = file.getModifiedTime().getValue() / 1000;
    }

    public void saveModifiedTimeToFile(Long timestamp) throws IOException {
        try (OutputStream outputStream = new FileOutputStream(latestFilePath)) {
            outputStream.write(Long.toString(timestamp).getBytes());
            outputStream.close();
        }
    }

    public void readModifiedTimeFromFile() throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(latestFilePath))) {
            String sizeString = reader.readLine();
            reader.close();
            this.savedModifiedTime = Long.parseLong(sizeString);
        }
    }

    public CompletableFuture<Void> downloadSpreadSheetAsync() {
        return CompletableFuture.runAsync(() -> {
            try {
                saveModifiedTimeToFile(this.onlineModifiedTime);
                String outputPath = pu.getPropAsString("DOWNLOAD_LOCATION");
                String fileName = pu.getPropAsString("DOWNLOAD_FILENAME");
                System.out.println("---Downloading spreadsheet---");
                try (OutputStream outputStream = new FileOutputStream(outputPath + fileName + ".xlsx")) {
                    gs.buildDriveService().files()
                            .export(this.spreadSheetID,
                                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                            .executeMediaAndDownloadTo(outputStream);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public Boolean checkIfRecentlyModified() {
        try {
            System.out.println("Lastest saved modified time: " + this.savedModifiedTime);
            System.out.println("Lastest online  modified time: " + this.onlineModifiedTime);
            if (this.savedModifiedTime < this.onlineModifiedTime) {
                return true;
            } else
                return false;
        } catch (Exception e) {
            return null;
        }
    }
}
