# Function-as-a-service Simulator (FaasSim)

FaasSim is an event-driven simulator for a FaaS platform. The architecture of FaasSim is as follows:

![](./images/architecture.png)

Here, the functions are ordered in terms of their popularity. That is, the first function has the most and the *n*<sup>th</sup> function has the least probability of being called for service. In FaasSim, the requests arrive at a rate ![formula](https://render.githubusercontent.com/render/math?math=\lambda) and each of the functions *f<sub>i</sub>* has a probability *p<sub>i</sub>* of being invoked for service. If a function is more popular than its *p<sub>i</sub>* will be higher. Each of the functions has its own service times, cold start times and idle times. A sample set of parameters is provided in the experiment folder.

To run the simulator first compile the code:

    javac *.java

Next run the code as follows:

    java -Xms6144m -Xmx8192m FaasSim 64 0.5 0.6 cocoa 2 1 5
