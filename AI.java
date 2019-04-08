package uk.ac.qub.eeecs.game.cardDemo;


import java.util.ArrayList;

import uk.ac.qub.eeecs.gage.world.GameScreen;
import uk.ac.qub.eeecs.gage.engine.ElapsedTime;
import uk.ac.qub.eeecs.gage.engine.graphics.IGraphics2D;

/*
 * @author Eimear
 * @author Laura
 */

//https://www.geeksforgeeks.org/perfect-sum-problem-print-subsets-given-sum/
//Reference for getAllSubsets method

public class AI {


    private ArrayList<Grid> aiGrid, fieldHandGrid;

    //Hands
    public Hand aiHand, fieldHand;
    public Hand aiStrongestHand;

    //Cards
    private Card aiStrongestCard;
    Card aiDragCard;

    //Boolean value used in get subsets method
    private boolean[][] dp;

    //Stores all possible options
    ArrayList<Hand> possibleOptions = new ArrayList<Hand>();

    //scores
    int maxScore = 0;
    int sevenOfHearts = 50;
    int otherSeven = 10;
    int heart = 5;
    int scuab = 100;
    int aiScuabPoints;


    //Constructor (Values of grids never change)
    public AI(ArrayList<Grid> aiGrid, ArrayList<Grid> fieldHandGrid) {
        this.aiGrid = aiGrid;
        this.fieldHandGrid = fieldHandGrid;

    }


    //Sets up the AI with the current ai hand and field hand at the beginning of each AI turn
    public void setUpAI(Hand aiHand, Hand fieldHand) {
        this.aiHand = aiHand;
        this.fieldHand = fieldHand;
        aiStrongestCard = null;
        aiStrongestHand = null;
        this.possibleOptions.clear(); //clears possible options for next turn
        for(Card c: aiHand.getCards()){
            c.scoreOfCard=0;
        }

    }


    //gets best option for aiHand turn
    public Hand getBestOption(ArrayList<Hand> possibleOptions) {

        //if cards are in the field hand
        if (possibleOptions.size() > 0) {
            for (Hand h : possibleOptions) {

                //Best option 1: scuab (awarded 100 points)
                checkForScuabCard(h);
                if (h.scuabHand == true) {
                    h.scoreOfHand += scuab;
                    aiScuabPoints++;
                    h.scuabHand = true; //Used to display animation in the event of a scuab ~ Sarah Rose and Cliona
                }

                //Best option 2: hand contains 7 hearts (awarded 50 points)
                if (h.has7Hearts()) {
                    h.scoreOfHand += sevenOfHearts;
                }

                //Best option 3: hand contains 7s (awarded 10 points for each)
                if (h.getNumOf7s() != 0) {
                    for (Card c : h.getCards()) {
                        if (c.getCardValue() == 7) {
                            h.scoreOfHand += otherSeven;
                        }
                    }
                }

                //Best option 4: hand contains hearts (awarded 5 points for each)
                if (h.getNumOfHearts() != 0) {
                    for (Card c : h.getCards()) {
                        if (c.getSuit() == "Hearts") {
                            h.scoreOfHand += heart;
                        }
                    }
                }
                //Best option 5: hand contains the most cards (awarded a point for each card in the hand)
                h.scoreOfHand += h.getCards().size();
            }

            //setting initial value for maxScore and aiStrongestHand
            maxScore = possibleOptions.get(0).getScoreOfHand();
            aiStrongestHand = possibleOptions.get(0);

            //Compares the score of each hand in the possible options and gets the hand with the highest score
            for (Hand h : possibleOptions) {
                if (h.getScoreOfHand() > maxScore) {
                    maxScore = h.getScoreOfHand();
                    aiStrongestHand = h;

                }

            }

            return aiStrongestHand;

        }

        //if no cards are in the field hand, pick the least valuable card to put in the field
        else {
            aiStrongestHand = noOptionCard();
            for (Card c : aiStrongestHand.getCards()) {
                c.setSelectedToTrue();
            }
            return aiStrongestHand;

        }
    }


    //method to get the best option of the possible options
    public Hand getStrongestHand() {

        return getBestOption(possibleOptions);
    }


    //Method to pick the least valuable card in the ai hand when they can't make 15
    Hand noOptionCard() {

        int fieldTotal = 0;

        for (Card c : fieldHand.getCards()) {
            fieldTotal += c.getCardValue();
        }

        //Setting initial values for maxVal and bestCard
        int maxVal = 0;
        Card bestCard = aiHand.getCards().get(0);

        //Ensures that a 7 and a heart are not picked unless they're the only option
        //If the field hand total is less than 5 the AI will try to keep it less than 5 so the player can't get a scuab
        //If the field hand total is less than 15 the AI will try to put it over 15 so the player can't get a scuab

        for (Card c : aiHand.getCards()) {

            if (c.getCardValue() + fieldTotal < 5) {
                c.scoreOfCard += 5;
            }
            if (c.getCardValue() + fieldTotal > 15 && c.getCardValue() != 7 && !(c.getSuit().equalsIgnoreCase("Hearts"))) {
                c.scoreOfCard += 5;
            }
            if (c.getCardValue() == 7 && c.getSuit().equalsIgnoreCase("Hearts")) {
                c.scoreOfCard += 1;
            }
            if (c.getCardValue() == 7) {
                c.scoreOfCard += 2;
            }
            if (c.getSuit().equalsIgnoreCase("Hearts")) {
                c.scoreOfCard += 3;
            }

                c.scoreOfCard += 4;


        }

        //The "bestCard" is the card that is most appropriate for the AI to play
        for (Card c : aiHand.getCards()) {
            if (c.scoreOfCard > maxVal) {
                maxVal = c.scoreOfCard;
                bestCard = c;

            }
        }

        //Add this card to a hand and return it
        Hand h = new Hand();
        bestCard.wasAICard = true;
        h.addCardToHand(bestCard);

        return h;

    }


    //checks if ai can make a scuab (clears the field hand by making 15)
    void checkForScuabCard(Hand h) {

        if (h.getCards().size() == (fieldHand.getCards().size() + 1)) {
            h.scuabHand = true;

        } else
            h.scuabHand = false;

    }


    public void addToPossibleOptions(ArrayList<Card> cards, Card aiCard) {
        Hand tempHand = new Hand();
        aiCard.setWasAICard(true);
        tempHand.addCardToHand(aiCard);

        for (Card c : cards) {

            tempHand.addCardToHand(c);
        }
        possibleOptions.add(tempHand);

    }

    //Gets all possible subsets of 15 using each ONE ai card and any number of cards in the field
    public void getSubsets(Hand fieldHand, int i, int sum, ArrayList<Card> currentSubset, Card aiCard) {

        // If we reached end and sum is non-zero. We print dp[] only if arr[0] is equal to sun OR dp[0][sum]
        // is true.
        if (i == 0 && sum != 0 && dp[0][sum]) {
            currentSubset.add(i, fieldHand.getCards().get(i));

            addToPossibleOptions(currentSubset, aiCard);

            currentSubset.clear();

            return;
        }

        // If sum becomes 0
        if (i == 0 && sum == 0) {
            addToPossibleOptions(currentSubset, aiCard);
            currentSubset.clear();
            return;
        }

        // If given sum can be achieved after ignoring
        // current element.
        if (dp[i - 1][sum]) {
            // Create a new vector to store path
            ArrayList<Card> b = new ArrayList<>();

            b.addAll(currentSubset);


            getSubsets(fieldHand, i - 1, sum, b, aiCard);
        }

        // If given sum can be achieved after considering
        // current element.
        if (sum >= fieldHand.getCards().get(i).getCardValue() && dp[i - 1][sum - fieldHand.getCards().get(i).getCardValue()]) {
            currentSubset.add(fieldHand.getCards().get(i));

            getSubsets(fieldHand, i - 1, sum - fieldHand.getCards().get(i).getCardValue(), currentSubset, aiCard);
        }
    }

    // gets all subsets
    public void getAllSubsets(Hand fieldHand, int n, int sum, Card aiCard) {
        if (n == 0 || sum < 0)
            return;

        // Sum 0 can always be achieved with 0 elements
        dp = new boolean[n][sum + 1];
        for (int i = 0; i < n; ++i) {
            dp[i][0] = true;
        }

        // Sum arr[0] can be achieved with single element
        if (fieldHand.getCards().get(0).getCardValue() <= sum)
            dp[0][fieldHand.getCards().get(0).getCardValue()] = true;

        // Fill rest of the entries in dp[][]
        for (int i = 1; i < n; ++i)
            for (int j = 0; j < sum + 1; ++j)
                dp[i][j] = (fieldHand.getCards().get(i).getCardValue() <= j) ? (dp[i - 1][j] ||
                        dp[i - 1][j - fieldHand.getCards().get(i).getCardValue()])
                        : dp[i - 1][j];
        if (dp[n - 1][sum] == false) {
            System.out.println("There are no subsets with sum " + sum);
            return;
        }

        // Now recursively traverse dp[][] to find all paths from dp[n-1][sum]
        ArrayList<Card> p = new ArrayList<>();

        getSubsets(fieldHand, n - 1, sum, p, aiCard);
    }
}









