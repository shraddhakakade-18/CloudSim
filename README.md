<b> CloudSim  </b> <br>
<b>Cloud Computing Lab </b> <br>
<b>Assingment 3 : Simulate a cloud scenario using CloudSim and run a scheduling algorithm that is not present in CloudSim.<br></b>
<b>Requirements: <br></b>
1. Java JDK and JRE <br>
2. CloudSim archives (CloudSim 3.0.3) <br>
3. Eclipse IDE <br>

<b>Procedure :</b><br>
A) Installation of CloudSim and creation of simulation environment<br>
1. Visit https://github.com/Cloudslab/cloudsim/releases to download the CloudSim archives for CloudSim<br>
3.0.3.

2. Extract the archive.<br>
3. The jars folder of the extracted archive should contain the following files:<br>
a)cloudsim-3.0.3.jar<br>
b)cloudsim-3.0.3-sources.jar<br>
c)cloudsim-examples-3.0.3.jar<br>
d)cloudsim-examples-3.0.3-sources.jar<br>
4. Create a new Java Project (cloudsim)using the Eclipse IDE.<br>
5. Right click on the project root and select the Build Path option from the dropdown<br>

6. Select the Configure Build Path section from the extended dropdown.<br>

7. Select the Libraries section and click on Add External JARs field on the pop up<br>

8. Navigate to the jars directory of the CloudSim archive and include the 4 jars in the project.<br>
9. Create a new package in the src directory of the project.<br>
10. Copy the code files for the constants, Data Center Creator, Data Center Broker, Matrix Generator and the SJF scheduler ,FCFS sheduler files from the this Repository or from
https://drive.google.com/drive/folders/1t_yJPCTVvFEc4v1Qe2q35ZEFzMuL2AJt?usp=sharing<br>
11. Make sure that the package name provided in each file is the same as the previously created package.<br>

B) Execution of code<br>
1. go to the eaxmples->org.cloudbus.cloudsim.examples ->FCFS_sheduler.java then click on run and run as java application<br>
2. go to the eaxmples->org.cloudbus.cloudsim.examples ->SJF_sheduler.java then click on run and run as java application<br>
3. go to the eaxmples->org.cloudbus.cloudsim.examples<br>
