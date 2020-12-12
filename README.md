# Langton's Ant 
Multicolor extension of Langton's Ant cellular automaton that finds highways and their periods

It supports square and hexagonal grid, 3D and 4D

Moves supported by each mode:

- Square: R, L
- Hexagonal: F, R, r, B, l, L
- 3D: R, L, U, D
- 4D: R, L, U, D, X, Y (90º and -90º rotations along xy, xz and xw planes)

More information on [Wikipedia](https://en.wikipedia.org/wiki/Langton%27s_ant#Extension_to_multiple_colors)

## How to use

The sign up must be done using the gui. After that, you can run the program on console

### Commands

- **-w** *n*:    Runs *n* ants simultaneously on different threads (limited to the number of CPU threads)
- **-wh** *n*:    Runs *n* hexagonal ants simultaneously on different threads (limited to the number of CPU threads)
- **-w3** *n*:    Runs *n* 3D ants simultaneously on different threads (limited to the number of CPU threads)
- **-w4** *n*:	Runs *n* 4D ants simultaneously on different threads (limited to the number of CPU threads)
- **-u** *username*: Login as *username*, you have to enter the password on the next line
- **--nogui**:   No interface mode
- **--nolog**:   No log mode

Example:

```console
	java -jar langton.jar -w 4 -wh 2 --nogui
```

## How it works

The server sends rules to each client to see if they formed a highway and if so, find their period

Every few minutes, the client sends the data back to the server and stores the rules in the database

This way we make sure that no rule is tested multiple times

## TODO
- [ ] Server
  - [ ] Verify rules by different clients
  - [ ] Prevent people from sending false data (trust system based on rule verification by other users)
- [ ] Webpage to visualize all the data
- [ ] Improve GUI
- [ ] Different work types:
  - [ ] Verify rules 
  - [ ] Approximate period of big highways
  - [ ] Find exact period of big highways
- [ ] Render hexagonal grid
- [ ] Parallel computing on GPU (doesn't seem possible)
- [ ] Compute approximate period of highways on the fly
- [ ] Algorithm to differentiate triangles/squares from highways:
- [x] Store size of highways (displacement of the ant each period, e.g. the displacement of the original ant is 2×2). This could be useful to distinguish highways with the same period but different structure

## Top highways with longest period

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
                                 

### Biggest highways with unknown period
| Tested to # iters	|	Rule String					|	Rule number		|	Highway size	|	Estimated period	|	Real period		|	Rel. Error	|
|:-:|:-|:-:|:-:|:-:|:-:|:-:|
|	4.7e11			|	LLRLRRRLRRLLLRRLLLLLLRLLRLRR|	220226420   	|	> 2^500		??	|	3e154				|					|				|
| 	1e11		  	| 	LLRLRRLLRLLLLLLRRLLLR 		|	1147188			|   >= 133671045600	|	>=1.104122836656e15	|					|				|
|	1.1e13			|	RRLRLLRRLRRRRRRRRRLLLRLLRLR	|	86245067		|	>= 4707316320	|	>=4.18492189e13 	|					| 				|
|	1e13			|	RRRLRRLLRLRRRRLLRRRLLRLR	|	10960183		|	2^42*3 ??		|	3.77616273e16 ??	|					|				|
|	1.74e12			|	RRRLRRLLRLRRRRRRRRRLR		|	1572151			|	2^40*3 ??		|	5.64742157e15 ??	|					|				|
|	4e11			|	LLRLRRLLRLLLLLLLLRRRRRRLRLR	|	92143924		|	>= 886624056	|	>=5.730524292850e12	|					|				|
|	7.6e11   		|	LLRLRRLLRLLLLLLLLLLRLRLR 	| 	11010356		| 	>= 146880		|						|					|				|
|	1.4e12			|	LLRLRRLLRLLLLLLLLRRRLLRLR	|	21889332		|	>= 1573560		|						|					|				|
|	1.5e10			|	RRLRLLRRLRRRRRRRRRLRRLRLRR	|	56360651		|	>= 2364582528	|						|					|				|
|					|								|	15004911		|					|						|					|				|
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

### Some functions that generate lots of highways

|functions| longest highway | longest highway period | % form highways | info |
|:-:|:-:|:-:|:-:|:-:|
|16384n+16075		| **RRLRLLRRLRRRRR**RRRRLR| 5307264488	| 28.1% | biggest highway found |
|16384n+15435<br>16384n+948		|**RRLRLLRLLLRRRR**RRR|	320374420 | 10.7% | periods of around 1m |
|8192n+8106		|	**LRLRLRLRRRRRR**RLRRRRRLRLRR | 907904  | 94.9% | periods vary from 3k to 27k |
|16384n+12892	| **LLRRRLRLLRLLRR**RLLRLLLRLRRLRLRLL RLLRLRLLLRRLLRRRRRLRRRRRLRLRLRR| 8797680 | 44.6%	|	got the rule from [vmainen](https://www.reddit.com/r/cellular_automata/comments/9mfthz/langtons_ant_exhibiting_a_distinct_highwaypattern/). Periods around 5k |
|32768n+28757		|	**RLRLRLRLLLLLRRR**RLLLR | 300078 |24.1% | 20k - 300k |