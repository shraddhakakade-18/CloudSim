package org.cloudbus.cloudsim.examples;

import org.cloudbus.cloudsim.*;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.CloudSimTags;
import org.cloudbus.cloudsim.core.SimEvent;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Calendar;

public class FCFS_Scheduler {

    private static List<Cloudlet> cloudletList;
    private static List<Vm> vmList;
    private static Datacenter[] datacenter;

    private static List<Vm> createVM(int userId, int vms) {
        LinkedList<Vm> list = new LinkedList<Vm>();
        long size = 10000; //image size (MB)
        int ram = 512; //vm memory (MB)
        int mips = 250;
        long bw = 1000;
        int pesNumber = 1; //number of cpus
        String vmm = "Xen"; //VMM name

        Vm[] vm = new Vm[vms];
        for (int i = 0; i < vms; i++) {
            vm[i] = new Vm(i, userId, mips, pesNumber, ram, bw, size, vmm, new CloudletSchedulerSpaceShared());
            list.add(vm[i]);
        }
        return list;
    }

    private static List<Cloudlet> createCloudlet(int userId, int cloudlets) {
        LinkedList<Cloudlet> list = new LinkedList<Cloudlet>();
        long fileSize = 300;
        long outputSize = 300;
        int pesNumber = 1;
        UtilizationModel utilizationModel = new UtilizationModelFull();

        Cloudlet[] cloudlet = new Cloudlet[cloudlets];
        for (int i = 0; i < cloudlets; i++) {
            long length = (long) (1e3 * (Math.random() * 10));
            cloudlet[i] = new Cloudlet(i, length, pesNumber, fileSize, outputSize, utilizationModel, utilizationModel, utilizationModel);
            cloudlet[i].setUserId(userId);
            list.add(cloudlet[i]);
        }
        return list;
    }

    public static void main(String[] args) {
        try {
            int num_user = 1;  // number of grid users
            Calendar calendar = Calendar.getInstance();
            boolean trace_flag = false; // mean trace events

            // Initialize the CloudSim simulation
            CloudSim.init(num_user, calendar, trace_flag);

            // Create Datacenters using the DatacenterCreator class
            datacenter = new Datacenter[2];  // For simplicity, let's use 2 datacenters.
            for (int i = 0; i < 2; i++) {
                datacenter[i] = DatacenterCreator.createDatacenter("Datacenter_" + i);
            }

            // Create Broker
            FCFS_DatacenterBroker broker = createBroker("Broker_0");
            int brokerId = broker.getId();

            // Create VMs and Cloudlets and submit them to broker
            vmList = createVM(brokerId, 2);  // 2 VMs
            cloudletList = createCloudlet(brokerId, 5);  // 5 cloudlets
            broker.submitVmList(vmList);
            broker.submitCloudletList(cloudletList);

            // Start the simulation
            CloudSim.startSimulation();

            // Print the results
            List<Cloudlet> newList = broker.getCloudletReceivedList();
            CloudSim.stopSimulation();

            printCloudletList(newList);

            Log.printLine("FCFS_Scheduler finished!");

        } catch (Exception e) {
            e.printStackTrace();
            Log.printLine("The simulation has been terminated due to an unexpected error.");
        }
    }

    private static FCFS_DatacenterBroker createBroker(String name) throws Exception {
        return new FCFS_DatacenterBroker(name);
    }

    private static void printCloudletList(List<Cloudlet> list) {
        int size = list.size();
        Cloudlet cloudlet;
        String indent = "    ";

        Log.printLine();
        Log.printLine("========== OUTPUT ==========");
        Log.printLine("Cloudlet ID" + indent + "STATUS" + indent + "VM ID" + indent + "Time" + indent + "Start Time" + indent + "Finish Time");

        DecimalFormat dft = new DecimalFormat("###.##");
        for (int i = 0; i < size; i++) {
            cloudlet = list.get(i);
            Log.print(indent + cloudlet.getCloudletId() + indent);
            if (cloudlet.getCloudletStatus() == Cloudlet.SUCCESS) {
                Log.print("SUCCESS");
                Log.printLine(indent + cloudlet.getVmId() +
                        indent + cloudlet.getActualCPUTime() +
                        indent + cloudlet.getExecStartTime() +
                        indent + cloudlet.getFinishTime());
            }
        }
    }
}
