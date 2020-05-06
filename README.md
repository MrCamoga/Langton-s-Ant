# Langton's Ant 
Multicolor extension of Langton's Ant cellular automaton program that finds highways and its period

## TODO
- [x] Create server to submit ants tested by users (in PHP or Java)

	-[x]Send rules to clients to test so that no rule is tested multiple times
	
	-Verify rules by different clients
	-[x]Store rules tested and verified by each user for credit
	-Prevent people from sending false data (trust system based on rule verification by other users)
- [ ] Webpage to visualize all the data
- [ ] Improve GUI
- [x] Save huge raster of highway to estimate its period. This could be use to determine if computing the period is feasible (i.e. if the estimated period is 1e17 it'd take 50 years!!)
- [x] Autosave ant state
- [ ] Different work types:

	- Check new rules
	
	- Verify rules (mostly periods)
	
	- Approximate period of big highways (or triangles/squares to discard them)
	
	- Find exact period of big highways
	
- [ ] Alg to differenciate between highways/triangles/squares

## How does it work?
The program will run through all the rules and try to automatically detect highways

After the ant reaches maximum iterations or highway has been found, the program will skip to another rule and will output to the command line all the rules that form highways along with its period.

**Example output:**

```
INFO: 1010973	RLRRRLLLRLRRLRRLRRRR	 5.9763936E7 it/s	2.231s
INFO: 1010974	LRRRRLLLRLRRLRRLRRRR	 5.8737152E7 it/s	2.27s
INFO: 1010975	RRRRRLLLRLRRLRRLRRRR	 6.1585836E7 it/s	2.165s
INFO: 1010976	LLLLLRLLRLRRLRRLRRRR	 6.1814248E7 it/s	2.157s
INFO: 1010977	RLLLLRLLRLRRLRRLRRRR	 5.9023168E7 it/s	2.259s
INFO: 1010978	LRLLLRLLRLRRLRRLRRRR	 5.4869684E7 it/s	2.43s
INFO: 1010979	RRLLLRLLRLRRLRRLRRRR	 6.2334428E7 it/s	2.139s
INFO: 1010981	RLRLLRLLRLRRLRRLRRRR	 6.1022124E7 it/s	2.185s
INFO: 1010982	LRRLLRLLRLRRLRRLRRRR	 6.0468636E7 it/s	2.205s
INFO: 1010983	RRRLLRLLRLRRLRRLRRRR	 5.8505192E7 it/s	2.279s
INFO: 1010984	LLLRLRLLRLRRLRRLRRRR	 6.068882E7 it/s	2.197s
```

it/s = iterations per second

All rules tested are sent to the server and stored in the database.
	
## Settings
#### Find highways:
*	**detectHighways**: program will try to detect highways if this is true
*	**ignoreSavedRules**: ignores rules that have already been tested
*	**chunkCheck**: when the ant exits that chunk, the program will start finding the highway
*	**repeatcheck**: # of times the period has to repeat before the highway period is confirmed
*	**maxiterations**: skip to next rule if limit reached

#### Save images:
*	**savepic**: if true rules with highways will be saved to disk in the folder /"period"/"rule".png (e.g. the regular Langton's Ant would be saved to /104/2.png)

#### When finding period of huge highways:
*	**deleteOldChunks**: deletes chunks that haven't been visited by the ant in the last 100M steps  to free up memory


## Our findings
1421748 rules have been tested

Of which 484646 form a highway

7405 distinct periods found

All rules up to 1010929 tested

All rules up to 19 letters have been tested

Rules of 20 letters left to test: 25545

#### TOP highways with longest period

|Rule Number|Period|Rule String|
|:-:|-:|:-|  
|54787787             	 |6518789812888   	 |RRLRLLRRLRRRRRRRRRLLLLRLRR                                      |
|34340555             	 |3409034558708   	 |RRLRLLRRLRRRRRRRRRLRLLLLLR                                      |
|120192715            	 |317869216552    	 |RRLRLLRRLRRRRRRRRLLRLRLLRRR                                     |
|5772148427           	 |241836027556    	 |RRLRLLRRLRRRRRRRRRLRLLLLLLLRRLRLR                               |
|45089076             	 |200631077404    	 |LLRLRRLLRLLLLLLLLLLLRRLRLR                                      |
|15416631             	 |117440512200    	 |RRRLRRLLRLRRRRLLRRLRLRRR                                        |
|52116791             	 |113816934400    	 |RRRLRRLLRLRRRRLLRRLRRLLLRR                                      |
|5403410740           	 |99463945900     	 |LLRLRRLLRLLLLLLRRLLLRLLLLRLLLLRLR                               |
|13025588             	 |66487151028     	 |LLRLRRLLRLLLLLRRLRRLLLRR                                        |
|22593844             	 |28299602536     	 |LLRLRRLLRLLLLLRRLLLRRLRLR                                       |
|18858376907          	 |26423448520     	 |RRLRLLRRLRRRRRRRRRLRLLLLLLRLLRRLLLR                             |
|113508043            	 |20009040104     	 |RRLRLLRRLRRRRRRRRRLLLLRRLRR                                     |
|96567604             	 |14264116224     	 |LLRLRRLLRLLLLLLRRLLLLLRRRLR                                     |
|13730100             	 |13498292016     	 |LLRLRRLLRLLLLLLRRLLLRLRR                                        |
|23723769547          	 |7112124816      	 |RRLRLLRRLRRRRRRRRRLRLLLLLRRLLLLRRLR                             |
|141229771            	 |6902623604      	 |RRLRLLRRLRRRRRRRLRLRLRRLLLLR                                    |
|3539252              	 |6740764524      	 |LLRLRRLLRLLLLLLLLRRLRR                                          |
|786123               	 |5307264488      	 |RRLRLLRRLRRRRRRRRRLR                                            |
|31575506635          	 |3886098700      	 |RRLRLLRRLRRRRRRRRRLRLLLLLRLRRLRLRRR                             |
|3477339851           	 |3289325504      	 |RRLRLLRRLRRRRRRRRRLLLLRLRRRRLLRR                                |
                                 

#### Biggest highways with unknown period
| Tested to # iters	|	Rule String					|	Rule number		|	Highway size	|	Estimated period	|	Real period		|	Rel. Error	|
|:-:|:-|:-:|:-:|:-:|:-:|:-:|
|	1.1e13			|	RRLRLLRRLRRRRRRRRRLLLRLLRLR	|	86245067		|	>= 4707316320	|	>=4.18492189e13 	|					| 				|
| 	1e11		  	| 	LLRLRRLLRLLLLLLRRLLLR 		|	1147188			|   >= 133671045600	|	>=1.104122836656e15	|					|				|
|	1e13			|	RRRLRRLLRLRRRRLLRRRLLRLR	|	10960183		|	2^42*3 ??		|	3.77616273e16 ??	|					|				|
|	1.74e12			|	RRRLRRLLRLRRRRRRRRRLR		|	1572151			|	2^40*3 ??		|	5.64742157e15 ??	|					|				|
|	4.7e11			|	LLRLRRRLRRLLLRRLLLLLLRLLRLRR|	220226420   	|	> 2^500		??	|						|					|				|
|	7.6e11   		|	LLRLRRLLRLLLLLLLLLLRLRLR 	| 	11010356		| 	>= 146880		|						|					|				|
|	1.4e12			|	LLRLRRLLRLLLLLLLLRRRLLRLR	|	21889332		|	>= 1573560		|						|					|				|
|	1.5e10			|	RRLRLLRRLRRRRRRRRRLRRLRLRR	|	56360651		|	>= 2364582528	|						|					|				|
|	4e11			|	LLRLRRLLRLLLLLLLLRRRRRRLRLR	|	92143924		|	>= 886624056	|	>=5.730524292850e12	|					|				|
|					|								|	96534219		|					|						|					|				|
|					|								|	5470519604		|					|						|					|				|
|					|								|	10369105611		|					|						|					|				|
|					|								|	16241131211		|	>= 65597220		|						|					|				|
|					|								|	25065946827		|					|						|					|				|
|					|								|	26106134219		|					|						|					|				|
|					|								|	28052291275		|					|						|					|				|
|					|								|	34763177675		|					|						|					|				|
|					|	LLRLRRLLRLLLLLLLLLLLRRLRLR	|	45089076		|	31460520		|	202740399826		|	200631077404	|	1.05%		|
|					|								|	5772148427		|	28301520		|	242294338124		|	241836027556	|	0.189%		|
|					|								|	120192715		|	34543080		|	318476375254		|	317869216552	|	0.191%		|
|					|	RRLRLLRRLRRRRRRRRRLRLLLLLR	|	34340555   		|	478474920		|	3406262955480 		|	3409034558708	|	0.081%		|
|					|	RRLRLLRRLRRRRRRRRRLLLLRLRR	|	54787787		|	777109320		|	6524244609340		|	6518789812888	|	0.083%		|






#### How I found these huge highways
Whenever I found a big highway such as <span style="color:red">**RRLRLLRLLLRRRR**</span>RR (31819) or <span style="color:blue">**RRLRLLRRLRRRRRR**</span> (32459), I tested all rules that started in the same way because they usually have similar behaviour.

Since I read the rules backwards (e.g. RRRLRR -> 110111 = 55) I can test rules that start with the same **n** letters really easily:

```java
	Simulation.init(31819, r -> r+(1<<n));
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

5006196

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