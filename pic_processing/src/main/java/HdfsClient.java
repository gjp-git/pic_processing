import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;


public class HdfsClient {

    FileSystem fs = null;

    /**
     * 初始化FileSystem
     */
    public void init() throws Exception {
        // 构造一个配置参数对象，设置一个参数：我们要访问的hdfs的URI
        // 从而FileSystem.get()方法就知道应该是去构造一个访问hdfs文件系统的客户端，以及hdfs的访问地址
        // new Configuration();的时候，它就会去加载jar包中的hdfs-default.xml
        // 然后再加载classpath下的hdfs-site.xml
        Configuration conf = new Configuration();
        //conf.set("fs.defaultFS", "hdfs://192.168.3.94:9000");
        /**
         * 参数优先级： 1、客户端代码中设置的值 2、classpath下的用户自定义配置文件 3、然后是服务器的默认配置
         */
        //conf.set("dfs.replication", "3");

        // 获取一个hdfs的访问客户端，根据参数，这个实例应该是DistributedFileSystem的实例
        //fs = FileSystem.get(conf);

        // 如果这样去获取，那conf里面就可以不要配"fs.defaultFS"参数，而且，这个客户端的身份标识已经是hadoop用户
        fs = FileSystem.get(new URI("hdfs://192.168.3.94:9000"), conf, "eshadoop");
    }

    /**
     * 往hdfs上传文件
     */
    public void addFileToHdfs(String dstPath,String srcPath) throws Exception {
        // 要上传的文件所在的本地路径
        Path src = new Path(srcPath);
        // 要上传到hdfs的目标路径
        Path dst = new Path(new StringBuilder().append("hdfs://192.168.3.94:9000").append(dstPath).toString());
        fs.copyFromLocalFile(src, dst);


    }

    public void closeHdfs() throws IOException {
        fs.close();
    }

    /**
     * 从hdfs中复制文件到本地文件系统
     */
    public void downloadFileToLocal() throws IllegalArgumentException, IOException {
        fs.copyToLocalFile(new Path("/jdk-7u65-linux-i586.tar.gz"), new Path("d:/"));

    }

    /**
     * 从hdfs中复制文件到本地文件系统
     */
    public void mkdir(String dir) throws IllegalArgumentException, IOException {
        fs.mkdirs(new Path("hdfs://192.168.3.94:9000"+dir));
    }

    /**
     * 在hfds中创建目录、删除目录、重命名
     */
    public void mkdirAndDeleteAndRename() throws IllegalArgumentException, IOException {
        // 创建目录
        fs.mkdirs(new Path("/a1/b1/c1"));

        // 删除文件夹 ，如果是非空文件夹，参数2必须给值true
        fs.delete(new Path("/aaa"), true);

        // 重命名文件或文件夹
        fs.rename(new Path("/a1"), new Path("/a2"));
    }

    public List<String> getAllFiles(String rootPath, List<String> fileList){
        File baseFile = new File(rootPath);
        if (baseFile.isFile() || !baseFile.exists()) {
            return fileList;
        }
        File[] files = baseFile.listFiles();
        if ( files == null ) {
            return fileList;
        }
        for ( File file : files ) {
            if ( file.isDirectory() ) {
                this.getAllFiles( file.getAbsolutePath(), fileList);
            } else {
                fileList.add( file.getAbsolutePath() );
            }
        }

        return fileList;



    }

    public static void main(String[] args) {
        HdfsClient hc = new HdfsClient();
        String localPath = "D:\\cicv\\test data\\";
        List<String> fileList = new LinkedList<String>();
        File localDir = new File(localPath);
        hc.getAllFiles(localPath,fileList);
        System.out.println("files number = [" + fileList.size() + "]");
        for(String s: fileList){
            System.out.println("file = [" + s.replace(localPath,"") + "]");
            try {
                hc.addFileToHdfs(s.replace(localPath,""),s);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

}
