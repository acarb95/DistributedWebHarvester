Warnings: 
	- Long running time for: biology, chemistry, physics, and biomedical sciences (explained below)
	- Currently the program does not run on those because when it tries to access those websites it gets an unexpected end of
file error when it connects. 
    - Page rank was not implemented :( 

I believe the long running time was due to the high amount of connections being made (not by me) while I was testing, I'm assuming those connections were made by others also testing their programs.

I have no idea why the end of file errors are occurring but they just started tonight (3/10) around 5:30 pm.

On to the instructions:

To make:
	make all - compiles the entire program
	make clean - removes all class files
	
To run: 
	java cs455.harvester.Crawler <portnum> <thread-pool-size> <root-url> <path-to-config-file>
	
Location of jar file: ./lib/jericho-html-3.3.jar

Locations of configurations for Thread pool manager:
	Thread pool size: cs455/harvester/threadpool/ThreadPoolManager.java on line 26 is where I create the thread pool of a certain size. The actual size of the thread pool is a program argument to the Crawler, therefore it is set in the crawler's constructor
	Sleep period: cs455/harvester/threadpool/CrawlerThread.java on line 57 is where I configure the niceness sleep period for all threads. 

Explanation of Scripts:
	clean-all.sh : sshs into all machines and removes the temp directorys I created. This ensures that there will be no "leftovers" from previous runs
	test-crawlers.sh : sshs into all my machines with my port number and starts the crawler with a hard coded thread number

Possible run times with 1 second sleep:
	It really depends on if biology, chem, bmb, and physics are accessible. If not, it runs in a matter of minutes. If they are, they themselves can take hours to run all the way through.

Hopefully when you test this it won't, a) run for 3+ hours (it's done this...), b) have end of file connection error, or c) have connection timeout errors.

In the case of possibility (a), grab a good book or another class's homework assignment and settle in. :)

Thanks!
