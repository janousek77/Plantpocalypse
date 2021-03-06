/**
 * This class contains the GUI elements for the Game.
 *
 * Only one GUI or CLI is meant to run at any given time.
 *
 *  * @author Jeffrey Haywood
 *  * @date September 8th, 2020
 *  * @version 0.1
 */
package com.plantpocalypse.view;

import com.plantpocalypse.controller.GameDirector;
import com.plantpocalypse.model.Game;
import com.plantpocalypse.util.AudioTools;
import com.plantpocalypse.util.Dialogue;
import com.plantpocalypse.util.TextParser;
import com.plantpocalypse.util.ImageTools;

import javax.sound.sampled.Clip;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

public class GameGUI implements ActionListener {
    private final Game game = Game.GAME_INSTANCE;

    // Theme music
    private Clip THEME_MUSIC;
    // Main window
    private final JFrame gameFrame;
    // Container for user input and other status displays
    private final JPanel userInputPanel;
    // Helper panel to organize status components
    private final JPanel[][] panelHolderInput;
    private final JScrollPane scrollPane;

    private final JButton newGameButton, loadGameButton, tutorialButton;
    private final JLabel inputFieldLabel, currentRoomLabel, currentHealthLabel, movesMadeLabel;
    private final JTextArea dialogueText;
    private final JTextField inputField;

    private final JMenu menu;
    private final JMenuItem newGame, save, load, help, about, quit, tutorial;
    private final JMenuBar menuBar;

    // Containers for mini map and title screen
    private final JPanel HUD_CONTAINER, SUB_CONTAINER_N, SUB_CONTAINER_S;
    // Panels for each floor's mini map
    private final JPanel FLOOR_1_PANEL, FLOOR_2_PANEL, FLOOR_0_PANEL;
    // Special case HUD views
    private final JPanel TITLE_SCREEN_PANEL, MONSTER_PANEL, ELIXIR_PANEL, HIDDEN_OFFICE;
    private final JPanel currentRoomIcon, roomStatusContainer;

    // containers for pop up
    private static JTextField inputBox;
    private JDialog dialog;
    Boolean lockUp = false;

    /**
     * CTOR for the GUI.
     * Renders all of the Components needed, sets up
     * ActionListener for when Player presses enter in InputField.
     */

    public GameGUI() {
        /* Instantiate Window and Containers */
        gameFrame = new JFrame();

        /* Instantiate Menu Components */
        menu = new JMenu("Menu");
        newGame = new JMenuItem("New Game");
        newGame.addActionListener(this);
        save = new JMenuItem("Save");
        save.addActionListener(this);
        tutorial = new JMenuItem("Tutorial");
        tutorial.addActionListener(this);
        load = new JMenuItem("Load");
        load.addActionListener(this);
        help = new JMenuItem("Help");
        help.addActionListener(this);
        about = new JMenuItem("About");
        about.addActionListener(this);
        quit = new JMenuItem("Quit");
        quit.addActionListener(this);
        menuBar = new JMenuBar();
        menu.add(newGame);
        menu.add(save);
        menu.add(tutorial);
        menu.add(load);
        menu.add(help);
        menu.add(about);
        menu.add(quit);
        menuBar.add(menu);
        gameFrame.setJMenuBar(menuBar);

        newGameButton = new JButton("New Game");
        newGameButton.addActionListener(this);
        tutorialButton = new JButton("Tutorial");
        tutorialButton.addActionListener(this);
        loadGameButton = new JButton("Load Game");
        loadGameButton.addActionListener(this);

        /* Create JPanel placeholder so component can be put in specific Grid cell */
        int rows = 2;
        int cols = 3;
        userInputPanel = new JPanel(new GridLayout(rows, cols));
        panelHolderInput = new JPanel[rows][cols];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                panelHolderInput[i][j] = new JPanel();
                userInputPanel.add(panelHolderInput[i][j]);
            }
        }

        /* Set attributes for Window */
        gameFrame.setLayout(new BorderLayout());
        gameFrame.setTitle("Plantpocalypse");
        gameFrame.setSize(1600,1000);

        // Add component containers for Heads Up Display to the main frame
        HUD_CONTAINER = new JPanel(new BorderLayout());
        SUB_CONTAINER_N = new JPanel(new BorderLayout());
        SUB_CONTAINER_S = new JPanel(new BorderLayout());

        HUD_CONTAINER.add(SUB_CONTAINER_N, BorderLayout.NORTH);
        HUD_CONTAINER.add(SUB_CONTAINER_S, BorderLayout.SOUTH);

        TITLE_SCREEN_PANEL = ImageTools.createJPanelFromPath("./resources/plantpocalypse_title.png");
        SUB_CONTAINER_N.add(TITLE_SCREEN_PANEL, BorderLayout.NORTH);
        gameFrame.add(HUD_CONTAINER, BorderLayout.WEST);
        gameFrame.add(userInputPanel, BorderLayout.SOUTH);
        roomStatusContainer = new JPanel(){
            @Override
            public boolean isOptimizedDrawingEnabled() {
                return false;
            }
        };
        roomStatusContainer.setLayout(new OverlayLayout(roomStatusContainer));


        /* Instantiate components for User Input section */
        inputFieldLabel = new JLabel("Enter command: ");
        inputField = new JTextField(16);
        inputField.setForeground(Color.white);
        inputField.setBackground(Color.black);
        currentRoomLabel = new JLabel();
        currentHealthLabel = new JLabel();
        movesMadeLabel = new JLabel();

        // Instantiate a background color panel for current room label
        currentRoomIcon  = new JPanel();
        currentRoomIcon.setPreferredSize(new Dimension(125,50));
        currentRoomIcon.setMaximumSize(currentRoomIcon.getPreferredSize());
        currentRoomIcon.setMinimumSize(currentRoomIcon.getPreferredSize());


        roomStatusContainer.add(currentRoomLabel);
        roomStatusContainer.add(currentRoomIcon);

        /* Instantiate TextArea for dialogue and set attributes */
        dialogueText = new JTextArea();
        dialogueText.setEditable(false);
        dialogueText.setBackground(Color.black);
        dialogueText.setForeground(Color.white);
        dialogueText.setLineWrap(true);
        scrollPane = new JScrollPane(dialogueText, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setAutoscrolls(true);
        gameFrame.add(scrollPane);

        /* Event listener for when Player press enter in the input field */
        inputField.addActionListener(this);

        /* Add related components to user input Grid */
        panelHolderInput[0][0].add(roomStatusContainer);
        panelHolderInput[0][1].add(currentHealthLabel);
        panelHolderInput[0][2].add(movesMadeLabel);
        panelHolderInput[1][0].add(inputFieldLabel);
        panelHolderInput[1][1].add(inputField);

        // Initialize HUD with images for special case views
        MONSTER_PANEL = ImageTools.createJPanelFromPath("./resources/plant_monster.png");
        MONSTER_PANEL.setVisible(false);
        ELIXIR_PANEL = ImageTools.createJPanelFromPath("./resources/elixir.png");
        ELIXIR_PANEL.setVisible(false);
        HIDDEN_OFFICE = ImageTools.createJPanelFromPath("./resources/map_hidden_office_unlocked.png");
        HIDDEN_OFFICE.setVisible(false);

        // Set up floor1 and floor2 containers to allow overlays in mini map drawing
        FLOOR_1_PANEL = new JPanel() {
            public boolean isOptimizedDrawingEnabled() {
                return false;
            }
        };
        LayoutManager overlay = new OverlayLayout(FLOOR_1_PANEL);
        FLOOR_1_PANEL.setLayout(overlay);

        FLOOR_2_PANEL = new JPanel() {
            public boolean isOptimizedDrawingEnabled() {
                return false;
            }
        };
        overlay = new OverlayLayout(FLOOR_2_PANEL);
        FLOOR_2_PANEL.setLayout(overlay);

        FLOOR_0_PANEL = new JPanel() {
            public boolean isOptimizedDrawingEnabled() {
                return false;
            }
        };
        overlay = new OverlayLayout(FLOOR_0_PANEL);
        FLOOR_0_PANEL.setLayout(overlay);



        /* Attributes to set after all components added to Window */
        gameFrame.setDefaultCloseOperation(gameFrame.EXIT_ON_CLOSE);
        gameFrame.setVisible(true);
        dialogueText.setText("\tSelect Menu > New Game to start a new game.\n\tSelect Menu > Load Game to load a save game.\n\tSelect Menu > Tutorial to play the tutorial.");
        dialog = null;

        // Set color of the diaglouge text
        dialogueText.setForeground(new Color(224, 211, 133));
    }


    // Event listener for user interaction with GUI mainframe
    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == newGame || e.getSource() == newGameButton) {
            startGame();
        }
        else if (e.getSource() == save) {
            game.saveGame();
        }
        else if (e.getSource() == tutorial || e.getSource() == tutorialButton) {
            startTutorial();
        }
        else if (e.getSource() == load || e.getSource() == loadGameButton) {
            loadSavedGame();
        }
        else if (e.getSource() == help) {
            help();
        }
        else if (e.getSource() == about) {
            about();
        }
        else if (e.getSource() == quit) {
            System.exit(0);
        }
        else if (e.getSource() == inputField) {
            String inputString = inputField.getText();
            inputField.setText("");
            // 1) Formats user input into one or two string commands with TextParser
            // 2) Validates user input command with TextParser, returning command as List<Strings>
            // 3) Uses GameDirector to enact command, returning result string to show user
            String result = GameDirector.interact(TextParser.getInputFromGUI(inputString));
            if(result.contains("Moved to")) {
                dialogueText.setText("");
            }
            if(result.contains("You opened the")) {
                int currentFloor = game.getPlayer().getCurrentRoom().getFloorNumber();
                result = "You opened the map.";
                // Point at the map file that corresponds to the current floor and display in a pop up
                String pathName = "./resources/map_background_floor_" + currentFloor + ".png";
                JPanel imageHolder = ImageTools.createJPanelFromPath(pathName, 800, 500);
                JOptionPane.showMessageDialog(gameFrame, imageHolder);

            }

            if(result == null || result == "")
                result = "Not a valid command. Type help if you need a list of possible commands";
            // If player moved to a new floor, toggle mini map visibility for both floors (one on, the other off)
            if(result.contains("Moved to Floor ")) {
                swapFloorPanelVisibility(FLOOR_1_PANEL, FLOOR_2_PANEL);
            }

            displayDialogue(result);
            if(result == "You pick the book off the shelf and find a hidden keypad behind it") {
                createUI(gameFrame);
            }
            displayStatus();

            if (game.checkGameOver()) {
                if (game.checkLostGame()) { lost();}
                if (game.checkTutorialComplete()) {
                    completedTutorial();
                    gameOver();
                }
                if (game.checkPlayerWon()) {
                    won();
                    gameOver();
                }
            }
        }
        // Handling combo lock puzzle for hidden office
        else if (e.getActionCommand() != null) {
            String command = e.getActionCommand().strip();
            if (command == "Clear") {
                inputBox.setText("");
            }else if (command == "Enter") {
                if(inputBox.getText().equals("092320")) {
                    inputBox.setText("Opened");
                    Timer timer = new Timer(1000, close);
                    timer.setRepeats(false);
                    timer.start();
                } else {
                    inputBox.setText("Wrong code");
                    Timer timer = new Timer(1000, clear);
                    timer.setRepeats(false);
                    timer.start();
                }
            }else {
                inputBox.setText(inputBox.getText() + command);
            }
        }
    }


    /**
     * Changes the text on the currentRoomLabel to Player's current room.
     * @param currentRoom The current room the Player is in.
     */
    public void displayCurrentRoom(String currentRoom) {
        currentRoomLabel.setText("<html>"+
//                "Current Room: " +
                "<font color = black>"+ currentRoom + "</html>");
        // Set background color here according to room
        int roomColor = Game.GAME_INSTANCE.getPlayer().getCurrentRoom().getColor();
        currentRoomIcon.setBackground(new Color(roomColor));
    }

    /**
     * Changes the text on the currentHealthLabel to Player's current health
     * out of their total health.
     * @param currentHealth The Player's current health.
     * @param totalHealth The total amount of health a Player can have.
     */
    public void displayPlayerHealth(int currentHealth, int totalHealth) {
        currentHealthLabel.setText("<html>"+ "Health: " + "<font color = red>" + currentHealth + "/" + totalHealth + "</html>");
    }

    /**
     * Changes the text on the movesMadeLabel to show the amount of times the
     * Player has moved between rooms.
     * @param movesMade Number of moves between rooms player has made.
     */
    public void displayMovesMade(int movesMade, int totalMoves) {
        movesMadeLabel.setText("<html>"+ "Moves Made: " + "<font color = red>" + movesMade + "/" + totalMoves + "</html>");
    }

    /**
     * Appends to dialogueText String from action or dialogue.
     * @param dialogue Story Dialogue or results from when command performed.
     */
    public void displayDialogue(String dialogue) {
        dialogueText.append(dialogue + "\n");
    }

    public void displayStatus() {
        displayCurrentRoom(game.getPlayer().getCurrentRoom().getName());
        displayPlayerHealth(game.getPlayer().getCurrentHealth(), game.getPlayer().getMaxHealth());
        displayMovesMade(game.getPlayer().getMovesMade(), game.getAllowedMoves());
        displayMonster();
        displayElixir();
    }

    private void displayMonster() {
        if (game.getPlayer().getCurrentRoom().getMonster() != null) {
            MONSTER_PANEL.setVisible(true);
        } else {
            MONSTER_PANEL.setVisible(false);
        }
    }

    private void displayElixir() {
        if (game.getPlayer().getCurrentRoom().getItems().containsKey("elixir")) {
            ELIXIR_PANEL.setVisible(true);
        } else {
            ELIXIR_PANEL.setVisible(false);
        }
    }

    // Methods for building and tearing down panels/containers used in HUD
    private void initializeFloorPanels(ComponentMap componentMap, JPanel panel) {
        // Add each component in the ComponentMap to the proper floor's JPanel container
        componentMap.getComponentMap().forEach((entry, component) -> {
            panel.add(component);
        });
    }

    private void resetFloorPanelVisibility() {
        FLOOR_1_PANEL.setVisible(true);
        FLOOR_2_PANEL.setVisible(false);
    }

    // Might have to update this for additional floors in the future
    public void swapFloorPanelVisibility(JPanel panel1, JPanel panel2){
        // Toggles visibility for each floor mini maps
        panel1.setVisible(!panel1.isVisible());
        panel2.setVisible(!panel2.isVisible());
    }

    private void tearDownPanel(JPanel panel) {
        for (Component component : panel.getComponents()) {
            panel.remove(component);
        }
    }
    private void tearDownPanels(ArrayList<JPanel> container) {
        container.forEach(panel -> {
            tearDownPanel(panel);
        });
    }


    /**
     * Calls methods to display beginning of story and game data to
     * the GUI.
     */

    public void startGame() {
        try {
            THEME_MUSIC.stop();
        } catch (Exception e) {}
        dialogueText.setText("\t\t");
        game.loadAssets();
        tearDownPanels(new ArrayList<JPanel>(java.util.List.of(FLOOR_0_PANEL, FLOOR_1_PANEL, FLOOR_2_PANEL, SUB_CONTAINER_N, SUB_CONTAINER_S)));
        FLOOR_1_PANEL.add(HIDDEN_OFFICE);
        HIDDEN_OFFICE.setVisible(false);
        initializeFloorPanels(game.floor1, FLOOR_1_PANEL);
        initializeFloorPanels(game.floor2, FLOOR_2_PANEL);
        title();
        intro();
        displayStatus();
        scrollPane.setVisible(true);
        userInputPanel.setVisible(true);
        HUD_CONTAINER.setVisible(true);

        SUB_CONTAINER_N.add(MONSTER_PANEL, BorderLayout.NORTH);
        SUB_CONTAINER_N.add(ELIXIR_PANEL, BorderLayout.SOUTH);
        SUB_CONTAINER_S.add(FLOOR_1_PANEL, BorderLayout.NORTH);
        SUB_CONTAINER_S.add(FLOOR_2_PANEL, BorderLayout.SOUTH);
        resetFloorPanelVisibility();
        THEME_MUSIC = AudioTools.Music.playTheme();
    }

    public void startTutorial() {
        try {
            THEME_MUSIC.stop();
        } catch (Exception e) {}
        dialogueText.setText("\t\t");
        game.loadAssetsTutorial();
        tearDownPanels(new ArrayList<JPanel>(java.util.List.of(FLOOR_0_PANEL, FLOOR_1_PANEL, FLOOR_2_PANEL, SUB_CONTAINER_N, SUB_CONTAINER_S)));
        initializeFloorPanels(game.floor1, FLOOR_1_PANEL);
        initializeFloorPanels(game.floor2, FLOOR_2_PANEL);
        initializeFloorPanels(game.floor0, FLOOR_0_PANEL);
        title();
        introTutorial();
        displayStatus();
        scrollPane.setVisible(true);
        userInputPanel.setVisible(true);
        HUD_CONTAINER.setVisible(true);
        SUB_CONTAINER_N.add(TITLE_SCREEN_PANEL, BorderLayout.NORTH);
        THEME_MUSIC = AudioTools.Music.playTheme();

    }

    public void loadSavedGame() {
        try {
            THEME_MUSIC.stop();
        } catch (Exception e) {}
        dialogueText.setText("");
        HIDDEN_OFFICE.setVisible(false);
        game.loadGame();
        tearDownPanels(new ArrayList<JPanel>(java.util.List.of(FLOOR_0_PANEL, FLOOR_1_PANEL, FLOOR_2_PANEL, SUB_CONTAINER_N, SUB_CONTAINER_S)));
        FLOOR_1_PANEL.add(HIDDEN_OFFICE);
        initializeFloorPanels(game.floor1, FLOOR_1_PANEL);
        initializeFloorPanels(game.floor2, FLOOR_2_PANEL);
        displayStatus();
        scrollPane.setVisible(true);
        userInputPanel.setVisible(true);
        HUD_CONTAINER.setVisible(true);
        SUB_CONTAINER_N.add(MONSTER_PANEL, BorderLayout.NORTH);
        SUB_CONTAINER_N.add(ELIXIR_PANEL, BorderLayout.SOUTH);
        SUB_CONTAINER_S.add(FLOOR_1_PANEL, BorderLayout.NORTH);
        SUB_CONTAINER_S.add(FLOOR_2_PANEL, BorderLayout.SOUTH);
        resetFloorPanelVisibility();
        // If player loaded a game from floor 2, make sure mini map panels are displayed properly
        if (game.getPlayer().getCurrentRoom().getFloorNumber() == 2) {
            swapFloorPanelVisibility(FLOOR_1_PANEL, FLOOR_2_PANEL);
        }
        THEME_MUSIC = AudioTools.Music.playTheme();
    }
//    add more details in the about section
    public void about() {
        JOptionPane.showMessageDialog(gameFrame, "Plantpocalypse A maze mystery game set to test your detective skills. Solve the mystery behind what happen to your Uncle. \nMade by Hunter Clark | Jeffrey Haywood | Maya Marks");
    }

    public void help() {
        JOptionPane.showMessageDialog(gameFrame, Dialogue.helpDialogueGUI());
    }

    /**
     * Appends the title of the game to dialogueArea.
     */
    public void title() {
        displayDialogue(Dialogue.titleScreenDialogue());
    }

    /**
     * Appends the introduction to the game to dialogueArea.
     */
    public void intro() {
        displayDialogue(Dialogue.introDialogue());
    }

    public void introTutorial() {
        displayDialogue(Dialogue.introDialogueTutorial());
    }

    /**
     * Appends losing dialogue to dialogueArea.
     */
    public void lost() {
        displayDialogue(Dialogue.losingDialogue());
    }

    /**
     * Appends winning dialogue to dialogueArea.
     */
    public void won() {
        dialogueText.setText("");
        displayDialogue(Dialogue.winningDialogue());
        displayDialogue(Dialogue.endingDialogue());
    }

    public void completedTutorial() {
        dialogueText.setText("");
        displayDialogue(Dialogue.completedTutorialDialogue());
    }

    /**
     * Appends ending dialogue to dialogueArea.
     */
    public void gameOver() {
        userInputPanel.setVisible(false);
    }

    private void createUI(JFrame frame) {
        JPanel panel = new JPanel();
        GridBagLayout layout = new GridBagLayout();
        GridBagConstraints gbc = new GridBagConstraints();
        panel.setLayout(layout);

        inputBox = new JTextField(10);
        inputBox.setEditable(false);

        JButton button0 = new JButton("0");
        JButton button1 = new JButton("1");
        JButton button2 = new JButton("   2   ");
        JButton button3 = new JButton("3");
        JButton button4 = new JButton("4");
        JButton button5 = new JButton("   5   ");
        JButton button6 = new JButton("6");
        JButton button7 = new JButton("7");
        JButton button8 = new JButton("   8   ");
        JButton button9 = new JButton("9");
        JButton enter = new JButton("Enter");
        JButton clear = new JButton("Clear");

        frame.getContentPane().add(panel, BorderLayout.CENTER);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0; gbc.gridy = 1; panel.add(button1, gbc);
        gbc.gridx = 1; gbc.gridy = 1; panel.add(button2, gbc);
        gbc.gridx = 2; gbc.gridy = 1; panel.add(button3, gbc);
        gbc.gridx = 0; gbc.gridy = 2; panel.add(button4, gbc);
        gbc.gridx = 1; gbc.gridy = 2; panel.add(button5, gbc);
        gbc.gridx = 2; gbc.gridy = 2; panel.add(button6, gbc);
        gbc.gridx = 0; gbc.gridy = 3; panel.add(button7, gbc);
        gbc.gridx = 1; gbc.gridy = 3; panel.add(button8, gbc);
        gbc.gridx = 2; gbc.gridy = 3; panel.add(button9, gbc);
        gbc.gridx = 0; gbc.gridy = 4; panel.add(clear, gbc);
        gbc.gridx = 1; gbc.gridy = 4; panel.add(button0, gbc);
        gbc.gridx = 2; gbc.gridy = 4; panel.add(enter, gbc);
        gbc.gridwidth = 3;
        gbc.gridx = 0; gbc.gridy = 0; panel.add(inputBox, gbc);

        button1.addActionListener(this);
        button2.addActionListener(this);
        button3.addActionListener(this);
        button4.addActionListener(this);
        button5.addActionListener(this);
        button6.addActionListener(this);
        button7.addActionListener(this);
        button8.addActionListener(this);
        button9.addActionListener(this);
        button0.addActionListener(this);
        enter.addActionListener(this);
        clear.addActionListener(this);

        JOptionPane jop = new JOptionPane();
        dialog = jop.createDialog("");
        dialog.setSize(200, 200);
        dialog.setContentPane(panel);
        dialog.setVisible(true);
    }

    ActionListener close = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            game.getPlayer().getCurrentRoom().getNeighboringRooms().get("east").toggleLock();
            displayDialogue("\nYou unlocked the Hidden Office");
            AudioTools.SFX.playDoorUnlocking();
            HIDDEN_OFFICE.setVisible(true);
            dialog.dispose();
        }
    };

    ActionListener clear = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            inputBox.setText("");
        }
    };
}
