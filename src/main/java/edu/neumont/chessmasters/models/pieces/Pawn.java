package edu.neumont.chessmasters.models.pieces;

import edu.neumont.chessmasters.models.Location;

public class Pawn extends Piece {

    public Pawn(PieceColor color) {
        super(color, "p");
    }

    @Override
    public boolean validateMove(String move) {
        move = move.toLowerCase();
        boolean canMove = false;

        int y = getLocation().getY();
        int newY = Location.getY(move);
        if ((getColor() == PieceColor.WHITE && newY > y) ||
                (getColor() == PieceColor.BLACK && newY < y)) {
            int dx = Math.abs(getLocation().getX() - Location.getX(move));
            int dy = Math.abs(y - newY);
            canMove = (dx <= 1 && dy <= 1) || (numMoves == 0 && dx == 0 && dy <= 2);
        }

        return canMove;
    }

}
