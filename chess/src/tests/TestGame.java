package tests;

import java.util.ArrayList;

import chess.board.ArrayBoard;
import chess.board.ArrayMove;
import chess.bots.AlphaBetaSearcher;
import chess.bots.JamboreeSearcher;
import chess.bots.LazySearcher;
import chess.bots.ParallelSearcher;
import chess.bots.SimpleSearcher;
import chess.game.SimpleEvaluator;
import cse332.chess.interfaces.Move;
import cse332.chess.interfaces.Searcher;

public class TestGame {

    public Searcher<ArrayMove, ArrayBoard> whitePlayer;
    public Searcher<ArrayMove, ArrayBoard> blackPlayer;
    public static final String STARTING_POSITION = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1";
    static ArrayList<String> position = new ArrayList<String>();
    private ArrayBoard board;
    
    public static void main(String[] args) {

    	int depth = 5;
    	int cutoff = depth/2;
    	double average = 0;

        TestGame game = new TestGame();
        game.play();
        //System.out.println("**************");
        //System.out.println("Depth : " + depth + " CUTOFF: "+ cutoff);
        //
        for(int i = 0; i < position.size(); i++) {

        	TestStartingPosition.NODE = 0;
        	Searcher<ArrayMove, ArrayBoard> dumb = new JamboreeSearcher<>();
        	TestStartingPosition.printMove(position.get(i), dumb,  depth, cutoff);
        	//TestStartingPosition.printMove(position.get(i),dumb,depth,0);
        	System.out.println("Move "+i +" : "+ TestStartingPosition.NODE + " Nodes ");
        	average += TestStartingPosition.NODE;

        }
        average /=position.size();
        System.out.println("Average = "+ average + " Nodes ");
    }

    public TestGame() {
        setupWhitePlayer(new AlphaBetaSearcher<ArrayMove, ArrayBoard>(), 3, 3);
        setupBlackPlayer(new SimpleSearcher<ArrayMove, ArrayBoard>(), 4, 4);
    }
    
    public void play() {
       this.board = ArrayBoard.FACTORY.create().init(STARTING_POSITION);
       Searcher<ArrayMove, ArrayBoard> currentPlayer = this.blackPlayer;
       
       int turn = 0;
       
       /* Note that this code does NOT check for stalemate... */
       while (!board.inCheck() || board.generateMoves().size() > 0) {
           currentPlayer = currentPlayer.equals(this.whitePlayer) ? this.blackPlayer : this.whitePlayer;
           System.out.printf("%3d: " + board.fen() + "\n", turn);
           position.add(board.fen());
           
           //System.out.printf("%3d: " + board.fen() + "\n", turn);
           
           this.board.applyMove(currentPlayer.getBestMove(board, 1000, 1000));
           turn++;
       }
    }
    
    public Searcher<ArrayMove, ArrayBoard> setupPlayer(Searcher<ArrayMove, ArrayBoard> searcher, int depth, int cutoff) {
        searcher.setDepth(depth);
        searcher.setCutoff(cutoff);
        searcher.setEvaluator(new SimpleEvaluator());
        return searcher; 
    }
    public void setupWhitePlayer(Searcher<ArrayMove, ArrayBoard> searcher, int depth, int cutoff) {
        this.whitePlayer = setupPlayer(searcher, depth, cutoff);
    }
    public void setupBlackPlayer(Searcher<ArrayMove, ArrayBoard> searcher, int depth, int cutoff) {
        this.blackPlayer = setupPlayer(searcher, depth, cutoff);
    }
}