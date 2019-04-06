package tests;

import chess.board.ArrayBoard;
import chess.board.ArrayMove;
import chess.bots.JamboreeSearcher;
import chess.bots.LazySearcher;
import chess.bots.SimpleSearcher;
import chess.game.SimpleEvaluator;
import cse332.chess.interfaces.Move;
import cse332.chess.interfaces.Searcher;

public class TestStartingPosition {

	public static int NODE = 0;
	
    public static final String STARTING_POSITION = "r3k2r/pp5p/2n1p1p1/q1pp1p2/5B2/2PP1Q2/P1P2PPP/R4RK1 b kq -";

    public static ArrayMove getBestMove(String fen, Searcher<ArrayMove, ArrayBoard> searcher, int depth, int cutoff) { 
        searcher.setDepth(depth);
        searcher.setCutoff(cutoff);
        searcher.setEvaluator(new SimpleEvaluator());

        return searcher.getBestMove(ArrayBoard.FACTORY.create().init(fen), 0, 0);
    }
    public static synchronized void incrementNode() {
    	NODE++;
    }
    
    public static void printMove(String fen, Searcher<ArrayMove, ArrayBoard> searcher, int depth, int cutoff) {
        String botName = searcher.getClass().toString().split(" ")[1].replace("chess.bots.", "");
        System.out.println(botName + " returned " + getBestMove(fen, searcher, depth, cutoff));
        
        
    }
    public static void main(String[] args) {
        Searcher<ArrayMove, ArrayBoard> dumb = new SimpleSearcher<>();
 

        
        long averageTime = 0;

        for(int i = 0; i < 20; i++) {
        	
            printMove(STARTING_POSITION, dumb, 5, 1);

        	if(i > 5) {
                long startTime = System.currentTimeMillis();
                printMove(STARTING_POSITION, dumb, 5, 1);
                long stopTime = System.currentTimeMillis();
                long elapsedTime = stopTime - startTime;
                averageTime += elapsedTime;
        	}
        }
  
        System.out.println(averageTime/15);

        System.out.println(NODE/20);

        //
    }
}