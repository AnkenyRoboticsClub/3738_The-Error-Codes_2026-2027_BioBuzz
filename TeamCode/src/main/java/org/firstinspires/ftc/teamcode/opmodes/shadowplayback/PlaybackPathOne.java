package org.firstinspires.ftc.teamcode.opmodes.shadowplayback;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;

@Autonomous(group = "Path Playback")
public class PlaybackPathOne extends PlaybackPathBase {
    // Changes ("overrides") function getShadowFileName
    @Override
    protected String getShadowFileName() {
        // Give the desired file name
        return "biobuzz_path.csv";
    }
}
