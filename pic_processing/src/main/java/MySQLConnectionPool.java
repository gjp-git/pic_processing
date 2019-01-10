import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class MySQLConnectionPool implements ConnectionPool<Connection>{
    //最大连接数,可以写配置文件
    private int size = 10;
    //被使用的连接
    private ConcurrentHashMap<Connection,Object> busyConnectionPool = null;
    //空闲的连接
    private ArrayBlockingQueue<Connection> idleConnectionPool = null;
    //TODO
    private String conf_filename = "src\\main\\resources\\fdfs_client.conf";
    private Object obj = new Object();

    //单例
    private static volatile MySQLConnectionPool instance;

    public static MySQLConnectionPool getConnectionPool(){
        if (instance == null) {
            synchronized (MySQLConnectionPool.class) {
                if (instance == null) {
                    instance = new MySQLConnectionPool();
                }
            }
        }
        return instance;
    }

    //初始化
    private MySQLConnectionPool(){
        busyConnectionPool = new ConcurrentHashMap<Connection, Object>();
        idleConnectionPool = new ArrayBlockingQueue<Connection>(size);
        this.init(size);
    }

    //取出连接
    public Connection checkout(int waitTime){
        Connection conn = null;
        try {
            conn = idleConnectionPool.poll(waitTime, TimeUnit.SECONDS);
            if(conn != null){
                busyConnectionPool.put(conn, obj);
            }
        } catch (InterruptedException e) {
            conn = null;
            e.printStackTrace();
        }
        return conn;
    }

    //回收连接
    public void checkin(Connection conn){
        if(busyConnectionPool.remove(conn) != null){
            idleConnectionPool.add(conn);
        }
    }

    //如果连接无效则抛弃，新建连接来补充到池里
    public void drop(Connection conn){
        if(busyConnectionPool.remove(conn) != null){
            idleConnectionPool.add(this.getConnection());
            System.out.println("------------------------- :mysql connection +1");
        }
    }

    //关闭所有连接
    public void closeAll(){
        for(Connection conn:busyConnectionPool.keySet()){
            try {
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        for(Connection conn:idleConnectionPool){
            try {
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

    }


    //初始化连接池
    private void init(int size){
        Connection conn = null;
        for(int i=0; i<size; i++){
            conn = this.getConnection();
            idleConnectionPool.add(conn);
            System.out.println("------------------------- :mysql connection +1");
        }
    }

    //创建链接
    private Connection getConnection(){
        Connection conn = null;
        Properties properties = new Properties();
        try {
            BufferedReader bufferedReader = new BufferedReader(new FileReader("src/main/resources/mysql.conf"));
            properties.load(bufferedReader);
            Class.forName(properties.getProperty("jdbc.driver"));
            conn = DriverManager.getConnection(properties.getProperty("jdbc.url"),properties.getProperty("jdbc.username"),properties.getProperty("jdbc.password"));
        } catch (IOException | SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return conn;
    }
}
