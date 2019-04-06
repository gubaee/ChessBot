package chess.bots;



import java.util.List;

import cse332.chess.interfaces.AbstractSearcher;
import cse332.chess.interfaces.Board;
import cse332.chess.interfaces.Evaluator;
import cse332.chess.interfaces.Move;

import tests.TestStartingPosition;

/**
 * This class should implement the minimax algorithm as described in the
 * assignment handouts.
 */
public class SimpleSearcher<M extends Move<M>, B extends Board<M, B>> extends
        AbstractSearcher<M, B> {
	
	
    public M getBestMove(B board, int myTime, int opTime) {
        /* Calculate the best move */
        //BestMove<M> best = minimax(this.evaluator, board, ply);
        return minimax(this.evaluator, board, ply).move;
    }

    static <M extends Move<M>, B extends Board<M, B>> BestMove<M> minimax(Evaluator<B> evaluator, B board, int depth) {
    	

    	if (depth == 0) {
    		return new BestMove<M>(evaluator.eval(board));
    	}
    	
    	List<M> list = board.generateMoves();
    	
    	if(list.isEmpty()) {
    		if(board.inCheck()) {
    			return new BestMove<M>(-evaluator.mate() - depth);
    		}
    		else {
    			return new BestMove<M>(-evaluator.stalemate());
    		}
    	}

    	
    	
    	BestMove<M> bestMove = new BestMove<M>(-evaluator.infty());
    	
    	for(M move : list) {
    		
    		board.applyMove(move);   	
    		TestStartingPosition.incrementNode();
    		int value = -minimax(evaluator,board,depth-1).value;
    		
    		if(value > bestMove.value) {
    			bestMove.value = value;
    			bestMove.move = move;
    		}
    		
    		board.undoMove();

    	}
    	//System.out.println("simplesearcher count = "+count);
    	return bestMove;
    }
}