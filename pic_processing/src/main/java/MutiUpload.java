import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.HiddenFileFilter;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.monitor.FileAlterationMonitor;
import org.apache.commons.io.monitor.FileAlterationObserver;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.*;

public class MutiUpload {
    private static Vector<String> resultList = new Vector<String>();
    //private static String tracker_ip = "192.168.4.189/";
    private static String tracker_ip = "";

    private <T> List<List<T>> splitAry(T[] ary, int count) {
        int subSize = ary.length % count == 0 ? ary.length / count: ary.length / count + 1;

        List<List<T>> subAryList = new ArrayList<List<T>>();

        for (int i = 0; i < count; i++) {
            int index = i * subSize;
            List<T> list = new ArrayList<T>();
            int j = 0;
            while (j < subSize && index < ary.length) {
                list.add(ary[index++]);
                j++;
            }
            subAryList.add(list);
        }

        return subAryList;
    }
     private void uploadFile(List<String> imageList, FastDFSTool fastDFSTool, String imagePath, Vector<String> resultList){

         /*for(String image: imageList){
             String fdfsFilePath = fastDFSTool.uploadFile(new StringBuffer().append(imagePath).append(image).toString());
             String timestamp = image.substring(0,image.length()-4);
             resultList.add(new StringBuffer(timestamp).append(",").append(fdfsFilePath).toString());
             //System.out.println("fastDFSTool.deleteFile(\"" + new StringBuffer(fdfsFilePath) + "\");");
             //System.out.println("timestamp = [" + timestamp + "]");
         }*/
         List<String> lineList = fastDFSTool.uploadFile(imagePath,imageList);
         resultList.addAll(lineList);

     }

    public static void main(String[] args) {
        String rootDir = "D:\\cicv\\AAAdata";
        // 轮询间隔 1 秒
        long interval = TimeUnit.SECONDS.toMillis(1);
        // 创建过滤器
        IOFileFilter directories = FileFilterUtils.and(
                FileFilterUtils.directoryFileFilter(),
                HiddenFileFilter.VISIBLE);
        IOFileFilter files       = FileFilterUtils.and(
                FileFilterUtils.fileFileFilter(),
                FileFilterUtils.suffixFileFilter(".webp"));
        IOFileFilter filter = FileFilterUtils.or(directories, files);
        // 使用过滤器
        FileAlterationObserver observer = new FileAlterationObserver(new File(rootDir), filter);
        //不使用过滤器
        //FileAlterationObserver observer = new FileAlterationObserver(new File(rootDir));
        BlockingQueue<String> bq = new LinkedBlockingQueue<>();
        observer.addListener(new FileListener(bq));
        //创建文件变化监听器
        FileAlterationMonitor monitor = new FileAlterationMonitor(interval, observer);
        // 开始监控
        try {
            monitor.start();
        } catch (Exception e) {
            System.out.println("文件监控启动失败！");
            e.printStackTrace();
            System.exit(0);
        }

        final MutiUpload mutiUpload = new MutiUpload();
        final FastDFSTool fastDFSTool = new FastDFSTool();
        MySQLConnectionPool sqlConnectionPool = MySQLConnectionPool.getConnectionPool();


        int threadCount = 10;

        ExecutorService fixedThreadPool = Executors.newFixedThreadPool(threadCount);
        //final CountDownLatch latch = new CountDownLatch(threadCount);
        for (int i = 0; i < threadCount; i++) {
            final int index = i;

            fixedThreadPool.execute(() -> {
                Connection sqlConnection = sqlConnectionPool.checkout(20);
                PreparedStatement ps = null;
                try {
                    ps = sqlConnection.prepareStatement("insert into path(city,timeStamp,type,storePath) values(?,?,?,?)");
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                while (true) {
                    String filePath = null;
                    String storePath = null;
                    String city = null;
                    String timeStamp = null;
                    String type = null;

                    try {
                        filePath = bq.take();
                        //System.out.println("args = [" + filePath + "]");
                        String[] strs = filePath.split(System.getProperty("file.separator").replace("\\","\\\\"));
                        city = strs[strs.length-3].split("_")[0];
                        type = strs[strs.length-2];
                        timeStamp = strs[strs.length-1].split("\\.")[0];
                        //System.out.println("th" + index + " = [" + filePath + "]");
                        storePath = fastDFSTool.uploadFile(filePath);
                        ps.setString(1,city);
                        ps.setString(2,timeStamp);
                        ps.setString(3,type);
                        ps.setString(4,storePath);
                        ps.execute();
                        //System.out.println("th" + index + "," + filePath + ","+storePath+ ","+city+ ","+type+ ","+timeStamp);
                    } catch (InterruptedException | SQLException e) {
                        e.printStackTrace();
                        System.out.println("th" + index + "," + filePath + ","+storePath+ ","+city+ ","+type+ ","+timeStamp);
                    }
                }
            });
        }






        /*final MutiUpload mutiUpload = new MutiUpload();
        final FastDFSTool fastDFSTool = new FastDFSTool();

        int threadCount = 5;
        //String localPath = "D:\\cicv\\BBB new data\\";
        String localPath = "D:\\cicv\\test data\\";

        File localDir = new File(localPath);
        String[] fileList = localDir.list();


        StringBuffer resultPath = new StringBuffer(localPath);
        File resultFile;
        FileWriter resultWritter = null;
        StringBuffer imagePath = new StringBuffer(localPath);
        File imageDir;
        String[] imageList;
        List<List<String>> subImageList = null;


        assert fileList != null;
        //for(String file: fileList1){
        for(String file: fileList){

            System.out.println("dir = [" + file + "]");
            resultFile =new File(resultPath.append(file).append("\\imageList.csv").toString());
            try {
                if(!resultFile.exists()){
                    resultFile.createNewFile();
                }
                resultWritter = new FileWriter(resultFile);
                imagePath.append(file);
                imagePath.append("\\image\\");
                imageDir = new File(imagePath.toString());
                imageList = imageDir.list();

                assert imageList != null;
                subImageList = mutiUpload.splitAry(imageList,threadCount);
                ExecutorService fixedThreadPool = Executors.newFixedThreadPool(threadCount);
                final CountDownLatch latch = new CountDownLatch(threadCount);
                for (int i = 0; i < threadCount; i++) {
                    final int index = i;
                    final List<String> pathList = subImageList.get(index);
                    final String imgPath = imagePath.toString();
                    fixedThreadPool.execute(() -> {
                        mutiUpload.uploadFile(pathList,fastDFSTool,imgPath,resultList);
                        latch.countDown();
                    });
                }
                latch.await();
                Collections.sort(resultList);
                //System.out.println("resultList = [" + resultList.size() + "]");
                for(String line:resultList){
                    //System.out.println("line = [" + line + "]");
                    resultWritter.write(line+"\n");
                }
                resultWritter.flush();
                resultWritter.close();
            }catch (IOException | InterruptedException e){
                e.printStackTrace();
            }
            resultList.removeAllElements();
            resultPath = new StringBuffer(localPath);
            imagePath = new StringBuffer(localPath);

        }*/
        //System.exit(0);

    }
}

