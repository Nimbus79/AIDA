import javafx.embed.swing.JFXPanel;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Blob;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import org.apache.commons.io.FileUtils;


public class SelectionWindow extends Component implements MouseListener, ActionListener, KeyListener {
    private java.sql.Connection connection;
    private Map<String, Exam> dayExams;
    private Map<String, Exam> doneExams;
    private String numExam;
    private String user;
    private Map<Integer, BufferedImage> savedImages;
    private Map<JButton, ButtonImage> buttonsSavedImages;
    private Map<JButton, ButtonImage> buttonsUnsavedImages;
    private int fileIndex;
    private Map<Integer, BufferedImage> selectedImages;
    private Map<Integer, File> selectedFiles;
    private SortedMap<Integer, File> allFiles;
    private boolean checkShowSavedImages;
    private boolean checkShowUnsavedImages;
    private boolean checkShowAllFiles;
    private Dimension maxDimension;
    private App app;
    private JFrame frame;
    private JMenu mainMenu;
    private JMenu historyMenu;
    private JTable table;
    private JButton showSavedImages;
    private JButton showUnsavedImages;
    private JButton showAllFiles;
    private JPanel screenPanel;
    private JPanel filesPanel;
    private SortedMap<Integer, JPanel> filePanel;
    private JButton saveToDB;
    private JButton select;
    private JButton next;
    private JButton previous;
    private JButton saveToFolder;
    private JScrollPane tableScrollPane;
    private JScrollPane savedImagesScrollPane;
    private JScrollPane unsavedImagesScrollPane;
    private JLabel errorMsg;


    public SelectionWindow(java.sql.Connection connection, Map<String, Exam> dayExams, Map<String, Exam> doneExams, String numExam, String user) throws IOException, SQLException {
        this.connection = connection;
        this.dayExams = dayExams;
        this.doneExams = doneExams;
        this.numExam = numExam;
        this.user = user;
        this.savedImages = new HashMap<>();
        this.buttonsSavedImages = new HashMap<>();
        this.buttonsUnsavedImages = new HashMap<>();
        this.selectedImages = new HashMap<>();
        this.selectedFiles = new HashMap<>();
        this.checkShowSavedImages = false;
        this.checkShowUnsavedImages = false;
        this.checkShowAllFiles = false;
        this.allFiles = new TreeMap<>();
        this.filePanel = new TreeMap<>();
        this.app = new App();
        this.fileIndex = 0;
        getSavedImages();
        createButtonsUnsavedImages();
        getAllFiles();
        initComponents();
    }

    private void getSavedImages() throws SQLException, IOException {
        PreparedStatement stm = this.connection.prepareStatement("SELECT ID_IMAG, BINARIO\n" +
                "FROM SIL.AIDA_IMAGENS_REL\n" +
                "WHERE NUM_PEDIDO = ?");
        stm.setString(1, this.numExam);
        ResultSet rs = stm.executeQuery();
        while (rs.next()) {
            int id_imag = rs.getInt("ID_IMAG");
            Blob binario = rs.getBlob("BINARIO");
            InputStream in = binario.getBinaryStream();
            BufferedImage image = ImageIO.read(in);
            this.savedImages.put(id_imag, image);
            createButtonSavedImage(image, id_imag);
        }
    }

    private void createButtonSavedImage(BufferedImage image, int id_imag) {
        Image resized = image.getScaledInstance(530, 386, Image.SCALE_DEFAULT);
        BufferedImage newImage = new BufferedImage(530, 386, Image.SCALE_REPLICATE);
        newImage.getGraphics().drawImage(resized, 0, 0 , null);
        JButton button = new JButton();
        button.setIcon(new ImageIcon(newImage));
        button.addActionListener(this);
        ButtonImage buttonImage = new ButtonImage(image, button, id_imag);
        this.buttonsSavedImages.put(button, buttonImage);
    }

    private void createButtonsUnsavedImages() {
        for (Map.Entry<Integer, BufferedImage> entryAll : this.doneExams.get(this.numExam).getImages().entrySet()) {
            boolean equal = false;
            for (Map.Entry<Integer, BufferedImage> entrySaved : this.savedImages.entrySet()) {
                if (compareImages(entryAll.getValue(), entrySaved.getValue())) {
                    equal = true;
                }
            }
            if (!equal) {
                createButtonUnsavedImage(entryAll.getValue(), entryAll.getKey());
            }
        }
    }

    private void createButtonUnsavedImage(BufferedImage image, int position) {
        Image resized = image.getScaledInstance(530, 386, Image.SCALE_DEFAULT);
        BufferedImage newImage = new BufferedImage(530, 386, Image.SCALE_REPLICATE);
        newImage.getGraphics().drawImage(resized, 0, 0 , null);
        JButton button = new JButton();
        button.setIcon(new ImageIcon(newImage));
        button.addActionListener(this);
        ButtonImage buttonImage = new ButtonImage(image, button, position);
        this.buttonsUnsavedImages.put(button, buttonImage);
    }

    private boolean compareImages(BufferedImage img1, BufferedImage img2) {
        if (img1.getWidth() == img2.getWidth() && img1.getHeight() == img2.getHeight()) {
            for (int x = 0; x < img1.getWidth(); x++) {
                for (int y = 0; y < img1.getHeight(); y++) {
                    if (img1.getRGB(x, y) != img2.getRGB(x, y)) {
                        return false;
                    }
                }
            }
        }
        else {
            return false;
        }
        return true;
    }

    private void getAllFiles() {
        Map<Integer, File> videos = new HashMap<>();
        File folder = new File("./files/" + numExam + "/");
        if (folder.exists()) {
            File[] files = folder.listFiles();
            for (int i = 0; i < files.length; i++) {
                String filename = files[i].getName();
                String[] parts = filename.split("_");
                if (parts[1].contains(".jpg")) {
                    File file = new File("./files/" + numExam + "/" + filename);
                    int position = Integer.parseInt(parts[parts.length - 1].split(".jpg")[0]);
                    this.allFiles.put(position, file);
                }
                else {
                    File file = new File("./files/" + numExam + "/" + filename);
                    int position = Integer.parseInt(parts[parts.length - 1].split(".mp4")[0]);
                    videos.put(position, file);
                }
            }
            int size = this.allFiles.size();
            for (Map.Entry<Integer, File> video : videos.entrySet()) {
                this.allFiles.put(video.getKey()+size, video.getValue());
            }
        }
    }

    private void initComponents() throws IOException {
        this.maxDimension = Toolkit.getDefaultToolkit().getScreenSize();
        this.frame = this.app.createFrame(0);
        createMenuBar();
        createTable();
        createErrorMsgLabel();
        Exam exam = this.doneExams.get(this.numExam);
        createSaveToDBButton();
        createScreen(220, 365);
        createPreviousButton();
        createNextButton();
        createSelectButton();
        createFilesPanel();
        createFilePanel();
        createSaveToFolderButton();
        if (exam.getStatus().equals("Enviado")) {
            createShowSavedImagesButton(this.maxDimension.width/2-265);
            createShowUnsavedImagesButton(this.maxDimension.width/2-85);
            createShowAllFilesButton(this.maxDimension.width/2+95);
        }
        else {
            createShowUnsavedImagesButton(this.maxDimension.width/2-180);
            createShowAllFilesButton(this.maxDimension.width/2+10);
        }
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
        String[][] exam = this.app.getExam(this.doneExams.get(this.numExam));
        String[] columnNames = {" ", " "};
        this.table = this.app.createTable(exam, columnNames);
        this.tableScrollPane = new JScrollPane(this.table);
        this.tableScrollPane.setBounds(0, 0, this.maxDimension.width, 180);
        this.frame.add(this.tableScrollPane);
    }

    private void createErrorMsgLabel() {
        this.errorMsg = this.app.createLabel("Não existem imagens para apresentar.", maxDimension.width/2-125, 240, 250, 50, 14, true);
        this.errorMsg.setVisible(false);
        this.frame.add(this.errorMsg);
    }

    private void createShowSavedImagesButton(int x) {
        this.showSavedImages = this.app.createButton("Imagens Guardadas", x, 185,170,30, 15);
        this.showSavedImages.addActionListener(this);
        this.frame.add(this.showSavedImages);
    }

    private void createShowUnsavedImagesButton(int x) {
        this.showUnsavedImages = this.app.createButton("Imagens Por Guardar", x, 185,170,30, 15);
        this.showUnsavedImages.addActionListener(this);
        this.frame.add(this.showUnsavedImages);
    }

    private void createShowAllFilesButton(int x) {
        this.showAllFiles = this.app.createButton("Todos os Ficheiros", x, 185,170,30, 15);
        this.showAllFiles.addActionListener(this);
        this.showAllFiles.addKeyListener(this);
        this.frame.add(this.showAllFiles);
    }

    private void createSaveToDBButton() {
        this.saveToDB = this.app.createButton("Exportar para a BD", this.maxDimension.width/2-80, this.maxDimension.height-140,160,30, 15);
        this.saveToDB.addActionListener(this);
        this.saveToDB.setVisible(false);
        this.frame.add(this.saveToDB);
    }

    private void createScreen(int y, int height) {
        this.screenPanel = this.app.createScreen(100, y,this.maxDimension.width-200,this.maxDimension.height-height);
        this.frame.add(this.screenPanel);
    }

    private void createPreviousButton() {
        this.previous = this.app.createButton("Anterior", 130, 365,160,30, 15);
        this.previous.addActionListener(this);
        this.previous.setVisible(false);
        this.frame.add(this.previous);
    }

    private void createNextButton() {
        this.next = this.app.createButton("Seguinte",470, 365,160,30, 15);
        this.next.addActionListener(this);
        this.next.setVisible(false);
        this.frame.add(this.next);
    }

    private void createSelectButton() {
        this.select = this.app.createButton("Selecionar", 300, 365,160,30, 15);
        this.select.addActionListener(this);
        this.select.addKeyListener(this);
        this.select.setVisible(false);
        this.frame.add(this.select);
    }

    private void createSaveToFolderButton() {
        this.saveToFolder = this.app.createButton("Guardar", this.maxDimension.width/2-80, this.maxDimension.height-140,160,30, 15);
        this.saveToFolder.addActionListener(this);
        this.saveToFolder.addKeyListener(this);
        this.saveToFolder.setVisible(false);
        this.frame.add(this.saveToFolder);
    }

    private void createFilesPanel() {
        this.filesPanel = this.app.createScreen(302, 220, this.maxDimension.width-604,this.maxDimension.height-370);
        this.filesPanel.setBackground(null);
        this.filesPanel.add(this.next);
        this.filesPanel.add(this.previous);
        this.filesPanel.add(this.select);
        this.filesPanel.setVisible(false);
        this.frame.add(this.filesPanel);
    }

    private void createFilePanel() throws IOException {
        for (Map.Entry<Integer, File> file : this.allFiles.entrySet()) {
            JPanel filePanel = new JPanel();
            filePanel.setVisible(false);
            if (file.getValue().getName().split("_")[1].contains("jpg")) {
                filePanel.setBounds(157, 1, 444, 360);
                showImage(filePanel, file.getKey());
            }
            else {
                filePanel.setBounds(162, 12, 437, 350);
                JFXPanel VFXPanel = new JFXPanel();
                filePanel.setLayout(new BorderLayout());
                filePanel.add(VFXPanel, BorderLayout.CENTER);
                showVideo(VFXPanel, file.getKey());
            }
            this.filesPanel.add(filePanel);
            this.filePanel.put(file.getKey(), filePanel);
        }
    }

    private void showImage(JPanel filePanel, int key) throws IOException {
        BufferedImage image = ImageIO.read(this.allFiles.get(key));
        Image resized = image.getScaledInstance(435, 349, Image.SCALE_DEFAULT);
        BufferedImage newImage = new BufferedImage(435, 349, Image.SCALE_REPLICATE);
        newImage.getGraphics().drawImage(resized, 0, 0 , null);
        JButton button = new JButton();
        button.setIcon(new ImageIcon(newImage));
        button.setOpaque(false);
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        filePanel.add(button);
    }

    private void showVideo(JFXPanel VFXPanel, int key) {
        Group root = new Group();
        Scene scene = new Scene(root);
        File video_source = this.allFiles.get(key);
        Media m = new Media(video_source.toURI().toString());
        MediaPlayer player = new MediaPlayer(m);
        MediaControl mediaControl = new MediaControl(player);
        scene.setRoot(mediaControl);
        VFXPanel.setScene(scene);
    }

    private void showFile(String action) {
        if (action.equals("previous")) {
            JPanel panel = this.filePanel.get(this.fileIndex+1);
            panel.setVisible(false);
            this.filePanel.put(this.fileIndex+1, panel);
        }
        else if (action.equals("next")) {
            JPanel panel = this.filePanel.get(this.fileIndex-1);
            panel.setVisible(false);
            this.filePanel.put(this.fileIndex-1, panel);
        }
        JPanel panel = this.filePanel.get(this.fileIndex);
        panel.setVisible(true);
        this.filePanel.put(this.fileIndex, panel);
    }

    private void showSavedImages() {
        JPanel savedImagesPanel = this.app.createPanel(this.maxDimension.width-200,this.maxDimension.height-365);
        this.savedImagesScrollPane = this.app.createScrollPane(savedImagesPanel, this.maxDimension.width-200,this.maxDimension.height-365);
        SortedMap<Integer, JButton> orderedButtons = this.app.orderButtons(this.buttonsSavedImages);
        for (JButton button : orderedButtons.values()) {
            button.setOpaque(false);
            button.setContentAreaFilled(false);
            button.setBorderPainted(false);
            savedImagesPanel.add(button);
        }
        this.screenPanel.add(this.savedImagesScrollPane);
    }

    private void showUnsavedImages(int height) {
        JPanel unsavedImagesPanel = this.app.createPanel(this.maxDimension.width-200,this.maxDimension.height-height);
        this.unsavedImagesScrollPane = this.app.createScrollPane(unsavedImagesPanel, this.maxDimension.width-200,this.maxDimension.height-height);
        SortedMap<Integer, JButton> orderedButtons = this.app.orderButtons(this.buttonsUnsavedImages);
        for (JButton button : orderedButtons.values()) {
            unsavedImagesPanel.add(button);
        }
        this.screenPanel.add(this.unsavedImagesScrollPane);
    }

    private void saveToDB() {
        this.frame.setVisible(false);
        new ConfirmationWindow(this.connection, this.dayExams, this.doneExams, this.numExam, this.user, this.doneExams.get(this.numExam).getImages(), this.selectedImages, false);
    }

    private void showSavedPics() {
        if (!this.checkShowSavedImages) {
            this.checkShowSavedImages = true;
            if (this.unsavedImagesScrollPane != null)
                this.unsavedImagesScrollPane.setVisible(false);
            this.saveToFolder.setVisible(false);
            this.errorMsg.setVisible(false);
            this.saveToDB.setVisible(false);
            this.next.setVisible(false);
            this.previous.setVisible(false);
            this.select.setVisible(false);
            this.filesPanel.setVisible(false);
            if (this.buttonsSavedImages.size() > 0) {
                this.screenPanel.setVisible(true);
                showSavedImages();
            }
            else {
                this.screenPanel.setVisible(false);
                this.errorMsg.setText("Não existem imagens para apresentar.");
                this.errorMsg.setVisible(true);
            }
        }
        this.checkShowUnsavedImages = false;
        this.checkShowAllFiles = false;
    }

    private void showUnsavedPics() {
        if (!this.checkShowUnsavedImages) {
            this.checkShowUnsavedImages = true;
            this.saveToFolder.setVisible(false);
            this.errorMsg.setVisible(false);
            this.next.setVisible(false);
            this.previous.setVisible(false);
            this.select.setVisible(false);
            this.filesPanel.setVisible(false);
            if (this.savedImagesScrollPane != null)
                this.savedImagesScrollPane.setVisible(false);
            if (this.buttonsUnsavedImages.size() > 0) {
                this.screenPanel.setVisible(true);
                this.saveToDB.setVisible(true);
                showUnsavedImages(365);
            }
            else {
                this.screenPanel.setVisible(false);
                this.errorMsg.setText("Não existem imagens para apresentar.");
                this.errorMsg.setVisible(true);
            }
        }
        this.checkShowSavedImages = false;
        this.checkShowAllFiles = false;
    }

    private void showAllFiles() {
        if (!this.checkShowAllFiles) {
            this.checkShowAllFiles = true;
            this.saveToDB.setVisible(false);
            this.errorMsg.setVisible(false);
            if (this.savedImagesScrollPane != null)
                this.savedImagesScrollPane.setVisible(false);
            if (this.unsavedImagesScrollPane != null)
                this.unsavedImagesScrollPane.setVisible(false);
            this.screenPanel.setVisible(false);
            if (this.allFiles.size() > 0) {
                this.saveToFolder.setVisible(true);
                this.filesPanel.setVisible(true);
                this.select.setVisible(true);
                this.next.setVisible(true);
                this.previous.setVisible(true);
                if (this.fileIndex == 0) {
                    this.previous.setEnabled(false);
                }
                else {
                    this.previous.setEnabled(true);
                }
                if (this.fileIndex == this.allFiles.size()-1) {
                    this.next.setEnabled(false);
                }
                else {
                    this.next.setEnabled(true);
                }
                showFile("file");
            }
            else {
                this.errorMsg.setText("Não existem ficheiros para apresentar.");
                this.errorMsg.setVisible(true);
            }
        }
        this.checkShowSavedImages = false;
        this.checkShowUnsavedImages = false;
    }

    private void previous() {
        this.fileIndex--;
        if (this.fileIndex == 0)
            this.previous.setEnabled(false);
        this.next.setEnabled(true);
        if (this.selectedFiles.containsKey(this.fileIndex)) {
            this.select.setBackground(Color.gray);
            this.select.setText("Selecionado");
        }
        else {
            this.select.setBackground(null);
            this.select.setText("Selecionar");
        }
        showFile("previous");
    }

    private void next() {
        this.fileIndex++;
        if (this.fileIndex == this.allFiles.size()-1)
            this.next.setEnabled(false);
        this.previous.setEnabled(true);
        if (this.selectedFiles.containsKey(this.fileIndex)) {
            this.select.setBackground(Color.gray);
            this.select.setText("Selecionado");
        }
        else {
            this.select.setBackground(null);
            this.select.setText("Selecionar");
        }
        showFile("next");
    }

    private void select() {
        if (this.select.getBackground().equals(Color.gray)) {
            this.select.setBackground(null);
            this.select.setText("Selecionar");
            this.selectedFiles.remove(this.fileIndex);
        }
        else {
            this.select.setBackground(Color.gray);
            this.select.setText("Selecionado");
            this.selectedFiles.put(this.fileIndex, this.allFiles.get(this.fileIndex));
        }
    }

    private void saveToFolder() {
        JFileChooser chooser = new JFileChooser();
        String userDir = System.getProperty("user.home");
        chooser.setCurrentDirectory(new java.io.File(userDir + "/Desktop"));
        chooser.setDialogTitle("Guardar ficheiros em...");
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setAcceptAllFileFilterUsed(false);
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            saveFiles(chooser.getSelectedFile().getPath());
        }
        else {
            System.out.println("No Selection ");
        }
    }

    private void saveFiles(String directory) {
        String directoryName = "";
        if (this.selectedFiles.size() > 0) {
            directoryName = directory + "/" + this.numExam + "/";
            File newDirectory = new File(directoryName);
            if (!newDirectory.exists()){
                newDirectory.mkdir();
            }
        }
        for (File file : this.selectedFiles.values()) {
            File dest = new File(directoryName + file.getName());
            try {
                FileUtils.copyFile(file, dest);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
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
        this.buttonsUnsavedImages.put(button, buttonImage);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == this.saveToDB) {
            saveToDB();
        }
        else if (e.getSource() == this.showSavedImages) {
            showSavedPics();
        }
        else if (e.getSource() == this.showUnsavedImages) {
            showUnsavedPics();
        }
        else if (e.getSource() == this.showAllFiles) {
            showAllFiles();
        }
        else if (e.getSource() == this.next) {
            next();
        }
        else if (e.getSource() == this.previous) {
            previous();
        }
        else if (e.getSource() == this.select) {
            select();
        }
        else if (e.getSource() == this.saveToFolder) {
            saveToFolder();
        }
        else {
            ButtonImage buttonImage = this.buttonsUnsavedImages.get(e.getSource());
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
            if (this.filesPanel.isVisible() && this.next.isEnabled()) {
                this.next.doClick();
            }
        }
        else if (e.getKeyCode() == KeyEvent.VK_LEFT) {
            if (this.filesPanel.isVisible() && this.previous.isEnabled()) {
                this.previous.doClick();
            }
        }
        else if (e.getKeyCode() == KeyEvent.VK_ENTER) {
            if (this.filesPanel.isVisible()) {
                this.select.doClick();
            }
        }
    }
}