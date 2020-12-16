/**
 Provides an implementation of counterfactual regret
 minimization on the game of rock paper scissors as described in
 An Introduction to Counterfactual Regret Minimization (Neller, Lanctot)
 */
import java.util.Arrays;
import java.util.Random;

public class RPSTrainer{

    // define constants and variables used throughout the processes of regret minimization
    private static int ROCK;
    private static int PAPER;
    private static int SCISSORS;
    private static int NUM_ACTIONS;
    private static Random random;
    public double[] regretSum;     // sum of regrets of each action (rock, paper, or scissors)
    private double[] strategy;      // strategy generated through regret matching
    private double[] strategySum;   // sum of all such strategies generated
    private double[] oppStrategy;   // opponent's strategy

    public RPSTrainer(double[] opponent_strategy){
        ROCK = 0;
        PAPER = 1;
        SCISSORS = 2;
        NUM_ACTIONS = 3;
        random = new Random();

        oppStrategy = opponent_strategy;
        regretSum = new double[NUM_ACTIONS];
        strategy = new double[NUM_ACTIONS];
        strategySum = new double[NUM_ACTIONS];

    }


    /**
     Get a mixed strategy through regret matching:
     1 - copy all positive regrets and sum them
     2 - loop through strategy entries
     -   if there exists at least one positive regret,
     normalize regrets by normalizing sum of positive regrets
     -   else, each strategy is chosen at an equal frequency
     */
    public double[] getStrategy(){
        double normal_sum = 0; // define normalizing sum

        // loop through regret sums and add up positive ones
        for(int k = 0; k < NUM_ACTIONS; k++){
            strategy[k] = regretSum[k] > 0 ? regretSum[k] : 0;  // if regretSum > 0, strategy = regretSum
            normal_sum += strategy[k];                          // add strategy to normalizing sum
        }

        //normalize sum of positive regrets if there exists a positive regret
        for(int k = 0; k< NUM_ACTIONS; k++){
            if (normal_sum > 0) strategy[k] /= normal_sum;  // divide by normalizing sum if its greater than 0
            else strategy[k] = 1.0 / NUM_ACTIONS;           // else give each strategy equal frequency
            strategySum[k] += strategy[k];                  // add frequencies to cummulative sum of frequencies (for each action)
        }
        return strategy;
    }

    /**
     Choose an action acording to mixed strategy produced by regret mathcing:
     1 - choose a random number between 0 and 1
     2 - at this point, one must examine the probability that an
     action with strategy array index < k is choosen
     - this equals the cummulative probability of all actions up to index k
     3 - if the random number is less than the "cummulative probability",
     choose the last index that was added to the cummulative probability
     */
    public int getAction(double[] strategy){
        double r = random.nextDouble();
        double cumm_probability = 0;

        // loop through strategies to cummulative probability
        // that is greater than the generated number
        for(int k = 0; k < NUM_ACTIONS - 1; k ++){
            cumm_probability += strategy[k];
            if(r < cumm_probability) return k;
        }
        return NUM_ACTIONS - 1;

    }

    /**
     Train Rock Paper Scissors agent according to fixed opponent strategy,
     the resulting strategy should converge to the correlated equilibrium
     response to opponent's strategy

     repeat desired number of times:
     1 - get regret-matched mixed strategy actions
     - compute strategy
     - get action
     2 - compute action utilities
     3 - accumulate action regrets
     */
    public void train(int iterations){
        double[] actionUtility = new double[NUM_ACTIONS]; // dfine new array to hold action utilities

        // repeat desired number of times
        for(int k = 0; k < iterations; k++){

            // get regret-matched mixed strategy
            double[] strategy = getStrategy();
            int myAction = getAction(strategy);
            int otherAction = getAction(oppStrategy);

            // compute action utilities
            actionUtility[otherAction] = 0;
            actionUtility[otherAction == NUM_ACTIONS - 1 ? 0 : otherAction + 1] = 1;
            actionUtility[otherAction == 0 ? NUM_ACTIONS - 1 : otherAction - 1] = -1;

            // accumulate action regrets
            for(int i = 0; i < NUM_ACTIONS; i++){
                regretSum[i] += actionUtility[i] - actionUtility[myAction];
            }
        }
    }


    /**
     Get average mixed strategy across all training iterations:
     - use sum of strategies computed over training iterations
     to choose an average strategy
     */
    public double[] getAverageStrategy(){
        double[] avgStrategy = new double[NUM_ACTIONS];
        double normalizingSum = 0;

        for(int k = 0; k < NUM_ACTIONS; k++){
            normalizingSum += strategySum[k];
        }
        for(int k = 0; k < NUM_ACTIONS; k++){
            if(normalizingSum > 0) avgStrategy[k] = strategySum[k] / normalizingSum;
            else avgStrategy[k] = 1.0 / NUM_ACTIONS;
        }
        return avgStrategy;
    }

    public static void main(String[] args){
        RPSTrainer agent = new RPSTrainer(new double[]{0.4, 0.3, 0.3});
        agent.train(1000000);
        System.out.println(Arrays.toString(agent.getAverageStrategy()));
    }

}


