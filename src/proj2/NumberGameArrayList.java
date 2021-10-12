package proj2;


import java.util.ArrayList;

public class NumberGameArrayList implements NumberSlider {

    //Used to store and manipulate grid values
    private int[][] grid;
    private int rows, columns;
    private int winningValue;

    private GameStatus gameStatus = GameStatus.IN_PROGRESS;

    //Mostly for used for undo, can save any board state to be used later
    private ArrayList<Cell> lastSave = new ArrayList<>();

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
        boolean canSlide = false;



        //TODO remave later for an actual check
        //TODO actually just check after if lastSave == current board after mutation
        canSlide = true;

        if(canSlide) {
            saveBoard();

            //TODO work in progress for saving some code on right/left, and up/odwn
//            if(dir == SlideDirection.LEFT || dir == SlideDirection.RIGHT) {
//
//                //Go through each row because thats how blocks
//                //are slide and merged when lief or right
//                for(int row=0; row < grid.length; row++) {
//                    ArrayList<Cell> cellsInRow = new ArrayList<>();
//                    for(int col=0; col<grid[row].length; col++) {
//                        if(grid[row][col] != 0) {
//                            cellsInRow.add(new Cell(row, col, grid[row][col]));
//                        }
//                    }
//                }
//            }

            if(dir == SlideDirection.RIGHT) {
                //Increment through and slide each row to the right
                for(int row=0; row<grid.length; row++) {

                    //Saves all the cells in the current row to a new ArrayList
                    ArrayList<Cell> cellsInRow = new ArrayList<>();
                    for(int col=0; col<grid[row].length; col++) {
                        if(grid[row][col] != 0) {
                            cellsInRow.add(new Cell(row, col, grid[row][col]));
                        }
                    }

                    //This next part merges each cell as much as possible
                    boolean doneMerging = false;
                    while(!doneMerging) {
                        doneMerging = true;

                        //goes right to left for merging, goes to 1 because that checks 0
                        for(int i=cellsInRow.size()-1; i>0; i--) {
                            if(cellsInRow.get(i).getValue() == cellsInRow.get(i-1).getValue()) {
                                cellsInRow.remove(i);
                                cellsInRow.get(i-1).setValue(cellsInRow.get(i-1).getValue()*2);
                                doneMerging = false;
                            }
                        }
                    }

                    //Wipes the row and adds the new shifted row back in
                    wipeRow(row);
                    for(int i=0; i<cellsInRow.size(); i++) {
                        //TODO this code could be better, the ArrayList should never be bigger than
                        //TODO the grid row length, but if it is it will throw an error (IOB)
                        grid[row][grid[row].length-i-1] = cellsInRow.get(cellsInRow.size()-i-1).getValue();
                    }
                }
            }

            if(dir == SlideDirection.LEFT) {
                //Increment through and slide each row to the left
                for(int row=0; row<grid.length; row++) {

                    //Saves all the cells in the current row to a new ArrayList
                    ArrayList<Cell> cellsInRow = new ArrayList<>();
                    for(int col=0; col<grid[row].length; col++) {
                        if(grid[row][col] != 0) {
                            cellsInRow.add(new Cell(row, col, grid[row][col]));
                        }
                    }

                    //This next part merges each cell as much as possible
                    boolean doneMerging = false;
                    while(!doneMerging) {
                        doneMerging = true;

                        //goes left to right for merging, goes to size-1 because 2nd to last catches
                        //also only do this if there is stuff in the row
                        if(cellsInRow.size() > 1) {
                            for(int i=0; i<(cellsInRow.size()-1); i++) {
                                if(cellsInRow.get(i).getValue() == cellsInRow.get(i+1).getValue()) {
                                    cellsInRow.remove(i+1);
                                    cellsInRow.get(i).setValue(cellsInRow.get(i).getValue()*2);
                                    i--;
                                    doneMerging = false;
                                }
                            }
                        }
                    }

                    //Wipes the row and adds the new shifted row back in
                    wipeRow(row);
                    for(int col=0; col<cellsInRow.size(); col++) {
                        grid[row][col] = cellsInRow.get(col).getValue();
                    }
                }
            }


            placeRandomValue();


        }

        return canSlide;
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

        this.grid = new int[rows][columns];

        for(Cell cell : lastSave) {
            this.grid[cell.getRow()][cell.getColumn()] = cell.getValue();
        }
    }

    /**
     * Saves the current board values to lastSave ArrayList
     */
    private void saveBoard() {
        this.lastSave = new ArrayList<>();

        for(int row=0; row<this.rows; row++) {
            for(int col=0; col<this.columns; col++) {
                if(grid[row][col] != 0) {
                    lastSave.add(new Cell(row, col, grid[row][col]));
                }
            }
        }
    }

    private void wipeRow(int row) {
        //TODO check for num row too big, small, neg, etc
        for(int i=0; i<grid[row].length; i++) {
            grid[row][i] = 0;
        }
    }

    private void wipeCol(int col) {

    }

}
