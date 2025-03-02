# Tic Tac Toe - JavaFX

## Overview
This is a Tic Tac Toe game implemented using Java, JavaFX, and socket-based networking. The game allows players to choose different board sizes (3x3, 4x4, or 5x5) and automatically pairs players for matches. It features a graphical user interface (GUI) built with JavaFX and supports multiplayer gameplay over a network.

## Features
- **Multiplayer Support:** Players can connect and compete over a network using sockets.
- **Multiple Board Sizes:** Players can choose to play on a 3x3, 4x4, or 5x5 grid.
- **Automated Player Matching:** The system queues players and matches them automatically.
- **Graphical User Interface:** The game is presented using JavaFX with interactive buttons.
- **Game Management:** Handles game flow, switching between turns, and determining a winner.

## Installation and Setup
### Prerequisites
- Java 11 or later
- JavaFX SDK
- An IDE such as IntelliJ IDEA or VS Code with JavaFX support

### Running the Game
1. Clone this repository:
   ```sh
   git clone <https://github.com/eshap31/Java-Tic-Tac-Toe>
   ```
2. Open the project in your preferred IDE.
3. Ensure JavaFX is configured in your IDE.
4. Start the server application.
5. Run the client application to connect and start playing.

## How the Game Works
1. A player enters their name and selects a board size.
2. Players are matched over the network using sockets.
3. A game board is displayed, and players take turns making moves.
4. The game determines the winner based on Tic Tac Toe rules.

## Future Improvements
- Add AI for single-player mode.
- Implement a score-tracking system.
- Enhance the UI with animations and better graphics.
- Improve network handling for better real-time performance.

## License
MIT License
