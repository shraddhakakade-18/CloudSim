package org.cloudbus.cloudsim.examples;

import org.cloudbus.cloudsim.*;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.SimEvent;
import org.cloudbus.cloudsim.core.CloudSimTags;

import java.util.ArrayList;
import java.util.List;

public class FCFS_DatacenterBroker extends DatacenterBroker {

    public FCFS_DatacenterBroker(String name) throws Exception {
        super(name);
    }

    @Override
    protected void processCloudletReturn(SimEvent ev) {
        Cloudlet cloudlet = (Cloudlet) ev.getData();
        getCloudletReceivedList().add(cloudlet);
        Log.printLine(CloudSim.clock() + ": " + getName() + ": Cloudlet " + cloudlet.getCloudletId() + " received");

        // FCFS: Once a cloudlet returns, schedule the next one (or bind to VM)
        scheduleTaskstoVms();
    }

    public void scheduleTaskstoVms() {
        // Bind cloudlets to VMs in the order they arrive (First-Come, First-Served)
        List<Cloudlet> cloudletList = getCloudletList();
        List<Vm> vmList = getVmList();

        int vmIndex = 0;
        for (Cloudlet cloudlet : cloudletList) {
            bindCloudletToVm(cloudlet.getCloudletId(), vmList.get(vmIndex).getId());
            vmIndex = (vmIndex + 1) % vmList.size();  // Round robin or bind in order
            Log.printLine("Cloudlet " + cloudlet.getCloudletId() + " is bound to VM " + vmList.get(vmIndex).getId());
        }
    }
}
