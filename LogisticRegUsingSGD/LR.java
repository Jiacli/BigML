/**
 * 
 */

/**
 * @author JK
 *
 */
public class LR {

    /**
     * @param args
     */
    public static void main(String[] args) {
        if (args.length != 6) {
            System.err.println("Error: invalid arguments!");
            System.exit(-1);
        }
        
        // load parameters
        int V = Integer.parseInt(args[0]);        // vocabulary size
        float lambda = Float.parseFloat(args[1]); // learning rate
        float mu = Float.parseFloat(args[2]);     // regularization coefficient
        int maxIter = Integer.parseInt(args[3]);  // maximum iteration
        int sizeT = Integer.parseInt(args[4]);    // size of trianing dataset
        String testset = args[5];
        
        // training
        
        

    }

}
