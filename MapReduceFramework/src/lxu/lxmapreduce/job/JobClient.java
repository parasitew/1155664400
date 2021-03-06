package lxu.lxmapreduce.job;

import lxu.lxmapreduce.configuration.JobConf;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

/**
 * JobClient.java
 * Created by Wei on 11/10/14.
 *
 * This class provides utilities to interact with
 * {@link lxu.lxmapreduce.job.JobTracker}. Current utilities include
 * locate JobTracker, update job status, and submit job.
 */
public class JobClient {
    private JobConf jobConf;
    private IJobTracker jobTracker;

    public JobClient() {
        this.jobConf = new JobConf();
        locateJobTracker();
    }

    public JobClient(JobConf jobConf) {
        this.jobConf = jobConf;
        locateJobTracker();
    }

    /**
     * locateJobTracker
     *
     * Locate JobTracker using java rmi.
     */
    public void locateJobTracker() {
        Registry registry = null;
        try {
            String masterAddr = jobConf.getSocketAddr("master.address", "localhost");
            int rmiPort = jobConf.getInt("rmi.port", 1099);
            registry = LocateRegistry.getRegistry(masterAddr, rmiPort);
            jobTracker = (IJobTracker)registry.lookup("JobTracker");
            if (jobTracker == null) {
                System.out.println("Cannot lookup JobTracker");
            }
        } catch (RemoteException | NotBoundException e) {
            e.printStackTrace();
            System.err.println("Error: JobClient getting JobTracker wrong!");
            System.err.println(e.getMessage());
            System.exit(-1);
        }
    }

    /**
     * updateStatus
     *
     * Given a jobID, connecting JobTracker and get the latest
     * status of that job.
     *
     * @param jobID
     * @return The latest job status.
     */
    public JobStatus updateStatus(String jobID) {
        if (jobTracker == null) {
            System.err.println("Error: JobClient cannot connect to JobTracker!");
            return null;
        }
        JobStatus newStatus = null;
        try {
            newStatus = jobTracker.getJobStatus(jobID);
        } catch (RemoteException e) {
            System.err.println("Error: JobClient fetching JobStatus wrong!");
            System.err.println(e.getMessage());
            return null;
        }
        return newStatus;
    }

    /**
     * submitJob
     *
     * First connecting to JobTracker to assign a new JobID for
     * new job. Then submit the {@link lxu.lxmapreduce.configuration.JobConf} of
     * the new job to JobTracker.
     *
     * @param job
     * @param jobConf
     * @return
     */
    public JobStatus submitJob(Job job, JobConf jobConf) {
        JobStatus jobStatus = null;
        try {
            String jobID = jobTracker.getNewJobID();
            job.setJobId(jobID);
            jobStatus = jobTracker.submitJob(jobID, jobConf);
        } catch (RemoteException e) {
            System.err.println("Error: JobClient submitting Job wrong!");
            e.printStackTrace();
            return null;
        }
        return jobStatus;
    }
}
