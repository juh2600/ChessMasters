package edu.neumont.chessmasters;

import edu.neumont.chessmasters.controllers.PlayerMove;
import edu.neumont.chessmasters.events.EventListener;
import edu.neumont.chessmasters.events.EventRegistry;
import me.travja.utils.utils.IOUtils;
import org.fusesource.jansi.AnsiConsole;

import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.security.CodeSource;

public class ChessMasters {

    public static boolean    debug = false;
    public static PlayerMove controller;

    private static boolean arrayContains(String[] arr, String test) {
        for (String s : arr) {
            if (s.equalsIgnoreCase(test))
                return true;
        }

        return false;
    }

    public static void main(String[] args) {

        //This just allows the jar to be double-clicked in windows.
        if (!System.getProperty("os.name").toLowerCase().contains("windows") || (args.length >= 1 && (arrayContains(args, "-start") || arrayContains(args, "-debug")))) {
            debug = arrayContains(args, "-debug");
            registerEvents();
            startGame();
        } else {
            try {
                CodeSource codeSource = ChessMasters.class.getProtectionDomain().getCodeSource();
                String path = codeSource.getLocation().toURI().getPath();
                String jarDir = new File(path).getParentFile().getPath();
                String fileName = new File(path).getName();
                Runtime.getRuntime().exec(new String[]{"cmd.exe", "/c start cmd.exe /k \"java -jar \"" + jarDir + File.separator + fileName + "\" -start\""});
            } catch (IOException | URISyntaxException e) {
                e.printStackTrace();
            }
        }
    }

    public static void startGame() {
        boolean playAgain;
        checkColorSupport();
        if (System.console() == null) {
            System.out.println(" ----[ IMPORTANT ]----\n" +
                    "This game session is not directly attached to a Console object. (Either stdin or stdout is being redirected.)\n" +
                    "If you enter a null string for any prompt, the application WILL terminate.\n" +
                    "Chances are, you know what you're doing, but if you accessed the application in a normal way, please let the developers know that you're receiving this error.\n");
        }

        try {
            do {
                //Run setup here.
//                Board board = new Board("r2qk2r/8/8/8/8/8/8/R2QK2R w KQkq - 0 1");
                controller = new PlayerMove();
                controller.run();
                playAgain = IOUtils.promptForBoolean("Play again? (y/n)", "y", "n");
            } while (playAgain);
        } catch (EOFException e) {
            System.err.println("Input stream was terminated. Exiting program.");
        }
        System.out.println("Goodbye");
    }

    public static void checkColorSupport() {
        if (System.getProperty("os.name").startsWith("Windows")) {
            AnsiConsole.systemInstall();
            Utils.USE_ANSI = true;
        }
    }

    private static void registerEvents() {
        EventRegistry.registerEvents(new EventListener());
    }
}
