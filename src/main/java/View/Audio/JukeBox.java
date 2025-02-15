package View.Audio;
import javax.sound.sampled.*;
import java.io.BufferedInputStream;
import java.io.InputStream;
import java.util.HashMap;

public class JukeBox {
    private static HashMap<String, Clip> clips;
    private static int gap;
    private static boolean mute = false;
    private static HashMap<String, FloatControl> Mixer;

    public static void init() {
        clips = new HashMap<String, Clip>();
        Mixer = new HashMap<String, FloatControl>();
        gap = 0;
    }

    public static void load(String s, String n) {
        if(clips.get(n) != null) return;
        Clip clip;
        try {
            InputStream audioSrc = JukeBox.class.getResourceAsStream(s);
            InputStream bufferedIn = new BufferedInputStream(audioSrc);
            AudioInputStream ais =
                    AudioSystem.getAudioInputStream(
                            bufferedIn
                    );
            AudioFormat baseFormat = ais.getFormat();
            AudioFormat decodeFormat = new AudioFormat(
                    AudioFormat.Encoding.PCM_SIGNED,
                    baseFormat.getSampleRate(),
                    16,
                    baseFormat.getChannels(),
                    baseFormat.getChannels() * 2,
                    baseFormat.getSampleRate(),
                    false
            );
            AudioInputStream dais = AudioSystem.getAudioInputStream(decodeFormat, ais);
            clip = AudioSystem.getClip();
            clip.open(dais);
            clips.put(n, clip);
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }

    public static void play(String s) {
        play(s, gap);
    }

    public static void play(String s, int i) {
        if(mute) return;
        Clip c = clips.get(s);
        if(c == null) return;
        if(c.isRunning()) c.stop();
        c.setFramePosition(i);
        while(!c.isRunning()) c.start();
    }

    public static void stop(String s) {
        if(clips.get(s) == null) return;
        if(clips.get(s).isRunning()) clips.get(s).stop();
    }


    public static void resume(String s) {
        if(mute) return;
        if(clips.get(s).isRunning()) return;
        clips.get(s).start();
    }

    public static boolean isPlaying(String s){
        if(clips.get(s).isRunning()) return true;
        else return false;
    }

    public static void loop(String s) {
        loop(s, gap, gap, clips.get(s).getFrameLength() - 1);
    }

    public static void loop(String s, int frame) {
        loop(s, frame, gap, clips.get(s).getFrameLength() - 1);
    }

    public static void loop(String s, int start, int end) {
        loop(s, gap, start, end);
    }

    public static void loop(String s, int frame, int start, int end) {
        stop(s);
        if(mute) return;
        clips.get(s).setLoopPoints(start, end);
        clips.get(s).setFramePosition(frame);
        clips.get(s).loop(Clip.LOOP_CONTINUOUSLY);
    }

    public static void setPosition(String s, int frame) {
        clips.get(s).setFramePosition(frame);
    }

    public static int getFrames(String s) { return clips.get(s).getFrameLength(); }
    public static int getPosition(String s) { return clips.get(s).getFramePosition(); }
    public static void setVolume(String s, float volume){
        if(clips.get(s) == null) return;
        float min, max;
        FloatControl volumeControl;
        if(volume < 0) volume = 0;
        if(volume > 1) volume = 1;
        if(Mixer.get(s) != null) {
            volumeControl = Mixer.get(s);
        }
        else {
            volumeControl = (FloatControl) clips.get(s)
                    .getControl(FloatControl.Type.MASTER_GAIN);
            Mixer.put(s, volumeControl);
        }
        min = volumeControl.getMinimum();
        max = volumeControl.getMaximum();
        volumeControl.setValue((max-min)*volume+min);
    }
    public static void close(String s) {
        stop(s);
        clips.get(s).close();
    }

}