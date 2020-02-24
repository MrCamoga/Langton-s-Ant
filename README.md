# Langton's Ant 
Multicolor extension of Langton's Ant cellular automaton program that finds highways and its period

## TODO
- [ ] Make it faster
- [ ] Create server to submit ants tested by users (in PHP or Java)
	-Send rules to clients to test so that no rule is tested multiple times
	-Verify rules by different clients
	-Store rules tested and verified by each user for credit
	-Prevent people from sending false data (trust system based on rule verification by other users, maybe user auth with twitter)
- [ ] Improve GUI
- [x] Save huge raster of highway to estimate its period. This could be use to determine if computing the period is feasible (i.e. if the estimated period is 1e17 it'd take 50 years!!)
- [ ] Autosave ant state

## How does it work?
The program will run through all the rules and try to automatically detect highways

After the ant reaches maximum iterations or highway has been found, the program will skip to another rule and will output to the command line all the rules that form highways along with its period.

**Example output:**

```
  134987	3096
  134991	160
  134995	292
  135001	?
  135003	18
  135011	264
  135012	18
  135015	210
  135018	180
  135027	1692
  135031	52
  135034	132
```

That **?** means that the program had started finding the highway but the ant reached max iterations before finding the highway.

All rules tested are saved to a binary file if **Settings.saverule** is set to true.

In IORules.java I explain how the rules are stored in the binary file.
	
## Usage
In LangtonsMain.java you have to put the starting rule and the function that generates the next rule to be tested.

### Some examples:
#### Test all rules
```java
	new Simulation(1, r->r+1);
	
```
	
#### Test rules that start with LRRLLR (test every 2^6 rules, I talk more about this in [Huge Highways](#how-i-found-these-huge-highways))
```java
	new Simulation(0b100110, r -> r+64); //Rules are written backwards (LRRLLR -> 011001 -> 100110)
```

#### Test random rules
```java
	new Simulation(new Random().nextLong(), r -> new Random().nextLong());
```

#### Test list of rules
```java
	static int i = 0;
	long[] rules = new long[]{1,2,27,3873};
	new Simulation(rules[0], r -> rules[++i]);
```
	
## Settings
#### Find highways:
*	**detectHighways**: program will try to detect highways if this is true
*	**saverule**: if true rules will be saved to file
*	**ignoreSavedRules**: ignores rules that have already been tested
*	**file**: file where tested rules will be saved
*	**chunkCheck**: when the ant exits that chunk, the program will start finding the highway
*	**repeatcheck**: # of times the period has to repeat before the highway period is confirmed
*	**maxiterations**: skip to next rule if limit reached

The program will use save the ant stats to a file to find the highways:
*	**fileChunkSize**: minimum file size. (Default 2GB)
*	**maxNumOfChunks**: every time the file is filled its size will increase by 2GB up to "maxNumOfChunks" times

#### Save images:
*	**savepic**: if true rules with highways will be saved to disk in the folder /"period"/"rule".png (e.g. the regular Langton's Ant would be saved to /104/2.png)

#### When finding period of huge highways:
*	**deleteOldChunks**: deletes chunks that haven't been visited by the ant in the last 100M steps  to free up memory


## IORules.java

At the start of this class I explain how the rules are stored in the binary file.

This class implements some methods to read the rules from the file
#### cleanRulesFile()
Removes repeated rules, and sorts them by rule id
#### getInfo()
Prints info about the rules tested (# of rules tested, how many highways there are, biggest highways found,...)
#### saveRulesToTxt()
That's self explanatory
#### searchSavedRules(boolean highways)
Returns long[] with all rules that have been tested. If highways == true, only rules that form highways will be returned

## My findings
I've tested all rules up to 17 letters (131072) to 100 million iterations each, 16551 of them formed highways

In total I've tested 159980 rules and found 30507 highways.

1921 different periods in total

#### TOP highways with longest period

|Rule Number|Period|Rule String|
|:-:|-:|:-|  
|15416631       |      	 117440512200 	| RRRLRRLLRLRRRRLLRRLRLRRR    |                                    
|13025588       |      	 66487151028 	| LLRLRRLLRLLLLLRRLRRLLLRR    |                                    
|22593844       |      	 28299602536 	| LLRLRRLLRLLLLLRRLLLRRLRLR   |                                    
|13730100       |      	 13498292016 	| LLRLRRLLRLLLLLLRRLLLRLRR    |                                    
|3539252        |      	 6740764524 	| LLRLRRLLRLLLLLLLLRRLRR      |                                    
|786123         |      	 5307264488 	| RRLRLLRRLRRRRRRRRRLR        |                                    
|1605323        |      	 1078710528 	| RRLRLLRRLRRRRRRLLLLRR       |                                    
|130123         |      	 320374420  	| RRLRLLRLLLRRRRRRR           |                                    
|2596555        |      	 281077180  	| RRLRLLRRLRRRRLLRRRRLLR      |                                    
|39795787       |      	 223586484  	| RRLRLLRLLLRRRRLLRRRRRLRLLR  |                                    
|34028619       |      	 143634980  	| RRLRLLRLLLRRRRLLRRRLLLLLLR  |                                    
|12483659       |      	 136647312  	| RRLRLLRLLLRRRRRLLRRRRRLR    |                                    
|26542388       |      	 79800524   	| LLRLRRLLRLLLLLLLRLRLRLLRR   |                                    
|1875083        |      	 53908956   	| RRLRLLLRLLRRRLLRLLRRR       |                                    
|32144459       |      	 41875566   	| RRLRLLRLLLRRRRRLLRLRLRRRR   |                                    
|5193419        |      	 41320192   	| RRLRLLRRLRRRRRLLRRRRLLR     |                                    
|41614411       |      	 39359140   	| RRLRLLRLLLRRRRRRLRLRRRRLLR  |                                    
|31819          |      	 34911892   	| RRLRLLRLLLRRRRR             |                                    
|7666763        |      	 33868240   	| RRLRLLRLLLRRRRRRLLRLRRR     |                                    
|13418804       |      	 33372780   	| LLRLRRLLRLLLLLRRLLRRLLRR    |                                    

#### Biggest highways with unknown period
| Tested to # iters	|	Rule String					|	Rule number		|	Size		|	Estimated period|
|:-:|:-|:-:|:-:|:-:|
|	1e13			|	RRRLRRLLRLRRRRLLRRRLLRLR	|	10960183		|	-			|					|
|	2.19e12			|	RRRLRRLLRLRRRRRLLRLRRLRR	|	14318903		|	-			|					|
|	1.74e12			|	RRRLRRLLRLRRRRRRRRRLR		|	1572151			|	-			|					|
|	1.4e10			|	RRLRLLRRLRRRRRRRRRLRLLLLLR	|	34340555   		|	-			|					|
|  	0				|	RRLRLLRRLRRRRRRRRRLLLLLLRR	|	50593483		|	-			|					|
|	2.01e12	31%		|	RRLRLLRRLRRRRRRRRRLLLLRLRR	|	54787787		|	777109320	|	6507894229248	|
|	0				|	RRLRLLRRLRRRRRRRRRLRRLRLRR	|	56360651		|	-			|					|
|	1.4e12			|	LLRLRRLLRLLLLLLLLRRRLLRLR	|	21889332		|	>= 1573560	|					|
|	7.6e11   		|	LLRLRRLLRLLLLLLLLLLRLRLR 	| 	11010356		| 	>= 146880	|					|
| 	586782472424  	| 	LLRLRRLLRLLLLLLRRLLLR 		|	1147188			|   >= 1422032400|	>=11745987624000|


#### How I found these huge highways
Whenever I found a big highway such as <span style="color:red">**RRLRLLRLLLRRRR**</span>RR (31819) or <span style="color:blue">**RRLRLLRRLRRRRRR**</span> (32459), I tested all rules that started in the same way because they usually have similar behaviour.

Since I read the rules backwards (e.g. RRRLRR -> 110111 = 55) I can test rules that start with the same **n** letters really easily:

```java
	new Simulation(31819, r -> r+(1<<n));
```
This code will test the following rules (with **n=14** will test every **16384th** rule)

|Rule|Binary| String |
|:-:|-:|:-|
|31819 |  1**11110001001011**  | **RRLRLLRLLLRRRR**R |
|48203 | 10**11110001001011**  | **RRLRLLRLLLRRRR**LR|
|64587 | 11**11110001001011**  | **RRLRLLRLLLRRRR**RR|
|80971 | 100**11110001001011** | **RRLRLLRLLLRRRR**LLR|
|...|...|

#### Some functions that generate lots of highways

|functions| longest highway | longest highway period | % form highways | info |
|:-:|:-:|:-:|:-:|:-:|
|16384n+16075		| **RRLRLLRRLRRRRR**RRRRLR| 5307264488	| 28.1% | biggest highway found |
|16384n+15435<br>16384n+948		|**RRLRLLRLLLRRRR**RRR|	320374420 | 10.7% | periods of around 1m |
|8192n+8106		|	**LRLRLRLRRRRRR**RLRRRRRLRLRR | 907904  | 94.9% | periods vary from 3k to 27k |
|16384n+12892	| **LLRRRLRLLRLLRR**RLLRLLLRLRRLRLRLL RLLRLRLLLRRLLRRRRRLRRRRRLRLRLRR| 721784 | 44.6%	|	got the rule from [vmainen](https://www.reddit.com/r/cellular_automata/comments/9mfthz/langtons_ant_exhibiting_a_distinct_highwaypattern/). Periods around 5k |
|32768n+28757		|	**RLRLRLRLLLLLRRR**RLLLR | 300078 |24.1% | 20k - 300k |
||||||
|1024n+455<br>1024n+568<br>2048n+1884||488|100%<br>100%<br>99.9%| period 38 |
|1024n+467<br>1024n+556||224|99.9%<br>100%| period 42|
|2048n+428<br>2048n+1619<br>4096n+1100||344|99.9%<br>100%<br>100%| period 46 |
|512n+469<br>512n+554||442|100%| period 88|
|1024n+1365<br>1024n+1706||894|100%|period 104|
|2048n+1194<br>2048n+2901||618|100%|period 108|
|8192n+1220||5028|100%| period 216|
|4096n+3643||472|99.1%|period 244|

#### Some rules that form highways

|Rules|Period|
|:-:|:-:|
|2 | 104|
|4 | 18 |
|2<sup>n</sup>  for n≥3 | 16n+4 |
|R**L...L**R**L...L**...R (n L's in each block): __2<sup>n</sup>*(2<sup>n+1</sup>*(2k-1)+1)__ for n>1,  k>0 | same period as 2<sup>n</sup>|
|L**R...R**L**R...R**...R (n R's in each block): __2<sup>n</sup>*(2<sup>n+1</sup>*(2k+1)-1)-1__ for n>1,  k>0 | same period as 2<sup>n</sup>|

