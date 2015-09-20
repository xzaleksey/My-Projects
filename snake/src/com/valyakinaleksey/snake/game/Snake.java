package com.valyakinaleksey.snake.game;

import java.util.ArrayDeque;
import java.util.Deque;

public class Snake {
    private final Deque<Cell> body = new ArrayDeque<>();

    public Snake(int startRow, int startCol) {
        addCell(startRow, startCol + 1);
        addCell(startRow, startCol);
        addCell(startRow, startCol - 1);
    }

    private void addCell(int startRow, int startCol) {
        Cell cell = Game.getCell(startRow, startCol);
        cell.setState(State.SNAKE);
        body.addFirst(cell);
    }

    public void addCell(Cell cell) {
        cell.setState(State.SNAKE);
        body.addFirst(cell);
    }

    public Cell removeLastCell() {
        Cell cell = body.peekLast();
        cell.setState(State.EMPTY);
        return body.removeLast();
    }


    public Deque<Cell> getBody() {
        return body;
    }
}
