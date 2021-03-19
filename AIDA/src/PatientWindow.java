import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Map;


public class PatientWindow implements MouseListener, ActionListener, KeyListener {
    private java.sql.Connection connection;
    private Map<String, Exam> dayExams;
    private String numExam;
    private String user;
    private Dimension maxDimension;
    private App app;
    private JFrame frame;
    private JMenu mainMenu;
    private JMenu historyMenu;
    private JTable table;
    private JScrollPane scrollPane;
    private JButton beginExam;


    public PatientWindow(java.sql.Connection connection, Map<String, Exam> dayExams, String numExam, String user) {
        this.connection = connection;
        this.dayExams = dayExams;
        this.numExam = numExam;
        this.user = user;
        this.app = new App();
        initComponents();
    }

    private void initComponents() {
        this.maxDimension = Toolkit.getDefaultToolkit().getScreenSize();
        this.frame = this.app.createFrame(0);
        createMenuBar();
        createTable();
        createBeginExamButton();
    }

    private void createMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        this.mainMenu = this.app.createMenu("Início");
        this.historyMenu = this.app.createMenu("Histórico");
        this.mainMenu.addMouseListener(this);
        this.historyMenu.addMouseListener(this);
        menuBar.add(this.mainMenu);
        menuBar.add(this.historyMenu);
        this.frame.setJMenuBar(menuBar);
    }

    private void createTable() {
        String[][] exam = this.app.getExam(this.dayExams.get(this.numExam));
        String[] columnNames = {" ", " "};
        this.table = this.app.createTable(exam, columnNames);
        this.table.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "none");
        this.table.addKeyListener(this);
        this.scrollPane = new JScrollPane(this.table);
        this.scrollPane.setBounds(0, 0, this.maxDimension.width, 180);
        this.frame.add(this.scrollPane);
    }

    private void createBeginExamButton() {
        this.beginExam = this.app.createButton("Iniciar Exame", this.maxDimension.width/2-60, 210,120,35, 15);
        this.beginExam.addActionListener(this);
        this.frame.add(this.beginExam);
    }

    private void beginExam() {
        this.frame.setVisible(false);
        new ExamWindow(this.connection, this.dayExams, this.numExam, this.user);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == this.beginExam) {
            beginExam();
        }
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if (e.getSource() == this.mainMenu) {
            this.frame.setVisible(false);
            new MainWindow(this.connection, this.dayExams, this.user);
        }
        else if (e.getSource() == this.historyMenu) {
            this.frame.setVisible(false);
            new HistoryWindow(this.connection, this.dayExams, this.user);
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {

    }

    @Override
    public void mouseReleased(MouseEvent e) {

    }

    @Override
    public void mouseEntered(MouseEvent e) {
        if (e.getSource() == this.mainMenu) {
            this.mainMenu.setOpaque(true);
            this.mainMenu.setBackground(Color.white);
        }
        else if (e.getSource() == this.historyMenu) {
            this.historyMenu.setOpaque(true);
            this.historyMenu.setBackground(Color.white);
        }
    }

    @Override
    public void mouseExited(MouseEvent e) {
        if (e.getSource() == this.mainMenu) {
            this.mainMenu.setOpaque(false);
            Color color = UIManager.getColor ( "Menu.background" );
            this.mainMenu.setBackground(color);
        }
        else if (e.getSource() == this.historyMenu) {
            this.historyMenu.setOpaque(false);
            Color color = UIManager.getColor ( "Menu.background" );
            this.historyMenu.setBackground(color);
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {

    }

    @Override
    public void keyPressed(KeyEvent e) {

    }

    @Override
    public void keyReleased(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ENTER) {
            this.beginExam.doClick();
        }
    }
}