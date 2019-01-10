import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.HiddenFileFilter;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.monitor.FileAlterationListenerAdaptor;
import org.apache.commons.io.monitor.FileAlterationMonitor;
import org.apache.commons.io.monitor.FileAlterationObserver;

import java.io.File;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * 文件变化监听器
 *
 * 在Apache的Commons-IO中有关于文件的监控功能的代码. 文件监控的原理如下：
 * 由文件监控类FileAlterationMonitor中的线程不停的扫描文件观察器FileAlterationObserver，
 * 如果有文件的变化，则根据相关的文件比较器，判断文件时新增，还是删除，还是更改。（默认为1000毫秒执行一次扫描）
 *
 *
 */
public class FileListener extends FileAlterationListenerAdaptor {
    //private Logger log = Logger.getLogger(FileListener.class.toString());
    private BlockingQueue imageRecorder = null;

    public FileListener(BlockingQueue queue){
        this.imageRecorder = queue;
    }

    /**
     * 文件创建执行
     */
    public void onFileCreate(File file) {
        //log.info("[新建]:" + file.getAbsolutePath());
        this.imageRecorder.offer(file.getAbsolutePath());
    }

    /**
     * 文件创建修改
     */
    /*public void onFileChange(File file) {
        log.info("[修改]:" + file.getAbsolutePath());
    }*/

    /**
     * 文件删除
     */
    /*public void onFileDelete(File file) {
        log.info("[删除]:" + file.getAbsolutePath());
    }*/

    /**
     * 目录创建
     */
    /*public void onDirectoryCreate(File directory) {
        log.info("[新建]:" + directory.getAbsolutePath());
    }*/

    /**
     * 目录修改
     */
    /*public void onDirectoryChange(File directory) {
        log.info("[修改]:" + directory.getAbsolutePath());
    }*/

    /**
     * 目录删除
     */
    /*public void onDirectoryDelete(File directory) {
        log.info("[删除]:" + directory.getAbsolutePath());
    }*/

    public void onStart(FileAlterationObserver observer) {
        super.onStart(observer);
    }

    public void onStop(FileAlterationObserver observer) {
        super.onStop(observer);
    }

    public static void main(String[] args) throws Exception{
        // 监控目录
        String rootDir = "D:\\cicv\\test data";
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
        monitor.start();
    }

}
