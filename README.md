# Langton's Ant 
Multicolor extension of Langton's Ant cellular automaton program that finds highways and its period

It supports square and hexagonal grids

## TODO
- [ ] Server
  - [x] Create server to submit ants tested by users
  - [x] Send rules to clients to test so that no rule is tested multiple times
  - [ ] Verify rules by different clients
  - [x] Store rules tested and verified by each user for credit
  - [ ] Prevent people from sending false data (trust system based on rule verification by other users)
- [ ] Webpage to visualize all the data
- [ ] Improve GUI
- [ ] Different work types:
  - [x] Check new rules
  - [ ] Verify rules 
  - [ ] Approximate period of big highways
  - [ ] Find exact period of big highways
- [x] Algorithm to differentiate between highways/triangles/squares
- [ ] Parallel computing

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






#### How we found the huge highways
The rules that start with the same letters often behave very similar. When we find rules with a long period, we test the ones that start in the same way to find even longer highways.


#### Some functions that generate lots of highways

|functions| longest highway | longest highway period | % form highways | info |
|:-:|:-:|:-:|:-:|:-:|
|16384n+16075		| **RRLRLLRRLRRRRR**RRRRLR| 5307264488	| 28.1% | biggest highway found |
|16384n+15435<br>16384n+948		|**RRLRLLRLLLRRRR**RRR|	320374420 | 10.7% | periods of around 1m |
|8192n+8106		|	**LRLRLRLRRRRRR**RLRRRRRLRLRR | 907904  | 94.9% | periods vary from 3k to 27k |
|16384n+12892	| **LLRRRLRLLRLLRR**RLLRLLLRLRRLRLRLL RLLRLRLLLRRLLRRRRRLRRRRRLRLRLRR| 8797680 | 44.6%	|	got the rule from [vmainen](https://www.reddit.com/r/cellular_automata/comments/9mfthz/langtons_ant_exhibiting_a_distinct_highwaypattern/). Periods around 5k |
|32768n+28757		|	**RLRLRLRLLLLLRRR**RLLLR | 300078 |24.1% | 20k - 300k |

#### Some rules that form highways

|Rules|Period|
|:-:|:-:|
|2 | 104|
|4 | 18 |
|2<sup>n</sup>  for n≥3 | 16n+4 |
|R**L...L**R**L...L**...R (n L's in each block): __2<sup>n</sup>*(2<sup>n+1</sup>*(2k-1)+1)__ for n>1,  k>0 | same period as 2<sup>n</sup>|
|L**R...R**L**R...R**...R (n R's in each block): __2<sup>n</sup>*(2<sup>n+1</sup>*(2k+1)-1)-1__ for n>1,  k>0 | same period as 2<sup>n</sup>|