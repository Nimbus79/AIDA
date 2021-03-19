import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamPanel;
import com.github.sarxos.webcam.WebcamResolution;
import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class ExamWindow implements MouseListener, ActionListener, KeyListener {
    private java.sql.Connection connection;
    private Map<String, Exam> dayExams;
    private String numExam;
    private String user;
    private Map<Integer, BufferedImage> images;
    private Map<Integer, File> videos;
    private Dimension maxDimension;
    private App app;
    private JFrame frame;
    private JMenu mainMenu;
    private JMenu historyMenu;
    private JPanel screenPanel;
    private JButton startExam;
    private JButton recordVideo;
    private JButton captureImage;
    private JButton proceed;
    private JButton stopExam;
    private JButton stopRecording;
    private WebcamPanel webcamPanel;
    private Webcam webcam;
    private int imageIndex;
    private int videoIndex;


    public ExamWindow(java.sql.Connection connection, Map<String, Exam> dayExams, String numExam, String user) {
        this.connection = connection;
        this.dayExams = dayExams;
        this.numExam = numExam;
        this.user = user;
        this.images = new HashMap<>();
        this.videos = new HashMap<>();
        this.app = new App();
        getIndexes();
        initComponents();
    }

    private void getIndexes() {
        File folder = new File("./files/" + numExam + "/");
        if (folder.exists()) {
            File[] files = folder.listFiles();
            for (int i = 0; i < files.length; i++) {
                String filename = files[i].getName();
                String[] parts = filename.split("_");
                if (parts[1].contains(".jpg")) {
                    this.imageIndex = Integer.parseInt(parts[parts.length - 1].split(".jpg")[0])+1;
                }
                else {
                    this.videoIndex = Integer.parseInt(parts[parts.length - 1].split(".mp4")[0])+1;
                }
            }
        }
        else {
            this.imageIndex = 0;
            this.videoIndex = 0;
        }
    }

    private void initComponents() {
        this.maxDimension = Toolkit.getDefaultToolkit().getScreenSize();
        this.frame = this.app.createFrame(0);
        createMenuBar();
        this.frame.add(this.app.createTitlePanel(this.dayExams.get(this.numExam).getPatientName()));
        createStartExamButton();
        createRecordVideoButton();
        createCaptureImageButton();
        createProceedButton();
        createStopExamButton();
        createStopRecordingButton();
        createScreen();
        createWebCam();
        this.frame.addKeyListener(this);
        this.frame.requestFocus();
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

    private void createStartExamButton() {
        this.startExam = this.app.createButton("Iniciar", this.maxDimension.width/2-239, 60,146,30, 15);
        this.startExam.addActionListener(this);
        this.startExam.addKeyListener(this);
        this.frame.add(this.startExam);
    }

    private void createRecordVideoButton() {
        this.recordVideo = this.app.createButton("Gravar Vídeo", this.maxDimension.width/2-73, 60,146,30, 15);
        this.recordVideo.addActionListener(this);
        this.recordVideo.setEnabled(false);
        this.frame.add(this.recordVideo);
    }

    private void createCaptureImageButton() {
        this.captureImage = this.app.createButton("Capturar Imagem", this.maxDimension.width/2+93, 60,146,30, 15);
        this.captureImage.addActionListener(this);
        this.captureImage.setEnabled(false);
        this.frame.add(this.captureImage);
    }

    private void createProceedButton() {
        this.proceed = this.app.createButton("Prosseguir", this.maxDimension.width/2-73, this.maxDimension.height-140,146,30, 15);
        this.proceed.addActionListener(this);
        this.proceed.setVisible(false);
        this.frame.add(this.proceed);
    }

    private void createStopExamButton() {
        this.stopExam = this.app.createButton("Parar", this.maxDimension.width/2-239, 60,146,30, 15);
        this.stopExam.addActionListener(this);
        this.stopExam.addKeyListener(this);
        this.stopExam.setVisible(false);
        this.frame.add(this.stopExam);
    }

    private void createStopRecordingButton() {
        this.stopRecording = this.app.createButton("Parar Gravação", this.maxDimension.width/2-73, 60,146,30, 15);
        this.stopRecording.addActionListener(this);
        this.stopRecording.setVisible(false);
        this.frame.add(this.stopRecording);
    }

    private void createScreen() {
        this.screenPanel = this.app.createScreen(222,100,this.maxDimension.width-444,this.maxDimension.height-250);
        this.frame.add(this.screenPanel);
    }

    private int getWebcamPosition(String webcam) {
        int result = 0;
        List<Webcam> webcams = Webcam.getWebcams();
        for (int i = 0; i < webcams.size(); i++) {
            if (webcams.get(i).toString().contains(webcam)) {
                result = i;
            }
        }
        return result;
    }

    private void createWebCam() {
        Dimension[] nonStandardResolutions = new Dimension[] {
                WebcamResolution.PAL.getSize(),
                WebcamResolution.HD.getSize(),
                new Dimension(2000, 1000),
                new Dimension(1000, 500),
        };
        int position = getWebcamPosition("Pinnacle");
        this.webcam = Webcam.getWebcams().get(position);
        this.webcam.setCustomViewSizes(nonStandardResolutions);
        this.webcam.setViewSize(WebcamResolution.HD.getSize());
    }

    private void saveImage (BufferedImage image) throws IOException {
        String directoryName = "./files/";
        File directory = new File(directoryName);
        if (!directory.exists()){
            directory.mkdir();
        }
        directoryName = "./files/" + this.numExam + "/";
        directory = new File(directoryName);
        if (!directory.exists()){
            directory.mkdir();
        }
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date date = new Date();
        String filename = dateFormat.format(date)+"_"+this.imageIndex+".jpg";
        File file = new File("./files/" + this.numExam + "/"+filename);
        ImageIO.write(image, "jpg", file);
        this.images.put(this.imageIndex, image);
        this.imageIndex++;
    }

    private void createWebCamPanel() {
        this.webcam.open(true);
        this.webcamPanel = new WebcamPanel(this.webcam);
        this.webcamPanel.setLayout(null);
        this.webcamPanel.setBounds(222,100,this.maxDimension.width-444,this.maxDimension.height-250);
        this.webcamPanel.setBorder(BorderFactory.createLineBorder(Color.black));
        this.webcamPanel.setMirrored(false);
        this.frame.add(this.webcamPanel);
    }

    private void startExam() {
        this.startExam.setVisible(false);
        this.stopExam.setVisible(true);
        this.recordVideo.setEnabled(true);
        this.captureImage.setEnabled(true);
        this.screenPanel.setVisible(false);
        createWebCamPanel();
    }

    private void stopExam() {
        this.proceed.setVisible(true);
        this.stopExam.setEnabled(false);
        this.recordVideo.setEnabled(false);
        this.captureImage.setEnabled(false);
        this.stopRecording.setEnabled(false);
        this.webcam.close();
        this.webcamPanel.setVisible(false);
        this.screenPanel.setBackground(null);
        this.screenPanel.setVisible(true);
    }

    private void captureImage() {
        BufferedImage image = this.webcam.getImage();
        try {
            saveImage(image);
        } catch (IOException e1) {
            e1.printStackTrace();
        }
    }

    private void recordVideo() {
        String directoryName = "./files/";
        File directory = new File(directoryName);
        if (!directory.exists()){
            directory.mkdir();
        }
        directoryName = "./files/" + this.numExam + "/";
        directory = new File(directoryName);
        if (!directory.exists()){
            directory.mkdir();
        }
        this.recordVideo.setVisible(false);
        this.stopRecording.setVisible(true);
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date date = new Date();
        String filename = "./files/"+this.numExam+"/"+dateFormat.format(date)+"_"+this.videoIndex+".mp4";
        this.videoIndex++;
        VideoRecorder videoRecorder = new VideoRecorder(this.webcam, filename, this.frame, this.stopRecording, this.recordVideo, this.stopExam, this.videos);
        Thread record = new Thread(videoRecorder);
        record.start();
    }

    private void proceed() {
        this.frame.setVisible(false);
        new ChoiceWindow(this.connection, this.dayExams, this.numExam, this.user, this.images, this.videos);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == this.startExam) {
            startExam();
        }
        else if (e.getSource() == this.stopExam) {
            stopExam();
        }
        else if (e.getSource() == this.captureImage) {
            captureImage();
        }
        else if (e.getSource() == this.recordVideo) {
            recordVideo();
        }
        else if (e.getSource() == this.proceed) {
            proceed();
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
            if (this.startExam.isVisible()) {
                this.startExam.doClick();
            }
            else if (this.stopExam.isVisible() && this.stopExam.isEnabled()){
                this.stopExam.doClick();
            }
            else if (this.proceed.isVisible()) {
                this.proceed.doClick();
            }
        }
        else if (e.getKeyCode() == KeyEvent.VK_F8) {
            if (this.stopExam.isEnabled() && this.stopExam.isVisible()) {
                this.captureImage.doClick();
            }
        }
        else if (e.getKeyCode() == KeyEvent.VK_F7) {
            if (this.stopExam.isEnabled() && this.stopExam.isVisible() && this.recordVideo.isVisible()) {
                this.recordVideo.doClick();
            }
        }
    }
}