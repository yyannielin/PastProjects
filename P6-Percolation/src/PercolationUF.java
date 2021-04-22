import java.util.Arrays;

public class PercolationUF implements IPercolate {
    private IUnionFind myFinder;
    private boolean[][] myGrid;
    private final int VTOP;
    private final int VBOTTOM;
    private int myOpenCount;

    public PercolationUF(IUnionFind finder, int size) {

        myGrid = new boolean[size][size];

        for (boolean[] row : myGrid) {
            Arrays.fill(row, false);
        }

        finder.initialize(size * size + 2);
        myFinder = finder;

        VTOP = size * size;
        VBOTTOM = size * size + 1;

        myOpenCount = 0;
    }

    /**
     * Returns true if and only if site (row, col) is OPEN
     *
     * @param row row index in range [0,N-1]
     * @param col
     */
    @Override
    public boolean isOpen(int row, int col) {
        if (! inBounds(row,col)) {
            throw new IndexOutOfBoundsException(
                    String.format("(%d,%d) not in bounds", row,col));
        }
        return myGrid[row][col];
    }

    /**
     * Returns true if and only if site (row, col) is FULL
     *
     * @param row row index in range [0,N-1]
     * @param col
     */
    @Override
    public boolean isFull(int row, int col) {
        if (! inBounds(row,col)) {
            throw new IndexOutOfBoundsException(
                    String.format("(%d,%d) not in bounds", row,col));
        }
        int set = row*myGrid.length + col;
        return myFinder.connected(set,VTOP); //is recursion needed here?
    }

    /**
     * Returns true if the simulated percolation actually percolates. What it
     * means to percolate could depend on the system being simulated, but
     * returning true typically means there's a connected path from
     * top-to-bottom.
     *
     * @return true iff the simulated system percolates
     */
    @Override
    public boolean percolates() {
        if (myFinder.connected(VTOP,VBOTTOM))
            return true;
        return false;
    }

    /**
     * Returns the number of distinct sites that have been opened in this
     * simulation
     *
     * @return number of open sites
     */
    @Override
    public int numberOfOpenSites() {
        return myOpenCount;
    }

    /**
    * Open site (row, col) if it is not already open. By convention, (0, 0)
    * is the upper-left site
    * <p>
    * The method modifies internal state so that determining if percolation
    * occurs could change after taking a step in the simulation.
    *
    * @param row row index in range [0,N-1]
    * @param col
    */
    @Override
    public void open(int row, int col) {
        if (! inBounds(row,col)) {
            throw new IndexOutOfBoundsException(
                    String.format("(%d,%d) not in bounds", row,col));
        }

        if (myGrid[row][col])
            return;
        myOpenCount += 1;
        myGrid[row][col] = true;
        updateOnOpen(row,col);
    }

    protected void updateOnOpen(int row, int col) {

        int set = row*myGrid.length + col;

        if (row==0){
            myFinder.union(set,VTOP);
        }

        if (row==myGrid.length-1){
            myFinder.union(set,VBOTTOM);
        }

        if (myGrid[row][col]) {
            int[] rowDelta = {-1, 1, 0, 0};
            int[] colDelta = {0, 0, -1, 1};

            int[] p = new int[]{row, col};

            for (int k = 0; k < rowDelta.length; k++) {
                row = p[0] + rowDelta[k];
                col = p[1] + colDelta[k];

                if (inBounds(row, col) && myGrid[row][col]){
                    int setNeighbor = row*myGrid.length + col;
                    myFinder.union(setNeighbor,set);
                }
            }
        }
    }

    protected boolean inBounds(int row, int col) {
        if (row < 0 || row >= myGrid.length) return false;
        if (col < 0 || col >= myGrid[0].length) return false;
        return true;
    }
}
