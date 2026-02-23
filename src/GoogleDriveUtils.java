/**
 * Created by ДаниленкоСП on 25.04.2019.
 */

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;

import javax.swing.*;
import java.io.*;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class GoogleDriveUtils {
    private static final String APPLICATION_NAME = "Google Drive Backups";
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
//    private static final File CREDENTIALS_FOLDER = new File("config");
    private static String url = MainForm.class.getResource("resources").getPath().substring(1);
    private static final File CREDENTIALS_FOLDER = new File(String.valueOf(url));
    private static final String CLIENT_SECRET_FILE_NAME = "credentials.json";
    private static final List<String> SCOPES = Collections.singletonList(DriveScopes.DRIVE);
    private static FileDataStoreFactory DATA_STORE_FACTORY;
    private static HttpTransport HTTP_TRANSPORT;
    private static Drive _driveService;
    public static boolean auto_upload = new ReadConfig("config\\config.ini").getAutoUpload();

    static {
        try {
            HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
            Runtime.getRuntime().exec("cacls config /E /G BUILTIN\\Пользователи:W");
            DATA_STORE_FACTORY = new FileDataStoreFactory(new File("config")/*CREDENTIALS_FOLDER*/);
        } catch (Throwable t) {
            if (auto_upload) {
                writeToLog(t.getMessage());
            } else {
                JOptionPane.showMessageDialog(null, t.getMessage(), "Google Drive Backups", JOptionPane.ERROR_MESSAGE);
            }
            t.printStackTrace();
//            System.exit(1);
        }
    }

    public static boolean getAuto_upload() {
        return auto_upload;
    }

    public static Credential getCredentials() {
//        File clientSecretFilePath = new File(CREDENTIALS_FOLDER, CLIENT_SECRET_FILE_NAME);
        File clientSecretFilePath = storeCredentialFile();
        if (!clientSecretFilePath.exists()) {
            try {
                throw new FileNotFoundException("Please copy " + CLIENT_SECRET_FILE_NAME
                        + " to folder: " + CREDENTIALS_FOLDER.getAbsolutePath());
            } catch (FileNotFoundException e) {
                if (auto_upload) {
                    writeToLog(e.getMessage());
                } else {
                    JOptionPane.showMessageDialog(null, e.getMessage(), "Google Drive Backups", JOptionPane.ERROR_MESSAGE);
                }
                e.printStackTrace();
            }
        }

        InputStream in;
        Credential credential = null;
        try {
            in = new FileInputStream(clientSecretFilePath);
            GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));
            GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(HTTP_TRANSPORT, JSON_FACTORY,
                    clientSecrets, SCOPES).setDataStoreFactory(DATA_STORE_FACTORY).setAccessType("offline").build();
            credential = new AuthorizationCodeInstalledApp(flow, new LocalServerReceiver()).authorize("user");
        } catch (IOException e) {
            if (auto_upload) {
                writeToLog(e.getMessage());
            } else {
                JOptionPane.showMessageDialog(null, e.getMessage(), "Google Drive Backups", JOptionPane.ERROR_MESSAGE);
            }
//            e.printStackTrace();
        }
        return credential;
    }

    public static Drive getDriveService() {
        if (_driveService != null) {
            return _driveService;
        }
        Credential credential = getCredentials();
        _driveService = new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential) //
                .setApplicationName(APPLICATION_NAME).build();
        return _driveService;
    }

    public static File storeCredentialFile() {
        File temp = null;
        try {
            File temp1 = File.createTempFile("credentials", ".tmp");
            String pathToTempFile = temp1.getParent();
            temp = new File(pathToTempFile + "\\credentials.json");
//            System.out.println("Временный файл: " + temp.getAbsolutePath());
            FileWriter fw = new FileWriter(temp);

            try (BufferedWriter bw = new BufferedWriter(fw)) {
                try (BufferedReader txtReader = new BufferedReader(new InputStreamReader(MainForm.class.getResourceAsStream("/resources/credentials.json")))) {
                    int c;
                    while ((c = txtReader.read()) != -1) {
                        bw.write((char) c);
                    }
                }
            }
            temp1.delete();
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(null, ex.getMessage(), "Google Drive Backups", JOptionPane.ERROR_MESSAGE);
//            ex.printStackTrace();
        }
        return temp;
    }

    public static void writeToLog(String message) {
        try {
            FileWriter writer = new FileWriter("log.txt", true);
            writer.append(new SimpleDateFormat("yyyy.MM.dd_HH:mm:ss").format(Date.from(Instant.now()))).append("\t").append(message).append("\n");
            writer.flush();
        } catch (IOException e1) {
            if (auto_upload) {
                writeToLog(e1.getMessage());
            } else {
                JOptionPane.showMessageDialog(null, "Can't write to log!", "Google Drive Backups", JOptionPane.ERROR_MESSAGE);
            }
//                                    e1.printStackTrace();
        }
    }
}