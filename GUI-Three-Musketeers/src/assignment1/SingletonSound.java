package assignment1;

import java.io.File;
import java.io.IOException;
import javax.sound.sampled.*;

// sound effect source: http://freesoundeffect.net/sound/board-game-piece-2-sound-effect

public class SingletonSound {

    private static SingletonSound singleton = new SingletonSound(); // private to avoid more than one instance

    public SingletonSound() {
    }

    public static SingletonSound getInstance() {
        return singleton;
    }

    void playEffect(String effectPath) {
        File soundEffect = new File(effectPath);
        try {
            AudioInputStream input = AudioSystem.getAudioInputStream(soundEffect);
            Clip clip = AudioSystem.getClip();
            clip.open(input);
            clip.start();
        } catch (UnsupportedAudioFileException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (LineUnavailableException e) {
            e.printStackTrace();
        }
    }
    public void main() {
        SingletonSound sound = SingletonSound.getInstance();
        sound.playEffect("sounds/board-game-piece.wav");
    }
}
