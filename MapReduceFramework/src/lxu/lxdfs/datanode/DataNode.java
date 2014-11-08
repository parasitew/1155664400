package mydfs.namenode;

import lxu.lxdfs.datanode.BlockService;
import lxu.lxdfs.service.INameSystemService;

import java.io.IOException;
import java.net.ServerSocket;

public class DataNode implements Runnable {

    private int port = 12345;
    private boolean isRunning = true;
    private Thread dataNodeThread = null;
    private Thread blockServiceThread = null;
    private ServerSocket dataNodeServerSocket = null;
    private INameSystemService nameNode = null;

    public DataNode() throws IOException {
        dataNodeServerSocket = new ServerSocket(port);
        dataNodeThread = new Thread(this);
        dataNodeThread.start();
        register();
    }

    private void register() throws IOException {

    }

    /**
     * Main loop for the DataNode.  Runs until shutdown,
     * forever calling remote NameNode functions.
     *
     * @throws Exception
     */
    public void offerService() throws Exception {
        while (isRunning) {
            /* TODO: heartbeat */
            /* TODO: check newly received block. if true, send block report */
        }
    }

    @Override
    public void run() {
        blockServiceThread = new Thread(new BlockService(dataNodeServerSocket));
        blockServiceThread.start();
        while (isRunning) {
            try {
                offerService();
            } catch (Exception e) {
                e.printStackTrace();
                if (isRunning) {
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException e1) {}
                }
            }
        }
    }

    void join() {
        if (dataNodeThread != null) {
            try {
                dataNodeThread.join();
            } catch (InterruptedException e) {}
        }
    }

    public static void main(String[] args) {
        try {
            DataNode dataNode = new DataNode();
            dataNode.join();
        } catch (Exception e) {
            System.err.println("Datanode error: ");
            e.printStackTrace();
            System.exit(-1);
        }
    }
}
