import javafx.embed.swing.JFXPanel;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import javax.swing.JPanel;


public class ChoiceWindow implements MouseListener, ActionListener, KeyListener {
    private java.sql.Connection connection;
    private Map<String, Exam> dayExams;
    private String numExam;
    private String user;
    private Map<Integer, BufferedImage> images;
    private Map<Integer, File> videos;
    private Map<JButton, ButtonImage> buttonsImages;
    private Map<Integer, BufferedImage> selectedImages;
    private int videoIndex;
    private boolean checkShowImages;
    private boolean checkShowVideos;
    private Dimension maxDimension;
    private App app;
    private JFrame frame;
    private JMenu mainMenu;
    private JMenu historyMenu;
    private JPanel screenPanel;
    private JButton showVideos;
    private JButton showImages;
    private JButton saveToHistory;
    private JButton saveToDB;
    private JButton previous;
    private JButton next;
    private JPanel videosPanel;
    private JFXPanel VFXPanel;
    private JScrollPane imagesScrollPane;
    private JLabel errorMsg;


    public ChoiceWindow(java.sql.Connection connection, Map<String, Exam> dayExams, String numExam, String user, Map<Integer, BufferedImage> images, Map<Integer, File> videos) {
        this.connection = connection;
        this.dayExams = dayExams;
        this.numExam = numExam;
        this.user = user;
        this.checkShowImages = false;
        this.checkShowVideos = false;
        this.images = images;
        this.videos = videos;
        this.buttonsImages = new HashMap<>();
        this.selectedImages = new HashMap<>();
        this.videoIndex = 0;
        this.app = new App();
        createButtonsImages();
        initComponents();
    }

    private void createButtonsImages() {
        for (Map.Entry<Integer, BufferedImage> entry : this.images.entrySet()) {
            BufferedImage image = entry.getValue();
            Image resized = image.getScaledInstance(530, 386, Image.SCALE_DEFAULT);
            BufferedImage newImage = new BufferedImage(530, 386, Image.SCALE_REPLICATE);
            newImage.getGraphics().drawImage(resized, 0, 0 , null);
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
        this.frame.add(this.app.createTitlePanel(this.dayExams.get(this.numExam).getPatientName()));
        createErrorMsgLabel();
        createShowVideosButton();
        createShowImagesButton();
        createSaveToHistoryButton();
        createSaveToDBButton();
        createPreviousButton();
        createNextButton();
        createScreen();
        createVideosPanel();
        this.frame.requestFocus();
        this.frame.addKeyListener(this);
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

    private void createErrorMsgLabel() {
        this.errorMsg = this.app.createLabel("Não existem imagens para apresentar.", maxDimension.width/2-125, 140, 250, 50, 14, true);
        this.errorMsg.setVisible(false);
        this.frame.add(this.errorMsg);
    }

    private void createShowVideosButton() {
        this.showVideos = this.app.createButton("Ver Vídeos", this.maxDimension.width/2-150, 60,140,30, 15);
        this.showVideos.addActionListener(this);
        this.showVideos.addKeyListener(this);
        this.frame.add(this.showVideos);
    }

    private void createShowImagesButton() {
        this.showImages = this.app.createButton("Ver Imagens", this.maxDimension.width/2+10, 60,140,30, 15);
        this.showImages.addActionListener(this);
        this.frame.add(this.showImages);
    }

    private void createSaveToHistoryButton() {
        this.saveToHistory = this.app.createButton("Guardar no Histórico", this.maxDimension.width/2-176, this.maxDimension.height-140,166,30, 15);
        this.saveToHistory.addActionListener(this);
        this.saveToHistory.setVisible(false);
        this.frame.add(this.saveToHistory);
    }

    private void createSaveToDBButton() {
        this.saveToDB = this.app.createButton("Exportar para a BD", this.maxDimension.width/2+10, this.maxDimension.height-140,166,30, 15);
        this.saveToDB.addActionListener(this);
        this.saveToDB.setVisible(false);
        this.frame.add(this.saveToDB);
    }

    private void createPreviousButton() {
        this.previous = this.app.createButton("Anterior", this.maxDimension.width/2-170, this.maxDimension.height-140,160,30, 15);
        this.previous.addActionListener(this);
        this.previous.setVisible(false);
        this.frame.add(this.previous);
    }

    private void createNextButton() {
        this.next = this.app.createButton("Seguinte", this.maxDimension.width/2+10, this.maxDimension.height-140,160,30, 15);
        this.next.addActionListener(this);
        this.next.setVisible(false);
        this.frame.add(this.next);
    }

    private void createScreen() {
        this.screenPanel = this.app.createScreen(100,100,this.maxDimension.width-200,this.maxDimension.height-250);
        this.frame.add(this.screenPanel);
    }

    private void createVideosPanel() {
        this.videosPanel = new JPanel();
        this.videosPanel.setBounds(360,100,this.maxDimension.width-720,this.maxDimension.height-250);
        this.VFXPanel = new JFXPanel();
        this.videosPanel.setVisible(false);
        this.videosPanel.setLayout(new BorderLayout());
        this.videosPanel.add(this.VFXPanel, BorderLayout.CENTER);
        this.frame.add(this.videosPanel);
    }

    private void showImages() {
        JPanel imagesPanel = this.app.createPanel(this.maxDimension.width-200,this.maxDimension.height-250);
        this.imagesScrollPane = this.app.createScrollPane(imagesPanel, this.maxDimension.width-200,this.maxDimension.height-250);
        SortedMap<Integer, JButton> orderedButtons = this.app.orderButtons(this.buttonsImages);
        for (JButton button : orderedButtons.values()) {
            imagesPanel.add(button);
        }
        this.screenPanel.add(this.imagesScrollPane);
    }

    private void showVideo() {
        Group root = new Group();
        Scene scene = new Scene(root);
        File video_source = this.videos.get(videoIndex);
        Media m = new Media(video_source.toURI().toString());
        MediaPlayer player = new MediaPlayer(m);
        MediaControl mediaControl = new MediaControl(player);
        scene.setRoot(mediaControl);
        this.VFXPanel.setScene(scene);
    }

    private void showPics() {
        if (!this.checkShowImages) {
            this.videosPanel.setVisible(false);
            this.checkShowImages = true;
            this.previous.setVisible(false);
            this.next.setVisible(false);
            this.errorMsg.setVisible(false);
            if (this.images.size() > 0) {
                this.screenPanel.setVisible(true);
                this.saveToHistory.setVisible(true);
                this.saveToDB.setVisible(true);
                showImages();
            }
            else {
                this.errorMsg.setText("Não existem imagens para apresentar.");
                this.errorMsg.setVisible(true);
            }
        }
        this.checkShowVideos = false;
    }

    private void showVideos() {
        if (!this.checkShowVideos) {
            this.screenPanel.setVisible(false);
            this.checkShowVideos = true;
            this.saveToHistory.setVisible(false);
            this.saveToDB.setVisible(false);
            if (this.imagesScrollPane != null)
                this.imagesScrollPane.setVisible(false);
            if (this.videos.size() > 0) {
                this.videosPanel.setVisible(true);
                this.previous.setVisible(true);
                this.next.setVisible(true);
                if (this.videoIndex == this.videos.size()-1)
                    this.next.setEnabled(false);
                if (this.videoIndex == 0)
                    this.previous.setEnabled(false);
                showVideo();
            }
            else {
                this.errorMsg.setText("Não existem vídeos para apresentar.");
                this.errorMsg.setVisible(true);
            }
        }
        this.checkShowImages = false;
    }

    private void previous() {
        this.videoIndex--;
        if (this.videoIndex == 0)
            this.previous.setEnabled(false);
        this.next.setEnabled(true);
        showVideo();
    }

    private void next() {
        this.videoIndex++;
        if (this.videoIndex == this.videos.size()-1)
            this.next.setEnabled(false);
        this.previous.setEnabled(true);
        showVideo();
    }

    private void saveToHistory() {
        this.frame.setVisible(false);
        new MainWindow(this.connection, this.dayExams, this.user);
    }

    private void saveToDB() {
        this.frame.setVisible(false);
        new ConfirmationWindow(this.connection, this.dayExams, this.numExam, this.user, this.images, this.videos, this.selectedImages, true);
    }

    private void selectImage(ButtonImage buttonImage) {
        JButton button = buttonImage.getButton();
        if (button.getBackground() == Color.darkGray) {
            button.setBackground(null);
            this.selectedImages.remove(buttonImage.getPosition());
        }
        else {
            button.setBackground(Color.darkGray);
            this.selectedImages.put(buttonImage.getPosition(), buttonImage.getImage());
        }
        buttonImage.setButton(button);
        this.buttonsImages.put(button, buttonImage);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == this.showImages) {
            showPics();
        }
        else if (e.getSource() == this.showVideos) {
            showVideos();
        }
        else if (e.getSource() == this.previous) {
            previous();
        }
        else if (e.getSource() == this.next) {
            next();
        }
        else if (e.getSource() == this.saveToHistory) {
            saveToHistory();
        }
        else if (e.getSource() == this.saveToDB) {
            saveToDB();
        }
        else {
            ButtonImage buttonImage = this.buttonsImages.get(e.getSource());
            selectImage(buttonImage);
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
        if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
            if (this.videosPanel.isVisible() && this.next.isEnabled()) {
                this.next.doClick();
            }
        }
        else if (e.getKeyCode() == KeyEvent.VK_LEFT) {
            if (this.videosPanel.isVisible() && this.previous.isEnabled()) {
                this.previous.doClick();
            }
        }
    }
}