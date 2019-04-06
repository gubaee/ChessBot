package chess.bots;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;
import cse332.chess.interfaces.AbstractSearcher;
import cse332.chess.interfaces.Board;
import cse332.chess.interfaces.Evaluator;
import cse332.chess.interfaces.Move;
import tests.TestStartingPosition;
public class ParallelSearcher<M extends Move<M>, B extends Board<M, B>> extends
        AbstractSearcher<M, B> {
	
    private final static ForkJoinPool POOL = new ForkJoinPool();
    private final static int DIVIDE_CUTOFF = 3;

	
	
    public M getBestMove(B board, int myTime, int opTime) {
    	List<M> moves = board.generateMoves();
    	BestMove<M> m = POOL.invoke(new ParallelTask<M, B>(board, evaluator, 0, moves.size(), ply, moves));
//		reportNewBestMove(m.move);
    	return m.move;
    }
    
    
    private class ParallelTask<M extends Move<M>, B extends Board<M, B>> extends RecursiveTask<BestMove<M>> {

    	B board;
    	Evaluator<B> evaluator;
    	int low, high, depth;
    	List<M> moves;
    	
    	public ParallelTask(B board, Evaluator<B> evaluator, int low, int high, int depth, List<M> moves) {
    		this.board = board;
    		this.evaluator = evaluator;
    		this.low = low;
    		this.high = high;
    		this.depth = depth;
    		this.moves = moves;
    	}
    	
		@Override
		protected BestMove<M> compute() {
			
			// Only gets in here if it is a child
			if(low == high) {
				B copyBoard = board.copy();
				copyBoard.applyMove(moves.get(low));
				TestStartingPosition.incrementNode();
				moves = copyBoard.generateMoves();		
				low = 0;
				high = moves.size();
				board = copyBoard;
			}
			
			
			
	    	if(moves.isEmpty()) {
	    		if(board.inCheck()) {
	    			return new BestMove<M>(-evaluator.mate() - depth);
	    		}
	    		else {
	    			return new BestMove<M>(-evaluator.stalemate());
	    		}
	    	}

			if(depth <= cutoff) {
				return SimpleSearcher.minimax(evaluator, board, depth);
			}
			
			if(high - low <= DIVIDE_CUTOFF) {
				
				
				ArrayList<ParallelTask<M, B>> Array = new ArrayList<>();
				
				
				// Make chlid to copy the board.
				for(int i = low; i < high; i++) {
					
					ParallelTask<M, B> task = new ParallelTask<M, B>(board, evaluator, i, i, depth - 1, moves);				
					Array.add(task);
					task.fork();
				}
				
				BestMove<M> returnBest = new BestMove<M>(-evaluator.infty());
				
				for(int i = 0; i < Array.size(); i++) {
					
					
					BestMove<M> value = Array.get(i).join().negate();
    			
    				if(value.value > returnBest.value) {
    					returnBest.move = moves.get(low+i);
    					returnBest.value = value.value;
    				}

				}
				return returnBest;

			}else {
				
            	int mid = low + (high - low) / 2; 
            	
            	ParallelTask<M, B> left = new ParallelTask<M, B> (board, evaluator, low, mid, depth, moves);
            	ParallelTask<M, B> right = new ParallelTask<M, B> (board, evaluator, mid, high, depth, moves);
            	
            	left.fork();
            	BestMove<M> rightResult = right.compute();
            	BestMove<M> leftResult = left.join();
            	
            	if(rightResult.value > leftResult.value) {
            		return rightResult;
            	}else {
            		return leftResult;
            	}
			}
		}
    }
}
