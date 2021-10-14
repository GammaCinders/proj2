package proj2;

public class Cell implements Comparable<Cell> {
    protected int row, column, value;


    public Cell()
    {
        this(0,0,0);
    }
    public Cell (int r, int c, int v)
    {
        row = r;
        column = c;
        value = v;
    }

    /* must override equals to ensure List::contains() works
     * correctly
     */
    @Override
    public int compareTo (Cell other) {
        if (this.row < other.row) return -1;
        if (this.row > other.row) return +1;

        /* break the tie using column */
        if (this.column < other.column) return -1;
        if (this.column > other.column) return +1;

        return this.value - other.value;
    }

    /**
     * @return the row
     */
    public int getRow() {
        return row;
    }

    /**
     * @param row the row to set
     */
    public void setRow(int row) {
        this.row = row;
    }

    /**
     * @return the column
     */
    public int getColumn() {
        return column;
    }

    /**
     * @param column the column to set
     */
    public void setColumn(int column) {
        this.column = column;
    }

    /**
     * @return the value
     */
    public int getValue() {
        return value;
    }

    /**
     * @param value the value to set
     */
    public void setValue(int value) {
        this.value = value;
    }

    /**
     * Returns true if the value, row and col are the same
     * @param o Cell instance to compare to this one
     * @return Returns true if the value, row and col are the same, false otherwise
     */
    @Override
    public boolean equals(Object o) {
        if(o == null || !(o instanceof Cell)) {
            throw new IllegalArgumentException();
        } else {
            //true if value, row and col are equal
            if(this.getValue()==((Cell) o).getValue()) {
                if(this.getRow()==((Cell) o).getRow()) {
                    if(this.getColumn()==((Cell) o).getColumn()) {
                        return true;
                    }
                }
            }
            return false;
        }
    }

}
