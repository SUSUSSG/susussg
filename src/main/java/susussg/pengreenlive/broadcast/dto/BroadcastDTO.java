package susussg.pengreenlive.broadcast.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Blob;
import java.util.Date;


@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BroadcastDTO {
    //방송
    private int broadcastSeq;
    private String channelNm;
    private String broadcastTitle;
    private Blob broadcastImage;
    private String broadcastSummary;
    private Date broadcastScheduledTime;
    private String categoryCd;
}