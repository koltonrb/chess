import chess.*;
import client.ChessClient;

public class Main {
    public static void main(String[] args) {
//        var piece = new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.PAWN);
//        System.out.println("♕ 240 Chess Client: " + piece);
        int serverPort = 8080;
        if (args.length == 1) {
            try {
                serverPort = Integer.parseInt(args[0]);
            } catch (Throwable ex) {
                System.err.println("Invalid port number: '" + args[0] +"'.  Using default port " + serverPort);
            }
        }

        try {
            ChessClient client = new ChessClient(serverPort);
            client.start();
        } catch (Throwable ex){
            System.out.printf("Unable to start server: %s%n", ex.getMessage());
        }

    }
}