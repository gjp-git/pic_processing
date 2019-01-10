import java.io.*;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.regex.Pattern;

public class Test {
    public static void main(String[] args) {
        /*Properties properties = new Properties();
        // 使用ClassLoader加载properties配置文件生成对应的输入流

        // 使用properties对象加载输入流
        try {
            BufferedReader bufferedReader = new BufferedReader(new FileReader("src/main/resources/mysql.conf"));
            properties.load(bufferedReader);
        } catch (IOException e) {
            e.printStackTrace();
        }
        //获取key对应的value值
        String key = properties.getProperty("jdbc.password");
        System.out.println("args = [" + key + "]");*/

       /* Connection conn = MySQLConnectionPool.getConnectionPool().checkout(3);
        try {
            Statement stmt = conn.createStatement();
        } catch (SQLException e) {
            e.printStackTrace();
        }*/
        String localPath = "D:\\cicv\\test data\\";

        File localDir = new File(localPath);
        String[] fileList = localDir.list();
        System.out.println("args = [" + localPath + "]");
        System.out.println("args = [" + System.getProperty("file.separator") + "]");
        System.out.println("args = [" + localPath.split(System.getProperty("file.separator").replace("\\","\\\\"))[0] + "]");

    }
}

