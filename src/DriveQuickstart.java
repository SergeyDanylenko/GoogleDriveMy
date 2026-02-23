/**
 * Created by ДаниленкоСП on 25.04.2019.
 */
import com.google.api.client.http.AbstractInputStreamContent;
import com.google.api.client.http.FileContent;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;

import javax.swing.*;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class DriveQuickstart {
    private static ReadConfig config = new ReadConfig("config\\config.ini");

    private static File _createGoogleFile(String googleFolderIdParent, String contentType, //
                                          String customFileName, AbstractInputStreamContent uploadStreamContent) {
        File fileMetadata = new File();
        fileMetadata.setName(customFileName);
        List<String> parents = Arrays.asList(googleFolderIdParent);
        fileMetadata.setParents(parents);
        Drive driveService;
        File file = null;
        try {
            driveService = GoogleDriveUtils.getDriveService();
//            file = driveService.files().create(fileMetadata, uploadStreamContent)
//                    .setFields("id, webContentLink, webViewLink, parents").execute();
            List<File> googleDriveFiles = getGoogleFiles(config.getID_GD_FOLDER());
            File findedFile = null;
            for (File googleDriveFile : googleDriveFiles) {
                if (googleDriveFile.getName().equals(customFileName)) {
                    findedFile = googleDriveFile;
                    break;
                }
            }
//            if (hasFile) {
//                file = driveService.files().update(findedFile.getId(), fileMetadata, uploadStreamContent)
//                        .setFileId("id, webContentLink, webViewLink, parents").execute();
//            } else {
            if (findedFile != null) {
//                System.out.println(findedFile.getName());
//                System.out.println(findedFile.getId());
//                try {
//                    driveService.files().delete(findedFile.getId()).execute();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
                File fileMetadata1 = new File();
                fileMetadata1.setName(customFileName);
                file = driveService.files().update(findedFile.getId(), fileMetadata1, uploadStreamContent)
                        .setFields("id, webContentLink, webViewLink, parents").execute();
            }else {
                file = driveService.files().create(fileMetadata, uploadStreamContent)
                    .setFields("id, webContentLink, webViewLink, parents").execute();
            }

        } catch (IOException e) {
            if (config.getAutoUpload()) {
                GoogleDriveUtils.writeToLog(e.getMessage());
            } else {
                JOptionPane.showMessageDialog(null, e.getMessage(), "Google Drive Backups", JOptionPane.ERROR_MESSAGE);
            }
//            e.printStackTrace();
        }
        return file;
    }

    public static File createGoogleFile(String googleFolderIdParent, String contentType, String customFileName, java.io.File uploadFile) {
        AbstractInputStreamContent uploadStreamContent = new FileContent(contentType, uploadFile);
        return _createGoogleFile(googleFolderIdParent, contentType, customFileName, uploadStreamContent);
    }

    public static void uploadFiles() {
        ArrayList<java.io.File> files = getFiles();
        for (int i = 0; i < files.size(); i++) {
            java.io.File uploadFile = files.get(i);//new java.io.File("1Cv8_PVRZ_26042019.rar");
            String filename = uploadFile.getName();
            File googleFile = createGoogleFile(config.getID_GD_FOLDER(), "application/x-rar-compressed", filename, uploadFile);
            System.out.println("Created Google file!");
            System.out.println("WebContentLink: " + googleFile.getWebContentLink() );
            System.out.println("WebViewLink: " + googleFile.getWebViewLink() );
            boolean delFile = uploadFile.delete();
            if (delFile) {
                System.out.println("file: " + filename + " deleted successfully");
            } else {
                System.out.println("Can`t delete file: " + filename);
            }
        }

        System.out.println("Done!");
    }

    public static ArrayList<java.io.File> getFiles() {
        ArrayList<java.io.File> files = new ArrayList<>();
        Path path = Paths.get(config.getPATH_FROM());

        java.io.File dir = path.toFile();
        if (dir.listFiles().length > 0) {
            for (int i = 0; i < dir.listFiles().length; i++) {
                files.add(dir.listFiles()[i]);
            }
        }
        return files;
    }

    public static List<File> getAllGoogleFolders() {
        Drive driveService = GoogleDriveUtils.getDriveService();

        String pageToken = null;
        List<File> list = new ArrayList<>();

        String query = " mimeType = 'application/vnd.google-apps.folder'";
        do {
            FileList result = null;
            try {
                result = driveService.files().list()
                        .setQ(query)
                        .setSpaces("drive")
                        .setFields("nextPageToken, files(id, name, createdTime, parents)")
                        .setPageToken(pageToken)
                        .execute();
            } catch (IOException e) {
                if (config.getAutoUpload()) {
                    GoogleDriveUtils.writeToLog(e.getMessage());
                } else {
                    JOptionPane.showMessageDialog(null, e.getMessage(), "Google Drive Backups", JOptionPane.ERROR_MESSAGE);
                }
//                e.printStackTrace();
            }
            list.addAll(result.getFiles().stream().collect(Collectors.toList()));
            pageToken = result.getNextPageToken();
        } while (pageToken != null);

        return list;
    }

    public static List<File> getGoogleFiles(String parent_id) {
        Drive driveService = GoogleDriveUtils.getDriveService();

        String pageToken = null;
        List<File> list = new ArrayList<>();

        String query = "'" + parent_id + "' in parents";
        do {
            FileList result = null;
            try {
                result = driveService.files().list()
                        .setQ(query)
                        .setSpaces("drive")
                        .setFields("nextPageToken, files(id, name, createdTime, parents)")
                        .setPageToken(pageToken)
                        .execute();
            } catch (IOException e) {
                if (config.getAutoUpload()) {
                    GoogleDriveUtils.writeToLog(e.getMessage());
                } else {
                    JOptionPane.showMessageDialog(null, e.getMessage(), "Google Drive Backups", JOptionPane.ERROR_MESSAGE);
                }
//                e.printStackTrace();
            }
            list.addAll(result.getFiles().stream().collect(Collectors.toList()));
            pageToken = result.getNextPageToken();
        } while (pageToken != null);

        return list;
    }
}