package com.javarush.task.task35.task3513;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class Controller extends KeyAdapter {
    private Model model;
    private View view;
    private final static int WINNING_TILE = 2048;

    public Controller(Model model){
        this.model = model;
        this.view = new View(this);
    }

    public void resetGame(){
        model.resetGameTiles();
        model.score=0;
        view.isGameWon = false;
        view.isGameLost=false;
    }
    public Tile[][] getGameTiles(){
        return model.getGameTiles();
    }

    public int getScore(){
        return model.score;
    }

    public View getView() {
        return view;
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int code = e.getKeyCode();
        if (code == KeyEvent.VK_ESCAPE){
            resetGame();
        }
        if (!view.isGameWon && !view.isGameLost){
            if (code == KeyEvent.VK_LEFT){
                model.left();
            }else if (code == KeyEvent.VK_RIGHT){
                model.right();
            }else if (code == KeyEvent.VK_UP){
                model.up();
            }else if (code == KeyEvent.VK_DOWN){
                model.down();
            }else if (code == KeyEvent.VK_Z){
                model.rollback();
            }else if (code == KeyEvent.VK_R){
                model.randomMove();
            }else if (code == KeyEvent.VK_A) {
                model.autoMove();
            }
        }
        if (model.maxTile == WINNING_TILE){
            view.isGameWon = true;
        }
        if (!model.canMove()){
            view.isGameLost = true;
        }
        view.repaint();
    }
}
