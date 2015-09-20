package com.example.FifteenPuzzleReworked;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static com.example.FifteenPuzzleReworked.FifteenPuzzle.COUNT;

public class Game {

    private Point[][] points;
    private Point emptyPoint; //Пустая точка
    private int movesCount = 0; //Количество ходов

    //Инициализация точек
    public Game() {
        points = new Point[COUNT][COUNT];
        for (int i = 0; i < COUNT; i++) {
            for (int j = 0; j < COUNT; j++) {
                points[i][j] = new Point(i, j);
            }

        }
        emptyPoint = points[3][3];
    }

    //Вернуть пустую точку
    public Point getEmptyPoint() {
        return emptyPoint;
    }

    //Проверка на конец игры
    public boolean checkEndGame() {
        for (int i = 0; i < COUNT; i++) {
            for (int j = 0; j < COUNT; j++) {
                Point point = points[i][j];
                if (point.x != point.correctX || point.y != point.correctY) {
                    return false;
                }
            }
        }
        return true;
    }


    //Рестарт игры
    public void restart(boolean mix) {
        movesCount = 0;
        if (mix){
            randomize();
        }
        else {
            resetPoints();
        }
    }

    //Восстановить точки без перемешивания
    public void resetPoints(){
        for (int i = 0; i < COUNT; i++) {
            for (int j = 0; j < COUNT; j++) {
                Point point = points[i][j];
                point.x = point.correctX;
                point.y = point.correctY;
            }
        }
        emptyPoint=points[3][3];
    }

    //Перемешать картинки
    public void randomize() {
        Random random = new Random();
        List<Structure> elements = new ArrayList<>();
        for (int i = 0; i < COUNT; i++) {
            for (int j = 0; j < COUNT; j++) {
                elements.add(new Structure(i, j));
            }
        }
        for (int i = 0; i < COUNT; i++) {
            for (int j = 0; j < COUNT; j++) {
                Structure structure = elements.remove(random.nextInt(elements.size()));
                points[i][j].x = structure.x;
                points[i][j].y = structure.y;
                if (structure.x == 3 && structure.y == 3) {
                    emptyPoint = points[i][j];
                }
            }
        }

    }

    //Поменять 2 точки местами
    public void swapPoints(int x, int y, int incX, int incY) {
        Point clicked = points[x][y];
        int tempX = clicked.x;
        int tempY = clicked.y;
        Point changedPoint = points[x + incX][y + incY];
        clicked.x = changedPoint.x;
        clicked.y = changedPoint.y;
        changedPoint.x = tempX;
        changedPoint.y = tempY;
        emptyPoint = clicked;
    }

    //Проверка на пустую клетку
    public boolean checkEmpty(int x, int y) {
        return (x == emptyPoint.x && y == emptyPoint.y);
    }

    public Point[][] getPoints() {
        return points;
    }

    public int getMovesCount() {
        return movesCount;
    }

    public void incMovesCount() {
        movesCount++;
    }

    private class Structure {
        int x;
        int y;

        public Structure(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }
}
