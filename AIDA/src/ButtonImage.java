import javax.swing.*;
import java.awt.image.BufferedImage;


public class ButtonImage {
    private BufferedImage image;
    private JButton button;
    private int position;


    public ButtonImage(BufferedImage image, JButton button, int position) {
        this.image = image;
        this.button = button;
        this.position = position;
    }

    public BufferedImage getImage() {
        return image;
    }

    public void setImage(BufferedImage image) {
        this.image = image;
    }

    public JButton getButton() {
        return button;
    }

    public void setButton(JButton button) {
        this.button = button;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }
}