import java.io.*;

public class picross {
    private int nbrows;    // Numbers of rows in the picross
    private int nbcols;    // Numbers of cols in the picross

    // row_constraints[i][k] represents the length of the k-th bloc of the i-th row.
    protected int[][] row_constraints;

    // col_constraint[j][k] represents the length of the k-th bloc of the j-th column.
    protected int[][] col_constraints;

    public int getNbcols() {
        return nbcols;
    }

    public int getNbrows() {
        return nbrows;
    }

    public int[] getCol_constraints(int j) {
        return col_constraints[j];
    }

    public int[] getRow_constraints(int i) {
        return row_constraints[i];
    }

    public picross(String filename) throws Exception{
        File pcross_file = new File(filename);
        BufferedReader bread = new BufferedReader(new FileReader(pcross_file));

        String chars;
        boolean definedims = false;
        int count_rowconstraint = 0;
        int count_colconstraint = 0;
        
        while ((chars = bread.readLine()) != null){
            if (!definedims){

                // Getting numbers of rows and lines in the first line of the file
                this.nbrows = Integer.parseInt(chars.split(",")[0]);
                this.nbcols = Integer.parseInt(chars.split(",")[1]);

                this.row_constraints = new int[nbrows][];
                this.col_constraints = new int[nbcols][];
                
                definedims = true;
            } else{
                // Getting each constraint
                String[] str_constraints = chars.split(",");
                if (count_rowconstraint < nbrows){
                    row_constraints[count_rowconstraint] = new int[str_constraints.length];
                    for (int i = 0; i < str_constraints.length; i++){
                        row_constraints[count_rowconstraint][i] = Integer.parseInt(str_constraints[i]);
                    }
                    count_rowconstraint += 1;
                } else {
                    col_constraints[count_colconstraint] = new int[str_constraints.length];
                    for (int i = 0; i < str_constraints.length; i++){
                        col_constraints[count_colconstraint][i] = Integer.parseInt(str_constraints[i]);
                    }
                    count_colconstraint += 1;
                }
            }
        }
        bread.close();
    }

    public static void main(String[] args) throws Exception {
        String filename = args[0];
        picross instance = new picross(filename);

    }
}
