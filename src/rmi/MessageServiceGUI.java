package rmi;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JSpinner;
import javax.swing.JToggleButton;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;


public class MessageServiceGUI {

    /* OBJEKTVARIABLEN */
    //private int numberOfCars;                 // Einstellbare Anzahl der Wagen
    //private int numberOfLaps;                 // Einstellbare Anzahl der Runden
    //private int maxLapTime;                   // Einstellbare Laenge einer zufaelligen Runde in Millisekunden
    //private static boolean isPaused;          // Indikator fuer das Pausieren eines Rennens
    //private static boolean accidentActivated; // Indikator fuer aktivierten Unfall eines Rennens
    private int t;
    
    private JFrame frame;               // Hauptfenster des Programms
    private JPanel northPanel;          // Panel mit den Start-, Pause- und Exit-Buttons
    private JPanel southPanel;          // Panel mit der Voreinstellungen
    private JPanel centerPanel;         // Panel fuer die Anzeige der Renninformationen

    private JButton newMessageButton;    // Button zum Starten des Rennens
    private JButton nextMessageButton;   // Button zum Beenden des Programms
    private JButton exitButton;    // Button zum Starten des Rennens
    //private JToggleButton pauseButton; // ToggleButton zum Pausieren des Rennens

    //private JSpinner selectCars;        // JSpinner fuer die Auswahl der Wagen
    //private JSpinner selectLaps;        // JSpinner fuer die Auswahl der Rennrunden
    //private JSpinner selectMaxLapTime;  // JSpinner fuer die Auswahl der maximalen zufaelligen Rundenzeit
    //private JCheckBox checkbox;         // CheckBox fuer die Aktivierung eines Rennunfalls

    //private JProgressBar[] progressBar; // JProgressBar-Array fuer die Fortschrittsanzeigen der einzelnen Autos
    //private JLabel[] carInfo;           // JLabel-Array fuer die Anzeige der Information der einzelnen Autos


    /* KONSTRUKTOREN */
    public MessageServiceGUI() {

        frame = new JFrame("RaceManager GUI Version");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setIconImage(frame.getToolkit().getImage("favicon.png"));
        frame.setMinimumSize(new Dimension(640, 480));
        frame.setResizable(false);

        /* Panels erstellen lassen*/
        createNorth();  // Panel fuer die Buttons und Start des Rennens
        createSouth();  // Panel fuer die Konfiguration eines Rennens

        frame.pack();                       // Fenstergroesse optimal anpassen
        frame.setLocationRelativeTo(null);  // Programmfenster zentriert starten
        frame.validate();                   // Container ausrichten
        frame.setVisible(true);             // Fenster sichtbar machen

    } // GUIController()


    /* METHODEN */
    private void createNorth() {

        /* NORTH Panel initialisieren und mit Rahmen zeichnen */
        northPanel = new JPanel();
        northPanel.setBorder(BorderFactory.createEtchedBorder(1));

        /* Start-Button erzeugen*/
        newMessageButton = new JButton("New message");
        northPanel.add(newMessageButton);

        /* ActionListener als anonyme Klasse einbauen */
        newMessageButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                newMessageButton.setEnabled(false);
                // send new message method();

            } // actionPerformed()
        });

        /* Pause-Button erzeugen */
        nextMessageButton = new JButton("Next message");
        nextMessageButton.setEnabled(false);
        northPanel.add(nextMessageButton);

        /* ActionListener als anonyme Klasse einbauen */
        nextMessageButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                nextMessageButton.setEnabled(false);
                // receive next message method();

            } // actionPerformed()
        });

        /* Exit-Button erzeugen */
        exitButton = new JButton("Exit");
        northPanel.add(exitButton);

        /* ActionListener als anonyme Klasse einbauen */
        exitButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            } // actionPerformed()
        });

        frame.getContentPane().add(northPanel, BorderLayout.NORTH);

    } // createNorth()


    private void createSouth() {

        /* SOUTH Panel initialisieren und Rahmen zeichnen */
        southPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        southPanel.setBorder(BorderFactory.createEtchedBorder(1));

//        /* JSpinner samt Bezeichnung fuer die Anzahl der Wagen einfuegen */
//        JPanel anzWagenPanel = new JPanel();
//        anzWagenPanel.setBorder(BorderFactory.createMatteBorder(2, 2, 2, 2, Color.BLUE));
//        anzWagenPanel.add(new JLabel("Anzahl der Autos:"));
//        selectCars = new JSpinner(new SpinnerNumberModel(1,1,20,1));// Default und Min 1 Auto, Max 12 Autos
//        anzWagenPanel.add(selectCars);
//        anzWagenPanel.setToolTipText("Bitte Anzahl der Wagen zwischen 1 und 12 eingeben");
//        selectCars.addChangeListener(new JSpinnerListener());        
//        southPanel.add(anzWagenPanel);
//
//        /* JSpinner samt Bezeichnung fuer die Anzahl der Rennrunden einfuegen */
//        JPanel anzRundenPanel = new JPanel();
//        anzRundenPanel.setBorder(BorderFactory.createMatteBorder(2, 2, 2, 2, Color.BLUE));
//        anzRundenPanel.add(new JLabel("Anzahl der Runden:"));
//        selectLaps = new JSpinner(new SpinnerNumberModel(1,1,100,1)); // Default und Min 1 Runde, Max 100 Runden
//        anzRundenPanel.add(selectLaps);
//        anzRundenPanel.setToolTipText("Bitte Anzahl der Rennrunden zwischen 1 und 100 eingeben");
//        selectLaps.addChangeListener(new JSpinnerListener());
//        southPanel.add(anzRundenPanel);
//
//        /* JSpinner samt Bezeichnung fuer die maximale Rundenzeit pro Auto einfuegen */
//        JPanel rundenLaengePanel = new JPanel();
//        rundenLaengePanel.setBorder(BorderFactory.createMatteBorder(2, 2, 2, 2, Color.BLUE));
//        rundenLaengePanel.add(new JLabel("Länge einer Runde:"));        
//        selectMaxLapTime = new JSpinner(new SpinnerNumberModel(100,100,1000,100)); // Default und Min 100ms, Max 1000ms, 100ms Schritte
//        rundenLaengePanel.add(selectMaxLapTime);
//        rundenLaengePanel.setToolTipText("Bitte die max. Rundenzeit pro Auto zwischen 100 und 1000 Millisekunden eingeben");
//        selectMaxLapTime.addChangeListener(new JSpinnerListener());        
//        southPanel.add(rundenLaengePanel);

//        /* Checkbox fuer Unfall-Aktivierung einfuegen */
//        checkbox = new JCheckBox("UNFALL AKTIVIEREN");
//
//        /* ItemListener fuer die CheckBox ueber eine anonyme Klasse hinzufuegen */
//        checkbox.addItemListener(new ItemListener() {
//
//            @Override
//            public void itemStateChanged(ItemEvent e) {
//
//                if (e.getSource() == checkbox) {
//                    if (checkbox.isSelected()) {
//                        accidentActivated = true;
//                    } else {
//                        accidentActivated = false;
//                    } // else
//                } // if
//            } // itemStateChanged()
//
//        });
//
//        checkbox.setToolTipText("Unfall aktivieren / deaktivieren");
//        southPanel.add(checkbox);

        frame.getContentPane().add(southPanel, BorderLayout.SOUTH);

    } // createSouth()


    private void createCenter() {

        /* CENTER Panel initialisieren und Rahmen zeichnen */
        centerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        centerPanel.setBorder(BorderFactory.createEtchedBorder(1));

//        JPanel carIDPanel       = new JPanel(new GridLayout(numberOfCars, 1, 0, 1));
//        JPanel carProgressPanel = new JPanel(new GridLayout(numberOfCars, 1, 0, 3));
//        JPanel carInfoPanel     = new JPanel(new GridLayout(numberOfCars, 1, 0, 1));
//
//        /* Wagennummer in das carIDPanel eintragen */
//        for (int i = 0; i < numberOfCars; i++) {
//            carIDPanel.add(new JLabel("Wagen " + (i + 1)));
//        } // for
//
//        /* Erstelle JProgressbar-Array */
//        progressBar = new JProgressBar[numberOfCars];
//        for (int i = 0; i < numberOfCars; i++) {
//            progressBar[i] = new JProgressBar(0, numberOfLaps);
//            progressBar[i].setStringPainted(false); // Aktivieren der Prozentanzeige
//            carProgressPanel.add(progressBar[i]);
//        } // for
//
//        /* Erstelle carInfo-Array */
//        carInfo = new JLabel[numberOfCars];
//        for (int i = 0; i < numberOfCars; i++) {
//            carInfo[i] = new JLabel();
//            carInfoPanel.add(carInfo[i]);
//        } // for
//
//        /* Panels und Progressbars dem centerPanel hinzufuegen */
//        centerPanel.add(carIDPanel);
//        centerPanel.add(carProgressPanel);
//        centerPanel.add(carInfoPanel);

        frame.getContentPane().add(centerPanel, BorderLayout.CENTER);

    } // createCenter()
    
    public static void main(String[] args) {
        MessageServiceGUI gui = new MessageServiceGUI();
       

    }

}
