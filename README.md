# wikigamewinner

## set up before first use

When first using this application, keep in mind you should initialize the wikipedia cache. 
This file should contain a large amount of the link structure of wikipedia.
You can auto-generate this file by running

````java
new DepthFirstWikipediaMiner().start("United_States", 2);
````

This will crawl Wikipedia, starting at the article about the United States, up to a depth of 2 articles.
Of course, if you want the bot to be more knowledgeable, it is advised to crawl other areas of interest as well.

- World_War_II
- Biology
- Cell
- IBM

I have provided an example file in the repository.
The file should be placed at

````java
System.getProperty("user.home")
````

## usage

Once properly set up, you can start the application. It might take some time to read the wikipedia cache.
When the cache is read, you should see a prompt appear, asking you to fill in the start and goal page.

The algorithm will then canonize your filled in words. e.g. 'Oprah winfrey' will be turned into 'Oprah_Winfrey'.
Then, one of 3 things will happen:

- The start or goal (or both) are unknown. The algorithm will need to download more Wikipedia.
- The start is known, but no outgoing links for the start page are known. The algorithm will need to download more Wikipedia.
- Both start and goal are known, the algorithm will attempt to calculate a path between both.

## example session

````
start > Michael Jackson
goal > Dolphin
FROM 'Michael_Jackson' TO 'Dolphin'
0	Michael_Jackson
1	Midwestern_United_States
2	Caribbean_Sea
3	Dolphin
````

## performance

I tested the performance by measuring executing of AdvancedDijkstraPathFinder for 1000 randomly selected pairs of Wikipedia articles. The output can be summarized as follows:

````
load time    : 3032 ms
success rate : 0.9765625
max          : 3891 ms
min          : 3 ms
avg          : 285 ms
````

I ran the similar tests counting the number of clicks every path took. For counting clicks, I included the start and goal page. So the above example session demonstrates a path of 4 clicks.

````
load time    : 2634 ms
success rate : 0.977
max          : 8 clicks
min          : 2 clicks
avg          : 4.0 clicks
````

## decimation

In digital signal processing, decimation is the process of reducing the sampling rate of a signal. The term downsampling usually refers to one step of the process, but sometimes the terms are used interchangeably. Complementary to upsampling, which increases sampling rate, decimation is a specific case of sample rate conversion in a multi-rate digital signal processing system.
When decimation is performed on a sequence of samples of a signal or other continuous function, it produces an approximation of the sequence that would have been obtained by sampling the signal at a lower rate (or density, as in the case of a photograph). 

I implemented a version of `IWikipediaPathFinder` that purposefully removes nodes from its graph with probability in correspondance to `1.0 - eigenvalue`. By doing so the graph becomes easier to traverse, but performance suffers.

````
del      success rate
0.1	    0.905
0.2	    0.855
0.3	    0.775
0.4	    0.675
0.5	    0.675
0.6	    0.585
0.7	    0.51
0.75	    0.53
0.8	    0.47
0.85	    0.43
````

Average click count (as well as minimum and maximum click count) did not vary.
