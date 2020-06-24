package com.javarush.task.task35.task3513;

import java.util.*;

public class Model {
    private static final int FIELD_WIDTH = 4; // size of the game's field
    private Tile[][] gameTiles = new Tile[FIELD_WIDTH][FIELD_WIDTH]; // the game's field
    int score;
    int maxTile; // max number on a tile
    private Stack<Tile[][]> previousStates = new Stack<>(); // stack for undo operation
    private Stack<Integer> previousScores = new Stack<>(); // stack for undo operation
    private boolean isSaveNeeded = true; // is saved in the stack

    public Model() {
        resetGameTiles();
    }

    public Tile[][] getGameTiles() {
        return gameTiles;
    }

    void resetGameTiles() {
        for (int i = 0; i < FIELD_WIDTH; i++) {
            for (int j = 0; j < FIELD_WIDTH; j++) {
                gameTiles[i][j] = new Tile();
            }
        }
        score = 0;
        maxTile = 2;
        addTile();
        addTile();
    }

    private List<Tile> getEmptyTiles() {
        ArrayList<Tile> emptyTiles = new ArrayList<>(FIELD_WIDTH * 3);
        for (int i = 0; i < FIELD_WIDTH; i++) {
            for (int j = 0; j < FIELD_WIDTH; j++) {
                if (gameTiles[i][j].isEmpty()) emptyTiles.add(gameTiles[i][j]);
            }
        }
        return emptyTiles;
    }

    //adding a tile (2 or 4) to a random position among empty tiles
    private void addTile() {
        List<Tile> emptyTiles = getEmptyTiles();
        if (!emptyTiles.isEmpty()) {
            int indexOfTileToAdd = (int) (emptyTiles.size() * Math.random());
            int value2or4 = Math.random() < 0.9 ? 2 : 4;
            emptyTiles.get(indexOfTileToAdd).value = value2or4;
        }
    }

    public boolean canMove() {
        // empty tiles exist -> can move
        if (!getEmptyTiles().isEmpty()) return true;

        // checking the first column and row for the same neighbours
        for (int i = 1; i < FIELD_WIDTH; i++) {
            if (gameTiles[0][i].value == gameTiles[0][i-1].value ||
                    gameTiles[i][0].value == gameTiles[i-1][0].value) {
                return true;
            }
        }

        //checking other tiles for the same neighbours
        for (int i = 1; i < FIELD_WIDTH; i++) {
            for (int j = 1; j < FIELD_WIDTH; j++) {
                if (gameTiles[i][j].value == gameTiles[i-1][j].value ||
                        gameTiles[i][j].value == gameTiles[i][j-1].value
                ) {
                    return true;
                }
            }
        }

        return false;
    }

    //moving tiles to the left
    private boolean compressTiles(Tile[] tiles) {
        boolean isChanged = false;

        for (int i = 1; i < tiles.length; i++) {
            // checking for 'zero - nonzero' border and moving for 1 position
            if (tiles[i-1].value == 0 && tiles[i].value > 0) {
                isChanged = true;
                tiles[i-1].value = tiles[i].value;
                tiles[i].value = 0;
                int j = i - 2;
                // moving 'nonzero' tile through all zeros (to the left)
                while (j >= 0) {
                    if (tiles[j].value == 0) {
                        tiles[j].value = tiles[j+1].value;
                        tiles[j+1].value = 0;
                    }
                    j--;
                }
            }
        }
        return isChanged;
    }

    //merging to the left
    private boolean mergeTiles(Tile[] tiles) {
        boolean isChanged = false;
        for (int i = 1; i < tiles.length && tiles[i].value > 0; i++) {
            // checking for the same neighbours and merging
            if (tiles[i-1].value == tiles[i].value && tiles[i].value != 0) {
                isChanged = true;
                tiles[i-1].value *= 2;
                tiles[i].value = 0;
                if (tiles[i-1].value > maxTile) maxTile = tiles[i-1].value;
                score += tiles[i-1].value;
                i++;
            }
        }
        compressTiles(tiles);
        return isChanged;
    }

    // rotating frame with particular depth (from the border of whole array) clockwise
    private void rotateFrame(int depth, int length) {
        Tile[] tmp = new Tile[length-2];
        // corners:
        // [depth][depth]               [depth][depth+length-1]
        // [depth+length-1][depth]      [depth+length-1][depth+length-1]

        //turning corners
        Tile oldUpRight = gameTiles[depth][depth+length-1];
        gameTiles[depth][depth+length-1] = gameTiles[depth][depth];
        gameTiles[depth][depth] = gameTiles[depth+length-1][depth];
        gameTiles[depth+length-1][depth] = gameTiles[depth+length-1][depth+length-1];
        gameTiles[depth+length-1][depth+length-1] = oldUpRight;

        //upside -> tmp
        for (int i = 0; i < length - 2; i++) {
            tmp[i] = gameTiles[depth][depth+1+i];
        }
        //leftside -> upside; downside -> leftside; rightside -> downside; tmp -> rightside
        for (int i = 0; i < length - 2; i++) {
            gameTiles[depth][depth+1+i] = gameTiles[depth+length-2-i][depth];
            gameTiles[depth+length-2-i][depth] = gameTiles[depth+length-1][depth+length-2-i];
            gameTiles[depth+length-1][depth+length-2-i] = gameTiles[depth+1+i][depth+length-1];
            gameTiles[depth+1+i][depth+length-1] = tmp[i];
        }
    }

    // rotating the array clockwise
    private void rotate() {
        for (int depth = 0; depth < FIELD_WIDTH / 2; depth++) {
            rotateFrame(depth, FIELD_WIDTH - depth * 2);
        }
    }

    public void left() {
        if (isSaveNeeded) {
            saveState(gameTiles);
        }
        isSaveNeeded = true;
        boolean isChanged = false;
        for (int i = 0; i < FIELD_WIDTH; i++) {
            isChanged = compressTiles(gameTiles[i]) || isChanged;
            isChanged = mergeTiles(gameTiles[i]) || isChanged;
        }
        if (isChanged) addTile();
    }

    public void right() {
        saveState(gameTiles);
        isSaveNeeded = false;
        rotate();
        rotate();
        left();
        rotate();
        rotate();
    }

    public void up() {
        saveState(gameTiles);
        isSaveNeeded = false;
        rotate();
        rotate();
        rotate();
        left();
        rotate();
    }

    public void down() {
        saveState(gameTiles);
        isSaveNeeded = false;
        rotate();
        left();
        rotate();
        rotate();
        rotate();
    }

    // saving state in stack for future undo operation
    private void saveState(Tile[][] gameTiles) {
        Tile[][] copy = new Tile[FIELD_WIDTH][FIELD_WIDTH];
        for (int i = 0; i < FIELD_WIDTH; i++) {
            for (int j = 0; j < FIELD_WIDTH; j++) {
                copy[i][j] = new Tile(gameTiles[i][j].value); // copying tile values
            }
        }
        previousStates.push(copy);
        previousScores.push(score);
    }

    // undo operation (KeyEvent.VK_Z)
    public void rollback() {
        if (!previousStates.empty() && !previousScores.empty()) {
            gameTiles = previousStates.pop();
            score = previousScores.pop();
        }
    }

    // random move - KeyEvent.VK_R
    public void randomMove() {
        int move = ((int) (Math.random() * 100)) % 4;
        switch (move) {
            case 0: left();
                break;
            case 1: right();
                break;
            case 2: up();
                break;
            case 3: down();
                break;
        }
    }

    // detecting if the board changed (based on the sum of all tiles)
    public boolean hasBoardChanged() {
        if (!previousStates.isEmpty()) {
            return getSumOfTiles(gameTiles) != getSumOfTiles(previousStates.peek());
        } else return false;
    }

    private static int getSumOfTiles(Tile[][] gameTiles) {
        int sum = 0;
        for (int i = 0; i < FIELD_WIDTH; i++) {
            for (int j = 0; j < FIELD_WIDTH; j++) {
                sum += gameTiles[i][j].value;
            }
        }
        return sum;
    }

    // MoveEfficiency object describes the game condition after particular move
    public MoveEfficiency getMoveEfficiency(Move move) {
        int oldScore = score;
        int oldNumberOfEmptyTiles = getEmptyTiles().size();
        move.move();
        int newNumberOfEmptyTiles = getEmptyTiles().size();
        MoveEfficiency moveEfficiency;
        if (!hasBoardChanged() && score == oldScore && oldNumberOfEmptyTiles == newNumberOfEmptyTiles) {
            moveEfficiency = new MoveEfficiency(-1, 0, move);
        } else {
            moveEfficiency = new MoveEfficiency(newNumberOfEmptyTiles, score, move);
        }
        rollback();
        return moveEfficiency;
    }

    // auto move based on searching the best move
    public void autoMove() {
        PriorityQueue<MoveEfficiency> queue = new PriorityQueue<>(Collections.reverseOrder());
        queue.offer(getMoveEfficiency(this::up));
        queue.offer(getMoveEfficiency(this::down));
        queue.offer(getMoveEfficiency(this::right));
        queue.offer(getMoveEfficiency(this::left));
        queue.peek().getMove().move();
    }
}
