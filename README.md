# Function-as-a-service Simulator (FaasSim)

FaasSim is an event-driven simulator for a FaaS platform. It simulates the platform with ![formula](https://render.githubusercontent.com/render/math?math=N) number of functions. Since it is not suggested as a [best practice](https://www.ibm.com/cloud/learn/faas#toc-principles-JNV0JBYp), the simulator doesn't consider a function calling another function. The architecture of FaasSim is as follows:

![](./images/architecture.png)
For this demonstration, we have chosen an open workload model where requests arrives following a Poisson process with a moderate arrival rate of 0.5. To introduce popularity among the functions, we have used the Zipf parameter \cite{newman2005power} with a commonly used value of 1.0. 

FaasSim follows an open workload model. The requests arrive following a Poisson process at a rate ![formula](https://render.githubusercontent.com/render/math?math=\lambda). The functions are ordered in terms of their popularity. That is, the first function has the most and the *n*<sup>th</sup> function has the least probability of being called for service. To introduce this popularity among the functions, we have used the [Zipf parameter](https://en.wikipedia.org/wiki/Zipf%27s_law). Each function has its own service times, cold start times, idle times and memory consumption values. A sample set of parameters is provided in the experiment folder.

To run the simulator first compile the code:

    javac *.java

Next run the code as follows:

    java -Xms6144m -Xmx8192m FaasSim 64 0.5 0.6 cocoa 2 1 5

We need to pass the VM arguments to allocate the required amount of memory. The values are high as by default COCOA collects large number of samples (10<sup>7</sup>) and stores different traces corresponding to these samples in the memory to make the simulation faster.

The arguments for the simulator are: **numberOfFunctions arrivalRate zipfShape expectedHitRate numberOfCpus iterationStartIndex iterationEndIndex**
    
FaasSim looks for the service times, cold start times, idle times and the memory consumption values in the folder corresponding to the arguments passed. For the above run command, it will look for the service times, cold start times and memory consumption values in the folder *experiment/params/64/0.5/0/6*. And for the idle times it will look at the *experiment/params/64/0.5/0/6/idleTimes/cocoa* folder.

The outputs are provided in the experiment folder. It creates four result folders inside the epxeriment folder. These are *cpu*, *logs*, *mem*, *response*. Inside each of those folders, a folder is created corresponding to the arguments passed like *64/0.5/0/6/cocoa*. Inside this folder, there can be multiple folders depending on the number iterations. The *cpu* folder contains the CPU utilization value. The cold start probabilities of each function can be calculated from the *logs*. The memory consumption values are provided in *mem*. Finally, the *response* folder contains the response time of each function.
