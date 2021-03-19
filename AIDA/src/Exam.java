import java.awt.image.BufferedImage;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;


public class Exam {
    private String num;
    private int numProcess;
    private int episode;
    private String patientName;
    private Date dateOfBirth;
    private Date examDate;
    private String observations;
    private String status;
    private Map<Integer, BufferedImage> images;


    public Exam(String num, int numProcess, int episode, String patientName, Date dateOfBirth, Date examDate, String observations) {
        this.num = num;
        this.numProcess = numProcess;
        this.episode = episode;
        this.patientName = patientName;
        this.dateOfBirth = dateOfBirth;
        this.examDate = examDate;
        this.observations = observations;
        this.status = "Por enviar";
        this.images = new HashMap<>();
    }

    public Exam(String num, int numProcess, int episode, String patientName, Date dateOfBirth, Date examDate, String observations, String status, Map<Integer, BufferedImage> images) {
        this.num = num;
        this.numProcess = numProcess;
        this.episode = episode;
        this.patientName = patientName;
        this.dateOfBirth = dateOfBirth;
        this.examDate = examDate;
        this.observations = observations;
        this.status = status;
        this.images = images;
    }

    public String getNum() {
        return this.num;
    }

    public void setNum(String num) {
        this.num = num;
    }

    public String getPatientName() {
        return this.patientName;
    }

    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }

    public Date getDateOfBirth() {
        return this.dateOfBirth;
    }

    public void setDateOfBirth(Date dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getObservations() {
        return this.observations;
    }

    public void setObservations(String observations) {
        this.observations = observations;
    }

    public int getNumProcess() {
        return numProcess;
    }

    public void setNumProcess(int numProcess) {
        this.numProcess = numProcess;
    }

    public int getEpisode() {
        return episode;
    }

    public void setEpisode(int episode) {
        this.episode = episode;
    }

    public Date getExamDate() {
        return examDate;
    }

    public void setExamDate(Date examDate) {
        this.examDate = examDate;
    }

    public Map<Integer, BufferedImage> getImages() {
        return images;
    }

    public void setImages(Map<Integer, BufferedImage> images) {
        this.images = images;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}