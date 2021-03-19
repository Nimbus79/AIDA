import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Map;
import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamResolution;
import com.xuggle.mediatool.IMediaWriter;
import com.xuggle.mediatool.ToolFactory;
import com.xuggle.xuggler.ICodec;
import com.xuggle.xuggler.IPixelFormat;
import com.xuggle.xuggler.IVideoPicture;
import com.xuggle.xuggler.video.ConverterFactory;
import com.xuggle.xuggler.video.IConverter;
import javax.swing.*;


public class VideoRecorder implements Runnable, ActionListener, KeyListener {
    private Webcam webcam;
    private String filename;
    private JFrame frame;
    private JButton stopRecording;
    private JButton recordVideo;
    private JButton stopExam;
    private Map<Integer, File> videos;
    private boolean stop;


    public VideoRecorder(Webcam webcam, String filename, JFrame frame, JButton stopRecording, JButton recordVideo, JButton stopExam, Map<Integer, File> videos) {
        this.webcam = webcam;
        this.filename = filename;
        this.frame = frame;
        this.frame.addKeyListener(this);
        this.stopRecording = stopRecording;
        this.stopRecording.addActionListener(this);
        this.recordVideo = recordVideo;
        this.recordVideo.addActionListener(this);
        this.stopExam = stopExam;
        this.stopExam.addActionListener(this);
        this.videos = videos;
        this.stop = false;
    }

    @Override
    public void run() {
        IMediaWriter writer = ToolFactory.makeWriter(this.filename);
        writer.addVideoStream(0, 0, ICodec.ID.CODEC_ID_H264, 720, 576);

        long start = System.currentTimeMillis();
        int i = 0;
        while(!this.stop) {
            BufferedImage image = ConverterFactory.convertToType(this.webcam.getImage(), BufferedImage.TYPE_3BYTE_BGR);
            IConverter converter = ConverterFactory.createConverter(image, IPixelFormat.Type.YUV420P);
            IVideoPicture frame = converter.toPicture(image, (System.currentTimeMillis() - start) * 1000);
            frame.setKeyFrame(i == 0);
            frame.setQuality(0);
            writer.encodeVideo(0, frame);
            // 10 FPS
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            i++;
        }
        writer.close();
        File file = new File(this.filename);
        this.videos.put(this.videos.size(), file);
        System.out.println("VÍDEO GUARDADO!");
    }

    public void stopRecording() {
        this.stopRecording.setVisible(false);
        this.recordVideo.setVisible(true);
        this.stop = true;
        this.frame.removeKeyListener(this);
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == this.stopRecording) {
            stopRecording();
        }
        else if (e.getSource() == this.stopExam) {
            this.stop = true;
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
        if (e.getKeyCode() == KeyEvent.VK_F7) {
            this.stopRecording.doClick();
        }
        else if (e.getKeyCode() == KeyEvent.VK_ENTER) {
            this.stop = true;
            this.frame.removeKeyListener(this);
        }
    }
}