package dimon.bot.listeners.music;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import lombok.SneakyThrows;

import java.util.concurrent.LinkedBlockingDeque;

public class TrackScheduler extends AudioEventAdapter {
    private final AudioPlayer player;
    private final LinkedBlockingDeque<AudioTrack> queue;

    public TrackScheduler(AudioPlayer player) {
        this.player = player;
        this.queue = new LinkedBlockingDeque<>();
    }

    public void queue(AudioTrack track){
        if (!player.startTrack(track, true)) {
            queue.offer(track);
        }
    }

    public void nextTrack(){
        player.startTrack(queue.poll(), false);
    }

    public void clearQueue(){
        player.stopTrack();
        queue.clear();
    }

    @Override
    public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason){
        if (endReason.mayStartNext) {
            nextTrack();
        }
    }

    @SneakyThrows
    public void hello(AudioTrack track){
        try{
            AudioTrack currentTrack = player.getPlayingTrack().makeClone();
            long position = player.getPlayingTrack().getPosition();
            player.stopTrack();
            currentTrack.setPosition(position);
            queue.addFirst(currentTrack);
        } catch (NullPointerException ignored){

        } finally {
            queue.addFirst(track);
            player.startTrack(queue.poll(), false);
        }
    }

}
