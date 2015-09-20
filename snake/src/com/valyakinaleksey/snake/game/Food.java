package com.valyakinaleksey.snake.game;

class Food {
    private Cell cell;

    public Food(Cell cell) {
        this.cell = cell;
    }

    public Cell getCell() {
        return cell;
    }

    public void setCell(Cell cell) {
        this.cell = cell;
    }
}
