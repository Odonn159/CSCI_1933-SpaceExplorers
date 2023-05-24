# CSCI_1933-SpaceExplorers
Create a game of space explorers using various data structures, and implement different strategies to win.
# How to use
## To run the game WITH graphics...
○ Open the file project4/src/spaceexplorers/publicapi/Driver.Java<br>
○ Edit the file to call the strategies of your choice (Or select in game):<br>
●E.g. GameWindow window = new GameWindow(Strategy.class, false);<br>
 &nbsp; &nbsp; &nbsp; &nbsp; ●  If you want to make your own strategy, you need to create a .jar file for your strategy:<br>
o Then, run the game by running the Driver.Java class, which has a main method.
Be sure to set your Working directory to the parent folder for the project. 
## To run the game WITHOUT graphics (For testing faster)...
○ Open the file project4/src/spaceexplorers/core/SpaceExplorers.Java<br>
○ Edit the file to call your strategy instead of the provided strategies<br>
■ Import your strategy with import spaceexplorers.strategies.MyStrategy<br>
■ In the main method, change strategy1 to be an instance of MyStrategy<br>
● E.g. with IStrategy strategy1 = new MyStrategy();<br>
■ Then, run the game by running the SpaceExplorers.Java class, which has
a main method. Be sure to set your Working directory to the parent folder
for the project.

# The Game Rules
## Premise
Space Explorers is a game where two players are pitted against each other in a system of
interconnected planets. The players each start out on a home planet and the object of the game
is to have the largest population by the end of the game.
## Game Board
The game board is set up as a graph of planets interconnected by weighted edges. 
● Population growth occurs at a set rate on each planet. <br>
● If two planets are connected by an edge, then shuttles can be sent between these
planets. The distance between the planets affects how many turns it takes for shuttles to
move from one planet to the other.<br>
● The planets vary by two factors: <br>
  &nbsp; &nbsp; &nbsp; &nbsp; ○   Size correlates to the total population the planet can support. Once a planet hits
its maximum population, population growth will cease and any population that
exceeds the maximum will decrease by the rate described below. <br>
  &nbsp; &nbsp; &nbsp; &nbsp; ○   Habitability correlates to the population growth rate on a planet. Population
change after one turn for a given planet is defined below. <br>
■ Let c = current population, m = max population, g = growth rate, and p =
overpopulation penalty. <br>
&nbsp; &nbsp; &nbsp; &nbsp; ●   If c < m, pop next turn = c*g <br>
&nbsp; &nbsp; &nbsp; &nbsp; ●   If c >= m, pop next turn = c - (c-m)*p <br> 
■ Currently, p = 0.1 and g = 1 + (habitability / 100) <br>
&nbsp; &nbsp; &nbsp; &nbsp;● Each player starts out with one planet with a given population. A player can send
shuttles with explorers to neighboring planets on their turn. <br>
&nbsp; &nbsp; &nbsp; &nbsp;● Each player receives information about the entire game board, including the planet IDs
of all planets and which planets are interconnected. However, a player can only see
detailed information (population percentages, size, habitability, incoming shuttles) about
the planets that their people have the majority on and their neighboring planets. <br>
## Game Flow
In one turn, a player <br>
● Receives information about the game state <br>
● Adds moves to the event queue <br>
&nbsp; &nbsp; &nbsp; &nbsp;○ A ‘move’ is sending a shuttle from one planet to a neighboring planet. The player
can set how many explorers are sent in the shuttle. <br>
&nbsp; &nbsp; &nbsp; &nbsp;○ A player can make as many moves as they want in a turn. <br>
● Returns the event queue to the game engine <br>
The game engine will then make all legal moves in the event queue, and allow one unit of time
(one full turn cycle) to pass, in which <br>
● Population growth (or decay, if overpopulation cap) occurs on all planets <br>
● All shuttles move one step <br>
The game play then passes to the other player.
## Shuttles and Landings
A shuttle carries some amount of population from Planet A to Planet B. Let’s say that Player 1
sent a shuttle with 100 explorers, and the distance between the planets is 2. The shuttle takes
two full turn cycles to arrive at Planet B, and during that time, no population growth occurs on
the shuttle. There are several possibilities when the shuttle arrives at Planet B.
### Player 1 Has Majority on Planet B
Since Player 1’s population is the majority on the planet, the explorers in the shuttle will always
be added to the population as their people are able to accommodate some overcrowding with
their own people if necessary.
### Player 2 Has Majority on Planet B
If the population on Planet B has not hit the population cap, the explorers in the shuttle will
simply be added to Player 1’s population on the planet. If the population cap has been reached,
the explorers on the shuttle will be lost since the player’s population on the planet does not have
resources necessary to accommodate overpopulation.
### Planet B is Neutral
Player 1’s explorers are added to the population if the population cap has not been reached.
Majority populations will give precedence to shuttles that they recognize (“friendly shuttles”), so
all friendly shuttles will land before any others are able to land.
## End of the Game
The game ends when one player has a majority population on all planets or a maximum number
of turns is reached. If the maximum number of turns is reached, the player with the larger total
population at that point wins the game.

# My strategy
My strategy was to assign each visible planet a "value", which would be based on Size and Habitability and ownership, and then have each of my planets send them population based on the value.
Values are weighted based on population. If my people are the only ones occupying, then it is undervalued, since it is uncontested and more population is unecessary. If it is unoccupied, it is heavily weighted in favor, since it is vitally important to take as many uncontested planets early to gain population for future fights. If it is enemy or joint occupation it is standard weight.
The goal of this strategy is to rapidly expand at the beginning of the game in order to dominate growth, gaining a sizeable population advantage to defeat the opponent. By prioritizing unoccupied->enemy->friendly we ensure that population is sent to the most valuable and important planet.  
Then my program will determine surplus population for each planet, based on friendly vs enemy population and incoming population. Then send 1 to any unoccupied adjacent planets (Always very highly valued, in order of habitability), then divide the remaining excess population based on the value of each planet adjacent in the graph. This method also ensures that planets that are entirely surrounded by friendly planets still send resources to the front lines, rather than building up to the population total while the enemy expands.
Overall this strategy was exceptionally effective and beat almost every AI the vast majority of the time.
