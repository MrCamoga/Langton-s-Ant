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

You first have to register [here](https://langtonsant.es/register.php). After that, go to settings and generate a secret token to login on the java client. The token can be regenerated again if it's lost.

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

The server sends rules to each client to check if they form a highway and if so, find their period, size and the ant rotation.

Every few minutes, the client sends the data back to the server and stores the rules in the database.

This way we make sure that no rule is tested multiple times.

## TODO
- [ ] Server
  - [ ] Verify rules by different clients (WIP)
  - [x] Improved client-server protocol.
- [x] Webpage
  - [x] Simulator (2d, hex, 3d, 4d,...), step by step, change map size, save image or video,...
  - [x] Search highways on database
  - [x] Login/Register
  - [x] Like rules
  - [x] User statistics and profiles
  - [x] API
  - [x] Add new winding data to database
  - [ ] Add histogram of the states used in the highway construction?
  - [x] User lists to save rules (WIP)
  - [ ] Comments on rules (WIP)
  - [ ] Translation to other languages (WIP)
- [ ] Improve GUI
- [ ] Refactor code 
- [ ] Different work types:
  - [ ] Approximate period of big highways
  - [ ] Find exact period of big highways
- [ ] Render hexagonal grid
- [ ] Parallel computing on GPU (doesn't seem possible)
- [ ] Compute approximate period of highways on the fly
- [x] Algorithm to differentiate triangles/squares from highways:
- [x] Calculate size of highways (displacement of the ant each period, e.g. the displacement of the original ant is 2x2). This is useful to distinguish highways with the same period but different structure.
- [x] Calculate ant rotation (accumulated rotation of the ant to further distinguish ants with same period and size).
- [ ] Make period calculation work 100% of the time in O(n). Right now the period calculation breaks on 0.1% of the rules but at least it says they have period 1 so they can be retested in the future.

## Database

Go to [Langton's Ant Rule DB](https://langtonsant.es/db/rules) to explore the database.

### Biggest highways with unknown period

If we find the size of the highway and the velocity at which it advances, we can estimate the total period of some huge highways (supposing the ant in fact forms a highway and doesn't break midway). To get the size of the highway, we find the period of each line of cells and compute the least common multiple of all of them. For the velocity, we iterate the ant and note approximately how many iterations it takes the ant to advance one cell in the direction the highway is growing.

| Tested to # iters	|	Rule String					|	Rule number		|	Highway size	|	Estimated period	|	Real period		|	Rel. Error	|
|:-:|:-|:-:|:-:|:-:|:-:|:-:|
|	4.7e11			|	LLRLRRRLRRLLLRRLLLLLLRLLRLRR|	220226420   	|	> 2^500		??	|	3e154				|					|				|
| 	1e11		  	| 	LLRLRRLLRLLLLLLRRLLLR 		|	1147188			|   >= 133671045600	|	>=1.104122836656e15	|					|				|
|	1.1e13			|	RRLRLLRRLRRRRRRRRRLLLRLLRLR	|	86245067		|	>= 4707316320	|	>=4.18492189e13 	|					| 				|
|	1e13			|	RRRLRRLLRLRRRRLLRRRLLRLR	|	10960183		|	2^42*3 ??		|	3.77616273e16 ??	|					|				|
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
|	1.74e12			|	RRRLRRLLRLRRRRRRRRRLR		|	1572151			|	2^40*3 ??		|	5.64742157e15 	|		5647091720257856?		|	0.00584%			|
|					|	LLRLRRLLRLLLLLLLLLLLRRLRLR	|	45089076		|	31460520		|	202740399826		|	200631077404	|	1.05%		|
|					|								|	5772148427		|	28301520		|	242294338124		|	241836027556	|	0.189%		|
|					|								|	120192715		|	34543080		|	318476375254		|	317869216552	|	0.191%		|
|					|	RRLRLLRRLRRRRRRRRRLRLLLLLR	|	34340555   		|	478474920		|	3406262955480 		|	3409034558708	|	0.081%		|
|					|	RRLRLLRRLRRRRRRRRRLLLLRLRR	|	54787787		|	777109320		|	6524244609340		|	6518789812888	|	0.083%		|
