# Langton's Ant 
Multicolor extension of Langton's Ant cellular automaton program that finds highways

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

### Some examples (below them written using lambda expressions):
#### Test all rules
```java
	window.rule = 2847;
	window.nextrule = new IRule() {
        public long nextRule(long current) {
            return current+1;
        }	
	};
	
    
	window.nextrule = r -> r+1;
	
```
	
#### Test rules that start with LRRLLR (test every 2^6 rules, I talk more about this in [Huge Highways](#how-i-found-these-huge-highways))
```java
	window.rule = 0b100110; //Rules are written backwards (LRRLLR -> 011001 -> 100110)
	window.nextrule = new IRule() {
		public long nextRule(long current) {
			return current+64;
		}	
	};
	
   window.rule = 0b100110;
	window.nextrule = r -> r+64;
```

#### Test random rules
```java
	window.rule = new Random().nextLong();
	window.nextrule = new IRule() {
		public long nextRule(long current) {
			return new Random().nextLong();
		}
	};
	
	window.nextrule = r -> new Random().nextLong();
```

#### Test list of rules
```java
	static int i = 0;
	long[] rules = new long[]{1,2,27,3873};
	window.rule = r[0];
	window.nextrule = new IRule() {
		public long nextRule(long current) {
			i++;
			//Add some code to catch ArrayIndexOutOfBoundException, close program, continue with random rules, etc.
			if(i > r.length) return current+1;
			return r[i];   
		}
	}
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

#### TOP 10 highways with longest period
*I've colored the rules depending on how they start*
|Period|Rule String|
|:-:|:-:|                                                 
|5307264488 |    <span style="color:blue">**RRLRLLRRLRRRRRR**</span>RRRLR    	|                                         
|1078710528 |    <span style="color:blue">**RRLRLLRRLRRRRRR**</span>LLLLRR		|                                      
|320374420  |	 <span style="color:red">**RRLRLLRLLLRRRR**</span>RRR			|                                       
|220391833  |	 <span style="color:red">**RRLRLLRLLLRRRR**</span>RLLLLRRRR     |                                       
|34911892   |	 <span style="color:red">**RRLRLLRLLLRRRR**</span>R            |                                       
|21561810   |	 <span style="color:red">**RRLRLLRLLLRRRR**</span>RLRLRRRR      |                                      
|20899462   |	 <span style="color:red">**RRLRLLRLLLRRRR**</span>LLRRRLLRR     |                                   
|10749868   |	 <span style="color:blue">**RRLRLLRRLRRRRRR**</span>RLRLRR      |                                  
|9275184    |	 <span style="color:blue">**RRLRLLRRLRRRRRR**</span>RRLLRLLLLR  |                                   
|8483164    |	 <span style="color:red">**RRLRLLRLLLRRRR**</span>RRRRLRRLR     |

#### How I found these huge highways
Whenever I found a big highway such as <span style="color:red">**RRLRLLRLLLRRRR**</span>RR (31819) or <span style="color:blue">**RRLRLLRRLRRRRRR**</span> (32459), I tested all rules that started in the same way because they usually have similar behaviour.

Since I read the rules backwards (e.g. RRRLRR -> 110111 = 55) I can test rules that start with the same **n** letters really easily:
```java
	window.rule = 31819;
	window.nextrule = new IRule() {
    	public long nextRule(int current) {
        	return current + (1<<n);
        }
    };
```
This code will test the following rules (with **n=14** will test every **16384th** rule)
|Rule|Binary|
|:-:|:-:|
|31819 |  111110001001011  |
|48203 | 1011110001001011  |
|64587 | 1111110001001011  |
|80971 | 10011110001001011 |
|...|...|

#### Some functions that generate lots of highways

|functions| longest highway | longest highway period | rules found | info |
|:-:|:-:|:-:|:-:|:-:|
|16384n + 16075		| **RRLRLLRRLRRRRR**RRRRLR| 5307264488	| 284| biggest highway found |
|16384n + 15435		|**RRLRLLRLLLRRRR**RRR|	320374420 | 66 | periods of around 1m |
|8192n + 8106		|	**LRLRLRLRRRRRR**RLRRRRRLRLRR | 907904  | 4317 | periods vary from 3k to 27k |
|16384n + 12892	| **LLRRRLRLLRLLRR**RLLRLLLRLRRLRLRLLRLLRLRLLLRRLLRRRRRLRRRRRLRLRLRR| 721784 | 2131	|	got the rule from [vmainen](https://www.reddit.com/r/cellular_automata/comments/9mfthz/langtons_ant_exhibiting_a_distinct_highwaypattern/). Periods around 5k |
|32768n + 28757		|	**RLRLRLRLLLLLRRR**RLLLR | 300078 |33 | 20k - 300k |

#### Some rules that form highways

|Rules|Period|
|:-:|:-:|
|2 | 104|
|4 | 18 |
|2<sup>n</sup>  for n≥3 | 16n+4 |
|R**L...L**R**L...L**... (n L's in each block): __2<sup>n</sup>*(2<sup>n+1</sup>*(2k-1)+1)__ for n>1,  k>0 | same period as 2<sup>n</sup>|
|L**R...R**L**R...R**... (n R's in each block): __2<sup>n</sup>*(2<sup>n+1</sup>*(2k+1)-1)-1__ for n>1,  k>0 | same period as 2<sup>n</sup>|

