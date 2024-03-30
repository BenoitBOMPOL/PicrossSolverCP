import org.chocosolver.solver.*;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.search.strategy.Search;
import org.chocosolver.solver.search.strategy.selectors.variables.FirstFail;
import org.chocosolver.solver.variables.*;
import java.util.Arrays;

import static org.chocosolver.solver.search.strategy.Search.intVarSearch;
import static org.chocosolver.solver.search.strategy.Search.setVarSearch;

public class picrossSlv extends picross{
    private final IntVar[][] grid;
    private IntVar[][] startX;
    private IntVar[][] startY;
    private final Model picrossModel;

    public IntVar[] get_jth_col(int j){
        IntVar[] jth_col = new IntVar[getNbrows()];
        for (int i = 0; i < getNbrows(); i++){jth_col[i] = grid[i][j];}
        return jth_col;
    }

    public picrossSlv(String filename) throws Exception {
        super(filename);

        this.grid = new IntVar[getNbrows()][getNbcols()];
        startX = new IntVar[getNbrows()][];
        startY = new IntVar[getNbcols()][];

        for (int i = 0; i < getNbrows(); i++) {startX[i] = new IntVar[getRow_constraints(i).length];}
        for (int j = 0; j < getNbcols(); j++) {startY[j] = new IntVar[getCol_constraints(j).length];}

        picrossModel = new Model("picrossModel");

        // NOTE : Variable creation
        // Boolean variables in grid
        for (int i = 0; i < getNbrows(); i++) {
            for (int j = 0; j < getNbcols(); j++) {
                grid[i][j] = picrossModel.intVar("x[" + i + "][" + j + "]", 0, 1);
            }
        }

        for (int i = 0; i < getNbrows(); i++) {
            for (int k = 0; k < getRow_constraints(i).length; k++) {
                startX[i][k] = picrossModel.intVar("sX[" + i + "][" + k + "]", 0, getNbcols() - 1);
            }
        }

        for (int j = 0; j < getNbcols(); j++) {
            for (int l = 0; l < getCol_constraints(j).length; l++) {
                startY[j][l] = picrossModel.intVar("sY[" + j + "][" + l + "]", 0, getNbrows() - 1);
            }
        }


        // On each row, the correct nb. of cells will be shaded
        for (int i = 0; i < getNbrows(); i++) {
            picrossModel.sum(grid[i], "=", Arrays.stream(getRow_constraints(i)).sum()).post();
        }

        // On each column, the correct nb. of cells will be shaded
        for (int j = 0; j < getNbcols(); j++) {
            picrossModel.sum(get_jth_col(j), "=", Arrays.stream(getCol_constraints(j)).sum()).post();
        }

        // sX[i][k + 1] - sX[i][k] >= rC(i)[k] + 1
        // sX[i][k + 1] >= sX[i][k] + rC(i)[k] + 1
        // The start of the next block in the row is set after the previous one
        for (int i = 0; i < getNbrows(); i++) {
            for (int k = 0; k < getRow_constraints(i).length - 1; k++) {
                picrossModel.arithm(startX[i][k + 1], "-", startX[i][k], ">=", getRow_constraints(i)[k] + 1).post();
            }
        }

        // sY[j][l + 1] - sY[j][l] >= cC(j)[l] + 1
        // sY[j][l + 1] >= sY[j][l] + cC(j)[l] + 1
        for (int j = 0; j < getNbcols(); j++) {
            for (int l = 0; l < getCol_constraints(j).length - 1; l++) {
                picrossModel.arithm(startY[j][l + 1], "-", startY[j][l], ">=", getCol_constraints(j)[l] + 1).post();
            }
        }

        // Cell (i, j) is lit iff there is a bloc in row i that has
        // either began at position j, or has began before j (but not "too" soon)
        for (int i = 0; i < getNbrows(); i++) {
            for (int j = 0; j < getNbcols(); j++) {
                BoolVar[] row_activ = picrossModel.boolVarArray("row_activ_" + i + "_" + j, getRow_constraints(i).length);
                for (int s = 0; s < getRow_constraints(i).length; s++) {
                    BoolVar condition1 = picrossModel.arithm(startX[i][s], "<=", j).reify();
                    BoolVar condition2 = picrossModel.arithm(startX[i][s], ">=", j - getRow_constraints(i)[s] + 1).reify();
                    row_activ[s] = picrossModel.and(condition1, condition2).reify();
                }
                picrossModel.or(row_activ).reifyWith(picrossModel.arithm(grid[i][j], "=", 1).reify());
            }
        }

        // Cell (i, j) is lit iff there is a bloc in column j that has
        // either began at position i, or has begun before i (but not "too" soon)
        for (int j = 0; j < getNbcols(); j++) {
            for (int i = 0; i < getNbrows(); i++) {
                BoolVar[] col_activ = picrossModel.boolVarArray("col_activ_" + j + "_" + i, getCol_constraints(j).length);
                for (int t = 0; t < getCol_constraints(j).length; t++) {
                    BoolVar condition3 = picrossModel.arithm(startY[j][t], "<=", i).reify();
                    BoolVar condition4 = picrossModel.arithm(startY[j][t], ">=", i - getCol_constraints(j)[t] + 1).reify();
                    col_activ[t] = picrossModel.and(condition3, condition4).reify();
                }
                picrossModel.or(col_activ).reifyWith(picrossModel.arithm(grid[i][j], "=", 1).reify());
            }
        }

        // The k-th bloc of row (i) cannot start "before" (k-1)-th before have been set up
        for (int i = 0; i < getNbrows(); i++) {
            for (int k = 0; k < getRow_constraints(i).length; k++) {
                int sum_proc_before = 0;
                for (int k_pr = 0; k_pr < k; k_pr++) {
                    sum_proc_before += getRow_constraints(i)[k_pr];
                }
                picrossModel.arithm(startX[i][k], ">=", sum_proc_before + k).post();
            }
        }

        // Same for columns
        for (int j = 0; j < getNbcols(); j++) {
            for (int l = 0; l < getCol_constraints(j).length; l++) {
                int sum_proc_before = 0;
                for (int l_pr = 0; l_pr < l; l_pr++) {
                    sum_proc_before += getCol_constraints(j)[l_pr];
                }
                picrossModel.arithm(startY[j][l], ">=", sum_proc_before + l).post();
            }
        }

        // The k-th bloc of row (i) cannot start too late
        for (int i = 0; i < getNbrows(); i++) {
            for (int k = 0; k < getRow_constraints(i).length; k++) {
                int sumrow = 0;
                for (int kpr = k; kpr < getRow_constraints(i).length; kpr++) {
                    sumrow += getRow_constraints(i)[kpr];
                }
                picrossModel.arithm(startX[i][k], "<=", getNbcols() - getRow_constraints(i).length + k + 1 - sumrow).post();
            }
        }

        // The l-th bloc of column (j) cannot start too late
        for (int j = 0; j < getNbcols(); j++) {
            for (int l = 0; l < getCol_constraints(j).length; l++) {
                int sumcol = 0;
                for (int lpr = l; lpr < getCol_constraints(j).length; lpr++) {
                    sumcol += getCol_constraints(j)[lpr];
                }
                picrossModel.arithm(startY[j][l], "<=", getNbrows() - getCol_constraints(j).length + l + 1 - sumcol).post();
            }
        }

        // If a bloc in a row starts at a given position
        //      1. I can put an unshaded cell before
        //      2. I can put an unshaded cell after

        for (int i = 0; i < getNbrows(); i++) {
            for (int ki = 0; ki < getRow_constraints(i).length; ki++) {
                for (int j = 0; j < getNbcols(); j++) {
                    if (j > 0) {
                        startX[i][ki].eq(j).imp(grid[i][j - 1].eq(0)).post();
                    }
                    if (j + getRow_constraints(i)[ki] < getNbcols()) {
                        startX[i][ki].eq(j).imp(grid[i][j + getRow_constraints(i)[ki]].eq(0)).post();
                    }
                }
            }
        }

        for (int i = 0; i < getNbrows(); i++){
            for (int ki = 0; ki < getRow_constraints(i).length; ki++){
                for (int j = 0; j < getNbcols(); j++){
                    // If startX[i][ki].eq(j) ==> each bloc in the proper must be == to 1
                    int d = 0;
                    while (d < getRow_constraints(i)[ki] && j+d < getNbcols()){
                        startX[i][ki].eq(j).imp(grid[i][j+d].eq(1)).post();
                        d++;
                    }
                }
            }
        }

        // Same for columns
        for (int j = 0; j < getNbcols(); j++) {
            for (int kj = 0; kj < getCol_constraints(j).length; kj++) {
                for (int i = 0; i < getNbrows(); i++) {
                    if (i > 0) {
                        startY[j][kj].eq(i).imp(grid[i - 1][j].eq(0)).post();
                    }
                    if (i + getCol_constraints(j)[kj] < getNbrows()) {
                        startY[j][kj].eq(i).imp(grid[i + getCol_constraints(j)[kj]][j].eq(0)).post();
                    }
                }
            }
        }

        // startX[i][s] = j ==> forall(0 <= d < rC(i)[s] | j+d < #C) grid[i][j + d] = 1
        // TODO :
        /*
                1. Construire le tableau des grid[i][j+d], forall(0 <= d < rC(i)[s] | j+d < #C)
                2. picrossModel.and(tableau1).reifyWith(picrossModel.arithm(grid[i][j], "=", 1).reify());
         */
    }

    public int[][] nextSolution(){
        Solver solver = picrossModel.getSolver();
        int[][] sol = null;
        if (solver.solve()){
            sol = new int[getNbrows()][getNbcols()];
            for (int i = 0; i < getNbrows(); i++){
                for (int j = 0; j < getNbcols(); j++){
                    sol[i][j] = grid[i][j].getValue();
                }
            }
            solver.printStatistics();
        } else if(solver.hasEndedUnexpectedly()){
            System.out.println("Le solveur s'est arrêté de manière inattendue.");
        }
        return sol;
    }

    public int[][] propagate() throws ContradictionException {
        Solver solver = picrossModel.getSolver();
        solver.propagate();
        int[][] vals = new int[getNbrows()][getNbcols()];
        for (int i = 0; i < getNbrows(); i++){
            for (int j = 0; j < getNbcols(); j++){
                if (grid[i][j].isInstantiated()){
                    vals[i][j] = grid[i][j].getValue();
                } else {
                    vals[i][j] = -1;
                }
            }
        }
        return vals;

    }
    public void displaysol(int[][] sol){
        if (sol == null){
            System.out.println("+++ null solution found. +++");
            return;
        }
        for (int i = 0; i < getNbrows(); i++){
            for (int j = 0; j < getNbcols(); j++){
                if (sol[i][j] == 1){
                    System.out.print("⬛"); // Case noire
                } else if (sol[i][j] == 0){
                    System.out.print("⬜"); // Case blanche
                } else {
                    System.out.print("\uD83D\uDFE5");
                }
            }
            System.out.println();
        }
    }

    public static void main(String[] args) {
        String filename = args[0];
        picrossSlv picross = null;
        try {
            picross = new picrossSlv(filename);
            int[][] prop = picross.propagate();
            picross.displaysol(prop);

            int[][] sol = picross.nextSolution();
            if (sol != null) {
                picross.displaysol(sol);
            } else {
                System.out.println("!!! Null solution found !!!");
            }
        } catch (Exception e) {
            System.out.println("[picrossSlv] Instance creation has failed");
            e.printStackTrace();
        }
    }
}
