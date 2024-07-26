package api.test.services;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Collections;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.sheets.v4.Sheets;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.auth.oauth2.ServiceAccountCredentials;

public class GoogleServices {
    private final String CREDENTIALS_FILE_PATH = "service_account_credentials.json";
    private final String PRIVATE_RESOURCE_PATH = "src/test/resources/private/";
    private final String APPLICATION_NAME = "Google Sheets API Java";
    private final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private NetHttpTransport HTTP_TRANSPORT;
    private HttpRequestInitializer requestInitializer;
    // private Sheets sheetService;
    // private Drive driveService;

    public GoogleServices() {
        try {
            GoogleCredentials savedCredentials = getCredentials();
            this.HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
            this.requestInitializer = new HttpCredentialsAdapter(savedCredentials);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    private GoogleCredentials getCredentials() throws IOException {
        java.io.File file = new File(PRIVATE_RESOURCE_PATH + CREDENTIALS_FILE_PATH);
        FileInputStream serviceAccountStream = new FileInputStream(file.getAbsolutePath());
        return ServiceAccountCredentials.fromStream(serviceAccountStream)
                .createScoped(Collections.singletonList(DriveScopes.DRIVE));
    }

    public Drive buildDriveService() {
        return new Drive.Builder(this.HTTP_TRANSPORT, this.JSON_FACTORY, this.requestInitializer)
                .setApplicationName(this.APPLICATION_NAME)
                .build();
    }

    public Sheets buildSheetService() {
        return new Sheets.Builder(this.HTTP_TRANSPORT, this.JSON_FACTORY, this.requestInitializer)
                .setApplicationName(this.APPLICATION_NAME)
                .build();
    }
}
