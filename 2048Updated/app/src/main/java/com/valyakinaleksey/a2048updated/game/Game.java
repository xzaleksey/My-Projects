package com.valyakinaleksey.a2048updated.game;

import android.util.Log;

import com.valyakinaleksey.a2048updated.MainActivityFragment;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import static com.valyakinaleksey.a2048updated.Constants.DOWN;
import static com.valyakinaleksey.a2048updated.Constants.LEFT;
import static com.valyakinaleksey.a2048updated.Constants.LOG_TAG;
import static com.valyakinaleksey.a2048updated.Constants.MAX_COL_ROW;
import static com.valyakinaleksey.a2048updated.Constants.MIN_COL_ROW;
import static com.valyakinaleksey.a2048updated.Constants.RIGHT;
import static com.valyakinaleksey.a2048updated.Constants.UP;

public class Game {
    int[][] ints = {{0, 1}, {0, -1}, {1, 0}, {-1, 0}};
    private Random random = new Random();
    private Cell[][] cells;
    private List<Cell> freeCells = new ArrayList<>();
    private List<Cell> canMove = new ArrayList<>();
    private Set<Cell> movedCells = new HashSet<>();
    private MainActivityFragment.Updater updater;
    private int record;
    private int score;
    private int prevRecord;
    private int prevScore;
    private Cell[][] prevCells;
    private List<Cell> prevFreeCells = new ArrayList<>();
    private List<Cell> tempFreeCells;

    public Game() {
        initCells();
    }

    public void initFirstCells() {
        getCellInitialized();
        getCellInitialized();
    }

    public Cell getCellInitialized() {
        Cell freeCell = getFreeCell();
        initCellValue(freeCell);
        return freeCell;
    }

    private void initCellValue(Cell freeCell) {
        freeCell.setValue(random.nextInt(10) == 0 ? 4 : 2);
    }

    public void loadData(String[] values) {
        int counter = 0;
        for (int i = 0; i < MAX_COL_ROW; i++) {
            for (int j = 0; j < MAX_COL_ROW; j++) {
                int value = Integer.parseInt(values[counter++]);
                if (value != 0) {
                    cells[i][j].setValue(value);
                    freeCells.remove(cells[i][j]);
                }
            }
        }
    }

    private void initCells() {
        cells = new Cell[MAX_COL_ROW][MAX_COL_ROW];
        for (int i = MIN_COL_ROW; i <= MAX_COL_ROW; i++) {
            for (int j = MIN_COL_ROW; j <= MAX_COL_ROW; j++) {
                Cell cell = new Cell(i, j);
                cells[i - 1][j - 1] = cell;
                freeCells.add(cell);
            }
        }
    }

    public void restartGame() {
        freeCells.clear();
        if (MAX_COL_ROW != cells[0].length) {
            initCells();
        }
        for (int i = MIN_COL_ROW; i <= MAX_COL_ROW; i++) {
            for (int j = MIN_COL_ROW; j <= MAX_COL_ROW; j++) {
                cells[i - 1][j - 1].setValue(0);
                freeCells.add(cells[i - 1][j - 1]);
            }
        }
        initFirstCells();
        score = 0;
        prevScore = 0;
        prevCells = null;
    }

    private Cell getFreeCell() {
        return freeCells.remove(random.nextInt(freeCells.size()));
    }

    public Cell[][] getCells() {
        return cells;
    }

    public boolean moveRight() {
        return startMove(RIGHT);
    }

    public boolean moveLeft() {
        return startMove(LEFT);
    }

    public boolean moveDown() {
        return startMove(DOWN);
    }

    public boolean moveUp() {
        return startMove(UP);
    }

    private boolean startMove(int direction) {
        boolean movesMade = false;
        int start;
        int end;
        int counter = 0;
        int increment;
        switch (direction) {
            case RIGHT:
            case DOWN:
                start = MAX_COL_ROW - 2;
                end = MIN_COL_ROW - 1;
                increment = 1;
                break;
            case LEFT:
            case UP:
                start = MIN_COL_ROW;
                end = MAX_COL_ROW - 1;
                increment = -1;
                break;
            default:
                start = 0;
                increment = 0;
                end = 0;
        }
        Log.d(LOG_TAG, "" + direction + "start " + start + " increment" + increment);
        int tempScore = score;
        int tempRecord = record;
        Cell[][] tempCells;
        tempFreeCells = new ArrayList<>();
        tempCells = cloneArray(cells);
        while (counter < MAX_COL_ROW - 1) {
            movesMade = move(start, end, increment, direction) || movesMade;
            end = end + increment;
            counter++;
        }
        if (movesMade) {
            resetChanged();
            prevRecord = tempRecord;
            prevScore = tempScore;
            prevCells = tempCells;
            prevFreeCells.clear();
            prevFreeCells.addAll(tempFreeCells);
        }

        return movesMade;
    }

    private boolean move(int start, int end, int increment, int direction) {
        boolean notEnded = true;
        boolean movesMade = false;

        while (notEnded) {
            checkMoves(start, increment, direction);
            if (canMove.size() > 0) {
                Log.d(LOG_TAG, "" + canMove.size());
                movesMade = true;
                for (Cell cell : canMove) {
                    updateCells(start, increment, direction, cell);
                }
            }

            if (start == end) {
                notEnded = false;
            } else {
                start -= increment;
            }
        }
        if (movedCells.size() > 0) {
            Cell[] cellsUpdated = movedCells.toArray(new Cell[movedCells.size()]);
            updater.update(cellsUpdated);
            movedCells.clear();
        }
        return movesMade;
    }

    private void resetChanged() {
        for (Cell[] cellRow : cells) {
            for (Cell cell : cellRow) {
                cell.setChanged(false);
            }
        }
    }

    private void updateCells(int start, int increment, int direction, Cell cell) {
        Cell adjacentCell = getAdjacentCell(start, increment, direction, cell);
        int value = cell.getValue();
        if (adjacentCell.getValue() == value) {
            int result = value * 2;
            adjacentCell.setValue(result);
            adjacentCell.setChanged(true);
            cell.setChanged(false);
            score += result;
            if (record < score) {
                record = score;
            }
        } else {
            adjacentCell.setValue(value);
            if (cell.isChanged()) {
                adjacentCell.setChanged(true);
                cell.setChanged(false);
            }
        }
        freeCells.remove(adjacentCell);
        cell.setValue(0);
        freeCells.add(cell);
        movedCells.add(cell);
        movedCells.add(adjacentCell);
    }

    private Cell getAdjacentCell(int start, int increment, int direction, Cell cell) {
        Cell adjacentCell;
        if (direction == RIGHT || direction == LEFT) {
            adjacentCell = cells[cell.getRowNumber() - 1][start + increment];
        } else {
            adjacentCell = cells[start + increment][cell.getColumnNumber() - 1];
        }
        return adjacentCell;
    }

    private void checkMoves(int start, int increment, int direction) {
        canMove.clear();
        for (int i = 0; i < MAX_COL_ROW; i++) {
            if (direction == RIGHT || direction == LEFT) {
                checkCells(cells[i][start], cells[i][start + increment]);
            } else {
                checkCells(cells[start][i], cells[start + increment][i]);
            }
        }
    }

    private void checkCells(Cell cell, Cell adjacentCell) {
        if (cell.getValue() != 0 && !adjacentCell.isChanged()) {
            int cellValue = cell.getValue();
            int adjacentCellValue = adjacentCell.getValue();
            if (adjacentCellValue == 0 || cellValue == adjacentCellValue && !cell.isChanged()) {
                canMove.add(cell);
            }
        }
    }

    public int getFreePositionsCount() {
        return freeCells.size();
    }

    public void setUpdater(MainActivityFragment.Updater myHandler) {
        updater = myHandler;
    }

    public int getRecord() {
        return record;
    }

    public void setRecord(int record) {
        this.record = record;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public boolean checkEndGame() {
        if (freeCells.size() == 0) {
            for (int i = 0; i < MAX_COL_ROW; i++) {
                for (int j = 0; j < MAX_COL_ROW; j++) {
                    if (checkAdjacentCells(i, j)) {
                        return false;
                    }
                }
            }
            return true;
        }
        return false;
    }

    private boolean checkAdjacentCells(int rowNumber, int colNumber) {
        Cell cell = cells[rowNumber][colNumber];
        for (int i = 0; i < 4; i++) {
            try {
                if (cells[rowNumber + ints[i][0]][colNumber + ints[i][1]].getValue() == cell.getValue()) {
                    return true;
                }
            } catch (Exception e) {

            }
        }
        return false;
    }

    public Cell[][] cloneArray(Cell[][] src) {
        int length = src.length;
        Cell[][] target = new Cell[length][src[0].length];
        for (int i = 0; i < MAX_COL_ROW; i++) {
            for (int j = 0; j < MAX_COL_ROW; j++) {
                Cell cell = src[i][j];
                target[i][j] = new Cell(cell.getRowNumber(), cell.getColumnNumber());
                target[i][j].setValue(cell.getValue());
                if (freeCells.contains(cell)) {
                    tempFreeCells.add(target[i][j]);
                }
            }
        }
        return target;
    }

    public void undo() {
        if (prevCells != null) {
            score = prevScore;
            cells = prevCells;
            record = prevRecord;
            freeCells.clear();
            freeCells.addAll(prevFreeCells);
            Log.d(LOG_TAG, "prevFreeCells :" + prevFreeCells.size());
            prevFreeCells.clear();
            prevCells = null;
        }
    }
}
