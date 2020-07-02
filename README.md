# Function-as-a-service Simulator (FaasSim)

FaasSim is an event-driven simulator for a FaaS platform. The architecture of FaasSim is as follows:

![](./images/architecture.png)

Here, the functions are ordered in terms of their popularity. That is, the first function has the most and the *n*<sup>th</sup> function has the least probability of being called for service. In FaasSim, the requests arrive at a rate ![formula](https://render.githubusercontent.com/render/math?math=\lambda) and each of the functions ![formula](https://render.githubusercontent.com/render/math?math=f_i) has a probability ![formula](https://render.githubusercontent.com/render/math?math=p_i) of being invoked for service. If a function is more popular than its ![formula](https://render.githubusercontent.com/render/math?math=p_i) will be higher. Each of the functions has its own service times, cold start times, idle times, memory consumption values. A sample set of parameters is provided in the experiment folder.

To run the simulator first compile the code:

    javac *.java

Next run the code as follows:

    java -Xms6144m -Xmx8192m FaasSim 64 0.5 0.6 cocoa 2 1 5

We need to pass the JVM parameters for the memroy. The values are high as by default COCOA collects large number of samples 10<sup>7</sup> and stores different traces corresponding to these samples in the memory to make the simulation faster.

The arguments for the application are: **numberOfFunctions arrivalRate zipfShape expectedHitRate numberOfCpus iterationStartIndex iterationEndIndex**
    
FaasSim looks for the service times, cold start times and idle times in the folder corresponding to the arguments passed. For example, for the above run command it will look for the service times, cold start times and memory consumption values in the experiment/params/64/0.5/0/6 folder. And for the idle times it will look at experiment/64/0.5/0/6/idleTimes/cocoa folder.
