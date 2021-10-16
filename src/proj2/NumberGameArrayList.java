package proj2;


import java.lang.reflect.Array;
import java.util.ArrayList;

public class NumberGameArrayList implements NumberSlider {

    //Used to store and manipulate grid values
    private int[][] grid;
    private int rows, columns;
    private int winningValue;

    private GameStatus gameStatus = GameStatus.IN_PROGRESS;

    //Mostly for used for undo, can save any board state to be used later
    private ArrayList<ArrayList> allMoves = new ArrayList<>();

    public NumberGameArrayList() {
        resizeBoard(4, 4, 16);

    }

    public NumberGameArrayList(int rows, int columns, int winningValue) {
        resizeBoard(rows, columns, winningValue);
    }

    @Override
    public void resizeBoard(int height, int width, int winningValue) {
        this.rows = height;
        this.columns = width;

        this.grid = new int[this.rows][this.columns];
        this.winningValue = winningValue;
    }

    @Override
    public void reset() {
        this.grid = new int[this.rows][this.columns];
        placeRandomValue();
        placeRandomValue();
        saveBoard();
    }

    @Override
    public void setValues(int[][] ref) {
        resizeBoard(ref.length, ref[0].length, this.winningValue);
        for(int row=0; row<ref.length; row++) {
            for(int col=0; col<ref[row].length; col++) {
                this.grid[row][col] = ref[row][col];
            }
        }
    }

    @Override
    public Cell placeRandomValue() {

        Cell newRandCell = new Cell();
        newRandCell.setColumn((int)(Math.random()*(this.columns)));
        newRandCell.setRow((int)(Math.random()*(this.rows)));
        //TODO add some random number (2 or 4) to this
        newRandCell.setValue(2);

        //TODO this won't work later for when have to check if entire board is full
        while(grid[newRandCell.getRow()][newRandCell.getColumn()] != 0) {
            newRandCell.setColumn((int)(Math.random()*(this.columns)));
            newRandCell.setRow((int)(Math.random()*(this.rows)));
        }

        this.grid[newRandCell.getRow()][newRandCell.getColumn()] = newRandCell.getValue();

        return newRandCell;
    }

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
                return true;
            }
        }


        return false;
    }

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

    @Override
    public GameStatus getStatus() {
        return this.gameStatus;
    }

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

    /**
     * Saves the current board values to allMoves ArrayList
     */
    private void saveBoard() {
        this.allMoves.add(getNonEmptyTiles());
    }

    //TODO check for invalids later
    private ArrayList<Cell> convertGridRowToCellArrayList(int row) {
        ArrayList<Cell> cellsInRow = new ArrayList<>();
        for(int col=0; col<grid[row].length; col++) {
            if(grid[row][col] != 0) {
                cellsInRow.add(new Cell(row, col, grid[row][col]));
            }
        }

        return cellsInRow;
    }

    //TODO check for invalids later
    private ArrayList<Cell> convertGridColToCellArrayList(int col) {
        ArrayList<Cell> cellsInRow = new ArrayList<>();
        for(int row=0; row<grid.length; row++) {
            if(grid[row][col] != 0) {
                cellsInRow.add(new Cell(row, col, grid[row][col]));
            }
        }

        return cellsInRow;
    }

    //TODO again check invalids
    //right and down are the same, left and up are the same
    private ArrayList<Cell> mergeCells(ArrayList<Cell> cells, SlideDirection dir) {
        if(dir == SlideDirection.RIGHT || dir == SlideDirection.DOWN) {
            //goes right to left for merging, goes to 1 because that checks 0
            for(int i=cells.size()-1; i>0; i--) {
                if(cells.get(i).getValue() == cells.get(i-1).getValue()) {
                    cells.remove(i);
                    cells.get(i-1).setValue(cells.get(i-1).getValue()*2);
                    //TODO this should prevent double/extra merging, but I'll see
                    i--;
                }
            }
        } else if (dir == SlideDirection.LEFT || dir == SlideDirection.UP) {
            for(int i=0; i<cells.size()-1; i++) {
                if(cells.get(i).getValue() == cells.get(i+1).getValue()) {
                    cells.remove(i);
                    cells.get(i).setValue(cells.get(i).getValue()*2);
                }
            }
        }

        return cells;
    }

    private void wipeRow(int row) {
        //TODO check for num row too big, small, neg, etc
        for(int col=0; col<grid[row].length; col++) {
            grid[row][col] = 0;
        }
    }

    private void wipeCol(int col) {
        //TODO add invalid check
        for(int row=0; row<grid.length; row++) {
            grid[row][col] = 0;
        }
    }

}
