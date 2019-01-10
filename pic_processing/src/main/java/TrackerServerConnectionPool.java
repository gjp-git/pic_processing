import org.csource.common.MyException;
import org.csource.fastdfs.ClientGlobal;
import org.csource.fastdfs.TrackerClient;
import org.csource.fastdfs.TrackerGroup;
import org.csource.fastdfs.TrackerServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.sql.SQLException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;


public class TrackerServerConnectionPool implements ConnectionPool<TrackerServer>{


    //最大连接数,可以写配置文件
    private int size = 10;
    //被使用的连接
    private ConcurrentHashMap<TrackerServer,Object> busyConnectionPool = null;
    //空闲的连接
    private ArrayBlockingQueue<TrackerServer> idleConnectionPool = null;

    private String conf_filename = "src\\main\\resources\\fdfs_client.conf";
    private Object obj = new Object();

    private static volatile TrackerServerConnectionPool instance;

    public static TrackerServerConnectionPool getConnectionPool(){
        if (instance == null) {
            synchronized (TrackerServerConnectionPool.class) {
                if (instance == null) {
                    instance = new TrackerServerConnectionPool();
                }
            }
        }
        return instance;
    }

    //初始化
    private TrackerServerConnectionPool(){
        busyConnectionPool = new ConcurrentHashMap<TrackerServer, Object>();
        idleConnectionPool = new ArrayBlockingQueue<TrackerServer>(size);
        init(size);
    }

    //取出连接
    public TrackerServer checkout(int waitTime){
        TrackerServer trackerServer = null;
        try {
            trackerServer = idleConnectionPool.poll(waitTime, TimeUnit.SECONDS);
            if(trackerServer != null){
                busyConnectionPool.put(trackerServer, obj);
            }
        } catch (InterruptedException e) {
            trackerServer = null;
            e.printStackTrace();
        }
        return trackerServer;
    }

    //回收连接
    public void checkin(TrackerServer trackerServer){
        if(busyConnectionPool.remove(trackerServer) != null){
            idleConnectionPool.add(trackerServer);
        }
    }

    //如果连接无效则抛弃，新建连接来补充到池里
    public void drop(TrackerServer trackerServer){
        if(busyConnectionPool.remove(trackerServer) != null){
            TrackerServer newTrackerServer = null;
            TrackerClient trackerClient = new TrackerClient();
            try {
                newTrackerServer = trackerClient.getConnection();
                idleConnectionPool.add(newTrackerServer);
                System.out.println("------------------------- :trackerServer connection +1");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    //关闭所有连接
    public void closeAll(){
        for(TrackerServer tc:busyConnectionPool.keySet()){
            try {
                tc.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        for(TrackerServer tc:idleConnectionPool){
            try {
                tc.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }


    //初始化连接池
    private void init(int size){
        initClientGlobal();
        TrackerServer trackerServer = null;
        try {
            TrackerClient trackerClient = new TrackerClient();
            for(int i=0; i<size; i++){
                trackerServer = trackerClient.getConnection();
                idleConnectionPool.add(trackerServer);
                System.out.println("------------------------- :trackerServer connection +1");
            }

        } catch (IOException e) {
            e.printStackTrace();
        }finally{
            if(trackerServer != null){
                try {
                    trackerServer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }



    //初始化客户端
    private void initClientGlobal(){
        try {
            ClientGlobal.init(conf_filename);
        } catch (IOException | MyException e) {
            e.printStackTrace();
            //连接超时时间
            ClientGlobal.setG_connect_timeout(1000);
            //网络超时时间
            ClientGlobal.setG_network_timeout(3000);
            ClientGlobal.setG_anti_steal_token(false);
            // 字符集
            ClientGlobal.setG_charset("UTF-8");
            ClientGlobal.setG_secret_key(null);
            // HTTP访问服务的端口号
            ClientGlobal.setG_tracker_http_port(80);

            InetSocketAddress[] trackerServers = new InetSocketAddress[1];
            trackerServers[0] = new InetSocketAddress("192.168.3.94",22122);
            TrackerGroup trackerGroup = new TrackerGroup(trackerServers);
            //tracker server 集群
            ClientGlobal.setG_tracker_group(trackerGroup);
        }
    }

}
