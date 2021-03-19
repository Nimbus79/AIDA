import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.font.TextAttribute;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;


public class App {
    private Dimension maxDimension;


    public App() {
        this.maxDimension = Toolkit.getDefaultToolkit().getScreenSize();
    }

    public JFrame createFrame (int mode) {
        JFrame frame = new JFrame();
        frame.setTitle("AIDA");
        if (mode == 0) {
            frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
            frame.setSize(this.maxDimension.width, this.maxDimension.height-40);
            frame.setLayout(null);
        }
        else if (mode == 1) {
            frame.setSize(500, 336);
            frame.setLocationRelativeTo(null);
            frame.setResizable(false);
            frame.setLayout(null);
        }
        else {
            frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
            frame.setSize(this.maxDimension.width, this.maxDimension.height-40);
            frame.setLayout(new BorderLayout());
        }
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        return frame;
    }

    public JMenu createMenu (String label) {
        JMenu menu = new JMenu(label);
        menu.setFont(new Font("Sans-serif", Font.PLAIN, 15));
        return menu;
    }

    public JPanel createTitlePanel (String patientName) {
        JPanel titlePanel = new JPanel();
        titlePanel.setBounds(-1,0,this.maxDimension.width+2,30);
        titlePanel.setBackground(Color.white);
        titlePanel.setBorder(BorderFactory.createLineBorder(Color.black));
        JLabel titleLabel = new JLabel("EXAME DE "+patientName);
        titleLabel.setFont(new Font("Sans-serif", Font.PLAIN, 15));
        titleLabel.setBounds(this.maxDimension.width/2-40, 15, 80, 30);
        titleLabel.setBackground(Color.white);
        titlePanel.add(titleLabel);
        return titlePanel;
    }

    public JButton createButton (String label, int x, int y, int width, int height, int fontSize) {
        JButton button = new JButton(label);
        button.setFont(new Font("Sans-serif", Font.PLAIN, fontSize));
        button.setBounds(x, y, width, height);
        return button;
    }

    public JLabel createLabel (String text, int x, int y, int width, int height, int fontSize, boolean errorMsg) {
        JLabel label = new JLabel(text);
        if (errorMsg) {
            Map<TextAttribute, Integer> fontAttributes = new HashMap<TextAttribute, Integer>();
            fontAttributes.put(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_ON);
            label.setFont(new Font("Sans-serif",Font.PLAIN, fontSize).deriveFont(fontAttributes));
        }
        else {
            label.setFont(new Font("Sans-serif", Font.BOLD, fontSize));
        }
        label.setBounds(x, y, width, height);
        return  label;
    }

    public String[][] getExams (Map<String, Exam> mapExams, int columns) {
        String[][] exams = new String[mapExams.size()][columns];
        int i = 0;
        for (Exam exam : mapExams.values()) {
            exams[i][0] = exam.getNum();
            exams[i][1] = exam.getPatientName();
            if (columns == 3) {
                exams[i][2] = exam.getStatus();
            }
            i++;
        }
        return exams;
    }

    public String[][] getExam (Exam e) {
        String[][] exam = new String[6][2];
        exam[0][0] = "Nº PEDIDO"; exam[0][1] = e.getNum();
        exam[1][0] = "EPISÓDIO"; exam[1][1] = Integer.toString(e.getEpisode());
        exam[2][0] = "UTENTE"; exam[2][1] = e.getPatientName();
        exam[3][0] = "DATA DE NASCIMENTO";
        if (e.getDateOfBirth() == null)
            exam[3][1] = "";
        else
            exam[3][1] = e.getDateOfBirth().toString();
        exam[4][0] = "DATA DO EXAME";
        if (e.getExamDate() == null)
            exam[4][1] = "";
        else
            exam[4][1] = e.getExamDate().toString();
        exam[5][0] = "OBSERVAÇÕES"; exam[5][1] = e.getObservations();
        return exam;
    }

    public JTable createTable (String[][] exams, String[] columnNames) {
        JTable table = new JTable(exams, columnNames);
        DefaultTableModel tableModel = new DefaultTableModel(exams, columnNames) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table.setModel(tableModel);
        table.setRowHeight(25);
        if (columnNames[0] != " ") {
            table.getTableHeader().setFont(new Font("Sans-serif", Font.PLAIN, 15));
        }
        table.setFont(new Font("Sans-serif", Font.PLAIN, 15));
        return table;
    }

    public JPanel createScreen (int x, int y, int width, int height) {
        JPanel screen = new JPanel();
        screen.setLayout(null);
        screen.setBounds(x, y, width, height);
        screen.setBackground(Color.white);
        screen.setBorder(BorderFactory.createLineBorder(Color.black));
        return screen;
    }

    public SortedMap<Integer, JButton> orderButtons (Map<JButton, ButtonImage> buttons) {
        SortedMap<Integer, JButton> result = new TreeMap<>();
        for (ButtonImage buttonImage : buttons.values()) {
            result.put(buttonImage.getPosition(), buttonImage.getButton());
        }
        return result;
    }

    public JPanel createPanel (int width, int height) {
        JPanel panel = new JPanel();
        panel.setSize(width, height);
        panel.setLayout(new GridLayout(0,2,10,10));
        return panel;
    }

    public JScrollPane createScrollPane (JPanel panel, int width, int height) {
        JScrollPane scrollPane = new JScrollPane(panel);
        scrollPane.setSize(width, height);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        return scrollPane;
    }
}