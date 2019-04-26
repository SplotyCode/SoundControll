package team.gutterteam123.soundcontrol.sound;

import lombok.Getter;
import lombok.Setter;
import team.gutterteam123.soundcontrol.sound.device.VirtualInput;
import team.gutterteam123.soundcontrol.sound.device.VirtualOutput;

import javax.sound.sampled.*;
import java.util.ArrayList;

@Getter
public class Controller {

    @Getter @Setter private static Controller instance = new Controller();


    public static final int TOTAL_FRAME_SIZE = 4; /* bytes/frame | frame size */
    public static final int CHANNEL = 2;
    public static final int FRAME_SIZE = TOTAL_FRAME_SIZE / CHANNEL;

    public static final int BUFFER_SIZE = 1024 * 16; //17640
    public static final int SAMPLE_BUFFER_SIZE = BUFFER_SIZE / FRAME_SIZE;

    public static final AudioFormat FORMAT = new AudioFormat(
            AudioFormat.Encoding.PCM_SIGNED,
            44100, /* sample rate */
            16, /* bit | sample size */
            CHANNEL, /* stereo | channels */
            TOTAL_FRAME_SIZE, /* bytes/frame | frame size */
            44100, /* frames per second | frame rate */
            false /* little-endian */
    );


    public static final DataLine.Info INPUT_INFO = new DataLine.Info(TargetDataLine.class, Controller.FORMAT);
    public static final DataLine.Info OUTPUT_INFO = new DataLine.Info(SourceDataLine.class, Controller.FORMAT);

    private SoundUpdater updater = new SoundUpdater();

    private ArrayList<Channel> activeChannels = new ArrayList<>();
    private ArrayList<VirtualInput> activeInputs = new ArrayList<>();
    private ArrayList<VirtualOutput> activeOutputs = new ArrayList<>();

    private ArrayList<String> outputMixers = new ArrayList<>();
    private ArrayList<String> inputMixers = new ArrayList<>();

    public void init() {
        updateMixers();
        Channel channel = new Channel("test");
        VirtualInput input = new VirtualInput("input", "default [default]");
        VirtualOutput output = new VirtualOutput("output", "default [default]");
        channel.setVolume(1);
        output.setVolume(1);
        output.getChannels().add(channel);
        channel.getOutputs().add(output);
        channel.getInputs().add(input);
        activeChannels.add(channel);
        activeOutputs.add(output);
        activeInputs.add(input);
        System.out.println(input.openLine());
        System.out.println(output.openLine());
        /*for (Channel channel : Channel.FILE_SYSTEM.getEntries()) {
            for (String rawInput : channel.getInputNames()) {
                VirtualInput input = VirtualInput.FILE_SYSTEM.getEntry(rawInput);
                if (input.openLine()) {
                    channel.getInputs().add(input);
                    activeInputs.add(input);
                }
            }
            for (String rawOutput : channel.getOutputNames()) {
                VirtualOutput output = VirtualOutput.FILE_SYSTEM.getEntry(rawOutput);
                if (output.openLine()) {
                    channel.getOutputs().add(output);
                    output.getChannels().add(channel);
                    activeOutputs.add(output);
                }
            }
        }*/
    }

    public void startSync() {
        updater.start();
    }

    public void stop() {
        stopSync();
        for (VirtualInput input : activeInputs) {
            input.closeLine();
        }
        for (VirtualOutput output : activeOutputs) {
            output.closeLine();
        }
    }

    public void stopSync() {
        updater.setRunning(false);
        try {
            updater.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public Mixer getMixerByName(String name) {
        name = name.trim();
        for (Mixer.Info info : AudioSystem.getMixerInfo()) {
            if (info.getName().trim().equalsIgnoreCase(name)) {
                return AudioSystem.getMixer(info);
            }
        }
        return null;
    }

    private void updateMixers() {
        outputMixers.clear();
        inputMixers.clear();
        for (Mixer.Info info : AudioSystem.getMixerInfo()) {
            if (AudioSystem.getMixer(info).isLineSupported(OUTPUT_INFO)) {
                outputMixers.add(info.getName());
            }
        }
        for (Mixer.Info info : AudioSystem.getMixerInfo()) {
            if (AudioSystem.getMixer(info).isLineSupported(INPUT_INFO)) {
                inputMixers.add(info.getName());
            }
        }
    }

}
