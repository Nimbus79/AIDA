import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;


public class ConfirmationWindow implements MouseListener, ActionListener {
    private java.sql.Connection connection;
    private Map<String, Exam> dayExams;
    private Map<String, Exam> doneExams;
    private String numExam;
    private String user;
    private Map<Integer, BufferedImage> images;
    private Map<Integer, File> videos;
    private Map<JButton, ButtonImage> buttonsImages;
    private Map<Integer, BufferedImage> selectedImages;
    private boolean page; // true = ChoiceWindow; false = SelectionWindow
    private Dimension maxDimension;
    private App app;
    private JFrame frame;
    private JMenu mainMenu;
    private JMenu historyMenu;
    private JPanel screenPanel;
    private JButton back;
    private JButton confirm;
    private JScrollPane scrollPane;


    public ConfirmationWindow(java.sql.Connection connection, Map<String, Exam> dayExams, Map<String, Exam> doneExams, String numExam, String user, Map<Integer, BufferedImage> images, Map<Integer, BufferedImage> selectedImages, boolean page) {
        this.connection = connection;
        this.dayExams = dayExams;
        this.doneExams = doneExams;
        this.numExam = numExam;
        this.user = user;
        this.images = images;
        this.buttonsImages = new HashMap<>();
        this.selectedImages = selectedImages;
        this.page = page;
        this.app = new App();
        createButtonsImages();
        initComponents();
    }

    public ConfirmationWindow(java.sql.Connection connection, Map<String, Exam> dayExams, String numExam, String user, Map<Integer, BufferedImage> images, Map<Integer, File> videos, Map<Integer, BufferedImage> selectedImages, boolean page) {
        this.connection = connection;
        this.dayExams = dayExams;
        this.doneExams = new HashMap<>();
        this.numExam = numExam;
        this.user = user;
        this.images = images;
        this.videos = videos;
        this.buttonsImages = new HashMap<>();
        this.selectedImages = selectedImages;
        this.page = page;
        this.app = new App();
        createButtonsImages();
        initComponents();
    }

    private void createButtonsImages() {
        if (this.selectedImages.size() == 0) {
            this.selectedImages = this.images;
        }
        for (Map.Entry<Integer, BufferedImage> entry : this.selectedImages.entrySet()) {
            BufferedImage image = entry.getValue();
            Image resized = image.getScaledInstance(530, 386, Image.SCALE_DEFAULT);
            BufferedImage newImage = new BufferedImage(530, 386, Image.SCALE_REPLICATE);
            newImage.getGraphics().drawImage(resized, 0, 0, null);
            JButton button = new JButton();
            button.setIcon(new ImageIcon(newImage));
            button.addActionListener(this);
            ButtonImage buttonImage = new ButtonImage(image, button, entry.getKey());
            this.buttonsImages.put(button, buttonImage);
        }
    }

    private void initComponents() {
        this.maxDimension = Toolkit.getDefaultToolkit().getScreenSize();
        this.frame = this.app.createFrame(0);
        createMenuBar();
        if (this.page) {
            this.frame.add(this.app.createTitlePanel(this.dayExams.get(this.numExam).getPatientName()));
        }
        else {
            this.frame.add(this.app.createTitlePanel(this.doneExams.get(this.numExam).getPatientName()));
        }
        createBackButton();
        createConfirmButton();
        createScreen();
        showImages();
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

    private void createBackButton() {
        this.back = this.app.createButton("Voltar", this.maxDimension.width/2-170, this.maxDimension.height-160,160,30, 15);
        this.back.addActionListener(this);
        this.frame.add(this.back);
    }

    private void createConfirmButton() {
        this.confirm = this.app.createButton("Confirmar", this.maxDimension.width/2+10, this.maxDimension.height-160,160,30, 15);
        this.confirm.addActionListener(this);
        this.frame.add(this.confirm);
    }

    private void createScreen() {
        this.screenPanel = this.app.createScreen(100,70,this.maxDimension.width-200,this.maxDimension.height-250);
        this.frame.add(this.screenPanel);
    }

    private void showImages() {
        JPanel imagesPanel = this.app.createPanel(this.maxDimension.width-200,this.maxDimension.height-250);
        this.scrollPane = this.app.createScrollPane(imagesPanel, this.maxDimension.width-200,this.maxDimension.height-250);
        Map<Integer, JButton> orderedButtons = this.app.orderButtons(this.buttonsImages);
        for (JButton button : orderedButtons.values()) {
            button.setOpaque(false);
            button.setContentAreaFilled(false);
            button.setBorderPainted(false);
            imagesPanel.add(button);
        }
        this.screenPanel.add(this.scrollPane);
    }

    private void saveImages() throws IOException, SQLException {
        for (BufferedImage image : this.selectedImages.values()) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(image, "jpg", baos);
            ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
            int id = getMaxID();
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date date = new Date();
            String data = dateFormat.format(date).replaceAll("-", ".");
            PreparedStatement stm = this.connection.prepareStatement("INSERT INTO SIL.AIDA_IMAGENS_REL\n" +
                    "VALUES(?,NULL,?,NULL,NULL,NULL,to_date(?,'YYYY.MM.DD HH24:MI:SS'),?,?,1)");
            stm.setInt(1, id);
            stm.setString(2, this.numExam);
            stm.setString(3, data);
            stm.setString(4, this.user);
            stm.setBlob(5, bais);
            stm.executeUpdate();
            stm.close();
        }
    }

    private int getMaxID() throws SQLException {
        PreparedStatement stm = this.connection.prepareStatement("SELECT MAX(ID_IMAG) FROM SIL.AIDA_IMAGENS_REL");
        ResultSet rs = stm.executeQuery();
        rs.next();
        int id = rs.getInt("MAX(ID_IMAG)");
        rs.close();
        stm.close();
        return id+1;
    }

    private void back() {
        this.frame.setVisible(false);
        if (this.page) {
            new ChoiceWindow(this.connection, this.dayExams, this.numExam, this.user, this.images, this.videos);
        }
        else {
            try {
                new SelectionWindow(this.connection, this.dayExams, this.doneExams, this.numExam, this.user);
            } catch (IOException e1) {
                e1.printStackTrace();
            } catch (SQLException e1) {
                e1.printStackTrace();
            }
        }
    }

    private void confirm() {
        try {
            saveImages();
            this.frame.setVisible(false);
            if (this.page) {
                new MainWindow(this.connection, this.dayExams, this.user);
            }
            else {
                Exam exam = this.doneExams.get(this.numExam);
                exam.setStatus("Enviado");
                new HistoryWindow(this.connection, this.dayExams, this.user);
            }
        } catch (IOException e1) {
            e1.printStackTrace();
        } catch (SQLException e1) {
            e1.printStackTrace();
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == this.back) {
            back();
        }
        else if (e.getSource() == this.confirm) {
            confirm();
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
}