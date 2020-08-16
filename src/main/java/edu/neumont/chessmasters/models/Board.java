package edu.neumont.chessmasters.models;

import edu.neumont.chessmasters.Utils;
import edu.neumont.chessmasters.events.EventRegistry;
import edu.neumont.chessmasters.events.PieceCaptureEvent;
import edu.neumont.chessmasters.events.PostPieceMoveEvent;
import edu.neumont.chessmasters.models.pieces.*;

import java.util.ArrayList;

public class Board {

	// y, x
	private Piece[][] squares;

	private Piece getSquare(String s) {
		return getSquare(new Location(s));
	}

	public Piece getSquare(Location l) {
		return squares[l.getY()][l.getX()];
	}

	public void setSquare(Location l, Piece p) {
		squares[l.getY()][l.getX()] = p;
		if (p != null) p.setLocation(l);
	}

	public King getKing(PieceColor color) {
		for (int y = 0; y < 8; y++) {
			for (int x = 0; x < 8; x++) {
				Piece piece = getSquare(new Location(x, y));
				if (piece != null && piece instanceof King && piece.getColor() == color)
					return (King) piece;
			}
		}

		return null;
	}

	public ArrayList<Piece> getAllPieces() {
		ArrayList<Piece> pieces = new ArrayList<>();
		pieces.addAll(getAllPieces(PieceColor.BLACK));
		pieces.addAll(getAllPieces(PieceColor.WHITE));
		return pieces;
	}

	public ArrayList<Piece> getAllPieces(PieceColor color) {
		ArrayList<Piece> pieces = new ArrayList<>();
		for (int y = 0; y < 8; y++) {
			for (int x = 0; x < 8; x++) {
				Piece piece = getSquare(new Location(x, y));
				if (piece != null && piece.getColor() == color)
					pieces.add(piece);
			}
		}
		return pieces;
	}

	public Board(boolean withPawns) {
		this.squares = new Piece[8][8];

		if (withPawns)
		for (int file = 0; file < 8; file++) {
			placePiece(new Pawn(PieceColor.WHITE), new Location(file, 1));
			placePiece(new Pawn(PieceColor.BLACK), new Location(file, 6));
		}

		placePiece(new   Rook(PieceColor.WHITE), new Location("a1"));
		placePiece(new Knight(PieceColor.WHITE), new Location("b1"));
		placePiece(new Bishop(PieceColor.WHITE), new Location("c1"));
		placePiece(new  Queen(PieceColor.WHITE), new Location("d1"));
		placePiece(new   King(PieceColor.WHITE), new Location("e1"));
		placePiece(new Bishop(PieceColor.WHITE), new Location("f1"));
		placePiece(new Knight(PieceColor.WHITE), new Location("g1"));
		placePiece(new   Rook(PieceColor.WHITE), new Location("h1"));

		placePiece(new   Rook(PieceColor.BLACK), new Location("a8"));
		placePiece(new Knight(PieceColor.BLACK), new Location("b8"));
		placePiece(new Bishop(PieceColor.BLACK), new Location("c8"));
		placePiece(new  Queen(PieceColor.BLACK), new Location("d8"));
		placePiece(new   King(PieceColor.BLACK), new Location("e8"));
		placePiece(new Bishop(PieceColor.BLACK), new Location("f8"));
		placePiece(new Knight(PieceColor.BLACK), new Location("g8"));
		placePiece(new   Rook(PieceColor.BLACK), new Location("h8"));
	}

	// copy ctor
	public Board(Board original) {
		this.squares = new Piece[8][8];

		// copy pieces over
		for (int rank = 0; rank < 8; rank++) {
			for (int file = 0; file < 8; file++) {
				Location l = new Location(file, rank);
				Piece p = original.getSquare(l);
				this.setSquare(l, p);
			}
		}
	}

	// create board with pawns
	public Board() {
		//this(true);
		this("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1");
	}

	// create board from FEN
	public Board(String fen) {
		this.squares = new Piece[8][8];

		String[] components = fen.split(" ");
		String layout = components[0];
		int index = 63; // iterate through the board
		for (Character p : layout.toCharArray()) {
			if (p == '/') continue;

			int rank = index / 8;
			int file = 7 - (index % 8);
			index--;

			try {
				index -= Integer.parseInt(p.toString()) - 1;
				continue;
			} catch (NumberFormatException nfe) {
				try {
					setSquare(new Location(file, rank), Piece.fromFEN(p.toString()));
				} catch (UnsupportedOperationException uoe) {
					throw new IllegalArgumentException("Invalid character in FEN string: " + p);
				}
			}
		}
	}

	public void clearBoard() {
		this.squares = new Piece[8][8];
	}

	public boolean placePiece(Piece p, Location l) {
		if (getSquare(l) != null) return false;
		setSquare(l, p);
		return true;
	}

	// Checks whether the exclusive range of squares is empty
	// (all squares between a and b, not including a and b)
	public boolean pathIsEmpty(Location a, Location b) {
		Location[] path = Location.getExclusiveRange(a, b);
		for (Location l : path)
			if (getSquare(l) != null)
				return false;
		return true;
	}

	public boolean validateMove(Piece p, Location dest) {
		// check whether we're moving through a piece
		if (!pathIsEmpty(p.getLocation(), dest))
			return false;

		// check whether we're capturing
		Piece victim = getSquare(dest);
		if (victim != null) {
			// if we are, ensure that we're capturing an opponent
			if (victim.getColor() == p.getColor())
				return false;
			// if we're trying to capture a king, something has gone horribly wrong---we
			// shouldn't have been able to reach this configuration in the first place
			if (victim instanceof King)
				throw new UnsupportedOperationException
					("An attempt (" + p.getLocation() + " -> " + dest + ") was made to capture a king, indicating that the game was in an illegal state:\n" + this.toString());
		}

		if (victim != null &&
				( (p instanceof Pawn && p.getLocation().getX() != dest.getX())
						|| !(p instanceof Pawn) ) ) {
			PieceCaptureEvent event = new PieceCaptureEvent(p, victim); //Fire our capture event when a piece is captured.
			EventRegistry.callEvents(event);
		}

		if (p instanceof Pawn) {
			// in a valid move, either we're capturing or we're going straight forward
			return (victim != null) ^ (p.getLocation().getX() == dest.getX());
		} else if (p instanceof Rook
				|| p instanceof Knight
				|| p instanceof Bishop
				|| p instanceof Queen
				|| p instanceof King) {
			return true;
		} else {
			throw new UnsupportedOperationException("I don't know how to validate this move for this piece");
		}
	}

	// Returns whether the given color is in checkmate.
	public boolean isInCheckmate(PieceColor color) {
		return isInCheckmate(getKing(color));
	}
	public boolean isInCheckmate(King king) {
		if (!isInCheck(king)) return false;
		for (int rank_from = 0; rank_from < 8; rank_from++) {
			for (int file_from = 0; file_from < 8; file_from++) {

				Location from = new Location(file_from, rank_from);
				Piece p = getSquare(from);

				if (p == null || p.getColor() != king.getColor()) continue;

				for (int rank_to = 0; rank_to < 8; rank_to++) {
					for (int file_to = 0; file_to < 8; file_to++) {

						Location to = new Location(file_to, rank_to);
						if (from.equals(to)) continue;

						Board b = new Board(this);
						try {
						if (b.movePiece(from, to) && !b.isInCheck(king.getColor()))
							// we found a move that's valid and removes us from check
							return false; // so we're not in checkmate
						} catch (UnsupportedOperationException uoe) {
							continue;
						}
					}
				}
			}
		}
		return true;
	}

	public boolean movePiece(String from, String to) {
		return movePiece(new Location(from), new Location(to));
	}

	public boolean movePiece(Move move) {
		return movePiece(move.from, move.to);
	}

	public boolean movePiece(Location from, Location to) {
		Piece p = getSquare(from);
		if (p == null) return false;
		if (!validateMove(p, to)) return false;
		if (!p.move(to)) return false;
		setSquare(to, p);
		setSquare(from, null);

		if (p instanceof Pawn && ((Pawn) p).shouldPromote()) { //Promote pawn to queen.
			p = new Queen(p.getColor());
			setSquare(to, p);
		}

		PostPieceMoveEvent post = new PostPieceMoveEvent(p);
		EventRegistry.callEvents(post);
		return true;
	}

	public boolean isInCheck(PieceColor color) {
		return isInCheck(getKing(color));
	}

	public boolean isInCheck(King king) {
		for (Piece piece : getAllPieces(king.getColor().getOpposite())) {
			if (pieceCreatesCheck(piece, king)) {
				return true;
			}
		}

		return false;
	}

	public boolean pieceCreatesCheck(Piece piece) {
		King king = getKing(piece.getColor().getOpposite());
		return pieceCreatesCheck(piece, king);
	}

	public boolean pieceCreatesCheck(Piece piece, King king) {
		if (!piece.validateMove(king.getLocation()))
			return false;
		try {
			validateMove(piece, king.getLocation());
		} catch (UnsupportedOperationException e) {
			return true;
		}

		return false;
	}

	@Override
	public String toString() {

		// Build the top edge
		String top = Utils.buildRow(
				"   " + Utils.Drawing.Corners.topLeft,
				Utils.Drawing.Edges.horizontal + Utils.Drawing.Edges.horizontal + Utils.Drawing.Edges.horizontal,
				Utils.Drawing.Joints.horizontalDown,
				Utils.Drawing.Corners.topRight,
				8
				) + "\n";

		// Build the row separator
		String rowSep = Utils.buildRow(
				"   " + Utils.Drawing.Joints.verticalRight,
				Utils.Drawing.Edges.horizontal + Utils.Drawing.Edges.horizontal + Utils.Drawing.Edges.horizontal,
				Utils.Drawing.Joints.cross,
				Utils.Drawing.Joints.verticalLeft,
				8
				) + "\n";

		// Build the bottom edge
		String bottom = Utils.buildRow(
				"   " + Utils.Drawing.Corners.bottomLeft,
				Utils.Drawing.Edges.horizontal + Utils.Drawing.Edges.horizontal + Utils.Drawing.Edges.horizontal,
				Utils.Drawing.Joints.horizontalUp,
				Utils.Drawing.Corners.bottomRight,
				8
				) + "\n";

		// Assemble the board
		StringBuilder sb_out = new StringBuilder();

		String prefix = top;
		int rankIndex = 8;

		boolean squareColor = true; // white, starting at a8

		// Build each row
		for (int row = 7; row >= 0; row--) {
			Piece[] rank = squares[row];
			sb_out.append(prefix);

			sb_out.append(' ').append(rankIndex--).append(' ');
			for (Piece piece : rank) {
				sb_out
					.append(Utils.Drawing.Edges.vertical)
					.append(squareColor ? Utils.Styles.lightSquare : Utils.Styles.darkSquare)
					.append(' ')
					.append(piece == null ? '-' : piece)
					.append(' ')
					.append(Utils.Styles.reset)
					;
				squareColor = !squareColor;
			}
			squareColor = !squareColor;

			sb_out
				.append(Utils.Drawing.Edges.vertical)
				.append('\n')
				;

			prefix = rowSep;
		}

		sb_out.append(bottom);
		sb_out.append("     a   b   c   d   e   f   g   h  ");

		return sb_out.toString();
	}
}
