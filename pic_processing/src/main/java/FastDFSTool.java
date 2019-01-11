import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.csource.common.NameValuePair;
import org.csource.fastdfs.*;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class FastDFSTool {

    //配置文件位置
    private String conf_filename = "src\\main\\resources\\fdfs_client.conf";

    private TrackerServerConnectionPool pool = TrackerServerConnectionPool.getConnectionPool();

    /**
     * 上传文件到FastDFS
     *
     * @param filename
     *            文件名
     * @return 上传成功后，存放在fastdfs中的文件位置及文件名
     * @throws FileNotFoundException
     * @throws IOException
     * @throws Exception
     */
    public String uploadFile(String filename){
        // 获得classpath下文件的绝对路径
        String upload_file1 =null;
        byte[] bs = getBytes(filename);
        // 获得文件名的扩展名
        String extension = FilenameUtils.getExtension(filename);
        // 从连接池取得连接获得跟踪服务器端
        TrackerServer connection = pool.checkout(20);
        //存储客户端
        StorageClient1 storageClient = new StorageClient1(connection, null);

        try {
            // 通过存储客户端开始上传文件，并返回存放在fastdfs中的文件位置及文件名
            upload_file1 = storageClient.upload_file1(bs, extension, null);
            pool.checkin(connection);
        } catch (Exception e) {
            //如果出现了IO异常应该销毁此连接
            pool.drop(connection);
            e.printStackTrace();
            upload_file1 = this.uploadFile(filename);
        }
        return upload_file1;
    }
    //批量
    public List<String>  uploadFile(String image_path,List<String> image_list){
        // 获得classpath下文件的绝对路径
        String upload_file =null;
        List<String> upload_path_list = new ArrayList<>() ;
        //从连接池取得连接获得跟踪服务器端
        TrackerServer connection = pool.checkout(20);
        //存储客户端
        StorageClient1 storageClient = new StorageClient1(connection, null);
        try {
            for(String filename:image_list){
                byte[] bs = getBytes(image_path+filename);
                // 获得文件名的扩展名
                String extension = FilenameUtils.getExtension(filename);
                // 通过存储客户端开始上传文件，并返回存放在fastdfs中的文件位置及文件名
                upload_file = storageClient.upload_file1(bs, extension, null);
                upload_path_list.add(filename.substring(0,filename.length()-4)+","+upload_file);
                //System.out.println("fastDFSTool.deleteFile(\"" + upload_file + "\");");
            }
            pool.checkin(connection);
        } catch (Exception e) {
            //如果出现了IO异常应该销毁此连接
            pool.drop(connection);
            e.printStackTrace();
        }
        return upload_path_list;
    }

    public void downloadFile(String group,String filename) {
        try {
            //初始化客户端(通过文件系统下的配置文件)
            ClientGlobal.init(conf_filename);

            TrackerClient tracker = new TrackerClient();
            TrackerServer trackerServer = tracker.getConnection();
            StorageServer storageServer = null;

            StorageClient storageClient = new StorageClient(trackerServer, storageServer);
            byte[] b = storageClient.download_file(group, filename);
            System.out.println(b);
            IOUtils.write(b, new FileOutputStream("D://fdfs//"+filename));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public FileInfo getFileInfo(String group,String filename){
        FileInfo fi=null;
        try {
            ClientGlobal.init(conf_filename);

            TrackerClient tracker = new TrackerClient();
            TrackerServer trackerServer = tracker.getConnection();
            StorageServer storageServer = null;

            StorageClient storageClient = new StorageClient(trackerServer, storageServer);

            //fi = storageClient.get_file_info("group2", "M00/02/02/wKgD0lv_wd2AUPBTAACbvyL6yBk615.png");
            fi = storageClient.get_file_info(group, filename);
            System.out.println(fi.getSourceIpAddr());
            System.out.println(fi.getFileSize());
            System.out.println(fi.getCreateTimestamp());
            System.out.println(fi.getCrc32());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return fi;
    }


    public NameValuePair[] getFileMate(String group,String filename){
        NameValuePair[] nvps  =null;
        try {
            ClientGlobal.init(conf_filename);

            TrackerClient tracker = new TrackerClient();
            TrackerServer trackerServer = tracker.getConnection();
            StorageServer storageServer = null;

            StorageClient storageClient = new StorageClient(trackerServer,
                    storageServer);
            //nvps = storageClient.get_metadata("group1", "M00/00/00/wKgRcFV_08OAK_KCAAAA5fm_sy874.conf");
            nvps = storageClient.get_metadata(group, filename);
                for(NameValuePair nvp : nvps){
                System.out.println(nvp.getName() + ":" + nvp.getValue());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return nvps ;
    }

    public int deleteFile(String filename){
        int index = filename.indexOf("/");
        return deleteFile(filename.substring(0,index),filename.substring(index+1));
    }

    public int deleteFile(String group,String filename){
        Integer i=null;
        //存储客户端
        //从连接池取得连接获得跟踪服务器端
        TrackerServer connection = pool.checkout(20);
        //存储客户端
        StorageClient1 storageClient = new StorageClient1(connection, null);
        try {
            //删除成功返回0
            //i = storageClient.delete_file("group1", "M00/00/00/wKgRcFV_08OAK_KCAAAA5fm_sy874.conf");
            i = storageClient.delete_file(group, filename);
            System.out.println( i==0 ? "删除成功" : "删除失败:"+i);
            pool.checkin(connection);
        } catch (Exception e) {
            pool.drop(connection);
            e.printStackTrace();
        }
        return i;
    }

    private byte[] getBytes(String filePath){
        byte[] buffer = null;
        try {
            File file = new File(filePath);
            FileInputStream fis = new FileInputStream(file);
            ByteArrayOutputStream bos = new ByteArrayOutputStream(1000);
            byte[] b = new byte[1000];
            int n;
            while ((n = fis.read(b)) != -1) {
                bos.write(b, 0, n);
            }
            fis.close();
            bos.close();
            buffer = bos.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return buffer;
    }
}
