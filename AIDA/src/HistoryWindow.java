import com.github.lgooddatepicker.components.DatePicker;
import com.github.lgooddatepicker.components.DatePickerSettings;
import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.DayOfWeek;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;


public class HistoryWindow implements ActionListener, MouseListener, KeyListener {
    private java.sql.Connection connection;
    private Map<String, Exam> dayExams;
    private Map<String, Exam> doneExams;
    private String user;
    private Dimension maxDimension;
    private App app;
    private JFrame frame;
    private JMenu mainMenu;
    private JMenu historyMenu;
    private DatePicker datePickerStart;
    private DatePicker datePickerEnd;
    private JButton search;
    private JLabel errorMsg;
    private JTable table;
    private JScrollPane scrollPane;


    public HistoryWindow(java.sql.Connection connection, Map<String, Exam> dayExams, String user) {
        this.connection = connection;
        this.dayExams = dayExams;
        this.doneExams = new HashMap<>();
        this.user = user;
        this.app = new App();
        initComponents();
    }

    private void initComponents() {
        this.maxDimension = Toolkit.getDefaultToolkit().getScreenSize();
        this.frame = this.app.createFrame(0);
        createMenuBar();
        createDatePickerStart();
        createDatePickerEnd();
        createSearchButton();
        createErrorMsgLabel();
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
        this.historyMenu.setOpaque(true);
        this.historyMenu.setBackground(Color.white);
    }

    private void createDatePickerStart() {
        this.frame.add(this.app.createLabel(" De:", maxDimension.width/2 - 250, 50, 40, 50, 18, false));
        URL dateImageURL = HistoryWindow.class.getResource("/images/datepickerbutton1.png");
        Image dateExampleImage = Toolkit.getDefaultToolkit().getImage(dateImageURL);
        ImageIcon dateExampleIcon = new ImageIcon(dateExampleImage);
        DatePickerSettings dateSettings = setDatePickerSettings();
        this.datePickerStart = new DatePicker(dateSettings);
        this.datePickerStart.setDateToToday();
        JButton datePickerButton = this.datePickerStart.getComponentToggleCalendarButton();
        datePickerButton.setText("");
        datePickerButton.setIcon(dateExampleIcon);
        this.datePickerStart.setBounds(maxDimension.width/2 - 200, 50, 180,50);
        this.datePickerStart.getComponentDateTextField().addKeyListener(this);
        this.frame.add(this.datePickerStart);
    }

    private void createDatePickerEnd() {
        this.frame.add(this.app.createLabel("Até:", maxDimension.width/2 + 20, 50, 40, 50, 18, false));
        URL dateImageURL = HistoryWindow.class.getResource("/images/datepickerbutton1.png");
        Image dateExampleImage = Toolkit.getDefaultToolkit().getImage(dateImageURL);
        ImageIcon dateExampleIcon = new ImageIcon(dateExampleImage);
        DatePickerSettings dateSettings = setDatePickerSettings();
        this.datePickerEnd = new DatePicker(dateSettings);
        this.datePickerEnd.setDateToToday();
        JButton datePickerButton = this.datePickerEnd.getComponentToggleCalendarButton();
        datePickerButton.setText("");
        datePickerButton.setIcon(dateExampleIcon);
        this.datePickerEnd.setBounds(maxDimension.width/2 + 70, 50, 180,50);
        this.datePickerEnd.getComponentDateTextField().addKeyListener(this);
        this.frame.add(this.datePickerEnd);
    }

    private DatePickerSettings setDatePickerSettings() {
        DatePickerSettings dateSettings = new DatePickerSettings();
        int newHeight = (int) (dateSettings.getSizeDatePanelMinimumHeight() * 1.6);
        int newWidth = (int) (dateSettings.getSizeDatePanelMinimumWidth() * 1.6);
        dateSettings.setSizeDatePanelMinimumHeight(newHeight);
        dateSettings.setSizeDatePanelMinimumWidth(newWidth);
        dateSettings.setFormatForDatesCommonEra("yyyy/MM/dd");
        dateSettings.setFormatForDatesBeforeCommonEra("uuuu/MM/dd");
        dateSettings.setFirstDayOfWeek(DayOfWeek.MONDAY);
        dateSettings.setFontValidDate(new Font("Sans-serif", Font.PLAIN, 17));
        dateSettings.setAllowEmptyDates(false);
        //dateSettings.setAllowKeyboardEditing(false);
        return dateSettings;
    }

    private void createErrorMsgLabel() {
        this.errorMsg = this.app.createLabel("Não foram encontrados resultados para as datas inseridas.", maxDimension.width/2-190, 180, 380, 50, 14, true);
        this.errorMsg.setVisible(false);
        this.frame.add(this.errorMsg);
    }

    private void createSearchButton() {
        this.search = this.app.createButton("Pesquisar", maxDimension.width/2 - 70,130,140,40, 17);
        this.search.addMouseListener(this);
        this.search.addActionListener(this);
        this.frame.add(this.search);
    }

    private void createTable() {
        String[][] doneExams = this.app.getExams(this.doneExams, 3);
        String[] columnNames = {"Nº PEDIDO", "UTENTE", "ESTADO"};
        this.table = this.app.createTable(doneExams, columnNames);
        this.table.addMouseListener(this);
        this.table.addKeyListener(this);
        this.table.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "none");
        this.scrollPane = new JScrollPane(this.table);
        this.scrollPane.setBounds(0, 200, maxDimension.width, maxDimension.height-290);
        this.frame.add(this.scrollPane);
    }

    private void getDoneExams(String startDate, String endDate) throws SQLException, IOException {
        this.doneExams = new HashMap<>();
        PreparedStatement stm = this.connection.prepareStatement("SELECT NUMPEDIDO, NUMPROCESSO, EPISODIO,\n" +
                "NOMEDOENTE, DATANASCIMENTO, DATAMARCACAO, OBSERVACOES\n" +
                "FROM SIL.DPEDIDOS\n" +
                "WHERE IDSER = 'BRO' AND POSTO = 'BR1' AND NUMEXAME IS NOT NULL AND MARCADO = 4 AND\n" +
                "(DATAMARCACAO BETWEEN to_date(?,'YYYY.MM.DD HH24:MI:SS') AND to_date(?,'YYYY.MM.DD HH24:MI:SS'))");
        stm.setString(1, startDate);
        stm.setString(2, endDate);
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
            String status = getExamStatus(numExam);
            Map<Integer, BufferedImage> examImages = getExamImages(numExam);
            Exam exam = new Exam(numExam, numProcess, episode, patientName, dateOfBirth, examDate, observations, status, examImages);
            this.doneExams.put(numExam, exam);
        }
        rs.close();
        stm.close();
    }

    private String getExamStatus(String numExam) throws SQLException {
        PreparedStatement stm = this.connection.prepareStatement("SELECT COUNT(ID_IMAG)\n"+
                        "FROM SIL.AIDA_IMAGENS_REL\n" +
                        "WHERE NUM_PEDIDO = ?");
        stm.setString(1, numExam);
        ResultSet rs = stm.executeQuery();
        rs.next();
        String status;
        if (rs.getInt("COUNT(ID_IMAG)") > 0) {
            status ="Enviado";
        }
        else {
            status = "Por enviar";
        }
        rs.close();
        stm.close();
        return status;
    }

    private Map<Integer, BufferedImage> getExamImages(String numExam) throws IOException {
        Map<Integer, BufferedImage> examImages = new HashMap<>();
        File folder = new File("./files/" + numExam + "/");
        if (folder.exists()) {
            File[] files = folder.listFiles();
            for (int i = 0; i < files.length; i++) {
                String filename = files[i].getName();
                String[] parts = filename.split("_");
                if (parts[1].contains(".jpg")) {
                    File file = new File("./files/" + numExam + "/" + filename);
                    BufferedImage image = ImageIO.read(file);
                    int position = Integer.parseInt(parts[parts.length - 1].split(".jpg")[0]);
                    examImages.put(position, image);
                }
            }
        }
        return examImages;
    }

    private void choosePatient(JTable target) {
        int row = target.getSelectedRow();
        this.frame.setVisible(false);
        try {
            new SelectionWindow(this.connection, this.dayExams, this.doneExams, this.table.getValueAt(row,0).toString(), this.user);
        } catch (IOException e1) {
            e1.printStackTrace();
        } catch (SQLException e1) {
            e1.printStackTrace();
        }
    }

    private void searchHistory() throws IOException, SQLException {
        String startDate = this.datePickerStart.getText().replaceAll("/",".") + " 00:00:00";
        String endDate = this.datePickerEnd.getText().replaceAll("/", ".") + " 23:59:59";
        getDoneExams(startDate, endDate);
        sortDoneExams();
        if (this.doneExams.size() > 0 && startDate != "" && endDate != "" && startDate.compareTo(endDate) < 0) {
            showDoneExams();
        }
        else {
            this.errorMsg.setVisible(true);
            if (this.scrollPane != null) {
                this.scrollPane.setVisible(false);
            }
        }
    }

    private void sortDoneExams() {
        this.doneExams = this.doneExams.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
                        (oldValue, newValue) -> oldValue, LinkedHashMap::new));
    }

    private void showDoneExams() {
        this.errorMsg.setVisible(false);
        if (this.scrollPane != null) {
            this.scrollPane.setVisible(false);
        }
        this.table = null;
        this.scrollPane = null;
        createTable();
        this.scrollPane.setVisible(true);
        this.table.requestFocus();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == this.search) {
            try {
                searchHistory();
            } catch (IOException e1) {
                e1.printStackTrace();
            } catch (SQLException e1) {
                e1.printStackTrace();
            }
        }
    }

    public void mouseClicked(MouseEvent e) {
        if (e.getSource() == this.table) {
            JTable target = (JTable) e.getSource();
            choosePatient(target);
        }
        else if (e.getSource() == this.search) {
            try {
                searchHistory();
            } catch (IOException e1) {
                e1.printStackTrace();
            } catch (SQLException e1) {
                e1.printStackTrace();
            }
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
        if (e.getSource() == this.mainMenu) {
            this.mainMenu.setOpaque(true);
            this.mainMenu.setBackground(Color.white);
        }
    }

    @Override
    public void mouseExited(MouseEvent e) {
        if (e.getSource() == this.mainMenu) {
            this.mainMenu.setOpaque(false);
            Color color = UIManager.getColor ( "Menu.background" );
            this.mainMenu.setBackground(color);
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
            if (this.datePickerStart.getComponentDateTextField().hasFocus() || this.datePickerEnd.getComponentDateTextField().hasFocus()) {
                this.search.doClick();
            }
            else {
                JTable target = (JTable) e.getSource();
                choosePatient(target);
            }
        }
    }
}