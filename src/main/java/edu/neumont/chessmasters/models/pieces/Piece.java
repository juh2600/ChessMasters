package edu.neumont.chessmasters.models.pieces;

import edu.neumont.chessmasters.models.Location;

public abstract class Piece {

    protected final PieceColor color;

    protected int numMoves = 0;
    protected Location location;

    public Piece(PieceColor color) {
        this.color = color;
    }

    public PieceColor getColor() {
        return color;
    }

    public int getNumMoves() {
        return numMoves;
    }

    public void setNumMoves(int numMoves) {
        this.numMoves = numMoves;
    }

    public Location getLocation() {
        return location;
    }


    public void setLocation(String location) {
        this.location = new Location(location);
    }

    /**
     * Attempts to move the piece to the given location. Returns false if the move is illegal.<br/>
     * If the piece's location has not yet been set, this will behave the same as {@link #setLocation(String)}
     * <br/>
     * <br/>
     * Please note, this does not check if another piece exists at that location already.
     *
     * @param location The coordinate location of the position to move to (ie 'a2')
     * @return boolean - Whether or not the move has been made.
     */
    public boolean move(String location) {
        location = location.toLowerCase();
        if (this.getLocation() != null && !validateMove(location))
            return false;

        this.location = new Location(location);
        numMoves++;
        return true;
    }

    public abstract boolean validateMove(String move);

}
