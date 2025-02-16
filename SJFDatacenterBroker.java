package org.cloudbus.cloudsim.examples;

import org.cloudbus.cloudsim.*;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.CloudSimTags;
import org.cloudbus.cloudsim.core.SimEvent;

import java.util.ArrayList;
import java.util.List;

public class SJFDatacenterBroker extends DatacenterBroker {

    // Declare cloudletList as a field
    private List<Cloudlet> cloudletList;

    // Constructor
    SJFDatacenterBroker(String name) throws Exception {
        super(name);
        cloudletList = new ArrayList<>(); // Initialize cloudletList
    }

    // Method to schedule tasks to VMs
    public void scheduleTaskstoVms() {
        int reqTasks = cloudletList.size();
        int reqVms = getVmList().size();  // Use getVmList() from DatacenterBroker

        for (int i = 0; i < reqTasks; i++) {
            // Bind cloudlets to VMs (this should ideally be done based on some scheduling strategy)
            bindCloudletToVm(i, i % reqVms); 
            System.out.println("Task " + cloudletList.get(i).getCloudletId() + " is bound with VM " + getVmList().get(i % reqVms).getId());
        }

        ArrayList<Cloudlet> list = new ArrayList<>();
        for (Cloudlet cloudlet : getCloudletReceivedList()) {
            list.add(cloudlet);
        }

        Cloudlet[] list2 = list.toArray(new Cloudlet[list.size()]);

        Cloudlet temp = null;
        int n = list.size();

        // Sorting cloudlets by their length (Shortest Job First scheduling)
        for (int i = 0; i < n; i++) {
            for (int j = 1; j < (n - i); j++) {
                if (list2[j - 1].getCloudletLength() / (getVmList().get(0).getMips() * getVmList().get(0).getNumberOfPes()) >
                        list2[j].getCloudletLength() / (getVmList().get(0).getMips() * getVmList().get(0).getNumberOfPes())) {
                    temp = list2[j - 1];
                    list2[j - 1] = list2[j];
                    list2[j] = temp;
                }
            }
        }

        ArrayList<Cloudlet> list3 = new ArrayList<>();
        for (Cloudlet cloudlet : list2) {
            list3.add(cloudlet);
        }

        setCloudletReceivedList(list3); // Set the sorted cloudlet list
    }

    // Print the cloudlet details
    public void printNumbers(ArrayList<Cloudlet> list) {
        for (Cloudlet cloudlet : list) {
            System.out.print(" " + cloudlet.getCloudletId());
        }
        System.out.println();
    }

    @Override
    protected void processCloudletReturn(SimEvent ev) {
        Cloudlet cloudlet = (Cloudlet) ev.getData();
        getCloudletReceivedList().add(cloudlet);
        Log.printLine(CloudSim.clock() + ": " + getName() + ": Cloudlet " + cloudlet.getCloudletId() + " received");
        cloudletsSubmitted--;

        if (getCloudletList().size() == 0 && cloudletsSubmitted == 0) {
            scheduleTaskstoVms();  // Schedule tasks once they are all received
            cloudletExecution(cloudlet);  // Execute the cloudlet
        }
    }

    protected void cloudletExecution(Cloudlet cloudlet) {
        if (getCloudletList().size() == 0 && cloudletsSubmitted == 0) { // All cloudlets executed
            Log.printLine(CloudSim.clock() + ": " + getName() + ": All Cloudlets executed. Finishing...");
            clearDatacenters();
            finishExecution();
        } else { // Some cloudlets are still pending
            if (getCloudletList().size() > 0 && cloudletsSubmitted == 0) {
                clearDatacenters();
                createVmsInDatacenter(0);
            }
        }
    }

    @Override
    protected void processResourceCharacteristics(SimEvent ev) {
        DatacenterCharacteristics characteristics = (DatacenterCharacteristics) ev.getData();
        getDatacenterCharacteristicsList().put(characteristics.getId(), characteristics);

        if (getDatacenterCharacteristicsList().size() == getDatacenterIdsList().size()) {
            distributeRequestsForNewVmsAcrossDatacenters();
        }
    }

    protected void distributeRequestsForNewVmsAcrossDatacenters() {
        int numberOfVmsAllocated = 0;
        int i = 0;

        final List<Integer> availableDatacenters = getDatacenterIdsList();

        for (Vm vm : getVmList()) {
            int datacenterId = availableDatacenters.get(i++ % availableDatacenters.size());
            String datacenterName = CloudSim.getEntityName(datacenterId);

            if (!getVmsToDatacentersMap().containsKey(vm.getId())) {
                Log.printLine(CloudSim.clock() + ": " + getName() + ": Trying to Create VM #" + vm.getId() + " in " + datacenterName);
                sendNow(datacenterId, CloudSimTags.VM_CREATE_ACK, vm);
                numberOfVmsAllocated++;
            }
        }

        setVmsRequested(numberOfVmsAllocated);
        setVmsAcks(0);
    }
}
