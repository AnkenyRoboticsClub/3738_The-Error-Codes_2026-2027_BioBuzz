package org.firstinspires.ftc.teamcode.opmodes.shadowrecord;

import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

@TeleOp(group = "Path Recording")
public class RecordPathOne extends RecordPathBase {
  // Changes ("overrides") function getShadowFileName
  @Override
  protected String getShadowFileName() {
    // Give the desired file name
    return "biobuzz_path.csv";
  }
}
