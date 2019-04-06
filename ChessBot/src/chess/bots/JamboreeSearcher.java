//veal
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

public class JamboreeSearcher<M extends Move<M>, B extends Board<M, B>> extends AbstractSearcher<M, B> {

	private final ForkJoinPool POOL = new ForkJoinPool();
	private final static int DIVIDE_CUTOFF = 3;
	private final static double PERCENTAGE_SEQUENTIAL = 0.5;
	
	public M getBestMove(B board, int myTime, int opTime) {

		List<M> moves = board.generateMoves();
		BestMove<M> m = POOL.invoke(
				new Task<M, B>(board, -evaluator.infty(), evaluator.infty(), evaluator, -1, -1, ply, moves));
		reportNewBestMove(m.move);
		return m.move;

	}

	private class Task<M extends Move<M>, B extends Board<M, B>> extends RecursiveTask<BestMove<M>> {

		B board;
		int alpha, beta;
		int low, high, depth;
		Evaluator<B> evaluator;
		List<M> moves;

		public Task(B board, int alpha, int beta, Evaluator<B> evaluator, int low, int high, int depth, List<M> moves) {

			this.board = board;
			this.alpha = alpha;
			this.beta = beta;
			this.evaluator = evaluator;
			this.low = low;
			this.high = high;
			this.depth = depth;
			this.moves = moves;

		}

		@Override
		protected BestMove<M> compute() {

			BestMove<M> bestMove = new BestMove<M>(alpha);
			
			/*Children to copy the board*/
			if (low == high) {
				
				if(low !=-1) {
				board = board.copy();
				board.applyMove(moves.get(low));
				TestStartingPosition.incrementNode();
				}
				
				if (depth <= cutoff) {
					// bot tends to lead the game to draw when it supposed to win
					// this fixes it.
					
					
//					if(moves.size() > 5) { 
//					
//					return AlphaBetaSearcher.AlphaBeta(evaluator, board, depth, alpha, beta);
//					}
//					else {
//						return SimpleSearcher.minimax(evaluator, board, depth+4);
//					}
					
//					if(moves.size() < 4) {
//						return AlphaBetaSearcher.AlphaBeta(evaluator, board, depth+1, alpha, beta);	
//					}
//					else {
//						return AlphaBetaSearcher.AlphaBeta(evaluator, board, depth, alpha, beta);
//					}
					
					return AlphaBetaSearcher.alphaBeta(evaluator, board, depth, alpha, beta);	
					
				}
				
				moves = board.generateMoves();
				
				if (moves.isEmpty()) {
					if (board.inCheck()) {
						return new BestMove<M>(-evaluator.mate() - depth);
					} else {
						return new BestMove<M>(-evaluator.stalemate());
					}
				}
				
				low = 0;
				high = moves.size();
				int newLow = (int)(high * PERCENTAGE_SEQUENTIAL);

				/*Doing sequentially*/
				for (int i = 0; i < newLow; i++) {
					
					Task<M, B> PS = new Task<M, B>(board, -beta, -alpha, evaluator, i, i, depth - 1, moves);
					BestMove<M> PSresult = PS.compute().negate();

					if (PSresult.value > alpha) {
						alpha = PSresult.value;
						bestMove.value = alpha;
						bestMove.move = moves.get(i);
					}
					if (alpha >= beta) {
						return bestMove;
					}
				} // end of percentage sequential
				low = newLow;
			}

			if(low!=high) { // This is for when we set PERCENTAGE_SEQUENTIAL = 1;
			
			/*Keep dividing until divide_cut_off*/
			if(low!=high) {
			if (high - low <= DIVIDE_CUTOFF) {

				ArrayList<Task<M, B>> array = new ArrayList<>();
				ArrayList<BestMove<M>> Barray = new ArrayList<>();
				
				Task<M, B> taskCompute = new Task<M, B>(board, -beta, -alpha, evaluator, low, low, depth - 1, moves);
				
				for (int i = low+1; i < high; i++) {		
					Task<M, B> taskFork = new Task<M, B>(board, -beta, -alpha, evaluator, i, i, depth - 1, moves);
					array.add(taskFork);
					taskFork.fork();
				}
				
				
				BestMove<M> value = taskCompute.compute().negate();
				
				
				if (value.value > alpha) {
					bestMove.move = moves.get(low); 
					bestMove.value = value.value;
					alpha = bestMove.value;
				}
			
				if (alpha >= beta) {
					return bestMove;
				}
			
				
				
				for (int i = 0; i < array.size(); i++) {
					Barray.add(array.get(i).join().negate());
				}
				
			
				
				for (int i = 0; i < Barray.size(); i++) {
					if (Barray.get(i).value > alpha) {
						bestMove.move = moves.get(low + i + 1); 
						bestMove.value = Barray.get(i).value;
						alpha = bestMove.value;
						
					}
					if (alpha >= beta) {
						return bestMove;
					}

				}
		
				
				
				
//				/*Pass board to children to copy board*/
//				for (int i = low; i < high; i++) {
//					
//					Task<M, B> task = new Task<M, B>(board, -beta, -alpha, evaluator, i, i, depth - 1, moves);
//					array.add(task);
//					task.fork();
//					
//				}
//
//				/* One copmute for parent*/
//				for (int i = 0; i < array.size(); i++) {
//
//					BestMove<M> value = array.get(i).join().negate();
//
//					if (value.value > alpha) {
//						bestMove.move = moves.get(low + i); 
//						bestMove.value = value.value;
//						alpha = bestMove.value;
//					}
//					
//					if (alpha >= beta) {
//						return bestMove;
//					}
//					
//				}
			
				return bestMove;
			} else {
				
			

				int mid = low + (high - low) / 2;

				Task<M, B> left = new Task<M, B>(board, alpha, beta, evaluator, low, mid, depth, moves);
				Task<M, B> right = new Task<M, B>(board, alpha, beta, evaluator, mid, high, depth, moves);

				left.fork();
				BestMove<M> rightResult = right.compute();
				BestMove<M> leftResult = left.join();
				
				if(rightResult.value > bestMove.value && rightResult.value >leftResult.value) {
					return rightResult;
				}
				else if(leftResult.value > bestMove.value) {
					return leftResult;
				}
				else {
					return bestMove;
				}
			} // end of high - low <= DIVIDE_CUTOFF
			}
			
			return bestMove;
		}

		return bestMove;
		}
		


	}

}
