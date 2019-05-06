package uk.ac.qub.eeecs.game.cardDemo;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;

import java.util.ArrayList;
import java.util.List;

import uk.ac.qub.eeecs.gage.Game;
import uk.ac.qub.eeecs.gage.engine.AssetManager;
import uk.ac.qub.eeecs.gage.engine.ElapsedTime;
import uk.ac.qub.eeecs.gage.engine.audio.AudioManager;
import uk.ac.qub.eeecs.gage.engine.graphics.IGraphics2D;
import uk.ac.qub.eeecs.gage.engine.input.Input;
import uk.ac.qub.eeecs.gage.engine.input.TouchEvent;
import uk.ac.qub.eeecs.gage.ui.PushButton;
import uk.ac.qub.eeecs.gage.world.GameScreen;
import uk.ac.qub.eeecs.gage.world.LayerViewport;

/**
 * Certain elements of code have been inspired by TOKEN (Past project)
 *
 * @version 1.0
 * @author Laura Walsh
 * @author  Eimear Murphy
 */
public class CardDemoScreen extends GameScreen {

    //Buttons

    private PushButton pauseGameButton;
    private PushButton mBackButton;
    private PushButton mSubmitButton;


    AssetManager assetManager = mGame.getAssetManager();
    private final float LEVEL_WIDTH = mGame.getScreenWidth() / 10;
    private final float LEVEL_HEIGHT = mGame.getScreenHeight() / 10;
    private LayerViewport mCardLayerViewport;
    private Rect boardBackground = new Rect();


    public Card draggedCard; //the card that the player has dragged from their hand to the field
    public ArrayList<Card> playerSelectedCards = new ArrayList<Card>(); //array list of cards to temporarily store the player's choice of cards
    private boolean playerHasDraggedCard = false; //boolean to ensure player can't drag more than one of their own cards

    //Card Decks
    private CardDeck originalDeck = new CardDeck(this);
    private CardDeck shuffledD = new CardDeck(this);


    //Player Grids
    private Grid playerHandGrid1, playerHandGrid2, playerHandGrid3;//set up the hand grid locations
    private ArrayList<Grid> playerHandGrid;    //add the player hand locations to array


    //Field Grids
    private Grid fieldHandGrid1, fieldHandGrid2, fieldHandGrid3, fieldHandGrid4, fieldHandGrid5, fieldHandGrid6, fieldHandGrid7;//set up the field grids
    private ArrayList<Grid> fieldHandGrid;//set up deck grid with all of the remaining cards


    //AI Grids
    private Grid aiHandGrid1, aiHandGrid2, aiHandGrid3; //set up the ai hand grid locations
    private ArrayList<Grid> aiHandGrid;//add the ai hand locations to array

    //shuffled deck grid for the remaining cards
    private Grid shuffledDeckGrid;



    //Hands
    Hand playerHand;
    Hand aiHand;
    Hand fieldHand;
    private Hand playerWinningsHand = new Hand();
    private Hand aiWinningsHand = new Hand();
    private Hand last15Made; //used to identify the last hand that makes 15 for the end of game


    //stores scuab points for each player
    private int playerScuabPoints=0;
    private int aiScuabPoints = 0;


    //AI variables used on ai turn
    private AI ai;
    Hand aiStrongestHand;
    private Card aiStrongestCard;

    //Turn
    private Turn turn; //sets up turn for the game
    private enum SelectTurn {PLAYER_TURN, AI_TURN, AI_DRAG_CARD, AI_HIGHLIGHT_CARD, AI_MOVE_CARDS};
    SelectTurn selectTurn = SelectTurn.PLAYER_TURN;


    //Constructor

    public CardDemoScreen(Game game) {
        super("CardScreen", game);

        AssetManager assetManager = mGame.getAssetManager();

        assetManager.loadAndAddMusic("OptionsMusic", "sound/laMer.mp3");
        assetManager.loadAndAddSound("CardMoveSound", "sound/CardMoveSound.mp3");
        assetManager.loadAndAddBitmap("PauseButton", "img/yellowPause.png");
        assetManager.loadAndAddBitmap("BackArrow", "img/BackArrow.png");
        assetManager.loadAndAddBitmap("BackArrowSelected", "img/BackArrowSelected.png");
        assetManager.loadAndAddFont("SettingsFont", "font/SettingsFont.ttf");
        assetManager.loadAndAddBitmap("sBackground", "img/sBackground.png");

        //Buttons
        mBackButton = new PushButton(
                mDefaultLayerViewport.getWidth() * 0.95f, mDefaultLayerViewport.getHeight() * 0.10f,
                mDefaultLayerViewport.getWidth() * 0.075f, mDefaultLayerViewport.getHeight() * 0.10f,
                "BackArrow", "BackArrowSelected", this);

        mBackButton.setPlaySounds(true, true);

        mSubmitButton = new PushButton(
                -10, -20,
                mDefaultLayerViewport.getWidth() * 0.1f, mDefaultLayerViewport.getHeight() * 0.2f,
                "submitButton", "submitButton", this);

        pauseGameButton = new PushButton(mDefaultLayerViewport.getWidth() * 0.94f, mDefaultLayerViewport.getHeight() * 0.85f,
                mDefaultLayerViewport.getWidth() * 0.1f, mDefaultLayerViewport.getHeight() * 0.15f,
                "PauseButton", "PauseButton", this);



        //Shuffling the cards and setting up the turn to 0 for starting
        setUpDeck();
        turn = new Turn(0);


        //Creating grids
        shuffledDeckGrid = new Grid(LEVEL_WIDTH * 1.25f, LEVEL_HEIGHT * 8.85f, LEVEL_WIDTH * 1.5f, LEVEL_HEIGHT * 2.5f, assetManager.getBitmap("CardGrid"), this);

        playerHandGrid1 = new Grid(LEVEL_WIDTH * 3.7f, LEVEL_HEIGHT * 8.85f, LEVEL_WIDTH * 1.5f, LEVEL_HEIGHT * 2.5f, assetManager.getBitmap("CardGrid"), this);
        playerHandGrid2 = new Grid(LEVEL_WIDTH * 5.15f, LEVEL_HEIGHT * 8.85f, LEVEL_WIDTH * 1.5f, LEVEL_HEIGHT * 2.5f, assetManager.getBitmap("CardGrid"), this);
        playerHandGrid3 = new Grid(LEVEL_WIDTH * 6.6f, LEVEL_HEIGHT * 8.85f, LEVEL_WIDTH * 1.5f, LEVEL_HEIGHT * 2.5f, assetManager.getBitmap("CardGrid"), this);
        playerHandGrid = new ArrayList<Grid>();
        playerHandGrid.add(playerHandGrid1);
        playerHandGrid.add(playerHandGrid2);
        playerHandGrid.add(playerHandGrid3);


        aiHandGrid1 = new Grid(LEVEL_WIDTH * 3.7f, LEVEL_HEIGHT * 1.15f, LEVEL_WIDTH * 1.5f, LEVEL_HEIGHT * 2.5f, assetManager.getBitmap("CardGrid"), this);
        aiHandGrid2 = new Grid(LEVEL_WIDTH * 5.15f, LEVEL_HEIGHT * 1.15f, LEVEL_WIDTH * 1.5f, LEVEL_HEIGHT * 2.5f, assetManager.getBitmap("CardGrid"), this);
        aiHandGrid3 = new Grid(LEVEL_WIDTH * 6.6f, LEVEL_HEIGHT * 1.15f, LEVEL_WIDTH * 1.5f, LEVEL_HEIGHT * 2.5f, assetManager.getBitmap("CardGrid"), this);
        aiHandGrid = new ArrayList<Grid>();
        aiHandGrid.add(aiHandGrid1);
        aiHandGrid.add(aiHandGrid2);
        aiHandGrid.add(aiHandGrid3);

        fieldHandGrid1 = new Grid(LEVEL_WIDTH * 0.75f, LEVEL_HEIGHT * 5.0f, LEVEL_WIDTH * 1.6f, LEVEL_HEIGHT * 2.85f, assetManager.getBitmap("CardGrid"), this);
        fieldHandGrid2 = new Grid(LEVEL_WIDTH * 2.15f, LEVEL_HEIGHT * 5.0f, LEVEL_WIDTH * 1.6f, LEVEL_HEIGHT * 2.85f, assetManager.getBitmap("CardGrid"), this);
        fieldHandGrid3 = new Grid(LEVEL_WIDTH * 3.55f, LEVEL_HEIGHT * 5.0f, LEVEL_WIDTH * 1.6f, LEVEL_HEIGHT * 2.85f, assetManager.getBitmap("CardGrid"), this);
        fieldHandGrid4 = new Grid(LEVEL_WIDTH * 4.95f, LEVEL_HEIGHT * 5.0f, LEVEL_WIDTH * 1.6f, LEVEL_HEIGHT * 2.85f, assetManager.getBitmap("CardGrid"), this);
        fieldHandGrid5 = new Grid(LEVEL_WIDTH * 6.35f, LEVEL_HEIGHT * 5.0f, LEVEL_WIDTH * 1.6f, LEVEL_HEIGHT * 2.85f, assetManager.getBitmap("CardGrid"), this);
        fieldHandGrid6 = new Grid(LEVEL_WIDTH * 7.75f, LEVEL_HEIGHT * 5.0f, LEVEL_WIDTH * 1.6f, LEVEL_HEIGHT * 2.85f, assetManager.getBitmap("CardGrid"), this);
        fieldHandGrid7 = new Grid(LEVEL_WIDTH * 9.15f, LEVEL_HEIGHT * 5.0f, LEVEL_WIDTH * 1.6f, LEVEL_HEIGHT * 2.85f, assetManager.getBitmap("CardGrid"), this);
        fieldHandGrid = new ArrayList<Grid>();
        fieldHandGrid.add(fieldHandGrid1);
        fieldHandGrid.add(fieldHandGrid2);
        fieldHandGrid.add(fieldHandGrid3);
        fieldHandGrid.add(fieldHandGrid4);
        fieldHandGrid.add(fieldHandGrid5);
        fieldHandGrid.add(fieldHandGrid6);
        fieldHandGrid.add(fieldHandGrid7);


        //When game initialises give 3 cards to player and ai hand
        playerHand = new Hand();
        dealRound(playerHand);

        aiHand = new Hand();
        dealRound(aiHand);

        //Shows back of AI cards
        for (Card c : aiHand.getCards()) {
            c.isFlipped = true;
        }

        //give 4 cards to the fieldHand
        fieldHand = new Hand();
        dealFieldCards(fieldHand);



        //Initialise AI
        ai = new AI(aiHandGrid, fieldHandGrid);
        //Puts cards into their appropriate grids
            playerHandPositionSetup();
            aIHandPositionSetup();
            gameFieldPositionSetup();
            shuffledDeckPositionSetup();


            setUpViewports();
            setUpCardObject();

           }


    //Method to set cards to a grid position
    private void playerHandPositionSetup() {
        for (int i = 0; i < playerHand.getCards().size(); i++) {
            playerHand.getCards().get(i).setPosition(playerHandGrid.get(i).getXPos(), playerHandGrid.get(i).getYPos());


        }
    }

    private void aIHandPositionSetup() {
        for (int i = 0; i < aiHand.getCards().size(); i++) {
            aiHand.getCards().get(i).isFlipped = true;
            aiHand.getCards().get(i).setPosition(aiHandGrid.get(i).getXPos(), aiHandGrid.get(i).getYPos());


        }
    }

    private void gameFieldPositionSetup() {
        for (int i = 0; i < fieldHand.getCards().size(); i++) {

            fieldHand.getCards().get(i).setPosition(fieldHandGrid.get(i).getXPos(), fieldHandGrid.get(i).getYPos());
        }
    }

    private void shuffledDeckPositionSetup() {
        for (int i = 0; i < shuffledD.getDeck().size(); i++) {
            shuffledD.getDeck().get(i).setPosition(shuffledDeckGrid.getXPos(), shuffledDeckGrid.getYPos());


        }
    }




    private void setUpViewports() {
        mDefaultScreenViewport.set(0, 0, mGame.getScreenWidth(), mGame.getScreenHeight());
        float layerHeight = mGame.getScreenHeight() * (480.0f / mGame.getScreenWidth());

        mDefaultLayerViewport.set(240.0f, layerHeight / 2.0f, 240.0f, layerHeight / 2.0f);
        mCardLayerViewport = new LayerViewport(240.0f, layerHeight / 2.0f, 240.0f, layerHeight / 2.0f);

    }

    private void setUpCardObject() {
        mGame.getAssetManager().loadAssets("txt/assets/CardDemoAssets.JSON");

    }



    //at the beginning of each round, call this method for player and ai hand to deal 3 cards to each
    public void dealRound(Hand hand) {
        hand.addCardToHand(shuffledD.dealCard());
        hand.addCardToHand(shuffledD.dealCard());
        hand.addCardToHand(shuffledD.dealCard());

    }

    //at the beginning of the game, deal 4 cards to the field hand
    public void dealFieldCards(Hand hand) {
        hand.addCardToHand(shuffledD.dealCard());
        hand.addCardToHand(shuffledD.dealCard());
        hand.addCardToHand(shuffledD.dealCard());
        hand.addCardToHand(shuffledD.dealCard());
    }


    //checks if player selected cards makes 15 and adds them to their winnings hand ~ Eimear and Cliona
    private void checkWinLoseConditions(Hand winningsHand, ArrayList<Card> selectedCards) {

        int cardTotal = 0;

        //checks the value of all the player selected cards added together
        for (Card c : selectedCards) {
            cardTotal += c.getCardValue();
        }

        //accepted if total=15
        if (cardTotal == 15) {

            last15Made = playerWinningsHand; // for use of the last round

            //set spaces to empty
            for (Grid g : fieldHandGrid) {
                for (Card c : selectedCards) {
                    if (g.getBound().intersects(c.getBound())) {
                        g.setSpaceEmpty(true);
                    }
                }
            }

           // takes cards from selected cards and moves them to winnings hand
            for (int i = selectedCards.size() - 1; i >= 0; i--) {
                selectedCards.get(i).moveFromFieldToWinnings(fieldHand, winningsHand);
                selectedCards.remove(i);

            }

            //if all the cards in the field hand have been used, give the player a scuab point
            if (fieldHand.getCards().size() == 0) {
                playerScuabPoints++;
                mGame.getScreenManager().addScreen(new scuabWin(mGame));

            }

        }

        //if player cannot make 15 and only drags card to field - remove cards from selectedCards
        else if (selectedCards.size() == 1) {
            selectedCards.removeAll(selectedCards);


        }
        //if total does not equal 15 - reject
        else {

            selectedCards.removeAll(selectedCards);

        }

        turn.endTurn(turn, this);


    }

    //sets empty to false if there is a card in the grid
    void checkAndSetGrids() {


        for (Grid g : fieldHandGrid) {
            for (Card c : fieldHand.getCards()) {
                if (g.getBound().intersects(c.getBound())) {
                    g.setSpaceEmpty(false);
                }

            }
        }

    }


    @Override
    public void update(ElapsedTime elapsedTime) {

        // Process any touch events occurring since the last update
        Input input = mGame.getInput();


        //buttons

        List<TouchEvent> touchEvents = input.getTouchEvents();
        mBackButton.update(elapsedTime);

        if (mBackButton.isPushTriggered()) {
            mGame.getScreenManager().removeScreen(this);
        }

        if (pauseGameButton.isPushTriggered()) {
            mGame.getScreenManager().addScreen(new scuabPauseScreen(mGame));
        }
        pauseGameButton.update(elapsedTime);

        //checks which grids are empty
        checkAndSetGrids();

        switch (selectTurn) {
            case PLAYER_TURN:
                playersTurn(touchEvents, elapsedTime);
                break;
            case AI_TURN:
                if (startTime == -1) {
                    startTime = elapsedTime.totalTime;
                    playerHasDraggedCard =false;
                    AIsTurn(touchEvents, elapsedTime);
                }
                // delays time between ai dragging card and highlighting the selected cards
                if (elapsedTime.totalTime > startTime + 2.0) {
                    selectTurn = SelectTurn.AI_DRAG_CARD;
                    startTime = -1;
                }
                break;


            case AI_DRAG_CARD:
                if (startTime == -1) {
                    startTime = elapsedTime.totalTime;
                    dragCardFromAIHand(aiStrongestCard);

                }
                if (elapsedTime.totalTime > startTime + 1.0) {
                    selectTurn = SelectTurn.AI_HIGHLIGHT_CARD;
                    startTime = -1;
                }
                break;

            case AI_HIGHLIGHT_CARD:
                if (startTime == -1) {
                    startTime = elapsedTime.totalTime;
                    highlightAIStrongestCards();
                }
                if (elapsedTime.totalTime > startTime + 1.5) {
                    selectTurn = SelectTurn.AI_MOVE_CARDS;
                    startTime = -1;
                }
                break;


            case AI_MOVE_CARDS:
                if (startTime == -1) {
                    startTime = elapsedTime.totalTime;
                    moveAiFromFieldToWinnings();
                }
                if (elapsedTime.totalTime > startTime + 2.0){
                    selectTurn = SelectTurn.PLAYER_TURN;
                    startTime = -1;
                }
                break;
        }
    }


    private void playersTurn(List<TouchEvent> touchEvents, ElapsedTime elapsedTime) {

        //when all the cards in the shuffled deck have been used and both the player and ai have used all of their cards
        //any remaining cards in the field hand go to the player which made the last 15
        if (shuffledD.getDeck().size() == 0 && aiHand.getCards().size() == 0 && playerHand.getCards().size() == 0 && turn.getRoundNumber() == 7) {
            if (last15Made == playerWinningsHand && fieldHand.getCards().size() != 0) {
                for (Card c : fieldHand.getCards()) {
                    c.moveFromFieldToWinnings(fieldHand, playerWinningsHand);
                }
            } else if (last15Made == aiWinningsHand && fieldHand.getCards().size() != 0) {
                for (Card c : fieldHand.getCards()) {
                    c.moveFromFieldToWinnings(fieldHand, aiWinningsHand);
                }
            }

            changeToScreen(new scoreBoard(mGame, playerWinningsHand, aiWinningsHand, playerScuabPoints, ai.aiScuabPoints));


        }

        //resets all cards from ai turn
        //unhighlights ai cards that didn't make 15
        for(Card c: fieldHand.getCards())
        {
            if(c.aiSelected){
                c.aiSelected=false;
                c.aiIsDragged=false;
                if(c.wasAICard)
                    c.wasAICard=false;
                highlightAIStrongestCards();
            }

        }

        //new round - deal more cards to player and ai
        if(turn.getTurnNumber()%6==0 && turn.getRoundNumber()>1 && playerHand.getCards().size()==0 && aiHand.getCards().size()==0 && turn.getRoundNumber()!=7){

            dealRound(playerHand);
            dealRound(aiHand);

            playerHandPositionSetup();
            aIHandPositionSetup();
        }

        for (Card c : playerHand.getCards()) {
            c.setCardBound();
            c.setUnlocked();

            //if player has not already dragged a card
            if (playerHasDraggedCard == false) {
                //move card from the player hand grid to the field grid
                c.moveCardIfTouched(touchEvents, playerHand.getCards());

                draggedCard = c.setCardToFieldGrid(touchEvents, playerHandGrid, fieldHandGrid, playerHand, fieldHand);

                c.setLocked();

                if (draggedCard != null) {
                    playerHasDraggedCard = true;
                    playerSelectedCards.add(draggedCard);
                    playerHand.removeCardFromHand(draggedCard);
                    mSubmitButton.setPosition(mDefaultLayerViewport.getWidth() * 0.93f, mDefaultLayerViewport.getHeight() * 0.30f);
                }

            }
        }

        //allows player to select/deselect cards in the field
        if (playerSelectedCards.size() > 0) {
            for (Card c : fieldHand.getCards()) {

                c.setUnlocked();
                Card selectedFieldCard = c.getSelectedCard(touchEvents);

                if (selectedFieldCard != null && c.getSelected() == false) {
                    playerSelectedCards.remove(selectedFieldCard);
                }

                if (selectedFieldCard != null && c.getSelected() == true) {
                    playerSelectedCards.add(selectedFieldCard);

                }


            }
            //highlights card when selected by player
            highlightCards();

        }


        mSubmitButton.update(elapsedTime);
        if (mSubmitButton.isPushTriggered()) {
            if (!playerSelectedCards.isEmpty()) {
                checkWinLoseConditions(playerWinningsHand, playerSelectedCards);


                //unhighlight any cards left in the field
                highlightCards();

                //reset cards in the field hand
                for (Card c : fieldHand.getCards()) {
                    c.setSelectedToFalse();
                    c.isDragged =false;

                }
                //moves submit button off screen
                mSubmitButton.setPosition(-10,-20);
                //reset time
                startTime = -1;
                selectTurn = SelectTurn.AI_TURN;

            }

        }
    }

    double startTime = -1;

    //calculates all possible subsets of 15 and calculates strongest AI hand
    private void AIsTurn(List<TouchEvent> touchEvents, ElapsedTime elapsedTime) {

        //passes current cards into ai
        ai.setUpAI(aiHand, fieldHand);

        //gets all possible subsets of 15 using each ONE of the ai cards with any number of cards in the field
        for (Card c : aiHand.getCards()) {
            c.setCardBound();
           ai.getAllSubsets(fieldHand, fieldHand.getCards().size(), 15 - c.getCardValue(), c);
        }

        //gets the best ai hand
        aiStrongestHand = ai.getStrongestHand();

        //gets strongestCard for drag purposes from the strongest hand
        for (Card c : aiStrongestHand.getCards()) {
            if (c.wasAICard == true) {
                aiStrongestCard = c;

            }
            c.setSelectedToFalse();
            c.aiSelected = true;
            c.isHighlighted = false;

        }
    }

    void dragCardFromAIHand(Card aiStrongestCard) {

        //flips ai card to display front of card when dragged
        aiStrongestCard.isFlipped = false;
        aiStrongestCard.flipCard();

        //removes ai card from the ai hand and puts it in the field hand
        aiHand.removeCardFromHand(aiStrongestCard);
        aiStrongestCard.setCardToFieldGrid(fieldHandGrid, aiStrongestHand, fieldHand);
        aiStrongestCard.aiIsDragged = true;
        aiStrongestHand.scoreOfHand=0;

    }

    //highlights AI cards in the field grid
    //also unhighlights the AI card if they cannot make a move
    public void highlightAIStrongestCards() {

        for (Card c : fieldHand.getCards()) {

            if (c.aiSelected && c.isHighlighted == false) {

                Bitmap merged = c.highlightCard(c.getFrontBitmap(), assetManager.getBitmap("CardGlow"));

                c.setFrontBitmap(merged);
                c.isHighlighted = true;


            } else if (!c.aiSelected) {
                c.setFrontBitmap(assetManager.getBitmap(c.getCardValue() + c.getSuit()));
                c.isHighlighted = false;
            }

        }


    }

    //moves the ai strongest cards from the field and adds them to ai winnings hand - Eimear and Laura
    void moveAiFromFieldToWinnings() {

        last15Made = aiWinningsHand; //used in final round

        //if ai makes a scuab play animation -Sarah Rose & Cliona
        if (aiStrongestHand != null && aiStrongestHand.scuabHand) {
            mGame.getScreenManager().addScreen(new scuabWin(mGame));
        }

        if (aiStrongestHand.getCards().size() > 0) {
           // aiScuabPoints += ai.aiScuabPoints;

            for (Grid g : fieldHandGrid) {
                for (Card c : fieldHand.getCards()) {
                    if (g.getBound().intersects(c.getBound()) && c.aiSelected) {
                        c.moveFromFieldToWinnings(fieldHand, aiWinningsHand);
                        aiStrongestHand.removeCardFromHand(c);

                        g.setSpaceEmpty(true);


                    }
                }
            }

        }

        aiStrongestCard = null; // resets at the end of turn


        turn.endTurn(turn, this);


    }


    public void setUpDeck() {
        //taking the original "cardDeck" object, initialising it and shuffling it in an array list.
        // Putting this shuffled array list back into a card deck called shuffledD

        originalDeck.initialiseDealerDeck();

        ArrayList<Card> shuffledDeck = originalDeck.shuffleCards(originalDeck.getDeck());

        shuffledD.setUpDeck(shuffledDeck);

    }



    public void drawCardsToScreen(Card c, ElapsedTime elapsedTime, IGraphics2D graphics2D) {


        Paint bitmapPaint = new Paint();
        Rect sourceRect = new Rect(
                1, 1, c.getFrontBitmap().getWidth(), c.getFrontBitmap().getHeight());
        Rect destRect = new Rect(
                (int) (c.getxPos()), (int) (c.getyPos()), (int) (c.getxPos()), (int) (c.getyPos()));
        graphics2D.drawBitmap(c.getFrontBitmap(), sourceRect, destRect, bitmapPaint);


    }


    //highlights / unhighlights cards when selected / deselected
    public void highlightCards() {

                for (Card c : fieldHand.getCards()) {

                    if (c.getSelected() && c.isHighlighted == false) {

                        Bitmap merged = c.highlightCard(c.getFrontBitmap(), assetManager.getBitmap("CardGlow"));
                        c.setFrontBitmap(merged);
                        c.isHighlighted = true;

                    } else if ((!c.getSelected() && c.isHighlighted )) {
                        c.setFrontBitmap(assetManager.getBitmap(c.getCardValue() + c.getSuit()));
                        c.isHighlighted = false;

                    } else if (playerSelectedCards.isEmpty()) {
                        c.setFrontBitmap(assetManager.getBitmap(c.getCardValue() + c.getSuit()));
                        c.isHighlighted = false;
                    }


                }
            }

    @Override
    public void draw(ElapsedTime elapsedTime, IGraphics2D graphics2D) {

        //draws background
        boardBackground.top = 0;
        boardBackground.left = 0;
        boardBackground.bottom = mGame.getScreenHeight();
        boardBackground.right = mGame.getScreenWidth();
        graphics2D.drawBitmap(mGame.getAssetManager().getBitmap("sBackground"), null, boardBackground, null);

        //draws buttons
        mBackButton.draw(elapsedTime, graphics2D, mCardLayerViewport, mDefaultScreenViewport);
        mSubmitButton.draw(elapsedTime, graphics2D, mCardLayerViewport, mDefaultScreenViewport);
        pauseGameButton.draw(elapsedTime, graphics2D, mCardLayerViewport, mDefaultScreenViewport);
        for (Card c : playerHand.getCards()) {
            drawCardsToScreen(c, elapsedTime, graphics2D);

        }

        //display round number in top left corner
        Paint textPaint = new Paint();
        textPaint.setTypeface(mGame.getAssetManager().getFont("SettingsFont"));
        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(LEVEL_HEIGHT / 3);
        textPaint.setTextAlign(Paint.Align.CENTER);

        graphics2D.drawText(
                "Round " + turn.getRoundNumber(), LEVEL_WIDTH * 0.5f, LEVEL_HEIGHT * 0.5f, textPaint);


        //draw grids
        playerHandGrid1.draw(elapsedTime, graphics2D);
        playerHandGrid2.draw(elapsedTime, graphics2D);
        playerHandGrid3.draw(elapsedTime, graphics2D);

        aiHandGrid1.draw(elapsedTime, graphics2D);
        aiHandGrid2.draw(elapsedTime, graphics2D);
        aiHandGrid3.draw(elapsedTime, graphics2D);

        fieldHandGrid1.draw(elapsedTime, graphics2D);
        fieldHandGrid2.draw(elapsedTime, graphics2D);
        fieldHandGrid3.draw(elapsedTime, graphics2D);
        fieldHandGrid4.draw(elapsedTime, graphics2D);
        fieldHandGrid5.draw(elapsedTime, graphics2D);
        fieldHandGrid6.draw(elapsedTime, graphics2D);
        fieldHandGrid7.draw(elapsedTime, graphics2D);

        shuffledDeckGrid.draw(elapsedTime, graphics2D);

        for (Card c : shuffledD.getDeck()) {
            c.setBitmap(c.getCardBack());
            c.draw(elapsedTime, graphics2D);
        }

        //draw cards in grids
        playerHand.displayCardsInGrid(elapsedTime, graphics2D);
        aiHand.displayCardsInGrid(elapsedTime, graphics2D);
        fieldHand.displayCardsInGrid(elapsedTime, graphics2D);

    }

    //method to change screens
    public void changeToScreen(GameScreen screen) {
        mGame.getScreenManager().removeScreen(this.getName());
        mGame.getScreenManager().addScreen(screen);
    }

}
