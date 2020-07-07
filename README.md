# Function-as-a-service Simulator (FaasSim)

<p align="justify" markdown="1">FaasSim is an event-driven simulator for a FaaS platform. It simulates the platform with <math>N</math> number of functions. Since it is not suggested as a [best practice](https://www.ibm.com/cloud/learn/faas#toc-principles-JNV0JBYp), the simulator doesn't consider a function calling another function. The architecture of FaasSim is as follows: </p>

![](./images/architecture.png)

<p align="justify">FaasSim follows an open workload model. The requests arrive following a Poisson process at a rate <math>&lambda</math>. The functions are ordered in terms of their popularity. That is, the first function has the most and the *n*<sup>th</sup> function has the least probability of being called for service. To introduce this popularity among the functions, we have used the [Zipf parameter](https://en.wikipedia.org/wiki/Zipf%27s_law). Each function has its own service times, cold start times, idle times and memory consumption values. There are two different memory consumption values - one while the function is idle and other when it is in-execution.  A sample set of parameters is provided in the experiment folder.</p>

To run the simulator first compile the code:

    javac *.java

Next run the code as follows:

    java -Xms6144m -Xmx8192m FaasSim 64 0.5 0.6 cocoa 2 1 5

<p align="justify">We need to pass the VM arguments (Xms and Xmx) to allocate the required amount of memory. The values are high as by default FaasSim collects large number of samples (10<sup>7</sup>) and stores different traces corresponding to these samples in the memory to make the simulation faster.</p>

<p align="justify">The arguments for the main program are: *numberOfFunctions arrivalRate zipfShape expectedHitRate numberOfCpus iterationStartIndex iterationEndIndex*</p>
    
<p align="justify">FaasSim looks for the service times, cold start times, idle times and the memory consumption values in the folder corresponding to the arguments passed. For the above run command, it will look for the service times, cold start times and memory consumption values in the folder *experiment/params/64/0.5/0.6*. And for the idle times it will look at the folder *experiment/params/64/0.5/0.6/idleTimes/cocoa*.</p>

<p align="justify">The outputs are provided in the *experiment* folder. It creates four result folders inside the epxeriment folder. These are *cpu*, *logs*, *mem*, *response*. Inside each of those folders, a folder is created corresponding to the arguments passed like *64/0.5/0.6/cocoa*. Inside this folder, there can be multiple folders depending on the number iterations. </p>

<p align="justify">The *cpu* folder contains the CPU utilization value. The cold start probabilities of each function can be calculated from the files in *logs* folder. The memory consumption values for the simulation period are provided in the *mem* folder. FaasSim provides two types memory of value - first, the memory consumption considering the function idle and in-execution memory usage and second, the memory consumption if the platform follows a guaranteed memory reservation policy. Finally, the *response* folder contains the response time of each function.</p>

<p align="justify">We have provided 3 set of idle times in the experiment folder. The ones inside the *experiment/params/64/0.5/0.6/idleTimes/0.8* and *experiment/params/64/0.5/0.6/idleTimes/0.95* are estimated using an availability aware approach from TTL cache research. The one in *experiment/params/64/0.5/0.6/idleTimes/cocoa* is approximated by a novel method [COCOA](https://arxiv.org/pdf/2007.01222.pdf). For the considered arguments, these idle times from COCOA can ensure that the response time of the functions will remain below **2 seconds** with only **2 CPUs**.</p>
