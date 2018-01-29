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
