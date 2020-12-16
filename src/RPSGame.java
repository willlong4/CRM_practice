/**
 This class provides a stimulation of two players playing rock paper scissors
 a million times. After each game, each player reevaluates their strategy using
 counterfactual regret minimization.  After enough games, each player's strategy
 should converge to their respective Nash Equilibrium strategies.
 */

import java.util.Arrays;

public class RPSGame {

    private RPSTrainer player1;
    private RPSTrainer player2;
    private double[][] NashStrategies;
    private static int NUM_ACTIONS;

    public RPSGame(double[] player1_strategy, double[] player2_strategy){
        player1 = new RPSTrainer(player2_strategy);
        player2 = new RPSTrainer(player1_strategy);

        NUM_ACTIONS = 3;
        NashStrategies = new double[2][NUM_ACTIONS];
    }

    /**
     * Have both players play RPS a given amount of times, each adjusting their
     * strategy each time they play
     * 1 - get regret-matched mixed strategy actions
     *      - compute strategy
     *      - get action
     * 2 - compute action utilities
     * 3 - accumulate action regrets
     * 4 - compute new strategy based on regret tables
     */
    public void play(int iterations){
        double[] actionUtility = new double[NUM_ACTIONS]; // define new array to hold action utilities

        // repeat desired number of times
        for(int k = 0; k < iterations; k++){

            // get strategies and actions for each player
            double[] strategy1 = player1.getStrategy();
            double[] strategy2 = player2.getStrategy();

            int action1 = player1.getAction(strategy1);
            int action2 = player2.getAction(strategy2);

            // compute action utilities from perspective of player1
            actionUtility[action2] = 0;
            actionUtility[action2 == NUM_ACTIONS - 1 ? 0 : action2 + 1] = 1;
            actionUtility[action2 == 0 ? NUM_ACTIONS - 1 : action2 - 1] = -1;

            // accumulate action regrets
            for(int i = 0; i < NUM_ACTIONS; i++){
                player1.regretSum[i] += actionUtility[i] - actionUtility[action1];
                player2.regretSum[i] += actionUtility[i] - actionUtility[action2];
            }
        }
    }

    /**
     * return the Nash Equilibrium strategies of both players
     */
    public double[][] getNashStrategies(){
        NashStrategies = new double[][]{player1.getAverageStrategy(), player2.getAverageStrategy()};
        return NashStrategies;
    }

    public static void main(String[] args){
        RPSGame game = new RPSGame(new double[]{0.6, 0.1, 0.3}, new double[]{0.2, 0.4, 0.4});
        game.play(1000000);
        double[][] nash_strategies = game.getNashStrategies();
        System.out.println("Player1's Nash Equilibrium strategy is: " +
                Arrays.toString(nash_strategies[0]));
        System.out.println("Player2's Nash Equilibrium strategy is: " +
                Arrays.toString(nash_strategies[1]));
    }

}
