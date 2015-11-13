package com.valyakinaleksey.snake.game;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import com.valyakinaleksey.snake.MainActivity;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static com.valyakinaleksey.snake.game.Direction.LEFT;

public class Game {
    private static final Lock lock = new ReentrantLock();
    private static Cell[][] cells;
    private static int ROW_COUNT, COL_COUNT;
    private static int result = 0;
    private final List<Cell> freeCells = new ArrayList<>();
    private final Random random = new Random();
    private final List<Cell> cellToUpdate = new ArrayList<>();
    private final Deque<Direction> directions = new LinkedList<>();
    private Snake snake;
    private Food food;
    private volatile Thread gameThread;
    private Direction direction = LEFT;
    private boolean gameLost = false;

    public Game(int rowCount) {
        ROW_COUNT = rowCount;
        COL_COUNT = MainActivity.COL_COUNT;
        initCells();
        initSnake();
        initFood();
    }

    public static Cell getCell(int row, int col) {
        return cells[row][col];
    }

    public static int getResult() {
        return result;
    }

    private void initFood() {
        food = new Food(getRandomFood());
    }

    private Cell getRandomFood() {
        Cell cell = freeCells.remove(random.nextInt(freeCells.size()));
        cell.setState(State.FOOD);
        cellToUpdate.add(cell);
        return cell;
    }

    private void initSnake() {
        int startRow = ROW_COUNT / 2;
        int startCol = COL_COUNT / 2;
        snake = new Snake(startRow, startCol);
        removeFreeCell(startRow, startCol);
        removeFreeCell(startRow, startCol + 1);
        removeFreeCell(startRow, startCol - 1);
    }

    private void removeFreeCell(int rowCount, int colCount) {
        Cell cell = getCell(rowCount, colCount);
        freeCells.remove(cell);
        cellToUpdate.add(cell);
    }

    private void initCells() {
        cells = new Cell[ROW_COUNT][COL_COUNT];
        for (int i = 0; i < ROW_COUNT; i++) {
            for (int j = 0; j < COL_COUNT; j++) {
                cells[i][j] = new Cell(i, j);
                freeCells.add(cells[i][j]);
            }
        }
    }

    public boolean move(Direction direction) {
        cellToUpdate.clear();
        Deque<Cell> body = snake.getBody();
        Cell head = body.getFirst();
        int row = head.getRow();
        int col = head.getCol();
        switch (direction) {
            case LEFT:
                col = col - 1 < 0 ? COL_COUNT - 1 : --col;
                break;
            case RIGHT:
                col = ++col % COL_COUNT;
                break;

            case DOWN:
                row = ++row % ROW_COUNT;
                break;
            case UP:
                row = row - 1 < 0 ? ROW_COUNT - 1 : --row;
                break;
        }

        Cell newCell = getCell(row, col);
        if (newCell.getState() == State.SNAKE) {
            stop();
            return false;
        } else if (newCell.getState() == State.FOOD) {
            initNewCell(newCell);
            food.setCell(getRandomFood());
            result++;
        } else {
            Cell freeCell = snake.removeLastCell();
            freeCells.add(freeCell);
            cellToUpdate.add(freeCell);
            initNewCell(newCell);
        }
        return true;
    }

    private void initNewCell(Cell newCell) {
        freeCells.remove(newCell);
        snake.addCell(newCell);
        cellToUpdate.add(newCell);
    }

    public Snake getSnake() {
        return snake;
    }

    public void stop() {
        gameThread = null;
    }

    public void start(final Handler handler) {
        gameThread = new Thread(new Runnable() {
            @Override
            public void run() {
                Thread thisThread = Thread.currentThread();
                while (gameThread == thisThread) {
                    Message message = handler.obtainMessage();
                    Bundle bundle = new Bundle();
                    bundle.putSerializable("Direction", getDirection());
                    message.setData(bundle);
                    handler.sendMessage(message);
                    try {
                        TimeUnit.MILLISECONDS.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        gameThread.start();

    }

    public Direction checkDirection() {
        try {
            lock.lock();
            return directions.isEmpty() ? direction : directions.peekLast();
        } finally {
            lock.unlock();
        }
    }


    private Direction getDirection() {
        try {
            lock.lock();
            return directions.isEmpty() ? direction : directions.pollLast();
        } finally {
            lock.unlock();
        }
    }

    public void setDirection(Direction direction) {
        lock.lock();
        if (directions.size() < 3) {
            directions.addFirst(direction);
            this.direction = direction;
        }
        lock.unlock();
    }

    public List<Cell> getCellsToUpdate() {
        return cellToUpdate;
    }

    public void reset(Handler handler) {
        result = 0;
        stop();
        clearField();
        initSnake();
        initFood();
        direction = LEFT;
        start(handler);
        gameLost=false;
    }

    public void updateAll() {
        for (Cell cell : snake.getBody()) {
            cellToUpdate.add(cell);
        }
        cellToUpdate.add(food.getCell());
    }

    private void clearField() {
        cellToUpdate.clear();
        Deque<Cell> body = snake.getBody();
        for (Cell cell : body) {
            cell.setState(State.EMPTY);
            cellToUpdate.add(cell);
        }
        cellToUpdate.add(food.getCell());
        food.getCell().setState(State.EMPTY);
    }

    public boolean isGameLost() {
        return gameLost;
    }

    public void setGameLost(boolean gameLost) {
        this.gameLost = gameLost;
    }
}
