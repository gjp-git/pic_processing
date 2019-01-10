import java.util.concurrent.ConcurrentLinkedQueue;

public class ImageQueue {
    private ConcurrentLinkedQueue<String> imageRecorder = null;

    public ImageQueue(){
        this.imageRecorder = new ConcurrentLinkedQueue<>();
    }

    public void produce(String path){
        synchronized (this.imageRecorder){
            this.imageRecorder.offer(path);
            this.imageRecorder.notifyAll();
        }
    }


    public String consume(){
        synchronized (this.imageRecorder){
            if (this.imageRecorder.size() == 0){ //产品仓库空了，等待生产者生产
                try {
                    this.imageRecorder.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
           return this.imageRecorder.poll();
        }
    }
}
