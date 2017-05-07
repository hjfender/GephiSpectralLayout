package org.classes;

import org.ejml.data.DenseMatrix64F;
import org.ejml.ops.CommonOps;

import java.util.Random;

import static org.ejml.ops.CommonOps.*;

/**
 * Created by henry on 4/27/17.
 */
public class ComputeLayout {
    final double tolerance = 0.99999999;

    DenseMatrix64F A;
    DenseMatrix64F D;
    DenseMatrix64F invD;
    DenseMatrix64F PowItr;

    int number_of_nodes;

    Random ran = new Random();

    ComputeLayout(int[][] edgeList, int[] degreelist){
        /**
         * This is the constructer of the class. It serves to initialize important matrices for later computation.
         **/

        this.number_of_nodes = degreelist.length;
        //Initialize and set the value of the adjacency matrix
        A = new DenseMatrix64F(number_of_nodes,number_of_nodes);
        for(int[] edge : edgeList){
            A.set(edge[0],edge[1],1.0);
            A.set(edge[1],edge[0],1.0);
        }

        //Initialize and set the value of the Degree Matrix
        D = new DenseMatrix64F(number_of_nodes,number_of_nodes);
        for(int i = 0; i<number_of_nodes; i++){
            D.set(i,i,degreelist[i]);
        }

        //Initialize and set the value of the Inverse Degree Matrix
        invD = new DenseMatrix64F(number_of_nodes,number_of_nodes);
        invert(D,invD);

        //Initialize and set the value of the Power Iteration Matrix 1/2(I+D^-1*A)
        PowItr = identity(number_of_nodes);
        multAdd(invD,A,PowItr);
        scale(0.5,PowItr);
    }

    float[][] computeEigenvectors(int p){
        /**
         * This method computes the eigenvectors of our graph related matrix as per Y. Koren's degree normalized
         * spectral drawing algorithm. We use parts of them as node coordinates in the layout of our graph.
         **/

        //initialize the the variable to be returned
        DenseMatrix64F[] eigenvectors = new DenseMatrix64F[p];
        //add the first eigenvector which is always the same for the matrices we are considering
        eigenvectors[0] = normalize(onesVector());

        //initialize a placeholder variable for EJML to use in the do while loop condition
        DenseMatrix64F multiplication_output = new DenseMatrix64F(1,1);

        for(int k = 2; k<=p; k++){
            //initialize the eigenvector
            DenseMatrix64F uk;
            //randomly initialize a vector and normalize it
            DenseMatrix64F u_k = randomVector();
            u_k = normalize(u_k);

            //the following prevents the algorithm from running too long
            int loop_count = 0;

            do{
                uk = u_k.copy();
                /*
                * the follwoing for loop D-orthogonalizes uk
                * do not stress about the length of the code
                * it is just an awkward artifact of computing linear algebra in Java
                * */
                for(int l=0;l<k-1;l++) {
                    //get the eigenvector we want to orthogonalize against
                    DenseMatrix64F ul = eigenvectors[l].copy();

                    //initialize vectors for use in subsequent computations
                    DenseMatrix64F ulT = new DenseMatrix64F(1, number_of_nodes);
                    DenseMatrix64F ukT = new DenseMatrix64F(1, number_of_nodes);
                    transpose(ul, ulT);
                    transpose(uk, ukT);

                    //compute ukT D ul, i.e. numerator of ratio
                    DenseMatrix64F numerator = new DenseMatrix64F(1, 1);
                    DenseMatrix64F partial_numerator_calc = new DenseMatrix64F(number_of_nodes, 1);
                    mult(D, ul, partial_numerator_calc);
                    mult(ukT, partial_numerator_calc, numerator);

                    //compute ulT D uk, i.e. denominator of ration
                    DenseMatrix64F denominator = new DenseMatrix64F(1, 1);
                    DenseMatrix64F partial_denominator_calc = new DenseMatrix64F(number_of_nodes, 1);
                    mult(D, ul, partial_denominator_calc);
                    mult(ulT, partial_denominator_calc, denominator);

                    //compute the ratio of how similar uk is to ul
                    double scalar = numerator.get(0, 0) / denominator.get(0, 0);

                    //make sure we are taking out the similarity
                    scale(-1 * scalar, ul);

                    //make uk orthogonal to ul by taking out the amount by which they are similar
                    add(uk, ul, uk);

                }

                /*
                * multiply with 1/2(I+D^-1*A)
                * THIS IS POWER ITERATION
                * it forces the random vector to converge to an eigenvector
                */
                mult(PowItr,uk,u_k);

                //normalize to maintain orthogonality
                u_k = normalize(u_k);

                //check to see if much has changed - 1 means no change
                multTransA(u_k,uk,multiplication_output);

                //make sure the loop hasn't run too long
                loop_count++;

                //check to see if the vector should converge more
            } while(multiplication_output.get(0,0) < tolerance && loop_count < 1000);

            //add the eigenvector to the return data structure
            eigenvectors[k-1] = u_k.copy();
        }

//        for(DenseMatrix64F eigenvector1 : eigenvectors){
//            for(DenseMatrix64F eigenvector2 : eigenvectors){
//                DenseMatrix64F result = new DenseMatrix64F(1,1);
//                DenseMatrix64F transpose = new DenseMatrix64F(1,number_of_nodes);
//                transpose(eigenvector1, transpose);
//                mult(transpose,eigenvector2,result);
//                System.out.print(result.get(0,0)+" ");
//            }
//            System.out.println();
//        }

        //Convert from EJML to a Java array of arrays for use in the layout algorithm
        float[][] eigenresult = new float[number_of_nodes][p];

        for(int i = 0; i < eigenvectors.length; i++){
            for(int j = 0; j<eigenvectors[i].getNumRows(); j++){
                eigenresult[j][i]= (float) eigenvectors[i].get(j,0);
            }
        }
        return eigenresult;
    }

    DenseMatrix64F normalize(DenseMatrix64F vector){
        /**
        * This method normalized an nx1 vector
        **/
        double length = 0;
        for(int i = 0; i<number_of_nodes; i++){
            length += Math.pow(vector.get(i,0),2);
        }
        length = Math.sqrt(length);
        for(int i = 0; i<number_of_nodes; i++){
            vector.set(i,0,(vector.get(i,0)/length));
        }
        return vector;
    }

    DenseMatrix64F randomVector(){
        /**
         * This method creates a random nx1 vector
         **/
        DenseMatrix64F vector = new DenseMatrix64F(number_of_nodes,1);
        for(int i = 0; i<number_of_nodes; i++){
            vector.add(i,0,ran.nextDouble());
        }
        return vector;
    }

    DenseMatrix64F onesVector(){
        /**
         * This method creates a nx1 vector filled with ones
         **/
        DenseMatrix64F vector = new DenseMatrix64F(number_of_nodes,1);
        for(int i=0;i<number_of_nodes;i++){
            vector.set(i,0,1.0);
        }
        return vector;
    }

}
