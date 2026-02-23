import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import org.joda.time.DateTime;

import java.io.IOException;
import java.util.Date;
import java.util.List;

/**
 * Created by ДаниленкоСП on 03.06.2019.
 */
public class TestDelFilesFromGD {
    public static void main(String[] args) {
        Date dt = new Date();
        DateTime dateMax = new DateTime(dt);
        dateMax = dateMax.minusDays(29);

        ReadConfig readConfig = new ReadConfig("config\\config.ini");
        List<File> files = DriveQuickstart.getGoogleFiles(readConfig.getID_GD_FOLDER());
        System.out.println(files.size());
//        int count = 0;
        Drive driveService = GoogleDriveUtils.getDriveService();
        for (File file : files) {
            DateTime fileDate = new DateTime(String.valueOf(file.getCreatedTime()));
//            if (fileDate.isBefore(dateMax) || fileDate.equals(dateMax)) {
            System.out.println(file.getName());
            System.out.println(file.getId());
            System.out.println(file.getTrashed());
            try {
                driveService.files().delete(file.getId()).execute();
            } catch (IOException e) {
                e.printStackTrace();
            }
            System.out.println("--------------------------------------------------------------");
//                count++;
//            }
            break;
        }
//        System.out.println(count);
    }
}
