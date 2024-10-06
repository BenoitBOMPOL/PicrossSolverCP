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
    // grid[i][j] is a boolean
    //      grid[i][j] = 1 iff the (i, j) cell has been blacked

    private IntVar[][] startX;
    // startX[r][p] is the position at which the p-th bloc of row r begins

    private IntVar[][] startY;
    // startY[c][q] is the position at which the q-th bloc of column c begins

    private final Model picrossModel;


    public IntVar[] get_jth_col(int j){
        IntVar[] jth_col = new IntVar[getNbrows()];
        for (int i = 0; i < getNbrows(); i++){jth_col[i] = grid[i][j];}
        return jth_col;
    }

    public picrossSlv(String filename, String active_constraints) throws Exception {
        // Reading the input file to get the dimensions and constraints
        super(filename);

        // Initialization of the variables arrays
        this.grid = new IntVar[getNbrows()][getNbcols()];
        startX = new IntVar[getNbrows()][];
        startY = new IntVar[getNbcols()][];

        for (int i = 0; i < getNbrows(); i++) {startX[i] = new IntVar[getRow_constraints(i).length];}
        for (int j = 0; j < getNbcols(); j++) {startY[j] = new IntVar[getCol_constraints(j).length];}

        picrossModel = new Model("picrossModel");

        // Creation of the variables themselves
        for (int i = 0; i < getNbrows(); i++) {
            for (int j = 0; j < getNbcols(); j++) {
                grid[i][j] = picrossModel.boolVar("x[" + i + "][" + j + "]");
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

        boolean is_ab_active = active_constraints.contains(Character.toString('A'));
        if (is_ab_active){
            System.out.println("Enabling constraints {a, b}.");
            // Set of constraints A = {a, b}
            // CONSTRAINT 1a :
            //          On each row, count how many cells have been checked
            for (int i = 0; i < getNbrows(); i++) {
                picrossModel.sum(grid[i], "=", Arrays.stream(getRow_constraints(i)).sum()).post();
            }

            // CONSTRAINT 1b :
            //          On each column, count how many cells have been checked
            for (int j = 0; j < getNbcols(); j++) {
                picrossModel.sum(get_jth_col(j), "=", Arrays.stream(getCol_constraints(j)).sum()).post();
            }
        }

        boolean is_cd_active = active_constraints.contains(Character.toString('B'));
        if (is_cd_active){
            System.out.println("Enabling constraints {c, d}.");
            // Set of constraints B = {c, d}
            // CONSTRAINT 1c :
            //          On each row, enforces that the (k + 1)-th bloc starts after the end of the k-th
            for (int i = 0; i < getNbrows(); i++) {
                for (int k = 0; k < getRow_constraints(i).length - 1; k++) {
                    picrossModel.arithm(startX[i][k + 1], "-", startX[i][k], ">=", getRow_constraints(i)[k] + 1).post();
                }
            }

            // CONSTRAINT 1d :
            //          On each column, enforces that the (l + 1)-th bloc starts after the end of the l-th
            for (int j = 0; j < getNbcols(); j++) {
                for (int l = 0; l < getCol_constraints(j).length - 1; l++) {
                    picrossModel.arithm(startY[j][l + 1], "-", startY[j][l], ">=", getCol_constraints(j)[l] + 1).post();
                }
            }
        }

        boolean is_ef_active = active_constraints.contains(Character.toString('C'));
        if (is_ef_active){
            System.out.println("Enabling constraints {e, f}.");
            // Set of constraints C = {e, f}
            // CONSTRAINT 1e :
            //          On each row, a cell is active iff there is a bloc that starts 
            //          neither too late, neither too *early*
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

            // CONSTRAINT 1f :
            //          On each column, a cell is active iff there is a bloc that starts 
            //          neither too late, neither too *early*
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
        }

        boolean is_gh_active = active_constraints.contains(Character.toString('D'));
        if (is_gh_active){
            System.out.println("Enabling constraints {g, h}.");
            // Set of constraints D = {g, h}
            // CONSTRAINT 1g :
            //          On each row, the k-th bloc starts after all the (k' < k) have been placed
            //          It includes the "blank space" between two consecutive blocs
            for (int i = 0; i < getNbrows(); i++) {
                for (int k = 0; k < getRow_constraints(i).length; k++) {
                    int sum_proc_before = 0;
                    for (int k_pr = 0; k_pr < k; k_pr++) {
                        sum_proc_before += getRow_constraints(i)[k_pr];
                    }
                    picrossModel.arithm(startX[i][k], ">=", sum_proc_before + k).post();
                }
            }

            // CONSTRAINT 1h :
            //          On each column, the l-th bloc starts after all the (l' < l) have been placed
            //          It includes the "blank space" between two consecutive blocs
            for (int j = 0; j < getNbcols(); j++) {
                for (int l = 0; l < getCol_constraints(j).length; l++) {
                    int sum_proc_before = 0;
                    for (int l_pr = 0; l_pr < l; l_pr++) {
                        sum_proc_before += getCol_constraints(j)[l_pr];
                    }
                    picrossModel.arithm(startY[j][l], ">=", sum_proc_before + l).post();
                }
            }
        }

        boolean is_ij_active = active_constraints.contains(Character.toString('E'));
        if (is_ij_active){
            System.out.println("Enabling constraints {i, j}.");
            // CONSTRAINT 1I :
            //          Limits, on each row, what's the biggest position at which the k-th bloc can start
            for (int i = 0; i < getNbrows(); i++) {
                for (int k = 0; k < getRow_constraints(i).length; k++) {
                    int sumrow = 0;
                    for (int kpr = k; kpr < getRow_constraints(i).length; kpr++) {
                        sumrow += getRow_constraints(i)[kpr];
                    }
                    picrossModel.arithm(startX[i][k], "<=", getNbcols() - getRow_constraints(i).length + k + 1 - sumrow).post();
                }
            }

            // CONSTRAINT 1J :
            //          Limits, on each column, what's the biggest position at which the l-th bloc can start

            for (int j = 0; j < getNbcols(); j++) {
                for (int l = 0; l < getCol_constraints(j).length; l++) {
                    int sumcol = 0;
                    for (int lpr = l; lpr < getCol_constraints(j).length; lpr++) {
                        sumcol += getCol_constraints(j)[lpr];
                    }
                    picrossModel.arithm(startY[j][l], "<=", getNbrows() - getCol_constraints(j).length + l + 1 - sumcol).post();
                }
            }
        }

        boolean is_klmn_active = active_constraints.contains(Character.toString('F'));
        if (is_klmn_active){
            System.out.println("Enabling constraints {k, l, m, n}.");
            // Set of constraints K, M, L, N
            for (int i = 0; i < getNbrows(); i++) {
                for (int ki = 0; ki < getRow_constraints(i).length; ki++) {
                    for (int j = 0; j < getNbcols(); j++) {
                        // CONSTRAINT 1K :
                        //      In a row, puts an un-checked cell exactly before the start of a bloc
                        if (j > 0) {
                            startX[i][ki].eq(j).imp(grid[i][j - 1].eq(0)).post();
                        }
                        // CONSTRAINT 1M :
                        //      In a column, puts an un-checked cell exactly after the end of a bloc 
                        if (j + getRow_constraints(i)[ki] < getNbcols()) {
                            startX[i][ki].eq(j).imp(grid[i][j + getRow_constraints(i)[ki]].eq(0)).post();
                        }
                    }
                }
            }

            for (int j = 0; j < getNbcols(); j++) {
                for (int kj = 0; kj < getCol_constraints(j).length; kj++) {
                    for (int i = 0; i < getNbrows(); i++) {
                        // CONSTRAINT 1L :
                        //      In a column, puts un un-checked cell exactly before the start of a bloc
                        if (i > 0) {
                            startY[j][kj].eq(i).imp(grid[i - 1][j].eq(0)).post();
                        }

                        // CONSTRAINT 1N :
                        //      In a column, puts un un-checked cell exactly after the end of a bloc
                        if (i + getCol_constraints(j)[kj] < getNbrows()) {
                            startY[j][kj].eq(i).imp(grid[i + getCol_constraints(j)[kj]][j].eq(0)).post();
                        }
                    }
                }
            }
        }
    }

    public int[][] nextSolution(){
        Solver solver = picrossModel.getSolver();
        solver.limitTime("60s");
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
        String constraints = args[1];
        picrossSlv picross = null;
        try {
            picross = new picrossSlv(filename, constraints);
            int[][] prop = picross.propagate();
            picross.displaysol(prop);

            int[][] sol = picross.nextSolution();
            if (sol != null) {
                picross.displaysol(sol);
            }
        } catch (Exception e) {
            System.out.println("[picrossSlv] Instance creation has failed");
            e.printStackTrace();
        }
    }
}
