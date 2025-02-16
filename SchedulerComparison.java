package org.cloudbus.cloudsim.examples;

import org.cloudbus.cloudsim.*;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.CloudSimTags;
import org.cloudbus.cloudsim.provisioners.*;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Calendar;

public class SchedulerComparison {

    private static List<Cloudlet> cloudletList;
    private static List<Vm> vmList;
    private static Datacenter[] datacenter;

    // Create VM method
    private static List<Vm> createVM(int userId, int vms) {
        LinkedList<Vm> list = new LinkedList<>();
        long size = 10000; // VM image size (MB)
        int ram = 512; // VM memory (MB)
        int mips = 250;
        long bw = 1000;
        int pesNumber = 1; // Number of CPUs
        String vmm = "Xen"; // Virtual Machine Monitor name

        for (int i = 0; i < vms; i++) {
            Vm vm = new Vm(i, userId, mips, pesNumber, ram, bw, size, vmm, new CloudletSchedulerSpaceShared());
            list.add(vm);
        }
        return list;
    }

    // Create Cloudlet method
    private static List<Cloudlet> createCloudlet(int userId, int cloudlets) {
        LinkedList<Cloudlet> list = new LinkedList<>();
        long fileSize = 300;
        long outputSize = 300;
        int pesNumber = 1;
        UtilizationModel utilizationModel = new UtilizationModelFull();

        for (int i = 0; i < cloudlets; i++) {
            long length = (long) (1e3 * (Math.random() * 10));
            Cloudlet cloudlet = new Cloudlet(i, length, pesNumber, fileSize, outputSize, utilizationModel, utilizationModel, utilizationModel);
            cloudlet.setUserId(userId);
            list.add(cloudlet);
        }
        return list;
    }

    // Create Datacenter method
    private static Datacenter createDatacenter(String name) {
        List<Host> hostList = new ArrayList<>();
        List<Pe> peList = new ArrayList<>();
        int mips = 1000;

        peList.add(new Pe(0, new PeProvisionerSimple(mips))); // One CPU core

        int hostId = 0;
        int ram = 2048; // Host memory (MB)
        long storage = 1000000; // Host storage (MB)
        int bw = 10000;

        hostList.add(new Host(hostId, new RamProvisionerSimple(ram), new BwProvisionerSimple(bw), storage, peList, new VmSchedulerTimeShared(peList)));

        String arch = "x86";
        String os = "Linux";
        String vmm = "Xen";
        double timeZone = 10.0; // Time zone
        double cost = 3.0; // Cost
        double costPerMem = 0.05; // Cost per memory
        double costPerStorage = 0.1; // Cost per storage
        double costPerBw = 0.1; // Cost per bandwidth
        LinkedList<Storage> storageList = new LinkedList<>(); // No SAN devices

        DatacenterCharacteristics characteristics = new DatacenterCharacteristics(arch, os, vmm, hostList, timeZone, cost, costPerMem, costPerStorage, costPerBw);

        Datacenter datacenter = null;
        try {
            datacenter = new Datacenter(name, characteristics, new VmAllocationPolicySimple(hostList), storageList, 0);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return datacenter;
    }

    // Create Broker method (this handles the scheduling algorithm)
    private static DatacenterBroker createBroker(String name) throws Exception {
        return new DatacenterBroker(name);
    }

    // Calculate total execution time from cloudlets
    private static double calculateTotalExecutionTime(List<Cloudlet> cloudlets) {
        double totalExecutionTime = 0.0;
        for (Cloudlet cloudlet : cloudlets) {
            if (cloudlet.getCloudletStatus() == Cloudlet.SUCCESS) {
                totalExecutionTime += cloudlet.getFinishTime() - cloudlet.getExecStartTime();
            }
        }
        return totalExecutionTime;
    }

    // Print Cloudlet results method
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

    // Main method
    public static void main(String[] args) {
        try {
            int num_user = 1;  // number of grid users
            Calendar calendar = Calendar.getInstance();
            boolean trace_flag = false; // mean trace events

            // Initialize the CloudSim simulation
            CloudSim.init(num_user, calendar, trace_flag);

            // Create Datacenters (we will create two datacenters for comparison)
            datacenter = new Datacenter[2]; // Two datacenters
            for (int i = 0; i < 2; i++) {
                datacenter[i] = createDatacenter("Datacenter_" + i);
            }

            // Create Brokers (for FCFS and SJF)
            DatacenterBroker brokerFCFS = createBroker("Broker_FCFS");
            int brokerIdFCFS = brokerFCFS.getId();
            DatacenterBroker brokerSJF = createBroker("Broker_SJF");
            int brokerIdSJF = brokerSJF.getId();

            // Create VMs and Cloudlets and submit them to the brokers
            vmList = createVM(brokerIdFCFS, 2); // 2 VMs for each broker
            cloudletList = createCloudlet(brokerIdFCFS, 5); // 5 Cloudlets for each broker
            brokerFCFS.submitVmList(vmList);
            brokerFCFS.submitCloudletList(cloudletList);

            // Start the simulation for FCFS
            CloudSim.startSimulation();

            // Print the results for FCFS
            List<Cloudlet> cloudletsReceivedFCFS = brokerFCFS.getCloudletReceivedList();
            CloudSim.stopSimulation();
            printCloudletList(cloudletsReceivedFCFS);
            Log.printLine("FCFS Finished");

            // Now, re-initialize the simulation for SJF (initialize everything again)
            CloudSim.init(num_user, calendar, trace_flag);

            // Create Datacenter again
            for (int i = 0; i < 2; i++) {
                datacenter[i] = createDatacenter("Datacenter_" + i);
            }

            // Create Brokers for SJF and submit VMs and Cloudlets
            brokerSJF = createBroker("Broker_SJF");
            brokerIdSJF = brokerSJF.getId();
            vmList = createVM(brokerIdSJF, 2); // 2 VMs for SJF
            cloudletList = createCloudlet(brokerIdSJF, 5); // 5 Cloudlets for SJF
            brokerSJF.submitVmList(vmList);
            brokerSJF.submitCloudletList(cloudletList);

            // Start the simulation for SJF
            CloudSim.startSimulation();

            // Print the results for SJF
            List<Cloudlet> cloudletsReceivedSJF = brokerSJF.getCloudletReceivedList();
            CloudSim.stopSimulation();
            printCloudletList(cloudletsReceivedSJF);
            Log.printLine("SJF Finished");

            // Calculate and Output total execution times for comparison
            double totalExecutionTimeFCFS = calculateTotalExecutionTime(cloudletsReceivedFCFS);
            double totalExecutionTimeSJF = calculateTotalExecutionTime(cloudletsReceivedSJF);

            Log.printLine("FCFS Total Execution Time: " + totalExecutionTimeFCFS);
            Log.printLine("SJF Total Execution Time: " + totalExecutionTimeSJF);

            // Compare SJF vs FCFS
            if (totalExecutionTimeSJF < totalExecutionTimeFCFS) {
                Log.printLine("SJF is the better approach with less execution time.");
            } else {
                Log.printLine("FCFS is the better approach with less execution time.");
            }

        } catch (Exception e) {
            e.printStackTrace();
            Log.printLine("The simulation has been terminated due to an unexpected error.");
        }
    }
}
