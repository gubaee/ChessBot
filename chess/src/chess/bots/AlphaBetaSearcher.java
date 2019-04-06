package chess.bots;



import java.util.List;

import cse332.chess.interfaces.AbstractSearcher;
import cse332.chess.interfaces.Board;
import cse332.chess.interfaces.Evaluator;
import cse332.chess.interfaces.Move;
import tests.TestStartingPosition;


public class AlphaBetaSearcher<M extends Move<M>, B extends Board<M, B>> extends AbstractSearcher<M, B> {

    public M getBestMove(B board, int myTime, int opTime) {
    	
    	//BestMove<M> best = AlphaBeta(this.evaluator, board, ply, -evaluator.infty(), evaluator.infty());
        return alphaBeta(this.evaluator, board, ply, -evaluator.infty(), evaluator.infty()).move;
        
    }
    
    static <M extends Move<M>, B extends Board<M, B>> BestMove<M> alphaBeta(Evaluator<B> evaluator, B board, int depth, int alpha, int beta) {

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
    		int value = -alphaBeta(evaluator, board, depth - 1, -beta, -alpha).value;
    		board.undoMove();
    		
    		// If value is between alpha and beta, we've
    		// found a new lower bound
    		if(value > alpha) {
    			alpha = value;
    			bestMove.value = alpha;
    			bestMove.move = move;
    		}
    		
    		// If the value is bigger than beta, we won't
    		// actually be able to get this move
    		if(value >= beta) {
    			return bestMove;
    		}
    	}
    	
    	return bestMove;
    } 
}