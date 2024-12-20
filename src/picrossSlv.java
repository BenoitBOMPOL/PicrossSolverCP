import org.chocosolver.solver.*;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.search.strategy.Search;
import org.chocosolver.solver.search.strategy.selectors.variables.FirstFail;
import org.chocosolver.solver.variables.*;
import java.util.Arrays;

import static org.chocosolver.solver.search.strategy.Search.intVarSearch;
import static org.chocosolver.solver.search.strategy.Search.setVarSearch;

import java.io.FileWriter;
import java.io.IOException;

public class picrossSlv extends picross{
    private Solver solver;
    private String model_name;
    private final String chosen_constraints;

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
        chosen_constraints = active_constraints;
        model_name = filename.substring(8, filename.length() - 3) + "_" + active_constraints;
        System.out.println("Solving [" + model_name + "]");
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
            // System.out.println("Enabling constraints {a, b}.");
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
            // System.out.println("Enabling constraints {c, d}.");
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
            // System.out.println("Enabling constraints {e, f}.");
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
            // System.out.println("Enabling constraints {g, h}.");
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
            // System.out.println("Enabling constraints {i, j}.");
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
            // System.out.println("Enabling constraints {k, l, m, n}.");
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

    public boolean check_solution(int[][] sol, int[][] start_x, int[][] start_y){
        // precRC : Checking, in every row and every column if precedence between blocks is satisfied
        // precR
        for (int i = 0; i < getNbrows(); i++){
            int[] row_i_constraints = getRow_constraints(i);
            for (int k = 0; k < row_i_constraints.length - 1; k++){
                if (start_x[i][k + 1] < start_x[i][k] + row_i_constraints[k] + 1){
                    return false;
                }
            }
        }
        // precC
        for (int j = 0; j < getNbcols(); j++){
            int[] col_j_constraints = getCol_constraints(j);
            for (int l = 0; l < col_j_constraints.length - 1; l++){
                if (start_y[j][l + 1] < start_y[j][l] + col_j_constraints[l] + 1){
                    return false;
                }
            }
        }

        // fitRC : Checking if each start is within the row / column
        //         Also checks for the end of each bloc is w. row/col
        // fitR
        for (int i = 0; i < getNbrows(); i++){
            int[] row_i_constraints = getRow_constraints(i);
            for (int k = 0; k < row_i_constraints.length; k++){
                if ((start_x[i][k] > getNbcols()) || (start_x[i][k] + row_i_constraints[k] > getNbcols())){
                    return false;
                }
            }
        }
        // fitC
        for (int j = 0; j < getNbcols(); j++){
            int[] col_j_constraints = getCol_constraints(j);
            for (int l = 0; l < col_j_constraints.length; l++){
                if ((start_y[j][l] > getNbrows()) || (start_y[j][l] + col_j_constraints[l] > getNbrows())){
                    return false;
                }
            }
        }

        // fillRC : Enforces the link between grid[i][j] and startX, startY
        // fillR : startX <-> grid
        for (int i = 0; i < getNbrows(); i++){
            int[] row_i_constraints = getRow_constraints(i);
            for (int k = 0; k < row_i_constraints.length; k++){
                int r_ik = row_i_constraints[k];
                int sx_ik = start_x[i][k];
                for (int j = 0; j < r_ik; j++){
                    if (sol[i][sx_ik + j] == 0){
                        return false;
                    }
                }
            }   
        }
        // fillC : startY <-> grid
        for (int j = 0; j < getNbcols(); j++){
            int[] col_j_constraints = getCol_constraints(j);
            for (int l = 0; l < col_j_constraints.length; l++){
                int c_jl = col_j_constraints[l];
                int sy_jl = start_y[j][l];
                for (int i = 0; i < c_jl; i++){
                    if (sol[sy_jl + i][j] == 0){
                        return false;
                    }
                }
            }
        }

        // countRC : Enforces that exactly the right amount of cells have been checked
        // countR
        for (int i = 0; i < getNbrows(); i++){
            int[] row_i_constraints = getRow_constraints(i);
            int lhs_ric = 0;
            for (int r_ik : row_i_constraints){
                lhs_ric = lhs_ric + r_ik;
            }
            int rhs_ric = 0;
            for (int b_ij : sol[i]){
                rhs_ric = rhs_ric + b_ij;
            }
            if (lhs_ric != rhs_ric){
                return false;
            }
        }
        // countC
        for (int j = 0; j < getNbcols(); j++){
            int[] col_j_constraints = getCol_constraints(j);
            int lhs_cjc = 0;
            for (int c_jl : col_j_constraints){
                lhs_cjc = lhs_cjc + c_jl;
            }
            int rhs_cjc = 0;
            for (int i = 0; i < getNbrows(); i++){
                rhs_cjc = rhs_cjc + sol[i][j];
            }
            if (lhs_cjc != rhs_cjc){
                return false;
            }
        }
        return true;
    }

    public int[][] checkNextSolution(){
        solver = picrossModel.getSolver();
        solver.limitTime("60s");
        int[][] sol = null;
        int[][] start_x = null;
        int[][] start_y = null;
        if (solver.solve()){
            sol = new int[getNbrows()][getNbcols()];
            for (int i = 0; i < getNbrows(); i++){
                for (int j = 0; j < getNbcols(); j++){
                    sol[i][j] = grid[i][j].getValue();
                }
            }

            start_x = new int[getNbrows()][];
            for (int i = 0; i < getNbrows(); i++){
                int[] row_i_constraints = getRow_constraints(i);
                start_x[i] = new int[row_i_constraints.length];
                for (int k = 0; k < row_i_constraints.length; k++){
                    start_x[i][k] = startX[i][k].getValue();
                }
            }
            start_y = new int[getNbcols()][];
            for (int j = 0; j < getNbcols(); j++){
                int[] col_j_constraints = getCol_constraints(j);
                start_y[j] = new int[col_j_constraints.length];
                for (int l = 0; l < col_j_constraints.length; l++){
                    start_y[j][l] = startY[j][l].getValue();
                }
            }
            if (!check_solution(sol, start_x, start_y)){
                sol = null;
            }
            
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


    public void export_results(){
        int[][] sol = checkNextSolution();
        long[] outputs = new long[4];
        String[] categories = {"is_valid", "fail_count", "node_count", "solving_time"};
        if (sol != null){
            System.out.println("[" + model_name + "] - ✅");
            displaysol(sol);
            outputs[0] = 1;
        } else {
            System.out.println("[" + model_name + "] - ❌");
            outputs[0] = 0;
        }
        outputs[1] = solver.getFailCount();
        outputs[2] = solver.getNodeCount();
        outputs[3] = solver.getTimeToBestSolutionInNanoSeconds();

    StringBuilder content = new StringBuilder();
    content.append("{");
    for (int i = 0; i < categories.length; i++) {
        content.append("\"" + categories[i] + "\"").append(": ").append(outputs[i]);
        if (i < categories.length - 1){
            content.append(",\n");
        }
    }
    content.append("}");

    // Écrire le contenu dans un fichier texte
    try (FileWriter file = new FileWriter("outputs/" + model_name + ".json")) {
        file.write(content.toString());
        file.flush();
    } catch (IOException e) {
        e.printStackTrace();
    }

    }

    public static void main(String[] args) {
        String filename = args[0];
        String constraints = args[1];
        picrossSlv picross = null;
        try {
            picross = new picrossSlv(filename, constraints);
            // int[][] prop = picross.propagate();
            // picross.displaysol(prop);

            picross.export_results();
            // int[][] sol = picross.checkNextSolution();
            /* if (sol != null) {
                picross.displaysol(sol);
            } */

        } catch (Exception e) {
            System.out.println("[picrossSlv] Instance creation has failed");
            e.printStackTrace();
        }
    }
}
