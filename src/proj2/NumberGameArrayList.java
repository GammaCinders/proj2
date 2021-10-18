package proj2;

import java.util.ArrayList;

/**********************************************************************
 * Computes all the logic of the 1024 game. Contains a 2d array
 * representing the play grid that can be resized, swiped, reset,
 * ect. Also contains an ArrayList of every previously made move
 * for undoing.
 *
 * @version 10/16/21
 * @author Keagen, Eric
 *********************************************************************/
public class NumberGameArrayList implements NumberSlider {

    /**2d array used to store and manipulate cell values */
    private int[][] grid;
    /**Barely used vars for the number of rows and cols respectively*/
    private int rows, columns;
    /**A power of 2 number that a cell must reach to win the game*/
    private int winningValue;

    /**An enum to track the current game state*/
    private GameStatus gameStatus = GameStatus.IN_PROGRESS;

    /** An ArrayList of Cell ArrayLists for undo(), saves board states
     *  in order when save() is called so that they can be loaded
     *  back later in undo()
     */
    private ArrayList<ArrayList> allMoves = new ArrayList<>();

    /******************************************************************
     * A default constructor that initializes the board with height 4,
     * width 4, and winningValue of 16. Also calls reset() to prevent
     * any errors with allMoves being empty otherwise. Use
     * resizeBoard() to set desired size.
     *****************************************************************/
    public NumberGameArrayList() {
        resizeBoard(4, 4, 16);
        reset();
    }

    /******************************************************************
     * Resets the board with the number of rows and columns, as well
     * as the target win value to the arguments. Also sets gameStatus
     * to IN_PROGRESS. This should be called after the initialization
     * for any size board different from the default:
     * (4 by 4, winningValue of 16).
     * @param height the number of rows in the board
     * @param width the number of columns in the board
     * @param winningValue the value that must appear on the board to
     *                     win the game
     * @throws IllegalArgumentException If height, width, or
     *                                  winningValue is less than 1,
     *                                  and if winningValue is not a
     *                                  power of 2
     *****************************************************************/
    @Override
    public void resizeBoard(int height, int width, int winningValue) {
        if(validPowerOf2(winningValue) && height > 0 && width > 0) {
            this.winningValue = winningValue;
            this.rows = height;
            this.columns = width;
            this.grid = new int[this.rows][this.columns];
            this.gameStatus = GameStatus.IN_PROGRESS;
        } else {
            throw new IllegalArgumentException();
        }
    }

    /******************************************************************
     * Removes all numbered cells from the board and places 2 new
     * random cells
     *****************************************************************/
    @Override
    public void reset() {
        this.grid = new int[this.rows][this.columns];
        this.gameStatus = GameStatus.IN_PROGRESS;
        placeRandomValue();
        placeRandomValue();
        this.allMoves = new ArrayList<>();
        saveBoard();
    }

    /******************************************************************
     * Sets the board to fit and hold the values of the passed 2d
     * array.
     * @param ref The 2d array with values to set the board to
     *****************************************************************/
    @Override
    public void setValues(int[][] ref) {
        resizeBoard(ref.length, ref[0].length, this.winningValue);
        for(int row=0; row<ref.length; row++) {
            for(int col=0; col<ref[row].length; col++) {
                if(ref[row][col] != 0) {
                    this.grid[row][col] = ref[row][col];
                }
            }
        }
        saveBoard();
        if(isGameLost()) {
            gameStatus = GameStatus.USER_LOST;
        } else {
            this.gameStatus = GameStatus.IN_PROGRESS;
        }
    }

    /******************************************************************
     * Sets the board to fit and hold the values of the passed
     * ArrayList of cells. Any grid value not covered by a cell
     * becomes a 0 (empty cell). Can use getNonEmptyTiles
     * to temporarily save a board and then load back with this method.
     * @param ref The cell ArrayList with values to set the board to
     *****************************************************************/
    public void setValues(ArrayList<Cell> ref) {
        this.grid = new int[rows][columns];
        for(Cell cell : ref) {
            grid[cell.getRow()][cell.getColumn()] = cell.getValue();
        }
    }

    /******************************************************************
     * Places a cell of value 2 at an empty board location.
     * @return a Cell object with its row, column, and value attributes
     * initialized properly.
     * @throws IllegalStateException if the board is full.
     *****************************************************************/
    @Override
    public Cell placeRandomValue() {
        if(getNonEmptyTiles().size() == grid.length*grid[0].length) {
            throw new IllegalStateException();
        } else {
            Cell newRandCell = new Cell();
            newRandCell.setColumn((int)(Math.random()*(this.columns)));
            newRandCell.setRow((int)(Math.random()*(this.rows)));
            newRandCell.setValue(2);

            while(grid[newRandCell.getRow()][newRandCell.getColumn()] != 0) {
                newRandCell.setColumn((int)(Math.random()*(this.columns)));
                newRandCell.setRow((int)(Math.random()*(this.rows)));
            }

            this.grid[newRandCell.getRow()][newRandCell.getColumn()] = newRandCell.getValue();
            return newRandCell;
        }
    }

    /******************************************************************
     * Slides all the tiles in the game to the direction given. This
     * merges nearby same value cells to be twice the value if
     * possible.
     * @param dir move direction of the tiles
     *
     * @return true if the board has changed (any tile has a different
     * value than before the move), false otherwise
     *****************************************************************/
    @Override
    public boolean slide(SlideDirection dir) {

        if(dir == SlideDirection.RIGHT) {
            for(int row=0; row<grid.length; row++) {
                ArrayList<Cell> cellsInRow = convertGridRowToCellArrayList(row);
                mergeCells(cellsInRow, SlideDirection.RIGHT);

                //Wipes the row and adds the new shifted row back in
                wipeRow(row);

                for(int i=0; i<cellsInRow.size(); i++) {
                    grid[row][grid[row].length-i-1] = cellsInRow.get(cellsInRow.size()-i-1).getValue();
                }
            }
        } else if(dir == SlideDirection.LEFT) {
            for(int row=0; row<grid.length; row++) {
                ArrayList<Cell> cellsInRow = convertGridRowToCellArrayList(row);
                mergeCells(cellsInRow, SlideDirection.LEFT);

                //Wipe current row before adding back merged array
                wipeRow(row);

                for(int col=0; col<cellsInRow.size(); col++) {
                   grid[row][col] = cellsInRow.get(col).getValue();
                }
            }
        } else if(dir == SlideDirection.UP) {
            for(int col=0; col<grid[0].length; col++) {
                ArrayList<Cell> cellsInCol = convertGridColToCellArrayList(col);
                mergeCells(cellsInCol, SlideDirection.UP);

                //Wipe column before adding back merged array
                wipeCol(col);

                for(int row=0; row<cellsInCol.size(); row++) {
                    grid[row][col] = cellsInCol.get(row).getValue();
                }
            }
        } else if(dir == SlideDirection.DOWN) {
            for(int col=0; col<grid[0].length; col++) {
                ArrayList<Cell> cellsInCol = convertGridColToCellArrayList(col);
                mergeCells(cellsInCol, SlideDirection.DOWN);

                //wipe this shit m8
                wipeCol(col);

                for(int i=0; i<cellsInCol.size(); i++) {
                    grid[grid.length-i-1][col] = cellsInCol.get(cellsInCol.size()-i-1).getValue();
                }
            }
        }

        //Check for if anything changed, to see to add one randcell after or not
        for(Cell cell : ((ArrayList<Cell>)allMoves.get(allMoves.size()-1))) {
            if(grid[cell.getRow()][cell.getColumn()] != cell.getValue()) {
                placeRandomValue();
                saveBoard();
                if(isGameLost()) {
                    gameStatus = GameStatus.USER_LOST;
                }
                return true;
            }
        }

        return false;
    }

    /******************************************************************
     * Returns an ArrayList of cells where each cell is a non-zero
     * value on the grid.
     * @return an ArrayList of cells where each cell is a non-zero
     * value on the grid
     *****************************************************************/
    @Override
    public ArrayList<Cell> getNonEmptyTiles() {
        ArrayList<Cell> cells = new ArrayList<>();

        //Go through each row and col
        for(int row=0; row<this.rows; row++) {
            for(int col=0; col<this.columns; col++) {
                if(grid[row][col] != 0) {
                    cells.add(new Cell(row, col, grid[row][col]));
                }
            }
        }

        return cells;
    }

    /******************************************************************
     * Returns the state of the game
     * @return a value of the GameStatus enum
     *****************************************************************/
    @Override
    public GameStatus getStatus() {
        return this.gameStatus;
    }

    /******************************************************************
     * Undo the most recent action, i.e. restore the board to its previous
     * state. Calling this method multiple times will ultimately restore
     * the game to the very first initial state of the board holding two
     * random values. Further attempt to undo beyond this state will throw
     * an IllegalStateException.
     *****************************************************************/
    @Override
    public void undo() {
        if(allMoves.size() > 1) {
            this.allMoves.remove(allMoves.size()-1);
            this.grid = new int[rows][columns];
            for(Cell cell : ((ArrayList<Cell>)allMoves.get(allMoves.size()-1))) {
                this.grid[cell.getRow()][cell.getColumn()] = cell.getValue();
            }
        } else {
            throw new IllegalStateException();
        }
    }

    /******************************************************************
     * Saves the current board values to allMoves ArrayList
     *****************************************************************/
    private void saveBoard() {
        this.allMoves.add(getNonEmptyTiles());
    }

    /******************************************************************
     * Returns and ArrayList of cells that are at the INDEX of
     * the arg row
     * @param row index of desired row in grid
     * @return ArrayList of each non-empty tile in the row
     * @throws IllegalArgumentException if the row is < 0 or >= the
     *                                  current number of board rows
     *****************************************************************/
    private ArrayList<Cell> convertGridRowToCellArrayList(int row) {
        if(row < 0 || row >= this.rows) {
            throw new IllegalArgumentException();
        } else {
            ArrayList<Cell> cellsInRow = new ArrayList<>();
            for(int col=0; col<grid[row].length; col++) {
                if(grid[row][col] != 0) {
                    cellsInRow.add(new Cell(row, col, grid[row][col]));
                }
            }

            return cellsInRow;
        }
    }

    /******************************************************************
     * Returns and ArrayList of cells that are at the INDEX of
     * the arg col
     * @param col index of desired col in grid
     * @return ArrayList of each non-empty tile in the col
     * @throws IllegalArgumentException if the col is < 0 or >= the
     *                                  current number of board cols
     *****************************************************************/
    private ArrayList<Cell> convertGridColToCellArrayList(int col) {
        if(col < 0 || col >= this.columns) {
            throw new IllegalArgumentException();
        } else {
            ArrayList<Cell> cellsInRow = new ArrayList<>();
            for(int row=0; row<grid.length; row++) {
                if(grid[row][col] != 0) {
                    cellsInRow.add(new Cell(row, col, grid[row][col]));
                }
            }

            return cellsInRow;
        }
    }

    /******************************************************************
     * Mutates/Merges an ArrayList of cells based on the slide direction, used
     * for each row or col of slide()
     * @param cells ArrayList of cells to merge
     * @param dir board direction to merge the cells
     * @return ArrayList of merged cells, which still has the same
     *          reference as the passed arg, (no new ArrayList is
     *          created!)
     *****************************************************************/
    private ArrayList<Cell> mergeCells(ArrayList<Cell> cells,
                                       SlideDirection dir) {
        if(dir == SlideDirection.RIGHT || dir == SlideDirection.DOWN) {
            //goes right to left for merging, goes to 1 because that checks 0
            for(int i=cells.size()-1; i>0; i--) {
                if(cells.get(i).getValue() == cells.get(i-1).getValue()) {
                    cells.remove(i);
                    cells.get(i-1).setValue(cells.get(i-1).getValue()*2);
                    if(cells.get(i-1).getValue() == winningValue) {
                        gameStatus = GameStatus.USER_WON;
                    }
                    i--;
                }
            }
        } else if (dir == SlideDirection.LEFT || dir == SlideDirection.UP) {
            for(int i=0; i<cells.size()-1; i++) {
                if(cells.get(i).getValue() == cells.get(i+1).getValue()) {
                    cells.remove(i);
                    cells.get(i).setValue(cells.get(i).getValue()*2);
                    if(cells.get(i).getValue() == winningValue) {
                        gameStatus = GameStatus.USER_WON;
                    }
                }
            }
        }

        return cells;
    }

    /******************************************************************
     * Cleans an entire index row (sets all values to 0)
     * @param row row index in grid to wipe
     * @throws IllegalArgumentException if row < 0 or > grid rows
     *****************************************************************/
    private void wipeRow(int row) {
        if(row < 0 || row >= this.rows) {
            throw new IllegalArgumentException();
        } else {
            for(int col=0; col<grid[row].length; col++) {
                grid[row][col] = 0;
            }
        }
    }

    /******************************************************************
     * Cleans an entire index col (sets all values to 0)
     * @param col col index in grid to wipe
     * @throws IllegalArgumentException if col < 0 or > grid cols
     *****************************************************************/
    private void wipeCol(int col) {
        if(col < 0 || col >= this.columns) {
            throw new IllegalArgumentException();
        } else {
            for(int row=0; row<grid.length; row++) {
                grid[row][col] = 0;
            }
        }

    }

    /******************************************************************
     * Checks to see if the game has been lost
     * @return bool true if the board is full and there are no more
     * valid moves, false otherwise
     *****************************************************************/
    private boolean isGameLost() {
        if(grid.length*grid[0].length == allMoves.get(allMoves.size()-1).size()) {
            for(int row=0; row<rows; row++) {
                ArrayList<Cell> cellRow = convertGridRowToCellArrayList(row);
                if(cellRow.size() != mergeCells(cellRow, SlideDirection.RIGHT).size()) {
                    return false;
                }
            }

            for(int col=0; col<columns; col++) {
                ArrayList<Cell> cellRow = convertGridColToCellArrayList(col);
                //TODO IDK if this works
                if(cellRow.size() != mergeCells(cellRow, SlideDirection.RIGHT).size()) {
                    return false;
                }
            }

            return true;
        }

        return false;
    }

    /******************************************************************
     * Checks if the given number is a power of 2. In this case it must
     * also be > 2 since this is used to check for valid winningValues
     * and 2 is just a stupid win value
     * @param n number to check if it is a power of 2
     * @return bool true if n is both > 2 and a power of 2, false
     * otherwise
     *****************************************************************/
    private boolean validPowerOf2(int n) {
        double pow = n;
        if(pow % 2 == 1 || pow <= 2) {
            return false;
        } else {
            while(pow != 1) {
                pow /= 2;
                if(pow % 1 != 0) {
                    return false;
                }
            }
            return true;
        }
    }

    /******************************************************************
     * Gives the current number of rows of the grid size
     * @return int number of rows in the current grid
     *****************************************************************/
    public int getRows() {
        return this.rows;
    }

    /******************************************************************
     * Gives the current number of columns of the grid size
     * @return int number of columns in the current grid
     *****************************************************************/
    public int getColumns() {
        return this.columns;
    }

    /******************************************************************
     * Sets the current winning value to the arg
     * @param newWinValue the new value needed to win the game, has
     *                    to be a power of 2 (and > 0).
     * @throws IllegalArgumentException if newWinValue is not a
     *                                  valid power of 2 (and > 0)
     *****************************************************************/
    public void setWinningValue(int newWinValue) {
        if(validPowerOf2(newWinValue)) {
            this.winningValue = newWinValue;
        } else {
            throw new IllegalArgumentException();
        }
        
        //TODO I should check here for if the new win value
        //TODO is already on the board
        gameStatus = GameStatus.IN_PROGRESS;
    }



}
