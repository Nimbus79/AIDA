import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;


public class MainWindow implements MouseListener, KeyListener {
    private java.sql.Connection connection;
    private Map<String, Exam> dayExams;
    private String user;
    private App app;
    private JFrame frame;
    private JMenu mainMenu;
    private JMenu historyMenu;
    private JTable table;
    private JScrollPane scrollPane;


    public MainWindow(java.sql.Connection connection, String user) throws SQLException {
        this.connection = connection;
        this.user = user;
        this.app = new App();
        getDayExams();
        sortDayExams();
        initComponents();
    }

    public MainWindow(java.sql.Connection connection, Map<String, Exam> dayExams, String user) {
        this.connection = connection;
        this.dayExams = dayExams;
        this.user = user;
        this.app = new App();
        initComponents();
    }

    private void initComponents() {
        this.frame = this.app.createFrame(2);
        createMenuBar();
        createTable();
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
        this.mainMenu.setOpaque(true);
        this.mainMenu.setBackground(Color.white);
    }

    private void createTable() {
        String[][] exams = this.app.getExams(this.dayExams, 2);
        String[] columnNames = {"Nº PEDIDO", "UTENTE"};
        this.table = this.app.createTable(exams, columnNames);
        this.table.addMouseListener(this);
        this.table.addKeyListener(this);
        this.table.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "none");
        this.scrollPane = new JScrollPane(this.table);
        this.frame.add(this.scrollPane);
    }

    private void getDayExams() throws SQLException {
        this.dayExams = new HashMap<>();
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date date = new Date();
        String data = dateFormat.format(date).replaceAll("-", ".");
        PreparedStatement stm = this.connection.prepareStatement("SELECT NUMPEDIDO, NUMPROCESSO, EPISODIO,\n" +
                "NOMEDOENTE, DATANASCIMENTO, DATAMARCACAO, OBSERVACOES\n" +
                "FROM SIL.DPEDIDOS\n" +
                "WHERE IDSER = 'BRO' AND POSTO = 'BR1' AND (DATAMARCACAO BETWEEN to_date(?,'YYYY.MM.DD HH24:MI:SS') AND\n" +
                "to_date(?,'YYYY.MM.DD HH24:MI:SS')) AND (MARCADO BETWEEN 2 AND 7)");
        stm.setString(1, data + " 00:00:00");
        stm.setString(2, data + " 23:59:59");
        ResultSet rs = stm.executeQuery();
        while (rs.next()) {
            String numExam = rs.getString("NUMPEDIDO");
            int numProcess = rs.getInt("NUMPROCESSO");
            int episode = rs.getInt("EPISODIO");
            String patientName = rs.getString("NOMEDOENTE");
            Date dateOfBirth = rs.getDate("DATANASCIMENTO");
            Date examDate = rs.getDate("DATAMARCACAO");
            String observations = rs.getString("OBSERVACOES");
            if (numExam == null)
                numExam = "";
            if (patientName == null)
                patientName = "";
            if (observations == null)
                observations = "";
            Exam exam = new Exam(numExam, numProcess, episode, patientName, dateOfBirth, examDate, observations);
            this.dayExams.put(numExam, exam);
        }
        rs.close();
        stm.close();
    }

    private void sortDayExams() {
        this.dayExams = this.dayExams.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
                        (oldValue, newValue) -> oldValue, LinkedHashMap::new));
    }

    private void choosePatient(JTable target) {
        int row = target.getSelectedRow();
        this.frame.setVisible(false);
        new PatientWindow(this.connection, this.dayExams, this.table.getValueAt(row,0).toString(), this.user);
    }

    public void mouseClicked(MouseEvent e) {
        if (e.getSource() == this.table) {
            JTable target = (JTable) e.getSource();
            choosePatient(target);
        }
        else if (e.getSource() == this.mainMenu) {
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
        if (e.getSource() == this.historyMenu) {
            this.historyMenu.setOpaque(true);
            this.historyMenu.setBackground(Color.white);
        }
    }

    @Override
    public void mouseExited(MouseEvent e) {
        if (e.getSource() == this.historyMenu) {
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
            JTable target = (JTable) e.getSource();
            choosePatient(target);
        }
    }
}