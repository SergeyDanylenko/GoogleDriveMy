import javax.swing.*;
import java.io.*;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;

/**
 * Created by ДаниленкоСП on 27.07.2017.
 */

public class ReadConfig {
    private String PATH_FROM;
    private String ID_GD_FOLDER;
    private String AUTO_UPLOAD;

    private String[] FILE_EXT_ARRAY;

    private Properties props;
    public ReadConfig(String filepath) {
        props = new Properties();
        File file = new File(filepath);
        try {
//            props.load(new FileInputStream(file));
            InputStreamReader isr = new InputStreamReader(new FileInputStream(file), "windows-1251");
            props.load(isr);
            isr.close();
        } catch (IOException e) {
            if (Boolean.parseBoolean(props.getProperty("AUTO_UPLOAD"))) {
                GoogleDriveUtils.writeToLog(e.getMessage());
            } else {
                JOptionPane.showMessageDialog(null, e.getMessage(), "Google Drive Backups", JOptionPane.ERROR_MESSAGE);
            }
        }

        PATH_FROM = props.getProperty("PATH_FROM");

        ID_GD_FOLDER = props.getProperty("ID_GD_FOLDER");

        AUTO_UPLOAD = props.getProperty("AUTO_UPLOAD");

        String[] extArrays = props.getProperty("FILE_EXT_ARRAY").split(";", -1);
        FILE_EXT_ARRAY = new String[extArrays.length];
        System.arraycopy(extArrays, 0, FILE_EXT_ARRAY, 0, extArrays.length);
    }

    public String getPATH_FROM() {
        return PATH_FROM;
    }

    public String getID_GD_FOLDER() {
        return ID_GD_FOLDER;
    }

    public Boolean getAutoUpload() {
        return Boolean.valueOf(AUTO_UPLOAD);
    }

    public String[] getFileExtArray() {
        return FILE_EXT_ARRAY;
    }

    public void updateProperty(String nameProperty, String valueProperty){
        props.put(nameProperty, valueProperty);
        Set<String> propsSet = props.stringPropertyNames();
        Object[] namesProps = propsSet.toArray();
        for (Object namesProp : namesProps) {
            if (Objects.equals(namesProp.toString(), nameProperty)) {
                props.put(namesProp, props.getProperty(namesProp.toString()));
            }
        }

        try {
            FileOutputStream out = new FileOutputStream("config\\config.ini");
            Writer writer = new OutputStreamWriter(out, "windows-1251");
            props.store(writer, "/* property \"" + nameProperty + "\" updated*/");
        } catch (IOException e) {
            if (Boolean.parseBoolean(AUTO_UPLOAD)) {
                GoogleDriveUtils.writeToLog(e.getMessage());
            } else {
                JOptionPane.showMessageDialog(null, e.getMessage());
            }
        }
    }
}